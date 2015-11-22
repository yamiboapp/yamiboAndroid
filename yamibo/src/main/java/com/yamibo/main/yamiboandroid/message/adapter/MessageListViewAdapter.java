package com.yamibo.main.yamiboandroid.message.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.daimajia.swipe.adapters.BaseSwipeAdapter;
import com.yamibo.main.yamiboandroid.R;

import java.util.List;
import java.util.Map;

/**
 * Created by WINFIELD on 2015/11/15.
 */
public class MessageListViewAdapter extends BaseSwipeAdapter{
    private Context mContext;
    private List<Map<String,String>> mData;

    public MessageListViewAdapter(Context mContext,List<Map<String,String>> mData){
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
        if(convertView == null){
            viewHolder = new ViewHolder();
            viewHolder.messagePotraitImg = (ImageView) convertView.findViewById(R.id.img_message_potrait);
            viewHolder.messageSenderTxt = (TextView) convertView.findViewById(R.id.txt_message_sender);
            viewHolder.messageContentTxt = (TextView) convertView.findViewById(R.id.txt_message_content);
            viewHolder.messageReceiveDateTxt = (TextView) convertView.findViewById(R.id.txt_message_receive_date);
            convertView.setTag(viewHolder);
        } else viewHolder = (ViewHolder) convertView.getTag();

        

    }

    @Override
    public int getCount() {
        return 50;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    class ViewHolder{
        public ImageView messagePotraitImg;
        public TextView messageSenderTxt;
        public TextView messageContentTxt;
        public TextView messageReceiveDateTxt;
    }
}
