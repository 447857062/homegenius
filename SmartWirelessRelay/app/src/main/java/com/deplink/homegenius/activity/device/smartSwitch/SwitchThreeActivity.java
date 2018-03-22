package com.deplink.homegenius.activity.device.smartSwitch;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.deplink.homegenius.Protocol.json.OpResult;
import com.deplink.homegenius.activity.personal.login.LoginActivity;
import com.deplink.homegenius.constant.AppConstant;
import com.deplink.homegenius.manager.connect.local.tcp.LocalConnectmanager;
import com.deplink.homegenius.manager.device.smartswitch.SmartSwitchListener;
import com.deplink.homegenius.manager.device.smartswitch.SmartSwitchManager;
import com.deplink.homegenius.util.NetUtil;
import com.deplink.homegenius.util.Perfence;
import com.deplink.homegenius.util.WeakRefHandler;
import com.deplink.homegenius.view.combinationwidget.TitleLayout;
import com.deplink.homegenius.view.dialog.AlertDialog;
import com.deplink.homegenius.view.toast.ToastSingleShow;
import com.deplink.sdk.android.sdk.DeplinkSDK;
import com.deplink.sdk.android.sdk.EventCallback;
import com.deplink.sdk.android.sdk.SDKAction;
import com.deplink.sdk.android.sdk.manager.SDKManager;
import com.google.gson.Gson;

import deplink.com.smartwirelessrelay.homegenius.EllESDK.R;

public class SwitchThreeActivity extends Activity implements View.OnClickListener, SmartSwitchListener {
    private static final String TAG = "SwitchFourActivity";
    private Button button_switch_left;
    private Button button_switch_middle;
    private Button button_switch_right;
    private SmartSwitchManager mSmartSwitchManager;
    private boolean switch_one_open;
    private boolean switch_two_open;
    private boolean switch_three_open;
    private Button button_all_switch;
    private SDKManager manager;
    private EventCallback ec;
    private boolean isUserLogin;
    private TitleLayout layout_title;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_switch_three);
        initViews();
        initDatas();
        initEvents();

    }

    @Override
    protected void onResume() {
        super.onResume();
        isUserLogin=Perfence.getBooleanPerfence(AppConstant.USER_LOGIN);
        switch_one_open = mSmartSwitchManager.getCurrentSelectSmartDevice().isSwitch_one_open();
        switch_two_open = mSmartSwitchManager.getCurrentSelectSmartDevice().isSwitch_two_open();
        switch_three_open = mSmartSwitchManager.getCurrentSelectSmartDevice().isSwitch_three_open();
        setSwitchImageviewBackground();
        mSmartSwitchManager.addSmartSwitchListener(this);
        manager.addEventCallback(ec);
        mSmartSwitchManager.querySwitchStatus("query");
    }

    @Override
    protected void onPause() {
        super.onPause();
        manager.removeEventCallback(ec);
        mSmartSwitchManager.removeSmartSwitchListener(this);
    }

    private void setSwitchImageviewBackground() {
        Log.i(TAG, "switch_one_open=" + switch_one_open);
        Log.i(TAG, "switch_two_open=" + switch_two_open);
        Log.i(TAG, "switch_three_open=" + switch_three_open);
        if (switch_one_open) {
            button_switch_left.setBackgroundResource(R.drawable.threewayswitchlefton);
        } else {
            button_switch_left.setBackgroundResource(R.color.transparent);
        }
        if (switch_two_open) {
            button_switch_middle.setBackgroundResource(R.drawable.threewayswitchcenteron);
        } else {
            button_switch_middle.setBackgroundResource(R.color.transparent);
        }
        if (switch_three_open) {
            button_switch_right.setBackgroundResource(R.drawable.threewayswitchrighton);
        } else {
            button_switch_right.setBackgroundResource(R.color.transparent);
        }

        if(switch_one_open&&switch_two_open&&switch_three_open){
            button_all_switch.setBackgroundResource(R.drawable.noallswitch);
        }else{
            button_all_switch.setBackgroundResource(R.drawable.allswitch);
        }
    }

    private void initEvents() {
        button_switch_left.setOnClickListener(this);
        button_switch_middle.setOnClickListener(this);
        button_switch_right.setOnClickListener(this);
    }

    private void initDatas() {
        layout_title.setReturnClickListener(new TitleLayout.ReturnImageClickListener() {
            @Override
            public void onBackPressed() {
                SwitchThreeActivity.this.onBackPressed();
            }
        });
        layout_title.setEditTextClickListener(new TitleLayout.EditTextClickListener() {
            @Override
            public void onEditTextPressed() {
                Intent intent = new Intent(SwitchThreeActivity.this, EditActivity.class);
                intent.putExtra("switchType", "三路开关");
                startActivity(intent);
            }
        });
        mSmartSwitchManager = SmartSwitchManager.getInstance();
        mSmartSwitchManager.InitSmartSwitchManager(this);
        mSmartSwitchManager.addSmartSwitchListener(this);
        button_all_switch.setOnClickListener(this);
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
                if (mOpResult.getOP().equalsIgnoreCase("REPORT")&& mOpResult.getMethod().equalsIgnoreCase("SmartWallSwitch")) {
                    String  mSwitchStatus=mOpResult.getSwitchStatus();
                    String[] sourceStrArray = mSwitchStatus.split(" ",4);
                    if(sourceStrArray[0].equals("01")){
                        switch_one_open=true;
                    }else if(sourceStrArray[0].equals("02")){
                        switch_one_open=false;
                    }
                    mSmartSwitchManager.getCurrentSelectSmartDevice().setSwitch_one_open(switch_one_open);
                    if(sourceStrArray[1].equals("01")){
                        switch_two_open=true;
                    }else if(sourceStrArray[1].equals("02")){
                        switch_two_open=false;
                    }
                    mSmartSwitchManager.getCurrentSelectSmartDevice().setSwitch_two_open(switch_two_open);
                    if(sourceStrArray[2].equals("01")){
                        switch_three_open=true;
                    }else if(sourceStrArray[2].equals("02")){
                        switch_three_open=false;
                    }
                    mSmartSwitchManager.getCurrentSelectSmartDevice().setSwitch_three_open(switch_three_open);

                    switch (mOpResult.getCommand()) {
                        case "close1":
                            ToastSingleShow.showText(SwitchThreeActivity.this,"开关一已关闭");
                            switch_one_open = false;
                            mSmartSwitchManager.getCurrentSelectSmartDevice().setSwitch_one_open(switch_one_open);
                            break;
                        case "close2":
                            ToastSingleShow.showText(SwitchThreeActivity.this,"开关二已关闭");
                            switch_two_open = false;
                            mSmartSwitchManager.getCurrentSelectSmartDevice().setSwitch_two_open(switch_two_open);
                            break;
                        case "close3":
                            ToastSingleShow.showText(SwitchThreeActivity.this,"开关三已关闭");
                            switch_three_open = false;
                            mSmartSwitchManager.getCurrentSelectSmartDevice().setSwitch_three_open(switch_three_open);
                            break;

                        case "open1":
                            ToastSingleShow.showText(SwitchThreeActivity.this,"开关一已开启");
                            switch_one_open = true;
                            mSmartSwitchManager.getCurrentSelectSmartDevice().setSwitch_one_open(switch_one_open);
                            break;
                        case "open2":
                            ToastSingleShow.showText(SwitchThreeActivity.this,"开关二已开启");
                            switch_two_open = true;
                            mSmartSwitchManager.getCurrentSelectSmartDevice().setSwitch_two_open(switch_two_open);
                            break;
                        case "open3":
                            ToastSingleShow.showText(SwitchThreeActivity.this,"开关三已开启");
                            switch_three_open = true;
                            mSmartSwitchManager.getCurrentSelectSmartDevice().setSwitch_three_open(switch_three_open);
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
                new AlertDialog(SwitchThreeActivity.this).builder().setTitle("账号异地登录")
                        .setMsg("当前账号已在其它设备上登录,是否重新登录")
                        .setPositiveButton("确认", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                startActivity(new Intent(SwitchThreeActivity.this, LoginActivity.class));
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
        button_switch_left = findViewById(R.id.button_switch_left);
        button_switch_middle = findViewById(R.id.button_switch_middle);
        button_switch_right = findViewById(R.id.button_switch_right);
        button_all_switch = findViewById(R.id.button_all_switch);
        layout_title= findViewById(R.id.layout_title);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_all_switch:
                if(NetUtil.isNetAvailable(this)){
                    if(!isUserLogin && !LocalConnectmanager.getInstance().isLocalconnectAvailable()){
                        ToastSingleShow.showText(this,"本地连接不可用,需要登录后才能操作");
                    }else{
                        if (switch_one_open||switch_two_open||switch_three_open) {
                            mSmartSwitchManager.setSwitchCommand("close_all");
                        } else {
                            mSmartSwitchManager.setSwitchCommand("open_all");
                        }
                    }
                }
                else{
                    ToastSingleShow.showText(this,"网络连接不正常");
                }

                break;
            case R.id.button_switch_left:
                if(NetUtil.isNetAvailable(this)){
                    if(!isUserLogin && !LocalConnectmanager.getInstance().isLocalconnectAvailable()){
                        ToastSingleShow.showText(this,"本地连接不可用,需要登录后才能操作");
                    }else{
                        Log.i(TAG, "switch_one_open=" + switch_one_open);
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

                break;
            case R.id.button_switch_middle:
                if(NetUtil.isNetAvailable(this)){
                    if(!isUserLogin && !LocalConnectmanager.getInstance().isLocalconnectAvailable()){
                        ToastSingleShow.showText(this,"本地连接不可用,需要登录后才能操作");
                    }else{
                        if (switch_two_open) {
                            mSmartSwitchManager.setSwitchCommand("close2");
                        } else {
                            mSmartSwitchManager.setSwitchCommand("open2");
                        }
                    }
                }
                else{
                    ToastSingleShow.showText(this,"网络连接不正常");
                }


                break;
            case R.id.button_switch_right:
                if(NetUtil.isNetAvailable(this)){
                    if(!isUserLogin && !LocalConnectmanager.getInstance().isLocalconnectAvailable()){
                        ToastSingleShow.showText(this,"本地连接不可用,需要登录后才能操作");
                    }else{
                        if (switch_three_open) {
                            mSmartSwitchManager.setSwitchCommand("close3");
                        } else {
                            mSmartSwitchManager.setSwitchCommand("open3");
                        }
                    }
                }
                else{
                    ToastSingleShow.showText(this,"网络连接不正常");
                }


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
        String  mSwitchStatus=mOpResult.getSwitchStatus();
        String[] sourceStrArray = mSwitchStatus.split(" ",3);
        if(sourceStrArray[0].equals("01")){
            switch_one_open=true;
        }else if(sourceStrArray[0].equals("02")){
            switch_one_open=false;
        }
        mSmartSwitchManager.getCurrentSelectSmartDevice().setSwitch_one_open(switch_one_open);
        if(sourceStrArray[1].equals("01")){
            switch_two_open=true;
        }else if(sourceStrArray[1].equals("02")){
            switch_two_open=false;
        }
        mSmartSwitchManager.getCurrentSelectSmartDevice().setSwitch_two_open(switch_two_open);
        if(sourceStrArray[2].equals("01")){
            switch_three_open=true;
        }else if(sourceStrArray[2].equals("02")){
            switch_three_open=false;
        }
        mSmartSwitchManager.getCurrentSelectSmartDevice().setSwitch_three_open(switch_three_open);
        switch (mOpResult.getCommand()) {
            case "close1":
                ToastSingleShow.showText(SwitchThreeActivity.this,"开关一已关闭");
                switch_one_open = false;
                mSmartSwitchManager.getCurrentSelectSmartDevice().setSwitch_one_open(switch_one_open);
                break;
            case "close2":
                ToastSingleShow.showText(SwitchThreeActivity.this,"开关二已关闭");
                switch_two_open = false;
                mSmartSwitchManager.getCurrentSelectSmartDevice().setSwitch_two_open(switch_two_open);
                break;
            case "close3":
                ToastSingleShow.showText(SwitchThreeActivity.this,"开关三已关闭");
                switch_three_open = false;
                mSmartSwitchManager.getCurrentSelectSmartDevice().setSwitch_three_open(switch_three_open);
                break;

            case "open1":
                ToastSingleShow.showText(SwitchThreeActivity.this,"开关一已开启");
                switch_one_open = true;
                mSmartSwitchManager.getCurrentSelectSmartDevice().setSwitch_one_open(switch_one_open);
                break;
            case "open2":
                ToastSingleShow.showText(SwitchThreeActivity.this,"开关二已开启");
                switch_two_open = true;
                mSmartSwitchManager.getCurrentSelectSmartDevice().setSwitch_two_open(switch_two_open);
                break;
            case "open3":
                ToastSingleShow.showText(SwitchThreeActivity.this,"开关三已开启");
                switch_three_open = true;
                mSmartSwitchManager.getCurrentSelectSmartDevice().setSwitch_three_open(switch_three_open);
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
