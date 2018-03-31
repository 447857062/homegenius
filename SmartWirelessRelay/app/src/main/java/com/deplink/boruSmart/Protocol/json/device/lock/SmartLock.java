package com.deplink.boruSmart.Protocol.json.device.lock;

import java.util.ArrayList;
import java.util.List;

import com.deplink.boruSmart.Protocol.json.device.lock.alertreport.Info;
import com.deplink.boruSmart.Protocol.json.device.SmartDev;

/**
 * Created by Administrator on 2017/10/30.
 * 智能锁设备
 */
public class SmartLock extends SmartDev {
    private List<Info> info = new ArrayList<>();
    public List<Info> getInfo() {
        return info;
    }
    public void setInfo(List<Info> info) {
        this.info = info;
    }

    @Override
    public String toString() {
        return "SmartLock{" +
                "info=" + info +
                '}';
    }
}
