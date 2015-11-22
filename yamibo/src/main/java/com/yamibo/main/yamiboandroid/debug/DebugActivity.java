package com.yamibo.main.yamiboandroid.debug;

import android.os.Bundle;
import android.view.View;

import com.yamibo.main.yamiboandroid.R;
import com.yamibo.main.yamibolib.app.YMBActivity;

/**
 * Created by wangxiaoyan on 15/11/22.
 */
public class DebugActivity extends YMBActivity implements View.OnClickListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);
        findViewById(R.id.btn_test).setOnClickListener(this);
        findViewById(R.id.btn_show_crash).setOnClickListener(this);

        //            List<NameValuePair> params = new ArrayList<>();
//            params.add(new BasicNameValuePair("module", "mypm"));
//            httpService().exec(BasicHttpRequest.httpPost(Environment.HTTP_ADDRESS, params), new RequestHandler<HttpRequest, HttpResponse>() {
//                @Override
//                public void onRequestFinish(HttpRequest req, HttpResponse resp) {
//                    Log.e(resp.toString());
//                }
//
//                @Override
//                public void onRequestFailed(HttpRequest req, HttpResponse resp) {
//                    Log.e(resp.toString());
//                }
//            });
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_test) {
//            throw new RuntimeException("test");
//            new Thread() {
//                @Override
//                public void run() {
//                    List<NameValuePair> params = new ArrayList<>();
//                    params.add(new BasicNameValuePair("module", "mypm"));
//                    HttpResponse response = httpService().execSync(BasicHttpRequest.httpPost(Environment.HTTP_ADDRESS, params));
//                    Log.e(response.toString());
//                }
//            }.start();

        } else if (v.getId() == R.id.btn_show_crash) {
            startActivity("ymb://crash");
        }
    }
}
