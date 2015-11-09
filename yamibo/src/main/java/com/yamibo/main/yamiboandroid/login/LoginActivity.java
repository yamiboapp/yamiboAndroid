package com.yamibo.main.yamiboandroid.login;

import android.os.Bundle;
import android.os.PersistableBundle;

import com.yamibo.main.yamiboandroid.R;
import com.yamibo.main.yamibolib.app.YMBActivity;

/**
 * Created by wangxiaoyan on 15/11/9.
 */
public class LoginActivity extends YMBActivity {

    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        initView();
    }

    private void initView() {
        setContentView(R.layout.activity_login);
    }
}
