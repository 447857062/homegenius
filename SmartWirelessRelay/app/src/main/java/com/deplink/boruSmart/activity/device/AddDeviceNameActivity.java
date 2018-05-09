package com.deplink.boruSmart.activity.device;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.deplink.boruSmart.Protocol.json.Room;
import com.deplink.boruSmart.Protocol.json.device.DeviceList;
import com.deplink.boruSmart.Protocol.json.device.SmartDev;
import com.deplink.boruSmart.Protocol.json.device.getway.GatwayDevice;
import com.deplink.boruSmart.Protocol.json.device.router.Router;
import com.deplink.boruSmart.Protocol.json.qrcode.QrcodeSmartDevice;
import com.deplink.boruSmart.activity.device.adapter.GetwaySelectListAdapter;
import com.deplink.boruSmart.activity.device.adapter.RemoteControlSelectListAdapter;
import com.deplink.boruSmart.activity.device.getway.wifi.ScanWifiListActivity;
import com.deplink.boruSmart.activity.device.remoteControl.add.ChooseBandActivity;
import com.deplink.boruSmart.activity.device.remoteControl.topBox.AddTopBoxActivity;
import com.deplink.boruSmart.activity.device.remoteControl.tv.AddTvDeviceActivity;
import com.deplink.boruSmart.constant.AppConstant;
import com.deplink.boruSmart.constant.DeviceTypeConstant;
import com.deplink.boruSmart.manager.connect.local.tcp.LocalConnectmanager;
import com.deplink.boruSmart.manager.device.DeviceListener;
import com.deplink.boruSmart.manager.device.DeviceManager;
import com.deplink.boruSmart.manager.device.doorbeel.DoorbeelManager;
import com.deplink.boruSmart.manager.device.getway.GetwayListener;
import com.deplink.boruSmart.manager.device.getway.GetwayManager;
import com.deplink.boruSmart.manager.device.light.SmartLightManager;
import com.deplink.boruSmart.manager.device.remoteControl.RemoteControlManager;
import com.deplink.boruSmart.manager.device.router.RouterManager;
import com.deplink.boruSmart.manager.device.smartswitch.SmartSwitchManager;
import com.deplink.boruSmart.manager.room.RoomManager;
import com.deplink.boruSmart.util.NetUtil;
import com.deplink.boruSmart.util.Perfence;
import com.deplink.boruSmart.util.StringValidatorUtil;
import com.deplink.boruSmart.util.WeakRefHandler;
import com.deplink.boruSmart.view.dialog.AlertDialog;
import com.deplink.boruSmart.view.toast.Ftoast;
import com.deplink.sdk.android.sdk.DeplinkSDK;
import com.deplink.sdk.android.sdk.EventCallback;
import com.deplink.sdk.android.sdk.SDKAction;
import com.deplink.sdk.android.sdk.bean.User;
import com.deplink.sdk.android.sdk.homegenius.DeviceAddBody;
import com.deplink.sdk.android.sdk.homegenius.DeviceOperationResponse;
import com.deplink.sdk.android.sdk.homegenius.VirtualDeviceAddBody;
import com.deplink.sdk.android.sdk.manager.SDKManager;
import com.google.gson.Gson;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

import deplink.com.smartwirelessrelay.homegenius.EllESDK.R;

public class AddDeviceNameActivity extends Activity implements View.OnClickListener, GetwayListener {
    private static final String TAG = "AddDeviceNameActivity";
    private String currentAddDevice;
    private DeviceManager mDeviceManager;
    private SmartSwitchManager mSmartSwitchManager;
    private Button button_add_device_sure;
    private EditText edittext_add_device_input_name;
    /**
     * 当前待添加设备
     */
    private QrcodeSmartDevice device;
    private String deviceType;
    private String switchqrcode;
    private TextView textview_select_getway_name;
    private RelativeLayout layout_getway_select;
    private RelativeLayout layout_getway_list;
    private RelativeLayout layout_room_select;
    private DoorbeelManager mDoorbeelManager;
    private List<GatwayDevice> mGetways;
    private ListView listview_select_getway;
    private List<SmartDev> mRemoteControls;
    private ListView listview_select_remotecontrol;
    private ImageView imageview_getway_arror_right;
    private ImageView imageview_remotecontrol_arror_right;
    private TextView textview_select_room_name;
    private TextView textview_select_remotecontrol_name;
    private String deviceName;
    private Room currentSelectedRoom;
    private GatwayDevice currentSelectGetway;
    private SmartDev currentSelectRemotecontrol;
    private RelativeLayout layout_remotecontrol_select;
    private RelativeLayout layout_remotecontrol_list;
    private SmartLightManager mSmartLightManager;
    private RoomManager mRoomManager;
    private static final int REQUEST_CODE_SELECT_DEVICE_IN_WHAT_ROOM = 100;
    private GetwayManager mGetwayManager;
    private SDKManager manager;
    private EventCallback ec;
    private boolean isUserLogin;
    private String selectGetwayName;
    private String selectRemotecontrolName;
    private String addDeviceUid;
    private RouterManager mRouterManager;
    private String topic;
    private DeviceListener mDeviceListener;
    private ImageView imageview_doorbell_step;
    private TextView textview_title;
    private FrameLayout framelayout_back;
    private FrameLayout framelayout_x;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_device_name);
        registerNetBroadcast(this);
        initViews();
        initDatas();
        initEvents();
    }

    private void initEvents() {
        button_add_device_sure.setOnClickListener(this);
        layout_getway_select.setOnClickListener(this);
        layout_room_select.setOnClickListener(this);
        layout_remotecontrol_select.setOnClickListener(this);
        framelayout_back.setOnClickListener(this);
        framelayout_x.setOnClickListener(this);
    }

    private void initViews() {
        framelayout_back = findViewById(R.id.framelayout_back);
        framelayout_x = findViewById(R.id.framelayout_x);
        textview_title = findViewById(R.id.textview_title);
        button_add_device_sure = findViewById(R.id.button_add_device_sure);
        edittext_add_device_input_name = findViewById(R.id.edittext_add_device_input_name);
        textview_select_remotecontrol_name = findViewById(R.id.textview_select_remotecontrol_name);
        textview_select_room_name = findViewById(R.id.textview_select_room_name);
        textview_select_getway_name = findViewById(R.id.textview_select_getway_name);
        layout_getway_select = findViewById(R.id.layout_getway_select);
        layout_getway_list = findViewById(R.id.layout_getway_list);
        layout_room_select = findViewById(R.id.layout_room_select);
        layout_remotecontrol_list = findViewById(R.id.layout_remotecontrol_list);
        layout_remotecontrol_select = findViewById(R.id.layout_remotecontrol_select);
        listview_select_getway = findViewById(R.id.listview_select_getway);
        listview_select_remotecontrol = findViewById(R.id.listview_select_remotecontrol);
        imageview_getway_arror_right = findViewById(R.id.imageview_getway_arror_right);
        imageview_remotecontrol_arror_right = findViewById(R.id.imageview_remotecontrol_arror_right);
        imageview_doorbell_step = findViewById(R.id.imageview_doorbell_step);
    }

    private void initDatas() {
        initManager();
        //getintent data
        currentAddDevice = getIntent().getStringExtra("currentAddDevice");
        deviceType = getIntent().getStringExtra("DeviceType");
        switchqrcode = getIntent().getStringExtra("switchqrcode");
        setRoomSelectVisible();
        setDeviceNameDefault();
        mRemoteControls = new ArrayList<>();
        mGetways = new ArrayList<>();
        mGetways.addAll(GetwayManager.getInstance().getAllGetwayDevice());
        GetwaySelectListAdapter selectGetwayAdapter = new GetwaySelectListAdapter(this, mGetways);
        mRemoteControls.addAll(RemoteControlManager.getInstance().findAllRemotecontrolDevice());
        RemoteControlSelectListAdapter selectRemotecontrolAdapter = new RemoteControlSelectListAdapter(this, mRemoteControls);
        listview_select_remotecontrol.setAdapter(selectRemotecontrolAdapter);
        listview_select_remotecontrol.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectRemotecontrolName = mRemoteControls.get(position).getName();
                textview_select_remotecontrol_name.setText(selectRemotecontrolName);
                layout_remotecontrol_list.setVisibility(View.GONE);
                imageview_remotecontrol_arror_right.setImageResource(R.drawable.gotoicon);
                currentSelectRemotecontrol = mRemoteControls.get(position);
            }
        });
        listview_select_getway.setAdapter(selectGetwayAdapter);
        listview_select_getway.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectGetwayName = mGetways.get(position).getName();
                textview_select_getway_name.setText(selectGetwayName);
                layout_getway_list.setVisibility(View.GONE);
                currentSelectGetway = mGetways.get(position);
            }
        });
        showSettinglayout();
        initMqttCallback();
        initDeviceListener();
    }

    private void initManager() {
        mDoorbeelManager = DoorbeelManager.getInstance();
        mDoorbeelManager.InitDoorbeelManager(this);
        mSmartLightManager = SmartLightManager.getInstance();
        mSmartLightManager.InitSmartLightManager(this);
        mSmartSwitchManager = SmartSwitchManager.getInstance();
        mSmartSwitchManager.InitSmartSwitchManager(this);
        mDeviceManager = DeviceManager.getInstance();
        mDeviceManager.InitDeviceManager(this);
        mRoomManager = RoomManager.getInstance();
        mRoomManager.initRoomManager(this);
        mGetwayManager = GetwayManager.getInstance();
        mGetwayManager.InitGetwayManager(this, this);
        mRouterManager = RouterManager.getInstance();
        mRouterManager.InitRouterManager();
    }

    private String titleString;

    private void setDeviceNameDefault() {
        Log.i(TAG, "setDeviceNameDefault deviceType=" + deviceType);
        switch (deviceType) {
            case DeviceTypeConstant.TYPE.TYPE_LOCK:
                titleString = "智能门锁";
                edittext_add_device_input_name.setText("智能门锁");
                edittext_add_device_input_name.setSelection(4);
                break;
            case "IRMOTE_V2":
                titleString = "智能遥控";
                edittext_add_device_input_name.setText("智能遥控");
                edittext_add_device_input_name.setSelection(4);
                break;
            case DeviceTypeConstant.TYPE.TYPE_AIR_REMOTECONTROL:
                titleString = "智能空调遥控器";
                edittext_add_device_input_name.setText("智能空调遥控器");
                edittext_add_device_input_name.setSelection(7);
                break;
            case DeviceTypeConstant.TYPE.TYPE_TV_REMOTECONTROL:
                titleString = "智能电视遥控器";
                edittext_add_device_input_name.setText("智能电视遥控器");
                edittext_add_device_input_name.setSelection(7);
                break;
            case DeviceTypeConstant.TYPE.TYPE_TVBOX_REMOTECONTROL:
                titleString = "智能机顶盒遥控";
                edittext_add_device_input_name.setText("智能机顶盒遥控");
                edittext_add_device_input_name.setSelection(7);
                break;
            case DeviceTypeConstant.TYPE.TYPE_SWITCH:
                titleString = "智能开关";
                edittext_add_device_input_name.setText("智能开关");
                edittext_add_device_input_name.setSelection(4);
                break;
            case DeviceTypeConstant.TYPE.TYPE_MENLING:
                titleString = "智能门铃";
                edittext_add_device_input_name.setText("智能门铃");
                edittext_add_device_input_name.setSelection(4);
                break;
            case DeviceTypeConstant.TYPE.TYPE_LIGHT:
                titleString = "智能灯泡";
                edittext_add_device_input_name.setText("智能灯泡");
                edittext_add_device_input_name.setSelection(4);
                break;
            case DeviceTypeConstant.TYPE.TYPE_GETWAY_OR_ROUTER:
                titleString = "智能网关/路由器";
                edittext_add_device_input_name.setText("智能网关/路由器");
                edittext_add_device_input_name.setSelection(8);
                break;
            case DeviceTypeConstant.TYPE.TYPE_SMART_GETWAY:
                titleString = "智能网关";
                edittext_add_device_input_name.setText("智能网关");
                edittext_add_device_input_name.setSelection(4);
                break;
            case DeviceTypeConstant.TYPE.TYPE_ROUTER:
                titleString = "路由器";
                edittext_add_device_input_name.setText("路由器");
                edittext_add_device_input_name.setSelection(3);
                break;
        }
        textview_title.setText(titleString);
    }

    private void setRoomSelectVisible() {
        edittext_add_device_input_name.setHint("最多10个字");
        if (deviceType.equalsIgnoreCase(DeviceTypeConstant.TYPE.TYPE_AIR_REMOTECONTROL) ||
                deviceType.equalsIgnoreCase(DeviceTypeConstant.TYPE.TYPE_TV_REMOTECONTROL) ||
                deviceType.equalsIgnoreCase(DeviceTypeConstant.TYPE.TYPE_TVBOX_REMOTECONTROL)
                ) {
            layout_room_select.setVisibility(View.GONE);
        } else {
            layout_room_select.setVisibility(View.VISIBLE);
            showSelectRoomName();
        }
    }

    private void showSelectRoomName() {
        //get current room
        currentSelectedRoom = RoomManager.getInstance().getCurrentSelectedRoom();
        if (currentSelectedRoom != null) {
            textview_select_room_name.setText(currentSelectedRoom.getRoomName());
        } else {
            textview_select_room_name.setText("未选择");
        }
    }

    private void initDeviceListener() {
        mDeviceListener = new DeviceListener() {
            @Override
            public void responseAddDeviceHttpResult(DeviceOperationResponse responseBody) {
                super.responseAddDeviceHttpResult(responseBody);
                Log.i(TAG, responseBody.toString());
                addDeviceUid = responseBody.getUid();
                String deviceTypeHttp = responseBody.getDevice_type();
                Log.i(TAG, "deviceTypeHttp=" + deviceTypeHttp);
                SmartDev dbSmartDev = DataSupport.where("Uid = ?", addDeviceUid).findFirst(SmartDev.class);
                if (dbSmartDev != null) {
                    Ftoast.create(AddDeviceNameActivity.this).setText("已添加过设备:" + dbSmartDev.getName() + "与待添加设备冲突,添加失败").setDuration(Toast.LENGTH_SHORT).show();
                    return;
                }
                if (deviceTypeHttp == null) {
                    switch (deviceType) {
                        case DeviceTypeConstant.TYPE.TYPE_MENLING:
                            //SMART_BELL
                            SmartDev doorbeelDev = new SmartDev();
                            doorbeelDev.setUid(addDeviceUid);
                            doorbeelDev.setType(DeviceTypeConstant.TYPE.TYPE_MENLING);
                            deviceName = edittext_add_device_input_name.getText().toString();
                            if (deviceName.equalsIgnoreCase("")) {
                                deviceName = "智能门铃";
                            }
                            doorbeelDev.setStatus("在线");
                            doorbeelDev.setName(deviceName);
                            Message msg = Message.obtain();
                            boolean result = mDoorbeelManager.saveDoorbeel(doorbeelDev);
                            if (result) {
                                boolean updateRoomResult = mDoorbeelManager.
                                        updateDeviceInWhatRoom(currentSelectedRoom, addDeviceUid);
                                if (updateRoomResult) {
                                    startActivity(new Intent(AddDeviceNameActivity.this, DevicesActivity.class));
                                } else {
                                    msg.what = MSG_UPDATE_ROOM_FAIL;
                                    mHandler.sendMessage(msg);
                                }
                            } else {
                                msg = Message.obtain();
                                msg.what = MSG_ADD_DOORBEEL_FAIL;
                                mHandler.sendMessage(msg);
                            }
                            break;
                    }
                } else {
                    if (deviceTypeHttp.equalsIgnoreCase("LKSGW")) {
                        topic = responseBody.getTopic();
                        //如果有可用的网关
                        deviceName = edittext_add_device_input_name.getText().toString();
                        if (deviceName.equalsIgnoreCase("")) {
                            deviceName = "中继器";
                        }
                        mGetwayManager.addDBGetwayDevice(deviceName, addDeviceUid, topic);
                        mGetwayManager.updateGetwayDeviceInWhatRoom(currentSelectedRoom, addDeviceUid);
                        if (LocalConnectmanager.getInstance().isLocalconnectAvailable()) {
                            mGetwayManager.bindDevice(addDeviceUid);
                        }
                        String mac = responseBody.getMac();
                        Log.i(TAG, "添加中继器,mac地址是:" + mac);
                        //提示连接
                        new AlertDialog(AddDeviceNameActivity.this).builder().setTitle("配置中继器")
                                .setMsg("中继器上电,然后手机连接中继器产生的WiFi(中继器的WiFi名称是A6-XXXX," +
                                        "或者是RE-XXXX),连接上中继器的WiFi后点击重新扫描")
                                .setPositiveButton("确认", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent intent = new Intent(AddDeviceNameActivity.this, ScanWifiListActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity(intent);
                                    }
                                }).setNegativeButton("跳过", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mHandler.sendEmptyMessage(MSG_BIND_DEVICE_RESPONSE);
                            }
                        }).show();
                    } else if (deviceTypeHttp.equalsIgnoreCase("LKRT")) {
                        //去掉重名的路由器
                        deviceName = edittext_add_device_input_name.getText().toString();
                        if (deviceName.equalsIgnoreCase("")) {
                            deviceName = "路由器";
                        }
                        SmartDev currentAddRouter = new SmartDev();
                        currentAddRouter.setName(deviceName);
                        currentAddRouter.setUid(addDeviceUid);
                        currentAddRouter.setType(DeviceTypeConstant.TYPE.TYPE_ROUTER);
                        //查询设备列表，sn和上传时一样才修改名字
                        Router router = new Router();
                        router.setSmartDev(currentAddRouter);
                        router.setSign_seed(responseBody.getSign_seed());
                        router.setSignature(responseBody.getSignature());
                        if (responseBody.getChannels().getPrimary() != null) {
                            router.setChannels(responseBody.getChannels().getPrimary().getSub());
                            router.setReceveChannels(responseBody.getChannels().getPrimary().getPub());
                            router.save();
                        }
                        if (responseBody.getChannels().getSecondary() != null) {
                            router.setChannels(responseBody.getChannels().getSecondary().getSub());
                            router.setReceveChannels(responseBody.getChannels().getSecondary().getPub());
                            router.save();
                        }
                        currentAddRouter.setRouter(router);
                        boolean saveResult = mRouterManager.saveRouter(currentAddRouter);
                        if (saveResult) {
                            Room room = RoomManager.getInstance().getCurrentSelectedRoom();
                            boolean result = mRouterManager.updateDeviceInWhatRoom(room, addDeviceUid, deviceName);
                            if (result) {
                                Message msg = Message.obtain();
                                msg.what = MSG_ADD_ROUTER_SUCCESS;
                                mHandler.sendMessage(msg);
                            }
                        }
                    } else if (deviceTypeHttp.equalsIgnoreCase("SMART_BELL")) {
                        //SMART_BELL
                        SmartDev doorbeelDev = new SmartDev();
                        doorbeelDev.setUid(addDeviceUid);
                        doorbeelDev.setType(DeviceTypeConstant.TYPE.TYPE_MENLING);
                        doorbeelDev.setName(deviceName);
                        doorbeelDev.setStatus("在线");
                        Message msg = Message.obtain();
                        boolean result = mDoorbeelManager.saveDoorbeel(doorbeelDev);
                        if (result) {
                            boolean updateRoomResult = mDoorbeelManager.
                                    updateDeviceInWhatRoom(currentSelectedRoom, addDeviceUid);
                            if (updateRoomResult) {
                                startActivity(new Intent(AddDeviceNameActivity.this, DevicesActivity.class));
                            } else {
                                msg.what = MSG_UPDATE_ROOM_FAIL;
                                mHandler.sendMessage(msg);
                            }
                        } else {
                            msg = Message.obtain();
                            msg.what = MSG_ADD_DOORBEEL_FAIL;
                            mHandler.sendMessage(msg);
                        }
                    } else {
                        switch (deviceType) {
                            case DeviceTypeConstant.TYPE.TYPE_LOCK:
                                device.setTp(DeviceTypeConstant.TYPE.TYPE_LOCK);
                                mDeviceManager.addDBSmartDevice(device, addDeviceUid, currentSelectGetway);
                                mDeviceManager.updateSmartDeviceInWhatRoom(currentSelectedRoom, addDeviceUid, deviceName);
                                break;
                            case DeviceTypeConstant.TYPE.TYPE_REMOTECONTROL:
                                mDeviceManager.addDBSmartDevice(device, addDeviceUid, currentSelectGetway);
                                mDeviceManager.updateSmartDeviceInWhatRoom(currentSelectedRoom, addDeviceUid, deviceName);
                                break;
                            case DeviceTypeConstant.TYPE.TYPE_SWITCH:
                                mSmartSwitchManager.addDBSwitchDevice(device, addDeviceUid);
                                mSmartSwitchManager.updateSmartDeviceInWhatRoom(currentSelectedRoom, addDeviceUid, deviceName);

                                break;
                            case DeviceTypeConstant.TYPE.TYPE_LIGHT:
                                mSmartLightManager.addDBSwitchDevice(device, addDeviceUid);
                                mSmartLightManager.updateSmartDeviceRoomAndName(currentSelectedRoom, addDeviceUid, deviceName);
                                break;
                        }
                        if (LocalConnectmanager.getInstance().isLocalconnectAvailable()) {
                            mDeviceManager.bindSmartDevList(device);
                        } else {
                            deviceIntent = new Intent(AddDeviceNameActivity.this, DevicesActivity.class);
                            deviceIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(deviceIntent);
                        }
                    }
                }
            }

            @Override
            public void responseAddVirtualDeviceHttp(DeviceOperationResponse deviceOperationResponse) {
                super.responseAddVirtualDeviceHttp(deviceOperationResponse);
                addDeviceUid = deviceOperationResponse.getUid();
                if (deviceType.equalsIgnoreCase(DeviceTypeConstant.TYPE.TYPE_AIR_REMOTECONTROL)) {
                    // 绑定智能遥控,现在智能单个添加，这个不扫码的虚拟设备需要给他一个识别码
                    final SmartDev addDevice = new SmartDev();
                    addDevice.setType(DeviceTypeConstant.TYPE.TYPE_AIR_REMOTECONTROL);
                    addDevice.setUid(addDeviceUid);
                    addDevice.setName(deviceName);
                    addDevice.setRooms(currentSelectRemotecontrol.getRooms());
                    addDevice.setRemotecontrolUid(currentSelectRemotecontrol.getUid());
                    addDevice.setMac(currentSelectRemotecontrol.getMac());
                    Log.i(TAG, "currentSelectedRoom:" + (currentSelectedRoom != null));
                    boolean addresult = RemoteControlManager.getInstance().addDeviceDbLocal(addDevice, currentSelectedRoom);
                    if (addresult) {
                        new AlertDialog(AddDeviceNameActivity.this).builder().setTitle("配置遥控器")
                                .setMsg("遥控器未配置，是否现在配置遥控器")
                                .setPositiveButton("确认", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        RemoteControlManager.getInstance().setmSelectRemoteControlDevice(addDevice);
                                        RemoteControlManager.getInstance().setCurrentActionIsAddactionQuickLearn(true);
                                        startActivity(new Intent(AddDeviceNameActivity.this, ChooseBandActivity.class));
                                    }
                                }).setNegativeButton("跳过", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                startActivity(new Intent(AddDeviceNameActivity.this, DevicesActivity.class));
                            }
                        }).show();

                    }
                } else if (deviceType.equalsIgnoreCase(DeviceTypeConstant.TYPE.TYPE_TV_REMOTECONTROL)) {
                    // 绑定智能遥控,现在智能单个添加，这个不扫码的虚拟设备需要给他一个识别码
                    final SmartDev addDevice = new SmartDev();
                    addDevice.setType(DeviceTypeConstant.TYPE.TYPE_TV_REMOTECONTROL);
                    addDevice.setUid(addDeviceUid);
                    addDevice.setName(deviceName);
                    addDevice.setRooms(currentSelectRemotecontrol.getRooms());
                    addDevice.setRemotecontrolUid(currentSelectRemotecontrol.getUid());
                    addDevice.setMac(currentSelectRemotecontrol.getMac());
                    Log.i(TAG, "currentSelectedRoom:" + (currentSelectedRoom != null));
                    boolean addresult = RemoteControlManager.getInstance().addDeviceDbLocal(addDevice, currentSelectedRoom);
                    if (addresult) {
                        new AlertDialog(AddDeviceNameActivity.this).builder().setTitle("配置遥控器")
                                .setMsg("遥控器未配置，是否现在配置遥控器")
                                .setPositiveButton("确认", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        RemoteControlManager.getInstance().setmSelectRemoteControlDevice(addDevice);
                                        RemoteControlManager.getInstance().setCurrentActionIsAddactionQuickLearn(true);
                                        startActivity(new Intent(AddDeviceNameActivity.this, AddTvDeviceActivity.class));
                                    }
                                }).setNegativeButton("跳过", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                startActivity(new Intent(AddDeviceNameActivity.this, DevicesActivity.class));
                            }
                        }).show();
                    }
                } else if (deviceType.equalsIgnoreCase(DeviceTypeConstant.TYPE.TYPE_TVBOX_REMOTECONTROL)) {
                    final SmartDev addDevice = new SmartDev();
                    // 绑定智能遥控,现在智能单个添加，这个不扫码的虚拟设备需要给他一个识别码
                    SmartDev tvBoxDevice = new SmartDev();
                    tvBoxDevice.setType(DeviceTypeConstant.TYPE.TYPE_TVBOX_REMOTECONTROL);
                    tvBoxDevice.setUid(addDeviceUid);
                    tvBoxDevice.setName(deviceName);
                    addDevice.setRooms(currentSelectRemotecontrol.getRooms());
                    tvBoxDevice.setRemotecontrolUid(currentSelectRemotecontrol.getUid());
                    tvBoxDevice.setMac(currentSelectRemotecontrol.getMac().toLowerCase());
                    boolean addTvBoxresult = RemoteControlManager.getInstance().addDeviceDbLocal(tvBoxDevice, currentSelectedRoom);
                    if (addTvBoxresult) {
                        new AlertDialog(AddDeviceNameActivity.this).builder().setTitle("配置遥控器")
                                .setMsg("遥控器未配置，是否现在配置遥控器")
                                .setPositiveButton("确认", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        RemoteControlManager.getInstance().setmSelectRemoteControlDevice(addDevice);
                                        RemoteControlManager.getInstance().setCurrentActionIsAddactionQuickLearn(true);
                                        startActivity(new Intent(AddDeviceNameActivity.this, AddTopBoxActivity.class));
                                    }
                                }).setNegativeButton("跳过", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                startActivity(new Intent(AddDeviceNameActivity.this, DevicesActivity.class));
                            }
                        }).show();
                    }
                }
            }

            @Override
            public void responseBindDeviceResult(String result) {
                super.responseBindDeviceResult(result);
                Gson gson = new Gson();
                final DeviceList aDeviceList = gson.fromJson(result, DeviceList.class);
                boolean success;
                Log.i(TAG, "绑定结果 type=" + deviceType);
                success = isSmartDeviceAddSuccess(aDeviceList);
                Message msg = Message.obtain();
                msg.what = MSG_ADD_DEVICE_RESULT;
                msg.obj = success;
                mHandler.sendMessage(msg);
            }
        };
    }

    private void login() {
        String phoneNumber = Perfence.getPerfence(Perfence.PERFENCE_PHONE);
        String password = Perfence.getPerfence(Perfence.USER_PASSWORD);
        Log.i(TAG, "phoneNumber=" + phoneNumber + "password=" + password);
        if (!password.equals("")) {
            manager.login(phoneNumber, password);
        }
    }

    private void initMqttCallback() {
        DeplinkSDK.initSDK(getApplicationContext(), Perfence.SDK_APP_KEY);
        manager = DeplinkSDK.getSDKManager();
        ec = new EventCallback() {
            @Override
            public void onSuccess(SDKAction action) {
                switch (action) {
                    case LOGIN:
                        Perfence.setPerfence(AppConstant.ADDDOORBELLTIPSACTIVITY,false);
                        isUserLogin = true;
                        manager.connectMQTT(AddDeviceNameActivity.this);
                        String uuid = manager.getUserInfo().getUuid();
                        Log.i(TAG, "login mqtt success uuid=" + uuid);
                        Perfence.setPerfence(AppConstant.PERFENCE_BIND_APP_UUID, manager.getUserInfo().getUuid());
                        User user = manager.getUserInfo();
                        Perfence.setPerfence(Perfence.USER_PASSWORD, user.getPassword());
                        Perfence.setPerfence(Perfence.PERFENCE_PHONE, user.getName());
                        Perfence.setPerfence(AppConstant.USER.USER_GETIMAGE_FROM_SERVICE, false);
                        Perfence.setPerfence(AppConstant.USER_LOGIN, true);
                        break;
                }
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
            }
        };
    }

    /**
     * 显示当前添加的设备
     * 可以设置的布局
     */
    private void showSettinglayout() {
        if (deviceType.equals(DeviceTypeConstant.TYPE.TYPE_AIR_REMOTECONTROL) ||
                deviceType.equals(DeviceTypeConstant.TYPE.TYPE_TV_REMOTECONTROL) ||
                deviceType.equals(DeviceTypeConstant.TYPE.TYPE_TVBOX_REMOTECONTROL)
                ) {
            layout_remotecontrol_select.setVisibility(View.VISIBLE);
            layout_getway_select.setVisibility(View.GONE);
            if (mRemoteControls.size() > 0) {
                textview_select_remotecontrol_name.setText(mRemoteControls.get(0).getName());
                currentSelectRemotecontrol = mRemoteControls.get(0);
            } else {
                textview_select_remotecontrol_name.setText("未找到遥控器");
            }
        } else {
            if (deviceType.equals(DeviceTypeConstant.TYPE.TYPE_SMART_GETWAY)) {
                layout_remotecontrol_select.setVisibility(View.GONE);
                layout_getway_select.setVisibility(View.GONE);
            } else if (deviceType.equalsIgnoreCase(DeviceTypeConstant.TYPE.TYPE_ROUTER)) {
                layout_remotecontrol_select.setVisibility(View.GONE);
                layout_getway_select.setVisibility(View.GONE);
            } else if (deviceType.equalsIgnoreCase(DeviceTypeConstant.TYPE.TYPE_GETWAY_OR_ROUTER)) {
                layout_remotecontrol_select.setVisibility(View.GONE);
                layout_getway_select.setVisibility(View.GONE);
            } else if (deviceType.equalsIgnoreCase(DeviceTypeConstant.TYPE.TYPE_MENLING)) {
                layout_remotecontrol_select.setVisibility(View.GONE);
                layout_getway_select.setVisibility(View.GONE);
                imageview_doorbell_step.setVisibility(View.VISIBLE);
            } else {
                layout_remotecontrol_select.setVisibility(View.GONE);
                layout_getway_select.setVisibility(View.VISIBLE);
                if (mGetways.size() > 0) {
                    textview_select_getway_name.setText(mGetways.get(0).getName());
                    currentSelectGetway = mGetways.get(0);
                } else {
                    textview_select_getway_name.setText("未检测到网关");
                }
            }
        }
    }

    private static final int MSG_ADD_DEVICE_RESULT = 100;
    private static final int MSG_FINISH_ACTIVITY = 101;
    private static final int MSG_UPDATE_ROOM_FAIL = 102;
    private static final int MSG_ADD_DOORBEEL_FAIL = 103;
    private static final int MSG_BIND_DEVICE_RESPONSE = 105;
    public static final int MSG_ADD_ROUTER_SUCCESS = 106;
    private Intent deviceIntent;
    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_ADD_DEVICE_RESULT:
                    mHandler.sendEmptyMessageDelayed(MSG_FINISH_ACTIVITY, 1500);
                    break;
                case MSG_FINISH_ACTIVITY:
                    deviceIntent = new Intent(AddDeviceNameActivity.this, DevicesActivity.class);
                    deviceIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(deviceIntent);
                    break;
                case MSG_UPDATE_ROOM_FAIL:
                    Toast.makeText(AddDeviceNameActivity.this, "更新智能门铃所在房间失败", Toast.LENGTH_SHORT).show();
                    break;
                case MSG_ADD_DOORBEEL_FAIL:
                    Toast.makeText(AddDeviceNameActivity.this, "添加智能门铃失败", Toast.LENGTH_SHORT).show();
                    break;
                case MSG_BIND_DEVICE_RESPONSE:
                    startActivity(new Intent(AddDeviceNameActivity.this, DevicesActivity.class));
                    Toast.makeText(AddDeviceNameActivity.this, "添加中继器成功", Toast.LENGTH_SHORT).show();
                    break;
                case MSG_ADD_ROUTER_SUCCESS:
                    startActivity(new Intent(AddDeviceNameActivity.this, DevicesActivity.class));
                    Toast.makeText(AddDeviceNameActivity.this, "添加路由器成功", Toast.LENGTH_SHORT).show();
                    break;
            }
            return true;
        }
    };
    private Handler mHandler = new WeakRefHandler(mCallback);
    /**
     * 当前的网络情况
     */
    private int currentNetStatu = 4;
    public static final int NET_TYPE_WIFI_CONNECTED = 0;
    /**
     * WIFI不可用
     */
    public static final int NET_TYPE_WIFI_DISCONNECTED = 4;
    public void registerNetBroadcast(Context conext) {
        //注册网络状态监听
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        conext.registerReceiver(broadCast, filter);
    }

    public void unRegisterNetBroadcast(Context conext) {
        conext.unregisterReceiver(broadCast);
    }
    private AddDeviceNameActivity.NetBroadCast broadCast = new AddDeviceNameActivity.NetBroadCast();
    class NetBroadCast extends BroadcastReceiver {
        public final String ACTION = "android.net.conn.CONNECTIVITY_CHANGE";
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION.equals(intent.getAction())) {
                ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo activeInfo = manager.getActiveNetworkInfo();
                if (activeInfo != null && NetUtil.isWiFiActive(context)) {
                    if (NetUtil.isWiFiActive(context)) {
                        currentNetStatu = NET_TYPE_WIFI_CONNECTED;
                    } else {
                        currentNetStatu = NET_TYPE_WIFI_DISCONNECTED;
                    }
                    Log.i(TAG, "网络连接变化 currentNetStatu"+currentNetStatu);
                    if (currentNetStatu == NET_TYPE_WIFI_CONNECTED) {
                        //重新连接
                        login();
                    }
                }
            }
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        //这个界面不提示下线
        Perfence.setPerfence(AppConstant.ADDDOORBELLTIPSACTIVITY,true);
        isUserLogin = Perfence.getBooleanPerfence(AppConstant.USER_LOGIN);
        manager.addEventCallback(ec);
        mDeviceManager.addDeviceListener(mDeviceListener);
        mGetwayManager.addGetwayListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mDeviceManager.removeDeviceListener(mDeviceListener);
        mGetwayManager.removeGetwayListener(this);
        manager.removeEventCallback(ec);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unRegisterNetBroadcast(this);
    }

    private boolean isGetwayDeviceAddSuccess(DeviceList aDeviceList, String tempDeviceSn) {
        for (int i = 0; i < aDeviceList.getDevice().size(); i++) {
            if (aDeviceList.getDevice().get(i).getUid().equals(tempDeviceSn)) {
                return true;
            }
        }
        return false;
    }

    //网关管理接口
    @Override
    public void responseResult(String result) {
        Log.i(TAG, "绑定网关设备返回：" + result + "当前要绑定的是：");
        Gson gson = new Gson();
        boolean addDeviceSuccess;
        DeviceList mDeviceList = gson.fromJson(result, DeviceList.class);
        if (deviceType.equals(DeviceTypeConstant.TYPE.TYPE_SMART_GETWAY)) {
            addDeviceSuccess = isGetwayDeviceAddSuccess(mDeviceList, currentAddDevice);
            if (addDeviceSuccess) {
                mHandler.sendEmptyMessage(MSG_BIND_DEVICE_RESPONSE);
            }
        }
    }

    @Override
    public void responseDeleteDeviceHttpResult(DeviceOperationResponse result) {
    }

    @Override
    public void responseSetWifirelayResult(int result) {
    }

    private boolean isSmartDeviceAddSuccess(DeviceList aDeviceList) {
        boolean result = false;
        for (int i = 0; i < aDeviceList.getSmartDev().size(); i++) {
            Log.i(TAG, "isSmartDeviceAddSuccess Uid()=" + aDeviceList.getSmartDev().get(i).getUid() + "device.getAd()=" + device.getAd());
            if (aDeviceList.getSmartDev().get(i).getSmartUid().equalsIgnoreCase(device.getAd())) {
                result = true;
            }
        }
        return result;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_add_device_sure:
                if (!isUserLogin) {
                    Ftoast.create(this).setText("用户未登录").setDuration(Toast.LENGTH_SHORT).show();
                    return;
                }
                DeviceAddBody deviceAddBody = new DeviceAddBody();
                VirtualDeviceAddBody virtualDeviceAddBody = new VirtualDeviceAddBody();
                deviceName = edittext_add_device_input_name.getText().toString();
                Gson gson = new Gson();
                switch (deviceType) {
                    case DeviceTypeConstant.TYPE.TYPE_LOCK:
                        if (deviceName.equals("")) {
                            deviceName = "我家的门锁";
                        }
                        addSmartDevice(deviceAddBody, gson);
                        break;
                    case DeviceTypeConstant.TYPE.TYPE_LIGHT:
                        if (deviceName.equals("")) {
                            deviceName = "我家的灯泡";
                        }
                        addSmartDevice(deviceAddBody, gson);
                        break;
                    case DeviceTypeConstant.TYPE.TYPE_SWITCH:
                        if (deviceName.equals("")) {
                            deviceName = "智能开关";
                        }
                        Log.i(TAG, "智能开关二维码=" + switchqrcode);
                        currentAddDevice = switchqrcode;
                        addSmartDevice(deviceAddBody, gson);
                        break;
                    case "IRMOTE_V2":
                        if (deviceName.equals("")) {
                            deviceName = "智能遥控";
                        }
                        addSmartDevice(deviceAddBody, gson);
                        break;
                    case DeviceTypeConstant.TYPE.TYPE_AIR_REMOTECONTROL:
                        if (deviceName.equals("")) {
                            deviceName = "智能空调";
                        }
                        boolean isRemoteControlAdded = RemoteControlManager.getInstance().judgAirconditionDeviceisAdded(deviceName);
                        if (isRemoteControlAdded) {
                            Ftoast.create(this).setText("已存在相同名称的空调遥控器").setDuration(Toast.LENGTH_SHORT).show();
                            return;
                        }
                        addVirtualDevice(virtualDeviceAddBody, DeviceTypeConstant.TYPE.TYPE_AIR_REMOTECONTROL);
                        break;
                    case DeviceTypeConstant.TYPE.TYPE_TV_REMOTECONTROL:
                        if (deviceName.equals("")) {
                            deviceName = "智能电视";
                        }
                        boolean isTvAdded = RemoteControlManager.getInstance().judgTvDeviceisAdded(deviceName);
                        if (isTvAdded) {
                            Ftoast.create(this).setText("已存在相同名称的电视遥控器").setDuration(Toast.LENGTH_SHORT).show();
                            return;
                        }

                        addVirtualDevice(virtualDeviceAddBody, DeviceTypeConstant.TYPE.TYPE_TV_REMOTECONTROL);
                        break;
                    case DeviceTypeConstant.TYPE.TYPE_TVBOX_REMOTECONTROL:
                        if (deviceName.equals("")) {
                            deviceName = "智能机顶盒遥控";
                        }
                        boolean isTvBoxAdded = RemoteControlManager.getInstance().judgTvBoxDeviceisAdded(deviceName);
                        if (isTvBoxAdded) {
                            Ftoast.create(this).setText("已存在相同名称的电视机顶盒遥控器").setDuration(Toast.LENGTH_SHORT).show();
                            return;
                        }

                        addVirtualDevice(virtualDeviceAddBody, DeviceTypeConstant.TYPE.TYPE_TVBOX_REMOTECONTROL);

                        break;
                    case DeviceTypeConstant.TYPE.TYPE_MENLING:
                        if (device == null) {
                            device = new QrcodeSmartDevice();
                            device.setVer("1");
                        }
                        if (deviceName.equals("")) {
                            deviceName = "智能门铃";
                        }
                        addDoorBeelDevice(deviceAddBody);

                        break;
                    case DeviceTypeConstant.TYPE.TYPE_SMART_GETWAY:
                    case DeviceTypeConstant.TYPE.TYPE_GETWAY_OR_ROUTER:
                    case DeviceTypeConstant.TYPE.TYPE_ROUTER:
                        addGatwayDevice(deviceAddBody);
                        break;
                }
                break;
            case R.id.layout_room_select:
                Intent intent = new Intent(this, SelectRommActivity.class);
                intent.putExtra("DeviceType", deviceType);
                intent.putExtra("currentAddDevice", currentAddDevice);
                startActivityForResult(intent, REQUEST_CODE_SELECT_DEVICE_IN_WHAT_ROOM);
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
            case R.id.layout_remotecontrol_select:
                if (layout_remotecontrol_list.getVisibility() == View.VISIBLE) {
                    layout_remotecontrol_list.setVisibility(View.GONE);
                    imageview_remotecontrol_arror_right.setImageResource(R.drawable.gotoicon);
                } else {
                    layout_remotecontrol_list.setVisibility(View.VISIBLE);
                    imageview_remotecontrol_arror_right.setImageResource(R.drawable.nextdirectionicon);
                }
                break;
            case R.id.framelayout_back:
                AddDeviceNameActivity.this.onBackPressed();
                break;
            case R.id.framelayout_x:
                 intent=new Intent(AddDeviceNameActivity.this,AddDeviceQRcodeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                break;
        }
    }

    private void addDoorBeelDevice(DeviceAddBody deviceAddBody) {
        if (!NetUtil.isNetAvailable(this)) {
            Ftoast.create(this).setText("网络连接不可用,请重新连接上网络").setDuration(Toast.LENGTH_SHORT).show();
            startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
        }
        deviceAddBody.setDevice_name(deviceName);
        if (mRoomManager.getCurrentSelectedRoom() != null) {
            deviceAddBody.setRoom_uid(mRoomManager.getCurrentSelectedRoom().getUid());
        }
        deviceAddBody.setDevice_type("SMART_BELL");
        deviceAddBody.setMac(mDoorbeelManager.getMac());
        mDeviceManager.addDeviceHttp(deviceAddBody);
    }

    /**
     * 添加智能设备
     *
     * @param deviceAddBody
     * @param gson
     */
    private void addSmartDevice(DeviceAddBody deviceAddBody, Gson gson) {
        device = gson.fromJson(currentAddDevice, QrcodeSmartDevice.class);
        Log.i(TAG, "deviceType=" + deviceType + "device=" + (device != null));
        switch (deviceType) {
            case DeviceTypeConstant.TYPE.TYPE_LIGHT:
                if (!device.getTp().equalsIgnoreCase("YWLIGHTCONTROL")) {
                    Ftoast.create(this).setText("请选择正确的设备类型然后添加").setDuration(Toast.LENGTH_SHORT).show();
                    return;
                }
                break;
            case DeviceTypeConstant.TYPE.TYPE_LOCK:
                if (!device.getTp().equalsIgnoreCase("SMART_LOCK")) {
                    Ftoast.create(this).setText("请选择正确的设备类型然后添加").setDuration(Toast.LENGTH_SHORT).show();
                    return;
                }
                break;
            case DeviceTypeConstant.TYPE.TYPE_SWITCH:
                if (!device.getTp().equalsIgnoreCase("SmartWallSwitch4")) {
                    Ftoast.create(this).setText("请选择正确的设备类型然后添加").setDuration(Toast.LENGTH_SHORT).show();
                    return;
                }
                break;
            case DeviceTypeConstant.TYPE.TYPE_REMOTECONTROL:
                if (!device.getTp().equalsIgnoreCase("IRMOTE_V2")) {
                    Ftoast.create(this).setText("请选择正确的设备类型然后添加").setDuration(Toast.LENGTH_SHORT).show();
                    return;
                }
                break;
        }
        device.setName(deviceName);
        deviceAddBody.setDevice_name(deviceName);
        if (mRoomManager.getCurrentSelectedRoom() != null) {
            deviceAddBody.setRoom_uid(mRoomManager.getCurrentSelectedRoom().getUid());
        }
        if (mGetways.size() > 0) {
            deviceAddBody.setGw_uid(currentSelectGetway.getUid());
        }
        deviceAddBody.setDevice_type(device.getTp());
        deviceAddBody.setMac(device.getAd().toLowerCase());
        deviceAddBody.setSn(device.getSn());
        deviceAddBody.setOrg_code(device.getOrg());
        deviceAddBody.setVersion(device.getVer());
        mDeviceManager.addDeviceHttp(deviceAddBody);
    }

    /**
     * 添加虚拟设备
     *
     * @param deviceAddBody
     */
    private void addVirtualDevice(VirtualDeviceAddBody deviceAddBody, String deviceType) {
        if (currentSelectRemotecontrol == null) {
            Ftoast.create(this).setText("未添加智能遥控").setDuration(Toast.LENGTH_SHORT).show();
            return;
        }
        deviceAddBody.setDevice_name(deviceName);
        switch (deviceType) {
            case DeviceTypeConstant.TYPE.TYPE_AIR_REMOTECONTROL:
                deviceType = "IREMOTE_V2_AC";
                break;
            case DeviceTypeConstant.TYPE.TYPE_TV_REMOTECONTROL:
                deviceType = "IREMOTE_V2_TV";
                break;
            case DeviceTypeConstant.TYPE.TYPE_TVBOX_REMOTECONTROL:
                deviceType = "IREMOTE_V2_STB";
                break;
        }
        deviceAddBody.setDevice_type(deviceType);
        deviceAddBody.setIrmote_uid(currentSelectRemotecontrol.getUid());
        deviceAddBody.setIrmote_mac(currentSelectRemotecontrol.getMac());
        mDeviceManager.addVirtualDeviceHttp(deviceAddBody);
    }

    /**
     * 添加网关路由器设备
     *
     * @param deviceAddBody
     */
    private void addGatwayDevice(DeviceAddBody deviceAddBody) {
        deviceName = edittext_add_device_input_name.getText().toString();
        if (deviceName.equals("")) {
            deviceName = "中继器/路由器";
        }
        Log.i(TAG, "device.getSn()=" + currentAddDevice);
        deviceAddBody.setDevice_name(deviceName);
        if (mRoomManager.getCurrentSelectedRoom() != null) {
            deviceAddBody.setRoom_uid(mRoomManager.getCurrentSelectedRoom().getUid());
        }
        if (StringValidatorUtil.judgeContainsStr(currentAddDevice)) {
            deviceAddBody.setMac(currentAddDevice);
        } else {
            deviceAddBody.setSn(currentAddDevice);
        }
        mDeviceManager.addDeviceHttp(deviceAddBody);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SELECT_DEVICE_IN_WHAT_ROOM && resultCode == RESULT_OK) {
            String roomName = data.getStringExtra("roomName");
            Log.i(TAG, "roomName=" + roomName);
            textview_select_room_name.setText(roomName);
        }
    }
}
