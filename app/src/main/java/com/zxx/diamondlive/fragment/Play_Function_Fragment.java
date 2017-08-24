package com.zxx.diamondlive.fragment;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.zxx.diamondlive.R;
import com.zxx.diamondlive.adapter.GiftGridViewAdapter;
import com.zxx.diamondlive.adapter.GiftViewPagerAdapter;
import com.zxx.diamondlive.adapter.Play_Recycler_Hor;
import com.zxx.diamondlive.adapter.Play_Recycler_Ver;
import com.zxx.diamondlive.bean.ChatContent;
import com.zxx.diamondlive.bean.Gift;
import com.zxx.diamondlive.fragment.base.BaseFragment;
import com.zxx.diamondlive.utils.TimeUtil;
import com.zxx.diamondlive.view.MyGridView;
import com.zxx.diamondlive.view.MyViewPager;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.OnClick;

import static com.zxx.diamondlive.R.id.play_tv_date;

/**
 * Created by Administrator on 2017/8/19 0019.
 */

public class Play_Function_Fragment extends BaseFragment {


    @BindView(R.id.play_ll_person_info)
    LinearLayout playLlPersonInfo;
    @BindView(R.id.play_recycler_hor)
    RecyclerView playRecyclerHor;
    @BindView(R.id.play_tv_number)
    TextView playTvNumber;
    @BindView(R.id.play_tv_duration)
    TextView playTvDuration;
    @BindView(play_tv_date)
    TextView playTvDate;
    @BindView(R.id.play_recycler_ver)
    RecyclerView playRecyclerVer;
    @BindView(R.id.play_iv_room_down_chat)
    ImageView playIvRoomDownChat;
    @BindView(R.id.play_iv_room_down_gift)
    ImageView playIvRoomDownGift;
    @BindView(R.id.play_iv_room_down_music)
    ImageView playIvRoomDownMusic;
    @BindView(R.id.play_iv_room_down_close)
    ImageView playIvRoomDownClose;
    private PopupWindow chatPop;
    private View chatPopView;
    private PopupWindow giftPop;
    private static final int GIFTCOLUMNS = 5;//礼物列表的列数
    private static final int PAGESIZE = 15;//礼物列表展示个数
    private View giftPopView;
    private List<ImageView> dotViewLists = new ArrayList<>();
    private ArrayList<ChatContent> chatContents;
    private Play_Recycler_Ver chatContentAdapter;

    private int time = 0;
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1){
                time++;
                playTvDuration.setText(TimeUtil.formatTime(time));
            }
            super.handleMessage(msg);
        }
    };

    Timer timer = new Timer();
    TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            Message message = Message.obtain();
            message.what = 1;
            handler.sendMessage(message);
        }
    };
    @Override
    protected int getContentResId() {
        return R.layout.frg_play_function;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initData();
    }

    private void initData() {
        timer.schedule(timerTask,1000,1000);//1s后执行，再过1s执行
        playTvDate.setText(TimeUtil.getNowDate("yyyy/MM/dd"));
        playRecyclerHor.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        playRecyclerHor.setAdapter(new Play_Recycler_Hor());

        playRecyclerVer.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, true));

        chatContents = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            ChatContent chatContent = new ChatContent("小源"+":","今天天气好晴朗"+i);
            chatContents.add(chatContent);
        }
        Collections.reverse(chatContents);
        chatContentAdapter = new Play_Recycler_Ver(chatContents);
        playRecyclerVer.setAdapter(chatContentAdapter);
        initChatPop();
        initGiftPop();
    }
    //弹出对话气泡窗口
    private void initChatPop(){
        chatPop = new PopupWindow(getActivity());
        chatPop.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        chatPop.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        chatPopView = LayoutInflater.from(getActivity()).inflate(R.layout.chat_pop, null);
        chatPop.setContentView(chatPopView);
        chatPop.setBackgroundDrawable(new ColorDrawable(0x00000000));
        chatPop.setOutsideTouchable(true);
        chatPop.setFocusable(true);

        //处理发送按钮的点击事件
        final EditText et_chat = chatPopView.findViewById(R.id.et_chat_pop);
        Button bt_chat_send = chatPopView.findViewById(R.id.bt_chat_pop);

        bt_chat_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String chat_content = et_chat.getText().toString();
                ChatContent chatContent = new ChatContent("小源" + ":", chat_content);
                chatContents.add(0,chatContent);
                chatContentAdapter.RefreshData(chatContents);
                et_chat.setText("");
            }
        });
    }
    //确定退出Dialog
    private void showCloseDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("温馨提示");
        builder.setMessage("确定结束观看？");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                getActivity().finish();
            }
        });
        builder.setNegativeButton("取消",null);
        builder.create().show();
    }
    //礼物气泡窗口
    private void initGiftPop(){
        //礼物列表的GridView
        ArrayList<MyGridView> myPageList = new ArrayList<>();
        giftPop = new PopupWindow(getActivity());
        giftPop.setOutsideTouchable(true);
        giftPop.setFocusable(true);
        giftPop.setBackgroundDrawable(new ColorDrawable(0x80000000));
        giftPop.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        giftPop.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        giftPopView = LayoutInflater.from(getActivity()).inflate(R.layout.gift_pop, null);
        giftPop.setContentView(giftPopView);


//        给礼物列表设置显示动画
        AnimationSet animationSet = (AnimationSet) AnimationUtils.loadAnimation(getActivity(),R.anim.option_entry_from_bottom);
        giftPopView.startAnimation(animationSet);

        List<Gift.GiftListBean> datas = readJson();
        final int pageCount = (int) Math.ceil(datas.size()*1.0/PAGESIZE);
        for (int i = 0; i < pageCount; i++) {
            //每个页面都创建一个GridView
            MyGridView gridView = new MyGridView(getActivity());
            gridView.setNumColumns(GIFTCOLUMNS);
            gridView.setAdapter(new GiftGridViewAdapter(getActivity(),datas,i,PAGESIZE));
            myPageList.add(gridView);
            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Toast.makeText(getActivity(), "点击事件", Toast.LENGTH_SHORT).show();
                }
            });
        }

        MyViewPager giftPager = giftPopView.findViewById(R.id.vp_gift_pop);
        final GiftViewPagerAdapter myViewAdapter = new GiftViewPagerAdapter(myPageList);
        giftPager.setAdapter(myViewAdapter);

//        给ViewPager设置小圆点
        final LinearLayout ll_cursor = giftPopView.findViewById(R.id.ll_gift_pop_cursor);
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.emoji_cursor_1);
        for (int i = 0; i < myPageList.size(); i++) {
            ImageView iv = new ImageView(getActivity());
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(bitmap.getWidth(), bitmap.getHeight());
            layoutParams.leftMargin = 5;
            iv.setLayoutParams(layoutParams);

            if (i==0){
                iv.setImageResource(R.mipmap.emoji_cursor_2);
            }else{
                iv.setImageResource(R.mipmap.emoji_cursor_1);
            }
            ll_cursor.addView(iv);
            dotViewLists.add(iv);
        }

        giftPager.addOnPageChangeListener(new MyViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }
            @Override
            public void onPageSelected(int position) {
                for (int i = 0; i < pageCount; i++) {
                    //选中的页面改变小圆点为选中状态，反之为未选中
                    if ((position % pageCount) == i) {
                        ImageView selectedIv = (ImageView) ll_cursor.getChildAt(i);
                        selectedIv.setImageResource(R.mipmap.emoji_cursor_2);
                    } else {
                        ImageView selectedIv = (ImageView) ll_cursor.getChildAt(i);
                        selectedIv.setImageResource(R.mipmap.emoji_cursor_1);
                    }
                }
            }
            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    @OnClick({R.id.play_iv_room_down_chat,R.id.play_iv_room_down_gift,R.id.play_iv_room_down_music,R.id.play_iv_room_down_close})
    public void onClick(View view){
             switch (view.getId()){
                 case R.id.play_iv_room_down_chat:
                     chatPop.showAtLocation(chatPopView, Gravity.BOTTOM,0,0);
                     break;
                 case R.id.play_iv_room_down_gift:
                     giftPop.showAtLocation(giftPopView,Gravity.BOTTOM,0,0);
                     break;
                 case R.id.play_iv_room_down_music:

                     break;
                 case R.id.play_iv_room_down_close:
                     showCloseDialog();
                     break;
             }
    }
    private List<Gift.GiftListBean> readJson(){
        List<Gift.GiftListBean> datas = new ArrayList<>();
        try {
            InputStream inputStream = getActivity().getAssets().open("gift.json");
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            Gson gosn = new Gson();
            Gift gift = gosn.fromJson(inputStreamReader, Gift.class);
            datas = gift.getGiftList();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return datas;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        timer.cancel();
        handler.removeCallbacksAndMessages(null);
    }
}
