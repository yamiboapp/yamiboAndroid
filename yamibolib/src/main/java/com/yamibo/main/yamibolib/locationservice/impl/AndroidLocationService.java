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
import java.util.HashMap;
import java.util.Iterator;
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
        if(locationManager==null||androidListener==null)
            return false;
        unregisterListener(androidListener);
        registerListener(androidListener);
        return true;//no more info avaialbe from registerListener
    }


    /**
     * flag isLocationDemand and isStarted reset to false, clear listeners
     */
    @Override
    public void stop() {
        if (locationManager == null)
            return;

/** this iteration will cause error as KeySet is changing in the loop?
 for (LocationListener listener: mapListeners.keySet())
 removeListener(listener);
 */

        try{
            for(AndroidListener androidListener:mapListeners.values())
                locationManager.removeUpdates(androidListener);
        }
        catch (Exception e){
            debugLog("error remove all android listener updates "+e.toString());
        }finally {
            mapListeners.clear();
            isStarted=false;
            resetProgressFlag();
            debugLog("location service stops");
        }
    }

    @Override
    public boolean refresh() {
        if (locationManager == null)
            return false;
        boolean state=false;
        for(AndroidListener androidListener:mapListeners.values()) {
            state=(state || requestLocation(androidListener));
        }
        isLocationReceived=false;
        return state;
    }

    @Override
    public void addListener(LocationListener listener) {
        if (listener != null && !mapListeners.containsKey(listener)) {
            AndroidListener androidListener=new AndroidListener(listener,this);
            androidListener.debugMessage="android Listener (added to android listeners' set of size "+mapListeners.values().size()+")";
            debugLog("register "+androidListener.debugMessage);
            registerListener(androidListener);
            mapListeners.put(listener, androidListener);
        }
        debugLog("\nsize " +mapListeners.keySet().size()+" keySet: "+mapListeners.keySet().toString()
                +"size " +mapListeners.values().size()+" values: "+mapListeners.values().toString());
    }

    @Override
    public void removeListener(LocationListener listener) {
        if (listener != null && mapListeners.containsKey(listener)) {
            unregisterListener(mapListeners.get(listener));
            //remove entries (should exist one) associated with the given listener
            for(Iterator<Map.Entry<LocationListener, AndroidListener>> it = mapListeners.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<LocationListener, AndroidListener> entry = it.next();
                if (entry.getKey().equals(listener)) {
                    it.remove();
                }
            }
        }
        debugLog("\nsize " + mapListeners.keySet().size() + " keySet: " + mapListeners.keySet().toString()
                + "size " + mapListeners.values().size() + " values: " + mapListeners.values().toString());
    }

    @Override
    public void resetAPIServiceOption(int updateInterval, int providerChoice) {
        setUpdateInterval(updateInterval);
        setProvider(providerChoice);
        refresh();
    }

    /**
     *
     * @return Android case, test if client exits (no need to explicitly start it)
     */
    @Override
    public boolean isClientStarted() {
        return (locationManager!=null);
    }


    /**
     * request location when register listener (set flag)
     * @param androidListener
     */
    void registerListener(AndroidListener androidListener) {
        if(locationManager==null){
            debugLog("locationManager is null");
            return;
        }
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
        isLocationDemand = true;
        debugLog("Android autoRequest sent with provider: "+provider);
    }


    void unregisterListener(AndroidListener androidListener) {
        if(locationManager==null){
            debugLog("locationManager is null");
            return;
        }
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

    /**
     * 此处使用Android的gCoder查询坐标的地理反编译。由网络条件限制可能较慢
     */
    public Location toLocation(android.location.Location source) {

        double latitude=source.getLatitude();
        double longitude=source.getLongitude();
        double offsetLatitude=latitude;
        double offsetLongitude=longitude;
        String address=null;
        City city=null;
        try {
            List<Address> addresses=gCoder.getFromLocation(latitude,longitude,1);
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
            debugLog("error gcoder "+ e.toString());
        }
        int accuracy=(int)source.getAccuracy();
        int isInCN;
        long mTime = source.getTime();
        if(isInChinaViaAndroid(source)) {
            isInCN = Location.IN_CN;
            //always convert the coord from Android API when in CN
            try {
                JSONObject bdCoord = new util().convertToBDCoord(latitude, longitude);
                if(bdCoord!=null){
                    offsetLatitude = (double) bdCoord.get("offsetLatitude");
                    offsetLongitude = (double) bdCoord.get("offsetLongitude");
                }
                else
                    debugLog("null JSONObject when call convertToBDCoord(latitude, longitude)");

            } catch (Exception e) {
                debugLog("error converting coords " + e.toString());  }
        }
        else
            isInCN= Location.NOT_IN_CN;

        Location Location =new Location
                (latitude,longitude,offsetLatitude,offsetLongitude,address,city,accuracy,isInCN,mTime);
        return Location;
    }

    public boolean isInChinaViaAndroid(android.location.Location location) {
        try {
            List<Address> addresses=gCoder.getFromLocation(location.getLatitude(),location.getLongitude(),1);
            if(addresses!=null&addresses.size()>0){
                String country=addresses.get(0).getCountryName();
                debugLog("country is "+country);
                return country.equals("China");
            }
        } catch (IOException e) {
            debugLog("EROOR: "+e.toString());
        }
        return true;
    }

    private void resetProgressFlag() {
        isLocationDemand=false;
        isLocationReceived=false;
    }

    void onReceiveLocation() {
        isLocationReceived=true;
        debugLog("Android service onReceiveLocation. Unregister all Android listener whose isAutoRequestUpdate=false");
        for (AndroidListener androidListener : mapListeners.values())
            if (!androidListener.isAutoRequestUpdate)
                unregisterListener(androidListener);
    }
}
