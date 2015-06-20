package com.yamibo.main.yamibolib.dataservice;

/**
 * 获取数据服务，一般同时提供同步和异步两种方式
 *
 * @param <T> Request的具体类型
 * @param <R> Response的具体类型
 * @author Yimin
 */
public interface DataService<T extends Request, R extends Response> {

    /**
     * 异步获取Request请求<br>
     * 可以通过abort()来取消请求，取消后handler不会被触发fail方法。
     *
     * @param req     要执行的Request请求，不可变
     * @param handler 支持传入FullRequestHandle 监听onRequestStart,onRequestProgress
     *                回调处理，在主线程中执行
     */
    void exec(T req, RequestHandler<T, R> handler);

    /**
     * 取消通过exec()方法执行的异步请求<br>
     * 不保证请求一定被取消<br>
     *
     * @param req                   被取消的Request请求
     * @param handler               制定被取消的Handler。如果为null，则取消所有req请求的handler
     * @param mayInterruptIfRunning 是否取消已经在执行中的请求。通常为true。
     */
    void abort(T req, RequestHandler<T, R> handler,
               boolean mayInterruptIfRunning);

}
