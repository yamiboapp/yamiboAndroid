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
    DefaultLocationService locationService;
    public static String debugMessage="Hello World";
    private static final boolean IS_DEBUG_ENABLED=true;
    protected TextView textMessage;
    protected Button btnStart, btnStop, btnRefresh, btnAddNewListener, btnRemoveLastListener;
    protected CheckBox isAutoSwitchAPI, isUseBd;
    EditText editInterval;
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
                    debugLog("add a new Location Listener to activeListener array of size " + locationService.activeListeners.size());
                }
            });
        }

    }
    public void clickRemoveLastListener(View view) {
        if(locationService.activeListeners!=null){
            if(!locationService.activeListeners.isEmpty()) {
                debugLog("activity demand remove the last available listener");
                LocationListener lastListener = locationService.activeListeners.get(locationService.activeListeners.size() - 1);
                locationService.removeListener(lastListener);
            }
        }
    }
    public void clickReconstructAPIService(View view){
        locationService.updateInterval=Integer.parseInt(editInterval.getText().toString());
        if(isUseBd.isChecked())
            locationService.serviceMode=DefaultLocationService.BAIDU_MODE;
        else
            locationService.serviceMode=DefaultLocationService.ANDROID_API_MODE;
        locationService.isAutoSwitchService=isAutoSwitchAPI.isChecked();
        if(locationService.activeListeners!=null){
                debugLog("activity demand reconstruct API service with new parameters");
                locationService.reconstructAPIService();
        }
    }

    public void updateMessage(View view) {
        //startActivity(intent);
/*        debugText=(TextView)findViewById(R.id.debug_text);
        BDLocation result=mBDLocationClient.getLastKnownLocation();

        debugMessage="request location returns"+mBDLocationClient.requestLocation()
                +"\n"+"is started"+mBDLocationClient.isStarted();
        if(result!=null)
            debugMessage=debugMessage+"\n"+"getLastKnownLocation"+result.getLocType();
        debugText.setText(debugMessage);
*/
        //The debugText in onCreate seems to be null here? relocate the element
        if(IS_DEBUG_ENABLED) {
            textMessage = (TextView) findViewById(R.id.textMessage);

            if (textMessage != null){
                debugLog("debugText created");
                textMessage.setText(debugMessage);
                debugLog("debugText updated");
            } else
                debugLog("debugText is a null pointer!");
        }
    }
    private void debugLog(String debugMessage){
        if(IS_DEBUG_ENABLED){
            Log.i("DEBUG_debugActivity:",debugMessage);
        }
    }
}
