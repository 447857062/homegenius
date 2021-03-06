package com.deplink.boruSmart.activity.device.smartSwitch.add;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;

import com.deplink.boruSmart.activity.device.smartSwitch.adapter.SwitchTypeAdapter;
import com.deplink.boruSmart.constant.DeviceTypeConstant;
import com.deplink.boruSmart.manager.device.smartswitch.SmartSwitchManager;
import com.deplink.boruSmart.util.qrcode.qrcodecapture.CaptureActivity;
import com.deplink.boruSmart.view.scrollview.NonScrollableListView;

import java.util.ArrayList;
import java.util.List;

import deplink.com.smartwirelessrelay.homegenius.EllESDK.R;

public class SelectSwitchTypeActivity extends Activity implements View.OnClickListener,AdapterView.OnItemClickListener{
    private ImageView image_back;
    private NonScrollableListView listview_switch_type;
    private SwitchTypeAdapter mTypeAdapter;
    private List<String>typeNames;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_switch_type);
        initViews();
        initDatas();
        initEvents();
    }

    private void initDatas() {
        typeNames=new ArrayList<>();
        //typeNames.add(DeviceTypeConstant.TYPE_SWITCH_SUBTYPE.SUB_TYPE_SWITCH_ONEWAY);
        typeNames.add(DeviceTypeConstant.TYPE_SWITCH_SUBTYPE.SUB_TYPE_SWITCH_TWOWAY);
        typeNames.add(DeviceTypeConstant.TYPE_SWITCH_SUBTYPE.SUB_TYPE_SWITCH_THREEWAY);
        typeNames.add(DeviceTypeConstant.TYPE_SWITCH_SUBTYPE.SUB_TYPE_SWITCH_FOURWAY);
        mTypeAdapter=new SwitchTypeAdapter(this,typeNames);
    }

    private void initEvents() {
        image_back.setOnClickListener(this);
        listview_switch_type.setAdapter(mTypeAdapter);
        listview_switch_type.setOnItemClickListener(this);
    }

    private void initViews() {
        image_back= findViewById(R.id.image_back);
        listview_switch_type= findViewById(R.id.listview_switch_type);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.image_back:
                onBackPressed();
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        SmartSwitchManager.getInstance().setCurrentAddSwitchSubType(typeNames.get(position));
        switch (typeNames.get(position)){
            case DeviceTypeConstant.TYPE_SWITCH_SUBTYPE.SUB_TYPE_SWITCH_ONEWAY:
            case DeviceTypeConstant.TYPE_SWITCH_SUBTYPE.SUB_TYPE_SWITCH_TWOWAY:
            case DeviceTypeConstant.TYPE_SWITCH_SUBTYPE.SUB_TYPE_SWITCH_THREEWAY:
            case DeviceTypeConstant.TYPE_SWITCH_SUBTYPE.SUB_TYPE_SWITCH_FOURWAY:
                Intent intentQrcodeSn = new Intent();
                intentQrcodeSn.setClass(SelectSwitchTypeActivity.this, CaptureActivity.class);
                intentQrcodeSn.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intentQrcodeSn.putExtra("requestType",CaptureActivity.CAPTURE_TYPE_SWITCH);
                startActivity(intentQrcodeSn);
                break;
        }
    }
}
