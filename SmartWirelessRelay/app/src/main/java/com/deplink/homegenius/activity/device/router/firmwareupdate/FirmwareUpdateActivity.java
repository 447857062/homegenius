package com.deplink.homegenius.activity.device.router.firmwareupdate;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.deplink.homegenius.activity.device.remoteControl.topBox.AddTopBoxActivity;
import com.deplink.homegenius.activity.personal.login.LoginActivity;
import com.deplink.homegenius.constant.AppConstant;
import com.deplink.homegenius.manager.connect.remote.HomeGenius;
import com.deplink.homegenius.manager.device.DeviceManager;
import com.deplink.homegenius.manager.device.router.RouterManager;
import com.deplink.homegenius.util.Perfence;
import com.deplink.homegenius.view.combinationwidget.TitleLayout;
import com.deplink.homegenius.view.dialog.AlertDialog;
import com.deplink.homegenius.view.toast.ToastSingleShow;
import com.deplink.sdk.android.sdk.DeplinkSDK;
import com.deplink.sdk.android.sdk.EventCallback;
import com.deplink.sdk.android.sdk.SDKAction;
import com.deplink.sdk.android.sdk.bean.CommonRes;
import com.deplink.sdk.android.sdk.bean.DeviceProperty;
import com.deplink.sdk.android.sdk.bean.DeviceUpgradeInfo;
import com.deplink.sdk.android.sdk.bean.DeviceUpgradeRes;
import com.deplink.sdk.android.sdk.device.router.RouterDevice;
import com.deplink.sdk.android.sdk.json.PERFORMANCE;
import com.deplink.sdk.android.sdk.manager.SDKManager;
import com.deplink.sdk.android.sdk.rest.RestfulTools;
import com.google.gson.Gson;

import deplink.com.smartwirelessrelay.homegenius.EllESDK.R;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FirmwareUpdateActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "FirmwareUpdateActivity";
    private RelativeLayout layout_update_immediately;
    private SDKManager manager;
    private EventCallback ec;
    private CheckBox checkbox_auto_update;
    private boolean canEnterUpdate = false;
    private TextView textview_show_can_update;
    private TextView textview_version_code;
    private HomeGenius mHomeGenius;
    private String channels;
    private TitleLayout layout_title;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firmware_update);
        initViews();
        initDatas();
        initEvents();
    }

    private void initDatas() {
        layout_title.setReturnClickListener(new TitleLayout.ReturnImageClickListener() {
            @Override
            public void onBackPressed() {
                FirmwareUpdateActivity.this.onBackPressed();
            }
        });
        mRouterManager = RouterManager.getInstance();
        mRouterManager.InitRouterManager(this);
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
                Log.i(TAG, "设置固件自动升级失败");
            }

            @Override
            public void deviceOpSuccess(String op, String deviceKey) {
                super.deviceOpSuccess(op, deviceKey);
                switch (op) {
                    case RouterDevice.OP_CHANGE_AUTO_UPGRADE:
                        Log.i(TAG, "设置固件自动升级成功");
                        break;
                    case RouterDevice.OP_LOAD_UPGRADEINFONULL:

                        break;
                    case RouterDevice.OP_LOAD_UPGRADEINFO:


                        break;
                    case RouterDevice.OP_GET_REPORT:

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
                Perfence.setPerfence(AppConstant.USER_LOGIN, false);
                new AlertDialog(FirmwareUpdateActivity.this).builder().setTitle("账号异地登录")
                        .setMsg("当前账号已在其它设备上登录,是否重新登录")
                        .setPositiveButton("确认", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                startActivity(new Intent(FirmwareUpdateActivity.this, LoginActivity.class));
                            }
                        }).setNegativeButton("取消", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                }).show();
            }
        };
    }
    private String currentVersion;
    private void parseDeviceReport(String xmlStr) {
        String op;
        String method;
        Gson gson = new Gson();
        PERFORMANCE content = gson.fromJson(xmlStr, PERFORMANCE.class);
        op = content.getOP();
        method = content.getMethod();
        Log.i(TAG, "op=" + op + "method=" + method + "result=" + content.getResult() + "xmlStr=" + xmlStr);
        if (op == null) {

        } else if (op.equalsIgnoreCase("REPORT")) {
            if (method.equalsIgnoreCase("PERFORMANCE")) {
                PERFORMANCE performance = gson.fromJson(xmlStr, PERFORMANCE.class);
                Log.i(TAG, "performance=" + performance.toString());
                currentVersion= performance.getDevice().getFWVersion();
                textview_version_code.setText("当前版本:" +currentVersion);
            }
        }
    }
    private RouterManager mRouterManager;

    @Override
    protected void onResume() {
        super.onResume();
        manager.addEventCallback(ec);
        mHomeGenius = new HomeGenius();

        //获取当前连接设备的是否自动升级固件，
        //当前选择的设备判断：没有绑定设备就没有，如果已绑定，或者别人添加管理者，就默认选中这个，
        // 之后用户手动选择路由器才切换
        if (!DeviceManager.getInstance().isStartFromExperience()) {
            channels = mRouterManager.getCurrentSelectedRouter().getRouter().getChannels();
            retrieveUpgradeInfo(mRouterManager.getCurrentSelectedRouter().getUid());
            if(channels!=null){
                mHomeGenius.getReport(channels);
            }
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        manager.removeEventCallback(ec);
    }

    private void initEvents() {
        layout_update_immediately.setOnClickListener(this);
        checkbox_auto_update.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                changeAutoUpgrade(isChecked);

            }
        });
    }
    /**
     * 修改自动升级选项
     *
     * @param auto
     */
    public void changeAutoUpgrade(final boolean auto) {
        DeviceProperty item = new DeviceProperty();
        item.setAuto_upgrade(auto);
        RestfulTools.getSingleton().updateDeviceProperty(mRouterManager.getCurrentSelectedRouter().getUid(), item, new Callback<CommonRes>() {
            @Override
            public void onResponse(Call<CommonRes> call, Response<CommonRes> response) {
                switch (response.code()) {
                    case 200:
                        break;
                }
            }

            @Override
            public void onFailure(Call<CommonRes> call, Throwable t) {
            }
        });
    }
    /**
     * 获取升级信息
     */
    public void retrieveUpgradeInfo(String deviceKey) {
        RestfulTools.getSingleton().getDeviceUpgradeInfo(deviceKey, new Callback<DeviceUpgradeRes>() {
            @Override
            public void onResponse(Call<DeviceUpgradeRes> call, Response<DeviceUpgradeRes> response) {
                switch (response.code()) {
                    case 200:
                        Log.i(TAG, "retrieveUpgradeInfo=" + response.body().toString() + "response.message()=" + response.message());
                        if (null != response.body().getUpgrade_info()) {
                            DeviceUpgradeInfo info = (response.body().getUpgrade_info());
                            Log.i(TAG, "已获取版本升级信息"+info.toString());
                            if (!info.getVersion().equalsIgnoreCase(currentVersion)) {
                                textview_show_can_update.setText("立即升级");
                                canEnterUpdate = true;
                            } else {
                                textview_show_can_update.setText("已是最新版本");
                                canEnterUpdate = false;
                            }
                        } else {
                            Log.i(TAG, "版本升级信息为空");
                            textview_show_can_update.setText("已是最新版本");
                            canEnterUpdate = false;
                        }
                        break;
                }
            }

            @Override
            public void onFailure(Call<DeviceUpgradeRes> call, Throwable t) {
                Log.i(TAG, "读取设备升级信息名称失败 " + t.getMessage());

            }
        });
    }
    private void initViews() {
        layout_title= findViewById(R.id.layout_title);
        layout_update_immediately = findViewById(R.id.layout_update_immediately);
        checkbox_auto_update = findViewById(R.id.checkbox_auto_update);
        textview_version_code = findViewById(R.id.textview_version_code);
        textview_show_can_update = findViewById(R.id.textview_show_can_update);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.layout_update_immediately:
                if (canEnterUpdate) {
                    startActivity(new Intent(FirmwareUpdateActivity.this, UpdateImmediatelyActivity.class));
                } else {
                    ToastSingleShow.showText(this, "已是最新版本");
                }

                break;
        }
    }
}
