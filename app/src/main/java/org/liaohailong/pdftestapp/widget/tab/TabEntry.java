package org.liaohailong.pdftestapp.widget.tab;

import android.support.annotation.ColorInt;

/**
 * Describe as : 单选tab视图的 单挑tab数据
 * Created by LHL on 2018/6/5.
 */

public class TabEntry {
    private float textSize;//默认字体大小
    @ColorInt
    private int textColor;//默认文字颜色
    private boolean textBold;//字体是否粗体
    @ColorInt
    private int selectedTextColor;//选中的文字颜色
    @ColorInt
    private int selectedBackColor;//选中的背景颜色

    private String text;//展示内容

    public float getTextSize() {
        return textSize;
    }

    public void setTextSize(float textSize) {
        this.textSize = textSize;
    }

    public int getTextColor() {
        return textColor;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }

    public boolean isTextBold() {
        return textBold;
    }

    public void setTextBold(boolean textBold) {
        this.textBold = textBold;
    }

    public int getSelectedTextColor() {
        return selectedTextColor;
    }

    public void setSelectedTextColor(int selectedTextColor) {
        this.selectedTextColor = selectedTextColor;
    }

    public int getSelectedBackColor() {
        return selectedBackColor;
    }

    public void setSelectedBackColor(int selectedBackColor) {
        this.selectedBackColor = selectedBackColor;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
