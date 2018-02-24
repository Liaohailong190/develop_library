package org.liaohailong.library.widget;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Describe as : 用于高性能展示的RecyclerView的适配器
 * Created by LHL on 2018/2/24.
 */

public abstract class PerformanceAdapter<VH extends VictorHolder> extends RecyclerView.Adapter<VH> {

    private WeakReference<RecyclerView> mRecyclerViewWeakReference;

    public PerformanceAdapter(RecyclerView recyclerView) {
        if (recyclerView == null) {
            return;
        }
        mRecyclerViewWeakReference = new WeakReference<>(recyclerView);
        RecyclerView.OnScrollListener mOnScrollListener = new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                switch (newState) {
                    case RecyclerView.SCROLL_STATE_DRAGGING://手拖拽
                    case RecyclerView.SCROLL_STATE_SETTLING://手滑翔
                        //do nothing...
                        break;
                    case RecyclerView.SCROLL_STATE_IDLE://滑动停止
                        RecyclerView mRecyclerView = mRecyclerViewWeakReference.get();
                        if (mRecyclerView != null) {
                            for (int i = 0; i < mRecyclerView.getChildCount(); i++) {
                                View view = mRecyclerView.getChildAt(i);
                                VH viewHolder = (VH) mRecyclerView.getChildViewHolder(view);
                                int position = mRecyclerView.getChildAdapterPosition(view);
                                onBindViewHolder(viewHolder, position, false, new ArrayList<>());
                            }
                        }
                        break;
                }
            }
        };
        recyclerView.addOnScrollListener(mOnScrollListener);
    }


    @Override
    public void onBindViewHolder(VH holder, int position) {
        onBindViewHolder(holder, position, isPerformance(holder), new ArrayList<>());
    }

    @Override
    public void onBindViewHolder(VH holder, int position, List<Object> payloads) {
        onBindViewHolder(holder, position, isPerformance(holder), payloads);
    }

    /**
     * 绑定视图，告知知否高效展示中
     * 视图正在滑动时为高效展示状态
     *
     * @param holder      条目视图携带者
     * @param position    条目位置
     * @param performance 是否高效
     * @param payloads    ？？？
     */
    public abstract void onBindViewHolder(VH holder, int position, boolean performance, List<Object> payloads);


    /**
     * 判断列表是否处于高效展示状态
     *
     * @param holder 条目视图携带者
     * @return true表示正在高效展示 false表示RecyclerView已经处于滑动停止状态
     */
    private boolean isPerformance(VH holder) {
        RecyclerView recyclerView = (RecyclerView) holder.itemView.getParent();
        if (recyclerView == null) {
            if (mRecyclerViewWeakReference == null || mRecyclerViewWeakReference.get() == null) {
                return false;
            }
            recyclerView = mRecyclerViewWeakReference.get();
        }
        int scrollState = recyclerView.getScrollState();
        return scrollState != RecyclerView.SCROLL_STATE_IDLE;
    }
}
