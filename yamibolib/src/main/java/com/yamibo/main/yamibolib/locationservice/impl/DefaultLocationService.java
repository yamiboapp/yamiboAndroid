package com.yamibo.main.yamibolib.locationservice.impl;

import android.content.Context;

import com.yamibo.main.yamibolib.locationservice.LocationListener;
import com.yamibo.main.yamibolib.locationservice.LocationService;
import com.yamibo.main.yamibolib.model.GPSCoordinate;

import org.json.JSONObject;

/**
 * Created by wangxiaoyan on 15/5/25.
 */
public class DefaultLocationService implements LocationService {

    private Context mContext;

    public DefaultLocationService(Context context) {
        mContext = context;
    }

    @Override
    public int status() {
        return 0;
    }

    @Override
    public boolean hasLocation() {
        return false;
    }

    @Override
    public JSONObject location() {
        return null;
    }

    @Override
    public GPSCoordinate realCoordinate() {
        return null;
    }

    @Override
    public GPSCoordinate offsetCoordinate() {
        return null;
    }

    @Override
    public String address() {
        return null;
    }

    @Override
    public JSONObject city() {
        return null;
    }

    @Override
    public boolean start() {
        return false;
    }

    @Override
    public void stop() {

    }

    @Override
    public boolean refresh() {
        return false;
    }

    @Override
    public void addListener(LocationListener listener) {

    }

    @Override
    public void removeListener(LocationListener listener) {

    }

    @Override
    public void selectCoordinate(int type, GPSCoordinate coord) {

    }
}
