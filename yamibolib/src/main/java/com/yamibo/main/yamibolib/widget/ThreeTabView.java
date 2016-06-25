package com.yamibo.main.yamibolib.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yamibo.main.yamibolib.R;

public class ThreeTabView extends LinearLayout implements OnClickListener {

    LinearLayout layout1;
    LinearLayout layout2;
//    LinearLayout layout3;

    TextView title1;
    TextView title2;
    //    TextView title3;
    int curIndex = -1;
    TabChangeListener listener;

    public ThreeTabView(Context context) {
        super(context);
    }

    public ThreeTabView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        layout1 = (LinearLayout) findViewById(R.id.tab1);
        layout2 = (LinearLayout) findViewById(R.id.tab2);
//        layout3 = (LinearLayout) findViewById(R.id.tab3);
        title1 = (TextView) findViewById(R.id.title1);
        title2 = (TextView) findViewById(R.id.title2);
//        title3 = (TextView) findViewById(R.id.title3);
//        setCurIndex(0);
        layout1.setOnClickListener(this);
        layout2.setOnClickListener(this);
//        if (layout3 != null) {
//            layout3.setOnClickListener(this);
//        }
    }

    public void setTabChangeListener(TabChangeListener listener) {
        this.listener = listener;
    }

    public void setLeftTitleText(String leftTitleText) {
        title1.setText(leftTitleText);
    }

    public void setRightTitleText(String rightTitleText) {
        title2.setText(rightTitleText);
    }

//    public void setMidTitleText(String midTitleText) { //当text为空时，隐藏中间的tab
//        if (layout3 == null || title3 == null
//                || Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) { //如果是4.0以下的机器，不出现中间的TAB
//            return;
//        }
//        if (TextUtils.isEmpty(midTitleText)) {
//            title3.setText("");
//            if (curIndex == 2) {
//                setCurIndex(0);
//            }
//            layout3.setVisibility(View.GONE);
//            ViewGroup.LayoutParams lp = layout1.getLayoutParams();
//            lp.width = ViewUtils.dip2px(getContext(), 80);
//            layout1.setLayoutParams(lp);
//            lp = layout2.getLayoutParams();
//            lp.width = ViewUtils.dip2px(getContext(), 80);
//            layout2.setLayoutParams(lp);
//        } else {
//            title3.setText(midTitleText);
//            layout3.setVisibility(View.VISIBLE);
//            ViewGroup.LayoutParams lp = layout1.getLayoutParams();
//            lp.width = ViewUtils.dip2px(getContext(), 68);
//            layout1.setLayoutParams(lp);
//            lp = layout2.getLayoutParams();
//            lp.width = ViewUtils.dip2px(getContext(), 68);
//            layout2.setLayoutParams(lp);
//        }
//
//    }

    /**
     * @param index
     * @return true if change, false if no change
     */
    public boolean setCurIndex(int index) {
        if (curIndex == index) {
            return false;
        }
        if (index == 0) {
            curIndex = index;

            layout1.setBackgroundResource(R.drawable.tab_left_pressed);
            title1.setTextColor(getResources().getColor(R.color.light_brown));

            layout2.setBackgroundResource(R.drawable.tab_right_normal);
            title2.setTextColor(getResources().getColor(R.color.light_yellow));
//            if (layout3 != null && title3 != null) {
//                layout3.setBackgroundResource(R.drawable.tab_mid_normal);
//                title3.setTextColor(getResources().getColor(R.color.titlebar_action_hint_text_color));
//            }
        } else if (index == 1) {
            curIndex = index;

            layout1.setBackgroundResource(R.drawable.tab_left_normal);
            title1.setTextColor(getResources().getColor(R.color.light_yellow));

            layout2.setBackgroundResource(R.drawable.tab_right_pressed);
            title2.setTextColor(getResources().getColor(R.color.light_brown));
//            if (layout3 != null && title3 != null) {
//                layout3.setBackgroundResource(R.drawable.tab_mid_normal);
//                title3.setTextColor(
//                        getResources().getColor(R.color.titlebar_action_hint_text_color));
//            }
//        }  else if (index == 2) {
//            curIndex = index;
//
//            layout1.setBackgroundResource(R.drawable.tab_left_normal);
//            title1.setTextColor(getResources().getColor(R.color.titlebar_action_hint_text_color));
//
//            layout2.setBackgroundResource(R.drawable.tab_right_normal);
//            title2.setTextColor(getResources().getColor(R.color.titlebar_action_hint_text_color));

//            layout3.setBackgroundResource(R.drawable.tab_mid_press);
//            title3.setTextColor(Color.WHITE);
        }
        if (listener != null) {
            post(new Runnable() {
                @Override
                public void run() {
                    listener.onTabChanged(curIndex);
                }
            });
        }
        return true;
    }

    public int getCurrentIndex() {
        return curIndex;
    }

    @Override
    public void onClick(View v) {

        if (v == layout1) {
            setCurIndex(0);
        } else if (v == layout2) {
            setCurIndex(1);
        }

//        else if (v == layout3) {
//            setCurIndex(2);
//        }

    }

    public interface TabChangeListener {
        void onTabChanged(int curIndex);
    }

}
