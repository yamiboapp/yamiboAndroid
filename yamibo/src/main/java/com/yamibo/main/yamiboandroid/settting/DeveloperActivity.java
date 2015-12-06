package com.yamibo.main.yamiboandroid.settting;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.yamibo.main.yamiboandroid.R;
import com.yamibo.main.yamibolib.accountservice.AccountListener;
import com.yamibo.main.yamibolib.app.YMBActivity;

/**
 * Created by Clover on 2015-12-05.
 */
public class DeveloperActivity extends YMBActivity implements View.OnClickListener {

    private TextView authors=null;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_developer);
        authors=(TextView)findViewById(R.id.authors);
    }

    @Override
    public void onClick(View view) {

    }
}
