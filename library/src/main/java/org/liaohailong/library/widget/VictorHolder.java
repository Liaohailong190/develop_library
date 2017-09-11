package org.liaohailong.library.widget;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import org.liaohailong.library.inject.Victor;

/**
 * 简化{@link android.support.v7.widget.RecyclerView.ViewHolder}中的findViewById
 * Created by LHl on 2017/9/11.
 */

public class VictorHolder extends RecyclerView.ViewHolder {

    public VictorHolder(View itemView) {
        super(itemView);
        Victor.inject(this);
    }
}
