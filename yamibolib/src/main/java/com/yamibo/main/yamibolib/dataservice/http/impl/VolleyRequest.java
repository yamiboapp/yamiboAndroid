package com.yamibo.main.yamibolib.dataservice.http.impl;

import android.os.Handler;
import android.os.Message;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.yamibo.main.yamibolib.Utils.Environment;
import com.yamibo.main.yamibolib.dataservice.RequestHandler;
import com.yamibo.main.yamibolib.dataservice.http.HttpRequest;
import com.yamibo.main.yamibolib.dataservice.http.HttpResponse;

import org.json.JSONObject;

import java.io.IOException;
import java.util.Map;

/**
 * Created by wangxiaoyan on 15/11/12.
 */
public class VolleyRequest extends JsonObjectRequest {

    private HttpRequest mHttpRequest;
    private RequestHandler<HttpRequest, HttpResponse> mRequestHandler;

    private final static int MESSAGE_REQUEST_SUCCEED = 0;
    private final static int MESSAGE_REQUEST_FAILED = 1;

    private Handler mHandler = new Handler(/*Looper.getMainLooper()*/) {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MESSAGE_REQUEST_SUCCEED) {
                if (mRequestHandler != null) {
                    mRequestHandler.onRequestFinish(mHttpRequest, (BasicHttpResponse) msg.obj);
                }
            } else if (msg.what == MESSAGE_REQUEST_FAILED) {
                if (mRequestHandler != null) {
                    mRequestHandler.onRequestFailed(mHttpRequest, (BasicHttpResponse) msg.obj);
                }
            }
        }
    };

    public VolleyRequest(HttpRequest httpRequest) {
        this(httpRequest, null);
    }

    public VolleyRequest(final HttpRequest httpRequest, final RequestHandler<HttpRequest, HttpResponse> requestHandler) {
        super(httpRequest.method(), Environment.isDebug() ? httpRequest.url().replaceFirst("www.", "ceshi.") : httpRequest.url(), null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });

        mHttpRequest = httpRequest;
        mRequestHandler = requestHandler;
    }

    @Override
    public byte[] getBody() {
        byte[] bytes = null;
        try {
            bytes = new byte[mHttpRequest.input().available()];
            mHttpRequest.input().read(bytes);
            mHttpRequest.input().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bytes;
//                return req.input().toString().getBytes();
    }

    public String getBodyContentType() {
        return "application/x-www-form-urlencoded; charset=" + getParamsEncoding();
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return mHttpRequest.headers() != null ? mHttpRequest.headers() : super.getHeaders();
    }

    @Override
    protected VolleyError parseNetworkError(VolleyError volleyError) {
        Message message = mHandler.obtainMessage();
        message.what = MESSAGE_REQUEST_FAILED;
        message.obj = new BasicHttpResponse(400, null, null, volleyError);
        mHandler.sendMessage(message);
        return super.parseNetworkError(volleyError);
    }

    @Override
    protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
        final Response<JSONObject> superResponse = super.parseNetworkResponse(response);
        Message message = mHandler.obtainMessage();
        message.what = MESSAGE_REQUEST_SUCCEED;
        message.obj = new BasicHttpResponse(response.statusCode, response.headers, superResponse.result, null);
        mHandler.sendMessage(message);
        return superResponse;
    }
}
