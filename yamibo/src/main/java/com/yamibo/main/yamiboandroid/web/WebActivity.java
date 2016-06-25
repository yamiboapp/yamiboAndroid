package com.yamibo.main.yamiboandroid.web;

import android.os.Bundle;
import android.text.TextUtils;
import android.webkit.WebView;

import com.yamibo.main.yamiboandroid.R;
import com.yamibo.main.yamibolib.app.YMBActivity;

/**
 * Created by wangxiaoyan on 15/12/5.
 */
public class WebActivity extends YMBActivity {
    private WebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);
        mWebView = (WebView) findViewById(R.id.webview);
        String url = getStringParam("url");
        if (!TextUtils.isEmpty(url)) {
            mWebView.loadUrl(url);
        }
    }
}
