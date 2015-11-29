package com.yamibo.main.yamiboandroid.location;

import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

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
import org.json.JSONObject;
//import org.json.simple.JSONArray;
//import org.json.simple.JSONObject;
//import org.json.simple.parser.JSONParser;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.yamibo.main.yamibolib.locationservice.impl.util.debugLog;
import static com.yamibo.main.yamibolib.locationservice.impl.util.debugShow;

/**
 * Created by Clover on 2015-11-22.
 * initiate and start the location service when creating the activity
 * TODO read user gender info from profile; update user location info to server
 */
//public class LocationActivity extends YMBActivity implements View.OnClickListener, RequestHandler<HttpRequest, HttpResponse>{
public class LocationActivity extends YMBActivity implements View.OnClickListener, RequestHandler<HttpRequest, HttpResponse>{
    private LocationService locationService;
    private String API_HTTP_ADDRESS =Environment.HTTP_ADDRESS;




    private int gender=-1;
    private int DEBUG_GENDER=2;

    private static JSONObject respResult=null;
    //Button btnNearBoy,btnNearGirl, btnNearAll;
    TextView mInfo;
    ListView resultList=null;
    SimpleAdapter adapter=null;

    static JSONArray nearbyArray=null;

    //static JSONArray debugResult=null;

    private HttpRequest mNearbyRequest;
    HttpRequest updateReq=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if(Environment.IS_DEBUG_ENABLED) {
            API_HTTP_ADDRESS = Environment.DEBUG_API_HTTP_ADDRESS;
            //gender = DEBUG_GENDER;
        }
        else
            API_HTTP_ADDRESS=Environment.HTTP_ADDRESS;

        gender=getGender();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        findViewById(R.id.btn_nearBoy).setOnClickListener(this);
        findViewById(R.id.btn_nearGirl).setOnClickListener(this);
        findViewById(R.id.btn_nearAll).setOnClickListener(this);

        mInfo=(TextView)findViewById(R.id.text_userInfo);
        resultList=(ListView)findViewById(R.id.listView_result);

       mInfo.setText("我的位置：");

        locationService=this.locationService();//IMPORTANT. the call will create a new service if not exist
        locationService.start();
        if(locationService.hasLocation()&&Environment.IS_DEBUG_ENABLED) {
            debugShow("already have location " + locationService.address());
            mInfo.setText("我的位置：" + locationService.address());
        }


        try{
            getNearby();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void getNearby() {
        if (mNearbyRequest!= null) {
            httpService().abort(mNearbyRequest, this, true);
        }
        showProgressDialog(getString(R.string.loading));
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("module", "lbs"));
        params.add(new BasicNameValuePair("sousa", "get"));
        mNearbyRequest = BasicHttpRequest.httpPost(API_HTTP_ADDRESS, params);
        debugShow("connecting to " + API_HTTP_ADDRESS + params);
        debugShow("mNearbyRequest " + mNearbyRequest.toString());
        httpService().exec(mNearbyRequest, this);
    }

    @Override
    public void onRequestFinish(HttpRequest req, HttpResponse resp) {
        debugShow("submitted request is "+req.toString());
        debugShow("response is " + resp.toString());

        if (mNearbyRequest == req) {
            dismissDialog();
            if (resp.result() instanceof JSONObject) {
                respResult = (JSONObject) resp.result();
                debugShow("response result is" + respResult.toString());

                // output results
                try {
                    JSONObject respVariables=(JSONObject)respResult.get("Variables");
                    String Charset=(String)respResult.get("Charset");
                    JSONObject notice=(JSONObject)respVariables.get("notice");
                    JSONObject pageinfo=(JSONObject)respVariables.get("pageinfo");

                    nearbyArray=(JSONArray)respVariables.get("result");


                    //add more users to debug output
                    if(Environment.IS_DEBUG_ENABLED) {
                        JSONObject obj = new JSONObject();
                        obj.put("uid", "201511");
                        obj.put("username", "安达");
                        obj.put("distance", "1.2KM");
                        nearbyArray.put(obj);

                        obj = new JSONObject();
                        obj.put("uid", "201512");
                        obj.put("username", "岛村");
                        obj.put("distance", "1.3KM");

                        nearbyArray.put(obj);

                        for (int k = 0; k < 5; k++) {
                            obj = new JSONObject();
                            obj.put("uid", 201513 + k);
                            obj.put("username", "少女" + (char) ((int) ('A') + k));
                            obj.put("distance", (1.3 + k) + "KM");
                            nearbyArray.put(obj);
                        }
                    }

                    debugShow("Charset is "+Charset.toString());
                    debugShow("pageinfo is "+pageinfo.toString());
                    debugShow("Notice is "+notice.toString());
                    for(int k=0;k<nearbyArray.length();k++)
                        debugShow("user: "+nearbyArray.get(k));

                    //update list view
                    debugShow("update adapter");
                    List<Map<String, Object>> ls=getData(nearbyArray);
                    debugShow("getData of size"+ls.size()+" : "+ls.toString());
                    adapter = new SimpleAdapter(this,ls,R.layout.person_item, new String[]{"near_logo","near_id","near_distance","near_intro"},
                            new int[]{R.id.near_logo,R.id.near_id,R.id.near_distance,R.id.near_intro,});
                    resultList.setAdapter(adapter);

                } catch (Exception e) {
                    e.printStackTrace();
                    showToast(getString(R.string.network_fail));
                }
            }
            mNearbyRequest = null;
        }

        if (updateReq == req) {
            dismissDialog();
            int resultCode;
            try{
                if (resp.result() instanceof JSONObject) {
                    respResult = (JSONObject) resp.result();
                    JSONObject respVariable= (JSONObject)respResult.get("Variables");
                    resultCode=respVariable.getInt("result");
                    debugShow("resultCode is" + resultCode);
                    if (resultCode == 1) {
                        debugShow("update nearby list");
                        getNearby();
                    }
                    else{
                        debugShow("result code "+resultCode+" unequal to 1");
                        showToast("列表未更新");
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
                showToast(getString(R.string.network_fail));
            }
            updateReq=null;
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
        if (updateReq== req) {
            dismissDialog();
            updateReq = null;
            showToast(getString(R.string.network_fail));
        }
    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.btn_nearAll){
            updateNearby(0);
        }

        if(v.getId()==R.id.btn_nearBoy){
            updateNearby(1);
        }
        if(v.getId()==R.id.btn_nearGirl){
            updateNearby(2);
        }
/*
        if(v.getId()==R.id.btn_redrawList){
            adapter.notifyDataSetChanged();
            resultList.invalidateViews();
        }
*/
    }

    void updateNearby(int flag){
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("module", "lbs"));
        params.add(new BasicNameValuePair("sousa", "set"));
        params.add(new BasicNameValuePair("gender", Integer.toString(gender)));

        params.add(new BasicNameValuePair("flag",Integer.toString(flag)));
        updateReq=BasicHttpRequest.httpPost(API_HTTP_ADDRESS, params);
        debugShow("request url " + updateReq.toString());
        showProgressDialog(getString(R.string.loading));
        httpService().exec(updateReq, this);
    }

    void debugShowResult(JSONObject respResult){
        try{
            debugLog("print respResult");

            JSONObject variables=(JSONObject)respResult.get("Variables");
            JSONArray result=(JSONArray)variables.get("result");

            debugShow("Charset is "+respResult.get("Charset").toString());
            debugShow(variables.get("member_username").toString());
            for(int k=0;k<result.length();k++) {
                JSONObject obj=(JSONObject)result.get(k);
                debugShow(obj.getString("uid") + " " + obj.getString("username") + " " + obj.getString("distance"));
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private List<Map<String,Object>> getData(JSONArray nearbyArray){
        List<Map<String,Object>> list=new ArrayList<Map<String,Object>>();
        Map<String, Object> map=null;
        int num;
        try {
            num=nearbyArray.length();
            JSONObject obj=new JSONObject();
            for (int k = 0; k < num; k++) {
                obj=(JSONObject)nearbyArray.get(k);
                map = new HashMap<String, Object>();
                map.put("near_logo",R.drawable.noavatar_small);
                map.put("near_id",obj.get("username"));
                map.put("near_distance",obj.get("distance"));
                //TODO need to fetch nearby user intro
                map.put("near_intro","雪の季節");
                debugShow("update JSONObject number " + k);
                list.add(map);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return list;
    }

    //TODO need to read profile from account
    int getGender(){
        return DEBUG_GENDER;
    }
}
