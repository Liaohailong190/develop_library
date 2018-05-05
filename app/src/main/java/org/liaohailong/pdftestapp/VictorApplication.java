package org.liaohailong.pdftestapp;

import android.support.annotation.Nullable;

import com.squareup.leakcanary.LeakCanary;

import org.liaohailong.library.RootApplication;
import org.liaohailong.pdftestapp.model.Student;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

/**
 * 本项目application类
 * Created by LHL on 2017/9/6.
 */

public class VictorApplication extends RootApplication {

    /**
     * 数据库建表
     */
    private final Class[] SQL_TABLE = new Class[]{Student.class};

    @Override
    public void onCreate() {
        super.onCreate();
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);
    }

    @Override
    public Class[] getTableClasses() {
        return SQL_TABLE;
    }

//    //初始化Client
//    long maxSize = 50_1024;
//    long ping = 3_000;
//    long timeout = 3_000;
//    OkHttpClient client = new OkHttpClient.Builder()
//            .cache(new Cache(getCacheDir(), maxSize))
//            .connectTimeout(timeout, TimeUnit.MILLISECONDS)
//            .readTimeout(timeout, TimeUnit.MILLISECONDS)
//            .writeTimeout(timeout, TimeUnit.MILLISECONDS)
//            .retryOnConnectionFailure(true)
//            .pingInterval(ping, TimeUnit.MILLISECONDS)
//            .cookieJar(new CookieJar() {
//                private Map<String, List<Cookie>> cookieStore = new HashMap<>();
//
//                @Override
//                public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
//                    cookieStore.put(url.host(), cookies);
//                }
//
//                @Override
//                public List<Cookie> loadForRequest(HttpUrl url) {
//                    List<Cookie> cookies = cookieStore.get(url.host());
//                    return cookies != null ? cookies : new ArrayList<Cookie>();
//                }
//            })
//            .build();
//
//    //发送一次POST请求
//    CacheControl build = new CacheControl.Builder().build();
//    FormBody requestBody = new FormBody.Builder()
//            .add("key", "value")
//            .build();
//    Request request = new Request.Builder()
//            .url("")
//            .post(requestBody)
//            .cacheControl(build)
//            .build();
//    Call call = client.newCall(request);
//    Callback callback = new Callback() {
//        @Override
//        public void onFailure(Call call, IOException e) {
//
//        }
//
//        @Override
//        public void onResponse(Call call, Response response) throws IOException {
//            //此处可以做文件下载的功能
//        }
//    };
//        call.enqueue(callback);
//
//    //发送一次文件上传
//    MultipartBody multipartBody = new MultipartBody.Builder()
//            .setType(MultipartBody.FORM)
//            .addFormDataPart("params", "i am params")
//            .addFormDataPart("file", "filename", RequestBody.create(MultipartBody.FORM, new File("")))
//            .build();
//    ProgressRequestBody.OnProgressCallback onProgressCallback = new ProgressRequestBody.OnProgressCallback() {
//        @Override
//        public void onProgress(int progress) {
//
//        }
//    };
//    ProgressRequestBody progressRequestBody = new ProgressRequestBody(multipartBody, onProgressCallback);
//    Request multipartRequest = new Request.Builder()
//            .url("")
//            .post(progressRequestBody)
//            .build();
//    Call multipartCall = client.newCall(multipartRequest);
//    Callback multipartCallback = new Callback() {
//        @Override
//        public void onFailure(Call call, IOException e) {
//
//        }
//
//        @Override
//        public void onResponse(Call call, Response response) throws IOException {
//
//        }
//    };
//        multipartCall.enqueue(multipartCallback);
//
//
//    //建立WebSocket链接
//    Request wekSocketRequest = new Request.Builder()
//            .url("")
//            .get()
//            .build();
//    WebSocketListener webSocketListener = new WebSocketListener() {
//        /**
//         * Invoked when a web socket has been accepted by the remote peer and may begin transmitting
//         * messages.
//         */
//        public void onOpen(WebSocket webSocket, Response response) {
//
//        }
//
//        /** Invoked when a text (type {@code 0x1}) message has been received. */
//        public void onMessage(WebSocket webSocket, String text) {
//
//        }
//
//        /** Invoked when a binary (type {@code 0x2}) message has been received. */
//        public void onMessage(WebSocket webSocket, ByteString bytes) {
//
//        }
//
//        /**
//         * Invoked when the remote peer has indicated that no more incoming messages will be
//         * transmitted.
//         */
//        public void onClosing(WebSocket webSocket, int code, String reason) {
//
//        }
//
//        /**
//         * Invoked when both peers have indicated that no more messages will be transmitted and the
//         * connection has been successfully released. No further calls to this listener will be made.
//         */
//        public void onClosed(WebSocket webSocket, int code, String reason) {
//
//        }
//
//        /**
//         * Invoked when a web socket has been closed due to an error reading from or writing to the
//         * network. Both outgoing and incoming messages may have been lost. No further calls to this
//         * listener will be made.
//         */
//        public void onFailure(WebSocket webSocket, Throwable t, @Nullable Response response) {
//
//        }
//    };
//    WebSocket webSocket = client.newWebSocket(wekSocketRequest, webSocketListener);
//        webSocket.send("");
}
