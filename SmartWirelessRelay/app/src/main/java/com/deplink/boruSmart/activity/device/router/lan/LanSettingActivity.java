package com.deplink.boruSmart.activity.device.router.lan;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.deplink.boruSmart.constant.AppConstant;
import com.deplink.boruSmart.manager.connect.remote.HomeGenius;
import com.deplink.boruSmart.manager.device.DeviceManager;
import com.deplink.boruSmart.manager.device.router.RouterManager;
import com.deplink.boruSmart.util.NetUtil;
import com.deplink.boruSmart.util.Perfence;
import com.deplink.boruSmart.util.StringValidatorUtil;
import com.deplink.boruSmart.view.combinationwidget.TitleLayout;
import com.deplink.boruSmart.view.toast.Ftoast;
import com.deplink.sdk.android.sdk.DeplinkSDK;
import com.deplink.sdk.android.sdk.EventCallback;
import com.deplink.sdk.android.sdk.SDKAction;
import com.deplink.sdk.android.sdk.device.router.RouterDevice;
import com.deplink.sdk.android.sdk.json.Lan;
import com.deplink.sdk.android.sdk.json.PERFORMANCE;
import com.deplink.sdk.android.sdk.manager.SDKManager;
import com.google.gson.Gson;

import deplink.com.smartwirelessrelay.homegenius.EllESDK.R;

public class LanSettingActivity extends Activity  {
    private static final String TAG = "LanSettingActivity";
    private EditText edittext_ip_address;
    private EditText edittext_submask;
    private EditText edittext_ip_addrss_start;
    private EditText edittext_ip_address_end;
    private RelativeLayout layout_ip_addrss_start;
    private RelativeLayout layout_ip_address_end;
    private CheckBox checkbox_dhcp_switch;
    private SDKManager manager;
    private EventCallback ec;
    private RouterManager mRouterManager;
    private HomeGenius mHomeGenius;
    private String channels;
    private TitleLayout layout_title;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lan_setting);
        initViews();
        initDatas();
        initEvents();
    }

    private void initDatas() {
        layout_title.setReturnClickListener(new TitleLayout.ReturnImageClickListener() {
            @Override
            public void onBackPressed() {
                LanSettingActivity.this.onBackPressed();
            }
        });
        layout_title.setEditTextClickListener(new TitleLayout.EditTextClickListener() {
            @Override
            public void onEditTextPressed() {
                Lan lan = new Lan();
                String lanIP = edittext_ip_address.getText().toString().trim();
                String submask = edittext_submask.getText().toString().trim();
                if (dhcpStatus.equalsIgnoreCase("ON")) {

                    if (!StringValidatorUtil.isIPString(lanIP)) {
                        Ftoast.create(LanSettingActivity.this).setText("IP地址格式不对").show();
                        return;
                    }
                    if (!StringValidatorUtil.isIPString(submask)) {
                        Ftoast.create(LanSettingActivity.this).setText("子网掩码格式不对").show();
                        return;
                    }

                    lan.setLANIP(lanIP);
                    lan.setNETMASK(submask);

                    String ipStart = edittext_ip_addrss_start.getText().toString().trim();
                    String ipEnd = edittext_ip_address_end.getText().toString().trim();
                    if (Integer.parseInt(ipStart) > 254 || ipStart.equals("") || Integer.parseInt(ipStart) == 0) {
                        Ftoast.create(LanSettingActivity.this).setText("输入1-254之间的数字").show();
                        return;
                    }
                    if (Integer.parseInt(ipEnd) > 254 || ipStart.equals("") || Integer.parseInt(ipEnd) == 0) {
                        Ftoast.create(LanSettingActivity.this).setText("输入1-254之间的数字").show();
                        return;
                    }
                    lan.setIpStart(ipStart);
                    lan.setIpOver(ipEnd);
                    lan.setDhcpStatus("ON");
                    if (NetUtil.isNetAvailable(LanSettingActivity.this)) {
                        try {
                            boolean isUserLogin;
                            isUserLogin = Perfence.getBooleanPerfence(AppConstant.USER_LOGIN);
                            if (isUserLogin) {
                                isSetLan = true;
                                mHomeGenius.setLan(lan, channels);
                            } else {
                                Ftoast.create(LanSettingActivity.this).setText("未登录，无法设置LAN,请登录后重试").show();
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        Ftoast.create(LanSettingActivity.this).setText("网络连接已断开").show();
                    }

                } else {
                    lan.setLANIP(lanIP);
                    lan.setNETMASK(submask);
                    lan.setDhcpStatus("OFF");
                    if (NetUtil.isNetAvailable(LanSettingActivity.this)) {

                        isSetLan = true;
                        mHomeGenius.setLan(lan, channels);

                    } else {
                        Ftoast.create(LanSettingActivity.this).setText("网络连接已断开").show();
                    }

                }
            }
        });
        mRouterManager = RouterManager.getInstance();
        mRouterManager.InitRouterManager();
        DeplinkSDK.initSDK(getApplicationContext(), Perfence.SDK_APP_KEY);
        manager = DeplinkSDK.getSDKManager();
        ec = new EventCallback() {

            @Override
            public void onSuccess(SDKAction action) {

            }

            @Override
            public void onBindSuccess(SDKAction action, String devicekey) {

            }


            @Override
            public void onFailure(SDKAction action, Throwable throwable) {

            }

            @Override
            public void notifyHomeGeniusResponse(String result) {
                super.notifyHomeGeniusResponse(result);
                parseDeviceReport(result);
            }

            @Override
            public void deviceOpSuccess(String op, String deviceKey) {
                super.deviceOpSuccess(op, deviceKey);
                switch (op) {
                    case RouterDevice.OP_GET_LAN:

                        break;
                    case RouterDevice.OP_SUCCESS:

                        break;
                }
            }

            @Override
            public void connectionLost(Throwable throwable) {
                super.connectionLost(throwable);

            }
        };
    }

    private void parseDeviceReport(String xmlStr) {
        String op = "";
        String method;
        Gson gson = new Gson();
        PERFORMANCE content = gson.fromJson(xmlStr, PERFORMANCE.class);
        op = content.getOP();
        method = content.getMethod();
        Log.i(TAG, "op=" + op + "method=" + method + "result=" + content.getResult() + "xmlStr=" + xmlStr);
        if (op == null) {
            if (content.getResult().equalsIgnoreCase("OK")) {
                Log.i(TAG, " mSDKCoordinator.notifyDeviceOpSuccess");
                if (isSetLan) {
                    Ftoast.create(LanSettingActivity.this).setText("设置成功").show();
                }
            }
        }  else if (op.equalsIgnoreCase("LAN")) {
            if (method.equalsIgnoreCase("REPORT")) {
                Lan lan = gson.fromJson(xmlStr, Lan.class);
                Log.i(TAG, "get lan =" + lan.toString());
                if (lan.getDhcpStatus().equalsIgnoreCase("ON")) {
                    checkbox_dhcp_switch.setChecked(true);
                    layout_ip_addrss_start.setVisibility(View.VISIBLE);
                    layout_ip_address_end.setVisibility(View.VISIBLE);
                    edittext_ip_addrss_start.setText(lan.getIpStart());
                    edittext_ip_address_end.setText(lan.getIpOver());
                } else if (lan.getDhcpStatus().equalsIgnoreCase("OFF")) {
                    checkbox_dhcp_switch.setChecked(false);
                    layout_ip_addrss_start.setVisibility(View.GONE);
                    layout_ip_address_end.setVisibility(View.GONE);
                }

                edittext_ip_address.setText(lan.getLANIP());
                if (lan.getLANIP().length() > 0) {
                    edittext_ip_address.setSelection(lan.getLANIP().length());
                }
                edittext_submask.setText(lan.getNETMASK());
                if (lan.getNETMASK().length() > 0) {
                    edittext_submask.setSelection(lan.getNETMASK().length());
                }
            }
        }
    }

    private boolean isSetLan;

    @Override
    protected void onResume() {
        super.onResume();
        if (!DeviceManager.getInstance().isStartFromExperience()) {
            mHomeGenius = new HomeGenius();
            boolean isStartFromExperience = DeviceManager.getInstance().isStartFromExperience();
            if(!isStartFromExperience){
                channels = mRouterManager.getCurrentSelectedRouter().getRouter().getChannels();
            }

            manager.addEventCallback(ec);
            if (NetUtil.isNetAvailable(this)) {
                if (channels != null) {
                    mHomeGenius.queryLan(channels);
                }
            } else {
                Ftoast.create(LanSettingActivity.this).setText("网络连接已断开").show();
            }
        }


    }

    @Override
    protected void onPause() {
        super.onPause();
        manager.removeEventCallback(ec);
    }

    private void initEvents() {
        checkbox_dhcp_switch.setOnCheckedChangeListener(dhcpCheckChangeListener);
    }

    private String dhcpStatus;
    private CompoundButton.OnCheckedChangeListener dhcpCheckChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                dhcpStatus = "ON";
            } else {
                dhcpStatus = "OFF";
            }
        }
    };

    private void initViews() {
        edittext_ip_address = (EditText) findViewById(R.id.edittext_ip_address);
        edittext_submask = (EditText) findViewById(R.id.edittext_submask);
        edittext_ip_addrss_start = (EditText) findViewById(R.id.edittext_ip_addrss_start);
        edittext_ip_address_end = (EditText) findViewById(R.id.edittext_ip_address_end);
        checkbox_dhcp_switch = (CheckBox) findViewById(R.id.checkbox_dhcp_switch);
        layout_ip_addrss_start = (RelativeLayout) findViewById(R.id.layout_ip_addrss_start);
        layout_ip_address_end = (RelativeLayout) findViewById(R.id.layout_ip_address_end);
        layout_title= (TitleLayout) findViewById(R.id.layout_title);
    }

}
