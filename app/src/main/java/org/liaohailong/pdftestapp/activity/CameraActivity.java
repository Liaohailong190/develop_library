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

    private static final String mSavePath = Environment.getExternalStorageDirectory() + "/" + "camera_test";
    private SurfaceView mSurfaceView;
    private ImageView mImageView;
    private Button mRecordBtn;

    private CameraHelper mCameraHelper;
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
        View mSwitchBtn = findViewById(R.id.switch_btn);
        View mShotBtn = findViewById(R.id.shot_btn);
        mRecordBtn = findViewById(R.id.record_btn);
        resetRecordStatus();

        mSurfaceView.setOnClickListener(this);
        mImageView.setOnClickListener(this);
        mSwitchBtn.setOnClickListener(this);
        mShotBtn.setOnClickListener(this);
        mRecordBtn.setOnClickListener(this);

        initCamera();
    }

    private void resetRecordStatus() {
        mRecordBtn.setText("开始录像");
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

    private void initCamera() {
        if (mCameraHelper == null) {
            mCameraHelper = new CameraHelper.Builder()
                    .setActivity(this)
                    .setSurfaceView(mSurfaceView)
                    .setAutoFocus(true)
                    .setCameraOptCallback(mCameraOptCallback)
                    .setDirectoryPath(mSavePath)
                    .build();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.surface_view:
                if (mCameraHelper != null) {
                    mCameraHelper.focus();
                }
                break;
            case R.id.avatar_img:
                if (mImageView != null) {
                    mImageView.setImageBitmap(null);
                }
                break;
            case R.id.switch_btn:
                if (mCameraHelper != null) {
                    mCameraHelper.switchCamera();
                    resetRecordStatus();
                }
                break;
            case R.id.shot_btn:
                if (mCameraHelper != null) {
                    mCameraHelper.takePicture();
                }
                break;
            case R.id.record_btn:
                if (mCameraHelper != null) {
                    if (mCameraHelper.isRecording()) {
                        mCameraHelper.stopRecorder();
                        mRecordBtn.setText("开始录像");
                    } else {
                        if (mCameraHelper.startRecorder()) {
                            mRecordBtn.setText("停止录像");
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
