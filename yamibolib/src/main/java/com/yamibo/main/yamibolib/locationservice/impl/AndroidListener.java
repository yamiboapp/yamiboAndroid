package com.yamibo.main.yamibolib.locationservice.impl;

import android.location.Location;
import android.os.Bundle;

import com.yamibo.main.yamibolib.locationservice.LocationListener;

import static com.yamibo.main.yamibolib.locationservice.impl.util.debugLog;


/**
 * Created by Clover on 2015-06-09.
 */
class AndroidListener implements android.location.LocationListener {
    private LocationListener supervisorListener=null;
    private DefaultLocationService supervisorService =null;

    // use this to unregister the listener so that single update can be realized
    private AndroidLocationService androidService =null;

    /**
     * 仅ANDROID API 使用。用于进行单次更新操作。
     */
    public boolean isAutoRequestUpdate =false;


    public AndroidListener(LocationListener supervisorListener, AndroidLocationService androidService){
        this.supervisorListener=supervisorListener;
        this.androidService=androidService;
        this.supervisorService=androidService.supervisorService;
    }

    @Override
    public void onLocationChanged(Location location) {
        // send result to supervisorService
        if(!isAutoRequestUpdate)
            androidService.unregisterListener(this);
        debugLog("Android on location changed received:\n" + location.toString());

        com.yamibo.main.yamibolib.locationservice.model.Location LocationResult = androidService.toLocation(location);
        //invoke service to retrieve this location

        androidService.onReceiveLocation();

        if(supervisorService !=null)
            supervisorService.onReceiveLocation(LocationResult);
        else
            debugLog("supervisorService not assigned!!");

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
