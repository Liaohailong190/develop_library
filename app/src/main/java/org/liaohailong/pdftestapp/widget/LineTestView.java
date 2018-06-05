package org.liaohailong.pdftestapp.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

/**
 * Describe as :
 * Created by LHL on 2018/6/5.
 */

public class LineTestView extends View {
    private Paint paint;
    private TextPaint textPaint;

    private Rect rect;

    public LineTestView(Context context) {
        this(context, null);
    }

    public LineTestView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LineTestView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setColor(Color.WHITE);

        textPaint = new TextPaint();
        textPaint.setAntiAlias(true);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(96);

        rect = new Rect();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        int measuredWidth = getMeasuredWidth();
        int measuredHeight = getMeasuredHeight();
        if (measuredWidth <= 0 || measuredHeight <= 0) {
            return;
        }
        canvas.drawLine(50, measuredHeight, 50, 0, paint);
        float span = measuredHeight / 10f;
        for (int i = 0; i < 10; i++) {
            float startY = span * i;
            canvas.drawLine(0, startY, measuredWidth, startY, paint);
            String text = String.valueOf(i);
            textPaint.getTextBounds(text, 0, text.length(), rect);
            float textY = startY + rect.height() / 2f;
            canvas.drawText(text, 0, textY, textPaint);
        }
    }
}
