package com.yamibo.main.yamibolib.statistics.impl;

import android.content.Context;

import com.yamibo.main.yamibolib.Utils.NameValuePair;
import com.yamibo.main.yamibolib.statistics.StatisticsService;

import java.util.List;

/**
 * Created by wangxiaoyan on 15/5/25.
 */
public class DefaultStatisticsService implements StatisticsService {
    private Context mContext;

    public DefaultStatisticsService(Context context) {
        mContext = context;
    }

    @Override
    public void push(String rawLine) {

    }

    @Override
    public void record(List<NameValuePair> form) {

    }

    @Override
    public void pageView(String url, List<NameValuePair> extras) {

    }

    @Override
    public void event(String category, String action, String label, int value, List<NameValuePair> extras) {

    }

    @Override
    public void flush() {

    }
}
