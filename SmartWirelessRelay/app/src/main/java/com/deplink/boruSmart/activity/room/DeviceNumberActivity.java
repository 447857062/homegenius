package com.deplink.boruSmart.activity.room;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.deplink.boruSmart.Protocol.packet.ellisdk.BasicPacket;
import com.deplink.boruSmart.Protocol.packet.ellisdk.EllE_Listener;
import com.deplink.boruSmart.activity.device.doorbell.DoorbeelMainActivity;
import com.deplink.boruSmart.activity.device.remoteControl.realRemoteControl.RemoteControlActivity;
import com.deplink.boruSmart.activity.device.smartlock.SmartLockActivity;
import com.deplink.boruSmart.constant.DeviceTypeConstant;
import com.deplink.boruSmart.util.Perfence;
import com.deplink.boruSmart.util.WeakRefHandler;
import com.deplink.boruSmart.Protocol.json.Room;
import com.deplink.boruSmart.Protocol.json.device.SmartDev;
import com.deplink.boruSmart.Protocol.json.device.getway.GatwayDevice;
import com.deplink.boruSmart.Protocol.packet.ellisdk.EllESDK;
import com.deplink.boruSmart.activity.device.adapter.DeviceListAdapter;
import com.deplink.boruSmart.activity.device.getway.GetwayDeviceActivity;
import com.deplink.boruSmart.activity.device.light.LightActivity;
import com.deplink.boruSmart.activity.device.remoteControl.airContorl.AirRemoteControlMianActivity;
import com.deplink.boruSmart.activity.device.remoteControl.topBox.TvBoxMainActivity;
import com.deplink.boruSmart.activity.device.remoteControl.tv.TvMainActivity;
import com.deplink.boruSmart.activity.device.router.RouterMainActivity;
import com.deplink.boruSmart.activity.device.smartSwitch.SwitchFourActivity;
import com.deplink.boruSmart.activity.device.smartSwitch.SwitchOneActivity;
import com.deplink.boruSmart.activity.device.smartSwitch.SwitchThreeActivity;
import com.deplink.boruSmart.activity.device.smartSwitch.SwitchTwoActivity;
import com.deplink.boruSmart.activity.personal.login.LoginActivity;
import com.deplink.boruSmart.constant.AppConstant;
import com.deplink.boruSmart.manager.device.DeviceListener;
import com.deplink.boruSmart.manager.device.DeviceManager;
import com.deplink.boruSmart.manager.device.doorbeel.DoorbeelManager;
import com.deplink.boruSmart.manager.device.getway.GetwayManager;
import com.deplink.boruSmart.manager.device.light.SmartLightManager;
import com.deplink.boruSmart.manager.device.remoteControl.RemoteControlManager;
import com.deplink.boruSmart.manager.device.router.RouterManager;
import com.deplink.boruSmart.manager.device.smartlock.SmartLockManager;
import com.deplink.boruSmart.manager.device.smartswitch.SmartSwitchManager;
import com.deplink.boruSmart.manager.room.RoomManager;
import com.deplink.boruSmart.util.DataExchange;
import com.deplink.boruSmart.view.combinationwidget.TitleLayout;
import com.deplink.boruSmart.view.dialog.AlertDialog;
import com.deplink.sdk.android.sdk.DeplinkSDK;
import com.deplink.sdk.android.sdk.EventCallback;
import com.deplink.sdk.android.sdk.SDKAction;
import com.deplink.sdk.android.sdk.manager.SDKManager;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import deplink.com.smartwirelessrelay.homegenius.EllESDK.R;

/**
 * 查看智能设备列表的界面
 */
public class DeviceNumberActivity extends Activity implements EllE_Listener {
    private static final String TAG = "DeviceNumberActivity";
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
    private SDKManager manager;
    private EventCallback ec;
    private SmartDev currentSmartDoorBell;
    private boolean isStartFromExperience;

    private String seachedDoorbellmac;
    private boolean isreceiverdDoorbeel = false;
    private TitleLayout layout_title;
    private Timer refreshTimer = null;
    private TimerTask refreshTask = null;
    private static final int TIME_DIFFERENCE_BETWEEN_MESSAGE_INTERVALS = 10000;
    private DeviceListener mDeviceListener;
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

    private void startTimer() {
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
                            for (int i = 0; i < datasTop.size(); i++) {
                                if ((mDeviceManager.getmDevicesStatus().get(datasTop.get(i).getUid()) != null)) {
                                    datasTop.get(i).setStatus(mDeviceManager.getmDevicesStatus().get(datasTop.get(i).getUid()));
                                    datasTop.get(i).saveFast();
                                }
                            }
                            for (int i = 0; i < datasBottom.size(); i++) {
                                if ((mDeviceManager.getmDevicesStatus().get(datasBottom.get(i).getMac()) != null)) {
                                    datasBottom.get(i).setStatus(mDeviceManager.getmDevicesStatus().get(datasBottom.get(i).getMac()));
                                    datasBottom.get(i).saveFast();
                                }
                                if (datasBottom.get(i).getType().equalsIgnoreCase(DeviceTypeConstant.TYPE.TYPE_MENLING)) {
                                    currentSmartDoorBell = datasBottom.get(i);
                                }
                            }
                            virtualDeviceUpdate();
                            updateListview();
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


    private void initEvents() {

    }

    @Override
    protected void onResume() {
        super.onResume();
        isStartFromExperience = mDeviceManager.isStartFromExperience();
        mDeviceManager.addDeviceListener(mDeviceListener);
        mDeviceManager.startQueryStatu();
        initListener();
        queryDeviceStatu();
        updateListview();
        startTimer();
    }

    private void initListener() {
        manager.addEventCallback(ec);
    }

    /**
     * 更新一些设备的在线离线状态
     */
    private void queryDeviceStatu() {
        if (currentRoom.getmDevices().size() > 0) {
            for (int i = 0; i < currentRoom.getmDevices().size(); i++) {
                switch (currentRoom.getmDevices().get(i).getType()) {
                    case DeviceTypeConstant.TYPE.TYPE_MENLING:
                        currentSmartDoorBell = DataSupport.where("Uid = ?", currentRoom.getmDevices().get(i).getUid()).findFirst(SmartDev.class, true);
                        if (currentSmartDoorBell != null) {
                            ellESDK.startSearchDevs();
                            mHandler.sendEmptyMessageDelayed(MSG_DOORBELL_STATUS_CHECK, MSG_DOORBELL_STATUS_CHECK_TIMEOUT);
                        }
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


    private void updateListview() {
        //更新房间关联的设备,设备状态更新了
        currentRoom = DataSupport.where("Uid = ?", currentRoom.getUid()).findFirst(Room.class, true);
        datasTop.clear();
        datasBottom.clear();
        datasTop.addAll(currentRoom.getmGetwayDevices());
        datasBottom.addAll(currentRoom.getmDevices());
        mDeviceAdapter.setTopList(datasTop);
        mDeviceAdapter.setBottomList(datasBottom);
        listview_devies.setAdapter(mDeviceAdapter);
        mDeviceAdapter.notifyDataSetChanged();
    }


    @Override
    protected void onPause() {
        super.onPause();
        manager.removeEventCallback(ec);
        mDeviceManager.removeDeviceListener(mDeviceListener);
        mDeviceManager.stopQueryStatu();
        ellESDK.stopSearchDevs();
        stopTimer();
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
                startActivity(new Intent(DeviceNumberActivity.this, ManageRoomActivity.class));
            }
        });
        mDeviceListener=new DeviceListener() {
        };
        initManager();
        currentRoom = mRoomManager.getCurrentSelectedRoom();
        initMqttCallback();
        if (!isStartFromExperience) {
            layout_title.setTitleText(currentRoom.getRoomName());
        }
        datasTop = new ArrayList<>();
        datasBottom = new ArrayList<>();
        mDeviceAdapter = new DeviceListAdapter(this, datasTop, datasBottom);
        initListViewItemClicklistener();
        ellESDK = EllESDK.getInstance();
        ellESDK.InitEllESDK(this, this);
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
                            RouterManager.getInstance().setCurrentSelectedRouter(datasBottom.get(position - datasTop.size()));
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
                                case DeviceTypeConstant.TYPE_SWITCH_SUBTYPE.SUB_TYPE_SWITCH_ONEWAY:
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
        mDeviceManager = DeviceManager.getInstance();
        mDeviceManager.InitDeviceManager(this);
    }

    private void initViews() {
        listview_devies = findViewById(R.id.listview_devies);
        layout_title = findViewById(R.id.layout_title);
    }

    @Override
    public void onRecvEllEPacket(BasicPacket packet) {
        Log.i(TAG, "onRecvEllEPacket" + packet.toString() + packet.mac);
        seachedDoorbellmac = DataExchange.byteArrayToHexString(DataExchange.longToEightByte(packet.mac));
        if (seachedDoorbellmac != null) {
            seachedDoorbellmac = seachedDoorbellmac.replaceAll("0x", "").trim();
            seachedDoorbellmac = seachedDoorbellmac.replaceAll(" ", "-");
        }

        Log.i(TAG, "onRecvEllEPacket" + seachedDoorbellmac);
        updateDoorbellStatu();
    }

    @Override
    public void searchDevCBS(long mac, byte type, byte ver) {
        Log.e(TAG, "mac:" + mac + "type:" + type + "ver:" + ver);
        isreceiverdDoorbeel = true;
        seachedDoorbellmac = DataExchange.byteArrayToHexString(DataExchange.longToEightByte(mac));
        if (seachedDoorbellmac != null) {
            seachedDoorbellmac = seachedDoorbellmac.replaceAll("0x", "").trim();
            seachedDoorbellmac = seachedDoorbellmac.replaceAll(" ", "-");
        }
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
