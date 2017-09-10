package org.liaohailong.library.image;

import android.graphics.Bitmap;
import android.widget.ImageView;

/**
 * 图片加载回调相关
 * Created by LHL on 2017/9/10.
 */

public interface ImageLoaderCallback {
    /**
     * 图片加载完毕时，回调次方法
     * @param url 网络图片的地址
     * @param bitmap 加载完成的bitmap
     * @param imageView 图片挂载视图
     */
    void onImageLoadComplete(String url, Bitmap bitmap, ImageView imageView);
}
