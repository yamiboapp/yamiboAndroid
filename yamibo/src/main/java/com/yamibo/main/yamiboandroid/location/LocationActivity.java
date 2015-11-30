package com.yamibo.main.yamiboandroid.location;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.ListView;
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
import com.yamibo.main.yamibolib.locationservice.model.Location;

import org.json.JSONArray;
import org.json.JSONObject;
//import org.json.simple.JSONArray;
//import org.json.simple.JSONObject;
//import org.json.simple.parser.JSONParser;


import java.util.ArrayList;
import java.util.List;

import static com.yamibo.main.yamibolib.locationservice.impl.util.debugShow;

/**
 * Created by Clover on 2015-11-22.
 * initiate and start the location service when creating the activity
 * TODO 每次初始化页面会自动上传新位置。是否需要隐私设置？
 * TODO read user gender info from profile
 * TODO 每次打开页面时的搜索偏好是否有记忆功能（存储于服务器）？还是初始化？
 * TODO 建议在什么页面里增加app重启定位服务（和网络服务）的选项
 */
//public class LocationActivity extends YMBActivity implements View.OnClickListener, RequestHandler<HttpRequest, HttpResponse>{
public class LocationActivity extends YMBActivity implements View.OnClickListener, RequestHandler<HttpRequest, HttpResponse> {
    private LocationService locationService;

    //debug模式下底层自动切换
    private String API_HTTP_ADDRESS = Environment.HTTP_ADDRESS;

    private int gender = 2;

    Location mLocation = null;

    TextView mInfo;

    private SwipeRefreshLayout swipeContainer = null;
    private NearbyAdapter customAdapter = null;
    private ListView list = null;
    private List<PersonItem> items = null;

    private HttpRequest mNearbyRequest;
    private HttpRequest updateReq = null;

    private static JSONObject respResult = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        gender = getGender();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        findViewById(R.id.btn_nearBoy).setOnClickListener(this);
        findViewById(R.id.btn_nearGirl).setOnClickListener(this);
        findViewById(R.id.btn_nearAll).setOnClickListener(this);

        mInfo = (TextView) findViewById(R.id.text_userInfo);
        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        list = (ListView) findViewById(R.id.listView_result);


        mInfo.setText("我的位置：");

        locationService = this.locationService();//IMPORTANT. the call will create a new service if not exist
        locationService.start();
        if (locationService.hasLocation()) {
            String message = null;
            mLocation = locationService.location();
            String streetAddress = "未知";
            if (mLocation.address() != null)
                streetAddress = mLocation.address();
            if (Environment.isDebug()) {
                message = "我的位置：" + streetAddress + " offlat:" + mLocation.offsetLatitude() + " offlon" + mLocation.offsetLongitude();
            } else
                message = "我的位置：" + streetAddress;
            debugShow("already have location " + message);
            mInfo.setText(message);
        }

        items = new ArrayList<PersonItem>();
        debugShow("initialize adapter");
        customAdapter = new NearbyAdapter(this, R.layout.person_item, items);
        list.setAdapter(customAdapter);

        try {
            debugShow("1st time fetch data");
            getNearby();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Your code to refresh the list here.
                // Make sure you call swipeContainer.setRefreshing(false)
                // once the network request has completed successfully.
                getNearby();
                debugShow("refresh done");
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

    }

    //fecth items from nearby API, push result to ListView
    private void getNearby() {
        if (mNearbyRequest != null) {
            httpService().abort(mNearbyRequest, this, true);
        }
        showProgressDialog(getString(R.string.loading));
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("module", "lbs"));
        params.add(new BasicNameValuePair("sousa", "get"));

        if (locationService.hasLocation()) {
            mLocation = locationService.location();
            params.add(new BasicNameValuePair("lat", Double.toString(mLocation.offsetLatitude())));
            params.add(new BasicNameValuePair("lon", Double.toString(mLocation.offsetLongitude())));
        }

        params.add(new BasicNameValuePair("page", Integer.toString(getPage())));

        mNearbyRequest = BasicHttpRequest.httpPost(API_HTTP_ADDRESS, params);
        debugShow("connecting to " + API_HTTP_ADDRESS + params);
        debugShow("mNearbyRequest " + mNearbyRequest.toString());
        httpService().exec(mNearbyRequest, this);
    }

    @Override
    public void onRequestFinish(HttpRequest req, HttpResponse resp) {
        debugShow("submitted request is " + req.toString());
        debugShow("response is " + resp.toString());

        if (mNearbyRequest == req) {
            dismissDialog();
            if (resp.result() instanceof JSONObject) {
                respResult = (JSONObject) resp.result();
                debugShow("response result is" + respResult.toString());

                // output results
                try {
                    JSONObject respVariables = (JSONObject) respResult.get("Variables");
                    //JSONObject notice=(JSONObject)respVariables.get("notice");
                    //JSONObject pageinfo=(JSONObject)respVariables.get("pageinfo");

                    items = JSONArrayToItems((JSONArray) respVariables.get("result"));


                    pushItemsToView();

                } catch (Exception e) {
                    e.printStackTrace();
                    showToast(getString(R.string.network_fail));
                }
            }
            mNearbyRequest = null;
        } else if (updateReq == req) {
            dismissDialog();
            int resultCode;
            try {
                if (resp.result() instanceof JSONObject) {
                    respResult = (JSONObject) resp.result();
                    JSONObject respVariable = (JSONObject) respResult.get("Variables");
                    resultCode = respVariable.getInt("result");
                    debugShow("resultCode is" + resultCode);
                    if (resultCode == 1) {
                        debugShow("update nearby list");
                        getNearby();
                    } else {
                        debugShow("result code " + resultCode + " unequal to 1");
                        showToast(getString(R.string.refresh_fail));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                showToast(getString(R.string.network_fail));
            }
            updateReq = null;
        }
    }

    @Override
    public void onRequestFailed(HttpRequest req, HttpResponse resp) {
        debugShow(resp.toString());
        if (mNearbyRequest == req) {
            dismissDialog();
            mNearbyRequest = null;
            showToast(getString(R.string.network_fail));
        } else if (updateReq == req) {
            dismissDialog();
            updateReq = null;
            showToast(getString(R.string.network_fail));
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_nearAll) {
            updateNearby(0);
        } else if (v.getId() == R.id.btn_nearBoy) {
            updateNearby(1);
        } else if (v.getId() == R.id.btn_nearGirl) {
            updateNearby(2);
        }
/*
        if(v.getId()==R.id.btn_redrawList){
            adapter.notifyDataSetChanged();
            resultList.invalidateViews();
        }
*/
    }

    void updateNearby(int flag) {
        //updateSetting
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("module", "lbs"));
        params.add(new BasicNameValuePair("sousa", "set"));
        params.add(new BasicNameValuePair("gender", Integer.toString(gender)));

        params.add(new BasicNameValuePair("flag", Integer.toString(flag)));
        updateReq = BasicHttpRequest.httpPost(API_HTTP_ADDRESS, params);
        debugShow("request url " + updateReq.toString());
        showProgressDialog(getString(R.string.loading));
        httpService().exec(updateReq, this);

        //fetch and update items view
        pushItemsToView();
    }

    //TODO need to read gender from account
    int getGender() {
        if (Environment.isDebug())
            return 2;//return female
        else {
            debugShow("gender not from profile");
            return 2;
        }
    }

    //push value of items to view
    void pushItemsToView() {
        debugShow("push items of size " + items.size() + " to ListView");
        customAdapter.clear();
        customAdapter.addAll(items);
        // Now we call setRefreshing(false) to signal refresh has finished
        swipeContainer.setRefreshing(false);
    }

    private List<PersonItem> JSONArrayToItems(JSONArray people) {
        debugShow("input people of size" + people.length());
        items = new ArrayList<PersonItem>();
        try {
            for (int k = 0; k < people.length(); k++) {
                JSONObject person = (JSONObject) people.get(k);
                //TODO need to update with API to fetch intro and logo
                //Object logo = R.drawable.noavatar_small;
                String intro = " ";
                //items.add(new PersonItem(logo, person.getString("username"), person.getString("distance"), intro));
                items.add(new PersonItem(person.getString("username"), person.getString("distance"), intro));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        debugShow("return items of size" + items.size());
        return items;
    }

    //TODO update page preference from user choice?
    int getPage() {
        if (Environment.isDebug())
            return 1;
        else
            return 0;
    }
}
