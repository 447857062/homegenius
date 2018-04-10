package com.deplink.boruSmart.activity.device.router;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.deplink.boruSmart.activity.device.router.connectType.DialConnectActivity;
import com.deplink.boruSmart.activity.device.router.connectType.DynamicsActivity;
import com.deplink.boruSmart.activity.device.router.connectType.StaticConnectActivity;
import com.deplink.boruSmart.activity.device.router.connectType.WirelessRelayActivity;
import com.deplink.boruSmart.activity.device.router.wifi.WifiSetting24;
import com.deplink.boruSmart.activity.personal.login.LoginActivity;
import com.deplink.boruSmart.constant.AppConstant;
import com.deplink.boruSmart.manager.connect.remote.HomeGenius;
import com.deplink.boruSmart.manager.device.DeviceManager;
import com.deplink.boruSmart.manager.device.router.RouterManager;
import com.deplink.boruSmart.util.Perfence;
import com.deplink.boruSmart.view.combinationwidget.TitleLayout;
import com.deplink.boruSmart.view.dialog.ActionSheetDialog;
import com.deplink.boruSmart.view.dialog.AlertDialog;
import com.deplink.boruSmart.view.toast.Ftoast;
import com.deplink.sdk.android.sdk.DeplinkSDK;
import com.deplink.sdk.android.sdk.EventCallback;
import com.deplink.sdk.android.sdk.SDKAction;
import com.deplink.sdk.android.sdk.json.Lan;
import com.deplink.sdk.android.sdk.json.PERFORMANCE;
import com.deplink.sdk.android.sdk.json.Proto;
import com.deplink.sdk.android.sdk.manager.SDKManager;
import com.deplink.sdk.android.sdk.rest.ErrorResponse;
import com.deplink.sdk.android.sdk.rest.RestfulToolsRouter;
import com.deplink.sdk.android.sdk.rest.RouterResponse;
import com.google.gson.Gson;

import deplink.com.smartwirelessrelay.homegenius.EllESDK.R;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ConnectSettingActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "ConnectSettingActivity";
    private RelativeLayout layout_connect_type_setting;
    private SDKManager manager;
    private EventCallback ec;
    private TextView textview_current_connect_type;
    private RelativeLayout layout_pppoe_account;
    private RelativeLayout layout_pppoe_password;
    private RelativeLayout layout_wan_ip_setting;
    private RelativeLayout layout_submask_setting;
    private RelativeLayout layout_gateway_setting;
    private RelativeLayout layout_dns_setting;
    private RelativeLayout layout_mtu;
    private RelativeLayout layout_mac;
    private RelativeLayout layout_wifissid;
    private RelativeLayout layout_wifipassword;
    private RelativeLayout layout_encryption;
    private RelativeLayout layout_encryption_algorithm;
    private RelativeLayout layout_channel;
    private TextView textview_pppoe_account;
    private TextView textview_pppoe_password;
    private TextView textview_wanip_value;
    private TextView textview_submask_value;
    private TextView textview_gateway_value;
    private TextView textview_dns_value;
    private TextView textview_mtu;
    private TextView textview_mac;
    private TextView textview_wifi_ssid;
    private TextView textview_wifipassword;
    private TextView textview_encryption;
    private TextView textview_encryption_algorithm;
    private TextView textview_channel;
    private TextView textview_lan_ip;
    private TextView textview_netmask;
    private TextView textview_dhcp_status;
    private RouterManager mRouterManager;
    private TitleLayout layout_title;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_setting);
        initViews();
        initDatas();
        initEvents();
    }

    @Override
    protected void onResume() {
        super.onResume();
        HomeGenius mHomeGenius = new HomeGenius();
        String channels = mRouterManager.getCurrentSelectedRouter().getRouter().getChannels();
        if (!DeviceManager.getInstance().isStartFromExperience()) {
            manager.addEventCallback(ec);
            if (channels != null) {
                mHomeGenius.queryWan(channels);
                mHomeGenius.queryLan(channels);
            }

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        manager.removeEventCallback(ec);
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

        } else if (op.equalsIgnoreCase("LAN")) {
            if (method.equalsIgnoreCase("REPORT")) {
                Lan lan = gson.fromJson(xmlStr, Lan.class);
                Log.i(TAG, "get lan =" + lan.toString());
                textview_lan_ip.setText(lan.getLANIP());
                textview_netmask.setText(lan.getNETMASK());
                if (lan.getDhcpStatus().equalsIgnoreCase("ON")) {
                    textview_dhcp_status.setText("开启");
                } else {
                    textview_dhcp_status.setText("关闭");
                }
            }
        } else if (op.equalsIgnoreCase("WAN")) {
            if (method.equalsIgnoreCase("REPORT")) {
                PERFORMANCE wan = gson.fromJson(xmlStr, PERFORMANCE.class);
                Proto proto;
                proto = wan.getProto();
                String ConnectType;
                if (proto != null) {
                    if (proto.getAP_CLIENT() != null) {
                        ConnectType = "中继功能";
                        setApclientConnectTypeTextview(proto);
                    } else if (proto.getDHCP() != null) {
                        ConnectType = "动态IP";
                        setDynamicConnectTypeTextview(proto);
                    } else if (proto.getSTATIC() != null) {
                        ConnectType = "静态IP";
                        setStaticipConnecttypeTextview(proto);
                    } else if (proto.getPPPOE() != null) {
                        ConnectType = "拨号上网";
                        setPPPOEConnectTypeTextview(proto);
                    } else {
                        ConnectType = "--";
                    }
                    textview_current_connect_type.setText(ConnectType);
                }
            }
        }
    }

    private void initDatas() {
        layout_title.setReturnClickListener(new TitleLayout.ReturnImageClickListener() {
            @Override
            public void onBackPressed() {
                ConnectSettingActivity.this.onBackPressed();
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
                new AlertDialog(ConnectSettingActivity.this).builder().setTitle("账号异地登录")
                        .setMsg("当前账号已在其它设备上登录,是否重新登录")
                        .setPositiveButton("确认", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                startActivity(new Intent(ConnectSettingActivity.this, LoginActivity.class));
                            }
                        }).setNegativeButton("取消", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                }).show();
            }
        };
    }

    /**
     * 设置拨号上网方式的信息显示
     *
     * @param proto
     */
    private void setPPPOEConnectTypeTextview(Proto proto) {
        layout_pppoe_account.setVisibility(View.VISIBLE);
        layout_pppoe_password.setVisibility(View.VISIBLE);
        layout_dns_setting.setVisibility(View.VISIBLE);
        layout_mtu.setVisibility(View.VISIBLE);
        layout_mac.setVisibility(View.VISIBLE);

        layout_wan_ip_setting.setVisibility(View.GONE);
        layout_submask_setting.setVisibility(View.GONE);
        layout_gateway_setting.setVisibility(View.GONE);
        layout_wifissid.setVisibility(View.GONE);
        layout_wifipassword.setVisibility(View.GONE);
        layout_encryption.setVisibility(View.GONE);
        layout_encryption_algorithm.setVisibility(View.GONE);
        layout_channel.setVisibility(View.GONE);
        layout_wan_ip_setting.setVisibility(View.GONE);
        layout_mac.setVisibility(View.GONE);
        textview_pppoe_account.setText("" + proto.getPPPOE().getUsername());
        textview_pppoe_password.setText("" + proto.getPPPOE().getPasswd());
        if (proto.getPPPOE().getDNS() == null) {
            textview_dns_value.setText("未设置");
        } else {
            textview_dns_value.setText("" + proto.getPPPOE().getDNS());
        }
        if (proto.getPPPOE().getMTU() == null) {
            textview_mtu.setText("未设置");
        } else {
            textview_mtu.setText("" + proto.getPPPOE().getMTU());
        }
        textview_mac.setText("" + proto.getPPPOE().getMAC());
    }

    /**
     * 设置静态ip上网方式的信息显示
     *
     * @param proto
     */
    private void setStaticipConnecttypeTextview(Proto proto) {
        layout_wan_ip_setting.setVisibility(View.VISIBLE);
        layout_submask_setting.setVisibility(View.VISIBLE);
        layout_gateway_setting.setVisibility(View.VISIBLE);
        layout_dns_setting.setVisibility(View.VISIBLE);
        layout_mtu.setVisibility(View.VISIBLE);

        layout_wifissid.setVisibility(View.GONE);
        layout_wifipassword.setVisibility(View.GONE);
        layout_encryption.setVisibility(View.GONE);
        layout_encryption_algorithm.setVisibility(View.GONE);
        layout_channel.setVisibility(View.GONE);
        layout_pppoe_account.setVisibility(View.GONE);
        layout_pppoe_password.setVisibility(View.GONE);
        layout_wan_ip_setting.setVisibility(View.GONE);
        layout_mac.setVisibility(View.GONE);

        textview_wanip_value.setText("" + proto.getSTATIC().getIPADDR());

        textview_gateway_value.setText("" + proto.getSTATIC().getGATEWAY());
        if (proto.getSTATIC().getDNS() == null) {
            textview_dns_value.setText("未设置");
        } else {
            textview_dns_value.setText("" + proto.getSTATIC().getDNS());
        }
        if (proto.getSTATIC().getMTU() == null) {
            textview_mtu.setText("未设置");
        } else {
            textview_mtu.setText("" + proto.getSTATIC().getMTU());
        }
        if (proto.getSTATIC().getNETMASK() == null) {
            textview_submask_value.setText("未设置");
        } else {
            textview_submask_value.setText("" + proto.getSTATIC().getNETMASK());
        }
    }

    /**
     * 设置动态ip上网方式 的信息显示
     *
     * @param proto
     */
    private void setDynamicConnectTypeTextview(Proto proto) {
        layout_dns_setting.setVisibility(View.VISIBLE);
        layout_mtu.setVisibility(View.VISIBLE);
        layout_wifissid.setVisibility(View.GONE);
        layout_wifipassword.setVisibility(View.GONE);
        layout_encryption.setVisibility(View.GONE);
        layout_encryption_algorithm.setVisibility(View.GONE);
        layout_channel.setVisibility(View.GONE);
        layout_pppoe_account.setVisibility(View.GONE);
        layout_pppoe_password.setVisibility(View.GONE);
        layout_wan_ip_setting.setVisibility(View.GONE);
        layout_submask_setting.setVisibility(View.GONE);
        layout_gateway_setting.setVisibility(View.GONE);
        layout_mac.setVisibility(View.GONE);
        if (proto.getDHCP().getDNS() == null) {
            textview_dns_value.setText("未设置");
        } else {
            textview_dns_value.setText("" + proto.getDHCP().getDNS());
        }
        if (proto.getDHCP().getMTU() == null) {
            textview_mtu.setText("未设置");
        } else {
            textview_mtu.setText("" + proto.getDHCP().getMTU());
        }
    }

    /**
     * 设置中继上网的信息显示
     *
     * @param proto
     */
    private void setApclientConnectTypeTextview(Proto proto) {
        layout_wifissid.setVisibility(View.VISIBLE);
        layout_wifipassword.setVisibility(View.VISIBLE);
        layout_encryption.setVisibility(View.VISIBLE);
        layout_encryption_algorithm.setVisibility(View.VISIBLE);
        layout_channel.setVisibility(View.VISIBLE);
        layout_pppoe_account.setVisibility(View.GONE);
        layout_pppoe_password.setVisibility(View.GONE);
        layout_wan_ip_setting.setVisibility(View.GONE);
        layout_submask_setting.setVisibility(View.GONE);
        layout_gateway_setting.setVisibility(View.GONE);
        layout_dns_setting.setVisibility(View.GONE);
        layout_mtu.setVisibility(View.GONE);
        layout_mac.setVisibility(View.GONE);
        textview_wifi_ssid.setText("" + proto.getAP_CLIENT().getApCliSsid());
        textview_wifipassword.setText("" + proto.getAP_CLIENT().getApCliWPAPSK());
        textview_encryption.setText("" + proto.getAP_CLIENT().getApCliAuthMode());
        textview_encryption_algorithm.setText("" + proto.getAP_CLIENT().getApCliEncrypType());
        textview_channel.setText("" + proto.getAP_CLIENT().getChannel());
    }

    private void initEvents() {
        layout_connect_type_setting.setOnClickListener(this);
    }

    private void initViews() {
        layout_title= (TitleLayout) findViewById(R.id.layout_title);
        layout_connect_type_setting = (RelativeLayout) findViewById(R.id.layout_connect_type_setting);
        textview_current_connect_type = (TextView) findViewById(R.id.textview_current_connect_type);
        textview_pppoe_account = (TextView) findViewById(R.id.textview_pppoe_account);
        textview_pppoe_password = (TextView) findViewById(R.id.textview_pppoe_password);
        textview_wanip_value = (TextView) findViewById(R.id.textview_wanip_value);
        textview_submask_value = (TextView) findViewById(R.id.textview_submask_value);
        textview_gateway_value = (TextView) findViewById(R.id.textview_gateway_value);
        textview_dns_value = (TextView) findViewById(R.id.textview_dns_value);
        textview_mtu = (TextView) findViewById(R.id.textview_mtu);
        textview_mac = (TextView) findViewById(R.id.textview_mac);
        textview_wifi_ssid = (TextView) findViewById(R.id.textview_wifi_ssid);
        textview_wifipassword = (TextView) findViewById(R.id.textview_wifipassword);
        textview_encryption = (TextView) findViewById(R.id.textview_encryption);
        textview_encryption_algorithm = (TextView) findViewById(R.id.textview_encryption_algorithm);
        textview_channel = (TextView) findViewById(R.id.textview_channel);
        layout_pppoe_account = (RelativeLayout) findViewById(R.id.layout_pppoe_account);
        layout_pppoe_password = (RelativeLayout) findViewById(R.id.layout_pppoe_password);
        layout_wan_ip_setting = (RelativeLayout) findViewById(R.id.layout_wan_ip_setting);
        layout_submask_setting = (RelativeLayout) findViewById(R.id.layout_submask_setting);
        layout_gateway_setting = (RelativeLayout) findViewById(R.id.layout_gateway_setting);
        layout_dns_setting = (RelativeLayout) findViewById(R.id.layout_dns_setting);
        layout_mtu = (RelativeLayout) findViewById(R.id.layout_mtu);
        layout_mac = (RelativeLayout) findViewById(R.id.layout_mac);
        layout_wifissid = (RelativeLayout) findViewById(R.id.layout_wifissid);
        layout_wifipassword = (RelativeLayout) findViewById(R.id.layout_wifipassword);
        layout_encryption = (RelativeLayout) findViewById(R.id.layout_encryption);
        layout_encryption_algorithm = (RelativeLayout) findViewById(R.id.layout_encryption_algorithm);
        layout_channel = (RelativeLayout) findViewById(R.id.layout_channel);

        textview_lan_ip = (TextView) findViewById(R.id.textview_lan_ip);
        textview_netmask = (TextView) findViewById(R.id.textview_netmask);
        textview_dhcp_status = (TextView) findViewById(R.id.textview_dhcp_status);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.layout_connect_type_setting:
                boolean isUserLogin = Perfence.getBooleanPerfence(AppConstant.USER_LOGIN);
                if (isUserLogin) {
                    new ActionSheetDialog(ConnectSettingActivity.this)
                            .builder()
                            .setCancelable(true)
                            .setCanceledOnTouchOutside(true)
                            .addSheetItem("宽带拨号", ActionSheetDialog.SheetItemColor.GRAY,
                                    new ActionSheetDialog.OnSheetItemClickListener() {
                                        @Override
                                        public void onClick(int which) {
                                            ConnectSettingActivity.this.startActivity(new Intent(ConnectSettingActivity.this, DialConnectActivity.class));
                                        }
                                    })
                            .addSheetItem("动态IP", ActionSheetDialog.SheetItemColor.GRAY,
                                    new ActionSheetDialog.OnSheetItemClickListener() {
                                        @Override
                                        public void onClick(int which) {
                                            ConnectSettingActivity.this.startActivity(new Intent(ConnectSettingActivity.this, DynamicsActivity.class));
                                        }
                                    })
                            .addSheetItem("静态IP", ActionSheetDialog.SheetItemColor.GRAY,
                                    new ActionSheetDialog.OnSheetItemClickListener() {
                                        @Override
                                        public void onClick(int which) {
                                            Intent staticIPIntent = new Intent(ConnectSettingActivity.this, StaticConnectActivity.class);
                                            staticIPIntent.putExtra(AppConstant.OPERATION_TYPE, AppConstant.OPERATION_TYPE_WEB);
                                            ConnectSettingActivity.this.startActivity(staticIPIntent);
                                        }
                                    })
                            .addSheetItem("无线中继", ActionSheetDialog.SheetItemColor.GRAY,
                                    new ActionSheetDialog.OnSheetItemClickListener() {
                                        @Override
                                        public void onClick(int which) {
                                            Intent wirelessRelayIntent = new Intent(ConnectSettingActivity.this,
                                                    WirelessRelayActivity.class);
                                            wirelessRelayIntent.putExtra(AppConstant.OPERATION_TYPE,
                                                    AppConstant.OPERATION_TYPE_WEB);
                                            ConnectSettingActivity.this.startActivity(wirelessRelayIntent);
                                        }
                                    })
                            .show();
                } else {
                    Ftoast.create(this).setText("未登录，无法设置上网方式,请登录后重试").show();
                }

                break;
        }
    }

    /**
     * （成功连接本地路由器后）选择上网方式
     */
    private void selectConnectType() {
        RestfulToolsRouter.getSingleton(ConnectSettingActivity.this).dynamicIp(new Callback<RouterResponse>() {
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
                                text = AppConstant.ERROR_MSG.OP_ERRCODE_BAD_TOKEN;
                                Ftoast.create(ConnectSettingActivity.this).setText( "登录已失效 :" + text).show();
                                startActivity(new Intent(ConnectSettingActivity.this, LoginActivity.class));
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
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (!errorMsg.equalsIgnoreCase("")) {
                        Ftoast.create(ConnectSettingActivity.this).setText( errorMsg).show();
                    }
                } else {
                    Ftoast.create(ConnectSettingActivity.this).setText( "动态IP设置成功，请设置wifi名字密码").show();
                    Intent intentWifiSetting = new Intent(ConnectSettingActivity.this, WifiSetting24.class);
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
