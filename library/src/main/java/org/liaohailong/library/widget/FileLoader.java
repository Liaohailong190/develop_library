package org.liaohailong.library.widget;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import org.liaohailong.library.RootApplication;
import org.liaohailong.library.http.ProgressRequestBody;
import org.liaohailong.library.util.FileUtil;
import org.liaohailong.library.util.Md5Util;
import org.liaohailong.library.util.Utility;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

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
    private String mDirectory;
    private static final String TEMP = ".temp";
    private Map<String, WeakReference<OnFileStatusCallBack>> mCallBackMap = new HashMap<>();
    private Map<String, WeakReference<Future<?>>> mTaskMap = new HashMap<>();
    private final OkHttpClient mClient;
    private final ExecutorService mExecutor;
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Status status = (Status) msg.obj;
            WeakReference<OnFileStatusCallBack> onFileStatusCallBackWeakReference = mCallBackMap.get(status.url);
            if (onFileStatusCallBackWeakReference == null) {
                return;
            }
            OnFileStatusCallBack callBack = onFileStatusCallBackWeakReference.get();
            if (callBack == null) {
                return;
            }
            switch (msg.what) {
                case FILE_LOADING://加载中
                    callBack.onFileLoading(status.tempPath, status.progress);
                    break;
                case FILE_DOWN_LOAD_COMPLETE://下载完成
                    callBack.onFileDownLoadComplete(status.path);
                    mCallBackMap.remove(status.url);
                    break;
                case FILE_UP_LOAD_COMPLETE://上传完毕
                    callBack.onFileUpLoadComplete(status.result);
                    mCallBackMap.remove(status.url);
                    break;
                case FILE_LOAD_FAILURE://失败了
                    callBack.onFileLoadFailure(status.result);
                    mCallBackMap.remove(status.url);
                    break;
            }
        }
    };

    private FileLoader() {
        mExecutor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        mClient = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.MINUTES)
                .readTimeout(15, TimeUnit.MINUTES)
                .writeTimeout(15, TimeUnit.MINUTES)
                .build();
        mDirectory = FileUtil.getRootDirectory(NAME);
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
        mDirectory = path;
    }

    /**
     * 下载文件
     *
     * @param url      文件链接
     * @param callBack 文件下载完毕回调
     */
    public void downloadFile(String url, OnFileStatusCallBack callBack) {
        String path = getPath(url);
        boolean saved = canFromDisk(path);
        //命中本地缓存
        if (saved) {
            if (callBack != null) {
                callBack.onFileDownLoadComplete(path);
            }
            return;
        }
        //从网络下载
        DownLoadFileRunnable runnable = new DownLoadFileRunnable(url);
        submit(url, callBack, runnable);
    }

    /**
     * 上传文件
     *
     * @param url      接收文件的api地址
     * @param params   文件上传的携带参数
     * @param callBack 文件上传状态回调s
     */
    public void upLoadFile(String url, Map<String, Object> params, OnFileStatusCallBack callBack) {
        //网络上传
        UpLoadFileRunnable runnable = new UpLoadFileRunnable(url, params);
        submit(url, callBack, runnable);
    }

    /**
     * 提交文件处理任务
     * 同时保证不提交重复任务
     *
     * @param url      文件下载/上传的路径
     * @param callBack 文件下载/上传的当前状态
     * @param runnable 执行任务
     */
    private void submit(String url, OnFileStatusCallBack callBack, Runnable runnable) {
        if (mTaskMap.get(url) == null) {
            WeakReference<OnFileStatusCallBack> onFileStatusCallBackWeakReference = new WeakReference<>(callBack);
            mCallBackMap.put(url, onFileStatusCallBackWeakReference);
            Future<?> submit = mExecutor.submit(runnable);
            WeakReference<Future<?>> futureWeakReference = new WeakReference<Future<?>>(submit);
            mTaskMap.put(url, futureWeakReference);
        }
    }

    /**
     * 获取本地File文件
     *
     * @param url 网络视频链接地址
     * @return 本地缓存文件地址
     */
    private String getPath(String url) {
        String path = mDirectory + Md5Util.MD5Encode(url);
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
     *
     * @param url 网络地址
     */
    public void clearTask(String url) {
        //必须在主线程调用
        Utility.checkMain();
        //参数为空表示清空所有
        if (TextUtils.isEmpty(url)) {
            mCallBackMap.clear();
            for (Map.Entry<String, WeakReference<Future<?>>> entry : mTaskMap.entrySet()) {
                WeakReference<Future<?>> value = entry.getValue();
                if (value != null && value.get() != null) {
                    value.get().cancel(true);
                }
            }
            mTaskMap.clear();
        } else {
            mCallBackMap.remove(url);
            WeakReference<Future<?>> futureWeakReference = mTaskMap.get(url);
            if (futureWeakReference != null && futureWeakReference.get() != null) {
                Future<?> future = futureWeakReference.get();
                boolean cancel = future.cancel(true);
                mTaskMap.remove(url);
            }
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
            Call call = mClient.newCall(build);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    onFileLoadFailure(url, e.toString());
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        return;
                    }
                    ResponseBody body = response.body();
                    if (body == null) {
                        onFileLoadFailure(url, "getContentLength ResponseBody = null");
                        return;
                    }
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
            Call call = mClient.newCall(build);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    onFileLoadFailure(url, e.toString());
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
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
                    InputStream inputStream = null;
                    RandomAccessFile randomAccessFile = null;
                    try (ResponseBody body = response.body()) {
                        if (body == null) {
                            onFileLoadFailure(url, "downLoad ResponseBody = null");
                            return;
                        }
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

            MultipartBody.Builder builder = new MultipartBody.Builder();
            builder.setType(MultipartBody.FORM);
            //追加参数
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                if (value instanceof File) {
                    File file = (File) value;
                    builder.addFormDataPart(key, file.getName(), RequestBody.create(MultipartBody.FORM, file));
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
            Call call = mClient.newCall(request);
            call.enqueue(new Callback() {
                Status status;

                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    onFileLoadFailure(uploadUrl, e.toString());
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    ResponseBody body = response.body();
                    if (body == null) {
                        onFileLoadFailure(uploadUrl, "up load ResponseBody = null");
                        return;
                    }
                    if (status == null) {
                        status = new Status();
                    }
                    String result = body.string();
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
