package org.liaohailong.library.inject;


import java.lang.reflect.Field;

/**
 * 该字段恢复时发生的回调
 * Created by qumiaowin on 2016/6/8.
 */
public abstract class SaveStateListener {

    public SaveStateListener() {

    }

    public abstract void onRestored(Field field, Object fieldOwner, Object oldValue, Object newValue) throws IllegalAccessException;


    public static class Impl extends SaveStateListener {

        @Override
        public void onRestored(Field field, Object fieldOwner, Object oldValue, Object newValue) throws IllegalAccessException {
            field.set(fieldOwner, newValue);
        }
    }
}
