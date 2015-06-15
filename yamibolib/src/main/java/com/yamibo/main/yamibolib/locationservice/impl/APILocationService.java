package com.yamibo.main.yamibolib.locationservice.impl;


import com.yamibo.main.yamibolib.locationservice.LocationListener;

/**
 * Created by Clover on 2015-06-03.
 */
interface APILocationService {
    public int status();
    public boolean start();

    public void stop();
    public boolean refresh();

    void setUpdateInterval(int updateInterval);

    void setProvider(int providerChoice);

    public void addListener(LocationListener listener);
    public void removeListener(LocationListener listener);

    void resetServiceOption(int updateInterval, int providerChoice);

    boolean isClientStarted();
}
