package com.yamibo.main.yamibolib.app;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.yamibo.main.yamibolib.R;
import com.yamibo.main.yamibolib.accountservice.AccountService;
import com.yamibo.main.yamibolib.configservice.ConfigService;
import com.yamibo.main.yamibolib.dataservice.http.HttpService;
import com.yamibo.main.yamibolib.dataservice.http.impl.SealedMApiService;
import com.yamibo.main.yamibolib.locationservice.LocationService;
import com.yamibo.main.yamibolib.statistics.StatisticsService;
import com.yamibo.main.yamibolib.widget.TitleBar;

import org.json.JSONObject;

/**
 * Created by wangxiaoyan on 15/8/7.
 */
public class YMBActivity extends FragmentActivity {

    // utils
    protected static final String TAG = YMBActivity.class.getSimpleName();

    // life cycle
    public boolean isResumed = false;
    public boolean isDestroyed = false;

    // Service
    private SealedMApiService sealedMApiService;
    private ConfigService configService;
    private AccountService accountService;
    private LocationService locationService;
    private StatisticsService statisticsService;

    // UI
    protected Dialog managedDialog;
    protected int managedDialogId = 0;
    private Toast toast;
    protected static final int DLG_PROGRESS = 0xFA05;
    protected static final int DLG_MESSAGE = 0xFA06;
    protected static final int DLG_ALERT = 0xFA07;

    private TitleBar mTitleBar;

    //
    // life cycle
    //


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getParent() == null) { // 代表不是在嵌套的TabActivity中
            mTitleBar = initCustomTitle();
            mTitleBar.setLeftView(new ImageButton.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    onLeftTitleButtonClicked();
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        isResumed = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        isResumed = false;
    }

    @Override
    protected void onDestroy() {
        isDestroyed = true;

        if (sealedMApiService != null) {
            sealedMApiService.onDestroy();
        }

        super.onDestroy();
    }


    //
    // Utils
    //

    public SharedPreferences preferences() {
        return getSharedPreferences(getPackageName(), MODE_PRIVATE);
    }

    public void startActivity(String scheme) {
        super.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(scheme)));
    }

    public void startActivityForResult(String scheme, int requestCode) {
        super.startActivityForResult(new Intent(Intent.ACTION_VIEW, Uri.parse(scheme)), requestCode);
    }

    //
    // UI Utils
    //

    public TitleBar getTitleBar() {
        return mTitleBar;
    }

    @Override
    public void setTitle(CharSequence title) {
        super.setTitle(title);
        if (mTitleBar != null) {
            mTitleBar.setTitle(title);
        }
    }

    public void setSubtitle(CharSequence title) {
        if (mTitleBar != null) {
            mTitleBar.setSubTitle(title);
        }
    }


    protected TitleBar initCustomTitle() {
        return TitleBar.build(this, TitleBar.TITLE_TYPE_STANDARD);
    }

    protected void onLeftTitleButtonClicked() {
        onBackPressed();
    }

    protected int getMessageIconId(int id) {
        switch (id) {
            case 1:
                return android.R.drawable.ic_dialog_info;
            default:
                return android.R.drawable.ic_dialog_alert;
        }
    }

    public void dismissDialog() {
        if (managedDialogId != 0) {
            if ((managedDialog != null) && managedDialog.isShowing()) {
                managedDialog.dismiss();
            }
            managedDialogId = 0;
            managedDialog = null;
        }
    }

    public void showProgressDialog(String title) {
        showProgressDialog(title, null);
    }

    /**
     * 显示Progress Dialog.
     *
     * @param title
     * @param cancelListener
     */
    public void showProgressDialog(String title,
                                   final DialogInterface.OnCancelListener cancelListener) {
        if (isDestroyed) {
            return;
        }
        dismissDialog();

        ProgressDialog dlg = new ProgressDialog(this);
        dlg.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if (cancelListener != null) {
                    cancelListener.onCancel(dialog);
                }
                if (managedDialogId == DLG_PROGRESS) {
                    managedDialogId = 0;
                }
                managedDialog = null;
            }
        });
        dlg.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_SEARCH) {
                    return true;
                }
                return false;
            }
        });
        dlg.setMessage(TextUtils.isEmpty(title) ? getString(R.string.loading) : title);

        managedDialogId = DLG_PROGRESS;
        managedDialog = dlg;
        dlg.show();
    }

    /**
     * 有两个按钮的对话框
     */
    public void showMessageDialog(String title, String message, DialogInterface.OnClickListener positiveButtonListener, DialogInterface.OnClickListener negativeButtonListener) {
        if (isDestroyed) {
            return;
        }
        dismissDialog();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(getMessageIconId(1))
                .setMessage(message)
                .setTitle(title)
                .setPositiveButton(android.R.string.ok, positiveButtonListener)
                .setNegativeButton(android.R.string.cancel, negativeButtonListener);
        AlertDialog dlg = builder.create();
        dlg.setCancelable(false);
        managedDialogId = DLG_MESSAGE;
        managedDialog = dlg;
        dlg.show();
    }

    /**
     * 只有一个确定按钮的AlertDialog
     *
     * @param title
     * @param message
     */
    public void showAlertDialog(String title, String message) {
        showAlertDialog(title, message, getString(android.R.string.ok));
    }

    public void showAlertDialog(String title, String message, String buttonTitle) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title).setMessage(message).setPositiveButton(buttonTitle,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        AlertDialog dlg = builder.create();
        managedDialogId = DLG_ALERT;
        managedDialog = dlg;
        dlg.show();
    }

    public void showToast(String msg) {
        showToast(msg, Toast.LENGTH_LONG);
    }

    public void showToast(String msg, int duration) {
        if (toast == null) {
            toast = Toast.makeText(this, msg, duration);
        } else {
            toast.setText(msg);
            toast.setDuration(duration);
        }
        toast.show();
    }

    public void showShortToast(String msg) {
        showToast(msg, Toast.LENGTH_SHORT);
    }

    //
    // get Service
    //

    public Object getService(String name) {
        if ("http".equals(name)) {
            if (sealedMApiService == null) {
                HttpService orig = (HttpService) YMBApplication.instance().getService("http");
                sealedMApiService = new SealedMApiService(orig);
            }
            return sealedMApiService;
        }
        return YMBApplication.instance().getService(name);
    }


    public ConfigService configService() {
        if (configService == null) {
            configService = (ConfigService) getService("config");
        }
        return configService;
    }

    public AccountService accountService() {
        if (accountService == null) {
            accountService = (AccountService) getService("account");
        }
        return accountService;
    }

    public LocationService locationService() {
        if (locationService == null) {
            locationService = (LocationService) getService("location");
        }
        return locationService;
    }

    public StatisticsService statisticsService() {
        if (statisticsService == null) {
            statisticsService = (StatisticsService) getService("statistics");
        }
        return statisticsService;
    }

    //
    // get params util method
    //

    public int getIntParam(String name, int defaultValue) {
        Intent i = getIntent();
        try {
            Uri uri = i.getData();
            if (uri != null) {
                String val = uri.getQueryParameter(name);
                return Integer.parseInt(val);
            }
        } catch (Exception e) {
        }

        return i.getIntExtra(name, defaultValue);
    }

    public int getIntParam(String name) {
        return getIntParam(name, 0);
    }

    public String getStringParam(String name) {
        Intent i = getIntent();
        try {
            Uri uri = i.getData();
            if (uri != null) {
                String val = uri.getQueryParameter(name);
                if (val != null)
                    return val;
            }
        } catch (Exception e) {
        }

        return i.getStringExtra(name);
    }

    public double getDoubleParam(String name, double defaultValue) {
        Intent i = getIntent();
        try {
            Uri uri = i.getData();
            if (uri != null) {
                String val = uri.getQueryParameter(name);
                return Double.parseDouble(val);
            }
        } catch (Exception e) {
        }

        return i.getDoubleExtra(name, defaultValue);
    }

    public double getDoubleParam(String name) {
        return getDoubleParam(name, 0);
    }

    public JSONObject getJsonObjectParam(String name) {
        Intent i = getIntent();
        try {
            Uri uri = i.getData();
            if (uri != null) {
                String val = uri.getQueryParameter(name);
                if (val != null) {
                    return new JSONObject(val);
                }
            }
            return new JSONObject(i.getStringExtra(name));
        } catch (Exception e) {
        }
        return new JSONObject();
    }

    public boolean getBooleanParam(String name, boolean defaultValue) {
        Intent i = getIntent();
        try {
            Uri uri = i.getData();
            if (uri != null) {
                String val = uri.getQueryParameter(name);
                if (!TextUtils.isEmpty(val))
                    return Boolean.parseBoolean(val);
            }
        } catch (Exception e) {
        }
        return i.getBooleanExtra(name, defaultValue);
    }

    public boolean getBooleanParam(String name) {
        return getBooleanParam(name, false);
    }

    public long getLongParam(String name) {
        return getLongParam(name, 0L);
    }

    public long getLongParam(String name, long defaultValue) {
        Intent i = getIntent();
        try {
            Uri uri = i.getData();
            if (uri != null) {
                String val = uri.getQueryParameter(name);
                return Long.parseLong(val);
            }
        } catch (Exception e) {
        }

        return i.getLongExtra(name, defaultValue);
    }

    public byte getByteParam(String name) {
        return getByteParam(name, (byte) 0);
    }

    public byte getByteParam(String name, byte defaultValue) {
        Intent i = getIntent();
        try {
            Uri uri = i.getData();
            if (uri != null) {
                String val = uri.getQueryParameter(name);
                return Byte.parseByte(val);
            }
        } catch (Exception e) {
        }

        return i.getByteExtra(name, defaultValue);
    }

    public float getFloatParam(String name) {
        return getFloatParam(name, 0f);
    }

    public float getFloatParam(String name, float defaultValue) {
        Intent i = getIntent();
        try {
            Uri uri = i.getData();
            if (uri != null) {
                String val = uri.getQueryParameter(name);
                return Float.parseFloat(val);
            }
        } catch (Exception e) {
        }

        return i.getFloatExtra(name, defaultValue);
    }

    public short getShortParam(String name) {
        return getShortParam(name, (short) 0);
    }

    public short getShortParam(String name, short defaultValue) {
        Intent i = getIntent();
        try {
            Uri uri = i.getData();
            if (uri != null) {
                String val = uri.getQueryParameter(name);
                return Short.parseShort(val);
            }
        } catch (Exception e) {
        }

        return i.getShortExtra(name, defaultValue);
    }

    public char getCharParam(String name) {
        return getCharParam(name, (char) 0);
    }

    public char getCharParam(String name, char defaultValue) {
        Intent i = getIntent();
        try {
            Uri uri = i.getData();
            if (uri != null) {
                String val = uri.getQueryParameter(name);
                return val.charAt(0);
            }
        } catch (Exception e) {
        }

        return i.getCharExtra(name, defaultValue);
    }
}
