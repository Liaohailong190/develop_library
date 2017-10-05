package org.liaohailong.library.http;

import android.text.TextUtils;

import org.liaohailong.library.util.Utility;

import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * 使用HttpUrlConnection请求网络的任务体
 * Created by LHL on 2017/10/2.
 */

public class HttpUrlConnectionWorker extends HttpWorker {
    private static final String PROTOCOL_HTTP = "http";
    private static final String PROTOCOL_HTTPS = "https";
    private static final String UTF_8 = "UTF-8";
    private static final String POST = "POST";
    private static final String GET = "GET";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String X_WWW_FORM_URL_ENCODE = "application/x-www-form-urlencoded";
    private static final String GZIP = "gzip";

    public void request() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                PrintWriter out = null;
                InputStream is = null;
                try {
                    String url = getUrl();
                    HttpURLConnection connection = openConnection(url);
                    setParams(connection);
                    setHeaders(connection);
                    String parameters = getParams();
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
                    boolean success;
                    if (code < 400) {
                        is = connection.getInputStream();
                        success = true;
                    } else {
                        is = connection.getErrorStream();
                        success = false;
                    }
                    String enc = connection.getContentEncoding();
                    // 注意这里 ↓
                    if (enc != null && enc.equals(GZIP)) {
                        is = new java.util.zip.GZIPInputStream(is);
                    }
                    String result = Utility.streamToString(is);
                    response(url, code, result, success);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    onFailure(getUrl(), "");
                } finally {
                    Utility.close(out);
                    Utility.close(is);
                }
            }
        };
        THREAD_POOL_EXECUTOR.execute(runnable);
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
                SSLContext mSSLContext = SSLContext.getInstance("TLS");
                mSSLContext.init(null, new TrustManager[]{new TrustAllManager()}, null);

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

    private static void setHeaders(HttpURLConnection connection) {
        connection.setRequestProperty("Connection", "close");
        connection.addRequestProperty("Charset", "UTF-8");
        connection.addRequestProperty("Accept-Encoding", "gzip,deflate");
    }

    private static void setParams(HttpURLConnection connection) {
        int timeout = getTimeout();
        connection.setDoInput(true);
        connection.setReadTimeout(timeout);
        connection.setConnectTimeout(timeout);
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
