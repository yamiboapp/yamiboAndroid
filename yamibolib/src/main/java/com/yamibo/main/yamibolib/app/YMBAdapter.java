package com.yamibo.main.yamibolib.app;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Toast;

import com.yamibo.main.yamibolib.R;
import com.yamibo.main.yamibolib.accountservice.AccountService;
import com.yamibo.main.yamibolib.accountservice.LoginResultListener;
import com.yamibo.main.yamibolib.configservice.ConfigService;
import com.yamibo.main.yamibolib.dataservice.http.HttpService;
import com.yamibo.main.yamibolib.dataservice.http.impl.SealedMApiService;
import com.yamibo.main.yamibolib.locationservice.LocationService;
import com.yamibo.main.yamibolib.statistics.StatisticsService;

/**
 * Created by WINFIELD on 2015/11/29.
 */
public class YMBAdapter extends BaseAdapter {

    // utils
    protected static final String TAG = YMBAdapter.class.getSimpleName();

    /*
    // life cycle
    public boolean isResumed = false;
    public boolean isDestroyed = false;
    */

    // Service
    private SealedMApiService sealedMApiService;
    private HttpService httpService;
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

    /*
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
    */

    private Context mContext;


    public YMBAdapter(Context mContext){
        this.mContext = mContext;
    }

    //
    // Utils
    //

    public SharedPreferences preferences() {
        return mContext.getSharedPreferences(mContext.getPackageName(), mContext.MODE_PRIVATE);
    }

//    public void startActivity(String scheme) {
//        super.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(scheme)));
//    }
//
//    public void startActivityForResult(String scheme, int requestCode) {
//        super.startActivityForResult(new Intent(Intent.ACTION_VIEW, Uri.parse(scheme)), requestCode);
//    }

    public void login(LoginResultListener listener) {
        accountService().login(listener);
    }

    public void logout() {
        accountService().logout();
    }

    public boolean isLogin() {
        return accountService().isLogin();
    }

    //
    // UI Utils
    //


    /*
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
    */

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
        /*
        if (isDestroyed) {
            return;
        }
        */
        dismissDialog();

        ProgressDialog dlg = new ProgressDialog(mContext);
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
        dlg.setMessage(TextUtils.isEmpty(title) ? mContext.getString(R.string.loading) : title);

        managedDialogId = DLG_PROGRESS;
        managedDialog = dlg;
        dlg.show();
    }

    /**
     * 有两个按钮的对话框
     */
    public void showMessageDialog(String title, String message, DialogInterface.OnClickListener positiveButtonListener, DialogInterface.OnClickListener negativeButtonListener) {
        /*
        if (isDestroyed) {
            return;
        }
        */
        dismissDialog();
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
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
        showAlertDialog(title, message, mContext.getString(android.R.string.ok));
    }

    public void showAlertDialog(String title, String message, String buttonTitle) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
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
            toast = Toast.makeText(mContext, msg, duration);
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

    public HttpService httpService() {
        if (httpService == null) {
            httpService = (HttpService) getService("http");
        }
        return httpService;
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

    //省去get params util method

    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return null;
    }
}
