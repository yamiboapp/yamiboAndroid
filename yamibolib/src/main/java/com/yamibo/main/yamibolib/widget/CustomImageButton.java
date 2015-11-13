package com.yamibo.main.yamibolib.widget;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;

/**
 * Created with IntelliJ IDEA.
 * User: yixing
 * Date: 14-7-4
 * Time: 下午1:57
 */
public class CustomImageButton extends ImageButton implements View.OnTouchListener {
    public CustomImageButton(Context context) {
        super(context);

        setOnTouchListener(this);
    }

    public CustomImageButton(Context context, AttributeSet attrs) {
        super(context, attrs);

        setOnTouchListener(this);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        // 按下
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            CustomImageButton.this.setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
        }

        // 抬起
        if (event.getAction() == MotionEvent.ACTION_UP
                || event.getAction() == MotionEvent.ACTION_CANCEL) {
            CustomImageButton.this.setColorFilter(null);
        }
        return false;
    }
}
