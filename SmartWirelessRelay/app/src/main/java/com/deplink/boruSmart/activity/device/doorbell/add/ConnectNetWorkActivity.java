package com.deplink.boruSmart.activity.device.doorbell.add;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import com.deplink.boruSmart.activity.device.AddDeviceNameActivity;
import com.deplink.boruSmart.activity.device.AddDeviceQRcodeActivity;
import com.deplink.boruSmart.constant.DeviceTypeConstant;
import com.deplink.boruSmart.util.NetUtil;
import com.deplink.boruSmart.util.Wifi;

import deplink.com.smartwirelessrelay.homegenius.EllESDK.R;

public class ConnectNetWorkActivity extends Activity implements View.OnClickListener{
    private Button button_next_step;
    private FrameLayout framelayout_back;
    private FrameLayout framelayout_x;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_net_work);
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
    @Override
    protected void onResume() {
        super.onResume();
        if(NetUtil.isNetAvailable(this)){
            button_next_step.setText("下一步");
        }else{
            button_next_step.setText("连接网络");
        }
    }

    private void initDatas() {

    }

    private void initEvents() {
        button_next_step.setOnClickListener(this);
        framelayout_back.setOnClickListener(this);
        framelayout_x.setOnClickListener(this);
    }

    private void initViews() {
        framelayout_back = findViewById(R.id.framelayout_back);
        framelayout_x = findViewById(R.id.framelayout_x);
        button_next_step = findViewById(R.id.button_next_step);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.button_next_step:
                if(NetUtil.isNetAvailable(this) && !Wifi.getConnectedWifiName(this).contains("SmartDoorBell")){
                    Intent intent = new Intent(ConnectNetWorkActivity.this, AddDeviceNameActivity.class);
                    intent.putExtra("DeviceType", DeviceTypeConstant.TYPE.TYPE_MENLING);
                    intent.putExtra("showTips", true);
                    startActivity(intent);
                }else{
                    Intent wifiSettingsIntent = new Intent("android.settings.WIFI_SETTINGS");
                    startActivity(wifiSettingsIntent);
                }
                break;
            case R.id.framelayout_back:
                ConnectNetWorkActivity.this.onBackPressed();
                break;
            case R.id.framelayout_x:
                Intent intent=new Intent(ConnectNetWorkActivity.this,AddDeviceQRcodeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                break;
        }
    }
}
