package com.zxx.diamondlive.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.zxx.diamondlive.R;
import com.zxx.diamondlive.fragment.base.BaseFragment;

import butterknife.BindView;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Administrator on 2017/8/15 0015.
 */

public class MeFragment extends BaseFragment {

    @BindView(R.id.frg_me_iv_avatar)
    CircleImageView frgMeIvAvatar;
    private SharedPreferences sp;

    @Override
    protected int getContentResId() {
        return R.layout.frg_me;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        sp = getActivity().getSharedPreferences("user", Context.MODE_PRIVATE);
        String avatar =sp.getString("avatar","");
        if (!(avatar == null || avatar .equals(""))) {
            Glide.with(getActivity()).load(avatar).into(frgMeIvAvatar);
        }
    }
}
