package com.deplink.boruSmart.manager.device.light;

import android.content.Context;
import android.util.Log;

import com.deplink.boruSmart.Protocol.json.QueryOptions;
import com.deplink.boruSmart.Protocol.json.device.lock.alertreport.Info;
import com.deplink.boruSmart.Protocol.json.qrcode.QrcodeSmartDevice;
import com.deplink.boruSmart.Protocol.packet.GeneralPacket;
import com.deplink.boruSmart.constant.DeviceTypeConstant;
import com.deplink.boruSmart.util.Perfence;
import com.deplink.boruSmart.Protocol.json.Room;
import com.deplink.boruSmart.Protocol.json.device.SmartDev;
import com.deplink.boruSmart.Protocol.json.device.getway.GatwayDevice;
import com.deplink.boruSmart.constant.AppConstant;
import com.deplink.boruSmart.manager.connect.local.tcp.LocalConnectmanager;
import com.deplink.boruSmart.manager.connect.remote.HomeGenius;
import com.deplink.boruSmart.manager.device.DeviceManager;
import com.deplink.boruSmart.manager.room.RoomManager;
import com.google.gson.Gson;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

/**
 */
public class SmartLightManager extends DeviceManager {
    private static final String TAG = "SmartLightManager";
    /**
     * 这个类设计成单例
     */
    private static SmartLightManager instance;
    private SmartDev currentSelectLight;
    private SmartLightManager() {

    }
    public SmartDev getCurrentSelectLight() {
        return currentSelectLight;
    }
    private static String uuid ;
    public void setCurrentSelectLight(SmartDev currentSelectLight) {
        this.currentSelectLight = currentSelectLight;
    }

    public static synchronized SmartLightManager getInstance() {
        if (instance == null) {
            instance = new SmartLightManager();
        }
        uuid = Perfence.getPerfence(AppConstant.PERFENCE_BIND_APP_UUID);
        return instance;
    }

    private List<SmartLightListener> mSmartLightListenerList;

    /**
     * 初始化本地连接管理器
     */
    public void InitSmartLightManager(Context context) {
        this.mSmartLightListenerList = new ArrayList<>();
        if (mLocalConnectmanager == null) {
            mLocalConnectmanager = LocalConnectmanager.getInstance();
            String uuid = Perfence.getPerfence(AppConstant.PERFENCE_BIND_APP_UUID);
            mLocalConnectmanager.InitLocalConnectManager(context, uuid);
        }
        if (mHomeGenius == null) {
            mHomeGenius = new HomeGenius();
        }
        mLocalConnectmanager.addLocalConnectListener(this);
        packet = new GeneralPacket(context);
        if (cachedThreadPool == null) {
            cachedThreadPool = Executors.newCachedThreadPool();
        }

    }

    public void addSmartLightListener(SmartLightListener listener) {
        if (listener != null && !mSmartLightListenerList.contains(listener)) {
            this.mSmartLightListenerList.add(listener);
        }
    }

    public void removeSmartLightListener(SmartLightListener listener) {
        if (listener != null && mSmartLightListenerList.contains(listener)) {
            this.mSmartLightListenerList.remove(listener);
        }
    }

    public void releaswSmartManager() {
        mLocalConnectmanager.removeLocalConnectListener(this);
    }

    public void setSmartLightSwitch(String cmd) {
        if (mLocalConnectmanager.isLocalconnectAvailable()) {
            QueryOptions queryCmd = new QueryOptions();
            queryCmd.setOP("SET");
            queryCmd.setMethod("YWLIGHTCONTROL");
            queryCmd.setSmartUid(currentSelectLight.getMac());
            queryCmd.setCommand(cmd);
            Gson gson = new Gson();
            String text = gson.toJson(queryCmd);
            packet.packSetCmdData(text.getBytes(), currentSelectLight.getUid());

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
                        mHomeGenius.setSmartLightSwitch(currentSelectLight, devices.get(i).getTopic(), uuid, cmd);
                    }

                }
            }
        }
    }

    public void setSmartLightParamas(String cmd, int yellow, int white) {
        if (mLocalConnectmanager.isLocalconnectAvailable()) {
            QueryOptions queryCmd = new QueryOptions();
            queryCmd.setOP("SET");
            queryCmd.setMethod("YWLIGHTCONTROL");
            queryCmd.setSmartUid(currentSelectLight.getMac());
            queryCmd.setCommand(cmd);
            queryCmd.setYellow(yellow);
            queryCmd.setWhite(white);
            Gson gson = new Gson();
            String text = gson.toJson(queryCmd);
            packet.packSetCmdData(text.getBytes(), currentSelectLight.getUid());
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
                        mHomeGenius.setSmartLightParamas(currentSelectLight, devices.get(i).getTopic(), uuid, cmd, yellow, white);
                    }

                }
            }
        }
    }

    public void queryLightStatus() {
        if (mLocalConnectmanager.isLocalconnectAvailable()) {
            QueryOptions queryCmd = new QueryOptions();
            queryCmd.setOP("SET");
            queryCmd.setMethod("YWLIGHTCONTROL");
            queryCmd.setCommand("query");
            queryCmd.setSmartUid(currentSelectLight.getMac());
            queryCmd.setTimestamp();
            Gson gson = new Gson();
            String text = gson.toJson(queryCmd);
            packet.packSetCmdData(text.getBytes(), currentSelectLight.getUid());
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
                    mHomeGenius.queryLightStatus(currentSelectLight, devices.get(i).getTopic(), uuid);
                }
            }
        }

    }

    public boolean addDBSwitchDevice(QrcodeSmartDevice device, String addDeviceUid) {
        //查询设备
        SmartDev smartDev = DataSupport.where("Uid=?", device.getAd()).findFirst(SmartDev.class);
        if (smartDev == null) {
            smartDev = new SmartDev();
            smartDev.setUid(addDeviceUid);
            smartDev.setOrg(device.getOrg());
            smartDev.setVer(device.getVer());
            smartDev.setMac(device.getAd().toLowerCase());
            smartDev.setType(DeviceTypeConstant.TYPE.TYPE_LIGHT);
            smartDev.setName(device.getName());
            return smartDev.save();
        }
        Log.i(TAG, "数据库中已存在相同设备，不必要添加");
        return false;
    }

    public boolean updateSmartDeviceGetway(GatwayDevice getwayDevice) {
        Log.i(TAG, "更新智能设备所在的网关=start");
        currentSelectLight.setGetwayDevice(getwayDevice);
        boolean saveResult = currentSelectLight.save();
        Log.i(TAG, "更新智能设备所在的网关=" + saveResult);
        return saveResult;
    }

    /**
     * 更新设备所在房间
     */
    public void updateSmartDeviceRoomAndName(Room room, String deviceUid, String deviceName) {
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
        }
        smartDev.setRooms(rooms);
        smartDev.setName(deviceName);
         smartDev.save();
    }

    public boolean updateSmartDeviceName(String deviceUid, String deviceName) {
        //查询设备
        SmartDev smartDev = DataSupport.where("Uid=?", deviceUid).findFirst(SmartDev.class, true);
        smartDev.setName(deviceName);
        return smartDev.saveFast();
    }

    /**
     * 更新设备所在房间
     */
    public void updateSmartDeviceRoom(Room room, String deviceUid) {
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
       smartDev.save();
    }



    @Override
    public void OnGetQueryresult(String result) {

    }

    /**
     * 门锁开锁，授权操作结果返回
     * 操作结果解释
     *
     * @param setResult
     */
    @Override
    public void OnGetSetresult(String setResult) {
        Log.i(TAG, "setResult=" + setResult);
        for (int i = 0; i < mSmartLightListenerList.size(); i++) {
            mSmartLightListenerList.get(i).responseSetResult(setResult);
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

    /**
     * 获取报警记录
     *
     * @param alarmList
     */
    @Override
    public void onGetalarmRecord(List<Info> alarmList) {

    }
}
