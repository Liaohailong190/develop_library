package org.liaohailong.library.image;

import org.liaohailong.library.RootApplication;

import java.io.File;

/**
 * ImageLoader配置属性相关
 * Created by LHL on 2017/9/10.
 */

public class ImageConfig {
    //图片文件本地缓存路径
    private File cacheDirectory = RootApplication.getInstance().getCacheDir();
    //图片默认裁剪宽度
    private int defaultWidth = 1280;
    //图片默认裁剪高度
    private int defaultHeight = 720;
    //是否打印log
    private boolean writeLog = false;

    public File getCacheDirectory() {
        return cacheDirectory;
    }

    public void setCacheDirectory(File cacheDirectory) {
        this.cacheDirectory = cacheDirectory;
    }

    public void setCacheDirectory(String cacheDirectory) {
        File file = new File(cacheDirectory);
        if (file.exists() && !file.isDirectory()) {
            boolean delete = file.delete();
        }
        if (!file.exists()) {
            boolean mkdirs = file.mkdirs();
        }
        this.cacheDirectory = file;
    }


    public int getDefaultWidth() {
        return defaultWidth;
    }

    public void setDefaultWidth(int defaultWidth) {
        this.defaultWidth = defaultWidth;
    }

    public int getDefaultHeight() {
        return defaultHeight;
    }

    public void setDefaultHeight(int defaultHeight) {
        this.defaultHeight = defaultHeight;
    }

    public boolean isWriteLog() {
        return writeLog;
    }

    public void setWriteLog(boolean writeLog) {
        this.writeLog = writeLog;
    }
}
