package com.yamibo.main.yamiboandroid.message.listener;

import android.support.v4.view.ViewPager;
import android.view.View;

/**
 * Created by WINFIELD on 2015/11/15.
 */
public class TabOnclickListener implements View.OnClickListener{
    private int currItem;
    private ViewPager viewPager;

    public TabOnclickListener(int currItem,ViewPager viewPager){
        this.currItem = currItem;
        this.viewPager = viewPager;
    }

    @Override
    public void onClick(View v) {
        viewPager.setCurrentItem(currItem);
    }
}
