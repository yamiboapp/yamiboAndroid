package com.yamibo.main.yamiboandroid.message;

import android.os.Bundle;
import android.view.View;
import android.view.ViewStub;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.yamibo.main.yamiboandroid.R;
import com.yamibo.main.yamiboandroid.message.adapter.MsgTalkListViewAdapter;
import com.yamibo.main.yamibolib.Utils.BasicNameValuePair;
import com.yamibo.main.yamibolib.Utils.Environment;
import com.yamibo.main.yamibolib.Utils.NameValuePair;
import com.yamibo.main.yamibolib.Utils.ViewUtils;
import com.yamibo.main.yamibolib.Utils.YMIUtils;
import com.yamibo.main.yamibolib.app.YMBActivity;
import com.yamibo.main.yamibolib.app.YMBApplication;
import com.yamibo.main.yamibolib.dataservice.RequestHandler;
import com.yamibo.main.yamibolib.dataservice.http.HttpRequest;
import com.yamibo.main.yamibolib.dataservice.http.HttpResponse;
import com.yamibo.main.yamibolib.dataservice.http.impl.BasicHttpRequest;
import com.yamibo.main.yamibolib.model.UserProfile;
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
public class PrivateMsgTalkActivity extends YMBActivity implements View.OnClickListener,RequestHandler<HttpRequest, HttpResponse> {

    private HttpRequest mPrivateMsgTalkRequest;
    private HttpRequest mPrivateSendMsgRequest;
    private List<Map<String,String>> mPrivateMsgTalkListData = new ArrayList<>();
    private MsgTalkListViewAdapter mMsgTalkListAdapter;
    private int mToLoadPage = 2;    //将要去加载的页数
    private boolean isFirstLoad = true;
    private String mTalkId;     //其实是用户的id
    private UserProfile mProfile = YMBApplication.instance().accountService().profile();

    //发送信息条
    private LinearLayout expPanel;
    private ImageView expBtnImg;
    private EditText msgInputEdit;
    private TextView msgSendBtnTxt;

    //下拉
    private PullableListView mMsgTalkListView;
    private PullToRefreshLayout mPullToRefreshLayout;



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
        setContentView(R.layout.activity_message_talk);
        setTitle(getString(R.string.message_talk_private));

        expPanel = (LinearLayout) findViewById(R.id.expression_panel);
        expBtnImg = (ImageView) findViewById(R.id.img_expression_btn);
        msgInputEdit = (EditText) findViewById(R.id.edit_message_input);
        msgSendBtnTxt = (TextView) findViewById(R.id.txt_message_send_btn);

        expBtnImg.setOnClickListener(this);
        msgSendBtnTxt.setOnClickListener(this);

        mMsgTalkListView = (PullableListView) findViewById(R.id.content_view);
        ViewStub viewStub = new ViewStub(this);
        mMsgTalkListView.addHeaderView(viewStub);
        mPullToRefreshLayout = (PullToRefreshLayout) findViewById(R.id.refresh_view);
        mPullToRefreshLayout.setOnRefreshListener(new PullToRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh(PullToRefreshLayout pullToRefreshLayout) {
                loadPrivateTalkMsg("1");
            }

            @Override
            public void onLoadMore(PullToRefreshLayout pullToRefreshLayout) {
                loadPrivateTalkMsg(String.valueOf(mToLoadPage));
            }
        });
        mMsgTalkListView.LIMIT_NOT_ALLOW_UP = 5;
        loadPrivateTalkMsg("1");
    }

    @Override
    public void onRequestFinish(HttpRequest req, HttpResponse resp) {
        if (mPrivateMsgTalkRequest == req) {
            dismissDialog();
            //初始化相关数据容器
            String page ="",perpage = "",count ="";
            List<Map<String,String>> tempDate = new ArrayList<>();
            //解析Json并放在相关容器里
            if (resp.result() instanceof JSONObject) {
                JSONObject privateMessage = (JSONObject) resp.result();
                try {
                    JSONObject var = privateMessage.getJSONObject("Variables");
                    JSONArray publicMsgList = var.getJSONArray("list");
                    for (int i = 0; i < publicMsgList.length(); i++) {
                        JSONObject msgObj= (JSONObject) publicMsgList.get(i);
                        Map<String,String> map = new HashMap<>();
                        map.put("portrait", YMIUtils.convertUid2BigImgUri(msgObj.optString("authorid")));
                        map.put("content", msgObj.optString("message"));
//                        map.put("date", DateUtils.timeStamp2Date(msgObj.optString("dateline")));
                        map.put("date", msgObj.optString("dateline"));
                        map.put("id", msgObj.optString("authorid"));
                        map.put("pmid", msgObj.optString("pmid"));
                        mProfile.setFormhash(var.optString("formhash"));    //更新formhash...
                        tempDate.add(map);
                    }
                    page = privateMessage.getJSONObject("Variables").optString("page");
                    perpage = privateMessage.getJSONObject("Variables").optString("perpage");
                    count = privateMessage.getJSONObject("Variables").optString("count");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            mPrivateMsgTalkRequest = null;


            //处理list里的数据
            if(isFirstLoad){
                mPrivateMsgTalkListData.addAll(tempDate);
                mMsgTalkListAdapter = new MsgTalkListViewAdapter(this, mPrivateMsgTalkListData,true);
                mMsgTalkListView.setAdapter(mMsgTalkListAdapter);
                isFirstLoad = false;
                mPullToRefreshLayout.refreshFinish(PullToRefreshLayout.SUCCEED);    //防止断网的时候进入,然后开启网络下拉刷新
                mPullToRefreshLayout.loadmoreFinish(PullToRefreshLayout.SUCCEED);
                return;
            }else if("1".equals(page)){ //不是第一次加载数据并且请求页数是1,代表是下拉刷新
                mPrivateMsgTalkListData.clear();
                mPrivateMsgTalkListData.addAll(tempDate);
                mMsgTalkListAdapter.notifyDataSetChanged();
                mToLoadPage = 2;  //重置一下由上拉加载改变的页数
            }else if( ((mToLoadPage -1) * Integer.parseInt(perpage)) < Integer.parseInt(count)){    //mToLoadPage - 1即为当前页数
                mPrivateMsgTalkListData.addAll(tempDate);
                mMsgTalkListAdapter.notifyDataSetChanged();
                mToLoadPage++;
            }

        }
        mPullToRefreshLayout.refreshFinish(PullToRefreshLayout.SUCCEED);
        mPullToRefreshLayout.loadmoreFinish(PullToRefreshLayout.SUCCEED);

        if (mPrivateSendMsgRequest == req) {
            dismissDialog();
            if (resp.result() instanceof JSONObject) {
                JSONObject privateMessage = (JSONObject) resp.result();
                try {
                    JSONObject msgObj = privateMessage.getJSONObject("Message");
                    String msgStr = msgObj.optString("messagestr");
                    String msgVal = msgObj.optString("messageval");
                    showToast(msgStr);

                    if("do_success".equals(msgVal)) {     //如果发送成功的话刷新一下
                        loadPrivateTalkMsg("1");
                        msgInputEdit.getText().clear();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            mPrivateSendMsgRequest = null;
        }
    }

    @Override
    public void onRequestFailed(HttpRequest req, HttpResponse resp) {
        if (mPrivateMsgTalkRequest == req) {
            dismissDialog();
            mPrivateMsgTalkRequest = null;
            showToast(getString(R.string.network_fail));
            mPullToRefreshLayout.refreshFinish(PullToRefreshLayout.FAIL);
            mPullToRefreshLayout.loadmoreFinish(PullToRefreshLayout.FAIL);
        }
        if(mPrivateSendMsgRequest == req){
            dismissDialog();
            mPrivateMsgTalkRequest = null;
            showToast(getString(R.string.network_fail));
        }
    }

    private void loadPrivateTalkMsg(String page){
        if (mPrivateMsgTalkRequest != null) {
            httpService().abort(mPrivateMsgTalkRequest, this, true);
        }
        showProgressDialog(getString(R.string.loading));
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("module", "mypm"));
        params.add(new BasicNameValuePair("page", page));
        params.add(new BasicNameValuePair("subop", "view"));
        params.add(new BasicNameValuePair("touid", mTalkId));

        mPrivateMsgTalkRequest = BasicHttpRequest.httpPost(Environment.HTTP_ADDRESS, params);
        httpService().exec(mPrivateMsgTalkRequest, this);
    }

    private void sendMsg(){
        if (mPrivateSendMsgRequest != null) {
            httpService().abort(mPrivateSendMsgRequest, this, true);
        }
        showProgressDialog(getString(R.string.loading));
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("module", "sendpm"));
        params.add(new BasicNameValuePair("message", msgInputEdit.getText().toString()));
        params.add(new BasicNameValuePair("formhash", mProfile.getFormhash()));
        params.add(new BasicNameValuePair("touid", mTalkId));

        mPrivateSendMsgRequest = BasicHttpRequest.httpPost(Environment.HTTP_ADDRESS, params);
        httpService().exec(mPrivateSendMsgRequest, this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.img_expression_btn){
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,ViewUtils.dip2px(this,200f));
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            expPanel.setLayoutParams(layoutParams);
        }
        if(v.getId() == R.id.txt_message_send_btn){
            sendMsg();
        }

    }
}
