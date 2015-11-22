package com.yamibo.main.yamibolib.Utils;

/**
 * Created by wangxiaoyan on 15/11/11.
 */
public class BasicNameValuePair implements NameValuePair {
    private String mName = "";
    private String mValue = "";

    public BasicNameValuePair(String name, String value) {
        mName = name;
        mValue = value;
    }

    @Override
    public String getName() {
        return mName;
    }

    @Override
    public String getValue() {
        return mValue;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
