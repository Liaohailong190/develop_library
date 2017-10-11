package org.liaohailong.library.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.DrawableRes;
import android.support.annotation.Size;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import org.liaohailong.library.RootApplication;
import org.liaohailong.library.util.Md5Util;
import org.liaohailong.library.util.Utility;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 图片加载器
 * 1，加载网络图片
 * 2，加载SD卡图片
 * Created by LHL on 2017/9/10.
 */

public class ImageLoader {
    private static final int TIME_OUT = 1000 * 15;
    private static final String TAG = "ImageLoader";
    private static final String HTTP = "http";
    private static final String SD_CARD = "/storage/emulated/0";

    private ImageLoader() {

    }

    private static final class SingletonHolder {
        private static final ImageLoader INSTANCE = new ImageLoader();
    }

    public static ImageLoader getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private ImageConfig config = new ImageConfig();

    private static final int THREAD_POOL_SIZE = 5;
    private static final Map<String, ExecutorService> EXECUTOR_POOL_MAP = new HashMap<>();
    private static final Handler HANDLER = new ImageHandler();
    private static final int BUFF_SIZE = 8192;//文件IO buff字节数

    public ImageConfig getConfig() {
        return config;
    }

    private static final BitmapLruCache CACHE = new BitmapLruCache();

    public void downloadOnly(String url, ImageLoaderCallback callback) {
        downloadOnly(url, 0, 0, callback);
    }

    public void downloadOnly(String url, int scaleWidth, int scaleHeight, ImageLoaderCallback callback) {
        setImage(null, url, 0, scaleWidth, scaleHeight, callback);
    }

    public void setImage(ImageView imageView, String url) {
        setImage(imageView, url, 0);
    }

    public void setImage(ImageView imageView, String url, @DrawableRes int placeHolder) {
        setImage(imageView, url, placeHolder, null);
    }

    public void setImage(ImageView imageView, String url, @DrawableRes int placeHolder, ImageLoaderCallback callback) {
        int defaultWidth = config.getDefaultWidth();
        int defaultHeight = config.getDefaultHeight();
        setImage(imageView, url, placeHolder, defaultWidth, defaultHeight, callback);
    }

    public void setImage(ImageView imageView, String url, @DrawableRes int placeHolder, int scaleWidth, int scaleHeight, ImageLoaderCallback callback) {
        //先从内存缓存中读取
        if (getBitmapFromLruCache(imageView, url, scaleWidth, scaleHeight, callback)) {
            return;
        }
        setPlaceHolder(imageView, placeHolder);
        //再从硬盘缓存读取
        if (getBitmapFromDiskCache(imageView, url, scaleWidth, scaleHeight, callback)) {
            return;
        }
        //从网络下载
        if (url.startsWith(HTTP)) {
            getBitmapFromHttp(imageView, url, scaleWidth, scaleHeight, callback);
        }
    }

    private void setPlaceHolder(ImageView imageView, @DrawableRes int placeHolder) {
        if (imageView == null || placeHolder < 0) {
            return;
        }
        try {
            Drawable placeHolderDrawable = RootApplication.getInstance().getResources().getDrawable(placeHolder);
            if (placeHolderDrawable != null) {
                imageView.setImageDrawable(placeHolderDrawable);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private boolean getBitmapFromLruCache(ImageView imageView, String url, int scaleWidth, int scaleHeight, ImageLoaderCallback callback) {
        String fileName = getFileName(url, scaleWidth, scaleHeight);
        Bitmap bitmap = CACHE.get(fileName);
        if (bitmap != null) {
            done(fileName, bitmap, imageView, callback);
            return true;
        }
        return false;
    }

    private synchronized boolean getBitmapFromDiskCache(ImageView imageView, String url, int scaleWidth, int scaleHeight, ImageLoaderCallback callback) {
        File diskBitmap = null;
        //先判断路径类型
        if (url.startsWith(HTTP)) {
            diskBitmap = getSavePath(url, scaleWidth, scaleHeight);
        } else if (url.startsWith(SD_CARD)) {
            diskBitmap = new File(url);
        }
        if (diskBitmap == null) {
            return false;
        }
        //判断是否命中本地SD卡缓存
        if (diskBitmap.exists() && diskBitmap.isFile()) {
            DiskRunnable diskRunnable = new DiskRunnable(imageView, url, diskBitmap, scaleWidth, scaleHeight, callback, getConfig());
            submitTask(imageView, diskRunnable);
            return true;
        }
        return false;
    }

    private synchronized void getBitmapFromHttp(ImageView imageView, String url, int scaleWidth, int scaleHeight, ImageLoaderCallback callback) {
        HttpRunnable httpRunnable = new HttpRunnable(imageView, url, getSavePath(url, scaleWidth, scaleHeight), scaleWidth, scaleHeight, callback, getConfig());
        submitTask(imageView, httpRunnable);
    }

    /**
     * 获取本地缓存图片路径
     *
     * @param url         网络图片地址
     * @param scaleWidth  裁剪宽度
     * @param scaleHeight 裁剪高度
     * @return 本地缓存文件
     */
    private File getSavePath(String url, int scaleWidth, int scaleHeight) {
        File cacheDirectory = config.getCacheDirectory();
        String fileName = getFileName(url, scaleWidth, scaleHeight);
        return new File(cacheDirectory, fileName);
    }

    /**
     * 根据不同的图片要求尺寸获取文件名称
     *
     * @param url         网络图片地址
     * @param scaleWidth  裁剪宽度
     * @param scaleHeight 裁剪高度
     * @return 本地缓存文件名称
     */
    private String getFileName(String url, int scaleWidth, int scaleHeight) {
        return Md5Util.MD5Encode(url) + "?width=" + scaleWidth + "&height=" + scaleHeight;
    }

    private static BitmapFactory.Options getBitmapOption(byte[] bytes, int w, int h) {
        // 创建 Bitmap 对象
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options.inScaled = false;
        BitmapFactory.decodeStream(new ByteArrayInputStream(bytes), null, options);

        int orgWidth = options.outWidth;
        int orgHeight = options.outHeight;

        int sampleSize1 = (int) Math.ceil((float) orgWidth / (float) w);
        int sampleSize2 = (int) Math.ceil((float) orgHeight / (float) h);

        int max = Math.max(sampleSize1, sampleSize2);
        if (max > 0) {
            options.inSampleSize = max;
            options.inJustDecodeBounds = false;
            // noinspection deprecation
            options.inPurgeable = true;
            // noinspection deprecation
            options.inInputShareable = true;
            return options;
        }

        return null;
    }

    private static BitmapFactory.Options getBitmapOption(String file, int w, int h) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options.inScaled = false;
        BitmapFactory.decodeFile(file, options);

        int orgWidth = options.outWidth;
        int orgHeight = options.outHeight;

        int sampleSize1 = (int) Math.ceil((float) orgWidth / (float) w);
        int sampleSize2 = (int) Math.ceil((float) orgHeight / (float) h);

        int max = Math.max(sampleSize1, sampleSize2);
        if (max > 0) {
            options.inSampleSize = max;
            options.inJustDecodeBounds = false;
            // noinspection deprecation
            options.inPurgeable = true;
            // noinspection deprecation
            options.inInputShareable = true;
            return options;
        }

        return null;
    }

    private void submitTask(View view, Runnable runnable) {
        String cxt = view.getContext().toString();
        ExecutorService executorService = EXECUTOR_POOL_MAP.get(cxt);
        if (executorService == null) {
            executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
            EXECUTOR_POOL_MAP.put(cxt, executorService);
        }
        executorService.execute(runnable);
    }

    private static void done(String fileName, Bitmap bitmap, ImageView imageView, ImageLoaderCallback callback) {
        if (bitmap == null || fileName == null) {
            return;
        }
        //完成任务之后首先进行内存缓存
        if (!CACHE.containsKey(fileName)) {
            CACHE.put(fileName, bitmap);
        }
        //设置图片
        if (imageView != null) {
            imageView.setImageBitmap(bitmap);
        }
        //回调接口
        if (callback != null) {
            callback.onImageLoadComplete(fileName, bitmap, imageView);
        }
    }

    /**
     * 根据Activity清理Activity中加载的图片
     * 必须在主线程调用
     */
    public synchronized void clear(Context context) {
        Utility.checkMain();
        String key = context.toString();
        if (EXECUTOR_POOL_MAP.containsKey(key)) {
            EXECUTOR_POOL_MAP.get(key).shutdownNow();
            EXECUTOR_POOL_MAP.remove(key);
        }
    }

    private static class ImageHandler extends Handler {
        private static final int DISK_LOAD_COMPLETE = 0;//硬盘读取完毕
        private static final int HTTP_LOAD_COMPLETE = 1;//网络下载完毕

        @Override
        public void handleMessage(Message msg) {
            Holder holder = (Holder) msg.obj;
            switch (msg.what) {
                case DISK_LOAD_COMPLETE:
                    done(holder.fileName, holder.bitmap, holder.imageView, holder.callback);
                    break;
                case HTTP_LOAD_COMPLETE:
                    done(holder.fileName, holder.bitmap, holder.imageView, holder.callback);
                    break;
            }
        }
    }

    /**
     * 从硬盘读取图片任务
     */
    private static class DiskRunnable implements Runnable {
        private WeakReference<ImageView> imageViewWeakReference;
        private WeakReference<String> urlWeakReference;
        private WeakReference<File> fileWeakReference;
        private WeakReference<Integer> widthWeakReference;
        private WeakReference<Integer> heightWeakReference;
        private WeakReference<ImageLoaderCallback> callbackWeakReference;
        private WeakReference<ImageConfig> configWeakReference;

        private DiskRunnable(ImageView imageView, String url, File file, int scaleWidth, int scaleHeight, ImageLoaderCallback callback, ImageConfig config) {
            imageViewWeakReference = new WeakReference<>(imageView);
            urlWeakReference = new WeakReference<>(url);
            fileWeakReference = new WeakReference<>(file);
            widthWeakReference = new WeakReference<>(scaleWidth);
            heightWeakReference = new WeakReference<>(scaleHeight);
            callbackWeakReference = new WeakReference<>(callback);
            configWeakReference = new WeakReference<>(config);
        }

        @Override
        public void run() {
            ImageView imageView = imageViewWeakReference.get();
            String url = urlWeakReference.get();
            File file = fileWeakReference.get();
            Integer width = widthWeakReference.get();
            Integer height = heightWeakReference.get();
            ImageLoaderCallback callback = callbackWeakReference.get();
            ImageConfig config = configWeakReference.get();
            if (file == null || config == null || url == null || width < 1 || height < 1) {
                return;
            }
            getImage(imageView, url, file, width, height, callback, config);
        }

        protected void getImage(ImageView imageView, String url, File file, Integer width, Integer height, ImageLoaderCallback callback, ImageConfig config) {
            String path = file.getAbsolutePath();
            BitmapFactory.Options options = getBitmapOption(path, width, height);

            FileInputStream fis = null;
            Bitmap bitmap = null;
            try {
                fis = new FileInputStream(file);
                bitmap = BitmapFactory.decodeStream(fis, null, options);
            } catch (Exception e) {
                e.printStackTrace();
                if (config.isWriteLog()) {
                    Log.i(TAG, "读取硬盘缓存图片异常 exception = " + e.toString());
                }
            } finally {
                Utility.close(fis);
            }

            if (bitmap == null) {
                return;
            }
            send2Handler(imageView, url, bitmap, callback, path, ImageHandler.DISK_LOAD_COMPLETE);
        }

        protected void send2Handler(ImageView imageView, String url, Bitmap bitmap, ImageLoaderCallback callback, String path, int flag) {
            Holder holder = new Holder();
            holder.imageView = imageView;
            holder.url = url;
            holder.callback = callback;
            String[] split = path.split("/");
            holder.fileName = split[split.length - 1];
            holder.bitmap = bitmap;
            Message message = HANDLER.obtainMessage();
            message.what = flag;
            message.obj = holder;
            message.sendToTarget();
        }
    }

    /**
     * 从网络加载图片
     */
    private static class HttpRunnable extends DiskRunnable {
        private static final String PNG = "image/png";
        private static final String JPG = "image/jpeg";

        private HttpRunnable(ImageView imageView, String url, File file, int scaleWidth, int scaleHeight, ImageLoaderCallback callback, ImageConfig config) {
            super(imageView, url, file, scaleWidth, scaleHeight, callback, config);
        }

        @Override
        protected void getImage(ImageView imageView, String url, File file, Integer width, Integer height, ImageLoaderCallback callback, ImageConfig config) {
            String path = file.getAbsolutePath();
            Bitmap bitmap = null;
            String[] mimeTypeHolder = new String[1];
            try {
                byte[] data = getRawDataFromHttp(url, mimeTypeHolder);
                if (data == null) {
                    return;
                }
                BitmapFactory.Options options = getBitmapOption(data, width, height);
                if (options == null) {
                    bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                } else {
                    bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (bitmap == null) {
                return;
            }
            //保存图片至本地
            save2Disk(bitmap, file, mimeTypeHolder[0]);
            send2Handler(imageView, url, bitmap, callback, path, ImageHandler.HTTP_LOAD_COMPLETE);
        }

        private byte[] getRawDataFromHttp(String url, @Size(1) String[] mimeTypeHolder) throws InterruptedIOException {
            byte[] rawData = null;
            InputStream inputStream = null;
            ByteArrayOutputStream outputStream = null;
            HttpURLConnection connection = null;

            if (TextUtils.isEmpty(url)) {
                return null;
            }

            try {
                URL uri = new URL(url);
                connection = (HttpURLConnection) uri.openConnection();

                connection.setDoOutput(false);
                String type = "GET";
                connection.setRequestMethod(type);
                connection.setUseCaches(true);
                connection.setConnectTimeout(TIME_OUT);
                connection.setReadTimeout(TIME_OUT);
                int code = connection.getResponseCode();
                if (code != HttpURLConnection.HTTP_OK) {
                    return null;
                }
                mimeTypeHolder[0] = connection.getContentType();
                inputStream = connection.getInputStream();
                outputStream = new ByteArrayOutputStream();

                rawData = inputStreamToData(inputStream, outputStream);
                outputStream.close();
                inputStream.close();
                connection.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                Utility.close(outputStream);
                Utility.close(inputStream);
                if (connection != null) {
                    connection.disconnect();
                }
            }
            return rawData;
        }

        private byte[] inputStreamToData(InputStream inputStream, ByteArrayOutputStream cache) throws IOException {
            int ch;
            byte[] buf = new byte[BUFF_SIZE];
            while ((ch = inputStream.read(buf)) != -1) {
                // 这些IO操作都无视Thread的interrupt（不产生IOException），所以还是自己来吧
                if (Thread.interrupted()) {
                    throw new InterruptedIOException("task cancel");
                }
                cache.write(buf, 0, ch);
            }

            return cache.toByteArray();
        }

        private void save2Disk(Bitmap bitmap, File file, String mimeType) {
            if (file.exists()) {
                if (file.isFile()) {
                    return;
                }
                boolean delete = file.delete();
                if (!delete) {
                    return;
                }
            }
            final int quality = 100;
            BufferedOutputStream bos = null;
            try {
                bos = new BufferedOutputStream(new FileOutputStream(file), BUFF_SIZE);

                if (TextUtils.equals(mimeType, PNG)) {
                    bitmap.compress(Bitmap.CompressFormat.PNG, quality, bos);
                } else {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, quality, bos);
                }
                bos.flush();
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                Utility.close(bos);
            }
        }
    }

    private static class Holder {
        private ImageView imageView;
        private String url;
        private Bitmap bitmap;
        private String fileName;
        private ImageLoaderCallback callback;
    }
}
