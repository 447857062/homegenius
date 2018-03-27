package com.deplink.homegenius.activity.device.smartlock;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.deplink.homegenius.Protocol.json.Room;
import com.deplink.homegenius.Protocol.json.device.DeviceList;
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
import com.deplink.homegenius.manager.device.smartlock.SmartLockManager;
import com.deplink.homegenius.manager.room.RoomManager;
import com.deplink.homegenius.util.NetUtil;
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

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

import deplink.com.smartwirelessrelay.homegenius.EllESDK.R;

public class EditSmartLockActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "EditSmartLockActivity";
    private Button button_delete_device;
    private DeviceManager mDeviceManager;
    private TextView textview_select_room_name;
    private RelativeLayout layout_select_room;
    private ClearEditText edittext_input_devie_name;
    private SmartLockManager mSmartLockManager;
    private List<GatwayDevice> mGetways;
    private ListView listview_select_getway;
    private RelativeLayout layout_getway_list;
    private TextView textview_select_getway_name;
    private RelativeLayout layout_getway;
    private ImageView imageview_getway_arror_right;
    private DeviceListener mDeviceListener;
    private GatwayDevice selectedGatway;
    private String action;
    private String deviceUid;
    private boolean isStartFromExperience;
    private boolean isOnActivityResult;
    private Room changeRoom;
    private String selectGetwayName;
    private boolean isLogin;
    private SDKManager manager;
    private EventCallback ec;
    private RelativeLayout layout_device_share;
    private TitleLayout layout_title;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_smart_lock);
        initViews();
        initDatas();
        initEvents();
    }

    private void initEvents() {
        button_delete_device.setOnClickListener(this);
        layout_select_room.setOnClickListener(this);
        layout_getway.setOnClickListener(this);
        layout_device_share.setOnClickListener(this);
    }

    private void initDatas() {
        layout_title.setReturnClickListener(new TitleLayout.ReturnImageClickListener() {
            @Override
            public void onBackPressed() {
                EditSmartLockActivity.this.onBackPressed();
            }
        });
        layout_title.setEditTextClickListener(new TitleLayout.EditTextClickListener() {
            @Override
            public void onEditTextPressed() {
                Intent intentBack = new Intent(EditSmartLockActivity.this, SmartLockActivity.class);
                intentBack.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                if (isStartFromExperience) {
                    startActivity(intentBack);
                } else {
                    if (NetUtil.isNetAvailable(EditSmartLockActivity.this)) {
                        if (isLogin) {
                            action = "alertname";
                            devcienameChange = edittext_input_devie_name.getText().toString();
                            if (devcienameChange.equals(lockName)) {
                                onBackPressed();
                            } else {
                                if (deviceUid != null) {
                                    mDeviceManager.alertDeviceHttp(deviceUid, null, devcienameChange, null);
                                }
                            }
                        } else {
                            EditSmartLockActivity.this.finish();
                        }

                    } else {
                        ToastSingleShow.showText(EditSmartLockActivity.this, "网络连接不可用");
                    }

                }
            }
        });
        mSmartLockManager = SmartLockManager.getInstance();
        mDeviceManager = DeviceManager.getInstance();
        mDeviceManager.InitDeviceManager(this);
        mSmartLockManager.InitSmartLockManager(this);
        isStartFromExperience = DeviceManager.getInstance().isStartFromExperience();
        if (isStartFromExperience) {
            edittext_input_devie_name.setText("我家的门锁");
            edittext_input_devie_name.setSelection(5);
        }
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
                    deviceUid = mSmartLockManager.getCurrentSelectLock().getUid();
                    mDeviceManager.alertDeviceHttp(deviceUid, null, null, selectedGatway.getUid());
                }
            }
        });
        mDeviceListener = new DeviceListener() {
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
            public void responseBindDeviceResult(String result) {
                super.responseBindDeviceResult(result);
                Gson gson = new Gson();
                boolean deleteSuccess = true;
                DeviceList mDeviceList = gson.fromJson(result, DeviceList.class);
                for (int i = 0; i < mDeviceList.getSmartDev().size(); i++) {
                    if (mDeviceList.getSmartDev().get(i).getUid().equals(mDeviceManager.getCurrentSelectSmartDevice().getUid())) {
                        deleteSuccess = false;
                    }
                }
                DialogThreeBounce.hideLoading();
                if (deleteSuccess) {
                    mHandler.sendEmptyMessage(MSG_HANDLE_DELETE_DEVICE_RESULT);
                } else {
                    mHandler.sendEmptyMessage(MSG_HANDLE_DELETE_DEVICE_FAILED);
                }
            }

            @Override
            public void responseAlertDeviceHttpResult(DeviceOperationResponse result) {
                super.responseAlertDeviceHttpResult(result);
                switch (action) {
                    case "alertroom":
                        mSmartLockManager.updateSmartDeviceInWhatRoom(changeRoom, deviceUid);
                        break;
                    case "alertname":
                        Intent intentBack = new Intent(EditSmartLockActivity.this, SmartLockActivity.class);
                        intentBack.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        mSmartLockManager.updateSmartDeviceName(devcienameChange);
                        startActivity(intentBack);
                        break;
                    case "alertgetway":
                        boolean savegetwayResult = mSmartLockManager.updateSmartDeviceGetway(selectedGatway);
                        if (!savegetwayResult) {
                            Toast.makeText(EditSmartLockActivity.this, "更新智能设备所属网关失败", Toast.LENGTH_SHORT).show();
                        }
                        break;
                }
                action = "";
            }
        };
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
                new AlertDialog(EditSmartLockActivity.this).builder().setTitle("账号异地登录")
                        .setMsg("当前账号已在其它设备上登录,是否重新登录")
                        .setPositiveButton("确认", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                startActivity(new Intent(EditSmartLockActivity.this, LoginActivity.class));
                            }
                        }).setNegativeButton("取消", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                }).show();
            }
        };
        if (getIntent().getBooleanExtra("isupdateroom", false)) {
            isOnActivityResult = true;
            String roomName = getIntent().getStringExtra("roomName");
            Log.i(TAG, "roomName=" + roomName);
            if (!isStartFromExperience) {
                action = "alertroom";
                changeRoom = RoomManager.getInstance().findRoom(roomName, true);
                deviceUid = mDeviceManager.getCurrentSelectSmartDevice().getUid();
                mDeviceManager.alertDeviceHttp(deviceUid, changeRoom.getUid(), null, null);
            }
            textview_select_room_name.setText(roomName);
        }
    }

    private void initViews() {
        layout_title= findViewById(R.id.layout_title);
        textview_select_getway_name = findViewById(R.id.textview_select_getway_name);
        button_delete_device = findViewById(R.id.button_delete_device);
        layout_select_room = findViewById(R.id.layout_select_room);
        textview_select_room_name = findViewById(R.id.textview_select_room_name);
        edittext_input_devie_name = findViewById(R.id.edittext_input_devie_name);
        layout_getway_list = findViewById(R.id.layout_getway_list);
        layout_getway = findViewById(R.id.layout_getway);
        listview_select_getway = findViewById(R.id.listview_select_getway);
        imageview_getway_arror_right = findViewById(R.id.imageview_getway_arror_right);
        layout_device_share = findViewById(R.id.layout_device_share);
    }

    private String devcienameChange;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.layout_select_room:
                Intent intent = new Intent(this, AddDeviceActivity.class);
                startActivity(intent);
                mSmartLockManager.setEditSmartLock(true);
                break;
            case R.id.layout_device_share:
                Intent inentShareDevice = new Intent(this, ShareDeviceActivity.class);
                inentShareDevice.putExtra("devicetype", DeviceTypeConstant.TYPE.TYPE_LOCK);
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
                new AlertDialog(EditSmartLockActivity.this).builder().setTitle("删除设备")
                        .setMsg("确认删除设备")
                        .setPositiveButton("确认", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (!isStartFromExperience) {
                                    if (NetUtil.isNetAvailable(EditSmartLockActivity.this)) {
                                        if (isLogin) {
                                            DialogThreeBounce.showLoading(EditSmartLockActivity.this);
                                            mDeviceManager.deleteDeviceHttp();
                                        }
                                    } else {
                                        ToastSingleShow.showText(EditSmartLockActivity.this, "网络连接不可用");
                                    }
                                } else {
                                    startActivity(new Intent(EditSmartLockActivity.this, ExperienceDevicesActivity.class));
                                }
                            }
                        }).setNegativeButton("取消", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                }).show();
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

    private String lockName;

    @Override
    protected void onResume() {
        super.onResume();
        isStartFromExperience = mDeviceManager.isStartFromExperience();
        isLogin = Perfence.getBooleanPerfence(AppConstant.USER_LOGIN);
        if (isStartFromExperience) {

        } else {
            lockName = mSmartLockManager.getCurrentSelectLock().getName();
            if (lockName != null) {
                edittext_input_devie_name.setText(lockName);
                Log.i(TAG, "lockName=" + lockName + "lockName.length()=" + lockName.length());
                edittext_input_devie_name.setSelection(lockName.length());
            }
            deviceUid = mSmartLockManager.getCurrentSelectLock().getUid();
            String roomname = getIntent().getStringExtra("roomName");
            if (roomname != null) {
                textview_select_room_name.setText(roomname);
            } else if (!isOnActivityResult) {
                isOnActivityResult = false;
                if (mSmartLockManager.getCurrentSelectLock().getRooms().size() == 1) {
                    textview_select_room_name.setText(mSmartLockManager.getCurrentSelectLock().getRooms().get(0).getRoomName());
                } else {
                    textview_select_room_name.setText("未选择");
                }
                SmartDev smartDev = DataSupport.where("Uid=?", mSmartLockManager.getCurrentSelectLock().getUid()).findFirst(SmartDev.class, true);
                GatwayDevice temp = smartDev.getGetwayDevice();
                if (temp == null) {
                    GatwayDevice localDbGatwayDevice = DataSupport.where("uid=?", smartDev.getGetwayDeviceUid()).findFirst(GatwayDevice.class);
                    if (localDbGatwayDevice != null) {
                        textview_select_getway_name.setText(localDbGatwayDevice.getName());
                    } else {
                        textview_select_getway_name.setText("未设置网关");
                    }
                } else {
                    textview_select_getway_name.setText(smartDev.getGetwayDevice().getName());
                }
            }
        }
        mDeviceManager.onResume(mDeviceListener);
        manager.addEventCallback(ec);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mDeviceManager.onPause(mDeviceListener);
        manager.removeEventCallback(ec);
    }

    private static final int MSG_HANDLE_DELETE_DEVICE_RESULT = 100;
    private static final int MSG_HANDLE_DELETE_DEVICE_FAILED = 101;
    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_HANDLE_DELETE_DEVICE_RESULT:
                    mDeviceManager.deleteDBSmartDevice(mDeviceManager.getCurrentSelectSmartDevice().getUid());
                    Toast.makeText(EditSmartLockActivity.this, "删除设备成功", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(EditSmartLockActivity.this, DevicesActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    break;
                case MSG_HANDLE_DELETE_DEVICE_FAILED:
                    Toast.makeText(EditSmartLockActivity.this, "删除设备失败", Toast.LENGTH_SHORT).show();
                    break;
            }
            return true;
        }
    };
    private Handler mHandler = new WeakRefHandler(mCallback);
}
