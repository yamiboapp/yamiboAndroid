package com.android.volley.toolbox;

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

    public static List<String> cookieArray(String cookieString) {
        List<String> cookieList = new ArrayList<>();
        if (cookieString == null) return cookieList;
        String[] cookies = cookieString.split(COOKIE_DIVIDER);
        if (cookies != null && cookies.length > 0) {
            cookieList = Arrays.asList(cookies);
        }

        return cookieList;
    }

    public static String cookieString(List<String> cookieList) {
        StringBuilder sb = new StringBuilder("");
        if (cookieList == null) return sb.toString();

        for (int i = 0; i < cookieList.size(); i++) {
            if (i == cookieList.size() - 1) {//last one
                sb.append(cookieList.get(i));
            } else {
                sb.append(cookieList.get(i)).append(COOKIE_DIVIDER);
            }
        }
        return sb.toString();
    }

}