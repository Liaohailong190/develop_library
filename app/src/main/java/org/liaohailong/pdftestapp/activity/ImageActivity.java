package org.liaohailong.pdftestapp.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import org.liaohailong.library.image.ImageLoader;
import org.liaohailong.library.image.ImageLoaderCallback;
import org.liaohailong.library.inject.BindView;
import org.liaohailong.library.util.ToastUtil;
import org.liaohailong.pdftestapp.BaseActivity;
import org.liaohailong.pdftestapp.R;

/**
 * Describe as : 图片加载界面
 * Created by LHL on 2018/2/23.
 */

public class ImageActivity extends BaseActivity {
    public static void show(Context context) {
        Intent intent = new Intent(context, ImageActivity.class);
        context.startActivity(intent);
    }

    @BindView(R.id.image_list)
    private RecyclerView mRecyclerView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(new CustomAdapter());
    }


    private class CustomAdapter extends RecyclerView.Adapter<CustomViewHolder> {

        @Override
        public CustomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            ImageView imageView = new ImageView(parent.getContext());
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 200);
            imageView.setLayoutParams(params);
            return new CustomViewHolder(imageView);
        }

        @Override
        public void onBindViewHolder(CustomViewHolder holder, int position) {
            holder.bindView();
        }

        @Override
        public int getItemCount() {
            return 10;
        }
    }


    private static class CustomViewHolder extends RecyclerView.ViewHolder {
        private static final String imageUrl01 = "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1505054331352&di=67367353f3ac52e7cdaca7221de9c39d&imgtype=0&src=http%3A%2F%2Fimg.mp.itc.cn%2Fupload%2F20161003%2F599d93c935d646b9a1b7e8adb049a8fa_th.jpg";

        private CustomViewHolder(View itemView) {
            super(itemView);
        }

        private void bindView() {
            if (itemView == null) {
                return;
            }
            if (itemView instanceof ImageView) {
                ImageView imageView = (ImageView) itemView;
                ImageLoader.instance.setImage(imageView, imageUrl01, 0, new ImageLoaderCallback() {
                    @Override
                    public void onImageLoadComplete(String url, Bitmap bitmap, ImageView imageView) {
                        ToastUtil.show("图片加载完毕 url = " + url);
                    }
                });
            }
        }
    }
}
