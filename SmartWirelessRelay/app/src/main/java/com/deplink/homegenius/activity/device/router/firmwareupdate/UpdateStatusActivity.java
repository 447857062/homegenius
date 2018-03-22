package com.deplink.homegenius.activity.device.router.firmwareupdate;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.deplink.homegenius.activity.device.DevicesActivity;
import com.deplink.homegenius.activity.device.remoteControl.topBox.AddTopBoxActivity;
import com.deplink.homegenius.activity.personal.login.LoginActivity;
import com.deplink.homegenius.constant.AppConstant;
import com.deplink.homegenius.manager.device.router.RouterManager;
import com.deplink.homegenius.util.Perfence;
import com.deplink.homegenius.util.WeakRefHandler;
import com.deplink.homegenius.view.combinationwidget.TitleLayout;
import com.deplink.homegenius.view.dialog.AlertDialog;
import com.deplink.sdk.android.sdk.DeplinkSDK;
import com.deplink.sdk.android.sdk.EventCallback;
import com.deplink.sdk.android.sdk.SDKAction;
import com.deplink.sdk.android.sdk.json.DeviceUpgradeReport;
import com.deplink.sdk.android.sdk.json.PERFORMANCE;
import com.deplink.sdk.android.sdk.manager.SDKManager;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import deplink.com.smartwirelessrelay.homegenius.EllESDK.R;

public class UpdateStatusActivity extends Activity implements View.OnClickListener{
    private static final String TAG = "UpdateStatusActivity";
    private TextView textview_updateing;
    private SDKManager manager;
    private EventCallback ec;
    private RouterManager mRouterManager;
    private Button button_sure;
    private TitleLayout layout_title;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_status);
        initViews();
        initDatas();
        initEvents();

    }

    private void initEvents() {
        button_sure.setOnClickListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        manager.removeEventCallback(ec);
    }

    private static final int MSG_HANDLER_UPDATE=100;
    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what){
                case MSG_HANDLER_UPDATE:
                    String result= (String) msg.obj;
                    String op = "";
                    String method;
                    Gson gson = new Gson();
                    PERFORMANCE content;
                    try {
                        content = gson.fromJson(result, PERFORMANCE.class);
                        Log.i(TAG,"processMqttMsg:"+content.toString()+""+content.getResult());
                        op = content.getOP();
                        method = content.getMethod();
                        if (op!=null && op.equalsIgnoreCase("REPORT")) {
                            if (method.equalsIgnoreCase("UPGRADE")) {
                                DeviceUpgradeReport upgrade = gson.fromJson(result, DeviceUpgradeReport.class);
                                if ("finish".equalsIgnoreCase(upgrade.getState())) {
                                    textview_updateing.setText("固件升级完成...");
                                    button_sure.setVisibility(View.VISIBLE);
                                } else if ("start".equalsIgnoreCase(upgrade.getState())) {
                                    textview_updateing.setText("开始升级固件...");
                                } else if ("download".equalsIgnoreCase(upgrade.getState())) {
                                    textview_updateing.setText("开始下载固件...");
                                } else if ("update".equalsIgnoreCase(upgrade.getState())) {
                                    textview_updateing.setText("开始烧写固件...");
                                } else if ("error".equalsIgnoreCase(upgrade.getState())) {
                                    textview_updateing.setText("升级固件出错...");
                                }
                            }
                        }
                        if(content.getResult()!=null && content.getResult().equalsIgnoreCase("ok")){
                            textview_updateing.setText("固件升级完成,等待路由器重启,点击确定返回设备列表界面");
                            button_sure.setVisibility(View.VISIBLE);
                        }
                    } catch (JsonSyntaxException e) {
                        e.printStackTrace();
                    }
                    break;
            }
            return true;
        }
    };
    private Handler mHandler = new WeakRefHandler(mCallback);
    private void initDatas() {
        layout_title.setReturnClickListener(new TitleLayout.ReturnImageClickListener() {
            @Override
            public void onBackPressed() {
                UpdateStatusActivity.this.onBackPressed();
            }
        });
        mRouterManager=RouterManager.getInstance();
        mRouterManager.InitRouterManager(this);
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
            }

            @Override
            public void deviceOpFailure(String op, String deviceKey, Throwable throwable) {
            }

            @Override
            public void notifyHomeGeniusResponse(String result) {
                super.notifyHomeGeniusResponse(result);
                Log.i(TAG,"processMqttMsg:"+result);
               Message msg= Message.obtain();
                msg.what=MSG_HANDLER_UPDATE;
                msg.obj=result;
                mHandler.sendMessage(msg);

            }

            @Override
            public void notifyDeviceDataChanged(String deviceKey, int type) {

            }


            @Override
            public void connectionLost(Throwable throwable) {
                super.connectionLost(throwable);
                Perfence.setPerfence(AppConstant.USER_LOGIN, false);
                new AlertDialog(UpdateStatusActivity.this).builder().setTitle("账号异地登录")
                        .setMsg("当前账号已在其它设备上登录,是否重新登录")
                        .setPositiveButton("确认", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                startActivity(new Intent(UpdateStatusActivity.this, LoginActivity.class));
                            }
                        }).setNegativeButton("取消", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                }).show();
            }
        };
    }
    @Override
    protected void onResume() {
        super.onResume();
        manager.addEventCallback(ec);
    }


    private void initViews() {
        textview_updateing = findViewById(R.id.textview_updateing);
        button_sure = findViewById(R.id.button_sure);
        layout_title= findViewById(R.id.layout_title);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_sure:
                startActivity(new Intent(this, DevicesActivity.class));
                finish();
                break;
        }
    }
}
