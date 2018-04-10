package com.deplink.boruSmart.activity.device.router.qos;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.deplink.boruSmart.activity.personal.login.LoginActivity;
import com.deplink.boruSmart.constant.AppConstant;
import com.deplink.boruSmart.manager.connect.remote.HomeGenius;
import com.deplink.boruSmart.manager.device.DeviceManager;
import com.deplink.boruSmart.manager.device.router.RouterManager;
import com.deplink.boruSmart.util.NetUtil;
import com.deplink.boruSmart.util.Perfence;
import com.deplink.boruSmart.view.combinationwidget.TitleLayout;
import com.deplink.boruSmart.view.dialog.AlertDialog;
import com.deplink.boruSmart.view.toast.Ftoast;
import com.deplink.sdk.android.sdk.DeplinkSDK;
import com.deplink.sdk.android.sdk.EventCallback;
import com.deplink.sdk.android.sdk.SDKAction;
import com.deplink.sdk.android.sdk.json.PERFORMANCE;
import com.deplink.sdk.android.sdk.json.Qos;
import com.deplink.sdk.android.sdk.manager.SDKManager;
import com.google.gson.Gson;

import deplink.com.smartwirelessrelay.homegenius.EllESDK.R;

public class QosSettingActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "QosSettingActivity";
    private RelativeLayout layout_model_A;
    private RelativeLayout layout_model_B;
    private RelativeLayout layout_model_download;
    private ImageView imageview_model_a;
    private ImageView imageview_model_b;
    private ImageView imageview_model_download;
    private String currentQosMode;
    private SDKManager manager;
    private EventCallback ec;
    private CheckBox checkbox_qos_switch;
    private RouterManager mRouterManager;
    private Qos qos;
    private HomeGenius mHomeGenius;
    private String channels;
    private TitleLayout layout_title;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qos_setting);
        initViews();
        initDatas();
        initEvents();

    }

    private void initDatas() {
        layout_title.setReturnClickListener(new TitleLayout.ReturnImageClickListener() {
            @Override
            public void onBackPressed() {
                QosSettingActivity.this.onBackPressed();
            }
        });
        layout_title.setEditTextClickListener(new TitleLayout.EditTextClickListener() {
            @Override
            public void onEditTextPressed() {

                    qos = new Qos();
                    qos.setCLASSIFY(currentQosMode);
                    if (checkbox_qos_switch.isChecked()) {
                        qos.setSWITCH("ON");
                    } else {
                        qos.setSWITCH("OFF");
                    }
                    if (NetUtil.isNetAvailable(QosSettingActivity.this)) {
                        boolean isUserLogin;
                        isUserLogin = Perfence.getBooleanPerfence(AppConstant.USER_LOGIN);
                        if (isUserLogin) {
                            Ftoast.create(QosSettingActivity.this).setText("QOS已设置").show();
                            if(mHomeGenius!=null){
                                mHomeGenius.setQos(qos, channels);
                            }
                        } else {
                            Ftoast.create(QosSettingActivity.this).setText("未登录，无法设置静态上网,请登录后重试").show();
                        }

                    } else {
                        Ftoast.create(QosSettingActivity.this).setText("网络连接已断开").show();
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
            public void deviceOpSuccess(final String op, String deviceKey) {
                super.deviceOpSuccess(op, deviceKey);


            }

            @Override
            public void connectionLost(Throwable throwable) {
                super.connectionLost(throwable);
                Perfence.setPerfence(AppConstant.USER_LOGIN, false);
                new AlertDialog(QosSettingActivity.this).builder().setTitle("账号异地登录")
                        .setMsg("当前账号已在其它设备上登录,是否重新登录")
                        .setPositiveButton("确认", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                startActivity(new Intent(QosSettingActivity.this, LoginActivity.class));
                            }
                        }).setNegativeButton("取消", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                }).show();
            }
        };
    }

    private void parseDeviceReport(String xmlStr) {
        String op;
        String method;
        Gson gson = new Gson();
        PERFORMANCE content = gson.fromJson(xmlStr, PERFORMANCE.class);
        op = content.getOP();
        method = content.getMethod();
        Log.i(TAG, "op=" + op + "method=" + method + "result=" + content.getResult() + "xmlStr=" + xmlStr);

        if (op == null) {
            if (content.getResult().equalsIgnoreCase("OK")) {
                Log.i(TAG, " mSDKCoordinator.notifyDeviceOpSuccess");

            }
        } else {
            if (op.equalsIgnoreCase("QOS")) {
                if (method.equalsIgnoreCase("REPORT")) {
                    qos = gson.fromJson(xmlStr, Qos.class);
                    if (qos.getSWITCH().equalsIgnoreCase("ON")) {
                        layout_model_A.setVisibility(View.VISIBLE);
                        layout_model_B.setVisibility(View.VISIBLE);
                        layout_model_download.setVisibility(View.VISIBLE);
                        checkbox_qos_switch.setChecked(true);
                        currentQosMode = qos.getCLASSIFY();
                        switch (currentQosMode) {
                            case "1":
                                layout_model_A.callOnClick();
                                break;
                            case "2":
                                layout_model_B.callOnClick();
                                break;
                            case "3":
                                layout_model_download.callOnClick();
                                break;
                        }
                    } else if (qos.getSWITCH().equalsIgnoreCase("OFF")) {
                        checkbox_qos_switch.setChecked(false);
                        layout_model_A.setVisibility(View.GONE);
                        layout_model_B.setVisibility(View.GONE);
                        layout_model_download.setVisibility(View.GONE);
                    }

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
                    mHomeGenius.queryQos(channels);
                }
            } else {
                Ftoast.create(QosSettingActivity.this).setText("网络连接已断开").show();
            }
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        manager.removeEventCallback(ec);
    }

    private void initEvents() {
        layout_model_A.setOnClickListener(this);
        layout_model_B.setOnClickListener(this);
        layout_model_download.setOnClickListener(this);
        checkbox_qos_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    layout_model_A.setVisibility(View.VISIBLE);
                    layout_model_B.setVisibility(View.VISIBLE);
                    layout_model_download.setVisibility(View.VISIBLE);
                } else {
                    layout_model_A.setVisibility(View.GONE);
                    layout_model_B.setVisibility(View.GONE);
                    layout_model_download.setVisibility(View.GONE);
                }
            }
        });
    }

    private void initViews() {
        layout_model_A = (RelativeLayout) findViewById(R.id.layout_model_A);
        layout_model_B = (RelativeLayout) findViewById(R.id.layout_model_B);
        layout_model_download = (RelativeLayout) findViewById(R.id.layout_model_download);
        imageview_model_a = (ImageView) findViewById(R.id.imageview_model_a);
        imageview_model_b = (ImageView) findViewById(R.id.imageview_model_b);
        imageview_model_download = (ImageView) findViewById(R.id.imageview_model_download);
        checkbox_qos_switch = (CheckBox) findViewById(R.id.checkbox_qos_switch);
        layout_title= (TitleLayout) findViewById(R.id.layout_title);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.layout_model_A:
                imageview_model_a.setImageLevel(1);
                imageview_model_b.setImageLevel(0);
                imageview_model_download.setImageLevel(0);
                currentQosMode = "1";
                break;
            case R.id.layout_model_B:
                imageview_model_a.setImageLevel(0);
                imageview_model_b.setImageLevel(1);
                imageview_model_download.setImageLevel(0);

                currentQosMode = "2";
                break;
            case R.id.layout_model_download:
                imageview_model_a.setImageLevel(0);
                imageview_model_b.setImageLevel(0);
                imageview_model_download.setImageLevel(1);

                currentQosMode = "3";
                break;

        }
    }
}
