package org.liaohailong.pdftestapp.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import org.liaohailong.library.widget.camera.CameraHelper;
import org.liaohailong.library.widget.camera.CameraOptCallback;
import org.liaohailong.library.widget.camera.CameraOptCallbackAdapter;
import org.liaohailong.library.widget.camera.CameraUtil;
import org.liaohailong.pdftestapp.BaseActivity;
import org.liaohailong.pdftestapp.R;

/**
 * Describe as : 相机操作界面
 * Created by LHL on 2018/4/7.
 */

public class CameraActivity extends BaseActivity implements View.OnClickListener {
    public static void show(Context context) {
        Intent intent = new Intent(context, CameraActivity.class);
        context.startActivity(intent);
    }

    private static final String mSavePath = Environment.getExternalStorageDirectory() + "/" + "camera_test";//相机拍照/录像缓存路径
    private SurfaceView mSurfaceView;//预览界面
    private ImageView mImageView;//拍照图片展示
    private Button mRecordBtn;//录制按钮

    //相机操作类
    private CameraHelper mCameraHelper;
    //相机操作数据回调
    private CameraOptCallback mCameraOptCallback = new CameraOptCallbackAdapter() {
        @Override
        public void onPictureComplete(String path, Bitmap bitmap) {
            if (mImageView != null) {
                mImageView.setImageBitmap(bitmap);
            }
            Toast.makeText(CameraActivity.this, "图片保存路径 = " + path, Toast.LENGTH_LONG).show();
        }

        @Override
        public void onVideoRecordComplete(String path) {
            Toast.makeText(CameraActivity.this, "录像保存路径 = " + path, Toast.LENGTH_LONG).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        mSurfaceView = findViewById(R.id.surface_view);
        mImageView = findViewById(R.id.avatar_img);
        View switchBtn = findViewById(R.id.switch_btn);
        View shotBtn = findViewById(R.id.shot_btn);
        mRecordBtn = findViewById(R.id.record_btn);
        resetRecordStatus();

        mSurfaceView.setOnClickListener(this);
        mImageView.setOnClickListener(this);
        switchBtn.setOnClickListener(this);
        shotBtn.setOnClickListener(this);
        mRecordBtn.setOnClickListener(this);

        initCamera();
    }

    /**
     * 重置录像按钮的状态
     */
    private void resetRecordStatus() {
        mRecordBtn.setText("开始录像");
    }

    /**
     * 初始化相机操作类
     * build()方法一旦调用，就会启用相机
     */
    private void initCamera() {
        if (mCameraHelper == null) {
            mCameraHelper = new CameraHelper.Builder()
                    .setActivity(this)
                    .setSurfaceView(mSurfaceView)
                    .setAutoFocus(true)//默认开启，3秒一次对焦
                    .setCameraOptCallback(mCameraOptCallback)//相机操作回调
                    .setDirectoryPath(mSavePath)//缓存路径
                    .build();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mCameraHelper != null) {
            mCameraHelper.onStart();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mCameraHelper != null) {
            mCameraHelper.onStop();
        }
        resetRecordStatus();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCameraHelper != null) {
            mCameraHelper.onDestroy();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.surface_view://手动对焦
                if (mCameraHelper != null) {
                    mCameraHelper.focus();
                }
                break;
            case R.id.avatar_img://重置预览图
                if (mImageView != null) {
                    mImageView.setImageBitmap(null);
                }
                break;
            case R.id.switch_btn://切换前后置摄像头
                if (mCameraHelper != null) {
                    mCameraHelper.switchCamera();
                    resetRecordStatus();
                }
                break;
            case R.id.shot_btn://拍照
                if (mCameraHelper != null) {
                    mCameraHelper.takePicture();
                }
                break;
            case R.id.record_btn://录像
                if (mCameraHelper != null) {
                    if (mCameraHelper.isRecording()) {
                        mCameraHelper.stopRecorder();
                        mRecordBtn.setText("开始录像");
                    } else {
                        if (mCameraHelper.startRecorder()) {
                            mRecordBtn.setText("结束录像");
                        }
                    }
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //激活摄像头
        if (CameraUtil.isCameraPermissionGranted(requestCode, grantResults)) {
            if (mCameraHelper != null) {
                mCameraHelper.onStart();
            }
        }
    }
}
