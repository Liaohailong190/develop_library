package org.liaohailong.pdftestapp.widget.ratioimage;

import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Shader;
import android.view.animation.AccelerateDecelerateInterpolator;

/**
 * Describe as : 单个比例图片
 * Created by LHL on 2018/5/17.
 */

class RatioImageModel {

    private final Paint paint;

    private int index = 0;
    private float width;//宽度
    private float height;//高度
    private float x;//x坐标（左上角）
    private float y;//y坐标（左上角）

    private RatioImagesView.RatioImageDirection ratioImageDirection = RatioImagesView.RatioImageDirection.left2Right;//绘制方向
    private boolean empty = false;//是否为空，表示是否需要图片覆盖渲染
    private boolean anim = false;//开启动画
    private float progress = 0.0f;//比例图片绘制进度
    private Shader backShader;//背面渲染器
    private Shader frontShader;//正面渲染器

    //动画相关
    private ValueAnimator valueAnimator;

    RatioImageModel() {
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setColor(Color.RED);
    }


    RatioImageModel setRatioImageDirection(RatioImagesView.RatioImageDirection ratioImageDirection) {
        this.ratioImageDirection = ratioImageDirection;
        return this;
    }

    RatioImageModel setIndex(int index) {
        this.index = index;
        return this;
    }

    RatioImageModel setWidth(float width) {
        this.width = width;
        return this;
    }

    RatioImageModel setHeight(float height) {
        this.height = height;
        return this;
    }

    RatioImageModel setX(float x) {
        this.x = x;
        return this;
    }

    RatioImageModel setY(float y) {
        this.y = y;
        return this;
    }

    RatioImageModel setEmpty(boolean empty) {
        this.empty = empty;
        return this;
    }

    RatioImageModel setAnim(boolean anim) {
        this.anim = anim;
        return this;
    }

    RatioImageModel setProgress(float progress) {
        this.progress = progress;
        return this;
    }

    RatioImageModel setBackShader(Shader backShader) {
        this.backShader = backShader;
        return this;
    }

    RatioImageModel setFrontShader(Shader frontShader) {
        this.frontShader = frontShader;
        return this;
    }

    void anim2Progress(float progress) {
        if (valueAnimator == null) {
            valueAnimator = ValueAnimator.ofFloat(0.0f, 1.0f);
            valueAnimator.setDuration(1000);
            valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    RatioImageModel.this.progress = (float) animation.getAnimatedValue();
                }
            });
        }
        valueAnimator.setFloatValues(0.0f, progress);
        if (valueAnimator.isRunning()) {
            valueAnimator.end();
        }
        valueAnimator.start();
    }

    void draw(Canvas canvas) {
        //绘制背景图片
        canvas.save();
        canvas.translate(x, y);
        //将图片混成背景色
        paint.setShader(backShader);
        canvas.drawRect(0, 0, width, height, paint);

        //将图片混成命中色
        paint.setShader(frontShader);
        float progressWidth = progress * width;
        switch (ratioImageDirection) {
            case left2Right://正常绘制方向
                canvas.drawRect(0, 0, progressWidth, height, paint);
                break;
            case right2Left://阿拉伯式绘制方向
                progressWidth = (1.0f - progress) * width;
                canvas.drawRect(progressWidth, 0, width, height, paint);
                break;
        }
        canvas.restore();
    }
}
