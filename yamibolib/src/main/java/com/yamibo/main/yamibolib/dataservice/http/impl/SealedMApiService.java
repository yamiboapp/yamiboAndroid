package com.yamibo.main.yamibolib.dataservice.http.impl;

import com.yamibo.main.yamibolib.Utils.Log;
import com.yamibo.main.yamibolib.dataservice.RequestHandler;
import com.yamibo.main.yamibolib.dataservice.http.HttpRequest;
import com.yamibo.main.yamibolib.dataservice.http.HttpResponse;
import com.yamibo.main.yamibolib.dataservice.http.HttpService;

import java.util.concurrent.ConcurrentHashMap;

public class SealedMApiService implements HttpService, RequestHandler<HttpRequest, HttpResponse> {
    private HttpService service;
    private ConcurrentHashMap<HttpRequest, RequestHandler<HttpRequest, HttpResponse>> running;

    public SealedMApiService(HttpService service) {
        this.service = service;
        running = new ConcurrentHashMap<>();
    }

    public void onDestroy() {
        for (HttpRequest req : running.keySet()) {
            service.abort(req, this, true);
            Log.i("SealedMApiService", "Abort leak request " + req);
        }
    }

    @Override
    public void exec(HttpRequest req, RequestHandler<HttpRequest, HttpResponse> handler) {
		if (handler != null) {
			running.put(req, handler);
			service.exec(req, this);
		} else {
			service.exec(req, handler);
		}
	}

    @Override
    public HttpResponse execSync(HttpRequest req) {
        return service.execSync(req);
    }

    @Override
    public void abort(HttpRequest req, RequestHandler<HttpRequest, HttpResponse> handler,
                      boolean mayInterruptIfRunning) {
        if (running.remove(req, handler)) {
            service.abort(req, this, mayInterruptIfRunning);
        } else {
            service.abort(req, handler, mayInterruptIfRunning);
        }
    }

//    @Override
//    public void onRequestStart(HttpRequest req) {
//        RequestHandler<HttpRequest, HttpResponse> handler = running.get(req);
//        if (handler instanceof RequestHandler) {
//            ((RequestHandler<HttpRequest, HttpResponse>) handler).onRequestStart(req);
//        }
//    }
//
//    @Override
//    public void onRequestProgress(HttpRequest req, int count, int total) {
//        RequestHandler<HttpRequest, HttpResponse> handler = running.get(req);
//        if (handler instanceof RequestHandler) {
//            ((RequestHandler<HttpRequest, HttpResponse>) handler).onRequestProgress(req, count,
//                    total);
//        }
//    }

    @Override
    public void onRequestFinish(HttpRequest req, HttpResponse resp) {
        RequestHandler<HttpRequest, HttpResponse> handler = running.remove(req);
        if (handler != null) {
            handler.onRequestFinish(req, resp);
        } else {
            Log.w("mapi_seal", "Sealed leak on " + req);
        }
    }

    @Override
    public void onRequestFailed(HttpRequest req, HttpResponse resp) {
        RequestHandler<HttpRequest, HttpResponse> handler = running.remove(req);
        if (handler != null) {
            handler.onRequestFailed(req, resp);
        } else {
            Log.w("mapi_seal", "Sealed leak on " + req);
        }
    }
}
