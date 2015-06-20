package com.yamibo.main.yamibolib.dataservice.http;

import com.yamibo.main.yamibolib.dataservice.Response;

import java.util.Map;

public interface HttpResponse extends Response {

    /**
     * HTTP请求返回状态值，Status Code
     *
     * @return
     */
    int statusCode();

    /**
     * HTTP请求返回头信息
     *
     * @return
     */
    Map<String, String> headers();

}
