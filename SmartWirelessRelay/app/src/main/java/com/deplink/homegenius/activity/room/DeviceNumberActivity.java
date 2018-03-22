package com.deplink.homegenius.activity.room;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.deplink.homegenius.Protocol.json.OpResult;
import com.deplink.homegenius.Protocol.json.Room;
import com.deplink.homegenius.Protocol.json.device.SmartDev;
import com.deplink.homegenius.Protocol.json.device.getway.GatwayDevice;
import com.deplink.homegenius.Protocol.json.device.lock.UserIdInfo;
import com.deplink.homegenius.Protocol.json.device.router.Router;
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
import com.deplink.homegenius.activity.personal.login.LoginActivity;
import com.deplink.homegenius.constant.AppConstant;
import com.deplink.homegenius.constant.DeviceTypeConstant;
import com.deplink.homegenius.manager.connect.remote.HomeGenius;
import com.deplink.homegenius.manager.device.DeviceListener;
import com.deplink.homegenius.manager.device.DeviceManager;
import com.deplink.homegenius.manager.device.doorbeel.DoorbeelManager;
import com.deplink.homegenius.manager.device.getway.GetwayManager;
import com.deplink.homegenius.manager.device.light.SmartLightListener;
import com.deplink.homegenius.manager.device.light.SmartLightManager;
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
import com.deplink.homegenius.view.combinationwidget.TitleLayout;
import com.deplink.homegenius.view.dialog.AlertDialog;
import com.deplink.sdk.android.sdk.DeplinkSDK;
import com.deplink.sdk.android.sdk.EventCallback;
import com.deplink.sdk.android.sdk.SDKAction;
import com.deplink.sdk.android.sdk.manager.SDKManager;
import com.deplink.sdk.android.sdk.mqtt.MQTTController;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

import deplink.com.smartwirelessrelay.homegenius.EllESDK.R;

/**
 * 查看智能设备列表的界面
 */
public class DeviceNumberActivity extends Activity implements SmartLightListener,SmartLockListener,EllE_Listener {
    private static final String TAG="DeviceNumberActivity";
    private DeviceListAdapter mDeviceAdapter;
    /**
     * 上面半部分列表的数据
     */
    private List<GatwayDevice> datasTop;
    /**
     * 下面半部分列表的数据
     */
    private List<SmartDev> datasBottom;
    private ListView listview_devies;
    private RoomManager mRoomManager;
    private Room currentRoom;
    private DeviceManager mDeviceManager;
    private GetwayManager mGetwayManager;
    private SDKManager manager;
    private EventCallback ec;
    private DeviceListener mDeviceListener;
    private GatwayDevice getwayDevices;
    private SmartDev currentSwitchDev;
    private SmartDev currentLightDev;
    private SmartDev currentLockDev;
    private SmartDev currentRemotecontrolDev;
    private SmartDev currentSmartDoorBell;
    private SmartDev routerDevices;
    private SmartSwitchManager mSmartSwitchManager;
    private RemoteControlManager mRemoteControlManager;
    private SmartLightManager mSmartLightManager;
    private SmartLockManager mSmartLockManager;
    private String lastremoteControlState;
    private SmartSwitchListener mSmartSwitchListener;
    private boolean isStartFromExperience;
    private String lastRouterStatu;
    //路由器是否订阅过,用于界面初始化后,订阅mqtt通道,查询设备在线状态
    private boolean isSubscribe = false;
    private HomeGenius mHomeGenius;
    private int refreshCount = 0;
    /**
     * 设备上线下线监控，2次发送没有回数据就认为设备下线,所以是大于1
     */
    private static final int TIME_OUT_WATCHDOG_MAXCOUNT = 1;
    private String lastSwitchState;
    private String seachedDoorbellmac;
    private boolean isreceiverdDoorbeel = false;
    private SmartDev airRcs;
    private SmartDev tvRcs;
    private SmartDev tvboxRcs;
    private TitleLayout layout_title;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_number);
        initViews();
        initDatas();
        initEvents();
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
                Log.i(TAG, "设备列表界面收到回调的mqtt消息=" + result);
                updateDeviceByMqtt(result);
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
                new AlertDialog(DeviceNumberActivity.this).builder().setTitle("账号异地登录")
                        .setMsg("当前账号已在其它设备上登录,是否重新登录")
                        .setPositiveButton("确认", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                startActivity(new Intent(DeviceNumberActivity.this, LoginActivity.class));
                            }
                        }).setNegativeButton("取消", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                }).show();
            }
        };
    }
    private void updateDeviceByMqtt(String result) {
        if (result.contains("DevList")) {
            if (getwayDevices != null) {
                ContentValues values = new ContentValues();
                values.put("Status", "在线");
                DataSupport.updateAll(GatwayDevice.class, values, "Uid=?", getwayDevices.getUid());
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
                        lastremoteControlState="在线";
                        currentRemotecontrolDev.saveFast();
                        if (getwayDevices != null) {
                            ContentValues values = new ContentValues();
                            values.put("Status", "在线");
                            DataSupport.updateAll(GatwayDevice.class, values, "Uid=?", getwayDevices.getUid());
                        }
                        if(airRcs!=null){
                            airRcs.setStatus("在线");
                            airRcs.saveFast();
                        }
                        if(tvRcs!=null){
                            tvRcs.setStatus("在线");
                            tvRcs.saveFast();
                        }
                        if(tvboxRcs!=null){
                            tvboxRcs.setStatus("在线");
                            tvboxRcs.saveFast();
                        }
                    } else if (content.getMethod().equalsIgnoreCase("SmartWallSwitch")) {
                        currentSwitchDev.setStatus("在线");
                        currentSwitchDev.saveFast();
                        if (getwayDevices != null) {
                            ContentValues values = new ContentValues();
                            values.put("Status", "在线");
                            DataSupport.updateAll(GatwayDevice.class, values, "Uid=?", getwayDevices.getUid());
                        }
                    } else if (content.getMethod().equalsIgnoreCase("YWLIGHTCONTROL")) {
                        if(currentLightDev!=null){
                            currentLightDev.setStatus("在线");
                            currentLightDev.saveFast();
                        }

                        if (getwayDevices != null) {
                            ContentValues values = new ContentValues();
                            values.put("Status", "在线");
                            DataSupport.updateAll(GatwayDevice.class, values, "Uid=?", getwayDevices.getUid());
                        }
                    } else if (content.getMethod().equalsIgnoreCase("SMART_LOCK")) {
                        if(currentLockDev!=null){
                            currentLockDev.setStatus("在线");
                            currentLockDev.saveFast();
                        }

                        if (getwayDevices != null) {
                            ContentValues values = new ContentValues();
                            values.put("Status", "在线");
                            DataSupport.updateAll(GatwayDevice.class, values, "Uid=?", getwayDevices.getUid());
                        }
                    } else if (content.getMethod().equalsIgnoreCase("PERFORMANCE")) {
                        if(routerDevices!=null){
                            routerDevices.setStatus("在线");
                            routerDevices.saveFast();
                        }

                    }
                }
            }
        }
        updateListview();
    }

    private void initEvents() {

    }

    @Override
    protected void onResume() {
        super.onResume();
        isStartFromExperience = mDeviceManager.isStartFromExperience();
        initListener();
        queryDeviceStatu();
        updateListview();

    }

    private void initListener() {
        manager.addEventCallback(ec);
        mDeviceManager.addDeviceListener(mDeviceListener);
        mSmartSwitchManager.addSmartSwitchListener(mSmartSwitchListener);
        mSmartLockManager.addSmartLockListener(this);
        mSmartLightManager.addSmartLightListener(this);
    }

    /**
     * 更新一些设备的在线离线状态
     */
    private void queryDeviceStatu() {
        if(currentRoom.getmGetwayDevices().size()>0){
            getwayDevices=currentRoom.getmGetwayDevices().get(0);
            mDeviceManager.queryDeviceList();
        }
        if(currentRoom.getmDevices().size()>0){
          for(int i=0;i<currentRoom.getmDevices().size();i++){
              switch (currentRoom.getmDevices().get(i).getType()){
                  case  DeviceTypeConstant.TYPE.TYPE_ROUTER:
                      queryRouterStatu(i);
                      break;
                  case  DeviceTypeConstant.TYPE.TYPE_LIGHT:
                      queryLightDevStatu(i);
                      break;
                  case  DeviceTypeConstant.TYPE.TYPE_LOCK:
                      queryLockDevStatu(i);
                      break;
                  case  DeviceTypeConstant.TYPE.TYPE_SWITCH:
                      querySwitchDevStatu(i);
                      break;
                  case  DeviceTypeConstant.TYPE.TYPE_REMOTECONTROL:
                      queryRemoteControlDevStatu(i);
                      break;
                  case  DeviceTypeConstant.TYPE.TYPE_MENLING:
                      currentSmartDoorBell = DataSupport.where("Uid = ?" ,currentRoom.getmDevices().get(i).getUid()).findFirst(SmartDev.class, true);
                      if (currentSmartDoorBell != null) {
                          ellESDK.startSearchDevs();
                          mHandler.sendEmptyMessageDelayed(MSG_DOORBELL_STATUS_CHECK, MSG_DOORBELL_STATUS_CHECK_TIMEOUT);
                      }
                      break;
                  case  DeviceTypeConstant.TYPE.TYPE_AIR_REMOTECONTROL:
                      airRcs= DataSupport.where("Uid = ?" ,currentRoom.getmDevices().get(i).getUid()).findFirst(SmartDev.class, true);
                      break;
                  case  DeviceTypeConstant.TYPE.TYPE_TV_REMOTECONTROL:
                      tvRcs= DataSupport.where("Uid = ?" ,currentRoom.getmDevices().get(i).getUid()).findFirst(SmartDev.class, true);
                      break;
                  case  DeviceTypeConstant.TYPE.TYPE_TVBOX_REMOTECONTROL:
                      tvboxRcs= DataSupport.where("Uid = ?" ,currentRoom.getmDevices().get(i).getUid()).findFirst(SmartDev.class, true);
                      break;
              }
          }
        }
    }
    private static final int MSG_DOORBELL_STATUS_CHECK = 0x03;
    private static final long MSG_DOORBELL_STATUS_CHECK_TIMEOUT = 5000;
    private EllESDK ellESDK;
    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_DOORBELL_STATUS_CHECK:
                    if (!isreceiverdDoorbeel) {
                        seachedDoorbellmac = "";
                    }
                    break;
            }
            return false;
        }
    };
    private Handler mHandler = new WeakRefHandler(mCallback);

    private void queryRemoteControlDevStatu(int i) {
        currentRemotecontrolDev = DataSupport.where("Uid = ?" ,currentRoom.getmDevices().get(i).getUid()).findFirst(SmartDev.class, true);
        if (currentRemotecontrolDev != null) {
            mRemoteControlManager.setmSelectRemoteControlDevice(currentRemotecontrolDev);
            mRemoteControlManager.queryStatu();
            if (lastremoteControlState == null) {
                lastremoteControlState = "离线";
            }
            if (lastremoteControlState.equalsIgnoreCase("在线")) {
                if (!gatwayAvailable()) {
                    lastremoteControlState = "离线";
                    currentRemotecontrolDev.setStatus("离线");
                    currentRemotecontrolDev.saveFast();
                }
            }
        }
    }

    private void querySwitchDevStatu(int i) {
        currentSwitchDev = DataSupport.where("Uid = ?",currentRoom.getmDevices().get(i).getUid()).findFirst(SmartDev.class, true);
        mSmartSwitchManager.setCurrentSelectSmartDevice(currentSwitchDev);
        mSmartSwitchManager.querySwitchStatus("query");
        if (!gatwayAvailable()) {
            if (lastSwitchState != null && lastSwitchState.equalsIgnoreCase("在线")) {
                lastSwitchState = "离线";
                currentSwitchDev.setStatus(lastSwitchState);
                currentSwitchDev.saveFast();
            }
        }
    }

    private void queryLockDevStatu(int i) {
        currentLockDev = DataSupport.where("Uid = ?", currentRoom.getmDevices().get(i).getUid()).findFirst(SmartDev.class, true);
        if (currentLockDev != null) {
            mSmartLockManager.setCurrentSelectLock(currentLockDev);
            mSmartLockManager.queryLockStatu();
            if (!gatwayAvailable()) {
                currentLockDev.setStatus("离线");
                currentLockDev.saveFast();
            }
        }
    }

    private void queryLightDevStatu(int i) {
        currentLightDev = DataSupport.where("Uid = ?", currentRoom.getmDevices().get(i).getUid()).findFirst(SmartDev.class, true);
        if (currentLightDev != null) {
            mSmartLightManager.setCurrentSelectLight(currentLightDev);
            mSmartLightManager.queryLightStatus();
            if (!gatwayAvailable()) {
                currentLightDev.setStatus("离线");
                currentLightDev.saveFast();
            }
        }
    }

    private void queryRouterStatu(int i) {
        routerDevices = DataSupport.where("Uid = ?", currentRoom.getmDevices().get(i).getUid()).findFirst(SmartDev.class, true);
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
    }

    private void updateListview() {
        //更新房间关联的设备,设备状态更新了
        currentRoom=DataSupport.where("Uid = ?" ,currentRoom.getUid()).findFirst(Room.class, true);
        datasTop.clear();
        datasBottom.clear();
        datasTop.addAll(currentRoom.getmGetwayDevices());
        datasBottom .addAll(currentRoom.getmDevices());
        mDeviceAdapter.setTopList(datasTop);
        mDeviceAdapter.setBottomList(datasBottom);
        listview_devies.setAdapter(mDeviceAdapter);
        mDeviceAdapter.notifyDataSetChanged();
    }

    /**
     * 有没有可用的网关
     *
     * @return
     */
    private boolean gatwayAvailable() {
        //如果没有可用的网关,其它智能设备也设置为离线状态
        boolean gatwayAvailable = false;
        List<GatwayDevice> allGatways = DataSupport.findAll(GatwayDevice.class);
        for (int j = 0; j < allGatways.size(); j++) {
            if (allGatways.get(j).getStatus()!=null && (allGatways.get(j).getStatus().equalsIgnoreCase("在线")
                    || (allGatways.get(j).getStatus().equalsIgnoreCase("ON")))) {
                gatwayAvailable = true;
            }
        }
        return gatwayAvailable;
    }
    @Override
    protected void onPause() {
        super.onPause();
        manager.removeEventCallback(ec);
        mSmartSwitchManager.removeSmartSwitchListener(mSmartSwitchListener);
        mSmartLightManager.removeSmartLightListener(this);
        mDeviceManager.removeDeviceListener(mDeviceListener);
        mSmartLockManager.removeSmartLockListener(this);
        ellESDK.stopSearchDevs();
    }

    @Override
    public void responseQueryResult(String result) {

    }

    @Override
    public void responseSetResult(String result) {
        Log.i(TAG, "智能灯泡设置结果result=" + result);
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
    private void initDatas() {
        layout_title.setReturnClickListener(new TitleLayout.ReturnImageClickListener() {
            @Override
            public void onBackPressed() {
                DeviceNumberActivity.this.onBackPressed();
            }
        });
        layout_title.setEditTextClickListener(new TitleLayout.EditTextClickListener() {
            @Override
            public void onEditTextPressed() {
                startActivity(new Intent(DeviceNumberActivity.this,ManageRoomActivity.class));
            }
        });

        mHomeGenius = new HomeGenius();
        initManager();
        currentRoom=mRoomManager.getCurrentSelectedRoom();
        initMqttCallback();
        if(!isStartFromExperience){
            layout_title.setTitleText(currentRoom.getRoomName());
        }
        datasTop = new ArrayList<>();
        datasBottom = new ArrayList<>();
        mDeviceAdapter = new DeviceListAdapter(this, datasTop, datasBottom);
        initListViewItemClicklistener();
        initDeviceListener();
        ellESDK = EllESDK.getInstance();
        ellESDK.InitEllESDK(this, this);
    }

    private void initDeviceListener() {
        mDeviceListener = new DeviceListener() {
            @Override
            public void responseQueryResult(String result) {
                super.responseQueryResult(result);
                if (result.contains("DevList")) {
                    Log.i(TAG, "本地接口接收到设备列表:" + result);
                    if (getwayDevices != null) {
                        ContentValues values = new ContentValues();
                        values.put("Status", "在线");
                        Log.i(TAG, "网关设备在线");
                        DataSupport.updateAll(GatwayDevice.class, values, "Uid=?", getwayDevices.getUid());
                    }
                }
            }
        };
        mSmartSwitchListener = new SmartSwitchListener() {
            @Override
            public void responseResult(String result) {
                Log.i(TAG, "开关本地状态反馈" + result);
                if(currentSwitchDev!=null){
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

    private void initListViewItemClicklistener() {
        listview_devies.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DeviceManager.getInstance().setStartFromExperience(false);
                if (datasTop.size() < (position + 1)) {
                    //智能设备
                    String deviceType = datasBottom.get(position - datasTop.size()).getType();
                    Log.i(TAG, "智能设备类型=" + deviceType);
                    mDeviceManager.setCurrentSelectSmartDevice(datasBottom.get(position - datasTop.size()));
                    switch (deviceType) {
                        case DeviceTypeConstant.TYPE.TYPE_LOCK:
                            SmartLockManager.getInstance().setCurrentSelectLock(datasBottom.get(position - datasTop.size()));
                            startActivity(new Intent(DeviceNumberActivity.this, SmartLockActivity.class));
                            break;
                        case DeviceTypeConstant.TYPE.TYPE_MENLING:
                            DoorbeelManager.getInstance().setCurrentSelectedDoorbeel(datasBottom.get(position - datasTop.size()));
                            startActivity(new Intent(DeviceNumberActivity.this, DoorbeelMainActivity.class));
                            break;
                        case "IRMOTE_V2":
                        case DeviceTypeConstant.TYPE.TYPE_REMOTECONTROL:
                            RemoteControlManager.getInstance().setmSelectRemoteControlDevice(datasBottom.get(position - datasTop.size()));
                            startActivity(new Intent(DeviceNumberActivity.this, RemoteControlActivity.class));
                            break;
                        case DeviceTypeConstant.TYPE.TYPE_AIR_REMOTECONTROL:
                            RemoteControlManager.getInstance().setmSelectRemoteControlDevice(datasBottom.get(position - datasTop.size()));
                            startActivity(new Intent(DeviceNumberActivity.this, AirRemoteControlMianActivity.class));
                            break;
                        case DeviceTypeConstant.TYPE.TYPE_ROUTER:
                            RouterManager.getInstance().setCurrentSelectedRouter(datasBottom.get(position-datasTop.size()));
                            startActivity(new Intent(DeviceNumberActivity.this, RouterMainActivity.class));
                            break;
                        case DeviceTypeConstant.TYPE.TYPE_TV_REMOTECONTROL:
                            RemoteControlManager.getInstance().setmSelectRemoteControlDevice(datasBottom.get(position - datasTop.size()));
                            startActivity(new Intent(DeviceNumberActivity.this, TvMainActivity.class));
                            break;
                        case DeviceTypeConstant.TYPE.TYPE_TVBOX_REMOTECONTROL:
                            RemoteControlManager.getInstance().setmSelectRemoteControlDevice(datasBottom.get(position - datasTop.size()));
                            startActivity(new Intent(DeviceNumberActivity.this, TvBoxMainActivity.class));
                            break;
                        case DeviceTypeConstant.TYPE.TYPE_LIGHT:
                            SmartLightManager.getInstance().setCurrentSelectLight(datasBottom.get(position - datasTop.size()));
                            startActivity(new Intent(DeviceNumberActivity.this, LightActivity.class));
                            break;
                        case DeviceTypeConstant.TYPE.TYPE_SWITCH:
                            SmartSwitchManager.getInstance().setCurrentSelectSmartDevice(datasBottom.get(position - datasTop.size()));
                            String deviceSubType = datasBottom.get(position - datasTop.size()).getSubType();
                            switch (deviceSubType) {
                                case  DeviceTypeConstant.TYPE_SWITCH_SUBTYPE.SUB_TYPE_SWITCH_ONEWAY:
                                    startActivity(new Intent(DeviceNumberActivity.this, SwitchOneActivity.class));
                                    break;
                                case DeviceTypeConstant.TYPE_SWITCH_SUBTYPE.SUB_TYPE_SWITCH_TWOWAY:
                                    startActivity(new Intent(DeviceNumberActivity.this, SwitchTwoActivity.class));
                                    break;
                                case DeviceTypeConstant.TYPE_SWITCH_SUBTYPE.SUB_TYPE_SWITCH_THREEWAY:
                                    startActivity(new Intent(DeviceNumberActivity.this, SwitchThreeActivity.class));
                                    break;
                                case DeviceTypeConstant.TYPE_SWITCH_SUBTYPE.SUB_TYPE_SWITCH_FOURWAY:
                                    startActivity(new Intent(DeviceNumberActivity.this, SwitchFourActivity.class));
                                    break;
                            }
                            break;
                    }
                } else {
                    //网关设备
                    GetwayManager.getInstance().setCurrentSelectGetwayDevice(datasTop.get(position));
                    startActivity(new Intent(DeviceNumberActivity.this, GetwayDeviceActivity.class));
                }
            }
        });
    }

    private void initManager() {
        mRoomManager = RoomManager.getInstance();
        mDeviceManager= DeviceManager.getInstance();
        mDeviceManager.InitDeviceManager(this);
        mGetwayManager= GetwayManager.getInstance();
        mGetwayManager.InitGetwayManager(this,null);
        mSmartSwitchManager = SmartSwitchManager.getInstance();
        mSmartSwitchManager.InitSmartSwitchManager(this);
        mSmartLockManager = SmartLockManager.getInstance();
        mSmartLockManager.InitSmartLockManager(DeviceNumberActivity.this);
        mRemoteControlManager = RemoteControlManager.getInstance();
        mRemoteControlManager.InitRemoteControlManager(this);
        mSmartLightManager = SmartLightManager.getInstance();
        mSmartLightManager.InitSmartLightManager(this);
    }

    private void initViews() {
        listview_devies= findViewById(R.id.listview_devies);
        layout_title= findViewById(R.id.layout_title);
    }

    @Override
    public void onRecvEllEPacket(BasicPacket packet) {
        Log.i(TAG, "onRecvEllEPacket" + packet.toString() + packet.mac);
        seachedDoorbellmac = DataExchange.byteArrayToHexString(DataExchange.longToEightByte(packet.mac));
        seachedDoorbellmac = seachedDoorbellmac.replaceAll("0x", "").trim();
        seachedDoorbellmac = seachedDoorbellmac.replaceAll(" ", "-");
        Log.i(TAG, "onRecvEllEPacket" + seachedDoorbellmac);
        updateDoorbellStatu();
    }

    @Override
    public void searchDevCBS(long mac, byte type, byte ver) {
        Log.e(TAG, "mac:" + mac + "type:" + type + "ver:" + ver);
        isreceiverdDoorbeel = true;
        seachedDoorbellmac = DataExchange.byteArrayToHexString(DataExchange.longToEightByte(mac));
        seachedDoorbellmac = seachedDoorbellmac.replaceAll("0x", "").trim();
        seachedDoorbellmac = seachedDoorbellmac.replaceAll(" ", "-");
        updateDoorbellStatu();
    }

    private void updateDoorbellStatu() {
        if (currentSmartDoorBell != null) {
            if (seachedDoorbellmac != null && seachedDoorbellmac.equalsIgnoreCase(currentSmartDoorBell.getMac())) {
                currentSmartDoorBell.setStatus("在线");
                currentSmartDoorBell.saveFast();
                ellESDK.stopSearchDevs();
            } else {
                currentSmartDoorBell.setStatus("离线");
                currentSmartDoorBell.saveFast();
            }
        }
    }
}
