package com.deplink.boruSmart.activity.device.smartSwitch;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.deplink.boruSmart.Protocol.json.OpResult;
import com.deplink.boruSmart.activity.personal.login.LoginActivity;
import com.deplink.boruSmart.constant.AppConstant;
import com.deplink.boruSmart.manager.device.smartswitch.SmartSwitchListener;
import com.deplink.boruSmart.manager.device.smartswitch.SmartSwitchManager;
import com.deplink.boruSmart.util.NetUtil;
import com.deplink.boruSmart.util.Perfence;
import com.deplink.boruSmart.util.WeakRefHandler;
import com.deplink.boruSmart.view.toast.ToastSingleShow;
import com.deplink.boruSmart.manager.connect.local.tcp.LocalConnectmanager;
import com.deplink.boruSmart.manager.device.DeviceManager;
import com.deplink.boruSmart.view.combinationwidget.TitleLayout;
import com.deplink.boruSmart.view.dialog.AlertDialog;
import com.deplink.sdk.android.sdk.DeplinkSDK;
import com.deplink.sdk.android.sdk.EventCallback;
import com.deplink.sdk.android.sdk.SDKAction;
import com.deplink.sdk.android.sdk.manager.SDKManager;
import com.google.gson.Gson;

import deplink.com.smartwirelessrelay.homegenius.EllESDK.R;

public class SwitchOneActivity extends Activity implements View.OnClickListener, SmartSwitchListener {
    private static final String TAG = "SwitchTwoActivity";
    private Button button_switch;
    private SmartSwitchManager mSmartSwitchManager;
    private boolean switch_one_open;
    private SDKManager manager;
    private EventCallback ec;
    private boolean isUserLogin;
    private TitleLayout layout_title;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_switch_one);
        initViews();
        initDatas();
        initEvents();
    }
    private boolean isStartFromExperience;
    @Override
    protected void onResume() {
        super.onResume();
        isUserLogin = Perfence.getBooleanPerfence(AppConstant.USER_LOGIN);
        manager.addEventCallback(ec);
        mSmartSwitchManager.addSmartSwitchListener(this);
        isStartFromExperience = DeviceManager.getInstance().isStartFromExperience();
        if (isStartFromExperience) {
            switch_one_open = true;
        } else {
            switch_one_open = mSmartSwitchManager.getCurrentSelectSmartDevice().isSwitch_one_open();
            mSmartSwitchManager.querySwitchStatus("query");
        }
        setSwitchImageviewBackground();

    }

    @Override
    protected void onPause() {
        super.onPause();
        manager.removeEventCallback(ec);
        mSmartSwitchManager.removeSmartSwitchListener(this);
    }

    private void setSwitchImageviewBackground() {
        Log.i(TAG, "switch_one_open=" + switch_one_open);
        if (switch_one_open) {
            button_switch.setBackgroundResource(R.drawable.switchallthewayon);
        } else {
            button_switch.setBackgroundResource(R.drawable.switchallthewayoff);
        }
    }

    private void initEvents() {
        button_switch.setOnClickListener(this);
    }

    private void initDatas() {
        layout_title.setReturnClickListener(new TitleLayout.ReturnImageClickListener() {
            @Override
            public void onBackPressed() {
                SwitchOneActivity.this.onBackPressed();
            }
        });
        layout_title.setEditTextClickListener(new TitleLayout.EditTextClickListener() {
            @Override
            public void onEditTextPressed() {
                Intent intent = new Intent(SwitchOneActivity.this, EditActivity.class);
                intent.putExtra("switchType", "一路开关");
                startActivity(intent);
            }
        });
        mSmartSwitchManager = SmartSwitchManager.getInstance();
        mSmartSwitchManager.InitSmartSwitchManager(this);
        mSmartSwitchManager.addSmartSwitchListener(this);
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
            public void notifyHomeGeniusResponse(String result) {
                super.notifyHomeGeniusResponse(result);
                Log.i(TAG, "设备列表界面收到回调的mqtt消息=" + result);
                Gson gson = new Gson();
                OpResult mOpResult = gson.fromJson(result, OpResult.class);
                if (mOpResult.getOP().equalsIgnoreCase("REPORT") && mOpResult.getMethod().equalsIgnoreCase("SmartWallSwitch")) {
                    String mSwitchStatus = mOpResult.getSwitchStatus();
                    String[] sourceStrArray = mSwitchStatus.split(" ", 4);
                    if (sourceStrArray[0].equals("01")) {
                        switch_one_open = true;
                    } else if (sourceStrArray[0].equals("02")) {
                        switch_one_open = false;
                    }
                    mSmartSwitchManager.getCurrentSelectSmartDevice().setSwitch_one_open(switch_one_open);
                    switch (mOpResult.getCommand()) {
                        case "close1":
                            switch_one_open = false;
                            mSmartSwitchManager.getCurrentSelectSmartDevice().setSwitch_one_open(switch_one_open);
                            break;
                        case "open1":
                            switch_one_open = true;
                            mSmartSwitchManager.getCurrentSelectSmartDevice().setSwitch_one_open(switch_one_open);
                            break;
                    }
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            setSwitchImageviewBackground();
                            mSmartSwitchManager.getCurrentSelectSmartDevice().setStatus("在线");
                            mSmartSwitchManager.getCurrentSelectSmartDevice().saveFast();
                        }
                    });
                }
            }

            @Override
            public void deviceOpSuccess(String op, String deviceKey) {
                super.deviceOpSuccess(op, deviceKey);
            }

            @Override
            public void onFailure(SDKAction action, Throwable throwable) {
            }

            @Override
            public void connectionLost(Throwable throwable) {
                super.connectionLost(throwable);
                isUserLogin = false;
                Perfence.setPerfence(AppConstant.USER_LOGIN, false);
                new AlertDialog(SwitchOneActivity.this).builder().setTitle("账号异地登录")
                        .setMsg("当前账号已在其它设备上登录,是否重新登录")
                        .setPositiveButton("确认", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                startActivity(new Intent(SwitchOneActivity.this, LoginActivity.class));
                            }
                        }).setNegativeButton("取消", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                }).show();
            }
        };
    }

    private void initViews() {
        button_switch = (Button) findViewById(R.id.button_switch);
        layout_title= (TitleLayout) findViewById(R.id.layout_title);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_switch:
                Log.i(TAG, "switch_one_open=" + switch_one_open);

                if (isStartFromExperience) {
                    switch_one_open = !switch_one_open;
                    setSwitchImageviewBackground();
                } else {
                    if(NetUtil.isNetAvailable(this)){
                        if(!isUserLogin && !LocalConnectmanager.getInstance().isLocalconnectAvailable()){
                            ToastSingleShow.showText(this,"本地连接不可用,需要登录后才能操作");
                        }else{
                            if (switch_one_open) {
                                mSmartSwitchManager.setSwitchCommand("close1");
                            } else {
                                mSmartSwitchManager.setSwitchCommand("open1");
                            }
                        }
                    }
                    else{
                        ToastSingleShow.showText(this,"网络连接不正常");
                    }
                }

                break;
        }
    }

    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {

            return true;
        }
    };
    private Handler mHandler = new WeakRefHandler(mCallback);

    @Override
    public void responseResult(String result) {
        Gson gson = new Gson();
        OpResult mOpResult = gson.fromJson(result, OpResult.class);
        String mSwitchStatus = mOpResult.getSwitchStatus();
        String[] sourceStrArray = mSwitchStatus.split(" ", 1);
        Log.i(TAG, "sourceStrArray[0]" + sourceStrArray[0]);
        if (sourceStrArray[0].equals("01")) {
            switch_one_open = true;
        } else if (sourceStrArray[0].equals("02")) {
            switch_one_open = false;
        }
        mSmartSwitchManager.getCurrentSelectSmartDevice().setSwitch_one_open(switch_one_open);
        switch (mOpResult.getCommand()) {
            case "close1":
                ToastSingleShow.showText(SwitchOneActivity.this,"开关已关闭");
                switch_one_open = false;
                mSmartSwitchManager.getCurrentSelectSmartDevice().setSwitch_one_open(switch_one_open);
                break;
            case "open1":
                ToastSingleShow.showText(SwitchOneActivity.this,"开关已开启");
                switch_one_open = true;
                mSmartSwitchManager.getCurrentSelectSmartDevice().setSwitch_one_open(switch_one_open);
                break;
        }
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                setSwitchImageviewBackground();
                mSmartSwitchManager.getCurrentSelectSmartDevice().setStatus("在线");
                mSmartSwitchManager.getCurrentSelectSmartDevice().saveFast();
            }
        });
    }
}
