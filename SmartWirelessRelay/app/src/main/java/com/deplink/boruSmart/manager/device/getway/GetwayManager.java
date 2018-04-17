package com.deplink.boruSmart.manager.device.getway;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.deplink.boruSmart.Protocol.json.OpResult;
import com.deplink.boruSmart.Protocol.json.QueryOptions;
import com.deplink.boruSmart.Protocol.json.device.SmartDev;
import com.deplink.boruSmart.Protocol.json.device.getway.GatwayDevice;
import com.deplink.boruSmart.Protocol.json.device.lock.Record;
import com.deplink.boruSmart.Protocol.json.device.lock.alertreport.Info;
import com.deplink.boruSmart.Protocol.json.device.router.Router;
import com.deplink.boruSmart.Protocol.json.wifi.AP_CLIENT;
import com.deplink.boruSmart.Protocol.json.wifi.Proto;
import com.deplink.boruSmart.Protocol.packet.GeneralPacket;
import com.deplink.boruSmart.activity.personal.login.LoginActivity;
import com.deplink.boruSmart.constant.AppConstant;
import com.deplink.boruSmart.constant.DeviceTypeConstant;
import com.deplink.boruSmart.manager.connect.remote.HomeGenius;
import com.deplink.boruSmart.manager.connect.remote.RemoteConnectManager;
import com.deplink.boruSmart.manager.room.RoomManager;
import com.deplink.boruSmart.util.Perfence;
import com.deplink.boruSmart.Protocol.json.Room;
import com.deplink.boruSmart.manager.connect.local.tcp.LocalConnectmanager;
import com.deplink.boruSmart.manager.device.DeviceManager;
import com.deplink.boruSmart.view.toast.Ftoast;
import com.deplink.sdk.android.sdk.homegenius.DeviceOperationResponse;
import com.deplink.sdk.android.sdk.homegenius.Deviceprops;
import com.deplink.sdk.android.sdk.rest.RestfulToolsHomeGenius;
import com.google.gson.Gson;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Administrator on 2017/11/22.
 */
public class GetwayManager extends DeviceManager {
    private static final String TAG = "GetwayManager";
    /**
     * 这个类设计成单例
     */
    private static GetwayManager instance;
    private Context mContext;
    private GatwayDevice currentSelectGetwayDevice;
    private static String uuid ;
    public void deleteDeviceHttp() {
        String uid = currentSelectGetwayDevice.getUid();
        Log.i(TAG, "删除设备uid=" + uid);
        String userName = Perfence.getPerfence(Perfence.PERFENCE_PHONE);
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
                Log.i(TAG, "code="+ response.code()+",message=" + response.message());
                if (response.code() == 200) {
                    Log.i(TAG, "" + response.body().toString());
                    for (int i = 0; i < mGetwayListenerList.size(); i++) {
                        mGetwayListenerList.get(i).responseDeleteDeviceHttpResult(response.body());
                    }
                }else if(response.code()==403){
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
    public static synchronized GetwayManager getInstance() {
        if (instance == null) {
            instance = new GetwayManager();
        }
        uuid = Perfence.getPerfence(AppConstant.PERFENCE_BIND_APP_UUID);
        return instance;
    }

    public void InitGetwayManager(Context context, GetwayListener listener) {
        this.mContext = context;
        mGetwayListenerList = new ArrayList<>();
        if (mLocalConnectmanager == null) {
            mLocalConnectmanager = LocalConnectmanager.getInstance();
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
        if (cachedThreadPool == null) {
            cachedThreadPool = Executors.newCachedThreadPool();
        }
    }

    private List<GetwayListener> mGetwayListenerList;

    public void addGetwayListener(GetwayListener listener) {
        if (listener != null && !mGetwayListenerList.contains(listener)) {
            this.mGetwayListenerList.add(listener);
        }
    }

    public void removeGetwayListener(GetwayListener listener) {
        if (listener != null && mGetwayListenerList.contains(listener)) {
            this.mGetwayListenerList.remove(listener);
        }
    }

    /**
     * 中继连接
     */
    public void setWifiRelay(AP_CLIENT paramas) {
        Log.i(TAG, "setWifiRelay");
        if (mLocalConnectmanager.isLocalconnectAvailable()) {
            QueryOptions setCmd = new QueryOptions();
            setCmd.setOP("WAN");
            setCmd.setMethod("SET");
            setCmd.setTimestamp();
            Proto proto = new Proto();
            proto.setAP_CLIENT(paramas);
            setCmd.setProto(proto);
            Gson gson = new Gson();
            String text = gson.toJson(setCmd);
            packet.packSetWifiListData(text.getBytes());
            cachedThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    mLocalConnectmanager.getOut(packet.data);
                }
            });
        } else {

            if (currentSelectGetwayDevice != null && currentSelectGetwayDevice.getTopic() != null
                    && !currentSelectGetwayDevice.getTopic().equals("")) {
                Log.i(TAG, "device.getTopic()=" + currentSelectGetwayDevice.getTopic());
                mHomeGenius.setWifiRelay(currentSelectGetwayDevice.getTopic(), uuid, paramas);
            }
        }
    }

    /**
     * 绑定网关，中继器
     */
    public void bindDevice(String deviceUid) {
        if (mLocalConnectmanager.isLocalconnectAvailable()) {
            QueryOptions queryCmd = new QueryOptions();
            queryCmd.setOP("SET");
            queryCmd.setMethod("DevList");
            queryCmd.setTimestamp();
            List<GatwayDevice> devs = new ArrayList<>();
            //设备赋值
            GatwayDevice dev = new GatwayDevice();
            dev.setUid(deviceUid);
            devs.add(dev);
            queryCmd.setDevice(devs);
            Gson gson = new Gson();
            Log.i(TAG, "绑定网关:" + queryCmd.toString());
            String text = gson.toJson(queryCmd);
            packet.packSendDevsData(text.getBytes());
            cachedThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    mLocalConnectmanager.getOut(packet.data);
                }
            });
        } else {
            if (currentSelectGetwayDevice != null && currentSelectGetwayDevice.getTopic() != null && !currentSelectGetwayDevice.getTopic().equals("")) {
                Log.i(TAG, "device.getTopic()=" + currentSelectGetwayDevice.getTopic());
                mHomeGenius.bindGetwayDevice(currentSelectGetwayDevice.getTopic(), uuid, deviceUid);
            }

        }
    }

    public GatwayDevice getCurrentSelectGetwayDevice() {
        return currentSelectGetwayDevice;
    }

    public void setCurrentSelectGetwayDevice(GatwayDevice currentSelectGetwayDevice) {
        this.currentSelectGetwayDevice = currentSelectGetwayDevice;
    }

    public void deleteGetwayDevice() {
        if (mLocalConnectmanager.isLocalconnectAvailable()) {
            QueryOptions queryCmd = new QueryOptions();
            queryCmd.setOP("DELETE");
            queryCmd.setMethod("DevList");
            queryCmd.setTimestamp();
            List<GatwayDevice> devs = new ArrayList<>();
            //设备赋值
            GatwayDevice dev = new GatwayDevice();
            dev.setUid(currentSelectGetwayDevice.getMac());
            devs.add(dev);
            queryCmd.setDevice(devs);
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
            if (currentSelectGetwayDevice != null && currentSelectGetwayDevice.getTopic() != null
                    && !currentSelectGetwayDevice.getTopic().equals("")) {
                Log.i(TAG, "device.getTopic()=" + currentSelectGetwayDevice.getTopic());
                mHomeGenius.deleteGetwayDevice(currentSelectGetwayDevice, currentSelectGetwayDevice.getTopic(), uuid);
            }


        }
    }

    //数据库操作函数
    public boolean addDBGetwayDevice(String deviceName, String uid, String topic) {
        //查询设备
        GatwayDevice currentAddGetwayDevice = new GatwayDevice();
        currentAddGetwayDevice.setType(DeviceTypeConstant.TYPE.TYPE_SMART_GETWAY);
        currentAddGetwayDevice.setUid(uid);
        currentAddGetwayDevice.setName(deviceName);
        currentAddGetwayDevice.setTopic(topic);
        boolean addResult = currentAddGetwayDevice.save();
        Log.i(TAG, "向数据库中添加一条网关设备数据=" + addResult);
        if (!addResult) {
            Log.i(TAG, "数据库中已存在相同网关设备，不必要添加");
        }
        return addResult;
    }

    /**
     * 删除数据库中的一个网关设备
     */
    public int deleteDBGetwayDevice(String uid) {
        int affectcolumn = DataSupport.deleteAll(GatwayDevice.class, "Uid=?", uid);
        Log.i(TAG, "删除一个网关设备，删除影响的行数=" + affectcolumn);
        return affectcolumn;
    }

    public List<GatwayDevice> getAllGetwayDevice() {
        List<GatwayDevice> list = DataSupport.findAll(GatwayDevice.class, true);
        if (list.size() > 0) {
            Log.i(TAG, "查询到的网关设备个数=" + list.size());
        }
        return list;
    }

    /**
     * @param room      更新房间
     * @param deviceUid 当前网关设备
     */
    public void updateGetwayDeviceInWhatRoom(Room room, String deviceUid) {
        //保存所在的房间
        //查询设备
        GatwayDevice getwayDevice = DataSupport.where("Uid=?", deviceUid).findFirst(GatwayDevice.class, true);
        //找到要更行的设备,设置关联的房间
        List<Room> roomList = new ArrayList<>();
        if (room != null) {
            roomList.add(room);
        } else {
            roomList.addAll(RoomManager.getInstance().getmRooms());
        }
        getwayDevice.setRoomList(roomList);
        boolean result = getwayDevice.save();
        Log.i(TAG, "更新网关所在房间" + result);

    }

    public void updateGetwayDeviceName(String name) {
        currentSelectGetwayDevice.setName(name);
        boolean result = currentSelectGetwayDevice.save();
        Log.i(TAG, "更新网关名称" + result);
    }
    //数据库操作函数-------------------------------------------------------end


    @Override
    public void OnGetQueryresult(String devList) {

    }

    @Override
    public void OnGetSetresult(String setResult) {

    }

    @Override
    public void OnGetBindresult(String setResult) {
        for (int i = 0; i < mGetwayListenerList.size(); i++) {
            mGetwayListenerList.get(i).responseResult(setResult);
        }
    }
    @Override
    public void getWifiList(String result) {

    }

    @Override
    public void onSetWifiRelayResult(String result) {
        Log.i(TAG, "onSetWifiRelayResult=" + result);
        Gson gson = new Gson();
        OpResult opResult = gson.fromJson(result, OpResult.class);
        if (opResult.getOP().equals("REPORT") && opResult.getMethod().equals("WIFI"))
            for (int i = 0; i < mGetwayListenerList.size(); i++) {
                mGetwayListenerList.get(i).responseSetWifirelayResult(opResult.getResult());
            }
    }

    @Override
    public void onGetalarmRecord(List<Info> alarmList) {

    }
}
