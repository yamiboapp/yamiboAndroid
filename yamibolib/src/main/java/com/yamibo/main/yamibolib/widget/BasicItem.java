package com.yamibo.main.yamibolib.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Parcelable;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.yamibo.main.yamibolib.R;

public class BasicItem extends LinearLayout {
    Spanned titleSpan;
    private Context mContext;
    private LinearLayout itemTitleLay;
    private TextView itemTitle;
    private TextView itemSubtitle;
    private EditText itemInput;
    private TextView itemCount;
    private Switch itemSwitch;
    private ImageView itemLeft1stPic;
    private ImageView itemArrow;
    private String title;
    private String subTitle;
    private String input;
    private String input_hint;
    private int input_type;
    private String count;
    private boolean clickable;
    private int subTitle_textType;
    private int count_textType;
    private int input_textType;
    private int iconResId;
    private int checked;

    public BasicItem(Context context) {
        this(context, null);
        mContext = context;

        this.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
    }

    public BasicItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;

        setupView(context);
    }

    private void setupView(Context context) {
        Resources resource = context.getResources();
        ColorStateList csl = resource.getColorStateList(R.color.text_color_default);

        LinearLayout.LayoutParams paramLeft = new LinearLayout.LayoutParams(dip2px(30), dip2px(30));
        paramLeft.setMargins(0, 0, dip2px(10), 0);

        // title lay
        itemTitleLay = new LinearLayout(context);
        itemTitleLay.setDuplicateParentStateEnabled(true);
        itemTitleLay.setGravity(Gravity.CENTER_VERTICAL);

        //itemLeftFirstImage
        itemLeft1stPic = new ImageView(context);
        itemLeft1stPic.setId(R.id.itemLeft1stPic);
        itemLeft1stPic.setLayoutParams(paramLeft);
        itemLeft1stPic.setDuplicateParentStateEnabled(true);
        itemTitleLay.addView(itemLeft1stPic);

        // title
        itemTitle = new TextView(context);
        itemTitle.setId(R.id.itemTitle);
        itemTitle.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        itemTitle.setText(title);
        itemTitle.setDuplicateParentStateEnabled(true);
        itemTitle.setTextAppearance(context, android.R.style.TextAppearance_Small);
        itemTitle.setTextColor(csl);
        itemTitle.setSingleLine(true);
        itemTitle.setEllipsize(TruncateAt.END);
        itemTitle.setPadding(0, 0, dip2px(10), 0);
        itemTitle.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        itemTitleLay.addView(itemTitle);

        // subTitle
        itemSubtitle = new TextView(context);
        itemSubtitle.setId(R.id.itemSubTitle);
        itemSubtitle.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        itemSubtitle.setText(subTitle);
        itemSubtitle.setDuplicateParentStateEnabled(true);
        itemSubtitle.setTextAppearance(context, android.R.style.TextAppearance_Small);
        itemSubtitle.setTextColor(csl);
        itemSubtitle.setSingleLine(true);
        itemSubtitle.setEllipsize(TruncateAt.END);
        itemTitleLay.addView(itemSubtitle);
        addView(itemTitleLay);

        // itemInput
        itemInput = new EditText(context);
        itemInput.setId(R.id.itemInput);
        LinearLayout.LayoutParams inputLayoutParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        itemInput.setLayoutParams(inputLayoutParams);
        itemInput.setGravity(Gravity.CENTER_VERTICAL);
        itemInput.setText(input);
        itemInput.setDuplicateParentStateEnabled(true);
        itemInput.setTextAppearance(context, android.R.style.TextAppearance_Small);
        itemInput.setTextColor(csl);
        itemInput.setSingleLine(true);
        itemInput.setEllipsize(TruncateAt.END);
        itemInput.setHint(input_hint);
        itemInput.setBackgroundDrawable(null);
        itemInput.setPadding(0, 0, 0, 0);
        itemInput.setInputType(InputType.TYPE_CLASS_TEXT);
        addView(itemInput);

        // itemCount
        itemCount = new TextView(context);
        itemCount.setId(R.id.itemCount);
        itemCount.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        itemCount.setText(count);
        itemCount.setMaxWidth(dip2px(180));
        itemCount.setDuplicateParentStateEnabled(true);
        itemCount.setTextAppearance(context, android.R.style.TextAppearance_Small);
        itemCount.setTextColor(resource.getColorStateList(R.color.text_color_default));
        itemCount.setPadding(0, 0, 0, 0);
        addView(itemCount);

        // itemSwitch
        itemSwitch = new Switch(context);
        itemSwitch.setId(R.id.itemSwitch);
        itemSwitch.setLayoutParams(new LinearLayout.LayoutParams(dip2px(26), dip2px(25)));
        itemSwitch.setChecked(checked == 1 ? true : false);
        itemSwitch.setPadding(0, 0, 0, 0);
        addView(itemSwitch);

        // itemArrow
        itemArrow = new ImageView(context);
        itemArrow.setId(R.id.itemArrow);
        itemArrow.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        itemArrow.setPadding(dip2px(10), 0, 0, 0);
        itemArrow.setDuplicateParentStateEnabled(true);
        itemArrow.setImageResource(R.drawable.arrow);
        addView(itemArrow);

        build();

        setGravity(Gravity.CENTER_VERTICAL);
        setMinimumHeight(dip2px(45));
    }

    /**
     * set the view's visibility
     */
    public void build() {
        itemTitle.setVisibility(title == null ? View.GONE : View.VISIBLE);
        itemSubtitle.setVisibility(subTitle == null ? View.GONE : View.VISIBLE);
        itemInput.setVisibility(input_hint != null || input != null ? View.VISIBLE : View.GONE);
        itemCount.setVisibility(count != null ? View.VISIBLE : View.GONE);
        itemSwitch.setVisibility(checked == 0 ? View.GONE : View.VISIBLE);
        itemLeft1stPic.setVisibility(iconResId == 0 ? View.GONE : View.VISIBLE);
        itemArrow.setVisibility(isClickable() ? View.VISIBLE : View.GONE);

        if (input_hint != null || input != null) {
            itemTitleLay.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 0));
        } else {
            itemTitleLay.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        }

        setClickable(clickable);
    }

    private int dip2px(float dipValue) {
        float scale = mContext.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    @Override
    protected void dispatchSaveInstanceState(SparseArray<Parcelable> container) {
        int inputId = R.id.itemInput - getId();
        EditText inputChild = (EditText) findViewById(R.id.itemInput);
        if (inputChild == null) {
            super.dispatchSaveInstanceState(container);
            return;
        } else {
            Parcelable state = inputChild.onSaveInstanceState();
            if (state != null) {
                container.put(inputId, state);
            }
        }

        int checkboxId = R.id.itemSwitch ^ getId();
        Switch checkboxChild = (Switch) findViewById(R.id.itemSwitch);
        Parcelable state = checkboxChild.onSaveInstanceState();
        if (state != null) {
            container.put(checkboxId, state);
        }
    }

    @Override
    protected void dispatchRestoreInstanceState(SparseArray<Parcelable> container) {
        int inputId = R.id.itemInput - getId();
        EditText inputChild = (EditText) findViewById(R.id.itemInput);
        if (inputChild == null) {
            super.dispatchRestoreInstanceState(container);
            return;
        } else {
            Parcelable state = container.get(inputId);
            if (state != null) {
                inputChild.onRestoreInstanceState(state);
            }
        }

        int checkboxId = R.id.itemSwitch ^ getId();
        Switch checkboxChild = (Switch) findViewById(R.id.itemSwitch);
        Parcelable state = container.get(checkboxId);
        if (state != null) {
            checkboxChild.onRestoreInstanceState(state);
        }
    }

    @Override
    public boolean isClickable() {
        return clickable;
    }

    @Override
    public void setClickable(boolean clickable) {
        super.setClickable(clickable);
        this.clickable = clickable;
        itemArrow.setVisibility(isClickable() ? View.VISIBLE : View.GONE);
    }

    public String getInputHint() {
        return input_hint;
    }

    public void setHint(String text) {
        this.input_hint = text;
        itemInput.setHint(text);
        build();
    }

    public String getInputText() {
        return input;
    }

    public void setInputText(String inputText) {
        this.input = inputText;
        itemInput.setText(inputText);
        build();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(Spanned spanText) {
        this.titleSpan = spanText;
        itemTitle.setText(spanText);
        build();
    }

    public void setTitle(String mTitle) {
        this.title = mTitle;
        itemTitle.setText(mTitle);
        build();
    }

    public String getSubTitle() {
        return subTitle;
    }

    public void setSubTitle(String mSubtitle) {
        this.subTitle = mSubtitle;
        itemSubtitle.setText(mSubtitle);
        build();
    }

    public String getCount() {
        return count;
    }

    public void setCount(String countText) {
        this.count = countText;
        itemCount.setText(countText);
        build();
    }

    public void setInputType(int inputType) {
        this.input_type = inputType;
        itemInput.setInputType(inputType);
        build();
    }

    public void setIconRes(int ResId) {
        this.iconResId = ResId;
        itemLeft1stPic.setImageResource(ResId);
        build();
    }

    public int getSubTitleTextType() {
        return subTitle_textType;
    }

    public void setSubTitleTextType(int textType) {
        this.subTitle_textType = textType;
    }

    public int getCountTextType() {
        return count_textType;
    }

    public void setCountTextType(int textType) {
        this.count_textType = textType;
    }

    public int getInputTextType() {
        return input_textType;
    }

    public void setInputTextType(int textType) {
        this.input_textType = textType;
    }

    public TextView itemTitle() {
        return itemTitle;
    }

    public LinearLayout getItemTitleLay() {
        return itemTitleLay;
    }

    public TextView getItemTitle() {
        return itemTitle;
    }

    public TextView getItemSubtitle() {
        return itemSubtitle;
    }

    public EditText getItemInput() {
        return itemInput;
    }

    public TextView getItemCount() {
        return itemCount;
    }

    public Switch getItemSwitch() {
        return itemSwitch;
    }

    public ImageView getItemArrow() {
        return itemArrow;
    }

    public void setArrowImage(int resId) {
        itemArrow.setImageResource(resId);
    }

}
