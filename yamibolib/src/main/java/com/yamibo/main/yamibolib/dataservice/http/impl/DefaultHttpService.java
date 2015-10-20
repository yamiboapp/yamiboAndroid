package com.yamibo.main.yamibolib.dataservice.http.impl;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.yamibo.main.yamibolib.dataservice.RequestHandler;
import com.yamibo.main.yamibolib.dataservice.http.HttpRequest;
import com.yamibo.main.yamibolib.dataservice.http.HttpResponse;
import com.yamibo.main.yamibolib.dataservice.http.HttpService;

import org.json.JSONObject;

import java.io.IOException;
import java.util.Map;

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
        JsonObjectRequest request = new JsonObjectRequest(volleyMethod(req), req.url(), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(final JSONObject response) {
                if (handler != null) {
                    handler.onRequestFinish(req, new HttpResponse() {
                        @Override
                        public int statusCode() {
                            return 200;
                        }

                        @Override
                        public Map<String, String> headers() {
                            return null;
                        }

                        @Override
                        public Object result() {
                            return response;
                        }

                        @Override
                        public Object error() {
                            return null;
                        }
                    });
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(final VolleyError error) {
                if (handler != null) {
                    handler.onRequestFailed(req, new HttpResponse() {
                        @Override
                        public int statusCode() {
                            return 400;
                        }

                        @Override
                        public Map<String, String> headers() {
                            return null;
                        }

                        @Override
                        public Object result() {
                            return null;
                        }

                        @Override
                        public Object error() {
                            return error;
                        }
                    });
                }
            }
        }) {
            @Override
            public byte[] getBody() {
                byte[] bytes = null;
                try {
                    bytes = new byte[req.input().available()];
                    req.input().read(bytes);
                    req.input().close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return bytes;
//                return req.input().toString().getBytes();
            }

            public String getBodyContentType() {
                return "application/x-www-form-urlencoded; charset=" + getParamsEncoding();
            }

        };
        request.setTag(req.url());
        request.setShouldCache(req.isShouldCache());
        mQueue.add(request);
    }

    @Override
    public void abort(HttpRequest req, RequestHandler<HttpRequest, HttpResponse> handler, boolean mayInterruptIfRunning) {
        mQueue.cancelAll(req.url());
    }

    private int volleyMethod(HttpRequest request) {
        switch (request.method()) {
            case BasicHttpRequest.GET:
                return Request.Method.GET;
            case BasicHttpRequest.POST:
                return Request.Method.POST;
            case BasicHttpRequest.PUT:
                return Request.Method.PUT;
            case BasicHttpRequest.DELETE:
                return Request.Method.DELETE;
        }
        return Request.Method.GET;
    }
}
