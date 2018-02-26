package org.liaohailong.library.inject;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.liaohailong.library.util.BundleUtil;

import java.lang.annotation.Annotation;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.WeakHashMap;

/**
 * 注解使用类
 * Created by LHL on 2017/9/6.
 */

public class Victor {
    private Victor() throws IllegalAccessException {
        throw new IllegalAccessException("no instance!");
    }

    /**
     * 此方法需要放在{@link Activity#onCreate(Bundle)}中调用
     */
    public static <T extends Activity> void injectSaveState(T t, @Nullable Bundle savedInstanceState) {
        setSaveState(t, savedInstanceState);
    }

    /**
     * 反射找出视图，并且绑定相应事件，
     * 此方法需要放在:
     * {@link Activity#setContentView(View)},
     * {@link Activity#setContentView(int)},
     * {@link Activity#setContentView(View, ViewGroup.LayoutParams)}
     * 方法后调用。
     *
     * @param t   界面Activity
     * @param <T> 界面Activity
     */
    public static <T extends Activity> void injectViewAndEvent(T t) {
        ViewFinder viewFinder = new ViewFinder(t, t.getWindow().getDecorView());
        checkBindView(viewFinder);
        checkOnClick(viewFinder);
        checkOnLongClick(viewFinder);
    }

    /**
     * 此方法需要在{@link Fragment#onCreateView(LayoutInflater, ViewGroup, Bundle)}方法中调用
     * 并且return返回值
     */
    public static <T extends Fragment> View inject(T t, View contentView, @Nullable Bundle savedInstanceState) {
        setSaveState(t, savedInstanceState);
        if (contentView != null) {
            ViewFinder viewFinder = new ViewFinder(t, contentView);
            checkBindView(viewFinder);
            checkOnClick(viewFinder);
            checkOnLongClick(viewFinder);
        }
        return contentView;
    }

    /**
     * 此方法需要在{@link RecyclerView.ViewHolder}子类构造法中调用
     */
    public static <T extends RecyclerView.ViewHolder> void inject(T t) {
        checkBindView(new ViewFinder(t, t.itemView));
    }

    /**
     * 绑定视图
     *
     * @param finder 视图寻找者
     */
    private static void checkBindView(@NonNull ViewFinder finder) {
        Object owner = finder.getOwner();
        if (owner == null) {
            return;
        }
        Class aClass = owner.getClass();
        while (aClass != Object.class) {
            Field[] fields = aClass.getDeclaredFields();
            for (Field field : fields) {
                handlerFindViewById(finder, field);
            }
            aClass = aClass.getSuperclass();
        }
    }

    /**
     * 处理要绑定的视图
     *
     * @param finder 视图寻找者
     * @param field  视图字段，需要绑定的对象
     */
    private static void handlerFindViewById(ViewFinder finder, Field field) {
        if (field.isAnnotationPresent(BindView.class)) {
            if (View.class.isAssignableFrom(field.getType())) {
                BindView annotation = field.getAnnotation(BindView.class);
                View viewById = finder.findViewById(annotation.value());
                if (viewById != null) {
                    try {
                        field.setAccessible(true);
                        Object owner = finder.getOwner();
                        if (owner != null) {
                            field.set(owner, viewById);
                        }
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * 绑定点击事件
     *
     * @param finder 视图寻找者
     */
    private static void checkOnClick(ViewFinder finder) {
        Object owner = finder.getOwner();
        if (owner == null) {
            return;
        }
        Class aClass = owner.getClass();
        while (aClass != Object.class) {
            Method[] declaredMethods = aClass.getDeclaredMethods();
            for (Method method : declaredMethods) {
                if (method.isAnnotationPresent(OnClick.class)) {
                    OnClick annotation = method.getAnnotation(OnClick.class);
                    int[] value = annotation.value();
                    for (int id : value) {
                        View view = finder.findViewById(id);
                        if (view != null) {
                            handlerViewListener(owner, view, annotation, method);
                        }
                    }
                }
            }
            aClass = aClass.getSuperclass();
        }
    }

    /**
     * 绑定长按事件
     *
     * @param finder 视图寻找者
     */
    private static void checkOnLongClick(ViewFinder finder) {
        Object owner = finder.getOwner();
        if (owner == null) {
            return;
        }
        Class aClass = owner.getClass();
        while (aClass != Object.class) {
            Method[] declaredMethods = aClass.getDeclaredMethods();
            for (Method method : declaredMethods) {
                if (method.isAnnotationPresent(OnLongClick.class)) {
                    OnLongClick annotation = method.getAnnotation(OnLongClick.class);
                    int[] value = annotation.value();
                    for (int id : value) {
                        View view = finder.findViewById(id);
                        if (view != null) {
                            handlerViewListener(owner, view, annotation, method);
                        }
                    }
                }
            }
            aClass = aClass.getSuperclass();
        }
    }

    /**
     * 处理绑定视图相关监听
     *
     * @param owner      需绑定方法的拥有者
     * @param view       视图
     * @param annotation 方法绑定的接口
     * @param method     方法
     */
    private static void handlerViewListener(Object owner, View view, Annotation annotation, Method method) {
        Class viewClass = view.getClass();
        try {
            BaseEvent baseEvent = annotation.annotationType().getAnnotation(BaseEvent.class);
            String listenerSetter = baseEvent.listenerSetter();
            Class clazz = baseEvent.listenerType();
            String methodName = baseEvent.methodName();
            Method injectMethod = getMethod(viewClass, listenerSetter, new Class[]{clazz});
            //拦截方法
            InjectInvocationHandler handler = new InjectInvocationHandler(owner);
            //添加到拦截列表
            handler.add(methodName, method);
            //得到监听的代理对象
            Proxy proxy = (Proxy) Proxy.newProxyInstance(clazz.getClassLoader(),
                    new Class[]{clazz}, handler);
            if (injectMethod != null) {
                injectMethod.invoke(view, proxy);
            }
        } catch (Exception e) {
            e.printStackTrace();
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
        private WeakReference<View> container = null;
        private WeakReference<Object> fieldOwner = null;

        private ViewFinder(@NonNull Object fieldOwner, @NonNull View container) {
            this.container = new WeakReference<>(container);
            this.fieldOwner = new WeakReference<>(fieldOwner);
        }

        private View findViewById(int id) {
            if (container == null) {
                return null;
            }
            View view = container.get();
            if (view == null) {
                return null;
            }
            View target = view.findViewById(id);
            if (target == null) {
                return null;
            }
            return target;
        }

        private Object getOwner() {
            return fieldOwner == null || fieldOwner.get() == null ? null : fieldOwner.get();
        }

    }

    private static class InjectInvocationHandler implements InvocationHandler {
        //拦截的方法名列表
        private WeakHashMap<String, Method> map = new WeakHashMap<>();
        //方法拥有者
        private WeakReference<Object> target;

        private InjectInvocationHandler(Object target) {
            this.target = new WeakReference<>(target);
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (target != null && target.get() != null) {
                //获取方法名
                String name = method.getName();
                Method m = map.get(name);
                if (m != null) {//如果不存在与拦截列表，就执行
                    m.setAccessible(true);
                    return m.invoke(target.get(), args);
                }
            }
            return null;
        }

        /**
         * 向拦截列表里添加拦截的方法
         */
        private void add(String name, Method method) {
            if (map != null) {
                map.put(name, method);
            }
        }
    }
}
