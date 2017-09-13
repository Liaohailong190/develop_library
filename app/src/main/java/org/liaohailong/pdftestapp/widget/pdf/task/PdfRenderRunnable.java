package org.liaohailong.pdftestapp.widget.pdf.task;

import android.graphics.Bitmap;

import com.shockwave.pdfium.PdfDocument;
import com.shockwave.pdfium.PdfiumCore;

import org.liaohailong.pdftestapp.widget.pdf.PdfView;
import org.liaohailong.pdftestapp.widget.photoview.PhotoView;

/**
 * pdf相关耗时操作
 * Created by LHL on 2017/9/13.
 */

public class PdfRenderRunnable implements Runnable {
    private PdfView.PdfResult pdfResult;
    private PdfiumCore pdfiumCore;
    private PdfDocument pdfDocument;
    private int pageIndex;
    private PdfView.PdfHandler handler;

    public PdfRenderRunnable(PdfView.PdfResult result, PdfiumCore pdfiumCore, PdfDocument pdfDocument, PdfView.PdfHandler handler) {
        this.pdfResult = result;
        this.pdfiumCore = pdfiumCore;
        this.pdfDocument = pdfDocument;
        this.pageIndex = result.getPageIndex();
        this.handler = handler;
    }

    @Override
    public void run() {
        pdfiumCore.openPage(pdfDocument, pageIndex);
        int pageWidth = pdfiumCore.getPageWidth(pdfDocument, pageIndex);
        int pageHeight = pdfiumCore.getPageHeight(pdfDocument, pageIndex);
        //异步渲染
        Bitmap bitmap = Bitmap.createBitmap(pageWidth, pageHeight, Bitmap.Config.ARGB_8888);
        pdfiumCore.renderPageBitmap(pdfDocument, bitmap, pageIndex, 0, 0, pageWidth, pageHeight);
        PhotoView view = pdfResult.getView();
        int width = (int) (view.getWidth() * 1.5f);
        int height = (int) (view.getHeight() * 1.5f);
        Bitmap result = Bitmap.createScaledBitmap(bitmap, width, height, true);
        pdfResult.setBitmap(result);
        pdfResult.setPageIndex(pageIndex);
        handler.sendRenderedMsg(pdfResult);
        if (bitmap != null) {
            bitmap.recycle();
        }
    }
}
