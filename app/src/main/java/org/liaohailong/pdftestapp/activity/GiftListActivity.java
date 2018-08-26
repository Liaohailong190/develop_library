package org.liaohailong.pdftestapp.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.liaohailong.pdftestapp.BaseActivity;
import org.liaohailong.pdftestapp.R;
import org.liaohailong.pdftestapp.widget.GiftList;

import java.util.LinkedList;

/**
 * Describe as: 礼物列表展示界面
 * Created by LHL on 2018/8/26.
 */
public class GiftListActivity extends BaseActivity implements View.OnClickListener {
    public static void show(Context context) {
        Intent intent = new Intent(context, GiftListActivity.class);
        context.startActivity(intent);
    }

    private Adapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gift_list);
        GiftList giftList = findViewById(R.id.gift_list);

        findViewById(R.id.add_btn).setOnClickListener(this);
        findViewById(R.id.remove_btn).setOnClickListener(this);
        adapter = new Adapter(giftList);
        giftList.setAdapter(adapter);
    }

    private int count = 0;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.add_btn:
                adapter.addTitle("第" + count + "号测试数据");
                count++;
                break;
            case R.id.remove_btn:
                adapter.removeTitle();
                break;
        }
    }


    private final class Adapter extends RecyclerView.Adapter<ViewHolder> {
        private final GiftList giftList;
        private LinkedList<String> titles = new LinkedList<>();

        private Adapter(GiftList giftList) {
            this.giftList = giftList;
        }

        private void addTitle(final String title) {
            if (titles.size() >= 4) {
                removeTitle();
                giftList.post(new Runnable() {
                    @Override
                    public void run() {
                        addFirst(title);
                    }
                });
            } else {
                addFirst(title);
            }
        }

        private void addFirst(String title) {
            titles.addFirst(title);
            notifyItemInserted(0);
        }

        private void removeTitle() {
            int lastIndex = titles.size() - 1;
            String last = titles.pollLast();
            if (last == null) {
                return;
            }
            notifyItemRemoved(lastIndex);
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_gift_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            String title = titles.get(position);
            TextView nameTxt = holder.itemView.findViewById(R.id.name_txt);
            nameTxt.setText(title);
        }

        @Override
        public int getItemCount() {
            return titles.size();
        }
    }

    private final class ViewHolder extends RecyclerView.ViewHolder {

        private ViewHolder(View itemView) {
            super(itemView);
        }
    }
}
