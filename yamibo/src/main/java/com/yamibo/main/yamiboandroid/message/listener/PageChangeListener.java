package com.yamibo.main.yamiboandroid.message.listener;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yamibo.main.yamibolib.Utils.ViewUtils;

import java.util.List;

/**
 * Created by WINFIELD on 2015/11/14.
 */
public class PageChangeListener implements ViewPager.OnPageChangeListener {
    private Context context;
    private TextView tabLineTxt;
    private List<TextView> tabList;
    private int tabCount;
    private float tabLineScale;     //tab线占一个tab的宽度的比例

    private int screenWidth;
    private int singleTabWidth;     //一个tab所占宽度
    private int tabLineWidth;       //tab下横线的宽度
    private int offset;             //横线在一个tab中的左右空隙宽度

    private static final float TEXT_ALPHA = 0.6f;   //文字透明度


    public PageChangeListener(Context context,TextView tabLineTxt,List<TextView> tabList,float tabLineScale){
        this.context = context;
        this.tabLineTxt = tabLineTxt;
        this.tabList = tabList;
        this.tabCount = tabList.size();
        this.tabLineScale = tabLineScale;
        configTabLine();


    }

    private void configTabLine() {
        screenWidth = ViewUtils.getScreenWidthPixels(context);
        singleTabWidth = screenWidth / tabCount;
        tabLineWidth = (int) (singleTabWidth * tabLineScale);
        offset = (singleTabWidth - tabLineWidth) / 2;
        //设置横线初始位置
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(tabLineWidth, LinearLayout.LayoutParams.MATCH_PARENT);
        layoutParams.setMargins(offset,0,0,0);
        tabLineTxt.setLayoutParams(layoutParams);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        tabLineTxt.setTranslationX(positionOffsetPixels / tabCount + position * singleTabWidth);
        tabList.get(position).setAlpha(1f - positionOffset * TEXT_ALPHA);
        if(position < tabCount - 1)
            tabList.get(position + 1).setAlpha(TEXT_ALPHA + positionOffset * (1 - TEXT_ALPHA));
    }

    @Override
    public void onPageSelected(int position) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}
