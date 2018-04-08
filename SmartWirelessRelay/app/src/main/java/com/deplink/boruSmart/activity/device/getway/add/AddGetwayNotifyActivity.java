package com.deplink.boruSmart.activity.device.getway.add;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.deplink.boruSmart.util.qrcode.qrcodecapture.CaptureActivity;
import com.deplink.boruSmart.view.combinationwidget.TitleLayout;

import deplink.com.smartwirelessrelay.homegenius.EllESDK.R;

public class AddGetwayNotifyActivity extends Activity implements View.OnClickListener{
    private Button button_next_step;
    private TitleLayout layout_title;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_getway_notify);
        initViews();
        initDatas();
        initEvents();
    }

    private void initEvents() {
        button_next_step.setOnClickListener(this);
    }

    private void initViews() {
        button_next_step= (Button) findViewById(R.id.button_next_step);
        layout_title= (TitleLayout) findViewById(R.id.layout_title);
    }

    private void initDatas() {
        layout_title.setReturnClickListener(new TitleLayout.ReturnImageClickListener() {
            @Override
            public void onBackPressed() {
                AddGetwayNotifyActivity.this.onBackPressed();
            }
        });
    }
    public final static int REQUEST_CODE_GETWAY = 3;
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.button_next_step:
                Intent intentQrcodeSn = new Intent();
                intentQrcodeSn.setClass(this, CaptureActivity.class);
                intentQrcodeSn.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intentQrcodeSn.putExtra("requestType", REQUEST_CODE_GETWAY);
                startActivity(intentQrcodeSn);
                break;
        }
    }
}
