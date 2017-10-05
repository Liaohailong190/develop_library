package org.liaohailong.library.http;


import java.util.HashMap;
import java.util.Map;

/**
 * 请求网络工具类
 * Created by LHL on 2017/9/4.
 */

public class Http {
    private static HttpWorker HTTP_WORKER = new OKHttpWorker();
    //请求参数相关
    private String url;
    private Map<String, String> params = new HashMap<>();
    private HttpWorker tempWorker;

    private Http() {
    }

    public static void initWorker(HttpWorker worker) {
        if (worker != null) {
            HTTP_WORKER = worker;
        }
    }

    public static Http create() {
        return new Http();
    }

    public Http url(String url) {
        this.url = url;
        return this;
    }

    public Http params(String key, String value) {
        params.put(key, value);
        return this;
    }

    public Http params(Map<String, String> params) {
        this.params.putAll(params);
        return this;
    }

    public Http worker(HttpWorker httpWorker) {
        tempWorker = httpWorker;
        return this;
    }

    public void execute(HttpCallback callback) {
        //动态切换任务执行者，如果用户设置的话
        HttpWorker worker = tempWorker != null ? tempWorker : HTTP_WORKER;
        worker.setUrl(url);
        worker.setParams(params);
        worker.setCallback(callback);
        worker.onPreExecute();
        worker.request();
    }
}
