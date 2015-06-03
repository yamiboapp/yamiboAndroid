package com.yamibo.main.yamibolib.locationservice.impl;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

import android.content.Context;
import android.util.Log;

/**
 * Clover:
 * modified from default BD location service sample: use getApplication() in activity to control this, and register its name in manifest.xml
 * here: remove its Application inheritance in order to easily instantiate this class in helper class
 */
class BDLocationApplication {
    BDLocation locationResult;

    //Use the following two flags to track the working status of location application
    boolean isLocationDemand=false;
    boolean isLocationReceived =false;

    DefaultLocationService targetService=null;

    private LocationClient mLocationClient;
    private MyLocationListener mMyLocationListener;

    private LocationClientOption.LocationMode locationMode = LocationClientOption.LocationMode.Hight_Accuracy;
    private String coordMode ="bd09ll";
    private int span=-1;//default requestLocation not auto update
    private boolean isNeedAddress=true;

    /**
     * toggle debug function output on/off
     */
    private final boolean IS_DEBUG_ENABLED=true;



    /**
     *
     * @param input LocationClientOption.LocationMode.Hight_Accuracy 高精度模式
     *              , Battery_Saving 低功耗模式
     *              , Device_Sensors 仅设备(Gps)模式
     */
    void setLocationMode(LocationClientOption.LocationMode input){
        locationMode=input;
    }

    /**
     *
     * @param input choose "gcj02","bd09ll","bd09"
     * @return
     */
    boolean setCoordMode(String input){
        String[] choices={"gcj02","bd09ll","bd09"};
        for(String choice:choices)
            if(choice.equals(input)) {
                coordMode =input;
                return true;
            }
        return false;
    }

    /**
     *
     * @param input
     * 设置发起定位请求的间隔时间为>=1000 (ms) 时为循环更新
     * default value -1 means no automatic update.
     */
    void setSpan(int input){
        span =input;
    }

    void setIsNeedAddress(boolean input){
        isNeedAddress=input;
    }


    /**
     * instantiate client and listener
     * @param context
     */
    public BDLocationApplication(Context context) {

        mLocationClient = new LocationClient(context);
        mMyLocationListener = new MyLocationListener();

        if(mLocationClient==null||mMyLocationListener==null)
            debugLog("error instantiate client or listener!");
        else
            debugLog("instantiate LocationClient and MyLocationListener done");
    }






    /**
     * 实现实位回调监听
     * call back to location target location service
     */
    public class MyLocationListener implements BDLocationListener {
        @Override
        /**
         * set isLocationReceived=true,
         * update locationResult,
         * call targetService.onReceiveBDLocation(location)
         */
        public void onReceiveLocation(BDLocation location) {
            isLocationReceived =true;
            locationResult=new BDLocation(location);
            debugLog("BDlocation received" + toStringOutput(locationResult));

            //invoke service to retrieve this location
            if(targetService!=null)
                targetService.onReceiveBDLocation(locationResult);
            else
                debugLog("targetService not assigned!!");
        }
    }


    /**
     *
     * @param location
     * @return its String format for Output
     */
    public static String toStringOutput(BDLocation location){
        StringBuffer sb = new StringBuffer(256);
        sb.append("time : ");
        sb.append(location.getTime());
        sb.append("\nerror code : ");
        sb.append(location.getLocType());
        sb.append("\nlatitude : ");
        sb.append(location.getLatitude());
        sb.append("\nlontitude : ");
        sb.append(location.getLongitude());
        sb.append("\nradius : ");
        sb.append(location.getRadius());
        if (location.getLocType() == BDLocation.TypeGpsLocation) {
            sb.append("\nspeed : ");
            sb.append(location.getSpeed());
            sb.append("\nsatellite : ");
            sb.append(location.getSatelliteNumber());
            sb.append("\ndirection : ");
            sb.append("\naddr : ");
            sb.append(location.getAddrStr());
            sb.append(location.getDirection());
        } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {
            sb.append("\naddr : ");
            sb.append(location.getAddrStr());
            //运营商信息
            sb.append("\noperationers : ");
            sb.append(location.getOperators());
        }
        return sb.toString();
    }


    private void debugLog(String Message){
        if(IS_DEBUG_ENABLED)
            Log.i("BDLocationApplication", "DEBUG_" + Message);
    }


    /**Baidu sample, set option for the client
     *
     */
    void initLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(locationMode);//设置定位模式
        option.setCoorType(coordMode);//返回的定位结果是百度经纬度
        option.setScanSpan(span);
        option.setIsNeedAddress(isNeedAddress);

        mLocationClient.setLocOption(option);
        debugLog("initiate Location done");
    }

    /**
     * BDclient start(). <br>
     * BDclient's requestLocation() not included
     */
    void start(){
        mLocationClient.start();
    }

    /**
     * BDclient requestLocation(),
     * set isLocationDemand flag
     *int code returned by requestLocation()
     *0：正常发起了定位。
     *1：服务没有启动。
     *2：没有监听函数。
     *6：请求间隔过短。 前后两次请求定位时间间隔不能小于1000ms。
     *
     */
    void requestLocation(){

        int code=mLocationClient.requestLocation();
        switch (code)
        {
            case 0:isLocationDemand=true;
                return;
            case 6:isLocationDemand=false;
                return;
            default:
                isLocationDemand=false;
                return;

        }


    }

    /**
     * BD add listener
     */
    void addListener(){
        mLocationClient.registerLocationListener(mMyLocationListener);
        debugLog("mListtner added" + mMyLocationListener.toString());
    }

    /**
     * BD remove listener
     */
    void removeListener() {
       mLocationClient.unRegisterLocationListener(mMyLocationListener);
        debugLog("mListener removed: " + mMyLocationListener.toString());
    }

    /**
     * reset location demand/receive flags to false
     */
    void resetFlag() {
        isLocationDemand=false;
        isLocationReceived=false;
    }

    /**
     * BD client stop
     */
    void stop() {
        mLocationClient.stop();
    }

}
