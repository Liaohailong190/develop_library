package org.liaohailong.pdftestapp.widget;

import android.content.Context;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.MaskFilter;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * 带有边界阴影效果的View
 * Created by LHL on 2017/12/3.
 */

public class MaskView extends View {
    private Paint mPaint;

    private int mWidth;
    private int mHeight;

    private int mMaskRadius = 100;

    private MaskFilter mOuterMaskFilter;
    private MaskFilter mSolidMaskFilter;


    public MaskView(Context context) {
        this(context, null);
    }

    public MaskView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MaskView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mOuterMaskFilter = new BlurMaskFilter(mMaskRadius, BlurMaskFilter.Blur.OUTER);
        mSolidMaskFilter = new BlurMaskFilter(mMaskRadius, BlurMaskFilter.Blur.SOLID);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mWidth = w;
        mHeight = h;
        setLayerType(LAYER_TYPE_SOFTWARE, mPaint);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        mPaint.setColor(Color.BLUE);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setMaskFilter(mOuterMaskFilter);
        canvas.drawRect(mMaskRadius, mMaskRadius, mWidth - mMaskRadius, mHeight - mMaskRadius, mPaint);

        mPaint.setColor(Color.RED);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setMaskFilter(mSolidMaskFilter);
        float radius = mWidth > mHeight ? mHeight / 6f : mWidth / 6f;
        canvas.drawCircle(mWidth / 2f, mHeight / 2f, radius, mPaint);
    }
}
