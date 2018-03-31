package com.deplink.boruSmart.activity.device.getway;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.deplink.boruSmart.activity.device.ShareDeviceActivity;
import com.deplink.boruSmart.activity.homepage.SmartHomeMainActivity;
import com.deplink.boruSmart.activity.personal.experienceCenter.ExperienceDevicesActivity;
import com.deplink.boruSmart.activity.personal.login.LoginActivity;
import com.deplink.boruSmart.constant.AppConstant;
import com.deplink.boruSmart.constant.DeviceTypeConstant;
import com.deplink.boruSmart.manager.device.getway.GetwayListener;
import com.deplink.boruSmart.manager.room.RoomManager;
import com.deplink.boruSmart.util.NetUtil;
import com.deplink.boruSmart.util.Perfence;
import com.deplink.boruSmart.util.WeakRefHandler;
import com.deplink.boruSmart.view.toast.ToastSingleShow;
import com.deplink.boruSmart.Protocol.json.Room;
import com.deplink.boruSmart.Protocol.json.device.DeviceList;
import com.deplink.boruSmart.activity.device.AddDeviceActivity;
import com.deplink.boruSmart.activity.device.DevicesActivity;
import com.deplink.boruSmart.activity.device.getway.wifi.ScanWifiListActivity;
import com.deplink.boruSmart.manager.connect.local.tcp.LocalConnectmanager;
import com.deplink.boruSmart.manager.device.DeviceListener;
import com.deplink.boruSmart.manager.device.DeviceManager;
import com.deplink.boruSmart.manager.device.getway.GetwayManager;
import com.deplink.boruSmart.view.combinationwidget.TitleLayout;
import com.deplink.boruSmart.view.dialog.AlertDialog;
import com.deplink.boruSmart.view.edittext.ClearEditText;
import com.deplink.sdk.android.sdk.DeplinkSDK;
import com.deplink.sdk.android.sdk.EventCallback;
import com.deplink.sdk.android.sdk.SDKAction;
import com.deplink.sdk.android.sdk.homegenius.DeviceOperationResponse;
import com.deplink.sdk.android.sdk.manager.SDKManager;
import com.google.gson.Gson;

import java.util.List;

import deplink.com.smartwirelessrelay.homegenius.EllESDK.R;

public class GetwayDeviceActivity extends Activity implements View.OnClickListener, GetwayListener {
    private static final String TAG = "GetwayDeviceActivity";
    private TextView button_delete_device;
    private GetwayManager mGetwayManager;
    private boolean isStartFromExperience;
    private RelativeLayout layout_config_wifi_getway;
    private RelativeLayout layout_select_room;
    private TextView textview_select_room_name;
    private ClearEditText edittext_input_devie_name;
    private String currentSelectDeviceName;
    private static final int REQUEST_CODE_SELECT_DEVICE_IN_WHAT_ROOM = 100;
    private boolean isUserLogin;
    private SDKManager manager;
    private EventCallback ec;
    private DeviceManager mDeviceManager;
    private String inputDeviceName;
    private DeviceListener mDeviceListener;
    private String deviceUid;
    private Room room;
    private String action;
    private RelativeLayout layout_device_share;
    private TitleLayout layout_title;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_getway_device);
        initViews();
        initDatas();
        initEvents();
    }

    private void initDatas() {
        layout_title.setReturnClickListener(new TitleLayout.ReturnImageClickListener() {
            @Override
            public void onBackPressed() {
                GetwayDeviceActivity.this.onBackPressed();
            }
        });
        layout_title.setEditTextClickListener(new TitleLayout.EditTextClickListener() {
            @Override
            public void onEditTextPressed() {
                if (isStartFromExperience) {
                    if (mDeviceManager.isStartFromHomePage()) {
                        startActivity(new Intent(GetwayDeviceActivity.this, SmartHomeMainActivity.class));
                    } else {
                        startActivity(new Intent(GetwayDeviceActivity.this, ExperienceDevicesActivity.class));
                    }
                } else {
                    if (isUserLogin) {
                        inputDeviceName = edittext_input_devie_name.getText().toString();
                        if (!inputDeviceName.equals(currentSelectDeviceName)) {
                            mDeviceManager.alertDeviceHttp(deviceUid, null, inputDeviceName, null);
                            action = "alertname";
                        } else {
                            onBackPressed();
                        }
                    } else {
                        onBackPressed();
                    }
                }
            }
        });
        mGetwayManager = GetwayManager.getInstance();
        mGetwayManager.InitGetwayManager(this, this);
        mDeviceManager = DeviceManager.getInstance();
        mDeviceManager.InitDeviceManager(this);
        isStartFromExperience = DeviceManager.getInstance().isStartFromExperience();
        initMqtt();
        mDeviceListener = new DeviceListener() {
            @Override
            public void responseAlertDeviceHttpResult(DeviceOperationResponse result) {
                super.responseAlertDeviceHttpResult(result);
                Log.i(TAG, "修改设备属性:" + result.toString());
                if (action.equalsIgnoreCase("alertroom")) {
                    mGetwayManager.updateGetwayDeviceInWhatRoom(room, deviceUid);
                } else if (action.equalsIgnoreCase("alertname")) {
                    Message msg = Message.obtain();
                    msg.what = MSG_ALERT_DEVICENAME_RESULT;
                    mHandler.sendMessage(msg);
                }
                action = "";
            }


        };
        if (isStartFromExperience) {
            edittext_input_devie_name.setText("家里的网关");
            edittext_input_devie_name.setSelection(5);
            edittext_input_devie_name.clearFocus();
            textview_select_room_name.setText("未选择");
        } else {
            currentSelectDeviceName = mGetwayManager.getCurrentSelectGetwayDevice().getName();
            edittext_input_devie_name.setText(currentSelectDeviceName);
            edittext_input_devie_name.setSelection(currentSelectDeviceName.length());
            edittext_input_devie_name.clearFocus();
            List<Room> rooms = mGetwayManager.getCurrentSelectGetwayDevice().getRoomList();
            if (rooms.size() == 1) {
                textview_select_room_name.setText(rooms.get(0).getRoomName());
            } else {
                textview_select_room_name.setText("未选择");
            }
        }
        if(getIntent().getBooleanExtra("isupdateroom",false)){
            isOnActivityResult = true;
            String roomName = getIntent().getStringExtra("roomName");
            if (!isStartFromExperience) {
                room = RoomManager.getInstance().findRoom(roomName, true);
                deviceUid = mGetwayManager.getCurrentSelectGetwayDevice().getUid();
                mDeviceManager.alertDeviceHttp(deviceUid, room.getUid(), null, null);
                action = "alertroom";
            }
            textview_select_room_name.setText(roomName);
        }
    }

    private void initMqtt() {
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
            public void deviceOpSuccess(String op, final String deviceKey) {
                super.deviceOpSuccess(op, deviceKey);
            }

            @Override
            public void connectionLost(Throwable throwable) {
                super.connectionLost(throwable);
                isUserLogin = false;
                Perfence.setPerfence(AppConstant.USER_LOGIN, false);
                new AlertDialog(GetwayDeviceActivity.this).builder().setTitle("账号异地登录")
                        .setMsg("当前账号已在其它设备上登录,是否重新登录")
                        .setPositiveButton("确认", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                startActivity(new Intent(GetwayDeviceActivity.this, LoginActivity.class));
                            }
                        }).setNegativeButton("取消", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                }).show();
            }

            @Override
            public void onFailure(SDKAction action, Throwable throwable) {
            }
        };
    }

    private void initEvents() {
        button_delete_device.setOnClickListener(this);
        layout_config_wifi_getway.setOnClickListener(this);
        layout_select_room.setOnClickListener(this);
        layout_device_share.setOnClickListener(this);

    }

    private void initViews() {
        button_delete_device = findViewById(R.id.button_delete_device);
        layout_config_wifi_getway = findViewById(R.id.layout_config_wifi_getway);
        layout_select_room = findViewById(R.id.layout_select_room);
        textview_select_room_name = findViewById(R.id.textview_select_room_name);
        edittext_input_devie_name = findViewById(R.id.edittext_input_devie_name);
        layout_device_share = findViewById(R.id.layout_device_share);
        layout_title= findViewById(R.id.layout_title);
    }
    private boolean isOnActivityResult;
    @Override
    protected void onResume() {
        super.onResume();
        isUserLogin = Perfence.getBooleanPerfence(AppConstant.USER_LOGIN);
        isStartFromExperience = mDeviceManager.isStartFromExperience();
        manager.addEventCallback(ec);
        mDeviceManager.addDeviceListener(mDeviceListener);
        if (!isStartFromExperience) {
            deviceUid = mGetwayManager.getCurrentSelectGetwayDevice().getUid();
        }
        if (!isOnActivityResult) {
            isOnActivityResult = false;
            if (isStartFromExperience) {
                textview_select_room_name.setText("未选择");
            } else {
                if (mGetwayManager.getCurrentSelectGetwayDevice().getRoomList().size() == 1) {
                    textview_select_room_name.setText((mGetwayManager.getCurrentSelectGetwayDevice().getRoomList().get(0).getRoomName()));
                } else {
                    textview_select_room_name.setText("未选择");
                }
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mDeviceManager.removeDeviceListener(mDeviceListener);
        manager.removeEventCallback(ec);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_delete_device:
                //删除设备
                if (NetUtil.isNetAvailable(this)) {
                    new AlertDialog(GetwayDeviceActivity.this).builder().setTitle("删除设备")
                            .setMsg("确认删除设备")
                            .setPositiveButton("确认", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (isStartFromExperience) {
                                        Toast.makeText(GetwayDeviceActivity.this, "删除网关设备成功", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(GetwayDeviceActivity.this, ExperienceDevicesActivity.class));
                                    } else {
                                        if (isUserLogin) {
                                            mGetwayManager.deleteDeviceHttp();
                                        } else {
                                            ToastSingleShow.showText(GetwayDeviceActivity.this, "未登录,登录后才能操作");
                                        }
                                    }
                                }
                            }).setNegativeButton("取消", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                        }
                    }).show();
                } else {
                    ToastSingleShow.showText(GetwayDeviceActivity.this, "网络未连接");
                }

                break;
            case R.id.layout_config_wifi_getway:
                if(NetUtil.isNetAvailable(this)){
                    Intent inent = new Intent(this, ScanWifiListActivity.class);
                    inent.putExtra("isShowSkipOption", false);
                    startActivity(inent);
                }else{
                    ToastSingleShow.showText(this,"无可用的网络连接");
                }

                break;
            case R.id.layout_device_share:
                Intent inentShareDevice = new Intent(this, ShareDeviceActivity.class);
                inentShareDevice.putExtra("devicetype", DeviceTypeConstant.TYPE.TYPE_SMART_GETWAY);
                if(isStartFromExperience){
                    startActivity(inentShareDevice);
                }else{
                    if (deviceUid != null) {
                        inentShareDevice.putExtra("deviceuid", deviceUid);
                        startActivity(inentShareDevice);
                    }
                }
                break;
            case R.id.layout_select_room:
                if (isStartFromExperience) {
                    Intent intent = new Intent(this, AddDeviceActivity.class);
                    startActivityForResult(intent, REQUEST_CODE_SELECT_DEVICE_IN_WHAT_ROOM);
                } else {
                    if (isUserLogin) {
                        mDeviceManager.setEditDevice(true);
                        mDeviceManager.setCurrentEditDeviceType(DeviceTypeConstant.TYPE.TYPE_SMART_GETWAY);
                        Intent intent = new Intent(this, AddDeviceActivity.class);
                        startActivity(intent);
                    }
                }
                break;

        }
    }

    private static final int MSG_HANDLE_DELETE_DEVICE_RESULT = 100;
    private static final int MSG_ALERT_DEVICENAME_RESULT = 101;
    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_HANDLE_DELETE_DEVICE_RESULT:
                    Log.i(TAG, "删除getway设备uid " + mGetwayManager.getCurrentSelectGetwayDevice().getUid());
                    mGetwayManager.deleteDBGetwayDevice(mGetwayManager.getCurrentSelectGetwayDevice().getUid());
                    Toast.makeText(GetwayDeviceActivity.this, "删除设备成功", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(GetwayDeviceActivity.this, DevicesActivity.class));
                    break;
                case MSG_ALERT_DEVICENAME_RESULT:
                    Log.i(TAG, "修改设备名称 handler msg");
                    mGetwayManager.updateGetwayDeviceName(inputDeviceName);
                    startActivity(new Intent(GetwayDeviceActivity.this, DevicesActivity.class));
                    break;
            }
            return true;
        }
    };
    private Handler mHandler = new WeakRefHandler(mCallback);



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SELECT_DEVICE_IN_WHAT_ROOM && resultCode == RESULT_OK) {
            String roomName = data.getStringExtra("roomName");
            Log.i(TAG, "isStartFromExperience=" + isStartFromExperience);
            if (!isStartFromExperience) {
                room = RoomManager.getInstance().findRoom(roomName, true);
                deviceUid = mGetwayManager.getCurrentSelectGetwayDevice().getUid();
                mDeviceManager.alertDeviceHttp(deviceUid, room.getUid(), null, null);
                action = "alertroom";
            }
            textview_select_room_name.setText(roomName);
        }
    }

    @Override
    public void responseResult(String result) {
        boolean deleteSuccess = true;
        Gson gson = new Gson();
        DeviceList mDeviceList = gson.fromJson(result, DeviceList.class);
        for (int i = 0; i < mDeviceList.getDevice().size(); i++) {
            if (mDeviceList.getDevice().get(i).getUid().equals(mGetwayManager.getCurrentSelectGetwayDevice().getUid())) {
                deleteSuccess = false;
            }
        }
        if (deleteSuccess) {
            mHandler.sendEmptyMessage(MSG_HANDLE_DELETE_DEVICE_RESULT);
        }
    }

    @Override
    public void responseSetWifirelayResult(int result) {

    }

    @Override
    public void responseDeleteDeviceHttpResult(DeviceOperationResponse result) {
        if (result.getStatus() != null && result.getStatus().equals("ok")) {
            if (LocalConnectmanager.getInstance().isLocalconnectAvailable()) {
                mGetwayManager.deleteGetwayDevice();
            } else {
                mHandler.sendEmptyMessage(MSG_HANDLE_DELETE_DEVICE_RESULT);
            }

        }
    }

}
