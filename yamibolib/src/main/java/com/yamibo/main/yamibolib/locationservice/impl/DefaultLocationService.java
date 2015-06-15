package com.yamibo.main.yamibolib.locationservice.impl;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;

import com.baidu.location.BDLocation;
import com.baidu.location.LocationClientOption;
import com.yamibo.main.yamibolib.Utils.Log;
import com.yamibo.main.yamibolib.locationservice.LocationListener;
import com.yamibo.main.yamibolib.locationservice.LocationService;
import com.yamibo.main.yamibolib.locationservice.model.City;
import com.yamibo.main.yamibolib.locationservice.model.Location;
import com.yamibo.main.yamibolib.model.GPSCoordinate;

import java.util.ArrayList;
import java.util.List;

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
 * 已封装：帮助类BDLocationService和AndroidLocationService会建立一个相应的API定位监听器。
 * <p/>
 * 以下方法更改已注册的所有监听器的参数，并将作为下一次的监听器参数: <br>
 * resetServiceOption(update_interval,provider);<br>
 * 注解：百度定位模式下只能设置统一的监听参数。因此对AndroidAPI也作此简化处理。
 *TODO user:若想使用GPS和网络混合provider并选择最优结果，推荐建立两个DefaultLocationService实例，分别监听网络和GPS并比较结果的精度：<br>
 *单监听器模式下，AndroidAPI默认的Best模式会变成监听GPS，导致长时间无为之结果。<br>
 *百度的单监听器在混合模式下，会将GPS和网络的结果混在一起返回，缺少文档说明。<br>
 *
 */
public class DefaultLocationService implements LocationService {



    private Context mContext;

    /**
     * 监听器队列
     */
    private List<LocationListener> activeListeners = new ArrayList<>();

    /**
     * 用于实例化 百度 BDLocationClient 或 Android locationManager
     */
    private APILocationService apiLocationService = null;

    /**
     * 任意定位服务取得的上次程序定位的结果
     * TODO user:  add methods storing and reading lastKnownLocation
     */
    private Location lastKnownLocation = null;
    /**
     * 默认的serviceMode为百度定位（适用中国）或AndroidAPI定位（适用中国之外）
     */
    private int serviceMode=BAIDU_MODE;
    // private int serviceMode=ANDROID_API_MODE;
    /**
     * 是否允许程序根据定位结果自动选择定位服务
     */
    private boolean isAutoSwitchService =false;




    /**
     * 当更新时间小于1000ms时，为单次更新
     */
    private int updateInterval =5000;

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
     * to be read by the textView for shown to mobile activity
     */
    public String debugMessage = null;

    //Baidu service
    // client and listener are in the BDLocationApplication's member field
    private BDLocationApplication mBDLocationApplication = null;
    private BDLocation mBDlocationResult = null;


    /**
     * DEBUG_CODE, change the boolean flag to enable/disable Log.i message started with "DEBUG_"
     */
    private static final boolean IS_DEBUG_ENABLED = true;

    private List<LocationListener> mListeners = new ArrayList<>();


    /**
     * Clover:
     * locationClient and Listener instantiated
     * link onReceived callback
     * listener not registered! service not started! use start();
     *
     * @param context
     */
    public DefaultLocationService(Context context) {
        mContext = context;
        mBDLocationApplication = new BDLocationApplication(mContext);
        mBDLocationApplication.targetService = this;
    }

    @Override
    public int status() {
        int mStatus;
        if (mBDLocationApplication == null)
            return LocationService.STATUS_FAIL;
        if (mBDLocationApplication.isLocationReceived)
            mStatus = LocationService.STATUS_LOCATED;
        else {
            if (mBDLocationApplication.isLocationDemand)
                mStatus = LocationService.STATUS_TRYING;
            else
                mStatus = LocationService.STATUS_FAIL;
        }
        return mStatus;
    }

    @Override
    public boolean hasLocation() {
        if (mBDlocationResult != null)
            return true;
        return false;
    }

    @Override
    //TODO
    public Location location() {
        return null;
    }

    @Override
    //TODO
    public GPSCoordinate realCoordinate() {
        return null;
    }

    @Override
    //TODO
    public GPSCoordinate offsetCoordinate() {
        return null;
    }

    @Override
    public String address() {
        if (hasLocation())
            return (mBDlocationResult.getAddrStr());
        return null;
    }

    @Override
    //TODO
    public City city() {
        return null;
    }

    @Override
    /**
     * Clover:
     *
     * register listener, init option, start service, requestLocation
     */
    public boolean start() {
        if (mBDLocationApplication == null) {
            return false;
        }
        if (isLocationEnabled(mContext)) {
            mBDLocationApplication.addListener();
            mBDLocationApplication.initLocation();
            mBDLocationApplication.start();
            mBDLocationApplication.requestLocation();

            debugLog("location service starts");
            debugShow("location service starts");
            return true;
        } else {
            return false;
        }
    }


    @Override
    /**
     * Clover:
     * unregister listener and stop client
     * in Baidu service sample, listener is not removed when client stops?
     */
    public void stop() {
        if (mBDLocationApplication == null)
            return;

        //reset flags
        mBDLocationApplication.resetFlag();
        mBDLocationApplication.removeListener();
        mBDLocationApplication.stop();

        debugLog("location service stops");
    }

    @Override
    /**
     * Clover
     * requestLocation (asynchronous)
     * return true if location demand has been sent
     */
    public boolean refresh() {
        if (mBDLocationApplication == null)
            return false;
        mBDLocationApplication.requestLocation();
        return mBDLocationApplication.isLocationDemand;
    }

    /**
     * @param timeMS 设置发起定位请求的间隔时间为>=1000 (ms) 时为循环更新
     *               default value -1 means no automatic update.
     *               to TEST: 热切换
     */
    public void newUpdateTime(int timeMS) {
        mBDLocationApplication.setSpan(timeMS);
        mBDLocationApplication.initLocation();
    }

    /**
     * @param isNeedAddress to TEST: 热切换
     */
    public void newAddressAppearance(boolean isNeedAddress) {
        mBDLocationApplication.setIsNeedAddress(isNeedAddress);
        mBDLocationApplication.initLocation();
    }

    /**
     * @param input LocationClientOption.LocationMode.Hight_Accuracy 高精度模式
     *              , Battery_Saving 低功耗模式
     *              , Device_Sensors 仅设备(Gps)模式
     *              热切换 in demo sample
     */
    public void newLocationMode(LocationClientOption.LocationMode input) {
        mBDLocationApplication.setLocationMode(input);
        mBDLocationApplication.initLocation();
    }

    /**
     * @param input choose "gcj02","bd09ll","bd09"
     * @return 热切换 in demo
     */
    public void newCoordMode(String input) {
        mBDLocationApplication.setCoordMode(input);
    }

    @Override
    /**
     * NEED to be changed: LocationListener is not a parameter for BD listener servive;
     * not used here
     * maybe overload with no parameter?
     */
    public void addListener(LocationListener listener) {
        if (listener != null && !mListeners.contains(listener)) {
            mListeners.add(listener);
        }
    }


    /**
     * NEED to be changed: LocationListener is not a parameter for BD listener servive;
     * not used here
     * maybe overload with no parameter?
     */
    @Override
    public void removeListener(LocationListener listener) {
        mListeners.remove(listener);
    }


    @Override
    //TODO
    public void selectCoordinate(int type, GPSCoordinate coord) {

    }


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
     * this message can be shown in the debut_text field on mobile by click the debug_button
     *
     * @param Message
     */
    private void debugShow(String Message) {
        if (IS_DEBUG_ENABLED)
            debugMessage = Message;
    }

    private void debugLog(String Message) {
        if (IS_DEBUG_ENABLED)
            Log.i("DefaultLocationSerivce", "DEBUG_" + Message);
    }

    /**
     * to be called by BDLocationListener's onReceive
     * when received, update mBDlocationResult
     */
    public void onReceiveBDLocation(BDLocation locationResult) {
        if (mBDLocationApplication == null)
            return;
        this.mBDlocationResult = locationResult;
        // TODO
        // 初始化这里LocationServier所有的变量,包括locaion,city,等等等等
        for (LocationListener listener : mListeners) {
            listener.onLocationChanged(this);
        }

        debugLog("LocationService receive location from BDLocation");
        debugShow(BDLocationApplication.toStringOutput(mBDlocationResult));
    }

    public void onReceiveLocation(Location locationResult) {
        debugLog("code to be updated");
    }
}
