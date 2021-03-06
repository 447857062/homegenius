package com.deplink.boruSmart.manager.connect.local.udp;

import android.content.Context;
import android.util.Log;

import com.deplink.boruSmart.Protocol.packet.GeneralPacket;
import com.deplink.boruSmart.util.NetStatusUtil;
import com.deplink.boruSmart.util.Perfence;
import com.deplink.boruSmart.constant.AppConstant;
import com.deplink.boruSmart.manager.connect.local.udp.packet.UdpPacket;
import com.deplink.boruSmart.util.PublicMethod;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;


/**
 * Created by benond on 2017/2/6.
 */

public class UdpThread {

    private static final String TAG = "UdpThread";
    Timer timer;
    public DatagramSocket dataSocket;
    public Context mContext;
    private UdpPacket udp;
    private TimerTask mTimerTimeoutTask;
    public UdpThread(Context context, UdpPacket udpPacket) {
        mContext = context;
        udp = udpPacket;
        try {
            dataSocket = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
        }

    }
    public void timerTimeout() {
        //"设备状体定时器运行正常";
        //先判断当前的网络状态，没有网络则不执行设备的检查
        //当前如果是WiFi的话，则优先执行本地查找，确定本地没有设备后再执行远程查找
        boolean net = NetStatusUtil.isWiFiActive(mContext);
        //更新下本地的网络状态和IP地址
        PublicMethod.getLocalIP(mContext);
        if (net) {
            wifiCheckHandler();
        }
    }
    public void wifiCheckHandler() {
        try {
            //发送一个局域网查询包
            GeneralPacket packet;
            packet = new GeneralPacket(InetAddress.getByName("255.255.255.255"), AppConstant.UDP_CONNECT_PORT, mContext);
            //查询设备，探测设备区别(这里探测设备就不需要发送查询的gson数据了)
            // 设备uid，必填
            String uid;
            //连接发送默认的uid
            uid = Perfence.getPerfence(AppConstant.PERFENCE_BIND_APP_UUID);
            if(!uid.equalsIgnoreCase("")){
                packet.packCheckPacketWithUID( uid);
            }
            Log.i(TAG, "wifiCheckHandler send udp packet");
            udp.writeNet(packet);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }
    public void open() {
        Log.i(TAG,"发送探测包open");
        timer = new Timer();
        try {
            if(mTimerTimeoutTask!=null){
                mTimerTimeoutTask.cancel();
            }
            mTimerTimeoutTask=new TimerTask() {
                @Override
                public void run() {
                        timerTimeout();

                }
            };
            timer.schedule(mTimerTimeoutTask, 1000, 5000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void cancel() {
        Log.i(TAG,"取消发送探测包");
        if(mTimerTimeoutTask!=null){
            mTimerTimeoutTask.cancel();
            mTimerTimeoutTask = null;
        }
        if(timer!=null){
            timer.cancel();
            timer=null;
        }

    }
}
