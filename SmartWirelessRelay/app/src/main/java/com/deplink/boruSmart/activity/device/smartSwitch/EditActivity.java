package com.deplink.boruSmart.activity.device.smartSwitch;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.deplink.boruSmart.Protocol.json.Room;
import com.deplink.boruSmart.Protocol.json.device.getway.GatwayDevice;
import com.deplink.boruSmart.activity.device.DevicesActivity;
import com.deplink.boruSmart.activity.device.SelectRommActivity;
import com.deplink.boruSmart.activity.device.ShareDeviceActivity;
import com.deplink.boruSmart.activity.device.adapter.GetwaySelectListAdapter;
import com.deplink.boruSmart.activity.personal.experienceCenter.ExperienceDevicesActivity;
import com.deplink.boruSmart.activity.personal.login.LoginActivity;
import com.deplink.boruSmart.constant.AppConstant;
import com.deplink.boruSmart.constant.DeviceTypeConstant;
import com.deplink.boruSmart.manager.device.DeviceListener;
import com.deplink.boruSmart.manager.device.DeviceManager;
import com.deplink.boruSmart.manager.device.getway.GetwayManager;
import com.deplink.boruSmart.manager.device.smartswitch.SmartSwitchManager;
import com.deplink.boruSmart.manager.room.RoomManager;
import com.deplink.boruSmart.util.NetUtil;
import com.deplink.boruSmart.util.Perfence;
import com.deplink.boruSmart.view.combinationwidget.TitleLayout;
import com.deplink.boruSmart.view.dialog.AlertDialog;
import com.deplink.boruSmart.view.dialog.loadingdialog.DialogThreeBounce;
import com.deplink.boruSmart.view.edittext.ClearEditText;
import com.deplink.boruSmart.view.toast.Ftoast;
import com.deplink.sdk.android.sdk.DeplinkSDK;
import com.deplink.sdk.android.sdk.EventCallback;
import com.deplink.sdk.android.sdk.SDKAction;
import com.deplink.sdk.android.sdk.homegenius.DeviceOperationResponse;
import com.deplink.sdk.android.sdk.manager.SDKManager;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

import deplink.com.smartwirelessrelay.homegenius.EllESDK.R;

public class EditActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "EditDoorbeelActivity";
    private static final int REQUEST_CODE_SELECT_DEVICE_IN_WHAT_ROOM = 100;
    private TextView button_delete_device;
    private SmartSwitchManager mSmartSwitchManager;
    private DeviceManager mDeviceManager;
    private RelativeLayout layout_select_room;
    private TextView textview_select_room_name;
    private ClearEditText edittext_add_device_input_name;
    private RelativeLayout layout_getway_select;
    private RelativeLayout layout_getway_list;
    private ListView listview_select_getway;
    private GetwaySelectListAdapter selectGetwayAdapter;
    private List<GatwayDevice> mGetways;
    private TextView textview_select_getway_name;
    private ImageView imageview_getway_arror_right;
    private String switchType;
    private String selectGetwayName;
    private boolean isStartFromExperience;
    private String deviceName;
    private boolean isOnActivityResult;
    private String action;
    private String deviceUid;
    private Room room;
    private DeviceListener mDeviceListener;
    private boolean isLogin;
    private SDKManager manager;
    private EventCallback ec;
    private GatwayDevice selectedGatway;
    private RelativeLayout layout_device_share;
    private TitleLayout layout_title;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        initViews();
        initDatas();
        initEvents();
    }

    private void initEvents() {
        button_delete_device.setOnClickListener(this);
        layout_select_room.setOnClickListener(this);
        layout_getway_select.setOnClickListener(this);
        layout_device_share.setOnClickListener(this);
    }

    private void initDatas() {
        switchType = getIntent().getStringExtra("switchType");
        Log.i(TAG, "initDatas switchType=" + switchType);
        layout_title.setTitleText(switchType);
        layout_title.setReturnClickListener(new TitleLayout.ReturnImageClickListener() {
            @Override
            public void onBackPressed() {
                EditActivity.this.onBackPressed();
            }
        });
        layout_title.setEditTextClickListener(new TitleLayout.EditTextClickListener() {
            @Override
            public void onEditTextPressed() {
                if (isStartFromExperience) {
                    onBackPressed();
                } else {
                    action = "alertname";
                    deviceUid = mSmartSwitchManager.getCurrentSelectSmartDevice().getUid();
                    deviceName = edittext_add_device_input_name.getText().toString();
                    if (isLogin) {
                        mDeviceManager.alertDeviceHttp(deviceUid, null, deviceName, null);
                    } else {
                        Ftoast.create(EditActivity.this).setText("未获取到开锁记录").show();
                    }
                }
            }
        });
        mSmartSwitchManager = SmartSwitchManager.getInstance();
        mDeviceManager = DeviceManager.getInstance();
        mDeviceManager.InitDeviceManager(this);
        mGetways = new ArrayList<>();
        mGetways.addAll(GetwayManager.getInstance().getAllGetwayDevice());
        selectGetwayAdapter = new GetwaySelectListAdapter(this, mGetways);
        listview_select_getway.setAdapter(selectGetwayAdapter);
        listview_select_getway.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectGetwayName = mGetways.get(position).getName();
                textview_select_getway_name.setText(selectGetwayName);
                layout_getway_list.setVisibility(View.GONE);
                action = "alertgetway";
                selectedGatway = mGetways.get(position);
                if(!isStartFromExperience){
                    deviceUid = mSmartSwitchManager.getCurrentSelectSmartDevice().getUid();
                    mDeviceManager.alertDeviceHttp(deviceUid, null, null, selectedGatway.getUid());
                }
            }
        });
        DeplinkSDK.initSDK(getApplicationContext(), Perfence.SDK_APP_KEY);
        manager = DeplinkSDK.getSDKManager();
        ec = new EventCallback() {
            @Override
            public void onSuccess(SDKAction action) {

            }

            @Override
            public void onBindSuccess(SDKAction action, String devicekey) {
            }


            @Override
            public void deviceOpSuccess(String op, String deviceKey) {
                super.deviceOpSuccess(op, deviceKey);
            }

            @Override
            public void onFailure(SDKAction action, Throwable throwable) {

            }

            @Override
            public void connectionLost(Throwable throwable) {
                super.connectionLost(throwable);
                isLogin = false;
                Perfence.setPerfence(AppConstant.USER_LOGIN, false);
                new AlertDialog(EditActivity.this).builder().setTitle("账号异地登录")
                        .setMsg("当前账号已在其它设备上登录,是否重新登录")
                        .setPositiveButton("确认", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                startActivity(new Intent(EditActivity.this, LoginActivity.class));
                            }
                        }).setNegativeButton("取消", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                }).show();
            }
        };
        mDeviceListener = new DeviceListener() {
            @Override
            public void responseDeleteDeviceHttpResult(DeviceOperationResponse result) {
                super.responseDeleteDeviceHttpResult(result);
                if (result.getStatus() != null && result.getStatus().equals("ok")) {
                    mDeviceManager.deleteSmartDevice();
                    DialogThreeBounce.hideLoading();
                    int deleteResult = mSmartSwitchManager.deleteDBSmartDevice(mSmartSwitchManager.getCurrentSelectSmartDevice().getUid());
                    if (deleteResult > 0) {
                        startActivity(new Intent(EditActivity.this, DevicesActivity.class));
                    } else {
                        Ftoast.create(EditActivity.this).setText("删除开关设备失败").show();
                    }
                } else {
                    Ftoast.create(EditActivity.this).setText("删除开关设备失败").show();
                }

            }

            @Override
            public void responseAlertDeviceHttpResult(DeviceOperationResponse result) {
                super.responseAlertDeviceHttpResult(result);
                deviceUid = mSmartSwitchManager.getCurrentSelectSmartDevice().getUid();
                switch (action) {
                    case "alertroom":
                        mSmartSwitchManager.updateSmartDeviceInWhatRoom(room, deviceUid, deviceName);
                        break;
                    case "alertname":
                        boolean saveResult = mSmartSwitchManager.updateSmartDeviceName(deviceUid, deviceName);
                        Log.i(TAG,"修改开关名称:"+deviceName+"修改结果:"+saveResult);
                        if (saveResult) {
                            onBackPressed();
                        }
                        break;
                    case "alertgetway":
                        boolean saveDbResult = mSmartSwitchManager.updateSmartDeviceGetway(selectedGatway);
                        if (!saveDbResult) {
                            Toast.makeText(EditActivity.this, "更新智能设备所属网关失败", Toast.LENGTH_SHORT).show();
                        }
                        break;
                }
                action = "";
            }
        };
        if (getIntent().getBooleanExtra("isupdateroom", false)) {
            String roomName = getIntent().getStringExtra("roomName");
            isOnActivityResult = true;
            if (!isStartFromExperience) {
                action = "alertroom";
                room = RoomManager.getInstance().findRoom(roomName, true);
                deviceUid = mSmartSwitchManager.getCurrentSelectSmartDevice().getUid();
                deviceName = edittext_add_device_input_name.getText().toString();
                mDeviceManager.alertDeviceHttp(deviceUid, room.getUid(), null, null);
            }
            textview_select_room_name.setText(roomName);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        isLogin = Perfence.getBooleanPerfence(AppConstant.USER_LOGIN);
        isStartFromExperience = mDeviceManager.isStartFromExperience();
        if (isStartFromExperience) {
            deviceName = "智能开关";
        } else {
            deviceName = mSmartSwitchManager.getCurrentSelectSmartDevice().getName();
        }
        if (deviceName != null && deviceName.length() > 0) {
            if (deviceName.length() > 10) {
                deviceName = deviceName.substring(0, 10);
            }
            edittext_add_device_input_name.setText(deviceName);
            edittext_add_device_input_name.setSelection(deviceName.length());
        }
        if (!isOnActivityResult) {
            isOnActivityResult = false;
            if (isStartFromExperience) {
                textview_select_room_name.setText("未选择");
            } else {
                if (mSmartSwitchManager.getCurrentSelectSmartDevice().getRooms().size() == 1) {
                    textview_select_room_name.setText(mSmartSwitchManager.getCurrentSelectSmartDevice().getRooms().get(0).getRoomName());
                } else {
                    textview_select_room_name.setText("未选择");
                }
            }
            if( mSmartSwitchManager.getCurrentSelectSmartDevice()!=null){
                deviceUid=mSmartSwitchManager.getCurrentSelectSmartDevice().getUid();
                GatwayDevice temp = mSmartSwitchManager.getCurrentSelectSmartDevice().getGetwayDevice();
                if (temp == null) {
                    if(mSmartSwitchManager.getCurrentSelectSmartDevice().getGetwayDeviceUid()!=null){
                        GatwayDevice localDbGatwayDevice =
                                DataSupport.where("uid=?", mSmartSwitchManager.getCurrentSelectSmartDevice().getGetwayDeviceUid())
                                        .findFirst(GatwayDevice.class);
                        if (localDbGatwayDevice != null) {
                            textview_select_getway_name.setText(localDbGatwayDevice.getName());
                        } else {
                            textview_select_getway_name.setText("未设置网关");
                        }
                    }


                } else {
                    textview_select_getway_name.setText(mSmartSwitchManager.getCurrentSelectSmartDevice().getGetwayDevice().getName());
                }
            }
        }
        mDeviceManager.addDeviceListener(mDeviceListener);
        manager.addEventCallback(ec);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mDeviceManager.removeDeviceListener(mDeviceListener);
        manager.removeEventCallback(ec);
    }

    private void initViews() {
        button_delete_device = (TextView) findViewById(R.id.button_delete_device);
        layout_select_room = (RelativeLayout) findViewById(R.id.layout_room_select);
        textview_select_room_name = (TextView) findViewById(R.id.textview_select_room_name);
        edittext_add_device_input_name = (ClearEditText) findViewById(R.id.edittext_add_device_input_name);
        layout_getway_list = (RelativeLayout) findViewById(R.id.layout_getway_list);
        layout_getway_select = (RelativeLayout) findViewById(R.id.layout_getway_select);
        listview_select_getway = (ListView) findViewById(R.id.listview_select_getway);
        textview_select_getway_name = (TextView) findViewById(R.id.textview_select_getway_name);
        imageview_getway_arror_right = (ImageView) findViewById(R.id.imageview_getway_arror_right);
        layout_device_share = (RelativeLayout) findViewById(R.id.layout_device_share);
        layout_title= (TitleLayout) findViewById(R.id.layout_title);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SELECT_DEVICE_IN_WHAT_ROOM && resultCode == RESULT_OK) {
            String roomName = data.getStringExtra("roomName");
            isOnActivityResult = true;
            if (!isStartFromExperience) {
                action = "alertroom";
                room = RoomManager.getInstance().findRoom(roomName, true);
                deviceUid = mSmartSwitchManager.getCurrentSelectSmartDevice().getUid();
                deviceName = edittext_add_device_input_name.getText().toString();
                mDeviceManager.alertDeviceHttp(deviceUid, room.getUid(), null, null);
            }
            textview_select_room_name.setText(roomName);
        }
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.layout_device_share:
                Intent inentShareDevice = new Intent(this, ShareDeviceActivity.class);
                inentShareDevice.putExtra("devicetype", DeviceTypeConstant.TYPE.TYPE_SWITCH);
                if(isStartFromExperience){
                    startActivity(inentShareDevice);
                }else{
                    if(isLogin){
                        if (deviceUid != null) {
                            inentShareDevice.putExtra("deviceuid", deviceUid);
                            startActivity(inentShareDevice);
                        }
                    }else{
                        startActivity(new Intent(EditActivity.this, LoginActivity.class));
                    }

                }

                break;

            case R.id.layout_getway_select:
                if (layout_getway_list.getVisibility() == View.VISIBLE) {
                    layout_getway_list.setVisibility(View.GONE);
                    imageview_getway_arror_right.setImageResource(R.drawable.gotoicon);
                } else {
                    layout_getway_list.setVisibility(View.VISIBLE);
                    imageview_getway_arror_right.setImageResource(R.drawable.nextdirectionicon);
                }
                break;
            case R.id.layout_room_select:
                if(isStartFromExperience){
                    mDeviceManager.setEditDevice(true);
                    mDeviceManager.setCurrentEditDeviceType(DeviceTypeConstant.TYPE.TYPE_SWITCH);
                    Intent intent = new Intent(this, SelectRommActivity.class);
                    startActivity(intent);
                }else{
                    if(isLogin){
                        mDeviceManager.setEditDevice(true);
                        mDeviceManager.setCurrentEditDeviceType(DeviceTypeConstant.TYPE.TYPE_SWITCH);
                        Intent intent = new Intent(this, SelectRommActivity.class);
                        startActivity(intent);
                    }else{
                        startActivity(new Intent(EditActivity.this, LoginActivity.class));
                    }
                }

                break;
            case R.id.button_delete_device:
                new AlertDialog(EditActivity.this).builder().setTitle("删除设备")
                        .setMsg("确认删除设备")
                        .setPositiveButton("确认", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if(isStartFromExperience){
                                    startActivity(new Intent(EditActivity.this, ExperienceDevicesActivity.class));
                                }else{
                                    if(NetUtil.isNetAvailable(EditActivity.this)){
                                        if (isLogin) {
                                            mDeviceManager.deleteDeviceHttp();
                                        } else {
                                            Ftoast.create(EditActivity.this).setText("未登录,登录后操作").show();
                                        }
                                    }else{
                                        Ftoast.create(EditActivity.this).setText("网络连接不可用").show();
                                    }
                                }
                            }
                        }).setNegativeButton("取消", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                }).show();
                break;

        }
    }
}
