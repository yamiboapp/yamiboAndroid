package com.yamibo.main.yamibolib.locationservice.impl;

import android.os.AsyncTask;

import com.yamibo.main.yamibolib.Utils.Log;
import com.yamibo.main.yamibolib.locationservice.model.Location;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;


/**
 * Created by Clover on 2015-06-13.
 */
public class util {
    /**
     * DEBUG_CODE, change the boolean flag to enable/disable Log.i message started with "DEBUG_"
     */
    static final boolean IS_DEBUG_ENABLED=true;

    private static final int READ_TIME_OUT = 1000;
    private static final int CONNECTION_TIME_OUT = 5000;

    private static String readAll(Reader rd){
        StringBuilder sb=new StringBuilder();
        int cp;
        try {
            while ((cp = rd.read()) != -1) {
                sb.append((char) cp);
            }
        }
        catch (IOException e){
            debugLog("error readingBuber" + e.toString());
            return null;
        }
        return sb.toString();
    }

    /**
     * @param url
     * @return
     * if read json from url in android main thread will cause error
     */
    public JSONObject readJsonFromUrl(String url) {
        JSONObject result=null;
        UrlJsonReader reader=new UrlJsonReader();
        try {
            result=reader.execute(url).get();
        } catch (Exception e) {
            debugLog("Exception in read"+e.toString());
            e.printStackTrace();
        }
        return result;
    }


    /**
     * @param latitude 维度
     * @param longitude 经度
     * @return
     * 返回百度偏转坐标，格式为JSONObject {"offsetLatitude":29.5, "offsetLongtitude":114.1} <br>
     * 详见百度坐标转换API文档示例 http://developer.baidu.com/map/index.php?title=webapi/guide/changeposition
     */
    public JSONObject convertToBDCoord(double latitude, double longitude) {
        final String BD_APP_KEY = "VFfjj9gziQzqzF9iEvulSewx";
        final String BD_APP_SECURITY_CODE ="2C:E0:AF:82:2B:07:B0:7D:13:2B:AE:EB:1A:20:D5:D0:BD:F7:FD:5F;com.yamibo.main.yamibolib";
        /**
         * et: http://api.map.baidu.com/geoconv/v1/?coords=116.3833,39.9167&from=1&to=5&ak=VFfjj9gziQzqzF9iEvulSewx&mcode=2C:E0:AF:82:2B:07:B0:7D:13:2B:AE:EB:1A:20:D5:D0:BD:F7:FD:5F;com.yamibo.main.yamibolib
         */
        String url="http://api.map.baidu.com/geoconv/v1/?coords="+longitude+","+latitude
                +"&from=1&to=5&ak="+BD_APP_KEY +"&mcode="+ BD_APP_SECURITY_CODE;
        try {
            JSONObject obj=readJsonFromUrl(url);
            int status=(int)obj.get("status");
            JSONArray array=obj.getJSONArray("result");
            if(status==0) {
                List<JSONObject> coords = new ArrayList<JSONObject>();
                for (int i = 0; i < array.length(); i++) {
                    System.out.println(array.getJSONObject(i).toString());
                    coords.add(array.getJSONObject(i));
                }
                double offsetLatitude=(double)coords.get(0).get("y");
                double offsetLongitude=(double)coords.get(0).get("x");
                String str="{\"offsetLatitude\":"+offsetLatitude+",\"offsetLongitude\":"+offsetLongitude+"}";
                return new JSONObject(str);
            }
            else{
                debugLog("BD API coordinate convert error status code"+ status);
                return null;
            }
        } catch (JSONException e) {
            debugLog("error reading Json "+e.toString());
            return null;
        }
    }


    static void debugLog(String Message) {
        if (IS_DEBUG_ENABLED)
            Log.i("DefaultLocationSerivce", "DEBUG_" + Message);
    }

    /**
     * TODO remark: defaultLocale seems not work well with mTime.
     * @param location
     * @return
     */
    public static String locationToDebugString(Location location){
        StringBuilder sb=new StringBuilder("Location debugInfo: ");
        if(location!=null) {
            DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault());
            String dateOut=dateFormatter.format(new Date(location.getTime()));
            sb.append("accuracy=" + location.accuracy() + " mtime=" + location.getTime()+" defaultLocaleDate="+dateOut
                    + "\nlatitude,longtitude=" + location.latitude() + "," + location.longitude()
                    + "\noffset lat,long=" + location.offsetLatitude() + "," + location.offsetLongitude()
                    +"\nisInChina="+(location.getRegion()==Location.IN_CN));
            if(location.city()!=null)
                sb.append(" city="+ location.city().toString());
            sb.append("\naddress="+location.address());
        }
        return sb.toString();
    }

    class UrlJsonReader extends AsyncTask<String, Void,JSONObject >{
        @Override
        protected JSONObject doInBackground(String... params) {
            try {
                URLConnection conn=(new URL(params[0])).openConnection();
                conn.setConnectTimeout(CONNECTION_TIME_OUT);
                conn.setReadTimeout(READ_TIME_OUT);
                InputStream is=conn.getInputStream();
//            InputStream is = new URL(url).openStream();
                try {
                    BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
                    String jsonText = readAll(rd);
                    JSONObject json = new JSONObject(jsonText);
                    return json;
                } finally {
                    is.close();
                }
            } catch (Exception e) {
                debugLog("error reading url "+e.toString());
                return null;
            }
        }
    }
}

