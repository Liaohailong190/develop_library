package org.liaohailong.pdftestapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;


public class MainActivity extends AppCompatActivity implements OnPageChangeListener, View.OnClickListener {
    private PDFView pdfView;

    private int currentPage = 1;
    private int totalCount = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        View upBtn = findViewById(R.id.up_btn);
        View downBtn = findViewById(R.id.down_btn);
        upBtn.setOnClickListener(this);
        downBtn.setOnClickListener(this);
        pdfView = (PDFView) findViewById(R.id.pdf_view);
        pdfView.fromAsset("sample02.pdf")
                .swipeHorizontal(false)
                .defaultPage(1)
                .enableDoubletap(true)
                .enableSwipe(true)
                .onPageChange(this)
                .load();
    }


    @Override
    public void onPageChanged(int page, int pageCount) {
        currentPage = page;
        totalCount = pageCount;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_UP:
                turnPrePage();
                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                turnNextPage();
                break;
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.up_btn://向上
                turnPrePage();
                break;
            case R.id.down_btn://向下
                turnNextPage();
                break;
        }
    }

    private void turnNextPage() {
        int upPage;
        upPage = ++currentPage;
        if (upPage > totalCount) {
            upPage = totalCount;
        }
        pdfView.jumpTo(upPage);
    }

    private void turnPrePage() {
        int upPage;
        upPage = --currentPage;
        if (upPage < 1) {
            upPage = 1;
        }
        pdfView.jumpTo(upPage);
    }
}
