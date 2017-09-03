package com.zxx.diamondlive.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.zxx.diamondlive.R;
import com.zxx.diamondlive.activity.PlayActivity;
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

import org.dync.giftlibrary.widget.GiftControl;
import org.dync.giftlibrary.widget.GiftModel;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.Unbinder;
import de.hdodenhof.circleimageview.CircleImageView;

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
    @BindView(R.id.play_tv_date)
    TextView playTvDate;
    @BindView(R.id.giftParent)
    LinearLayout giftParent;
    @BindView(R.id.play_avatar_iv)
    CircleImageView playAvatarIv;
    @BindView(R.id.play_live_name_tv)
    TextView playLiveNameTv;
    @BindView(R.id.play_user_name_tv)
    TextView playUserNameTv;
    @BindView(R.id.play_chronometer)
    Chronometer playChronometer;
    Unbinder unbinder;

    private PopupWindow chatPop;
    private View chatPopView;
    private PopupWindow giftPop;
    private static final int GIFTCOLUMNS = 5;//礼物列表的列数
    private static final int PAGESIZE = 15;//礼物列表展示个数
    private View giftPopView;
    private List<ImageView> dotViewLists = new ArrayList<>();
    private ArrayList<ChatContent> chatContents;
    private Play_Recycler_Ver chatContentAdapter;

    private GiftControl giftControl;
    private String live_name;
    private String avatar;
    private String user_name;
    private long userId;
    private InputMethodManager imm;
    private String my_name;

    @Override
    protected int getContentResId() {
        return R.layout.frg_play_function;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initData();
        initView();
    }

    private void initView() {
        playChronometer.start();
        //获取直播名
        live_name = ((PlayActivity) getActivity()).getLive_name();
        //获取直播用户信息
        avatar = ((PlayActivity) getActivity()).getAvatar();
        user_name = ((PlayActivity) getActivity()).getUser_name();
        userId = ((PlayActivity) getActivity()).getUser_id();
        if (!TextUtils.isEmpty(avatar)) {
            Glide.with(getActivity()).load(avatar)
                    .error(R.mipmap.ic_my_avatar)
                    .into(playAvatarIv);
        }else{
            Glide.with(getActivity()).load(R.mipmap.ic_my_avatar).into(playAvatarIv);
        }
        if (!TextUtils.isEmpty(live_name)) {
            playLiveNameTv.setText(live_name);
        }
        if (!TextUtils.isEmpty(user_name)) {
            playUserNameTv.setText(user_name);
        }
        giftControl = new GiftControl(getActivity());
        giftControl.setGiftLayout(false, giftParent, 3);
    }

    private void initData() {
        //获取用户自己的名字
        SharedPreferences sp = getActivity().getSharedPreferences("user",Context.MODE_PRIVATE);
        my_name = sp.getString("user_name", "");
        playTvDate.setText(TimeUtil.getNowDate("yyyy/MM/dd"));
        ArrayList<String> list = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            list.add(avatar);
        }
        playRecyclerHor.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        playRecyclerHor.setAdapter(new Play_Recycler_Hor(list));

        playRecyclerVer.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, true));

        chatContents = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            ChatContent chatContent = new ChatContent("小源", "今天天气好晴朗");
            chatContents.add(chatContent);
        }
//        Collections.reverse(chatContents);
        chatContentAdapter = new Play_Recycler_Ver(chatContents);
        playRecyclerVer.setAdapter(chatContentAdapter);
        initChatPop();
        initGiftPop();
    }

    //弹出对话气泡窗口
    private void initChatPop() {
        imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        chatPop = new PopupWindow(getActivity());
        chatPop.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        chatPop.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        chatPopView = LayoutInflater.from(getActivity()).inflate(R.layout.chat_pop, null);
        chatPop.setContentView(chatPopView);
        chatPop.setBackgroundDrawable(new ColorDrawable(0x00000000));
        chatPop.setOutsideTouchable(true);
        chatPop.setFocusable(true);
        chatPop.setSoftInputMode(PopupWindow.INPUT_METHOD_NEEDED);
        chatPop.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        //处理发送按钮的点击事件
        final EditText et_chat = chatPopView.findViewById(R.id.et_chat_pop);
        Button bt_chat_send = chatPopView.findViewById(R.id.bt_chat_pop);

        bt_chat_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imm.hideSoftInputFromWindow(et_chat.getWindowToken(), 0);
                String chat_content = et_chat.getText().toString();
                ChatContent chatContent = new ChatContent(my_name, chat_content);
                chatContents.add(0,chatContent);
                chatContentAdapter.RefreshData(chatContents);
                et_chat.setText("");
            }
        });
    }

    //礼物气泡窗口
    private void initGiftPop() {
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
        AnimationSet animationSet = (AnimationSet) AnimationUtils.loadAnimation(getActivity(), R.anim.option_entry_from_bottom);
        giftPopView.startAnimation(animationSet);

        final List<Gift.GiftListBean> datas = readJson();
        final int pageCount = (int) Math.ceil(datas.size() * 1.0 / PAGESIZE);
        for (int i = 0; i < pageCount; i++) {
            //每个页面都创建一个GridView
            MyGridView gridView = new MyGridView(getActivity());
            gridView.setNumColumns(GIFTCOLUMNS);
            gridView.setAdapter(new GiftGridViewAdapter(getActivity(), datas, i, PAGESIZE));
            myPageList.add(gridView);
            final int currentPage = i;
            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                    GiftModel giftModel = new GiftModel();
                    position = position + currentPage * PAGESIZE;
                    giftModel.setGiftId(datas.get(position).getGiftPrice());
                    giftModel.setGiftName(datas.get(position).getGiftName());//礼物名字
                    giftModel.setGiftCount(1);
                    giftModel.setGiftPic(datas.get(position).getGiftPic());
                    giftModel.setGiftPrice(datas.get(position).getGiftPrice());
                    giftModel.setSendUserName(live_name);
                    giftModel.setSendGiftTime(System.currentTimeMillis());
                    giftModel.setCurrentStart(false);
                    giftModel.setSendUserId(String.valueOf(userId));
                    giftModel.setSendUserPic(avatar);
                    giftModel.setHitCombo(0);
                    giftControl.loadGift(giftModel);
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

            if (i == 0) {
                iv.setImageResource(R.mipmap.emoji_cursor_2);
            } else {
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

    @OnClick({R.id.play_iv_room_down_chat, R.id.play_iv_room_down_gift, R.id.play_iv_room_down_music, R.id.play_iv_room_down_close})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.play_iv_room_down_chat:
                chatPop.showAtLocation(chatPopView, Gravity.BOTTOM, 0, 0);
                break;
            case R.id.play_iv_room_down_gift:
                giftPop.showAtLocation(giftPopView, Gravity.BOTTOM, 0, 0);
                break;
            case R.id.play_iv_room_down_music:

                break;
            case R.id.play_iv_room_down_close:
                ((PlayActivity)(getActivity())).showCloseDialog();
                break;
        }
    }

    private List<Gift.GiftListBean> readJson() {
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
}
