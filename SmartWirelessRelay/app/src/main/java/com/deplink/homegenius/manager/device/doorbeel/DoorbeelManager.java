package com.deplink.homegenius.manager.device.doorbeel;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.deplink.homegenius.Protocol.json.Room;
import com.deplink.homegenius.Protocol.json.device.SmartDev;
import com.deplink.homegenius.util.JsonArrayParseUtil;
import com.deplink.homegenius.util.Perfence;
import com.deplink.sdk.android.sdk.homegenius.DeviceOperationResponse;
import com.deplink.sdk.android.sdk.homegenius.Deviceprops;
import com.deplink.sdk.android.sdk.json.homegenius.DoorBellItem;
import com.deplink.sdk.android.sdk.rest.RestfulToolsHomeGenius;
import com.deplink.sdk.android.sdk.rest.RestfulToolsHomeGeniusString;
import com.deplink.sdk.android.sdk.rest.RestfulToolsPng;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Administrator on 2017/12/6.
 * * private DoorbeelManager mDoorbeelManager;
 * mDoorbeelManager=DoorbeelManager.getInstance();
 * mDoorbeelManager.InitRouterManager(this);
 */
public class DoorbeelManager {
    private static final String TAG = "DoorbeelManager";
    /**
     * 创建一个可缓存线程池，如果线程池长度超过处理需要，可灵活回收空闲线程，若无可回收，则新建线程。
     */
    private ExecutorService cachedThreadPool;
    /**
     * 这个类设计成单例
     */
    private static DoorbeelManager instance;
    private Context mContext;
    private SmartDev currentSelectedDoorbeel;
    private String mac;
    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public SmartDev getCurrentSelectedDoorbeel() {
        return currentSelectedDoorbeel;
    }

    public void setCurrentSelectedDoorbeel(SmartDev currentSelectedDoorbeel) {
        this.currentSelectedDoorbeel = currentSelectedDoorbeel;
    }
    private List<DoorBellListener> mDeviceListenerList;

    public void addDeviceListener(DoorBellListener listener) {
        if (listener != null && !mDeviceListenerList.contains(listener)) {
            this.mDeviceListenerList.add(listener);
        }
    }

    public void removeDeviceListener(DoorBellListener listener) {
        if (listener != null && mDeviceListenerList.contains(listener)) {
            this.mDeviceListenerList.remove(listener);
        }
    }
    public static synchronized DoorbeelManager getInstance() {
        if (instance == null) {
            instance = new DoorbeelManager();
        }
        return instance;
    }
    private String ssid;
    private String password;
    private boolean isConfigWifi;

    public boolean isConfigWifi() {
        return isConfigWifi;
    }

    public void setConfigWifi(boolean configWifi) {
        isConfigWifi = configWifi;
    }

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void InitDoorbeelManager(Context context) {
        this.mContext = context;
        if (cachedThreadPool == null) {
            cachedThreadPool = Executors.newCachedThreadPool();
        }
        if(mDeviceListenerList==null){
            mDeviceListenerList=new ArrayList<>();
        }
    }

    /**
     * 保存智能门铃到数据库
     *
     * @param dev 路由器
     */
    public boolean saveDoorbeel(SmartDev dev) {
        boolean success = dev.save();
        Log.i(TAG, "保存智能门铃设备=" + success);
        return success;
    }

    public void updateDoorbeelName( String name) {
                ContentValues values = new ContentValues();
                values.put("name", name);
                 int affectColumn = DataSupport.updateAll(SmartDev.class, values, "Uid=?", currentSelectedDoorbeel.getUid());
                Log.i(TAG, "更新智能门铃名称=" + affectColumn);


    }
    public void getDoorbellHistory() {
        String uid = currentSelectedDoorbeel.getUid();
        String userName = Perfence.getPerfence(Perfence.PERFENCE_PHONE);
        if (userName.equals("")) {
            return;
        }
        Deviceprops device = new Deviceprops();
        if (uid != null) {
            device.setUid(uid);
        }
        RestfulToolsHomeGeniusString.getSingleton().readDoorBeelVistorInfo(userName, uid, new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.code() == 200) {
                    Log.i(TAG, "" + response.message());
                    Log.i(TAG, "" + response.body());
                    for (int i = 0; i < mDeviceListenerList.size(); i++) {
                        if(!response.body().startsWith("{")){
                            List<DoorBellItem>list=  JsonArrayParseUtil.jsonToArrayList(response.body(),DoorBellItem.class);
                            mDeviceListenerList.get(i).responseVisitorListResult(list);
                        }
                    }
                }else{

                    if(response.errorBody()!=null){
                        try {
                            Log.i(TAG, "" + response.body());
                            Log.i(TAG, "" + response.errorBody().string());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.i(TAG, "" + t.getMessage());
            }
        });
    }
    public void getDoorbellVistorImage(String file, final int count) {
        String uid = currentSelectedDoorbeel.getUid();
        String userName = Perfence.getPerfence(Perfence.PERFENCE_PHONE);
        if (userName.equals("")) {
            return;
        }
        Deviceprops device = new Deviceprops();
        if (uid != null) {
            device.setUid(uid);
        }
        RestfulToolsPng.getSingleton().getDoorBellImage(userName, uid, file, new Callback<Bitmap>() {
            @Override
            public void onResponse(Call<Bitmap> call, Response<Bitmap> response) {
                if(response.errorBody()!=null){
                    try {
                        Log.i(TAG,response.errorBody().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if(response.code()==200){
                    for (int i = 0; i < mDeviceListenerList.size(); i++) {
                        Log.i(TAG,"getDoorBellImage");
                        mDeviceListenerList.get(i).responseVisitorImage(response.body(),count);
                    }
                }
            }
            @Override
            public void onFailure(Call<Bitmap> call, Throwable t) {
                Log.i(TAG,t.getMessage());
            }
        });
    }
    public void deleteDoorbellVistorImage(String file) {
        String uid = currentSelectedDoorbeel.getUid();
        String userName = Perfence.getPerfence(Perfence.PERFENCE_PHONE);
        if (userName.equals("")) {
            return;
        }
        Deviceprops device = new Deviceprops();
        if (uid != null) {
            device.setUid(uid);
        }
        
        RestfulToolsHomeGenius.getSingleton().deleteDoorBellVisitor(userName, uid, file, new Callback<DeviceOperationResponse>() {
            @Override
            public void onResponse(Call<DeviceOperationResponse> call, Response<DeviceOperationResponse> response) {
                if(response.errorBody()!=null){
                    try {
                        Log.i(TAG,response.errorBody().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if(response.code()==200){
                    for (int i = 0; i < mDeviceListenerList.size(); i++) {
                        Log.i(TAG,"deleteDoorBellVisitor");
                        DeviceOperationResponse responseBody=response.body();
                        boolean success;
                       if(responseBody.getStatus()!=null && responseBody.getStatus().equalsIgnoreCase("OK")){
                           mDeviceListenerList.get(i).responseDeleteRecordHistory(true);
                       }else{
                           mDeviceListenerList.get(i).responseDeleteRecordHistory(false);
                       }

                    }
                }
            }

            @Override
            public void onFailure(Call<DeviceOperationResponse> call, Throwable t) {

            }
        });
    }
    public void deleteDoorbeel(SmartDev dev) {
        int affectColumn = DataSupport.deleteAll(SmartDev.class, "Uid = ?", dev.getUid());
        Log.i(TAG, "删除智能门铃设备=" + affectColumn);

    }

    /**
     * 更新设备所在房间
     *
     * @param room
     * @param uid
     */
    public boolean updateDeviceInWhatRoom(Room room, String uid) {
        //保存所在的房间
        //查询设备
        SmartDev smartDev = DataSupport.where("Uid=?", uid).findFirst(SmartDev.class, true);
        //找到要更行的设备,设置关联的房间
        List<Room> rooms = new ArrayList<>();
        if(room!=null){
            rooms.add(room);
        }else{
            rooms.addAll( DataSupport.findAll(Room.class, true));
        }
        smartDev.setRooms(rooms);
        boolean saveResult = smartDev.save();
        if(getCurrentSelectedDoorbeel()!=null){
            getCurrentSelectedDoorbeel().setRooms(rooms);
        }
        return saveResult;
    }

}
