package com.deplink.boruSmart.manager.device.smartswitch;

import android.content.Context;
import android.util.Log;

import com.deplink.boruSmart.Protocol.json.QueryOptions;
import com.deplink.boruSmart.Protocol.json.device.lock.alertreport.Info;
import com.deplink.boruSmart.Protocol.json.qrcode.QrcodeSmartDevice;
import com.deplink.boruSmart.Protocol.packet.GeneralPacket;
import com.deplink.boruSmart.constant.DeviceTypeConstant;
import com.deplink.boruSmart.manager.connect.remote.RemoteConnectManager;
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
 * Created by Administrator on 2017/12/7.
 */
public class SmartSwitchManager extends DeviceManager {
    private static final String TAG = "SmartSwitchManager";
    /**
     * 这个类设计成单例
     */
    private static SmartSwitchManager instance;
    private SmartDev currentSelectSmartDevice;
    /**
     * 添加开关时指定当前添加开关的类型
     */
    private String currentAddSwitchSubType;
    public SmartDev getCurrentSelectSmartDevice() {
        return currentSelectSmartDevice;
    }
    private List<SmartSwitchListener> mSmartSwitchListenerList;
     private static String uuid;
    public void addSmartSwitchListener(SmartSwitchListener listener) {
        if (listener != null && !mSmartSwitchListenerList.contains(listener)) {
            this.mSmartSwitchListenerList.add(listener);
        }
    }

    public void removeSmartSwitchListener(SmartSwitchListener listener) {
        if (listener != null && mSmartSwitchListenerList.contains(listener)) {
            this.mSmartSwitchListenerList.remove(listener);
        }
    }

    public void setCurrentSelectSmartDevice(SmartDev currentSelectSmartDevice) {
        this.currentSelectSmartDevice = currentSelectSmartDevice;
    }

    public static synchronized SmartSwitchManager getInstance() {
        if (instance == null) {
            instance = new SmartSwitchManager();
        }
        uuid = Perfence.getPerfence(AppConstant.PERFENCE_BIND_APP_UUID);
        return instance;
    }

    /**
     * 初始化本地连接管理器
     */
    public void InitSmartSwitchManager(Context context) {
        this.mSmartSwitchListenerList = new ArrayList<>();
        if (mLocalConnectmanager == null) {
            mLocalConnectmanager = LocalConnectmanager.getInstance();
            mLocalConnectmanager.InitLocalConnectManager(context, uuid);
        }
        if (mRemoteConnectManager == null) {
            mRemoteConnectManager = RemoteConnectManager.getInstance();
            mRemoteConnectManager.InitRemoteConnectManager(context);
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

    public void setSwitchCommand(String cmd) {
        if (mLocalConnectmanager.isLocalconnectAvailable()) {
            QueryOptions queryCmd = new QueryOptions();
            queryCmd.setOP("SET");
            queryCmd.setMethod("SmartWallSwitch");
            queryCmd.setCommand(cmd);
            queryCmd.setSmartUid(currentSelectSmartDevice.getMac());
            Log.i(TAG, "设置开关smartUid=" + currentSelectSmartDevice.getUid());
            queryCmd.setTimestamp();
            Gson gson = new Gson();
            String text = gson.toJson(queryCmd);
            packet.packSetCmdData(text.getBytes(), currentSelectSmartDevice.getUid());
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
                    Log.i(TAG, "远程接口查询设备列表" + "topic" + devices.get(i).getTopic());
                    Log.i(TAG, "device.getTopic()=" + devices.get(i).getTopic());
                    mHomeGenius.setSwitchCommand(currentSelectSmartDevice, devices.get(i).getTopic(), uuid, cmd);
                }
            }
        }
    }

    public void querySwitchStatus(String cmd) {
        if (mLocalConnectmanager.isLocalconnectAvailable()) {
            QueryOptions queryCmd = new QueryOptions();
            queryCmd.setOP("SET");
            queryCmd.setMethod("SmartWallSwitch");
            queryCmd.setCommand(cmd);
            queryCmd.setSmartUid(currentSelectSmartDevice.getMac());
            queryCmd.setTimestamp();
            Gson gson = new Gson();
            String text = gson.toJson(queryCmd);
            packet.packSetCmdData(text.getBytes(), currentSelectSmartDevice.getUid());
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
                    mHomeGenius.querySwitchStatus(currentSelectSmartDevice, devices.get(i).getTopic(), uuid, cmd);
                }
            }
        }

    }

    public boolean updateSmartDeviceGetway(GatwayDevice getwayDevice) {
        currentSelectSmartDevice.setGetwayDevice(getwayDevice);
        currentSelectSmartDevice.setGetwayDeviceUid(getwayDevice.getUid());
        boolean saveResult = currentSelectSmartDevice.saveFast();
        Log.i(TAG, "更新智能设备所在的网关=" + saveResult);
        return saveResult;
    }


    public void setCurrentAddSwitchSubType(String currentAddSwitchSubType) {
        this.currentAddSwitchSubType = currentAddSwitchSubType;
    }

    public boolean addDBSwitchDevice(QrcodeSmartDevice device, String uid) {
        //查询设备
        Log.i(TAG, "当前添加的开关子类型" + currentAddSwitchSubType);
        SmartDev smartDev = DataSupport.where("Uid=?", device.getAd()).findFirst(SmartDev.class);
        if (smartDev == null) {
            smartDev = new SmartDev();
            smartDev.setUid(uid);
            smartDev.setOrg(device.getOrg());
            smartDev.setVer(device.getVer());
            smartDev.setMac(device.getAd().toLowerCase());
            smartDev.setType(DeviceTypeConstant.TYPE.TYPE_SWITCH);
            smartDev.setName(device.getName());
            smartDev.setSubType(currentAddSwitchSubType);
            boolean addResult = smartDev.save();
            Log.i(TAG, "向数据库中添加一条智能设备数据=" + addResult);
            return addResult;
        }
        Log.i(TAG, "数据库中已存在相同设备，不必要添加");
        return false;
    }

    /**
     * 更新设备所在房间
     */
    public void updateSmartDeviceInWhatRoom(Room room, String deviceUid, String deviceName) {
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
        boolean saveResult = smartDev.save();
        Log.i(TAG, "更新智能设备所在的房间=" + saveResult);
    }

    public boolean updateSmartDeviceName(String deviceUid, String deviceName) {
        //保存所在的房间
        //查询设备
        SmartDev smartDev = DataSupport.where("Uid=?", deviceUid).findFirst(SmartDev.class, true);
        setCurrentSelectSmartDevice(smartDev);
        smartDev.setName(deviceName);
        return smartDev.save();
    }

    /**
     * 删除数据库中的一个智能设备
     */
    public int deleteDBSmartDevice(String uid) {
        int affectcolumn = DataSupport.deleteAll(SmartDev.class, "Uid=?", uid);
        Log.i(TAG, "删除一个智能设备，删除影响的行数=" + affectcolumn);
        return affectcolumn;
    }

    @Override
    public void OnGetQueryresult(String devList) {

    }

    @Override
    public void OnGetSetresult(String setResult) {
        Log.i(TAG, "开关控制结果返回=" + setResult);
        for (int i = 0; i < mSmartSwitchListenerList.size(); i++) {
            mSmartSwitchListenerList.get(i).responseResult(setResult);
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
