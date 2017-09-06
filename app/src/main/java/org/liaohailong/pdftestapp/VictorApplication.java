package org.liaohailong.pdftestapp;

import org.liaohailong.library.RootApplication;
import org.liaohailong.pdftestapp.model.Student;

/**
 * 本项目application类
 * Created by LHL on 2017/9/6.
 */

public class VictorApplication extends RootApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        TABLE_CLASSES = new Class[]{Student.class};
    }
}
