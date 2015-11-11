package com.yamibo.main.yamiboandroid.login;

import android.os.Bundle;

import com.yamibo.main.yamiboandroid.R;
import com.yamibo.main.yamibolib.app.YMBActivity;

/**
 * Created by wangxiaoyan on 15/11/9.
 */
public class LoginActivity extends YMBActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
    }

    private void initView() {
        setContentView(R.layout.activity_login);
        setTitle(getString(R.string.login));
    }
}
