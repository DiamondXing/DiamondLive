package com.zxx.diamondlive.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.zxx.diamondlive.R;
import com.zxx.diamondlive.activity.base.BaseActivity;
import com.zxx.diamondlive.bean.User;
import com.zxx.diamondlive.fragment.LiveFragment;
import com.zxx.diamondlive.fragment.MeFragment;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.OnClick;

public class LiveHostActivity extends BaseActivity {


    @BindView(R.id.vp_main)
    ViewPager vpMain;
    @BindView(R.id.rb_live_main)
    RadioButton rbLiveMain;
    @BindView(R.id.rb_room_main)
    RadioButton rbRoomMain;
    @BindView(R.id.rb_me_main)
    RadioButton rbMeMain;
    @BindView(R.id.rg_main)
    RadioGroup rgMain;

    private ArrayList<Fragment> listFragment;
    private LiveFragment liveFragment;
    private MeFragment meFragment;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        user = (User) intent.getSerializableExtra("user");
        initVariable();
        initData();
    }
    //获取User对象
    public User getUser(){
        return user;
    }

    @Override
    protected void initTitleBar(HeaderBuilder builder) {
        builder.goneToolbar();
    }

    @Override
    protected int getContentResId() {
        return R.layout.live_host;
    }

    private void initData() {
        vpMain.setAdapter(new MyPagerAdapter(getSupportFragmentManager()));
        vpMain.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == 0){
                    rbLiveMain.setChecked(true);
                }else if (position == 1){
                    rbMeMain.setChecked(true);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void initVariable() {
        liveFragment = new LiveFragment();
        meFragment = new MeFragment();
        listFragment = new ArrayList<>();
        listFragment.add(liveFragment);
        listFragment.add(meFragment);


        vpMain.setCurrentItem(0);
        rbLiveMain.setChecked(true);
    }

    @OnClick({R.id.rb_live_main, R.id.rb_room_main, R.id.rb_me_main})
    public void showView(View view) {
        switch (view.getId()) {
            case R.id.rb_live_main:
                vpMain.setCurrentItem(0);
                break;
            case R.id.rb_room_main:
                break;
            case R.id.rb_me_main:
                vpMain.setCurrentItem(1);
                break;
        }
    }


    class MyPagerAdapter extends FragmentPagerAdapter {

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return listFragment.get(position);
        }

        @Override
        public int getCount() {
            return listFragment.size();
        }
    }
}
