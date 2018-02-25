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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 图片加载器
 * 1，加载网络图片
 * 2，加载SD卡图片
 * Created by LHL on 2017/9/10.
 */

public enum ImageLoader {
    /*
    * 枚举维护单例：
    * 1，防止反射实例化。
    * 2，防止序列化、反序列化过程中实例化。
    * 3，线程安全。
    * */
    instance;

    private static final int TIME_OUT = 1000 * 15;
    private static final String TAG = "ImageLoader";
    private static final String HTTP = "http";
    private static final String SD_CARD = "/storage";

    private ImageConfig config = new ImageConfig();

    private static final int THREAD_POOL_SIZE = 5;//图片请求线程池数量
    private static final int BUFF_SIZE = 8192;//文件IO buff字节数
    private final BitmapLruCache CACHE = new BitmapLruCache();//内存缓存
    private final Map<String, ExecutorService> EXECUTOR_POOL_MAP = new HashMap<>();//按照界面分类存储图片请求线程池
    private final Handler HANDLER = new ImageHandler(this);//回调主线程Handler
    private final Map<String, Set<Recorder>> RECORD_TASK = new HashMap<>();//记录正在执行请求的任务---><展示界面，Set<图片url为标识>>

    public ImageConfig getConfig() {
        return config;
    }

    public void downloadOnly(String url, ImageLoaderCallback callback) {
        downloadOnly(url, 0, 0, callback);
    }

    public void downloadOnly(String url, int scaleWidth, int scaleHeight, ImageLoaderCallback callback) {
        setImage(null, url, 0, scaleWidth, scaleHeight, callback);
    }

    public void setImagePerformance(ImageView imageView, String url, boolean performance) {
        setImagePerformance(imageView, url, 0, performance);
    }

    public void setImagePerformance(ImageView imageView, String url, @DrawableRes int placeHolder, boolean performance) {
        setImagePerformance(imageView, url, placeHolder, 0, 0, performance);
    }

    public void setImagePerformance(ImageView imageView, String url, @DrawableRes int placeHolder, int scaleWidth, int scaleHeight, boolean performance) {
        setImagePerformance(imageView, url, placeHolder, scaleWidth, scaleHeight, performance, null);
    }

    public void setImagePerformance(ImageView imageView, String url, @DrawableRes int placeHolder, int scaleWidth, int scaleHeight, boolean performance, ImageLoaderCallback callback) {
        if (performance) {
            setPlaceHolder(imageView, placeHolder);
        } else {
            setImage(imageView, url, placeHolder, scaleWidth, scaleHeight, callback);
        }
    }

    public void setImage(ImageView imageView, String url) {
        setImage(imageView, url, 0);
    }

    public void setImage(ImageView imageView, String url, @DrawableRes int placeHolder) {
        setImage(imageView, url, placeHolder, null);
    }

    public void setImage(ImageView imageView, String url, ImageLoaderCallback callback) {
        setImage(imageView, url, 0, callback);
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
            done(url, fileName, bitmap, imageView, callback);
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
            DiskRunnable diskRunnable = new DiskRunnable(imageView, url, diskBitmap, scaleWidth, scaleHeight, HANDLER, getConfig());
            submitTask(imageView, url, callback, diskRunnable);
            return true;
        }
        return false;
    }

    private synchronized void getBitmapFromHttp(ImageView imageView, String url, int scaleWidth, int scaleHeight, ImageLoaderCallback callback) {
        HttpRunnable httpRunnable = new HttpRunnable(imageView, url, getSavePath(url, scaleWidth, scaleHeight), scaleWidth, scaleHeight, HANDLER, getConfig());
        submitTask(imageView, url, callback, httpRunnable);
    }

    private void submitTask(ImageView view, String url, ImageLoaderCallback callback, Runnable runnable) {
        //必须主线程
        Utility.checkMain();
        //必须为有效操作
        if (TextUtils.isEmpty(url)) {
            return;
        }
        String cxt = view == null ? RootApplication.getInstance().toString() : view.getContext().toString();
        //保存每次图片任务，以路径包保存标记
        Set<Recorder> recorders = RECORD_TASK.get(url);
        if (recorders == null) {
            recorders = new HashSet<>();
            RECORD_TASK.put(url, recorders);
        }
        //检测任务是否已开始，防止重复进行
        if (recorders.isEmpty()) {
            ExecutorService executorService = EXECUTOR_POOL_MAP.get(cxt);
            if (executorService == null || executorService.isShutdown()) {
                executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
                EXECUTOR_POOL_MAP.put(cxt, executorService);
            }
            executorService.execute(runnable);
        }
        //保存回调，当url对应的资源加载完毕之后调用
        boolean contains = false;
        for (Recorder recorder : recorders) {
            Object other = null;
            if (view != null) {
                other = view;
            } else if (callback != null) {
                other = callback;
            }
            if (recorder.equals(other)) {
                contains = true;
                break;
            }
        }
        if (!contains) {
            Recorder recorder = new Recorder(view, callback);
            recorders.add(recorder);
        }
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

    private void done(String url, String fileName, Bitmap bitmap, ImageView imageView, ImageLoaderCallback callback) {
        if (bitmap == null || TextUtils.isEmpty(fileName)) {
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
        } else {
            //通知所有请求同一路径地址的图片完毕
            if (RECORD_TASK.containsKey(url)) {
                Set<Recorder> recorders = RECORD_TASK.get(url);
                for (Recorder recorder : recorders) {
                    recorder.done(url, bitmap);
                }
                RECORD_TASK.remove(url);
            }
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
        private WeakReference<ImageLoader> mImageLoaderWeakReference;

        private ImageHandler(ImageLoader imageLoader) {
            mImageLoaderWeakReference = new WeakReference<>(imageLoader);
        }

        @Override
        public void handleMessage(Message msg) {
            Holder holder = (Holder) msg.obj;
            if (holder == null) {
                return;
            }
            if (mImageLoaderWeakReference == null || mImageLoaderWeakReference.get() == null) {
                holder.release();
                return;
            }
            ImageLoader imageLoader = mImageLoaderWeakReference.get();
            String url = holder.getUrl();
            switch (msg.what) {
                case DISK_LOAD_COMPLETE:
                    imageLoader.done(url, holder.getFileName(), holder.getBitmap(), holder.getImageView(), null);
                    break;
                case HTTP_LOAD_COMPLETE:
                    imageLoader.done(url, holder.getFileName(), holder.getBitmap(), holder.getImageView(), null);
                    break;
            }
            holder.release();
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
        private WeakReference<ImageConfig> configWeakReference;
        private WeakReference<Handler> handlerWeakReference;

        private DiskRunnable(ImageView imageView, String url, File file, int scaleWidth, int scaleHeight, Handler handler, ImageConfig config) {
            imageViewWeakReference = new WeakReference<>(imageView);
            urlWeakReference = new WeakReference<>(url);
            fileWeakReference = new WeakReference<>(file);
            widthWeakReference = new WeakReference<>(scaleWidth);
            heightWeakReference = new WeakReference<>(scaleHeight);
            configWeakReference = new WeakReference<>(config);
            handlerWeakReference = new WeakReference<>(handler);
        }

        @Override
        public void run() {
            ImageView imageView = imageViewWeakReference.get();
            String url = urlWeakReference.get();
            File file = fileWeakReference.get();
            Integer width = widthWeakReference.get();
            Integer height = heightWeakReference.get();
            ImageConfig config = configWeakReference.get();
            if (file == null || config == null || url == null || width < 1 || height < 1) {
                return;
            }
            getImage(imageView, url, file, width, height, config);
        }

        protected void getImage(ImageView imageView, String url, File file, Integer width, Integer height, ImageConfig config) {
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
            send2Handler(imageView, url, bitmap, path, ImageHandler.DISK_LOAD_COMPLETE);
        }

        void send2Handler(ImageView imageView, String url, Bitmap bitmap, String path, int flag) {
            if (handlerWeakReference == null || handlerWeakReference.get() == null) {
                return;
            }
            String[] split = path.split("/");
            Holder holder = new Holder()
                    .setImageView(imageView)
                    .setUrl(url)
                    .setFileName(split[split.length - 1])
                    .setBitmap(bitmap);
            Handler handler = handlerWeakReference.get();
            Message message = handler.obtainMessage();
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

        private HttpRunnable(ImageView imageView, String url, File file, int scaleWidth, int scaleHeight, Handler handler, ImageConfig config) {
            super(imageView, url, file, scaleWidth, scaleHeight, handler, config);
        }

        @Override
        protected void getImage(ImageView imageView, String url, File file, Integer width, Integer height, ImageConfig config) {
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
            send2Handler(imageView, url, bitmap, path, ImageHandler.HTTP_LOAD_COMPLETE);
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
        private WeakReference<ImageView> imageView;
        private String url;
        private WeakReference<Bitmap> bitmap;
        private String fileName;
        private WeakReference<ImageLoaderCallback> callback;

        private ImageView getImageView() {
            return imageView != null && imageView.get() != null ? imageView.get() : null;
        }

        private Holder setImageView(ImageView imageView) {
            this.imageView = new WeakReference<>(imageView);
            return this;
        }

        private String getUrl() {
            return url;
        }

        private Holder setUrl(String url) {
            this.url = url;
            return this;
        }

        private Bitmap getBitmap() {
            return bitmap != null && bitmap.get() != null ? bitmap.get() : null;
        }

        private Holder setBitmap(Bitmap bitmap) {
            this.bitmap = new WeakReference<>(bitmap);
            return this;
        }

        private String getFileName() {
            return fileName;
        }

        private Holder setFileName(String fileName) {
            this.fileName = fileName;
            return this;
        }

        private void release() {
            if (imageView != null) {
                imageView.clear();
                imageView = null;
            }
            if (bitmap != null) {
                bitmap.clear();
                bitmap = null;
            }
            if (callback != null) {
                callback.clear();
                callback = null;
            }
        }
    }

    private static class Recorder {
        private WeakReference<ImageView> imageViewWeakReference;
        private WeakReference<ImageLoaderCallback> imageLoaderCallbackWeakReference;

        private Recorder(ImageView imageView, ImageLoaderCallback imageLoaderCallback) {
            imageViewWeakReference = new WeakReference<>(imageView);
            imageLoaderCallbackWeakReference = new WeakReference<>(imageLoaderCallback);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof ImageView) {
                if (imageViewWeakReference == null || imageViewWeakReference.get() == null) {
                    return false;
                }
                ImageView imageView = imageViewWeakReference.get();
                ImageView other = (ImageView) obj;
                if (other == imageView) {
                    return true;
                }
            } else if (obj instanceof ImageLoaderCallback) {
                if (imageLoaderCallbackWeakReference == null || imageLoaderCallbackWeakReference.get() == null) {
                    return false;
                }
                ImageLoaderCallback other = (ImageLoaderCallback) obj;
                ImageLoaderCallback loaderCallback = imageLoaderCallbackWeakReference.get();
                if (other == loaderCallback) {
                    return true;
                }
            }
            return false;
        }

        private void done(String url, Bitmap bitmap) {
            if (imageViewWeakReference != null && imageViewWeakReference.get() != null) {
                ImageView imageView = imageViewWeakReference.get();
                imageView.setImageBitmap(bitmap);
            }
            if (imageLoaderCallbackWeakReference != null && imageLoaderCallbackWeakReference.get() != null) {
                ImageLoaderCallback loaderCallback = imageLoaderCallbackWeakReference.get();
                loaderCallback.onImageLoadComplete(url, bitmap, imageViewWeakReference != null && imageViewWeakReference.get() != null ? imageViewWeakReference.get() : null);
            }
        }
    }
}
