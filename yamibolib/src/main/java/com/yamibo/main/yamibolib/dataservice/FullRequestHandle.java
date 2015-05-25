package com.yamibo.main.yamibolib.dataservice;

/**
 * DataService异步调用方式的回传代理
 * 
 * 完整的网络监听接口,可以跟RequestHandler一样传入监听器
 * 
 */
public interface FullRequestHandle<T extends com.yamibo.main.yamibolib.dataservice.http.Request, R extends Response> extends
        RequestHandler<T, R> {

    /**
     * Request请求开始执行<br>
     * 注意不包括队列的排队时间，即Request请求真正被处理的时刻发出
     * 
     * @param req
     *            被执行的Request请求
     */
    void onRequestStart(T req);

    /**
     * Request请求的执行进度<br>
     * 是否支持进度回传由DataService的实现决定<br>
     * count和total的具体含义由DataService的实现决定
     * 
     * @param req
     *            被执行的Request请求
     * @param count
     * @param total
     */
    void onRequestProgress(T req, int count, int total);
}
