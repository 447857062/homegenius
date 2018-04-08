package com.deplink.boruSmart.activity.device.router.wifi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.deplink.boruSmart.activity.personal.login.LoginActivity;
import com.deplink.boruSmart.util.Perfence;
import com.deplink.boruSmart.constant.AppConstant;
import com.deplink.boruSmart.view.combinationwidget.TitleLayout;
import com.deplink.boruSmart.view.dialog.AlertDialog;
import com.deplink.boruSmart.view.toast.ToastSingleShow;
import com.deplink.sdk.android.sdk.DeplinkSDK;
import com.deplink.sdk.android.sdk.EventCallback;
import com.deplink.sdk.android.sdk.SDKAction;
import com.deplink.sdk.android.sdk.device.router.RouterDevice;
import com.deplink.sdk.android.sdk.manager.SDKManager;

import deplink.com.smartwirelessrelay.homegenius.EllESDK.R;

public class BandwidthActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "BandwidthActivity";

    private RelativeLayout layout_bandwidth_20;
    private RelativeLayout layout_bandwidth_40;
    private ImageView imageview_bandwidth_20;
    private ImageView imageview_bandwidth_40;
    private String currentBandwidth;
    private SDKManager manager;
    private EventCallback ec;
    private TitleLayout layout_title;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bandwidth);

        initViews();
        initDatas();
        initEvents();


    }

    private void initDatas() {
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
                        if (isSetBandWidth) {
                            ToastSingleShow.showText(BandwidthActivity.this, "设置成功");
                        }
                        break;
                }
            }

            @Override
            public void connectionLost(Throwable throwable) {
                super.connectionLost(throwable);
                Perfence.setPerfence(AppConstant.USER_LOGIN, false);
                new AlertDialog(BandwidthActivity.this).builder().setTitle("账号异地登录")
                        .setMsg("当前账号已在其它设备上登录,是否重新登录")
                        .setPositiveButton("确认", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                startActivity(new Intent(BandwidthActivity.this, LoginActivity.class));
                            }
                        }).setNegativeButton("取消", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                }).show();
            }
        };
        currentBandwidth = getIntent().getStringExtra(AppConstant.WIFISETTING.WIFI_BANDWIDTH);
        if (currentBandwidth != null) {
            switch (currentBandwidth) {
                case "0":
                    Log.i(TAG, "currentBandwidth=" + currentBandwidth);
                    setCurrentBandWidth(R.id.layout_bandwidth_20);
                    break;
                case "1":
                    setCurrentBandWidth(R.id.layout_bandwidth_40);
                    break;


            }
        }
        layout_title.setReturnClickListener(new TitleLayout.ReturnImageClickListener() {
            @Override
            public void onBackPressed() {
                BandwidthActivity.this.onBackPressed();
            }
        });
        layout_title.setEditTextClickListener(new TitleLayout.EditTextClickListener() {
            @Override
            public void onEditTextPressed() {
                if (!currentBandwidth.equals("")) {
                    Intent intent = new Intent();
                    intent.putExtra("bandwidth", currentBandwidth);
                    setResult(RESULT_OK, intent);
                    BandwidthActivity.this.finish();
                }
            }
        });
    }

    private boolean isSetBandWidth;

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
        layout_bandwidth_20.setOnClickListener(this);
        layout_bandwidth_40.setOnClickListener(this);
    }

    private void initViews() {
        layout_title= (TitleLayout) findViewById(R.id.layout_title);
        layout_bandwidth_20 = (RelativeLayout) findViewById(R.id.layout_bandwidth_20);
        layout_bandwidth_40 = (RelativeLayout) findViewById(R.id.layout_bandwidth_40);
        imageview_bandwidth_20 = (ImageView) findViewById(R.id.imageview_bandwidth_20);
        imageview_bandwidth_40 = (ImageView) findViewById(R.id.imageview_bandwidth_40);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.layout_bandwidth_20:

                setCurrentBandWidth(R.id.layout_bandwidth_20);
                break;
            case R.id.layout_bandwidth_40:
                setCurrentBandWidth(R.id.layout_bandwidth_40);

                break;
        }
    }

    private void setCurrentBandWidth(int id) {
        switch (id) {
            case R.id.layout_bandwidth_20:
                imageview_bandwidth_20.setImageLevel(1);
                imageview_bandwidth_40.setImageLevel(0);
                currentBandwidth = "0";
                break;
            case R.id.layout_bandwidth_40:
                imageview_bandwidth_20.setImageLevel(0);
                imageview_bandwidth_40.setImageLevel(1);
                currentBandwidth = "1";
                break;
        }

    }
}
