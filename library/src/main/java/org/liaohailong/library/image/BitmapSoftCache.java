package org.liaohailong.library.image;

import android.graphics.Bitmap;
import android.support.annotation.Nullable;
import android.util.Log;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;

public class BitmapSoftCache implements BitmapCache {

    private static final boolean DEBUG = false;
    private static final String TAG = "BitmapSoftCache";

    private Map<String, SoftReference<Bitmap>> CACHE;

    public BitmapSoftCache() {
        CACHE = new HashMap<String, SoftReference<Bitmap>>();
        log("iam soft");
    }

    @Override
    public boolean containsKey(String url) {
        return CACHE.containsKey(url);
    }

    @Nullable
    @Override
    public Bitmap put(String url, Bitmap bitmap) {
        SoftReference<Bitmap> last = CACHE.put(url, new SoftReference<Bitmap>(bitmap));
        return getBitmapInSoft(last);
    }

    @Nullable
    @Override
    public Bitmap get(String url) {
        SoftReference<Bitmap> current = CACHE.get(url);
        return getBitmapInSoft(current);
    }

    @Override
    public void clear() {
        CACHE.clear();
    }

    @Nullable
    @Override
    public Bitmap remove(String url) {
        SoftReference<Bitmap> current = CACHE.remove(url);
        return getBitmapInSoft(current);
    }

    @Nullable
    private Bitmap getBitmapInSoft(@Nullable SoftReference<Bitmap> softbitmap) {
        return softbitmap != null ? softbitmap.get() : null;
    }

    @Override
    public int getCacheType() {
        return CacheType.SOFT;
    }

    private static void log(String msg) {
        if (DEBUG) {
            Log.d(TAG, msg);
        }
    }
}
