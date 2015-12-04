package com.yamibo.main.yamiboandroid.message;

import android.os.Bundle;
import android.view.ViewStub;

import com.yamibo.main.yamiboandroid.R;
import com.yamibo.main.yamiboandroid.message.adapter.MsgTalkListViewAdapter;
import com.yamibo.main.yamibolib.Utils.BasicNameValuePair;
import com.yamibo.main.yamibolib.Utils.Environment;
import com.yamibo.main.yamibolib.Utils.NameValuePair;
import com.yamibo.main.yamibolib.app.YMBActivity;
import com.yamibo.main.yamibolib.dataservice.RequestHandler;
import com.yamibo.main.yamibolib.dataservice.http.HttpRequest;
import com.yamibo.main.yamibolib.dataservice.http.HttpResponse;
import com.yamibo.main.yamibolib.dataservice.http.impl.BasicHttpRequest;
import com.yamibo.main.yamibolib.widget.pulltorefresh.PullToRefreshLayout;
import com.yamibo.main.yamibolib.widget.pulltorefresh.PullableListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by WINFIELD on 2015/11/29.
 */
public class PublicMsgTalkActivity extends YMBActivity implements RequestHandler<HttpRequest, HttpResponse> {

    private PullableListView mMsgTalkListView;
    private PullToRefreshLayout mPullToRefreshLayout;
    private HttpRequest mPublicMsgTalkRequest;
    private List<Map<String,String>> mPublicMsgTalkListData = new ArrayList<>();
    private MsgTalkListViewAdapter mMsgTalkListAdapter;
    private int mToLoadPage = 2;    //将要去加载的页数
    private boolean isFirstLoad = true;
    private String mTalkId;     //其实是公共消息的pmid

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getData();  //获取从上个Activity里面传过来的数据
        initViews();
    }

    private void getData() {
        mTalkId = this.getIntent().getStringExtra("talkId");
    }

    private void initViews() {
        setContentView(R.layout.fragment_message);
        setTitle(getString(R.string.message_talk_public));

        mMsgTalkListView = (PullableListView) findViewById(R.id.content_view);
        ViewStub viewStub = new ViewStub(this);
        mMsgTalkListView.addHeaderView(viewStub);
        mPullToRefreshLayout = (PullToRefreshLayout) findViewById(R.id.refresh_view);
        mPullToRefreshLayout.setOnRefreshListener(new PullToRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh(PullToRefreshLayout pullToRefreshLayout) {
                loadPublicTalkMsg("1");
            }

            @Override
            public void onLoadMore(PullToRefreshLayout pullToRefreshLayout) {
                loadPublicTalkMsg(String.valueOf(mToLoadPage));
            }
        });
        mMsgTalkListView.LIMIT_NOT_ALLOW_UP = 5;
        loadPublicTalkMsg("1");
    }

    @Override
    public void onRequestFinish(HttpRequest req, HttpResponse resp) {
        if (mPublicMsgTalkRequest == req) {
            dismissDialog();
            //初始化相关数据容器
            String page ="",perpage = "",count ="";
            List<Map<String,String>> tempDate = new ArrayList<>();
            //解析Json并放在相关容器里
            if (resp.result() instanceof JSONObject) {
                JSONObject publicMessage = (JSONObject) resp.result();
                try {
                    JSONArray publicMsgList = publicMessage.getJSONObject("Variables").getJSONArray("list");
                    for (int i = 0; i < publicMsgList.length(); i++) {
                        JSONObject msgObj= (JSONObject) publicMsgList.get(i);
                        Map<String,String> map = new HashMap<>();
                        map.put("content", msgObj.optString("message"));
//                        map.put("date", DateUtils.timeStamp2Date(msgObj.optString("dateline")));
                        map.put("date", msgObj.optString("dateline"));
                        map.put("id",msgObj.optString("id"));
                        tempDate.add(map);
                    }
                    page = publicMessage.getJSONObject("Variables").optString("page");
                    perpage = publicMessage.getJSONObject("Variables").optString("perpage");
                    count = publicMessage.getJSONObject("Variables").optString("count");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            mPublicMsgTalkRequest = null;


            //处理list里的数据
            if(isFirstLoad){
                mPublicMsgTalkListData.addAll(tempDate);
                mMsgTalkListAdapter = new MsgTalkListViewAdapter(this, mPublicMsgTalkListData,false);
                mMsgTalkListView.setAdapter(mMsgTalkListAdapter);
                isFirstLoad = false;
                mPullToRefreshLayout.refreshFinish(PullToRefreshLayout.SUCCEED);    //防止断网的时候进入,然后开启网络下拉刷新
                mPullToRefreshLayout.loadmoreFinish(PullToRefreshLayout.SUCCEED);
                return;
            }else if("1".equals(page)){ //不是第一次加载数据并且请求页数是1,代表是下拉刷新
                mPublicMsgTalkListData.clear();
                mPublicMsgTalkListData.addAll(tempDate);
                mMsgTalkListAdapter.notifyDataSetChanged();
                mToLoadPage = 2;  //重置一下由上拉加载改变的页数
            }else if( ((mToLoadPage -1) * Integer.parseInt(perpage)) < Integer.parseInt(count)){    //mToLoadPage - 1即为当前页数
                mPublicMsgTalkListData.addAll(tempDate);
                mMsgTalkListAdapter.notifyDataSetChanged();
                mToLoadPage++;
            }

        }
        mPullToRefreshLayout.refreshFinish(PullToRefreshLayout.SUCCEED);
        mPullToRefreshLayout.loadmoreFinish(PullToRefreshLayout.SUCCEED);
    }

    @Override
    public void onRequestFailed(HttpRequest req, HttpResponse resp) {
        if (mPublicMsgTalkRequest == req) {
            dismissDialog();
            mPublicMsgTalkRequest = null;
            showToast(getString(R.string.network_fail));
            mPullToRefreshLayout.refreshFinish(PullToRefreshLayout.FAIL);
            mPullToRefreshLayout.loadmoreFinish(PullToRefreshLayout.FAIL);
        }
    }

    private void loadPublicTalkMsg(String page){
        if (mPublicMsgTalkRequest != null) {
            httpService().abort(mPublicMsgTalkRequest, this, true);
        }
        showProgressDialog(getString(R.string.loading));
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("module", "publicpm"));
        params.add(new BasicNameValuePair("page", page));
        params.add(new BasicNameValuePair("subop", "viewg"));
        params.add(new BasicNameValuePair("pmid", mTalkId));

        mPublicMsgTalkRequest = BasicHttpRequest.httpPost(Environment.HTTP_ADDRESS, params);
        httpService().exec(mPublicMsgTalkRequest, this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
