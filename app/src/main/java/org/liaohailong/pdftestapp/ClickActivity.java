package org.liaohailong.pdftestapp;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;

import org.liaohailong.pdftestapp.announce.OnClickListener;
import org.liaohailong.pdftestapp.announce.FindViewById;
import org.liaohailong.pdftestapp.http.HttpUtils;
import org.liaohailong.pdftestapp.http.OnHttpCallback;

import java.util.HashMap;
import java.util.Map;

/**
 * 点击事件测试
 * Created by LHL on 2017/9/5.
 */

public class ClickActivity extends BaseActivity implements View.OnClickListener {
    @FindViewById(R.id.toast_text)
    private TextView textView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_click);
    }

    @OnClickListener({R.id.btn_01, R.id.btn_02, R.id.btn_03, R.id.btn_04})
    @Override
    public void onClick(View v) {
        String toast = "";
        switch (v.getId()) {
            case R.id.btn_01:
                toast = "toast 01";
                break;
            case R.id.btn_02:
                toast = "toast 02";
                break;
            case R.id.btn_03:
                toast = "toast 03";
                break;
            case R.id.btn_04:
                toast = "请求网络";
                String url = "http://z.hidajian.com/api/charts/wall_data";
                Map<String, String> params = new HashMap<>();
                params.put("wall", "1529");
                HttpUtils.post(url, params, new OnHttpCallback<JsonObject>() {
                    @Override
                    public void onSuccess(JsonObject result) {
                        Toast.makeText(getApplicationContext(), result.toString(), Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onFail(int code, String info) {

                    }
                });

                break;
        }
        Toast.makeText(this, toast, Toast.LENGTH_LONG).show();
        if (textView != null) {
            textView.setText(toast);
        }
    }
}
