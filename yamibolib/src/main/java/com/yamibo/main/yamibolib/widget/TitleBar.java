package com.yamibo.main.yamibolib.widget;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yamibo.main.yamibolib.R;
import com.yamibo.main.yamibolib.Utils.ViewUtils;

import java.lang.reflect.Method;

/**
 * Created by feifengxu on 13-9-1.<br/>
 * <p/>
 * 自定义TitleBar<br/>
 * 为兼容老的Activity,定义了九种废弃的Style<br/>
 * 新增Activity需使用 TITLE_TYPE_STANDARD<br/>
 * 具体用法可以参考Debug面板的UICatalog--->DPTitleBarActivity.java
 */
public class TitleBar {

    public static final int TITLE_TYPE_STANDARD = 100;

    @Deprecated
    public static final int TITLE_TYPE_WIDE = 1;
    @Deprecated
    public static final int TITLE_TYPE_NONE = 2;
    @Deprecated
    public static final int TITLE_TYPE_ARROW = 3;
    @Deprecated
    public static final int TITLE_TYPE_EDIT_SEARCH = 4;
    @Deprecated
    public static final int TITLE_TYPE_BUTTON_SEARCH = 5;
    @Deprecated
    public static final int TITLE_TYPE_DOUBLE_LINE = 6;
    @Deprecated
    public static final int TITLE_TYPE_DOUBLE_LINE_PROGRESS = 7;
    @Deprecated
    public static final int TITLE_TYPE_FILTER = 8;
    @Deprecated
    public static final int TITLE_TYPE_DOUBLE_TEXT_BUTTON = 9;

    private Style mStyle;

    private TitleBar(Style style) {
        mStyle = style;
    }

    public static TitleBar build(Activity activity, final int titleTypeOrResId) {

        switch (titleTypeOrResId) {
            case TITLE_TYPE_STANDARD:
                return new StandardStyle(activity).build();
            case TITLE_TYPE_WIDE:
                return new WideStyle(activity).build();
            case TITLE_TYPE_ARROW:
                return new ArrowStyle(activity).build();
            case TITLE_TYPE_NONE:
                return new NoTitleStyle(activity).build();
            case TITLE_TYPE_EDIT_SEARCH:
                return new EditSearchStyle(activity).build();
            case TITLE_TYPE_BUTTON_SEARCH:
                return new ButtonSearchStyle(activity).build();
            case TITLE_TYPE_DOUBLE_LINE:
                return new DoubleLineStyle(activity).build();
            case TITLE_TYPE_DOUBLE_LINE_PROGRESS:
                return new DoubleLineProgressStyle(activity).build();
            case TITLE_TYPE_DOUBLE_TEXT_BUTTON:
                return new DoubleTextStyle(activity).build();
            case TITLE_TYPE_FILTER:
                return new FilterStyle(activity).build();
            default:
                return new Style(activity) {
                    @Override
                    public TitleBar build() {
                        TitleBar t = new TitleBar(this);
                        mActivity.getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
                                titleTypeOrResId);
                        return t;
                    }

                    @Override
                    public void setTitle(CharSequence title) {

                    }

                    @Override
                    public void setSubTitle(CharSequence title) {

                    }
                }.build();
        }
    }

    /**
     * 自定义标题栏背景
     */
    public void setBackgournd(Drawable drawable) {
        if (!(mStyle instanceof StandardStyle)) {
            throw new RuntimeException("只支持TitleBar类型为TITLE_TYPE_STANDORD");
        }
        ((StandardStyle) mStyle).rootView.setBackgroundDrawable(drawable);
    }

    /**
     * 设置默认标题栏左边按钮点击事件
     */
    public void setLeftView(View.OnClickListener l) {
        setLeftView(0, l);
    }

    /**
     * 设置默认标题栏左边按钮drawable及点击事件
     */
    public void setLeftView(int drawableId, View.OnClickListener l) {

        if (mStyle instanceof StandardStyle) {
            ((StandardStyle) mStyle).setLeftView(drawableId, l);
            return;
        }

        if (mStyle.findViewById(R.id.left_title_button) == null) {
            return;
        }
        if (drawableId == -1) {
            mStyle.findViewById(R.id.left_title_button).setVisibility(View.GONE);
        } else {
            if (drawableId > 0) {
                ((ImageView) mStyle.findViewById(R.id.left_title_button)).setImageResource(drawableId);
            }
            mStyle.findViewById(R.id.left_title_button).setOnClickListener(l);
        }
    }

    /**
     * 设置默认标题栏空白区域双击事件监听器，主要用于双击标题栏使界面滚回顶部
     * 参数：TitleBar.OnDoubleClickListener接口对象，需实现接口中onDoubleClick方法
     */
    public void setOnDoubleClickListener(OnDoubleClickListener listener) {
        mStyle.setOnDoubleClickListener(listener);
    }

    /**
     * 自定义标题栏左边按钮
     */
    public void setCustomLeftView(View view) {
        if (!(mStyle instanceof StandardStyle)) {
            throw new RuntimeException("只支持TitleBar类型为TITLE_TYPE_STANDORD");
        }

        ((StandardStyle) mStyle).leftViewContainer.removeAllViews();
        ((StandardStyle) mStyle).leftViewContainer.addView(view);
    }

    /**
     * 自定义标题栏中间视图
     */
    public void setCustomContentView(View view) {
        if (!(mStyle instanceof StandardStyle)) {
            throw new RuntimeException("只支持TitleBar类型为TITLE_TYPE_STANDORD");
        }
        ((StandardStyle) mStyle).contentViewContainer.removeAllViews();
        ((StandardStyle) mStyle).contentViewContainer.addView(view);
    }

    /**
     * 增加标题栏右边自定义按钮
     *
     * @param tag
     * @param drawableId
     * @param listener   如果tag为新增，根据字符串从左到右排序<br>
     *                   如果tag为已有，则替换原先的view<br>
     *                   可以通过tag找到该View和删除该View<br>
     *                   新打点中tag会被作为element_id计入打点,如外部设置则会覆盖tag
     * @see #findRightViewItemByTag
     * @see #removeRightViewItem
     */
    public View addRightViewItem(String tag, int drawableId, View.OnClickListener listener) {
        Drawable drawable = mStyle.mActivity.getResources().getDrawable(drawableId);
        return addRightViewItem(tag, drawable, listener);
    }

    /**
     * 增加标题栏右边自定义按钮
     *
     * @param tag
     * @param drawable
     * @param listener 如果tag为新增，根据字符串从左到右排序<br>
     *                 如果tag为已有，则替换原先的view<br>
     *                 可以通过tag找到该View和删除该View<br>
     *                 新打点中tag会被作为element_id计入打点,如外部设置则会覆盖tag
     * @see #findRightViewItemByTag
     * @see #removeRightViewItem
     */
    private View addRightViewItem(String tag, Drawable drawable, View.OnClickListener listener) {
        if (!(mStyle instanceof StandardStyle)) {
            throw new RuntimeException("只支持TitleBar类型为TITLE_TYPE_STANDORD");
        }
        if (drawable == null) {
            return null;
        }

        ImageView imageButton = new ImageView(mStyle.mActivity);
        final int ImageViewMargin = ViewUtils.dip2px(mStyle.mActivity, 20);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(ImageViewMargin, 0, 0, 0);
        imageButton.setBackgroundResource(android.R.color.transparent);
        imageButton.setImageDrawable(drawable);
        imageButton.setLayoutParams(layoutParams);
        imageButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // 按下
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    // ((ImageView)v).setColorFilter(Color.GRAY,
                    // PorterDuff.Mode.MULTIPLY);
                    ((ImageView) v).setAlpha(123);
                }

                // 抬起
                if (event.getAction() == MotionEvent.ACTION_UP
                        || event.getAction() == MotionEvent.ACTION_CANCEL) {
                    // ((ImageView)v).setColorFilter(null);
                    ((ImageView) v).setAlpha(255);
                }
                return false;
            }
        });
        return addRightViewItem(imageButton, tag, listener);
    }

    /**
     * 增加标题栏右边自定义按钮
     * textView id: title_bar_right_tv
     *
     * @param text     文字类的右边按钮
     * @param tag
     * @param listener 如果tag为新增，根据字符串从左到右排序<br>
     *                 如果tag为已有，则替换原先的view<br>
     *                 可以通过tag找到该View和删除该View<br>
     *                 新打点中tag会被作为element_id计入打点,如外部设置则会覆盖tag
     * @see #findRightViewItemByTag
     * @see #removeRightViewItem
     */
    public View addRightViewItem(String text, String tag, View.OnClickListener listener) {
        if (TextUtils.isEmpty(text)) {
            return null;
        }
        TextView textView = (TextView) LayoutInflater.from(mStyle.mActivity).inflate(
                R.layout.title_bar_text, null, false);
        textView.setText(text);
        return addRightViewItem(textView, tag, listener);
    }

    /**
     * 增加标题栏右边自定义按钮
     *
     * @param view
     * @param tag
     * @param listener 如果tag为新增，根据字符串从左到右排序<br>
     *                 如果tag为已有，则替换原先的view<br>
     *                 可以通过tag找到该View和删除该View<br>
     *                 新打点中tag会被作为element_id计入打点,如外部设置则会覆盖tag
     * @see #findRightViewItemByTag
     * @see #removeRightViewItem
     */
    public View addRightViewItem(View view, String tag, View.OnClickListener listener) {
        if (!(mStyle instanceof StandardStyle)) {
            throw new RuntimeException("只支持TitleBar类型为TITLE_TYPE_STANDORD");
        }

        if (view == null) {
            return null;
        }

        view.setOnClickListener(listener);

        if (TextUtils.isEmpty(tag)) {
            ((StandardStyle) mStyle).rightViewContainer.addView(view,
                    ((StandardStyle) mStyle).rightViewContainer.getChildCount());
        } else {
            try {
                Method setGAString = view.getClass().getMethod("setGAString", String.class);
                setGAString.invoke(view, tag);
            } catch (Exception e) {
            }
            view.setTag(Integer.MAX_VALUE, tag);
            View child = findRightViewItemByTag(tag);
            if (child != null) {
                int index = ((StandardStyle) mStyle).rightViewContainer.indexOfChild(child);
                ((StandardStyle) mStyle).rightViewContainer.removeView(child);
                ((StandardStyle) mStyle).rightViewContainer.addView(view, index);
            } else {
                int index = 0;
                for (int i = 0; i < ((StandardStyle) mStyle).rightViewContainer.getChildCount(); i++) {
                    View c = ((StandardStyle) mStyle).rightViewContainer.getChildAt(i);
                    if (tag.compareTo((String) c.getTag(Integer.MAX_VALUE)) > 0) {
                        index = i + 1;
                        break;
                    }
                }
                ((StandardStyle) mStyle).rightViewContainer.addView(view, index);
            }
        }
        return view;
    }

    /**
     * 通过tag查找标题栏右边自定义按钮
     */
    public View findRightViewItemByTag(String tag) {
        if (!(mStyle instanceof StandardStyle)) {
            throw new RuntimeException("只支持TitleBar类型为TITLE_TYPE_STANDORD");
        }

        if (TextUtils.isEmpty(tag)) {
            return null;
        }

        for (int i = 0; i < ((StandardStyle) mStyle).rightViewContainer.getChildCount(); i++) {
            View childView = ((StandardStyle) mStyle).rightViewContainer.getChildAt(i);
            if (tag.equals(childView.getTag(Integer.MAX_VALUE))) {
                return childView;
            }
        }
        return null;
    }

    /**
     * 通过tag删除标题栏右边自定义按钮
     */
    public void removeRightViewItem(String tag) {
        if (!(mStyle instanceof StandardStyle)) {
            throw new RuntimeException("只支持TitleBar类型为TITLE_TYPE_STANDORD");
        }

        View view = findRightViewItemByTag(tag);
        if (view != null) {
            ((StandardStyle) mStyle).rightViewContainer.removeView(view);
        }
    }

    /**
     * 删除所有标题栏右边自定义按钮
     */
    public void removeAllRightViewItem() {
        if (!(mStyle instanceof StandardStyle)) {
            throw new RuntimeException("只支持TitleBar类型为TITLE_TYPE_STANDORD");
        }
        ((StandardStyle) mStyle).rightViewContainer.removeAllViews();
    }

    public void setTitle(CharSequence title) {
        mStyle.setTitle(title);
    }

    public void setSubTitle(CharSequence title) {
        mStyle.setSubTitle(title);
    }

    public View findViewById(int id) {
        return mStyle.findViewById(id);
    }

    public void show() {
        if (mStyle.findViewById(R.id.title_bar) != null) {
            ((View) mStyle.findViewById(R.id.title_bar).getParent()).setVisibility(View.VISIBLE);
        }
    }

    public void hide() {
        if (mStyle.findViewById(R.id.title_bar) != null) {
            ((View) mStyle.findViewById(R.id.title_bar).getParent()).setVisibility(View.GONE);
        }
    }

    private static abstract class Style {
        protected Activity mActivity;
        protected View rootView;

        OnDoubleClickListener doubleClickListener;
        GestureDetector gestureDetector;

        public Style(Activity activity) {
            mActivity = activity;
        }

        public abstract void setTitle(CharSequence title);

        public abstract void setSubTitle(CharSequence title);

        public View findViewById(int resId) {
            if (rootView == null) {
                return null;
            }
            return rootView.findViewById(resId);
        }

        public void setOnDoubleClickListener(OnDoubleClickListener listener) {
            if (rootView == null || mActivity == null)
                return;
            gestureDetector = new GestureDetector(mActivity, new GestureListener());
            rootView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return gestureDetector.onTouchEvent(event);
                }
            });
            doubleClickListener = listener;
        }

        public abstract TitleBar build();

        public class GestureListener extends GestureDetector.SimpleOnGestureListener {
            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                if (doubleClickListener != null)
                    doubleClickListener.onDoubleClick();
                return true;
            }
        }
    }

    public static interface OnDoubleClickListener {
        public void onDoubleClick();
    }

    private static class StandardStyle extends Style {

        ViewGroup leftViewContainer;
        ViewGroup contentViewContainer;
        ViewGroup rightViewContainer;

        private ImageButton leftView;

        private TextView titleView;
        private TextView subTitleView;

        public StandardStyle(Activity activity) {
            super(activity);
            mActivity.getWindow().requestFeature(Window.FEATURE_CUSTOM_TITLE);
            mActivity.setContentView(new ViewStub(mActivity));
        }

        @Override
        public void setTitle(CharSequence title) {
            if (titleView != null) {
                titleView.setText(title);
            }
        }

        @Override
        public void setSubTitle(CharSequence title) {
            if (subTitleView != null) {
                subTitleView.setVisibility(View.VISIBLE);
                subTitleView.setText(title);
            }
            FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) titleView.getLayoutParams();
            lp.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
            titleView.setLayoutParams(lp);
        }

        @Override
        public TitleBar build() {
            TitleBar t = new TitleBar(this);

            mActivity.getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
                    R.layout.standard_title_bar);
            rootView = mActivity.findViewById(R.id.title_bar);
            leftViewContainer = (ViewGroup) rootView.findViewById(R.id.title_bar_left_view_container);
            leftView = (ImageButton) leftViewContainer.findViewById(R.id.left_view);
            contentViewContainer = (ViewGroup) rootView.findViewById(R.id.title_bar_content_container);
            titleView = (TextView) contentViewContainer.findViewById(R.id.title_bar_title);
            subTitleView = (TextView) contentViewContainer.findViewById(R.id.title_bar_subtitle);
            rightViewContainer = (ViewGroup) rootView.findViewById(R.id.title_bar_right_view_container);

            return t;
        }

        public void setLeftView(int resId, View.OnClickListener l) {
            if (leftView != null) {
                if (resId == -1) {
                    leftView.setVisibility(View.GONE);
                } else if (resId > 0) {
                    leftView.setImageResource(resId);
                    leftView.setVisibility(View.VISIBLE);
                } else if (resId == 0) {
                    leftView.setImageResource(R.drawable.title_back);
                    leftView.setVisibility(View.VISIBLE);
                }
                leftView.setOnClickListener(l);
            }
        }
    }

    private static class NoTitleStyle extends Style {

        public NoTitleStyle(Activity activity) {
            super(activity);
            mActivity.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }

        @Override
        public void setTitle(CharSequence title) {
        }

        @Override
        public void setSubTitle(CharSequence title) {
        }

        @Override
        public TitleBar build() {
            TitleBar t = new TitleBar(this);
            return t;
        }

    }

    /**
     * Deprecated
     * 兼容老的Activity，新写的Activity应该使用TITLE_TYPE_STANDARD
     */
    @Deprecated
    private static class WideStyle extends Style {

        public WideStyle(Activity activity) {
            super(activity);
            mActivity.getWindow().requestFeature(Window.FEATURE_CUSTOM_TITLE);
            mActivity.setContentView(new ViewStub(mActivity));
        }

        @Override
        public void setTitle(CharSequence title) {

            if (rootView.findViewById(android.R.id.title) == null) {
                return;
            }
            ((TextView) rootView.findViewById(android.R.id.title)).setText(title);
        }

        @Override
        public void setSubTitle(CharSequence title) {

            if (rootView.findViewById(R.id.subtitle) == null) {
                return;
            }
            ((TextView) rootView.findViewById(R.id.subtitle)).setText(title);
        }

        @Override
        public TitleBar build() {
            TitleBar t = new TitleBar(this);
            mActivity.getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
                    R.layout.wide_title_bar);
            rootView = mActivity.findViewById(R.id.title_bar);
            return t;
        }
    }

    /**
     * Deprecated
     * 兼容老的Activity，新写的Activity应该使用TitleBar样式为TITLE_TYPE_STANDARD
     */
    @Deprecated
    private static class ArrowStyle extends Style {

        public ArrowStyle(Activity activity) {
            super(activity);
            mActivity.getWindow().requestFeature(Window.FEATURE_CUSTOM_TITLE);
            mActivity.setContentView(new ViewStub(mActivity));
        }

        @Override
        public void setTitle(CharSequence title) {

            if (rootView.findViewById(android.R.id.title) == null) {
                return;
            }
            ((TextView) rootView.findViewById(android.R.id.title)).setText(title);
        }

        @Override
        public void setSubTitle(CharSequence title) {

            if (rootView.findViewById(R.id.subtitle) == null) {
                return;
            }
            ((TextView) rootView.findViewById(R.id.subtitle)).setText(title);
        }

        @Override
        public TitleBar build() {
            TitleBar t = new TitleBar(this);
            mActivity.getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
                    R.layout.arrow_title_bar);
            rootView = mActivity.findViewById(R.id.title_bar);
            return t;
        }
    }

    /**
     * Deprecated
     * 兼容老的Activity，新写的Activity应该使用TitleBar样式为TITLE_TYPE_STANDARD
     */
    @Deprecated
    private static class EditSearchStyle extends Style {

        public EditSearchStyle(Activity activity) {
            super(activity);
            mActivity.getWindow().requestFeature(Window.FEATURE_CUSTOM_TITLE);
            mActivity.setContentView(new ViewStub(mActivity));
        }

        @Override
        public void setTitle(CharSequence title) {

            if (rootView.findViewById(android.R.id.title) == null) {
                return;
            }
            ((TextView) rootView.findViewById(android.R.id.title)).setText(title);
        }

        @Override
        public void setSubTitle(CharSequence title) {

            if (rootView.findViewById(R.id.subtitle) == null) {
                return;
            }
            ((TextView) rootView.findViewById(R.id.subtitle)).setText(title);
        }

        @Override
        public TitleBar build() {
            TitleBar t = new TitleBar(this);
            mActivity.getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
                    R.layout.edit_search_title_bar);
            rootView = mActivity.findViewById(R.id.title_bar);
            return t;
        }
    }

    /**
     * Deprecated
     * 兼容老的Activity，新写的Activity应该使用TitleBar样式为TITLE_TYPE_STANDARD
     */
    @Deprecated
    private static class ButtonSearchStyle extends Style {
        public ButtonSearchStyle(Activity activity) {
            super(activity);
            mActivity.getWindow().requestFeature(Window.FEATURE_CUSTOM_TITLE);
            mActivity.setContentView(new ViewStub(mActivity));
        }

        @Override
        public void setTitle(CharSequence title) {

            if (rootView.findViewById(android.R.id.title) == null) {
                return;
            }
            ((TextView) rootView.findViewById(android.R.id.title)).setText(title);
        }

        @Override
        public void setSubTitle(CharSequence title) {

            if (rootView.findViewById(R.id.subtitle) == null) {
                return;
            }
            ((TextView) rootView.findViewById(R.id.subtitle)).setText(title);
        }

        @Override
        public TitleBar build() {
            TitleBar t = new TitleBar(this);
            mActivity.getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
                    R.layout.button_search_title_bar);
            rootView = mActivity.findViewById(R.id.title_bar);
            return t;
        }
    }

    /**
     * Deprecated
     * 兼容老的Activity，新写的Activity应该使用TitleBar样式为TITLE_TYPE_STANDARD
     */
    @Deprecated
    private static class DoubleLineStyle extends Style {

        public DoubleLineStyle(Activity activity) {
            super(activity);
            mActivity.getWindow().requestFeature(Window.FEATURE_CUSTOM_TITLE);
            mActivity.setContentView(new ViewStub(mActivity));
        }

        @Override
        public void setTitle(CharSequence title) {

            if (rootView.findViewById(android.R.id.title) == null) {
                return;
            }
            ((TextView) rootView.findViewById(android.R.id.title)).setText(title);
        }

        @Override
        public void setSubTitle(CharSequence title) {

            if (rootView.findViewById(R.id.subtitle) == null) {
                return;
            }
            ((TextView) rootView.findViewById(R.id.subtitle)).setText(title);
        }

        @Override
        public TitleBar build() {
            TitleBar t = new TitleBar(this);
            mActivity.getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
                    R.layout.double_line_title_bar);
            rootView = mActivity.findViewById(R.id.title_bar);
            return t;
        }
    }

    /**
     * Deprecated
     * 兼容老的Activity，新写的Activity应该使用TitleBar样式为TITLE_TYPE_STANDARD
     */
    @Deprecated
    private static class DoubleLineProgressStyle extends Style {

        public DoubleLineProgressStyle(Activity activity) {
            super(activity);
            mActivity.getWindow().requestFeature(Window.FEATURE_CUSTOM_TITLE);
            mActivity.setContentView(new ViewStub(mActivity));
        }

        @Override
        public void setTitle(CharSequence title) {

            if (rootView.findViewById(android.R.id.title) == null) {
                return;
            }
            ((TextView) rootView.findViewById(android.R.id.title)).setText(title);
        }

        @Override
        public void setSubTitle(CharSequence title) {

            if (rootView.findViewById(R.id.subtitle) == null) {
                return;
            }
            ((TextView) rootView.findViewById(R.id.subtitle)).setText(title);
        }

        @Override
        public TitleBar build() {
            TitleBar t = new TitleBar(this);
            mActivity.getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
                    R.layout.double_line_title_progress_bar);
            rootView = mActivity.findViewById(R.id.title_bar);

            return t;
        }
    }

    /**
     * Deprecated
     * 兼容老的Activity，新写的Activity应该使用TitleBar样式为TITLE_TYPE_STANDARD
     */
    @Deprecated
    private static class FilterStyle extends Style {

        public FilterStyle(Activity activity) {
            super(activity);
            mActivity.getWindow().requestFeature(Window.FEATURE_CUSTOM_TITLE);
            mActivity.setContentView(new ViewStub(mActivity));
        }

        @Override
        public void setTitle(CharSequence title) {

            if (rootView.findViewById(android.R.id.title) == null) {
                return;
            }
            ((TextView) rootView.findViewById(android.R.id.title)).setText(title);
        }

        @Override
        public void setSubTitle(CharSequence title) {

            if (rootView.findViewById(R.id.subtitle) == null) {
                return;
            }
            ((TextView) rootView.findViewById(R.id.subtitle)).setText(title);
        }

        @Override
        public TitleBar build() {
            TitleBar t = new TitleBar(this);
            mActivity.getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
                    R.layout.filter_title_bar);
            rootView = mActivity.findViewById(R.id.title_bar);
            return t;
        }
    }

    /**
     * Deprecated
     * 兼容老的Activity，新写的Activity应该使用TitleBar样式为TITLE_TYPE_STANDARD
     */
    @Deprecated
    private static class DoubleTextStyle extends Style {

        public DoubleTextStyle(Activity activity) {
            super(activity);
            mActivity.getWindow().requestFeature(Window.FEATURE_CUSTOM_TITLE);
            mActivity.setContentView(new ViewStub(mActivity));
        }

        @Override
        public void setTitle(CharSequence title) {

            if (rootView.findViewById(android.R.id.title) == null) {
                return;
            }
            ((TextView) rootView.findViewById(android.R.id.title)).setText(title);
        }

        @Override
        public void setSubTitle(CharSequence title) {

            if (rootView.findViewById(R.id.subtitle) == null) {
                return;
            }
            ((TextView) rootView.findViewById(R.id.subtitle)).setText(title);
        }

        @Override
        public TitleBar build() {
            TitleBar t = new TitleBar(this);
            mActivity.getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
                    R.layout.double_text_title_bar);
            rootView = mActivity.findViewById(R.id.title_bar);
            return t;
        }
    }
}
