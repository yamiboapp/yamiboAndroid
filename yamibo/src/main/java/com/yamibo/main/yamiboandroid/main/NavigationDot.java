package com.yamibo.main.yamiboandroid.main;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;

import com.yamibo.main.yamiboandroid.R;


public class NavigationDot extends View {
    protected static final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    protected final int padding;
    protected int totalDot;
    protected int currentDot;
    protected Bitmap dotNormal;
    protected Bitmap dotPressed;
    protected int dot_width;
    protected int dot_height;
    protected int width;
    long deleayedTime = 0;
    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    moveToNext();
                    sendEmptyMessageDelayed(1, deleayedTime);
                    break;

                default:
                    break;
            }
        }

    };
    private boolean isLoop;

    public NavigationDot(Context context) {
        this(context, null);
    }

    public NavigationDot(Context context, AttributeSet attrs) {
        super(context, attrs);
        Resources res = getResources();
        dotPressed = BitmapFactory.decodeResource(res, R.drawable.navigation_dot_pressed);
        dotNormal = BitmapFactory.decodeResource(res, R.drawable.navigation_dot_normal);

        if (dotNormal != null) {
            dot_width = dotNormal.getWidth();
            dot_height = dotNormal.getHeight();
        }

        padding = dip2px(context, 6);
    }

    public NavigationDot(Context context, boolean isLoop) {
        this(context, null);
        this.isLoop = isLoop;
    }

    public static int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) ((dipValue * scale) + 0.5f);
    }

    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) ((pxValue / scale) + 0.5f);
    }

    public void setDotNormalId(int dotNormalId) {
        dotNormal = BitmapFactory.decodeResource(getResources(), dotNormalId);
        dot_width = dotNormal.getWidth();
        dot_height = dotNormal.getHeight();
    }

    public void setDotPressedId(int dotPressedId) {
        dotPressed = BitmapFactory.decodeResource(getResources(), dotPressedId);
    }

    public void setDotNormalBitmap(Bitmap dotNormalBitmap) {
        dotNormal = dotNormalBitmap;
        dot_width = dotNormal.getWidth();
        dot_height = dotNormal.getHeight();
    }

    public void setDotPressedBitmap(Bitmap dotPressedBitmap) {
        dotPressed = dotPressedBitmap;
    }

    public void setTotalDot(int dotNum) {
        if (dotNum > 0) {
            totalDot = dotNum;
            requestLayout();
        }
    }

    public void setCurrentIndex(int index) {
        // Log.i("xxx",index + " : " + totalDot);
        if ((index < 0) || (index > totalDot)) {
            return;
        }
        if (currentDot != index) {
            currentDot = index;
            invalidate();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(measureWidth(widthMeasureSpec), measureHeight(heightMeasureSpec));
        // setMeasuredDimension((dot_width + padding) * totalDot,
        // dot_height);
    }

    private int measureWidth(int measureSpec) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        // Log.i("xxx", specSize + "");
        if (specMode == MeasureSpec.EXACTLY) {
            // We were told how big to be
            result = specSize;
        } else {
            // Measure the text
            result = ((dot_width + padding) * totalDot) + getPaddingLeft() + getPaddingRight();
            ;
            if (specMode == MeasureSpec.AT_MOST) {
                // Respect AT_MOST value if that was what is called for by
                // measureSpec
                result = Math.min(result, specSize);
            }
        }
        width = result;
        return result;
    }

    private int measureHeight(int measureSpec) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            // We were told how big to be
            result = specSize;
        } else {
            // Measure the text (beware: ascent is a negative number)
            result = dot_height + getPaddingTop() + getPaddingBottom();
            if (specMode == MeasureSpec.AT_MOST) {
                // Respect AT_MOST value if that was what is called for by
                // measureSpec
                result = Math.min(result, specSize);
            }
        }
        return result;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.translate(getPaddingLeft(), getPaddingTop());
        // int previousColor = paint.getColor();
        // paint.setColor(Color.argb(150, 150, 150, 150));
        // canvas.drawRoundRect(new RectF(0, 0, width, height), 5f, 5f, paint);
        // canvas.drawRect(0, 0, (dot_width + padding) * totalDot,
        // dot_height, paint2);
        // paint.setColor(previousColor);
        int dotsWidth = (dot_width * totalDot) + (padding * (totalDot - 1));
        int startDrawDot = (width - dotsWidth) / 2;
        for (int i = 0; i < totalDot; i++) {
            Bitmap bmp;
            if (currentDot == i) {
                bmp = dotPressed;
            } else {
                bmp = dotNormal;
            }
            canvas.drawBitmap(bmp, startDrawDot + ((dot_width + padding) * i), 0, paint);
        }
    }

    public void moveToPosition(int position) {
        if (position > totalDot) {
            return;
        }
        currentDot = position;
        invalidate();
    }

    public void moveToNext() {
        if (currentDot >= totalDot) {
            if (!isLoop) {
                return;
            }
        }
        currentDot = ++currentDot % totalDot;
        invalidate();
    }

    public void moveToPrevious() {
        if (currentDot <= 0) {
            if (!isLoop) {
                return;
            }
            currentDot = totalDot;
        }
        currentDot--;
        invalidate();
    }

    /**
     * 自动轮播
     */
    public void startFlipping() {
        deleayedTime = 500;
        handler.sendEmptyMessageDelayed(1, deleayedTime);
    }

    public void setFlipInterval(int milliseconds) {
        deleayedTime = milliseconds > 0 ? milliseconds : 500;
    }

    public void stopFlipping() {
        handler.removeMessages(1);
    }
}
