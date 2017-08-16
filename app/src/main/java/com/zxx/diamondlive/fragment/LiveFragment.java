package com.zxx.diamondlive.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zxx.diamondlive.R;

import java.util.ArrayList;

/**
 * Created by Administrator on 2017/8/15 0015.
 */

public class LiveFragment extends Fragment {

    private View view;
    private TabLayout tabLayout;
    private ViewPager pager;
    private ArrayList<Fragment> listFragment;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = LayoutInflater.from(getActivity()).inflate(R.layout.frg_live, null);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        tabLayout = view.findViewById(R.id.tab_live);
        pager = view.findViewById(R.id.vp_live);
        final String[] title = new String[]{"精选","热门"};
        Live_Selection_Fragment live_selection_fragment = new Live_Selection_Fragment();
        Live_Hot_Fragment live_hot_fragment = new Live_Hot_Fragment();
        listFragment = new ArrayList<>();
        listFragment.add(live_selection_fragment);
        listFragment.add(live_hot_fragment);
        pager.setAdapter(new FragmentPagerAdapter(getChildFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return listFragment.get(position);
            }
            @Override
            public int getCount() {
                return listFragment.size();
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return title[position];
            }
        });
        tabLayout.setupWithViewPager(pager);
    }
}
