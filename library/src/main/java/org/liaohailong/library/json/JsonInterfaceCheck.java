package org.liaohailong.library.json;

import android.os.Build;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.util.SparseArrayCompat;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.util.SparseIntArray;
import android.util.SparseLongArray;

import com.google.gson.JsonElement;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 校验Json数据类的正确性
 * Created by qumiao on 2015/11/24.
 */
public class JsonInterfaceCheck {

    public static final boolean DEBUG = true;

    private JsonInterfaceCheck() {

    }

    /**
     * 检验指定的类型是否能被Json反序列化
     * @param type 待检查的类型
     */
    public static void assetType(@NonNull Type type) {
        assetType(type, type.toString(), "", 0, new HashSet<Type>());
    }

    private static void assetType(@NonNull Type type, @NonNull String fieldOwner,
                                  @NonNull String fieldName, int modifiers, @NonNull Set<Type> excludes) {
        if (!DEBUG) {
            return;
        }

        // transient和static修饰的field不参与检查，直接跳过
        if (Modifier.isTransient(modifiers) || Modifier.isStatic(modifiers)) {
            return;
        }

        final String msgNotJsonInterface = "%s （类：%s，属性：%s） 没有实现 JsonInterface!";
        final String msgParameterizedType = "%s （类：%s，属性：%s） 必须参数化，不能为原始类型!";

        if (!(type instanceof Class)) {
            if (type instanceof ParameterizedType) {    // 参数化类型，如HashMap<String, String>
                assetParameterizedType((ParameterizedType) type, fieldOwner, fieldName, excludes);
            } else if (type instanceof WildcardType) {  // 通配符类型，如<? extends List>
                assetType(((WildcardType) type).getUpperBounds()[0], fieldOwner, fieldName, 0, excludes);
            } else if (type instanceof GenericArrayType) {  // 参数化类型或类型变量的数组，如ArrayList<String>[]、T[]
                assetType(((GenericArrayType) type).getGenericComponentType(), fieldOwner, fieldName, 0, excludes);
            } else {    // 未解泛型不能通过检查，如<T> <E>之类的
                throw new RuntimeException(String.format(msgNotJsonInterface, type, fieldOwner, fieldName));
            }
            return;
        }

        Class<?> objCls = (Class<?>) type;

        if (objCls.isArray()) { // 数组，需要进一步检查元素类型
            assetType(objCls.getComponentType(), fieldOwner, fieldName, 0, excludes);
            return;
        }

        if (isBaseAcceptedType(objCls)) {   // 直接通过检查的类型
            return;
        }

        // 原始类型能通过的Map或者Collection等，不能通过检查，必须参数化
        if (isRawAcceptedParameterizedType(objCls)) {
            throw new RuntimeException(String.format(msgParameterizedType, objCls, fieldOwner, fieldName));
        }

        if (!JsonInterface.class.isAssignableFrom(objCls)) {
            throw new RuntimeException(String.format(msgNotJsonInterface, objCls, fieldOwner, fieldName));
        }

        excludes.add(type);
        Field[] fields = objCls.getDeclaredFields();
        for (Field field : fields) {
            if (excludes.contains(field.getGenericType())) {
                continue;
            }
            assetType(field.getGenericType(), type.toString(), field.getName(), field.getModifiers(), excludes);
        }

        Class<?> superClass = objCls.getSuperclass();
        if (superClass != Object.class && !excludes.contains(superClass)) {
            assetType(superClass, fieldOwner, fieldName, 0, excludes);
        }
    }

    private static void assetParameterizedType(ParameterizedType type, String fieldOwner, String fieldName, Set<Type> excludes) {
        Type rawType = type.getRawType();

        if (!isRawAcceptedParameterizedType(rawType) && !excludes.contains(rawType)) {
            assetType(rawType, fieldOwner, fieldName, 0, excludes);
        }

        for (Type argument : type.getActualTypeArguments()) {
            if (excludes.contains(argument)) {
                continue;
            }
            if (argument instanceof ParameterizedType) {
                assetParameterizedType((ParameterizedType) argument, fieldOwner, fieldName, excludes);
            } else {
                assetType(argument, fieldOwner, fieldName, 0, excludes);
            }
        }
    }

    /**
     * 直接通过检查的类型
     * @param type
     * @return
     */
    private static boolean isBaseAcceptedType(Type type) {
        if (!(type instanceof Class)) {
            return false;
        }
        Class<?> cls = (Class<?>) type;

        if (cls.isPrimitive() || cls == String.class) {
            return true;
        }

        if (SparseBooleanArray.class.isAssignableFrom(cls)
                || SparseIntArray.class.isAssignableFrom(cls)
                || isSparseLongArray(cls)
                || JsonElement.class.isAssignableFrom(cls)) {
            return true;
        }

        if (Serializable.class.isAssignableFrom(cls)
                || Parcelable.class.isAssignableFrom(cls)) {
            // 其中包含部分参数化类型，不能直接通过，必须进一步检查参数类型（如HashMap、ArrayList等）
            return !isRawAcceptedParameterizedType(cls);
        }

        return false;
    }

    private static boolean isSparseLongArray(Class<?> cls) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
            return false;
        }

        return SparseLongArray.class.isAssignableFrom(cls);
    }

    /**
     * 原始类型能通过检查的 参数化类型（需要进一步检查参数的类型）
     * @param type
     * @return
     */
    private static boolean isRawAcceptedParameterizedType(Type type) {
        if (!(type instanceof Class)) {
            return false;
        }
        Class<?> cls = (Class<?>) type;
        return Collection.class.isAssignableFrom(cls) || Map.class.isAssignableFrom(cls)
                || SparseArray.class.isAssignableFrom(cls)
                || SparseArrayCompat.class.isAssignableFrom(cls);
    }

}
