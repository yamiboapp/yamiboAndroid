package com.yamibo.main.yamibolib.app;

import android.app.Application;

import com.yamibo.main.yamibolib.accountservice.AccountService;
import com.yamibo.main.yamibolib.configservice.ConfigService;
import com.yamibo.main.yamibolib.dataservice.http.HttpService;
import com.yamibo.main.yamibolib.locationservice.LocationService;
import com.yamibo.main.yamibolib.statistics.StatisticsService;

/**
 * Created by wangxiaoyan on 15/4/20.
 */
public class YMBApplication extends Application {
    private static YMBApplication instance;
    private ServiceManager services;
    private HttpService httpService;
    private ConfigService configService;
    private AccountService accountService;
    private LocationService locationService;
    private StatisticsService statisticsService;

    public static YMBApplication instance() {
        if (instance == null) {
            throw new IllegalStateException("Application has not been created");
        }

        return instance;
    }

    public static YMBApplication _instance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //TODO temporary change: comment out the following for debug purpose
        //locationService().start();
    }

    public Object getService(String name) {
        if (services == null) {
            services = new ServiceManager(this);
        }
        return services.getService(name);
    }

    public HttpService httpService() {
        if (httpService == null) {
            httpService = (HttpService) getService("http");
        }
        return httpService;
    }

    public ConfigService configService() {
        if (configService == null) {
            configService = (ConfigService) getService("config");
        }
        return configService;
    }

    public AccountService accountService() {
        if (accountService == null) {
            accountService = (AccountService) getService("account");
        }
        return accountService;
    }

    public LocationService locationService() {
        if (locationService == null) {
            locationService = (LocationService) getService("location");
        }
        return locationService;
    }

    public StatisticsService statisticsService() {
        if (statisticsService == null) {
            statisticsService = (StatisticsService) getService("statistics");
        }
        return statisticsService;
    }
}
