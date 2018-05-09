package com.deplink.boruSmart.activity.device.doorbell.add;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import com.deplink.boruSmart.activity.device.AddDeviceQRcodeActivity;
import com.deplink.boruSmart.view.dialog.AlertDialog;

import deplink.com.smartwirelessrelay.homegenius.EllESDK.R;

public class ConfigWifiJugeActivity extends Activity implements View.OnClickListener{
    private Button button_reconfig_wifi;
    private Button button_next_step;
    private FrameLayout framelayout_back;
    private FrameLayout framelayout_x;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config_wifi_juge);
        initViews();
        initEvents();
        initDatas();
    }
    private void initDatas() {

    }
    private void initEvents() {
        button_reconfig_wifi.setOnClickListener(this);
        button_next_step.setOnClickListener(this);
        framelayout_back.setOnClickListener(this);
        framelayout_x.setOnClickListener(this);
    }

    private void initViews() {
        button_reconfig_wifi = findViewById(R.id.button_reconfig_wifi);
        button_next_step = findViewById(R.id.button_next_step);
        framelayout_back = findViewById(R.id.framelayout_back);
        framelayout_x = findViewById(R.id.framelayout_x);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.button_reconfig_wifi:
                startActivity(new Intent(ConfigWifiJugeActivity.this,ApModeActivity.class));
                break;
            case R.id.button_next_step:
                startActivity(new Intent(ConfigWifiJugeActivity.this,ConnectNetWorkActivity.class));
                break;
            case R.id.framelayout_back:
                AlertDialog alertDialog= new AlertDialog(this).builder().setTitle("退出添加设备")
                        .setMsg("当前门铃wifi配置信息已发送,是否开启AP模式重新添加设备,或者继续完成添加")
                        .setPositiveButton("确认", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent=new Intent(ConfigWifiJugeActivity.this,AddDeviceQRcodeActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        }).setNegativeButton("取消", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                            }
                        });
                alertDialog.show();
                break;
            case R.id.framelayout_x:
                Intent intent=new Intent(ConfigWifiJugeActivity.this,AddDeviceQRcodeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                break;
                default:
                    break;
        }
    }
}
