package org.liaohailong.pdftestapp.http;

import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import org.liaohailong.pdftestapp.util.Utility;

import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * 请求网络工具类
 * Created by LHL on 2017/9/4.
 */

public class HttpUtils {
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final ExecutorService THREAD_POOL_EXECUTOR;
    private static final Handler HANDLER = new HttpHandler();
    private static final Map<String, OnHttpCallback> CALLBACK_MAP = new HashMap<>();
    private static final Map<String, Future> TASK_MAP = new HashMap<>();

    static {
        THREAD_POOL_EXECUTOR = Executors.newFixedThreadPool(CPU_COUNT);
    }

    private HttpUtils() {
    }


    public static void post(@NonNull String url, @NonNull Map<String, String> params, @NonNull OnHttpCallback callback) {
        if (TextUtils.isEmpty(url)) {
            return;
        }
        HttpTask httpTask = new HttpTask(url, params);
        Future submit = THREAD_POOL_EXECUTOR.submit(httpTask);
        CALLBACK_MAP.put(url, callback);
        TASK_MAP.put(url, submit);
    }

    public static void get(@NonNull String url, @NonNull OnHttpCallback callback) {
        if (TextUtils.isEmpty(url)) {
            return;
        }
        HttpTask httpTask = new HttpTask(url, new HashMap<String, String>());
        Future submit = THREAD_POOL_EXECUTOR.submit(httpTask);
        CALLBACK_MAP.put(url, callback);
        TASK_MAP.put(url, submit);
    }

    public static void stop(String url) {
        if (url != null) {
            Future future = TASK_MAP.get(url);
            if (future != null && !future.isDone() && future.isCancelled()) {
                future.cancel(true);
            }
            CALLBACK_MAP.remove(url);
        } else {
            for (Map.Entry<String, Future> entry : TASK_MAP.entrySet()) {
                Future future = entry.getValue();
                if (future != null && !future.isDone() && future.isCancelled()) {
                    future.cancel(true);
                }
            }
            CALLBACK_MAP.clear();
        }
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
            OnHttpCallback onHttpCallback = CALLBACK_MAP.get(url);
            if (onHttpCallback != null) {
                onHttpCallback.setResponse(code, data);
                CALLBACK_MAP.remove(url);
            }
        }
    }

    private static class HttpTask implements Runnable {
        private static final int TIME_OUT = 1000 * 15;
        private static final String PROTOCOL_HTTP = "http";
        private static final String PROTOCOL_HTTPS = "https";
        private static final char PARAMETER_DELIMITER = '&';
        private static final char PARAMETER_EQUALS_CHAR = '=';
        private static final String UTF_8 = "UTF-8";
        private static final String POST = "POST";
        private static final String GET = "GET";
        private static final String CONTENT_TYPE = "Content-Type";
        private static final String X_WWW_FORM_URL_ENCODE = "application/x-www-form-urlencoded";
        private static final String GZIP = "gzip";


        private String url = "";
        private Map<String, String> params;
        private HttpURLConnection connection;
        private SSLContext mSSLContext;

        private HttpTask(String url, Map<String, String> params) {
            this.url = url;
            this.params = params;
        }

        @Override
        public void run() {
            PrintWriter out = null;
            InputStream is = null;
            try {
                connection = openConnection(url);
                setParams();
                setHeaders();
                String parameters = createQueryStringForParameters(params);
                if (TextUtils.isEmpty(parameters)) {
                    connection.setRequestMethod(GET);
                    connection.setDoOutput(false);
                } else {
                    connection.setRequestMethod(POST);
                    connection.setDoOutput(true);
                    connection.setFixedLengthStreamingMode(parameters.getBytes(UTF_8).length);
                    connection.addRequestProperty(CONTENT_TYPE, X_WWW_FORM_URL_ENCODE);
                }
                connection.connect();

                if (!TextUtils.isEmpty(parameters)) {
                    out = new PrintWriter(new OutputStreamWriter(connection.getOutputStream(), UTF_8));
                    out.print(parameters);
                    out.flush();
                }

                int code = connection.getResponseCode();
                if (code < 400) {
                    is = connection.getInputStream();
                } else {
                    is = connection.getErrorStream();
                }
                String enc = connection.getContentEncoding();
                // 注意这里 ↓
                if (enc != null && enc.equals(GZIP)) {
                    is = new java.util.zip.GZIPInputStream(is);
                }
                String result = Utility.streamToString(is);
                HttpResponse httpResponse = new HttpResponse();
                httpResponse.setUrl(url);
                httpResponse.setCode(code);
                httpResponse.setData(result);
                httpResponse.setError(result);
                Message message = HANDLER.obtainMessage();
                message.obj = httpResponse;
                message.what = HttpHandler.HTTP_OK;
                message.sendToTarget();
            } catch (Exception e) {
                e.printStackTrace();
                HttpResponse httpResponse = new HttpResponse();
                httpResponse.setUrl(url);
                httpResponse.setCode(400);
                Message message = HANDLER.obtainMessage();
                message.obj = httpResponse;
                message.what = HttpHandler.HTTP_FAIL;
                message.sendToTarget();
            } finally {
                Utility.close(out);
                Utility.close(is);
            }
        }

        private HttpURLConnection openConnection(String url) throws Exception {
            HttpURLConnection connection;
            URL uri = new URL(url);
            String protocol = uri.getProtocol();
            switch (protocol) {
                case PROTOCOL_HTTP:
                    connection = (HttpURLConnection) uri.openConnection();
                    break;

                case PROTOCOL_HTTPS:
                    if (mSSLContext == null) {
                        mSSLContext = SSLContext.getInstance("TLS");
                        mSSLContext.init(null, new TrustManager[]{new TrustAllManager()}, null);
                    }

                    connection = (HttpsURLConnection) uri.openConnection();

                    ((HttpsURLConnection) connection).setSSLSocketFactory(mSSLContext.getSocketFactory());
                    ((HttpsURLConnection) connection).setHostnameVerifier(new HostnameVerifier() {

                        @Override
                        public boolean verify(String hostname, SSLSession session) {
                            return true;
                        }
                    });

                    break;

                default:
                    throw new Exception("not support protocol");
            }
            return connection;
        }

        private void setHeaders() {
            connection.setRequestProperty("Connection", "close");
            connection.addRequestProperty("Charset", "UTF-8");
            connection.addRequestProperty("Accept-Encoding", "gzip,deflate");
        }

        private void setParams() {
            int timeout = getTimeout();
            connection.setDoInput(true);
            connection.setReadTimeout(timeout);
            connection.setConnectTimeout(timeout);
        }

        private int getTimeout() {
            return TIME_OUT;
        }

        /**
         * 将post里的字段封装
         *
         * @param parameters
         * @return
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

    private static class TrustAllManager implements X509TrustManager {

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {

        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {

        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }
    }


}
