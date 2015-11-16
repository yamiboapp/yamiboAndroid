package com.android.volley.toolbox;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by wangxiaoyan on 15/11/12.
 */
public class CookieHelper {
    private final static String COOKIE_DIVIDER = "__YMB_COOKIE__";
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
    public static List<String> cookieArray(Context context, URL url) {
        List<String> cookieList = new ArrayList<>();
        if (context == null || url == null) return cookieList;
        String[] cookies = perference(context).getString(perferCookieKey(url), "").split(COOKIE_DIVIDER);
        if (cookies != null && cookies.length > 0) {
            cookieList = Arrays.asList(cookies);
        }

        return cookieList;
    }

    /**
     * 保存从服务器获取的cookie
     *
     * @param cookieList
     * @return
     */
    public static String cookieString(Context context, URL url, List<String> cookieList) {
        StringBuilder sb = new StringBuilder("");
        if (cookieList == null || context == null || url == null) return sb.toString();

        for (int i = 0; i < cookieList.size(); i++) {
            if (i == cookieList.size() - 1) {//last one
                sb.append(cookieList.get(i));
            } else {
                sb.append(cookieList.get(i)).append(COOKIE_DIVIDER);
            }
        }

        String cookieString = sb.toString();
        perference(context).edit().putString(perferCookieKey(url), cookieString).commit();

        return cookieString;
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

}