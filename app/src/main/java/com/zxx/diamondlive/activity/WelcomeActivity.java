package com.zxx.diamondlive.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;

import com.zxx.diamondlive.R;
import com.zxx.diamondlive.activity.base.BaseActivity;

public class WelcomeActivity extends BaseActivity {

    SharedPreferences sp;
    Context mContext = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sp = getSharedPreferences("user",MODE_PRIVATE);
        final String username = sp.getString("user_name", "");
        final String password = sp.getString("password", "");
        new Thread(new Runnable() {
            @Override
            public void run() {
                SystemClock.sleep(3000);
                if (username.equals("") || password.equals("")){
                    Intent intent = new Intent(mContext,LoginActivity.class);
                    startActivity(intent);
                    finish();
                }else {
                    Intent intent = new Intent(mContext, LiveHostActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        }).start();
    }

    @Override
    protected void initTitleBar(HeaderBuilder builder) {
        builder.goneToolbar();
    }

    @Override
    protected int getContentResId() {
        return R.layout.activity_welcome;
    }
}
