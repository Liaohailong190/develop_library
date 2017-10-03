package org.liaohailong.pdftestapp.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import org.liaohailong.library.widget.FileLoader;
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
    private static final int REQUEST_WRITE_EXTERNAL_STORAGE = 0x001;

    @BindView(R.id.toast_text)
    private TextView textView;

    //辅助类相关
    private Random random = new Random();
    //数据相关
    private MainFragment mainFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (PermissionUtil.requestPermissionIfNeed(this, PERMISSION_WRITE_EXTERNAL_STORAGE, "", REQUEST_WRITE_EXTERNAL_STORAGE)) {
            downloadFile();
        }
    }

    private void downloadFile() {
        FileLoader.getInstance().downloadFile("http://play.22mtv.com:1010/play4/42754.mp4", new FileLoader.OnFileStatusCallBackAdapter() {
            Toast toast = null;

            @Override
            public void onFileLoading(String tempFilePath, int progress) {
                if (toast == null) {
                    toast = Toast.makeText(MainActivity.this, "", Toast.LENGTH_SHORT);
                }
                toast.setText("文件下载 进度---> progress = " + progress);
                toast.show();
            }

            @Override
            public void onFileDownLoadComplete(String path) {
                if (toast != null) {
                    toast.setText("文件下载完毕 path + " + path);
                    toast.show();
                }
            }
        });
    }

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
        PermissionUtil.requestPermissionIfNeed(this, PERMISSION_WRITE_EXTERNAL_STORAGE, "", REQUEST_WRITE_EXTERNAL_STORAGE);
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


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //权限获取失败
        if (grantResults.length != 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        switch (requestCode) {
            case REQUEST_WRITE_EXTERNAL_STORAGE:
                downloadFile();
                break;
        }
    }
}
