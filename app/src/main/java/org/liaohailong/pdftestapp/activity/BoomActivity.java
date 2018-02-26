package org.liaohailong.pdftestapp.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import org.liaohailong.library.inject.BindView;
import org.liaohailong.library.inject.OnClick;
import org.liaohailong.pdftestapp.BaseActivity;
import org.liaohailong.pdftestapp.R;
import org.liaohailong.pdftestapp.widget.BombView;

/**
 * 炸弹爆炸
 * Created by LHL on 2017/10/28.
 */
public class BoomActivity extends BaseActivity {
    public static void show(Context context) {
        Intent intent = new Intent(context, BoomActivity.class);
        context.startActivity(intent);
    }

    @BindView(R.id.bomb_view)
    private BombView bombView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_boom);
    }

    @OnClick(R.id.bomb_view)
    private void boom(View view) {
        bombView.startAnim();
    }
}
