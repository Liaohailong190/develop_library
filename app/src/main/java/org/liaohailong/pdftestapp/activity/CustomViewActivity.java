package org.liaohailong.pdftestapp.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;

import org.liaohailong.pdftestapp.BaseActivity;
import org.liaohailong.pdftestapp.R;
import org.liaohailong.pdftestapp.widget.wave.WaterWaveView;
import org.liaohailong.pdftestapp.widget.wave.WaterWaveHelper;

/**
 * Describe as: 展示自定义视图的界面
 * Created by LiaoHaiLong on 2018/5/14.
 */

public class CustomViewActivity extends BaseActivity {

    public static void show(Context context) {
        Intent intent = new Intent(context, CustomViewActivity.class);
        context.startActivity(intent);
    }

    private WaterWaveHelper mWaveHelper;
    private int index = 0;
    private float[] progresses = new float[]{0.15f, 0.25f, 0.5f, 0.75f, 0.85f};

    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable progressRunnable = new Runnable() {
        @Override
        public void run() {
            float progress = progresses[index % progresses.length];
            mWaveHelper.setProgress(progress);
            index++;
            handler.postDelayed(progressRunnable, 3000);
        }
    };
    private Runnable initRunnable = new Runnable() {

        @Override
        public void run() {
            WaterWaveView waterWaveView = findViewById(R.id.wave_view);
            try {
                waterWaveView.setDrawable(getResources().getDrawable(R.drawable.wave_img));
                waterWaveView.setShape(WaterWaveView.Shape.DRAWABLE);
                mWaveHelper = new WaterWaveHelper(waterWaveView);
                handler.postDelayed(progressRunnable, 3000);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_view);
        handler.post(initRunnable);
    }
}
