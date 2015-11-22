package com.yamibo.main.yamibolib.dataservice.http.impl;

import android.content.Context;
import android.os.AsyncTask;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.Volley;
import com.yamibo.main.yamibolib.dataservice.RequestHandler;
import com.yamibo.main.yamibolib.dataservice.http.HttpRequest;
import com.yamibo.main.yamibolib.dataservice.http.HttpResponse;
import com.yamibo.main.yamibolib.dataservice.http.HttpService;

import org.json.JSONObject;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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

//    @Override
//    public HttpResponse execSync(final HttpRequest req) {
//
//        ThreadB threadB = new ThreadB(req);
//        try {
//            final JSONObject result = threadB.execute().get(40, TimeUnit.SECONDS);
//            return new BasicHttpResponse(result != null ? 200 : 400, null, result, null);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        } catch (ExecutionException e) {
//            e.printStackTrace();
//        } catch (TimeoutException e) {
//            e.printStackTrace();
//        }
//        return new BasicHttpResponse(400, null, null, null);
//    }

    private class ThreadB extends AsyncTask<Void, Void, JSONObject> {
        private HttpRequest req;

        public ThreadB(HttpRequest req) {
            this.req = req;
        }

        @Override
        protected JSONObject doInBackground(Void... params) {

            RequestFuture<JSONObject> future = RequestFuture.newFuture();
            VolleyRequest request = new VolleyRequest(req);
            request.setShouldCache(req.isShouldCache());
            future.setRequest(mQueue.add(request));

            try {
                return future.get(30, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (TimeoutException e) {
                e.printStackTrace();
            }
            return null;
        }
    }


    @Override
    public void abort(HttpRequest req, RequestHandler<HttpRequest, HttpResponse> handler,
                      boolean mayInterruptIfRunning) {
        mQueue.cancelAll(req.url());
    }
}
