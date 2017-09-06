package org.liaohailong.pdftestapp;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;

import org.liaohailong.pdftestapp.inject.BindContentView;
import org.liaohailong.pdftestapp.inject.BindOnClick;
import org.liaohailong.pdftestapp.inject.FindViewById;
import org.liaohailong.pdftestapp.http.HttpUtils;
import org.liaohailong.pdftestapp.http.OnHttpCallback;

import java.util.HashMap;
import java.util.Map;

/**
 * 点击事件测试
 * Created by LHL on 2017/9/5.
 */
@BindContentView(R.layout.activity_main)
public class MainActivity extends BaseActivity implements View.OnClickListener {
    @FindViewById(R.id.toast_text)
    private TextView textView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initFragment();
    }

    private void initFragment() {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        MainFragment mainFragment = new MainFragment();
        ft.replace(R.id.frame_layout, mainFragment, mainFragment.getClass().getName());
        ft.commit();
    }

    @BindOnClick({R.id.btn_01, R.id.btn_02})
    @Override
    public void onClick(View v) {
        String toast = "";
        switch (v.getId()) {
            case R.id.btn_01:
                toast = "我是按钮一号，我弹出了吐司!!";
                break;
            case R.id.btn_02:
                toast = "我是按钮二号，我也会弹吐司!!";
                break;
        }
        Toast.makeText(this, toast, Toast.LENGTH_LONG).show();
        if (textView != null) {
            textView.setText(toast);
        }
    }
}
