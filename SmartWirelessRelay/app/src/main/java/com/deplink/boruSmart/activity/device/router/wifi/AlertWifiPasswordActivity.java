package com.deplink.boruSmart.activity.device.router.wifi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.deplink.boruSmart.constant.AppConstant;
import com.deplink.boruSmart.manager.connect.remote.HomeGenius;
import com.deplink.boruSmart.manager.device.DeviceManager;
import com.deplink.boruSmart.manager.device.router.RouterManager;
import com.deplink.boruSmart.util.Perfence;
import com.deplink.boruSmart.view.combinationwidget.TitleLayout;
import com.deplink.boruSmart.view.toast.Ftoast;
import com.deplink.sdk.android.sdk.DeplinkSDK;
import com.deplink.sdk.android.sdk.EventCallback;
import com.deplink.sdk.android.sdk.SDKAction;
import com.deplink.sdk.android.sdk.json.PERFORMANCE;
import com.deplink.sdk.android.sdk.json.Wifi;
import com.deplink.sdk.android.sdk.json.Wifi_2G;
import com.deplink.sdk.android.sdk.manager.SDKManager;
import com.google.gson.Gson;

import deplink.com.smartwirelessrelay.homegenius.EllESDK.R;

public class AlertWifiPasswordActivity extends Activity implements View.OnClickListener {
    private static final String TAG="AlertWifiPassword";
    private EditText edittext_password;
    private EditText edittext_password_again;
    private SDKManager manager;
    private EventCallback ec;
    private String wifiType;
    private ImageView image_input_eye_password;
    private FrameLayout framelayout_input_eye_password;
    private ImageView image_input_eye_password_again;
    private FrameLayout framelayout_nput_eye_password_again;
    private RouterManager mRouterManager;
    private HomeGenius mHomeGenius;
    private String channels;
    private TitleLayout layout_topbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert_wifi_password);
        initViews();
        initDatas();
        initEvents();

    }

    private void initDatas() {
        layout_topbar.setReturnClickListener(new TitleLayout.ReturnImageClickListener() {
            @Override
            public void onBackPressed() {
                AlertWifiPasswordActivity.this.onBackPressed();
            }
        });
        layout_topbar.setEditTextClickListener(new TitleLayout.EditTextClickListener() {
            @Override
            public void onEditTextPressed() {
                String password = edittext_password.getText().toString().trim();
                String passwordAgain = edittext_password_again.getText().toString().trim();
                if (password.length() < 8) {
                    Ftoast.create(AlertWifiPasswordActivity.this).setText("密码长度最小为8").show();
                    return;
                }
                if (!password.equals(passwordAgain)) {
                    Ftoast.create(AlertWifiPasswordActivity.this).setText("两次输入的密码不一致").show();
                    return;
                }
                if (isLogin && channels != null) {
                    isSetWifiPassword = true;
                    Wifi wifi = new Wifi();
                    switch (wifiType) {
                        case AppConstant.WIFISETTING.WIFI_TYPE_2G:
                            Wifi_2G wifi_2g = new Wifi_2G();
                            wifi_2g.setWifiPassword(password);
                            wifi.setWifi_2G(wifi_2g);
                            mHomeGenius.setWifi(wifi,channels);
                            break;
                        case AppConstant.WIFISETTING.WIFI_TYPE_VISITOR:
                            Intent intent = new Intent();
                            intent.putExtra("password", password);
                            setResult(RESULT_OK, intent);
                            break;
                    }
                    AlertWifiPasswordActivity.this.finish();
                } else {
                    Intent intent = new Intent();
                    intent.putExtra("password", password);
                    setResult(RESULT_OK, intent);
                    AlertWifiPasswordActivity.this.finish();

                }
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
            public void onFailure(SDKAction action, Throwable throwable) {

            }

            @Override
            public void deviceOpSuccess(String op, String deviceKey) {
                super.deviceOpSuccess(op, deviceKey);
            }

            @Override
            public void notifyHomeGeniusResponse(String result) {
                super.notifyHomeGeniusResponse(result);
                parseDeviceReport(result);
            }

            @Override
            public void connectionLost(Throwable throwable) {
                super.connectionLost(throwable);

            }
        };
        wifiType = getIntent().getStringExtra(AppConstant.WIFISETTING.WIFI_TYPE);
        String password = getIntent().getStringExtra(AppConstant.WIFISETTING.WIFI_PASSWORD);
        if (password != null && !password.equals("")) {
            edittext_password.setText(password);
            edittext_password.setSelection(password.length());
        }

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
            if (content.getResult().equalsIgnoreCase("OK")) {
                Log.i(TAG," mSDKCoordinator.notifyDeviceOpSuccess");
                if (isSetWifiPassword) {
                    Ftoast.create(AlertWifiPasswordActivity.this).setText("设置成功").show();
                }
            }
        }
    }
    private boolean isSetWifiPassword;

    private void initEvents() {
        framelayout_input_eye_password.setOnClickListener(this);
        framelayout_nput_eye_password_again.setOnClickListener(this);
    }

    private void initViews() {
        image_input_eye_password = (ImageView) findViewById(R.id.image_input_eye_password);
        framelayout_input_eye_password = (FrameLayout) findViewById(R.id.framelayout_input_eye_password);
        image_input_eye_password_again = (ImageView) findViewById(R.id.image_input_eye_password_again);
        framelayout_nput_eye_password_again = (FrameLayout) findViewById(R.id.framelayout_nput_eye_password_again);
        edittext_password = (EditText) findViewById(R.id.edittext_password);
        edittext_password_again = (EditText) findViewById(R.id.edittext_password_again);
        layout_topbar= (TitleLayout) findViewById(R.id.layout_topbar);
    }
    private boolean isLogin;
    @Override
    protected void onResume() {
        super.onResume();
        if(! DeviceManager.getInstance().isStartFromExperience()){
            manager.addEventCallback(ec);
            isLogin = Perfence.getBooleanPerfence(AppConstant.USER_LOGIN);
            mHomeGenius = new HomeGenius();
            channels = mRouterManager.getCurrentSelectedRouter().getRouter().getChannels();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        manager.removeEventCallback(ec);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_cancel:
                onBackPressed();
                break;
            case R.id.framelayout_input_eye_password:
                if (edittext_password.getTransformationMethod() instanceof PasswordTransformationMethod) {
                    image_input_eye_password.setImageResource(R.drawable.blueeye);
                    edittext_password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());

                } else if (edittext_password.getTransformationMethod() instanceof HideReturnsTransformationMethod) {
                    image_input_eye_password.setImageResource(R.drawable.grayeye);
                    edittext_password.setTransformationMethod(PasswordTransformationMethod.getInstance());

                }
                if (edittext_password.getText().toString().trim().length() > 0) {
                    edittext_password.setSelection(edittext_password.getText().toString().trim().length());
                }
                break;
            case R.id.framelayout_nput_eye_password_again:
                if (edittext_password_again.getTransformationMethod() instanceof PasswordTransformationMethod) {
                    image_input_eye_password_again.setImageResource(R.drawable.blueeye);
                    edittext_password_again.setTransformationMethod(HideReturnsTransformationMethod.getInstance());

                } else if (edittext_password_again.getTransformationMethod() instanceof HideReturnsTransformationMethod) {
                    image_input_eye_password_again.setImageResource(R.drawable.grayeye);
                    edittext_password_again.setTransformationMethod(PasswordTransformationMethod.getInstance());

                }
                if (edittext_password_again.getText().toString().trim().length() > 0) {
                    edittext_password_again.setSelection(edittext_password_again.getText().toString().trim().length());
                }
                break;

        }
    }
}
