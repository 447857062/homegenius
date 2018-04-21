package com.deplink.boruSmart.activity.device.doorbell.add;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.deplink.boruSmart.manager.device.doorbeel.DoorbeelManager;
import com.deplink.boruSmart.util.Wifi;
import com.deplink.boruSmart.view.combinationwidget.TitleLayout;
import com.deplink.boruSmart.view.edittext.ClearEditText;
import com.deplink.boruSmart.view.toast.Ftoast;

import deplink.com.smartwirelessrelay.homegenius.EllESDK.R;

public class WifipasswordInputActivity extends Activity implements View.OnClickListener{
    private Button button_next_step;
    private TextView textview_wifi_name;
    private ClearEditText edittext_wifi_password;
    private DoorbeelManager mDoorbeelManager;
    private TitleLayout layout_title;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifipassword_input);
        initViews();
        initDatas();
        initEvents();

    }
    private void initEvents() {
        button_next_step.setOnClickListener(this);
    }
    private String  wifiName;
    private void initDatas() {
        layout_title.setReturnClickListener(new TitleLayout.ReturnImageClickListener() {
            @Override
            public void onBackPressed() {
                WifipasswordInputActivity.this.onBackPressed();
            }
        });
        mDoorbeelManager=DoorbeelManager.getInstance();
        wifiName= Wifi.getConnectedWifiName(this);
        if(!wifiName.equals("")){
            textview_wifi_name.setText(wifiName);
        }

    }

    private void initViews() {
        button_next_step = findViewById(R.id.button_next_step);
        textview_wifi_name = findViewById(R.id.textview_wifi_name);
        layout_title= findViewById(R.id.layout_title);
        edittext_wifi_password= findViewById(R.id.edittext_wifi_password);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.button_next_step:
                String wifiPassword = edittext_wifi_password.getText().toString();
                if(wifiPassword.length()<8){
                    Ftoast.create(WifipasswordInputActivity.this).setText("请输入正确的wifi密码").show();
                    return;
                }
                mDoorbeelManager.setSsid(wifiName);
                mDoorbeelManager.setPassword(wifiPassword);
                startActivity(new Intent(this,ApModeActivity.class));
                break;
        }
    }
}
