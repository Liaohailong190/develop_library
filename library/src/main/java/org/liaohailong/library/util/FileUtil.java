package org.liaohailong.library.util;

import android.content.res.AssetManager;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;


import org.liaohailong.library.RootApplication;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

/**
 * 文件操作类
 * <p>
 * 缓存文件管理，去那吃，菜谱通用
 * <p>
 * 实现缓存文件更新，设置缓存过期时间
 * <p>
 * 图片缓存文件存入时做裁减
 * <p>
 * 缓存目录文件个数监控
 * <p>
 * 缓存目录使用空间监控
 */
public class FileUtil {

    private static final String TAG = "FileUtil";
    private static final int BUFF_SIZE = 8192;
    public final static String FILE_EXTENSION_SEPARATOR = ".";

    private FileUtil() {

    }

    /**
     * 文件或文件夹拷贝
     * 如果是文件夹拷贝 目标文件必须也是文件夹
     *
     * @param srcFile 源文件
     * @param dstFile 目标文件
     * @return
     */
    public static boolean copy(File srcFile, File dstFile) {
        if (!srcFile.exists()) { //源文件不存在
            return false;
        }

        if (srcFile.isDirectory()) { //整个文件夹拷贝
            if (!dstFile.isDirectory()) {    //如果目标不是目录，返回false
                return false;
            }

            for (File f : srcFile.listFiles()) {
                if (!copy(f, new File(dstFile, f.getName()))) {
                    return false;
                }
            }
            return true;

        } else { //单个文价拷贝
            return copyFile(srcFile, dstFile);
        }

    }

    /**
     * 判断某个文件所在的文件夹是否存在，不存在时直接创建
     *
     * @param path
     */
    public static void parentFolder(String path) {
        File file = new File(path);
        String parent = file.getParent();

        File parentFile = new File(parent + File.separator);
        if (!parentFile.exists()) {
            mkdirs(parentFile);
        }
    }

    /**
     * 拷贝文件
     *
     * @param srcFile  源文件
     * @param destFile 目标文件，如果是目录，则生成该目录下的同名文件再拷贝
     */
    private static boolean copyFile(File srcFile, File destFile) {
        if (!destFile.exists()) {
            if (!mkdirs(destFile.getParentFile()) || !createNewFile(destFile)) {
                return false;
            }
        } else if (destFile.isDirectory()) {
            destFile = new File(destFile, srcFile.getName());
        }

        FileInputStream in = null;
        FileOutputStream out = null;
        try {
            in = new FileInputStream(srcFile);
            out = new FileOutputStream(destFile);
            FileChannel src = in.getChannel();
            FileChannel dst = out.getChannel();
            dst.transferFrom(src, 0, src.size());
            src.close();
            dst.close();

            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            Utility.close(out);
            Utility.close(in);
        }

        return false;
    }

    /**
     * 创建目录（如果不存在）。
     *
     * @param dirPath 目录的路径
     * @return true表示创建，false表示该目录已经存在
     */
    public static boolean createDirIfMissed(String dirPath) {
        File dir = new File(dirPath);
        return !dir.exists() && dir.mkdirs();
    }

    /**
     * 创建文件（如果不存在）
     *
     * @param file 目标文件
     */
    public static void createFileIfMissed(File file) {
        if (file.exists()) {
            if (file.isDirectory()) {
                boolean delete = file.delete();
            }
        }
        if (!file.exists()) {
            try {
                boolean mkdir = file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 递归删除目录下的所有文件及子目录下所有文件，并且删除该目录
     *
     * @param path 将要删除的文件目录
     * @return boolean 成功清除目录及子文件返回true；
     * 若途中删除某一文件或清除目录失败，则终止清除工作并返回false.
     */
    public static boolean deleteDir(String path) {
        return deleteDir(new File(path));
    }

    /**
     * 递归删除目录下的所有文件及子目录下所有文件，并且删除该目录
     *
     * @param dir 将要删除的文件目录
     * @return boolean 成功清除目录及子文件返回true；
     * 若途中删除某一文件或清除目录失败，则终止清除工作并返回false.
     */
    public static boolean deleteDir(File dir) {
        if (dir == null) {
            return false;
        }

        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (String child : children) {
                boolean success = deleteDir(new File(dir, child));
                if (!success) {
                    return false;
                }
            }
        }
        // 目录此时为空，可以删除
        return dir.delete();
    }

    /**
     * 递归清空目录下的所有文件及子目录下所有文件，但不删除目录（包括子目录）
     *
     * @param path 将要清空的文件目录
     * @return boolean 成功清除目录及子文件返回true；
     * 若途中清空某一文件或清除目录失败，则终止清除工作并返回false.
     */
    public static boolean clearDir(String path) {
        return clearDir(path, null);
    }

    /**
     * 递归清空目录下的所有文件及子目录下所有文件，但不删除目录（包括子目录）
     *
     * @param path    将要清空的文件目录
     * @param excepts 除去这些目录或者文件，可以为null
     * @return boolean 成功清除目录及子文件返回true；
     * 若途中清空某一文件或清除目录失败，则终止清除工作并返回false.
     */
    public static boolean clearDir(String path, List<String> excepts) {
        ArrayList<File> exceptFiles = new ArrayList<>();
        if (excepts != null) {
            for (String except : excepts) {
                exceptFiles.add(new File(except));
            }
        }
        return clearDir(new File(path), exceptFiles);
    }

    /**
     * 递归清空目录下的所有文件及子目录下所有文件，但不删除目录（包括子目录）
     *
     * @param dir 将要清空的文件目录
     * @return boolean 成功清除目录及子文件返回true；
     * 若途中清空某一文件或清除目录失败，则终止清除工作并返回false.
     */
    public static boolean clearDir(File dir) {
        return clearDir(dir, null);
    }

    /**
     * 递归清空目录下的所有文件及子目录下所有文件，但不删除目录（包括子目录）
     *
     * @param dir     将要清空的文件目录
     * @param excepts 除去这些目录或者文件，可以为null
     * @return boolean 成功清除目录及子文件返回true；
     * 若途中清空某一文件或清除目录失败，则终止清除工作并返回false.
     */
    public static boolean clearDir(File dir, List<File> excepts) {
        if (dir == null) {
            return false;
        }

        if (excepts != null && excepts.contains(dir)) {
            return true;
        }

        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (String child : children) {
                boolean success = clearDir(new File(dir, child), excepts);
                if (!success) {
                    return false;
                }
            }
            return true;
        }

        return dir.delete();
    }


    /**
     * 获取某个目录下所有文件的大小之和
     *
     * @param path
     * @return
     */
    public static float getDirSize(String path, boolean isRoot) {
        return getDirSize(path, null, isRoot);
    }

    /**
     * 获取某个目录下所有文件的大小之和
     *
     * @param path
     * @param excepts 除去这些目录或者文件，可以为null
     * @return
     */
    public static float getDirSize(String path, List<String> excepts, boolean isRoot) {
        if (TextUtils.isEmpty(path)) {
            return 0.f;
        }
        ArrayList<File> exceptFiles = new ArrayList<>();
        if (excepts != null) {
            for (String except : excepts) {
                exceptFiles.add(new File(except));
            }
        }
        return getDirSize(new File(path), exceptFiles, isRoot);
    }

    /**
     * 获取某个目录下所有文件的大小之和
     *
     * @return
     */
    public static float getDirSize(File dir, boolean isRoot) {
        return getDirSize(dir, null, isRoot);
    }

    /**
     * 获取某个目录下所有文件的大小之和
     *
     * @param excepts 除去这些目录或者文件，可以为null
     * @return
     */
    public static float getDirSize(File dir, List<File> excepts, boolean isRoot) {
        float size = 0.f;

        if (dir == null) {
            return size;
        }

        if (excepts != null && excepts.contains(dir)) {
            return size;
        }

        if (dir.exists()) {
            if (dir.isDirectory()) {
                File[] fs = dir.listFiles();
                for (File childFile : fs) {
                    if (childFile.isFile()) {
                        size += childFile.length();
                    } else {
                        size += getDirSize(childFile, excepts, false);
                    }
                }
            } else {
                if (!isRoot) {
                    size += dir.length();
                }
            }
        }

        return size;
    }


    /**
     * 删除文件。如果删除失败，则打出error级别的log
     *
     * @param file 文件
     * @return 成功与否
     */
    public static boolean deleteFile(File file) {
        if (file == null) {
            return false;
        }
        boolean result = file.delete();
        if (!result) {
            Log.e(TAG, "FileUtil cannot delete file: " + file);
        }
        return result;
    }

    /**
     * 创建文件。如果创建失败，则打出error级别的log
     *
     * @param file 文件
     * @return 成功与否
     */
    public static boolean createNewFile(File file) {
        if (file == null) {
            return false;
        }

        boolean result;
        try {
            result = file.createNewFile() || file.isFile();
        } catch (IOException e) {
            e.printStackTrace();
            result = false;
        }

        if (!result) {
            Log.e(TAG, "FileUtil cannot create file: " + file);
        }
        return result;
    }

    /**
     * 创建目录。如果创建失败，则打出error级别的log
     *
     * @param file 文件
     * @return 成功与否
     */
    public static boolean mkdir(File file) {
        if (file == null) {
            return false;
        }
        if (!file.mkdir() && !file.isDirectory()) {
            Log.e(TAG, "FileUtil cannot make dir: " + file);
            return false;
        }
        return true;
    }

    /**
     * 创建文件对应的所有父目录。如果创建失败，则打出error级别的log
     *
     * @param file 文件
     * @return 成功与否
     */
    public static boolean mkdirs(File file) {
        if (file == null) {
            return false;
        }
        if (!file.mkdirs() && !file.isDirectory()) {
            Log.e(TAG, "FileUtil cannot make dirs: " + file);
            return false;
        }
        return true;
    }

    /**
     * 文件或目录重命名。如果失败，则打出error级别的log
     *
     * @param srcFile 原始文件或目录
     * @param dstFile 重命名后的文件或目录
     * @return 成功与否
     */
    public static boolean renameTo(File srcFile, @Nullable File dstFile) {
        if (srcFile == null || dstFile == null) {
            return false;
        }
        if (!srcFile.renameTo(dstFile)) {
            Log.e(TAG, "FileUtil cannot rename " + srcFile + " to " + dstFile);
            return false;
        }

        return true;
    }

    /**
     * 判断是否存在sdCard
     */
    public static boolean hasSdcard() {
        if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
            return true;
        }
        return false;
    }

    /**
     * 获取文件
     *
     * @param path
     * @param filenameFilter
     */
    public static void delete(@NonNull String path, FilenameFilter filenameFilter) {
        File file = new File(path);
        if (file.exists() && file.canWrite() && filenameFilter != null && file.isDirectory()) {
            File files[] = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                String name = files[i].getName();
                if (filenameFilter.accept(files[i], name)) {
                    deleteFile(files[i]);
                }
            }
        }
    }

    /**
     * get file name from path, not include suffix
     * <p>
     * <pre>
     *      getFileNameWithoutExtension(null)               =   null
     *      getFileNameWithoutExtension("")                 =   ""
     *      getFileNameWithoutExtension("   ")              =   "   "
     *      getFileNameWithoutExtension("abc")              =   "abc"
     *      getFileNameWithoutExtension("a.mp3")            =   "a"
     *      getFileNameWithoutExtension("a.b.rmvb")         =   "a.b"
     *      getFileNameWithoutExtension("c:\\")              =   ""
     *      getFileNameWithoutExtension("c:\\a")             =   "a"
     *      getFileNameWithoutExtension("c:\\a.b")           =   "a"
     *      getFileNameWithoutExtension("c:a.txt\\a")        =   "a"
     *      getFileNameWithoutExtension("/home/admin")      =   "admin"
     *      getFileNameWithoutExtension("/home/admin/a.txt/b.mp3")  =   "b"
     * </pre>
     *
     * @param filePath
     * @return file name from path, not include suffix
     * @see
     */
    public static String getFileNameWithoutExtension(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return filePath;
        }

        int extenPosi = filePath.lastIndexOf(FILE_EXTENSION_SEPARATOR);
        int filePosi = filePath.lastIndexOf(File.separator);
        if (filePosi == -1) {
            return (extenPosi == -1 ? filePath : filePath.substring(0, extenPosi));
        }
        if (extenPosi == -1) {
            return filePath.substring(filePosi + 1);
        }
        return (filePosi < extenPosi ? filePath.substring(filePosi + 1, extenPosi) : filePath.substring(filePosi + 1));
    }

    /**
     * 获取文件保存根目录路径
     *
     * @return
     */
    public synchronized static String getRootDirectory(String name) {
        File directory = null;
        final String state = Environment.getExternalStorageState();
        switch (state) {
            case Environment.MEDIA_SHARED:
            case Environment.MEDIA_MOUNTED:
                directory = Environment.getExternalStorageDirectory();
                break;
        }

        if (directory == null) {
            directory = Environment.getDataDirectory();
        }

        if (directory == null) {
            return null;
        }

        String path = directory.getPath() + "/" + name + "/";
        File file = new File(path);
        if (!file.exists()) {
            boolean mkdirs = file.mkdirs();
            if (mkdirs) {
                return path;
            }
        }
        if (file.isFile()) {
            boolean delete = file.delete();
            if (delete) {
                boolean mkdirs = file.mkdirs();
                if (mkdirs) {
                    return path;
                }
            }
        }
        return path;
    }

    /**
     * 获得指定文件的byte数组
     */
    public static byte[] getBytes(String filePath) {
        byte[] buffer = null;
        try {
            File file = new File(filePath);
            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream bos = new ByteArrayOutputStream(1000);
            byte[] b = new byte[1000];
            int n;
            while ((n = fis.read(b)) != -1) {
                bos.write(b, 0, n);
            }
            fis.close();
            bos.close();
            buffer = bos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return buffer;
    }

    /**
     * 根据byte数组，生成文件
     */
    public static void getFile(byte[] bfile, String filePath, String fileName) {
        BufferedOutputStream bos = null;
        FileOutputStream fos = null;
        File file = null;
        try {
            File dir = new File(filePath);
            if (!dir.exists() && dir.isDirectory()) {//判断文件目录是否存在
                dir.mkdirs();
            }
            file = new File(filePath + "\\" + fileName);
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            bos.write(bfile);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    /**
     * 复制Asset文件
     *
     * @param assetDir Assets文件
     * @param dir      复制目标路径
     */
    public static void copyAssets(String assetDir, String dir) {
        FileOutputStream fos = null;
        BufferedInputStream bis = null;
        try {
            File dirFile = new File(dir);
            boolean exists = dirFile.exists();
            boolean directory = dirFile.isDirectory();
            if (!exists || directory) {
                FileUtil.deleteDir(dirFile);
                FileUtil.createNewFile(dirFile);
            }
            AssetManager assets = RootApplication.getInstance().getAssets();
            byte[] data = new byte[1024];
            int len;
            bis = new BufferedInputStream(assets.open(assetDir));
            fos = new FileOutputStream(dirFile);
            while ((len = bis.read(data)) != -1) {
                fos.write(data, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            Utility.close(bis);
            Utility.close(fos);
        }
    }

    /**
     * 读取本地文件字符串数据
     *
     * @param path 文件路径
     * @return 文件内字符串数据
     */
    public static String getStringFromFile(String path) {
        File file = new File(path);
        if (!file.exists()) {
            return null;
        }
        InputStream is = null;
        try {
            is = new FileInputStream(file);
            return Utility.streamToString(is);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Utility.close(is);
        }
        return null;
    }

    public static byte[] fileToByte(File file) {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            return inputStreamToData(fis);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Utility.close(fis);
        }
        return null;
    }

    private static byte[] inputStreamToData(InputStream inputStream) throws IOException {
        ByteArrayOutputStream cache = new ByteArrayOutputStream();
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
}

