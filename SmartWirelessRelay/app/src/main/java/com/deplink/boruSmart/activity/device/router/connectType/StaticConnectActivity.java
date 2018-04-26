package com.deplink.boruSmart.activity.device.router.connectType;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.deplink.boruSmart.activity.device.router.wifi.WifiSetting24;
import com.deplink.boruSmart.activity.personal.login.LoginActivity;
import com.deplink.boruSmart.constant.AppConstant;
import com.deplink.boruSmart.manager.connect.remote.HomeGenius;
import com.deplink.boruSmart.manager.device.DeviceManager;
import com.deplink.boruSmart.manager.device.router.RouterManager;
import com.deplink.boruSmart.util.NetUtil;
import com.deplink.boruSmart.util.Perfence;
import com.deplink.boruSmart.util.StringValidatorUtil;
import com.deplink.boruSmart.view.combinationwidget.TitleLayout;
import com.deplink.boruSmart.view.dialog.InputAlertDialog;
import com.deplink.boruSmart.view.toast.Ftoast;
import com.deplink.sdk.android.sdk.DeplinkSDK;
import com.deplink.sdk.android.sdk.EventCallback;
import com.deplink.sdk.android.sdk.SDKAction;
import com.deplink.sdk.android.sdk.device.router.RouterDevice;
import com.deplink.sdk.android.sdk.json.PERFORMANCE;
import com.deplink.sdk.android.sdk.json.Proto;
import com.deplink.sdk.android.sdk.json.STATIC;
import com.deplink.sdk.android.sdk.manager.SDKManager;
import com.deplink.sdk.android.sdk.rest.ErrorResponse;
import com.deplink.sdk.android.sdk.rest.RestfulToolsRouter;
import com.deplink.sdk.android.sdk.rest.RouterResponse;
import com.google.gson.Gson;

import java.io.IOException;

import deplink.com.smartwirelessrelay.homegenius.EllESDK.R;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StaticConnectActivity extends Activity {
    private static final String TAG = "StaticConnectActivity";
    private EditText ip_address;
    private EditText edittext_submask;
    private EditText edittext_getway;
    private EditText edittext_dns1;
    private EditText edittext_dns2;
    private String op_type;
    private EditText edittext_mtu;
    private SDKManager manager;
    private EventCallback ec;
    private boolean isSetStaticConnect;
    //本地设置需要的参数
    private String ipaddress;
    private String submask;
    private String getway;
    private String dns1;
    private String mtu;
    //本地设置需要的参数 end
    private String channels;
    private HomeGenius mHomeGenius;
    private TitleLayout layout_title;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_static_connect);
        initViews();
        initDatas();
        initEvents();
    }
    private void initDatas() {
        layout_title.setReturnClickListener(new TitleLayout.ReturnImageClickListener() {
            @Override
            public void onBackPressed() {
                StaticConnectActivity.this.onBackPressed();
            }
        });
        layout_title.setEditTextClickListener(new TitleLayout.EditTextClickListener() {
            @Override
            public void onEditTextPressed() {
                ipaddress = ip_address.getText().toString().trim();
                submask = edittext_submask.getText().toString().trim();
                getway = edittext_getway.getText().toString().trim();
                dns1 = edittext_dns1.getText().toString().trim();
                mtu = edittext_mtu.getText().toString().trim();
                if (mtu.equals("1480(默认)")) {
                    mtu = "1480";
                }
                String dns2 = edittext_dns2.getText().toString().trim();
                if (!StringValidatorUtil.isIPString(ipaddress)) {
                    Ftoast.create(StaticConnectActivity.this).setText("输入的ip地址格式不正确").show();
                    return;
                }
                if (!StringValidatorUtil.isIPString(submask)) {
                    Ftoast.create(StaticConnectActivity.this).setText("输入的子网掩码格式不正确").show();
                    return;
                }
                if (!StringValidatorUtil.isIPString(getway)) {
                    Ftoast.create(StaticConnectActivity.this).setText("输入的默认网关地址格式不正确").show();
                    return;
                }
                Log.i(TAG, "ipaddress=" + ipaddress + "submask=" + submask + "getway=" + getway + "dns1=" + dns1);
                if (op_type != null && op_type.equals(AppConstant.OPERATION_TYPE_LOCAL)) {
                    if (NetUtil.isNetAvailable(StaticConnectActivity.this)) {
                        setStaticConnectLocal(ipaddress, submask, getway, dns1);
                    } else {
                        Ftoast.create(StaticConnectActivity.this).setText("请确保连接上想配置路由器的wifi").show();
                    }
                } else {
                    //MQTT接口
                    if (NetUtil.isNetAvailable(StaticConnectActivity.this)) {
                        isSetStaticConnect = true;
                        Proto proto = new Proto();
                        STATIC static_ = new STATIC();
                        static_.setIPADDR(ipaddress);
                        static_.setNETMASK(submask);
                        static_.setGATEWAY(getway);
                        static_.setMTU(mtu);
                        if (!StringValidatorUtil.isIPString(dns1)) {
                            if (!dns1.equals("")) {
                                Ftoast.create(StaticConnectActivity.this).setText("DNS1 不是有效的Ip地址,已忽略DNS1").show();
                            }
                            //return;
                        } else if (!StringValidatorUtil.isIPString(dns2)) {
                            if (!dns2.equals("")) {
                                Ftoast.create(StaticConnectActivity.this).setText("DNS2 不是有效的Ip地址,已忽略DNS2").show();
                            }
                        } else {
                            if (!dns1.equals("")) {
                                static_.setDNS(dns1);
                            } else if (!dns2.equals("")) {
                                static_.setDNS(dns2);
                            }
                        }
                        proto.setSTATIC(static_);
                        boolean isUserLogin;
                        isUserLogin= Perfence.getBooleanPerfence(AppConstant.USER_LOGIN);
                        if(isUserLogin){
                            mHomeGenius.setWan(proto,channels);
                        }else{
                            Ftoast.create(StaticConnectActivity.this).setText("未登录，无法设置静态上网,请登录后重试").show();
                        }
                    } else {
                        Ftoast.create(StaticConnectActivity.this).setText("网络连接已断开").show();
                    }

                }
            }
        });
        DeplinkSDK.initSDK(getApplicationContext(), Perfence.SDK_APP_KEY);
        mRouterManager = RouterManager.getInstance();
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
                parseDeviceReport(result);
            }
            @Override
            public void deviceOpSuccess(String op, String deviceKey) {
                super.deviceOpSuccess(op, deviceKey);
                switch (op) {
                    case RouterDevice.OP_GET_WAN:
                        Ftoast.create(StaticConnectActivity.this).setText("动态IP设置成功").show();

                    case RouterDevice.OP_SUCCESS:

                        break;
                }
            }

            @Override
            public void onFailure(SDKAction action, Throwable throwable) {

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
                if (content.getResult().equalsIgnoreCase("OK")) {
                    Log.i(TAG," mSDKCoordinator.notifyDeviceOpSuccess");
                    if (isSetStaticConnect) {
                        Ftoast.create(StaticConnectActivity.this).setText("设置成功").show();
                    }
                }
            }
        }else if (op.equalsIgnoreCase("WAN")) {
            if (method.equalsIgnoreCase("REPORT")) {
                PERFORMANCE wan = gson.fromJson(xmlStr, PERFORMANCE.class);
                Ftoast.create(StaticConnectActivity.this).setText("静态IP设置成功").show();
            }
        }
    }
    private boolean isUserLogin;
    private boolean isStartFromExperience;
    private RouterManager mRouterManager;
    @Override
    protected void onResume() {
        super.onResume();
        isStartFromExperience = DeviceManager.getInstance().isStartFromExperience();
        isUserLogin = Perfence.getBooleanPerfence(AppConstant.USER_LOGIN);
        manager.addEventCallback(ec);
        mHomeGenius = new HomeGenius();
        if(!isStartFromExperience){
            channels = mRouterManager.getCurrentSelectedRouter().getRouter().getChannels();
        }
    }



    @Override
    protected void onPause() {
        super.onPause();
        manager.removeEventCallback(ec);
    }

    private void initEvents() {
    }

    private void initViews() {
        layout_title= (TitleLayout) findViewById(R.id.layout_title);
        op_type = getIntent().getStringExtra(AppConstant.OPERATION_TYPE);
        ip_address = (EditText) findViewById(R.id.edittext_ip_address);
        edittext_submask = (EditText) findViewById(R.id.edittext_submask);
        edittext_getway = (EditText) findViewById(R.id.edittext_getway);
        RelativeLayout layout_dns2 = (RelativeLayout) findViewById(R.id.layout_dns2);
        edittext_mtu = (EditText) findViewById(R.id.edittext_mtu);
        edittext_dns1 = (EditText) findViewById(R.id.edittext_dns1);
        edittext_dns2 = (EditText) findViewById(R.id.edittext_dns2);
        if (op_type != null && op_type.equals(AppConstant.OPERATION_TYPE_LOCAL)) {
            layout_dns2.setVisibility(View.GONE);
        } else {
            layout_dns2.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 检查使用本地接口的本地路由器连接情况
     */
    private boolean isConnectLocalRouter = false;
    private Runnable connectStatus = new Runnable() {
        @Override
        public void run() {
            if (!isConnectLocalRouter) {
                new InputAlertDialog(StaticConnectActivity.this).builder()
                        .setTitle("连接路由器WIFI")
                        .setMsg("确保连接上路由器提供的wifi,现在跳转到设置wifi界面？")
                        .setPositiveButton("确认", new InputAlertDialog.onSureBtnClickListener() {
                            @Override
                            public void onSureBtnClicked(String password) {
                                startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                            }
                        }).setNegativeButton("取消", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                }).show();
            }
        }
    };




    private void setStaticConnectLocal(String ipaddress, String submask, String getway, String dns1) {
       RestfulToolsRouter.getSingleton(StaticConnectActivity.this).staticIp(ipaddress, submask, getway, dns1, new Callback<RouterResponse>() {
            @Override
            public void onResponse(Call<RouterResponse> call, Response<RouterResponse> response) {
                int code = response.code();
                Log.i(TAG, "getSingleton code=" + code);
                RouterResponse result = response.body();
                Log.i(TAG, "getSingleton RouterResponse=" + result.toString());
                if (code != 200) {
                    String errorMsg = "";
                    try {
                        String text = response.errorBody().string();
                        Gson gson = new Gson();
                        ErrorResponse errorResponse = gson.fromJson(text, ErrorResponse.class);
                        switch (errorResponse.getErrcode()) {
                            case AppConstant.ERROR_CODE.OP_ERRCODE_BAD_TOKEN:
                                text = AppConstant.ERROR_MSG.OP_ERRCODE_BAD_TOKEN;
                                Ftoast.create(StaticConnectActivity.this).setText( "登录失效 :" + text).show();
                                startActivity(new Intent(StaticConnectActivity.this, LoginActivity.class));
                                return;
                            case AppConstant.ERROR_CODE.OP_ERRCODE_BAD_ACCOUNT:
                                errorMsg = AppConstant.ERROR_MSG.OP_ERRCODE_BAD_ACCOUNT;
                                break;
                            case AppConstant.ERROR_CODE.OP_ERRCODE_LOGIN_FAIL:
                                errorMsg = AppConstant.ERROR_MSG.OP_ERRCODE_LOGIN_FAIL;

                                break;
                            case AppConstant.ERROR_CODE.OP_ERRCODE_NOT_FOUND:
                                errorMsg = AppConstant.ERROR_MSG.OP_ERRCODE_NOT_FOUND;

                                break;
                            case AppConstant.ERROR_CODE.OP_ERRCODE_LOGIN_FAIL_MAX:
                                errorMsg = AppConstant.ERROR_MSG.OP_ERRCODE_LOGIN_FAIL_MAX;

                                break;
                            case AppConstant.ERROR_CODE.OP_ERRCODE_CAPTCHA_INCORRECT:
                                errorMsg = AppConstant.ERROR_MSG.OP_ERRCODE_CAPTCHA_INCORRECT;

                                break;
                            case AppConstant.ERROR_CODE.OP_ERRCODE_PASSWORD_INCORRECT:
                                errorMsg = AppConstant.ERROR_MSG.OP_ERRCODE_PASSWORD_INCORRECT;

                                break;
                            case AppConstant.ERROR_CODE.OP_ERRCODE_PASSWORD_SHORT:
                                errorMsg = AppConstant.ERROR_MSG.OP_ERRCODE_PASSWORD_SHORT;

                                break;
                            case AppConstant.ERROR_CODE.OP_ERRCODE_BAD_ACCOUNT_INFO:
                                errorMsg = AppConstant.ERROR_MSG.OP_ERRCODE_BAD_ACCOUNT_INFO;

                                break;
                            case AppConstant.ERROR_CODE.OP_ERRCODE_DB_TRANSACTION_ERROR:
                                errorMsg = AppConstant.ERROR_MSG.OP_ERRCODE_DB_TRANSACTION_ERROR;

                                break;
                            default:
                                errorMsg = errorResponse.getMsg();
                                break;

                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Ftoast.create(StaticConnectActivity.this).setText(errorMsg).show();
                } else {
                    Ftoast.create(StaticConnectActivity.this).setText("静态IP设置成功，请设置wifi名字密码").show();
                    Intent intentWifiSetting = new Intent(StaticConnectActivity.this, WifiSetting24.class);
                    intentWifiSetting.putExtra(AppConstant.OPERATION_TYPE, AppConstant.OPERATION_TYPE_LOCAL);
                    startActivity(intentWifiSetting);
                }

            }

            @Override
            public void onFailure(Call<RouterResponse> call, Throwable t) {

            }
        });
    }
}
