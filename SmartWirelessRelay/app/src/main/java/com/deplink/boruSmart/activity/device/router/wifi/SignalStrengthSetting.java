package com.deplink.boruSmart.activity.device.router.wifi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.deplink.boruSmart.activity.personal.login.LoginActivity;
import com.deplink.boruSmart.constant.AppConstant;
import com.deplink.boruSmart.manager.device.DeviceManager;
import com.deplink.boruSmart.util.NetUtil;
import com.deplink.boruSmart.util.Perfence;
import com.deplink.boruSmart.view.combinationwidget.TitleLayout;
import com.deplink.boruSmart.view.dialog.AlertDialog;
import com.deplink.boruSmart.view.toast.ToastSingleShow;
import com.deplink.sdk.android.sdk.DeplinkSDK;
import com.deplink.sdk.android.sdk.EventCallback;
import com.deplink.sdk.android.sdk.SDKAction;
import com.deplink.sdk.android.sdk.device.router.RouterDevice;
import com.deplink.sdk.android.sdk.json.Wifi;
import com.deplink.sdk.android.sdk.json.Wifi_2G;
import com.deplink.sdk.android.sdk.manager.SDKManager;

import deplink.com.smartwirelessrelay.homegenius.EllESDK.R;

public class SignalStrengthSetting extends Activity implements View.OnClickListener{
    private static final String TAG="SignalStrengthSetting";
    /**
     * 孕妇模式的布局按钮
     */
    private RelativeLayout layout_model_pregnant;
    /**
     * 穿墙模式的布局按钮
     */
    private RelativeLayout layout_model_walls;
    /**
     * 平衡模式的布局按钮
     */
    private RelativeLayout layout_model_balance;
    /**
     * 当点击相对布局的时候，设置图片的iamgelevle，做成checkbox的样式，孕妇模式
     */
    private ImageView imageview_model_pregnant;
    /**
     * 当点击相对布局的时候，设置图片的iamgelevle，做成checkbox的样式，穿墙模式
     */
    private ImageView imageview_model_walls;
    /**
     * 当点击相对布局的时候，设置图片的iamgelevle，做成checkbox的样式，平衡模式
     */
    private ImageView imageview_model_balance;
    private String currentWifiMode;
    private SDKManager manager;
    private EventCallback ec;
    private RouterDevice routerDevice;
    private TitleLayout layout_title;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signal_strength_setting);
        initViews();
        initDatas();
        initEvents();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if( !DeviceManager.getInstance().isStartFromExperience()){
            getRouterDevice();
            manager.addEventCallback(ec);
            if (NetUtil.isNetAvailable(this)) {
                try {
                    routerDevice.queryWifi();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                ToastSingleShow.showText(this, "网络连接已断开");
            }
        }
    }


    private void getRouterDevice() {
        String currentDevcieKey = Perfence.getPerfence(AppConstant.DEVICE.CURRENT_DEVICE_KEY);
        if (currentDevcieKey.equals("")) {
            if(manager.getDeviceList()!=null && manager.getDeviceList().size()!=0){
                Perfence.setPerfence(AppConstant.DEVICE.CURRENT_DEVICE_KEY, manager.getDeviceList().get(0).getDeviceKey());
            }else{
                ToastSingleShow.showText(this,"还没有绑定设备");
            }
        }
        routerDevice = (RouterDevice) manager.getDevice(Perfence.getPerfence(AppConstant.DEVICE.CURRENT_DEVICE_KEY));
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
                SignalStrengthSetting.this.onBackPressed();
            }
        });
        layout_title.setEditTextClickListener(new TitleLayout.EditTextClickListener() {
            @Override
            public void onEditTextPressed() {
                if (NetUtil.isNetAvailable(SignalStrengthSetting.this)) {
                    isSetSignalStreng=true;
                    Wifi wifi = new Wifi();
                    Wifi_2G wifi_2g = new Wifi_2G();
                    wifi_2g.setPOWER(currentWifiMode);
                    wifi.setWifi_2G(wifi_2g);
                    try {
                        routerDevice.setWifi(wifi);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    ToastSingleShow.showText(SignalStrengthSetting.this, "网络连接已断开");
                }
            }
        });
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
                Log.i(TAG,"op="+op);
                switch (op) {
                    case RouterDevice.OP_GET_WIFI:
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                currentWifiMode = routerDevice.getWifi().getWifi_2G().getPOWER();
                                if (currentWifiMode.equals("")) {
                                    currentWifiMode = "--";
                                }
                                switch (currentWifiMode) {
                                    case "0":
                                        layout_model_pregnant.callOnClick();
                                        break;
                                    case "1":
                                        layout_model_balance.callOnClick();
                                        break;
                                    case "2":
                                        layout_model_walls.callOnClick();

                                        break;
                                }

                            }
                        });
                        break;
                    case RouterDevice.OP_SUCCESS:
                        if (isSetSignalStreng) {
                            ToastSingleShow.showText(SignalStrengthSetting.this, "设置成功");
                        }
                        break;
                }
            }

            @Override
            public void connectionLost(Throwable throwable) {
                super.connectionLost(throwable);
                Perfence.setPerfence(AppConstant.USER_LOGIN, false);
                new AlertDialog(SignalStrengthSetting.this).builder().setTitle("账号异地登录")
                        .setMsg("当前账号已在其它设备上登录,是否重新登录")
                        .setPositiveButton("确认", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                startActivity(new Intent(SignalStrengthSetting.this, LoginActivity.class));
                            }
                        }).setNegativeButton("取消", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                }).show();
            }
        };
    }

    private void initEvents() {
        layout_model_pregnant.setOnClickListener(this);
        layout_model_walls.setOnClickListener(this);
        layout_model_balance.setOnClickListener(this);
    }
    private boolean  isSetSignalStreng;
    private void initViews() {
        layout_model_pregnant = (RelativeLayout) findViewById(R.id.layout_model_pregnant);
        layout_model_walls = (RelativeLayout) findViewById(R.id.layout_model_walls);
        layout_model_balance = (RelativeLayout) findViewById(R.id.layout_model_balance);
        imageview_model_pregnant = (ImageView) findViewById(R.id.imageview_model_pregnant);
        imageview_model_walls = (ImageView) findViewById(R.id.imageview_model_walls);
        imageview_model_balance = (ImageView) findViewById(R.id.imageview_model_balance);
        layout_title= (TitleLayout) findViewById(R.id.layout_title);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.layout_model_pregnant:
                imageview_model_pregnant.setImageLevel(1);
                imageview_model_walls.setImageLevel(0);
                imageview_model_balance.setImageLevel(0);
                currentWifiMode = "0";
                break;
            case R.id.layout_model_walls:
                imageview_model_pregnant.setImageLevel(0);
                imageview_model_walls.setImageLevel(1);
                imageview_model_balance.setImageLevel(0);
                currentWifiMode = "2";
                break;
            case R.id.layout_model_balance:
                imageview_model_pregnant.setImageLevel(0);
                imageview_model_walls.setImageLevel(0);
                imageview_model_balance.setImageLevel(1);
                currentWifiMode = "1";
                break;

        }
    }
}
