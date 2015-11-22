package com.yamibo.main.yamibolib.dataservice.http.impl;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.Volley;
import com.yamibo.main.yamibolib.dataservice.RequestHandler;
import com.yamibo.main.yamibolib.dataservice.http.HttpRequest;
import com.yamibo.main.yamibolib.dataservice.http.HttpResponse;
import com.yamibo.main.yamibolib.dataservice.http.HttpService;

import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

/**
 * Created by wangxiaoyan on 15/5/25.
 */
public class DefaultHttpService implements HttpService {
    private Context mContext;
    private RequestQueue mQueue;

    public DefaultHttpService(Context context) {
        mContext = context;
        mQueue = Volley.newRequestQueue(mContext);
    }

    @Override
    public void exec(final HttpRequest req, final RequestHandler<HttpRequest, HttpResponse> handler) {
        if (req == null) return;
        VolleyRequest request = new VolleyRequest(req, handler);
        request.setTag(req.url());
        request.setShouldCache(req.isShouldCache());
        mQueue.add(request);
    }

    @Override
    public HttpResponse execSync(final HttpRequest req) {
        RequestFuture<JSONObject> future = RequestFuture.newFuture();
        VolleyRequest request = new VolleyRequest(req);
        request.setShouldCache(req.isShouldCache());
        future.setRequest(mQueue.add(request));
        try {
            final JSONObject result = future.get();
            return new BasicHttpResponse(result != null ? 200 : 400, null, result, null);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        return new BasicHttpResponse(400, null, null, null);
    }

    @Override
    public void abort(HttpRequest req, RequestHandler<HttpRequest, HttpResponse> handler,
                      boolean mayInterruptIfRunning) {
        mQueue.cancelAll(req.url());
    }
}
