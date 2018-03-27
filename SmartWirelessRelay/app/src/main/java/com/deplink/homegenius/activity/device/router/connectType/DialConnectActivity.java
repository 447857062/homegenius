package com.deplink.homegenius.activity.device.router.connectType;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.deplink.homegenius.activity.device.router.wifi.WifiSetting24;
import com.deplink.homegenius.activity.personal.login.LoginActivity;
import com.deplink.homegenius.constant.AppConstant;
import com.deplink.homegenius.manager.connect.remote.HomeGenius;
import com.deplink.homegenius.manager.device.DeviceManager;
import com.deplink.homegenius.manager.device.router.RouterManager;
import com.deplink.homegenius.util.NetUtil;
import com.deplink.homegenius.util.Perfence;
import com.deplink.homegenius.view.combinationwidget.TitleLayout;
import com.deplink.homegenius.view.dialog.AlertDialog;
import com.deplink.homegenius.view.toast.ToastSingleShow;
import com.deplink.sdk.android.sdk.DeplinkSDK;
import com.deplink.sdk.android.sdk.EventCallback;
import com.deplink.sdk.android.sdk.SDKAction;
import com.deplink.sdk.android.sdk.json.PERFORMANCE;
import com.deplink.sdk.android.sdk.json.PPPOE;
import com.deplink.sdk.android.sdk.json.Proto;
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

public class DialConnectActivity extends Activity  {
    private static final String TAG="DialConnectActivity";
    private EditText edittext_account;
    private EditText edittext_password;
    private EditText edittext_dns;
    private EditText edittext_mtu;
    private EditText edittext_mac;
    private SDKManager manager;
    private EventCallback ec;
    private RouterManager mRouterManager;
    private HomeGenius mHomeGenius;
    private String channels;
    private TitleLayout layout_title;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dial_connect);
        initViews();
        initDatas();
        initEvents();
    }

    private void initDatas() {
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
            public void notifyHomeGeniusResponse(String result) {
                super.notifyHomeGeniusResponse(result);
                parseDeviceReport(result);

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
                new AlertDialog(DialConnectActivity.this).builder().setTitle("账号异地登录")
                        .setMsg("当前账号已在其它设备上登录,是否重新登录")
                        .setPositiveButton("确认", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                startActivity(new Intent(DialConnectActivity.this, LoginActivity.class));
                            }
                        }).setNegativeButton("取消", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                }).show();
            }
        };
        layout_title.setReturnClickListener(new TitleLayout.ReturnImageClickListener() {
            @Override
            public void onBackPressed() {
                DialConnectActivity.this.onBackPressed();
            }
        });
        layout_title.setEditTextClickListener(new TitleLayout.EditTextClickListener() {
            @Override
            public void onEditTextPressed() {
                String account = edittext_account.getText().toString().trim();
                String password = edittext_password.getText().toString();
                String dns = edittext_dns.getText().toString();
                String mtu = edittext_mtu.getText().toString().trim();
                String mac = edittext_mac.getText().toString().trim();
                if (NetUtil.isNetAvailable(DialConnectActivity.this)) {
                    if (account.equals("") || password.equals("")) {
                        ToastSingleShow.showText(DialConnectActivity.this, "账号，密码不能为空");
                        return;
                    }
                    Proto proto = new Proto();
                    PPPOE pppoe = new PPPOE();
                    pppoe.setUsername(account);
                    pppoe.setPasswd(password);
                    pppoe.setDNS(dns);
                    pppoe.setMAC(mac);
                    pppoe.setMTU(mtu);
                    proto.setPPPOE(pppoe);
                    if (mRouterManager.getCurrentSelectedRouter().getStatus().equals("在线")) {
                        isUserLogin = Perfence.getBooleanPerfence(AppConstant.USER_LOGIN);
                        if (isUserLogin) {
                            if(channels!=null){
                                mHomeGenius.setWan(proto,channels);
                            }
                        } else {
                            ToastSingleShow.showText(DialConnectActivity.this, "未登录，无法设置拨号上网,请登录后重试");
                        }


                    } else {
                        RestfulToolsRouter.getSingleton(DialConnectActivity.this).internetAccess(account, password, new Callback<RouterResponse>() {
                            @Override
                            public void onResponse(Call<RouterResponse> call, Response<RouterResponse> response) {
                                int code = response.code();
                                if (code != 200) {
                                    String errorMsg = "";
                                    try {
                                        String text = response.errorBody().string();
                                        Gson gson = new Gson();
                                        ErrorResponse errorResponse = gson.fromJson(text, ErrorResponse.class);

                                        switch (errorResponse.getErrcode()) {
                                            case AppConstant.ERROR_CODE.OP_ERRCODE_BAD_TOKEN:
                                                ToastSingleShow.showText(DialConnectActivity.this, "登录的Token已过期");
                                                startActivity(new Intent(DialConnectActivity.this, LoginActivity.class));
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
                                    ToastSingleShow.showText(DialConnectActivity.this, errorMsg);
                                } else {
                                    startActivity(new Intent(DialConnectActivity.this, WifiSetting24.class));
                                }
                            }

                            @Override
                            public void onFailure(Call<RouterResponse> call, Throwable t) {
                            }
                        });
                    }

                } else {
                    ToastSingleShow.showText(DialConnectActivity.this, "网络连接已断开");
                }

            }
        });
    }
    private void parseDeviceReport(String xmlStr) {
        String op;
        String method;
        Gson gson = new Gson();
        PERFORMANCE content = gson.fromJson(xmlStr, PERFORMANCE.class);
        op = content.getOP();
        method = content.getMethod();
        Log.i(TAG, "op=" + op + "method=" + method + "result=" + content.getResult() + "xmlStr=" + xmlStr);

        if (op!= null) {
            if (op.equalsIgnoreCase("WAN")){
                if (method.equalsIgnoreCase("REPORT")) {
                    PERFORMANCE wan = gson.fromJson(xmlStr, PERFORMANCE.class);
                    Proto proto = wan.getProto();
                    if (proto.getPPPOE() != null) {
                        ToastSingleShow.showText(DialConnectActivity.this, "拨号上网设置成功");
                    }
                }
            }
        }
    }
    private void initEvents() {
    }

    private void initViews() {
        layout_title= findViewById(R.id.layout_title);
        edittext_account = findViewById(R.id.edittext_account);
        edittext_password = findViewById(R.id.edittext_password);
        edittext_dns = findViewById(R.id.edittext_dns);
        edittext_mtu = findViewById(R.id.edittext_mtu);
        edittext_mac = findViewById(R.id.edittext_mac);
    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean isStartFromExperience = DeviceManager.getInstance().isStartFromExperience();
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

    private boolean isUserLogin;

}
