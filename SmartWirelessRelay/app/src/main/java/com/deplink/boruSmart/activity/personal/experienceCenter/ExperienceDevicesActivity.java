package com.deplink.boruSmart.activity.personal.experienceCenter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.deplink.boruSmart.Protocol.json.device.ExperienceCenterDevice;
import com.deplink.boruSmart.activity.device.DevicesActivity;
import com.deplink.boruSmart.activity.device.doorbell.DoorbeelMainActivity;
import com.deplink.boruSmart.activity.device.getway.GetwayDeviceActivity;
import com.deplink.boruSmart.activity.device.light.LightActivity;
import com.deplink.boruSmart.activity.device.remoteControl.airContorl.AirRemoteControlMianActivity;
import com.deplink.boruSmart.activity.device.remoteControl.realRemoteControl.RemoteControlActivity;
import com.deplink.boruSmart.activity.device.remoteControl.topBox.TvBoxMainActivity;
import com.deplink.boruSmart.activity.device.remoteControl.tv.TvMainActivity;
import com.deplink.boruSmart.activity.device.router.RouterMainActivity;
import com.deplink.boruSmart.activity.device.smartSwitch.SwitchFourActivity;
import com.deplink.boruSmart.activity.device.smartSwitch.SwitchOneActivity;
import com.deplink.boruSmart.activity.device.smartSwitch.SwitchThreeActivity;
import com.deplink.boruSmart.activity.device.smartSwitch.SwitchTwoActivity;
import com.deplink.boruSmart.activity.device.smartlock.SmartLockActivity;
import com.deplink.boruSmart.activity.homepage.SmartHomeMainActivity;
import com.deplink.boruSmart.activity.homepage.adapter.ExperienceCenterListAdapter;
import com.deplink.boruSmart.activity.personal.PersonalCenterActivity;
import com.deplink.boruSmart.constant.DeviceTypeConstant;
import com.deplink.boruSmart.manager.device.DeviceManager;

import java.util.ArrayList;
import java.util.List;

import deplink.com.smartwirelessrelay.homegenius.EllESDK.R;

public class ExperienceDevicesActivity extends Activity implements AdapterView.OnItemClickListener, View.OnClickListener {
    private static final String TAG = "EDActivity";
    private ListView listview_experience_center;
    private List<ExperienceCenterDevice> mExperienceCenterDevices;
    private ExperienceCenterListAdapter mAdapter;
    private ImageView imageview_back;
    private TextView textview_title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_experience_devices);
        initViews();
        initDatas();
        initEvents();
    }
    private void initEvents() {
        listview_experience_center.setAdapter(mAdapter);
        listview_experience_center.setOnItemClickListener(this);
        imageview_back.setOnClickListener(this);
    }

    private void initDatas() {
        textview_title.setText("体验中心");
        mExperienceCenterDevices = new ArrayList<>();
        ExperienceCenterDevice device = new ExperienceCenterDevice();
        device.setDeviceName(DeviceTypeConstant.TYPE.TYPE_SMART_GETWAY);
        device.setOnline(true);
        mExperienceCenterDevices.add(device);
        device = new ExperienceCenterDevice();
        device.setDeviceName(DeviceTypeConstant.TYPE.TYPE_ROUTER);
        device.setOnline(true);
        mExperienceCenterDevices.add(device);
        device = new ExperienceCenterDevice();
        device.setDeviceName(DeviceTypeConstant.TYPE.TYPE_LOCK);
        device.setOnline(true);
        mExperienceCenterDevices.add(device);
        device = new ExperienceCenterDevice();
        device.setDeviceName(DeviceTypeConstant.TYPE.TYPE_MENLING);
        device.setOnline(true);
        mExperienceCenterDevices.add(device);
        device = new ExperienceCenterDevice();
        device.setDeviceName(DeviceTypeConstant.TYPE.TYPE_REMOTECONTROL);
        device.setOnline(true);
        mExperienceCenterDevices.add(device);
        device = new ExperienceCenterDevice();
        device.setDeviceName(DeviceTypeConstant.TYPE.TYPE_TV_REMOTECONTROL);
        device.setOnline(true);
        mExperienceCenterDevices.add(device);
        device = new ExperienceCenterDevice();
        device.setDeviceName(DeviceTypeConstant.TYPE.TYPE_AIR_REMOTECONTROL);
        device.setOnline(true);
        mExperienceCenterDevices.add(device);
        device = new ExperienceCenterDevice();
        device.setDeviceName(DeviceTypeConstant.TYPE.TYPE_TVBOX_REMOTECONTROL);
        device.setOnline(true);
        mExperienceCenterDevices.add(device);
        device = new ExperienceCenterDevice();
        device.setDeviceName(DeviceTypeConstant.TYPE.TYPE_LIGHT);
        device.setOnline(true);
        mExperienceCenterDevices.add(device);
       /* device = new ExperienceCenterDevice();
        device.setDeviceName(DeviceTypeConstant.TYPE.TYPE_SWITCH);
        device.setSubtype(DeviceTypeConstant.TYPE_SWITCH_SUBTYPE.SUB_TYPE_SWITCH_ONEWAY);
        device.setOnline(true);
        mExperienceCenterDevices.add(device);*/
        device = new ExperienceCenterDevice();
        device.setDeviceName(DeviceTypeConstant.TYPE_SWITCH_SUBTYPE.SUB_TYPE_SWITCH_TWOWAY);
        device.setSubtype(DeviceTypeConstant.TYPE_SWITCH_SUBTYPE.SUB_TYPE_SWITCH_TWOWAY);
        device.setOnline(true);
        mExperienceCenterDevices.add(device);
        device = new ExperienceCenterDevice();
        device.setDeviceName(DeviceTypeConstant.TYPE_SWITCH_SUBTYPE.SUB_TYPE_SWITCH_THREEWAY);
        device.setSubtype(DeviceTypeConstant.TYPE_SWITCH_SUBTYPE.SUB_TYPE_SWITCH_THREEWAY);
        device.setOnline(true);
        mExperienceCenterDevices.add(device);
        device = new ExperienceCenterDevice();
        device.setDeviceName(DeviceTypeConstant.TYPE_SWITCH_SUBTYPE.SUB_TYPE_SWITCH_FOURWAY);
        device.setSubtype(DeviceTypeConstant.TYPE_SWITCH_SUBTYPE.SUB_TYPE_SWITCH_FOURWAY);
        device.setOnline(true);
        mExperienceCenterDevices.add(device);
        mAdapter = new ExperienceCenterListAdapter(this, mExperienceCenterDevices);

    }

    private void initViews() {
        listview_experience_center = findViewById(R.id.listview_experience_center);
        imageview_back = findViewById(R.id.image_back);
        textview_title = findViewById(R.id.textview_title);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent;
        DeviceManager.getInstance().setStartFromExperience(true);
        DeviceManager.getInstance().setStartFromHomePage(false);
        switch (mExperienceCenterDevices.get(position).getDeviceName()) {
            case DeviceTypeConstant.TYPE.TYPE_SMART_GETWAY:
                Intent intentGetwayDevice = new Intent(ExperienceDevicesActivity.this, GetwayDeviceActivity.class);
                startActivity(intentGetwayDevice);
                break;
            case DeviceTypeConstant.TYPE.TYPE_LOCK:
                intent = new Intent(this, SmartLockActivity.class);
                startActivity(intent);
                break;
            case DeviceTypeConstant.TYPE.TYPE_ROUTER:
                intent = new Intent(this, RouterMainActivity.class);
                startActivity(intent);
                break;
            case DeviceTypeConstant.TYPE.TYPE_MENLING:
                intent = new Intent(this, DoorbeelMainActivity.class);
                startActivity(intent);
                break;
            case DeviceTypeConstant.TYPE_SWITCH_SUBTYPE.SUB_TYPE_SWITCH_ONEWAY:
                intent = new Intent(this, SwitchOneActivity.class);
                startActivity(intent);
                break;
            case DeviceTypeConstant.TYPE_SWITCH_SUBTYPE.SUB_TYPE_SWITCH_TWOWAY:
                intent = new Intent(this, SwitchTwoActivity.class);
                startActivity(intent);
                break;
            case DeviceTypeConstant.TYPE_SWITCH_SUBTYPE.SUB_TYPE_SWITCH_THREEWAY:
                intent = new Intent(this, SwitchThreeActivity.class);
                startActivity(intent);
                break;
            case DeviceTypeConstant.TYPE_SWITCH_SUBTYPE.SUB_TYPE_SWITCH_FOURWAY:
                intent = new Intent(this, SwitchFourActivity.class);
                startActivity(intent);
                break;
            case DeviceTypeConstant.TYPE.TYPE_REMOTECONTROL:
                intent = new Intent(this, RemoteControlActivity.class);
                startActivity(intent);
                break;
            case DeviceTypeConstant.TYPE.TYPE_TV_REMOTECONTROL:
                intent = new Intent(this, TvMainActivity.class);
                startActivity(intent);
                break;
            case DeviceTypeConstant.TYPE.TYPE_AIR_REMOTECONTROL:
                intent = new Intent(this, AirRemoteControlMianActivity.class);
                startActivity(intent);
                break;
            case DeviceTypeConstant.TYPE.TYPE_TVBOX_REMOTECONTROL:
                intent = new Intent(this, TvBoxMainActivity.class);
                startActivity(intent);
                break;
            case DeviceTypeConstant.TYPE.TYPE_LIGHT:
                intent = new Intent(this, LightActivity.class);
                startActivity(intent);
                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        DeviceManager.getInstance().setExperCenterStartFromDevice(false);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.image_back:
                if(DeviceManager.getInstance().isExperCenterStartFromHomePage()){
                    Intent intent=new Intent(this, SmartHomeMainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }else if(DeviceManager.getInstance().isExperCenterStartFromDevice()){
                    Intent intent=new Intent(this, DevicesActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
                else{
                    Intent intent=new Intent(this, PersonalCenterActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
                break;
        }
    }
}
