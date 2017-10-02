package org.liaohailong.library.http;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.Map;

/**
 * 使用OKHttp做为请求网络任务体
 * Created by LHL on 2017/10/2.
 */

public class OKHttpWorker extends HttpWorker {

    private OkHttpClient client = new OkHttpClient();

    @Override
    public void request() {
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
                        String result = response.body().toString();
                        onSuccess(url, result);
                    }
                });
            }
        };
        THREAD_POOL_EXECUTOR.execute(runnable);
    }

    private static void setHeaders(Request.Builder builder) {
        builder.addHeader("Connection", "close");
        builder.addHeader("Charset", "UTF-8");
        builder.addHeader("Accept-Encoding", "gzip,deflate");
    }
}
