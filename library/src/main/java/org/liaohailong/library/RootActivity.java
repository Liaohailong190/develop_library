package org.liaohailong.library;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

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
        Victor.inject(this, savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Victor.getSaveState(this, outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ImageLoader.getInstance().clear();
        Http.clearTask(null);
    }
}
