package com.yamibo.main.yamibolib.locationservice.impl;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.yamibo.main.yamibolib.locationservice.LocationListener;
import com.yamibo.main.yamibolib.locationservice.model.Location;

import static com.yamibo.main.yamibolib.locationservice.impl.BDLocationService.bdLocationToString;
import static com.yamibo.main.yamibolib.locationservice.impl.util.debugLog;


/**
 * Created by Clover on 2015-06-09.
 */
class BDListener implements BDLocationListener {
    private LocationListener supervisorListener=null;
    private DefaultLocationService supervisorService =null;

    private BDLocationService bdService =null;

    public BDListener(LocationListener supervisorListener, BDLocationService bdService){
        this.supervisorListener=supervisorListener;
        this.bdService=bdService;
        this.supervisorService=bdService.supervisorService;
    }

    @Override
    public void onReceiveLocation(BDLocation bdLocation) {

        debugLog("BDlocation received" + bdLocationToString(bdLocation));

        Location LocationResult = BDLocationService.toLocation(bdLocation);

        bdService.onReceiveLocation();
        //invoke service to retrieve this location
       /* if(supervisorService !=null)
            supervisorService.onReceiveLocation(LocationResult);
        else
            debugLog("targetService not assigned!!");
*/
    }
}