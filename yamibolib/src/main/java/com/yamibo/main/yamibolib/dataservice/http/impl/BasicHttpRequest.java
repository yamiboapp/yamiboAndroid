package com.yamibo.main.yamibolib.dataservice.http.impl;

import com.yamibo.main.yamibolib.dataservice.http.HttpRequest;

import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Created by wangxiaoyan on 15/6/17.
 */
public class BasicHttpRequest implements HttpRequest {

    public static final String GET = HttpGet.METHOD_NAME;
    public static final String POST = HttpPost.METHOD_NAME;
    public static final String PUT = HttpPut.METHOD_NAME;
    public static final String DELETE = HttpDelete.METHOD_NAME;

    private String method;
    private InputStream input;
    private Map<String, String> headers;
    private long timeout;
    private String url;
    private boolean shouldCache = true;

    public BasicHttpRequest(String url, String method, InputStream input) {
        this(url, method, input, null, 0, true);
    }

    public BasicHttpRequest(String url, String method, InputStream input,
                            Map<String, String> headers) {
        this(url, method, input, headers, 0, true);
    }

    public BasicHttpRequest(String url, String method, InputStream input,
                            Map<String, String> headers, long timeout, boolean shouldCache) {
        this.url = url;
        this.method = method;
        this.input = input;
        this.headers = headers;
        this.timeout = timeout;
        this.shouldCache = shouldCache;
    }

    public static HttpRequest httpGet(String url) {
        return new BasicHttpRequest(url, GET, null, null);
    }

    public static HttpRequest httpPost(String url, List<NameValuePair> forms) {
        return new BasicHttpRequest(url, POST, new FormInputStream(forms), null);
    }

    public static HttpRequest httpPost(String url, InputStream input) {
        return new BasicHttpRequest(url, POST, input, null);
    }

    @Override
    public String url() {
        return url;
    }

    @Override
    public String method() {
        return method;
    }

    @Override
    public InputStream input() {
        return input;
    }

    @Override
    public Map<String, String> headers() {
        return headers;
    }

    @Override
    public long timeout() {
        return timeout;
    }

    @Override
    public boolean isShouldCache() {
        return shouldCache;
    }

    @Override
    public void addHeaders(Map<String, String> headers) {
        if (headers == null) return;
        if (this.headers != null) {
            this.headers.putAll(headers);
        } else {
            this.headers = headers;
        }
    }

    @Override
    public String toString() {
        return method + ": " + super.toString();
    }
}
