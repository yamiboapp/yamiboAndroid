package com.yamibo.main.yamiboandroid.message;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;

import com.yamibo.main.yamiboandroid.R;
import com.yamibo.main.yamiboandroid.message.adapter.MessageListViewAdapter;
import com.yamibo.main.yamibolib.Utils.BasicNameValuePair;
import com.yamibo.main.yamibolib.Utils.Environment;
import com.yamibo.main.yamibolib.Utils.NameValuePair;
import com.yamibo.main.yamibolib.Utils.YMIUtils;
import com.yamibo.main.yamibolib.app.YMBFragment;
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
 * Created by WINFIELD on 2015/11/14.
 */
public class PrivateMessageFragment extends YMBFragment implements RequestHandler<HttpRequest, HttpResponse> {
    private View mFragmentView;
    private PullableListView mMessageListView;
    private PullToRefreshLayout mPullToRefreshLayout;
    private HttpRequest mPrivateMessageRequest;
    private List<Map<String,String>> mPrivateMsgListData = new ArrayList<>();
    private MessageListViewAdapter mMessageListAdapter;
    private int mToLoadPage = 2;    //将要去加载的页数
    private boolean isFirstLoad = true;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.mFragmentView = inflater.inflate(R.layout.fragment_message,null);
        initViews();




        return mFragmentView;
    }

    private void initViews() {
        mMessageListView = (PullableListView) mFragmentView.findViewById(R.id.content_view);
        ViewStub viewStub = new ViewStub(getActivity());
        mMessageListView.addHeaderView(viewStub);
        mPullToRefreshLayout = (PullToRefreshLayout) mFragmentView.findViewById(R.id.refresh_view);
        mPullToRefreshLayout.setOnRefreshListener(new PullToRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh(PullToRefreshLayout pullToRefreshLayout) {
                loadPrivateMessage("1");
            }

            @Override
            public void onLoadMore(PullToRefreshLayout pullToRefreshLayout) {
                loadPrivateMessage(String.valueOf(mToLoadPage));
            }
        });
        loadPrivateMessage("1");


    }


    @Override
    public void onRequestFinish(HttpRequest req, HttpResponse resp) {
        if (mPrivateMessageRequest == req) {
            dismissDialog();
            //初始化相关数据容器
            String page ="",perpage = "",count ="";
            List<Map<String,String>> tempDate = new ArrayList<>();
            //解析Json并放在相关容器里
            if (resp.result() instanceof JSONObject) {
                JSONObject privateMessage = (JSONObject) resp.result();
                try {
                    JSONArray publicMsgList = privateMessage.getJSONObject("Variables").getJSONArray("list");
                    for (int i = 0; i < publicMsgList.length(); i++) {
                        JSONObject msgObj= (JSONObject) publicMsgList.get(i);
                        Map<String,String> map = new HashMap<>();
                        map.put("username",msgObj.optString("tousername"));
//                        map.put("date", DateUtils.timeStamp2Date(msgObj.optString("dateline")));
                        map.put("date", msgObj.optString("dateline"));
                        map.put("touid",msgObj.optString("touid"));
                        map.put("portrait",YMIUtils.convertUid2BigImgUri(msgObj.optString("touid")));
                        map.put("message", YMIUtils.convertNbsp2Space(msgObj.optString("message")));
                        map.put("messageId",msgObj.optString("touid"));
                        map.put("formhash",privateMessage.getJSONObject("Variables").optString("formhash"));

                        tempDate.add(map);
                    }
                    page = privateMessage.getJSONObject("Variables").optString("page");
                    perpage = privateMessage.getJSONObject("Variables").optString("perpage");
                    count = privateMessage.getJSONObject("Variables").optString("count");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            mPrivateMessageRequest = null;


            //处理list里的数据
            if(isFirstLoad){
                mPrivateMsgListData.addAll(tempDate);
                mMessageListAdapter = new MessageListViewAdapter(getActivity(), mPrivateMsgListData);
                mMessageListView.setAdapter(mMessageListAdapter);
                isFirstLoad = false;
                mPullToRefreshLayout.refreshFinish(PullToRefreshLayout.SUCCEED);    //防止断网的时候进入,然后开启网络下拉刷新
                mPullToRefreshLayout.loadmoreFinish(PullToRefreshLayout.SUCCEED);
                return;
            }else if("1".equals(page)){ //不是第一次加载数据并且请求页数是1,代表是下拉刷新
                mPrivateMsgListData.clear();
                mPrivateMsgListData.addAll(tempDate);
                mMessageListAdapter.notifyDataSetChanged();
                mToLoadPage = 2;  //重置一下由上拉加载改变的页数
            }else if( ((mToLoadPage -1) * Integer.parseInt(perpage)) < Integer.parseInt(count)){    //mToLoadPage - 1即为当前页数
                mPrivateMsgListData.addAll(tempDate);
                mMessageListAdapter.notifyDataSetChanged();
                mToLoadPage++;
            }

        }
        mPullToRefreshLayout.refreshFinish(PullToRefreshLayout.SUCCEED);
        mPullToRefreshLayout.loadmoreFinish(PullToRefreshLayout.SUCCEED);
    }

    @Override
    public void onRequestFailed(HttpRequest req, HttpResponse resp) {
        if (mPrivateMessageRequest == req) {
            dismissDialog();
            mPrivateMessageRequest = null;
            showToast(getString(R.string.network_fail));
            mPullToRefreshLayout.refreshFinish(PullToRefreshLayout.FAIL);
            mPullToRefreshLayout.loadmoreFinish(PullToRefreshLayout.FAIL);
        }
    }

    private void loadPrivateMessage(String page){
        if (mPrivateMessageRequest != null) {
            httpService().abort(mPrivateMessageRequest, this, true);
        }
        showProgressDialog(getString(R.string.loading));
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("module", "mypm"));
        params.add(new BasicNameValuePair("page", page));

        mPrivateMessageRequest = BasicHttpRequest.httpPost(Environment.HTTP_ADDRESS, params);
        httpService().exec(mPrivateMessageRequest, this);
    }

}
