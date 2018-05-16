package com.deplink.boruSmart.activity.device.doorbell.add;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.deplink.boruSmart.view.combinationwidget.TitleLayout;

import deplink.com.smartwirelessrelay.homegenius.EllESDK.R;

public class DoorBellConfigWifiTypeSelect extends Activity implements View.OnClickListener{
    private TitleLayout layout_title;
    private ImageView imageview_doorbell_configwifi;
    private ImageView imageview_doorbell_add_configedwifi;
    private ImageView imageview_instrution;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_door_bell_config_wifi_type_select);
        initViews();
        initEvents();
        initDatas();
    }

    private void initDatas() {

    }

    private void initEvents() {
        layout_title.setReturnClickListener(new TitleLayout.ReturnImageClickListener() {
            @Override
            public void onBackPressed() {
                DoorBellConfigWifiTypeSelect.this.onBackPressed();
            }
        });
        imageview_doorbell_configwifi.setOnClickListener(this);
        imageview_doorbell_add_configedwifi.setOnClickListener(this);
        imageview_instrution.setOnClickListener(this);
    }

    private void initViews() {
        layout_title = findViewById(R.id.layout_title);
        imageview_doorbell_configwifi = findViewById(R.id.imageview_doorbell_configwifi);
        imageview_doorbell_add_configedwifi = findViewById(R.id.imageview_doorbell_add_configedwifi);
        imageview_instrution = findViewById(R.id.imageview_instrution);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.imageview_doorbell_configwifi:
                startActivity(new Intent(this,ApModeActivity.class));
                break;
            case R.id.imageview_doorbell_add_configedwifi:
                Intent intent = new Intent();
                intent.setClass(DoorBellConfigWifiTypeSelect.this, AutoFindActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                break;
            case R.id.imageview_instrution:
                startActivity(new Intent(this,PairingGuideOneActivity.class));
                break;
           default:
                break;

        }
    }

}
