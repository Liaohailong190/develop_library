package org.liaohailong.library.http;


import java.util.HashMap;
import java.util.Map;

/**
 * 请求网络工具类
 * Created by LHL on 2017/9/4.
 */

public class Http {
    private static HttpWorker HTTP_WORKER = new HttpUrlConnectionWorker();
    //请求参数相关
    private String url;
    private Map<String, String> params = new HashMap<>();

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

    public void execute(HttpCallback callback) {
        HTTP_WORKER.setUrl(url);
        HTTP_WORKER.setParams(params);
        HTTP_WORKER.setCallback(callback);
        HTTP_WORKER.run();
    }
}
