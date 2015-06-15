package com.yamibo.main.yamiboandroid;

import android.app.Activity;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.yamibo.main.yamibolib.locationservice.impl.DefaultLocationService;

import static com.yamibo.main.yamibolib.locationservice.impl.BDLocationService.bdLocationToString;
/*
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.yamibo.main.yamibolib.locationservice.impl.BDLocationApplication;
*/

public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    /**
     * DEBUG_CODE show debug text on the mobile
     * For cleaning up : Delete the code followed by the commend "DEBUG_CODE", and the elements in the xml file.
     * if want to preserve the debug activity page: mmove them to a new activity page.
     */
    protected TextView debugText;
    protected Button debugButton;
    protected String debugMessage="Hello World";
    private static final boolean IS_DEBUG_ENABLED=true;
    DefaultLocationService locationService;
    /**
     * DEBUG_CODE to remove
     */
    protected LocationClient mBDLocationClient = null;
    private LocationClientOption.LocationMode tempMode = LocationClientOption.LocationMode.Hight_Accuracy;
    private String tempcoor="gcj02";
    private int frequence=-1;
    private boolean isNeedAddress=true;
    private Context mContext;
    private BDLocationListener listener1=null;
    //Baidu sample, set opton for the client
    private void applyOption() {
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(tempMode);//设置定位模式
        option.setCoorType(tempcoor);//返回的定位结果是百度经纬度，默认值gcj02
        int span=frequence;//设置发起定位请求的间隔时间为1000ms
        if(frequence>=1000)//Baidu: 前后两次请求定位时间间隔不能小于1000ms。
            span=frequence;
        else
            com.yamibo.main.yamibolib.Utils.Log.i("DEBUG applyOption", "Invalid frequence. use default value");
        option.setScanSpan(span);
        option.setIsNeedAddress(isNeedAddress);
        mBDLocationClient.setLocOption(option);
        com.yamibo.main.yamibolib.Utils.Log.i("DEBUG", "initiate Location done");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        /**
         * DEBUG_CODE for testing the functionality of the location service lib
         */
        locationService=new DefaultLocationService(getApplicationContext());
        locationService.start();

        mBDLocationClient=new LocationClient(getApplicationContext());
        listener1=new BDLocationListener() {
            @Override
            public void onReceiveLocation(BDLocation bdLocation) {
                debugLog("the debug listener1 has received location "+bdLocationToString(bdLocation));
            }
        };
        mBDLocationClient.registerLocationListener(listener1);
        applyOption();
        mBDLocationClient.start();



        debugButton=(Button)findViewById(R.id.debug_button);
        if(debugButton!=null)
            debugLog("debugButton created");
        //debugText reference seems to be null when called by debugShow(), why?
        debugText=(TextView)findViewById(R.id.debug_text);
        if(debugText!=null)
            debugLog("debugText created");
/*
     mBDLocationClient = ((BDLocationApplication)getApplication()).mBDLocationClient;

        applyOption();
        mBDLocationClient.start();
        mBDLocationClient.requestLocation();
        Log.i("DEBUG", " mBDLocationClient " + mBDLocationClient.toString());
*/




    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                .commit();
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section1);
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                break;
            case 3:
                mTitle = getString(R.string.title_section3);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
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

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MainActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }

    /**
     * Clover:
     * DEBUG_CODE click the debugButton to show the debugMessage(should be the last time location)
     * and request a new loaction (won't be shown this time
     */
    public void debugShow(View view) {
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
           debugText = (TextView) findViewById(R.id.debug_text);
           debugButton=(Button)findViewById(R.id.debug_button);

           if (debugText != null)
               debugLog("debugText created");

           if (locationService != null) {
               locationService.refresh();
               debugMessage = locationService.debugMessage;
               debugLog("location debugMessage assigned");
           }

           if(mBDLocationClient!=null){
               int code=mBDLocationClient.requestLocation();
               debugLog("mBDLocationClient location request has returned code= "+code);
           }


           if (debugText != null) {
               debugText.setText(debugMessage);
               debugLog("debugText updated");
           } else
               debugLog("debugText is null!");
       }
    }
    private void debugLog(String debugMessage){
        if(IS_DEBUG_ENABLED){
            Log.i("main","DEBUG_"+debugMessage);
        }
    }


}
