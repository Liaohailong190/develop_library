package org.liaohailong.pdftestapp.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.util.AttributeSet;

import org.liaohailong.pdftestapp.R;

/**
 * Describe as: 头像样式的ImageView
 * Created by LHL on 2018/10/22.
 */
public class AvatarImageView extends android.support.v7.widget.AppCompatImageView {
    public static final int CIRCLE = 0;
    public static final int ROUND_RECT = 1;
    private float rxy;
    private RectF rectF;
    private RectF drawRectF;
    private Paint strokePaint;
    private Paint bitmapPaint;
    private Matrix imageMatrix;

    private float radiusPercent = 0.1f;
    private float borderWidth = 2f;//边界厚度
    private int borderColor = Color.TRANSPARENT;//边界颜色
    private int borderType = CIRCLE;//边界样式
    //同尺寸的Bitmap可以复用
    private Bitmap inBitmap;
    private Canvas inCanvas;
    private PorterDuffXfermode porterDuffXfermode = new PorterDuffXfermode(PorterDuff.Mode.SRC_IN);
    //规避硬件加速
    private Bitmap drawBitmap;
    private Canvas drawCanvas;

    public AvatarImageView(Context context) {
        this(context, null);
    }

    public AvatarImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AvatarImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.AvatarImageView);
        radiusPercent = typedArray.getFloat(R.styleable.AvatarImageView_radius_percent, 0.1f);
        borderWidth = typedArray.getDimension(R.styleable.AvatarImageView_border_width, 2f);
        borderColor = typedArray.getColor(R.styleable.AvatarImageView_border_color, Color.TRANSPARENT);
        borderType = typedArray.getInteger(R.styleable.AvatarImageView_border_type, CIRCLE);
        typedArray.recycle();
        init();
    }

    private void init() {
        rectF = new RectF();
        drawRectF = new RectF();

        strokePaint = new Paint();
        strokePaint.setAntiAlias(true);
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(borderWidth);
        strokePaint.setColor(borderColor);

        bitmapPaint = new Paint();
        bitmapPaint.setAntiAlias(true);
        bitmapPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        bitmapPaint.setColor(Color.WHITE);
        bitmapPaint.setStrokeWidth(0f);

        imageMatrix = new Matrix();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        int minLength = w > h ? h : w;
        rxy = (float) Math.floor(minLength * radiusPercent);
        rxy = Math.max(rxy, 1);
        rectF.left = 0;
        rectF.top = 0;
        rectF.right = w;
        rectF.bottom = h;
        //规避createBitmap失败
        w = w <= 0 ? 1 : w;
        h = h <= 0 ? 1 : h;
        if (drawBitmap != null) {
            drawBitmap.recycle();
        }
        drawBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        drawCanvas = new Canvas(drawBitmap);
    }

    public void setType(int type) {
        borderType = type;
        invalidate();
    }

    public void setBorderWidth(float borderWidth) {
        strokePaint.setStrokeWidth(borderWidth);
        invalidate();
    }

    public void setBorderColor(@ColorInt int borderColor) {
        strokePaint.setColor(borderColor);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (drawBitmap == null || drawBitmap.isRecycled()) {
            return;
        }
        drawBitmap.eraseColor(Color.TRANSPARENT);
        drawBitmap(drawCanvas);
        canvas.drawBitmap(drawBitmap, 0, 0, null);
    }

    private void drawBitmap(Canvas canvas) {
        if (getMeasuredWidth() <= 0 || getMeasuredHeight() <= 0) {
            return;
        }
        Drawable drawable = getDrawable();
        if (drawable == null) {
            return; // couldn't resolve the URI
        }

        if (drawable.getIntrinsicWidth() == 0 || drawable.getIntrinsicHeight() == 0) {
            return;     // nothing to draw (empty bounds)
        }

        final int saveCount = canvas.getSaveCount();

        if (getCropToPadding()) {
            final int scrollX = getScrollX();
            final int scrollY = getScrollY();
            canvas.clipRect(scrollX + getPaddingLeft(), scrollY + getPaddingTop(),
                    scrollX + getRight() - getLeft() - getPaddingRight(),
                    scrollY + getBottom() - getTop() - getPaddingBottom());
        }

        Bitmap drawBitmap;
        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            drawBitmap = bitmapDrawable.getBitmap();
        } else {
            if (inBitmap == null) {
                int intrinsicWidth = drawable.getIntrinsicWidth();
                int intrinsicHeight = drawable.getIntrinsicHeight();

                Rect bounds = drawable.getBounds();
                intrinsicWidth = intrinsicWidth <= 0 ? bounds.width() : intrinsicWidth;
                intrinsicHeight = intrinsicHeight <= 0 ? bounds.height() : intrinsicHeight;

                intrinsicWidth = intrinsicWidth <= 0 ? getMeasuredWidth() : intrinsicWidth;
                intrinsicHeight = intrinsicHeight <= 0 ? getMeasuredHeight() : intrinsicHeight;

                inBitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888);
                inCanvas = new Canvas(inBitmap);
            }
            inBitmap.eraseColor(Color.TRANSPARENT);
            drawable.draw(inCanvas);
            drawBitmap = inBitmap;
        }

        imageMatrix.reset();
        int measuredWidth = getMeasuredWidth();
        int measuredHeight = getMeasuredHeight();
        float bitmapWidth = drawBitmap.getWidth();
        float bitmapHeight = drawBitmap.getHeight();
        float scaleW = measuredWidth / bitmapWidth;
        float scaleH = measuredHeight / bitmapHeight;
        switch (getScaleType()) {
            case FIT_XY:
                imageMatrix.preScale(scaleW, scaleH);
                break;
            case CENTER_CROP:
                float scale = scaleW > scaleH ? scaleW : scaleH;
                float xOffset = ((bitmapWidth * scale) - measuredWidth) / 2f;
                float yOffset = ((bitmapHeight * scale) - measuredHeight) / 2f;
                imageMatrix.preScale(scale, scale);
                imageMatrix.postTranslate(-xOffset, -yOffset);
                break;
            default:
                imageMatrix.set(getImageMatrix());
                break;
        }
        imageMatrix.postTranslate(getPaddingLeft(), getPaddingTop());

        //绘制形状
        bitmapPaint.setXfermode(null);
        drawShape(canvas, bitmapPaint);
        //绘制内容
        bitmapPaint.setXfermode(porterDuffXfermode);
        canvas.drawBitmap(drawBitmap, imageMatrix, bitmapPaint);
        canvas.restoreToCount(saveCount);
        //绘制边界线
        drawShape(canvas, strokePaint);
    }

    private void drawShape(Canvas canvas, Paint paint) {
        float offset = paint.getStrokeWidth() / 2f;
        switch (borderType) {
            //圆形
            case CIRCLE:
                float centerX = rectF.centerX();
                float centerY = rectF.centerY();
                int radius = (int) ((int) (rectF.width() > rectF.height() ? rectF.height() / 2f : rectF.width() / 2f) - offset);
                canvas.drawCircle(centerX, centerY, radius, paint);
                break;
            //圆角矩形
            case ROUND_RECT:
                drawRectF.left = rectF.left + offset;
                drawRectF.top = rectF.top + offset;
                drawRectF.right = rectF.right - offset;
                drawRectF.bottom = rectF.bottom - offset;
                float r = rxy - offset * 2f;
                canvas.drawRoundRect(drawRectF, r, r, paint);
                break;
        }
    }
}
