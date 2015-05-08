package com.yamibo.main.yamibolib.app;

import android.app.Application;

import org.apache.http.protocol.HttpService;

/**
 * Created by wangxiaoyan on 15/4/20.
 */
public class YMBApplication extends Application {
    private static YMBApplication instance;
    private ServiceManager services;

    public static YMBApplication instance() {
        if (instance == null) {
            throw new IllegalStateException("Application has not been created");
        }

        return instance;
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

    public CacheService mapiCacheService() {
        if (mapiCacheService == null) {
            mapiCacheService = (CacheService) getService("mapi_cache");
        }
        return mapiCacheService;
    }

    public MApiService mapiService() {
        if (mapiService == null) {
            mapiService = (MApiService) getService("mapi");
        }
        return mapiService;
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
}
