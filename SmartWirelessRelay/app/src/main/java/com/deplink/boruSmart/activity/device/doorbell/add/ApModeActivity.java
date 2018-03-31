package com.deplink.boruSmart.activity.device.doorbell.add;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.deplink.boruSmart.view.combinationwidget.TitleLayout;

import deplink.com.smartwirelessrelay.homegenius.EllESDK.R;

public class ApModeActivity extends Activity implements View.OnClickListener{
    private Button button_next_step;
    private TitleLayout layout_title;
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
    }

    private void initViews() {
        button_next_step = findViewById(R.id.button_next_step);
        layout_title= findViewById(R.id.layout_title);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){

            case R.id.button_next_step:
                startActivity(new Intent(this,AddDoorbellTipsActivity.class));
                break;
        }
    }
}
