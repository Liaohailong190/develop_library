package org.liaohailong.library;

import android.app.Application;

/**
 * 所有APP基类 项目的Application类需要集成此类
 * Created by LHL on 2017/9/6.
 */

public class RootApplication extends Application {

    static RootApplication INSTANCE;
    protected static Class[] TABLE_CLASSES = {};

    public static RootApplication getInstance() {
        return INSTANCE;
    }

    /**
     * @return 数据库名
     */
    public static String DB_NAME() {
        String packageName = getInstance().getPackageName();
        if (packageName.contains(".")) {
            String[] split = packageName.split("\\.");
            packageName = split[split.length - 1];
        }
        packageName = packageName + ".db";
        return packageName;
    }

    /**
     * @return 需要建多少表
     */
    public static Class[] TABLE_CLASSES() {
        return TABLE_CLASSES;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        INSTANCE = this;
    }
}