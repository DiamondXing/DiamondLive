package com.zxx.diamondlive.test;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.zxx.diamondlive.R;
import com.zxx.diamondlive.fragment.LiveFragment;
import com.zxx.diamondlive.fragment.MeFragment;
import com.zxx.diamondlive.fragment.RoomFragment;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.vp_main)
    ViewPager vpMain;
    @BindView(R.id.bt_main_live)
    ImageView btMainLive;
    @BindView(R.id.bt_main_room)
    ImageView btMainRoom;
    @BindView(R.id.bt_main_me)
    ImageView btMainMe;
    private ArrayList<Fragment> listFragment;
    private LiveFragment liveFragment;
    private RoomFragment roomFragment;
    private MeFragment meFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        ButterKnife.bind(this);
        initVariable();
        initData();
    }

    private void initData() {
        vpMain.setAdapter(new MyPagerAdapter(getSupportFragmentManager()));
    }

    private void initVariable() {
        liveFragment = new LiveFragment();
        roomFragment = new RoomFragment();
        meFragment = new MeFragment();
        listFragment = new ArrayList<>();
        listFragment.add(liveFragment);
        listFragment.add(roomFragment);
        listFragment.add(meFragment);


        vpMain.setCurrentItem(0);
        btMainLive.setImageResource(R.mipmap.tab_live_p);
    }
    @OnClick({R.id.bt_main_live,R.id.bt_main_room,R.id.bt_main_me})
    public void showView(View view){
        resetTab();
        switch (view.getId()) {
            case R.id.bt_main_live:
                vpMain.setCurrentItem(0);
                btMainLive.setImageResource(R.mipmap.tab_live_p);
                break;
            case R.id.bt_main_room:
                vpMain.setCurrentItem(1);
                btMainRoom.setImageResource(R.mipmap.tab_room_p);
                break;
            case R.id.bt_main_me:
                vpMain.setCurrentItem(2);
                btMainMe.setImageResource(R.mipmap.tab_me_p);
                break;
        }
    }
    private void resetTab(){
        btMainLive.setImageResource(R.mipmap.tab_live);
        btMainRoom.setImageResource(R.mipmap.tab_room);
        btMainMe.setImageResource(R.mipmap.tab_me);
    }

    class MyPagerAdapter extends FragmentPagerAdapter{

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
