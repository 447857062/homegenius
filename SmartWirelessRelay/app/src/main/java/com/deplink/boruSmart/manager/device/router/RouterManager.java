package com.deplink.boruSmart.manager.device.router;

import android.content.ContentValues;
import android.util.Log;

import com.deplink.boruSmart.constant.DeviceTypeConstant;
import com.deplink.boruSmart.manager.room.RoomManager;
import com.deplink.boruSmart.Protocol.json.Room;
import com.deplink.boruSmart.Protocol.json.device.SmartDev;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Administrator on 2017/11/22.
 * 路由器管理
 * private RouterManager mRouterManager;
 * mRouterManager=RouterManager.getInstance();
 * mRouterManager.InitRouterManager(this);
 */
public class RouterManager {
    private static final String TAG = "RouterManager";
    /**
     * 创建一个可缓存线程池，如果线程池长度超过处理需要，可灵活回收空闲线程，若无可回收，则新建线程。
     */
    private ExecutorService cachedThreadPool;
    /**
     * 这个类设计成单例
     */
    private static RouterManager instance;
    private SmartDev currentSelectedRouter;

    public SmartDev getCurrentSelectedRouter() {
        Log.i(TAG, "设置当前选中的路由器 router!=null" + (currentSelectedRouter.getRouter() != null));
        return currentSelectedRouter;
    }

    public void setCurrentSelectedRouter(SmartDev currentSelectedRouter) {
        //关联查询一下
        currentSelectedRouter = DataSupport.where("Uid = ?", currentSelectedRouter.getUid()).findFirst(SmartDev.class, true);
        this.currentSelectedRouter = currentSelectedRouter;
        this.currentSelectedRouter.setStatus("在线");
    }

    public List<Room> getRouterAtRooms() {
        SmartDev dev = DataSupport.where("Uid = ?", currentSelectedRouter.getUid()).findFirst(SmartDev.class, true);
        final List<Room> rooms = dev.getRooms();
        Log.i(TAG, "所在房间有几个=" + rooms.size());
        return rooms;

    }

    public static synchronized RouterManager getInstance() {
        if (instance == null) {
            instance = new RouterManager();
        }
        return instance;
    }

    public void InitRouterManager() {
        if (cachedThreadPool == null) {
            cachedThreadPool = Executors.newCachedThreadPool();
        }
    }


    /**
     * 保存路由器到数据库
     *
     * @param dev 路由器
     */
    public boolean saveRouter(SmartDev dev) {
        boolean success = dev.save();
        Log.i(TAG, "保存路由器设备=" + success);
        return success;
    }

    public int updateRouterName(String name) {
        ContentValues values = new ContentValues();
        values.put("name", name);
        int affectColumn = DataSupport.updateAll(SmartDev.class, values, "Uid=?", currentSelectedRouter.getUid());
        Log.i(TAG, "更新路由器名称=" + affectColumn);
        return affectColumn;
    }

    public boolean updateDeviceInWhatRoom(Room room, String uid, String deviceName) {
        Log.i(TAG, "更新路由器设备所在的房间=start");
        SmartDev smartDev = DataSupport.where("Uid=?", uid).findFirst(SmartDev.class, true);
        List<Room> rooms = new ArrayList<>();
        rooms.clear();
        if (room != null) {
            rooms.add(room);
        } else {
            rooms.addAll(RoomManager.getInstance().getmRooms());
        }
        smartDev.setRooms(rooms);
        smartDev.setName(deviceName);
        boolean saveResult = smartDev.save();
        Log.i(TAG, "更新路由器设备所在的房间=" + saveResult);
        return saveResult;
    }
    public List<SmartDev> getAllRouterDevice() {
        return DataSupport.where("Type = ?", DeviceTypeConstant.TYPE.TYPE_ROUTER).find(SmartDev.class);
    }
}
