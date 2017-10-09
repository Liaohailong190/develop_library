package org.liaohailong.library.http;

import android.text.TextUtils;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.Headers;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.liaohailong.library.util.Utility;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * 使用OKHttp做为请求网络任务体
 * Created by LHL on 2017/10/2.
 */

public class OKHttpWorker extends HttpWorker {
    private static final String CONTENT_ENCODING = "Content-Encoding";
    private static final String GZIP = "gzip";

    private OkHttpClient client;

    public OKHttpWorker() {
        client = new OkHttpClient();
        setParams(client);
    }

    @Override
    public Future request() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                final String url = getUrl();
                Request.Builder builder = new Request.Builder();
                setHeaders(builder);
                builder.url(url);
                //添加post请求参数
                Map<String, String> paramMap = getParamMap();
                if (!paramMap.isEmpty()) {
                    FormEncodingBuilder formEncodingBuilder = new FormEncodingBuilder();
                    for (Map.Entry<String, String> entry : paramMap.entrySet()) {
                        String key = entry.getKey();
                        String value = entry.getValue();
                        formEncodingBuilder.add(key, value);
                    }
                    RequestBody requestBody = formEncodingBuilder.build();
                    builder.post(requestBody);
                } else {
                    builder.get();
                }
                Request request = builder.build();
                Call call = client.newCall(request);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Request request, IOException e) {
                        OKHttpWorker.this.onFailure(url, e.toString());
                    }

                    @Override
                    public void onResponse(Response response) throws IOException {
                        InputStream is = response.body().byteStream();
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
        return THREAD_POOL_EXECUTOR.submit(runnable);
    }

    private static void setHeaders(Request.Builder builder) {
        builder.addHeader("Connection", "close");
        builder.addHeader("Charset", "UTF-8");
        builder.addHeader("Accept-Encoding", "gzip,deflate");
    }

    private static void setParams(OkHttpClient client) {
        int timeout = getTimeout();
        client.setWriteTimeout(timeout, TimeUnit.MILLISECONDS);
        client.setReadTimeout(timeout, TimeUnit.MILLISECONDS);
        client.setConnectTimeout(timeout, TimeUnit.MILLISECONDS);
    }
}
