package com.android.volley.toolbox;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by wangxiaoyan on 15/11/12.
 */
public class CookieHelper {
    private final static String COOKIE_DIVIDER = ";";
    public static final String SET_COOKIE_KEY = "Set-Cookie";
    public static final String COOKIE_KEY = "Cookie";
    public static final String SESSION_COOKIE = "sessionid";

    private final static String PERFER_KEY_COOKIE_STRING = "com.cookie_helper.cookie_string";
    private final static String PERFER_COOKIE_PERFER = "com.volley.cookie_helper.share_perference";

    private static SharedPreferences mPerference;

    /**
     * 从存储中获取cookie参数
     *
     * @param url
     * @return
     */
    public static String getCookies(Context context, URL url) {
        if (context == null || url == null) return "";
        return perference(context).getString(perferCookieKey(url), "");
    }

    /**
     * 保存从服务器获取的cookie
     *
     * @param cookieList
     * @return
     */
    public static String setCookies(Context context, URL url, List<String> cookieList) {
        StringBuilder sb = new StringBuilder("");
        if (cookieList == null || context == null || url == null) return sb.toString();

        String localCookies = getCookies(context, url); //不可能为null
        String cookie;
        Map<String, String> localCookiesMap = new HashMap<>();
        String[] localCookiesArray = localCookies.split(COOKIE_DIVIDER);
        for (String localCookie : localCookiesArray) {
            if (!TextUtils.isEmpty(localCookie)) {
                localCookiesMap.put(getCookieName(localCookie), getCookieValue(localCookie));
            }
        }
        for (int i = 0; i < cookieList.size(); i++) {
            cookie = cookieList.get(i);
            if (cookie.contains(COOKIE_DIVIDER)) {
                cookie = cookieList.get(i).substring(0, cookieList.get(i).indexOf(COOKIE_DIVIDER));
            }
            if (!TextUtils.isEmpty(cookie)) {
                localCookiesMap.put(getCookieName(cookie), getCookieValue(cookie));
            }
        }
        if (localCookiesMap.size() > 0) {
            for (Map.Entry<String, String> entry : localCookiesMap.entrySet()) {
                sb.append(entry.getKey()).append("=").append(entry.getValue()).append(COOKIE_DIVIDER);
            }
            sb.deleteCharAt(sb.length() - 1);
        }

        String cookieString = sb.toString();
        perference(context).edit().putString(perferCookieKey(url), cookieString).commit();

        return cookieString;
    }

    private static String getCookieName(String cookie) {
        return cookie.substring(0, cookie.indexOf('='));
    }

    private static String getCookieValue(String cookie) {
        return cookie.substring(cookie.indexOf('=') + 1, cookie.length());
    }

    public static boolean hasCookies(Context context, URL url) {
        if (context == null || url == null) return false;
        return !TextUtils.isEmpty(perference(context).getString(perferCookieKey(url), ""));
    }


    private static String perferCookieKey(URL url) {
        return PERFER_KEY_COOKIE_STRING + url.getHost();
    }

    private static SharedPreferences perference(Context context) {
        if (mPerference == null) {//context can't be null
            mPerference = context.getSharedPreferences(PERFER_COOKIE_PERFER, Context.MODE_PRIVATE);
        }
        return mPerference;
    }

    public static void removeCookies(Context context, URL url) {
        perference(context).edit().remove(perferCookieKey(url)).commit();
    }
}