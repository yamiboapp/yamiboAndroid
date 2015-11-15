package com.yamibo.main.yamiboandroid.message;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.widget.TextView;

import com.yamibo.main.yamiboandroid.R;
import com.yamibo.main.yamiboandroid.message.listener.TabOnclickListener;
import com.yamibo.main.yamibolib.app.YMBActivity;
import com.yamibo.main.yamiboandroid.message.adapter.MessageViewPagerAdapter;
import com.yamibo.main.yamiboandroid.message.listener.PageChangeListener;
import com.yamibo.main.yamibolib.widget.NoScrollViewPager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by WINFIELD on 2015/11/14.
 */
public class MessageActivity extends YMBActivity{
    private NoScrollViewPager messageViewPager;
    private TextView privateMessageTxt;
    private TextView publicMessageTxt;
    private TextView tabLineTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();

    }

    private void initView() {
        setContentView(R.layout.activity_message);
        setTitle("信息");

        messageViewPager= (NoScrollViewPager) findViewById(R.id.viewpager_message);
        privateMessageTxt = (TextView) findViewById(R.id.txt_message_private);
        publicMessageTxt = (TextView) findViewById(R.id.txt_message_public);
        tabLineTxt = (TextView) findViewById(R.id.txt_message_tabline);

        List<Fragment> fragmentList = new ArrayList<>();
        fragmentList.add(new PublicMessageFragment());
        fragmentList.add(new PrivateMessageFragment());
        messageViewPager.setAdapter(new MessageViewPagerAdapter(getSupportFragmentManager(), fragmentList));

        List<TextView> tabList = new ArrayList<>();
        tabList.add(publicMessageTxt);
        tabList.add(privateMessageTxt);
        messageViewPager.setOnPageChangeListener(new PageChangeListener(this, tabLineTxt, tabList, 0.5f));

        publicMessageTxt.setOnClickListener(new TabOnclickListener(0,messageViewPager));
        privateMessageTxt.setOnClickListener(new TabOnclickListener(1,messageViewPager));
    }
}
