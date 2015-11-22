package com.yamibo.main.yamibolib.accountservice;

/**
 * AccountListener与LoginResultListener区别在于，
 * AccountListener类似广播事件。
 * LoginResultListener是回调事件。
 *
 * @author Yutao
 */
public interface AccountListener {

    /**
     * 用户登录或者退出登录或者更新profile都会被call到
     * sender.profile() == null 表示用户退出登录
     * sender.profile() != null 表示用户登录。
     */
    void onAccountChanged(AccountService sender);

}
