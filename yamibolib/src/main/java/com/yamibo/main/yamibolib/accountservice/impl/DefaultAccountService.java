package com.yamibo.main.yamibolib.accountservice.impl;

import android.content.Context;

import com.yamibo.main.yamibolib.accountservice.AccountListener;
import com.yamibo.main.yamibolib.accountservice.AccountService;
import com.yamibo.main.yamibolib.accountservice.LoginResultListener;

import org.apache.http.NameValuePair;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by wangxiaoyan on 15/5/25.
 */
public class DefaultAccountService implements AccountService {
    private Context mContext;

    public DefaultAccountService(Context context) {
        mContext = context;
    }

    @Override
    public JSONObject profile() {
        return null;
    }

    @Override
    public int id() {
        return 0;
    }

    @Override
    public String token() {
        return null;
    }

    @Override
    public void login(LoginResultListener listener) {

    }

    @Override
    public void login(LoginResultListener listener, List<NameValuePair> params) {

    }

    @Override
    public void signup(LoginResultListener listener) {

    }

    @Override
    public void logout() {

    }

    @Override
    public void update(JSONObject profile) {

    }

    @Override
    public void addListener(AccountListener listener) {

    }

    @Override
    public void removeListener(AccountListener listener) {

    }

    @Override
    public void removeLoginResultListener() {

    }
}
