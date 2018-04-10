package com.deplink.boruSmart.activity.device.router;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Toast;

import com.deplink.boruSmart.activity.personal.login.LoginActivity;
import com.deplink.boruSmart.constant.AppConstant;
import com.deplink.boruSmart.manager.device.DeviceListener;
import com.deplink.boruSmart.manager.device.DeviceManager;
import com.deplink.boruSmart.manager.device.router.RouterManager;
import com.deplink.boruSmart.util.Perfence;
import com.deplink.boruSmart.util.WeakRefHandler;
import com.deplink.boruSmart.view.combinationwidget.TitleLayout;
import com.deplink.boruSmart.view.dialog.AlertDialog;
import com.deplink.boruSmart.view.edittext.ClearEditText;
import com.deplink.boruSmart.view.toast.Ftoast;
import com.deplink.sdk.android.sdk.DeplinkSDK;
import com.deplink.sdk.android.sdk.EventCallback;
import com.deplink.sdk.android.sdk.SDKAction;
import com.deplink.sdk.android.sdk.homegenius.DeviceOperationResponse;
import com.deplink.sdk.android.sdk.manager.SDKManager;

import deplink.com.smartwirelessrelay.homegenius.EllESDK.R;

public class RouterNameUpdateActivity extends Activity {
    private static final String TAG="NameUpdateActivity";
    private ClearEditText edittext_router_name;
    private RouterManager mRouterManager;
    private String deviceName;
    private DeviceManager mDeviceManager;
    private boolean isUserLogin;
    private SDKManager manager;
    private EventCallback ec;
    private DeviceListener mDeviceListener;
    private TitleLayout layout_title;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_router_name_update);
        initViews();
        initDatas();
        initEvents();
    }

    @Override
    protected void onResume() {
        super.onResume();
        manager.addEventCallback(ec);
        mDeviceManager.addDeviceListener(mDeviceListener);
        isUserLogin = Perfence.getBooleanPerfence(AppConstant.USER_LOGIN);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mDeviceManager.removeDeviceListener(mDeviceListener);
        manager.removeEventCallback(ec);
    }

    private void initDatas() {
        layout_title.setReturnClickListener(new TitleLayout.ReturnImageClickListener() {
            @Override
            public void onBackPressed() {
                RouterNameUpdateActivity.this.onBackPressed();
            }
        });
        layout_title.setEditTextClickListener(new TitleLayout.EditTextClickListener() {
            @Override
            public void onEditTextPressed() {
                routerName = edittext_router_name.getText().toString();
                if (!routerName.equals("")) {
                    if (mDeviceManager.isStartFromExperience()) {
                        Intent intentSeleteedRoom = new Intent();
                        intentSeleteedRoom.setClass(RouterNameUpdateActivity.this, RouterSettingActivity.class);
                        intentSeleteedRoom.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intentSeleteedRoom.putExtra("routerName", routerName);
                        intentSeleteedRoom.putExtra("isupdaterouter", true);
                        startActivity(intentSeleteedRoom);
                    } else {
                        if (isUserLogin) {
                            mDeviceManager.alertDeviceHttp(
                                    mRouterManager.getCurrentSelectedRouter().getUid(),
                                    null,
                                    routerName,
                                    null
                            );
                        } else {
                            Ftoast.create(RouterNameUpdateActivity.this).setText( "登录后才能操作").show();
                        }
                    }
                } else {
                    Ftoast.create(RouterNameUpdateActivity.this).setText( "请输入路由器名称").show();
                }
            }
        });
        mRouterManager = RouterManager.getInstance();
        mRouterManager.InitRouterManager();
        mDeviceManager = DeviceManager.getInstance();
        mDeviceManager.InitDeviceManager(this);
        if (DeviceManager.getInstance().isStartFromExperience()) {
            deviceName = "体验路由器";
        } else {
            deviceName = mRouterManager.getCurrentSelectedRouter().getName();
        }
        if(deviceName!=null){
            edittext_router_name.setText(deviceName);
            edittext_router_name.setSelection(deviceName.length());
        }
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
            public void deviceOpSuccess(String op, String deviceKey) {
                super.deviceOpSuccess(op, deviceKey);

            }

            @Override
            public void onFailure(SDKAction action, Throwable throwable) {
            }

            @Override
            public void connectionLost(Throwable throwable) {
                super.connectionLost(throwable);
                Perfence.setPerfence(AppConstant.USER_LOGIN, false);
                isUserLogin = false;
                new AlertDialog(RouterNameUpdateActivity.this).builder().setTitle("账号异地登录")
                        .setMsg("当前账号已在其它设备上登录,是否重新登录")
                        .setPositiveButton("确认", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                startActivity(new Intent(RouterNameUpdateActivity.this, LoginActivity.class));
                            }
                        }).setNegativeButton("取消", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                }).show();
            }
        };
        mDeviceListener=new DeviceListener() {
            @Override
            public void responseAlertDeviceHttpResult(DeviceOperationResponse result) {
                super.responseAlertDeviceHttpResult(result);
                int saveResult = mRouterManager.updateRouterName(routerName);
                if (saveResult > 0) {
                    mRouterManager.getCurrentSelectedRouter().setName(routerName);
                    RouterNameUpdateActivity.this.finish();
                } else {
                    Message msg = Message.obtain();
                    msg.what = MSG_UPDATE_NAME_FAIL;
                    mHandler.sendMessage(msg);
                }
            }
        };
    }

    private void initEvents() {
    }

    private void initViews() {
        edittext_router_name = (ClearEditText) findViewById(R.id.edittext_router_name);
        layout_title= (TitleLayout) findViewById(R.id.layout_title);
    }

    private String routerName;

    private static final int MSG_UPDATE_NAME_FAIL = 100;
    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_UPDATE_NAME_FAIL:
                    Toast.makeText(RouterNameUpdateActivity.this, "更新路由器名称失败", Toast.LENGTH_SHORT).show();
                    break;
            }
            return true;
        }
    };
    private Handler mHandler = new WeakRefHandler(mCallback);
}
