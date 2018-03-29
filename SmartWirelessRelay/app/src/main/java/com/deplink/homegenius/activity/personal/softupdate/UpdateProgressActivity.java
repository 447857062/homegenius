package com.deplink.homegenius.activity.personal.softupdate;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.daimajia.numberprogressbar.NumberProgressBar;
import com.deplink.homegenius.activity.personal.login.LoginActivity;
import com.deplink.homegenius.activity.personal.softupdate.download.UpdateService;
import com.deplink.homegenius.constant.AppConstant;
import com.deplink.homegenius.util.Perfence;
import com.deplink.homegenius.view.combinationwidget.TitleLayout;
import com.deplink.homegenius.view.dialog.AlertDialog;
import com.deplink.sdk.android.sdk.DeplinkSDK;
import com.deplink.sdk.android.sdk.EventCallback;
import com.deplink.sdk.android.sdk.SDKAction;
import com.deplink.sdk.android.sdk.manager.SDKManager;

import deplink.com.smartwirelessrelay.homegenius.EllESDK.R;

public class UpdateProgressActivity extends Activity implements View.OnClickListener{
    private static final String TAG = "UpdateProgressActivity";
    private NumberProgressBar numberProgressBar;
    private TextView textview_updateing;
    private SDKManager manager;
    private EventCallback ec;
    /**
     * 在线升级的广播接收器，状态和进度
     */
    BroadcastReceiver br;
    private Button button_cancel;
    private TitleLayout layout_title;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_progress);
        initViews();
        initDatas();
        initEvents();
        String md5Value = manager.getAppUpdateInfo().getMd5();
        url_1 = manager.getAppUpdateInfo().getDownload_url();
        url_2 = manager.getAppUpdateInfo().getDownload_url2();
        Log.i(TAG, "url_1=" + url_1 + "url_2=" + url_2);
        if (url_1 != null && !url_1.equals("")) {
            UpdateService.Builder.create(url_1)
                    .setIsSendBroadcast(true).build(UpdateProgressActivity.this);
        } else if (url_2 != null && !url_2.equals("")) {
            UpdateService.Builder.create(url_2)
                    .setIsSendBroadcast(true).build(UpdateProgressActivity.this);
        }
    }

    private void initEvents() {
        button_cancel.setOnClickListener(this);
        IntentFilter intentFilter = new IntentFilter(UpdateService.ACTION);
        registerReceiver(br, intentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(br);
    }

    private String url_1;
    private String url_2;

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
    private void initDatas() {
        layout_title.setReturnClickListener(new TitleLayout.ReturnImageClickListener() {
            @Override
            public void onBackPressed() {
                UpdateProgressActivity.this.onBackPressed();
            }
        });
        manager = DeplinkSDK.getSDKManager();
        br = new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                int status = arg1.getIntExtra(UpdateService.STATUS, UpdateService.UPDATE_PROGRESS_STATUS);
                int progress = arg1.getIntExtra(UpdateService.PROGRESS, UpdateService.UPDATE_PROGRESS_STATUS);
                switch (status) {
                    case 0:
                        numberProgressBar.setProgress(progress);
                        break;
                    case -1:
                        //下载失败
                        textview_updateing.setText(getString(R.string.update_app_model_error));
                        numberProgressBar.setVisibility(View.INVISIBLE);
                        break;
                    case 1:
                        //下载成功
                        numberProgressBar.setVisibility(View.INVISIBLE);
                        textview_updateing.setText("软件下载已完成");
                        break;
                    default:
                        break;
                }
            }
        };
        ec = new EventCallback() {
            @Override
            public void onSuccess(SDKAction action) {

            }

            @Override
            public void onBindSuccess(SDKAction action, String devicekey) {

            }

            @Override
            public void onGetImageSuccess(SDKAction action, Bitmap bm) {

            }

            @Override
            public void onFailure(SDKAction action, Throwable throwable) {

            }

            @Override
            public void connectionLost(Throwable throwable) {
                super.connectionLost(throwable);
                Perfence.setPerfence(AppConstant.USER_LOGIN, false);
                new AlertDialog(UpdateProgressActivity.this).builder().setTitle("账号异地登录")
                        .setMsg("当前账号已在其它设备上登录,是否重新登录")
                        .setPositiveButton("确认", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                startActivity(new Intent(UpdateProgressActivity.this, LoginActivity.class));
                            }
                        }).setNegativeButton("取消", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                }).show();

            }
        };
    }

    private void initViews() {
        numberProgressBar = findViewById(R.id.number_progress_bar);
        textview_updateing = findViewById(R.id.textview_updateing);
        button_cancel = findViewById(R.id.button_cancel);
        layout_title= findViewById(R.id.layout_title);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.button_cancel:
                onBackPressed();
                break;
        }
    }
}
