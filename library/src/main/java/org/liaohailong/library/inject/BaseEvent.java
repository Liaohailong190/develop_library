package org.liaohailong.library.inject;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 基本注解接口
 * Created by LHL on 2017/9/7.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface BaseEvent {

    Class<?> listenerType();

    String listenerSetter();

    String methodName();
}
