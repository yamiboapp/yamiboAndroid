package com.yamibo.main.yamibolib.widget;

import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yamibo.main.yamibolib.R;


public class CustomEditText extends LinearLayout implements TextWatcher, OnClickListener {

    public ImageView mIcon;
    public TextView mTitle;
    public EditText mEdit;
    public ImageButton mClear;

    public CustomEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mTitle = (TextView) findViewById(R.id.title);
        mIcon = (ImageView) findViewById(R.id.icon);

        mEdit = (EditText) findViewById(R.id.edit_text);
        mEdit.addTextChangedListener(this);

        mClear = (ImageButton) findViewById(R.id.edit_clear);
        mClear.setOnClickListener(this);
    }

    @Override
    public void afterTextChanged(Editable arg0) {
    }

    @Override
    public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (TextUtils.isEmpty(s)) {
            mClear.setVisibility(View.GONE);
        } else {
            mClear.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClick(View v) {
        if (v == mClear) {
            mEdit.setText("");
        }
    }
}
