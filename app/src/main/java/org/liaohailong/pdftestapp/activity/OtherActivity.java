package org.liaohailong.pdftestapp.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import org.liaohailong.library.inject.OnClick;
import org.liaohailong.pdftestapp.BaseActivity;
import org.liaohailong.pdftestapp.R;

/**
 * Describe as : 其他Activity的跳转入口
 * Created by LHL on 2018/4/10.
 */

public class OtherActivity extends BaseActivity {
    public static void show(Context context) {
        Intent intent = new Intent(context, OtherActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other);
    }

    @OnClick({R.id.gl_btn, R.id.custom_view_btn, R.id.gift_list_btn})
    public void jump(View v) {
        switch (v.getId()) {
            case R.id.gl_btn:
                GLActivity.show(v.getContext());
                break;
            case R.id.custom_view_btn:
                PercentWavePieActivity.show(v.getContext());
                break;
            case R.id.gift_list_btn:
                GiftListActivity.show(v.getContext());
                break;
        }
    }
}
