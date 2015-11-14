package com.yamibo.main.yamiboandroid.login;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.yamibo.main.yamiboandroid.R;
import com.yamibo.main.yamibolib.Utils.BasicNameValuePair;
import com.yamibo.main.yamibolib.Utils.Environment;
import com.yamibo.main.yamibolib.Utils.NameValuePair;
import com.yamibo.main.yamibolib.accountservice.impl.DefaultAccountService;
import com.yamibo.main.yamibolib.app.YMBActivity;
import com.yamibo.main.yamibolib.dataservice.RequestHandler;
import com.yamibo.main.yamibolib.dataservice.http.HttpRequest;
import com.yamibo.main.yamibolib.dataservice.http.HttpResponse;
import com.yamibo.main.yamibolib.dataservice.http.impl.BasicHttpRequest;
import com.yamibo.main.yamibolib.model.UserProfile;
import com.yamibo.main.yamibolib.widget.BasicItem;
import com.yamibo.main.yamibolib.widget.CustomEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangxiaoyan on 15/11/9.
 */
public class LoginActivity extends YMBActivity implements View.OnClickListener, RequestHandler<HttpRequest, HttpResponse> {

    private CustomEditText mUserName;
    private CustomEditText mUserPsw;
    private BasicItem mUserQuestion;
    private CustomEditText mUserAnswer;
    private Button mLoginButton;

    private String[] mQuestArray;
    private int mQuestNum = 0;
    private Dialog mQuestDialog;

    private HttpRequest mLoginRequest;

    private boolean mLoginResult = false;

    private final static String PERFER_USER_NAME = "com.yamibo.USER_NAME";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
    }

    private void initView() {
        setContentView(R.layout.activity_login);
        setTitle(getString(R.string.login));

        mUserName = (CustomEditText) findViewById(R.id.user_name);
        String userName = preferences().getString(PERFER_USER_NAME, "");
        if (!TextUtils.isEmpty(userName)) {
            mUserName.mEdit.setText(userName);
        } else {
            mUserName.mEdit.setHint(getString(R.string.user_name));
        }

        mUserPsw = (CustomEditText) findViewById(R.id.user_psd);
        mUserPsw.mEdit.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        mUserPsw.mEdit.setHint(getString(R.string.user_psw));

        mUserQuestion = (BasicItem) findViewById(R.id.user_question);
        mUserQuestion.setTitle(getString(R.string.user_question_not_set));
        mUserQuestion.setOnClickListener(this);

        mUserAnswer = (CustomEditText) findViewById(R.id.user_answer);
        mUserAnswer.mEdit.setHint(getString(R.string.user_answer));
        mUserAnswer.mEdit.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        mLoginButton = (Button) findViewById(R.id.btn_login);
        mLoginButton.setOnClickListener(this);

        mQuestArray = new String[]{
                getString(R.string.user_question_not_set),
                getString(R.string.user_question_mather_name),
                getString(R.string.user_question_grandfather_name),
                getString(R.string.user_question_father_city),
                getString(R.string.user_question_teach_name),
                getString(R.string.user_question_pc_type),
                getString(R.string.user_question_fav_res),
                getString(R.string.user_question_drive_last_four),
        };
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.user_question) {
            if (mQuestDialog != null && mQuestDialog.isShowing()) return;
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setItems(mQuestArray, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    mQuestNum = which;
                    mUserQuestion.setTitle(mQuestArray[which]);
                    mQuestDialog = null;
                }
            });
            mQuestDialog = builder.create();
            mQuestDialog.show();
        } else if (v.getId() == R.id.btn_login) {
            Editable userName = mUserName.mEdit.getText();
            Editable userPsd = mUserPsw.mEdit.getText();
            Editable answer = mUserAnswer.mEdit.getText();
            if (userName != null && !TextUtils.isEmpty(userName.toString())
                    && userPsd != null && !TextUtils.isEmpty(userPsd.toString())) {
                login(userName.toString().trim(), userPsd.toString().trim(), mQuestNum, answer != null ? answer.toString().trim() : null);
            } else {
                showToast(getString(R.string.user_name_or_psd_empty));
            }
        }

    }

    private void login(String userName, String userPsd, int questNum, String answer) {
        if (mLoginRequest != null) {
            httpService().abort(mLoginRequest, this, true);
        }
        showProgressDialog(getString(R.string.loading));
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("module", "login"));
        params.add(new BasicNameValuePair("username", userName));
        params.add(new BasicNameValuePair("password", userPsd));
        if (questNum != 0) {
            params.add(new BasicNameValuePair("questionid", String.valueOf(questNum)));
            if (!TextUtils.isEmpty(answer)) {
                params.add(new BasicNameValuePair("answer", answer));
            }
        }

        mLoginRequest = BasicHttpRequest.httpPost(Environment.HTTP_ADDRESS, params);
        httpService().exec(mLoginRequest, this);
    }

    @Override
    public void onRequestFinish(HttpRequest req, HttpResponse resp) {
        if (mLoginRequest == req) {
            dismissDialog();
            if (resp.result() instanceof JSONObject) {
                JSONObject userProfile = (JSONObject) resp.result();
                try {
                    String messagerStr = userProfile.getJSONObject("Message").optString("messagestr");
                    String messagerVal = userProfile.getJSONObject("Message").optString("messageval");

                    String auth = userProfile.getJSONObject("Variables").optString("auth");

                    showToast(messagerStr);


                    if (auth == null || "null".equals(auth)) {//auth无效时
                        accountService().logout();
                        accountService().update(null);
                    } else {//auth有效时
                        if ("login_succeed".equals(messagerVal)) {
                            preferences().edit().putString(PERFER_USER_NAME, mUserName.mEdit.getText().toString().trim()).apply();
                            accountService().update(new UserProfile(userProfile.getJSONObject("Variables")));

                            finish();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            mLoginRequest = null;
        }
    }

    @Override
    public void onRequestFailed(HttpRequest req, HttpResponse resp) {
        if (mLoginRequest == req) {
            dismissDialog();
            mLoginRequest = null;
            showToast(getString(R.string.network_fail));
        }
    }

    @Override
    protected void onDestroy() {
        if (!mLoginResult) {
            ((DefaultAccountService) accountService()).onLoginCancel();
        }
        super.onDestroy();
    }

}
