package com.deplink.boruSmart.manager.device.remoteControl;

import android.content.Context;
import android.util.Log;

import com.deplink.boruSmart.Protocol.json.OpResult;
import com.deplink.boruSmart.Protocol.json.QueryOptions;
import com.deplink.boruSmart.Protocol.json.device.lock.alertreport.Info;
import com.deplink.boruSmart.Protocol.packet.GeneralPacket;
import com.deplink.boruSmart.constant.AppConstant;
import com.deplink.boruSmart.constant.DeviceTypeConstant;
import com.deplink.boruSmart.constant.SmartLockConstant;
import com.deplink.boruSmart.manager.connect.remote.HomeGenius;
import com.deplink.boruSmart.manager.connect.remote.RemoteConnectManager;
import com.deplink.boruSmart.manager.room.RoomManager;
import com.deplink.boruSmart.util.Perfence;
import com.deplink.boruSmart.Protocol.json.Room;
import com.deplink.boruSmart.Protocol.json.device.SmartDev;
import com.deplink.boruSmart.Protocol.json.device.getway.GatwayDevice;
import com.deplink.boruSmart.manager.connect.local.tcp.LocalConnectmanager;
import com.deplink.boruSmart.manager.device.DeviceManager;
import com.deplink.boruSmart.util.JsonArrayParseUtil;
import com.deplink.sdk.android.sdk.homegenius.DeviceOperationResponse;
import com.deplink.sdk.android.sdk.homegenius.Deviceprops;
import com.deplink.sdk.android.sdk.homegenius.VirtualDeviceAlertBody;
import com.deplink.sdk.android.sdk.rest.RestfulToolsHomeGenius;
import com.deplink.sdk.android.sdk.rest.RestfulToolsHomeGeniusString;
import com.google.gson.Gson;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Administrator on 2017/11/22.
 * private RemoteControlManager mRemoteControlManager;
 * mRemoteControlManager=RemoteControlManager.getInstance();
 * mRemoteControlManager.InitRemoteControlManager(this,this);
 */
public class RemoteControlManager extends DeviceManager {
    private static final String TAG = "RemoteControlManager";
    /**
     * 这个类设计成单例
     */
    private static RemoteControlManager instance;
    private Context mContext;
    private List<RemoteControlListener> mRemoteControlListenerList;
    private Gson gson;
    private List<SmartDev> mRemoteControlDeviceList;
    /**
     * 当前选中的遥控设备
     */
    private SmartDev mSelectRemoteControlDevice;
    private int currentLearnByHandKeyName;
    private String currentLearnByHandTypeName;
    private static String uuid ;
    public String getCurrentLearnByHandTypeName() {
        return currentLearnByHandTypeName;
    }

    public void setCurrentLearnByHandTypeName(String currentLearnByHandTypeName) {
        this.currentLearnByHandTypeName = currentLearnByHandTypeName;
    }

    public int getCurrentLearnByHandKeyName() {
        return currentLearnByHandKeyName;
    }

    public void setCurrentLearnByHandKeyName(int currentLearnByHandKeyName) {
        this.currentLearnByHandKeyName = currentLearnByHandKeyName;
    }
    private boolean currentActionIsAddactionQuickLearn;

    public boolean isCurrentActionIsAddactionQuickLearn() {
        return currentActionIsAddactionQuickLearn;
    }

    public void setCurrentActionIsAddactionQuickLearn(boolean currentActionIsAddactionQuickLearn) {
        this.currentActionIsAddactionQuickLearn = currentActionIsAddactionQuickLearn;
    }

    private boolean currentActionIsAddDevice;

    /**
     * 当前是添加设备还是配置遥控器按键列表（快速学习）
     *
     * @return
     */
    public boolean isCurrentActionIsAddDevice() {
        return currentActionIsAddDevice;
    }

    public void setCurrentActionIsAddDevice(boolean currentActionIsAddDevice) {
        this.currentActionIsAddDevice = currentActionIsAddDevice;
    }

    public SmartDev getmSelectRemoteControlDevice() {
        Log.i(TAG, "mSelectRemoteControlDevice=" + (mSelectRemoteControlDevice != null));
        return mSelectRemoteControlDevice;
    }

    public void setmSelectRemoteControlDevice(SmartDev mSelectRemoteControlDevice) {
        this.mSelectRemoteControlDevice = mSelectRemoteControlDevice;
    }

    public List<SmartDev> findAllRemotecontrolDevice() {
        return DataSupport.where("Type = ?", DeviceTypeConstant.TYPE.TYPE_REMOTECONTROL).find(SmartDev.class);
    }

    /**
     * 找到遥控器挂载在哪个物理遥控器下面
     *
     * @return
     */
    public List<SmartDev> findRemotecontrolDevice() {
        return DataSupport.where("Uid = ?", mSelectRemoteControlDevice.getRemotecontrolUid()).find(SmartDev.class);
    }

    public static synchronized RemoteControlManager getInstance() {
        if (instance == null) {
            instance = new RemoteControlManager();
        }
        uuid= Perfence.getPerfence(AppConstant.PERFENCE_BIND_APP_UUID);
        return instance;
    }

    public void InitRemoteControlManager(Context context) {
        this.mContext = context;
        if (mLocalConnectmanager == null) {
            mLocalConnectmanager = LocalConnectmanager.getInstance();
            mLocalConnectmanager.InitLocalConnectManager(context, uuid);

        }
        if (mRemoteConnectManager == null) {
            mRemoteConnectManager = RemoteConnectManager.getInstance();
            mRemoteConnectManager.InitRemoteConnectManager(mContext);
        }

        if (mHomeGenius == null) {
            mHomeGenius = new HomeGenius();
        }
        mLocalConnectmanager.addLocalConnectListener(this);
        packet = new GeneralPacket(mContext);
        mRemoteControlListenerList = new ArrayList<>();
        gson = new Gson();
        mRemoteControlDeviceList = new ArrayList<>();
        mRemoteControlDeviceList.addAll(DataSupport.where("Type=?", "IRMOTE_V2").find(SmartDev.class));
        if (cachedThreadPool == null) {
            cachedThreadPool = Executors.newCachedThreadPool();
        }

    }

    /**
     * 更新设备所在房间
     */
    public void updateSmartDeviceInWhatRoom(Room room, String deviceUid) {
        //查询设备
        SmartDev smartDev = DataSupport.where("Uid=?", deviceUid).findFirst(SmartDev.class, true);
        //找到要更行的设备,设置关联的房间
        List<Room> rooms = new ArrayList<>();
        if (room != null) {
            rooms.add(room);
        } else {
            rooms.addAll(RoomManager.getInstance().getmRooms());
        }
        mSelectRemoteControlDevice.setRooms(rooms);
        smartDev.setRooms(rooms);
        smartDev.save();
    }

    public void study() {
        if (mLocalConnectmanager.isLocalconnectAvailable()) {
            QueryOptions cmd = new QueryOptions();
            cmd.setOP("SET");
            cmd.setMethod("IrmoteV2");
            cmd.setTimestamp();
            cmd.setSmartUid(mSelectRemoteControlDevice.getMac());
            cmd.setCommand("Study");
            String text = gson.toJson(cmd);
            packet.packRemoteControlData(text.getBytes(), mSelectRemoteControlDevice.getMac());
            cachedThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    mLocalConnectmanager.getOut(packet.data);
                }
            });
        } else {
            List<GatwayDevice> devices = DataSupport.findAll(GatwayDevice.class);
            for(int i=0;i<devices.size();i++){
                if(devices.get(i).getTopic()!=null && !devices.get(i).getTopic().equals("")){
                    Log.i(TAG, "device.getTopic()=" + devices.get(i).getTopic());
                    if(devices.get(i).getStatus().equalsIgnoreCase("on")||devices.get(i).getStatus().equalsIgnoreCase("在线")){
                        mHomeGenius.study(mSelectRemoteControlDevice, devices.get(i).getTopic(), uuid);
                    }

                }
            }
        }
    }

    public void stopStudy() {
        if (mLocalConnectmanager.isLocalconnectAvailable()) {
            QueryOptions cmd = new QueryOptions();
            cmd.setOP("SET");
            cmd.setMethod("IrmoteV2");
            cmd.setTimestamp();
            cmd.setSmartUid(mSelectRemoteControlDevice.getMac());
            cmd.setCommand("Quit");
            String text = gson.toJson(cmd);
            packet.packRemoteControlData(text.getBytes(), mSelectRemoteControlDevice.getMac());
            cachedThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    mLocalConnectmanager.getOut(packet.data);
                }
            });
        } else {
            List<GatwayDevice> devices = DataSupport.findAll(GatwayDevice.class);
            for(int i=0;i<devices.size();i++){
                if(devices.get(i).getTopic()!=null && !devices.get(i).getTopic().equals("")){
                    if(devices.get(i).getStatus().equalsIgnoreCase("on")||devices.get(i).getStatus().equalsIgnoreCase("在线")){
                        Log.i(TAG, "device.getTopic()=" + devices.get(i).getTopic());
                        mHomeGenius.stopStudy(mSelectRemoteControlDevice, devices.get(i).getTopic(), uuid);
                    }
                }
            }
        }

    }

    public void queryStatu() {
        Log.i(TAG, "查询遥控器设备状态");
        if (mLocalConnectmanager.isLocalconnectAvailable()) {
            QueryOptions queryCmd = new QueryOptions();
            queryCmd.setOP("SET");
            queryCmd.setMethod("IrmoteV2");
            queryCmd.setCommand("query");
            queryCmd.setSmartUid(mSelectRemoteControlDevice.getMac());
            queryCmd.setTimestamp();
            Gson gson = new Gson();
            String text = gson.toJson(queryCmd);
            packet.packOpenLockListData(text.getBytes(), mSelectRemoteControlDevice.getUid());
            cachedThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    mLocalConnectmanager.getOut(packet.data);
                }
            });
        } else {
            GatwayDevice device = mSelectRemoteControlDevice.getGetwayDevice();
            if (device == null) {
                device = DataSupport.where("Status = ?", "在线").findFirst(GatwayDevice.class);
            }
            if(device==null){
                device = DataSupport.findFirst(GatwayDevice.class);
            }
            if (device != null) {
                Log.i(TAG, "device.getTopic()=" + device.getTopic());
                if (device.getTopic() != null && !device.getTopic().equals("")) {
                    mHomeGenius.queryRemoteControlStatu(mSelectRemoteControlDevice, device.getTopic(), uuid);
                }
            }
        }
    }

    public void sendData(String data) {
        if (mLocalConnectmanager.isLocalconnectAvailable()) {
            Log.i(TAG, "mSelectRemoteControlDevice mac=" + mSelectRemoteControlDevice.getMac());
            String controlUid = mSelectRemoteControlDevice.getMac();
            if (mSelectRemoteControlDevice.getMac() == null) {
                controlUid = mSelectRemoteControlDevice.getMac();
            }
            QueryOptions cmd = new QueryOptions();
            cmd.setOP("SET");
            cmd.setMethod("IrmoteV2");
            cmd.setTimestamp();
            cmd.setSmartUid(controlUid);
            cmd.setCommand("Send");
            cmd.setData(data);
            String text = gson.toJson(cmd);
            Log.i(TAG, "controlUid=" + controlUid);
            packet.packRemoteControlData(text.getBytes(), controlUid);
            cachedThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    mLocalConnectmanager.getOut(packet.data);
                }
            });
        } else {
            List<GatwayDevice> devices = DataSupport.findAll(GatwayDevice.class);
            for(int i=0;i<devices.size();i++){
                if(devices.get(i).getTopic()!=null && !devices.get(i).getTopic().equals("")){
                    Log.i(TAG, "device.getTopic()=" + devices.get(i).getTopic());
                    if(devices.get(i).getStatus().equalsIgnoreCase("on")||devices.get(i).getStatus().equalsIgnoreCase("在线")){
                        mHomeGenius.sendData(mSelectRemoteControlDevice, devices.get(i).getTopic(), uuid, data);
                    }
                }
            }
        }
    }

    public void deleteVirtualDeviceHttp() {
        String uid = mSelectRemoteControlDevice.getUid();
        String userName = Perfence.getPerfence(Perfence.PERFENCE_PHONE);
        if (userName.equals("")) {
            return;
        }
        Deviceprops device = new Deviceprops();
        if (uid != null) {
            device.setUid(uid);
        }
        RestfulToolsHomeGenius.getSingleton().deleteVirtualDevice(userName, uid, new Callback<DeviceOperationResponse>() {
            @Override
            public void onResponse(Call<DeviceOperationResponse> call, Response<DeviceOperationResponse> response) {
                Log.i(TAG, "" + response.message());
                if (response.code() == 200) {
                    Log.i(TAG, "" + response.body().toString());
                    for (int i = 0; i < mRemoteControlListenerList.size(); i++) {
                        mRemoteControlListenerList.get(i).responseDeleteVirtualDevice(response.body());
                    }
                }
            }

            @Override
            public void onFailure(Call<DeviceOperationResponse> call, Throwable t) {
                Log.i(TAG, "" + t.getMessage());
            }
        });
    }

    /**
     * 查询设备列表
     */
    public void queryVirtualDeviceList() {
        String userName = Perfence.getPerfence(Perfence.PERFENCE_PHONE);
        Log.i(TAG, "查询虚拟设备:" + userName);
        if (userName.equals("")) {
            return;
        }
        RestfulToolsHomeGeniusString.getSingleton().readVirtualDevices(userName, new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                Log.i(TAG, "查询虚拟设备返回:" + response.message());
                if (response.code() == 200) {
                    ArrayList<DeviceOperationResponse> list = JsonArrayParseUtil.jsonToArrayList(response.body(), DeviceOperationResponse.class);
                    for (int i = 0; i < list.size(); i++) {
                        Log.i(TAG, "device=" + list.get(i).toString());
                    }
                    for (int i = 0; i < mRemoteControlListenerList.size(); i++) {
                        mRemoteControlListenerList.get(i).responseQueryVirtualDevices(list);
                    }
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {

            }
        });
    }

    /**
     * 修改设备属性
     */
    public void alertVirtualDevice(String uid, String device_name, String key_codes, String iremote_uid) {
        String userName = Perfence.getPerfence(Perfence.PERFENCE_PHONE);
        if (userName.equals("")) {
            return;
        }
        VirtualDeviceAlertBody device = new VirtualDeviceAlertBody();
        if (uid != null) {
            device.setUid(uid);
        }
        if (device_name != null) {
            device.setDevice_name(device_name);
        }
        if (key_codes != null) {
            device.setKey_codes(key_codes);
        }
        if (iremote_uid != null) {
            device.setIremote_uid(iremote_uid);
        }
        Log.i(TAG, "alert device:" + device.toString());
        RestfulToolsHomeGenius.getSingleton().alertVirtualDevice(userName, device, new Callback<DeviceOperationResponse>() {
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
                    for (int i = 0; i < mRemoteControlListenerList.size(); i++) {
                        mRemoteControlListenerList.get(i).responseAlertVirtualDevice(response.body());
                    }
                }
            }

            @Override
            public void onFailure(Call<DeviceOperationResponse> call, Throwable t) {
                Log.i(TAG, "" + t.getMessage());
            }
        });
    }

    /**
     * 如果数据库中没有这个设备，需要修改数据库
     * 如果有这个设备就不处理
     * 添加智能设备成功，需要更新数据库
     */
    public boolean addDeviceDbLocal(SmartDev device, Room atRoom) {
        List<Room> rooms = new ArrayList<>();
        if (atRoom == null) {
            rooms.addAll(RoomManager.getInstance().getmRooms());
        } else {
            rooms.add(atRoom);
        }
        device.setRooms(rooms);
        device.setStatus("在线");
        return device.saveFast();
    }

    public boolean judgAirconditionDeviceisAdded(String name) {
        //sql 多条件查询
        List<SmartDev> smartDevs = DataSupport.where("Type= ? and name= ? ",
                DeviceTypeConstant.TYPE.TYPE_AIR_REMOTECONTROL, name).find(SmartDev.class);
        return smartDevs.size() > 0;
    }

    public boolean judgTvDeviceisAdded(String name) {
        //sql 多条件查询
        List<SmartDev> smartDevs = DataSupport.where("Type= ? and name= ? ",
                DeviceTypeConstant.TYPE.TYPE_TV_REMOTECONTROL, name).find(SmartDev.class);
        return smartDevs.size() > 0;
    }

    public boolean judgTvBoxDeviceisAdded(String name) {
        //sql 多条件查询
        List<SmartDev> smartDevs = DataSupport.where("Type= ? and name= ? ",
                DeviceTypeConstant.TYPE.TYPE_TVBOX_REMOTECONTROL, name).find(SmartDev.class);
        return smartDevs.size() > 0;
    }

    public boolean updateSmartDeviceGetway(GatwayDevice getwayDevice) {
        mSelectRemoteControlDevice.setGetwayDevice(getwayDevice);
        return mSelectRemoteControlDevice.save();
    }

    public int deleteCurrentSelectDevice() {
        return DataSupport.deleteAll(SmartDev.class, "Uid=?", mSelectRemoteControlDevice.getUid());
    }

    public boolean saveCurrentSelectDeviceName(String name) {
        mSelectRemoteControlDevice.setName(name);
        return mSelectRemoteControlDevice.save();
    }

    /**
     * 修改当前虚拟遥控器绑定的真实物理遥控器的uid
     *
     * @param uid
     * @return
     */
    public boolean saveCurrentVirtualDeviceBindRCUid(String uid) {
        mSelectRemoteControlDevice.setRemotecontrolUid(uid);
        boolean result = mSelectRemoteControlDevice.save();
        Log.i(TAG, "绑定的真实物理遥控器的uid=" + result);
        return result;
    }

    public void addRemoteControlListener(RemoteControlListener listener) {
        if (listener != null && !mRemoteControlListenerList.contains(listener)) {
            this.mRemoteControlListenerList.add(listener);
        }
        mLocalConnectmanager.addLocalConnectListener(this);
    }

    public void removeRemoteControlListener(RemoteControlListener listener) {
        if (listener != null && mRemoteControlListenerList.contains(listener)) {
            this.mRemoteControlListenerList.remove(listener);
        }
    }


    @Override
    public void OnGetQueryresult(String result) {
        Gson gson = new Gson();
        OpResult type = gson.fromJson(result, OpResult.class);
        if (type != null && type.getOP().equalsIgnoreCase("REPORT")) {
            if ((type.getMethod().equalsIgnoreCase("IrmoteV2"))) {
                Log.i(TAG,"遥控器返回数据="+result);
                switch (type.getCommand()) {
                    case SmartLockConstant.CMD.QUERY:
                        for (int i = 0; i < mRemoteControlListenerList.size(); i++) {
                            if(type.getResult()!=-1){
                                mRemoteControlListenerList.get(i).responseOnlineStatu("在线");
                            }else{
                                mRemoteControlListenerList.get(i).responseOnlineStatu("离线");
                            }
                        }
                        break;
                }
            }
        }
    }

    @Override
    public void OnGetSetresult(String setResult) {
        QueryOptions result = gson.fromJson(setResult, QueryOptions.class);
        Log.i(TAG, TAG + ":获取设置结果setResult=" + setResult);
        if (result != null && result.getOP().equalsIgnoreCase("REPORT")) {
            if (setResult.contains("Study")) {
                for (int i = 0; i < mRemoteControlListenerList.size(); i++) {
                    mRemoteControlListenerList.get(i).responseQueryResult(setResult);
                }
            } else {
                for (int i = 0; i < mRemoteControlListenerList.size(); i++) {
                    mRemoteControlListenerList.get(i).responseQueryResult(result.getCommand() + result.getResult());
                }
            }
        }

    }

    @Override
    public void OnGetBindresult(String setResult) {

    }


    @Override
    public void getWifiList(String result) {

    }

    @Override
    public void onSetWifiRelayResult(String result) {

    }

    @Override
    public void onGetalarmRecord(List<Info> alarmList) {

    }

}
