package com.deplink.boruSmart.manager.connect.local.tcp;

import com.deplink.boruSmart.Protocol.json.device.lock.alertreport.Info;

import java.util.List;

/**
 * Created by ${kelijun} on 2017/11/7.
 */
public interface LocalConnecteListener {
    /**
     * 获取设备列表查询结果
     */
    void OnGetQueryresult(String devList);

    /**
     * 智能设备的绑定结果
     */
    void OnGetBindresult(String setResult);

    /**
     * 获取设置结果
     */
    void OnGetSetresult(String setResult);

    /**
     * 查询到wifi列表
     */
    void getWifiList(String result);

    /**
     * 设置wifi中继
     *
     * @param result
     */
    void onSetWifiRelayResult(String result);

    /**
     * 获取报警记录
     */
    void onGetalarmRecord(List<Info> alarmList);
}
