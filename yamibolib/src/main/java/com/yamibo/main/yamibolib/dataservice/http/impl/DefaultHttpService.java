package com.yamibo.main.yamibolib.dataservice.http.impl;

import android.content.Context;

import com.yamibo.main.yamibolib.dataservice.RequestHandler;
import com.yamibo.main.yamibolib.dataservice.http.HttpRequest;
import com.yamibo.main.yamibolib.dataservice.http.HttpResponse;
import com.yamibo.main.yamibolib.dataservice.http.HttpService;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by wangxiaoyan on 15/5/25.
 */
public class DefaultHttpService implements HttpService {
    private Context mContext;
    private Executor executor = new ThreadPoolExecutor(2, 6, 60, TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>());

    public DefaultHttpService(Context context) {
        mContext = context;
    }

    @Override
    public void exec(HttpRequest req, RequestHandler<HttpRequest, HttpResponse> handler) {

    }

    @Override
    public HttpResponse execSync(HttpRequest req) {
        return null;
    }

    @Override
    public void abort(HttpRequest req, RequestHandler<HttpRequest, HttpResponse> handler, boolean mayInterruptIfRunning) {

    }
}
