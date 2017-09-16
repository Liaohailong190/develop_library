package org.liaohailong.pdftestapp;

/**
 * JNI测试
 * Created by LHL on 2017/9/16.
 */

public class JNI {
    static {
        System.loadLibrary("jni_test");
    }
    public static native String getHelloWord();
    public static native int addCalc(int a,int b);
}
