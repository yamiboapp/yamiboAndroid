package com.yamibo.main.yamiboandroid.main;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.yamibo.main.yamiboandroid.R;
import com.yamibo.main.yamibolib.accountservice.AccountListener;
import com.yamibo.main.yamibolib.accountservice.AccountService;
import com.yamibo.main.yamibolib.accountservice.LoginResultListener;
import com.yamibo.main.yamibolib.app.YMBFragment;
import com.yamibo.main.yamibolib.model.UserProfile;
import com.yamibo.main.yamibolib.widget.BasicItem;
import com.yamibo.main.yamibolib.widget.YMBNetworkImageView;

public class NavigationDrawerFragment extends YMBFragment implements LoginResultListener, AccountListener {

    private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";

    private static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerListView;
    private View mFragmentContainerView;

    private int mCurrentSelectedPosition = 0;
    private boolean mFromSavedInstanceState;
    private boolean mUserLearnedDrawer;
    private LinearLayout mUserDetail;
    private Button mLoginButton;

    private TextView tvMemberUsername, tvMemberUid, tvMemberCredits, tvMemberGrouptitle;
    private ImageView iv_gender;
    private YMBNetworkImageView ivMemberAvater;

    private MenuItem[] mMenuItems = new MenuItem[]{
            //论坛
            new MenuItem(R.drawable.bg_menu_forum_rest, R.string.navigation_drawer_forum, new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            }),

            //收藏
            new MenuItem(R.drawable.bg_menu_fav_rest, R.string.navigation_drawer_favforum, new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            }),

            //消息
            new MenuItem(R.drawable.bg_menu_pm_rest, R.string.navigation_drawer_mypm, new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            }),

            //附近的人
            new MenuItem(R.drawable.bg_menu_nearby_rest, R.string.navigation_drawer_nearby, new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            }),

            //设置
            new MenuItem(R.drawable.bg_menu_set_rest, R.string.navigation_drawer_set, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity("ymb://setting");
                    if (mDrawerLayout != null) {
                        mDrawerLayout.closeDrawer(mFragmentContainerView);
                    }

                }
            }),
    };

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
        accountService().addListener(this);
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
        iv_gender =  (ImageView) layout.findViewById(R.id.iv_gender);
        tvMemberUid = (TextView) layout.findViewById(R.id.member_uid);
        tvMemberGrouptitle = (TextView) layout.findViewById(R.id.grouptitle);
        tvMemberCredits = (TextView) layout.findViewById(R.id.user_points);
        ivMemberAvater = (YMBNetworkImageView) layout.findViewById(R.id.member_avatar);

        mDrawerListView = (ListView) layout.findViewById(R.id.listview_navigation_drawer);

        mDrawerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectItem(position);
            }
        });

        mUserDetail = (LinearLayout) layout.findViewById(R.id.user_detail);
        mLoginButton = (Button) layout.findViewById(R.id.btn_login);
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login(NavigationDrawerFragment.this);
            }
        });

        mDrawerListView.setItemChecked(mCurrentSelectedPosition, true);
        return layout;
    }

    @Override
    public void onDestroy() {
        accountService().removeListener(this);
        super.onDestroy();
    }

    /**
     * 初始化侧边栏要显示的标题，以及点击时显示的Fragment
     * 在DrawerFragmentInfo生成实例时，前面填写标题，后面填写fragment
     */
    private void initFragments() {
        mDrawerListView.setAdapter(new NavigationDrawerAdapter(mMenuItems, getActivity()));
    }


    public void onNavigationDrawerItemSelected(int position) {

    }

    public boolean isDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(mFragmentContainerView);
    }


    public void setUp(int fragmentId, DrawerLayout drawerLayout) {
        mFragmentContainerView = getActivity().findViewById(fragmentId);
        mDrawerLayout = drawerLayout;
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        initFragments();
    }

    private void selectItem(int position) {
        mCurrentSelectedPosition = position;
        if (mDrawerListView != null) {
            mDrawerListView.setItemChecked(position, true);
        }
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(mFragmentContainerView);
        }
        onNavigationDrawerItemSelected(position);
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

    @Override
    public void onAccountChanged(AccountService sender) {
        updateUserProfile();
    }

    class NavigationDrawerAdapter extends BaseAdapter {
        Context mContext;
        MenuItem[] mMenuItems;

        NavigationDrawerAdapter(MenuItem[] menuItems, Context context) {
            this.mMenuItems = menuItems;
            this.mContext = context;
        }

        @Override
        public int getCount() {
            return mMenuItems.length;
        }

        @Override
        public Object getItem(int i) {
            return mMenuItems[i];
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = LayoutInflater.from(mContext).inflate(R.layout.navigation_item, null);
            }
            BasicItem basicItem = (BasicItem) view.findViewById(R.id.list_item);
            basicItem.setIconRes(mMenuItems[i].iconResId);
            basicItem.setTitle(getString(mMenuItems[i].title));
            basicItem.setClickable(true);
            basicItem.setOnClickListener(mMenuItems[i].clickListener);
            if (i == mMenuItems.length - 1) {
                view.findViewById(R.id.gray_line).setVisibility(View.GONE);
            } else {
                view.findViewById(R.id.gray_line).setVisibility(View.VISIBLE);
            }

            return view;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUserProfile();
    }

    public void updateUserProfile() {
        if (getActivity() == null)
            return;
        UserProfile profile = accountService().profile();
        if (accountService().isLogin()) {
            mUserDetail.setVisibility(View.VISIBLE);
            mLoginButton.setVisibility(View.GONE);
            tvMemberUsername.setText(profile.getMember_username());
            Log.d("profile.getGender()", String.valueOf(profile.getGender()));
            if(profile.getGender() == 1){//男
                iv_gender.setImageResource(R.drawable.boy);
            }else if(profile.getGender() == 2){//女
                iv_gender.setImageResource(R.drawable.girl);
            }else{
                iv_gender.setVisibility(View.GONE);
            }

            ivMemberAvater.setImageUri(profile.getMember_avatar());
            tvMemberGrouptitle.setText(profile.getGrouptitle());
            tvMemberCredits.setText("积分：" + profile.getCredits());
            tvMemberUid.setText(getActivity().getString(R.string.navigation_drawer_member_uid) + profile.getMember_uid());

        } else {
            mUserDetail.setVisibility(View.GONE);
            mLoginButton.setVisibility(View.VISIBLE);
            ivMemberAvater.setLocalResourceId(R.drawable.icon_logo);
        }
    }


    @Override
    public void onLoginSuccess(AccountService sender) {
        updateUserProfile();

    }

    @Override
    public void onLoginCancel(AccountService sender) {
        updateUserProfile();
    }


    private class MenuItem {
        public MenuItem(int iconResId, int title, View.OnClickListener listener) {
            this.iconResId = iconResId;
            this.title = title;
            this.clickListener = listener;
        }

        public int iconResId;
        public int title;
        public View.OnClickListener clickListener;
    }
}
