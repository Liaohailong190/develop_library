package org.liaohailong.pdftestapp.activity;

import android.Manifest;
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
import org.liaohailong.library.util.PermissionUtil;
import org.liaohailong.pdftestapp.BaseActivity;
import org.liaohailong.pdftestapp.R;
import org.liaohailong.pdftestapp.fragment.MainFragment;

import java.util.Random;

/**
 * 点击事件测试
 * Created by LHL on 2017/9/5.
 */
@BindContentView(R.layout.activity_main)
public class MainActivity extends BaseActivity {
    private static int[] COLORS = new int[]{Color.RED, Color.GREEN, Color.BLUE};
    private static final String PERMISSION_WRITE_EXTERNAL_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;

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
                PdfActivity.show(v.getContext());
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
        PermissionUtil.requestPermissionIfNeed(this, PERMISSION_WRITE_EXTERNAL_STORAGE, "", 0);
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
