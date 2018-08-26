package org.liaohailong.pdftestapp.widget.gift;

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
        //设置布局样式
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        linearLayoutManager.setOrientation(VERTICAL);
        linearLayoutManager.setReverseLayout(true);
        setLayoutManager(linearLayoutManager);
        //设置Item动画
        TransItemAnimator transItemAnimator = new TransItemAnimator();
        setItemAnimator(transItemAnimator);
        transItemAnimator.isRunning(new ItemAnimator.ItemAnimatorFinishedListener() {
            @Override
            public void onAnimationsFinished() {
                //do something...
            }
        });
    }
}
