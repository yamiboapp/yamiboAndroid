package com.yamibo.main.yamibolib.dataservice.http.impl;

import com.yamibo.main.yamibolib.dataservice.http.HttpResponse;

import java.util.Map;

/**
 * Created by wangxiaoyan on 15/6/17.
 */
public class BasicHttpResponse implements HttpResponse {

    private int mStatusCode = 0;
    private Map<String, String> mHeaders;
    private Object mResult;
    private Object mError;

    public BasicHttpResponse(int statusCode, Map<String, String> headers, Object result, Object error) {
        mStatusCode = statusCode;
        mHeaders = headers;
        mResult = result;
        mError = error;
    }

    @Override
    public int statusCode() {
        return mStatusCode;
    }

    @Override
    public Map<String, String> headers() {
        return mHeaders;
    }

    @Override
    public Object result() {
        return mResult;
    }

    @Override
    public Object error() {
        return mError;
    }

    @Override
    public String toString() {
        return "statusCode: " + mStatusCode + "   result: " + mResult + "   error: " + mError;
    }
}
