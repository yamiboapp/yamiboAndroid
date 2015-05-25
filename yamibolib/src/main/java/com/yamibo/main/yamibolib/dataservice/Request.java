package com.yamibo.main.yamibolib.dataservice;

/**
 * 一个基于url的数据请求<br>
 * 一般为不可变(Immutable)对象
 *
 * @author Yimin
 */
public interface Request {

    /**
     * 得到当前Request的url
     *
     * @return
     */
    String url();

}
