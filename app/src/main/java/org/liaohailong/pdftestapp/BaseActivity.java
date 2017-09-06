package org.liaohailong.pdftestapp;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import org.liaohailong.pdftestapp.inject.Victor;

/**
 * 所有Activity基类
 * Created by LHL on 2017/9/5.
 */

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Victor.inject(this);
    }


}
