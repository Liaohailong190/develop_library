package org.liaohailong.pdftestapp.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

/**
 * Describe as: 礼物展示列表
 * Created by LHL on 2018/8/26.
 */
public class GiftList extends RecyclerView {
    public GiftList(Context context) {
        this(context, null);
    }

    public GiftList(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GiftList(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        linearLayoutManager.setOrientation(VERTICAL);
        linearLayoutManager.setReverseLayout(true);
        setLayoutManager(linearLayoutManager);
    }

}
