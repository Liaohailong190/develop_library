package org.liaohailong.pdftestapp.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import org.liaohailong.library.inject.OnClick;
import org.liaohailong.pdftestapp.BaseActivity;
import org.liaohailong.pdftestapp.R;
import org.liaohailong.pdftestapp.widget.percentwave.PercentWavePie;
import org.liaohailong.pdftestapp.widget.percentwave.WaveModel;

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

    private int[] colors = {Color.GREEN, Color.BLUE, Color.YELLOW, Color.RED};

    private PercentWavePie mPercentWavePie;
    private float period = 2f;
    private float swing = 0.15f;
    private WaveModel.Direction direction = WaveModel.Direction.left2Right;
    private int cnt = 2;
    private boolean anim = true;

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

        setData();
    }

    @OnClick({R.id.plus_progress_btn, R.id.minus_progress_btn,
            R.id.plus_period_btn, R.id.minus_period_btn,
            R.id.plus_swing_btn, R.id.minus_swing_btn,
            R.id.left_to_right_btn, R.id.right_to_left_btn,
            R.id.plus_cnt_btn, R.id.minus_cnt_btn,
            R.id.anim_start_btn, R.id.anim_stop_btn,
            R.id.ring_btn, R.id.square_btn, R.id.round_square_btn})
    public void wave(View v) {
        switch (v.getId()) {
            case R.id.plus_progress_btn://水位线上涨
                currentProgress = currentProgress + 0.1f;
                currentProgress = currentProgress > 1.0f ? 1.0f : currentProgress;
                refresh();
                break;
            case R.id.minus_progress_btn://水位线下降
                currentProgress = currentProgress - 0.1f;
                currentProgress = currentProgress < 0.1f ? 0.1f : currentProgress;
                refresh();
                break;
            case R.id.plus_period_btn://周期增加
                period += 0.25f;
                period = period > 60f ? 60f : period;
                refresh();
                break;
            case R.id.minus_period_btn://周期减少
                period -= 0.25f;
                period = period < 0.5f ? 0.5f : period;
                refresh();
                break;
            case R.id.plus_swing_btn://振幅增加
                swing += 0.1f;
                swing = swing > 1.0f ? 1.0f : swing;
                refresh();
                break;
            case R.id.minus_swing_btn://振幅减少
                swing -= 0.1f;
                swing = swing < 0.1f ? 0.1f : swing;
                refresh();
                break;
            case R.id.left_to_right_btn://波浪从左到右
                direction = WaveModel.Direction.left2Right;
                refresh();
                break;
            case R.id.right_to_left_btn://波浪从右到左
                direction = WaveModel.Direction.right2Left;
                refresh();
                break;
            case R.id.plus_cnt_btn://波峰增加
                cnt++;
                cnt = cnt > 10 ? 10 : cnt;
                refresh();
                break;
            case R.id.minus_cnt_btn://波峰减少
                cnt--;
                cnt = cnt < 2 ? 2 : cnt;
                refresh();
                break;
            case R.id.anim_start_btn://动画开启
                anim = true;
                refresh();
                break;
            case R.id.anim_stop_btn://动画停止
                anim = false;
                refresh();
                break;
            case R.id.ring_btn://圆形样式
                mPercentWavePie.setShape(PercentWavePie.Shape.RING);
                break;
            case R.id.square_btn://正方形样式
                mPercentWavePie.setShape(PercentWavePie.Shape.SQUARE);
                break;
            case R.id.round_square_btn://圆角正方形样式
                mPercentWavePie.setShape(PercentWavePie.Shape.ROUND_SQUARE);
                break;
        }
    }

    private float currentProgress = 0.0f;

    private void setData() {
        float progress = 0.15f;
        LinkedList<WaveModel> waveModelLinkedList = new LinkedList<>();
        for (int color : colors) {
            WaveModel waveModel = new WaveModel(color)
                    .setCnt(2)//波峰个数
                    .setSwing(0.15f)//振幅
                    .setPhase(90f)//初相
                    .setPeriod(2f)//周期
                    .setOpacity(0.55f)//透明度
                    .setProgress(progress);//进度
            waveModelLinkedList.add(waveModel);
            progress += 0.12f;
        }
        mPercentWavePie.setWaveModelList(waveModelLinkedList);

        /*int fillColor = Color.parseColor("#294D99");
        LinkedList<WaveModel> waveModelList = mPercentWavePie.getWaveModelList();
        if (waveModelList.isEmpty()) {
            LinkedList<WaveModel> data = new LinkedList<>();
            WaveModel waveModel = new WaveModel(fillColor)
                    .setCnt(cnt)//波峰个数
                    .setSwing(swing)//振幅
                    .setPhase(90f)//初相
                    .setPeriod(period)//周期
                    .setOpacity(0.8f)//透明度
                    .setProgress(currentProgress);//进度
            data.add(waveModel);
            mPercentWavePie.setWaveModelList(data);
        }*/
    }

    private void refresh() {
        LinkedList<WaveModel> waveModelList = mPercentWavePie.getWaveModelList();
        if (!waveModelList.isEmpty()) {
            waveModelList.get(0).setCnt(cnt)//波峰个数
                    .setSwing(swing)//振幅
                    .setPhase(90f)//初相
                    .setPeriod(period)//周期
                    .setOpacity(0.8f)//透明度
                    .setAnim(anim)//是否开启动画
                    .setDirection(direction)//动画方向
                    .animToProgress(currentProgress)//进度
                    .notifyDataSetChanged();//刷新配置信息
        }
    }
}
