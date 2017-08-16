package com.zxx.diamondlive.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zxx.diamondlive.R;

/**
 * Created by Administrator on 2017/8/16 0016.
 */

public class Hot_Recy_Ada extends RecyclerView.Adapter<MySelectionAda> {
    Context mContext;

    public Hot_Recy_Ada(Context context) {
        mContext = context;
    }

    @Override
    public MySelectionAda onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.live_item, parent,false);
        MySelectionAda mySelectionAda = new MySelectionAda(view);
        return mySelectionAda;
    }

    @Override
    public void onBindViewHolder(MySelectionAda holder, int position) {
//        holder.ivPhotoLiveItem.setImageResource();
//        holder.tvNameLiveItem.setText();
//        holder.tvDescLiveItem.setText();
//        holder.ivPicLiveItem.setImageResource();
    }

    @Override
    public int getItemCount() {
        return 30;
    }
}

