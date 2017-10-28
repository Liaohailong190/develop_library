package org.liaohailong.pdftestapp.activity;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import org.liaohailong.library.inject.BindContentView;
import org.liaohailong.library.inject.BindView;
import org.liaohailong.library.inject.OnClick;
import org.liaohailong.pdftestapp.BaseActivity;
import org.liaohailong.pdftestapp.R;
import org.liaohailong.pdftestapp.widget.BombView;

/**
 * 炸弹爆炸
 * Created by LHL on 2017/10/28.
 */
@BindContentView(R.layout.activity_boom)
public class BoomActivity extends BaseActivity {
    public static void show(Context context) {
        Intent intent = new Intent(context, BoomActivity.class);
        context.startActivity(intent);
    }

    @BindView(R.id.bomb_view)
    private BombView bombView;

    @OnClick(R.id.bomb_view)
    private void boom(View view) {
        bombView.startAnim();
    }
}
