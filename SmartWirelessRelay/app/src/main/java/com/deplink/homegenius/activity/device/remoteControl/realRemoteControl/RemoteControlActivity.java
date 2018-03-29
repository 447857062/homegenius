package com.deplink.homegenius.activity.device.remoteControl.realRemoteControl;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.deplink.homegenius.Protocol.json.OpResult;
import com.deplink.homegenius.Protocol.json.Room;
import com.deplink.homegenius.Protocol.json.device.SmartDev;
import com.deplink.homegenius.Protocol.json.device.getway.GatwayDevice;
import com.deplink.homegenius.activity.device.AddDeviceActivity;
import com.deplink.homegenius.activity.device.DevicesActivity;
import com.deplink.homegenius.activity.device.ShareDeviceActivity;
import com.deplink.homegenius.activity.device.adapter.GetwaySelectListAdapter;
import com.deplink.homegenius.activity.personal.experienceCenter.ExperienceDevicesActivity;
import com.deplink.homegenius.activity.personal.login.LoginActivity;
import com.deplink.homegenius.constant.AppConstant;
import com.deplink.homegenius.constant.DeviceTypeConstant;
import com.deplink.homegenius.manager.connect.local.tcp.LocalConnectmanager;
import com.deplink.homegenius.manager.device.DeviceListener;
import com.deplink.homegenius.manager.device.DeviceManager;
import com.deplink.homegenius.manager.device.getway.GetwayManager;
import com.deplink.homegenius.manager.device.remoteControl.RemoteControlManager;
import com.deplink.homegenius.manager.room.RoomManager;
import com.deplink.homegenius.util.Perfence;
import com.deplink.homegenius.util.WeakRefHandler;
import com.deplink.homegenius.view.combinationwidget.TitleLayout;
import com.deplink.homegenius.view.dialog.AlertDialog;
import com.deplink.homegenius.view.dialog.loadingdialog.DialogThreeBounce;
import com.deplink.homegenius.view.edittext.ClearEditText;
import com.deplink.homegenius.view.toast.ToastSingleShow;
import com.deplink.sdk.android.sdk.DeplinkSDK;
import com.deplink.sdk.android.sdk.EventCallback;
import com.deplink.sdk.android.sdk.SDKAction;
import com.deplink.sdk.android.sdk.homegenius.DeviceOperationResponse;
import com.deplink.sdk.android.sdk.manager.SDKManager;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

import deplink.com.smartwirelessrelay.homegenius.EllESDK.R;

public class RemoteControlActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "RemoteControlActivity";
    private RemoteControlManager mRemoteControlManager;
    private List<GatwayDevice> mGetways;
    private ListView listview_select_getway;
    private RelativeLayout layout_getway_list;
    private TextView textview_select_getway_name;
    private TextView textview_select_room_name;
    private RelativeLayout layout_getway;
    private RelativeLayout layout_select_room;
    private ImageView imageview_getway_arror_right;
    private TextView button_delete_device;
    private DeviceManager mDeviceManager;
    private boolean isOnActivityResult;
    private boolean isStartFromExperience;
    private ClearEditText edittext_input_devie_name;
    private String deviceName;
    private SDKManager manager;
    private EventCallback ec;
    private boolean isLogin;
    private RoomManager mRoomManager;
    private String selectGetwayName;
    private DeviceListener mDeviceListener;
    private String deviceUid;
    private Room room;
    private GatwayDevice selectedGatway;
    private String action;
    private RelativeLayout layout_device_share;
    private TitleLayout layout_title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remote_control);
        initViews();
        initDatas();
        initEvents();
    }

    private void initDatas() {
        isLogin = Perfence.getBooleanPerfence(AppConstant.USER_LOGIN);
        layout_title.setReturnClickListener(new TitleLayout.ReturnImageClickListener() {
            @Override
            public void onBackPressed() {
                RemoteControlActivity.this.onBackPressed();
            }
        });
        layout_title.setEditTextClickListener(new TitleLayout.EditTextClickListener() {
            @Override
            public void onEditTextPressed() {
                action = "alertname";
                String changeDeviceName = edittext_input_devie_name.getText().toString();
                if (changeDeviceName.equals("")) {
                    ToastSingleShow.showText(RemoteControlActivity.this, "请输入设备而名称");
                    return;
                }
                if (!isStartFromExperience) {
                    if (!changeDeviceName.equals(deviceName)) {
                        mRemoteControlManager.saveCurrentSelectDeviceName(changeDeviceName);
                    }
                    if (isLogin) {
                        deviceUid = DeviceManager.getInstance().getCurrentSelectSmartDevice().getUid();
                        deviceName = changeDeviceName;
                        mDeviceManager.alertDeviceHttp(deviceUid, changeDeviceName, null, null);
                    }
                } else {
                    startActivity(new Intent(RemoteControlActivity.this, ExperienceDevicesActivity.class));
                }
            }
        });
        mDeviceManager = DeviceManager.getInstance();
        mRoomManager = RoomManager.getInstance();
        mRoomManager.initRoomManager(this);
        isStartFromExperience = DeviceManager.getInstance().isStartFromExperience();
        mRemoteControlManager = RemoteControlManager.getInstance();
        mRemoteControlManager.InitRemoteControlManager(this);
        mGetways = new ArrayList<>();
        mGetways.addAll(GetwayManager.getInstance().getAllGetwayDevice());
        GetwaySelectListAdapter selectGetwayAdapter = new GetwaySelectListAdapter(this, mGetways);
        listview_select_getway.setAdapter(selectGetwayAdapter);
        listview_select_getway.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectGetwayName = mGetways.get(position).getName();
                textview_select_getway_name.setText(selectGetwayName);
                layout_getway_list.setVisibility(View.GONE);
                if (!isStartFromExperience) {
                    action = "alertgetway";
                    selectedGatway = mGetways.get(position);
                    deviceUid = mRemoteControlManager.getmSelectRemoteControlDevice().getUid();
                    mDeviceManager.alertDeviceHttp(deviceUid, null, null, selectedGatway.getUid());
                }
            }
        });
        mDeviceManager.InitDeviceManager(this);
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
                new AlertDialog(RemoteControlActivity.this).builder().setTitle("账号异地登录")
                        .setMsg("当前账号已在其它设备上登录,是否重新登录")
                        .setPositiveButton("确认", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                startActivity(new Intent(RemoteControlActivity.this, LoginActivity.class));
                            }
                        }).setNegativeButton("取消", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                }).show();
            }

            @Override
            public void notifyHomeGeniusResponse(String result) {
                super.notifyHomeGeniusResponse(result);
                updateDeviceStatu(result);

            }
        };
        mDeviceListener = new DeviceListener() {
            @Override
            public void responseBindDeviceResult(String result) {
                super.responseBindDeviceResult(result);
                //不处理本地删除设备的回应,远程http接口删除了就代表删除成功

            }

            @Override
            public void responseAlertDeviceHttpResult(DeviceOperationResponse result) {
                super.responseAlertDeviceHttpResult(result);
                switch (action) {
                    case "alertroom":
                        mRemoteControlManager.updateSmartDeviceInWhatRoom(room, deviceUid);
                        break;
                    case "alertname":
                        boolean saveNameresult = mRemoteControlManager.saveCurrentSelectDeviceName(deviceName);
                        if (!saveNameresult) {
                            Toast.makeText(RemoteControlActivity.this, "更新智能设备名称失败", Toast.LENGTH_SHORT).show();
                        } else {
                            startActivity(new Intent(RemoteControlActivity.this, DevicesActivity.class));
                        }
                        break;
                    case "alertgetway":
                        boolean saveGetwayResult = mRemoteControlManager.updateSmartDeviceGetway(selectedGatway);
                        if (!saveGetwayResult) {
                            ToastSingleShow.showText(RemoteControlActivity.this, "更新智能设备所属网关失败");
                        }
                        break;
                }
                action = "";
            }

            @Override
            public void responseDeleteDeviceHttpResult(DeviceOperationResponse result) {
                super.responseDeleteDeviceHttpResult(result);
                if (LocalConnectmanager.getInstance().isLocalconnectAvailable()) {
                    mDeviceManager.deleteSmartDevice();
                }
                DialogThreeBounce.hideLoading();
                mHandler.sendEmptyMessage(MSG_HANDLE_DELETE_DEVICE_RESULT);
            }

            @Override
            public void responseQueryResult(String result) {
                super.responseQueryResult(result);
                updateDeviceStatu(result);
            }
        };
        if (getIntent().getBooleanExtra("isupdateroom", false)) {
            isOnActivityResult = true;
            String roomName = getIntent().getStringExtra("roomName");
            Log.i(TAG, "roomName=" + roomName);
            if (!isStartFromExperience) {
                if (isLogin) {
                    action = "alertroom";
                    room = RoomManager.getInstance().findRoom(roomName, true);
                    deviceUid = DeviceManager.getInstance().getCurrentSelectSmartDevice().getUid();
                    mDeviceManager.alertDeviceHttp(deviceUid, room.getUid(), null, null);
                } else {
                    ToastSingleShow.showText(RemoteControlActivity.this, "未登录登录后操作");
                }
            }
            textview_select_room_name.setText(roomName);
        }
    }

    private void updateDeviceStatu(String result) {
        Gson gson = new Gson();
        OpResult content = null;
        try {
            content = gson.fromJson(result, OpResult.class);
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        }
        if (content != null) {
            if (content.getOP() != null && content.getOP().equalsIgnoreCase("REPORT")) {
                if (content.getMethod().equalsIgnoreCase("IrmoteV2")) {
                    Message msg = Message.obtain();
                    msg.what = MSG_UPDATE_DEVICE_STATU;
                    msg.arg1 = content.getResult();
                    mHandler.sendMessage(msg);
                }
            }
        }
    }

    private void initEvents() {
        layout_getway.setOnClickListener(this);
        layout_select_room.setOnClickListener(this);
        button_delete_device.setOnClickListener(this);
        layout_device_share.setOnClickListener(this);
    }


    @Override
    protected void onPause() {
        super.onPause();
        manager.removeEventCallback(ec);
        mDeviceManager.removeDeviceListener(mDeviceListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        manager.addEventCallback(ec);
        mDeviceManager.addDeviceListener(mDeviceListener);
        isLogin = Perfence.getBooleanPerfence(AppConstant.USER_LOGIN);
        deviceName = edittext_input_devie_name.getText().toString();
        if (!isStartFromExperience) {
            if (!isOnActivityResult) {
                deviceUid = mRemoteControlManager.getmSelectRemoteControlDevice().getUid();
                SmartDev smartDev = DataSupport.where("Uid=?", deviceUid).findFirst(SmartDev.class, true);
                List<Room> rooms = mRemoteControlManager.getmSelectRemoteControlDevice().getRooms();
                if (rooms.size() == 1) {
                    textview_select_room_name.setText(rooms.get(0).getRoomName());
                } else {
                    textview_select_room_name.setText("未选择");
                }
                GatwayDevice temp = smartDev.getGetwayDevice();
                if (temp == null && smartDev.getGetwayDeviceUid() != null) {
                    GatwayDevice localDbGatwayDevice = DataSupport.where("uid=?", smartDev.getGetwayDeviceUid()).findFirst(GatwayDevice.class);
                    if (localDbGatwayDevice != null) {
                        textview_select_getway_name.setText(localDbGatwayDevice.getName());
                    } else {
                        textview_select_getway_name.setText("未设置网关");
                    }
                } else {
                    textview_select_getway_name.setText(smartDev.getGetwayDevice().getName());
                }
            } else {
                isOnActivityResult = false;
                textview_select_getway_name.setText("未设置网关");
            }
            String deviceName = mDeviceManager.getCurrentSelectSmartDevice().getName();
            edittext_input_devie_name.setText(deviceName);
            edittext_input_devie_name.setSelection(deviceName.length());
            mRemoteControlManager.queryStatu();
        } else {
            if (!isOnActivityResult) {
                textview_select_room_name.setText("未选择");
            }
            textview_select_getway_name.setText("未设置网关");
            edittext_input_devie_name.setText("我家的遥控器");
            edittext_input_devie_name.setSelection(6);
        }

    }

    private void initViews() {
        button_delete_device = findViewById(R.id.button_delete_device);
        textview_select_room_name = findViewById(R.id.textview_select_room_name);
        layout_getway_list = findViewById(R.id.layout_getway_list);
        textview_select_getway_name = findViewById(R.id.textview_select_getway_name);
        layout_getway = findViewById(R.id.layout_getway);
        layout_select_room = findViewById(R.id.layout_select_room);
        listview_select_getway = findViewById(R.id.listview_select_getway);
        imageview_getway_arror_right = findViewById(R.id.imageview_getway_arror_right);
        edittext_input_devie_name = findViewById(R.id.edittext_input_devie_name);
        layout_device_share = findViewById(R.id.layout_device_share);
        layout_title = findViewById(R.id.layout_title);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.layout_device_share:
                Intent inentShareDevice = new Intent(this, ShareDeviceActivity.class);
                inentShareDevice.putExtra("devicetype", DeviceTypeConstant.TYPE.TYPE_REMOTECONTROL);
                if (isStartFromExperience) {
                    startActivity(inentShareDevice);
                } else {
                    if (deviceUid != null) {
                        inentShareDevice.putExtra("deviceuid", deviceUid);
                        startActivity(inentShareDevice);
                    }
                }

                break;

            case R.id.button_delete_device:
                new AlertDialog(RemoteControlActivity.this).builder().setTitle("删除设备")
                        .setMsg("确认删除设备")
                        .setPositiveButton("确认", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (!isStartFromExperience) {
                                    if (isLogin) {
                                        DialogThreeBounce.showLoading(RemoteControlActivity.this);
                                        mDeviceManager.deleteDeviceHttp();
                                    } else {
                                        ToastSingleShow.showText(RemoteControlActivity.this, "用户未登录");
                                    }
                                } else {
                                    startActivity(new Intent(RemoteControlActivity.this, ExperienceDevicesActivity.class));
                                }
                            }
                        }).setNegativeButton("取消", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                }).show();
                break;
            case R.id.layout_select_room:
                mDeviceManager.setEditDevice(true);
                mDeviceManager.setCurrentEditDeviceType(DeviceTypeConstant.TYPE.TYPE_REMOTECONTROL);
                Intent intent = new Intent(this, AddDeviceActivity.class);
                startActivity(intent);
                break;
            case R.id.layout_getway:
                if (layout_getway_list.getVisibility() == View.VISIBLE) {
                    layout_getway_list.setVisibility(View.GONE);
                    imageview_getway_arror_right.setImageResource(R.drawable.gotoicon);
                } else {
                    layout_getway_list.setVisibility(View.VISIBLE);
                    imageview_getway_arror_right.setImageResource(R.drawable.nextdirectionicon);
                }
                break;

        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SELECT_DEVICE_IN_WHAT_ROOM && resultCode == RESULT_OK) {
            isOnActivityResult = true;
            String roomName = data.getStringExtra("roomName");
            Log.i(TAG, "roomName=" + roomName);
            if (!isStartFromExperience) {
                if (isLogin) {
                    action = "alertroom";
                    room = RoomManager.getInstance().findRoom(roomName, true);
                    deviceUid = DeviceManager.getInstance().getCurrentSelectSmartDevice().getUid();
                    mDeviceManager.alertDeviceHttp(deviceUid, room.getUid(), null, null);
                } else {
                    ToastSingleShow.showText(RemoteControlActivity.this, "未登录登录后操作");
                }
            }
            textview_select_room_name.setText(roomName);
        }
    }

    private static final int REQUEST_CODE_SELECT_DEVICE_IN_WHAT_ROOM = 100;
    private static final int MSG_HANDLE_DELETE_DEVICE_RESULT = 100;
    private static final int MSG_HANDLE_DELETE_DEVICE_FAILED = 101;
    private static final int MSG_UPDATE_DEVICE_STATU = 102;
    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_HANDLE_DELETE_DEVICE_RESULT:
                    mDeviceManager.deleteDBSmartDevice(mDeviceManager.getCurrentSelectSmartDevice().getUid());
                    Toast.makeText(RemoteControlActivity.this, "删除设备成功", Toast.LENGTH_SHORT).show();
                    mRemoteControlManager.deleteCurrentSelectDevice();
                    startActivity(new Intent(RemoteControlActivity.this, DevicesActivity.class));
                    break;
                case MSG_HANDLE_DELETE_DEVICE_FAILED:
                    Toast.makeText(RemoteControlActivity.this, "删除设备失败", Toast.LENGTH_SHORT).show();
                    break;
                case MSG_UPDATE_DEVICE_STATU:
                    if (mRemoteControlManager.getmSelectRemoteControlDevice() != null) {
                        if (msg.arg1 != -1) {
                            mRemoteControlManager.getmSelectRemoteControlDevice().setStatus("在线");
                            mRemoteControlManager.getmSelectRemoteControlDevice().saveFast();
                        } else {
                            mRemoteControlManager.getmSelectRemoteControlDevice().setStatus("离线");
                            mRemoteControlManager.getmSelectRemoteControlDevice().saveFast();
                        }

                    }
                    break;
            }
            return true;
        }
    };
    private Handler mHandler = new WeakRefHandler(mCallback);
}
