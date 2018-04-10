package com.deplink.boruSmart.activity.device.remoteControl;

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

import com.deplink.boruSmart.Protocol.json.Room;
import com.deplink.boruSmart.Protocol.json.device.SmartDev;
import com.deplink.boruSmart.activity.device.DevicesActivity;
import com.deplink.boruSmart.activity.device.adapter.RemoteControlSelectListAdapter;
import com.deplink.boruSmart.activity.personal.experienceCenter.ExperienceDevicesActivity;
import com.deplink.boruSmart.activity.personal.login.LoginActivity;
import com.deplink.boruSmart.constant.AppConstant;
import com.deplink.boruSmart.manager.device.DeviceManager;
import com.deplink.boruSmart.manager.device.remoteControl.RemoteControlListener;
import com.deplink.boruSmart.manager.device.remoteControl.RemoteControlManager;
import com.deplink.boruSmart.util.NetUtil;
import com.deplink.boruSmart.util.Perfence;
import com.deplink.boruSmart.view.combinationwidget.TitleLayout;
import com.deplink.boruSmart.view.dialog.AlertDialog;
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

public class EditRemoteDevicesActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "EditDoorbeelActivity";
    private TextView button_delete_device;
    /**
     * 当前要绑定的物理遥控器
     */
    private SmartDev currentSelectRemotecontrol;
    private RelativeLayout layout_remotecontrol_select;
    private RelativeLayout layout_remotecontrol_list;
    private ImageView imageview_remotecontrol_arror_right;
    private List<SmartDev> mRemoteControls;
    private ListView listview_select_remotecontrol;
    private TextView textview_select_remotecontrol_name;
    private TextView textview_select_room_name;
    private ClearEditText edittext_add_device_input_name;
    private SDKManager manager;
    private EventCallback ec;
    private boolean isUserLogin;
    private boolean isStartFromExperience;
    private String deviceType;
    private String selectRemotecontrolName;
    private RemoteControlListener mRemoteControlListener;
    private RemoteControlManager mRemoteControlManager;
    private String action;
    private  String changeDevicename;
    private String deviceUid;
    private TitleLayout layout_title;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_remote_devices);
        initViews();
        initDatas();
        initEvents();
    }
    private void initEvents() {
        button_delete_device.setOnClickListener(this);
        layout_remotecontrol_select.setOnClickListener(this);
    }
    private void initDatas() {
        layout_title.setReturnClickListener(new TitleLayout.ReturnImageClickListener() {
            @Override
            public void onBackPressed() {
                EditRemoteDevicesActivity.this.onBackPressed();
            }
        });
        layout_title.setEditTextClickListener(new TitleLayout.EditTextClickListener() {
            @Override
            public void onEditTextPressed() {
                if (!isStartFromExperience) {
                    if (isUserLogin) {
                        action="alertname";
                        changeDevicename = edittext_add_device_input_name.getText().toString();
                        if (!changeDevicename.equals("")) {
                            deviceUid=mRemoteControlManager.getmSelectRemoteControlDevice().getUid();
                            mRemoteControlManager.alertVirtualDevice(deviceUid,changeDevicename,null,null);
                        }
                    } else {
                        Ftoast.create(EditRemoteDevicesActivity.this).setText("未登录,登录后才能删除设备").show();
                    }
                }else{
                    onBackPressed();
                }
            }
        });
        deviceType = getIntent().getStringExtra("deviceType");
        String deviceName;
        isStartFromExperience = DeviceManager.getInstance().isStartFromExperience();
        if (isStartFromExperience) {
            deviceName = "智能空调遥控器";
            textview_select_room_name.setText("客厅");
            textview_select_remotecontrol_name.setText("未设置物理遥控器");
        } else {
            deviceName = RemoteControlManager.getInstance().getmSelectRemoteControlDevice().getName();
            String realRcUid= RemoteControlManager.getInstance().getmSelectRemoteControlDevice().getRemotecontrolUid();
            SmartDev realRc=DataSupport.where("Uid=?", realRcUid).findFirst(SmartDev.class,true);
            List<Room> rooms = realRc.getRooms();
            if (rooms.size() > 0) {
                textview_select_room_name.setText(rooms.get(0).getRoomName());
            } else {
                textview_select_room_name.setText("未选择");

            }
            List<SmartDev> remoteControls = RemoteControlManager.getInstance().findRemotecontrolDevice();
            if (remoteControls.size() > 0) {
                textview_select_remotecontrol_name.setText(remoteControls.get(0).getName());
            } else {
                textview_select_remotecontrol_name.setText("未设置物理遥控器");
            }
        }
        edittext_add_device_input_name.setText(deviceName);
        edittext_add_device_input_name.setSelection(deviceName.length());
        Log.i(TAG, "initDatas deviceType=" + deviceType);
        layout_title.setTitleText(deviceType);
        mRemoteControls = new ArrayList<>();
        mRemoteControls.addAll(RemoteControlManager.getInstance().findAllRemotecontrolDevice());
        RemoteControlSelectListAdapter selectRemotecontrolAdapter = new RemoteControlSelectListAdapter(this, mRemoteControls);
        listview_select_remotecontrol.setAdapter(selectRemotecontrolAdapter);
        listview_select_remotecontrol.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(!isStartFromExperience){
                    action="alertBindedRemoteControl";
                    currentSelectRemotecontrol = mRemoteControls.get(position);
                    selectRemotecontrolName = mRemoteControls.get(position).getName();
                    textview_select_remotecontrol_name.setText(selectRemotecontrolName);
                    layout_remotecontrol_list.setVisibility(View.GONE);
                    imageview_remotecontrol_arror_right.setImageResource(R.drawable.gotoicon);
                    deviceUid=mRemoteControlManager.getmSelectRemoteControlDevice().getUid();
                    mRemoteControlManager.alertVirtualDevice(deviceUid,null,null,currentSelectRemotecontrol.getUid());
                }

            }
        });
        initMqttCallback();
        mRemoteControlListener=new RemoteControlListener() {
            @Override
            public void responseDeleteVirtualDevice(DeviceOperationResponse result) {
                super.responseDeleteVirtualDevice(result);
                if(result.getStatus().equalsIgnoreCase("ok")){
                    int saveResult = RemoteControlManager.getInstance().deleteCurrentSelectDevice();
                    if (saveResult > 0) {
                        startActivity(new Intent(EditRemoteDevicesActivity.this, DevicesActivity.class));
                    } else {
                        Ftoast.create(EditRemoteDevicesActivity.this).setText("删除" + deviceType + "失败").show();
                    }
                }else{
                    Ftoast.create(EditRemoteDevicesActivity.this).setText("删除" + deviceType + "失败").show();
                }
            }

            @Override
            public void responseAlertVirtualDevice(DeviceOperationResponse result) {
                super.responseAlertVirtualDevice(result);
                boolean saveResult;
                switch (action){
                    case "alertname":
                         saveResult = mRemoteControlManager.saveCurrentSelectDeviceName(changeDevicename);
                        if (saveResult) {
                            finish();
                        } else {
                            Ftoast.create(EditRemoteDevicesActivity.this).setText("修改设备名称失败").show();
                        }
                        break;
                    case "alertBindedRemoteControl":
                         saveResult = mRemoteControlManager.saveCurrentVirtualDeviceBindRCUid(changeDevicename);
                        if (saveResult) {
                            finish();
                        } else {
                            Ftoast.create(EditRemoteDevicesActivity.this).setText("修改绑定的真实智能遥控失败").show();
                        }
                        break;
                }
                action="";
            }
        };

        mRemoteControlManager=RemoteControlManager.getInstance();
        mRemoteControlManager.InitRemoteControlManager(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        isStartFromExperience = DeviceManager.getInstance().isStartFromExperience();
        manager.addEventCallback(ec);
        mRemoteControlManager.addRemoteControlListener(mRemoteControlListener);
        isUserLogin = Perfence.getBooleanPerfence(AppConstant.USER_LOGIN);
        if(!isStartFromExperience){
            deviceUid=mRemoteControlManager.getmSelectRemoteControlDevice().getUid();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        mRemoteControlManager.removeRemoteControlListener(mRemoteControlListener);
        manager.removeEventCallback(ec);
    }

    private void initMqttCallback() {
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
                isUserLogin = false;
                Perfence.setPerfence(AppConstant.USER_LOGIN, false);
                new AlertDialog(EditRemoteDevicesActivity.this).builder().setTitle("账号异地登录")
                        .setMsg("当前账号已在其它设备上登录,是否重新登录")
                        .setPositiveButton("确认", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                startActivity(new Intent(EditRemoteDevicesActivity.this, LoginActivity.class));
                            }
                        }).setNegativeButton("取消", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                }).show();
            }
        };
    }

    private void initViews() {
        textview_select_remotecontrol_name = (TextView) findViewById(R.id.textview_select_remotecontrol_name);
        button_delete_device = (TextView) findViewById(R.id.button_delete_device);
        textview_select_room_name = (TextView) findViewById(R.id.textview_select_room_name);
        layout_remotecontrol_select = (RelativeLayout) findViewById(R.id.layout_remotecontrol_select);
        layout_remotecontrol_list = (RelativeLayout) findViewById(R.id.layout_remotecontrol_list);
        imageview_remotecontrol_arror_right = (ImageView) findViewById(R.id.imageview_remotecontrol_arror_right);
        listview_select_remotecontrol = (ListView) findViewById(R.id.listview_select_remotecontrol);
        edittext_add_device_input_name = (ClearEditText) findViewById(R.id.edittext_add_device_input_name);
        layout_title= (TitleLayout) findViewById(R.id.layout_title);
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.layout_remotecontrol_select:
                if (layout_remotecontrol_list.getVisibility() == View.VISIBLE) {
                    layout_remotecontrol_list.setVisibility(View.GONE);
                    imageview_remotecontrol_arror_right.setImageResource(R.drawable.gotoicon);
                } else {
                    layout_remotecontrol_list.setVisibility(View.VISIBLE);
                    imageview_remotecontrol_arror_right.setImageResource(R.drawable.nextdirectionicon);
                }
                break;
            case R.id.button_delete_device:
                new AlertDialog(EditRemoteDevicesActivity.this).builder().setTitle("删除设备")
                        .setMsg("确认删除设备")
                        .setPositiveButton("确认", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                            }
                        }).setNegativeButton("取消", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!isStartFromExperience) {
                            if(NetUtil.isNetAvailable(EditRemoteDevicesActivity.this)){
                                if (isUserLogin) {
                                    mRemoteControlManager.deleteVirtualDeviceHttp();
                                } else {
                                    Ftoast.create(EditRemoteDevicesActivity.this).setText("未登录,登录后才能删除设备").show();
                                }
                            }else{
                                Ftoast.create(EditRemoteDevicesActivity.this).setText("网络连接不可用").show();
                            }


                        } else {
                            startActivity(new Intent(EditRemoteDevicesActivity.this, ExperienceDevicesActivity.class));
                        }
                    }
                }).show();
                break;

        }
    }
}
