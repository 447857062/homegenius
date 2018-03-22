package com.deplink.homegenius.activity.personal.usrinfo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.deplink.homegenius.activity.personal.login.LoginActivity;
import com.deplink.homegenius.constant.AppConstant;
import com.deplink.homegenius.util.Perfence;
import com.deplink.homegenius.view.combinationwidget.TitleLayout;
import com.deplink.homegenius.view.dialog.AlertDialog;
import com.deplink.homegenius.view.edittext.ClearEditText;
import com.deplink.sdk.android.sdk.DeplinkSDK;
import com.deplink.sdk.android.sdk.EventCallback;
import com.deplink.sdk.android.sdk.SDKAction;
import com.deplink.sdk.android.sdk.homegenius.DeviceOperationResponse;
import com.deplink.sdk.android.sdk.homegenius.UserInfoAlertBody;
import com.deplink.sdk.android.sdk.manager.SDKManager;

import deplink.com.smartwirelessrelay.homegenius.EllESDK.R;

public class UpdateNicknameActivity extends Activity  {
    private static final String TAG = "UpdateNicknameActivity";
    private ClearEditText edittext_update_nickname;
    private SDKManager manager;
    private EventCallback ec;
    private String nickName;
    private boolean isUserLogin;
    private TitleLayout layout_title;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_nickname);
        initViews();
        initDatas();
        initEvents();
    }
    private void initDatas() {
        layout_title.setReturnClickListener(new TitleLayout.ReturnImageClickListener() {
            @Override
            public void onBackPressed() {
                UpdateNicknameActivity.this.onBackPressed();
            }
        });
        layout_title.setEditTextClickListener(new TitleLayout.EditTextClickListener() {
            @Override
            public void onEditTextPressed() {
                String nickNameChange=edittext_update_nickname.getText().toString();
                if(isUserLogin){
                    if(!nickNameChange.equalsIgnoreCase(nickName)){
                        String userName=Perfence.getPerfence(Perfence.PERFENCE_PHONE);
                        UserInfoAlertBody body=new UserInfoAlertBody();
                        body.setNickname(nickNameChange);
                        manager.alertUserInfo(userName,body);
                    }
                }else{
                    onBackPressed();
                }
            }
        });
        nickName=getIntent().getStringExtra("nickname");
        if(nickName!=null){
            edittext_update_nickname.setText(nickName);
            edittext_update_nickname.setSelection(nickName.length());
        }
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
            public void alertUserInfo(DeviceOperationResponse info) {
                super.alertUserInfo(info);
                Log.i(TAG,"alertUserInfo:"+info.toString());
                onBackPressed();
            }
            @Override
            public void connectionLost(Throwable throwable) {
                super.connectionLost(throwable);
                isUserLogin=false;
                Perfence.setPerfence(AppConstant.USER_LOGIN, false);
                new AlertDialog(UpdateNicknameActivity.this).builder().setTitle("账号异地登录")
                        .setMsg("当前账号已在其它设备上登录,是否重新登录")
                        .setPositiveButton("确认", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                startActivity(new Intent(UpdateNicknameActivity.this, LoginActivity.class));
                            }
                        }).setNegativeButton("取消", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                }).show();
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        isUserLogin=Perfence.getBooleanPerfence(AppConstant.USER_LOGIN);
        manager.addEventCallback(ec);
    }
    @Override
    protected void onPause() {
        super.onPause();
        manager.removeEventCallback(ec);
    }

    private void initEvents() {
    }

    private void initViews() {
        layout_title= findViewById(R.id.layout_title);
        edittext_update_nickname = findViewById(R.id.edittext_update_nickname);
    }
}
