package com.zxx.diamondlive.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import com.zxx.diamondlive.R;
import com.zxx.diamondlive.activity.base.BaseActivity;
import com.zxx.diamondlive.fragment.Play_Empty_Fragment;
import com.zxx.diamondlive.fragment.Play_Function_Fragment;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

public class PlayActivity extends BaseActivity {

    @BindView(R.id.vp_play)
    ViewPager vpPlay;
    private List<Fragment> listFragment;

    @Override
    protected void initTitleBar(HeaderBuilder builder) {
        builder.goneToolbar();
    }

    @Override
    protected int getContentResId() {
        return R.layout.activity_play;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initVariable();
        initData();
    }

    private void initVariable() {
        Play_Empty_Fragment play_empty_fragment = new Play_Empty_Fragment();
        Play_Function_Fragment play_function_fragment = new Play_Function_Fragment();

        listFragment = new ArrayList<>();
        listFragment.add(play_empty_fragment);
        listFragment.add(play_function_fragment);
    }

    private void initData() {
        vpPlay.setAdapter(new MyPagerAdapter(getSupportFragmentManager()));
        vpPlay.setCurrentItem(1);
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
    };
}
