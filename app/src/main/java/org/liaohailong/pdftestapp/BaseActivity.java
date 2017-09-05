package org.liaohailong.pdftestapp;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;

import org.liaohailong.pdftestapp.announce.OnClickListener;
import org.liaohailong.pdftestapp.announce.FindViewById;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * 所有Activity基类
 * Created by LHL on 2017/9/5.
 */

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        super.setContentView(layoutResID);
        checkAnnounce();
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);
        checkAnnounce();
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        super.setContentView(view, params);
        checkAnnounce();
    }

    private void checkAnnounce() {
        checkBindOnClickAnnounce();
        checkFindViewByIdAnnounce();
    }

    private void checkBindOnClickAnnounce() {
        Class aClass = getClass();
        while (aClass != Object.class) {
            Method[] declaredMethods = aClass.getDeclaredMethods();
            for (Method declaredMethod : declaredMethods) {
                if (declaredMethod.isAnnotationPresent(OnClickListener.class)) {
                    OnClickListener annotation = declaredMethod.getAnnotation(OnClickListener.class);
                    int[] value = annotation.value();
                    for (int id : value) {
                        View view = findViewById(id);
                        if (view != null) {
                            Class viewClass = view.getClass();
                            try {
                                Method setOnClickListener = getMethod(viewClass, "setOnClickListener", new Class[]{View.OnClickListener.class});
                                setOnClickListener.invoke(view, this);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
            aClass = aClass.getSuperclass();
        }
    }

    private void checkFindViewByIdAnnounce() {
        Class aClass = getClass();
        while (aClass != Object.class) {
            Field[] fields = aClass.getDeclaredFields();
            for (Field field : fields) {
                if (field.isAnnotationPresent(FindViewById.class)) {
                    if (View.class.isAssignableFrom(field.getType())) {
                        FindViewById annotation = field.getAnnotation(FindViewById.class);
                        View viewById = findViewById(annotation.value());
                        try {
                            field.setAccessible(true);
                            field.set(this, viewById);
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            aClass = aClass.getSuperclass();
        }
    }


    /**
     * 利用递归找一个类的指定方法，如果找不到，去父亲里面找直到最上层Object对象为止。
     *
     * @param clazz      目标类
     * @param methodName 方法名
     * @param classes    方法参数类型数组
     * @return 方法对象
     * @throws Exception
     */
    public static Method getMethod(Class clazz, String methodName,
                                   final Class[] classes) throws Exception {
        Method method = null;
        try {
            method = clazz.getDeclaredMethod(methodName, classes);
        } catch (NoSuchMethodException e) {
            try {
                method = clazz.getMethod(methodName, classes);
            } catch (NoSuchMethodException ex) {
                if (clazz.getSuperclass() == null) {
                    return method;
                } else {
                    method = getMethod(clazz.getSuperclass(), methodName,
                            classes);
                }
            }
        }
        return method;
    }


}
