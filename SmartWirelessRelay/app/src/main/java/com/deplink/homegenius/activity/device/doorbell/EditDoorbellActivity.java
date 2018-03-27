package com.deplink.homegenius.activity.device.doorbell;

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

import com.deplink.homegenius.Protocol.json.Room;
import com.deplink.homegenius.Protocol.json.device.SmartDev;
import com.deplink.homegenius.Protocol.packet.ellisdk.BasicPacket;
import com.deplink.homegenius.Protocol.packet.ellisdk.EllESDK;
import com.deplink.homegenius.Protocol.packet.ellisdk.EllE_Listener;
import com.deplink.homegenius.Protocol.packet.ellisdk.Handler_Background;
import com.deplink.homegenius.Protocol.packet.ellisdk.Handler_UiThread;
import com.deplink.homegenius.Protocol.packet.ellisdk.WIFIData;
import com.deplink.homegenius.activity.device.AddDeviceActivity;
import com.deplink.homegenius.activity.device.DevicesActivity;
import com.deplink.homegenius.activity.device.ShareDeviceActivity;
import com.deplink.homegenius.activity.device.doorbell.add.WifipasswordInputActivity;
import com.deplink.homegenius.activity.personal.login.LoginActivity;
import com.deplink.homegenius.constant.AppConstant;
import com.deplink.homegenius.constant.DeviceTypeConstant;
import com.deplink.homegenius.manager.device.DeviceListener;
import com.deplink.homegenius.manager.device.DeviceManager;
import com.deplink.homegenius.manager.device.doorbeel.DoorbeelManager;
import com.deplink.homegenius.manager.device.smartlock.SmartLockManager;
import com.deplink.homegenius.manager.room.RoomManager;
import com.deplink.homegenius.util.Perfence;
import com.deplink.homegenius.util.WeakRefHandler;
import com.deplink.homegenius.view.combinationwidget.TitleLayout;
import com.deplink.homegenius.view.dialog.AlertDialog;
import com.deplink.homegenius.view.dialog.loadingdialog.DialogThreeBounce;
import com.deplink.homegenius.view.edittext.ClearEditText;
import com.deplink.sdk.android.sdk.DeplinkSDK;
import com.deplink.sdk.android.sdk.EventCallback;
import com.deplink.sdk.android.sdk.SDKAction;
import com.deplink.sdk.android.sdk.homegenius.DeviceOperationResponse;
import com.deplink.sdk.android.sdk.manager.SDKManager;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

import deplink.com.smartwirelessrelay.homegenius.EllESDK.R;

public class EditDoorbellActivity extends Activity implements View.OnClickListener, EllE_Listener {
    private static final String TAG = "EditDoorbellActivity";
    private TextView button_delete_device;
    private DeviceManager mDeviceManager;
    private DeviceListener mDeviceListener;
    private ClearEditText edittext_add_device_input_name;
    private DoorbeelManager mDoorbeelManager;
    private String deviceUid;
    private RelativeLayout layout_room_select;
    private boolean isUserLogin;
    private SDKManager manager;
    private EventCallback ec;
    private TextView textview_select_room_name;
    private boolean isStartFromExperience;
    private String devicename;
    private RelativeLayout layout_getway_select;
    private Room room;
    private String action;
    private EllESDK ellESDK;
    private TextView textview_select_getway_name;
    private TextView textview_select_lock_name;
    private RelativeLayout layout_lock_list;
    private ListView listview_select_lock;
    private RelativeLayout layout_lock_select;
    private ImageView imageview_lock_arror_right;
    private LockSelectListAdapter lockSelectListAdapter;
    private List<SmartDev> mLockList;
    private String selectLockName;
    private RelativeLayout layout_device_share;
    private TitleLayout layout_title;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit2);
        initViews();
        initDatas();
        initEvents();
    }

    private WIFIData wifiData;

    @Override
    protected void onResume() {
        super.onResume();
        isStartFromExperience = DeviceManager.getInstance().isStartFromExperience();
        isUserLogin = Perfence.getBooleanPerfence(AppConstant.USER_LOGIN);
        mDeviceManager.onResume(mDeviceListener);
        if (isStartFromExperience) {
            edittext_add_device_input_name.setText("智能门铃");
            edittext_add_device_input_name.setSelection(4);
        } else {
            manager.addEventCallback(ec);
            devicename = mDoorbeelManager.getCurrentSelectedDoorbeel().getName();
            if (devicename != null && !devicename.equalsIgnoreCase("")) {
                edittext_add_device_input_name.setText(devicename);
                edittext_add_device_input_name.setSelection(devicename.length());
            }
            deviceUid = mDoorbeelManager.getCurrentSelectedDoorbeel().getUid();
            if (mDoorbeelManager.getCurrentSelectedDoorbeel().getBindLockUid() != null) {
                SmartDev bindlock = DataSupport.where("Uid=?", mDoorbeelManager.getCurrentSelectedDoorbeel().getBindLockUid()).findFirst(SmartDev.class, true);
                textview_select_lock_name.setText(bindlock.getName());
            }else{
                Log.i(TAG,"绑定的门锁uid是:"+mDoorbeelManager.getCurrentSelectedDoorbeel().getBindLockUid());
            }

            ellESDK.startSearchDevs();
            Handler_Background.execute(new Runnable() {
                @Override
                public void run() {
                    ellESDK.stopSearchDevs();
                    wifiData = ellESDK.getDevWiFiConfigWithMac(maclong, type, ver);
                    Handler_UiThread.runTask("", new Runnable() {
                        @Override
                        public void run() {
                            if (wifiData != null) {
                                textview_select_getway_name.setText("当前配置的WIFI:" + wifiData.ssid);
                            }
                        }
                    }, 0);
                }
            });
            if (!isOnActivityResult) {
                List<Room> rooms = mDoorbeelManager.getCurrentSelectedDoorbeel().getRooms();
                if (rooms.size() == 1) {
                    textview_select_room_name.setText(rooms.get(0).getRoomName());
                } else {
                    textview_select_room_name.setText("全部");
                }
            }

        }
    }

    private long maclong;
    private byte ver;
    private byte type;

    @Override
    protected void onPause() {
        super.onPause();
        isOnActivityResult = false;
        mDeviceManager.onPause(mDeviceListener);
        manager.removeEventCallback(ec);
    }

    private void initEvents() {
        button_delete_device.setOnClickListener(this);
        layout_device_share.setOnClickListener(this);
        layout_room_select.setOnClickListener(this);
        layout_getway_select.setOnClickListener(this);
        layout_lock_select.setOnClickListener(this);
        listview_select_lock.setAdapter(lockSelectListAdapter);
        listview_select_lock.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                selectLockName = mLockList.get(position).getName();
                textview_select_lock_name.setText(selectLockName);
                layout_lock_list.setVisibility(View.GONE);
                if (mDoorbeelManager.getCurrentSelectedDoorbeel() != null) {
                    Log.i(TAG,"mLockList.get(position).getBindLockUid()="+mLockList.get(position).getUid());
                    mDoorbeelManager.getCurrentSelectedDoorbeel().setBindLockUid(mLockList.get(position).getUid());
                    mDoorbeelManager.getCurrentSelectedDoorbeel().saveFast();
                }
            }
        });
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
                new AlertDialog(EditDoorbellActivity.this).builder().setTitle("账号异地登录")
                        .setMsg("当前账号已在其它设备上登录,是否重新登录")
                        .setPositiveButton("确认", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                startActivity(new Intent(EditDoorbellActivity.this, LoginActivity.class));
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

    private void initDatas() {
        ellESDK = EllESDK.getInstance();
        ellESDK.InitEllESDK(this, this);
        mDeviceManager = DeviceManager.getInstance();
        mDeviceManager.InitDeviceManager(this);
        isStartFromExperience = DeviceManager.getInstance().isStartFromExperience();
        mDoorbeelManager = DoorbeelManager.getInstance();
        mDeviceManager.InitDeviceManager(this);
        SmartLockManager mSmartLockManager = SmartLockManager.getInstance();
        mSmartLockManager.InitSmartLockManager(this);
        mDeviceListener = new DeviceListener() {
            @Override
            public void responseDeleteDeviceHttpResult(DeviceOperationResponse result) {
                super.responseDeleteDeviceHttpResult(result);
                DialogThreeBounce.hideLoading();
                mHandler.sendEmptyMessage(MSG_HANDLE_DELETE_DEVICE_RESULT);
            }

            @Override
            public void responseAlertDeviceHttpResult(DeviceOperationResponse result) {
                super.responseAlertDeviceHttpResult(result);
                switch (action) {
                    case "alertname":
                        mHandler.sendEmptyMessage(MSG_ALERT_DEVICENAME_RESULT);
                        break;
                    case "alertroom":
                        mHandler.sendEmptyMessage(MSG_ALERT_DEVICEROOM_RESULT);
                        break;
                }
                action = "";

            }
        };
        mLockList = new ArrayList<>();
        mLockList.addAll(mSmartLockManager.getAllLock());
        lockSelectListAdapter = new LockSelectListAdapter(this, mLockList);
        initMqtt();
        layout_title.setReturnClickListener(new TitleLayout.ReturnImageClickListener() {
            @Override
            public void onBackPressed() {
                EditDoorbellActivity.this.onBackPressed();
            }
        });
        layout_title.setEditTextClickListener(new TitleLayout.EditTextClickListener() {
            @Override
            public void onEditTextPressed() {
                String devcienamechange = edittext_add_device_input_name.getText().toString();
                if (!devcienamechange.equalsIgnoreCase(devicename)) {
                    action = "alertname";
                    devicename = devcienamechange;
                    mDeviceManager.alertDeviceHttp(deviceUid, null, devcienamechange, null);
                } else {
                    Intent intent = new Intent(EditDoorbellActivity.this, DoorbeelMainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
            }
        });
    }

    private static final int REQUEST_CODE_SELECT_DEVICE_IN_WHAT_ROOM = 100;
    private static final int MSG_HANDLE_DELETE_DEVICE_RESULT = 100;
    private static final int MSG_ALERT_DEVICENAME_RESULT = 101;
    private static final int MSG_ALERT_DEVICEROOM_RESULT = 102;
    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_HANDLE_DELETE_DEVICE_RESULT:
                    mDeviceManager.deleteDBSmartDevice(mDeviceManager.getCurrentSelectSmartDevice().getUid());
                    Toast.makeText(EditDoorbellActivity.this, "删除设备成功", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(EditDoorbellActivity.this, DevicesActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    break;
                case MSG_ALERT_DEVICENAME_RESULT:
                    Log.i(TAG, "修改设备名称 handler msg");
                    mDoorbeelManager.updateDoorbeelName(devicename);
                    intent = new Intent(EditDoorbellActivity.this, DoorbeelMainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    break;
                case MSG_ALERT_DEVICEROOM_RESULT:
                    Log.i(TAG, "修改设备房间 handler msg");
                    if (room != null) {
                        mDoorbeelManager.updateDeviceInWhatRoom(room, deviceUid);
                    }
                    break;
            }
            return true;
        }
    };
    private Handler mHandler = new WeakRefHandler(mCallback);
    private void initViews() {
        layout_title= findViewById(R.id.layout_title);
        button_delete_device = findViewById(R.id.button_delete_device);
        edittext_add_device_input_name = findViewById(R.id.edittext_add_device_input_name);
        layout_room_select = findViewById(R.id.layout_room_select);
        textview_select_room_name = findViewById(R.id.textview_select_room_name);
        layout_getway_select = findViewById(R.id.layout_getway_select);
        textview_select_getway_name = findViewById(R.id.textview_select_getway_name);
        textview_select_lock_name = findViewById(R.id.textview_select_lock_name);
        layout_lock_list = findViewById(R.id.layout_lock_list);
        listview_select_lock = findViewById(R.id.listview_select_lock);
        layout_lock_select = findViewById(R.id.layout_lock_select);
        imageview_lock_arror_right = findViewById(R.id.imageview_lock_arror_right);
        layout_device_share = findViewById(R.id.layout_device_share);
    }

    private boolean isOnActivityResult;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SELECT_DEVICE_IN_WHAT_ROOM && resultCode == RESULT_OK) {
            isOnActivityResult = true;
            String roomName = data.getStringExtra("roomName");
            Log.i(TAG, "isStartFromExperience=" + isStartFromExperience);
            if (!isStartFromExperience) {
                room = RoomManager.getInstance().findRoom(roomName, true);
                mDeviceManager.alertDeviceHttp(deviceUid, room.getUid(), null, null);
                action = "alertroom";
            }
            textview_select_room_name.setText(roomName);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_delete_device:
                new AlertDialog(EditDoorbellActivity.this).builder().setTitle("删除设备")
                        .setMsg("确认删除设备")
                        .setPositiveButton("确认", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mDeviceManager.deleteDeviceHttp();
                            }
                        }).setNegativeButton("取消", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                }).show();
                break;
            case R.id.layout_getway_select:
                mDoorbeelManager.setConfigWifi(true);
                startActivity(new Intent(this, WifipasswordInputActivity.class));
                break;
            case R.id.layout_room_select:
                if (isStartFromExperience) {
                    Intent intent = new Intent(this, AddDeviceActivity.class);
                    startActivityForResult(intent, REQUEST_CODE_SELECT_DEVICE_IN_WHAT_ROOM);
                } else {
                    if (isUserLogin) {
                        Intent intent = new Intent(this, AddDeviceActivity.class);
                        intent.putExtra("addDeviceSelectRoom", true);
                        startActivityForResult(intent, REQUEST_CODE_SELECT_DEVICE_IN_WHAT_ROOM);
                    }
                }
                break;

            case R.id.layout_lock_select:
                if (layout_lock_list.getVisibility() == View.VISIBLE) {
                    layout_lock_list.setVisibility(View.GONE);
                    imageview_lock_arror_right.setImageResource(R.drawable.gotoicon);
                } else {
                    layout_lock_list.setVisibility(View.VISIBLE);
                    imageview_lock_arror_right.setImageResource(R.drawable.nextdirectionicon);
                }
                break;
            case R.id.layout_device_share:
                Intent inentShareDevice = new Intent(this, ShareDeviceActivity.class);
                inentShareDevice.putExtra("devicetype", DeviceTypeConstant.TYPE.TYPE_MENLING);
                if (isStartFromExperience) {
                    startActivity(inentShareDevice);
                } else {
                    if (deviceUid != null) {
                        inentShareDevice.putExtra("deviceuid", deviceUid);
                        startActivity(inentShareDevice);
                    }
                }

                break;
        }
    }

    @Override
    public void onRecvEllEPacket(BasicPacket packet) {

    }

    @Override
    public void searchDevCBS(long mac, byte type, byte ver) {
        Log.e(TAG, "mac:" + mac + "type:" + type + "ver:" + ver);
        maclong = mac;
        this.type = type;
        this.ver = ver;
    }
}
