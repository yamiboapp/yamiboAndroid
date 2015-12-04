package com.yamibo.main.yamiboandroid.message.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.yamibo.main.yamiboandroid.R;
import com.yamibo.main.yamiboandroid.message.PrivateMsgTalkActivity;
import com.yamibo.main.yamiboandroid.message.PublicMsgTalkActivity;
import com.yamibo.main.yamibolib.Utils.BasicNameValuePair;
import com.yamibo.main.yamibolib.Utils.Environment;
import com.yamibo.main.yamibolib.Utils.Log;
import com.yamibo.main.yamibolib.Utils.NameValuePair;
import com.yamibo.main.yamibolib.app.YMBSwipeAdapter;
import com.yamibo.main.yamibolib.dataservice.RequestHandler;
import com.yamibo.main.yamibolib.dataservice.http.HttpRequest;
import com.yamibo.main.yamibolib.dataservice.http.HttpResponse;
import com.yamibo.main.yamibolib.dataservice.http.impl.BasicHttpRequest;
import com.yamibo.main.yamibolib.widget.YMBNetworkImageView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by WINFIELD on 2015/11/15.
 */
public class MessageListViewAdapter extends YMBSwipeAdapter implements View.OnClickListener,View.OnLongClickListener,RequestHandler<HttpRequest, HttpResponse> {
    private HttpRequest mDeleteItemRequest;
    private Context mContext;
    private List<Map<String,String>> mData;
    private int mNowPosition;
    private boolean isPrivate = false;

    public MessageListViewAdapter(Context mContext,List<Map<String,String>> mData){
        super(mContext);
        this.mContext = mContext;
        this.mData = mData;
    }


    @Override
    public int getSwipeLayoutResourceId(int i) {
        return R.id.swipe_item;
    }

    @Override
    public View generateView(int i, ViewGroup viewGroup) {
        return LayoutInflater.from(mContext).inflate(R.layout.item_message, null);
    }

    @Override
    public void fillValues(int i, View convertView) {
        ViewHolder viewHolder;
        if(convertView.getTag() == null){
            viewHolder = new ViewHolder();
            viewHolder.messagePortraitImg = (YMBNetworkImageView) convertView.findViewById(R.id.img_message_potrait);
            viewHolder.messageSenderTxt = (TextView) convertView.findViewById(R.id.txt_message_sender);
            viewHolder.messageContentTxt = (TextView) convertView.findViewById(R.id.txt_message_content);
            viewHolder.messageReceiveDateTxt = (TextView) convertView.findViewById(R.id.txt_message_receive_date);
            viewHolder.trashImg = (ImageView) convertView.findViewById(R.id.img_trash);
            viewHolder.messageLlayout = (LinearLayout) convertView.findViewById(R.id.llayout_message);
            convertView.setTag(viewHolder);
        } else viewHolder = (ViewHolder) convertView.getTag();

        Map<String,String> map = mData.get(i);
        viewHolder.messageSenderTxt.setText(map.get("username"));
        viewHolder.messageContentTxt.setText(map.get("message"));
        viewHolder.messageReceiveDateTxt.setText(map.get("date"));
        String portraitUri = map.get("portrait");
        if(portraitUri != null && portraitUri.length()!= 0)
            isPrivate = true;
        if(isPrivate)
            viewHolder.messagePortraitImg.setImageUri(portraitUri);
        else viewHolder.messagePortraitImg.setLocalResourceId(R.drawable.icon_logo);

        viewHolder.messageLlayout.setOnLongClickListener(this);
        viewHolder.messageLlayout.setTag(i);
        viewHolder.messageLlayout.setOnClickListener(this);
        viewHolder.messageLlayout.setTag(i);
        viewHolder.trashImg.setOnClickListener(this);
        viewHolder.trashImg.setTag(i);
        Log.d("message pos","" + i);
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.img_trash){
            int position = (int) v.getTag();
            mNowPosition = position;
            //获取data里面的值,判断是公共还是私人,调用相应方法
            Map<String,String> map = mData.get(mNowPosition);
            String id = map.get("messageId");
            String formhash = map.get("formhash");
            if(isPrivate)     //私人
                deletePrivateMsgRequest(mNowPosition,id,formhash);
            else deletePublicMsgRequest(mNowPosition,id,formhash);    //公共
        }
        if(v.getId() == R.id.llayout_message){
            int position = (int) v.getTag();
            Map<String,String> map = mData.get(position);

            Intent intent = new Intent();
            String talkId;
            if(isPrivate){
                talkId = map.get("touid");
                intent.setClass(mContext, PrivateMsgTalkActivity.class);
            }else{
                talkId = map.get("id");
                intent.setClass(mContext, PublicMsgTalkActivity.class);
            }
            intent.putExtra("talkId",talkId);   //talkId的意思为此对话的发起人的id
            mContext.startActivity(intent);
        }

    }

    @Override
    public void onRequestFinish(HttpRequest req, HttpResponse resp) {
        if (mDeleteItemRequest == req) {
            mData.remove(mNowPosition);
            MessageListViewAdapter.this.notifyDataSetChanged();
            MessageListViewAdapter.this.closeAllItems();    //用这个好看一点

            Toast.makeText(mContext,mContext.getString(R.string.delete_hint),Toast.LENGTH_LONG).show();
            JSONObject message = (JSONObject) resp.result();
//            try {
//                Toast.makeText(mContext,message.getJSONObject("Message").optString("messagestr"),Toast.LENGTH_LONG).show();
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }

            mDeleteItemRequest = null;
        }
    }

    @Override
    public void onRequestFailed(HttpRequest req, HttpResponse resp) {
        if (mDeleteItemRequest == req) {
            mDeleteItemRequest = null;
            Toast.makeText(mContext,mContext.getString(R.string.network_fail),Toast.LENGTH_LONG).show();
        }
    }

    private void deletePrivateMsgRequest(int position,String touid,String formhash){
        deleteMessageRequest(position,"touid",touid,formhash);
    }
    private void deletePublicMsgRequest(int position,String gpmid,String formhash){
        deleteMessageRequest(position,"gpmid",gpmid,formhash);
    }

    private void  deleteMessageRequest(int position,String type,String id,String formhash){
        if (mDeleteItemRequest != null) {
            httpService().abort(mDeleteItemRequest, this, true);
        }
//        showProgressDialog(getString(R.string.loading));
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("module", "sendpm"));
        params.add(new BasicNameValuePair("op", "delete"));
        params.add(new BasicNameValuePair(type, id));
        params.add(new BasicNameValuePair("formhash", formhash));

        mDeleteItemRequest = BasicHttpRequest.httpPost(Environment.HTTP_ADDRESS, params);
        httpService().exec(mDeleteItemRequest, this);
    }

    @Override
    public boolean onLongClick(View v) {
        if(v.getId() == R.id.llayout_message){
            int position = (int) v.getTag();
            Map<String,String> map = mData.get(position);
            showAlertDialog(mContext.getString(R.string.message_detail),map.get("message"));
        }
        return true;
    }

    class ViewHolder {
        public YMBNetworkImageView messagePortraitImg;
        public TextView messageSenderTxt;
        public TextView messageContentTxt;
        public TextView messageReceiveDateTxt;
        public ImageView trashImg;
        public LinearLayout messageLlayout;
    }
}
