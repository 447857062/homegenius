package com.deplink.boruSmart.activity.device.doorbell.add;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;

import com.deplink.boruSmart.view.combinationwidget.TitleLayout;

import deplink.com.smartwirelessrelay.homegenius.EllESDK.R;

public class ApModeActivity extends Activity implements View.OnClickListener{
    private Button button_next_step;
    private TitleLayout layout_title;
    private ImageView imageview_ap_gif;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ap_mode);
        initViews();
        initEvents();
        initDatas();
    }

    private void initDatas() {
        layout_title.setReturnClickListener(new TitleLayout.ReturnImageClickListener() {
            @Override
            public void onBackPressed() {
                ApModeActivity.this.onBackPressed();
            }
        });

    }


    private void initEvents() {
        button_next_step.setOnClickListener(this);
        final Animation animationFadeIn= AnimationUtils.loadAnimation(this, R.anim.fade_in_doorbell);
        final Animation animationFadeOut= AnimationUtils.loadAnimation(this, R.anim.fade_out_doorbell);
        final Animation animationFadeHold= AnimationUtils.loadAnimation(this, R.anim.fade_hold_doorbell);
        animationFadeIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                imageview_ap_gif.startAnimation(animationFadeOut);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        animationFadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                imageview_ap_gif.startAnimation(animationFadeHold);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        animationFadeHold.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                imageview_ap_gif.startAnimation(animationFadeIn);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        imageview_ap_gif.startAnimation(animationFadeIn);
    }

    private void initViews() {
        button_next_step = findViewById(R.id.button_next_step);
        layout_title= findViewById(R.id.layout_title);
        imageview_ap_gif= findViewById(R.id.imageview_ap_gif);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){

            case R.id.button_next_step:
                startActivity(new Intent(this,ConnectApWifiActivity.class));
                break;
        }
    }
}
