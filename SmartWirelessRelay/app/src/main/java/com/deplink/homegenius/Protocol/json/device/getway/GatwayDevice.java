package com.deplink.homegenius.Protocol.json.device.getway;

import com.deplink.homegenius.Protocol.json.Room;

import org.litepal.annotation.Column;
import org.litepal.crud.DataSupport;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/10/30.
 */
public class GatwayDevice extends DataSupport implements Serializable{
    @Column(unique = true,nullable = false)
    private String Uid;
    private String Status;
    private String Type;
    private String Mac;
    private String Org;
    private String Ver;
    private String name;
    private String bindLocal;
    private String topic;
    private String sign_seed;
    private String signature;
    private boolean isShared;

    public boolean isShared() {
        return isShared;
    }

    public void setShared(boolean shared) {
        isShared = shared;
    }

    public String getSign_seed() {
        return sign_seed;
    }

    public void setSign_seed(String sign_seed) {
        this.sign_seed = sign_seed;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getBindLocal() {
        return bindLocal;
    }

    public void setBindLocal(String bindLocal) {
        this.bindLocal = bindLocal;
    }

    public String getOrg() {
        return Org;
    }

    public void setOrg(String org) {
        Org = org;
    }

    public String getVer() {
        return Ver;
    }

    public void setVer(String ver) {
        Ver = ver;
    }

    private List<Room>roomList=new ArrayList<>();
    /**
     * 网关的IP地址
     */
    private String ipAddress;

    public String getType() {
        return Type;
    }

    public void setType(String type) {
        Type = type;
    }

    public String getMac() {
        return Mac;
    }

    public void setMac(String mac) {
        Mac = mac;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUid() {
        return Uid;
    }

    public void setUid(String uid) {
        Uid = uid;
    }


    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }

    public List<Room> getRoomList() {
        return roomList;
    }

    public void setRoomList(List<Room> roomList) {
        this.roomList = roomList;
    }

}
