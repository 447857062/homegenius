package com.deplink.homegenius.activity.device;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.deplink.homegenius.Protocol.json.Room;
import com.deplink.homegenius.Protocol.json.device.DeviceList;
import com.deplink.homegenius.Protocol.json.device.SmartDev;
import com.deplink.homegenius.Protocol.json.device.getway.GatwayDevice;
import com.deplink.homegenius.Protocol.json.device.router.Router;
import com.deplink.homegenius.Protocol.json.qrcode.QrcodeSmartDevice;
import com.deplink.homegenius.activity.device.adapter.GetwaySelectListAdapter;
import com.deplink.homegenius.activity.device.adapter.RemoteControlSelectListAdapter;
import com.deplink.homegenius.activity.device.getway.wifi.ScanWifiListActivity;
import com.deplink.homegenius.activity.device.remoteControl.airContorl.add.AirconditionChooseBandActivity;
import com.deplink.homegenius.activity.device.remoteControl.topBox.AddTopBoxActivity;
import com.deplink.homegenius.activity.device.remoteControl.tv.AddTvDeviceActivity;
import com.deplink.homegenius.activity.personal.login.LoginActivity;
import com.deplink.homegenius.constant.AppConstant;
import com.deplink.homegenius.constant.DeviceTypeConstant;
import com.deplink.homegenius.manager.connect.local.tcp.LocalConnectmanager;
import com.deplink.homegenius.manager.device.DeviceListener;
import com.deplink.homegenius.manager.device.DeviceManager;
import com.deplink.homegenius.manager.device.doorbeel.DoorbeelManager;
import com.deplink.homegenius.manager.device.getway.GetwayListener;
import com.deplink.homegenius.manager.device.getway.GetwayManager;
import com.deplink.homegenius.manager.device.light.SmartLightManager;
import com.deplink.homegenius.manager.device.remoteControl.RemoteControlManager;
import com.deplink.homegenius.manager.device.router.RouterManager;
import com.deplink.homegenius.manager.device.smartswitch.SmartSwitchManager;
import com.deplink.homegenius.manager.room.RoomManager;
import com.deplink.homegenius.util.NetUtil;
import com.deplink.homegenius.util.Perfence;
import com.deplink.homegenius.util.StringValidatorUtil;
import com.deplink.homegenius.util.WeakRefHandler;
import com.deplink.homegenius.view.combinationwidget.TitleLayout;
import com.deplink.homegenius.view.dialog.AlertDialog;
import com.deplink.homegenius.view.toast.ToastSingleShow;
import com.deplink.sdk.android.sdk.DeplinkSDK;
import com.deplink.sdk.android.sdk.EventCallback;
import com.deplink.sdk.android.sdk.SDKAction;
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
    private GetwaySelectListAdapter selectGetwayAdapter;
    private List<GatwayDevice> mGetways;
    private ListView listview_select_getway;
    private RemoteControlSelectListAdapter selectRemotecontrolAdapter;
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
    private TitleLayout layout_title;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_device_name);
        initViews();
        initDatas();
        initEvents();
    }

    private void initEvents() {
        button_add_device_sure.setOnClickListener(this);
        layout_getway_select.setOnClickListener(this);
        layout_room_select.setOnClickListener(this);
        layout_remotecontrol_select.setOnClickListener(this);
    }

    private void initViews() {
        layout_title = findViewById(R.id.layout_title);
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
    }

    private void initDatas() {
        initManager();
        //getintent data
        currentAddDevice = getIntent().getStringExtra("currentAddDevice");
        deviceType = getIntent().getStringExtra("DeviceType");
        switchqrcode = getIntent().getStringExtra("switchqrcode");
        setRoomSelectVisible();
        setDeviceNameDefault();
        layout_title.setReturnClickListener(new TitleLayout.ReturnImageClickListener() {
            @Override
            public void onBackPressed() {
                AddDeviceNameActivity.this.onBackPressed();
            }
        });
        mRemoteControls = new ArrayList<>();
        mGetways = new ArrayList<>();
        mGetways.addAll(GetwayManager.getInstance().getAllGetwayDevice());
        selectGetwayAdapter = new GetwaySelectListAdapter(this, mGetways);
        mRemoteControls.addAll(RemoteControlManager.getInstance().findAllRemotecontrolDevice());
        selectRemotecontrolAdapter = new RemoteControlSelectListAdapter(this, mRemoteControls);
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
        mRouterManager.InitRouterManager(this);
    }
    private String titleString;
    private void setDeviceNameDefault() {
        switch (deviceType) {
            case DeviceTypeConstant.TYPE.TYPE_LOCK:
                titleString="智能门锁";
                edittext_add_device_input_name.setText("智能门锁");
                edittext_add_device_input_name.setSelection(4);
                break;
            case "IRMOTE_V2":
                titleString="智能遥控";
                edittext_add_device_input_name.setText("智能遥控");
                edittext_add_device_input_name.setSelection(4);
                break;
            case DeviceTypeConstant.TYPE.TYPE_AIR_REMOTECONTROL:
                titleString="智能空调遥控器";
                edittext_add_device_input_name.setText("智能空调遥控器");
                edittext_add_device_input_name.setSelection(7);
                break;
            case DeviceTypeConstant.TYPE.TYPE_TV_REMOTECONTROL:
                titleString="智能电视遥控器";
                edittext_add_device_input_name.setText("智能电视遥控器");
                edittext_add_device_input_name.setSelection(7);
                break;
            case DeviceTypeConstant.TYPE.TYPE_TVBOX_REMOTECONTROL:
                titleString="智能机顶盒遥控";
                edittext_add_device_input_name.setText("智能机顶盒遥控");
                edittext_add_device_input_name.setSelection(7);
                break;
            case DeviceTypeConstant.TYPE.TYPE_SWITCH:
                titleString="智能开关";
                edittext_add_device_input_name.setText("智能开关");
                edittext_add_device_input_name.setSelection(4);
                break;
            case DeviceTypeConstant.TYPE.TYPE_MENLING:
                titleString="智能门铃";
                edittext_add_device_input_name.setText("智能门铃");
                edittext_add_device_input_name.setSelection(4);
                break;
            case DeviceTypeConstant.TYPE.TYPE_LIGHT:
                titleString="智能灯泡";
                edittext_add_device_input_name.setText("智能灯泡");
                edittext_add_device_input_name.setSelection(4);
                break;
            case DeviceTypeConstant.TYPE.TYPE_SMART_GETWAY:
                titleString="智能网关/路由器";
                edittext_add_device_input_name.setText("智能网关/路由器");
                edittext_add_device_input_name.setSelection(8);
                break;
        }
        layout_title.setTitleText(titleString);
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
            textview_select_room_name.setText("全部");
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
                Log.i(TAG,"deviceTypeHttp="+deviceTypeHttp);
                SmartDev dbSmartDev = DataSupport.where("Uid = ?", addDeviceUid).findFirst(SmartDev.class);
                if (dbSmartDev != null) {
                    ToastSingleShow.showText(AddDeviceNameActivity.this, "已添加过设备:" + dbSmartDev.getName() + "与待添加设备冲突,添加失败");
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
                         String mac=responseBody.getMac();
                        Log.i(TAG,"添加中继器,mac地址是:"+mac);
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
                                        startActivity(new Intent(AddDeviceNameActivity.this, AirconditionChooseBandActivity.class));
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
                new AlertDialog(AddDeviceNameActivity.this).builder().setTitle("账号异地登录")
                        .setMsg("当前账号已在其它设备上登录,是否重新登录")
                        .setPositiveButton("确认", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                startActivity(new Intent(AddDeviceNameActivity.this, LoginActivity.class));
                            }
                        }).setNegativeButton("取消", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                }).show();
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
            } else if (deviceType.equalsIgnoreCase(DeviceTypeConstant.TYPE.TYPE_MENLING)) {
                layout_remotecontrol_select.setVisibility(View.GONE);
                layout_getway_select.setVisibility(View.GONE);
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

    @Override
    protected void onResume() {
        super.onResume();
        manager.addEventCallback(ec);
        isUserLogin = Perfence.getBooleanPerfence(AppConstant.USER_LOGIN);
        mDeviceManager.addDeviceListener(mDeviceListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mDeviceManager.removeDeviceListener(mDeviceListener);
        manager.removeEventCallback(ec);
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
                    ToastSingleShow.showText(this, "用户未登录");
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
                            ToastSingleShow.showText(this, "已存在相同名称的空调遥控器");
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
                            ToastSingleShow.showText(this, "已存在相同名称的电视遥控器");
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
                            ToastSingleShow.showText(this, "已存在相同名称的电视机顶盒遥控器");
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
                        addGatwayDevice(deviceAddBody);
                        break;
                }
                break;
            case R.id.layout_room_select:
                Intent intent = new Intent(this, AddDeviceActivity.class);
                intent.putExtra("DeviceType",deviceType);
                intent.putExtra("currentAddDevice",currentAddDevice);
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
        }
    }

    private void addDoorBeelDevice(DeviceAddBody deviceAddBody) {
        if (!NetUtil.isNetAvailable(this)) {
            ToastSingleShow.showText(this, "网络连接不可用,请重新连接上网络");
            startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
        }
        deviceAddBody.setDevice_name(deviceName);
        if (mRoomManager.getCurrentSelectedRoom() != null) {
            deviceAddBody.setRoom_uid(mRoomManager.getCurrentSelectedRoom().getUid());
        }
        deviceAddBody.setDevice_type("SMART_BELL");
        deviceAddBody.setMac(mDoorbeelManager.getMac());
        //  deviceAddBody.setVersion(device.getVer());
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
            ToastSingleShow.showText(this, "未添加智能遥控");
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
