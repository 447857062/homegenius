package com.deplink.boruSmart.activity.device.router.wifi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;

import com.deplink.boruSmart.constant.AppConstant;
import com.deplink.boruSmart.manager.device.DeviceManager;
import com.deplink.boruSmart.manager.device.router.RouterManager;
import com.deplink.boruSmart.util.Perfence;
import com.deplink.boruSmart.view.combinationwidget.TitleLayout;
import com.deplink.boruSmart.view.toast.Ftoast;
import com.deplink.sdk.android.sdk.DeplinkSDK;
import com.deplink.sdk.android.sdk.EventCallback;
import com.deplink.sdk.android.sdk.SDKAction;
import com.deplink.sdk.android.sdk.device.router.RouterDevice;
import com.deplink.sdk.android.sdk.manager.SDKManager;

import deplink.com.smartwirelessrelay.homegenius.EllESDK.R;

public class WifinameSetActivity extends Activity {
    /**
     * wifi名称密码设置成功，后面要重启了
     */
    private static final int MSG_LOCAL_OP_RETURN_OK = 1;
    private String wifiname;

    private EditText edittext_wifi_name;
    private String wifiType;
    private SDKManager manager;
    private EventCallback ec;
    private RouterManager mRouterManager;
    private String channels;
    private TitleLayout layout_title;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifiname_set);
        initViews();
        initDatas();
        initEvents();
    }
    @Override
    protected void onResume() {
        super.onResume();
        if(! DeviceManager.getInstance().isStartFromExperience()){
            manager.addEventCallback(ec);
            isLogin = Perfence.getBooleanPerfence(AppConstant.USER_LOGIN);
            channels = mRouterManager.getCurrentSelectedRouter().getRouter().getChannels();
        }


    }
    @Override
    protected void onPause() {
        super.onPause();
        manager.removeEventCallback(ec);
    }

    private void initDatas() {
        layout_title.setReturnClickListener(new TitleLayout.ReturnImageClickListener() {
            @Override
            public void onBackPressed() {
                WifinameSetActivity.this.onBackPressed();
            }
        });
        layout_title.setEditTextClickListener(new TitleLayout.EditTextClickListener() {
            @Override
            public void onEditTextPressed() {
                wifiname = edittext_wifi_name.getText().toString().trim();
                if (wifiname.equals("")) {
                    Ftoast.create(WifinameSetActivity.this).setText("还没有输入wifi名称").show();
                    return;
                }
                if (isLogin && channels != null) {
                    isSetWifiname = true;
                    Intent intent = new Intent();
                    switch (wifiType) {
                        case AppConstant.WIFISETTING.WIFI_TYPE_2G:
                            intent.putExtra("wifiname", wifiname);
                            setResult(RESULT_OK, intent);
                            break;
                        case AppConstant.WIFISETTING.WIFI_TYPE_VISITOR:
                            intent.putExtra("wifiname", wifiname);
                            setResult(RESULT_OK, intent);
                            break;
                    }
                    WifinameSetActivity. this.finish();
                } else {
                    Intent intent = new Intent();
                    intent.putExtra("wifiname", wifiname);
                    setResult(RESULT_OK, intent);
                    WifinameSetActivity.this.finish();
                }
            }
        });
        mRouterManager=RouterManager.getInstance();
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
                    case RouterDevice.OP_SUCCESS:
                        if (isSetWifiname) {
                            Ftoast.create(WifinameSetActivity.this).setText("设置成功").show();
                        }
                        break;
                }
            }

            @Override
            public void connectionLost(Throwable throwable) {
                super.connectionLost(throwable);

            }
        };
        try {
            wifiname = getIntent().getStringExtra(AppConstant.WIFISETTING.WIFI_NAME);
            wifiType = getIntent().getStringExtra(AppConstant.WIFISETTING.WIFI_TYPE);
            if (wifiname != null) {
                edittext_wifi_name.setText(wifiname);
                edittext_wifi_name.setSelection(wifiname.length());
            } else {
                wifiname = "";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initEvents() {

    }

    private void initViews() {
        layout_title= (TitleLayout) findViewById(R.id.layout_title);
        edittext_wifi_name = (EditText) findViewById(R.id.edittext_wifi_name);

    }

    private boolean isSetWifiname;
    private boolean isLogin;


}
