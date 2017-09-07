package org.liaohailong.library.inject;

import android.view.View;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 绑定长按事件的注解接口
 * Created by LHL on 2017/9/7.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@BaseEvent(listenerType = View.OnLongClickListener.class, listenerSetter = "setOnLongClickListener", methodName = "onLongClick")
public @interface OnLongClick {
    int[] value();
}
