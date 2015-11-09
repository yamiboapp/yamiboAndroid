package com.yamibo.main.yamibolib.accountservice.impl;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.text.TextUtils;

import com.yamibo.main.yamibolib.accountservice.AccountListener;
import com.yamibo.main.yamibolib.accountservice.AccountService;
import com.yamibo.main.yamibolib.accountservice.LoginResultListener;
import com.yamibo.main.yamibolib.app.YMBApplication;
import com.yamibo.main.yamibolib.model.UserProfile;

import org.apache.http.NameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangxiaoyan on 15/5/25.
 */
public class DefaultAccountService implements AccountService {
    private Context mContext;
    private UserProfile mUserProfile;
    private List<AccountListener> mAccountListeners;
    private LoginResultListener mLoginResultListener;
    private SharedPreferences preferences;

    private final static String PRE_USER_DATA = "com.yamibo.user_data";


    public DefaultAccountService(Context context) {
        mContext = context;
        mAccountListeners = new ArrayList<>();
        preferences = YMBApplication.preferences(mContext);
        String userData = preferences.getString(PRE_USER_DATA, null);

        if (!TextUtils.isEmpty(userData)) {
            try {
                mUserProfile = new UserProfile(new JSONObject(userData));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public UserProfile profile() {
        return mUserProfile;
    }

    @Override
    public int id() {
        if (mUserProfile != null) {
            String uid = mUserProfile.getMember_uid();
            return Integer.valueOf(uid);
        }
        return 0;
    }

    @Override
    public boolean isLogin() {
        return mUserProfile != null;
    }

    @Override
    public void login(LoginResultListener listener) {
        login(listener, null);
    }

    @Override
    public void login(LoginResultListener listener, List<NameValuePair> params) {
        mLoginResultListener = listener;
        mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("ymb://login")));
    }

    @Override
    public void signup(LoginResultListener listener) {
        //没有注册接口
    }

    @Override
    public void logout() {
        preferences.edit().remove(PRE_USER_DATA).commit();
        for (AccountListener listener : mAccountListeners) {
            mUserProfile = null;
            listener.onAccountChanged(this);
        }
    }

    @Override
    public void update(UserProfile profile) {
        mUserProfile = profile;
        if (profile != null) {
            preferences.edit().putString(PRE_USER_DATA, mUserProfile.toString()).commit();
            if (mLoginResultListener != null) {
                mLoginResultListener.onLoginSuccess(this);
            }
        } else {
            preferences.edit().remove(PRE_USER_DATA).commit();
        }
        for (AccountListener listener : mAccountListeners) {//触发所有的AccountListener.onAccountChanged事件
            listener.onProfileChanged(this);
        }
    }

    @Override
    public void addListener(AccountListener listener) {
        if (listener != null) {
            mAccountListeners.add(listener);
        }
    }

    @Override
    public void removeListener(AccountListener listener) {
        if (listener != null && mAccountListeners.contains(listener)) {
            mAccountListeners.remove(listener);
        }
    }

    @Override
    public void removeLoginResultListener() {
        mLoginResultListener = null;
    }
}
