package com.yamibo.main.yamibolib.configservice.impl;


import android.content.Context;

import com.yamibo.main.yamibolib.configservice.ConfigChangeListener;
import com.yamibo.main.yamibolib.configservice.ConfigService;

import org.json.JSONObject;

public class DefaultConfigService implements ConfigService {
    private Context mContext;

    public DefaultConfigService(Context context) {
        mContext = context;
    }

    @Override
    public JSONObject dump() {
        return null;
    }

    @Override
    public void refresh() {

    }

    @Override
    public void addListener(String key, ConfigChangeListener l) {

    }

    @Override
    public void removeListener(String key, ConfigChangeListener l) {

    }
}
