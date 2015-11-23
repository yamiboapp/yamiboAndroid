package com.yamibo.main.yamiboandroid.settting;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.yamibo.main.yamiboandroid.R;
import com.yamibo.main.yamibolib.Utils.Environment;
import com.yamibo.main.yamibolib.accountservice.AccountListener;
import com.yamibo.main.yamibolib.accountservice.AccountService;
import com.yamibo.main.yamibolib.app.YMBActivity;

/**
 * Created by wangxiaoyan on 15/11/21.
 */
public class SettingActivity extends YMBActivity implements View.OnClickListener, AccountListener {

    Button mLogoutButton;
    Button mTestButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        mLogoutButton = (Button) findViewById(R.id.btn_logout);
        if (!accountService().isLogin()) {
            mLogoutButton.setVisibility(View.GONE);
        }
        mLogoutButton.setOnClickListener(this);

        // debug 才有，之后还会写其他的打开方式，或者写一个测试界面
        mTestButton = (Button) findViewById(R.id.btn_test);
        if (!Environment.isDebug()) {
            mTestButton.setVisibility(View.GONE);
        } else {
            mTestButton.setVisibility(View.VISIBLE);
        }
        mTestButton.setOnClickListener(this);
        accountService().addListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_logout) {
            accountService().logout();
        } else if (v.getId() == R.id.btn_test) {
            startActivity("ymb://debug");
        }
    }

    @Override
    protected void onDestroy() {
        accountService().removeListener(this);
        super.onDestroy();
    }

    @Override
    public void onAccountChanged(AccountService sender) {
        if (!sender.isLogin()) {
            mLogoutButton.setVisibility(View.GONE);
        } else {
            mLogoutButton.setVisibility(View.VISIBLE);
        }
    }
}
