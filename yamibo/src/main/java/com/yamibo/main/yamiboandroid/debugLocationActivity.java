package com.yamibo.main.yamiboandroid;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.yamibo.main.yamibolib.locationservice.LocationListener;
import com.yamibo.main.yamibolib.locationservice.LocationService;
import com.yamibo.main.yamibolib.locationservice.impl.DefaultLocationService;


public class debugLocationActivity extends ActionBarActivity {
    public static String debugMessage="Hello World";
    private static final boolean IS_DEBUG_ENABLED=true;
    DefaultLocationService locationService;
    TextView debugTextMessage;
    Button btnStart, btnStop, btnRefresh, btnAddNewListener, btnRemoveLastListener, btnInputCoords;
    CheckBox isAutoSwitchAPI, isUseBd;
    EditText editInterval, editLatitude, editLongitude;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug_location);
        //receive no data from the intent

        locationService=new DefaultLocationService(getApplicationContext());
        //locationService.start();

        btnStart=(Button)findViewById(R.id.start);
        btnStop=(Button)findViewById(R.id.stop);
        btnRefresh=(Button)findViewById(R.id.refresh);
        btnAddNewListener=(Button)findViewById(R.id.addNewListener);
        btnRemoveLastListener=(Button)findViewById(R.id.removeLastListener);
        isAutoSwitchAPI=(CheckBox)findViewById(R.id.isAutoSwitchApi);
        isUseBd=(CheckBox)findViewById(R.id.isUseBaidu);
        editInterval =(EditText)findViewById(R.id.editInterval);

        debugTextMessage=(TextView)findViewById(R.id.debugShowMessage);
        locationService.debugTextView=debugTextMessage;

        btnInputCoords=(Button)findViewById(R.id.inputCoords);
        editLatitude =(EditText)findViewById(R.id.inputLatitude);
        editLongitude=(EditText)findViewById(R.id.inputLongitude);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_debug_location, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void clickStart(View view){
        if (locationService != null) {
            debugLog("activity demand start");


            locationService.start();
        }
        else
            debugLog("null service");


    }

    public void clickStop(View view){
        if (locationService != null) {
            debugLog("activity demand stop");
            locationService.stop();
        }

    }

    public void clickRefresh(View view){
        if (locationService != null) {
            debugLog("activity demand refresh");
            locationService.refresh();
        }
    }
    public void clickAddNewListener(View view){
        if (locationService != null) {
            debugLog("activity demand add a new listener");
            locationService.addListener(new LocationListener() {
                @Override
                public void onLocationChanged(LocationService sender) {
                    debugLog("custom action from Location Listener");
                }
            });
        }
    }
    public void clickRemoveLastListener(View view) {
        if (locationService != null) {
            debugLog("activity demand remove the last available listener");
                locationService.removeLastListener();
        }
    }
    public void clickReconstructAPIService(View view){
        int updateInterval=Integer.parseInt(editInterval.getText().toString());
        int serviceMode;
        if(isUseBd.isChecked())
            serviceMode=DefaultLocationService.BAIDU_MODE;
        else
            serviceMode=DefaultLocationService.ANDROID_API_MODE;
        boolean isAutoSwitchService=isAutoSwitchAPI.isChecked();
        int providerChoice=DefaultLocationService.PROVIDER_NETWORK;//fixed

        if(locationService!=null){
                debugLog("activity demand reconstruct API service with new parameters");
                locationService.reconstructAPIService(updateInterval, serviceMode, isAutoSwitchService, providerChoice);
        }
    }

    public void clickInputRealCoords(View view){
        if(editLatitude !=null&&editLongitude!=null) {
            if(editLatitude.getText()==null|editLongitude.getText()==null){
                debugLog("error input doubles");
                return;
            }
            double latitude = Double.parseDouble(editLatitude.getText().toString());
            double longtitude = Double.parseDouble(editLongitude.getText().toString());
            locationService.onReceiveLocation(locationService.realCoordsToLocationViaAndroid(latitude, longtitude));
        }
        else{
            debugLog("null editText pointers!");
        }
    }

    private void debugLog(String debugMessage){
        if(IS_DEBUG_ENABLED){
            Log.i("DEBUG_debugActivity:",debugMessage);
        }
    }


}
