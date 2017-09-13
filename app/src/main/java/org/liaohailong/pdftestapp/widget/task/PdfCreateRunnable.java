package org.liaohailong.pdftestapp.widget.task;

import android.os.Handler;
import android.os.Message;

import com.shockwave.pdfium.PdfDocument;
import com.shockwave.pdfium.PdfiumCore;

import org.liaohailong.pdftestapp.VictorApplication;
import org.liaohailong.pdftestapp.widget.PdfView;
import org.liaohailong.pdftestapp.widget.source.DocumentSource;

import java.io.IOException;

/**
 * Pdf创建任务
 * Created by LHL on 2017/9/13.
 */

public class PdfCreateRunnable implements Runnable {
    private DocumentSource source;
    private PdfiumCore pdfiumCore;
    private String password;
    private PdfView.PdfHandler handler;

    public PdfCreateRunnable(DocumentSource source, PdfiumCore pdfiumCore, String password, PdfView.PdfHandler handler) {
        this.source = source;
        this.pdfiumCore = pdfiumCore;
        this.password = password;
        this.handler = handler;
    }

    @Override
    public void run() {
        PdfDocument pdfDocument = null;
        try {
            pdfDocument = source.createDocument(VictorApplication.getInstance(), pdfiumCore, password);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (pdfDocument != null) {
            handler.sendCreateMsg(pdfDocument);
        }
        source = null;
        pdfiumCore = null;
        password = null;
        handler = null;
    }
}
