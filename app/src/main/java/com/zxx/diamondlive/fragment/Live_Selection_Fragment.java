package com.zxx.diamondlive.fragment;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import com.cjj.MaterialRefreshLayout;
import com.cjj.MaterialRefreshListener;
import com.zxx.diamondlive.R;
import com.zxx.diamondlive.adapter.Selection_Recy_Ada;
import com.zxx.diamondlive.bean.Live;
import com.zxx.diamondlive.fragment.base.BaseNetFragment;
import com.zxx.diamondlive.network.RetrofitManager;
import com.zxx.diamondlive.network.api.LiveApi;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import okhttp3.FormBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Administrator on 2017/8/15 0015.
 */

public class Live_Selection_Fragment extends BaseNetFragment<Live> {


    @BindView(R.id.recycler_select)
    RecyclerView recyclerSelect;
    @BindView(R.id.select_refresh)
    MaterialRefreshLayout selectRefresh;
    private Selection_Recy_Ada adapter;
    private int type = 0;
    private int page = 1;
    private List<Live.ResultBean.ListBean> list;
    private List<Live.ResultBean.ListBean> newList = new ArrayList<>();

    @Override
    protected int getContentResId() {
        return R.layout.frg_select;
    }

    @Override
    protected void initViews() {
        recyclerSelect.setLayoutManager(new LinearLayoutManager(getActivity()));
        initRefresh();
    }

    @Override
    protected void loadData() {
        getData();
    }
    private List<Live.ResultBean.ListBean> getData(){
        LiveApi liveApi = RetrofitManager.getTestRetrofit().create(LiveApi.class);
        FormBody formBody = new FormBody.Builder()
                .add("type", type+"")
                .add("page", page+"")
                .build();
        Call<Live> liveCall = liveApi.postLive(formBody);
        liveCall.enqueue(new Callback<Live>() {
            @Override
            public void onResponse(Call<Live> call, Response<Live> response) {
                if (response.body().getResult().getList() == null){
                    return;
                }
                goneLoading();
                selectRefresh.finishRefresh();
                selectRefresh.finishRefreshLoadMore();
                list = response.body().getResult().getList();
                if (adapter == null) {
                    adapter = new Selection_Recy_Ada(getActivity(), list);
                    recyclerSelect.setAdapter(adapter);
                    newList.addAll(list);
                }
            }
            @Override
            public void onFailure(Call<Live> call, Throwable t) {
                selectRefresh.finishRefresh();
                selectRefresh.finishRefreshLoadMore();
                Toast.makeText(getActivity(), "网络连接失败", Toast.LENGTH_SHORT).show();
            }
        });
        return list;
    }

    @Override
    protected void processData(Live live) {

    }

    private void initRefresh() {
        selectRefresh.setLoadMore(true);
        selectRefresh.setMaterialRefreshListener(new MaterialRefreshListener() {
            @Override
            public void onRefresh(MaterialRefreshLayout materialRefreshLayout) {
                List<Live.ResultBean.ListBean> newList = getData();
                adapter.refresh(newList);
            }

            @Override
            public void onRefreshLoadMore(MaterialRefreshLayout materialRefreshLayout) {
                page += 1;
                newList.addAll(newList.size()-1,getData());
                adapter.refresh(newList);
            }
        });
    }
}
