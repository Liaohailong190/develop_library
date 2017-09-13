package org.liaohailong.pdftestapp.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.shockwave.pdfium.PdfDocument;
import com.shockwave.pdfium.PdfiumCore;

import org.liaohailong.pdftestapp.widget.source.AssetSource;
import org.liaohailong.pdftestapp.widget.source.ByteArraySource;
import org.liaohailong.pdftestapp.widget.source.DocumentSource;
import org.liaohailong.pdftestapp.widget.source.FileSource;
import org.liaohailong.pdftestapp.widget.source.InputStreamSource;
import org.liaohailong.pdftestapp.widget.source.UriSource;
import org.liaohailong.pdftestapp.widget.task.PdfCreateRunnable;
import org.liaohailong.pdftestapp.widget.task.PdfRenderRunnable;

import java.io.File;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * PDF展示视图
 * Created by LHL on 2017/9/13.
 */

public class PdfView extends RelativeLayout {
    public static final int HORIZONTAL = 0;
    public static final int VERTICAL = 1;
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    private Context context;
    private PdfiumCore pdfiumCore;
    private PdfDocument pdfDocument;
    private Canvas canvas;
    private Paint paint;
    private PdfHandler handler;

    //配置属性相关
    private int defaultPage = 0;
    private int orientation = VERTICAL;
    private int pageCount = 0;

    public PdfView(Context context) {
        this(context, null);
    }

    public PdfView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PdfView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context.getApplicationContext();
        init();
    }

    private void init() {
        pdfiumCore = new PdfiumCore(context);
        canvas = new Canvas();
        paint = new Paint();
        handler = new PdfHandler(this);
    }


    public void setDefaultPage(int defaultPage) {
        this.defaultPage = defaultPage;
    }

    public void setOrientation(int orientation) {
        this.orientation = orientation;
    }

    public Builder fromAsset(String assetName) {
        return new Builder(new AssetSource(assetName));
    }

    public Builder fromFile(File file) {
        return new Builder(new FileSource(file));
    }

    public Builder fromUri(Uri uri) {
        return new Builder(new UriSource(uri));
    }

    public Builder fromBytes(byte[] bytes) {
        return new Builder(new ByteArraySource(bytes));
    }

    public Builder fromStream(InputStream stream) {
        return new Builder(new InputStreamSource(stream));
    }

    public Builder fromSource(DocumentSource docSource) {
        return new Builder(docSource);
    }

    public void load(DocumentSource source, String password) {
        PdfCreateRunnable createRunnable = new PdfCreateRunnable(source, pdfiumCore, password, handler);
        EXECUTOR.execute(createRunnable);
    }

    private Bitmap bitmap;

    private void onRenderBitmap(Bitmap bitmap) {
        if (this.bitmap != null) {
            this.bitmap.recycle();
        }
        this.bitmap = bitmap;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (bitmap != null && !bitmap.isRecycled()) {
            canvas.drawBitmap(bitmap, 0, 0, paint);
        }
    }

    public static class PdfHandler extends Handler {
        private static final int CREATE_DOCUMENT = 0;
        private static final int RENDER_BITMAP = 1;

        private PdfView pdfView;

        private PdfHandler(PdfView pdfView) {
            this.pdfView = pdfView;
        }

        public void sendCreateMsg(PdfDocument pdfDocument) {
            Message message = obtainMessage();
            message.what = PdfView.PdfHandler.CREATE_DOCUMENT;
            message.obj = pdfDocument;
            message.sendToTarget();
        }

        public void sendRenderedMsg(Bitmap bitmap) {
            Message message = obtainMessage();
            message.what = PdfView.PdfHandler.RENDER_BITMAP;
            message.obj = bitmap;
            message.sendToTarget();
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CREATE_DOCUMENT:
                    pdfView.pdfDocument = (PdfDocument) msg.obj;
                    renderBitmap();
                    break;
                case RENDER_BITMAP:
                    Bitmap bitmap = (Bitmap) msg.obj;
                    pdfView.onRenderBitmap(bitmap);
                    break;
            }
        }

        private void renderBitmap() {
            PdfRenderRunnable renderRunnable = new PdfRenderRunnable(pdfView.pdfiumCore, pdfView.pdfDocument, pdfView.defaultPage, this);
            EXECUTOR.execute(renderRunnable);
        }
    }


    public class Builder {
        private int defaultPage = 0;
        private int orientation = VERTICAL;
        private String password = null;
        private DocumentSource source;

        private Builder(DocumentSource source) {
            this.source = source;
        }

        public Builder setDefaultPage(int defaultPage) {
            this.defaultPage = defaultPage;
            return this;
        }

        public Builder setOrientation(int orientation) {
            this.orientation = orientation;
            return this;
        }

        public Builder setPassword(String password) {
            this.password = password;
            return this;
        }

        public void load() {
            PdfView.this.setDefaultPage(defaultPage);
            PdfView.this.setOrientation(orientation);
            PdfView.this.load(source, password);
        }
    }
}
