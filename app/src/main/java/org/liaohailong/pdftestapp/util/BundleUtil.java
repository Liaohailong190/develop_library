package org.liaohailong.pdftestapp.util;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.SparseArray;

import org.liaohailong.pdftestapp.inject.SaveState;
import org.liaohailong.pdftestapp.inject.SaveStateListener;
import org.liaohailong.pdftestapp.json.JsonInterface;
import org.liaohailong.pdftestapp.json.JsonUtil;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Bundle相关的方法集合
 * Created by qumiaowin on 2016/6/8.
 */
public class BundleUtil {

    private static final boolean DEBUG = true;

    private BundleUtil() {
        //no instance
    }

    /**
     * 从Bundle中恢复对象的属性值
     *
     * @param bundle
     * @param key
     * @param field      属性
     * @param fieldOwner 对象
     * @param annotation 属性上的注解
     * @return 成功与否
     * @throws IllegalAccessException
     */
    public static boolean restoreFromBundle(Bundle bundle, String key, Field field, Object fieldOwner, SaveState annotation)
            throws IllegalAccessException, NoSuchMethodException, InstantiationException, InvocationTargetException {
        field.setAccessible(true);
        Class<?> cls = field.getType();

        if (Collection.class.isAssignableFrom(cls)) {
            return restoreListFromBundle(bundle, key, field, fieldOwner, annotation);
        }

        if (Enum.class.isAssignableFrom(cls)) {
            String name = bundle.getString(key);
            if (name == null) {
                restore(field, fieldOwner, null, annotation);
            } else {
                //noinspection unchecked
                Class<? extends Enum> enumClass = (Class<? extends Enum>) cls;
                restore(field, fieldOwner, Enum.valueOf(enumClass, name), annotation);
            }
            return true;
        }

        NormalType normalType = hitNormalType(cls);
        if (normalType != null) {
            restore(field, fieldOwner, bundle.get(key), annotation);
            return true;
        }

        if (JsonInterface.class.isAssignableFrom(cls)) {
            //noinspection unchecked
            restore(field, fieldOwner, JsonUtil.getData(bundle, key, (Class<? extends JsonInterface>) cls), annotation);
            return true;
        }

        return failed(fieldOwner, key, "找不到匹配的数据类型");
    }

    private static boolean restoreListFromBundle(Bundle bundle, String key, Field field, Object fieldOwner, SaveState annotation)
            throws IllegalAccessException, NoSuchMethodException, InvocationTargetException, InstantiationException {
        Class<?> parameterType = annotation.parameterType();
        if (parameterType == null || parameterType == SaveState.DEFAULT.class) {
            return failed(fieldOwner, key, "list类型的数据必须指定SaveState中的parameterType");
        }

        ListType listType = hitListType(parameterType);
        if (listType != null) {
            Object value = bundle.get(key);
            if (value == null || field.getType().isAssignableFrom(ArrayList.class)) {
                restore(field, fieldOwner, value, annotation);
            } else {
                Constructor constructor = field.getType().getConstructor(Collection.class);
                restore(field, fieldOwner, constructor.newInstance(value), annotation);
            }
            return true;
        }

        if (JsonInterface.class.isAssignableFrom(parameterType)) {
            //noinspection unchecked
            Class<? extends JsonInterface> elementClass = (Class<? extends JsonInterface>) parameterType;
            return restoreJsonInterfaceListFromBundle(bundle, key, field, fieldOwner, elementClass, annotation);
        }

        return failed(fieldOwner, key, "找不到匹配的list数据类型");
    }

    private static boolean restoreJsonInterfaceListFromBundle(
            Bundle bundle, String key, Field field, Object fieldOwner,
            Class<? extends JsonInterface> elementClass, SaveState annotation)
            throws IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {

        ArrayList<String> stringList = bundle.getStringArrayList(key);
        if (stringList == null) {
            restore(field, fieldOwner, null, annotation);
        } else {
            ArrayList<JsonInterface> list = new ArrayList<>();
            for (String str : stringList) {
                list.add(JsonUtil.jsonStringToObject(str, elementClass));
            }
            restore(field, fieldOwner, list, annotation);
        }

        return true;
    }

    private static void restore(Field field, Object fieldOwner, Object newValue, SaveState annotation)
            throws IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        Object oldValue = field.get(fieldOwner);
        Class<? extends SaveStateListener> listenerCls = annotation.listener();
        Constructor<? extends SaveStateListener> constructor = listenerCls.getDeclaredConstructor();
        constructor.setAccessible(true);
        SaveStateListener listener = constructor.newInstance();
        listener.onRestored(field, fieldOwner, oldValue, newValue);
    }


    /**
     * 将对象的属性值存入Bundle
     *
     * @param bundle
     * @param key
     * @param field      属性
     * @param fieldOwner 对象
     * @param annotation 属性上的注解
     * @return 成功与否
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public static boolean saveToBundle(Bundle bundle, String key, Field field, Object fieldOwner,
                                       SaveState annotation) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        field.setAccessible(true);
        Class<?> cls = field.getType();
        Object value = field.get(fieldOwner);

        if (value == null) {
            return false;
        }

        // 把Collection数据全部转换成ArrayList
        if ((value instanceof Collection) && !(value instanceof ArrayList)) {
            //noinspection unchecked
            value = new ArrayList((Collection) value);
        }

        if (value instanceof ArrayList) {
            ArrayList list = (ArrayList) value;
            return saveListToBundle(bundle, key, list, fieldOwner, annotation);
        }

        if (value instanceof Enum) {
            bundle.putString(key, ((Enum) value).name());
            return true;
        }

        NormalType normalType = hitNormalType(cls);
        if (normalType != null) {
            Method method = Bundle.class.getMethod(normalType.putMethod, String.class, normalType.classes[0]);
            method.invoke(bundle, key, value);
            return true;
        }

        if (value instanceof JsonInterface) {
            JsonUtil.putData(bundle, key, (JsonInterface) value);
            return true;
        }

        return failed(fieldOwner, key, "找不到匹配的数据类型");
    }

    private static boolean saveListToBundle(Bundle bundle, String key, ArrayList list, Object fieldOwner,
                                            SaveState annotation) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Class<?> parameterType = annotation.parameterType();
        if (parameterType == null || parameterType == SaveState.DEFAULT.class) {
            return failed(fieldOwner, key, "list类型的数据必须指定SaveState中的parameterType");
        }

        ListType listType = hitListType(parameterType);
        if (listType != null) {
            Method method = Bundle.class.getMethod(listType.putMethod, String.class, ArrayList.class);
            method.invoke(bundle, key, list);
            return true;
        }

        if (JsonInterface.class.isAssignableFrom(parameterType)) {
            //noinspection unchecked
            Class<? extends JsonInterface> elementClass = (Class<? extends JsonInterface>) parameterType;
            //noinspection unchecked
            return saveJsonInterfaceListToBundle(bundle, key, list, elementClass);
        }

        return failed(fieldOwner, key, "找不到匹配的list数据类型");
    }

    private static <T extends JsonInterface> boolean saveJsonInterfaceListToBundle(
            Bundle bundle, String key, ArrayList<T> list, Class<T> elementClass) {
        ArrayList<String> stringList = new ArrayList<>();
        for (T e : list) {
            stringList.add(JsonUtil.objectToJsonString(e, elementClass));
        }
        bundle.putStringArrayList(key, stringList);
        return true;
    }

    private static NormalType hitNormalType(Class<?> elementClass) {
        for (NormalType normalType : NormalType.values()) {
            boolean hit = false;
            for (Class<?> typeCls : normalType.classes) {
                if (typeCls == elementClass || typeCls.isAssignableFrom(elementClass)) {
                    hit = true;
                    break;
                }
            }
            if (hit) {
                return normalType;
            }
        }
        return null;
    }

    private static ListType hitListType(Class<?> elementClass) {
        for (ListType listType : ListType.values()) {
            boolean hit = false;
            for (Class<?> typeCls : listType.classes) {
                if (typeCls == elementClass || typeCls.isAssignableFrom(elementClass)) {
                    hit = true;
                    break;
                }
            }
            if (hit) {
                return listType;
            }
        }
        return null;
    }


    private static boolean failed(Object fieldOwner, String key, String extraMessage) {
        if (DEBUG) {
            throw new RuntimeException(fieldOwner.getClass().getName() + " field = " + key
                    + " : " + extraMessage);
        }
        return false;
    }

    enum NormalType {
        putBundle(Bundle.class),
        putBoolean(boolean.class, Boolean.class),
        putBooleanArray(boolean[].class),
        putByte(byte.class, Byte.class),
        putByteArray(byte[].class),
        putChar(char.class, Character.class),
        putCharArray(char[].class),
        putCharSequence(CharSequence.class),
        putCharSequenceArray(CharSequence[].class),
        putDouble(double.class, Double.class),
        putDoubleArray(double[].class),
        putFloat(float.class, Float.class),
        putFloatArray(float[].class),
        putInt(int.class, Integer.class),
        putIntArray(int[].class),
        putLong(long.class, Long.class),
        putLongArray(long[].class),
        putParcelable(Parcelable.class),
        putParcelableArray(Parcelable[].class),
        putShort(short.class, Short.class),
        putShortArray(short[].class),
        putString(String.class),
        putStringArray(String[].class),
        putSerializable(Serializable.class),
        putSparseParcelableArray(SparseArray.class),;

        final Class<?>[] classes;
        final String putMethod;
        final String getMethod;

        NormalType(Class<?>... cls) {
            this.classes = cls;
            this.putMethod = name();
            this.getMethod = putMethod.replaceFirst("put", "get");
        }
    }

    enum ListType {
        putStringArrayList(String.class),
        putIntegerArrayList(Integer.class, int.class),
        putParcelableArrayList(Parcelable.class),
        putCharSequenceArrayList(CharSequence.class),;

        final Class<?>[] classes;
        final String putMethod;
        final String getMethod;

        ListType(Class<?>... cls) {
            this.classes = cls;
            this.putMethod = name();
            this.getMethod = putMethod.replaceFirst("put", "get");
        }
    }


    /**
     * 获取需要保存的字段列表
     *
     * @param fieldOwner 字段所属对象
     * @return
     */
    @NonNull
    public static List<Field> getSaveStateFields(@NonNull Object fieldOwner) {
        ArrayList<Field> fieldList = new ArrayList<>();

        Class<?> cls = fieldOwner.getClass();
        while (cls != Object.class) {
            Field[] fields = cls.getDeclaredFields();
            for (Field field : fields) {
                SaveState annotation = field.getAnnotation(SaveState.class);
                if (annotation != null) {
                    fieldList.add(field);
                }
            }
            cls = cls.getSuperclass();
        }

        Collections.sort(fieldList, BundleUtil.FIELD_COMPARATOR);
        return fieldList;
    }

    private static final Comparator<Field> FIELD_COMPARATOR = new Comparator<Field>() {
        @Override
        public int compare(Field lhs, Field rhs) {
            SaveState lhsAnnotation = lhs.getAnnotation(SaveState.class);
            SaveState rhsAnnotation = rhs.getAnnotation(SaveState.class);

            return lhsAnnotation.order() - rhsAnnotation.order();
        }
    };
}
