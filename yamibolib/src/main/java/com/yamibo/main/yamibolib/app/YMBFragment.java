package com.yamibo.main.yamibolib.app;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.widget.Toast;

import com.yamibo.main.yamibolib.R;
import com.yamibo.main.yamibolib.dataservice.http.HttpService;
import com.yamibo.main.yamibolib.dataservice.http.impl.SealedMApiService;

/**
 * Created by WINFIELD on 2015/11/21.
 */
public class YMBFragment extends Fragment {
    // utils
    protected static final String TAG = YMBActivity.class.getSimpleName();

    // life cycle
    public boolean isResumed = false;
    public boolean isDestroyed = false;

    // Service
    private SealedMApiService sealedMApiService;
    private HttpService httpService;

    // UI
    protected Dialog managedDialog;
    protected int managedDialogId = 0;
    private Toast toast;
    protected static final int DLG_PROGRESS = 0xFA05;
    protected static final int DLG_MESSAGE = 0xFA06;
    protected static final int DLG_ALERT = 0xFA07;


    //
    // life cycle
    //

    public HttpService httpService() {
        if (httpService == null) {
            httpService = (HttpService) getHttpService("http");
        }
        return httpService;
    }

    public Object getHttpService(String name) {
        if ("http".equals(name)) {
            if (sealedMApiService == null) {
                HttpService orig = (HttpService) YMBApplication.instance().getService("http");
                sealedMApiService = new SealedMApiService(orig);
            }
            return sealedMApiService;
        }
        return YMBApplication.instance().getService(name);
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

        ProgressDialog dlg = new ProgressDialog(getActivity());
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

    public void dismissDialog() {
        if (managedDialogId != 0) {
            if ((managedDialog != null) && managedDialog.isShowing()) {
                managedDialog.dismiss();
            }
            managedDialogId = 0;
            managedDialog = null;
        }
    }

    public void showToast(String msg) {
        showToast(msg, Toast.LENGTH_LONG);
    }

    public void showToast(String msg, int duration) {
        if (toast == null) {
            toast = Toast.makeText(getActivity(), msg, duration);
        } else {
            toast.setText(msg);
            toast.setDuration(duration);
        }
        toast.show();
    }
}
