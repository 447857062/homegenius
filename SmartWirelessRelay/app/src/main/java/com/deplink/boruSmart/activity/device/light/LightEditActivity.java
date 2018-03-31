package com.deplink.boruSmart.activity.device.light;

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

import com.deplink.boruSmart.activity.device.adapter.GetwaySelectListAdapter;
import com.deplink.boruSmart.activity.personal.experienceCenter.ExperienceDevicesActivity;
import com.deplink.boruSmart.activity.personal.login.LoginActivity;
import com.deplink.boruSmart.constant.DeviceTypeConstant;
import com.deplink.boruSmart.manager.device.light.SmartLightManager;
import com.deplink.boruSmart.util.Perfence;
import com.deplink.boruSmart.util.WeakRefHandler;
import com.deplink.boruSmart.Protocol.json.Room;
import com.deplink.boruSmart.Protocol.json.device.SmartDev;
import com.deplink.boruSmart.Protocol.json.device.getway.GatwayDevice;
import com.deplink.boruSmart.activity.device.AddDeviceActivity;
import com.deplink.boruSmart.activity.device.DevicesActivity;
import com.deplink.boruSmart.activity.device.ShareDeviceActivity;
import com.deplink.boruSmart.constant.AppConstant;
import com.deplink.boruSmart.manager.connect.local.tcp.LocalConnectmanager;
import com.deplink.boruSmart.manager.device.DeviceListener;
import com.deplink.boruSmart.manager.device.DeviceManager;
import com.deplink.boruSmart.manager.device.getway.GetwayManager;
import com.deplink.boruSmart.manager.room.RoomManager;
import com.deplink.boruSmart.util.NetUtil;
import com.deplink.boruSmart.view.combinationwidget.TitleLayout;
import com.deplink.boruSmart.view.dialog.AlertDialog;
import com.deplink.boruSmart.view.edittext.ClearEditText;
import com.deplink.boruSmart.view.toast.ToastSingleShow;
import com.deplink.sdk.android.sdk.DeplinkSDK;
import com.deplink.sdk.android.sdk.EventCallback;
import com.deplink.sdk.android.sdk.SDKAction;
import com.deplink.sdk.android.sdk.homegenius.DeviceOperationResponse;
import com.deplink.sdk.android.sdk.manager.SDKManager;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

import deplink.com.smartwirelessrelay.homegenius.EllESDK.R;

public class LightEditActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "LightEditActivity";
    private Button button_delete_device;
    private TextView textview_select_room_name;
    private RelativeLayout layout_select_room;
    private ClearEditText edittext_input_devie_name;
    private List<GatwayDevice> mGetways;
    private ListView listview_select_getway;
    private RelativeLayout layout_getway_list;
    private TextView textview_select_getway_name;
    private RelativeLayout layout_getway;
    private ImageView imageview_getway_arror_right;
    private SmartLightManager mSmartLightManager;
    private DeviceManager mDeviceManager;
    private DeviceListener mDeviceListener;
    private GatwayDevice selectedGatway;
    private String action;
    private String deviceUid;
    private SDKManager manager;
    private EventCallback ec;
    private boolean isLogin;
    private String lightName;
    private boolean isStartFromExperience;
    private String selectGetwayName;
    /**
     * onactivityresult设置完房间名称后onresume就不能设置
     */
    private boolean isOnActivityResult;
    private Room changeRoom;
    private RelativeLayout layout_device_share;
    private TitleLayout layout_title;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_light_edit);
        initViews();
        initDatas();
        initEvents();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mDeviceManager.addDeviceListener(mDeviceListener);
        manager.addEventCallback(ec);
        isLogin = Perfence.getBooleanPerfence(AppConstant.USER_LOGIN);
        isStartFromExperience = DeviceManager.getInstance().isStartFromExperience();
        if (isStartFromExperience) {
            textview_select_room_name.setText("未选择");
            textview_select_getway_name.setText("未设置网关");
        } else {
            lightName = mSmartLightManager.getCurrentSelectLight().getName();
            if (lightName != null) {
                edittext_input_devie_name.setText(lightName);
                edittext_input_devie_name.setSelection(lightName.length());
            }
            deviceUid= mSmartLightManager.getCurrentSelectLight().getUid();
            SmartDev smartDev = DataSupport.where("Uid=?",deviceUid).findFirst(SmartDev.class, true);
            if (!isOnActivityResult) {
                if (mSmartLightManager.getCurrentSelectLight().getRooms().size() == 1) {
                    textview_select_room_name.setText(smartDev.getRooms().get(0).getRoomName());
                } else {
                    textview_select_room_name.setText("未选择");
                }
            }
            GatwayDevice temp = smartDev.getGetwayDevice();
            if (temp == null) {
                GatwayDevice localDbGatwayDevice = null;
                if(smartDev.getGetwayDeviceUid()!=null){
                     localDbGatwayDevice= DataSupport.where("uid=?", smartDev.getGetwayDeviceUid()).findFirst(GatwayDevice.class);
                }
               if(localDbGatwayDevice!=null){
                   textview_select_getway_name.setText(localDbGatwayDevice.getName());
               }else{
                   textview_select_getway_name.setText("未设置网关");
               }

            } else {
                textview_select_getway_name.setText(smartDev.getGetwayDevice().getName());
            }
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        isOnActivityResult=false;
        mDeviceManager.removeDeviceListener(mDeviceListener);
        manager.removeEventCallback(ec);
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
                LightEditActivity.this.onBackPressed();
            }
        });
        layout_title.setEditTextClickListener(new TitleLayout.EditTextClickListener() {
            @Override
            public void onEditTextPressed() {
                if (isStartFromExperience) {
                    onBackPressed();
                } else {
                    if(NetUtil.isNetAvailable(LightEditActivity.this)){
                        if (isLogin) {
                            String lightnameChange = edittext_input_devie_name.getText().toString();
                            if (lightnameChange.equals("")) {
                                ToastSingleShow.showText(LightEditActivity.this, "请输入设备名称");
                                return;
                            }
                            if (lightnameChange.equals(lightName)) {
                                onBackPressed();
                            } else {
                                action = "alertname";
                                lightName = lightnameChange;
                                deviceUid = mSmartLightManager.getCurrentSelectLight().getUid();
                                mDeviceManager.alertDeviceHttp(deviceUid, null, lightName, null);
                            }
                        } else {
                            LightEditActivity.this.finish();
                        }
                    }else{
                        ToastSingleShow.showText(LightEditActivity.this, "网络连接不可用");
                    }
                }
            }
        });
        isStartFromExperience = DeviceManager.getInstance().isStartFromExperience();
        mDeviceManager = DeviceManager.getInstance();
        mSmartLightManager = SmartLightManager.getInstance();
        if (isStartFromExperience) {
            edittext_input_devie_name.setText("我家的智能灯");
            edittext_input_devie_name.setSelection(6);
        }
        mDeviceManager.InitDeviceManager(this);
        mSmartLightManager.InitSmartLightManager(this);
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
                    deviceUid = mSmartLightManager.getCurrentSelectLight().getUid();
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
                Perfence.setPerfence(AppConstant.USER_LOGIN, false);
                isLogin = false;
                new AlertDialog(LightEditActivity.this).builder().setTitle("账号异地登录")
                        .setMsg("当前账号已在其它设备上登录,是否重新登录")
                        .setPositiveButton("确认", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                startActivity(new Intent(LightEditActivity.this, LoginActivity.class));
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
                if (LocalConnectmanager.getInstance().isLocalconnectAvailable()) {
                    mDeviceManager.deleteSmartDevice();
                }
                mHandler.sendEmptyMessage(MSG_HANDLE_DELETE_DEVICE_RESULT);
            }

            @Override
            public void responseAlertDeviceHttpResult(DeviceOperationResponse result) {
                super.responseAlertDeviceHttpResult(result);
                switch (action) {
                    case "alertroom":
                        mSmartLightManager.updateSmartDeviceRoom(changeRoom, deviceUid);
                        break;
                    case "alertname":
                        boolean saveNameresult = mSmartLightManager.updateSmartDeviceName(mSmartLightManager.getCurrentSelectLight().getUid(), lightName);
                        if (!saveNameresult) {
                            Toast.makeText(LightEditActivity.this, "更新智能设备名称失败", Toast.LENGTH_SHORT).show();
                        }
                        Intent intent = new Intent(LightEditActivity.this, LightActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        break;
                    case "alertgetway":
                        boolean saveTbResult = mSmartLightManager.updateSmartDeviceGetway(selectedGatway);
                        if (!saveTbResult) {
                            Toast.makeText(LightEditActivity.this, "更新智能设备所属网关失败", Toast.LENGTH_SHORT).show();
                        }
                        break;
                }
                action = "";
            }

            @Override
            public void responseBindDeviceResult(String result) {
                super.responseBindDeviceResult(result);
            }
        };
        if(getIntent().getBooleanExtra("isupdateroom",false)){
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



    private static final int MSG_HANDLE_DELETE_DEVICE_RESULT = 100;
    private static final int MSG_HANDLE_DELETE_DEVICE_FAILED = 101;
    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_HANDLE_DELETE_DEVICE_RESULT:
                    mDeviceManager.deleteDBSmartDevice(mDeviceManager.getCurrentSelectSmartDevice().getUid());
                    Toast.makeText(LightEditActivity.this, "删除设备成功", Toast.LENGTH_SHORT).show();
                    mDeviceManager.deleteDBSmartDevice(mDeviceManager.getCurrentSelectSmartDevice().getUid());
                    startActivity(new Intent(LightEditActivity.this, DevicesActivity.class));
                    break;
                case MSG_HANDLE_DELETE_DEVICE_FAILED:
                    Toast.makeText(LightEditActivity.this, "删除设备失败", Toast.LENGTH_SHORT).show();
                    break;
            }
            return true;
        }
    };
    private Handler mHandler = new WeakRefHandler(mCallback);
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.layout_device_share:
                Intent inentShareDevice = new Intent(this, ShareDeviceActivity.class);
                inentShareDevice.putExtra("devicetype", DeviceTypeConstant.TYPE.TYPE_LIGHT);
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
               mDeviceManager.setEditDevice(true);
                mDeviceManager.setCurrentEditDeviceType(DeviceTypeConstant.TYPE.TYPE_LIGHT);
                Intent intent = new Intent(this, AddDeviceActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                break;
            case R.id.button_delete_device:
                //删除设备
                new AlertDialog(LightEditActivity.this).builder().setTitle("删除设备")
                        .setMsg("确认删除设备")
                        .setPositiveButton("确认", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (!isStartFromExperience) {
                                    if (isLogin) {
                                        mDeviceManager.deleteDeviceHttp();
                                    } else {
                                        ToastSingleShow.showText(LightEditActivity.this, "用户未登录");
                                    }
                                } else {
                                    startActivity(new Intent(LightEditActivity.this, ExperienceDevicesActivity.class));
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
}
