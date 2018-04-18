package com.deplink.boruSmart.activity.device.router;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.deplink.boruSmart.Protocol.json.Room;
import com.deplink.boruSmart.Protocol.json.device.SmartDev;
import com.deplink.boruSmart.activity.device.DevicesActivity;
import com.deplink.boruSmart.activity.device.SelectRommActivity;
import com.deplink.boruSmart.activity.device.ShareDeviceActivity;
import com.deplink.boruSmart.activity.device.router.connectType.DialConnectActivity;
import com.deplink.boruSmart.activity.device.router.connectType.StaticConnectActivity;
import com.deplink.boruSmart.activity.device.router.connectType.WirelessRelayActivity;
import com.deplink.boruSmart.activity.device.router.firmwareupdate.FirmwareUpdateActivity;
import com.deplink.boruSmart.activity.device.router.lan.LanSettingActivity;
import com.deplink.boruSmart.activity.device.router.qos.QosSettingActivity;
import com.deplink.boruSmart.activity.device.router.wifi.WiFiSettingActivity;
import com.deplink.boruSmart.activity.device.router.wifi.WifiSetting24;
import com.deplink.boruSmart.activity.personal.experienceCenter.ExperienceDevicesActivity;
import com.deplink.boruSmart.activity.personal.login.LoginActivity;
import com.deplink.boruSmart.constant.AppConstant;
import com.deplink.boruSmart.constant.DeviceTypeConstant;
import com.deplink.boruSmart.manager.connect.remote.HomeGenius;
import com.deplink.boruSmart.manager.device.DeviceListener;
import com.deplink.boruSmart.manager.device.DeviceManager;
import com.deplink.boruSmart.manager.device.router.RouterManager;
import com.deplink.boruSmart.manager.room.RoomManager;
import com.deplink.boruSmart.util.NetUtil;
import com.deplink.boruSmart.util.Perfence;
import com.deplink.boruSmart.util.WeakRefHandler;
import com.deplink.boruSmart.view.combinationwidget.TitleLayout;
import com.deplink.boruSmart.view.dialog.ActionSheetDialog;
import com.deplink.boruSmart.view.dialog.AlertDialog;
import com.deplink.boruSmart.view.dialog.loadingdialog.DialogThreeBounce;
import com.deplink.boruSmart.view.toast.Ftoast;
import com.deplink.sdk.android.sdk.DeplinkSDK;
import com.deplink.sdk.android.sdk.EventCallback;
import com.deplink.sdk.android.sdk.SDKAction;
import com.deplink.sdk.android.sdk.device.router.RouterDevice;
import com.deplink.sdk.android.sdk.homegenius.DeviceOperationResponse;
import com.deplink.sdk.android.sdk.manager.SDKManager;
import com.deplink.sdk.android.sdk.rest.ErrorResponse;
import com.deplink.sdk.android.sdk.rest.RestfulToolsRouter;
import com.deplink.sdk.android.sdk.rest.RouterResponse;
import com.google.gson.Gson;

import org.litepal.crud.DataSupport;

import java.util.List;

import deplink.com.smartwirelessrelay.homegenius.EllESDK.R;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RouterSettingActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "RouterSettingActivity";
    private RelativeLayout layout_router_name_out;
    private RelativeLayout layout_room_select_out;
    private RelativeLayout layout_connect_type_select_out;
    private RelativeLayout layout_wifi_setting_out;
    private RelativeLayout layout_reboot_out;
    private TextView buttton_delete_router;
    private RouterManager mRouterManager;
    private TextView textview_room_select_2;
    private TextView textview_route_name_2;
    private boolean isUserLogin;
    private SDKManager manager;
    private EventCallback ec;
    private RelativeLayout layout_lan_setting_out;
    private RelativeLayout layout_update_out;
    private RelativeLayout layout_QOS_setting_out;
    private DeviceManager mDeviceManager;
    private DeviceListener mDeviceListener;
    private HomeGenius mHomeGenius;
    private String channels;
    private boolean isStartFromExperience;
    private RelativeLayout layout_device_share;
    private TitleLayout layout_title;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_router_setting);
        initViews();
        initDatas();
        initEvents();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isStartFromExperience = mDeviceManager.isStartFromExperience();
        if (!isStartFromExperience) {
            textview_route_name_2.setText(mRouterManager.getCurrentSelectedRouter().getName());
            if (mRouterManager.getCurrentSelectedRouter().getStatus().equals("离线")) {
                layout_lan_setting_out.setVisibility(View.GONE);
                layout_update_out.setVisibility(View.GONE);
                layout_QOS_setting_out.setVisibility(View.GONE);
            }
            List<Room> rooms = mRouterManager.getRouterAtRooms();
            if (rooms.size() == 1) {
                textview_room_select_2.setText(rooms.get(0).getRoomName());
            } else {
                textview_room_select_2.setText("全部");
            }
            manager.addEventCallback(ec);
            mDeviceManager.addDeviceListener(mDeviceListener);
            isUserLogin = Perfence.getBooleanPerfence(AppConstant.USER_LOGIN);
            try {
                channels = mRouterManager.getCurrentSelectedRouter().getRouter().getChannels();
            } catch (Exception e) {
                e.printStackTrace();
            }
            deviceUid=mRouterManager.getCurrentSelectedRouter().getUid();
        } else {
            if(textview_route_name_2.getText().toString().equalsIgnoreCase("")){
                textview_route_name_2.setText("体验路由器");
            }
        }
        mHomeGenius = new HomeGenius();
    }
    private void initDatas() {
        layout_title.setReturnClickListener(new TitleLayout.ReturnImageClickListener() {
            @Override
            public void onBackPressed() {
                RouterSettingActivity.this.onBackPressed();
            }
        });
        mRouterManager = RouterManager.getInstance();
        mRouterManager.InitRouterManager();
        mDeviceManager = DeviceManager.getInstance();
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
            public void deviceOpSuccess(String op, String deviceKey) {
                super.deviceOpSuccess(op, deviceKey);
                switch (op) {
                    case RouterDevice.OP_REBOOT:
                        Ftoast.create(RouterSettingActivity.this).setText( "重启设备成功").show();
                        break;
                }
            }
            @Override
            public void onFailure(SDKAction action, Throwable throwable) {
            }

            @Override
            public void connectionLost(Throwable throwable) {
                super.connectionLost(throwable);
                Perfence.setPerfence(AppConstant.USER_LOGIN, false);
                isUserLogin = false;
                new AlertDialog(RouterSettingActivity.this).builder().setTitle("账号异地登录")
                        .setMsg("当前账号已在其它设备上登录,是否重新登录")
                        .setPositiveButton("确认", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                startActivity(new Intent(RouterSettingActivity.this, LoginActivity.class));
                            }
                        }).setNegativeButton("取消", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                }).show();
            }
        };
        mDeviceListener = new DeviceListener() {
            @Override
            public void responseAlertDeviceHttpResult(DeviceOperationResponse result) {
                super.responseAlertDeviceHttpResult(result);
                if (action.equals("alertroom")) {
                    String deviceName = mRouterManager.getCurrentSelectedRouter().getName();
                    boolean saveResult = mRouterManager.updateDeviceInWhatRoom(room, deviceUid, deviceName);
                    if (saveResult) {
                        textview_room_select_2.setText(room.getRoomName());
                    } else {
                        Message msg = Message.obtain();
                        msg.what = MSG_UPDATE_ROOM_FAIL;
                        mHandler.sendMessage(msg);
                        action = "";
                    }
                }
            }

            @Override
            public void responseDeleteDeviceHttpResult(DeviceOperationResponse result) {
                super.responseDeleteDeviceHttpResult(result);
                int affectColumn = DataSupport.deleteAll(SmartDev.class, "Uid = ?", mRouterManager.getCurrentSelectedRouter().getUid());
                Log.i(TAG, "删除路由器设备=" + affectColumn);
                Ftoast.create(RouterSettingActivity.this).setText( "解除绑定成功").show();
                RouterSettingActivity.this.startActivity(new Intent(RouterSettingActivity.this, DevicesActivity.class));
            }
        };
        Log.i(TAG,"roomName update="+getIntent().getBooleanExtra("isupdateroom",false));
        if(getIntent().getBooleanExtra("isupdateroom",false)){
            String roomName = getIntent().getStringExtra("roomName");
            if (!mDeviceManager.isStartFromExperience()) {
                room = RoomManager.getInstance().findRoom(roomName, true);
                deviceUid = mRouterManager.getCurrentSelectedRouter().getUid();
                action = "alertroom";
                mDeviceManager.alertDeviceHttp(deviceUid, room.getUid(), null, null);
            }
            Log.i(TAG,"roomName="+roomName);
            textview_room_select_2.setText(roomName);
        }
        if(getIntent().getBooleanExtra("isupdaterouter",false)){
            String routerName = getIntent().getStringExtra("routerName");
            textview_route_name_2.setText(routerName);
        }
    }
    private void initEvents() {
        layout_router_name_out.setOnClickListener(this);
        layout_room_select_out.setOnClickListener(this);
        layout_connect_type_select_out.setOnClickListener(this);
        layout_wifi_setting_out.setOnClickListener(this);
        layout_lan_setting_out.setOnClickListener(this);
        layout_QOS_setting_out.setOnClickListener(this);
        layout_update_out.setOnClickListener(this);
        layout_reboot_out.setOnClickListener(this);
        buttton_delete_router.setOnClickListener(this);
        layout_device_share.setOnClickListener(this);
    }

    private void initViews() {
        layout_device_share = findViewById(R.id.layout_device_share);
        buttton_delete_router = findViewById(R.id.buttton_delete_router);
        layout_router_name_out = findViewById(R.id.layout_router_name_out);
        layout_room_select_out = findViewById(R.id.layout_room_select_out);
        layout_connect_type_select_out = findViewById(R.id.layout_connect_type_select_out);
        layout_wifi_setting_out = findViewById(R.id.layout_wifi_setting_out);
        layout_lan_setting_out = findViewById(R.id.layout_lan_setting_out);
        layout_QOS_setting_out = findViewById(R.id.layout_QOS_setting_out);
        layout_update_out = findViewById(R.id.layout_update_out);
        layout_reboot_out = findViewById(R.id.layout_reboot_out);
        textview_room_select_2 = findViewById(R.id.textview_room_select_2);
        textview_route_name_2 = findViewById(R.id.textview_route_name_2);
        layout_title= findViewById(R.id.layout_title);
    }

    private String action;
    private Room room;
    private String deviceUid;
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.layout_device_share:
                Intent inentShareDevice = new Intent(this, ShareDeviceActivity.class);
                inentShareDevice.putExtra("devicetype", DeviceTypeConstant.TYPE.TYPE_ROUTER);
                if(isStartFromExperience){
                    startActivity(inentShareDevice);
                }else{
                    if(isUserLogin){
                        if (deviceUid != null) {
                            inentShareDevice.putExtra("deviceuid", deviceUid);
                            startActivity(inentShareDevice);
                        }
                    }else{
                        startActivity(new Intent(RouterSettingActivity.this, LoginActivity.class));
                    }

                }
                break;
            case R.id.layout_router_name_out:

                startActivity(new Intent(this, RouterNameUpdateActivity.class));
                break;
            case R.id.layout_room_select_out:
                if(isStartFromExperience){
                    Intent intent = new Intent(this, SelectRommActivity.class);
                    DeviceManager.getInstance().setEditDevice(true);
                    DeviceManager.getInstance().setCurrentEditDeviceType(DeviceTypeConstant.TYPE.TYPE_ROUTER);
                    startActivity(intent);
                }else{
                    if(isUserLogin){
                        Intent intent = new Intent(this, SelectRommActivity.class);
                        DeviceManager.getInstance().setEditDevice(true);
                        DeviceManager.getInstance().setCurrentEditDeviceType(DeviceTypeConstant.TYPE.TYPE_ROUTER);
                        startActivity(intent);
                    }else{
                        startActivity(new Intent(RouterSettingActivity.this, LoginActivity.class));
                    }
                }

                break;
            case R.id.layout_connect_type_select_out:
                if (DeviceManager.getInstance().isStartFromExperience()) {
                    new ActionSheetDialog(RouterSettingActivity.this)
                            .builder()
                            .setCancelable(true)
                            .setCanceledOnTouchOutside(true)
                            .addSheetItem("宽带拨号", ActionSheetDialog.SheetItemColor.GRAY,
                                    new ActionSheetDialog.OnSheetItemClickListener() {
                                        @Override
                                        public void onClick(int which) {
                                            Intent dialConnectIntent=new Intent(RouterSettingActivity.this,DialConnectActivity.class);
                                            dialConnectIntent.putExtra(AppConstant.OPERATION_TYPE,AppConstant.OPERATION_TYPE_LOCAL);
                                            RouterSettingActivity.this.startActivity(dialConnectIntent);
                                        }
                                    })
                            .addSheetItem("动态IP", ActionSheetDialog.SheetItemColor.GRAY,
                                    new ActionSheetDialog.OnSheetItemClickListener() {
                                        @Override
                                        public void onClick(int which) {
                                            selectConnectType();
                                        }
                                    })
                            .addSheetItem("静态IP", ActionSheetDialog.SheetItemColor.GRAY,
                                    new ActionSheetDialog.OnSheetItemClickListener() {
                                        @Override
                                        public void onClick(int which) {
                                            Intent staticIpIntent=new Intent(RouterSettingActivity.this,StaticConnectActivity.class);
                                            staticIpIntent.putExtra(AppConstant.OPERATION_TYPE,AppConstant.OPERATION_TYPE_LOCAL);
                                            RouterSettingActivity.this.startActivity(staticIpIntent);
                                        }
                                    })
                            .addSheetItem("无线中继", ActionSheetDialog.SheetItemColor.GRAY,
                                    new ActionSheetDialog.OnSheetItemClickListener() {
                                        @Override
                                        public void onClick(int which) {
                                            Intent wirelessRelayIntent=new Intent(RouterSettingActivity.this,WirelessRelayActivity.class);
                                            wirelessRelayIntent.putExtra(AppConstant.OPERATION_TYPE,AppConstant.OPERATION_TYPE_LOCAL);
                                            RouterSettingActivity.this.startActivity(wirelessRelayIntent);
                                        }
                                    })
                            .show();
                } else {
                    if (mRouterManager.getCurrentSelectedRouter().getStatus().equals("在线")) {
                        startActivity(new Intent(this, ConnectSettingActivity.class));
                    } else {
                        new ActionSheetDialog(RouterSettingActivity.this)
                                .builder()
                                .setCancelable(true)
                                .setCanceledOnTouchOutside(true)
                                .addSheetItem("宽带拨号", ActionSheetDialog.SheetItemColor.GRAY,
                                        new ActionSheetDialog.OnSheetItemClickListener() {
                                            @Override
                                            public void onClick(int which) {
                                                Intent dialConnectIntent=new Intent(RouterSettingActivity.this,DialConnectActivity.class);
                                                dialConnectIntent.putExtra(AppConstant.OPERATION_TYPE,AppConstant.OPERATION_TYPE_LOCAL);
                                                RouterSettingActivity.this.startActivity(dialConnectIntent);
                                            }
                                        })
                                .addSheetItem("动态IP", ActionSheetDialog.SheetItemColor.GRAY,
                                        new ActionSheetDialog.OnSheetItemClickListener() {
                                            @Override
                                            public void onClick(int which) {
                                                selectConnectType();
                                            }
                                        })
                                .addSheetItem("静态IP", ActionSheetDialog.SheetItemColor.GRAY,
                                        new ActionSheetDialog.OnSheetItemClickListener() {
                                            @Override
                                            public void onClick(int which) {
                                                Intent staticIpIntent=new Intent(RouterSettingActivity.this,StaticConnectActivity.class);
                                                staticIpIntent.putExtra(AppConstant.OPERATION_TYPE,AppConstant.OPERATION_TYPE_LOCAL);
                                                RouterSettingActivity.this.startActivity(staticIpIntent);
                                            }
                                        })
                                .addSheetItem("无线中继", ActionSheetDialog.SheetItemColor.GRAY,
                                        new ActionSheetDialog.OnSheetItemClickListener() {
                                            @Override
                                            public void onClick(int which) {
                                                Intent wirelessRelayIntent=new Intent(RouterSettingActivity.this,WirelessRelayActivity.class);
                                                wirelessRelayIntent.putExtra(AppConstant.OPERATION_TYPE,AppConstant.OPERATION_TYPE_LOCAL);
                                                RouterSettingActivity.this.startActivity(wirelessRelayIntent);
                                            }
                                        })
                                .show();
                    }
                }
                break;
            case R.id.layout_wifi_setting_out:
                startActivity(new Intent(this, WiFiSettingActivity.class));
                break;
            case R.id.layout_lan_setting_out:
                startActivity(new Intent(this, LanSettingActivity.class));
                break;
            case R.id.layout_QOS_setting_out:
                startActivity(new Intent(this, QosSettingActivity.class));
                break;
            case R.id.layout_update_out:
                startActivity(new Intent(this, FirmwareUpdateActivity.class));
                break;
            case R.id.layout_reboot_out:
                new AlertDialog(RouterSettingActivity.this).builder().setTitle("重启设备")
                        .setMsg("确定立即重启?")
                        .setPositiveButton("确认", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Ftoast.create(RouterSettingActivity.this).setText( "已重启设备").show();
                                if (!NetUtil.isNetAvailable(RouterSettingActivity.this)) {
                                    Ftoast.create(RouterSettingActivity.this).setText( "网络连接已断开").show();
                                } else {
                                    if (isUserLogin && mRouterManager.getCurrentSelectedRouter().getStatus().equals("在线")) {
                                        if(channels!=null){
                                            mHomeGenius.reboot(channels);
                                        }
                                    } else {
                                        RebootLocal();
                                    }
                                }
                            }
                        }).setNegativeButton("取消", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                }).show();

                break;
            case R.id.buttton_delete_router:
                new AlertDialog(RouterSettingActivity.this).builder().setTitle("删除设备")
                        .setMsg("确认删除设备")
                        .setPositiveButton("确认", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (DeviceManager.getInstance().isStartFromExperience()) {
                                    startActivity(new Intent(RouterSettingActivity.this, ExperienceDevicesActivity.class));
                                } else {
                                    if (NetUtil.isNetAvailable(RouterSettingActivity.this)) {
                                        if (isUserLogin) {
                                            DialogThreeBounce.showLoading(RouterSettingActivity.this);
                                            mDeviceManager.deleteDeviceHttp();
                                        } else {
                                            Ftoast.create(RouterSettingActivity.this).setText( "用户已离线，登录后使用").show();
                                        }

                                    } else {
                                        Ftoast.create(RouterSettingActivity.this).setText( "网络连接不可用").show();
                                    }
                                }
                            }
                        }).setNegativeButton("取消", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                }).show();
                break;
        }
    }

    /**
     * 重启，使用本地接口
     */
    private void RebootLocal() {
        RestfulToolsRouter.getSingleton(RouterSettingActivity.this).rebootRouter(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                Log.i(TAG, "response.code=" + response.code());
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {

            }
        });
        startActivity(new Intent(RouterSettingActivity.this, DevicesActivity.class));
    }

    /**
     * （成功连接本地路由器后）选择上网方式
     */
    private void selectConnectType() {
        if(mDeviceManager.isStartFromExperience()){
            Ftoast.create(RouterSettingActivity.this).setText( "动态IP设置成功，请设置wifi名字密码").show();
            Intent intentWifiSetting = new Intent(RouterSettingActivity.this, WifiSetting24.class);
            intentWifiSetting.putExtra(AppConstant.OPERATION_TYPE, AppConstant.OPERATION_TYPE_LOCAL);
            startActivity(intentWifiSetting);
        }else{
            RestfulToolsRouter.getSingleton(RouterSettingActivity.this).dynamicIp(new Callback<RouterResponse>() {
                @Override
                public void onResponse(Call<RouterResponse> call, Response<RouterResponse> response) {
                    int code = response.code();
                    if (code != 200) {
                        String errorMsg = "";
                        try {
                            String text = response.errorBody().string();
                            Gson gson = new Gson();
                            ErrorResponse errorResponse;
                            errorResponse = gson.fromJson(text, ErrorResponse.class);
                            switch (errorResponse.getErrcode()) {
                                case AppConstant.ERROR_CODE.OP_ERRCODE_BAD_TOKEN:
                                    text = AppConstant.ERROR_MSG.OP_ERRCODE_BAD_TOKEN;
                                    Ftoast.create(RouterSettingActivity.this).setText( "登录已失效").show();
                                    startActivity(new Intent(RouterSettingActivity.this, LoginActivity.class));
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
                        Ftoast.create(RouterSettingActivity.this).setText( "errorMsg").show();
                    } else {
                        Ftoast.create(RouterSettingActivity.this).setText( "动态IP设置成功，请设置wifi名字密码").show();
                        Intent intentWifiSetting = new Intent(RouterSettingActivity.this, WifiSetting24.class);
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

    private static final int MSG_DELETE_ROUTER_FAIL = 100;
    /**
     * 更新路由器所在房间失败
     */
    private static final int MSG_UPDATE_ROOM_FAIL = 101;
    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_DELETE_ROUTER_FAIL:
                    Toast.makeText(RouterSettingActivity.this, "删除路由器失败", Toast.LENGTH_SHORT).show();
                    break;
                case MSG_UPDATE_ROOM_FAIL:
                    Toast.makeText(RouterSettingActivity.this, "更新路由器所在房间失败", Toast.LENGTH_SHORT).show();
                    break;
            }
            return true;
        }
    };
    private Handler mHandler = new WeakRefHandler(mCallback);
    @Override
    protected void onPause() {
        super.onPause();
        if (!isStartFromExperience) {
            mDeviceManager.removeDeviceListener(mDeviceListener);
            manager.removeEventCallback(ec);
        }

    }
}
