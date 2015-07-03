package com.yamibo.main.yamibolib.accountservice;


import android.content.Context;

import com.yamibo.main.yamibolib.accountservice.AccountListener;
import com.yamibo.main.yamibolib.accountservice.LoginResultListener;
import com.yamibo.main.yamibolib.accountservice.impl.DefaultAccountService;
import com.yamibo.main.yamibolib.model.UserProfile;

import org.apache.http.NameValuePair;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Remiany on 2015/6/7 0007.
 */
public class YamiboAccountService extends DefaultAccountService {
    UserProfile mUserProfile;
    List<AccountListener> mAccountListeners;

    public YamiboAccountService(Context context) {
        super(context);
        mAccountListeners = new ArrayList<AccountListener>();
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
    public UserProfile profile() {
        return mUserProfile;
    }

    @Override
    public String token() {
        //尚不确定应该返回那部分的参数
        return super.token();
    }

    @Override
    public void login(LoginResultListener listener) {
        super.login(listener);
    }

    @Override
    public void login(LoginResultListener listener, List<NameValuePair> params) {
        super.login(listener, params);
    }

    @Override
    public void signup(LoginResultListener listener) {
        //没有接口
        super.signup(listener);
    }

    @Override
    public void logout() {
        super.logout();
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
            flag = true;
        }
        //用户名
        if (!former.getMember_username().equals(latter.getMember_username())) {
            former.setMember_username(latter.getMember_username());
            flag = true;
        }
        //用户头像
        if (!former.getMember_avatar().equals(latter.getMember_avatar())) {
            former.setMember_avatar(latter.getMember_avatar());
            flag = true;
        }
        //防灌水（？）
        if (!former.getFormhash().equals(latter.getFormhash())) {
            former.setFormhash(latter.getFormhash());
            flag = true;
        }
        //
        if (!former.getReadaccess().equals(latter.getReadaccess())) {
            former.setReadaccess(latter.getReadaccess());
            flag = true;
        }
        //消息更新
        if (former.getNewmypost() != latter.getNewmypost()) {
            former.setNewmypost(latter.getNewmypost());
            flag = true;
        }
        if (former.getNewpm() != latter.getNewpm()) {
            former.setNewpm(latter.getNewpm());
            flag = true;
        }
        if (former.getNewprompt() != latter.getNewprompt()) {
            former.setNewprompt(latter.getNewprompt());
            flag = true;
        }
        if (former.getNewpush() != latter.getNewpush()) {
            former.setNewpush(latter.getNewpush());
            flag = true;
        }
        return flag;
    }

    @Override
    public void addListener(AccountListener listener) {
        super.addListener(listener);
        mAccountListeners.add(listener);
    }

    @Override
    public void removeListener(AccountListener listener) {
        super.removeListener(listener);
        mAccountListeners.remove(listener);
    }

    @Override
    public void removeLoginResultListener() {
        super.removeLoginResultListener();
    }
}
