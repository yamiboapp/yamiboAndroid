package com.yamibo.main.yamibolib.locationservice.impl;

import android.content.Context;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.LocationManager;

import com.yamibo.main.yamibolib.Utils.Log;
import com.yamibo.main.yamibolib.locationservice.LocationListener;
import com.yamibo.main.yamibolib.locationservice.LocationService;
import com.yamibo.main.yamibolib.locationservice.model.City;
import com.yamibo.main.yamibolib.locationservice.model.Location;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.yamibo.main.yamibolib.locationservice.impl.util.debugLog;


/**
 * Created by Clover on 2015-06-03.
 * helper Class to control AndroidManager
 * 将LocationListener 翻译为apiListener
 */
class AndroidLocationService implements APILocationService {
    DefaultLocationService supervisorService=null;

    private Context mContext;

    /**
     * NO USE
     */
    private Location lastKnownLocation = null;


    private int providerChoice=PROVIDER_NETWORK;
    private boolean isStarted=false;




    public static final int PROVIDER_BEST=0;
    public static final int PROVIDER_NETWORK=1;
    public static final int PROVIDER_GPS=2;



    /**
     * 为每个LocationListener提供一个相应的API location listener
     */
    Map<LocationListener, AndroidListener> mapListeners=new HashMap<LocationListener, AndroidListener>();


    //Use the following two flags to track the working status of location application
    private boolean isLocationDemand=false;
    private boolean isLocationReceived=false;

    private LocationManager locationManager;
    private Geocoder gCoder;

    private static String provider_best =null;
    private static String provider_network =null;
    private static String provider_gps =null;
    private static int MIN_DISTANCE= 0;
    private String provider;
    /**
     * when this field is inferior than 1000, listener will be made with interval 1000
     * and noAutoRequestUpdate
     */
    private int updateInterval=3000;


    public AndroidLocationService(Context mContext,
                                  int updateInterval, int providerChoice,
                                  DefaultLocationService supervisorService){
        this.supervisorService =supervisorService;
        locationManager=(LocationManager)mContext.getSystemService(Context.LOCATION_SERVICE);
        provider_best =locationManager.getBestProvider(new Criteria(),true);
        provider_network =locationManager.NETWORK_PROVIDER;
        provider_gps =locationManager.GPS_PROVIDER;
        setUpdateInterval(updateInterval);
        setProvider(providerChoice);
        gCoder = new Geocoder(mContext);
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
    @Override
    public boolean start() {
        if (locationManager== null||mapListeners.isEmpty()) {
            return false;
        }
        if(isStarted){
            debugLog("already in work");
            return true;
        }
        for(AndroidListener listener:mapListeners.values())
            registerListener(listener);
        isStarted=true;
        debugLog("location service starts");
        return true;
    }



    boolean requestLocation(AndroidListener androidListener){
        unregisterListener(androidListener);
        registerListener(androidListener);
        return true;//no more info avaialbe from registerListener
    }


    /**
     * flag isLocationDemand and isStarted reset to false
     */
    @Override
    public void stop() {
        if (locationManager == null)
            return;

        for (LocationListener listener: mapListeners.keySet())
            removeListener(listener);
        isStarted=false;
        resetProgressFlag();
        debugLog("location service stops");
    }

    @Override
    public boolean refresh() {
        if (locationManager == null)
            return false;
        boolean state=false;
        for(AndroidListener androidListener:mapListeners.values()) {
            state=(state || requestLocation(androidListener));
            if(state)
                break;
        }
        isLocationReceived=false;
        return state;
    }

    @Override
    public void addListener(LocationListener listener) {
        if (listener != null && !mapListeners.containsKey(listener)) {
            AndroidListener androidListener=new AndroidListener(listener,this);
            androidListener.debugMessage="register android Listener which is added to android listener array of size "+mapListeners.size();
            registerListener(androidListener);
            mapListeners.put(listener, androidListener);
        }
    }

    @Override
    public void removeListener(LocationListener listener) {
        if (listener != null && mapListeners.containsKey(listener)) {
            unregisterListener(mapListeners.get(listener));
            mapListeners.remove(listener);
        }
    }

    @Override
    public void resetServiceOption(int updateInterval, int providerChoice) {
        setUpdateInterval(updateInterval);
        setProvider(providerChoice);
        refresh();
    }

    @Override
    public boolean isClientStarted() {
        return false;
    }


    /**
     * request location when register listener (set flag)
     * @param androidListener
     */
    void registerListener(AndroidListener androidListener) {
        //check type is DefaultLoctionListner, if not, need a translation
        int tempInt;
        if (updateInterval<1000) {
            tempInt = 1000;
            androidListener.isAutoRequestUpdate=false;
        }
        else {
            tempInt=updateInterval;
            androidListener.isAutoRequestUpdate=true;
        }
        locationManager.requestLocationUpdates
                (provider, tempInt, MIN_DISTANCE, androidListener);
        isLocationDemand = true;//TODO TEST:check if the above functions normally
        debugLog("Android autoRequest sent with provider: "+provider);
    }


    void unregisterListener(AndroidListener androidListener) {
        //check type is DefaultLocationListener, if not, need a translation
        locationManager.removeUpdates(androidListener);
        debugLog("Android listener removed");
    }

    @Override
    public void setUpdateInterval(int updateInterval) {
        this.updateInterval =updateInterval;
    }

    @Override
    public void setProvider(int providerChoice) {
        switch (providerChoice){
            case DefaultLocationService.PROVIDER_BEST:
                provider= provider_best;
                break;
            case DefaultLocationService.PROVIDER_NETWORK:
                provider= provider_network;
                break;
            case DefaultLocationService.PROVIDER_GPS:
                provider= provider_gps;
                break;
            default:
                debugLog("provider not supported!");
                return;
        }
    }


    public Location toLocation(android.location.Location source) {

        double latitude=source.getLatitude();
        double longtitude=source.getLongitude();
        double offsetLatitude=latitude;
        double offsetLongtitude=longtitude;
        String address=null;
        City city=null;
        try {
            List<Address> addresses=gCoder.getFromLocation(latitude,longtitude,1);
            if(addresses!=null&&addresses.size()>0){
                Address returnedAddress=addresses.get(0);
                String strCity = returnedAddress.getLocality();
//                String state = returnedAddress.getAdminArea();
//                String country =returnedAddress.getCountryName();
//                String postalCode =returnedAddress.getPostalCode();
//                String knownName = returnedAddress.getFeatureName(); // Only if available else return NULL

                StringBuilder strReturnedAddress=new StringBuilder("");
                for (int i = 0; i < returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                }
                address= strReturnedAddress.toString();
                Log.w("My Current loction address", "" + address);
                city=new City(strCity);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        int accuracy=(int)source.getAccuracy();
        int isInCN;
        long mTime = source.getTime();
        if(isInChina(source)) {
            isInCN = Location.IN_CN;
            //always convert the coord from Android API when in CN
            try {
                JSONObject bdCoord = util.convertToBDCoord(latitude, longtitude);
                offsetLatitude = (double) bdCoord.get("offsetLatitude");
                offsetLatitude = (double) bdCoord.get("offsetLongitude");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else
            isInCN= Location.NOT_IN_CN;

        Location Location =new Location
                (latitude,longtitude,offsetLatitude,offsetLongtitude,address,city,accuracy,isInCN,mTime);
        return Location;
    }

    public boolean isInChina(android.location.Location location) {
        try {
            List<Address> addresses=gCoder.getFromLocation(location.getLatitude(),location.getLongitude(),1);
            if(addresses!=null&addresses.size()>0){
                String country=addresses.get(0).getCountryName();
                debugLog("country is "+country);
                return country.equals("China");
            }
        } catch (IOException e) {
            debugLog("EROOR: "+e);
        }
        return true;
    }

    private void resetProgressFlag() {
        isLocationDemand=false;
        isLocationReceived=false;
    }

    void onReceiveLocation() {
        isLocationReceived=true;
        for (AndroidListener androidListener : mapListeners.values())
            if (!androidListener.isAutoRequestUpdate)
                unregisterListener(androidListener);
    }
}
