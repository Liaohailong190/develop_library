package org.liaohailong.pdftestapp.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import org.liaohailong.pdftestapp.BaseActivity;
import org.liaohailong.pdftestapp.R;
import org.liaohailong.pdftestapp.widget.wave.PercentWavePie;
import org.liaohailong.pdftestapp.widget.wave.WaveModel;

import java.util.LinkedList;

/**
 * Describe as: 展示自定义视图的界面
 * Created by LiaoHaiLong on 2018/5/14.
 */

public class CustomViewActivity extends BaseActivity {
    public static void show(Context context) {
        Intent intent = new Intent(context, CustomViewActivity.class);
        context.startActivity(intent);
    }

    private PercentWavePie mPercentWavePie;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_view);
        mPercentWavePie = findViewById(R.id.wave_view);
        int fillColor = Color.parseColor("#294D99");
        mPercentWavePie.setBorderColor(fillColor);//设置填充颜色（边界线的颜色）
//        mPercentWavePie.setBorderWidth();//设置边界线厚度 = outside_diameter - inside_diameter
//        mPercentWavePie.setSpace();//设置边框与波浪之前的间隙 0%~50%
        mPercentWavePie.setShape(PercentWavePie.Shape.SQUARE);//多值波浪图形状
        mPercentWavePie.setTextColorOutWave(fillColor);//文字单独颜色
        mPercentWavePie.setTextColorInWave(Color.WHITE);//波纹与文字混色
        mPercentWavePie.setTextFontSize(52 * 3.0f);//字体大小 PX单位
        mPercentWavePie.setTextType(true);//是否粗体
        mPercentWavePie.setTextPosition(0.5f);//文本绘制位置（从底部往上走，值越小，位置越靠底部，反之）

        setData(true);

        findViewById(R.id.plus_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setData(true);
            }
        });
        findViewById(R.id.minus_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setData(false);
            }
        });

        findViewById(R.id.ring_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPercentWavePie.setShape(PercentWavePie.Shape.RING);
            }
        });
        findViewById(R.id.square_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPercentWavePie.setShape(PercentWavePie.Shape.SQUARE);
            }
        });
        findViewById(R.id.round_square_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPercentWavePie.setShape(PercentWavePie.Shape.ROUND_SQUARE);
            }
        });
    }

    private float currentProgress = 0.0f;

    private void setData(boolean plus) {
        int fillColor = Color.parseColor("#294D99");
        /*float progress = 0.1f;
        for (int i = 0; i < 5; i++) {
            WaveModel waveModel = new WaveModel(fillColor);
            waveModel.setCnt(2);//波峰个数
            waveModel.setSwing(0.15f);//振幅
            waveModel.setPhase(90f);//初相
            waveModel.setPeriod(2f);//周期
            waveModel.setOpacity(0.55f);//透明度
            waveModel.setProgress(progress);//进度
            waveModelLinkedList.add(waveModel);
            progress += 0.12f;
        }*/

        currentProgress = plus ? currentProgress + 0.1f : currentProgress - 0.1f;
        currentProgress = currentProgress < 0.1f ? 0.1f : currentProgress;
        currentProgress = currentProgress > 1.0f ? 1.0f : currentProgress;

        LinkedList<WaveModel> waveModelList = mPercentWavePie.getWaveModelList();
        if (waveModelList.isEmpty()) {
            LinkedList<WaveModel> data = new LinkedList<>();
            WaveModel waveModel = new WaveModel(fillColor);
            waveModel.setCnt(2);//波峰个数
            waveModel.setSwing(0.15f);//振幅
            waveModel.setPhase(90f);//初相
            waveModel.setPeriod(2f);//周期
            waveModel.setOpacity(0.8f);//透明度
            waveModel.setProgress(currentProgress);//进度
            data.add(waveModel);
            mPercentWavePie.setWaveModelList(data);
        } else {
            WaveModel cache = waveModelList.get(0);
            cache.setCnt(2);//波峰个数
            cache.setSwing(0.15f);//振幅
            cache.setPhase(90f);//初相
            cache.setPeriod(2f);//周期
            cache.setOpacity(0.8f);//透明度
            cache.animToProgress(currentProgress);//进度
        }
    }
}
