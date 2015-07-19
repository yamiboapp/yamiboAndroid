package com.yamibo.main.yamibolib.dataservice.http.impl;

import com.yamibo.main.yamibolib.Utils.Log;

import org.apache.http.NameValuePair;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.UnsupportedCharsetException;
import java.util.List;


public class FormInputStream extends InputStream {
    public static final String ISO_8859_1 = "ISO-8859-1";
    public static final String UTF_8 = "UTF-8";
    public static final String DEFAULT_CHARSET = UTF_8;

    private List<NameValuePair> form;
    private String charsetName;

    private InputStream mInputStream;

    public FormInputStream(List<NameValuePair> form, String charsetName) {
        this.form = form;
        this.charsetName = charsetName;
        try {
            String str = encode();
            byte[] bytes = str.getBytes(charsetName);
            mInputStream = new ByteArrayInputStream(bytes);
        } catch (UnsupportedCharsetException e) {
            Log.e("e:" + e.toString());
        } catch (UnsupportedEncodingException e) {
            Log.e("e:" + e.toString());
        }
    }

    public FormInputStream(List<NameValuePair> form) {
        this(form, DEFAULT_CHARSET);
    }

    public List<NameValuePair> form() {
        return form;
    }

    public String charsetName() {
        return charsetName;
    }

    private String encode() throws UnsupportedEncodingException {
        StringBuilder sb = new StringBuilder();
        for (NameValuePair e : form) {
            if (sb.length() > 0)
                sb.append('&');
            sb.append(e.getName());
            sb.append('=');
            if (e.getValue() != null)
                sb.append(URLEncoder.encode(e.getValue(), charsetName));
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        try {
            return encode();
        } catch (Exception e) {
            return "";
        }
    }

    @Override
    public int read() throws IOException {
        return mInputStream.read();
    }

    @Override
    public int available() throws IOException {
        return mInputStream.available();
    }
}
