package com.yamibo.main.yamiboandroid.main;

import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
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
import com.yamibo.main.yamibolib.widget.ThreeTabView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends YMBActivity implements ThreeTabView.TabChangeListener, RequestHandler<HttpRequest, HttpResponse> {

    private HttpRequest mHotRequest;
    private HttpRequest mMainForumRequest;
    private ListView mList;
    private BaseAdapter mAdapter;
    private BaseBannerView mBannerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        NavigationDrawerFragment navigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        navigationDrawerFragment.setUp(R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        getTitleBar().setLeftView(R.drawable.btn_main_menu, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    drawerLayout.openDrawer(GravityCompat.START);
                }
            }
        });
        ThreeTabView tabBar = (ThreeTabView) LayoutInflater.from(this).inflate(R.layout.three_tab_view,
                null);
        tabBar.setTabChangeListener(this);
        tabBar.setLeftTitleText(getString(R.string.tab_hot));
        tabBar.setRightTitleText(getString(R.string.tab_all));
        super.getTitleBar().setCustomContentView(tabBar);
        tabBar.setCurIndex(0);

        mList = (ListView) findViewById(R.id.list);
        mBannerView = new BaseBannerView(this);
        mAdapter = new BaseAdapter() {


            @Override
            public int getCount() {
                return 1;
            }

            @Override
            public Object getItem(int position) {
                return null;
            }

            @Override
            public long getItemId(int position) {
                return 0;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView textView = new TextView(MainActivity.this);
                textView.setText("test");
                return textView;
            }


        };
        mList.setAdapter(mAdapter);
        mList.addHeaderView(mBannerView);
    }

    private void sendHotRequest() {
        if (mHotRequest != null) {
            httpService().abort(mHotRequest, this, true);
        }
        showProgressDialog(getString(R.string.loading));
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("module", "hot"));

        mHotRequest = BasicHttpRequest.httpPost(Environment.HTTP_ADDRESS, params);
        httpService().exec(mHotRequest, this);
    }

    private void sendMainForumRequest() {
        if (mMainForumRequest != null) {
            httpService().abort(mMainForumRequest, this, true);
        }
        showProgressDialog(getString(R.string.loading));
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("module", "forumindex"));

        mMainForumRequest = BasicHttpRequest.httpPost(Environment.HTTP_ADDRESS, params);
        httpService().exec(mMainForumRequest, this);
    }

    @Override
    public void onTabChanged(int curIndex) {
        if (curIndex == 0) {
            sendHotRequest();
        } else {
            sendMainForumRequest();
        }
    }

    @Override
    public void onRequestFinish(HttpRequest req, HttpResponse resp) {
        if (mHotRequest == req) {
            dismissDialog();
            if (resp.result() instanceof JSONObject) {
//                JSONObject hot = ((JSONObject) resp.result()).optJSONObject("Variables");
//                JSONArray announce = hot.optJSONArray("data_img");
//                JSONArray test = new JSONArray();
//                mBannerView.setAnnounce(announce, true);
            }

            mHotRequest = null;
        } else if (mMainForumRequest == req) {
            dismissDialog();
            mMainForumRequest = null;
            if (resp.result() instanceof JSONObject) {
                JSONObject mainForm = (JSONObject) resp.result();
                preferences();
            }
        }
    }

    @Override
    public void onRequestFailed(HttpRequest req, HttpResponse resp) {
        dismissDialog();
        showToast(getString(R.string.network_fail));
        if (mHotRequest == req) {
            mHotRequest = null;
        } else if (mMainForumRequest == req) {
            mMainForumRequest = null;
        }
    }
}
