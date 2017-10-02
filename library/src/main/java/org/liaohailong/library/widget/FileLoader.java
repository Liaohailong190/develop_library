package org.liaohailong.library.widget;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import org.liaohailong.library.RootApplication;
import org.liaohailong.library.util.FileUtil;
import org.liaohailong.library.util.Md5Util;
import org.liaohailong.library.util.Utility;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * 文件上传/下载
 * Created by LHL on 2017/8/8.
 */

public class FileLoader {
    private static final String NAME = "cache_files";
    private static final int FILE_LOADING = 0;//视频下载中
    private static final int FILE_DOWN_LOAD_COMPLETE = 1;//文件下载完成
    private static final int FILE_UP_LOAD_COMPLETE = 2;//文件上传完成
    private static final int FILE_LOAD_FAILURE = 3;//文件下载/上传失败
    private static final int THREAD_POOL_SIZE = 5;
    private String directory;
    private static final String TEMP = ".temp";
    private Map<String, OnFileStatusCallBack> callBackMap = new HashMap<>();
    private Map<String, Future<?>> taskMap = new HashMap<>();
    private final ExecutorService executor;
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
        directory = FileUtil.getRootDirectory(NAME);
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

    public boolean loadFile(String url, OnFileStatusCallBack callBack) {
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

    public void upLoadFile(String url, String fileKey, File file, OnFileStatusCallBack callBack) {
        //网络上传
        if (taskMap.get(url) == null) {
            UpLoadFileRunnable runnable = new UpLoadFileRunnable(url, fileKey, file);
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
        return directory + Md5Util.MD5Encode(url);
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
            try {
                HttpURLConnection urlConnection = (HttpURLConnection) new URL(url).openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setConnectTimeout(10 * 1000);
                urlConnection.setRequestProperty("Accept-Encoding", "identity");
                int responseCode = urlConnection.getResponseCode();
                if (responseCode >= 200 && responseCode < 300) {
                    int maxProgress = urlConnection.getContentLength();//获取文件
                    //检查用户是否已经删除了临时下载文件
                    String tempPath = getTempPath(url);
                    boolean loaded = isLoaded(tempPath);
                    if (!loaded) {
                        setFileLoadingSize(url, 0);
                    }
                    int lastPosition = getFileLoadingSize(url);
                    urlConnection.disconnect();
                    downLoad(lastPosition, maxProgress);
                } else {
                    urlConnection.disconnect();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Status status = new Status();
                status.result = e.toString();
                status.url = url;
                Message message = handler.obtainMessage();
                message.what = FILE_LOAD_FAILURE;
                message.obj = status;
                message.sendToTarget();
            }
        }

        private void downLoad(int lastPosition, int maxLength) {
            InputStream inputStream = null;
            RandomAccessFile randomAccessFile = null;
            Status status = null;
            try {
                HttpURLConnection urlConnection = (HttpURLConnection) new URL(url).openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setConnectTimeout(10 * 1000);
                urlConnection.setRequestProperty("Accept-Encoding", "identity");
                urlConnection.setRequestProperty("Accept-Ranges", "bytes");
                urlConnection.setRequestProperty("Range", "bytes=" + lastPosition + "-" + maxLength);
                int code = urlConnection.getResponseCode();
                if (code >= HttpURLConnection.HTTP_BAD_REQUEST) {
                    return;
                }
                switch (code) {
                    case HttpURLConnection.HTTP_PARTIAL://请求部分网络成功
                        break;
                    case HttpURLConnection.HTTP_OK://请求网络成功
                        lastPosition = 0;//请求部分网络失败
                        break;
                }
                inputStream = urlConnection.getInputStream();
                String tempPath = getTempPath(url);
                File file = new File(tempPath);
                FileUtil.createFileIfMissed(file);
                status = new Status();
                randomAccessFile = new RandomAccessFile(file, "rwd");
                randomAccessFile.seek(lastPosition);
                byte[] buffer = new byte[1024 * 1024 * 2];
                int len;
                int total = 0;
                while ((len = inputStream.read(buffer)) != -1) {
                    total += len;
                    randomAccessFile.write(buffer, 0, len);
                    int progress = total + lastPosition;
                    int percent = (int) (((progress * 1f) / (maxLength * 1f)) * 100f);
                    //正在下载
                    Message message = handler.obtainMessage();
                    message.what = FILE_LOADING;
                    status.url = url;
                    status.tempPath = tempPath;
                    status.progress = percent;
                    message.obj = status;
                    message.sendToTarget();
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
                Message message = handler.obtainMessage();
                message.what = FILE_DOWN_LOAD_COMPLETE;
                status.url = url;
                status.progress = 100;
                status.path = path;
                message.obj = status;
                message.sendToTarget();
                urlConnection.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
                if (status == null) {
                    status = new Status();
                }
                status.result = e.toString();
                status.url = url;
                Message message = handler.obtainMessage();
                message.what = FILE_LOAD_FAILURE;
                message.obj = status;
                message.sendToTarget();
            } finally {
                Utility.close(inputStream);
                Utility.close(randomAccessFile);
            }
        }
    }

    /**
     * 文件上传
     */
    private class UpLoadFileRunnable implements Runnable {
        private String uploadUrl = "";
        private File file = null;
        private String key = "";

        private UpLoadFileRunnable(@NonNull String uploadUrl, @NonNull String key, @NonNull File file) {
            this.uploadUrl = uploadUrl;
            this.file = file;
            this.key = key;
        }

        @Override
        public void run() {
            DataOutputStream dos = null;
            InputStream is = null;
            Status status = null;
            try {
                String end = "\r\n";
                String twoHyphens = "--";
                String boundary = UUID.randomUUID().toString(); // 边界标识 随机生成
                String CONTENT_TYPE = "multipart/form-data"; // 内容类型
                URL url = new URL(uploadUrl);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url
                        .openConnection();
                httpURLConnection.setConnectTimeout(10 * 1000);
                httpURLConnection.setReadTimeout(10 * 1000);
                httpURLConnection.setChunkedStreamingMode(128 * 1024);// 128K
                // 允许输入输出流
                httpURLConnection.setDoInput(true);// 允许输入流
                httpURLConnection.setDoOutput(true);// 允许输出流
                httpURLConnection.setUseCaches(false);// 不允许使用缓存
                // 使用POST方法
                httpURLConnection.setRequestMethod("POST");// 请求方式
                httpURLConnection.setRequestProperty("Connection", "Keep-Alive");
                httpURLConnection.setRequestProperty("Charset", "UTF-8");// 设置编码
                httpURLConnection.setRequestProperty("Content-Type",
                        CONTENT_TYPE + ";boundary=" + boundary);

                dos = new DataOutputStream(
                        httpURLConnection.getOutputStream());
                dos.writeBytes(twoHyphens + boundary + end);
                /*
                 * 这里重点注意： name里面的值为服务器端需要key 只有这个key 才可以得到对应的文件
                 * filename是文件的名字，包含后缀名
                 */
                dos.writeBytes("Content-Disposition: form-data; name=\"" + key + "\"; filename=\""
                        + file.getName() + "\"" + end);
                dos.writeBytes(end);

                status = new Status();
                FileInputStream fis = new FileInputStream(file);
                long length = file.length();
                int outLength = 0;
                byte[] buffer = new byte[8192]; // 8k
                int count;
                // 读取文件
                while ((count = fis.read(buffer)) != -1) {
                    dos.write(buffer, 0, count);
                    outLength += count;
                    int percent = (int) ((outLength / length) * 100);
                    //正在上传
                    Message message = handler.obtainMessage();
                    message.what = FILE_LOADING;
                    status.url = uploadUrl;
                    status.progress = percent;
                    message.obj = status;
                    message.sendToTarget();
                    //手动打断线程执行
                    boolean interrupted = Thread.interrupted();
                    if (interrupted) {
                        return;
                    }
                }
                fis.close();
                dos.writeBytes(end);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + end);
                dos.flush();
                int code = httpURLConnection.getResponseCode();
                if (code < 400) {
                    is = httpURLConnection.getInputStream();
                } else {
                    is = httpURLConnection.getErrorStream();
                }
                String result = Utility.streamToString(is);
                //上传完毕
                Message message = handler.obtainMessage();
                message.what = FILE_UP_LOAD_COMPLETE;
                status.url = uploadUrl;
                status.progress = 100;
                status.result = result;
                message.obj = status;
                message.sendToTarget();
                dos.close();
                is.close();
            } catch (Exception e) {
                e.printStackTrace();
                if (status == null) {
                    status = new Status();
                }
                status.result = e.toString();
                status.url = uploadUrl;
                Message message = handler.obtainMessage();
                message.what = FILE_LOAD_FAILURE;
                message.obj = status;
                message.sendToTarget();
            } finally {
                Utility.close(dos);
                Utility.close(is);
            }
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
    private static synchronized void setFileLoadingSize(String url, int size) {
        sp.edit().putInt(url, size).apply();
    }

    /**
     * @param url 文件下载链接（标识）
     * @return 文件已下载大小
     */
    private static synchronized int getFileLoadingSize(String url) {
        return sp.getInt(url, 0);
    }
}
