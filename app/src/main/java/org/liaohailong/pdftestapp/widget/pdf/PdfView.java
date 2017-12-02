package org.liaohailong.pdftestapp.widget.pdf;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.shockwave.pdfium.PdfDocument;
import com.shockwave.pdfium.PdfiumCore;

import org.liaohailong.pdftestapp.widget.pdf.source.AssetSource;
import org.liaohailong.pdftestapp.widget.pdf.source.ByteArraySource;
import org.liaohailong.pdftestapp.widget.pdf.source.DocumentSource;
import org.liaohailong.pdftestapp.widget.pdf.source.FileSource;
import org.liaohailong.pdftestapp.widget.pdf.source.InputStreamSource;
import org.liaohailong.pdftestapp.widget.pdf.source.UriSource;
import org.liaohailong.pdftestapp.widget.pdf.task.PdfCreateRunnable;
import org.liaohailong.pdftestapp.widget.pdf.task.PdfRenderRunnable;
import org.liaohailong.pdftestapp.widget.photoview.PhotoView;

import java.io.File;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;
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
    private RecyclerView recyclerView;//展示列表
    private PdfAdapter pdfAdapter;//展示列表适配器

    private PdfiumCore pdfiumCore;
    private PdfDocument pdfDocument;
    private Canvas canvas;
    private Paint paint;
    private PdfHandler handler;

    //配置属性相关
    private int defaultPage = 0;
    private int orientation = VERTICAL;
    private int pageCount = 0;

    //数据缓存相关
    private Map<String, SoftReference<Bitmap>> save = new HashMap<>();


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

        //使用列表展示pdf，性能会优化很多
        recyclerView = new RecyclerView(context);
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        recyclerView.setLayoutParams(params);
        addView(recyclerView);
    }


    public void setDefaultPage(int defaultPage) {
        this.defaultPage = defaultPage;
    }

    public void setOrientation(int orientation) {
        this.orientation = orientation;
    }

    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }

    public int getPageCount() {
        return pageCount;
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
        //初始化PDF展示列表
        LinearLayoutManager manager = new LinearLayoutManager(context);
        manager.setOrientation(orientation == VERTICAL ? LinearLayoutManager.VERTICAL : LinearLayoutManager.HORIZONTAL);
        recyclerView.setLayoutManager(manager);
        pdfAdapter = new PdfAdapter();
        recyclerView.setAdapter(pdfAdapter);
        //开始创建pdf文件相关信息
        PdfCreateRunnable createRunnable = new PdfCreateRunnable(source, pdfiumCore, password, handler);
        EXECUTOR.execute(createRunnable);
    }

    public int getCurrentPage(boolean next) {
        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        return next ? layoutManager.findFirstVisibleItemPosition() : layoutManager.findLastVisibleItemPosition();
    }

    public void jumpToPrevious() {
        int currentPage = getCurrentPage(false);
        //已经到了第一个不再跳转
        if (currentPage == 0) {
            return;
        }
        currentPage--;
        jumpTo(currentPage, true);
    }

    public void jumpToNext() {
        int currentPage = getCurrentPage(true);
        if (currentPage >= getPageCount() - 1) {
            return;
        }
        currentPage++;
        jumpTo(currentPage, true);
    }

    public void jumpTo(int pageIndex) {
        jumpTo(pageIndex, false);
    }

    public void jumpTo(int pageIndex, boolean anim) {
        int childCount = getPageCount();
        if (pageIndex < 0 || pageIndex > childCount) {
            return;
        }
        if (anim) {
            recyclerView.smoothScrollToPosition(pageIndex);
        } else {
            recyclerView.scrollToPosition(pageIndex);
        }
    }

    public void release() {
        recyclerView.removeAllViews();
        recyclerView = null;
        for (Map.Entry<String, SoftReference<Bitmap>> entry : save.entrySet()) {
            SoftReference<Bitmap> bitmapSoftReference = entry.getValue();
            Bitmap bitmap = bitmapSoftReference.get();
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
            }
        }
        save.clear();
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

        public void sendRenderedMsg(PdfResult result) {
            Message message = obtainMessage();
            message.what = PdfView.PdfHandler.RENDER_BITMAP;
            message.obj = result;
            message.sendToTarget();
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CREATE_DOCUMENT:
                    pdfView.pdfDocument = (PdfDocument) msg.obj;
                    //信息创建完毕，开始拉取pdf页数
                    initRecyclerView();
                    break;
                case RENDER_BITMAP:
                    PdfResult result = (PdfResult) msg.obj;
                    renderBitmap(result);
                    break;
            }
        }

        private void initRecyclerView() {
            if (pdfView == null) {
                return;
            }
            int pageCount = pdfView.pdfiumCore.getPageCount(pdfView.pdfDocument);
            int width = pdfView.getWidth();
            int height = pdfView.getHeight();
            pdfView.setPageCount(pageCount);
            pdfView.pdfAdapter.set(pageCount, width, height);
            pdfView.pdfAdapter.notifyDataSetChanged();
            pdfView.recyclerView.post(new Runnable() {
                @Override
                public void run() {
                    int defaultPage = pdfView.defaultPage;
                    pdfView.recyclerView.scrollToPosition(defaultPage);
                }
            });
        }

        private void renderBitmap(PdfResult result) {
            if (result != null) {
                PhotoView view = result.getView();
                Bitmap bitmap = result.getBitmap();
                if (view != null && bitmap != null) {
                    view.setImageBitmap(bitmap);
                    int pageIndex = result.getPageIndex();
                    pdfView.save.put(String.valueOf(pageIndex), new SoftReference<>(bitmap));
                }
            }
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

    private class PdfViewHolder extends RecyclerView.ViewHolder {
        private PdfAdapter adapter;
        private PhotoView photoView;

        private PdfViewHolder(PdfAdapter adapter, View itemView) {
            super(itemView);
            if (itemView instanceof PhotoView) {
                photoView = (PhotoView) itemView;
            }
            this.adapter = adapter;
        }

        private void bind(int position) {
            SoftReference<Bitmap> bitmapSoftReference = save.get(String.valueOf(position));
            boolean noSoftReference = bitmapSoftReference == null;
            boolean noBitmap = noSoftReference || bitmapSoftReference.get() == null;
            /*
            * 1，软引用缓存没有
            * 2，图片bitmap对象没有
            * 3，缩放比例发生变化
            * 以上情况均需要重新加载当前页的pdf数据
            * */
            if (noSoftReference || noBitmap) {
                PdfResult pdfResult = new PdfResult();
                pdfResult.setView(photoView);
                pdfResult.setPageIndex(position);
                PdfRenderRunnable renderRunnable = new PdfRenderRunnable(pdfResult, pdfiumCore, pdfDocument, handler);
                EXECUTOR.execute(renderRunnable);
            } else {
                Bitmap bitmap = bitmapSoftReference.get();
                if (bitmap != null) {
                    photoView.setImageBitmap(bitmap);
                }
            }
        }

    }

    private class PdfAdapter extends RecyclerView.Adapter<PdfViewHolder> {
        private int pageCount = 0;
        private int width = 0;
        private int height = 0;

        private void set(int pageCount, int width, int height) {
            this.pageCount = pageCount;
            this.width = width;
            this.height = height;
        }

        @Override
        public PdfViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            PhotoView photoView = new PhotoView(parent.getContext());
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(width, height);
            photoView.setLayoutParams(params);
            return new PdfViewHolder(this, photoView);
        }

        @Override
        public void onBindViewHolder(PdfViewHolder holder, int position) {
            int page = position % pageCount;
            holder.bind(page);
        }

        @Override
        public int getItemCount() {
            return pageCount;
        }
    }

    public static class PdfResult {
        private PhotoView view;
        private Bitmap bitmap;
        private int pageIndex = 0;

        public int getPageIndex() {
            return pageIndex;
        }

        public void setPageIndex(int pageIndex) {
            this.pageIndex = pageIndex;
        }

        public PhotoView getView() {
            return view;
        }

        public void setView(PhotoView view) {
            this.view = view;
        }

        private Bitmap getBitmap() {
            return bitmap;
        }

        public void setBitmap(Bitmap bitmap) {
            this.bitmap = bitmap;
        }
    }
}
