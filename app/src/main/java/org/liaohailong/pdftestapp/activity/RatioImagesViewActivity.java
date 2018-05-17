package org.liaohailong.pdftestapp.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import org.liaohailong.library.inject.OnClick;
import org.liaohailong.pdftestapp.BaseActivity;
import org.liaohailong.pdftestapp.R;
import org.liaohailong.pdftestapp.widget.ratioimage.RatioImagesView;

/**
 * Describe as : 图片比例图
 * Created by LHL on 2018/5/17.
 */

public class RatioImagesViewActivity extends BaseActivity {

    private RatioImagesView ratioImagesView;

    private RatioImagesView.RatioImageDirection mRatioImageDirection = RatioImagesView.RatioImageDirection.left2Right;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ratio_images);
        ratioImagesView = findViewById(R.id.ratio_images_view);
        ratioImagesView.post(new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ratio_image);
                ratioImagesView.setBitmap(bitmap)//渲染图片
                        .setRowCnt(2)//设置行数
                        .setColCnt(10)//设置列数（每行多少个）
                        .setTotalCnt(20)//设置总数
                        .setRatioImageDirection(mRatioImageDirection)//设置布局方向
                        .setPaddingCol(0f)//设置列间距 （每个图片中间的间隙）
                        .setPaddingRow(10f)//设置行间距 （每个横排之间的间隙）
                        .setCellBackColor(Color.parseColor("#18A4F6"))//默认蓝
                        .setCellFrontColor(Color.parseColor("#EE434B"))//默认红
                        .notifyDataSetChanged();//使配置生效
            }
        });
    }

    @OnClick({R.id.start_left_2_right_btn, R.id.start_right_2_left_btn})
    public void start(View view) {
        switch (view.getId()) {
            case R.id.start_left_2_right_btn://从左至右开始动画
                mRatioImageDirection = RatioImagesView.RatioImageDirection.left2Right;
                break;
            case R.id.start_right_2_left_btn://从右至左开始动画
                mRatioImageDirection = RatioImagesView.RatioImageDirection.right2Left;
                break;
        }
        ratioImagesView.setRatioImageDirection(mRatioImageDirection).notifyDataSetChanged();
        ratioImagesView.setProgress(0.625f);
    }
}
