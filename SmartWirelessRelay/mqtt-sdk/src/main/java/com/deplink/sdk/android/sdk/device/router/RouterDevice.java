package com.deplink.sdk.android.sdk.device.router;

import android.util.Log;

import com.deplink.sdk.android.sdk.DeplinkSDK;
import com.deplink.sdk.android.sdk.interfaces.SDKCoordinator;
import com.deplink.sdk.android.sdk.json.DeviceUpgradeReport;
import com.deplink.sdk.android.sdk.json.Lan;
import com.deplink.sdk.android.sdk.json.PERFORMANCE;
import com.deplink.sdk.android.sdk.json.Proto;
import com.deplink.sdk.android.sdk.json.Qos;
import com.deplink.sdk.android.sdk.json.SSIDList;
import com.deplink.sdk.android.sdk.json.Wifi;
import com.deplink.sdk.android.sdk.mqtt.MQTTController;
import com.google.gson.Gson;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by huqs on 2016/7/6.
 * 路由器设备
 */
public class RouterDevice extends BaseDevice {
    public static final String TAG = "RouterDevice";
    public static final String OP_REBOOT = "reboot";
    /**
     * 这个代表请求得到响应了
     */
    public static final String OP_GET_REPORT = "getReport";
    /**
     * 这个回调只代表请求的数据已发送
     */
    public static final String OP_QUERY_REPORT = "queryReport";
    public static final String OP_SUCCESS = "opSuccess";
    public static final String OP_SET_WAN = "setWan";
    public static final String OP_SET_LAN = "setLan";
    public static final String OP_GET_WIFIRELAY = "getWifiRelay";
    public static final String OP_QUERY_DEVICES = "queryDevices";
    public static final String OP_GET_DEVICES = "getDevices";
    public static final String OP_QUERY_LAN = "queryLan";
    public static final String OP_GET_LAN = "getLan";
    public static final String OP_QUERY_WAN = "queryWan";
    public static final String OP_GET_WAN = "GetWan";
    public static final String OP_QUERY_WIFIRELAY = "queryWifiRelay";
    public static final String OP_QUERY_WIFI = "queryWifi";
    public static final String OP_QUERY_QOS = "queryQos";
    public static final String OP_GET_WIFI = "getWifi";
    public static final String OP_GET_QOS = "getQos";
    public static final String OP_SET_WIFI = "setWifi";
    public static final String OP_SET_DEVICE_CONTROL = "setDeviceControl";
    public static final String OP_SET_QOS = "setQos";
    public static final String OP_CHANGE_AUTO_UPGRADE = "changeAutoUpgrade";
    public static final String OP_CHG_START_UPGRADE = "startUpgrade";
    public static final String OP_LOAD_UPGRADEINFO = "loadUpgradeInfo";
    public static final String OP_LOAD_UPGRADEINFONULL = "loadUpgradeInfoNull";
    //start:开始，download:下载，update:烧写，finish:完成，error:出错）
    public static final int MSG_REPORT_START = MSG_DUMMY + 1;
    public static final int MSG_REPORT_DOWNLOAD = MSG_DUMMY + 2;
    public static final int MSG_REPORT_WRITE = MSG_DUMMY + 3;
    public static final int MSG_REPORT_FINSH = MSG_DUMMY + 4;
    public static final int MSG_REPORT_ERROR = MSG_DUMMY + 5;
    private long currentReceivedDataTime = 0;
    private Proto proto;
    private List<SSIDList> wifiRelay = new ArrayList<>();
    private SDKCoordinator mSDKCoordinator = null;
    private Lan lan;
    private Qos qos;
    public RouterDevice(SDKCoordinator coordinator) {
        mSDKCoordinator = coordinator;
    }
    @Override
    public int processMqttMsg(String topic, MqttMessage message) {
        String jsonString = new String(message.getPayload());
        Log.i(DeplinkSDK.SDK_TAG, "--->mqtt msg: " + jsonString);
        return parseDeviceReport(jsonString);
    }
    private int parseDeviceReport(String xmlStr) {
        int msgFormat = MSG_DUMMY;
        String op = "";
        String method;
        Gson gson = new Gson();
        PERFORMANCE content = gson.fromJson(xmlStr, PERFORMANCE.class);
        op = content.getOP();
        method = content.getMethod();
        Log.i(TAG, "op=" + op + "method=" + method + "result=" + content.getResult() + "xmlStr=" + xmlStr);
        if(op!=null){
            isOnline = true;
        }
        if (op == null) {
            if (content.getResult().equalsIgnoreCase("OK")) {
                Log.i(TAG," mSDKCoordinator.notifyDeviceOpSuccess");
                mSDKCoordinator.notifyDeviceOpSuccess(RouterDevice.OP_SUCCESS, deviceKey);
                return msgFormat;
            }
        } else if (op.equalsIgnoreCase("REPORT")) {
            if (method.equalsIgnoreCase("PERFORMANCE")) {
                performance = gson.fromJson(xmlStr, PERFORMANCE.class);
                Log.i(TAG, "performance=" + performance.toString());
                updateDevice(performance);
                mSDKCoordinator.notifyDeviceOpSuccess(RouterDevice.OP_GET_REPORT, deviceKey);
            } else if (method.equalsIgnoreCase("WIFIRELAY")) {
                updateWifiRelay(content);
                mSDKCoordinator.notifyDeviceOpSuccess(RouterDevice.OP_GET_WIFIRELAY, deviceKey);
            } else if (method.equalsIgnoreCase("UPGRADE")) {
                DeviceUpgradeReport upgrade = gson.fromJson(xmlStr, DeviceUpgradeReport.class);
                if ("finish".equalsIgnoreCase(upgrade.getState())) {
                    upgradeInfo = null;
                    return MSG_REPORT_FINSH;
                } else if ("start".equalsIgnoreCase(upgrade.getState())) {
                    return MSG_REPORT_START;
                } else if ("download".equalsIgnoreCase(upgrade.getState())) {
                    return MSG_REPORT_DOWNLOAD;
                } else if ("update".equalsIgnoreCase(upgrade.getState())) {
                    return MSG_REPORT_WRITE;
                } else if ("error".equalsIgnoreCase(upgrade.getState())) {
                    return MSG_REPORT_ERROR;
                }
            }
        } else if (op.equalsIgnoreCase("DEVICES")) {
            if (method.equalsIgnoreCase("REPORT")) {
                mSDKCoordinator.notifyDeviceOpSuccess(RouterDevice.OP_GET_DEVICES, deviceKey);
            }
        } else if (op.equalsIgnoreCase("LAN")) {
            if (method.equalsIgnoreCase("REPORT")) {
                lan = gson.fromJson(xmlStr, Lan.class);
                Log.i(TAG, "get lan =" + lan.toString());
                mSDKCoordinator.notifyDeviceOpSuccess(RouterDevice.OP_GET_LAN, deviceKey);
            }
        } else if (op.equalsIgnoreCase("WAN")) {
            if (method.equalsIgnoreCase("REPORT")) {
                PERFORMANCE wan = gson.fromJson(xmlStr, PERFORMANCE.class);
                updateQueryedWan(wan);
                mSDKCoordinator.notifyDeviceOpSuccess(RouterDevice.OP_GET_WAN, deviceKey);
            }
        } else if (op.equalsIgnoreCase("QOS")) {
            if (method.equalsIgnoreCase("REPORT")) {
                qos = gson.fromJson(xmlStr, Qos.class);
                mSDKCoordinator.notifyDeviceOpSuccess(RouterDevice.OP_GET_QOS, deviceKey);
            }
        } else if (op.equalsIgnoreCase("WIFI")) {
            if (method.equalsIgnoreCase("REPORT")) {
                wifi = gson.fromJson(xmlStr, Wifi.class);
                mSDKCoordinator.notifyDeviceOpSuccess(RouterDevice.OP_GET_WIFI, deviceKey);
            }
        }
        return msgFormat;
    }
    private Wifi wifi;

    public Wifi getWifi() {
        return wifi;
    }

    private void updateWifiRelay(PERFORMANCE content) {
        wifiRelay.clear();
        Log.i(TAG, "PERFORMANCE:getSSIDList().size" + content.getSSIDList().size());
        wifiRelay.addAll(content.getSSIDList());
    }

    private void updateQueryedWan(PERFORMANCE wan) {
        proto = wan.getProto();
    }

    public Qos getQos() {
        return qos;
    }

    public Lan getLan() {
        return lan;
    }

    public Proto getProto() {
        return proto;
    }
    private PERFORMANCE performance;

    public PERFORMANCE getPerformance() {
        return performance;
    }

    private void updateDevice(PERFORMANCE report) {
        //流量下载
        String downloadBytes = report.getDevice().getDataTraffic().getRX();
        if (downloadBytes.length() > 6) {//去掉数据开始的字符：bytes:
            downloadBytes = downloadBytes.substring(6);
        } else {
            downloadBytes = "0";
        }
        long downloadBytesMath = Long.parseLong(downloadBytes);
        Log.i(TAG, downloadBytes + "downloadBytesMath=" + downloadBytesMath);
        //流量上传
        String uploadBytes = report.getDevice().getDataTraffic().getTX();
        if (uploadBytes.length() > 6) {
            uploadBytes = report.getDevice().getDataTraffic().getTX().substring(6);
        } else {
            uploadBytes = "0";
        }
        long uploadBytesMath = Long.parseLong(uploadBytes);
        Log.i(TAG, uploadBytes + "uploadBytesMath=" + uploadBytesMath);
        if (currentReceivedDataTime == 0) {
            //说明是第一次收到反馈数据,不处理
            currentReceivedDataTime = System.currentTimeMillis();
        } else {
            currentReceivedDataTime = System.currentTimeMillis();
        }
    }

    /**
     * 查询wifi
     */
    public void queryWifi() {
        PERFORMANCE textContent = new PERFORMANCE();
        textContent.setOP("QUERY");
        textContent.setMethod("WIFI");
        textContent.setTimestamp(System.currentTimeMillis() / 1000);
        Gson gson = new Gson();
        String text = gson.toJson(textContent);
        MQTTController.getSingleton().publish(exclusive.getSub(), text, new MqttActionHandler(RouterDevice.OP_QUERY_WIFI));
    }

    /**
     * 设置wifi
     */
    public void setWifi(Wifi wifi) {
        wifi.setOP("WIFI");
        wifi.setMethod("SET");
        wifi.setTimestamp(System.currentTimeMillis() / 1000);
        Gson gson = new Gson();
        String text = gson.toJson(wifi);
        MQTTController.getSingleton().publish(exclusive.getSub(), text, new MqttActionHandler(RouterDevice.OP_SET_WIFI));
    }

    /**
     * 设置上网方式
     */
    public void setWan(Proto proto) {
        PERFORMANCE textContent = new PERFORMANCE();
        textContent.setOP("WAN");
        textContent.setMethod("SET");
        textContent.setProto(proto);
        textContent.setTimestamp(System.currentTimeMillis() / 1000);
        Gson gson = new Gson();
        String text = gson.toJson(textContent);
        MQTTController.getSingleton().publish(exclusive.getSub(), text, new MqttActionHandler(RouterDevice.OP_SET_WAN));
    }

    /**
     * 设置LAN
     */
    public void setLan(Lan lan) {
        lan.setOP("LAN");
        lan.setMethod("SET");
        lan.setTimestamp(System.currentTimeMillis() / 1000);
        Gson gson = new Gson();
        String text = gson.toJson(lan);
        MQTTController.getSingleton().publish(exclusive.getSub(), text, new MqttActionHandler(RouterDevice.OP_SET_LAN));
    }

    /**
     * 设置Qos
     */
    public void setQos(Qos qos) {

        qos.setOP("QOS");
        qos.setMethod("SET");
        qos.setTimestamp(System.currentTimeMillis() / 1000);
        Gson gson = new Gson();
        String text = gson.toJson(qos);
        MQTTController.getSingleton().publish(exclusive.getSub(), text, new MqttActionHandler(RouterDevice.OP_SET_QOS));
    }


    private class MqttActionHandler implements IMqttActionListener {
        private String action;
        public MqttActionHandler(String action) {
            this.action = action;
        }
        @Override
        public void onSuccess(IMqttToken iMqttToken) {
            notifySuccess(action);
        }
        @Override
        public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
            throwable.printStackTrace();
            String error = "操作失败";
            notifyFailure(action, error);
        }
    }

    private void notifySuccess(String action) {
        if (mSDKCoordinator != null) {
            mSDKCoordinator.notifyDeviceOpSuccess(action, deviceKey);
        }
    }
    private void notifyFailure(String action, String error) {
        if (mSDKCoordinator != null) {
            mSDKCoordinator.notifyDeviceOpFailure(action, deviceKey, new Throwable(error));
        }
    }

    @Override
    public String toString() {
        return super.toString();
    }
}