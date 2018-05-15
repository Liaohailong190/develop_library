package org.liaohailong.pdftestapp.widget.wave;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.View;

import java.util.LinkedList;

/**
 * Describe as : 水波纹视图 (多值波浪饼图)
 * y=A*sin(ωx+φ)+k
 * A——振幅，当物体作轨迹符合正弦曲线的直线往复运动时，其值为行程的1/2。
 * (ωx+φ)——相位，反映变量y所处的状态。
 * φ——初相，x=0时的相位；反映在坐标系上则为图像的左右移动。
 * k——偏距，反映在坐标系上则为图像的上移或下移。
 * ω——角速度， 控制正弦周期(单位角度内震动的次数)。
 * Created by LHL on 2018/5/14.
 */

public class PercentWavePie extends View {
    // 默认属性值
    private static final int FPS_60 = 1000 / 60;//绘制间隔  1秒钟60次
    private static final String PERCENT = "%";//百分号

    //绘制基础类相关
    private Paint mBorderPaint;
    private TextPaint mValuePaint;
    private Shape mShape = Shape.SQUARE;
    private RectF mRectF = new RectF();
    private Path mClipPath = new Path();

    //绘制基础属性相关
    private int mWidth;//视图的宽度
    private int mHeight;//视图的高度
    private float mRadius;//圆形的半径
    private float mRoundLength;//圆角正方形的圆角度

    //右外部设置进来的配置属性
    private float mBorderWidth = 0;//边界环线厚度
    private float mSpace = 0;//边界环线与内部波浪绘制空间的间距
    @ColorInt
    private int mBorderColor = -1;
    //核心集合，绘制波浪
    private LinkedList<WaveModel> mWaveModelList = new LinkedList<>();
    //最大进度数
    private String mMaxValueStr;
    //缓存进度数
    private SparseArray<String> mValueCache = new SparseArray<>();
    //最大水位线的波浪模型
    private WaveModel mMaxWaveModel;
    @ColorInt
    private int mTextColorInWave = Color.WHITE;//波浪内颜色
    @ColorInt
    private int mTextColorOutWave = Color.WHITE;//波浪外颜色
    private float mTextPosition = 0.5f;//文本离底部的间距

    public PercentWavePie(Context context) {
        this(context, null);
    }

    public PercentWavePie(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PercentWavePie(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mBorderPaint = new Paint();
        mBorderPaint.setAntiAlias(true);
        mBorderPaint.setStyle(Paint.Style.STROKE);
        mBorderPaint.setStrokeWidth(mBorderWidth);

        mValuePaint = new TextPaint();
        mValuePaint.setAntiAlias(true);
    }

    /**
     * @param mShape 设置波纹图形状 {@link Shape}
     */
    public void setShape(Shape mShape) {
        this.mShape = mShape;
    }

    /**
     * @param color 设置边框颜色
     */
    public void setBorderColor(@ColorInt int color) {
        mBorderColor = color;
        mBorderPaint.setColor(color);
    }

    /**
     * @param borderWidth 设置边框厚度
     */
    public void setBorderWidth(float borderWidth) {
        mBorderWidth = borderWidth;
        mBorderPaint.setStrokeWidth(borderWidth);
    }

    /**
     * @param mSpace 设置边框与波纹之间的间隙 (大于0，小于半径mRadius)
     */
    public void setSpace(float mSpace) {
        this.mSpace = mSpace;
    }

    /**
     * @param textColorInWave 设置波纹与文字合体的颜色
     */
    public void setTextColorInWave(int textColorInWave) {
        this.mTextColorInWave = textColorInWave;
    }

    /**
     * @param textColorOutWave 设置未与波纹合体的字体颜色
     */
    public void setTextColorOutWave(int textColorOutWave) {
        this.mTextColorOutWave = textColorOutWave;
    }

    /**
     * @param textFontSize 值的字体大小。      px单位!px单位!px单位!
     */
    public void setTextFontSize(float textFontSize) {
        mValuePaint.setTextSize(textFontSize);
    }

    /**
     * @param isBold 值的字体是否要为粗体？
     */
    public void setTextType(boolean isBold) {
        mValuePaint.setTypeface(isBold ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
    }

    /**
     * @param textPosition 取值范围[0.0f,1.0f]
     */
    public void setTextPosition(float textPosition) {
        this.mTextPosition = textPosition;
    }

    /**
     * @param mWaveModelList 设置波浪模型
     */
    public void setWaveModelList(LinkedList<WaveModel> mWaveModelList) {
        this.mWaveModelList.clear();
        if (!mWaveModelList.isEmpty()) {
            this.mWaveModelList.addAll(mWaveModelList);
        }
        initWave();
    }

    /**
     * @return 获取波浪模式集合，推荐调用此方法，复用波浪模型！
     */
    public LinkedList<WaveModel> getWaveModelList() {
        return mWaveModelList;
    }

    private void initWave() {
        if (mWidth > 0 && mHeight > 0) {
            //初始化波浪数据
            for (int i = 0; i < mWaveModelList.size(); i++) {
                WaveModel waveModel = mWaveModelList.get(i);
                waveModel.setIndex(i + 1);
                int leftAndTop = (int) (mBorderWidth + mSpace);
                int rightAndBottom = (int) (mRadius * 2f - leftAndTop);
                waveModel.setRect(0, 0, rightAndBottom, rightAndBottom);
                waveModel.initConfig();
            }
        }
    }

    private void refreshWave() {
        if (mWidth > 0 && mHeight > 0) {
            //初始化水波数据，计算最高水位值
            int maxValue = 0;
            for (int i = 0; i < mWaveModelList.size(); i++) {
                WaveModel waveModel = mWaveModelList.get(i);
                int tempValue = Math.round(waveModel.getProgress() * 100);
                if (tempValue > maxValue) {
                    maxValue = tempValue;
                    mMaxWaveModel = waveModel;
                }
            }
            String valueStr = mValueCache.get(maxValue);
            if (TextUtils.isEmpty(valueStr)) {
                mMaxValueStr = String.valueOf(maxValue) + PERCENT;
                mValueCache.put(maxValue, mMaxValueStr);
            } else {
                mMaxValueStr = valueStr;
            }
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mWidth = w;
        mHeight = h;
        mRadius = mWidth > mHeight ? mHeight / 2f : mWidth / 2f;
        if (mWidth > 0 && mHeight > 0) {
            initDefaultConfig();
        }
    }

    private void initDefaultConfig() {
        if (mBorderWidth <= 0) {
            //默认十分之一的边线厚度
            setBorderWidth(mBorderWidth = mRadius * 0.1f);
        }
        if (mBorderColor <= 0) {
            //默认蓝
            setBorderColor(Color.parseColor("#294D99"));
        }
        if (mRoundLength <= 0) {
            //默认圆角
            mRoundLength = mRadius * 0.2f;
        }
        //默认间距与环线厚度一致
        if (mSpace <= 0) {
            mSpace = mBorderWidth;
        }
        //重置波纹绘制范围
        initWave();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //每次绘制首先刷新数据
        refreshWave();
        //绘制边界样式
        drawBorder(canvas);
        //绘制纹外文字
        drawOutWaveText(canvas);
        //绘制波浪
        drawWave(canvas);
        //绘制纹内文字
        drawInWaveText(canvas);
        //绘制帧数 60帧每秒
        postInvalidateDelayed(FPS_60);
    }

    private void drawBorder(Canvas canvas) {
        float length = mRadius - mBorderWidth / 2f;
        switch (mShape) {
            //绘制圆形边界
            case RING:
                canvas.drawCircle(mWidth / 2f, mHeight / 2f, length, mBorderPaint);
                break;
            //绘制正方形边界
            case SQUARE: {
                canvas.save();
                canvas.translate(mWidth / 2f, mHeight / 2f);
                canvas.drawRect(-length, -length, length, length, mBorderPaint);
                canvas.restore();
            }
            break;
            //绘制圆角正方形边界
            case ROUND_SQUARE: {
                canvas.save();
                canvas.translate(mWidth / 2f, mHeight / 2f);
                mRectF.set(-length, -length, length, length);
                canvas.drawRoundRect(mRectF, mRoundLength, mRoundLength, mBorderPaint);
                canvas.restore();
            }
            break;
        }
    }

    private void drawOutWaveText(Canvas canvas) {
        float x = mWidth / 2f;
        float y = (1.0f - mTextPosition) * mHeight;
        float textWidth = mValuePaint.measureText(mMaxValueStr);//文本宽度
        float textX = x - textWidth / 2f;
        float textY = y;
        mValuePaint.setColor(mTextColorOutWave);
        canvas.drawText(mMaxValueStr, textX, textY, mValuePaint);
    }

    private void drawInWaveText(Canvas canvas) {
        if (mMaxWaveModel == null) {
            return;
        }
        float x = mWidth / 2f;
        float y = (1.0f - mTextPosition) * mHeight;
        float textWidth = mValuePaint.measureText(mMaxValueStr);//文本宽度
        float textX = x - textWidth / 2f;
        float textY = y;
        mValuePaint.setColor(mTextColorInWave);
        canvas.save();
        Path path = mMaxWaveModel.getPath();
        canvas.clipPath(path);
        canvas.drawText(mMaxValueStr, textX, textY, mValuePaint);
        canvas.restore();
    }

    private void drawWave(Canvas canvas) {
        if (mWaveModelList == null || mWaveModelList.isEmpty()) {
            return;
        }
        //此处裁剪波浪可视化区域
        float length = getWaveRadius();
        mClipPath.reset();
        canvas.save();
        switch (mShape) {
            //裁剪圆形区域
            case RING:
                mClipPath.addCircle(mWidth / 2f, mHeight / 2f, length, Path.Direction.CW);
                canvas.clipPath(mClipPath);
                break;
            //裁剪正方形区域
            case SQUARE: {
                mClipPath.addRect(-length, -length, length, length, Path.Direction.CW);
                canvas.translate(mWidth / 2f, mHeight / 2f);
                canvas.clipPath(mClipPath);
                canvas.translate(-mWidth / 2f, -mHeight / 2f);
            }
            break;
            //裁剪圆角正方形区域
            case ROUND_SQUARE: {
                mRectF.set(-length, -length, length, length);
                mClipPath.addRoundRect(mRectF, mRoundLength, mRoundLength, Path.Direction.CW);
                canvas.translate(mWidth / 2f, mHeight / 2f);
                canvas.clipPath(mClipPath);
                canvas.translate(-mWidth / 2f, -mHeight / 2f);
            }
            break;
        }
        //按顺序排列绘制波纹
        for (WaveModel waveModel : mWaveModelList) {
            waveModel.draw(canvas);
        }
        canvas.restore();
    }

    /**
     * @return 获取波浪绘制区域的半径
     */
    private float getWaveRadius() {
        return mRadius - mBorderWidth - mSpace;
    }

    public enum Shape {
        RING, //圆形
        SQUARE, //正方形
        ROUND_SQUARE //圆角正方形
    }
}
