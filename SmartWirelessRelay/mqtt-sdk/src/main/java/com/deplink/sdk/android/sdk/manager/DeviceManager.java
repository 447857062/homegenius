package com.deplink.sdk.android.sdk.manager;

import android.util.Log;

import com.deplink.sdk.android.sdk.DeplinkSDK;
import com.deplink.sdk.android.sdk.MqttAction;
import com.deplink.sdk.android.sdk.SDKAction;
import com.deplink.sdk.android.sdk.bean.DeviceUpgradeInfo;
import com.deplink.sdk.android.sdk.device.router.BaseDevice;
import com.deplink.sdk.android.sdk.device.router.RouterDevice;
import com.deplink.sdk.android.sdk.interfaces.MqttListener;
import com.deplink.sdk.android.sdk.interfaces.SDKCoordinator;
import com.deplink.sdk.android.sdk.json.Content;
import com.deplink.sdk.android.sdk.json.DeviceImageUpgrade;
import com.deplink.sdk.android.sdk.mqtt.MQTTController;
import com.google.gson.Gson;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by ${kelijun} on 2016/6/30.
 */
public class DeviceManager implements MqttListener {
    private static final String TAG = "DeviceManager";
    final static private String XML_FMT_IMAGE_UPGRADE = "xmlImageUpgrade";
    private SDKCoordinator mSDKCoordinator = null;
    final LinkedHashMap<String, BaseDevice> mDeviceMap = new LinkedHashMap<>();
    LinkedHashMap<String, BaseDevice> mDeviceTopics = new LinkedHashMap<>();

    public DeviceManager(SDKCoordinator coordinator) {
        mSDKCoordinator = coordinator;
    }

    /**
     * MQTT连接建立
     */
    public void onMQTTConnection() {
        Log.d(DeplinkSDK.SDK_TAG, "---->MQTT connected");
        //订阅所有设备的sub topic
        for (Object o : mDeviceTopics.keySet()) {
            String key = o.toString();
            MQTTController.getSingleton().subscribe(key, this);
        }
        String userTopic = mSDKCoordinator.getUserSession().getTopic_sub().get(0);
        Log.i(TAG,"userTopic="+userTopic);
        MQTTController.getSingleton().subscribe(userTopic, this);
        mSDKCoordinator.notifySuccess(SDKAction.CONNECTED);
    }

    public List<BaseDevice> getDeviceList() {

        return mapToList(mDeviceMap);
    }

    private void printHashMap(HashMap attMap) {
        for (Object o : attMap.entrySet()) {
            String key = o.toString();
            //这样就可以遍历该HashMap的key值了。
            Log.i(TAG, "hashmap key=" + key);
        }

    }

    public BaseDevice getDevice(String deviceKey) {
        printHashMap(mDeviceMap);
        return mDeviceMap.get(deviceKey);
    }

    public void cleanup() {
        mDeviceMap.clear();
        mDeviceTopics.clear();
    }

    private List<BaseDevice> mapToList(LinkedHashMap<String, BaseDevice> map) {
        List<BaseDevice> lis = new ArrayList<>();
        for (Object o : map.keySet()) {
            String key = o.toString();
            lis.add(map.get(key));
        }
        Log.i(TAG, "mapToList" + lis.size());
        return lis;
    }

    @Override
    public void connectionLost(Throwable cause) {

    }

    @Override
    public void onSuccess(IMqttToken asyncActionToken, MqttAction cation, String clientHandle, String topic) {
    }

    @Override
    public void onFailure(IMqttToken token, Throwable exception, MqttAction cation, String clientHandle, String topic) {
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        Log.i(TAG, "messageArrived" + message.toString());
        if (topic.equals(mSDKCoordinator.getUserSession().getTopic_sub().get(0))) {
            String xmlStr = new String(message.getPayload());
            String msgFormat = null;
            Gson gson = new Gson();
            Content content = gson.fromJson(xmlStr, Content.class);
            String op;
            String method;
            op = content.getOP();
            method = content.getMethod();
           if ("IMAGE".equalsIgnoreCase(op) && "UPGRADE".equalsIgnoreCase(method)) {
                msgFormat = XML_FMT_IMAGE_UPGRADE;
            }
            processMqttMsg(message);
            if (msgFormat == null) {
                return;
            }
            //parse by type
            switch (msgFormat) {
                case XML_FMT_IMAGE_UPGRADE:
                    DeviceImageUpgrade upgrade = gson.fromJson(xmlStr, DeviceImageUpgrade.class);
                    synchronized (mDeviceMap) {
                        for (Object o : mDeviceMap.keySet()) {
                            String key = o.toString();
                            BaseDevice device = mDeviceMap.get(key);
                            if (device.getProductKey().equals(upgrade.getProductKey())) {
                                DeviceUpgradeInfo info = new DeviceUpgradeInfo();
                                info.setProduct_key(upgrade.getProductKey());
                                info.setVersion(upgrade.getSoftwareVersion());
                                info.setProtocol(upgrade.getProtocol());
                                info.setImg_url(upgrade.getImgUrl());
                                info.setBak_protocol(upgrade.getBakProtocol());
                                info.setBak_img_url(upgrade.getBakImgUrl());
                                info.setFile_len(upgrade.getFileLen());
                                info.setFile_md5(upgrade.getMD5());
                                info.setUpgrade_state(BaseDevice.UPGRADE_STATE_READY);
                                device.setUpgradeInfo(info);
                                mSDKCoordinator.notifyDeviceUpgrade(device.getDeviceKey());
                            }
                        }
                    }
                    break;
            }
            return;
        }
        processMqttMsg(message);
        //设备状态改变
        RouterDevice device = (RouterDevice) mDeviceTopics.get(topic);
        if (device == null) {
            return;
        }
        int update = device.processMqttMsg(topic, message);
        if (update != BaseDevice.MSG_DUMMY) {
            synchronized (mDeviceMap) {
                mDeviceMap.put(device.getDeviceKey(), device);
            }
            Log.i(TAG, "notifyDeviceDataUpdate update=" + update);
            mSDKCoordinator.notifyDeviceDataUpdate(device.getDeviceKey(), update);
        }
    }

    private void processMqttMsg(MqttMessage message) {
        String jsonString = new String(message.getPayload());
        mSDKCoordinator.notifyHomeGeniusResult(jsonString);
    }
    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        Log.i(TAG, "deliveryComplete");
    }
}
