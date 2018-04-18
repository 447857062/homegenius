package com.deplink.boruSmart.Protocol.json.device;

import com.deplink.boruSmart.Protocol.json.Room;
import com.deplink.boruSmart.Protocol.json.device.getway.GatwayDevice;
import com.deplink.boruSmart.Protocol.json.device.lock.alertreport.Info;
import com.deplink.boruSmart.Protocol.json.device.router.Router;

import org.litepal.annotation.Column;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/10/30.
 */
public class SmartDev extends Device {
    /*设备类型子类型，比如开关，下面又会分1,2,3，4,路开关*/
    private String subType;
    /*绑定的网关*/
    private GatwayDevice getwayDevice;
    private String bindLockUid;
    private String  getwayDeviceUid;
    private boolean switch_one_open;
    private boolean switch_two_open;
    private boolean switch_three_open;
    private boolean switch_four_open;
    private String key_codes;

    private Router router;
    /**
     * 查询智能设备使用
     */
    private String SmartUid;
    /**
     * 数据库中的关联关系必须要初始化好列表
     */
    @Column(nullable = false)
    private List<Room> rooms = new ArrayList<>();
    /**
     * 各种类型的遥控器需要指定物理遥控器
     */
    private String remotecontrolUid;
    private String lockPassword = "";
    private int lightIsOpen;
    private int whiteValue;
    private int yellowValue;




    public int getLightIsOpen() {
        return lightIsOpen;
    }

    public void setLightIsOpen(int lightIsOpen) {
        this.lightIsOpen = lightIsOpen;
    }

    public int getWhiteValue() {
        return whiteValue;
    }

    public void setWhiteValue(int whiteValue) {
        this.whiteValue = whiteValue;
    }

    public int getYellowValue() {
        return yellowValue;
    }

    public void setYellowValue(int yellowValue) {
        this.yellowValue = yellowValue;
    }

    public String getBindLockUid() {
        return bindLockUid;
    }

    public void setBindLockUid(String bindLockUid) {
        this.bindLockUid = bindLockUid;
    }

    private boolean remerberPassword = true;
    private List<Info> alarmInfo = new ArrayList<>();


    public String getGetwayDeviceUid() {
        return getwayDeviceUid;
    }

    public void setGetwayDeviceUid(String getwayDeviceUid) {
        this.getwayDeviceUid = getwayDeviceUid;
    }


    public String getSmartUid() {
        return SmartUid;
    }

    public void setSmartUid(String smartUid) {
        SmartUid = smartUid;
    }



    public Router getRouter() {
        return router;
    }


    public void setRouter(Router router) {
        this.router = router;
    }

    public boolean isSwitch_one_open() {
        return switch_one_open;
    }
    public void setSwitch_one_open(boolean switch_one_open) {
        this.switch_one_open = switch_one_open;
    }

    public boolean isSwitch_two_open() {
        return switch_two_open;
    }

    public void setSwitch_two_open(boolean switch_two_open) {
        this.switch_two_open = switch_two_open;
    }

    public boolean isSwitch_three_open() {
        return switch_three_open;
    }

    public void setSwitch_three_open(boolean switch_three_open) {
        this.switch_three_open = switch_three_open;
    }

    public boolean isSwitch_four_open() {
        return switch_four_open;
    }

    public void setSwitch_four_open(boolean switch_four_open) {
        this.switch_four_open = switch_four_open;
    }


    public String getRemotecontrolUid() {
        return remotecontrolUid;
    }

    public void setRemotecontrolUid(String remotecontrolUid) {
        this.remotecontrolUid = remotecontrolUid;
    }

    public GatwayDevice getGetwayDevice() {
        return getwayDevice;
    }

    public void setGetwayDevice(GatwayDevice getwayDevice) {
        this.getwayDevice = getwayDevice;
    }

    public List<Info> getAlarmInfo() {
        return alarmInfo;
    }

    public void setAlarmInfo(List<Info> alarmInfo) {
        this.alarmInfo = alarmInfo;
    }

    public boolean isRemerberPassword() {
        return remerberPassword;
    }

    public void setRemerberPassword(boolean remerberPassword) {
        this.remerberPassword = remerberPassword;
    }

    public String getLockPassword() {
        return lockPassword;
    }

    public void setLockPassword(String lockPassword) {
        this.lockPassword = lockPassword;
    }



    public String getSubType() {
        return subType;
    }

    public void setSubType(String subType) {
        this.subType = subType;
    }



    public List<Room> getRooms() {
        return rooms;
    }

    public void setRooms(List<Room> rooms) {
        this.rooms = rooms;
    }


    public String getKey_codes() {
        return key_codes;
    }

    public void setKey_codes(String key_codes) {
        this.key_codes = key_codes;
    }

    @Override
    public String toString() {
        return "SmartDev{" +
                ", subType='" + subType + '\'' +
                ", getwayDevice=" + getwayDevice +
                ", switch_one_open=" + switch_one_open +
                ", switch_two_open=" + switch_two_open +
                ", switch_three_open=" + switch_three_open +
                ", switch_four_open=" + switch_four_open +
                ", router=" + router +
                ", SmartUid='" + SmartUid + '\'' +
                ", rooms=" + rooms +
                ", remotecontrolUid='" + remotecontrolUid + '\'' +
                ", lockPassword='" + lockPassword + '\'' +
                ", remerberPassword=" + remerberPassword +
                ", alarmInfo=" + alarmInfo +
                '}';
    }
}
