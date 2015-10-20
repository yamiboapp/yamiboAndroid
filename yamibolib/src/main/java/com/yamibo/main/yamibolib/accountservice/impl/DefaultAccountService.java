package com.yamibo.main.yamibolib.accountservice.impl;

import android.content.Context;

import com.yamibo.main.yamibolib.accountservice.AccountListener;
import com.yamibo.main.yamibolib.accountservice.AccountService;
import com.yamibo.main.yamibolib.accountservice.LoginResultListener;
import com.yamibo.main.yamibolib.model.UserProfile;

import org.apache.http.NameValuePair;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangxiaoyan on 15/5/25.
 */
public class DefaultAccountService implements AccountService {
    private Context mContext;
    UserProfile mUserProfile;
    List<AccountListener> mAccountListeners;

    public DefaultAccountService(Context context) {
        mContext = context;
        mAccountListeners = new ArrayList<AccountListener>();
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
    public String token() {
        //尚不确定应该返回那部分的参数
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
        //没有注册接口
    }

    @Override
    public void logout() {
        for (AccountListener listener : mAccountListeners) {
            mUserProfile = null;
            listener.onAccountChanged(this);
        }
    }

    @Override
    public void update(UserProfile profile) {
        if (mUserProfile == null) {
            mUserProfile = profile;
            for (AccountListener listener : mAccountListeners) {//触发所有的AccountListener.onAccountChanged事件
                listener.onAccountChanged(this);
            }
        } else {
            if (profile.getMember_uid() == mUserProfile.getMember_uid()) {//只有两次uid一致的时候，才进行更新，否则不做处理
                if (updateProfile(mUserProfile, profile)) {//信息有更新
                    for (AccountListener listener : mAccountListeners) {//触发所有的AccountListener.onAccountChanged事件
                        listener.onProfileChanged(this);
                    }
                }
            }
        }
    }

    /**
     * 更新新旧两个用户信息的方法
     *
     * @param former 原来的用户信息
     * @param latter 新得到的用户信息
     * @return
     */
    public boolean updateProfile(UserProfile former, UserProfile latter) {
        boolean flag = false;
        //用户组id
        if (former.getGroupid() != latter.getGroupid()) {
            former.setGroupid(latter.getGroupid());
            mUserProfile = latter;
            return true;
        }


        return flag;
    }


    @Override
    public void addListener(AccountListener listener) {
        mAccountListeners.add(listener);
    }

    @Override
    public void removeListener(AccountListener listener) {
        mAccountListeners.remove(listener);
    }

    @Override
    public void removeLoginResultListener() {

    }
}
