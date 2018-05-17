package org.liaohailong.pdftestapp.widget.ratioimage;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import java.util.LinkedList;

/**
 * Describe as : 图片比例图
 * Created by LHL on 2018/5/17.
 */

public class RatioImagesView extends View {
    private static final int FPS_60 = 1000 / 60;//绘制间隔  1秒钟60次

    //绘制辅助类相关

    //绘制配置信息相关
    private int rowCnt = 5;//多少行
    private int colCnt = 10;//多少列
    private int totalCnt = 9;//总共多少个
    private RatioImageDirection ratioImageDirection = RatioImageDirection.left2Right;//布局方向
    private float paddingRow = 0.0f;//行间距
    private float paddingCol = 0.0f;//列间距
    @ColorInt
    private int cellBackColor = Color.YELLOW;//单元图片的背景颜色
    @ColorInt
    private int cellFrontColor = Color.RED;//单元图片的前景颜色

    //内部绘制信息相关

    //绘制模型
    private Bitmap bitmap;
    private LinkedList<RatioImageModel> models = new LinkedList<>();
    private float progress = 0.5f;

    public RatioImagesView(Context context) {
        this(context, null);
    }

    public RatioImagesView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RatioImagesView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    public RatioImagesView setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
        return this;
    }

    /**
     * @param rowCnt 多少行
     */
    public RatioImagesView setRowCnt(int rowCnt) {
        this.rowCnt = rowCnt;
        return this;
    }

    /**
     * @param colCnt 一行多少列
     */
    public RatioImagesView setColCnt(int colCnt) {
        this.colCnt = colCnt;
        return this;
    }

    public RatioImagesView setTotalCnt(int totalCnt) {
        this.totalCnt = totalCnt;
        return this;
    }

    public RatioImagesView setRatioImageDirection(RatioImageDirection ratioImageDirection) {
        this.ratioImageDirection = ratioImageDirection;
        return this;
    }

    /**
     * @param paddingRow 每一横排之间的间隙
     */
    public RatioImagesView setPaddingRow(float paddingRow) {
        this.paddingRow = paddingRow;
        return this;
    }

    /**
     * @param paddingCol 每个图片中间的间隙
     */
    public RatioImagesView setPaddingCol(float paddingCol) {
        this.paddingCol = paddingCol;
        return this;
    }

    public RatioImagesView setCellBackColor(int cellBackColor) {
        this.cellBackColor = cellBackColor;
        return this;
    }

    public RatioImagesView setCellFrontColor(int cellFrontColor) {
        this.cellFrontColor = cellFrontColor;
        return this;
    }

    private volatile int drawIndex;
    private volatile float lastProgress;
    private ValueAnimator valueAnimator;
    private volatile int currentIndex = -1;//当前动画进入哪一个了

    /**
     * @param progress 进度 取值范围[0.0f,1.0f];
     */
    public void setProgress(float progress) {
        progress = progress < 0 ? 0 : progress;
        progress = progress > 1.0f ? 1.0f : progress;
        //记录进度
        this.progress = progress;

        if (models.isEmpty()) {
            return;
        }
        //重置进度
        for (RatioImageModel model : models) {
            model.setProgress(0.0f);
        }
        //刷新单个比例图片的状态
        float count = progress * totalCnt;
        //记录需要绘制的个数---->进一
        drawIndex = (int) Math.ceil(count) - 1;
        //多少个需要全部满渲染---->去尾
        int lastFullIndex = (int) Math.floor(count);
        //最后一个比例图片的进度
        lastProgress = 1.0f - (count - drawIndex);

        //启动动画
        if (valueAnimator == null) {
            valueAnimator = ValueAnimator.ofInt(0, 1);
            valueAnimator.setDuration(1000);
            valueAnimator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    //重置渲染位置
                    resetCurrentIndex();
                    drawNext();
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    resetCurrentIndex();
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    resetCurrentIndex();
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                    drawNext();
                }
            });
        }
        valueAnimator.setRepeatCount(lastFullIndex);
        if (valueAnimator.isRunning()) {
            valueAnimator.end();
        }
        valueAnimator.start();
    }

    /**
     * 重置渲染位置
     */
    private void resetCurrentIndex() {
        //判断渲染方向
        currentIndex = -1;
    }

    /**
     * 绘制下一个
     */
    private void drawNext() {
        currentIndex++;
        //判断渲染方向
        switch (ratioImageDirection) {
            //正常布局
            case left2Right: {
                RatioImageModel ratioImageModel = models.get(currentIndex);
                ratioImageModel.anim2Progress(currentIndex < drawIndex ? 1.0f : lastProgress);
            }
            break;
            //阿拉伯布局
            case right2Left: {
                int lastIndex = models.size() - 1;
                int index = lastIndex - currentIndex;
                int lastDrawIndex = lastIndex - drawIndex;
                RatioImageModel ratioImageModel = models.get(index);
                ratioImageModel.anim2Progress(index > lastDrawIndex ? 1.0f : lastProgress);
            }
            break;
        }
    }

    public float getProgress() {
        return progress;
    }

    public void notifyDataSetChanged() {
        if (mWidth > 0 && mHeight > 0 && bitmap != null && !bitmap.isRecycled()) {
            //计算所有的列间距
            float totalPaddingCol = (colCnt - 1) * paddingCol;
            //计算所有行间距
            float totalPaddingRow = (rowCnt - 1) * paddingRow;
            //计算可使用的宽度
            float valuableWidth = mWidth - totalPaddingCol;
            valuableWidth = valuableWidth < 0 ? 0 : valuableWidth;
            //计算可使用的高度
            float valuableHeight = mHeight - totalPaddingRow;
            valuableHeight = valuableHeight < 0 ? 0 : valuableHeight;
            //计算单个图片可用的宽度
            float singleWidth = valuableWidth / colCnt;
            //计算单个图片可使用的高度
            float singleHeight = valuableHeight / rowCnt;
            //裁剪图片
            Bitmap cropBitmap = zoomBitmap(bitmap, singleWidth, singleHeight);
            //创建比例图片模型
            models.clear();
            //生成双面shader
            Bitmap bitmap = Bitmap.createBitmap(cropBitmap);
            Paint paint = new Paint();
            paint.setFilterBitmap(true);
            paint.setAntiAlias(true);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

            Canvas canvas = new Canvas(bitmap);
            paint.setColor(cellBackColor);
            canvas.drawRect(0, 0, bitmap.getWidth(), bitmap.getHeight(), paint);
            BitmapShader backBitmapShader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);

            bitmap = Bitmap.createBitmap(cropBitmap);
            canvas = new Canvas(bitmap);
            paint.setColor(cellFrontColor);
            canvas.drawRect(0, 0, bitmap.getWidth(), bitmap.getHeight(), paint);
            BitmapShader frontBitmapShader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);

            for (int i = 0; i < totalCnt; i++) {
                int colIndex = i % colCnt;//行中第几列（个） 值的范围[1,colCnt)
                int rowIndex = i / colCnt;//位于第几行（排） 值的范围[1,rowCnt)
                float x = (singleWidth + paddingCol) * colIndex;
                float y = (singleHeight + paddingRow) * rowIndex;
                RatioImageModel ratioImageModel = new RatioImageModel()
                        .setAnim(false)
                        .setEmpty(true)
                        .setProgress(0.0f)
                        .setX(x)
                        .setY(y)
                        .setWidth(singleWidth)
                        .setHeight(singleHeight)
                        .setIndex(i)
                        .setRatioImageDirection(ratioImageDirection)
                        .setBackShader(backBitmapShader)
                        .setFrontShader(frontBitmapShader);
                models.add(ratioImageModel);
            }
        }
    }

    private int mWidth;
    private int mHeight;

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mWidth = w;
        mHeight = h;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        for (RatioImageModel model : models) {
            model.draw(canvas);
        }
        //绘制帧数 60帧每秒
        postInvalidateDelayed(FPS_60);
    }

    public enum RatioImageDirection {
        left2Right,
        right2Left
    }

    /**
     * 缩放图片
     *
     * @param bm        原图
     * @param newWidth  理想宽度
     * @param newHeight 理想高度
     * @return 理想大小的图 Bitmap
     */
    public Bitmap zoomBitmap(Bitmap bm, float newWidth, float newHeight) {
        // 获得图片的宽高
        int width = bm.getWidth();
        int height = bm.getHeight();
        // 计算缩放比例
        float scaleWidth = newWidth / width;
        float scaleHeight = newHeight / height;
        // 取得想要缩放的matrix参数
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        // 得到新的图片
        return Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true);
    }
}
