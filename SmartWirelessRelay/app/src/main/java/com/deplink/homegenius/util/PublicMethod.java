package com.deplink.homegenius.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static android.content.Context.TELEPHONY_SERVICE;

/**
 */

public class PublicMethod {
    public static long getTimeMs() {
        return System.currentTimeMillis();
    }
    public static String getSSID(Context ctx) {
        WifiManager wifiMgr = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifiMgr.getConnectionInfo();
        return info != null ? info.getSSID() : null;
    }

    public static int seq;

    /**
     * 取得SEQ值
     *
     * @return
     */
    public static int getSeq() {
        seq++;
        if (seq != 0)
            return seq;
        else
            return (seq + 1);
    }
    public static String getIPAddress(Context ctx) {
        WifiManager wifiMan = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifiMan.getConnectionInfo();
        int ipAddress = info.getIpAddress();
        if (ipAddress != 0) {
            return ((ipAddress & 0xff) + "." + (ipAddress >> 8 & 0xff) + "."
                    + (ipAddress >> 16 & 0xff) + "." + (ipAddress >> 24 & 0xff));

        }
        return null;
    }

    /**
     * 检查网络状态
     */
    public static int checkConnectionState(Context ctx) {
        ConnectivityManager mConnectivity = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        TelephonyManager mTelephony = (TelephonyManager) ctx.getSystemService(TELEPHONY_SERVICE);
    /* 检查有没有网络 */
        NetworkInfo info = mConnectivity.getActiveNetworkInfo();
        if (info == null || !mConnectivity.getBackgroundDataSetting()) {
            return -1;
        }

    /* 判断网络连接类型,只有在3G 或 wifi 里进行一些数据更新。 */
        int netType = info.getType();
        int netSubtype = info.getSubtype();
        if (netType == ConnectivityManager.TYPE_WIFI) {
            netType = 1;
        } else if (netType == ConnectivityManager.TYPE_MOBILE && netSubtype == TelephonyManager.NETWORK_TYPE_UMTS && !mTelephony.isNetworkRoaming()) {
            return 2;
        } else {
            return 0;
        }
        if (getSSID(ctx).equals("EllE.")) {
            return 3;

        } else if (netType == 1) {
            return 1;
        }
        return -1;

    }

    public static String getLocalIP(Context ctx) {
        if (NetStatusUtil.isWiFiActive(ctx)) {
            return PublicMethod.getIPAddress(ctx);
        }
        return null;
    }

    public static byte[] getUuid(Context context) {
        Log.i("getUuid","context!=null"+(context!=null));
        byte[] tmp = new byte[4];
        String m_szAndroidID = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        String m_szWLANMAC = wm.getConnectionInfo().getMacAddress();
        String m_szLongID = m_szAndroidID + m_szWLANMAC;
        MessageDigest m = null;
        try {
            m = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        if (m != null) {
            m.update(m_szLongID.getBytes(), 0, m_szLongID.length());
            byte p_md5Data[] = m.digest();
            System.arraycopy(p_md5Data, 0, tmp, 0, 4);
        }
        return tmp;
    }

}

