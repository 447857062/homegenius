package com.deplink.boruSmart.activity.device;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.deplink.boruSmart.Protocol.json.device.SmartDev;
import com.deplink.boruSmart.activity.device.adapter.AddDeviceTypeSelectAdapter;
import com.deplink.boruSmart.activity.device.doorbell.add.DoorBellConfigWifiTypeSelect;
import com.deplink.boruSmart.activity.device.smartSwitch.add.SelectSwitchTypeActivity;
import com.deplink.boruSmart.activity.personal.login.LoginActivity;
import com.deplink.boruSmart.constant.AppConstant;
import com.deplink.boruSmart.constant.DeviceTypeConstant;
import com.deplink.boruSmart.manager.device.doorbeel.DoorbeelManager;
import com.deplink.boruSmart.manager.device.remoteControl.RemoteControlManager;
import com.deplink.boruSmart.manager.device.smartlock.SmartLockManager;
import com.deplink.boruSmart.util.Perfence;
import com.deplink.boruSmart.util.qrcode.qrcodecapture.CaptureActivity;
import com.deplink.boruSmart.view.toast.Ftoast;
import com.deplink.sdk.android.sdk.DeplinkSDK;
import com.deplink.sdk.android.sdk.EventCallback;
import com.deplink.sdk.android.sdk.SDKAction;
import com.deplink.sdk.android.sdk.manager.SDKManager;

import java.util.ArrayList;
import java.util.List;

import deplink.com.smartwirelessrelay.homegenius.EllESDK.R;

/**
 * 扫码添加设备
 */
public class AddDeviceQRcodeActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, View.OnClickListener {
    private static final String TAG = "AddDeviceQRcodeActivity";
    private GridView mGridView;
    private AddDeviceTypeSelectAdapter mAdapter;
    private SmartLockManager mSmartLockManager;
    private ImageView imageview_scan_device;
    private FrameLayout image_back;
    private List<String> mDeviceTypes;
    private SDKManager manager;
    private EventCallback ec;
    private boolean isUserLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_device_qrcode);
        initViews();
        initDatas();
        initEvents();
        setToolBarReplaceActionBar();
    }

    private void initEvents() {
        imageview_scan_device.setOnClickListener(this);
        image_back.setOnClickListener(this);
    }

    private void initViews() {
        mGridView = (GridView) findViewById(R.id.gridview_add_device_type);
        imageview_scan_device = (ImageView) findViewById(R.id.imageview_scan_device);
        image_back = (FrameLayout) findViewById(R.id.image_back);
        imageview_return = (ImageView) findViewById(R.id.imageview_return);
        textview_add_device = (TextView) findViewById(R.id.textview_add_device);
    }

    private void initDatas() {
        mSmartLockManager = SmartLockManager.getInstance();
        mSmartLockManager.InitSmartLockManager(this);
        mDeviceTypes = new ArrayList<>();
        mDeviceTypes.add(DeviceTypeConstant.TYPE.TYPE_SMART_GETWAY);
        mDeviceTypes.add(DeviceTypeConstant.TYPE.TYPE_ROUTER);
        mDeviceTypes.add(DeviceTypeConstant.TYPE.TYPE_LOCK);
        mDeviceTypes.add(DeviceTypeConstant.TYPE.TYPE_REMOTECONTROL);
        mDeviceTypes.add(DeviceTypeConstant.TYPE.TYPE_SWITCH);
        mDeviceTypes.add(DeviceTypeConstant.TYPE.TYPE_TV_REMOTECONTROL);
        mDeviceTypes.add(DeviceTypeConstant.TYPE.TYPE_AIR_REMOTECONTROL);
        mDeviceTypes.add(DeviceTypeConstant.TYPE.TYPE_TVBOX_REMOTECONTROL);
        mDeviceTypes.add(DeviceTypeConstant.TYPE.TYPE_MENLING);
        mDeviceTypes.add(DeviceTypeConstant.TYPE.TYPE_LIGHT);
        mAdapter = new AddDeviceTypeSelectAdapter(this, mDeviceTypes);
        mGridView.setAdapter(mAdapter);
        mGridView.setOnItemClickListener(this);
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
                isUserLogin = false;
            }
        };
    }

    private ImageView imageview_return;
    private TextView textview_add_device;

    /**
     * 用toolBar替换ActionBar
     */
    private void setToolBarReplaceActionBar() {
        final Toolbar toolbar;
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        AppBarLayout app_bar_layout = (AppBarLayout) findViewById(R.id.app_bar_layout);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationIcon(null);
        app_bar_layout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (Math.abs(verticalOffset) >= 480) {
                    toolbar.setBackgroundColor(0xFFffffff);
                    textview_add_device.setTextColor(0xFF333333);
                    imageview_return.setImageDrawable(getResources().getDrawable(R.drawable.back_button));
                } else {
                    toolbar.setBackgroundResource(R.color.transparent);
                    textview_add_device.setTextColor(0xFFffffff);
                    imageview_return.setImageDrawable(getResources().getDrawable(R.drawable.white_return_button));

                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        isUserLogin = Perfence.getBooleanPerfence(AppConstant.USER_LOGIN);
        manager.addEventCallback(ec);
    }

    public Toolbar mToolbarTb;

    @Override
    protected void onTitleChanged(CharSequence title, int color) {
        super.onTitleChanged(title, color);
        if (mToolbarTb != null) {
            mToolbarTb.setTitle(title);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        manager.removeEventCallback(ec);
    }

    private String addDeviceType;

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.i(TAG, "onItemClick " + mDeviceTypes.get(position));
        if (isUserLogin) {
            Intent intentQrcodeSn = new Intent(AddDeviceQRcodeActivity.this, CaptureActivity.class);
            intentQrcodeSn.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intentQrcodeSn.putExtra("requestType", REQUEST_CODE_DEVICE_QRCODE);
            Intent intentEditDeviceMessage = new Intent(AddDeviceQRcodeActivity.this, AddDeviceNameActivity.class);
            List<SmartDev> mRemotecontrol = new ArrayList<>();
            switch (mDeviceTypes.get(position)) {
                case DeviceTypeConstant.TYPE.TYPE_SMART_GETWAY:
                    addDeviceType = DeviceTypeConstant.TYPE.TYPE_SMART_GETWAY;
                    startActivityForResult(intentQrcodeSn, REQUEST_ADD_GETWAY_OR_ROUTER);
                    break;
                case DeviceTypeConstant.TYPE.TYPE_REMOTECONTROL:
                    startActivityForResult(intentQrcodeSn, REQUEST_ADD_INFRAED_UNIVERSAL_RC);
                    break;
                case DeviceTypeConstant.TYPE.TYPE_AIR_REMOTECONTROL:
                    RemoteControlManager.getInstance().setCurrentActionIsAddDevice(true);
                    mRemotecontrol.addAll(RemoteControlManager.getInstance().findAllRemotecontrolDevice());
                    if (mRemotecontrol.size() == 0) {
                        Ftoast.create(this).setText("未添加智能遥控，无法添加设备").setDuration(Toast.LENGTH_SHORT).show();
                    } else {
                        RemoteControlManager.getInstance().setmSelectRemoteControlDevice(mRemotecontrol.get(0));
                        intentEditDeviceMessage.putExtra("DeviceType", DeviceTypeConstant.TYPE.TYPE_AIR_REMOTECONTROL);
                        startActivity(intentEditDeviceMessage);
                    }
                    break;
                case DeviceTypeConstant.TYPE.TYPE_ROUTER:
                    addDeviceType = DeviceTypeConstant.TYPE.TYPE_ROUTER;
                    startActivityForResult(intentQrcodeSn, REQUEST_ADD_GETWAY_OR_ROUTER);
                    break;
                case DeviceTypeConstant.TYPE.TYPE_TV_REMOTECONTROL:
                    RemoteControlManager.getInstance().setCurrentActionIsAddDevice(true);
                    mRemotecontrol.addAll(RemoteControlManager.getInstance().findAllRemotecontrolDevice());
                    if (mRemotecontrol.size() == 0) {
                        Ftoast.create(this).setText("未添加智能遥控，无法添加设备").setDuration(Toast.LENGTH_SHORT).show();
                    } else {
                        RemoteControlManager.getInstance().setmSelectRemoteControlDevice(mRemotecontrol.get(0));
                        intentEditDeviceMessage.putExtra("DeviceType", DeviceTypeConstant.TYPE.TYPE_TV_REMOTECONTROL);
                        startActivity(intentEditDeviceMessage);
                    }

                    break;
                case DeviceTypeConstant.TYPE.TYPE_TVBOX_REMOTECONTROL:
                    RemoteControlManager.getInstance().setCurrentActionIsAddDevice(true);
                    mRemotecontrol.addAll(RemoteControlManager.getInstance().findAllRemotecontrolDevice());
                    if (mRemotecontrol.size() == 0) {
                        Ftoast.create(this).setText("未添加智能遥控，无法添加设备").setDuration(Toast.LENGTH_SHORT).show();
                    } else {
                        RemoteControlManager.getInstance().setmSelectRemoteControlDevice(mRemotecontrol.get(0));
                        intentEditDeviceMessage.putExtra("DeviceType", DeviceTypeConstant.TYPE.TYPE_TVBOX_REMOTECONTROL);
                        startActivity(intentEditDeviceMessage);
                    }
                    break;
                case DeviceTypeConstant.TYPE.TYPE_SWITCH:
                    startActivity(new Intent(AddDeviceQRcodeActivity.this, SelectSwitchTypeActivity.class));
                    break;
                case DeviceTypeConstant.TYPE.TYPE_MENLING:
                    DoorbeelManager.getInstance().setConfigWifi(false);
                    startActivity(new Intent(AddDeviceQRcodeActivity.this, DoorBellConfigWifiTypeSelect.class));
                    break;
                default:
                    //智能门锁，等没有在case中的设备
                    startActivityForResult(intentQrcodeSn, REQUEST_CODE_DEVICE_QRCODE);
                    break;
            }
        } else {
            startActivity(new Intent(AddDeviceQRcodeActivity.this, LoginActivity.class));
        }
    }

    public final static int REQUEST_CODE_DEVICE_QRCODE = 1;
    /**
     * 红外万能遥控
     */
    public final static int REQUEST_ADD_INFRAED_UNIVERSAL_RC = 3;
    public final static int REQUEST_ADD_GETWAY_OR_ROUTER = 5;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imageview_scan_device:
                if (isUserLogin) {
                    Intent intentQrcodeSn = new Intent();
                    intentQrcodeSn.setClass(AddDeviceQRcodeActivity.this, CaptureActivity.class);
                    intentQrcodeSn.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intentQrcodeSn.putExtra("requestType", REQUEST_CODE_DEVICE_QRCODE);
                    startActivityForResult(intentQrcodeSn, REQUEST_CODE_DEVICE_QRCODE);
                } else {
                    startActivity(new Intent(AddDeviceQRcodeActivity.this, LoginActivity.class));
                }

                break;
            case R.id.image_back:
                onBackPressed();
                break;

        }
    }

    //智能门锁设备扫码返回 {"org":"ismart","tp":"SMART_LOCK","ad":"00-12-4b-00-0b-26-c2-15","ver":"1"}
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Intent intent = new Intent(AddDeviceQRcodeActivity.this, AddDeviceNameActivity.class);
        if (resultCode == RESULT_OK) {
            String qrCodeResult = data.getStringExtra("deviceSN");
            Log.i(TAG, "二维码扫码结果=" + qrCodeResult);
            switch (requestCode) {
                case REQUEST_CODE_DEVICE_QRCODE:
                    if (qrCodeResult.contains("SMART_LOCK")) {
                        intent.putExtra("currentAddDevice", qrCodeResult);
                        intent.putExtra("DeviceType", DeviceTypeConstant.TYPE.TYPE_LOCK);
                        startActivity(intent);
                    } else if (qrCodeResult.length() == 12) {//网关,路由器
                        intent.putExtra("currentAddDevice", qrCodeResult);
                        intent.putExtra("DeviceType", DeviceTypeConstant.TYPE.TYPE_SMART_GETWAY);
                        startActivity(intent);
                    } else if (qrCodeResult.contains("YWLIGHTCONTROL")) {
                        intent.putExtra("currentAddDevice", qrCodeResult);
                        intent.putExtra("DeviceType", DeviceTypeConstant.TYPE.TYPE_LIGHT);
                        startActivity(intent);
                    } else if (qrCodeResult.contains("IRMOTE_V2")) {
                        intent.putExtra("currentAddDevice", qrCodeResult);
                        intent.putExtra("DeviceType", "IRMOTE_V2");
                        startActivity(intent);
                    } else {
                        Ftoast.create(this).setText("不支持的设备").setDuration(Toast.LENGTH_SHORT).show();
                    }
                    break;
                case REQUEST_ADD_INFRAED_UNIVERSAL_RC:
                    //{"org":"ismart","tp":"IRMOTE_V2","ad":"00-12-4b-00-08-93-55-bb","ver":"1"}
                    intent.putExtra("currentAddDevice", qrCodeResult);
                    intent.putExtra("DeviceType", "IRMOTE_V2");
                    startActivity(intent);
                    break;
                case REQUEST_ADD_GETWAY_OR_ROUTER:
                    //添加路由器,网关
                    intent.putExtra("currentAddDevice", qrCodeResult);
                    if (addDeviceType != null) {
                        intent.putExtra("DeviceType", addDeviceType);
                    } else {
                        intent.putExtra("DeviceType", DeviceTypeConstant.TYPE.TYPE_GETWAY_OR_ROUTER);
                    }
                    startActivity(intent);
                    break;
            }
        }
    }
}
