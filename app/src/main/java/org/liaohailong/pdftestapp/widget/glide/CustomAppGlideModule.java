package org.liaohailong.pdftestapp.widget.glide;

import android.content.Context;

import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.engine.cache.DiskLruCacheFactory;
import com.bumptech.glide.load.engine.cache.LruResourceCache;
import com.bumptech.glide.module.AppGlideModule;

import org.liaohailong.library.util.FileUtil;

import java.io.InputStream;

/**
 * 自定义AppGlideModule
 * Created by LHL on 2017/10/6.
 */
@GlideModule
public class CustomAppGlideModule extends AppGlideModule {
    /**
     * 通过GlideBuilder设置默认的结构(Engine,BitmapPool ,ArrayPool,MemoryCache等等).
     *
     * @param context
     * @param builder
     */
    @Override
    public void applyOptions(Context context, GlideBuilder builder) {
        int maxMemory = (int) Runtime.getRuntime().maxMemory();//获取系统分配给应用的总内存大小
        int memoryCacheSize = maxMemory / 8;//设置图片内存缓存占用八分之一
        //重新设置内存限制
        builder.setMemoryCache(new LruResourceCache(memoryCacheSize));
        String cacheDir = FileUtil.getRootDirectory("victor");//指定的是数据的缓存地址
        int diskCacheSize = 1024 * 1024 * 1024;//最多可以缓存多少字节的数据
        //设置磁盘缓存大小
        builder.setDiskCache(new DiskLruCacheFactory(cacheDir, "glide", diskCacheSize));
    }

    /**
     * 为App注册一个自定义的String类型的BaseGlideUrlLoader
     *
     * @param context
     * @param registry
     */
    @Override
    public void registerComponents(Context context, Registry registry) {

        registry.append(String.class, InputStream.class, new CustomBaseGlideUrlLoader.Factory());
    }

    /**
     * 清单解析的开启
     * <p>
     * 这里不开启，避免添加相同的modules两次
     *
     * @return 是否开启清单解析
     */
    @Override
    public boolean isManifestParsingEnabled() {
        return false;
    }
}
