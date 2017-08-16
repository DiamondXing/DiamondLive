package com.zxx.diamondlive.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.zxx.diamondlive.R;
import com.zxx.diamondlive.adapter.Hot_Recy_Ada;
import com.zxx.diamondlive.fragment.base.BaseFragment;

import butterknife.BindView;

/**
 * Created by Administrator on 2017/8/15 0015.
 */

public class Live_Hot_Fragment extends BaseFragment {

    @BindView(R.id.recycler_hot)
    RecyclerView recyclerHot;

    @Override
    protected int getContentResId() {
        return R.layout.frg_hot;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        recyclerHot.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerHot.setAdapter(new Hot_Recy_Ada(getActivity()));
    }
}
