package com.yamibo.main.yamibolib.dataservice;

/**
 * Request请求的返回值，包括结果内容和错误原因。
 *
 * @author Yimin
 */
public interface Response {

    public static final Object SUCCESS = null;

    /**
     * 返回结果内容，通常为byte[]<br>
     * 如果发生错误，结果为null
     *
     * @return
     */
    Object result();

    /**
     * 错误原因<br>
     * 一般为发生错误时抛出的Exception<br>
     * 如果为null则表示没有错误
     *
     * @return
     */
    Object error();
}
