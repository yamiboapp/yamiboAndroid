package com.yamibo.main.yamibolib.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.LruImageCache;
import com.android.volley.toolbox.Volley;
import com.yamibo.main.yamibolib.app.YMBApplication;

/**
 * Created by wangxiaoyan on 15/11/21.
 */
public class YMBNetworkImageView extends com.android.volley.toolbox.NetworkImageView {

    private static ImageLoader mImageLoader;


    private Bitmap mLocalBitmap;

    private int mLocalResourceId;

    private boolean mShowLocal;

    public YMBNetworkImageView(Context context) {
        this(context, null);
    }

    public YMBNetworkImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public YMBNetworkImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setImageUri(String url) {
        mShowLocal = false;
        setImageUrl(url, getImageLoader());
    }

    private ImageLoader getImageLoader() {
        if (mImageLoader == null) {
            RequestQueue queue = Volley.newRequestQueue(YMBApplication.instance());
            LruImageCache lruImageCache = LruImageCache.instance();
            mImageLoader = new ImageLoader(queue, lruImageCache);
        }
        return mImageLoader;
    }

    public void setLocalImageBitmap(Bitmap bitmap) {
        if (bitmap != null) {
            mShowLocal = true;
            this.mLocalBitmap = bitmap;
            resetImageContainerAndUrl();
            requestLayout();
        }

    }

    public void setLocalResourceId(int ResId) {
        if (ResId != 0) {
            mShowLocal = true;
            this.mLocalResourceId = ResId;
            resetImageContainerAndUrl();
            requestLayout();
        }
    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {

        super.onLayout(changed, left, top, right, bottom);
        if (mShowLocal) {
            if (mLocalBitmap != null) {
                setImageBitmap(mLocalBitmap);
            } else if (mLocalResourceId != 0) {
                setImageResource(mLocalResourceId);
            }
        }
    }
}
