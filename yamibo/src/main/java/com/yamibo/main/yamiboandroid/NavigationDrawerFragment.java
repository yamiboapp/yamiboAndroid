package com.yamibo.main.yamiboandroid;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.yamibo.main.yamibolib.Utils.Log;
import com.yamibo.main.yamibolib.app.YMBApplication;
import com.yamibo.main.yamibolib.app.model.DrawerFragmentInfo;
import com.yamibo.main.yamibolib.model.UserProfile;

import java.util.List;

public class NavigationDrawerFragment extends Fragment {

    private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";

    private static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";

    private NavigationDrawerCallbacks mCallbacks;

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerListView;
    private View mFragmentContainerView;

    private int mCurrentSelectedPosition = 0;
    private boolean mFromSavedInstanceState;
    private boolean mUserLearnedDrawer;

    private List<DrawerFragmentInfo> mDrawerFragmentInfos;

    private TextView tvMemberUsername, tvMemberUid, tvMemberPoints, tvMemberGroup;
    private ImageView ivMemberAvater;

    public NavigationDrawerFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mUserLearnedDrawer = sp.getBoolean(PREF_USER_LEARNED_DRAWER, false);

        if (savedInstanceState != null) {
            mCurrentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
            mFromSavedInstanceState = true;
        }

        selectItem(mCurrentSelectedPosition);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        RelativeLayout layout = (RelativeLayout) inflater.inflate(
                R.layout.fragment_navigation_drawer, container, false);

        tvMemberUsername = (TextView) layout.findViewById(R.id.member_username);
        tvMemberUid = (TextView) layout.findViewById(R.id.member_uid);
        tvMemberPoints = (TextView) layout.findViewById(R.id.user_points);
        tvMemberGroup = (TextView) layout.findViewById(R.id.groupid);
        ivMemberAvater = (ImageView) layout.findViewById(R.id.member_avatar);

        mDrawerListView = (ListView) layout.findViewById(R.id.listview_navigation_drawer);
        mDrawerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectItem(position);
            }
        });

        mDrawerListView.setItemChecked(mCurrentSelectedPosition, true);

        updateUserProfile();
        return layout;
    }

    public List<DrawerFragmentInfo> getDrawerFragmentInfos() {
        return mDrawerFragmentInfos;
    }

    public void setDrawerFragmentInfos(List<DrawerFragmentInfo> mDrawerFragmentInfos) {
        this.mDrawerFragmentInfos = mDrawerFragmentInfos;
        if (mDrawerFragmentInfos == null) {
            Log.e("mDrawerFragmentInfos is null,please initialize");
        } else {
            mDrawerListView.setAdapter(new NavigationDrawerAdapter(mDrawerFragmentInfos, getActivity()));
        }
    }

    public boolean isDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(mFragmentContainerView);
    }


    public void setUp(int fragmentId, DrawerLayout drawerLayout) {
        mFragmentContainerView = getActivity().findViewById(fragmentId);
        mDrawerLayout = drawerLayout;
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
    }

    private void selectItem(int position) {
        mCurrentSelectedPosition = position;
        if (mDrawerListView != null) {
            mDrawerListView.setItemChecked(position, true);
        }
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(mFragmentContainerView);
        }
        if (mCallbacks != null) {
            mCallbacks.onNavigationDrawerItemSelected(position);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallbacks = (NavigationDrawerCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement NavigationDrawerCallbacks.");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_POSITION, mCurrentSelectedPosition);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (mDrawerLayout != null && isDrawerOpen()) {
            inflater.inflate(R.menu.global, menu);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    public interface NavigationDrawerCallbacks {
        void onNavigationDrawerItemSelected(int position);
    }

    class NavigationDrawerAdapter extends BaseAdapter {
        List<DrawerFragmentInfo> mTitles;
        Context mContext;

        NavigationDrawerAdapter(List<DrawerFragmentInfo> list, Context context) {
            this.mTitles = list;
            this.mContext = context;
        }

        @Override
        public int getCount() {
            return mTitles.size();
        }

        @Override
        public Object getItem(int i) {
            return mTitles.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder holder = null;
            if (view == null) {
                view = LayoutInflater.from(mContext).inflate(R.layout.item_navigation_drawer, null);
                holder = new ViewHolder();
                holder.tvTitle = (TextView) view.findViewById(R.id.page_title);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }

            holder.tvTitle.setText(mTitles.get(i).getTitle());

            return view;
        }

        class ViewHolder {
            TextView tvTitle;
        }
    }

    public void updateUserProfile() {
        UserProfile profile = YMBApplication.instance().accountService().profile();
        if (profile == null) {
            return;
        }
        tvMemberUsername.setText(profile.getMember_username());
//        tvMemberGroup.setText(profile.getGroupid());
        tvMemberUid.setText(getActivity().getString(R.string.navigation_drawer_member_uid) + profile.getMember_uid());
//        tvMemberPoints
    }
}
