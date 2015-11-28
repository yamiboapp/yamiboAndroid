package com.yamibo.main.yamiboandroid.location;

import android.os.Bundle;
import android.view.View;

import com.yamibo.main.yamiboandroid.R;
import com.yamibo.main.yamibolib.Utils.BasicNameValuePair;
import com.yamibo.main.yamibolib.Utils.Environment;
import com.yamibo.main.yamibolib.Utils.NameValuePair;
import com.yamibo.main.yamibolib.app.YMBActivity;
import com.yamibo.main.yamibolib.dataservice.RequestHandler;
import com.yamibo.main.yamibolib.dataservice.http.HttpRequest;
import com.yamibo.main.yamibolib.dataservice.http.HttpResponse;
import com.yamibo.main.yamibolib.dataservice.http.impl.BasicHttpRequest;
import com.yamibo.main.yamibolib.locationservice.LocationService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
//import org.json.simple.JSONArray;
//import org.json.simple.JSONObject;
//import org.json.simple.parser.JSONParser;


import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import static com.yamibo.main.yamibolib.locationservice.impl.util.debugShow;

/**
 * Created by Clover on 2015-11-22.
 * initiate and start the location service when creating the activity
 */
public class LocationActivity extends YMBActivity implements View.OnClickListener, RequestHandler<HttpRequest, HttpResponse>{
    private LocationService locationService;
    private String API_HTTP_ADDRESS =Environment.HTTP_ADDRESS;//TODO: should be updated
    private String DEBUG_API_HTTP_ADDRESS ="http://ceshi.yamibo.com/chobits/index.php?";



    private HttpRequest mNearbyRequest;



    //private static final String debug_filePath = "C:\\SYNC\\workspace(git)\\androidProj\\yamiboAndroid\\debug_nearbyResp.json";
    //private static final String debug_filePath = "debug_nearbyResp.json";
    //private static final String debug_filePath = "D:\\nearby.json";
    private static final String debug_url = "https://github.com/clovercodemk/yamiboAndroid/blob/master/yamibo/debug/debug_nearbyResp.json";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if(Environment.IS_DEBUG_ENABLED)
            API_HTTP_ADDRESS=DEBUG_API_HTTP_ADDRESS;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        locationService=this.locationService();//IMPORTANT. the call will create a new service if not exist
        locationService.start();
        if(locationService.hasLocation()&&Environment.IS_DEBUG_ENABLED)
            debugShow("already have location " + locationService.address());

        //            List<NameValuePair> params = new ArrayList<>();
//            params.add(new BasicNameValuePair("module", "mypm"));
//            httpService().exec(BasicHttpRequest.httpPost(Environment.API_HTTP_ADDRESS, params), new RequestHandler<HttpRequest, HttpResponse>() {
//                @Override
//                public void onRequestFinish(HttpRequest req, HttpResponse resp) {
//                    debugShow(resp.toString());
//                }
//
//                @Override
//                public void onRequestFailed(HttpRequest req, HttpResponse resp) {
//                    debugShow(resp.toString());
//                }
//            });
        getNearby();
    }

    private void getNearby() {
        if (mNearbyRequest!= null) {
            httpService().abort(mNearbyRequest, this, true);
        }
        showProgressDialog(getString(R.string.loading));
        List<NameValuePair> params = new ArrayList<>();
        if(Environment.IS_DEBUG_ENABLED){
            mNearbyRequest=BasicHttpRequest.httpPost(debug_url,params);
            debugShow("connecting to "+debug_url+params);
        }
        else {
            params.add(new BasicNameValuePair("module", "lbs"));
            params.add(new BasicNameValuePair("sousa", "get"));
            mNearbyRequest = BasicHttpRequest.httpPost(API_HTTP_ADDRESS, params);
            debugShow("connecting to "+API_HTTP_ADDRESS+params);
        }
        debugShow("mNearbyRequest "+mNearbyRequest.toString());
        httpService().exec(mNearbyRequest, this);
    }

    /*
    public void debugShowNearbyResponse(){
        debugShow("use debug JSON object");
        if(Environment.IS_DEBUG_ENABLED)
        try {
            FileReader reader = new FileReader(debug_filePath);
            JSONParser jsonParser = new JSONParser();
            JSONObject jsonObject = (JSONObject) jsonParser.parse(reader);
            JSONObject jsonVariables=(JSONObject)jsonObject.get("Variables");

            debugShow(jsonObject.toString());

            String Charset=(String)jsonObject.get("Charset");
            JSONObject notice=(JSONObject)jsonVariables.get("notice");
            JSONObject pageinfo=(JSONObject)jsonVariables.get("pageinfo");
            JSONArray nearbyArray=(JSONArray)jsonVariables.get("result");
            debugShow("Charset is "+Charset.toString());
            debugShow("pageinfo is "+pageinfo.toString());
            debugShow("Notice is "+notice.toString());
            for(int k=0;k<nearbyArray.size();k++)
                debugShow("user: "+nearbyArray.get(k));

        } catch (Exception  e) {
            e.printStackTrace();
        }

    }*/

    @Override
    public void onRequestFinish(HttpRequest req, HttpResponse resp) {
        debugShow("response is " + resp.toString());
        debugShow("mNearByRequest is "+mNearbyRequest);
        if (mNearbyRequest == req) {
            dismissDialog();
            if (resp.result() instanceof JSONObject) {
                JSONObject respResult = (JSONObject) resp.result();
                debugShow("response result is" + respResult.toString());
                try {
                    JSONObject respVariables=(JSONObject)respResult.get("variables");


                    String Charset=(String)respResult.get("Charset");
                    JSONObject notice=(JSONObject)respVariables.get("notice");
                    JSONObject pageinfo=(JSONObject)respVariables.get("pageinfo");
                    JSONArray nearbyArray=(JSONArray)respVariables.get("result");
                    debugShow("Charset is "+Charset.toString());
                    debugShow("pageinfo is "+pageinfo.toString());
                    debugShow("Notice is "+notice.toString());
                    for(int k=0;k<nearbyArray.length();k++)
                        debugShow("user: "+nearbyArray.get(k));
                } catch (Exception e) {
                    e.printStackTrace();
                    showToast(getString(R.string.network_fail));
                }
            }
            mNearbyRequest = null;
        }
    }

    @Override
    public void onRequestFailed(HttpRequest req, HttpResponse resp) {
        debugShow("debug_" + resp.toString());
        if (mNearbyRequest == req) {
            dismissDialog();
            mNearbyRequest = null;
            showToast(getString(R.string.network_fail));
        }
    }

    @Override
    public void onClick(View v) {
       // if (v.getId() == R.id.btn_debug_location) {
         //   startActivity("ymb://debugLocation");
//            throw new RuntimeException("test");
//            new Thread() {
//                @Override
//                public void run() {
//                    List<NameValuePair> params = new ArrayList<>();
//                    params.add(new BasicNameValuePair("module", "mypm"));
//                    HttpResponse response = httpService().execSync(BasicHttpRequest.httpPost(Environment.API_HTTP_ADDRESS, params));
//                    debugShow(response.toString());
//                }
//            }.start();

      //  }
    }
}
