package com.yamibo.main.yamibolib.app.model;


import android.support.v4.app.Fragment;

/**
 * Created by remiany on 2015/11/15 0015.
 */
public class DrawerFragmentInfo {
    protected String title;
    protected Fragment fragment;

    public DrawerFragmentInfo(String title, Fragment fragment) {
        this.title = title;
        this.fragment = fragment;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Fragment getFragment() {
        return fragment;
    }

    public void setFragment(Fragment fragment) {
        this.fragment = fragment;
    }
}
