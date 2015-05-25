package com.yamibo.main.yamibolib.dataservice.http;

import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.telephony.TelephonyManager;

import com.yamibo.main.yamibolib.Utils.Log;

import org.apache.http.HttpHost;

public class NetworkInfoHelper {
    public static final int NETWORK_TYPE_UNKNOWN = 0;
    public static final int NETWORK_TYPE_WIFI = 1;
    public static final int NETWORK_TYPE_2G = 2;
    public static final int NETWORK_TYPE_3G = 3;
    public static final int NETWORK_TYPE_4G = 4;
    private Context context;
    private ConnectivityManager connectivityManager;
    private TelephonyManager teleManager;

    public NetworkInfoHelper(Context context) {
        this.context = context;
    }

    /**
     * 判断是否有网络连接
     *
     * @param context
     * @return
     */
    public static boolean isNetworkConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    protected ConnectivityManager connectivityManager() {
        if (connectivityManager == null) {
            try {
                connectivityManager = (ConnectivityManager) context
                        .getSystemService(Context.CONNECTIVITY_SERVICE);
            } catch (Exception e) {
                Log.w("network",
                        "cannot get connectivity manager, maybe the permission is missing in AndroidManifest.xml?" +
                                e);
            }
        }
        return connectivityManager;
    }

    protected TelephonyManager telephonyManager() {
        if (teleManager == null) {
            try {
                teleManager = (TelephonyManager) context
                        .getSystemService(Context.TELEPHONY_SERVICE);
            } catch (Exception e) {
                Log.w("network",
                        "cannot get telephony manager, maybe the permission is missing in AndroidManifest.xml?"
                                + e);
            }
        }
        return teleManager;
    }

    /**
     * 获取当前网络环境下应使用的代理
     * <p/>
     * 用以适配中国特色的WAP网关
     */
    public HttpHost getProxy() {
        ConnectivityManager connectivityManager = connectivityManager();
        if (connectivityManager == null)
            return null;
        try {
            NetworkInfo activeNetInfo = connectivityManager
                    .getActiveNetworkInfo();
            if (activeNetInfo == null)
                return null;
            if (activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                return null;
            }
            if (activeNetInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                String extraInfo = activeNetInfo.getExtraInfo();
                if (extraInfo == null)
                    return null;
                extraInfo = extraInfo.toLowerCase();
                if (extraInfo.contains("cmnet"))
                    return null;
                if (extraInfo.contains("cmwap"))
                    return new HttpHost("10.0.0.172");
                if (extraInfo.contains("3gnet"))
                    return null;
                if (extraInfo.contains("3gwap"))
                    return new HttpHost("10.0.0.172");
                if (extraInfo.contains("uninet"))
                    return null;
                if (extraInfo.contains("uniwap"))
                    return new HttpHost("10.0.0.172");
                if (extraInfo.contains("ctnet"))
                    return null;
                if (extraInfo.contains("ctwap"))
                    return new HttpHost("10.0.0.200");
                if (extraInfo.contains("#777")) {
                    Cursor c = context
                            .getContentResolver()
                            .query(Uri
                                            .parse("content://telephony/carriers/preferapn"),
                                    new String[]{"proxy", "port"}, null,
                                    null, null);
                    if (c.moveToFirst()) {
                        String host = c.getString(0);
                        if (host.length() > 3) {
                            int port = 0;
                            try {
                                port = Integer.parseInt(c.getString(1));
                            } catch (NumberFormatException e) {
                            }
                            return new HttpHost(host, port > 0 ? port : 80);
                        }
                    }
                    return null;
                }
            }
        } catch (Exception e) {
        }
        return null;
    }

    /**
     * 设备连接的网络类型，有以下几种返回：<br>
     * wifi: 从无线网络连接<br>
     * mobile***: 从手机网络连接。注意mobile后面会跟详细信息：<br>
     * &nbsp;&nbsp;mobile(手机数据网络类型,连接点名称)<br>
     * &nbsp;&nbsp;mobile(EDGE,cmnet)<br>
     * &nbsp;&nbsp;mobile(UTMS,3gnet)<br>
     * unknown: 未知网络类型<br>
     * 其他...<br>
     */
    public String getNetworkInfo() {
        ConnectivityManager connectivityManager = connectivityManager();
        if (connectivityManager == null)
            return "unknown";
        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetInfo == null)
            return "unknown";
        switch (activeNetInfo.getType()) {
            case ConnectivityManager.TYPE_WIFI:
                return "wifi";
            case ConnectivityManager.TYPE_MOBILE:
                return "mobile(" + activeNetInfo.getSubtypeName() + ","
                        + activeNetInfo.getExtraInfo() + ")";
            default:
                return activeNetInfo.getTypeName();
        }
    }

    /*
     * 返回详细的网络类型
     */
    public String getDetailNetworkType() {
        if (getNetworkType() == NETWORK_TYPE_UNKNOWN) {
            return "unknown";
        }
        if (getNetworkType() == NETWORK_TYPE_WIFI) {
            return "wifi";
        }
        TelephonyManager tm = telephonyManager();
        return String.valueOf(tm.getNetworkType());
    }

    /**
     * 返回当前网络类型，NETWORK_TYPE_XXX
     */
    public int getNetworkType() {
        ConnectivityManager connectivityManager = connectivityManager();
        if (connectivityManager == null)
            return NETWORK_TYPE_UNKNOWN;
        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetInfo == null)
            return NETWORK_TYPE_UNKNOWN;
        switch (activeNetInfo.getType()) {
            case ConnectivityManager.TYPE_WIFI:
                return NETWORK_TYPE_WIFI;
            case ConnectivityManager.TYPE_MOBILE:
                TelephonyManager tm = telephonyManager();
                int t = tm.getNetworkType();
                switch (t) {
                    case TelephonyManager.NETWORK_TYPE_GPRS:
                    case TelephonyManager.NETWORK_TYPE_EDGE:
                    case TelephonyManager.NETWORK_TYPE_CDMA:
                    case TelephonyManager.NETWORK_TYPE_1xRTT:
                    case TelephonyManager.NETWORK_TYPE_IDEN:
                        return NETWORK_TYPE_2G;
                    case TelephonyManager.NETWORK_TYPE_UMTS:
                    case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    case TelephonyManager.NETWORK_TYPE_HSDPA:
                    case TelephonyManager.NETWORK_TYPE_HSUPA:
                    case TelephonyManager.NETWORK_TYPE_HSPA:
                    case TelephonyManager.NETWORK_TYPE_HSPAP:
                    case TelephonyManager.NETWORK_TYPE_EVDO_B:
                    case TelephonyManager.NETWORK_TYPE_EHRPD:
                        return NETWORK_TYPE_3G;
                    case TelephonyManager.NETWORK_TYPE_LTE:
                        return NETWORK_TYPE_4G;
                }
        }
        return NETWORK_TYPE_UNKNOWN;
    }
}
