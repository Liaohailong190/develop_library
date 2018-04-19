package org.liaohailong.library.http;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import org.liaohailong.library.util.Utility;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * 使用OKHttp做为请求网络任务体
 * Created by LHL on 2017/10/2.
 */

public class OKHttpWorker extends HttpWorker {
    private static final String CONTENT_ENCODING = "Content-Encoding";
    private static final String GZIP = "gzip";

    private OkHttpClient client;

    public OKHttpWorker() {
        int timeout = getTimeout();
        client = new OkHttpClient.Builder()
                .connectTimeout(timeout, TimeUnit.MILLISECONDS)
                .writeTimeout(timeout, TimeUnit.MILLISECONDS)
                .readTimeout(timeout, TimeUnit.MILLISECONDS)
                .build();
    }

    @Override
    public Runnable request() {
        return new Runnable() {
            @Override
            public void run() {
                final String url = getUrl();
                Request.Builder builder = new Request.Builder();
                setHeaders(builder);
                builder.url(url);
                //添加post请求参数
                Map<String, String> paramMap = getParamMap();
                if (!paramMap.isEmpty()) {
                    FormBody.Builder formBuilder = new FormBody.Builder();
                    for (Map.Entry<String, String> entry : paramMap.entrySet()) {
                        String key = entry.getKey();
                        String value = entry.getValue();
                        formBuilder.add(key, value);
                    }
                    FormBody build = formBuilder.build();
                    builder.post(build);
                } else {
                    builder.get();
                }
                Request request = builder.build();
                Call call = client.newCall(request);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        OKHttpWorker.this.onFailure(url, e.toString());
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        ResponseBody body = response.body();
                        if (body == null) {
                            OKHttpWorker.this.onFailure(url, "ResponseBody = null");
                            return;
                        }
                        InputStream is = body.byteStream();
                        // 注意这里 ↓
                        Headers headers = response.headers();
                        for (String name : headers.names()) {
                            //获取内容编译类型
                            if (TextUtils.equals(name, CONTENT_ENCODING)) {
                                String value = headers.get(name);
                                //内容编译类型为GZIP
                                if (TextUtils.equals(value, GZIP)) {
                                    is = new java.util.zip.GZIPInputStream(is);
                                }
                            }
                        }
                        String result = Utility.streamToString(is, "UTF-8");
                        onSuccess(url, result);
                    }
                });
            }
        };
    }

    private static void setHeaders(Request.Builder builder) {
        builder.addHeader("Connection", "close");
        builder.addHeader("Charset", "UTF-8");
        builder.addHeader("Accept-Encoding", "gzip,deflate");
    }
}
