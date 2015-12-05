package com.yamibo.main.yamiboandroid.settting;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.yamibo.main.yamiboandroid.R;
import com.yamibo.main.yamibolib.Utils.BasicNameValuePair;
import com.yamibo.main.yamibolib.Utils.Environment;
import com.yamibo.main.yamibolib.Utils.NameValuePair;
import com.yamibo.main.yamibolib.accountservice.AccountListener;
import com.yamibo.main.yamibolib.accountservice.AccountService;
import com.yamibo.main.yamibolib.app.YMBActivity;
import com.yamibo.main.yamibolib.dataservice.RequestHandler;
import com.yamibo.main.yamibolib.dataservice.http.HttpRequest;
import com.yamibo.main.yamibolib.dataservice.http.HttpResponse;
import com.yamibo.main.yamibolib.dataservice.http.impl.BasicHttpRequest;
import com.yamibo.main.yamibolib.widget.BasicItem;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.yamibo.main.yamibolib.locationservice.impl.util.debugShow;

/**
 * Created by wangxiaoyan on 15/11/21.
 */
public class SettingActivity extends YMBActivity implements View.OnClickListener, AccountListener, RequestHandler<HttpRequest, HttpResponse> {
    Button mLogoutButton;
    Button mTestButton;

    private BasicItem mVersion;
    private BasicItem mDeveloper;

    private String API_HTTP_ADDRESS = Environment.HTTP_ADDRESS;
    private int currentVersion=-1;
    private int newVersion=-1;
    private HttpRequest mUpdateRequest=null;

    private SharedPreferences sharedPref=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        mLogoutButton = (Button) findViewById(R.id.btn_logout);
        if (!accountService().isLogin()) {
            mLogoutButton.setVisibility(View.GONE);
        }
        mLogoutButton.setOnClickListener(this);

        // debug 才有，之后还会写其他的打开方式，或者写一个测试界面
        mTestButton = (Button) findViewById(R.id.btn_test);
        if (!Environment.isDebug()) {
            mTestButton.setVisibility(View.GONE);
        } else {
            mTestButton.setVisibility(View.VISIBLE);
        }
        mTestButton.setOnClickListener(this);
        accountService().addListener(this);

        sharedPref =preferences();

        initView();

        debugShow("current version is "+currentVersion);
        newVersion=readNewVersion();
        //if no new version is available, fetch it from API via showNewVersion(). MUST initiate view before http service
        if(newVersion==-1) {
            //fetch version and update info from API, update it to
            int mNewVersion = -1;
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("module", "update"));
            params.add(new BasicNameValuePair("platform", "android"));
            params.add(new BasicNameValuePair("version", Integer.toString(currentVersion)));
            mUpdateRequest = BasicHttpRequest.httpPost(API_HTTP_ADDRESS, params);
            httpService().exec(mUpdateRequest, this);
        }


    }

    private String versionToString(int version){
        String message=Integer.toString(version);
        if(message!=null)
        {
            int length=message.length();

        String decoratedMessage="";

        for(int k=0;k<length-1;k++){
            decoratedMessage+=message.charAt(k);
            decoratedMessage+=".";
        }
        decoratedMessage+=message.charAt(length-1);
        return decoratedMessage;
        }
        else
            return null;
    }
    private void initView() {
        mVersion=(BasicItem)findViewById(R.id.version);
        currentVersion=Environment.versionCode();

        mVersion.setTitle(getResources().getString(R.string.version)+" "+versionToString(currentVersion));

        mDeveloper=(BasicItem)findViewById(R.id.developer);
        mDeveloper.setTitle("开发者");
        mDeveloper.setOnClickListener(this);
    }

    //if new version information is avalable, newVersion!=-1
    private void showNewVersion(){
        if(newVersion!=-1&&newVersion!=currentVersion)
                mVersion.setSubTitle(" (有新版本 "+versionToString(newVersion)+")");
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_logout) {
            accountService().logout();
        } else if (v.getId() == R.id.btn_test) {
            startActivity("ymb://debug");
        } else if (v.getId()==R.id.developer){
            startActivity("ymb://developer");
        }

    }

    @Override
    protected void onDestroy() {
        accountService().removeListener(this);
        super.onDestroy();
    }

    @Override
    public void onAccountChanged(AccountService sender) {
        if (!sender.isLogin()) {
            mLogoutButton.setVisibility(View.GONE);
        } else {
            mLogoutButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onRequestFinish(HttpRequest req, HttpResponse resp) {
        if(mUpdateRequest==req){
            if (resp.result() instanceof JSONObject) {
                JSONObject respResult = (JSONObject) resp.result();
                debugShow("response result is" + respResult.toString());
                // output results
                try {
                    newVersion= respResult.getInt("version");
                    String url= (String) respResult.get("url");

                    commitNewVersion(newVersion);

                } catch (Exception e) {
                    e.printStackTrace();
                    showToast(getString(R.string.network_fail));
                }
            }
            mUpdateRequest = null;
            showNewVersion();
        }
    }

    @Override
    public void onRequestFailed(HttpRequest req, HttpResponse resp) {
        if(mUpdateRequest==req){
            mUpdateRequest = null;
            showToast(getString(R.string.network_fail));
        }

    }

    private void commitNewVersion(int newVersion){
        SharedPreferences.Editor editor=sharedPref.edit();
        editor.putInt(getString(R.string.newVersion_key),newVersion);
    }
    // return -1 if no current version found.
    private int readNewVersion(){
        return sharedPref.getInt(getString(R.string.newVersion_key),-1);
    }
}
