package com.deplink.boruSmart.activity.device.router.wifi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

import com.deplink.boruSmart.constant.AppConstant;
import com.deplink.boruSmart.manager.connect.remote.HomeGenius;
import com.deplink.boruSmart.manager.device.DeviceManager;
import com.deplink.boruSmart.manager.device.router.RouterManager;
import com.deplink.boruSmart.util.Perfence;
import com.deplink.boruSmart.view.combinationwidget.TitleLayout;
import com.deplink.sdk.android.sdk.DeplinkSDK;
import com.deplink.sdk.android.sdk.EventCallback;
import com.deplink.sdk.android.sdk.SDKAction;
import com.deplink.sdk.android.sdk.manager.SDKManager;

import deplink.com.smartwirelessrelay.homegenius.EllESDK.R;

public class WiFiSettingActivity extends Activity implements View.OnClickListener{
    private static final String TAG = "WiFiSettingActivity";
    private RelativeLayout layout_wifi_24;
    private RelativeLayout layout_wifi_custom;
    private RelativeLayout layout_signal_strength;
    private boolean isUserLogin;
    private SDKManager manager;
    private EventCallback ec;
    private RouterManager mRouterManager;
    private HomeGenius mHomeGenius;
    private String channels;
    private TitleLayout layout_title;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_setting);
        initViews();
        initDatas();
        initEvents();
    }
    private void initDatas() {
        layout_title.setReturnClickListener(new TitleLayout.ReturnImageClickListener() {
            @Override
            public void onBackPressed() {
                WiFiSettingActivity.this.onBackPressed();
            }
        });
        mRouterManager=RouterManager.getInstance();
        mRouterManager.InitRouterManager();
        DeplinkSDK.initSDK(getApplicationContext(), Perfence.SDK_APP_KEY);
        manager = DeplinkSDK.getSDKManager();
        ec=new EventCallback() {
            @Override
            public void onSuccess(SDKAction action) {

            }

            @Override
            public void onBindSuccess(SDKAction action, String devicekey) {

            }



            @Override
            public void onFailure(SDKAction action, Throwable throwable) {

            }

            @Override
            public void connectionLost(Throwable throwable) {
                super.connectionLost(throwable);

            }
        };
    }


    private boolean deviceOnline;

    @Override
    protected void onResume() {
        super.onResume();
        if( !DeviceManager.getInstance().isStartFromExperience()){
            mHomeGenius = new HomeGenius();
            channels = mRouterManager.getCurrentSelectedRouter().getRouter().getChannels();
            isUserLogin = Perfence.getBooleanPerfence(AppConstant.USER_LOGIN);

            //登录了，并且绑定了设备
            if (isUserLogin && deviceOnline) {
                layout_wifi_custom.setVisibility(View.VISIBLE);//目前不支持访客WIFI
                layout_signal_strength.setVisibility(View.VISIBLE);
            } else {
                layout_wifi_custom.setVisibility(View.GONE);
                layout_signal_strength.setVisibility(View.GONE);
            }
            manager.addEventCallback(ec);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        manager.removeEventCallback(ec);
    }

    private void initEvents() {
        layout_wifi_24.setOnClickListener(this);
        layout_wifi_custom.setOnClickListener(this);
        layout_signal_strength.setOnClickListener(this);
    }

    private void initViews() {
        layout_wifi_24 = (RelativeLayout) findViewById(R.id.layout_wifi_24);
        layout_wifi_custom = (RelativeLayout) findViewById(R.id.layout_wifi_custom);
        layout_signal_strength = (RelativeLayout) findViewById(R.id.layout_signal_strength);
        layout_title= (TitleLayout) findViewById(R.id.layout_title);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.layout_wifi_24:
                startActivity(new Intent(WiFiSettingActivity.this, WifiSetting24.class));
                break;
            case R.id.layout_wifi_custom:
                startActivity(new Intent(WiFiSettingActivity.this, WifiSettingCustom.class));
                break;
            case R.id.layout_signal_strength:
                startActivity(new Intent(WiFiSettingActivity.this, SignalStrengthSetting.class));
                break;

        }
    }
}
