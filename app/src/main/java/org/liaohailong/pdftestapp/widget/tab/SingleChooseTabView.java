package org.liaohailong.pdftestapp.widget.tab;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.support.annotation.ColorInt;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.liaohailong.pdftestapp.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Describe as : 单选Tab图
 * Created by LHL on 2018/6/5.
 */

public class SingleChooseTabView extends LinearLayout implements View.OnClickListener {

    private Path borderPath = new Path();
    private RectF rectF = new RectF();
    private Paint borderPaint = new Paint();
    private ArrayList<TabEntry> tabEntries = new ArrayList<>();

    //默认配置样式
    @ColorInt
    private int backColor;//背景颜色
    private int rawCount;//坑位数量
    private float radius = 4;//圆角半径默认4px
    private int tabDirection = LinearLayout.HORIZONTAL;//布局方向

    private boolean showBorder;//是否展示边界，默认隐藏
    private float borderWidth;//边界线的厚度
    @ColorInt
    private int borderColor = Color.TRANSPARENT;//边界颜色

    private boolean isAuto;//是否轮播 默认启用
    private long autoDuration = 30 * 1000;//轮播间隔时间，默认30s


    //内容运行时参数
    private int selectIndex = -1;//选中的条目下标

    public SingleChooseTabView(Context context) {
        this(context, null);
    }

    public SingleChooseTabView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SingleChooseTabView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public SingleChooseTabView setBackColor(int backColor) {
        this.backColor = backColor;
        return this;
    }

    public SingleChooseTabView setRawCount(int rawCount) {
        this.rawCount = rawCount;
        return this;
    }

    public SingleChooseTabView setRadius(float radius) {
        this.radius = radius;
        return this;
    }

    public SingleChooseTabView setTabDirection(int tabDirection) {
        this.tabDirection = tabDirection;
        return this;
    }

    public SingleChooseTabView setShowBorder(boolean showBorder) {
        this.showBorder = showBorder;
        return this;
    }

    public SingleChooseTabView setBorderWidth(float borderWidth) {
        this.borderWidth = borderWidth;
        return this;
    }

    public SingleChooseTabView setBorderColor(int borderColor) {
        this.borderColor = borderColor;
        return this;
    }

    public SingleChooseTabView setAuto(boolean auto) {
        isAuto = auto;
        return this;
    }

    public SingleChooseTabView setAutoDuration(long autoDuration) {
        this.autoDuration = autoDuration;
        return this;
    }

    public SingleChooseTabView setData(List<TabEntry> data) {
        tabEntries.clear();
        tabEntries.addAll(data);
        return this;
    }

    public ArrayList<TabEntry> getData() {
        return tabEntries;
    }

    public boolean isAuto() {
        return isAuto;
    }

    public void notifyDataSetChanged() {
        //设置布局摆放
        setOrientation(tabDirection);
        //设置背景颜色
        setBackgroundColor(backColor);
        //保护最小数量
        rawCount = rawCount < 1 ? 1 : rawCount;
        //实例化Tab View(满载)
        int planCount = rawCount;//配置坑数
        int dataCount = tabEntries.size();//实际萝卜数量
        boolean lessData = dataCount <= planCount;//萝卜数据少于坑数
        int newCount = lessData ? dataCount : planCount;//如果萝卜数据量少于行数的情况下，就按萝卜数据来
        resizeChildrenCount(this, newCount, R.layout.layout_simple_text);//重置容器子视图个数（重置坑位数量）
        //初始化视图与数据的绑定
        for (int i = 0; i < newCount; i++) {
            View view = getChildAt(i);
            TabEntry data = tabEntries.get(i);
            view.setTag(data);
        }
        //刷新tab数据
        removeCallbacks(autoRunnable);
        post(autoRunnable);
    }

    private Runnable autoRunnable = new Runnable() {
        @Override
        public void run() {
            selectIndex++;
            int lastIndex = tabEntries.size() - 1;
            selectIndex = selectIndex > lastIndex ? 0 : selectIndex;
            //刷新tab数据
            refreshTab();
            //轮询启动下一个任务
            if (isAuto()) {
                postDelayed(autoRunnable, autoDuration);
            }
        }
    };

    /**
     * 刷新tab item
     */
    private void refreshTab() {
        int childCount = getChildCount();
        int dataCount = tabEntries.size();
        int lastViewIndex = childCount - 1;
        int lastDataIndex = dataCount - 1;
        int indexOffset = 0;
        //是否轮播到"翻页位置",当数据数量大于设置展示列数时生效
        boolean carousel = dataCount > childCount;//是否具有轮播条件
        if (carousel) {
            View firstVisibleView = getChildAt(0);//第一个能看见的数据
            View lastVisibleView = getChildAt(lastViewIndex);//最后一个能看见的数据
            TabEntry firstVisibleTabEntry = (TabEntry) firstVisibleView.getTag();
            TabEntry lastVisibleTabEntry = (TabEntry) lastVisibleView.getTag();
            int firstIndex = tabEntries.indexOf(firstVisibleTabEntry);
            int lastIndex = tabEntries.indexOf(lastVisibleTabEntry);
            //轮播条目超过倒数第一个时
            if (selectIndex > lastIndex - 1) {
                indexOffset = selectIndex - lastViewIndex + 1;
            }
            //轮播条目前置第一个时
            else if (selectIndex < firstIndex + 1) {
                indexOffset = selectIndex - 1;
            }
            //偏移位保持不变
            else {
                indexOffset = firstIndex;
            }
            //防止偏移过程中下标越界
            int maxOffset = lastDataIndex - lastViewIndex;
            int minOffset = 0;
            indexOffset = indexOffset > maxOffset ? maxOffset : indexOffset;
            indexOffset = indexOffset < minOffset ? minOffset : indexOffset;
        }
        //绘制数据内容
        int j = 0;//子视图的下标位置
        for (int i = indexOffset; i < indexOffset + childCount; i++) {
            TextView view = (TextView) getChildAt(j);
            TabEntry tabEntry = getData().get(i);
            //将数据绑定到视图上
            bindView(view, tabEntry);
            j++;
        }
    }

    private void bindView(TextView textView, TabEntry tabEntry) {
        float textSize = tabEntry.getTextSize();//默认字体大小
        boolean textBold = tabEntry.isTextBold();//字体是否加粗
        int textColor = tabEntry.getTextColor();//默认字体颜色

        int selectedTextColor = tabEntry.getSelectedTextColor();//选中时的字体颜色
        int selectedBackColor = tabEntry.getSelectedBackColor();//选中时的字体背景

        //潜配置 与Web端一致
        int defaultPadding = (int) Math.ceil(textSize);//字体大小进一法
        textView.setPadding(defaultPadding, 0, defaultPadding, 0);//默认文本左右间隙
        textView.setMaxLines(2);//最多两行
        textView.setEllipsize(TextUtils.TruncateAt.END);//末端展示...
        textView.setGravity(Gravity.CENTER);//文字居中展示
        //设置通用配置
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        textView.setTypeface(textBold ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
        textView.setText(tabEntry.getText());

        //设置宽高尺寸
        int measuredWidth = getMeasuredWidth();
        int measuredHeight = getMeasuredHeight();
        ViewGroup.LayoutParams layoutParams = textView.getLayoutParams();
        int orientation = getOrientation();
        switch (orientation) {
            case LinearLayout.HORIZONTAL:
                layoutParams.width = measuredWidth / rawCount;
                layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
                break;
            case LinearLayout.VERTICAL:
                layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
                layoutParams.height = measuredHeight / rawCount;
                break;
        }
        textView.setLayoutParams(layoutParams);

        //是否需要高亮
        int targetIndex = getData().indexOf(tabEntry);
        //非单个坑
        if (getChildCount() > 1) {
            //命中高亮
            if (targetIndex == selectIndex) {
                textView.setTextColor(selectedTextColor);
                textView.setBackgroundColor(selectedBackColor);
            }
            //非高亮
            else {
                textView.setTextColor(textColor);
                textView.setBackgroundColor(Color.TRANSPARENT);
            }
        }
        //单个坑的时候，不用考虑，都是高亮展示
        else {
            textView.setTextColor(selectedTextColor);
            textView.setBackgroundColor(selectedBackColor);
        }

        //点击事件把控
        textView.setOnClickListener(this);
        textView.setTag(tabEntry);
    }

    @Override
    public void onClick(View v) {
        //如果坑位少于两个就不点击无效了
        if (rawCount < 2 || tabEntries.size() < 2) {
            return;
        }
        Object tabEntry = v.getTag();
        if (tabEntry instanceof TabEntry) {
            //确认点击条目下标
            selectIndex = tabEntries.indexOf(tabEntry);
            //刷新tab数据
            refreshTab();
            //继续开始轮播
            removeCallbacks(autoRunnable);
            if (isAuto()) {
                postDelayed(autoRunnable, autoDuration);
            }
        }
    }

    public void resizeChildrenCount(ViewGroup layout, int newCount, @LayoutRes int childRes) {
        int oldCount = layout.getChildCount();
        if (newCount < oldCount) {
            layout.removeViews(newCount, oldCount - newCount);
        } else if (newCount > oldCount) {
            for (int i = oldCount; i < newCount; i++) {
                LayoutInflater.from(layout.getContext()).inflate(childRes, layout);
            }
        }
    }

    @Override
    public void draw(Canvas canvas) {
        int measuredWidth = getWidth();
        int measuredHeight = getHeight();
        if (measuredWidth <= 0 || measuredHeight <= 0) {
            return;
        }
        //裁剪圆角区域
        borderPath.reset();
        rectF.setEmpty();
        rectF.set(0, 0, measuredWidth, measuredHeight);
        borderPath.addRoundRect(rectF, radius, radius, Path.Direction.CW);
        canvas.clipPath(borderPath);
        super.draw(canvas);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        int measuredWidth = getWidth();
        int measuredHeight = getHeight();
        if (measuredWidth <= 0 || measuredHeight <= 0) {
            return;
        }
        //绘制边界
        if (showBorder && borderWidth > 0.000000001) {
            borderPaint.setStyle(Paint.Style.STROKE);
            borderPaint.setAntiAlias(true);
            borderPaint.setColor(borderColor);
            borderPaint.setStrokeWidth(borderWidth);
            canvas.drawPath(borderPath, borderPaint);
        }
    }
}
