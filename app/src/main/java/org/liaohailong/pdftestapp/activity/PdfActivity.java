package org.liaohailong.pdftestapp.activity;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import org.liaohailong.library.inject.BindContentView;
import org.liaohailong.library.inject.BindView;
import org.liaohailong.pdftestapp.BaseActivity;
import org.liaohailong.pdftestapp.R;
import org.liaohailong.pdftestapp.widget.PdfView;

/**
 * 展示PDF的界面
 * Created by LHL on 2017/9/13.
 */
@BindContentView(R.layout.activity_pdf)
public class PdfActivity extends BaseActivity {
    public static void show(Context context) {
        Intent intent = new Intent(context, PdfActivity.class);
        context.startActivity(intent);
    }

    @BindView(R.id.pdf_view)
    private PdfView pdfView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        pdfView.fromAsset("sample.pdf")
                .setOrientation(PdfView.VERTICAL)
                .setDefaultPage(0)
                .load();


    }
}
