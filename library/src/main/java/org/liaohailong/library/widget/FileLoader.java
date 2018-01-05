package org.liaohailong.library.widget;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;

import org.liaohailong.library.RootApplication;
import org.liaohailong.library.http.ProgressRequestBody;
import org.liaohailong.library.util.FileUtil;
import org.liaohailong.library.util.Md5Util;
import org.liaohailong.library.util.Utility;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * 文件上传/下载
 * Created by LHL on 2017/8/8.
 */

public class FileLoader {
    private static final String NAME = "victor";
    private static final int FILE_LOADING = 0;//视频下载中
    private static final int FILE_DOWN_LOAD_COMPLETE = 1;//文件下载完成
    private static final int FILE_UP_LOAD_COMPLETE = 2;//文件上传完成
    private static final int FILE_LOAD_FAILURE = 3;//文件下载/上传失败
    private static final int THREAD_POOL_SIZE = 5;
    private String directory;
    private static final String TEMP = ".temp";
    private Map<String, OnFileStatusCallBack> callBackMap = new HashMap<>();
    private Map<String, Future<?>> taskMap = new HashMap<>();
    private final OkHttpClient client;
    private final ExecutorService executor;
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Status status = (Status) msg.obj;
            OnFileStatusCallBack callBack = callBackMap.get(status.url);
            if (callBack == null) {
                return;
            }
            switch (msg.what) {
                case FILE_LOADING://加载中
                    callBack.onFileLoading(status.tempPath, status.progress);
                    break;
                case FILE_DOWN_LOAD_COMPLETE://下载完成
                    callBack.onFileDownLoadComplete(status.path);
                    callBackMap.remove(status.url);
                    break;
                case FILE_UP_LOAD_COMPLETE://上传完毕
                    callBack.onFileUpLoadComplete(status.result);
                    callBackMap.remove(status.url);
                    break;
                case FILE_LOAD_FAILURE://失败了
                    callBack.onFileLoadFailure(status.result);
                    callBackMap.remove(status.url);
                    break;
            }
        }
    };

    private FileLoader() {
        executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        client = new OkHttpClient();
        initClient();
        directory = FileUtil.getRootDirectory(NAME);
    }

    //设置超时，不设置可能会报异常
    private void initClient() {
        client.setConnectTimeout(15, TimeUnit.MINUTES);
        client.setReadTimeout(15, TimeUnit.MINUTES);
        client.setWriteTimeout(15, TimeUnit.MINUTES);
    }


    private static class SingletonHolder {
        static final FileLoader INSTANCE = new FileLoader();
    }

    public static FileLoader getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * @param path 设置文件缓存路径，最好是在Application中设置好
     */
    public void initDirectory(String path) {
        if (TextUtils.isEmpty(path)) {
            return;
        }
        directory = path;
    }

    public boolean downloadFile(String url, OnFileStatusCallBack callBack) {
        String path = getPath(url);
        boolean saved = canFromDisk(path);
        //命中本地缓存
        if (saved) {
            if (callBack != null) {
                callBack.onFileDownLoadComplete(path);
            }
            return true;
        }
        //从网络下载
        if (taskMap.get(url) == null) {
            DownLoadFileRunnable runnable = new DownLoadFileRunnable(url);
            callBackMap.put(url, callBack);
            Future<?> submit = executor.submit(runnable);
            taskMap.put(url, submit);
        }
        return false;
    }

    public void upLoadFile(String url, Map<String, Object> params, OnFileStatusCallBack callBack) {
        //网络上传
        if (taskMap.get(url) == null) {
            UpLoadFileRunnable runnable = new UpLoadFileRunnable(url, params);
            callBackMap.put(url, callBack);
            Future<?> submit = executor.submit(runnable);
            taskMap.put(url, submit);
        }
    }

    /**
     * 获取本地File文件
     *
     * @param url 网络视频链接地址
     * @return 本地缓存文件地址
     */
    private String getPath(String url) {
        String path = directory + Md5Util.MD5Encode(url);
        //尽量添加后缀
        if (url.contains("/")) {
            String[] urlSplit = url.split("/");
            String name = urlSplit[urlSplit.length - 1];
            if (name.contains(".")) {
                String[] nameSplit = name.split("\\.");
                path = path + "." + nameSplit[nameSplit.length - 1];
            }
        }
        return path;
    }

    /**
     * 获取正在需要下载的视频路径
     *
     * @param url 网络视频链接地址
     * @return 本地缓存临时文件地址
     */
    private String getTempPath(String url) {
        return getPath(url) + TEMP;
    }

    /**
     * @param path 本地视频路径
     * @return 是否已经存在本地
     */
    private boolean canFromDisk(String path) {
        File file = new File(path);
        boolean exists = file.exists();
        boolean isFile = file.isFile();
        boolean canRead = file.canRead();
        return exists && isFile && canRead;
    }

    /**
     * @param tempPath 临时下载视频文件
     * @return 是否已经存在临时文件
     */
    private boolean isLoaded(String tempPath) {
        File file = new File(tempPath);
        return file.exists() && file.isFile();
    }

    /**
     * 终止任务，必须在主线程调用
     */
    public void clearAllTask() {
        callBackMap.clear();
        for (Map.Entry<String, Future<?>> entry : taskMap.entrySet()) {
            Future<?> value = entry.getValue();
            value.cancel(true);
        }
        taskMap.clear();
    }

    /**
     * 终止任务，必须在主线程调用
     *
     * @param url 网络地址
     */
    public void clearTask(String url) {
        callBackMap.remove(url);
        Future<?> future = taskMap.get(url);
        if (future != null) {
            boolean cancel = future.cancel(true);
            taskMap.remove(url);
        }
    }

    private void onFileLoading(Status status) {
        //正在下载
        Message message = handler.obtainMessage();
        message.what = FILE_LOADING;
        message.obj = status;
        message.sendToTarget();
    }

    private void onFileComplete(Status status) {
        //视频下载完毕
        Message message = handler.obtainMessage();
        message.what = FILE_DOWN_LOAD_COMPLETE;
        message.obj = status;
        message.sendToTarget();
    }

    private void onFileLoadFailure(String url, String info) {
        //下载失败
        Status status = new Status();
        status.result = info;
        status.url = url;
        Message message = handler.obtainMessage();
        message.what = FILE_LOAD_FAILURE;
        message.obj = status;
        message.sendToTarget();
    }

    /**
     * 下载网络视频
     */
    private class DownLoadFileRunnable implements Runnable {
        private String url;

        private DownLoadFileRunnable(String url) {
            this.url = url;
        }

        @Override
        public void run() {
            Request build = new Request.Builder()
                    .url(url)
                    .get()
                    .build();
            Call call = client.newCall(build);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    onFileLoadFailure(url, e.toString());
                }

                @Override
                public void onResponse(Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        return;
                    }
                    ResponseBody body = response.body();
                    long contentLength = body.contentLength();
                    //检查用户是否已经删除了临时下载文件
                    String tempPath = getTempPath(url);
                    boolean loaded = isLoaded(tempPath);
                    if (!loaded) {
                        setFileLoadingSize(url, 0);
                    }
                    long lastPosition = getFileLoadingSize(url);
                    downLoad(lastPosition, contentLength);
                    body.close();
                }
            });
        }

        private void downLoad(final long lastPosition, final long maxLength) {
            Request build = new Request.Builder()
                    .url(url)
                    .addHeader("Accept-Encoding", "identity")//断点下载需要
                    .addHeader("Accept-Ranges", "bytes")//断点下载需要
                    .addHeader("Range", "bytes=" + lastPosition + "-" + maxLength)//断点下载需要
                    .get()
                    .build();
            Call call = client.newCall(build);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    onFileLoadFailure(url, e.toString());
                }

                @Override
                public void onResponse(Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        return;
                    }
                    int code = response.code();
                    long lastPos = lastPosition;
                    switch (code) {
                        case HttpURLConnection.HTTP_PARTIAL://请求部分网络成功
                            break;
                        case HttpURLConnection.HTTP_OK://请求网络成功
                            lastPos = 0;//请求部分网络失败
                            break;
                    }
                    ResponseBody body = response.body();
                    InputStream inputStream = null;
                    RandomAccessFile randomAccessFile = null;
                    try {
                        inputStream = body.byteStream();
                        String tempPath = getTempPath(url);
                        File file = new File(tempPath);
                        FileUtil.createFileIfMissed(file);
                        Status status = new Status();
                        randomAccessFile = new RandomAccessFile(file, "rwd");
                        randomAccessFile.seek(lastPos);
                        byte[] buffer = new byte[1024 * 1024 * 2];
                        int len;
                        long total = 0;
                        while ((len = inputStream.read(buffer)) != -1) {
                            total += len;
                            randomAccessFile.write(buffer, 0, len);
                            long progress = total + lastPos;
                            int percent = (int) (((progress * 1f) / (maxLength * 1f)) * 100f);
                            status.url = url;
                            status.tempPath = tempPath;
                            status.progress = percent;
                            onFileLoading(status);
                            //本地记录文件下载量
                            setFileLoadingSize(url, progress);
                            //手动打断线程执行
                            boolean interrupted = Thread.interrupted();
                            if (interrupted) {
                                return;
                            }
                        }
                        String path = getPath(url);
                        FileUtil.renameTo(file, new File(path));
                        //视频下载完毕
                        status.url = url;
                        status.progress = 100;
                        status.path = path;
                        onFileComplete(status);
                        body.close();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    } finally {
                        Utility.close(inputStream);
                        Utility.close(randomAccessFile);
                        body.close();
                    }
                }
            });
        }
    }

    /**
     * 文件上传
     */
    private class UpLoadFileRunnable implements Runnable {
        private String uploadUrl = "";
        private Map<String, Object> params;

        private UpLoadFileRunnable(@NonNull String uploadUrl, @NonNull Map<String, Object> params) {
            this.uploadUrl = uploadUrl;
            this.params = params;
        }

        @Override
        public void run() {
            if (TextUtils.isEmpty(uploadUrl) || params == null || params.isEmpty()) {
                return;
            }
            MultipartBuilder builder = new MultipartBuilder();
            builder.type(MultipartBuilder.FORM);
            //追加参数
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                if (value instanceof File) {
                    File file = (File) value;
                    builder.addFormDataPart(key, file.getName(), RequestBody.create(null, file));
                } else {
                    builder.addFormDataPart(key, value.toString());
                }
            }
            RequestBody body = builder.build();
            //上传文件进度监听
            ProgressRequestBody.OnProgressCallback onProgressCallback = new ProgressRequestBody.OnProgressCallback() {
                Status status = null;

                @Override
                public void onProgress(int progress) {
                    if (status == null) {
                        status = new Status();
                    }
                    //正在上传
                    status.url = uploadUrl;
                    status.tempPath = "";
                    status.progress = progress;
                    onFileLoading(status);
                }
            };
            body = new ProgressRequestBody(body, onProgressCallback);
            Request request = new Request.Builder()
                    .url(uploadUrl)
                    .post(body)
                    .build();
            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                Status status = null;

                @Override
                public void onFailure(Request request, IOException e) {
                    onFileLoadFailure(uploadUrl, e.toString());
                }

                @Override
                public void onResponse(Response response) throws IOException {
                    String result = response.body().string();
                    //上传完毕
                    status.url = uploadUrl;
                    status.progress = 100;
                    status.result = result;
                    onFileComplete(status);
                }
            });
        }
    }

    /**
     * 视频状态回调
     */
    public interface OnFileStatusCallBack {
        void onFileLoading(String tempFilePath, int progress);

        void onFileDownLoadComplete(String path);

        void onFileUpLoadComplete(String result);

        void onFileLoadFailure(String ex);
    }

    public static abstract class OnFileStatusCallBackAdapter implements OnFileStatusCallBack {

        @Override
        public void onFileLoading(String tempFilePath, int progress) {

        }

        @Override
        public void onFileDownLoadComplete(String path) {

        }

        @Override
        public void onFileUpLoadComplete(String result) {

        }

        @Override
        public void onFileLoadFailure(String ex) {

        }
    }


    private class Status {
        private String url = "";
        private int progress = 0;
        private String path = "";
        private String tempPath = "";
        private String result = "";
    }

    //存储相关
    private static final String SP_NAME = "file_loader";
    private static final SharedPreferences sp = RootApplication.getInstance().getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);

    /**
     * 获取文件下载大小
     *
     * @param url  文件下载链接（标识）
     * @param size 文件已下载大小
     */
    private static synchronized void setFileLoadingSize(String url, long size) {
        sp.edit().putLong(url, size).apply();
    }

    /**
     * @param url 文件下载链接（标识）
     * @return 文件已下载大小
     */
    private static synchronized long getFileLoadingSize(String url) {
        return sp.getLong(url, 0);
    }
}
