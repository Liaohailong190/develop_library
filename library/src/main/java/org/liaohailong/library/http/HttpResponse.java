package org.liaohailong.library.http;

/**
 * Http数据返回体
 * Created by LHL on 2017/9/5.
 */

public class HttpResponse {
    private String url = "";
    private int code = 400;
    private String data = "";
    private String error = "";
    private HttpCallback callback;

    public HttpCallback getCallback() {
        return callback;
    }

    public void setCallback(HttpCallback callback) {
        this.callback = callback;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
