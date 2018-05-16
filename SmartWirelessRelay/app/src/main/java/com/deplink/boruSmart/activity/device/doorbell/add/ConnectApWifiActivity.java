package com.deplink.boruSmart.activity.device.doorbell.add;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.deplink.boruSmart.constant.AppConstant;
import com.deplink.boruSmart.util.Perfence;
import com.deplink.boruSmart.util.Wifi;
import com.deplink.boruSmart.view.combinationwidget.TitleLayout;

import deplink.com.smartwirelessrelay.homegenius.EllESDK.R;

public class ConnectApWifiActivity extends Activity implements View.OnClickListener{
    private TitleLayout layout_title;
    private Button button_next_step;
    private String wifiname;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_ap_wifi);
        initViews();
        initEvents();
        initDatas();
    }
    @Override
    public void finish() {
        super.finish();
        //注释掉activity本身的过渡动画
        overridePendingTransition(R.anim.in_left, R.anim.out_right);
    }
    private void initDatas() {
        Perfence.setPerfence(AppConstant.ADDDOORBELLTIPSACTIVITY,true);
    }
    private void initEvents() {
        layout_title.setReturnClickListener(new TitleLayout.ReturnImageClickListener() {
            @Override
            public void onBackPressed() {
                ConnectApWifiActivity.this.onBackPressed();
            }
        });
        button_next_step.setOnClickListener(this);
    }

    private void initViews() {
        layout_title = findViewById(R.id.layout_title);
        button_next_step = findViewById(R.id.button_next_step);
    }

    @Override
    protected void onResume() {
        super.onResume();
        wifiname= Wifi.getConnectedWifiName(this);
        if(wifiname.startsWith("SmartDoorBell")){
            //下一步
            button_next_step.setText("下一步");
        }else{
            button_next_step.setText("连接热点");
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.button_next_step:
                if(wifiname.startsWith("SmartDoorBell")){
                    //下一步
                    startActivity( new Intent(this,ConfigDoorBellWiFiActivity.class));
                }else{
                    Intent wifiSettingsIntent = new Intent("android.settings.WIFI_SETTINGS");
                    startActivity(wifiSettingsIntent);
                }

                break;
                default:
                    break;
        }
    }
}
