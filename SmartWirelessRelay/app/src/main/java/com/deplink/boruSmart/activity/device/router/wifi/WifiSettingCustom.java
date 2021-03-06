package com.deplink.boruSmart.activity.device.router.wifi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.deplink.boruSmart.constant.AppConstant;
import com.deplink.boruSmart.manager.connect.remote.HomeGenius;
import com.deplink.boruSmart.manager.device.DeviceManager;
import com.deplink.boruSmart.manager.device.router.RouterManager;
import com.deplink.boruSmart.util.NetUtil;
import com.deplink.boruSmart.util.Perfence;
import com.deplink.boruSmart.view.combinationwidget.TitleLayout;
import com.deplink.boruSmart.view.toast.Ftoast;
import com.deplink.sdk.android.sdk.DeplinkSDK;
import com.deplink.sdk.android.sdk.EventCallback;
import com.deplink.sdk.android.sdk.SDKAction;
import com.deplink.sdk.android.sdk.device.router.RouterDevice;
import com.deplink.sdk.android.sdk.json.PERFORMANCE;
import com.deplink.sdk.android.sdk.json.VISITOR;
import com.deplink.sdk.android.sdk.json.Wifi;
import com.deplink.sdk.android.sdk.manager.SDKManager;
import com.google.gson.Gson;

import deplink.com.smartwirelessrelay.homegenius.EllESDK.R;

public class WifiSettingCustom extends Activity implements View.OnClickListener {
    private static final String TAG = "WifiSettingCustom";
    private RelativeLayout layout_encryption;
    private RelativeLayout layout_password;
    private RelativeLayout layout_wifiname_setting;
    private TextView textview_encryption;
    private TextView textview_wifi_name;
    private TextView textview_password;
    private String encryptionType;
    private String password;
    private String wifiName;
    private String wifiSwitch;
    private SDKManager manager;
    private EventCallback ec;
    private CheckBox checkbox_wifi_switch;
    private RouterManager mRouterManager;
    private HomeGenius mHomeGenius;
    private String channels;
    private TitleLayout layout_title;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_setting_custom);
        initViews();
        initDatas();
        initEvent();


    }

    private void initDatas() {
        layout_title.setReturnClickListener(new TitleLayout.ReturnImageClickListener() {
            @Override
            public void onBackPressed() {
                WifiSettingCustom.this.onBackPressed();
            }
        });
        layout_title.setEditTextClickListener(new TitleLayout.EditTextClickListener() {
            @Override
            public void onEditTextPressed() {
                VISITOR visitor = new VISITOR();
                Wifi content;
                if (!wifiName.equals("")) {
                    visitor.setWifiSSID(wifiName);
                }
                if (!wifiSwitch.equals("")) {
                    visitor.setWifiStatus(wifiSwitch);
                }
                if (!password.equals("")) {
                    visitor.setWifiPassword(password);
                }
                if (checkbox_wifi_switch.isChecked()) {
                    visitor.setWifiStatus("ON");
                } else {
                    visitor.setWifiStatus("OFF");
                }

                if (!encryptionType.equals("")) {
                    visitor.setEncryption(encryptionType);
                }
                content = new Wifi();
                content.setVISITOR(visitor);

                isSetWifiVisitor = true;
                mHomeGenius.setWifi(content, channels);

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
            public void deviceOpSuccess(String op, String deviceKey) {
                super.deviceOpSuccess(op, deviceKey);
                switch (op) {
                    case RouterDevice.OP_GET_WIFI:

                        break;
                    case RouterDevice.OP_SUCCESS:

                        break;
                }
            }

            @Override
            public void notifyHomeGeniusResponse(String result) {
                super.notifyHomeGeniusResponse(result);
                parseDeviceReport(result);
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
                if (isSetWifiVisitor) {
                    Ftoast.create(this).setText("设置成功").show();
                }
            }
        } else {
            if (op.equalsIgnoreCase("WIFI")) {
                if (method.equalsIgnoreCase("REPORT")) {
                    final Wifi wifi = gson.fromJson(xmlStr, Wifi.class);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (!isActivityResultSetWifiname) {
                                wifiName = wifi.getVISITOR().getWifiSSID();
                            }
                            if (!isActivityResultSetEncryptType) {
                                encryptionType = wifi.getVISITOR().getEncryption();
                            }

                            textview_wifi_name.setText(wifiName);

                            if (encryptionType.equals("")) {
                                encryptionType = "--";
                            }
                            switch (encryptionType) {
                                case "none":
                                    textview_encryption.setText("无加密");
                                    break;
                                case "psk-mixed":
                                    textview_encryption.setText("混合加密");
                                    break;
                                case "psk2":
                                    textview_encryption.setText("强加密");
                                    break;
                            }
                            if (encryptionType.equalsIgnoreCase("none")) {
                                password = "";
                                textview_password.setText("");
                            } else {
                                if (!isActivityResultSetWifiPassword) {
                                    password = wifi.getVISITOR().getWifiPassword();
                                }
                                textview_password.setText(password);
                            }


                            wifiSwitch = wifi.getVISITOR().getWifiStatus();
                            Log.i(TAG, "wifiSwitch=" + wifiSwitch);
                            if (wifiSwitch.equalsIgnoreCase("ON")) {
                                checkbox_wifi_switch.setChecked(true);
                            } else {
                                checkbox_wifi_switch.setChecked(false);
                            }
                        }
                    });

                }
            }
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!DeviceManager.getInstance().isStartFromExperience()) {
            mHomeGenius = new HomeGenius();
            channels = mRouterManager.getCurrentSelectedRouter().getRouter().getChannels();
            manager.addEventCallback(ec);
            if (NetUtil.isNetAvailable(this)) {
                if (channels != null) {
                    Log.i(TAG, "onResume queryWifi");
                    mHomeGenius.queryWifi(channels);
                }
            } else {
                Ftoast.create(this).setText("网络连接已断开").show();
            }
        }

    }


    @Override
    protected void onPause() {
        super.onPause();
        manager.removeEventCallback(ec);
    }

    private void initEvent() {
        layout_encryption.setOnClickListener(this);
        layout_password.setOnClickListener(this);


        layout_wifiname_setting.setOnClickListener(this);
        checkbox_wifi_switch.setOnCheckedChangeListener(wifiSwitchCheckChangeListener);
    }

    private CompoundButton.OnCheckedChangeListener wifiSwitchCheckChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            VISITOR visitor = new VISITOR();
            if (isChecked) {
                visitor.setWifiStatus("ON");
            } else {
                visitor.setWifiStatus("OFF");
            }
        }
    };

    private void initViews() {
        layout_encryption = (RelativeLayout) findViewById(R.id.layout_encryption);
        layout_password = (RelativeLayout) findViewById(R.id.layout_password);
        layout_wifiname_setting = (RelativeLayout) findViewById(R.id.layout_wifiname_setting);
        textview_encryption = (TextView) findViewById(R.id.textview_encryption);
        textview_wifi_name = (TextView) findViewById(R.id.textview_wifi_name);
        textview_password = (TextView) findViewById(R.id.textview_password);
        checkbox_wifi_switch = (CheckBox) findViewById(R.id.checkbox_wifi_switch);
        layout_title= (TitleLayout) findViewById(R.id.layout_title);
    }

    /**
     * startactivityforresult 返回就不显示查询到的参数，显示用户修改的参数.
     */
    private boolean isActivityResultSetWifiname;
    private boolean isActivityResultSetEncryptType;
    private boolean isActivityResultSetWifiPassword;
    private static final int REQUESTCODE_SET_WIFI_NAME = 100;
    private static final int REQUESTCODE_SET_ENCRYPT_TYPE = 101;
    private static final int REQUESTCODE_SET_WIFI_PASSWORD = 102;
    private boolean isSetWifiVisitor;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.layout_encryption:
                if (wifiSwitch != null) {
                    if (wifiSwitch.equals("ON")) {
                        Intent intent = new Intent(WifiSettingCustom.this, EncryptTypeActivity.class);
                        intent.putExtra(AppConstant.WIFISETTING.WIFI_ENCRYPT_TYPE, encryptionType);
                        intent.putExtra(AppConstant.WIFISETTING.WIFI_TYPE, AppConstant.WIFISETTING.WIFI_TYPE_VISITOR);
                        startActivityForResult(intent, REQUESTCODE_SET_ENCRYPT_TYPE);
                    } else {
                        Ftoast.create(this).setText("访客wifi未打开").show();
                    }
                } else {
                    Ftoast.create(this).setText("未获取到访客wifi状态").show();
                }


                break;
            case R.id.layout_wifiname_setting:
                if (wifiSwitch != null) {
                    if (wifiSwitch.equals("ON")) {
                        Intent intentWifiname = new Intent(WifiSettingCustom.this, WifinameSetActivity.class);
                        intentWifiname.putExtra(AppConstant.WIFISETTING.WIFI_NAME, wifiName);
                        intentWifiname.putExtra(AppConstant.WIFISETTING.WIFI_TYPE, AppConstant.WIFISETTING.WIFI_TYPE_VISITOR);
                        startActivityForResult(intentWifiname, REQUESTCODE_SET_WIFI_NAME);
                    } else {
                        Ftoast.create(this).setText("访客wifi未打开").show();
                    }
                } else {
                    Ftoast.create(this).setText("还未获取到访客wifi状态").show();
                }


                break;
            case R.id.layout_password:
                if (wifiSwitch != null) {
                    if (wifiSwitch.equals("ON")) {
                        Intent intentAlertPassword = new Intent(WifiSettingCustom.this, AlertWifiPasswordActivity.class);
                        intentAlertPassword.putExtra(AppConstant.WIFISETTING.WIFI_TYPE, AppConstant.WIFISETTING.WIFI_TYPE_VISITOR);
                        intentAlertPassword.putExtra(AppConstant.WIFISETTING.WIFI_PASSWORD, password);
                        startActivityForResult(intentAlertPassword, REQUESTCODE_SET_WIFI_PASSWORD);
                    } else {
                        Ftoast.create(this).setText("访客wifi未打开").show();
                    }
                } else {
                    Ftoast.create(this).setText("还未获取到访客wifi状态").show();
                }


                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUESTCODE_SET_WIFI_NAME:
                    isActivityResultSetWifiname = true;
                    wifiName = data.getExtras().getString("wifiname");
                    Log.i(TAG, "onActivityResult wifiname=" + wifiName);
                    break;
                case REQUESTCODE_SET_WIFI_PASSWORD:
                    isActivityResultSetWifiPassword = true;
                    password = data.getExtras().getString("password");
                    Log.i(TAG, "onActivityResult password=" + password);
                    break;
                case REQUESTCODE_SET_ENCRYPT_TYPE:
                    isActivityResultSetEncryptType = true;
                    encryptionType = data.getExtras().getString("encryptionType");
                    Log.i(TAG, "onActivityResult encryptionType=" + encryptionType);
                    break;
            }
        }
    }
}
