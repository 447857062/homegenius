package com.deplink.boruSmart.manager.device;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.deplink.boruSmart.Protocol.json.OpResult;
import com.deplink.boruSmart.Protocol.json.QueryOptions;
import com.deplink.boruSmart.Protocol.json.Room;
import com.deplink.boruSmart.Protocol.json.device.DeviceList;
import com.deplink.boruSmart.Protocol.json.device.SmartDev;
import com.deplink.boruSmart.Protocol.json.device.getway.GatwayDevice;
import com.deplink.boruSmart.Protocol.json.device.lock.Record;
import com.deplink.boruSmart.Protocol.json.device.lock.alertreport.Info;
import com.deplink.boruSmart.Protocol.json.device.router.Router;
import com.deplink.boruSmart.Protocol.json.qrcode.QrcodeSmartDevice;
import com.deplink.boruSmart.Protocol.packet.GeneralPacket;
import com.deplink.boruSmart.activity.personal.login.LoginActivity;
import com.deplink.boruSmart.constant.AppConstant;
import com.deplink.boruSmart.constant.DeviceTypeConstant;
import com.deplink.boruSmart.constant.SmartLockConstant;
import com.deplink.boruSmart.manager.connect.local.tcp.LocalConnecteListener;
import com.deplink.boruSmart.manager.connect.local.tcp.LocalConnectmanager;
import com.deplink.boruSmart.manager.connect.remote.HomeGenius;
import com.deplink.boruSmart.manager.connect.remote.RemoteConnectManager;
import com.deplink.boruSmart.manager.device.getway.GetwayManager;
import com.deplink.boruSmart.manager.device.light.SmartLightManager;
import com.deplink.boruSmart.manager.device.remoteControl.RemoteControlManager;
import com.deplink.boruSmart.manager.device.smartlock.SmartLockManager;
import com.deplink.boruSmart.manager.device.smartswitch.SmartSwitchManager;
import com.deplink.boruSmart.manager.room.RoomManager;
import com.deplink.boruSmart.util.JsonArrayParseUtil;
import com.deplink.boruSmart.util.Perfence;
import com.deplink.boruSmart.view.toast.Ftoast;
import com.deplink.sdk.android.sdk.DeplinkSDK;
import com.deplink.sdk.android.sdk.EventCallback;
import com.deplink.sdk.android.sdk.SDKAction;
import com.deplink.sdk.android.sdk.homegenius.DeviceAddBody;
import com.deplink.sdk.android.sdk.homegenius.DeviceOperationResponse;
import com.deplink.sdk.android.sdk.homegenius.Deviceprops;
import com.deplink.sdk.android.sdk.homegenius.ShareDeviceBody;
import com.deplink.sdk.android.sdk.homegenius.VirtualDeviceAddBody;
import com.deplink.sdk.android.sdk.json.ErrorBody;
import com.deplink.sdk.android.sdk.manager.SDKManager;
import com.deplink.sdk.android.sdk.rest.RestfulToolsHomeGenius;
import com.deplink.sdk.android.sdk.rest.RestfulToolsHomeGeniusString;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Administrator on 2017/11/9.
 * 使用：
 * private DeviceManager mDeviceManager;
 * mDeviceManager = DeviceManager.getInstance();
 * mDeviceManager.InitDeviceManager(this, null);
 */
public class DeviceManager implements LocalConnecteListener {
    private static final String TAG = "DeviceManager";
    protected ExecutorService cachedThreadPool;
    private static DeviceManager instance;
    protected LocalConnectmanager mLocalConnectmanager;
    protected GeneralPacket packet;
    private Context mContext;
    private SmartDev currentSelectSmartDevice;
    private boolean isStartFromExperience;
    private boolean isStartFromHomePage;
    private boolean isExperCenterStartFromHomePage;
    private boolean isExperCenterStartFromDevice;
    protected RemoteConnectManager mRemoteConnectManager;
    protected HomeGenius mHomeGenius;
    private static String uuid;
    private SDKManager manager;
    private EventCallback ec;
    /**
     * key device uid
     * value onlineStatu
     */
    private HashMap<String, String> mDevicesStatus;
    private SmartLockManager mSmartLockManager;
    private SmartSwitchManager mSmartSwitchManager;
    private SmartLightManager mSmartLightManager;
    private RemoteControlManager mRemoteControlManager;
    private boolean editDevice;
    private String currentEditDeviceType;
    private static String userName;

    public boolean isEditDevice() {
        return editDevice;
    }

    public boolean isExperCenterStartFromDevice() {
        return isExperCenterStartFromDevice;
    }

    public void setExperCenterStartFromDevice(boolean experCenterStartFromDevice) {
        isExperCenterStartFromDevice = experCenterStartFromDevice;
    }

    public void setEditDevice(boolean editDevice) {
        this.editDevice = editDevice;
    }

    public String getCurrentEditDeviceType() {
        return currentEditDeviceType;
    }

    public void setCurrentEditDeviceType(String currentEditDeviceType) {
        this.currentEditDeviceType = currentEditDeviceType;
    }

    public boolean isExperCenterStartFromHomePage() {
        return isExperCenterStartFromHomePage;
    }

    public void setExperCenterStartFromHomePage(boolean experCenterStartFromHomePage) {
        isExperCenterStartFromHomePage = experCenterStartFromHomePage;
    }

    public boolean isStartFromExperience() {
        return isStartFromExperience;
    }

    public void setStartFromExperience(boolean startFromExperience) {
        isStartFromExperience = startFromExperience;
    }

    public boolean isStartFromHomePage() {
        return isStartFromHomePage;
    }

    public void setStartFromHomePage(boolean startFromHomePage) {
        isStartFromHomePage = startFromHomePage;
    }

    public static synchronized DeviceManager getInstance() {
        if (instance == null) {
            instance = new DeviceManager();
        }
        uuid = Perfence.getPerfence(AppConstant.PERFENCE_BIND_APP_UUID);
        userName = Perfence.getPerfence(Perfence.PERFENCE_PHONE);
        return instance;
    }

    private List<DeviceListener> mDeviceListenerList;

    public void addDeviceListener(DeviceListener listener) {
        Log.i(TAG, "onResume");
        if (listener != null && !mDeviceListenerList.contains(listener)) {
            Log.i(TAG, "onResume" + listener.toString());
            this.mDeviceListenerList.add(listener);
        }
        manager.addEventCallback(ec);
    }

    public void startQueryStatu() {
        if(mDevicesStatus==null){
            mDevicesStatus = new HashMap<>();
        }
        if(!timertaskIsScheduled){
            startTimer();
        }
    }
    private boolean timertaskIsScheduled;
    public void stopQueryStatu() {
        mDevicesStatus = null;
        stopTimer();
    }

    public void removeDeviceListener(DeviceListener listener) {
        if (listener != null && mDeviceListenerList.contains(listener)) {
            this.mDeviceListenerList.remove(listener);
        }

        manager.removeEventCallback(ec);
    }

    /**
     * 查询设备列表
     */
    public void queryDeviceList() {
        Log.i(TAG, "本地接口可用 :"
                + mLocalConnectmanager.isLocalconnectAvailable() +
                "远程接口可用:" + mRemoteConnectManager.isRemoteConnectAvailable()
        );
        if (mLocalConnectmanager.isLocalconnectAvailable()) {
            Log.i(TAG, "本地接口查询设备列表");
            QueryOptions queryCmd = new QueryOptions();
            queryCmd.setOP("QUERY");
            queryCmd.setMethod("DevList");
            queryCmd.setTimestamp();
            Gson gson = new Gson();
            String text = gson.toJson(queryCmd);
            packet.packQueryDevListData(text.getBytes(), false, null);
            cachedThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    mLocalConnectmanager.getOut(packet.data);
                }
            });
        } else {
            List<GatwayDevice> devices = DataSupport.findAll(GatwayDevice.class);
            for (int i = 0; i < devices.size(); i++) {
                if (devices.get(i).getTopic() != null && !devices.get(i).getTopic().equals("")) {
                    Log.i(TAG, "远程接口查询设备列表" + "topic" + devices.get(i).getTopic());
                    Log.i(TAG, "device.getTopic()=" + devices.get(i).getTopic());
                    mHomeGenius.queryDeviceList(devices.get(i).getTopic(), uuid);
                }
            }
        }
    }

    private Timer refreshTimer = null;
    private TimerTask refreshTask = null;
    private static final int TIME_DIFFERENCE_BETWEEN_MESSAGE_INTERVALS = 10000;

    private void stopTimer() {
        timertaskIsScheduled=false;
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
        timertaskIsScheduled=true;
        Log.i(TAG, "startTimer");
        if (refreshTimer == null) {
            refreshTimer = new Timer();
        }
        if (refreshTask == null) {
            refreshTask = new TimerTask() {
                @Override
                public void run() {
                    List<GatwayDevice> list = GetwayManager.getInstance().getAllGetwayDevice();
                    for (int i = 0; i < list.size(); i++) {
                        Log.i(TAG, "device=" + list.get(i).toString());
                        //查询各种设备的状态
                        queryDeviceList();
                    }
                    List<SmartDev> smartDevices = findAllSmartDevice();
                    for (int i = 0; i < smartDevices.size(); i++) {
                        switch (smartDevices.get(i).getType()) {
                            case DeviceTypeConstant.TYPE.TYPE_SWITCH:
                                mSmartSwitchManager.setCurrentSelectSmartDevice(smartDevices.get(i));
                                mSmartSwitchManager.querySwitchStatus("query");
                                break;
                            case DeviceTypeConstant.TYPE.TYPE_REMOTECONTROL:
                                mRemoteControlManager.setmSelectRemoteControlDevice(smartDevices.get(i));
                                mRemoteControlManager.queryStatu();
                                break;
                            case DeviceTypeConstant.TYPE.TYPE_LIGHT:
                                mSmartLightManager.setCurrentSelectLight(smartDevices.get(i));
                                mSmartLightManager.queryLightStatus();
                                break;
                            case DeviceTypeConstant.TYPE.TYPE_LOCK:
                                mSmartLockManager.setCurrentSelectLock(smartDevices.get(i));
                                mSmartLockManager.queryLockStatu();
                                break;
                            case "LKRT":
                                break;
                            case "SMART_BELL":
                                break;
                        }
                    }

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
     * 查询设备列表
     */
    public void queryDeviceListHttp() {
        userName = Perfence.getPerfence(Perfence.PERFENCE_PHONE);
        Log.i(TAG, "queryDeviceListHttp"+"userName:"+userName);
        if (userName.equals("")) {
            return;
        }
        RestfulToolsHomeGeniusString.getSingleton().getDeviceInfo(userName, new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                Log.i(TAG, "" + response.message());
                if (response.code() == 200) {
                    ArrayList<Deviceprops> list = JsonArrayParseUtil.jsonToArrayList(response.body(), Deviceprops.class);
                    //网关设备要先保存
                    Collections.sort(list, new Comparator<Deviceprops>() {
                        @Override
                        public int compare(Deviceprops o1, Deviceprops o2) {
                            //compareTo就是比较两个值，如果前者大于后者，返回1，等于返回0，小于返回-1
                            //重小到大排序的相反
                            if (o1.getDevice_type().equalsIgnoreCase(o2.getDevice_type())) {
                                return 0;
                            }
                            if (o1.getDevice_type().equalsIgnoreCase("LKSGW") && !o2.getDevice_type().equalsIgnoreCase("LKSGW")) {
                                return -1;
                            }
                            if (!o1.getDevice_type().equalsIgnoreCase("LKSGW") && o2.getDevice_type().equalsIgnoreCase("LKSGW")) {
                                return 1;
                            }

                            return 0;
                        }
                    });
                    for (int i = 0; i < list.size(); i++) {
                        Log.i(TAG, "device=" + list.get(i).toString());
                    }
                    for (int i = 0; i < mDeviceListenerList.size(); i++) {
                        mDeviceListenerList.get(i).responseQueryHttpResult(list);
                    }
                }
            }
            @Override
            public void onFailure(Call<String> call, Throwable t) {

            }
        });
    }
    public void addDeviceHttp(DeviceAddBody device) {
        if (userName.equals("")) {
            return;
        }
        Log.i(TAG, device.toString());
        RestfulToolsHomeGenius.getSingleton().addDevice(userName, device, new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                Log.i(TAG, "" + response.message());
                DeviceOperationResponse deviceOperationResponse = null;
                Gson gson = new Gson();
                if (response.body() != null) {

                    deviceOperationResponse =
                            gson.fromJson(response.body().toString(), DeviceOperationResponse.class);
                }
                if (response.code() == 200) {
                    Log.i(TAG, "" + response.body().toString() + "返回结果" + (deviceOperationResponse != null ? deviceOperationResponse.toString() : null));
                    for (int i = 0; i < mDeviceListenerList.size(); i++) {
                        mDeviceListenerList.get(i).responseAddDeviceHttpResult(
                                deviceOperationResponse
                        );
                    }
                } else if (response.code() == 403) {
                    if (response.errorBody() != null) {
                        try {
                            String errorString=response.errorBody().string();
                            Log.i(TAG, "" + errorString);
                           ErrorBody errorBody= gson.fromJson(errorString, ErrorBody.class);
                            if(errorBody.getMsg().equalsIgnoreCase("token invalid or expired, please login")){
                                Ftoast.create(mContext).setText("登录已过期,请重新登录").show();
                                Perfence.setPerfence(AppConstant.USER_LOGIN, false);
                                DataSupport.deleteAll(SmartDev.class);
                                DataSupport.deleteAll(GatwayDevice.class);
                                DataSupport.deleteAll(Room.class);
                                DataSupport.deleteAll(Record.class);
                                DataSupport.deleteAll(Router.class);
                                mContext.startActivity(new Intent(mContext, LoginActivity.class));
                            }else{
                                Ftoast.create(mContext).setText("没有授权,请让第一次添加此设备的用户给你授权").show();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.i(TAG, "" + t.getMessage());
            }
        });
    }

    public void addVirtualDeviceHttp(VirtualDeviceAddBody device) {
        if (userName.equals("")) {
            return;
        }
        Log.i(TAG, device.toString());
        RestfulToolsHomeGenius.getSingleton().addVirtualDevice(userName, device, new Callback<DeviceOperationResponse>() {
            @Override
            public void onResponse(Call<DeviceOperationResponse> call, Response<DeviceOperationResponse> response) {
                Log.i(TAG, "" + response.message());
                if (response.code() == 200) {
                    Log.i(TAG, "" + response.body().toString());
                    for (int i = 0; i < mDeviceListenerList.size(); i++) {
                        mDeviceListenerList.get(i).responseAddVirtualDeviceHttp(
                                response.body()
                        );
                    }
                }
            }

            @Override
            public void onFailure(Call<DeviceOperationResponse> call, Throwable t) {
                Log.i(TAG, "" + t.getMessage());
            }
        });
    }

    public void deleteDeviceHttp() {
        String uid = currentSelectSmartDevice.getUid();
        if (userName.equals("")) {
            return;
        }
        Deviceprops device = new Deviceprops();
        if (uid != null) {
            device.setUid(uid);
        }
        RestfulToolsHomeGenius.getSingleton().deleteDevice(userName, uid, new Callback<DeviceOperationResponse>() {
            @Override
            public void onResponse(Call<DeviceOperationResponse> call, Response<DeviceOperationResponse> response) {
                Log.i(TAG, "" + response.message());
                if (response.code() == 200) {
                    Log.i(TAG, "" + response.body().toString());
                    for (int i = 0; i < mDeviceListenerList.size(); i++) {
                        mDeviceListenerList.get(i).responseDeleteDeviceHttpResult(response.body());
                    }
                }else if(response.code() ==403){
                    Ftoast.create(mContext).setText("登录已过期,请重新登录").show();
                    Perfence.setPerfence(AppConstant.USER_LOGIN, false);
                    DataSupport.deleteAll(SmartDev.class);
                    DataSupport.deleteAll(GatwayDevice.class);
                    DataSupport.deleteAll(Room.class);
                    DataSupport.deleteAll(Record.class);
                    DataSupport.deleteAll(Router.class);
                    mContext.startActivity(new Intent(mContext, LoginActivity.class));
                }
            }

            @Override
            public void onFailure(Call<DeviceOperationResponse> call, Throwable t) {
                Log.i(TAG, "" + t.getMessage());
            }
        });
    }

    /**
     * 修改设备属性
     */
    public void alertDeviceHttp(String uid, String room_uid, String device_name, String gw_uid) {
        if (userName.equals("")) {
            return;
        }
        Deviceprops device = new Deviceprops();
        device.setUid(uid);
        if (gw_uid != null) {
            device.setGw_uid(gw_uid);
        }
        if (room_uid != null) {
            device.setRoom_uid(room_uid);
        }
        if (device_name != null) {
            device.setDevice_name(device_name);
        }
        Log.i(TAG, "alert device:" + device.toString());
        RestfulToolsHomeGenius.getSingleton().alertDevice(userName, device, new Callback<DeviceOperationResponse>() {
            @Override
            public void onResponse(Call<DeviceOperationResponse> call, Response<DeviceOperationResponse> response) {
                Log.i(TAG, "" + response.message());
                if (response.errorBody() != null) {
                    try {
                        Log.i(TAG, "" + response.errorBody().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (response.code() == 200) {
                    Log.i(TAG, "" + response.body().toString());
                    for (int i = 0; i < mDeviceListenerList.size(); i++) {
                        Log.i(TAG, "alert device:" + mDeviceListenerList.get(i).toString());
                        mDeviceListenerList.get(i).responseAlertDeviceHttpResult(response.body());
                    }
                }else if(response.code() == 403){
                    Ftoast.create(mContext).setText("登录已过期,请重新登录").show();
                    Perfence.setPerfence(AppConstant.USER_LOGIN, false);
                    DataSupport.deleteAll(SmartDev.class);
                    DataSupport.deleteAll(GatwayDevice.class);
                    DataSupport.deleteAll(Room.class);
                    DataSupport.deleteAll(Record.class);
                    DataSupport.deleteAll(Router.class);
                    mContext.startActivity(new Intent(mContext, LoginActivity.class));
                }
            }

            @Override
            public void onFailure(Call<DeviceOperationResponse> call, Throwable t) {
                Log.i(TAG, "" + t.getMessage());
            }
        });
    }

    /**
     * 读设备属性
     */
    public void readDeviceInfoHttp(String uid) {
        if (userName.equals("")) {
            return;
        }
        RestfulToolsHomeGeniusString.getSingleton().readDeviceInfo(userName, uid, new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                Log.i(TAG, "" + response.message());
                if (response.code() == 200) {
                    Log.i(TAG, "" + response.body());
                    for (int i = 0; i < mDeviceListenerList.size(); i++) {
                        mDeviceListenerList.get(i).responseGetDeviceInfoHttpResult(response.body());
                    }
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.i(TAG, "" + t.getMessage());
            }
        });
    }

    /**
     * 读设备分享信息
     */
    public void readDeviceShareInfo(String uid) {
        if (userName.equals("")) {
            return;
        }
        RestfulToolsHomeGeniusString.getSingleton().getDeviceShareInfo(userName, uid, new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                Log.i(TAG, "" + response.message());
                if (response.code() == 200) {
                    Log.i(TAG, "" + response.body());
                    for (int i = 0; i < mDeviceListenerList.size(); i++) {
                        mDeviceListenerList.get(i).responseGetDeviceShareInfo(response.body());
                    }
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.i(TAG, "" + t.getMessage());
            }
        });
    }

    public void shareDevice(String uid, ShareDeviceBody body) {
        if (userName.equals("")) {
            return;
        }
        RestfulToolsHomeGenius.getSingleton().shareDevice(userName, uid, body, new Callback<DeviceOperationResponse>() {
            @Override
            public void onResponse(Call<DeviceOperationResponse> call, Response<DeviceOperationResponse> response) {
                Log.i(TAG, "" + response.message());
                if (response.code() == 200) {
                    Log.i(TAG, "" + response.body());
                    for (int i = 0; i < mDeviceListenerList.size(); i++) {
                        mDeviceListenerList.get(i).responseDeviceShareResult(response.body());
                    }
                }
            }

            @Override
            public void onFailure(Call<DeviceOperationResponse> call, Throwable t) {
                Log.i(TAG, "onFailure:" + t.getMessage());
                t.printStackTrace();
            }
        });
    }

    public void cancelDeviceShare(String uid, int shareid) {
        if (userName.equals("")) {
            return;
        }
        RestfulToolsHomeGenius.getSingleton().cancelDeviceShare(userName, shareid, uid, new Callback<DeviceOperationResponse>() {
            @Override
            public void onResponse(Call<DeviceOperationResponse> call, Response<DeviceOperationResponse> response) {
                Log.i(TAG, "" + response.message());
                if (response.code() == 200) {
                    Log.i(TAG, "" + response.body());
                    for (int i = 0; i < mDeviceListenerList.size(); i++) {
                        mDeviceListenerList.get(i).responseCancelDeviceShare(response.body());
                    }
                }
            }

            @Override
            public void onFailure(Call<DeviceOperationResponse> call, Throwable t) {
                Log.i(TAG, "onFailure:" + t.getMessage());
                t.printStackTrace();
            }
        });
    }

    /**
     * 初始化本地连接管理器
     */
    public void InitDeviceManager(Context context) {
        this.mContext = context;
        this.mDeviceListenerList = new ArrayList<>();
        if (mLocalConnectmanager == null) {
            mLocalConnectmanager = LocalConnectmanager.getInstance();
        }
        if (mRemoteConnectManager == null) {
            mRemoteConnectManager = RemoteConnectManager.getInstance();
            mRemoteConnectManager.InitRemoteConnectManager(mContext);
        }
        if (mSmartLockManager == null) {
            mSmartLockManager = SmartLockManager.getInstance();
            mSmartLockManager.InitSmartLockManager(mContext);
        }
        if (mSmartSwitchManager == null) {
            mSmartSwitchManager = SmartSwitchManager.getInstance();
            mSmartSwitchManager.InitSmartSwitchManager(mContext);
        }
        if (mSmartLightManager == null) {
            mSmartLightManager = SmartLightManager.getInstance();
            mSmartLightManager.InitSmartLightManager(mContext);
        }
        if (mRemoteControlManager == null) {
            mRemoteControlManager = RemoteControlManager.getInstance();
            mRemoteControlManager.InitRemoteControlManager(mContext);
        }
        if (mHomeGenius == null) {
            mHomeGenius = new HomeGenius();
        }
        if (manager == null) {
            initMqttCallback();
        }
        timertaskIsScheduled=false;
        mLocalConnectmanager.addLocalConnectListener(this);
        packet = new GeneralPacket(mContext);
        cachedThreadPool = Executors.newCachedThreadPool();
    }

    public HashMap<String, String> getmDevicesStatus() {
        return mDevicesStatus;
    }

    private void initMqttCallback() {
        DeplinkSDK.initSDK(mContext, Perfence.SDK_APP_KEY);
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
                    Log.i(TAG, "notifyHomeGeniusResponse=" + result);
                    Gson gson = new Gson();
                    DeviceList aDeviceList = gson.fromJson(result, DeviceList.class);
                    List<GatwayDevice> devices = aDeviceList.getDevice();
                    for (int i = 0; i < devices.size(); i++) {
                        mDevicesStatus.put(devices.get(i).getUid(), devices.get(i).getStatus());
                    }
                } else {
                    {
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
                                    mDevicesStatus.put(content.getSmartUid(), "在线");
                                    virtualDeviceUpdate();
                                } else if (content.getMethod().equalsIgnoreCase("SmartWallSwitch")) {
                                    mDevicesStatus.put(content.getSmartUid(), "在线");
                                } else if (content.getMethod().equalsIgnoreCase("YWLIGHTCONTROL")) {
                                    mDevicesStatus.put(content.getSmartUid(), "在线");

                                } else if (content.getMethod().equalsIgnoreCase("SMART_LOCK")) {
                                    mDevicesStatus.put(content.getSmartUid(), "在线");
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

            }
        };
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
     * 查询wifi列表
     * 返回:{ "OP": "REPORT", "Method": "WIFIRELAY", "SSIDList": [ ] }
     */
    public void queryWifiList() {
        Log.i(TAG, "queryWifiList" + mLocalConnectmanager.isLocalconnectAvailable());
        if (mLocalConnectmanager.isLocalconnectAvailable()) {
            QueryOptions query = new QueryOptions();
            query.setOP("QUERY");
            query.setMethod("WIFIRELAY");
            query.setTimestamp();
            Gson gson = new Gson();
            String text = gson.toJson(query);
            packet.packQueryWifiListData(text.getBytes());
            cachedThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    mLocalConnectmanager.getOut(packet.data);
                }
            });
        } else {
            List<GatwayDevice> devices = DataSupport.findAll(GatwayDevice.class);
            for (int i = 0; i < devices.size(); i++) {
                if (devices.get(i).getTopic() != null && !devices.get(i).getTopic().equals("")) {
                    Log.i(TAG, "远程接口查询设备列表" + "topic" + devices.get(i).getTopic());
                    Log.i(TAG, "device.getTopic()=" + devices.get(i).getTopic());
                    //在线网关,查询wifi中继列表
                    if (devices.get(i).getUid().equalsIgnoreCase(GetwayManager.getInstance().getCurrentSelectGetwayDevice().getUid())) {
                        mHomeGenius.queryWifiList(devices.get(i).getTopic(), uuid);
                    }
                }
            }
        }
    }


    /**
     * 绑定智能设备列表
     * {"org":"ismart","tp":"SMART_LOCK","ad":"00-12-4b-00-0b-26-c2-15","ver":"1"}
     *
     * @param smartDevice 智能设备（除去网关设备：中继器）
     */
    public void bindSmartDevList(QrcodeSmartDevice smartDevice) {
        if (mLocalConnectmanager.isLocalconnectAvailable()) {
            QueryOptions queryCmd = new QueryOptions();
            queryCmd.setOP("SET");
            queryCmd.setMethod("DevList");
            queryCmd.setTimestamp();
            List<SmartDev> devs = new ArrayList<>();
            //设备赋值
            SmartDev dev = new SmartDev();
            dev.setSmartUid(smartDevice.getAd());
            dev.setOrg(smartDevice.getOrg());
            Log.i(TAG, "bindSmartDevList type=" + smartDevice.getTp() + "getVer=" + smartDevice.getVer() + "smartDeviceUID=" + smartDevice.getAd());
            dev.setType(smartDevice.getTp());
            dev.setVer(smartDevice.getVer());
            //设备列表添加一个设备
            devs.add(dev);
            queryCmd.setSmartDev(devs);
            Gson gson = new Gson();
            String text = gson.toJson(queryCmd);
            packet.packSendSmartDevsData(text.getBytes());
            cachedThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    mLocalConnectmanager.getOut(packet.data);
                }
            });
        } else {
            List<GatwayDevice> devices = DataSupport.findAll(GatwayDevice.class);
            for (int i = 0; i < devices.size(); i++) {
                if (devices.get(i).getTopic() != null && !devices.get(i).getTopic().equals("")) {
                    Log.i(TAG, "远程接口查询设备列表" + "topic" + devices.get(i).getTopic());
                    Log.i(TAG, "device.getTopic()=" + devices.get(i).getTopic());
                    mHomeGenius.bindSmartDevList(devices.get(i).getTopic(), uuid, smartDevice);
                }
            }
        }

    }

    /**
     * 如果数据库中没有这个设备，需要修改数据库
     * 如果有这个设备就不处理
     * 添加智能设备成功，需要更新数据库
     */
    public boolean addDBSmartDevice(QrcodeSmartDevice device, String uid, GatwayDevice getwayDevice) {
        //查询设备
        SmartDev smartDev = DataSupport.where("Uid=?", uid).findFirst(SmartDev.class);
        if (smartDev == null) {
            smartDev = new SmartDev();
            smartDev.setUid(uid);
            smartDev.setOrg(device.getOrg());
            smartDev.setVer(device.getVer());
            smartDev.setType(device.getTp());
            smartDev.setGetwayDevice(getwayDevice);
            smartDev.setName(device.getName());
            smartDev.setMac(device.getAd().toLowerCase());
            boolean addResult = smartDev.save();
            Log.i(TAG, "向数据库中添加一条智能设备数据=" + addResult);
            return addResult;
        }
        Log.i(TAG, "数据库中已存在相同设备，不必要添加");
        return false;
    }

    public SmartDev getCurrentSelectSmartDevice() {
        return currentSelectSmartDevice;
    }

    public void setCurrentSelectSmartDevice(SmartDev currentSelectSmartDevice) {
        this.currentSelectSmartDevice = currentSelectSmartDevice;
    }

    /**
     * 查找所有的智能设备
     */
    public List<SmartDev> findAllSmartDevice() {
        List<SmartDev> smartDevices = DataSupport.findAll(SmartDev.class);
        Log.i(TAG, "查找所有的智能设备,设备个数=" + smartDevices.size());
        return smartDevices;
    }
    /**
     * 删除数据库中的一个智能设备
     */
    public int deleteDBSmartDevice(String uid) {
        int affectcolumn = DataSupport.deleteAll(SmartDev.class, "Uid=?", uid);
        Log.i(TAG, "删除一个智能设备，删除影响的行数=" + affectcolumn);
        return affectcolumn;
    }
    /**
     * 解除绑定
     * 解除绑定后根据返回结果更新数据库
     */
    public void deleteSmartDevice() {
        QueryOptions queryCmd = new QueryOptions();
        queryCmd.setOP("DELETE");
        queryCmd.setMethod("DevList");
        queryCmd.setTimestamp();
        queryCmd.setSmartUid(currentSelectSmartDevice.getMac());
        List<SmartDev> devs = new ArrayList<>();
        //设备赋值
        SmartDev dev = new SmartDev();
        dev.setUid(currentSelectSmartDevice.getMac());
        dev.setOrg(currentSelectSmartDevice.getOrg());
        dev.setType(currentSelectSmartDevice.getType());
        dev.setVer(currentSelectSmartDevice.getVer());
        devs.add(dev);
        queryCmd.setSmartDev(devs);
        Gson gson = new Gson();
        String text = gson.toJson(queryCmd);
        packet.packSendSmartDevsData(text.getBytes());
        cachedThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                mLocalConnectmanager.getOut(packet.data);
            }
        });
    }

    /**
     * 更新设备所在房间
     */
    public void updateSmartDeviceInWhatRoom(Room room, String deviceUid, String deviceName) {
        Log.i(TAG, "更新智能设备所在的房间=start" + "room=" + (room != null));
        //保存所在的房间
        //查询设备
        SmartDev smartDev = DataSupport.where("Uid=?", deviceUid).findFirst(SmartDev.class, true);
        //找到要更行的设备,设置关联的房间
        List<Room> rooms = new ArrayList<>();
        if (room != null) {
            rooms.add(room);
        } else {
            rooms.addAll(RoomManager.getInstance().getmRooms());
            Log.i(TAG, "房间列表大小" + rooms.size());
        }
        smartDev.setRooms(rooms);
        smartDev.setName(deviceName);
        smartDev.save();
    }


    @Override
    public void OnGetQueryresult(String result) {
        //返回查询结果：设备列表
        Log.i(TAG, "返回查询结果：设备列表=" + result);
        //保存智能锁设备的DevUid
        Gson gson = new Gson();
        if (result.contains("DevList")) {
            DeviceList aDeviceList = gson.fromJson(result, DeviceList.class);
            List<GatwayDevice> devices = aDeviceList.getDevice();
            for (int i = 0; i < devices.size(); i++) {
                mDevicesStatus.put(devices.get(i).getUid(), devices.get(i).getStatus());
            }
            for (int i = 0; i < mDeviceListenerList.size(); i++) {
                mDeviceListenerList.get(i).responseQueryResult(result);
            }
        }
        OpResult type = gson.fromJson(result, OpResult.class);
        if (type.getOP().equalsIgnoreCase("REPORT")
                ) {
            if ((type.getMethod().equalsIgnoreCase("SMART_LOCK") || type.getMethod().equalsIgnoreCase("SmartLock"))) {
                if (type.getCommand().equalsIgnoreCase(SmartLockConstant.CMD.QUERY)) {
                    if (type.getSmartUid() != null) {
                        if (mDevicesStatus != null) {
                            mDevicesStatus.put(type.getSmartUid(), "在线");
                        }
                    }
                }
            } else if (type.getMethod().equalsIgnoreCase("SmartWallSwitch")) {
                if (type.getCommand().equalsIgnoreCase(SmartLockConstant.CMD.QUERY)) {
                    if (type.getSmartUid() != null) {
                        if (mDevicesStatus != null) {
                          if(type.getResult()!=-1){
                              mDevicesStatus.put(type.getSmartUid(), "在线");
                          }else{
                              mDevicesStatus.put(type.getSmartUid(), "在线");
                          }

                        }

                    }

                }
            } else if (type.getMethod().equalsIgnoreCase("YWLIGHTCONTROL")) {
                if (type.getCommand().equalsIgnoreCase(SmartLockConstant.CMD.QUERY)) {
                    if (type.getSmartUid() != null) {
                        if (mDevicesStatus != null) {
                            mDevicesStatus.put(type.getSmartUid(), "在线");
                        }

                    }

                }
            } else if (type.getMethod().equalsIgnoreCase("IrmoteV2")) {
                if (type.getCommand().equalsIgnoreCase(SmartLockConstant.CMD.QUERY)) {
                    if (type.getSmartUid() != null) {
                        if (mDevicesStatus != null) {
                            mDevicesStatus.put(type.getSmartUid(), "在线");
                        }
                    }
                }
            }
        }
    }
    @Override
    public void OnGetSetresult(String result) {
        Gson gson = new Gson();
        OpResult type = gson.fromJson(result, OpResult.class);
        if (type.getOP().equalsIgnoreCase("REPORT")
                ) {
            if (type.getMethod().equalsIgnoreCase("SmartWallSwitch")) {
                if (type.getCommand().equalsIgnoreCase(SmartLockConstant.CMD.QUERY)) {
                    if (type.getSmartUid() != null) {
                        if (mDevicesStatus != null) {
                            mDevicesStatus.put(type.getSmartUid(), "在线");
                        }

                    }

                }
            }else if(type.getMethod().equalsIgnoreCase("YWLIGHTCONTROL")){
                if (type.getCommand().equalsIgnoreCase(SmartLockConstant.CMD.QUERY)) {
                    if (type.getSmartUid() != null) {
                        if (mDevicesStatus != null) {
                            mDevicesStatus.put(type.getSmartUid(), "在线");
                        }

                    }

                }
            }
        }
    }

    @Override
    public void OnGetBindresult(String result) {
        for (int i = 0; i < mDeviceListenerList.size(); i++) {
            mDeviceListenerList.get(i).responseBindDeviceResult(result);
        }
    }

    @Override
    public void getWifiList(String result) {
        Gson gson = new Gson();
        OpResult wifiList = gson.fromJson(result, OpResult.class);
        if (wifiList.getOP().equalsIgnoreCase("REPORT") && wifiList.getMethod().equalsIgnoreCase("WIFIRELAY")) {
            for (int i = 0; i < mDeviceListenerList.size(); i++) {
                mDeviceListenerList.get(i).responseWifiListResult(wifiList.getSSIDList());
            }
        }
    }
    @Override
    public void onSetWifiRelayResult(String result) {
    }
    @Override
    public void onGetalarmRecord(List<Info> alarmList) {
    }
}
