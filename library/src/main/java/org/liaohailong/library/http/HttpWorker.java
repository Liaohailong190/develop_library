package org.liaohailong.library.http;


import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import java.net.URLEncoder;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Http请求任务规范
 * Created by LHL on 2017/9/4.
 */

public abstract class HttpWorker {
    //配置参数相关
    private static final int TIME_OUT = 1000 * 15;
    //请求网络主体相关
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static ThreadPoolExecutor THREAD_POOL_EXECUTOR;
    private static final Set<Future> SUBMIT = new HashSet<>();

    private static final char PARAMETER_DELIMITER = '&';
    private static final char PARAMETER_EQUALS_CHAR = '=';
    private static final HttpHandler HANDLER = new HttpHandler();
    private String url;
    private Map<String, String> params;
    private HttpCallback callback;

    public void setUrl(String mUrl) {
        this.url = mUrl;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }

    public void setCallback(HttpCallback callback) {
        this.callback = callback;
    }

    protected String getUrl() {
        return url;
    }

    protected String getParams() {
        return createQueryStringForParameters(params);
    }

    protected Map<String, String> getParamMap() {
        return params;
    }

    public HttpCallback getCallback() {
        return callback;
    }

    protected static int getTimeout() {
        return TIME_OUT;
    }

    /**
     * 子类需要实现此方法，做请求网络的执行工作
     */
    protected abstract Runnable request();

    /**
     * 开始请求任务
     */
    void start() {
        Runnable runnable = request();
        submitTask(runnable);
    }

    /**
     * @param runnable 需要提交的任务
     */
    private void submitTask(Runnable runnable) {
        if (THREAD_POOL_EXECUTOR == null || THREAD_POOL_EXECUTOR.isShutdown()) {
            THREAD_POOL_EXECUTOR = (ThreadPoolExecutor) Executors.newFixedThreadPool(CPU_COUNT);
        }
        Future submit = THREAD_POOL_EXECUTOR.submit(runnable);
        SUBMIT.add(submit);
    }

    /**
     * 清除请求任务
     *
     * @param future 已请求任务的返回对象
     */
    public static void clear(Future future) {
        //参数为null表示清楚当前所有任务（执行中+队列中）
        if (future == null) {
            for (Future submit : SUBMIT) {
                boolean cancel = submit.cancel(true);
            }
            SUBMIT.clear();
            if (THREAD_POOL_EXECUTOR != null) {
                if (!THREAD_POOL_EXECUTOR.isShutdown()) {
                    THREAD_POOL_EXECUTOR.shutdownNow();
                }
                THREAD_POOL_EXECUTOR = null;
            }
        } else {
            if (SUBMIT.contains(future)) {
                boolean cancel = future.cancel(true);
            }
            SUBMIT.remove(future);
        }
    }

    /**
     * 任务执行前回调
     */
    public void onPreExecute() {
        if (callback != null) {
            callback.onPreExecute();
        }
    }

    /**
     * 请求成功之后子类需要调用此方法
     * 用来激活回调
     *
     * @param url  请求网络的url
     * @param data 请求返回数据
     */
    protected void onSuccess(String url, String data) {
        response(url, HttpHandler.HTTP_OK, data, true);
    }

    /**
     * 请求失败之后子类需要调用此方法
     * 用来激活回调
     *
     * @param url  请求网络的url
     * @param data 请求返回数据
     */
    protected void onFailure(String url, String data) {
        response(url, HttpHandler.HTTP_FAIL, data, false);
    }

    /**
     * 请求完毕后子类需要调用此方法
     *
     * @param url     请求网络的url
     * @param code    请求状态码
     * @param data    请求返回数据
     * @param success 是否成功
     */
    protected void response(String url, int code, String data, boolean success) {
        HttpResponse httpResponse = new HttpResponse();
        httpResponse.setUrl(url);
        httpResponse.setCode(code);
        httpResponse.setData(data);
        httpResponse.setError(data);
        httpResponse.setCallback(getCallback());
        Message message = HANDLER.obtainMessage();
        message.obj = httpResponse;
        message.what = success ? HttpHandler.HTTP_OK : HttpHandler.HTTP_FAIL;
        message.sendToTarget();
    }

    private static class HttpHandler extends Handler {
        private static final int HTTP_OK = 200;
        private static final int HTTP_FAIL = 400;

        @Override
        public void handleMessage(Message msg) {
            HttpResponse response = (HttpResponse) msg.obj;
            String url = response.getUrl();
            int code = response.getCode();
            String data = response.getData();
            HttpCallback callback = response.getCallback();
            if (callback != null) {
                callback.setResponse(url, code, data);
            }
        }
    }

    /**
     * 将post里的字段封装
     *
     * @param parameters POST请求参数
     * @return POST请求参数字符串
     */
    @SuppressWarnings("deprecation")
    private String createQueryStringForParameters(Map<String, String> parameters) {
        StringBuilder sb = new StringBuilder();
        if (parameters != null) {
            boolean firstParameter = true;

            for (Map.Entry<String, String> entry : parameters.entrySet()) {
                if (!firstParameter) {
                    sb.append(PARAMETER_DELIMITER);
                }

                String name = entry.getKey();
                String value = entry.getValue();
                sb.append(URLEncoder.encode(name))
                        .append(PARAMETER_EQUALS_CHAR)
                        .append(!TextUtils.isEmpty(value) ? URLEncoder.encode(value) : "");

                firstParameter = false;
            }
        }
        return sb.toString();
    }
}
