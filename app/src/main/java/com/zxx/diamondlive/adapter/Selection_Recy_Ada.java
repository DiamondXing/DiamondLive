package com.zxx.diamondlive.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.zxx.diamondlive.R;
import com.zxx.diamondlive.bean.Live;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Administrator on 2017/8/16 0016.
 */

public class Selection_Recy_Ada extends RecyclerView.Adapter<MySelectionAda> {
    Context mContext;
    List<Live.ResultBean.ListBean> mList;
    public Selection_Recy_Ada(Context context,List<Live.ResultBean.ListBean> list) {
        mContext = context;
        mList = list;
    }
    public void refresh(List<Live.ResultBean.ListBean> list){
        mList = list;
        notifyDataSetChanged();
    }

    @Override
    public MySelectionAda onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.live_item, parent,false);
        MySelectionAda mySelectionAda = new MySelectionAda(view);
        return mySelectionAda;
    }

    @Override
    public void onBindViewHolder(MySelectionAda holder, int position) {
        if (position < mList.size()) {
            Glide.with(mContext).load(mList.get(position)
                    .getUser().getUser_data().getAvatar()).into(holder.ivPhotoLiveItem);
            Glide.with(mContext).load(mList.get(position)
                    .getData().getPic()).into(holder.ivPicLiveItem);
            holder.tvNameLiveItem.setText(mList.get(position).getData().getLive_name());
            holder.tvDescLiveItem.setText(mList.get(position).getUser()
                    .getUser_data().getUser_name());
        }
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }
}

class MySelectionAda extends RecyclerView.ViewHolder {
    @BindView(R.id.iv_photo_live_item)
    ImageView ivPhotoLiveItem;
    @BindView(R.id.tv_name_live_item)
    TextView tvNameLiveItem;
    @BindView(R.id.tv_desc_live_item)
    TextView tvDescLiveItem;
    @BindView(R.id.iv_pic_live_item)
    ImageView ivPicLiveItem;
    public MySelectionAda(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

}
