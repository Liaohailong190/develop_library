package org.liaohailong.library.image;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.util.LruCache;

/**
 * 图片内存缓存
 * Created by LHL on 2017/9/10.
 */

public class BitmapLruCache extends LruCache<String,Bitmap>implements BitmapCache{

    private static final boolean DEBUG = false;
    private static final String TAG = "BitmapLruCache";

    private static int PRESENT = 4;// SUPPRESS CHECKSTYLE

    public BitmapLruCache() {
        // 使用内存的1/4
        super((int) (Runtime.getRuntime().maxMemory() / PRESENT));
        if (DEBUG)
            log("iam strong cache  size " + Runtime.getRuntime().maxMemory() / PRESENT);
    }

    @Override
    protected int sizeOf(String key, @NonNull Bitmap value) {
        return (int) getBitmapSize(value);
    }

    public void clear() {
        trimToSize(-1);
    }

    public boolean containsKey(@NonNull String url) {
        return get(url) != null;

    }

    @Override
    protected void entryRemoved(boolean evicted, String key, @Nullable Bitmap oldValue, Bitmap newValue) {
        super.entryRemoved(evicted, key, oldValue, newValue);
        if (evicted && !TextUtils.isEmpty(key) && oldValue != null) {
            log("缓存池满了 ，开始删,目前的size是   " + size());
        }

    }

    /**
     * 图片占用的内存
     *
     * @return
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    public long getBitmapSize(@NonNull Bitmap bitmap) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
            return bitmap.getByteCount();
        }
        return bitmap.getRowBytes() * bitmap.getHeight();
    }

    @Override
    public int getCacheType() {
        return CacheType.LRU;
    }

    private static void log(String msg) {
        if (DEBUG) {
            Log.d(TAG, msg);
        }
    }
}
