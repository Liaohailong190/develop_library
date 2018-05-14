package org.liaohailong.pdftestapp.widget.wave;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Shader;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * Describe as : 水波纹视图
 * y=A*sin(ωx+φ)+k
 * A——振幅，当物体作轨迹符合正弦曲线的直线往复运动时，其值为行程的1/2。
 * (ωx+φ)——相位，反映变量y所处的状态。
 * φ——初相，x=0时的相位；反映在坐标系上则为图像的左右移动。
 * k——偏距，反映在坐标系上则为图像的上移或下移。
 * ω——角速度， 控制正弦周期(单位角度内震动的次数)。
 * Created by LHL on 2018/5/14.
 */

public class WaterWaveView extends View {
    // 默认属性值
    private static final int DEFAULT_AMPLITUDE = 50;
    private static final int DEFAULT_PERIOD = 1000 / 60;//绘制间隔  1秒钟60次
    private static final float DEFAULT_FREQUENCY = 0.16F / 360F;

    private Paint mPaint;
    private Paint mBackgroundPaint;

    private int mWidth;//视图的宽度
    private int mHeight;//视图的高度
    private float mRadius;//圆形的半径


    @ColorInt
    private int mBackColor;
    @ColorInt
    private int mFrontColor;
    private BitmapShader mWaveShader;
    private Matrix mShaderMatrix;//渲染器动画矩阵
    private Shape mShape = Shape.round;

    public WaterWaveView(Context context) {
        this(context, null);
    }

    public WaterWaveView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WaterWaveView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    private void init() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mBackgroundPaint = new Paint();
        mBackgroundPaint.setAntiAlias(true);
        mBackgroundPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mShaderMatrix = new Matrix();

        //初始化一些默认属性
        setWaveColor(Color.parseColor("#B0B0E8"));
        setWaveBackgroundColor(Color.parseColor("#40eeeeee"));
    }

    public void setWaveColor(@ColorInt int waveColor) {
        int red = Color.red(waveColor);
        int green = Color.green(waveColor);
        int blue = Color.blue(waveColor);
        mBackColor = Color.argb(75, red, green, blue);
        mFrontColor = Color.argb(50, red, green, blue);
        if (mWidth > 0 && mHeight > 0) createShader();
    }

    public void setWaveBackgroundColor(@ColorInt int backgroundColor) {
        mBackgroundPaint.setColor(backgroundColor);
    }


    public void setShape(Shape mShape) {
        this.mShape = mShape;
        if (mWidth > 0 && mHeight > 0) createShader();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mWidth = w;
        mHeight = h;
        mRadius = mWidth > mHeight ? mHeight / 2f : mWidth / 2f;
        //初始化渲染器
        if (mWidth > 0 && mHeight > 0) createShader();
    }


    private static final float DEFAULT_WAVE_LENGTH_RATIO = 1.0f;
    private static final float DEFAULT_AMPLITUDE_RATIO = 0.05f;
    private static final float DEFAULT_WATER_LEVEL_RATIO = 0.5f;

    private float progress = 0.55f;
    private float amplitude = 1.0f;//振幅，波浪最高浪与最低浪的中间差
    private float waveLength = 1.0f;////波浪单个的长度，影响画面中有几个浪头
    private float waveShift = 0.05f;//波浪的水平移动速度

    /**
     * @param progress [0.0f,1.0f]
     */
    public void setProgress(float progress) {
        progress = progress < 0.0f ? 0.0f : progress;
        progress = progress > 1.0f ? 1.0f : progress;
        this.progress = 1.0f - progress;
    }

    public float getProgress() {
        return progress;
    }

    /**
     * @param amplitude [1.0f,1.5f]
     */
    public void setAmplitude(float amplitude) {
        this.amplitude = amplitude;
    }

    public float getAmplitude() {
        return amplitude;
    }

    /**
     * @param waveLength [1.0f,1.5f]
     */
    public void setWaveLength(float waveLength) {
        this.waveLength = waveLength;
    }

    public float getWaveLength() {
        return waveLength;
    }

    private void createShader() {
        float frequency = (float) (2.0f * Math.PI / DEFAULT_WAVE_LENGTH_RATIO / getWidth());
        float amplitude = getHeight() * DEFAULT_AMPLITUDE_RATIO;
        float waterLevel = getHeight() * DEFAULT_WATER_LEVEL_RATIO;
        float waveLength = getWidth();

        Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        Paint wavePaint = new Paint();
        wavePaint.setStrokeWidth(2);
        wavePaint.setAntiAlias(true);

        // Draw default waves into the bitmap
        // y=Asin(ωx+φ)+h
        final int endX = getWidth() + 1;
        final int endY = getHeight() + 1;

        float[] waveY = new float[endX];

        wavePaint.setColor(mBackColor);
        for (int beginX = 0; beginX < endX; beginX++) {
            double wx = beginX * frequency;
            float beginY = (float) (waterLevel + amplitude * Math.sin(wx));
            canvas.drawLine(beginX, beginY, beginX, endY, wavePaint);

            waveY[beginX] = beginY;
        }

        wavePaint.setColor(mFrontColor);
        final int wave2Shift = (int) (waveLength / 4);
        for (int beginX = 0; beginX < endX; beginX++) {
            canvas.drawLine(beginX, waveY[(beginX + wave2Shift) % endX], beginX, endY, wavePaint);
        }

        // use the bitamp to create the shader
        mWaveShader = new BitmapShader(bitmap, Shader.TileMode.REPEAT, Shader.TileMode.CLAMP);
        mPaint.setShader(mWaveShader);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mWaveShader != null) {
            if (mPaint.getShader() == null) {
                mPaint.setShader(mWaveShader);
            }

            waveShift = waveShift >= Float.MAX_VALUE ? 0.0f : waveShift;
            waveShift += 0.05f;
            mShaderMatrix.reset();
            mShaderMatrix.preScale(
                    waveLength,
                    amplitude,
                    0,
                    mHeight);
            mShaderMatrix.postTranslate(
                    waveShift * mWidth,
                    progress * mHeight);
            mWaveShader.setLocalMatrix(mShaderMatrix);

            switch (mShape) {
                case square:
                    //绘制背景
                    canvas.drawRect(0, 0, mWidth, mHeight, mBackgroundPaint);
                    //绘制波浪
                    canvas.drawRect(0, 0, mWidth, mHeight, mPaint);
                    break;
                case round:
                    //绘制背景
                    canvas.drawCircle(mWidth / 2f, mHeight / 2f, mRadius, mBackgroundPaint);
                    //绘制波浪
                    canvas.drawCircle(mWidth / 2f, mHeight / 2f, mRadius, mPaint);
                    break;
            }
        } else {
            mPaint.setShader(null);
        }
        postInvalidateDelayed(DEFAULT_PERIOD);
    }

    public enum Shape {
        square, round
    }
}
