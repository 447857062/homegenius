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
import com.deplink.boruSmart.manager.connect.local.tcp.LocalConnectmanager;
import com.deplink.boruSmart.manager.device.DeviceManager;
import com.deplink.boruSmart.manager.device.smartswitch.SmartSwitchListener;
import com.deplink.boruSmart.manager.device.smartswitch.SmartSwitchManager;
import com.deplink.boruSmart.util.NetUtil;
import com.deplink.boruSmart.util.Perfence;
import com.deplink.boruSmart.util.WeakRefHandler;
import com.deplink.boruSmart.view.combinationwidget.TitleLayout;
import com.deplink.boruSmart.view.dialog.AlertDialog;
import com.deplink.boruSmart.view.toast.Ftoast;
import com.deplink.sdk.android.sdk.DeplinkSDK;
import com.deplink.sdk.android.sdk.EventCallback;
import com.deplink.sdk.android.sdk.SDKAction;
import com.deplink.sdk.android.sdk.manager.SDKManager;
import com.google.gson.Gson;

import deplink.com.smartwirelessrelay.homegenius.EllESDK.R;

public class SwitchFourActivity extends Activity implements View.OnClickListener, SmartSwitchListener {
    private static final String TAG = "SwitchFourActivity";
    private Button button_switch_left;
    private Button button_switch_2;
    private Button button_switch_3;
    private Button button_switch_right;
    private SmartSwitchManager mSmartSwitchManager;
    private Button button_all_switch;
    private DeviceManager mDeviceManager;
    private SDKManager manager;
    private EventCallback ec;
    private boolean isUserLogin;
    private boolean switch_one_open;
    private boolean switch_two_open;
    private boolean switch_three_open;
    private boolean switch_four_open;
    private TitleLayout layout_title;
    private Button button_all_switch_open;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_switch_four);
        initViews();
        initDatas();
        initEvents();
    }
    private void initEvents() {
        button_switch_left.setOnClickListener(this);
        button_switch_2.setOnClickListener(this);
        button_switch_3.setOnClickListener(this);
        button_switch_right.setOnClickListener(this);
        button_all_switch.setOnClickListener(this);
        button_all_switch_open.setOnClickListener(this);
    }
    private boolean isStartFromExperience;
    @Override
    protected void onResume() {
        super.onResume();
        isStartFromExperience = mDeviceManager.isStartFromExperience();
        isUserLogin= Perfence.getBooleanPerfence(AppConstant.USER_LOGIN);
        if(isStartFromExperience){
            switch_one_open = false;
            switch_two_open = false;
            switch_three_open = false;
            switch_four_open = false;
        }else{
            switch_one_open = mSmartSwitchManager.getCurrentSelectSmartDevice().isSwitch_one_open();
            switch_two_open = mSmartSwitchManager.getCurrentSelectSmartDevice().isSwitch_two_open();
            switch_three_open = mSmartSwitchManager.getCurrentSelectSmartDevice().isSwitch_three_open();
            switch_four_open = mSmartSwitchManager.getCurrentSelectSmartDevice().isSwitch_four_open();
        }

        setSwitchImageviewBackground();
        manager.addEventCallback(ec);
        if (!isStartFromExperience) {
            mSmartSwitchManager.querySwitchStatus("query");
            mDeviceManager.readDeviceInfoHttp(mDeviceManager.getCurrentSelectSmartDevice().getUid());
            int usercount = mSmartSwitchManager.getCurrentSelectSmartDevice().getUserCount();
            usercount++;
            mSmartSwitchManager.getCurrentSelectSmartDevice().setUserCount(usercount);
            mSmartSwitchManager.getCurrentSelectSmartDevice().save();
        }
    }
    private void setSwitchImageviewBackground() {
        Log.i(TAG, "switch_one_open=" + switch_one_open);
        Log.i(TAG, "switch_two_open=" + switch_two_open);
        Log.i(TAG, "switch_three_open=" + switch_three_open);
        Log.i(TAG, "switch_four_open=" + switch_four_open);
        if (switch_one_open) {
            button_switch_left.setBackgroundResource(R.drawable.fourwayswitchlefton);
        } else {
            button_switch_left.setBackgroundResource(R.drawable.fourwayswitch_leftoff);
        }
        if (switch_two_open) {
            button_switch_2.setBackgroundResource(R.drawable.fourwayswitchrighton);
        } else {
            button_switch_2.setBackgroundResource(R.drawable.fourwayswitch_rightoff);
        }
        if (switch_three_open) {
            button_switch_3.setBackgroundResource(R.drawable.fourwayswitchleftnext);
        } else {
            button_switch_3.setBackgroundResource(R.drawable.fourwayswitchleft_nextoff);
        }
        if (switch_four_open) {
            button_switch_right.setBackgroundResource(R.drawable.fourwayswitchrightnext);
        } else {
            button_switch_right.setBackgroundResource(R.drawable.fourwayswitch_rightnextoff);
        }
    }
    private void initDatas() {
        layout_title.setReturnClickListener(new TitleLayout.ReturnImageClickListener() {
            @Override
            public void onBackPressed() {
                SwitchFourActivity.this.onBackPressed();
            }
        });
        layout_title.setEditTextClickListener(new TitleLayout.EditTextClickListener() {
            @Override
            public void onEditTextPressed() {
                Intent intent = new Intent(SwitchFourActivity.this, EditSwitchActivity.class);
                intent.putExtra("switchType", "智能四路开关");
                startActivity(intent);
            }
        });
        layout_title.setBackResource(R.color.switch_page_background);
        layout_title.setLineDirverVisiable(false);
        layout_title.setBackImageResource(R.drawable.whitereturn);
        layout_title.setEditTextWhiteColor();
        layout_title.setTitleTextWhiteColor();

        mSmartSwitchManager = SmartSwitchManager.getInstance();
        mSmartSwitchManager.InitSmartSwitchManager(this);
        mSmartSwitchManager.addSmartSwitchListener(this);
        mDeviceManager=DeviceManager.getInstance();
        mDeviceManager.InitDeviceManager(this);
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
                    if(sourceStrArray[3].equals("01")){
                        switch_four_open=true;
                    }else if(sourceStrArray[3].equals("02")){
                        switch_four_open=false;
                    }
                    mSmartSwitchManager.getCurrentSelectSmartDevice().setSwitch_four_open(switch_four_open);
                    switch (mOpResult.getCommand()) {
                        case "close1":
                            Ftoast.create(SwitchFourActivity.this).setText("开关一已关闭").show();
                            switch_one_open = false;
                            mSmartSwitchManager.getCurrentSelectSmartDevice().setSwitch_one_open(switch_one_open);
                            break;
                        case "close2":
                            Ftoast.create(SwitchFourActivity.this).setText("开关二已关闭").show();
                            switch_two_open = false;
                            mSmartSwitchManager.getCurrentSelectSmartDevice().setSwitch_two_open(switch_two_open);
                            break;
                        case "close3":
                            Ftoast.create(SwitchFourActivity.this).setText("开关三已关闭").show();
                            switch_three_open = false;
                            mSmartSwitchManager.getCurrentSelectSmartDevice().setSwitch_three_open(switch_three_open);
                            break;
                        case "close4":
                            Ftoast.create(SwitchFourActivity.this).setText("开关四已关闭").show();
                            switch_four_open = false;
                            mSmartSwitchManager.getCurrentSelectSmartDevice().setSwitch_four_open(switch_four_open);
                            break;
                        case "open1":
                            Ftoast.create(SwitchFourActivity.this).setText("开关一已开启").show();
                            switch_one_open = true;
                            mSmartSwitchManager.getCurrentSelectSmartDevice().setSwitch_one_open(switch_one_open);
                            break;
                        case "open2":
                            Ftoast.create(SwitchFourActivity.this).setText("开关二已开启").show();
                            switch_two_open = true;
                            mSmartSwitchManager.getCurrentSelectSmartDevice().setSwitch_two_open(switch_two_open);
                            break;
                        case "open3":
                            Ftoast.create(SwitchFourActivity.this).setText("开关三已开启").show();
                            switch_three_open = true;
                            mSmartSwitchManager.getCurrentSelectSmartDevice().setSwitch_three_open(switch_three_open);
                            break;
                        case "open4":
                            Ftoast.create(SwitchFourActivity.this).setText("开关四已开启").show();
                            switch_four_open = true;
                            mSmartSwitchManager.getCurrentSelectSmartDevice().setSwitch_four_open(switch_four_open);
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
                new AlertDialog(SwitchFourActivity.this).builder().setTitle("账号异地登录")
                        .setMsg("当前账号已在其它设备上登录,是否重新登录")
                        .setPositiveButton("确认", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                startActivity(new Intent(SwitchFourActivity.this, LoginActivity.class));
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
        button_switch_2 = findViewById(R.id.button_switch_2);
        button_switch_3 = findViewById(R.id.button_switch_3);
        button_switch_right = findViewById(R.id.button_switch_right);
        button_all_switch = findViewById(R.id.button_all_switch);
        layout_title= findViewById(R.id.layout_title);
        button_all_switch_open= findViewById(R.id.button_all_switch_open);
    }
    @Override
    protected void onPause() {
        super.onPause();
        mSmartSwitchManager.removeSmartSwitchListener(this);
        manager.removeEventCallback(ec);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_all_switch:
                if(isStartFromExperience){
                    switch_one_open=false;
                    switch_two_open=false;
                    switch_three_open=false;
                    switch_four_open=false;
                    setSwitchImageviewBackground();

                }else{
                    if(NetUtil.isNetAvailable(this)){
                        if(!isUserLogin && !LocalConnectmanager.getInstance().isLocalconnectAvailable()){
                            Ftoast.create(SwitchFourActivity.this).setText("本地连接不可用,需要登录后才能操作").show();
                        }else{
                            mSmartSwitchManager.setSwitchCommand("close_all");
                        }
                    }else{
                        Ftoast.create(SwitchFourActivity.this).setText("网络连接不正常").show();
                    }
                }

                break;
            case R.id.button_all_switch_open:
                if(isStartFromExperience){
                    switch_one_open=true;
                    switch_two_open=true;
                    switch_three_open=true;
                    switch_four_open=true;
                    setSwitchImageviewBackground();

                }else{
                    if(NetUtil.isNetAvailable(this)){
                        if(!isUserLogin && !LocalConnectmanager.getInstance().isLocalconnectAvailable()){
                            Ftoast.create(SwitchFourActivity.this).setText("本地连接不可用,需要登录后才能操作").show();
                        }else{
                            mSmartSwitchManager.setSwitchCommand("open_all");
                        }
                    }else{
                        Ftoast.create(SwitchFourActivity.this).setText("网络连接不正常").show();
                    }
                }


                break;
            case R.id.button_switch_left:
                if(isStartFromExperience){
                    if(switch_one_open){
                        switch_one_open=false;
                    }else{
                        switch_one_open=true;
                    }

                    setSwitchImageviewBackground();

                }else{
                    if(NetUtil.isNetAvailable(this)){
                        if(!isUserLogin && !LocalConnectmanager.getInstance().isLocalconnectAvailable()){
                            Ftoast.create(SwitchFourActivity.this).setText("本地连接不可用,需要登录后才能操作").show();
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
                        Ftoast.create(SwitchFourActivity.this).setText("网络连接不正常").show();
                    }
                }


                break;
            case R.id.button_switch_2:
                if(isStartFromExperience){
                    if(switch_two_open){
                        switch_two_open=false;
                    }else{
                        switch_two_open=true;
                    }

                    setSwitchImageviewBackground();
                }else{
                    if(NetUtil.isNetAvailable(this)){
                        if(!isUserLogin && !LocalConnectmanager.getInstance().isLocalconnectAvailable()){
                            Ftoast.create(SwitchFourActivity.this).setText("本地连接不可用,需要登录后才能操作").show();
                        }else{
                            if (switch_two_open) {
                                mSmartSwitchManager.setSwitchCommand("close2");
                            } else {
                                mSmartSwitchManager.setSwitchCommand("open2");
                            }
                        }
                    }
                    else{
                        Ftoast.create(SwitchFourActivity.this).setText("网络连接不正常").show();
                    }
                }



                break;
            case R.id.button_switch_3:
                if(isStartFromExperience){
                    if(switch_three_open){
                        switch_three_open=false;
                    }else{
                        switch_three_open=true;
                    }
                    setSwitchImageviewBackground();
                }else{
                    if(NetUtil.isNetAvailable(this)){
                        if(!isUserLogin && !LocalConnectmanager.getInstance().isLocalconnectAvailable()){
                            Ftoast.create(SwitchFourActivity.this).setText("本地连接不可用,需要登录后才能操作").show();
                        }else{
                            if (switch_three_open) {
                                mSmartSwitchManager.setSwitchCommand("close3");
                            } else {
                                mSmartSwitchManager.setSwitchCommand("open3");
                            }
                        }
                    }
                    else{
                        Ftoast.create(SwitchFourActivity.this).setText("网络连接不正常").show();
                    }
                }



                break;
            case R.id.button_switch_right:
                if(isStartFromExperience){
                    if(switch_four_open){
                        switch_four_open=false;
                    }else{
                        switch_four_open=true;
                    }
                    setSwitchImageviewBackground();
                }else{
                    if(NetUtil.isNetAvailable(this)){
                        if(!isUserLogin && !LocalConnectmanager.getInstance().isLocalconnectAvailable()){
                            Ftoast.create(SwitchFourActivity.this).setText("本地连接不可用,需要登录后才能操作").show();
                        }else{
                            if (switch_four_open) {
                                mSmartSwitchManager.setSwitchCommand("close4");
                            } else {
                                mSmartSwitchManager.setSwitchCommand("open4");
                            }
                        }
                    }
                    else{
                        Ftoast.create(SwitchFourActivity.this).setText("网络连接不正常").show();
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
    public void responseResult(final String result) {

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Gson gson = new Gson();
                OpResult mOpResult = gson.fromJson(result, OpResult.class);
                String  mSwitchStatus=mOpResult.getSwitchStatus();
                if(mSwitchStatus!=null){
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
                    if(sourceStrArray[3].equals("01")){
                        switch_four_open=true;
                    }else if(sourceStrArray[3].equals("02")){
                        switch_four_open=false;
                    }
                    mSmartSwitchManager.getCurrentSelectSmartDevice().setSwitch_four_open(switch_four_open);
                    switch (mOpResult.getCommand()) {
                        case "close1":
                            switch_one_open = false;
                            mSmartSwitchManager.getCurrentSelectSmartDevice().setSwitch_one_open(switch_one_open);
                            break;
                        case "close2":
                            switch_two_open = false;
                            mSmartSwitchManager.getCurrentSelectSmartDevice().setSwitch_two_open(switch_two_open);
                            break;
                        case "close3":
                            switch_three_open = false;
                            mSmartSwitchManager.getCurrentSelectSmartDevice().setSwitch_three_open(switch_three_open);
                            break;
                        case "close4":
                            switch_four_open = false;
                            mSmartSwitchManager.getCurrentSelectSmartDevice().setSwitch_four_open(switch_four_open);
                            break;
                        case "open1":
                            switch_one_open = true;
                            mSmartSwitchManager.getCurrentSelectSmartDevice().setSwitch_one_open(switch_one_open);
                            break;
                        case "open2":
                            switch_two_open = true;
                            mSmartSwitchManager.getCurrentSelectSmartDevice().setSwitch_two_open(switch_two_open);
                            break;
                        case "open3":
                            switch_three_open = true;
                            mSmartSwitchManager.getCurrentSelectSmartDevice().setSwitch_three_open(switch_three_open);
                            break;
                        case "open4":
                            switch_four_open = true;
                            mSmartSwitchManager.getCurrentSelectSmartDevice().setSwitch_four_open(switch_four_open);
                            break;
                    }
                    setSwitchImageviewBackground();
                    mSmartSwitchManager.getCurrentSelectSmartDevice().setStatus("在线");
                    mSmartSwitchManager.getCurrentSelectSmartDevice().saveFast();
                }


            }
        });

    }

}
