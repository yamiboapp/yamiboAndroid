package com.yamibo.main.yamibolib.dataservice;


/**
 * DataService异步调用方式的回传代理
 * <p/>
 * 如需onRequestStart,onRequestProgress 则让监听器实现FullRequestHandle接口按原样传入就好
 * <p/>
 * modify by gloria.wang 2015.2.6
 */
public interface RequestHandler<T extends Request, R extends Response> {

    /**
     * 请求成功并返回<br>
     * response.error = null<br>
     *
     * @param req  被执行的Request请求
     * @param resp
     */
    void onRequestFinish(T req, R resp);

    /**
     * 请求成功并返回<br>
     * response.error为异常原因<br>
     *
     * @param req  被执行的Request请求
     * @param resp
     */
    void onRequestFailed(T req, R resp);

}
