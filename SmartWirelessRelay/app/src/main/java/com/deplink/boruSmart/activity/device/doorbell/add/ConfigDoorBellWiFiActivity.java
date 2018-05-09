package com.deplink.boruSmart.activity.device.doorbell.add;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.deplink.boruSmart.Protocol.packet.ellisdk.BasicPacket;
import com.deplink.boruSmart.Protocol.packet.ellisdk.EllESDK;
import com.deplink.boruSmart.Protocol.packet.ellisdk.EllE_Listener;
import com.deplink.boruSmart.Protocol.packet.ellisdk.Handler_Background;
import com.deplink.boruSmart.Protocol.packet.ellisdk.Handler_UiThread;
import com.deplink.boruSmart.Protocol.packet.ellisdk.WIFIData;
import com.deplink.boruSmart.activity.device.doorbell.EditDoorbellActivity;
import com.deplink.boruSmart.activity.device.doorbell.add.wifilistadapter.WIFIListAdapter;
import com.deplink.boruSmart.manager.device.doorbeel.DoorbeelManager;
import com.deplink.boruSmart.util.DataExchange;
import com.deplink.boruSmart.util.WeakRefHandler;
import com.deplink.boruSmart.view.combinationwidget.TitleLayout;
import com.deplink.boruSmart.view.dialog.DoorBellConfigWifiDialog;
import com.deplink.boruSmart.view.dialog.InputAlertDialog;
import com.deplink.boruSmart.view.dialog.loadingdialog.DialogThreeBounce;
import com.deplink.boruSmart.view.toast.Ftoast;

import java.util.ArrayList;
import java.util.List;

import deplink.com.smartwirelessrelay.homegenius.EllESDK.R;

public class ConfigDoorBellWiFiActivity extends Activity implements EllE_Listener, View.OnClickListener {
    private static final String TAG = "ConfigDoorBell";
    private TitleLayout layout_title;
    private Button button_input_wifi_info;
    private Button button_select_wifi;
    private ListView wifi_list;
    private List<String> mWiFilist;
    private WIFIListAdapter wifiListAdapter;
    private EllESDK ellESDK;
    private DoorbeelManager mDoorbeelManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config_door_bell_wi_fi);
        initViews();
        initEvents();
        initDatas();
    }

    private void initDatas() {
        initialized();
    }


    private void initialized() {
        mDoorbeelManager = DoorbeelManager.getInstance();
        mDoorbeelManager.InitDoorbeelManager(this);
        mWiFilist = new ArrayList<>();
        wifiListAdapter = new WIFIListAdapter(this, mWiFilist);

        ellESDK = EllESDK.getInstance();
        ellESDK.startSearchDevs();
        ellESDK.setElleListener(this);
        wifi_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final String wifiSsid = mWiFilist.get(position);
                new InputAlertDialog(ConfigDoorBellWiFiActivity.this).builder()
                        .setTitle(wifiSsid)
                        .setEditTextHint("请输入wifi密码")
                        .setEditTextHeader("密码:")
                        .setPositiveButton("确认", new InputAlertDialog.onSureBtnClickListener() {
                            @Override
                            public void onSureBtnClicked(final String result) {
                                //配置wifi
                                Log.i(TAG, "onDoneClick setDevWiFiConfigWithMac mac=" + mac + "type=" + type + "ver=" + ver);
                                //设置wifi
                                WIFIData wifiData = new WIFIData(wifiSsid, result);
                                int setresult = EllESDK.getInstance().setDevWiFiConfigWithMac(mac, type, ver, wifiData);
                                Log.i(TAG, "配置wifi结果是=" + setresult);
                                if (setresult == 1) {
                                    EllESDK.getInstance().stopSearchDevs();
                                    Ftoast.create(ConfigDoorBellWiFiActivity.this).setText("门铃网络已配置,现在重启门铃设备").show();
                                    if (mDoorbeelManager.isConfigWifi()) {
                                        Intent intent = new Intent(ConfigDoorBellWiFiActivity.this, EditDoorbellActivity.class);
                                        startActivity(intent);
                                    } else {
                                        //启动配网判断界面
                                        startActivity(new Intent(ConfigDoorBellWiFiActivity.this,
                                                ConfigWifiJugeActivity.class));
                                    }
                                }
                                //
                            }
                        }).setNegativeButton("取消", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                }).show();

            }
        });
    }

    private void initEvents() {
        layout_title.setReturnClickListener(new TitleLayout.ReturnImageClickListener() {
            @Override
            public void onBackPressed() {
                ConfigDoorBellWiFiActivity.this.onBackPressed();
            }
        });
        button_input_wifi_info.setOnClickListener(this);
        button_select_wifi.setOnClickListener(this);
    }

    private void initViews() {
        layout_title = findViewById(R.id.layout_title);
        button_input_wifi_info = findViewById(R.id.button_input_wifi_info);
        button_select_wifi = findViewById(R.id.button_select_wifi);
        wifi_list = findViewById(R.id.wifi_list);
    }

    @Override
    public void onRecvEllEPacket(BasicPacket packet) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        DialogThreeBounce.showLoading(this);
        mHandler.sendEmptyMessageDelayed(HIDE_LOADING,3000);
    }
    private static  final int  HIDE_LOADING=1;
    private boolean isReceiverdWifiList=false;
    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if(!isReceiverdWifiList){
                DialogThreeBounce.hideLoading();
            }
            return true;
        }
    };
    private Handler mHandler = new WeakRefHandler(mCallback);

    private long mac;
    private byte type;
    private byte ver;

    @Override
    public void searchDevCBS(long mac, byte type, byte ver) {
        mWiFilist.addAll(ellESDK.getDevWiFiListWithMac(mac, type, ver));
        Handler_UiThread.runTask("", new Runnable() {
            @Override
            public void run() {
                wifi_list.setAdapter(wifiListAdapter);
                wifiListAdapter.notifyDataSetChanged();
            }
        }, 0);

        isReceiverdWifiList=true;
        DialogThreeBounce.hideLoading();
        String macS= DataExchange.byteArrayToHexString( DataExchange.longToEightByte(mac));
        if (macS != null) {
            macS=macS.replaceAll("0x","").trim();
            macS=macS.replaceAll(" ","-");
        }
        Log.i(TAG,"savemac="+macS);
        mDoorbeelManager.setMac(macS);
        this.mac = mac;
        this.type = type;
        this.ver = ver;

        Log.i(TAG, "mWiFilist.size()=" + mWiFilist.size());
        ellESDK.stopSearchDevs();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_input_wifi_info:
                new DoorBellConfigWifiDialog(ConfigDoorBellWiFiActivity.this).builder()
                        .setPositiveButton(new DoorBellConfigWifiDialog.onSureBtnClickListener() {
                            @Override
                            public void onSureBtnClicked(final String ssid, final String password) {
                                //配置wifi
                                Log.i(TAG, "onDoneClick setDevWiFiConfigWithMac mac=" + mac + "type=" + type + "ver=" + ver);
                                //设置wifi
                                Handler_Background.execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        WIFIData wifiData = new WIFIData(ssid, password);
                                        int setresult = EllESDK.getInstance().setDevWiFiConfigWithMac(mac, type, ver, wifiData);
                                        Log.i(TAG, "配置wifi结果是=" + setresult);
                                        if (setresult == 1) {
                                            Handler_UiThread.runTask("", new Runnable() {
                                                @Override
                                                public void run() {
                                                    EllESDK.getInstance().stopSearchDevs();
                                                    //启动配网判断界面
                                                    startActivity(new Intent(ConfigDoorBellWiFiActivity.this,
                                                            ConfigWifiJugeActivity.class));
                                                }
                                            }, 0);
                                        }
                                    }
                                });

                            }
                        }).setNegativeButton(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                }).show();
                break;
            case R.id.button_select_wifi:
                DialogThreeBounce.showLoading(this);
                isReceiverdWifiList=false;
                mHandler.sendEmptyMessageDelayed(HIDE_LOADING,3000);
                ellESDK.startSearchDevs();
                mWiFilist.clear();
                wifiListAdapter.notifyDataSetChanged();
                break;
            default:
                break;
        }
    }
}
