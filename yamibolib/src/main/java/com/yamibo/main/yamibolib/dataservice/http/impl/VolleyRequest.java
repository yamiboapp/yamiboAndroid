package com.yamibo.main.yamibolib.dataservice.http.impl;

import android.app.Application;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.yamibo.main.yamibolib.Utils.Environment;
import com.yamibo.main.yamibolib.Utils.Log;
import com.yamibo.main.yamibolib.accountservice.AccountService;
import com.yamibo.main.yamibolib.app.YMBApplication;
import com.yamibo.main.yamibolib.dataservice.RequestHandler;
import com.yamibo.main.yamibolib.dataservice.http.HttpRequest;
import com.yamibo.main.yamibolib.dataservice.http.HttpResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by wangxiaoyan on 15/11/12.
 */
public class VolleyRequest extends JsonObjectRequest {

    private static final String SET_COOKIE_KEY = "Set-Cookie";
    private static final String COOKIE_KEY = "Cookie";
    private static final String SESSION_COOKIE = "sessionid";

    private final static String PERFER_COOKIE_STRING = "com.yamibo.cookie_string";
    private SharedPreferences preferences;

    private HttpRequest mHttpRequest;
    private RequestHandler<HttpRequest, HttpResponse> mRequestHandler;

    private final static int MESSAGE_REQUEST_SUCCEED = 0;
    private final static int MESSAGE_REQUEST_FAILED = 1;

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
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
        preferences = YMBApplication.preferences();
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
        String headers = preferences.getString(PERFER_COOKIE_STRING, null);
        if (headers != null) {
            Map<String, String> header = new HashMap<>();
            header.put(COOKIE_KEY, headers);
            mHttpRequest.addHeaders(header);
        }
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
        Map<String, String> responseHeaders = response.headers;
        if (responseHeaders != null) {
            preferences.edit().putString(PERFER_COOKIE_STRING, responseHeaders.get(SET_COOKIE_KEY)).commit();
        }
        Message message = mHandler.obtainMessage();
        message.what = MESSAGE_REQUEST_SUCCEED;
        message.obj = new BasicHttpResponse(response.statusCode, responseHeaders, superResponse.result, null);
        handlerUserProfile(superResponse.result);
        mHandler.sendMessage(message);
        return superResponse;
    }

    /**
     * 从底层截获用户信息，为用户的登录状态做判断
     *
     * @param userProfile
     */
    protected void handlerUserProfile(JSONObject userProfile) {
        try {
            String auth = userProfile.getJSONObject("Variables").optString("auth");
            if (TextUtils.isEmpty(auth) || "null".equals(auth)) {//auth无效时
                AccountService accountService = YMBApplication.instance().accountService();
                if (accountService.profile() == null) {
                    return;
                }
                accountService.logout();
                accountService.update(null);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
