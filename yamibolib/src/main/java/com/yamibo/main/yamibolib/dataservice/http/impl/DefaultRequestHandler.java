package com.yamibo.main.yamibolib.dataservice.http.impl;

import android.text.TextUtils;

import com.yamibo.main.yamibolib.app.YMBActivity;
import com.yamibo.main.yamibolib.dataservice.Request;
import com.yamibo.main.yamibolib.dataservice.RequestHandler;
import com.yamibo.main.yamibolib.dataservice.Response;
import com.yamibo.main.yamibolib.dataservice.http.HttpRequest;
import com.yamibo.main.yamibolib.dataservice.http.HttpResponse;

import org.json.JSONException;
import org.json.JSONObject;

public class DefaultRequestHandler implements RequestHandler<HttpRequest, HttpResponse> {
    YMBActivity mYMBActivity;
    OnRequestListener mOnRequestListener;

    public DefaultRequestHandler(YMBActivity activity, OnRequestListener onRequestListener) {
        this.mYMBActivity = activity;
        this.mOnRequestListener = onRequestListener;
    }

    @Override
    public void onRequestFinish(HttpRequest req, HttpResponse resp) {
        if (resp.result() instanceof JSONObject) {
            JSONObject userProfile = (JSONObject) resp.result();
            try {
                String auth = userProfile.getJSONObject("Variables").optString("auth");
                if (TextUtils.isEmpty(auth) || "null".equals(auth)) {//auth无效时
                    mYMBActivity.accountService().logout();
                    mYMBActivity.accountService().update(null);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        //处理其他网络请求的数据
        if (mOnRequestListener != null) {
            mOnRequestListener.onTheRequestFinish(req, resp);
        }
    }

    @Override
    public void onRequestFailed(HttpRequest req, HttpResponse resp) {
        if (mOnRequestListener != null) {
            mOnRequestListener.onTheRequestFailed(req, resp);
        }
    }

    /**
     * 对数据获取的监听
     */
    public interface OnRequestListener {
        void onTheRequestFinish(Request req, Response resp);

        void onTheRequestFailed(Request req, Response resp);
    }

    public OnRequestListener getOnRequestListener() {
        return mOnRequestListener;
    }

    public void setOnRequestListener(OnRequestListener mOnRequestListener) {
        this.mOnRequestListener = mOnRequestListener;
    }
}
