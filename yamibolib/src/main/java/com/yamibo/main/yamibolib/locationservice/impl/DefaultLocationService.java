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
 * Created by wangxiaoyan on 15/5/25.
 * Clover: implemented on 01/06/25, use BDLocationApplication class based on Baidu sample, member variables are added correspondingly
 */
public class DefaultLocationService implements LocationService {

    private Context mContext;


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
}
