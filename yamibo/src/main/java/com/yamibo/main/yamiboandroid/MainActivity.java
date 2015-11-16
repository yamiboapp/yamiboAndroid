package com.yamibo.main.yamiboandroid;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.widget.Toast;

import com.yamibo.main.yamibolib.app.YMBActivity;
import com.yamibo.main.yamibolib.app.model.DrawerFragmentInfo;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends YMBActivity implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    private DrawerLayout mDrawerLayout;

    private NavigationDrawerFragment mNavigationDrawerFragment;


    List<DrawerFragmentInfo> mDrawerFragmentInfos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);

        // Set up the drawer.
        initFragments();
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        login(null);
    }

    /**
     * 初始化侧边栏要显示的标题，以及点击时显示的Fragment
     * 在DrawerFragmentInfo生成实例时，前面填写标题，后面填写fragment
     */
    private void initFragments() {
        mDrawerFragmentInfos = new ArrayList<DrawerFragmentInfo>();
        mDrawerFragmentInfos.add(new DrawerFragmentInfo(getString(R.string.navigation_drawer_forum), null));
        mDrawerFragmentInfos.add(new DrawerFragmentInfo(getString(R.string.navigation_drawer_favforum), null));
        mDrawerFragmentInfos.add(new DrawerFragmentInfo(getString(R.string.navigation_drawer_mypm), null));
        mDrawerFragmentInfos.add(new DrawerFragmentInfo(getString(R.string.navigation_drawer_nearby), null));
        mDrawerFragmentInfos.add(new DrawerFragmentInfo(getString(R.string.navigation_drawer_set), null));
        mNavigationDrawerFragment.setDrawerFragmentInfos(mDrawerFragmentInfos);
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        if (mDrawerFragmentInfos == null) {
            return;
        }

        DrawerFragmentInfo info = mDrawerFragmentInfos.get(position);
        if (info.getFragment() == null) {
            Toast.makeText(MainActivity.this, "position:" + position + " title:" + info.getTitle(), Toast.LENGTH_SHORT).show();
        } else {
            Fragment fragment = mDrawerFragmentInfos.get(position).getFragment();
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.container, fragment)
                    .commit();
        }
    }

}
