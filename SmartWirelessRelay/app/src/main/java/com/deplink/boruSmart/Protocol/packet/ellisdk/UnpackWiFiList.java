package com.deplink.boruSmart.Protocol.packet.ellisdk;

import android.util.Log;

/**
 * Created by benond on 2017/3/6.
 */

public class UnpackWiFiList implements OnRecvListener {
    private static final String TAG = "UnpackWiFiList";
    public String ssid;
    public int size;
    public int index;


    @Override
    public void OnRecvData(BasicPacket packet) {
        if (packet != null) {
            if (packet.fun == (byte) 0xEE) {

                size = packet.xdata[0];
                index = packet.xdata[1];
                int ssidLen = packet.xdata[2];
                if (ssidLen != 0) {
                    byte str[] = new byte[ssidLen + 1];
                    System.arraycopy(packet.xdata, 3, str, 0, ssidLen);
                    ssid = new String(str);
                    Log.d(TAG, "收到一个WiFi str:" + packet.xdata[3] + "len:" + packet.xdata[2] + " ssid:%@" + ssid);
                } else {
                    ssid = null;
                }

            }
        }
    }
}
