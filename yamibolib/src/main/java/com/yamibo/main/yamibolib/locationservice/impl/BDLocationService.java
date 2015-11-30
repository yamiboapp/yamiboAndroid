package com.yamibo.main.yamibolib.locationservice.impl;


import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.yamibo.main.yamibolib.Utils.Log;
import com.yamibo.main.yamibolib.locationservice.LocationListener;
import com.yamibo.main.yamibolib.locationservice.LocationService;
import com.yamibo.main.yamibolib.locationservice.model.City;
import com.yamibo.main.yamibolib.locationservice.model.Location;

import android.content.Context;
import android.location.Address;

import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.yamibo.main.yamibolib.locationservice.impl.util.debugLog;


/**
 * Clover on 2015-06-01
 * helper class to control BDLocationClient.<br>
 * modified from default BD location service sample, which is an Application classuse getApplication() in activity to control this, and register its name in manifest.xml
 * 将LocationListener 翻译为apiListener
 */
public class BDLocationService implements APILocationService {
    DefaultLocationService supervisorService=null;

    /**
     * 为每个LocationListener提供一个相应的API location listener
     */
    Map<LocationListener, BDListener> mapListeners=new HashMap<LocationListener, BDListener>();

    //BDListener myListener;

    private Context mContext;

    private int providerChoice=PROVIDER_NETWORK;
    private boolean isStarted=false;

    public static final int PROVIDER_BEST=0;
    public static final int PROVIDER_NETWORK=1;
    public static final int PROVIDER_GPS=2;





    //Use the following two flags to track the working status of location application
    private boolean isLocationDemand=false;
    private boolean isLocationReceived=false;


    private LocationClient bdLocationClient;

    private LocationClientOption.LocationMode locationMode = LocationClientOption.LocationMode.Hight_Accuracy;
    private int updateInterval=3000;//default requestLocation not auto update

    /**
     * 返回街道名称
     */
    private static final boolean IS_NEED_ADDRESS =true;
    /**
     * 统一设定为三种模式 "gcj02","bd09ll","bd09"
     * 这里设定为百度经纬度
     */
    private static final String COORD_MODE ="bd09ll";

    /**
     *
     * @param providerChoice 使用GPS/Network定位
     */
    public void setProvider(int providerChoice){
        switch(providerChoice){
            case DefaultLocationService.PROVIDER_BEST:
                locationMode=LocationClientOption.LocationMode.Hight_Accuracy;
                break;
            case DefaultLocationService.PROVIDER_NETWORK:
                locationMode=LocationClientOption.LocationMode.Battery_Saving;
                break;
            case DefaultLocationService.PROVIDER_GPS:
                locationMode=LocationClientOption.LocationMode.Device_Sensors;
                break;
            default:
                debugLog("Unknown provider mode!");
                return;
        }
    }


    /**
     * @param input
     * 设置发起定位请求的间隔时间为>=1000 (ms) 时为循环更新
     * default value -1 means no automatic update.
     */
    public void setUpdateInterval(int input){
        updateInterval =input;
    }

    /**
     * instantiate client
     * @param context
     */
    public BDLocationService(Context context, int updateInterval, int providerChoice, DefaultLocationService supervisorService) {
        this.supervisorService= supervisorService;
        bdLocationClient = new LocationClient(context);
        mContext=context;
        setUpdateInterval(updateInterval);
        debugLog("construct BD service with updateInterval:" + updateInterval);
        setProvider(providerChoice);
        //myListener=new BDListener(null,this);
    }

    /**
     * for debugLog mainly
     * @param location
     * @return its String format for Output
     */
    public static String bdLocationToString(BDLocation location){
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

    /**
     * 百度定位为所有监听器的参数统一更改
     *
     */
    void applyOption() {
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(locationMode);//设置定位模式
        option.setCoorType(COORD_MODE);//返回的定位结果是百度经纬度
        option.setScanSpan(updateInterval);
        debugLog("apply option updateInterval:" + updateInterval);
        option.setIsNeedAddress(IS_NEED_ADDRESS);

        bdLocationClient.setLocOption(option);
        debugLog("initiate BD client Option done");
    }

    @Override
    public int status() {
        int mStatus;
        if (isLocationReceived)
            mStatus = LocationService.STATUS_LOCATED;
        else {
            if (isLocationDemand)
                mStatus = LocationService.STATUS_TRYING;
            else
                mStatus = LocationService.STATUS_FAIL;
        }
        return mStatus;
    }


    /**
     * BDclient start(). <br>
     * BDclient's requestLocation() not included
     * 测试结果：可以添加多个监听器。百度重复添加同一个监听器无效
     */
    public boolean start(){
        if (bdLocationClient== null||mapListeners.isEmpty()) {
            return false;
        }
        if(isStarted){
            debugLog("already in work");
            return true;
        }
        //parameter input in the register
        bdLocationClient.start();
        isStarted=true;
        debugLog("location service starts");
        return true;
    }

    /**
     * @return true if location demand was successfully sent <br>
     *     <br>
     * invoke BDclient requestLocation(),whose int code returned by requestLocation()<br>
     * 0：正常发起了定位。
     *1：服务没有启动。
     *2：没有监听函数。
     *6：请求间隔过短。 前后两次请求定位时间间隔不能小于1000ms。<br>
     *
     */
    boolean requestLocation() {
        if (bdLocationClient == null) {
            debugLog("null bdClient");
            return false;
        }
        debugLog("Request BDLocation update");

        // bdLocationClient.start();
        int code= bdLocationClient.requestLocation();
        debugLog("request code = " + code);
        switch (code)
        {
            case 0:
                isLocationDemand=true;
                isLocationReceived=false;
                return true;
            case 6:
                return false;
            default:
                return false;

        }
    }

    /**
     * BD add listener
     * 测试结果:添加新的监听器，无需重新start()
     */
    void registerListener(BDListener bdListener){
        if(bdLocationClient==null){
            debugLog("null bdLocationClient");
            return;
        }
        bdLocationClient.registerLocationListener(bdListener);
        debugLog("BDListenerBDListener added" + bdListener.toString());
        applyOption();
    }

    void unregisterListener(BDListener bdListener) {
        if(bdLocationClient==null){
            debugLog("null bdLocationClient");
            return;
        }
        bdLocationClient.unRegisterLocationListener(bdListener);
        debugLog("BDListener removed: " + bdListener.toString());
    }

    /**
     * BD client stop
     * 测试结果：可以重复停止
     * reset flags, clear listeners
     */
    public void stop() {
        if (bdLocationClient == null)
            return;

/** this iteration will cause error (because KeySet is changing in the loop?)
      for (LocationListener listener: mapListeners.keySet())
            removeListener(listener);
 */
        /**
         * when multiple listners exist, asynchronous unregister listener!!!<br>
         * if first clear map or stop client, then unregister listener will cause error
         */
        try{
            for(BDListener listener:mapListeners.values())
                bdLocationClient.unRegisterLocationListener(listener);
        }
        catch (Exception e){
            debugLog("error in unregister listeners "+e.toString());
        }finally {
            mapListeners.clear();
            bdLocationClient.stop();
            isStarted=false;
            resetProgressFlag();
            debugLog("location service stops");
        }

    }

    @Override
    public boolean refresh() {
        return requestLocation();
    }

    @Override
    public void addListener(LocationListener listener) {
        if (listener != null && !mapListeners.containsKey(listener)) {
            BDListener bdListener=new BDListener(listener,this);

            bdListener.debugMessage="BD Listener (added to BDlisteners' set of size "+mapListeners.values().size()+")";
            debugLog("new and register "+bdListener.debugMessage);
            registerListener(bdListener);
            mapListeners.put(listener,bdListener);
        }
        debugLog("\nsize " +mapListeners.keySet().size()+" keySet: "+mapListeners.keySet().toString()
                +"size " +mapListeners.values().size()+" values: "+mapListeners.values().toString());
    }

    @Override
    public void removeListener(LocationListener listener) {
        if (listener != null && mapListeners.containsKey(listener)) {
            unregisterListener(mapListeners.get(listener));
            //remove entries (should exist one) associated with the given listener
            for(Iterator<Map.Entry<LocationListener, BDListener>> it = mapListeners.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<LocationListener, BDListener> entry = it.next();
                if (entry.getKey().equals(listener)) {
                    it.remove();
                }
            }
        }
        else
            debugLog("listener is null or not contained as key");
        debugLog("\nsize " + mapListeners.keySet().size() + " keySet: " + mapListeners.keySet().toString()
                + "size " + mapListeners.values().size() + " values: " + mapListeners.values().toString());
    }

    @Override
    public void resetAPIServiceOption(int updateInterval, int providerChoice) {
        setUpdateInterval(updateInterval);
        setProvider(providerChoice);
        applyOption();
    }

    @Override
    public boolean isClientStarted() {
        if(bdLocationClient!=null)
            return bdLocationClient.isStarted();
        else
            return false;
    }


    /**
     * 用Baidu来处理真实坐标
     * @param latitude
     * @param longtitude
     * @return
     */
    public Location realCoordsToLocationViaBD(double latitude, double longtitude){
        double offsetLatitude=latitude;
        double offsetLongitude=longtitude;
        String address=null;
        City city=null;
        int accuracy=0;
        int isInCN;
        long mTime =System.currentTimeMillis();
        if(isInChinaViaBD(latitude,longtitude)) {
            isInCN = Location.IN_CN;
            //always convert the coord from Android API when in CN
            try {
                JSONObject bdCoord = new util().convertToBDCoord(latitude, longtitude);
                if(bdCoord!=null){
                    offsetLatitude = (double) bdCoord.get("offsetLatitude");
                    offsetLongitude = (double) bdCoord.get("offsetLongitude");
                }
                else
                    debugLog("obtain null JSON when call convertToBDCoord(latitude, longitude)");
            } catch (Exception e) {
                debugLog("error converting coords " + e.toString());
            }
        }
        else
            isInCN= Location.NOT_IN_CN;
        //read addres by BD using offsetCoords
        try {
            JSONObject jsonAddress= new util().geocodingViaBD(offsetLatitude, offsetLongitude);
            if(jsonAddress!=null){
                address=(String)jsonAddress.get("address");
                city=new City((String)jsonAddress.get("city"));
            }
            else {
                debugLog("obtain null JSON");
            }
        }
        catch (Exception e) {
            debugLog("error geocoding offsetCoords via BD" + e.toString());
        }

        Location Location =new Location
                (latitude,longtitude,offsetLatitude,offsetLongitude,address,city,accuracy,isInCN,mTime);
        return Location;
    }

    /**
     * use real coords to judge country (not far from offset coords)
     * @param latitude
     * @param longtitude
     * @return return true if encouter an error
     */
    private boolean isInChinaViaBD(double latitude, double longtitude) {
        JSONObject addressViaBD=new util().geocodingViaBD(latitude,longtitude);
        try{
            if(addressViaBD!=null)
                return addressViaBD.get("country").equals("中国");
            else
                return true;
        }
        catch (Exception e){
            debugLog("error locate country via BD"+e.toString());
            return true;
        }
    }

    /**
     * @param source
     * @return
     * 注：百度坐标使用的偏转函数当应用在国外真实坐标（芯片坐标）的时候可能会出错，因此：<br>
     * 若为国内+GPS的情形，应使用百度的偏转函数；若转换出错，依旧使用原始GPS坐标<br>
     * 其它情形保持不变。<br>
     */
    public static Location toLocation(BDLocation source){
        if(source==null)
            return null;
        double latitude=source.getLatitude();
        double longtitude=source.getLongitude();
        double offsetLatitude=latitude;
        double offsetLongtitude=longtitude;
        String address=source.getAddrStr();
        City city=new City(source.getCity());
        int accuracy=(int)source.getRadius();
        int isInCN;
        if(isInChina(source)) {
            isInCN = Location.IN_CN;
            if(source.getLocType()==BDLocation.TypeGpsLocation) {
                try {
                    JSONObject bdCoord =new util().convertToBDCoord(latitude, longtitude);
                    if(bdCoord!=null){
                        offsetLatitude = (double) bdCoord.get("offsetLatitude");
                        offsetLatitude = (double) bdCoord.get("offsetLongitude");
                    }
                    else
                        debugLog("null JSONObject when call convertToBDCoord(latitude, longitude)");

                } catch (Exception e) {
                    debugLog("error "+e.toString());
                }
            }
        }
        else
            isInCN= Location.NOT_IN_CN;
        Long mTime=toMTime(source.getTime());

        Location Location =new Location
                (latitude,longtitude,offsetLatitude,offsetLongtitude,address,city,accuracy,isInCN,mTime);
        return Location;
    }

    /**
     * Calculate the millisecond from BD location's time
     * TODO test: 百度返回位置结果的时间是用的什么Locale?服务器的中国区还是手机用户的默认区
     * @param strTime
     * @return
     */
    private static long toMTime(String strTime) {
        try{
            DateFormat df=new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault());
            Date currentDate= df.parse(strTime);
            long millisecond= currentDate.getTime();
            return millisecond;
        }
        catch(Exception e){
            debugLog("transform time error "+e.toString());
            return System.currentTimeMillis();
        }
    }
    //Location location = new Location();
    //return location;

    /**
     * 根据百度定位服务的升级可能会发生变化
     * @param location
     * @return <br>
     * return true when encounter error
     */
    public static boolean isInChina(BDLocation location){
        if(location==null)
            return true;
        if((location.getCountry())!=null)
            return(location.getCountry().equals("中国"));
        else
            return true;
    }

    private void resetProgressFlag() {
        isLocationDemand=false;
        isLocationReceived=false;
    }
    void onReceiveLocation() {
        debugLog("BDservice onReceiveLocation");
        isLocationReceived=true;
    }

}
