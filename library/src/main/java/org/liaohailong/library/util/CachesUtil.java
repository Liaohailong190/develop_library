package org.liaohailong.library.util;

import android.os.Environment;

import org.liaohailong.library.RootApplication;

import java.io.File;

public class CachesUtil {
    public static String VIDEO = "video";

    /**
     * 获取媒体缓存文件
     *
     * @param child
     * @return
     */
    public static File getMediaCacheFile(String child) {
        String directoryPath;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            // 外部储存可用
            directoryPath = RootApplication.getInstance().getExternalFilesDir(child).getAbsolutePath();
        } else {
            directoryPath = RootApplication.getInstance().getFilesDir().getAbsolutePath() + File.separator + child;
        }
        File file = new File(directoryPath);
        //判断文件目录是否存在
        if (!file.exists()) {
            file.mkdirs();
        }
        return file;
    }
}
