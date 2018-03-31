package com.deplink.boruSmart.manager.device.remoteControl;

import com.deplink.sdk.android.sdk.homegenius.DeviceOperationResponse;

import java.util.List;

/**
 * Created by Administrator on 2017/11/9.
 */
public abstract class RemoteControlListener {
    public void responseQueryResult(String result) {};
    public void responseOnlineStatu(String result) {};
    public void responseDeleteVirtualDevice(DeviceOperationResponse result) {};
    public void responseAlertVirtualDevice(DeviceOperationResponse result) {};
    public void responseQueryVirtualDevices(List<DeviceOperationResponse> result) {};

}
