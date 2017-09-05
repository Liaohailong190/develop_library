package org.liaohailong.pdftestapp;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Xml;

import com.google.gson.internal.$Gson$Types;
import com.google.gson.reflect.TypeToken;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * 常用方法集合
 * Created by LHL on 2017/9/4.
 */

public class Utility {
    /**
     * File buffer stream size.
     */
    public static final int FILE_STREAM_BUFFER_SIZE = 32;


    private Utility() {

    }

    /**
     * 获取cls中的泛型参数类型。参考自{@link TypeToken#getSuperclassTypeParameter(Class)}。
     * <br/>注意：泛型类本身不能获取参数类型（因为被擦除了），只有泛型类的子类才可以获取。
     * <br/>比如ArrayList&lt;String&gt;无法通过ArrayList.class获取String类型；
     * <br/>但如果class MyList extends ArrayList&lt;String&gt;，那么MyList.class是可以获取String类型的。
     */
    public static Type getClassTypeParameter(Class<?> cls) {
        Type superclass = cls.getGenericSuperclass();
        while (superclass instanceof Class) {
            if (superclass == Object.class) {
                throw new RuntimeException(cls.getName() + " extends " + cls.getSuperclass().getName() + ": missing type parameter.");
            } else {
                superclass = ((Class) superclass).getGenericSuperclass();
            }
        }
        ParameterizedType parameterized = (ParameterizedType) superclass;
        return $Gson$Types.canonicalize(parameterized.getActualTypeArguments()[0]);
    }


    /**
     * 关闭，并捕获IOException
     *
     * @param closeable Closeable
     */
    public static void close(@Nullable Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 转换Stream成string
     *
     * @param is Stream源
     * @return 目标String
     */
    @NonNull
    public static String streamToString(@NonNull InputStream is) {
        return streamToString(is, Xml.Encoding.UTF_8.toString());
    }

    /**
     * 按照特定的编码格式转换Stream成string
     *
     * @param is  Stream源
     * @param enc 编码格式
     * @return 目标String
     */
    @NonNull
    public static String streamToString(@NonNull InputStream is, String enc) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            int availableLength = is.available();
            int count;
            if (availableLength < FILE_STREAM_BUFFER_SIZE)
                availableLength = FILE_STREAM_BUFFER_SIZE;
            final byte[] data = new byte[availableLength];
            while ((count = is.read(data)) > 0) {
                os.write(data, 0, count);
            }
            return new String(os.toByteArray(), Xml.Encoding.UTF_8.toString());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        } finally {
            close(os);
            close(is);
        }
        return "";
    }
}
