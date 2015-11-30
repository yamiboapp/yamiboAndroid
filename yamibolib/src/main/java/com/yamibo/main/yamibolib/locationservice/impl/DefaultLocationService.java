package com.yamibo.main.yamibolib.locationservice.impl;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.yamibo.main.yamibolib.locationservice.LocationListener;
import com.yamibo.main.yamibolib.locationservice.LocationService;
import com.yamibo.main.yamibolib.locationservice.model.City;
import com.yamibo.main.yamibolib.locationservice.model.Location;
import com.yamibo.main.yamibolib.model.GPSCoordinate;

import java.util.ArrayList;
import java.util.List;

import static com.yamibo.main.yamibolib.locationservice.impl.util.debugLog;

/**
 * 这个类与上层用户交谈，检查环境变量(system settings)，读取更新间隔等参数，管理切换定位服务，定义常量和计算函数。
 * Created by wangxiaoyan on 15/5/25.<br>
 * Clover: 将这个类实例化，对定位服务进行启动刷新,读取定位数据。<br>
 * 注意百度定位服务模式须在在主线程里使用。
 * <p/>
 * 基本使用方法 ：<br>
 * 在activity中，运行<br>
 * DefaultLocationService apiLocationService=new DefaultLocationService(getApplicationContext()),或单有参数的形式<br>
 * apiLocationService.start();<br>
 * apiLocationService.stop();<br>
 * <p/>
 *默认是单次更新请求。刷新监听请调用 refresh();<br>
 * <p/>
 * 可以将任何拥有LocationLisener接口的实例listener添加到队列:<br>
 * addListener(listener), removeListener(listener);
 * <i>封装：帮助类BDLocationService和AndroidLocationService会建立一个相应的API定位监听器。</i>
 * <p/>
 * 以下方法更改已注册的所有监听器的参数，并将作为下一次的监听器参数: <br>
 * resetAPIServiceOption(update_interval,provider);<br>
 *REMARK user:若想使用GPS和网络混合provider并选择最优结果，推荐建立两个DefaultLocationService实例，分别监听网络和GPS并比较结果的精度：<br>
 *  <i>注解：百度定位模式下只能设置统一的监听参数。因此对AndroidAPI也作此简化处理。<br>
 *单监听器模式下，AndroidAPI默认的Best模式会变成监听GPS，导致长时间无为之结果。<br>
 *百度的单监听器在混合模式下，会将GPS和网络的结果混在一起返回，缺少文档说明。<br>
 *</i>
 *
* REMAKR user: 经测试，若在activity里注册本服务的实例，百度API自动刷新功能在离开当前activity后不会停止。所以请在离开activity前stop或者保存必要信息。
 *REMARK user: 定位服务的许多方法都是异步的，如果要sequential的使用这些方法，需要小心避免错误。
 *
 */
public class DefaultLocationService implements LocationService {



    private Context mContext;


    /**
     * 用于实例化 百度 BDLocationClient 或 Android locationManager
     */
    private APILocationService apiLocationService = null;
    /**
     * 监听器队列
     *
     */
    private List<LocationListener> activeListeners = new ArrayList<>();
    /**
     * 当更新时间小于1000ms时，为单次更新
     */
    private int updateInterval =-1;
    /**
     * 默认的serviceMode为百度定位（适用中国）或AndroidAPI定位（适用中国之外）
     */
    private int serviceMode=BAIDU_MODE;
    //public int serviceMode=ANDROID_API_MODE;
    /**
     * 是否允许程序根据定位结果自动选择定位服务
     */
    private boolean isAutoSwitchService =false;

    /**
     * 任意定位服务取得的上次程序定位的结果
     * TODO user:  add methods storing and reading lastKnownLocation
     */
    protected Location lastKnownLocation = null;




    /**
     * 默认选择GPS and/or Network进行定位
     */
    private int providerChoice=PROVIDER_NETWORK;

    //No Use
//    private boolean isStarted=false;
//    private boolean isLocationDemand=false;


    private boolean isLocationReceived=false;



    /**
     * 自动更新启动时的默认更新间隔
     */
    public static final int DEFAULT_UPDATE_INTERVAL=10*60*1000;//default requestLocation time 10min


    public static final int BAIDU_MODE=0;
    public static final int ANDROID_API_MODE=1;
    /**
     * 同时用GPS和Network
     */
    public static final int PROVIDER_BEST=0;
    /**
     *   只用Network
     */
    public static final int PROVIDER_NETWORK=1;
    /**
     * 只用GPS
     */
    public static final int PROVIDER_GPS=2;








    /**
     * Clover:
     * locationClient and Listener instantiated
     * link onReceived callback
     * listener not registered! service not started! use start();
     * Creat manager for BAIDU location by default
     * @param context
     */
    public DefaultLocationService(Context context) {
        mContext = context;
        //TODO user: read stored lastKnownLocation
        constructAPIService();
    }

    private void constructAPIService() {
        debugLog("construct API service");
        if(lastKnownLocation!=null&&isAutoSwitchService){
            debugLog("autoswitch api based on lastKnownLocation");
            if(lastKnownLocation.getRegion()==Location.IN_CN)
                serviceMode=BAIDU_MODE;
            else
                serviceMode=ANDROID_API_MODE;
        }

        switch (serviceMode) {
            case BAIDU_MODE:
                apiLocationService = new BDLocationService(mContext,updateInterval,providerChoice,this);
                debugLog("Baidu location mode selected.");
                break;
            case ANDROID_API_MODE:
                apiLocationService =new AndroidLocationService(mContext,updateInterval,providerChoice,this);
                debugLog("Android API location mode selected");
                break;
            default:
                debugLog("Unknown location mode selected!");
                return;
        }
    }

    /**
     * 获取当前服务状态(交由具体API实例判断）
     * @return STATUS_LOCATED 表示当前定位已经完成。并可以持续获取可用的位置（定位服务可用）<br>
     *     <p/>
     *     STATUS_FAIL  表示当前状态为定位失败<br>
     *         <p/>
     *         STATUS_TRYING 表示当前定位服务在start()或refresh()正在尝试获取最新的位置<br>
     */
    @Override
    public int status() {
        return apiLocationService.status();
    }


    /**
     * 当有可用位置时返回true（仅包括最近一次取得的，和之前存储的）<br>
     * 这个位置不一定是最新的，请用requestLocation()来更新，
     * status()来确定当前实例有没有取得新位置。
     * @return
     */
    @Override
    public boolean hasLocation() {
        if(lastKnownLocation!=null)
            return true;
        else
            return false;
    }


    /**
     * 最近一次的位置结果（包括实例读取的存储结果，可能过时）<br>
     * 要确定结果是最新的，请确定status()为真（若需更新结果，可调用refresh()）。
     * @return
     */
    @Override
    public Location location() {
        return lastKnownLocation;
    }


    /**
     *
     * @return GPS模式：芯片坐标<br>
     *     网络查询：Android坐标，或百度纠偏坐标（国外很可能出错！，请自动或手动切换至AndroidAPI模式）
     *     <p>
     *         provider信息未写入，因为lastKnownLocation未存储此信息。
     *     </p>
     */
    @Override
    public GPSCoordinate realCoordinate() {
        return new GPSCoordinate(lastKnownLocation.latitude(),lastKnownLocation.longitude(),
                lastKnownLocation.accuracy(),lastKnownLocation.getTime(),"");
    }


    /**
     *
     * @return 国内GPS模式：芯片坐标经过百度纠偏计算<br>
     *     国内网络查询：百度纠偏或Android坐标经过百度纠偏计算<br>
     *         国外：realCoordinate
     *     <i>
     *         注释：provider信息未写入，因为lastKnownLocation未存储此信息。
     *     </i>
     */
    @Override
    public GPSCoordinate offsetCoordinate() {
        return new GPSCoordinate(lastKnownLocation.offsetLatitude(),lastKnownLocation.offsetLongitude(),
                lastKnownLocation.accuracy(),lastKnownLocation.getTime(),"");
    }

    @Override
    public String address() {
        if(hasLocation())
            return lastKnownLocation.address();
        else
            return null;
    }

    @Override
    public City city() {
        if(hasLocation())
            return lastKnownLocation.city();
        else
            return null;
    }

    @Override
    /**
     *
     * 若具体API尚未开始进行定位工作（未调用过start()或者调用stop()之后），
     * 会创建一个新的监听器并开始定位。
     *
     */
    public boolean start() {
        if(apiLocationService ==null||!isLocationEnabled(mContext))
            return false;
        if(activeListeners.isEmpty()){
            LocationListener listener=new LocationListener() {
                @Override
                public void onLocationChanged(LocationService sender) {
                    debugLog("A listener auto generated while service start() " +
                            "because the activeListeners arrays is empty");
                }
            };
            addListener(listener);
        }
        else {
            debugLog("Use existing listeners");
        }
        return apiLocationService.start();
    }

    /**
     *
     * 删除所有监听器，停止定位功能<br>
     * 可多次调用
     */
    @Override
    public void stop() {
        apiLocationService.stop();
        activeListeners.clear();
    }

    /**
     * Clover:
     * unregister listener and stop client
     * in Baidu service sample, listener is not removed when client stops?
     */


    /**
     * Clover
     * requestLocation (asynchronous)
     * return true if location demand has been sent
     */
    /*    @Override
    public boolean refresh() {
        if (mBDLocationApplication == null)
            return false;
        mBDLocationApplication.requestLocation();
        return mBDLocationApplication.isLocationDemand;
    }
    */
    /**
     *  让所有已知监听器发送异步刷新当前位置的请求。可多次调用<br>
     * 如果当前系统定位开关未打开，会直接返回false<br>
     * 注意：百度的返回值由它的定位服务统一提供<br>
     *     AndroidAPI 至少一个listener获取位置时返回值为true
     */
    @Override
    public boolean refresh() {
        if(!isLocationEnabled(mContext))
            return false;
        resetReceivedFlag();
        debugLog("goto apiLocationService.refresh()");
        return apiLocationService.refresh();
    }
    /**
     * 重置发送/接到 位置信息的flags
     */
    private void resetReceivedFlag() {
        isLocationReceived=false;
    }



    /**
     *
     * @param updateInterval 百度/Android API设置发起自动更新定位请求的间隔时间(ms)<br>
     *                        <1000 不会自动发送新请求。需要手动发送。
     * @param providerChoice PROVIDER_BEST 返回GPS和网络定位中最好的结果
     *              , PROVIDER_NETWORK 只使用网络和基站
     *              ,  PROVIDER_GPS 只用GPS模式<br>
     *让定位服务的所有已知监听器以新的参数连接并运行。
     */
    public void resetServiceOption(int updateInterval, int providerChoice){
        apiLocationService.resetAPIServiceOption(updateInterval, providerChoice);
    }



    /**
     * @param listener 任何有Location listener interface的监听器<br>
     *     <p/>
     *同一个监听器不会被重复添加<br>
     * <i>封装：在APILocationService里面增加一个相应的位置监听器</i>
     */
    @Override
    public void addListener(LocationListener listener) {
        if(activeListeners.contains(listener)){
            debugLog("listener is already active and known by the service!");
            return;
        }
        debugLog("new LocationListener is added to activeListeners array of size " + activeListeners.size() + "\n");
        activeListeners.add(listener);
        apiLocationService.addListener(listener);
    }

    /**
     * 删除监听器
     * @param listener
     * <i>封装：在APILocationService里删除相对应的位置监听器</i>
     */
    @Override
    public void removeListener(LocationListener listener)
    {
        if(listener != null && activeListeners.contains(listener)) {
            apiLocationService.removeListener(listener);
            activeListeners.remove(listener);
        }
        else
            debugLog("listener is null or not contained as activeListeners");
    }

    //TODO QUESTION: 这个方法用来做什么的？默认坐标是怎么回事？另外用户传来的GPS里并未指定address和City（使用默认的上海）<br>
    /**
     * 当用户明确指定realCoords时，用百度geocoding将其翻译为Location并作为lastKnownResult,<br>
     *     <i>
     *         不调用onReceiveLocation方法
     *     </i>
      */
    @Override
    public void selectCoordinate(int type, GPSCoordinate coord) {
        switch (type){
            case 0:
            case 1:
            case -1:
                debugLog("selectedLocation not stored");
                return;
            case 0xFF01:
                lastKnownLocation=realCoordsToLocationViaBD(coord.latitude(),coord.longitude());
        }
        return;
    }

    /**
     * 判断系统的定位服务设置是否开启
     * @param context
     * @return
     */
    public static boolean isLocationEnabled(Context context) {
        if (context == null)
            return false;

        int locationMode = 0;
        String locationProviders;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);

            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
            }
            return locationMode != Settings.Secure.LOCATION_MODE_OFF;

        } else {
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }


    }


    /**
     * 当任何一个监听器收到位置信息时，运行监听器队列中的每一个监听器。<p/>
     * 实现方法注解：AndroidAPI情形：当一个监听器设置为处于单次更新模式时，用unregister来停止它的更新功能，仍将其保留在监听器列表中。
     * 下次refresh时它会被重新注册并尝试获取位置信息。
     * TODO user: 可以添加代码
     */
    public void onReceiveLocation(Location LocationResult) {
        //记下最新的位置，设定flag
        this.lastKnownLocation = LocationResult;
        isLocationReceived=true;
        debugLog("LocationService updated location from one listener");
        //所有监听器LocationListener运行自定义方法
        for (LocationListener listener : activeListeners) {
            listener.onLocationChanged(this);
        }
        debugLog("all activeListeners perform their own actions");
        //根据位置信息判断是否自动切换为baidu或AndroidAPI定位服务
        if(isAutoSwitchService) {
            debugLog("switch service!");
            if (lastKnownLocation.getRegion() == Location.IN_CN && serviceMode != BAIDU_MODE) {
                switchServiceMode(BAIDU_MODE);
                debugLog("autoSwitch to Baidu mode");
            }
            if (lastKnownLocation.getRegion() == Location.NOT_IN_CN && serviceMode==BAIDU_MODE) {
                switchServiceMode(ANDROID_API_MODE);
                debugLog("autoSwitch to Android mode");
            }
        }
        //debug code show lastKnownLocation
        util.debugShow(util.locationToDebugString(location()));

        //TODO user 可以在此添加代码

    }






        /**
         * android可能有网络连接的问题
         */
    public Location realCoordsToLocationViaAndroid(double latitude, double longitude){
        android.location.Location androidLocation=new android.location.Location("userInput");
        androidLocation.setLatitude(latitude);
        androidLocation.setLongitude(longitude);
        androidLocation.setTime(System.currentTimeMillis());
        androidLocation.setAccuracy(0);

        debugLog("create an androidLocationService for translate location");
        AndroidLocationService androidLocationService=new AndroidLocationService(mContext,-1,PROVIDER_NETWORK,this);
        return androidLocationService.toLocation(androidLocation);
    }

    /**
     * 用Baidu来处理真实坐标
     * @param latitude
     * @param longitude
     * @return
     */
    public Location realCoordsToLocationViaBD(double latitude, double longitude) {
        debugLog("create a BDLocationService for translate location");
        BDLocationService bdLocationService=new BDLocationService(mContext,-1,PROVIDER_NETWORK,this);
        return bdLocationService.realCoordsToLocationViaBD(latitude,longitude);
    }
   /**
     * removeLastListener in the active listener array. For debug activity.
     */
    public void removeLastListener(){
        if(!activeListeners.isEmpty())
            removeListener(activeListeners.get(activeListeners.size() - 1));
    }


    /**
     *
     * @param newServiceMode
     * 在百度/AndroidAPI服务间切换。切换后所有flags和已运行的监视器将消失。保留最后一次获取的位置。
     */
    void switchServiceMode(int newServiceMode) {
        if(newServiceMode==serviceMode){
            debugLog("Same location service, no need to switch");
            return;
        }
        debugLog("restart service with the new service mode");
        serviceMode=newServiceMode;
        stop();
        constructAPIService();
        resetReceivedFlag();
        start();
    }

    /**
     * 选用新的默认参数构造API定位服务
     */
    public void reconstructAPIService(int updateInterval, int serviceMode, boolean isAutoSwitchService,int providerChoice){
        stop();
        this.updateInterval=updateInterval;
        this.serviceMode=serviceMode;
        this.isAutoSwitchService=isAutoSwitchService;
        this.providerChoice=providerChoice;
        constructAPIService();
    }


}
