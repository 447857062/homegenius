package com.deplink.boruSmart.activity.device.doorbell.add;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.deplink.boruSmart.Protocol.packet.ellisdk.BasicPacket;
import com.deplink.boruSmart.Protocol.packet.ellisdk.EllESDK;
import com.deplink.boruSmart.Protocol.packet.ellisdk.EllE_Listener;
import com.deplink.boruSmart.Protocol.packet.ellisdk.Handler_Background;
import com.deplink.boruSmart.Protocol.packet.ellisdk.Handler_UiThread;
import com.deplink.boruSmart.Protocol.packet.ellisdk.WIFIData;
import com.deplink.boruSmart.activity.device.AddDeviceNameActivity;
import com.deplink.boruSmart.activity.device.doorbell.EditDoorbellActivity;
import com.deplink.boruSmart.constant.DeviceTypeConstant;
import com.deplink.boruSmart.manager.device.DeviceManager;
import com.deplink.boruSmart.manager.device.doorbeel.DoorbeelManager;
import com.deplink.boruSmart.util.DataExchange;
import com.deplink.boruSmart.view.combinationwidget.TitleLayout;
import com.deplink.boruSmart.view.toast.ToastSingleShow;

import deplink.com.smartwirelessrelay.homegenius.EllESDK.R;

public class AddDoorbellTipsActivity extends Activity implements View.OnClickListener, EllE_Listener {
    private static final String TAG = "AddDoorbellTipsActivity";
    private Button button_next_step;
    private DoorbeelManager mDoorbeelManager;
    private boolean isStartFromExperience;
    private TitleLayout layout_title;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_doorbell_tips);
        initViews();
        initDatas();
        initEvents();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isStartFromExperience = DeviceManager.getInstance().isStartFromExperience();
    }

    private void initEvents() {
        button_next_step.setOnClickListener(this);
    }

    EllESDK ellESDK;

    private void initDatas() {
        mDoorbeelManager = DoorbeelManager.getInstance();
        mDoorbeelManager.InitDoorbeelManager(this);
        ellESDK = EllESDK.getInstance();
        ellESDK.InitEllESDK(this, this);
        account = mDoorbeelManager.getSsid();
        password = mDoorbeelManager.getPassword();
        layout_title.setReturnClickListener(new TitleLayout.ReturnImageClickListener() {
            @Override
            public void onBackPressed() {
                AddDoorbellTipsActivity.this.onBackPressed();
            }
        });
    }

    private void initViews() {
        button_next_step = findViewById(R.id.button_next_step);
        layout_title = findViewById(R.id.layout_title);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_next_step:
                if(!isStartFromExperience){
                    ellESDK.startSearchDevs();
                }else{
                    if(mDoorbeelManager.isConfigWifi()){
                        ToastSingleShow.showText(AddDoorbellTipsActivity.this, "门铃网络已配置,现在重启门邻设备");
                        Intent intent = new Intent(AddDoorbellTipsActivity.this, EditDoorbellActivity.class);
                        startActivity(intent);
                    }
                }
                break;
        }
    }

    String account;
    String password;
    @Override
    public void onRecvEllEPacket(BasicPacket packet) {

    }

    public void onDoneClick(final long mac, final byte type, final byte ver) {
        if (account.length() > 0 && password.length() > 0) {
            Log.i(TAG, "onDoneClick setDevWiFiConfigWithMac mac=" + mac + "type=" + type + "ver=" + ver);
            //设置wifi
            Handler_Background.execute(new Runnable() {
                @Override
                public void run() {
                    WIFIData wifiData = new WIFIData(account, password);
                    int setresult = EllESDK.getInstance().setDevWiFiConfigWithMac(mac, type, ver, wifiData);
                    Log.i(TAG, "配置wifi结果是=" + setresult);
                    if (setresult == 1) {
                        Handler_UiThread.runTask("", new Runnable() {
                            @Override
                            public void run() {
                                EllESDK.getInstance().stopSearchDevs();
                                if(mDoorbeelManager.isConfigWifi()){
                                    ToastSingleShow.showText(AddDoorbellTipsActivity.this, "门铃网络已配置,现在重启门邻设备");
                                    Intent intent = new Intent(AddDoorbellTipsActivity.this, EditDoorbellActivity.class);
                                    startActivity(intent);
                                }else{
                                    ToastSingleShow.showText(AddDoorbellTipsActivity.this, "门铃网络已配置,现在重启门邻设备,等手机连上网络后进行设备添加");
                                    Intent intent = new Intent(AddDoorbellTipsActivity.this, AddDeviceNameActivity.class);
                                    intent.putExtra("DeviceType", DeviceTypeConstant.TYPE.TYPE_MENLING);
                                    startActivity(intent);
                                }

                            }
                        }, 0);

                    }
                }
            });

        }
    }

    @Override
    public void searchDevCBS(long mac, byte type, byte ver) {
        Log.e(TAG, "mac:" + mac + "type:" + type + "ver:" + ver);
        String macS=DataExchange.byteArrayToHexString( DataExchange.longToEightByte(mac));
        if (macS != null) {
            macS=macS.replaceAll("0x","").trim();
            macS=macS.replaceAll(" ","-");
        }
        Log.i(TAG,"savemac="+macS);
        mDoorbeelManager.setMac(macS);
        onDoneClick(mac, type, ver);
    }
}
