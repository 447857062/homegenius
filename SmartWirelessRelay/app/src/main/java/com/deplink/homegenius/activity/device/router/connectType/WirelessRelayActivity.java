package com.deplink.homegenius.activity.device.router.connectType;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.deplink.homegenius.activity.personal.login.LoginActivity;
import com.deplink.homegenius.constant.AppConstant;
import com.deplink.homegenius.manager.connect.remote.HomeGenius;
import com.deplink.homegenius.manager.device.DeviceManager;
import com.deplink.homegenius.manager.device.router.RouterManager;
import com.deplink.homegenius.util.NetUtil;
import com.deplink.homegenius.util.Perfence;
import com.deplink.homegenius.util.WeakRefHandler;
import com.deplink.homegenius.view.combinationwidget.TitleLayout;
import com.deplink.homegenius.view.dialog.AlertDialog;
import com.deplink.homegenius.view.dialog.InputAlertDialog;
import com.deplink.homegenius.view.dialog.loadingdialog.DialogThreeBounce;
import com.deplink.homegenius.view.toast.ToastSingleShow;
import com.deplink.sdk.android.sdk.DeplinkSDK;
import com.deplink.sdk.android.sdk.EventCallback;
import com.deplink.sdk.android.sdk.SDKAction;
import com.deplink.sdk.android.sdk.json.AP_CLIENT;
import com.deplink.sdk.android.sdk.json.PERFORMANCE;
import com.deplink.sdk.android.sdk.json.Proto;
import com.deplink.sdk.android.sdk.json.SSIDList;
import com.deplink.sdk.android.sdk.manager.SDKManager;
import com.deplink.sdk.android.sdk.rest.ErrorResponse;
import com.deplink.sdk.android.sdk.rest.RestfulToolsRouter;
import com.deplink.sdk.android.sdk.rest.RestfulToolsString;
import com.deplink.sdk.android.sdk.rest.RouterResponse;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import deplink.com.smartwirelessrelay.homegenius.EllESDK.R;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WirelessRelayActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "WirelessRelayActivity";
    private static final int MSG_LOCAL_OP_RETURN_OK = 1;
    private PullToRefreshListView mPullToRefreshListView;
    //本地接口
    private WirelessRelayAdapter mAdapter;
    private List<wifiScanRoot> mDatas;
    //本地接口 end
    //MQTT接口
    private SDKManager manager;
    private EventCallback ec;
    private List<SSIDList> mDatasMqtt;
    private WirelessRelayAdapterMqtt mAdapterMqtt;
    //MQTT接口 end
    private String op_type;
    //链接参数
    private String crypt = "";
    private String encryption = "";
    private int channel = 1;
    private String userName = "";
    private String password = "";
    private TextView button_reload_wifirelay;
    private HomeGenius mHomeGenius;
    private TitleLayout layout_title;
    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_LOCAL_OP_RETURN_OK:
                    showRebootConfirmDialog();
                    break;
            }
            return true;
        }
    };
    private Handler mHandler = new WeakRefHandler(mCallback);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wireless_relay);
        op_type = getIntent().getStringExtra(AppConstant.OPERATION_TYPE);
        initViews();
        initDatas();
        initEvents();
    }

    private void initDatas() {

        layout_title.setReturnClickListener(new TitleLayout.ReturnImageClickListener() {
            @Override
            public void onBackPressed() {
                WirelessRelayActivity.this.onBackPressed();
            }
        });
        mRouterManager = RouterManager.getInstance();
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
            public void notifyHomeGeniusResponse(String result) {
                super.notifyHomeGeniusResponse(result);
                parseDeviceReport(result);
            }

            @Override
            public void deviceOpSuccess(String op, String deviceKey) {
                super.deviceOpSuccess(op, deviceKey);
            }

            @Override
            public void connectionLost(Throwable throwable) {
                super.connectionLost(throwable);
                Perfence.setPerfence(AppConstant.USER_LOGIN, false);
                new AlertDialog(WirelessRelayActivity.this).builder().setTitle("账号异地登录")
                        .setMsg("当前账号已在其它设备上登录,是否重新登录")
                        .setPositiveButton("确认", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                startActivity(new Intent(WirelessRelayActivity.this, LoginActivity.class));
                            }
                        }).setNegativeButton("取消", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                }).show();
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

        } else if (op.equalsIgnoreCase("REPORT")) {
            if (method.equalsIgnoreCase("WIFIRELAY")) {
                mDatasMqtt.clear();
                mDatasMqtt.addAll(content.getSSIDList());
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        DialogThreeBounce.hideLoading();
                        mAdapterMqtt.notifyDataSetChanged();
                        mPullToRefreshListView.onRefreshComplete();
                    }
                });
            }
        }
    }
    /**
     * 显示重启确认对话框
     */
    private void showRebootConfirmDialog() {
        //提示重启
        new AlertDialog(WirelessRelayActivity.this).builder().setTitle("重启路由器")
                .setMsg("确认重启路由器?")
                .setPositiveButton("确认", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        RestfulToolsRouter.getSingleton(WirelessRelayActivity.this).rebootRouter(new Callback<String>() {
                            @Override
                            public void onResponse(Call<String> call, Response<String> response) {
                                Log.i(TAG, "response.code=" + response.code());
                            }
                            @Override
                            public void onFailure(Call<String> call, Throwable t) {

                            }
                        });
                    }
                }).setNegativeButton("取消", new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        }).show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        manager.removeEventCallback(ec);
    }
    private boolean isStartFromExperience;
    private String channels;
    private RouterManager mRouterManager;
    @Override
    protected void onResume() {
        super.onResume();
        isStartFromExperience= DeviceManager.getInstance().isStartFromExperience();
        mHomeGenius = new HomeGenius();
        if(!isStartFromExperience){
            manager.addEventCallback(ec);
            channels = mRouterManager.getCurrentSelectedRouter().getRouter().getChannels();
            getRouterDevice();
            handlerResumeDialogShowing();
            Log.i(TAG, "op_type=" + op_type);
            if (op_type.equals(AppConstant.OPERATION_TYPE_LOCAL)) {
                initLocalPulltorefreshView();
                getLocalWirelessRelay();
            } else {
                initMqttPulltoRefreshView();
                    mHomeGenius.queryWifiRelay(channels);

            }
        }

    }

    /**
     * 初始化本地接口的pulltorefresh View
     */
    private void initLocalPulltorefreshView() {
        mDatas = new ArrayList<>();
        mAdapter = new WirelessRelayAdapter(WirelessRelayActivity.this, mDatas);
        mPullToRefreshListView.setVisibility(View.VISIBLE);
        mPullToRefreshListView.getRefreshableView().setAdapter(mAdapter);
        mPullToRefreshListView.getRefreshableView().setOnItemClickListener(mLocalInterfaceItemClick);
    }

    /**
     * 初始化mqtt的pulltorefresh View
     */
    private void initMqttPulltoRefreshView() {
        mDatasMqtt = new ArrayList<>();
        mAdapterMqtt = new WirelessRelayAdapterMqtt(WirelessRelayActivity.this, mDatasMqtt);
        mPullToRefreshListView.setVisibility(View.VISIBLE);
        mPullToRefreshListView.getRefreshableView().setAdapter(mAdapterMqtt);
        mPullToRefreshListView.getRefreshableView().setOnItemClickListener(mMqttItemClick);
    }

    private void handlerResumeDialogShowing() {
        DialogThreeBounce.showLoading(this);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                DialogThreeBounce.hideLoading();
            }
        }, 2500);
    }

    /**
     * 获取当前选中的设备
     */
    private void getRouterDevice() {
        String currentDevcieKey = Perfence.getPerfence(AppConstant.DEVICE.CURRENT_DEVICE_KEY);
        if (currentDevcieKey.equals("")) {
            if (manager.getDeviceList() != null && manager.getDeviceList().size() > 0) {
                Perfence.setPerfence(AppConstant.DEVICE.CURRENT_DEVICE_KEY, manager.getDeviceList().get(0).getDeviceKey());
            }

        }
    }

    /**
     * 获取本地接口的无线中继列表
     */
    private void getLocalWirelessRelay() {
        RestfulToolsString.getSingleton(WirelessRelayActivity.this).WirelessRelayScan(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                Log.i(TAG, "WirelessRelayScan=" + response.code() + "mDatas.size()=" + mDatas.size());
                mDatas.clear();
                mDatas.addAll(readJsonWlanScan(response.body().trim()));
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        DialogThreeBounce.hideLoading();
                        mAdapter.notifyDataSetChanged();
                        mPullToRefreshListView.onRefreshComplete();
                    }
                });
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {

            }
        });
    }

    private void getConnectParams(int position) {
        crypt = "";
        int getGroupCiphersSize = 0;
        if (mDatas.get(position).getencryption().getGroupCiphers() != null) {

            getGroupCiphersSize = mDatas.get(position).getencryption().getGroupCiphers().size();
        }
        Log.i(TAG, "getGroupCiphersSize=" + getGroupCiphersSize + "crypt=" + crypt);
        if (getGroupCiphersSize == 1) {
            if (mDatas.get(position).getencryption().getWep()) {//object
                crypt = "WEP";
            } else {
                crypt = "";
            }
        } else {//array
            for (int i = 0; i < getGroupCiphersSize; i++) {
                crypt += mDatas.get(position).getencryption().getGroupCiphers().get(i);
            }
            Log.i(TAG, "crypt=" + crypt);
        }

        encryption = mDatas.get(position).getencryption().getDescription();
        boolean wep = mDatas.get(position).getencryption().getWep();
        int wpa = mDatas.get(position).getencryption().getWpa();
        boolean enable = mDatas.get(position).getencryption().getEnabled();
        if (wep) {
            encryption = "WEP";
        } else if (wpa > 0) {
            if (wpa == 2 || wpa == 3) {

                String temp = "";
                for (int i = 0; i < mDatas.get(position).getencryption().getAuthSuites().size(); i++) {
                    temp += mDatas.get(position).getencryption().getAuthSuites().get(i);
                }
                encryption = "WPA2" + temp;
            } else {
                String temp = "";
                for (int i = 0; i < mDatas.get(position).getencryption().getAuthSuites().size(); i++) {
                    temp += mDatas.get(position).getencryption().getAuthSuites().get(i);
                }
                encryption += "WPA" + temp;
            }
        } else if (enable) {
            encryption = "UNKNOWN";
        } else {
            encryption = "OPEN";
        }
        channel = mDatas.get(position).getChannel();
        userName = mDatas.get(position).getSsid();
        Log.i(TAG, "crypt=" + crypt + "encryption=" + encryption + "channel=" + channel + "userName=" + userName + "password=" + password);
    }
    private AdapterView.OnItemClickListener mMqttItemClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
            Log.i(TAG, "mqtt item click position=" + position);
            //注意position位置从1开始,data数据要减少一个
            if (!mDatasMqtt.get(position - 1).getEncryption().equalsIgnoreCase("none")) {
                mqttSetWanDialogWithPasswordShow(position);
            } else {
                mqttSetWanDialogNoPasswordShow(position);
            }
        }
    };

    /**
     * 显示  mqtt的方式，设置wan方法（无线中继连接方式，需要密码）确认框
     *
     * @param position
     */
    private void mqttSetWanDialogWithPasswordShow(final int position) {
        new InputAlertDialog(WirelessRelayActivity.this).builder()
                .setTitle("连接:" + mDatasMqtt.get(position - 1).getSSID())
                .setEditTextHint("请输入wifi密码")
                .setPositiveButton("确认", new InputAlertDialog.onSureBtnClickListener() {
                    @Override
                    public void onSureBtnClicked(String result) {
                        Proto proto = new Proto();
                        AP_CLIENT ap_client = new AP_CLIENT();
                        SSIDList ssidlist = mDatasMqtt.get(position - 1);
                        ap_client.setApCliSsid(ssidlist.getSSID());
                        ap_client.setApCliWPAPSK(password);
                        ap_client.setChannel(ssidlist.getChannel());
                        ap_client.setApCliEncrypType(ssidlist.getCRYTP());
                        ap_client.setApCliAuthMode(ssidlist.getEncryption());
                        proto.setAP_CLIENT(ap_client);
                        if (NetUtil.isNetAvailable(WirelessRelayActivity.this)) {
                            mHomeGenius.setWan(proto,channels);
                            ToastSingleShow.showText(WirelessRelayActivity.this, "中继上网已设置,正在重启路由器");

                        } else {
                            ToastSingleShow.showText(WirelessRelayActivity.this, "网络连接已断开");
                        }
                    }
                }).setNegativeButton("取消", new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        }).show();
    }

    /**
     * 显示  mqtt的方式，设置wan方法（无线中继连接方式，不需要密码）确认框
     *
     * @param position
     */
    private void mqttSetWanDialogNoPasswordShow(final int position) {
        new AlertDialog(WirelessRelayActivity.this).builder().setTitle("无线中继")
                .setMsg("连接：" + mDatasMqtt.get(position - 1).getSSID())
                .setPositiveButton("确认", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Proto proto = new Proto();
                        AP_CLIENT ap_client = new AP_CLIENT();
                        SSIDList ssidlist = mDatasMqtt.get(position - 1);
                        ap_client.setApCliSsid(ssidlist.getSSID());
                        ap_client.setChannel(ssidlist.getChannel());
                        ap_client.setApCliEncrypType(ssidlist.getEncryption());
                        proto.setAP_CLIENT(ap_client);
                        if (NetUtil.isNetAvailable(WirelessRelayActivity.this)) {

                            boolean isUserLogin;
                            isUserLogin = Perfence.getBooleanPerfence(AppConstant.USER_LOGIN);
                            if (isUserLogin) {
                                mHomeGenius.setWan(proto,channels);
                                ToastSingleShow.showText(WirelessRelayActivity.this, "中继上网已设置，正在重启路由器");
                            } else {
                                ToastSingleShow.showText(WirelessRelayActivity.this, "未登录，无法设置静态上网,请登录后重试");
                            }


                        } else {
                            ToastSingleShow.showText(WirelessRelayActivity.this, "网络连接已断开");
                        }
                    }
                }).setNegativeButton("取消", new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        }).show();
    }

    /**
     * 本地接口的无线中继界面item点击监听
     */
    private AdapterView.OnItemClickListener mLocalInterfaceItemClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
            Log.i(TAG, " mData.get(position).getencryption().getEnabled()=" + mDatas.get(position - 1).getencryption().getEnabled());
            {
                getConnectParams(position - 1);
                Log.i(TAG, " mData.get(position).getencryption().getEnabled()=" + mDatas.get(position - 1).getencryption().getEnabled());
                if (!encryption.equals("OPEN")) {//需要密码的
                    new InputAlertDialog(WirelessRelayActivity.this).builder()
                            .setTitle("连接:" + mDatas.get(position - 1).getSsid())
                            .setEditTextHint("请输入wifi密码")
                            .setPositiveButton("确认", new InputAlertDialog.onSureBtnClickListener() {
                                @Override
                                public void onSureBtnClicked(String result) {
                                    password = result;
                                    if (!password.equals("")) {
                                        Log.i(TAG, "本地设置无线中继 crypt=" + crypt + "encryption=" + encryption + "password=" + password + "userName=" + userName + "channel=" + channel);
                                        wirelessRelayConnectLocal(true);
                                    }
                                }
                            }).setNegativeButton("取消", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                        }
                    }).show();
                } else {
                    new AlertDialog(WirelessRelayActivity.this).builder().setTitle("无线中继")
                            .setMsg("连接：" + mDatas.get(position - 1).getSsid())
                            .setPositiveButton("确认", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    wirelessRelayConnectLocal(false);
                                }
                            }).setNegativeButton("取消", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                        }
                    }).show();
                }
            }
        }
    };


    /**
     * 无线中继连接（本地接口）
     */
    private void wirelessRelayConnectLocal(boolean hasPassword) {
        if (!hasPassword) {
            password = "";
        }
        RestfulToolsRouter.getSingleton(WirelessRelayActivity.this).WirelessRelayConnect(crypt, encryption, channel, userName, password, new Callback<RouterResponse>() {
            @Override
            public void onResponse(Call<RouterResponse> call, Response<RouterResponse> response) {
                int code = response.code();
                if (code == 200) {
                    Log.i(TAG, "WirelessRelayConnect" + code + "message=" + response.body().getMsg());
                    ToastSingleShow.showText(WirelessRelayActivity.this, "无线中继上网设置成功");
                    //重启
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            //提示重启
                            new AlertDialog(WirelessRelayActivity.this).builder().setTitle("重启路由器")
                                    .setMsg("确认重启路由器?")
                                    .setPositiveButton("确认", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            rebootRouterLocal();
                                        }
                                    }).setNegativeButton("取消", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                }
                            }).show();
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<RouterResponse> call, Throwable t) {
                t.printStackTrace();
                Log.i(TAG, "WirelessRelayConnect onFailure" + t.getMessage());
            }
        });
    }

    private void rebootRouterLocal() {
        RestfulToolsRouter.getSingleton(WirelessRelayActivity.this).rebootRouter(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                Log.i(TAG, "response.code=" + response.code());
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {

            }
        });

    }

    private void initEvents() {
        button_reload_wifirelay.setOnClickListener(this);
        mPullToRefreshListView.getRefreshableView().setDivider(null);
        mPullToRefreshListView.getRefreshableView().setSelector(android.R.color.transparent);
        mPullToRefreshListView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
        // 设置上下刷新 使用 OnRefreshListener2
        mPullToRefreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                mPullToRefreshListView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (op_type.equals(AppConstant.OPERATION_TYPE_LOCAL)) {
                            refreshLocalWirelessRelay();
                        } else {

                                mHomeGenius.queryWifiRelay(channels);


                        }
                    }
                }, 500);
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {

            }
        });
    }

    /**
     * 下拉刷新-本地接口
     */
    private void refreshLocalWirelessRelay() {
       RestfulToolsString.getSingleton(WirelessRelayActivity.this).WirelessRelayScan(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                int code = response.code();
                Log.i(TAG, "code=" + code + "WirelessRelayScan=" + response.body().trim());
                if (code != 200) {
                    String errorMsg = "";
                    try {
                        String text = response.errorBody().string();
                        Gson gson = new Gson();
                        ErrorResponse errorResponse = gson.fromJson(text, ErrorResponse.class);

                        switch (errorResponse.getErrcode()) {
                            case AppConstant.ERROR_CODE.OP_ERRCODE_BAD_TOKEN:
                                text = AppConstant.ERROR_MSG.OP_ERRCODE_BAD_TOKEN;
                                ToastSingleShow.showText(WirelessRelayActivity.this, "登录失效 :" + text);
                                startActivity(new Intent(WirelessRelayActivity.this, LoginActivity.class));
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
                    ToastSingleShow.showText(WirelessRelayActivity.this, errorMsg);
                } else {

                    if (mDatas == null) {
                        mDatas = new ArrayList<>();
                    }
                    mDatas.clear();
                    mDatas.addAll(readJsonWlanScan(response.body().trim()));
                    mHandler.sendEmptyMessage(0);
                    mPullToRefreshListView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mPullToRefreshListView.onRefreshComplete();
                        }
                    }, 1000);
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
            }
        });
    }

    private void initViews() {
        button_reload_wifirelay = findViewById(R.id.button_reload_wifirelay);
        mPullToRefreshListView = findViewById(R.id.list_wireless_relay_line);
        layout_title= findViewById(R.id.layout_title);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_reload_wifirelay:
                if(!isStartFromExperience) {
                    if (op_type.equals(AppConstant.OPERATION_TYPE_LOCAL)) {
                        getLocalWirelessRelay();
                    } else {
                            mHomeGenius.queryWifiRelay(channels);

                    }
                }

                break;
        }
    }

    public List<wifiScanRoot> readJsonWlanScan(String jsonData) {
        List<wifiScanRoot> listData = new ArrayList<>();
        try {
            JsonReader reader = new JsonReader(new StringReader(jsonData));
            String tagName;
            reader.beginArray();//数组对象
            while (reader.hasNext()) {
                reader.beginObject();//wifiScanRoot
                wifiScanRoot mWifiScanRoot = new wifiScanRoot();
                while (reader.hasNext()) {
                    tagName = reader.nextName();
                    if (tagName.equals("encryption")) {
                        reader.beginObject();//encryption
                        encryption encryption = new encryption();
                        while (reader.hasNext()) {
                            tagName = reader.nextName();
                            if (tagName.equals("enabled")) {
                                encryption.setEnabled(reader.nextBoolean());
                            } else if (tagName.equals("auth_algs")) {
                                reader.beginObject();
                                AuthAlgs mAuthAlgs = new AuthAlgs();
                                encryption.setAuthAlgs(mAuthAlgs);
                                reader.endObject();
                            } else if (tagName.equals("description")) {
                                encryption.setDescription(reader.nextString());
                            } else if (tagName.equals("wep")) {
                                encryption.setWep(reader.nextBoolean());
                            } else if (tagName.equals("auth_suites")) {
                                List<String> auth_suites = new ArrayList<>();
                                Log.i(TAG, "reader=auth_suites");
                                if (reader.peek() == JsonToken.BEGIN_ARRAY) {
                                    reader.beginArray();
                                    while (reader.hasNext()) {
                                        auth_suites.add(reader.nextString());
                                    }
                                    encryption.setAuthSuites(auth_suites);
                                    reader.endArray();
                                } else if (reader.peek() == JsonToken.BEGIN_OBJECT) {
                                    reader.beginObject();
                                    encryption.setAuthSuites(auth_suites);
                                    reader.endObject();
                                }
                            } else if (tagName.equals("wpa")) {
                                encryption.setWpa(reader.nextInt());
                            } else if (tagName.equals("pair_ciphers")) {
                                List<String> pair_ciphers = new ArrayList<>();
                                if (reader.peek() == JsonToken.BEGIN_ARRAY) {
                                    reader.beginArray();
                                    while (reader.hasNext()) {
                                        pair_ciphers.add(reader.nextString());
                                    }
                                    encryption.setPairCiphers(pair_ciphers);
                                    reader.endArray();
                                } else if (reader.peek() == JsonToken.BEGIN_OBJECT) {
                                    reader.beginObject();
                                    encryption.setPairCiphers(pair_ciphers);
                                    reader.endObject();
                                }
                            } else if (tagName.equals("group_ciphers")) {
                                List<String> group_ciphers = new ArrayList<>();
                                if (reader.peek() == JsonToken.BEGIN_ARRAY) {
                                    reader.beginArray();
                                    group_ciphers.clear();
                                    while (reader.hasNext()) {
                                        group_ciphers.add(reader.nextString());
                                    }
                                    for (int i = 0; i < group_ciphers.size(); i++) {
                                        Log.i(TAG, "解析array group_ciphers=" + group_ciphers + "group_ciphers.size()=" + group_ciphers.size());
                                    }
                                    encryption.setGroupCiphers(group_ciphers);
                                    reader.endArray();
                                } else if (reader.peek() == JsonToken.BEGIN_OBJECT) {
                                    reader.beginObject();
                                    group_ciphers.clear();
                                    if (reader.peek() == JsonToken.END_OBJECT) {
                                        reader.endObject();
                                        group_ciphers.add("");
                                        encryption.setGroupCiphers(group_ciphers);
                                        Log.i(TAG, "解析 group_ciphers=" + group_ciphers);
                                    } else {
                                        group_ciphers.add(reader.nextString());
                                        encryption.setGroupCiphers(group_ciphers);
                                        Log.i(TAG, "解析 group_ciphers=" + group_ciphers);
                                        reader.endObject();
                                    }
                                }
                            }
                        }
                        mWifiScanRoot.setencryption(encryption);
                        reader.endObject();
                    } else if (tagName.equals("quality_max")) {
                        mWifiScanRoot.setQuality_max(reader.nextInt());
                    } else if (tagName.equals("ssid")) {
                        mWifiScanRoot.setSsid(reader.nextString());
                    } else if (tagName.equals("channel")) {
                        mWifiScanRoot.setChannel(reader.nextInt());
                    } else if (tagName.equals("signal")) {
                        mWifiScanRoot.setSignal(reader.nextInt());
                    } else if (tagName.equals("bssid")) {
                        mWifiScanRoot.setBssid(reader.nextString());
                    } else if (tagName.equals("mode")) {
                        mWifiScanRoot.setMode(reader.nextString());
                    } else if (tagName.equals("quality")) {
                        mWifiScanRoot.setQuality(reader.nextInt());
                    }

                }
                listData.add(mWifiScanRoot);
                reader.endObject();
            }
            reader.endArray();
        } catch (Exception e) {
            e.printStackTrace();
            return listData;
        }
        return listData;
    }
}
