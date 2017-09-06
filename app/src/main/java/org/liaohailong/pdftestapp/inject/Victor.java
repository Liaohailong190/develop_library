package org.liaohailong.pdftestapp.inject;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.liaohailong.pdftestapp.util.BundleUtil;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

/**
 * 注解使用类
 * Victor是我的英文名字
 * Created by LHL on 2017/9/6.
 */

public class Victor {
    private static final String METHOD_SET_ONCLICK_LISTENER = "setOnClickListener";
    private static final String METHOD_ON_CLICK = "onClick";

    private Victor() {

    }

    /**
     * 此方法需要放在{@link Activity#onCreate(Bundle)}中调用
     */
    public static <T extends Activity> void inject(T t, @Nullable Bundle savedInstanceState) {
        checkBindContentViewAnnounce(t);
        checkFindViewByIdAnnounce(t);
        checkBindOnClickAnnounce(new ViewFinder(t, t.getWindow().getDecorView()));
        setSaveState(t, savedInstanceState);
    }

    private static <T extends Activity> void checkBindContentViewAnnounce(@NonNull T t) {
        Class aClass = t.getClass();
        while (aClass != Object.class) {
            if (aClass.isAnnotationPresent(BindContentView.class)) {
                BindContentView annotation = (BindContentView) aClass.getAnnotation(BindContentView.class);
                if (annotation != null) {
                    int value = annotation.value();
                    t.setContentView(value);
                }
            }
            aClass = aClass.getSuperclass();
        }
    }

    private static <T extends Activity> void checkFindViewByIdAnnounce(@NonNull T t) {
        Class aClass = t.getClass();
        while (aClass != Object.class) {
            Field[] fields = aClass.getDeclaredFields();
            for (Field field : fields) {
                handlerFindViewById(new ViewFinder(t, t.getWindow().getDecorView()), field);
            }
            aClass = aClass.getSuperclass();
        }
    }

    private static void checkBindOnClickAnnounce(ViewFinder finder) {
        Object owner = finder.getOwner();
        Class aClass = owner.getClass();
        while (aClass != Object.class) {
            Method[] declaredMethods = aClass.getDeclaredMethods();
            for (Method method : declaredMethods) {
                if (method.isAnnotationPresent(BindOnClick.class)) {
                    BindOnClick annotation = method.getAnnotation(BindOnClick.class);
                    int[] value = annotation.value();
                    for (int id : value) {
                        View view = finder.findViewById(id);
                        if (view != null) {
                            Class viewClass = view.getClass();
                            try {
                                Method setOnClickListener = getMethod(viewClass, METHOD_SET_ONCLICK_LISTENER, new Class[]{View.OnClickListener.class});
                                //拦截方法
                                InjectInvocationHandler handler = new InjectInvocationHandler(owner);
                                //添加到拦截列表
                                handler.add(METHOD_ON_CLICK, method);
                                //得到监听的代理对象
                                Proxy proxy = (Proxy) Proxy.newProxyInstance(View.OnClickListener.class.getClassLoader(),
                                        new Class[]{View.OnClickListener.class}, handler);
                                if (setOnClickListener != null) {
                                    setOnClickListener.invoke(view, proxy);
                                }
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

    /**
     * 恢复带有{@link SaveState}注解的字段
     */
    private static void setSaveState(Object fieldOwner, @Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            for (Field field : BundleUtil.getSaveStateFields(fieldOwner)) {
                try {
                    SaveState annotation = field.getAnnotation(SaveState.class);
                    BundleUtil.restoreFromBundle(savedInstanceState, field.toString(), field, fieldOwner, annotation);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 保存带有{@link SaveState}注解的字段
     */
    public static void getSaveState(Object fieldOwner, Bundle outState) {
        for (Field field : BundleUtil.getSaveStateFields(fieldOwner)) {
            SaveState annotation = field.getAnnotation(SaveState.class);
            if (annotation != null) {
                try {
                    BundleUtil.saveToBundle(outState, field.toString(), field, fieldOwner, annotation);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 此方法需要在{@link Fragment#onCreateView(LayoutInflater, ViewGroup, Bundle)}方法中调用
     * 并且return返回值
     */

    public static <T extends Fragment> View inject(T t, LayoutInflater inflater, ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = checkBindContentViewAnnounce(t, inflater, container);
        if (view != null) {
            checkFindViewByIdAnnounce(t, view);
            checkBindOnClickAnnounce(new ViewFinder(t, view));
            setSaveState(t, savedInstanceState);
        }
        return view;
    }

    private static View checkBindContentViewAnnounce(@NonNull Fragment t, LayoutInflater inflater, ViewGroup container) {
        Class aClass = t.getClass();
        View contentView;
        while (aClass != Object.class) {
            if (aClass.isAnnotationPresent(BindContentView.class)) {
                try {
                    BindContentView annotation = (BindContentView) aClass.getAnnotation(BindContentView.class);
                    if (annotation != null) {
                        //获得内容布局ID
                        int value = annotation.value();
                        //将加载视图注入fragment的View身上
                        if (inflater != null && value > 0) {
                            contentView = inflater.inflate(value, container, false);
                            return contentView;
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    aClass = aClass.getSuperclass();
                }
            }
            aClass = aClass.getSuperclass();
        }
        return null;
    }

    private static <T extends Fragment> void checkFindViewByIdAnnounce(@NonNull T t, @NonNull View view) {
        Class aClass = t.getClass();
        while (aClass != Object.class) {
            Field[] fields = aClass.getDeclaredFields();
            for (Field field : fields) {
                try {
                    handlerFindViewById(new ViewFinder(t, view), field);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            aClass = aClass.getSuperclass();
        }
    }

    private static void handlerFindViewById(ViewFinder finder, Field field) {
        if (field.isAnnotationPresent(FindViewById.class)) {
            if (View.class.isAssignableFrom(field.getType())) {
                FindViewById annotation = field.getAnnotation(FindViewById.class);
                View viewById = finder.findViewById(annotation.value());
                try {
                    field.setAccessible(true);
                    field.set(finder.getOwner(), viewById);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
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
    private static Method getMethod(Class clazz, String methodName,
                                    final Class[] classes) throws Exception {
        Method method;
        try {
            method = clazz.getDeclaredMethod(methodName, classes);
        } catch (NoSuchMethodException e) {
            try {
                method = clazz.getMethod(methodName, classes);
            } catch (NoSuchMethodException ex) {
                if (clazz.getSuperclass() == null) {
                    return null;
                } else {
                    method = getMethod(clazz.getSuperclass(), methodName,
                            classes);
                }
            }
        }
        return method;
    }

    private static class ViewFinder {
        private View container = null;
        private Object fieldOwner = null;

        private ViewFinder(@NonNull Object fieldOwner, @NonNull View container) {
            this.container = container;
            this.fieldOwner = fieldOwner;
        }

        private View findViewById(int id) {
            return container == null ? null : container.findViewById(id);
        }

        private Object getOwner() {
            return fieldOwner;
        }

    }

    private static class InjectInvocationHandler implements InvocationHandler {
        //拦截的方法名列表
        private Map<String, Method> map = new HashMap<>();
        //方法拥有者
        private Object target;

        private InjectInvocationHandler(Object target) {
            this.target = target;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (target != null) {
                //获取方法名
                String name = method.getName();
                Method m = map.get(name);
                if (m != null) {//如果不存在与拦截列表，就执行
                    return m.invoke(target, args);
                }
            }
            return null;
        }

        /**
         * 向拦截列表里添加拦截的方法
         */
        private void add(String name, Method method) {
            map.put(name, method);
        }

    }

}
