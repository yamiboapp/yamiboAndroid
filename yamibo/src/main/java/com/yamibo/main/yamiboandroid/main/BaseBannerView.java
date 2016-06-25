package com.yamibo.main.yamiboandroid.main;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.yamibo.main.yamiboandroid.R;
import com.yamibo.main.yamibolib.Utils.Environment;
import com.yamibo.main.yamibolib.Utils.Log;
import com.yamibo.main.yamibolib.Utils.ViewUtils;
import com.yamibo.main.yamibolib.app.YMBActivity;
import com.yamibo.main.yamibolib.widget.YMBNetworkImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 广告banner控件
 * 如果希望实现轮播效果，则按照以下方式添加条目：
 * originSet：原始容器 newSet：新容器
 * 1、生成一个新的newSet，size为originSet.size + 2的容器
 * 2、在newSet容器位置0处，添加originSet[originSize-1]的元素
 * 3、按顺序添加originSet剩余元素
 * 4、最后，添加originSet[0]的元素
 * 5、使用方法updateBannerView()添加View，参数isLoopable设置为true，或者直接使用不带参数isLoopable的同名方法
 * 即可实现轮播效果
 * <p/>
 * setOnDragListener方法可以设置banner页切换的外部监听（包括左右，轮播切换）
 */
public class BaseBannerView extends FrameLayout implements ViewPager.OnPageChangeListener {

    public static final int ANNOUNCELAY_HEAD_ID = 1111;
    private static final int MSG_AUTO_FLIP = 1001;
    private static final int AUTO_FLIP_INTERVAL = 5000;
    /**
     * 关闭公告按钮
     */
    protected ImageView mBtnClose;
    protected List<View> mImageViews = new ArrayList<View>();
    long mLastTouchUpTime;
    protected int HACK_ITEM_COUNT = 0;
    protected NavigationDot mNaviDot;
    protected ViewPager mPager;
    private Handler mHandler;
    private OnDragListener mDragListener;
    private OnPageChangedListener mPageChangedListener;
    private YMBActivity mymbActivity;
    private int pageIndex;

    public BaseBannerView(Context context) {
        this(context, null);
    }

    public BaseBannerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setVisibility(View.GONE);

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG_AUTO_FLIP:
                        autoFlip();
                        startAutoFlip();
                        break;

                    default:
                        break;
                }
            }
        };

        mymbActivity = (YMBActivity) context;

        initView(context);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        startAutoFlip();
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopAutoFlip();
    }

    public void setAnnounce(JSONArray announces, boolean isLoopable) {
        if ((announces == null) || (announces.length() == 0)) {
            setVisibility(GONE);
            return;
        }
        setVisibility(View.VISIBLE);

        HACK_ITEM_COUNT = isLoopable ? 2 : 0;

        int totalCount = announces.length();

        int size = totalCount > 1 && HACK_ITEM_COUNT == 2 ? totalCount + 2 : totalCount;

        for (int i = 0; i < size; i++) {
            try {
                JSONObject announce;
                if (totalCount > 1 && HACK_ITEM_COUNT == 2) {
                    if (i == 0) {
                        announce = announces.getJSONObject(totalCount - 1);
                    } else if (i == size - 1) {
                        announce = announces.getJSONObject(0);
                    } else {
                        announce = announces.getJSONObject(i - 1);
                    }
                } else {
                    announce = announces.getJSONObject(i);
                }

                final String content = Environment.DOMAIN_ADDRESS + announce.optString("url");
                String imageUrl = Environment.DOMAIN_ADDRESS + "data/attachment/" + announce.optString("thumbpath");
                int id = announce.optInt("id");

                View announcementItem = null;
                if (mImageViews.size() <= i || mImageViews.get(i) == null) {
                    announcementItem = new AdaptiveNetworkImageView(getContext());
                    if (mImageViews.size() > i)
                        mImageViews.remove(i);
                    mImageViews.add(i, announcementItem);
                } else {
                    announcementItem = mImageViews.get(i);
                }
                ((YMBNetworkImageView) announcementItem).setImageUri(imageUrl);
                if (!TextUtils.isEmpty(content)) {
                    announcementItem.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("ymb://web?url=" + content)));
                        }
                    });
                }

            } catch (JSONException e) {
                Log.e(getClass().getName(), "", e);
            }
        }
        mNaviDot.setTotalDot(totalCount);
        if (totalCount == 1) {
            mNaviDot.setVisibility(GONE);
        } else {
            mNaviDot.setVisibility(VISIBLE);
        }

        for (int i = mImageViews.size() - 1; i >= size; i--) {
            mImageViews.remove(i);
        }

        mPager.getAdapter().notifyDataSetChanged();
        mPager.setCurrentItem(isLoopable && totalCount > 1 ? 1 : 0);
    }

    public void updateBannerView(int bannerCount, ArrayList<View> bannerViews) {
        updateBannerView(bannerCount, bannerViews, true);
    }

    /*
     * @isLoopable 是否轮播
     */
    public void updateBannerView(int bannerCount, ArrayList<View> bannerViews, boolean isLoopable) {
        HACK_ITEM_COUNT = isLoopable ? 2 : 0;

        mNaviDot.setTotalDot(bannerCount);
        mNaviDot.setVisibility(bannerCount > 1 ? View.VISIBLE : View.GONE);

        if (bannerViews == null) {
            mImageViews.clear();
        } else {
            mImageViews = (ArrayList) bannerViews.clone();
        }

        mPager.getAdapter().notifyDataSetChanged();
        mPager.setCurrentItem(isLoopable && mImageViews.size() > 1 ? 1 : 0);
    }

    void autoFlip() {
        if ((mymbActivity == null || !mymbActivity.isResumed)) {
            // Activity不在前台的时候禁止自动滑动
            return;
        }

        // 距离用户最近一次触摸松开的时间在自动滑动间隔之内
        if ((SystemClock.elapsedRealtime() - mLastTouchUpTime) < AUTO_FLIP_INTERVAL) {
            return;
        }

        int position = mPager.getCurrentItem() + 1;
        if (position >= mPager.getAdapter().getCount()) { // 滑到底就再滑到开头
            position = 0;
        }
        mPager.setCurrentItem(position);
    }

    public void startAutoFlip() {
        stopAutoFlip();
        if (mImageViews.size() < 2) {
            return;
        }

        mHandler.sendEmptyMessageDelayed(MSG_AUTO_FLIP, AUTO_FLIP_INTERVAL);
    }

    public void stopAutoFlip() {
        mHandler.removeMessages(MSG_AUTO_FLIP);
    }

    protected void initView(Context context) {
        FrameLayout layHeader = new FrameLayout(getContext());
        layHeader.setId(R.id.banner);
        layHeader.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT));

        mPager = new MyPager(context);
        mPager.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL));
        mPager.setAdapter(new MyPagerAdapter());
        mPager.setOnPageChangeListener(this);
        layHeader.addView(mPager);

        mBtnClose = new ImageView(context);
        mBtnClose.setImageResource(R.drawable.btn_close);
        mBtnClose.setScaleType(ImageView.ScaleType.CENTER);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER_VERTICAL | Gravity.RIGHT);
        params.setMargins(0, 0, ViewUtils.dip2px(getContext(), 10), 0);
        mBtnClose.setLayoutParams(params);

//        layHeader.addView(mBtnClose);
        // mBtnClose.setOnClickListener(new OnCloseBtnClickListener());

        mNaviDot = new NavigationDot(getContext(), true);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM + Gravity.RIGHT);
        lp.setMargins(ViewUtils.dip2px(context, 10), 0, 0, ViewUtils.dip2px(context, 6));
        mNaviDot.setLayoutParams(lp);
        layHeader.addView(mNaviDot);

        addView(layHeader);
    }

    public void setCloseDrawable(int id) {
        mBtnClose.setImageResource(id);
    }

    public void setNavigationDotNormalDrawable(int id) {
        mNaviDot.setDotNormalId(id);
    }

    public void setNavigationDotPressedDrawable(int id) {
        mNaviDot.setDotPressedId(id);
    }

    public void setBtnOnCloseListener(View.OnClickListener listener) {
        if (mBtnClose != null) {
            mBtnClose.setOnClickListener(listener);
        }
    }

    public void setNaviDotGravity(int gravity) {
        final Context context = getContext();
        FrameLayout.LayoutParams lp = null;
        switch (gravity) {
            case Gravity.LEFT:
                lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT,
                        Gravity.BOTTOM + Gravity.LEFT);
                lp.setMargins(ViewUtils.dip2px(context, 10), 0, 0, ViewUtils.dip2px(context, 6));
                break;
            case Gravity.CENTER:
                lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT,
                        Gravity.BOTTOM + Gravity.CENTER_HORIZONTAL);
                lp.setMargins(0, 0, 0, ViewUtils.dip2px(context, 6));
                break;
            case Gravity.RIGHT:
                lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT,
                        Gravity.BOTTOM + Gravity.RIGHT);
                lp.setMargins(0, 0, ViewUtils.dip2px(context, 10), ViewUtils.dip2px(context, 6));
                break;
            case Gravity.CENTER_VERTICAL + Gravity.RIGHT:
                lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT,
                        Gravity.CENTER_VERTICAL + Gravity.RIGHT);
                lp.setMargins(0, 0, ViewUtils.dip2px(context, 10), 0);
                break;
            default:
                break;
        }
        mNaviDot.setLayoutParams(lp);
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        if (state == ViewPager.SCROLL_STATE_IDLE) {
            if (mPager.getCurrentItem() != pageIndex) {
                mPager.setCurrentItem(pageIndex, false);
            }
        }
    }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {
        if (mDragListener != null) {
            mDragListener.onDraged();//外部滑动事件响应
        }
    }

    @Override
    public void onPageSelected(int i) {
        pageIndex = i;
        final int imageViewSize = mImageViews.size();
        if (HACK_ITEM_COUNT == 2 && imageViewSize > 1) {
            if (i == 0) {// 当视图在第一个时，将页面号设置为图片的最后一张
                pageIndex = imageViewSize - HACK_ITEM_COUNT;
            } else if (i == mImageViews.size() - 1) {// 当视图在最后一个时,将页面号设置为图片的第一张
                pageIndex = 1;
            }
        }
        int index = (i - 1) % (imageViewSize - HACK_ITEM_COUNT);
        if (HACK_ITEM_COUNT == 2 && index == -1 && imageViewSize > 2) {//循环轮播Dot切换修正
            index = imageViewSize - HACK_ITEM_COUNT - 1;
        }
        mNaviDot.setCurrentIndex(index);

        if (mPageChangedListener != null) {
            mPageChangedListener.onChanged(index);
        }
    }

    public void setOnDragListener(OnDragListener listener) {
        mDragListener = listener;
    }

    public void setOnPageChangedListener(OnPageChangedListener listener) {
        mPageChangedListener = listener;
    }

    public interface OnDragListener {//banner滑动事件监听

        void onDraged();
    }

    public interface OnPageChangedListener {//banner页改变事件监听

        void onChanged(int index);
    }

    class MyPager extends ViewPager {

        int itemHeight = Integer.MIN_VALUE;

        private GestureDetector mGestureDetector;

        public MyPager(Context context) {
            this(context, null);
        }

        public MyPager(Context context, AttributeSet attrs) {
            super(context, attrs);

            mGestureDetector = new GestureDetector(context, new MyGestureListener());
            setFadingEdgeLength(0);
        }

        @Override
        public boolean dispatchTouchEvent(MotionEvent ev) {
            mLastTouchUpTime = SystemClock.elapsedRealtime();

            if (mGestureDetector.onTouchEvent(ev)) {
                getParent().requestDisallowInterceptTouchEvent(true);
            }
            return super.dispatchTouchEvent(ev);
        }

        class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                if (Math.abs(distanceY) < Math.abs(distanceX)) {
                    return true;
                }
                return false;
            }
        }
    }

    class MyPagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return mImageViews.size();
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return (arg0 == arg1);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            if (mImageViews.get(position).getParent() == null) {
                container.addView(mImageViews.get(position));
            }

            return mImageViews.get(position);
        }

    }

    class AdaptiveNetworkImageView extends YMBNetworkImageView {
        public AdaptiveNetworkImageView(Context context) {
            this(context, null);
        }

        public AdaptiveNetworkImageView(Context context, AttributeSet attrs) {
            this(context, attrs, 0);
        }

        public AdaptiveNetworkImageView(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
        }

        @Override
        public void setImageBitmap(Bitmap bmp) {
            int width = bmp.getWidth();
            int height = bmp.getHeight();
            int newWidth = ViewUtils.getScreenWidthPixels(getContext());
            Bitmap newBmp = Bitmap.createScaledBitmap(bmp, newWidth, (height * newWidth) / width,
                    true);
//            if (newBmp != bmp) {
//                if (!bmp.isRecycled()) {
//                    bmp.recycle();
//                }
//            }

            ((MyPager) mPager).itemHeight = newBmp.getHeight() > ((MyPager) mPager).itemHeight ? newBmp.getHeight()
                    : ((MyPager) mPager).itemHeight;

            super.setImageBitmap(newBmp);
            mPager.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                    ((MyPager) mPager).itemHeight, Gravity.CENTER_HORIZONTAL));
        }
    }
}
