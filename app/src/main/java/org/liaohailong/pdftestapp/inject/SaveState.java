package org.liaohailong.pdftestapp.inject;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 加上该注解，表示该字段需要保存和恢复。
 * <br/>泛型类型的检查，暂只支持一个参数的List。其它类型，如HashMap，无法通过检查保证，只能靠自觉了……
 * <br/>
 * Created by qumiaowin on 2016/6/8.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface SaveState {
    /**
     * 如果是List泛型，一定要指明参数类型。如ArrayList&lt;String&gt;，则需要指明 <code>parameterType=String.class</code>
     * @return
     */
    Class<?> parameterType() default DEFAULT.class;

    /**
     * 保存和恢复的顺序。越小越靠前，越大越靠后。
     * @return
     */
    int order() default 0;

    /**
     * 字段恢复时，会创建该返回类型的listener回调实例，并执行回调。
     * 注意：
     * <ul>
     *      <li>该类一定要继承自SaveStateListener，并且需要有默认的构造方法。</li>
     *      <li>如果该类为内部类，请务必将其声明为静态。</li>
     * </ul>
     *
     * 默认的回调是直接将字段（field）设置成需要恢复的值。
     * @return
     */
    Class<? extends SaveStateListener> listener() default SaveStateListener.Impl.class;


    final class DEFAULT {

    }
}
