package org.liaohailong.pdftestapp;

import com.squareup.leakcanary.LeakCanary;

import org.liaohailong.library.RootApplication;
import org.liaohailong.pdftestapp.model.Student;

/**
 * 本项目application类
 * Created by LHL on 2017/9/6.
 */

public class VictorApplication extends RootApplication {

    /**
     * 数据库建表
     */
    private final Class[] SQL_TABLE = new Class[]{Student.class};

    @Override
    public void onCreate() {
        super.onCreate();
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);
    }

    @Override
    public Class[] getTableClasses() {
        return SQL_TABLE;
    }
}
