package com.deplink.boruSmart.activity.device.router.wifi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.deplink.boruSmart.activity.personal.login.LoginActivity;
import com.deplink.boruSmart.constant.AppConstant;
import com.deplink.boruSmart.util.Perfence;
import com.deplink.boruSmart.view.dialog.AlertDialog;
import com.deplink.boruSmart.view.toast.ToastSingleShow;
import com.deplink.boruSmart.view.combinationwidget.TitleLayout;
import com.deplink.sdk.android.sdk.DeplinkSDK;
import com.deplink.sdk.android.sdk.EventCallback;
import com.deplink.sdk.android.sdk.SDKAction;
import com.deplink.sdk.android.sdk.device.router.RouterDevice;
import com.deplink.sdk.android.sdk.manager.SDKManager;

import deplink.com.smartwirelessrelay.homegenius.EllESDK.R;

public class EncryptTypeActivity extends Activity implements View.OnClickListener{
    private static final String TAG="EncryptTypeActivity";
    private RelativeLayout layout_type_strong;
    private RelativeLayout layout_type_mix;
    private RelativeLayout layout_type_none;
    private ImageView imageview_strong;
    private ImageView imageview_mix;
    private ImageView imageview_none;
    private String currentSelectEncryptType;
    private String wifiType;
    private SDKManager manager;
    private EventCallback ec;
    private TitleLayout layout_title;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_encrypt_type);
        initViews();
        initDatas();
        initEvents();
    }

    private void initDatas() {
        layout_title.setReturnClickListener(new TitleLayout.ReturnImageClickListener() {
            @Override
            public void onBackPressed() {
                EncryptTypeActivity.this.onBackPressed();
            }
        });
        layout_title.setEditTextClickListener(new TitleLayout.EditTextClickListener() {
            @Override
            public void onEditTextPressed() {
                if (!currentSelectEncryptType.equals("")) {
                    isSetEncrypt = true;
                    // Wifi wifi = new Wifi();
                    Intent intent = new Intent();
                    try {
                        switch (wifiType) {
                            case AppConstant.WIFISETTING.WIFI_TYPE_2G:

                                intent.putExtra("encryptionType", currentSelectEncryptType);
                                setResult(RESULT_OK, intent);
                                break;
                            case AppConstant.WIFISETTING.WIFI_TYPE_VISITOR:
                                intent.putExtra("encryptionType", currentSelectEncryptType);
                                setResult(RESULT_OK, intent);
                                break;
                        }
                        EncryptTypeActivity.this.finish();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

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
                switch (op) {
                    case RouterDevice.OP_SUCCESS:
                        if (isSetEncrypt) {
                            ToastSingleShow.showText(EncryptTypeActivity.this, "设置成功");
                        }
                        break;
                }
            }

            @Override
            public void connectionLost(Throwable throwable) {
                super.connectionLost(throwable);
                Perfence.setPerfence(AppConstant.USER_LOGIN, false);
                new AlertDialog(EncryptTypeActivity.this).builder().setTitle("账号异地登录")
                        .setMsg("当前账号已在其它设备上登录,是否重新登录")
                        .setPositiveButton("确认", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                startActivity(new Intent(EncryptTypeActivity.this, LoginActivity.class));
                            }
                        }).setNegativeButton("取消", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                }).show();
            }
        };
        currentSelectEncryptType = getIntent().getStringExtra(AppConstant.WIFISETTING.WIFI_ENCRYPT_TYPE);
        wifiType = getIntent().getStringExtra(AppConstant.WIFISETTING.WIFI_TYPE);
        if (currentSelectEncryptType != null) {
            switch (currentSelectEncryptType) {
                case "psk2":
                    setCurrentEncrytype(R.id.layout_type_strong);
                    break;
                case "psk-mixed":
                    setCurrentEncrytype(R.id.layout_type_mix);
                    break;
                case "none":
                    setCurrentEncrytype(R.id.layout_type_none);
                    break;
            }
        }
    }


    private boolean isSetEncrypt;

    @Override
    protected void onResume() {
        super.onResume();
        manager.addEventCallback(ec);
    }

    @Override
    protected void onPause() {
        super.onPause();
        manager.removeEventCallback(ec);
    }

    private void initEvents() {
        layout_type_strong.setOnClickListener(this);
        layout_type_mix.setOnClickListener(this);
        layout_type_none.setOnClickListener(this);

    }

    private void initViews() {
        layout_type_strong = (RelativeLayout) findViewById(R.id.layout_type_strong);
        layout_type_mix = (RelativeLayout) findViewById(R.id.layout_type_mix);
        layout_type_none = (RelativeLayout) findViewById(R.id.layout_type_none);
        imageview_strong = (ImageView) findViewById(R.id.imageview_strong);
        imageview_mix = (ImageView) findViewById(R.id.imageview_mix);
        imageview_none = (ImageView) findViewById(R.id.imageview_none);
        layout_title= (TitleLayout) findViewById(R.id.layout_title);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.layout_type_strong:
                setCurrentEncrytype(R.id.layout_type_strong);
                break;
            case R.id.layout_type_mix:
                setCurrentEncrytype(R.id.layout_type_mix);
                break;
            case R.id.layout_type_none:
                setCurrentEncrytype(R.id.layout_type_none);
                break;

        }
    }

    private void setCurrentEncrytype(int id) {
        switch (id) {
            case R.id.layout_type_none:
                imageview_strong.setImageLevel(0);
                imageview_mix.setImageLevel(0);
                imageview_none.setImageLevel(1);
                currentSelectEncryptType = "none";
                break;
            case R.id.layout_type_mix:
                imageview_strong.setImageLevel(0);
                imageview_mix.setImageLevel(1);
                imageview_none.setImageLevel(0);
                currentSelectEncryptType = "psk-mixed";
                break;
            case R.id.layout_type_strong:
                imageview_strong.setImageLevel(1);
                imageview_mix.setImageLevel(0);
                imageview_none.setImageLevel(0);
                currentSelectEncryptType = "psk2";
                break;
        }

    }

}
