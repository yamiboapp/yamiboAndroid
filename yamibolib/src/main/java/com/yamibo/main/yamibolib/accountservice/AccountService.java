package com.yamibo.main.yamibolib.accountservice;

import org.apache.http.NameValuePair;
import org.json.JSONObject;

import java.util.List;

/**
 * 账户服务: 负责登录，第三方账号登录，注册
 * <p/>
 * 1. 需要关心Account的页面，要先profile()判断是否有登录 没有登录的情况下，call login(LoginResultListener
 * listener)
 * <p/>
 * 2. 需要关心Account的页面, 要call addListener(AccountListener listener)
 * <p/>
 * 3. 登录成功后，会先call LoginResultListener.onLoginSuccess, 然后再call
 * AccountListener.onAccountChanged。 对于同时关心LoginResultListener
 * .onLoginSuccess和AccountListener.onAccountChanged事件的页面要注意。
 *
 * @author Yutao
 */
public interface AccountService {

    /**
     * 获取完整的用户账户信息
     * <p/>
     * 没有登录时返回null <br>
     * 注意用户账户信息不包含Token
     */
    public JSONObject profile();

    /**
     * 没有登录时返回0
     */
    public int id();

    /**
     * 没有登录时返回null
     */
    public String token();

    /**
     * 不需要remove listener. AccountService发送完消息后会自动remove listener
     */
    public void login(LoginResultListener listener);

    /**
     * 不需要remove listener. AccountService发送完消息后会自动remove listener
     */
    public void login(LoginResultListener listener, List<NameValuePair> params);

    /**
     * 不需要remove listener. AccountService发送完消息后会自动remove listener
     */
    public void signup(LoginResultListener listener);

    /**
     * 注销登录
     */
    public void logout();

    /**
     * 未登陆时，进行登陆，并触发AccountListener.onAccountChanged事件
     * <p/>
     * <p/>
     * 已登录时，更新当前账号信息
     * <p/>
     * 只更新增量字段，如传入的profile只包含Avatar，则只更新Avatar字段，其他字段值不受影响。<br>
     * 如传入的profile中带有Token，则Token被忽略<br>
     * 如传入的profile中带有UserID，则必须与当前的UserID一致，否则忽略<br>
     * 任意字段更新都会触发AccountListener.onProfileChanged事件<br>
     */
    public void update(JSONObject profile);

    /**
     * 用Activity implement AccountListener,在Activity里面会帮你removeListener
     * </p>注意listener里面不要做耗时的动作
     */
    public void addListener(AccountListener listener);

    /**
     * 用Activity implement AccountListener,在Activity里面会帮你removeListener
     */
    public void removeListener(AccountListener listener);

    /**
     * 反注册登录登录监听器
     */
    public void removeLoginResultListener();
}
