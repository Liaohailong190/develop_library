package org.liaohailong.pdftestapp;

import android.graphics.Color;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.liaohailong.library.inject.BindContentView;
import org.liaohailong.library.inject.OnClick;
import org.liaohailong.library.inject.BindView;
import org.liaohailong.library.inject.OnLongClick;

import java.util.Random;

/**
 * 点击事件测试
 * Created by LHL on 2017/9/5.
 */
@BindContentView(R.layout.activity_main)
public class MainActivity extends BaseActivity {
    private static int[] COLORS = new int[]{Color.RED, Color.GREEN, Color.BLUE};

    @BindView(R.id.toast_text)
    private TextView textView;

    //辅助类相关
    private Random random = new Random();
    //数据相关
    private MainFragment mainFragment;

    @OnClick({R.id.btn_01, R.id.btn_02})
    public void showToast(View v) {
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

    @OnClick(R.id.btn_03)
    public void changeColor(View view) {
        int i = random.nextInt(COLORS.length);
        textView.setTextColor(COLORS[i]);
    }

    @OnLongClick(R.id.btn_04)
    private boolean initFragment(View view) {
        if (mainFragment != null) {
            return false;
        }
        try {
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            mainFragment = new MainFragment();
            ft.replace(R.id.frame_layout, mainFragment, mainFragment.getClass().getName());
            ft.commit();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return true;
    }

}
