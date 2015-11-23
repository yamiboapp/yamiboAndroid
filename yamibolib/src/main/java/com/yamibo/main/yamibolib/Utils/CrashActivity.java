package com.yamibo.main.yamibolib.Utils;

import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import com.yamibo.main.yamibolib.app.YMBActivity;

/**
 * Created by wangxiaoyan on 15/11/22.
 */
public class CrashActivity extends YMBActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ScrollView scrollView = new ScrollView(this);
        TextView textView = new TextView(this);
        textView.setLayoutParams(new ScrollView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        scrollView.addView(textView);
        String crash = CrashReportHelper.getReport();
        if (crash == null) {
            crash = CrashReportHelper.getReportBak();
        }

        if (crash == null) {
            crash = "没有可显示的crash";
        }
        textView.setText(crash);
        setContentView(scrollView);
    }
}
