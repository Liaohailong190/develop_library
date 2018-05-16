package org.liaohailong.pdftestapp.widget.percentwave;

import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.support.annotation.ColorInt;
import android.view.animation.AccelerateDecelerateInterpolator;

/**
 * Describe as : 波浪模型 {@link PercentWavePie}
 * Created by LHL on 2018/5/15.
 */

public class WaveModel {
    private static final int ANIM_DURATION = 4000;
    //波浪顺序，用来控制波浪的错位感觉
    private int index = 1;
    //波浪进度 (水位线高度) 取值[0.0f,1.0f] 默认0.05f
    private float progress = 0.05f;
    //绘制区域
    private Rect rect;
    //波峰个数 取值[2,10] 默认2
    private int cnt = 2;
    //振幅 取值[0.1f,1.0f] 默认0.15f
    private float swing = 0.15f;
    //相位 取值[0,360] 默认90
    private float phase = 90;
    //波浪周期 取值[0.5f,60f] 默认2
    private float period = 2;
    //透明度 取值[0.0f,1.0f] 默认0.8f
    private float opacity = 0.8f;
    //是否开启动画
    private boolean anim = true;
    //动画方向 默认从左到右
    private Direction direction = Direction.left2Right;

    //绘制基础类相关
    private Paint paint;
    private Path path;

    private int width = 0;
    private int height = 0;
    private float frequency = 0f;//角速度
    private float factor = 0f;//周期控制速度
    private float firstPointY = 0f;//第一个波浪起点Y
    private float amplitude = 0.0f;//振幅

    //动画相关
    private ValueAnimator progressAnim;

    public WaveModel(@ColorInt int color) {
        this.rect = new Rect();
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setColor(color);
        path = new Path();
        progressAnim = ValueAnimator.ofFloat(0.0f, 1.0f);
        progressAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        progressAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float animatedValue = (float) animation.getAnimatedValue();
                setProgress(animatedValue);
            }
        });

        //初始化默认值
        setCnt(2);
        setSwing(0.15f);
        setPhase(90);
        setPeriod(2f);
        setOpacity(0.25f);
        setAnim(true);
        setDirection(Direction.left2Right);
        setProgress(0.05f);
    }

    void setIndex(int index) {
        index = index < 1 ? 1 : index;
        this.index = index;
    }

    public WaveModel setColor(@ColorInt int color) {
        paint.setColor(color);
        return this;
    }

    /**
     * @param progress 取值范围 [0.0f,1.0f]
     */
    public WaveModel setProgress(float progress) {
        progress = progress < 0.0f ? 0.0f : progress;
        progress = progress > 1.0f ? 1.0f : progress;
        this.progress = 1.0f - progress;//反向处理水位线
        return this;
    }

    float getProgress() {
        return Math.abs(progress - 1.0f);
    }

    /**
     * @param progress 目标进度(水位线) 取值范围[0.0f,1.0f]
     */
    public WaveModel animToProgress(float progress) {
        if (progressAnim.isRunning()) {
            progressAnim.pause();
        }
        float current = getProgress();
        float offset = Math.abs(progress - current);
        if (offset > 0) {
            progressAnim.setFloatValues(current, progress);
            progressAnim.setDuration((long) (offset * ANIM_DURATION));
            progressAnim.start();
        }
        return this;
    }

    void setRect(int left, int top, int right, int bottom) {
        this.rect.set(left, top, right, bottom);
    }

    /**
     * @param cnt 波峰个数 取值[2,10] 默认2
     */
    public WaveModel setCnt(int cnt) {
        cnt = cnt < 2 ? 2 : cnt;
        cnt = cnt > 10 ? 10 : cnt;
        this.cnt = cnt;
        return this;
    }

    /**
     * @param swing 振幅 取值[0.1f,1.0f] 默认0.15f
     */
    public WaveModel setSwing(float swing) {
        swing = swing < 0.1f ? 0.1f : swing;
        swing = swing > 1.0f ? 1.0f : swing;
        this.swing = swing;
        return this;
    }

    /**
     * @param phase 相位 取值[0,360] 默认90
     */
    public WaveModel setPhase(float phase) {
        phase = phase < 0 ? 0 : phase;
        phase = phase > 360 ? 360 : phase;
        this.phase = phase;
        return this;
    }

    /**
     * @param period 波浪周期 取值[0.5f,60f] 默认2
     */
    public WaveModel setPeriod(float period) {
        period = period < 0.5f ? 0.5f : period;
        period = period > 60f ? 60f : period;
        this.period = period;
        return this;
    }

    /**
     * @param opacity 透明度 取值[0.0f,1.0f] 默认0.8f
     */
    public WaveModel setOpacity(float opacity) {
        opacity = opacity < 0.0f ? 0.0f : opacity;
        opacity = opacity > 1.0f ? 1.0f : opacity;
        this.opacity = opacity;
        int alpha = Math.round(255f * opacity);
        paint.setAlpha(alpha);
        return this;
    }

    /**
     * @param anim 是否开启动画
     */
    public WaveModel setAnim(boolean anim) {
        this.anim = anim;
        return this;
    }

    /**
     * @param direction 动画方向 默认从左到右
     */
    public WaveModel setDirection(Direction direction) {
        this.direction = direction;
        return this;
    }

    /**
     * 刷新配置信息，不要忘记在最后调用！
     */
    public void notifyDataSetChanged() {
        width = rect.width();
        height = rect.height();

        firstPointY = progress * height;//波浪起始点Y坐标
        frequency = (float) (cnt * 2.0f * Math.PI / width);//(角速度)
        factor = 30f / period;//周期控制速度
        amplitude = swing * 0.25f;//振幅
    }

    private float offset = 0.0f;

    void draw(Canvas canvas) {
        path.reset();
        path.moveTo(0, firstPointY);

        //设置动画偏移
        if (anim) {
            offset = direction == Direction.left2Right ? offset - factor : offset + factor;
        }

        /*
        * y=A*sin(ωx+φ)+k
        * A——振幅，当物体作轨迹符合正弦曲线的直线往复运动时，其值为行程的1/2。
        * (ωx+φ)——相位，反映变量y所处的状态。
        * φ——初相，x=0时的相位；反映在坐标系上则为图像的左右移动。
        * k——偏距，反映在坐标系上则为图像的上移或下移。
        * ω——角速度， 控制正弦周期(单位角度内震动的次数)。
        * */
        float offsetAngle = phase + offset + 135f * index;//偏移角度
        float initialPhase = (float) (offsetAngle * Math.PI / 180f);//初相
        for (int i = 0; i < width; i++) {
            float radians = i * frequency + initialPhase;//运动角度
            float sin = (float) (amplitude * Math.sin(radians)
                    + 0.125f * (1.0f - progress));
            /*
            * ↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑
            * + 0.125f * (1.0f - progress))这段是对波浪进行偏移量的修正，
            * 确保波浪在0%的时候有一点点波峰，看得见波动的感觉。
            * 确保波浪在100%的时候也有一点点波谷，看得见波动的感觉。
            * 以上判断是参考DataV的水纹波浪图产生的，仅是大概相似，非绝对相似。
            * */
            float sinY = sin + progress;
            float y = sinY * height;
            path.lineTo(i, y);
        }
        path.lineTo(width, height);
        path.lineTo(0, height);
        path.close();
        canvas.drawPath(path, paint);
    }

    Path getPath() {
        return path;
    }

    public enum Direction {
        left2Right,//从左到右
        right2Left //从右到左
    }
}
