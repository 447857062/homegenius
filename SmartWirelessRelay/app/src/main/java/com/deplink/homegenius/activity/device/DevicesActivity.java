package com.deplink.homegenius.activity.device;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.deplink.homegenius.Protocol.json.OpResult;
import com.deplink.homegenius.Protocol.json.Room;
import com.deplink.homegenius.Protocol.json.device.DeviceList;
import com.deplink.homegenius.Protocol.json.device.SmartDev;
import com.deplink.homegenius.Protocol.json.device.getway.GatwayDevice;
import com.deplink.homegenius.Protocol.json.device.lock.UserIdInfo;
import com.deplink.homegenius.Protocol.json.device.router.Router;
import com.deplink.homegenius.Protocol.json.qrcode.QrcodeSmartDevice;
import com.deplink.homegenius.Protocol.packet.ellisdk.BasicPacket;
import com.deplink.homegenius.Protocol.packet.ellisdk.EllESDK;
import com.deplink.homegenius.Protocol.packet.ellisdk.EllE_Listener;
import com.deplink.homegenius.activity.device.adapter.DeviceListAdapter;
import com.deplink.homegenius.activity.device.doorbell.DoorbeelMainActivity;
import com.deplink.homegenius.activity.device.getway.GetwayDeviceActivity;
import com.deplink.homegenius.activity.device.light.LightActivity;
import com.deplink.homegenius.activity.device.remoteControl.airContorl.AirRemoteControlMianActivity;
import com.deplink.homegenius.activity.device.remoteControl.realRemoteControl.RemoteControlActivity;
import com.deplink.homegenius.activity.device.remoteControl.topBox.TvBoxMainActivity;
import com.deplink.homegenius.activity.device.remoteControl.tv.TvMainActivity;
import com.deplink.homegenius.activity.device.router.RouterMainActivity;
import com.deplink.homegenius.activity.device.smartSwitch.SwitchFourActivity;
import com.deplink.homegenius.activity.device.smartSwitch.SwitchOneActivity;
import com.deplink.homegenius.activity.device.smartSwitch.SwitchThreeActivity;
import com.deplink.homegenius.activity.device.smartSwitch.SwitchTwoActivity;
import com.deplink.homegenius.activity.device.smartlock.SmartLockActivity;
import com.deplink.homegenius.activity.homepage.SmartHomeMainActivity;
import com.deplink.homegenius.activity.personal.PersonalCenterActivity;
import com.deplink.homegenius.activity.personal.login.LoginActivity;
import com.deplink.homegenius.activity.room.RoomActivity;
import com.deplink.homegenius.application.AppManager;
import com.deplink.homegenius.constant.AppConstant;
import com.deplink.homegenius.constant.DeviceTypeConstant;
import com.deplink.homegenius.manager.connect.remote.HomeGenius;
import com.deplink.homegenius.manager.device.DeviceListener;
import com.deplink.homegenius.manager.device.DeviceManager;
import com.deplink.homegenius.manager.device.doorbeel.DoorbeelManager;
import com.deplink.homegenius.manager.device.getway.GetwayListener;
import com.deplink.homegenius.manager.device.getway.GetwayManager;
import com.deplink.homegenius.manager.device.light.SmartLightListener;
import com.deplink.homegenius.manager.device.light.SmartLightManager;
import com.deplink.homegenius.manager.device.remoteControl.RemoteControlListener;
import com.deplink.homegenius.manager.device.remoteControl.RemoteControlManager;
import com.deplink.homegenius.manager.device.router.RouterManager;
import com.deplink.homegenius.manager.device.smartlock.SmartLockListener;
import com.deplink.homegenius.manager.device.smartlock.SmartLockManager;
import com.deplink.homegenius.manager.device.smartswitch.SmartSwitchListener;
import com.deplink.homegenius.manager.device.smartswitch.SmartSwitchManager;
import com.deplink.homegenius.manager.room.RoomManager;
import com.deplink.homegenius.util.DataExchange;
import com.deplink.homegenius.util.Perfence;
import com.deplink.homegenius.util.WeakRefHandler;
import com.deplink.homegenius.view.dialog.AlertDialog;
import com.deplink.homegenius.view.dialog.devices.DeviceAtRoomDialog;
import com.deplink.sdk.android.sdk.DeplinkSDK;
import com.deplink.sdk.android.sdk.EventCallback;
import com.deplink.sdk.android.sdk.SDKAction;
import com.deplink.sdk.android.sdk.homegenius.DeviceOperationResponse;
import com.deplink.sdk.android.sdk.homegenius.Deviceprops;
import com.deplink.sdk.android.sdk.manager.SDKManager;
import com.deplink.sdk.android.sdk.mqtt.MQTTController;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import deplink.com.smartwirelessrelay.homegenius.EllESDK.R;

public class DevicesActivity extends Activity implements View.OnClickListener, GetwayListener, EllE_Listener, SmartLightListener, SmartLockListener {
    private static final String TAG = "DevicesActivity";
    //底部一排导航栏
    private LinearLayout layout_home_page;
    private LinearLayout layout_devices;
    private LinearLayout layout_rooms;
    private LinearLayout layout_personal_center;
    private ImageView imageview_devices;
    private ImageView imageview_home_page;
    private ImageView imageview_rooms;
    private ImageView imageview_personal_center;
    private TextView textview_home;
    private TextView textview_device;
    private TextView textview_room;
    private TextView textview_mine;
    //设备列表下拉刷新
    private PullToRefreshListView listview_devies;
    private DeviceListAdapter mDeviceAdapter;
    /**
     * 上面半部分列表的数据
     */
    private List<GatwayDevice> datasTop;
    /**
     * 下面半部分列表的数据
     */
    private List<SmartDev> datasBottom;
    //添加设备按钮
    private ImageView imageview_add_device;
    //选择房间,显示当前选中的房间有哪些设备
    private LinearLayout layout_select_room_type;
    //选中房间,按照房间过滤显示设备的对话框
    private DeviceAtRoomDialog roomTypeDialog;
    private boolean isRoomfilter;
    private int filterPosition;
    //当前选中房间的名称
    private TextView textview_room_name;
    //所有房间的名称
    private List<String> mRooms = new ArrayList<>();
    private ScrollView layout_empty_view_scroll;
    private SDKManager manager;
    private EventCallback ec;
    private DeviceListener mDeviceListener;
    private GetwayManager mGetwayManager;
    private DeviceManager mDeviceManager;
    private SmartLockManager mSmartLockManager;
    private RoomManager mRoomManager;
    private SmartSwitchManager mSmartSwitchManager;
    private RemoteControlManager mRemoteControlManager;
    private SmartLightManager mSmartLightManager;
    private RemoteControlListener mRemoteControlListener;
    private DoorbeelManager mDoorbeelManager;
    private Timer refreshTimer = null;
    private TimerTask refreshTask = null;
    private static final int TIME_DIFFERENCE_BETWEEN_MESSAGE_INTERVALS = 10000;
    private boolean isUserLogin;
    private HomeGenius mHomeGenius;
    private SmartDev routerDevices;
    private GatwayDevice getwayDevices;
    private int refreshCount = 0;
    private int refreshCountGetway = 0;
    /**
     * 设备上线下线监控，2次发送没有回数据就认为设备下线,所以是大于1
     */
    private static final int TIME_OUT_WATCHDOG_MAXCOUNT = 1;
    //路由器是否订阅过,用于界面初始化后,订阅mqtt通道,查询设备在线状态
    private boolean isSubscribe = false;
    private SmartDev currentSwitchDev;
    private SmartDev currentLightDev;
    private SmartDev currentLockDev;
    private SmartDev currentRemotecontrolDev;
    private String lastSwitchState;
    private String lastremoteControlState="离线";
    private String lastRouterStatu;
    private String lastGetwayStatu;
    private SmartSwitchListener mSmartSwitchListener;
    private  SmartDev currentSmartDoorBell;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devices);
        initViews();
        initDatas();
        initEvents();
    }
    private void stopTimer() {
        if (refreshTask != null) {
            refreshTask.cancel();
            refreshTask = null;
        }
        if (refreshTimer != null) {
            refreshTimer.cancel();
            refreshTimer = null;
        }
    }
    private void startTimer(final List<Deviceprops> devices) {
        if (refreshTimer == null) {
            refreshTimer = new Timer();
        }
        if (refreshTask == null) {
            refreshTask = new TimerTask() {
                @Override
                public void run() {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            updateDeviceOnlineStatu(devices);
                            notifyDeviceListView();
                        }
                    });
                }
            };
        }
        if (refreshTimer != null) {
            //10秒钟发一次查询的命令
            try {
                refreshTimer.schedule(refreshTask, 0, TIME_DIFFERENCE_BETWEEN_MESSAGE_INTERVALS);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    private static final int MSG_UPDATE_LOCAL_DEVS = 0x01;
    private static final int MSG_GET_DEVS_HTTPS = 0x02;
    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_UPDATE_LOCAL_DEVS:
                    String str = (String) msg.obj;
                    Log.i(TAG, "设备列表=" + str);
                    List<GatwayDevice> tempDevice = new ArrayList<>();
                    List<SmartDev> tempSmartDevice = new ArrayList<>();
                    parseLocalReturnDeviceList(str, tempDevice, tempSmartDevice);
                    gatwayDevcieBindLocal(tempDevice);
                    smartDeviceBindLocal(tempSmartDevice);
                    //查询虚拟设备
                    mRemoteControlManager.queryVirtualDeviceList();
                    virtualDeviceUpdate();
                    break;
                case MSG_GET_DEVS_HTTPS:
                    List<Deviceprops> devices;
                    devices = (List<Deviceprops>) msg.obj;
                    List<SmartDev> dbSmartDev = saveSmartDevices(devices);
                    syncSmartDevices(devices, dbSmartDev);
                    List<GatwayDevice> dbGetwayDev = saveGatwayDevices(devices);
                    SyncGatwayDevices(devices, dbSmartDev, dbGetwayDev);
                    startTimer(devices);
                    //如果网关不可用,就需要在这里查询虚拟设备.网关可用在本地查询后再查询虚拟设备
                    if (lastGetwayStatu.equalsIgnoreCase("离线")) {
                        mRemoteControlManager.queryVirtualDeviceList();
                    }
                    mDeviceManager.queryDeviceList();
                    break;
            }
            return true;
        }
    };
    /**
     * 更新智能设备的在线离线状态,不包含虚拟遥控器
     */
    private void updateDeviceOnlineStatu(List<Deviceprops> devices) {
        for (int i = 0; i < devices.size(); i++) {
            switch (devices.get(i).getDevice_type()) {
                case "LKRT":
                    routerDevices = DataSupport.where("Uid = ?", devices.get(i).getUid()).findFirst(SmartDev.class, true);
                    Router router = routerDevices.getRouter();
                    if (router != null) {
                        lastRouterStatu = routerDevices.getStatus();
                        if (router.getReceveChannels() != null) {
                            if (!isSubscribe) {
                                isSubscribe = true;
                                Log.i(TAG, "订阅路由器的通道");
                                MQTTController.getSingleton().subscribe(router.getReceveChannels(), manager.getmDeviceManager());
                            }
                            mHomeGenius.getReport(routerDevices.getRouter().getChannels());
                        }
                    }
                    if (refreshCount > TIME_OUT_WATCHDOG_MAXCOUNT) {
                        if (lastRouterStatu != null && lastRouterStatu.equalsIgnoreCase("在线")) {
                            lastRouterStatu = "离线";
                            ContentValues values = new ContentValues();
                            values.put("Status", "离线");
                            DataSupport.updateAll(SmartDev.class, values, "Uid=?", routerDevices.getUid());
                        }
                    } else {
                        refreshCount++;
                    }
                    break;
                case "LKSGW":
                    getwayDevices = DataSupport.where("Uid = ?", devices.get(i).getUid()).findFirst(GatwayDevice.class, true);
                    if (getwayDevices != null) {
                        lastGetwayStatu = getwayDevices.getStatus();
                        if (lastGetwayStatu == null) {
                            lastGetwayStatu = "离线";
                        }
                        mDeviceManager.queryDeviceList();
                    }
                    if (refreshCountGetway > TIME_OUT_WATCHDOG_MAXCOUNT) {
                        if (lastGetwayStatu.equalsIgnoreCase("在线") || lastGetwayStatu.equalsIgnoreCase("on")) {
                            lastGetwayStatu = "离线";
                            ContentValues values = new ContentValues();
                            values.put("Status", "离线");
                            DataSupport.updateAll(GatwayDevice.class, values, "Uid=?", getwayDevices.getUid());
                        }
                    } else {
                        refreshCountGetway++;
                    }
                    break;
                case "SmartWallSwitch1":
                    ///  break;
                case "SmartWallSwitch2":
                    // break;
                case "SmartWallSwitch3":
                    //break;
                case "SmartWallSwitch4":
                    currentSwitchDev = DataSupport.where("Uid = ?", devices.get(i).getUid()).findFirst(SmartDev.class, true);
                    mSmartSwitchManager.setCurrentSelectSmartDevice(currentSwitchDev);
                    mSmartSwitchManager.querySwitchStatus("query");
                    if (!gatwayAvailable()) {
                        if (lastSwitchState != null && lastSwitchState.equalsIgnoreCase("在线")) {
                            lastSwitchState = "离线";
                            currentSwitchDev.setStatus(lastSwitchState);
                            currentSwitchDev.saveFast();
                        }
                    }
                    break;
                case "IRMOTE_V2":
                    currentRemotecontrolDev = DataSupport.where("Uid = ?", devices.get(i).getUid()).findFirst(SmartDev.class, true);
                    if (currentRemotecontrolDev != null) {
                        mRemoteControlManager.setmSelectRemoteControlDevice(currentRemotecontrolDev);
                        mRemoteControlManager.queryStatu();
                        if (lastremoteControlState.equalsIgnoreCase("在线")) {
                            if (!gatwayAvailable()) {
                                lastremoteControlState = "离线";
                                currentRemotecontrolDev.setStatus("离线");
                                currentRemotecontrolDev.saveFast();
                            }
                        }
                    }
                    break;
                case "SMART_LOCK":
                    currentLockDev = DataSupport.where("Uid = ?", devices.get(i).getUid()).findFirst(SmartDev.class, true);
                    if (currentLockDev != null) {
                        mSmartLockManager.setCurrentSelectLock(currentLockDev);
                        mSmartLockManager.queryLockStatu();
                        if (!gatwayAvailable()) {
                            currentLockDev.setStatus("离线");
                            currentLockDev.saveFast();
                        }
                    }
                    break;
                case "YWLIGHTCONTROL":
                    currentLightDev = DataSupport.where("Uid = ?", devices.get(i).getUid()).findFirst(SmartDev.class, true);
                    if (currentLightDev != null) {
                        mSmartLightManager.setCurrentSelectLight(currentLightDev);
                        mSmartLightManager.queryLightStatus();
                        if (!gatwayAvailable()) {
                            currentLightDev.setStatus("离线");
                            currentLightDev.saveFast();
                        }
                    }
                    break;
                case "SMART_BELL":
                     currentSmartDoorBell = DataSupport.where("Uid = ?", devices.get(i).getUid()).findFirst(SmartDev.class, true);
                    break;
            }
        }
    }
    /**
     * 有没有可用的网关
     * @return
     */
    private boolean gatwayAvailable() {
        //如果没有可用的网关,其它智能设备也设置为离线状态
        boolean gatwayAvailable = false;
        List<GatwayDevice> allGatways = DataSupport.findAll(GatwayDevice.class);
        for (int j = 0; j < allGatways.size(); j++) {
            if (allGatways.get(j).getStatus() != null && (allGatways.get(j).getStatus().equalsIgnoreCase("在线")
                    || (allGatways.get(j).getStatus().equalsIgnoreCase("ON")))) {
                gatwayAvailable = true;
            }
        }
        Log.i(TAG, "gatwayAvailable=" + gatwayAvailable);
        return gatwayAvailable;
    }

    private EllESDK ellESDK;

    @Override
    protected void onResume() {
        super.onResume();
        manager.addEventCallback(ec);
        mDeviceManager.addDeviceListener(mDeviceListener);
        mSmartLightManager.addSmartLightListener(this);
        mRemoteControlManager.addRemoteControlListener(mRemoteControlListener);
        mSmartLockManager.addSmartLockListener(this);
        mSmartSwitchManager.addSmartSwitchListener(mSmartSwitchListener);
        setButtomBarImageResource();
        isUserLogin = Perfence.getBooleanPerfence(AppConstant.USER_LOGIN);
        if (isUserLogin) {
            mDeviceManager.queryDeviceListHttp();
            mRoomManager.queryRooms();
            //查询门邻设备状态
            ellESDK.startSearchDevs();
        }else{
            new AlertDialog(DevicesActivity.this).builder().setTitle("账号登录")
                    .setMsg("未登录,是否立即登录")
                    .setPositiveButton("确认", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startActivity(new Intent(DevicesActivity.this, LoginActivity.class));
                        }
                    }).setNegativeButton("取消", new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            }).show();
        }
        if (lastGetwayStatu == null) {
            lastGetwayStatu = "离线";
        }
        notifyDeviceListView();
    }

    /**
     * 刷新列表
     */
    private void notifyDeviceListView() {
        datasTop.clear();
        datasBottom.clear();
        if (isRoomfilter) {
            Room room = mRoomManager.findRoom(mRooms.get(filterPosition), true);
            datasTop.addAll(room.getmGetwayDevices());
            datasBottom.addAll(room.getmDevices());
        } else {
            datasTop.addAll(GetwayManager.getInstance().getAllGetwayDevice());
            datasBottom.addAll(DataSupport.findAll(SmartDev.class, true));
        }
        mDeviceAdapter.setTopList(datasTop);
        mDeviceAdapter.setBottomList(datasBottom);
        mDeviceAdapter.notifyDataSetChanged();
        listview_devies.onRefreshComplete();
        listview_devies.setEmptyView(layout_empty_view_scroll);
    }

    private void setButtomBarImageResource() {
        textview_home.setTextColor(ContextCompat.getColor(this,R.color.line_clolor));
        textview_device.setTextColor(ContextCompat.getColor(this,R.color.room_type_text));
        textview_room.setTextColor(ContextCompat.getColor(this,R.color.line_clolor));
        textview_mine.setTextColor(ContextCompat.getColor(this,R.color.line_clolor));
        imageview_home_page.setImageResource(R.drawable.nocheckthehome);
        imageview_devices.setImageResource(R.drawable.checkthedevice);
        imageview_rooms.setImageResource(R.drawable.nochecktheroom);
        imageview_personal_center.setImageResource(R.drawable.nocheckthemine);
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopTimer();
        isSubscribe = false;
        manager.removeEventCallback(ec);
        refreshCountGetway = 0;
        mDeviceManager.removeDeviceListener(mDeviceListener);
        mSmartLockManager.removeSmartLockListener(this);
        mRemoteControlManager.removeRemoteControlListener(mRemoteControlListener);
        mSmartLightManager.removeSmartLightListener(this);
        mSmartSwitchManager.removeSmartSwitchListener(mSmartSwitchListener);
        ellESDK.stopSearchDevs();
    }

    private void initDatas() {
        initManager();
        mHomeGenius = new HomeGenius();
        mDoorbeelManager = DoorbeelManager.getInstance();
        roomTypeDialog = new DeviceAtRoomDialog(this, mRooms);
        mRooms.addAll(mRoomManager.getRoomNames());
        datasTop = new ArrayList<>();
        datasBottom = new ArrayList<>();
        mDeviceAdapter = new DeviceListAdapter(this, datasTop, datasBottom);
        listview_devies.getRefreshableView().setAdapter(mDeviceAdapter);
        listview_devies.getRefreshableView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                position -= 1;
                Log.i(TAG, "OnItemClickListener position=" + position);
                mDeviceManager.setStartFromExperience(false);
                if (datasTop.size() < (position + 1)) {
                    //智能设备
                    String deviceType = datasBottom.get(position - datasTop.size()).getType();
                    String deviceSubType = datasBottom.get(position - datasTop.size()).getSubType();
                    Log.i(TAG, "智能设备类型=" + deviceType);
                    mDeviceManager.setCurrentSelectSmartDevice(datasBottom.get(position - datasTop.size()));
                    switch (deviceType) {
                        case DeviceTypeConstant.TYPE.TYPE_LOCK:
                            //设置当前选中的门锁设备
                            mSmartLockManager.setCurrentSelectLock(datasBottom.get(position - datasTop.size()));
                            startActivity(new Intent(DevicesActivity.this, SmartLockActivity.class));
                            break;
                        case "IRMOTE_V2":
                        case DeviceTypeConstant.TYPE.TYPE_REMOTECONTROL:
                            RemoteControlManager.getInstance().setmSelectRemoteControlDevice(datasBottom.get(position - datasTop.size()));
                            startActivity(new Intent(DevicesActivity.this, RemoteControlActivity.class));
                            break;
                        case DeviceTypeConstant.TYPE.TYPE_AIR_REMOTECONTROL:
                            RemoteControlManager.getInstance().setmSelectRemoteControlDevice(datasBottom.get(position - datasTop.size()));
                            startActivity(new Intent(DevicesActivity.this, AirRemoteControlMianActivity.class));
                            break;
                        case DeviceTypeConstant.TYPE.TYPE_ROUTER:
                            RouterManager.getInstance().setCurrentSelectedRouter(datasBottom.get(position - datasTop.size()));
                            startActivity(new Intent(DevicesActivity.this, RouterMainActivity.class));
                            break;
                        case DeviceTypeConstant.TYPE.TYPE_TV_REMOTECONTROL:
                            RemoteControlManager.getInstance().setmSelectRemoteControlDevice(datasBottom.get(position - datasTop.size()));
                            startActivity(new Intent(DevicesActivity.this, TvMainActivity.class));
                            break;
                        case DeviceTypeConstant.TYPE.TYPE_TVBOX_REMOTECONTROL:
                            RemoteControlManager.getInstance().setmSelectRemoteControlDevice(datasBottom.get(position - datasTop.size()));
                            startActivity(new Intent(DevicesActivity.this, TvBoxMainActivity.class));
                            break;
                        case DeviceTypeConstant.TYPE.TYPE_SWITCH:
                            SmartSwitchManager.getInstance().setCurrentSelectSmartDevice(datasBottom.get(position - datasTop.size()));
                            switch (deviceSubType) {
                                case DeviceTypeConstant.TYPE_SWITCH_SUBTYPE.SUB_TYPE_SWITCH_ONEWAY:
                                    startActivity(new Intent(DevicesActivity.this, SwitchOneActivity.class));
                                    break;
                                case DeviceTypeConstant.TYPE_SWITCH_SUBTYPE.SUB_TYPE_SWITCH_TWOWAY:
                                    startActivity(new Intent(DevicesActivity.this, SwitchTwoActivity.class));
                                    break;
                                case DeviceTypeConstant.TYPE_SWITCH_SUBTYPE.SUB_TYPE_SWITCH_THREEWAY:
                                    startActivity(new Intent(DevicesActivity.this, SwitchThreeActivity.class));
                                    break;
                                case DeviceTypeConstant.TYPE_SWITCH_SUBTYPE.SUB_TYPE_SWITCH_FOURWAY:
                                    startActivity(new Intent(DevicesActivity.this, SwitchFourActivity.class));
                                    break;
                            }
                            break;
                        case DeviceTypeConstant.TYPE.TYPE_MENLING:
                            mDoorbeelManager.setCurrentSelectedDoorbeel(datasBottom.get(position - datasTop.size()));
                            startActivity(new Intent(DevicesActivity.this, DoorbeelMainActivity.class));
                            break;
                        case DeviceTypeConstant.TYPE.TYPE_LIGHT:
                            SmartLightManager.getInstance().setCurrentSelectLight(datasBottom.get(position - datasTop.size()));
                            startActivity(new Intent(DevicesActivity.this, LightActivity.class));
                            break;
                    }
                } else {
                    //网关设备
                    GetwayManager.getInstance().setCurrentSelectGetwayDevice(datasTop.get(position));
                    startActivity(new Intent(DevicesActivity.this, GetwayDeviceActivity.class));
                }
            }
        });
        initMqttCallback();
        initListener();
    }

    private void initListener() {
        ellESDK = EllESDK.getInstance();
        ellESDK.InitEllESDK(this, this);
        mDeviceListener = new DeviceListener() {
            @Override
            public void responseQueryResult(String result) {
                super.responseQueryResult(result);
                Log.i(TAG, "本地接口接收到网关消息:" + result);
                if (result.contains("DevList")) {
                    Message msg = Message.obtain();
                    msg.what = MSG_UPDATE_LOCAL_DEVS;
                    msg.obj = result;
                    mHandler.sendMessage(msg);
                }
            }

            @Override
            public void responseQueryHttpResult(List<Deviceprops> devices) {
                super.responseQueryHttpResult(devices);
                Message message = Message.obtain();
                message.obj = devices;
                message.what = MSG_GET_DEVS_HTTPS;
                mHandler.sendMessage(message);

            }
        };

        mRemoteControlListener = new RemoteControlListener() {
            @Override
            public void responseQueryVirtualDevices(List<DeviceOperationResponse> result) {
                super.responseQueryVirtualDevices(result);
                //保存虚拟设备
                for (int i = 0; i < result.size(); i++) {
                    saveVirtualDeviceToSqlite(result, i);
                }
            }
            @Override
            public void responseOnlineStatu(String result) {
                super.responseOnlineStatu(result);
                if(currentRemotecontrolDev!=null){
                    currentRemotecontrolDev.setStatus(result);
                    currentRemotecontrolDev.saveFast();
                }
            }
        };
        mSmartSwitchListener = new SmartSwitchListener() {
            @Override
            public void responseResult(String result) {
                Log.i(TAG, "开关本地状态反馈" + result);
                if (currentSwitchDev != null) {
                    currentSwitchDev.setStatus("在线");
                    currentSwitchDev.saveFast();
                }
                if (getwayDevices != null) {
                    ContentValues values = new ContentValues();
                    values.put("Status", "在线");
                    DataSupport.updateAll(GatwayDevice.class, values, "Uid=?", getwayDevices.getUid());
                }
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
            public void notifyHomeGeniusResponse(String result) {
                super.notifyHomeGeniusResponse(result);
                if (result.contains("DevList")) {
                    Log.i(TAG, "设备列表界面收到回调的mqtt消息=" + result);
                    Message msg = Message.obtain();
                    msg.what = MSG_UPDATE_LOCAL_DEVS;
                    msg.obj = result;
                    mHandler.sendMessage(msg);
                    //网关远程收到消息就在线了
                    refreshCountGetway = 0;
                    if (getwayDevices != null) {
                        getwayDevices.setStatus("在线");
                        getwayDevices.saveFast();
                    }
                } else {
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
                                currentRemotecontrolDev.setStatus("在线");
                                currentRemotecontrolDev.saveFast();
                                lastremoteControlState = "在线";
                                //网关远程收到消息就在线了
                                refreshCountGetway = 0;
                                if (getwayDevices != null) {
                                    ContentValues values = new ContentValues();
                                    values.put("Status", "在线");
                                    DataSupport.updateAll(GatwayDevice.class, values, "Uid=?", getwayDevices.getUid());
                                }
                                virtualDeviceUpdate();
                            } else if (content.getMethod().equalsIgnoreCase("SmartWallSwitch")) {
                                lastSwitchState = "在线";
                                currentSwitchDev.setStatus("在线");
                                currentSwitchDev.saveFast();
                                //网关远程收到消息就在线了
                                refreshCountGetway = 0;
                                if (getwayDevices != null) {
                                    ContentValues values = new ContentValues();
                                    values.put("Status", "在线");
                                    DataSupport.updateAll(GatwayDevice.class, values, "Uid=?", getwayDevices.getUid());
                                }
                            } else if (content.getMethod().equalsIgnoreCase("YWLIGHTCONTROL")) {

                                currentLightDev.setStatus("在线");
                                currentLightDev.saveFast();
                                //网关远程收到消息就在线了
                                refreshCountGetway = 0;
                                if (getwayDevices != null) {
                                    ContentValues values = new ContentValues();
                                    values.put("Status", "在线");
                                    DataSupport.updateAll(GatwayDevice.class, values, "Uid=?", getwayDevices.getUid());
                                }
                            } else if (content.getMethod().equalsIgnoreCase("SMART_LOCK")) {
                                currentLockDev.setStatus("在线");
                                currentLockDev.saveFast();
                                //网关远程收到消息就在线了
                                refreshCountGetway = 0;
                                if (getwayDevices != null) {
                                    ContentValues values = new ContentValues();
                                    values.put("Status", "在线");
                                    DataSupport.updateAll(GatwayDevice.class, values, "Uid=?", getwayDevices.getUid());
                                }
                            } else if (content.getMethod().equalsIgnoreCase("PERFORMANCE")) {
                                refreshCount = 0;
                                if (lastRouterStatu == null) {
                                    lastRouterStatu = "离线";
                                }
                                if (lastRouterStatu.equalsIgnoreCase("离线")) {
                                    routerDevices.setStatus("在线");
                                    routerDevices.saveFast();
                                    lastRouterStatu = "在线";
                                    notifyDeviceListView();
                                }
                            }
                        }
                    }
                }
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
                new AlertDialog(DevicesActivity.this).builder().setTitle("账号异地登录")
                        .setMsg("当前账号已在其它设备上登录,是否重新登录")
                        .setPositiveButton("确认", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                startActivity(new Intent(DevicesActivity.this, LoginActivity.class));
                            }
                        }).setNegativeButton("取消", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                }).show();
            }
        };
    }

    private void initManager() {
        mSmartLockManager = SmartLockManager.getInstance();
        mSmartLockManager.InitSmartLockManager(DevicesActivity.this);
        mDeviceManager = DeviceManager.getInstance();
        mDeviceManager.InitDeviceManager(this);
        mRoomManager = RoomManager.getInstance();
        mRoomManager.initRoomManager(this);
        RouterManager mRouterManager = RouterManager.getInstance();
        mRouterManager.InitRouterManager(DevicesActivity.this);
        mGetwayManager = GetwayManager.getInstance();
        mGetwayManager.InitGetwayManager(this, this);
        mRemoteControlManager = RemoteControlManager.getInstance();
        mRemoteControlManager.InitRemoteControlManager(this);
        mSmartSwitchManager = SmartSwitchManager.getInstance();
        mSmartSwitchManager.InitSmartSwitchManager(this);
        mSmartLightManager = SmartLightManager.getInstance();
        mSmartLightManager.InitSmartLightManager(this);
    }

    private void initEvents() {
        AppManager.getAppManager().addActivity(this);
        layout_home_page.setOnClickListener(this);
        layout_devices.setOnClickListener(this);
        layout_rooms.setOnClickListener(this);
        layout_personal_center.setOnClickListener(this);
        imageview_add_device.setOnClickListener(this);
        layout_select_room_type.setOnClickListener(this);
        listview_devies.getRefreshableView().setSelector(android.R.color.transparent);
        listview_devies.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
        // 设置上下刷新 使用 OnRefreshListener2
        listview_devies.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                listview_devies.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mDeviceManager.queryDeviceList();
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                listview_devies.onRefreshComplete();
                            }
                        }, 3000);

                    }
                }, 500);
            }
            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {

            }
        });
    }

    private void initViews() {
        layout_home_page = findViewById(R.id.layout_home_page);
        layout_devices = findViewById(R.id.layout_devices);
        layout_rooms = findViewById(R.id.layout_rooms);
        layout_personal_center = findViewById(R.id.layout_personal_center);
        listview_devies = findViewById(R.id.listview_devies);
        imageview_add_device = findViewById(R.id.imageview_add_device);
        layout_select_room_type = findViewById(R.id.layout_select_room_type);
        imageview_devices = findViewById(R.id.imageview_devices);
        layout_empty_view_scroll = findViewById(R.id.layout_empty_view_scroll);
        imageview_home_page = findViewById(R.id.imageview_home_page);
        imageview_rooms = findViewById(R.id.imageview_rooms);
        imageview_personal_center = findViewById(R.id.imageview_personal_center);
        textview_home = findViewById(R.id.textview_home);
        textview_device = findViewById(R.id.textview_device);
        textview_room = findViewById(R.id.textview_room);
        textview_mine = findViewById(R.id.textview_mine);
        textview_room_name = findViewById(R.id.textview_room_name);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.layout_home_page:
                startActivity(new Intent(this, SmartHomeMainActivity.class));
                break;
            case R.id.layout_select_room_type:
                showRoomFilterSelected();
                break;
            case R.id.layout_rooms:
                startActivity(new Intent(this, RoomActivity.class));
                break;
            case R.id.layout_personal_center:
                startActivity(new Intent(this, PersonalCenterActivity.class));
                break;
            case R.id.imageview_add_device:
                startActivity(new Intent(this, AddDeviceQRcodeActivity.class));
                break;
        }
    }


    /**
     * 按照房间过滤设备对话框
     */
    private void showRoomFilterSelected() {
        roomTypeDialog.setRoomTypeItemClickListener(new DeviceAtRoomDialog.onItemClickListener() {
            @Override
            public void onItemClicked(int position) {
                datasTop.clear();
                datasBottom.clear();
                mRooms.clear();
                mRoomManager.queryRooms();
                mRooms.addAll(mRoomManager.getRoomNames());
                if (mRooms.get(position).equals("全部")) {
                    datasTop.addAll(GetwayManager.getInstance().getAllGetwayDevice());
                    datasBottom.addAll(DataSupport.findAll(SmartDev.class, true));
                    mDeviceAdapter.setTopList(datasTop);
                    mDeviceAdapter.setBottomList(datasBottom);
                    isRoomfilter = false;
                } else {
                    Room room = mRoomManager.findRoom(mRooms.get(position), true);
                    //使用数据库中的数据
                    isRoomfilter = true;
                    filterPosition = position;
                    datasTop.addAll(room.getmGetwayDevices());
                    datasBottom.addAll(room.getmDevices());
                    mDeviceAdapter.setTopList(datasTop);
                    mDeviceAdapter.setBottomList(datasBottom);
                }
                mDeviceAdapter.notifyDataSetChanged();
                textview_room_name.setText(mRooms.get(position));
                roomTypeDialog.dismiss();
            }
        });
        roomTypeDialog.show();
    }

    @Override
    public void responseDeleteDeviceHttpResult(DeviceOperationResponse result) {

    }

    @Override
    public void responseResult(String result) {

    }

    @Override
    public void responseSetWifirelayResult(int result) {

    }

    /**
     * 保存网关设备到本地数据库
     *
     * @param devices
     * @param i
     */
    private void saveGetwayDeviceToSqlite(List<Deviceprops> devices, int i) {
        GatwayDevice dev = new GatwayDevice();
        String deviceType = devices.get(i).getDevice_type();
        dev.setType(deviceType);
        String deviceName = devices.get(i).getDevice_name();
        if (deviceType.equalsIgnoreCase("LKSWG") || deviceType.equalsIgnoreCase("LKSGW")) {
            if (deviceName == null || deviceName.equals("")) {
                dev.setName("中继器");
            } else {
                deviceName = deviceName.replace("/路由器", "");
                dev.setName(deviceName);
            }
            deviceType = DeviceTypeConstant.TYPE.TYPE_SMART_GETWAY;
            dev.setType(deviceType);
        }
        dev.setUid(devices.get(i).getUid());
        dev.setOrg(devices.get(i).getOrg_code());
        dev.setVer(devices.get(i).getVersion());
        dev.setTopic("device/" + devices.get(i).getUid() + "/sub");
        List<Room> rooms = new ArrayList<>();
        Room room = DataSupport.where("Uid=?", devices.get(i).getRoom_uid()).findFirst(Room.class);
        if (room != null) {
            rooms.add(room);
        } else {
            rooms.addAll(DataSupport.findAll(Room.class));
        }
        dev.setRoomList(rooms);
        boolean success = dev.save();
        Log.i(TAG, "保存设备:" + success + "deviceName=" + deviceName);
    }

    private void saveSmartDeviceToSqlite(List<Deviceprops> devices, int i) {
        SmartDev dev = new SmartDev();
        String deviceType = devices.get(i).getDevice_type();
        dev.setType(deviceType);
        String deviceName = devices.get(i).getDevice_name();
        if (deviceType.equalsIgnoreCase("SMART_LOCK")) {
            deviceType = DeviceTypeConstant.TYPE.TYPE_LOCK;
            dev.setType(deviceType);
            dev.setName(deviceName);
        } else if (deviceType.equalsIgnoreCase("IRMOTE_V2")) {
            deviceType = DeviceTypeConstant.TYPE.TYPE_REMOTECONTROL;
            dev.setType(deviceType);
            dev.setName(deviceName);
        } else if (deviceType.equalsIgnoreCase("SmartWallSwitch1")) {
            deviceType = DeviceTypeConstant.TYPE.TYPE_SWITCH;
            dev.setType(deviceType);
            dev.setName(deviceName);
            dev.setSubType(DeviceTypeConstant.TYPE_SWITCH_SUBTYPE.SUB_TYPE_SWITCH_ONEWAY);
        } else if (deviceType.equalsIgnoreCase("SmartWallSwitch2")) {
            deviceType = DeviceTypeConstant.TYPE.TYPE_SWITCH;
            dev.setType(deviceType);
            dev.setName(deviceName);
            dev.setSubType(DeviceTypeConstant.TYPE_SWITCH_SUBTYPE.SUB_TYPE_SWITCH_TWOWAY);
        } else if (deviceType.equalsIgnoreCase("SmartWallSwitch3")) {
            deviceType = DeviceTypeConstant.TYPE.TYPE_SWITCH;
            dev.setType(deviceType);
            dev.setName(deviceName);
            dev.setSubType(DeviceTypeConstant.TYPE_SWITCH_SUBTYPE.SUB_TYPE_SWITCH_THREEWAY);
        } else if (deviceType.equalsIgnoreCase("SmartWallSwitch4")) {
            deviceType = DeviceTypeConstant.TYPE.TYPE_SWITCH;
            dev.setType(deviceType);
            dev.setName(deviceName);
            dev.setSubType(DeviceTypeConstant.TYPE_SWITCH_SUBTYPE.SUB_TYPE_SWITCH_FOURWAY);
        } else if (deviceType.equalsIgnoreCase("YWLIGHTCONTROL")) {
            deviceType = DeviceTypeConstant.TYPE.TYPE_LIGHT;
            dev.setType(deviceType);
            dev.setName(deviceName);
        } else if (deviceType.equalsIgnoreCase("SMART_BELL")) {
            deviceType = DeviceTypeConstant.TYPE.TYPE_MENLING;
            dev.setType(deviceType);
            dev.setStatus("在线");
            dev.setName(deviceName);
        } else if (deviceType.equalsIgnoreCase("LKRT")) {
            deviceType = DeviceTypeConstant.TYPE.TYPE_ROUTER;
            dev.setType(deviceType);
            Router router = new Router();
            Log.i(TAG, "获取绑定的设备" + manager.getDeviceList().size());
            if (deviceName == null || deviceName.equals("")) {
                dev.setName("路由器");
            } else {
                dev.setName(deviceName);
            }
            router.setSign_seed(devices.get(i).getSign_seed());
            router.setSignature(devices.get(i).getSignature());
            router.setChannels(devices.get(i).getChannels().getSecondary().getSub());
            router.setReceveChannels(devices.get(i).getChannels().getSecondary().getPub());
            router.setSmartDev(dev);
            router.save();
            dev.setRouter(router);
        }
        GatwayDevice addGatwayDevice = null;
        String gw_uid = devices.get(i).getGw_uid();
        if (gw_uid != null) {
            dev.setGetwayDeviceUid(gw_uid);
            addGatwayDevice = DataSupport.where("uid=?", gw_uid).findFirst(GatwayDevice.class);
        }
        if (addGatwayDevice != null) {
            dev.setGetwayDevice(addGatwayDevice);
        }
        dev.setUid(devices.get(i).getUid());
        dev.setOrg(devices.get(i).getOrg_code());
        dev.setVer(devices.get(i).getVersion());
        dev.setMac(devices.get(i).getMac().toLowerCase());
        List<Room> rooms = new ArrayList<>();
        Room room = DataSupport.where("Uid=?", devices.get(i).getRoom_uid()).findFirst(Room.class);
        if (room != null) {
            rooms.add(room);
            dev.setRooms(rooms);
        } else {
            dev.setRooms(mRoomManager.queryRooms());
        }
        dev.saveFast();
    }

    /**
     * 保存虚拟设备
     *
     * @param devices
     * @param i
     */
    private void saveVirtualDeviceToSqlite(List<DeviceOperationResponse> devices, int i) {
        SmartDev dev = DataSupport.where("Uid=?", devices.get(i).getUid()).findFirst(SmartDev.class);
        if (dev == null) {
            dev = new SmartDev();
        }
        String deviceType = devices.get(i).getDevice_type();
        switch (deviceType) {
            case "IREMOTE_V2_AC":
                deviceType = DeviceTypeConstant.TYPE.TYPE_AIR_REMOTECONTROL;
                break;
            case "IREMOTE_V2_TV":
                deviceType = DeviceTypeConstant.TYPE.TYPE_TV_REMOTECONTROL;
                break;
            case "IREMOTE_V2_STB":
                deviceType = DeviceTypeConstant.TYPE.TYPE_TVBOX_REMOTECONTROL;
                break;
        }
        dev.setType(deviceType);
        String deviceName = devices.get(i).getDevice_name();
        dev.setName(deviceName);
        dev.setUid(devices.get(i).getUid());
        SmartDev realRc = DataSupport.where("Uid=?", devices.get(i).getIrmote_uid()).findFirst(SmartDev.class, true);
        if (realRc != null) {
            Log.i(TAG, "物理遥控器uid=" + devices.get(i).getIrmote_uid());
            //物理遥控器的房间就是虚拟遥控器的房间
            if (realRc.getRooms() != null) {
                dev.setRooms(realRc.getRooms());
            } else {
                dev.setRooms(RoomManager.getInstance().getmRooms());
            }
            if (realRc.getStatus() != null) {
                dev.setStatus(realRc.getStatus());
            }

        }
        dev.setRemotecontrolUid(devices.get(i).getIrmote_uid());
        dev.setMac(devices.get(i).getIrmote_mac());
        String key_codes = devices.get(i).getKey_codes();
        if (key_codes != null) {
            dev.setKey_codes(key_codes);
        }
        dev.save();
    }



    private void syncSmartDevices(List<Deviceprops> devices, List<SmartDev> dbSmartDev) {
        //本地数据库中有,http返回没有(设备在其他地方删除了,在这个设备需要同步服务器的)
        for (int j = 0; j < dbSmartDev.size(); j++) {
            boolean deleteDevice = true;
            for (int i = 0; i < devices.size(); i++) {
                if (dbSmartDev.get(j).getUid().equals(devices.get(i).getUid())) {
                    deleteDevice = false;
                }
            }
            if (dbSmartDev.get(j).getType().equalsIgnoreCase(DeviceTypeConstant.TYPE.TYPE_AIR_REMOTECONTROL) ||
                    dbSmartDev.get(j).getType().equalsIgnoreCase(DeviceTypeConstant.TYPE.TYPE_TV_REMOTECONTROL) ||
                    dbSmartDev.get(j).getType().equalsIgnoreCase(DeviceTypeConstant.TYPE.TYPE_TVBOX_REMOTECONTROL)
                    ) {
                deleteDevice = false;
            }
            if (deleteDevice) {
                Log.i(TAG, "删除设备" + dbSmartDev.get(j).getUid());
                DataSupport.deleteAll(SmartDev.class, "Uid = ? ", dbSmartDev.get(j).getUid());
            }
        }
    }

    private List<SmartDev> saveSmartDevices(List<Deviceprops> devices) {
        //保存设备列表
        List<SmartDev> dbSmartDev = mDeviceManager.findAllSmartDevice();
        for (int i = 0; i < devices.size(); i++) {
            boolean addToDb = true;
            if (devices.get(i).getDevice_type().equalsIgnoreCase("LKSGW")) {
                addToDb = false;
            } else {
                for (int j = 0; j < dbSmartDev.size(); j++) {
                    if (dbSmartDev.get(j).getUid().equals(devices.get(i).getUid())) {
                        addToDb = false;
                    }
                }
            }
            if (addToDb) {
                saveSmartDeviceToSqlite(devices, i);
            }
        }
        return dbSmartDev;
    }

    private void SyncGatwayDevices(List<Deviceprops> devices, List<SmartDev> dbSmartDev, List<GatwayDevice> dbGetwayDev) {
        //本地数据库中有,http返回没有(设备在其他地方删除了,在这个设备需要同步服务器的)
        for (int j = 0; j < dbGetwayDev.size(); j++) {
            boolean deleteDevice = true;
            for (int i = 0; i < devices.size(); i++) {
                if (devices.get(i).getDevice_type().equalsIgnoreCase("LKSGW")) {
                    if (dbSmartDev.get(j).getUid().equals(devices.get(i).getUid())) {
                        deleteDevice = false;
                    }
                } else {
                    deleteDevice = false;
                }
            }
            if (deleteDevice) {
                DataSupport.deleteAll(GatwayDevice.class, "Uid = ? ", dbGetwayDev.get(j).getUid());
            }
        }
    }

    private List<GatwayDevice> saveGatwayDevices(List<Deviceprops> devices) {
        List<GatwayDevice> dbGetwayDev = GetwayManager.getInstance().getAllGetwayDevice();
        for (int i = 0; i < devices.size(); i++) {
            boolean addToDb = true;
            if (devices.get(i).getDevice_type().equalsIgnoreCase("LKSGW")){
                for (int j = 0; j < dbGetwayDev.size(); j++) {
                    if (dbGetwayDev.get(j).getUid().equals(devices.get(i).getUid())) {
                        addToDb = false;
                    }
                }
            } else {
                addToDb = false;
            }
            if (addToDb) {
                saveGetwayDeviceToSqlite(devices, i);
            }
        }
        return dbGetwayDev;
    }

    private Handler mHandler = new WeakRefHandler(mCallback);


    private void parseLocalReturnDeviceList(String str, List<GatwayDevice> tempDevice, List<SmartDev> tempSmartDevice) {
        Gson gson = new Gson();
        DeviceList aDeviceList = gson.fromJson(str, DeviceList.class);
        if (aDeviceList.getSmartDev() != null && aDeviceList.getSmartDev().size() > 0) {
            tempSmartDevice.addAll(aDeviceList.getSmartDev());
        }
        //存储设备列表
        if (aDeviceList.getDevice() != null && aDeviceList.getDevice().size() > 0) {
            tempDevice.addAll(aDeviceList.getDevice());
        }
    }

    /**
     * //智能设备下发列表
     *
     * @param tempSmartDevice
     */
    private void smartDeviceBindLocal(List<SmartDev> tempSmartDevice) {
        for (int j = 0; j < datasBottom.size(); j++) {
            boolean addSmartdev = true;
            for (int i = 0; i < tempSmartDevice.size(); i++) {
                if (tempSmartDevice.get(i).getSmartUid().equalsIgnoreCase(datasBottom.get(j).getMac())
                        || datasBottom.get(j).getType().equals(DeviceTypeConstant.TYPE.TYPE_ROUTER)
                        || datasBottom.get(j).getType().equals(DeviceTypeConstant.TYPE.TYPE_AIR_REMOTECONTROL)
                        || datasBottom.get(j).getType().equals(DeviceTypeConstant.TYPE.TYPE_TV_REMOTECONTROL)
                        || datasBottom.get(j).getType().equals(DeviceTypeConstant.TYPE.TYPE_TVBOX_REMOTECONTROL)
                        || datasBottom.get(j).getType().equals(DeviceTypeConstant.TYPE.TYPE_MENLING
                )) {
                    addSmartdev = false;
                }
            }
            if (addSmartdev) {
                Log.i(TAG, "下发远程添加了本地没有添加的智能设备名称:" + datasBottom.get(j).getName() + "设备类型" +
                        datasBottom.get(j).getType()
                );
                QrcodeSmartDevice device = new QrcodeSmartDevice();
                device.setAd(datasBottom.get(j).getMac());
                switch (datasBottom.get(j).getType()) {
                    case DeviceTypeConstant.TYPE.TYPE_LIGHT:
                        device.setTp("YWLIGHTCONTROL");
                        break;
                    case DeviceTypeConstant.TYPE.TYPE_LOCK:
                        device.setTp("SMART_LOCK");
                        break;
                    case DeviceTypeConstant.TYPE.TYPE_REMOTECONTROL:
                    case "IRMOTE_V2":
                        device.setTp("IRMOTE_V2");
                        break;
                    case DeviceTypeConstant.TYPE.TYPE_SWITCH:
                        if (datasBottom.get(j).getSubType() != null) {
                            switch (datasBottom.get(j).getSubType()) {
                                case DeviceTypeConstant.TYPE_SWITCH_SUBTYPE.SUB_TYPE_SWITCH_ONEWAY:
                                    device.setTp("SmartWallSwitch1");
                                    break;
                                case DeviceTypeConstant.TYPE_SWITCH_SUBTYPE.SUB_TYPE_SWITCH_TWOWAY:
                                    device.setTp("SmartWallSwitch2");
                                    break;
                                case DeviceTypeConstant.TYPE_SWITCH_SUBTYPE.SUB_TYPE_SWITCH_THREEWAY:
                                    device.setTp("SmartWallSwitch3");
                                    break;
                                case DeviceTypeConstant.TYPE_SWITCH_SUBTYPE.SUB_TYPE_SWITCH_FOURWAY:
                                    device.setTp("SmartWallSwitch4");
                                    break;
                            }
                        }
                        break;
                }
                device.setOrg(datasBottom.get(j).getOrg());
                device.setVer(datasBottom.get(j).getVer());
                mDeviceManager.bindSmartDevList(device);
            }
            mDeviceAdapter.setTopList(datasTop);
            mDeviceAdapter.setBottomList(datasBottom);
            mDeviceAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 更新虚拟设备的状态
     */
    private void virtualDeviceUpdate() {
        List<SmartDev> airRcs = DataSupport.where("Type=?", DeviceTypeConstant.TYPE.TYPE_AIR_REMOTECONTROL).find(SmartDev.class);
        for (int i = 0; i < airRcs.size(); i++) {
            SmartDev realRc = DataSupport.where("Uid=?", airRcs.get(i).getRemotecontrolUid()).findFirst(SmartDev.class, true);
            if (realRc != null) {
                airRcs.get(i).setRooms(realRc.getRooms());
                airRcs.get(i).setStatus(realRc.getStatus());
                airRcs.get(i).saveFast();
            }
        }
        List<SmartDev> tvRcs = DataSupport.where("Type=?", DeviceTypeConstant.TYPE.TYPE_TV_REMOTECONTROL).find(SmartDev.class);
        for (int i = 0; i < tvRcs.size(); i++) {
            SmartDev realRc = DataSupport.where("Uid=?", tvRcs.get(i).getRemotecontrolUid()).findFirst(SmartDev.class, true);
            if (realRc != null) {
                tvRcs.get(i).setStatus(realRc.getStatus());
                tvRcs.get(i).setRooms(realRc.getRooms());
                tvRcs.get(i).saveFast();
            }
        }
        List<SmartDev> tvboxRcs = DataSupport.where("Type=?", DeviceTypeConstant.TYPE.TYPE_TVBOX_REMOTECONTROL).find(SmartDev.class);
        for (int i = 0; i < tvboxRcs.size(); i++) {
            SmartDev realRc = DataSupport.where("Uid=?", tvboxRcs.get(i).getRemotecontrolUid()).findFirst(SmartDev.class, true);
            if (realRc != null) {
                tvboxRcs.get(i).setStatus(realRc.getStatus());
                tvboxRcs.get(i).setRooms(realRc.getRooms());
                tvboxRcs.get(i).saveFast();
            }
        }
    }


    /**
     * 网关设备下发列表
     * 网关设备本地绑定
     *
     * @param tempDevice
     */
    private void gatwayDevcieBindLocal(List<GatwayDevice> tempDevice) {
        for (int j = 0; j < datasTop.size(); j++) {
            boolean addGatway = true;
            for (int i = 0; i < tempDevice.size(); i++) {
                if (tempDevice.get(i).getUid().equalsIgnoreCase(datasTop.get(j).getUid())) {
                    addGatway = false;
                }
            }
            if (addGatway) {
                Log.i(TAG, "下发网关设备 uid:" + datasTop.get(j).getUid());
                mGetwayManager.bindDevice(datasTop.get(j).getUid());
            }
        }
    }


    @Override
    public void onRecvEllEPacket(BasicPacket packet) {
        Log.i(TAG, "onRecvEllEPacket" + packet.toString() + packet.mac);
        seachedDoorbellmac = DataExchange.byteArrayToHexString(DataExchange.longToEightByte(packet.mac));
        updateDoorBellStatus();
        Log.i(TAG, "onRecvEllEPacket" + seachedDoorbellmac);

    }
    private void updateDoorBellStatus() {
        if (seachedDoorbellmac != null) {
            seachedDoorbellmac = seachedDoorbellmac.replaceAll("0x", "").trim();
            seachedDoorbellmac = seachedDoorbellmac.replaceAll(" ", "-") ;
            if (currentSmartDoorBell != null) {
                if (seachedDoorbellmac.equalsIgnoreCase(currentSmartDoorBell.getMac())) {
                    currentSmartDoorBell.setStatus("在线");
                    currentSmartDoorBell.saveFast();
                    ellESDK.stopSearchDevs();
                }
            }
        }
    }

    private String seachedDoorbellmac;
    @Override
    public void searchDevCBS(long mac, byte type, byte ver) {
        Log.e(TAG, "mac:" + mac + "type:" + type + "ver:" + ver);
        seachedDoorbellmac = DataExchange.byteArrayToHexString(DataExchange.longToEightByte(mac));
        updateDoorBellStatus();
    }

    @Override
    public void responseQueryResult(String result) {
        Log.i(TAG, "responseQueryResult" + result);
    }

    @Override
    public void responseSetResult(String result) {
        Log.i(TAG, "智能灯泡设置结果result=" + result);
        //网关远程收到消息就在线了
        refreshCountGetway = 0;
        if (getwayDevices != null) {
            ContentValues values = new ContentValues();
            values.put("Status", "在线");
            Log.i(TAG, "网关设备在线");
            DataSupport.updateAll(GatwayDevice.class, values, "Uid=?", getwayDevices.getUid());
        }
        if (currentLightDev != null) {
            currentLightDev.setStatus("在线");
            currentLightDev.saveFast();
        }
    }

    @Override
    public void responseBind(String result) {

    }

    @Override
    public void responseLockStatu(int RecondNum, int LockStatus) {
        refreshCountGetway = 0;
        if (getwayDevices != null) {
            ContentValues values = new ContentValues();
            values.put("Status", "在线");
            DataSupport.updateAll(GatwayDevice.class, values, "Uid=?", getwayDevices.getUid());
        }
        Log.i(TAG, "responseLockStatu" + (currentLockDev != null));
        if (currentLockDev != null) {
            currentLockDev.setStatus("在线");
            currentLockDev.saveFast();
        }
    }
    @Override
    public void responseUserIdInfo(UserIdInfo userIdInfo) {

    }
}
