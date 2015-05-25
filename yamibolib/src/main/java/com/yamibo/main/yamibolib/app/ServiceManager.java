package com.yamibo.main.yamibolib.app;

import android.content.Context;

import com.yamibo.main.yamibolib.accountservice.AccountService;
import com.yamibo.main.yamibolib.accountservice.impl.DefaultAccountService;
import com.yamibo.main.yamibolib.configservice.ConfigService;
import com.yamibo.main.yamibolib.configservice.impl.DefaultConfigService;
import com.yamibo.main.yamibolib.dataservice.http.HttpService;
import com.yamibo.main.yamibolib.dataservice.http.impl.DefaultHttpService;
import com.yamibo.main.yamibolib.locationservice.LocationService;
import com.yamibo.main.yamibolib.locationservice.impl.DefaultLocationService;
import com.yamibo.main.yamibolib.statistics.StatisticsService;
import com.yamibo.main.yamibolib.statistics.impl.DefaultStatisticsService;

/**
 * Created by wangxiaoyan on 15/4/20.
 */
public class ServiceManager {

    private final Context context;
    private HttpService http;
    private ConfigService config;
    private AccountService account;
    private LocationService location;
    private StatisticsService statistics;

    public ServiceManager(Context context) {
        this.context = context;
    }

    public synchronized Object getService(String name) {
        if ("http".equals(name)) {
            if (http == null) {
                http = new DefaultHttpService(context);
            }
            return http;
        }
        if ("config".equals(name)) {
            if (config == null) {
                config = new DefaultConfigService(context);
            }
            return config;
        }
        if ("account".equals(name)) {
            if (account == null) {
                account = new DefaultAccountService(context);
            }
            return account;
        }
        if ("location".equals(name)) {
            if (location == null) {
                location = new DefaultLocationService(context);
            }
            return location;
        }
        if ("statistics".equals(name)) {
            if (statistics == null) {
                statistics = new DefaultStatisticsService(context);
            }
            return statistics;
        }
        return null;
    }
}
