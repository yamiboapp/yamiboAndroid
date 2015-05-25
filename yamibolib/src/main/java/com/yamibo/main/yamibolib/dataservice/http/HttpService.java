package com.yamibo.main.yamibolib.dataservice.http;


import com.yamibo.main.yamibolib.dataservice.DataService;

/**
 * 基于HTTP协议的网络服务<br>
 * <br>
 * 当statusCode = 2xx | 4xx | 5xx时，均表示成功<br>
 * 失败的情况一般有无网络或网络超时<br>
 * <br>
 * 所有异步调用的发起必须在主线程中执行，回调方法也在主线程中执行<br>
 * 同步方法不允许在主线程中调用
 *
 * @author Yimin
 */
public interface HttpService extends DataService<HttpRequest, HttpResponse> {

}
