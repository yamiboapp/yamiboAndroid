package com.yamibo.main.yamiboandroid.message.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yamibo.main.yamiboandroid.R;
import com.yamibo.main.yamibolib.Utils.BasicNameValuePair;
import com.yamibo.main.yamibolib.Utils.Environment;
import com.yamibo.main.yamibolib.Utils.NameValuePair;
import com.yamibo.main.yamibolib.app.YMBAdapter;
import com.yamibo.main.yamibolib.app.YMBApplication;
import com.yamibo.main.yamibolib.dataservice.RequestHandler;
import com.yamibo.main.yamibolib.dataservice.http.HttpRequest;
import com.yamibo.main.yamibolib.dataservice.http.HttpResponse;
import com.yamibo.main.yamibolib.dataservice.http.impl.BasicHttpRequest;
import com.yamibo.main.yamibolib.model.UserProfile;
import com.yamibo.main.yamibolib.widget.YMBNetworkImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by WINFIELD on 2015/11/29.
 */
public class MsgTalkListViewAdapter extends YMBAdapter implements View.OnClickListener,RequestHandler<HttpRequest, HttpResponse> {

    private Context mContext;
    private List<Map<String,String>> mData;
    private boolean isPrivate;
    private UserProfile mProfile = YMBApplication.instance().accountService().profile();
    private final int VIEW_TYPE_COUNT = 2;
    private final int VIEW_TYPE_YOU = 0;
    private final int VIEW_TYPE_ME = 1;
    private HttpRequest mDeleteRequest;
    private int mNowPosition;

    public MsgTalkListViewAdapter(Context mContext,List<Map<String,String>> mData,boolean isPrivate){
        super(mContext);
        this.mContext = mContext;
        this.mData = mData;
        this.isPrivate = isPrivate;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolderYou = null;
        ViewHolder viewHolderMe = null;
        int type = getItemViewType(position);
        if(convertView == null){
            LayoutInflater inflater = LayoutInflater.from(mContext);
            switch (type){
                case VIEW_TYPE_YOU:
                    convertView = inflater.inflate(R.layout.item_message_talk_you,parent,false);
                    viewHolderYou = new ViewHolder();
                    viewHolderYou.msgTalkLlaout = (LinearLayout) convertView.findViewById(R.id.talk_item_useful);
                    viewHolderYou.msgTalkPortraitImg = (YMBNetworkImageView) convertView.findViewById(R.id.img_msg_talk_potrait);
                    viewHolderYou.msgTalkContentTxt = (TextView) convertView.findViewById(R.id.txt_msg_talk_content);
                    viewHolderYou.msgTalkDateTxt = (TextView) convertView.findViewById(R.id.txt_msg_talk_date);
                    break;
                case VIEW_TYPE_ME:
                    convertView = inflater.inflate(R.layout.item_message_talk_me,parent,false);
                    viewHolderMe = new ViewHolder();
                    viewHolderMe.msgTalkLlaout = (LinearLayout) convertView.findViewById(R.id.talk_item_useful);
                    viewHolderMe.msgTalkPortraitImg = (YMBNetworkImageView) convertView.findViewById(R.id.img_msg_talk_potrait);
                    viewHolderMe.msgTalkContentTxt = (TextView) convertView.findViewById(R.id.txt_msg_talk_content);
                    viewHolderMe.msgTalkDateTxt = (TextView) convertView.findViewById(R.id.txt_msg_talk_date);
                    break;
            }
        }else{
            switch (type){
                case VIEW_TYPE_YOU:
                    viewHolderYou = (ViewHolder) convertView.getTag();
                    break;
                case VIEW_TYPE_ME:
                    viewHolderMe = (ViewHolder) convertView.getTag();
                    break;
            }
        }

        Map<String,String> map = mData.get(position);
        String portraitUri = map.get("portrait");
        //设置资源
        switch (type){
            case VIEW_TYPE_YOU:
                if(isPrivate)
                    viewHolderYou.msgTalkPortraitImg.setImageUri(portraitUri);
                else viewHolderYou.msgTalkPortraitImg.setLocalResourceId(R.drawable.icon_logo);
                viewHolderYou.msgTalkContentTxt.setText(map.get("content"));
                viewHolderYou.msgTalkDateTxt.setText(map.get("date"));
                viewHolderYou.msgTalkLlaout.setOnClickListener(this);
                viewHolderYou.msgTalkLlaout.setTag(position);
                convertView.setTag(viewHolderYou);
                break;
            case VIEW_TYPE_ME:
                viewHolderMe.msgTalkPortraitImg.setImageUri(portraitUri);
                viewHolderMe.msgTalkContentTxt.setText(map.get("content"));
                viewHolderMe.msgTalkDateTxt.setText(map.get("date"));
                viewHolderMe.msgTalkLlaout.setOnClickListener(this);
                viewHolderMe.msgTalkLlaout.setTag(position);
                convertView.setTag(viewHolderMe);
                break;
        }

        return convertView;
    }

    @Override
    public int getViewTypeCount() {
        return VIEW_TYPE_COUNT;
    }

    @Override
    public int getItemViewType(int position) {
        Map<String,String> map = mData.get(position);
        String memberUid = mProfile.getMember_uid();
        String id = map.get("id");       //个人touid和公共pmid的合称
        if(memberUid == null)    //不可能的情况
            return VIEW_TYPE_ME;
        if(memberUid.equals(id))
            return VIEW_TYPE_ME;
        else return VIEW_TYPE_YOU;

    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.talk_item_useful){
            int position = (int) v.getTag();
            mNowPosition = position;
            if(!isPrivate)  //如果是公共对话列表的话,就不给删除
                return ;
            showMessageDialog(mContext.getString(R.string.delete_confirm),mContext.getString(R.string.delete_prompt),new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //启动删除逻辑
                    deleteMsgTalkItem();
                }
            },new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
        }
    }

    private void deleteMsgTalkItem(){
        if (mDeleteRequest != null) {
            httpService().abort(mDeleteRequest, this, true);
        }
        Map<String,String> map = mData.get(mNowPosition);
        showProgressDialog(mContext.getString(R.string.loading));
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("module", "sendpm"));
        params.add(new BasicNameValuePair("op", "delete"));
        params.add(new BasicNameValuePair("pmid", map.get("pmid")));
        params.add(new BasicNameValuePair("formhash", mProfile.getFormhash()));

        mDeleteRequest = BasicHttpRequest.httpPost(Environment.HTTP_ADDRESS, params);
        httpService().exec(mDeleteRequest, this);
    }

    @Override
    public void onRequestFinish(HttpRequest req, HttpResponse resp) {
        if (mDeleteRequest == req) {
            dismissDialog();
            //初始化相关数据容器
            List<Map<String,String>> tempDate = new ArrayList<>();
            //解析Json并放在相关容器里
            if (resp.result() instanceof JSONObject) {
                JSONObject returnMessage = (JSONObject) resp.result();
                try {
                    JSONObject deleteMsg = returnMessage.getJSONObject("Message");
                    String deleteMessage = deleteMsg.optString("messagestr");
                    showToast(deleteMessage);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            mDeleteRequest = null;
            //处理list里的数据
            mData.remove(mNowPosition);
            this.notifyDataSetChanged();
        }

    }

    @Override
    public void onRequestFailed(HttpRequest req, HttpResponse resp) {
        if (mDeleteRequest == req) {
            dismissDialog();
            mDeleteRequest = null;
            showToast(mContext.getString(R.string.network_fail));
        }
    }

    class ViewHolder{
        public LinearLayout msgTalkLlaout;
        public YMBNetworkImageView msgTalkPortraitImg;
        public TextView msgTalkContentTxt;
        public TextView msgTalkDateTxt;
    }


}
