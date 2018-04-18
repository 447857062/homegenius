package com.deplink.boruSmart.Protocol.json.device;

import org.litepal.annotation.Column;
import org.litepal.crud.DataSupport;

import java.io.Serializable;

/**
 * Created by ${kelijun} on 2018/4/18.
 */
public class Device extends DataSupport implements Serializable{
    @Column(unique = true,nullable = false)
    private String Uid;
    private String Status;
    private String Type;
    private String Mac;
    private String Org;
    private String Ver;
    private String name;
    private int userCount;
    public String getUid() {
        return Uid;
    }

    public int getUserCount() {
        return userCount;
    }

    public void setUserCount(int userCount) {
        this.userCount = userCount;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Device{" +
                "Uid='" + Uid + '\'' +
                ", Status='" + Status + '\'' +
                ", Type='" + Type + '\'' +
                ", Mac='" + Mac + '\'' +
                ", Org='" + Org + '\'' +
                ", Ver='" + Ver + '\'' +
                ", name='" + name + '\'' +
                ", userCount=" + userCount +
                '}';
    }
}
