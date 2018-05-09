package com.deplink.boruSmart.activity.device.doorbell.add;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.deplink.boruSmart.view.combinationwidget.TitleLayout;

import deplink.com.smartwirelessrelay.homegenius.EllESDK.R;

public class PairingGuideOneActivity extends Activity implements View.OnClickListener{
    private TitleLayout layout_title;
    private Button button_next_step;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pairing_guide_one);
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
                PairingGuideOneActivity.this.onBackPressed();
            }
        });
        button_next_step.setOnClickListener(this);
    }

    private void initViews() {
        layout_title = (TitleLayout) findViewById(R.id.layout_title);
        button_next_step = (Button) findViewById(R.id.button_next_step);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.button_next_step:
                startActivity(new Intent(this,PairGuideTwoActivity.class));
                break;
                default:
                    break;
        }
    }
}
