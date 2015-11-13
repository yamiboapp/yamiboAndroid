package com.yamibo.main.yamibolib.dataservice.http;


import com.yamibo.main.yamibolib.dataservice.Request;

import java.io.InputStream;
import java.util.Map;

public interface HttpRequest extends Request {

    /**
     * HTTP方法，常用的有GET和POST<br>
     * 必须为大写
     *
     * @return
     */
    int method();

    /**
     * HTTP输入流<br>
     * 如果为GET方法或不需要Body，可为null<br>
     * <br>
     * 一般使用FormInputStream来封装URL Form形式的Body（key1=value1&key2=value2...)<br>
     * 或使用StringInputStream来封装纯字符串Body<br>
     * <br>
     * 注意如果InputStream不支持mark()，那么这个HttpRequest只能被执行一次。因为流只能够被读取一次，所以当请求被第二次执行时，
     * 会遇到Body为空的情况。
     *
     * @return
     */
    InputStream input();

    /**
     * HTTP头<br>
     * 如果为空，则没有额外的头信息
     *
     * @return
     */
    Map<String, String> headers();

    /**
     * 超时时间，单位毫秒
     * <p/>
     * 如果为0表示不指定超时，由系统自己处理
     */
    long timeout();

    /**
     * 允许增加Headers
     */
    void addHeaders(Map<String, String> headers);

    /**
     * 是否需要缓存
     *
     * @return
     */
    boolean isShouldCache();
}
