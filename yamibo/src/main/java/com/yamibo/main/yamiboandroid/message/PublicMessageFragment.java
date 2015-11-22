package com.yamibo.main.yamiboandroid.message;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yamibo.main.yamiboandroid.R;
import com.yamibo.main.yamiboandroid.message.adapter.MessageListViewAdapter;
import com.yamibo.main.yamibolib.Utils.BasicNameValuePair;
import com.yamibo.main.yamibolib.Utils.Environment;
import com.yamibo.main.yamibolib.Utils.NameValuePair;
import com.yamibo.main.yamibolib.app.YMBFragment;
import com.yamibo.main.yamibolib.dataservice.RequestHandler;
import com.yamibo.main.yamibolib.dataservice.http.HttpRequest;
import com.yamibo.main.yamibolib.dataservice.http.HttpResponse;
import com.yamibo.main.yamibolib.dataservice.http.impl.BasicHttpRequest;
import com.yamibo.main.yamibolib.widget.pulltorefresh.PullToRefreshLayout;
import com.yamibo.main.yamibolib.widget.pulltorefresh.PullableListView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by WINFIELD on 2015/11/14.
 */
public class PublicMessageFragment extends YMBFragment implements RequestHandler<HttpRequest, HttpResponse> {
    private View mFragmentView;
    private PullableListView mMessageListView;
    private PullToRefreshLayout mPullToRefreshLayout;
    private HttpRequest mPublicMessageRequest;
    private int mRefreshCount = 0;
    private boolean isFirstLoad = true;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.mFragmentView = inflater.inflate(R.layout.fragment_message,null);
        initViews();


    return mFragmentView;
    }

    private void initViews() {
        mMessageListView = (PullableListView) mFragmentView.findViewById(R.id.content_view);
        mPullToRefreshLayout = (PullToRefreshLayout) mFragmentView.findViewById(R.id.refresh_view);
        mPullToRefreshLayout.setOnRefreshListener(new PullToRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh(PullToRefreshLayout pullToRefreshLayout) {
                loadPublicMessage("1");
            }

            @Override
            public void onLoadMore(PullToRefreshLayout pullToRefreshLayout) {
                loadPublicMessage(String.valueOf(mRefreshCount++));
            }
        });
        loadPublicMessage("1");


    }


    @Override
    public void onRequestFinish(HttpRequest req, HttpResponse resp) {
        if (mPublicMessageRequest == req) {
            dismissDialog();
            if (resp.result() instanceof JSONObject) {
                JSONObject publicMessage = (JSONObject) resp.result();
                try {
                    String msgall=publicMessage.getJSONObject("Variables").toString();
                    String messagerStr = publicMessage.getJSONObject("Variables").optString("perpage");
                    showToast(messagerStr);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            mPublicMessageRequest = null;

            if(isFirstLoad){
                mMessageListView.setAdapter(new MessageListViewAdapter(getActivity(), null));
                isFirstLoad = false;
                return;
            }
        }


        mPullToRefreshLayout.refreshFinish(PullToRefreshLayout.SUCCEED);
    }

    @Override
    public void onRequestFailed(HttpRequest req, HttpResponse resp) {
        if (mPublicMessageRequest == req) {
            dismissDialog();
            mPublicMessageRequest = null;
            showToast(getString(R.string.network_fail));
        }
    }

    private void loadPublicMessage(String page){
        if (mPublicMessageRequest != null) {
            httpService().abort(mPublicMessageRequest, this, true);
        }
        showProgressDialog(getString(R.string.loading));
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("module", "publicpm"));
        params.add(new BasicNameValuePair("page", page));

        mPublicMessageRequest = BasicHttpRequest.httpPost(Environment.HTTP_ADDRESS, params);
//        mPublicMessageRequest.addHeaders();
        httpService().exec(mPublicMessageRequest, this);
    }

}
