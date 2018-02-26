package org.liaohailong.library;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;

import org.liaohailong.library.http.Http;
import org.liaohailong.library.image.ImageLoader;
import org.liaohailong.library.inject.Victor;

/**
 * 所有APP基类 项目BaseActivity需继承此类
 * Created by LHL on 2017/9/6.
 */

public class RootActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Victor.injectSaveState(this, savedInstanceState);
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);
        Victor.injectViewAndEvent(this);
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        Victor.injectViewAndEvent(this);
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        super.setContentView(view, params);
        Victor.injectViewAndEvent(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Victor.getSaveState(this, outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ImageLoader.instance.clear(this);
        Http.clearTask(null);
    }
}
