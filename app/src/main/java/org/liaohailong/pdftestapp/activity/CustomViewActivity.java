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

    private PercentWavePie mPercentWavePie;

    public static void show(Context context) {
        Intent intent = new Intent(context, CustomViewActivity.class);
        context.startActivity(intent);
    }

    private int[] colors = {Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW, Color.CYAN};

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_view);
        mPercentWavePie = findViewById(R.id.wave_view);
        int fillColor = Color.parseColor("#294D99");
        mPercentWavePie.setFillColor(fillColor);
        mPercentWavePie.setShape(PercentWavePie.Shape.SQUARE);

        mPercentWavePie.setTextColorInWave(Color.WHITE);
        mPercentWavePie.setTextColorOutWave(fillColor);
        mPercentWavePie.setTextFontSize(52 * 3.0f);
        mPercentWavePie.setTextType(true);
        mPercentWavePie.setTextPosition(0.5f);


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

    private float currentProgress = 0.1f;

    private void setData(boolean plus) {
        LinkedList<WaveModel> waveModelLinkedList = new LinkedList<>();
        /*float progress = 0.1f;
        for (int color : colors) {
            WaveModel waveModel = new WaveModel(color);
            waveModel.setCnt(2);//波峰个数
            waveModel.setSwing(0.15f);//振幅
            waveModel.setPhase(90f);//初相
            waveModel.setPeriod(2f);//周期
            waveModel.setOpacity(0.8f);//透明度
            waveModel.setProgress(progress);//进度
            waveModelLinkedList.add(waveModel);
            progress += 0.1f;
        }*/

        currentProgress = plus ? currentProgress + 0.1f : currentProgress - 0.1f;
        currentProgress = currentProgress < 0.1f ? 0.1f : currentProgress;
        currentProgress = currentProgress > 1.0f ? 1.0f : currentProgress;
        WaveModel waveModel = new WaveModel(Color.RED);
        waveModel.setCnt(2);//波峰个数
        waveModel.setSwing(0.15f);//振幅
        waveModel.setPhase(90f);//初相
        waveModel.setPeriod(2f);//周期
        waveModel.setOpacity(0.8f);//透明度
        waveModel.setProgress(currentProgress);//进度
        waveModelLinkedList.add(waveModel);
        mPercentWavePie.setWaveModelList(waveModelLinkedList);
    }
}
