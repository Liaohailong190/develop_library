package org.liaohailong.pdftestapp.widget.task;

import android.graphics.Bitmap;

import com.shockwave.pdfium.PdfDocument;
import com.shockwave.pdfium.PdfiumCore;

import org.liaohailong.pdftestapp.widget.PdfView;

/**
 * pdf相关耗时操作
 * Created by LHL on 2017/9/13.
 */

public class PdfRenderRunnable implements Runnable {
    private PdfiumCore pdfiumCore;
    private PdfDocument pdfDocument;
    private int defaultPage;
    private PdfView.PdfHandler handler;

    public PdfRenderRunnable(PdfiumCore pdfiumCore, PdfDocument pdfDocument, int defaultPage, PdfView.PdfHandler handler) {
        this.pdfiumCore = pdfiumCore;
        this.pdfDocument = pdfDocument;
        this.defaultPage = defaultPage;
        this.handler = handler;
    }

    @Override
    public void run() {
        pdfiumCore.openPage(pdfDocument, defaultPage);
        int pageWidth = pdfiumCore.getPageWidth(pdfDocument, defaultPage);
        int pageHeight = pdfiumCore.getPageHeight(pdfDocument, defaultPage);
        //异步渲染
        Bitmap bitmap = Bitmap.createBitmap(pageWidth, pageHeight, Bitmap.Config.ARGB_8888);
        pdfiumCore.renderPageBitmap(pdfDocument, bitmap, defaultPage, 0, 0, pageWidth, pageHeight);
        handler.sendRenderedMsg(bitmap);
    }
}
