package com.deplink.boruSmart.activity.device;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.deplink.boruSmart.Protocol.json.device.share.UserShareInfo;
import com.deplink.boruSmart.activity.device.adapter.ShareDeviceListAdapter;
import com.deplink.boruSmart.constant.AppConstant;
import com.deplink.boruSmart.manager.device.DeviceListener;
import com.deplink.boruSmart.manager.device.DeviceManager;
import com.deplink.boruSmart.util.JsonArrayParseUtil;
import com.deplink.boruSmart.util.Perfence;
import com.deplink.boruSmart.util.StringValidatorUtil;
import com.deplink.boruSmart.util.WeakRefHandler;
import com.deplink.boruSmart.view.combinationwidget.TitleLayout;
import com.deplink.boruSmart.view.dialog.AlertDialog;
import com.deplink.boruSmart.view.dialog.InputAlertDialog;
import com.deplink.boruSmart.view.scrollview.NonScrollableListView;
import com.deplink.boruSmart.view.toast.Ftoast;
import com.deplink.sdk.android.sdk.DeplinkSDK;
import com.deplink.sdk.android.sdk.EventCallback;
import com.deplink.sdk.android.sdk.SDKAction;
import com.deplink.sdk.android.sdk.homegenius.DeviceOperationResponse;
import com.deplink.sdk.android.sdk.homegenius.ShareDeviceBody;
import com.deplink.sdk.android.sdk.manager.SDKManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import deplink.com.smartwirelessrelay.homegenius.EllESDK.R;

public class ShareDeviceActivity extends Activity  {
    private static final String TAG = "ShareDeviceActivity";
    private NonScrollableListView listview_share_user;
    private DeviceListener mDeviceListener;
    private DeviceManager mDeviceManager;
    private boolean isStartFromExperience;
    private boolean isUserLogin;
    private SDKManager manager;
    private EventCallback ec;
    private String deviceuid;
    private ShareDeviceListAdapter mAdapter;
    private List<UserShareInfo> userInfos;
    private TitleLayout layout_title;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_device);
        initViews();
        initDatas();
        initEvents();
    }
    private void initEvents() {
        listview_share_user.setAdapter(mAdapter);
        listview_share_user.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int position, long id) {
                String selfusername = Perfence.getPerfence(Perfence.PERFENCE_PHONE);
                boolean selfIsManager = false;
                for (int i = 0; i < userInfos.size(); i++) {
                    if (userInfos.get(i).getUsername().equalsIgnoreCase(selfusername) && userInfos.get(i).getIssuper() == 0) {
                        //自己不是管理员
                        selfIsManager = false;
                    } else if (userInfos.get(i).getUsername().equalsIgnoreCase(selfusername) && userInfos.get(i).getIssuper() == 1) {
                        selfIsManager = true;
                    }
                }
                if (selfIsManager) {
                    if (userInfos.get(position).getIssuper() == 1
                            ){
                    } else {
                        if (userInfos.get(position).getStatus() == 2) {
                            //该用户绑定
                            new AlertDialog(ShareDeviceActivity.this).builder().setTitle("设备管理")
                                    .setMsg("确定将" + userInfos.get(position).getUsername() + "设为管理员")
                                    .setPositiveButton("确认", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            if (userInfos.get(position).getStatus() == 2) {
                                                //该用户绑定
                                                action = "setmanager";
                                                if (!isStartFromExperience) {
                                                    if (isUserLogin) {
                                                        ShareDeviceBody body = new ShareDeviceBody();
                                                        body.setUser_name(userInfos.get(position).getUsername());
                                                        body.setAssuper(1);
                                                        mDeviceManager.shareDevice(deviceuid, body);
                                                    } else {
                                                        Ftoast.create(ShareDeviceActivity.this).setText("未登录,登录后操作").show();
                                                    }
                                                }
                                            } else if (userInfos.get(position).getStatus() == 1) {
                                                if (!isStartFromExperience) {
                                                    if (isUserLogin) {
                                                        mDeviceManager.cancelDeviceShare(deviceuid, userInfos.get(position).getShareid());
                                                    } else {
                                                        Ftoast.create(ShareDeviceActivity.this).setText("未登录,登录后操作").show();
                                                    }
                                                }
                                            }
                                        }
                                    }).setNegativeButton("取消", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                }
                            }).show();
                            action = "setmanager";
                        } else if (userInfos.get(position).getStatus() == 1) {
                            new AlertDialog(ShareDeviceActivity.this).builder().setTitle("删除用户")
                                    .setMsg("确定删除" + userInfos.get(position).getUsername())
                                    .setPositiveButton("确认", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            if (userInfos.get(position).getStatus() == 2) {
                                                //该用户绑定
                                                action = "setmanager";
                                                if (!isStartFromExperience) {
                                                    if (isUserLogin) {
                                                        ShareDeviceBody body = new ShareDeviceBody();
                                                        body.setUser_name(userInfos.get(position).getUsername());
                                                        body.setAssuper(1);
                                                        mDeviceManager.shareDevice(deviceuid, body);
                                                    } else {
                                                        Ftoast.create(ShareDeviceActivity.this).setText("未登录,登录后操作").show();
                                                    }
                                                }
                                            } else if (userInfos.get(position).getStatus() == 1) {
                                                if (!isStartFromExperience) {
                                                    if (isUserLogin) {
                                                        mDeviceManager.cancelDeviceShare(deviceuid, userInfos.get(position).getShareid());
                                                    } else {
                                                        Ftoast.create(ShareDeviceActivity.this).setText("未登录,登录后操作").show();
                                                    }
                                                }
                                            }
                                        }
                                    }).setNegativeButton("取消", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                }
                            }).show();
                        }
                    }
                }
            }
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
        isStartFromExperience = mDeviceManager.isStartFromExperience();
        isUserLogin = Perfence.getBooleanPerfence(AppConstant.USER_LOGIN);
        mDeviceManager.addDeviceListener(mDeviceListener);
        manager.addEventCallback(ec);
        if (!isStartFromExperience) {
            if (isUserLogin) {
                mDeviceManager.readDeviceShareInfo(deviceuid);
            }
        }else{
            userInfos.clear();
            UserShareInfo info=new UserShareInfo();
            info.setUsername("体验设备");
            info.setIssuper(1);
            userInfos.add(info);
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mDeviceManager.removeDeviceListener(mDeviceListener);
        manager.removeEventCallback(ec);
        userInfos.clear();
    }
    private String devicetype;
    private void initDatas() {
        devicetype=getIntent().getStringExtra("devicetype");
        layout_title.setReturnClickListener(new TitleLayout.ReturnImageClickListener() {
            @Override
            public void onBackPressed() {
                ShareDeviceActivity.this.onBackPressed();
            }
        });
        layout_title.setEditTextClickListener(new TitleLayout.EditTextClickListener() {
            @Override
            public void onEditTextPressed() {
                String selfusername = Perfence.getPerfence(Perfence.PERFENCE_PHONE);
                boolean selfIsManager = false;
                for (int i = 0; i < userInfos.size(); i++) {
                    if (userInfos.get(i).getUsername().equalsIgnoreCase(selfusername) && userInfos.get(i).getIssuper() == 0) {
                        //自己不是管理员
                        selfIsManager = false;
                    } else if (userInfos.get(i).getUsername().equalsIgnoreCase(selfusername) && userInfos.get(i).getIssuper() == 1) {
                        selfIsManager = true;
                    }
                }
                if(selfIsManager){
                    new InputAlertDialog(ShareDeviceActivity.this).builder()
                            .setTitle("分享设备")
                            .setEditTextHeader("用户名:")
                            .setEditTextHint("输入想分享此设备的用户名")
                            .setPositiveButton("确认", new InputAlertDialog.onSureBtnClickListener() {
                                @Override
                                public void onSureBtnClicked(String password) {
                                    action = "share";
                                    ShareDeviceBody body = new ShareDeviceBody();
                                    if (StringValidatorUtil.isMobileNO(password)) {
                                        body.setUser_name(password);
                                        body.setAssuper(0);
                                        if (!isStartFromExperience) {
                                            if (isUserLogin) {
                                                mDeviceManager.shareDevice(deviceuid, body);
                                            } else {
                                                Ftoast.create(ShareDeviceActivity.this).setText("未登录,登录后操作").setDuration(Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    } else {
                                        Ftoast.create(ShareDeviceActivity.this).setText("请输入想要分享该设备的用户名").show();
                                    }
                                }
                            }).setNegativeButton("取消", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                        }
                    }).show();
                }else{
                    Ftoast.create(ShareDeviceActivity.this).setText("自己不是管理员,无法分享此设备").show();
                }
            }
        });
        mDeviceManager = DeviceManager.getInstance();
        mDeviceManager.InitDeviceManager(this);
        mDeviceListener = new DeviceListener() {
            @Override
            public void responseGetDeviceShareInfo(String result) {
                super.responseGetDeviceShareInfo(result);
                if (result.contains("errcode")) {

                } else {
                    List<UserShareInfo> response = JsonArrayParseUtil.jsonToArrayList(result, UserShareInfo.class);
                    if (response != null) {
                        userInfos.clear();
                        Log.i(TAG, "分享的用户列表长度:" + response.size());
                        for (int i = 0; i < response.size(); i++) {
                            String username = response.get(i).getUsername();
                            manager.getImage(username);
                        }
                        Collections.sort(response, new Comparator<UserShareInfo>() {
                            @Override
                            public int compare(UserShareInfo o1, UserShareInfo o2) {
                                //compareTo就是比较两个值，如果前者大于后者，返回1，等于返回0，小于返回-1
                                //重小到大排序的相反
                                String selfusername = Perfence.getPerfence(Perfence.PERFENCE_PHONE);
                                if (o1.getUsername().equalsIgnoreCase(selfusername) &&
                                        o2.getUsername().equalsIgnoreCase(selfusername)) {
                                    return 0;
                                }
                                if (o1.getUsername().equalsIgnoreCase(selfusername) &&
                                        !o2.getUsername().equalsIgnoreCase(selfusername)) {
                                    return -1;
                                }
                                if (!o1.getUsername().equalsIgnoreCase(selfusername) &&
                                        o2.getUsername().equalsIgnoreCase(selfusername)) {
                                    return 1;
                                }
                                if (o1.getIssuper() == o2.getIssuper()) {
                                    if (o1.getStatus() == o2.getStatus()) {
                                        return 0;
                                    }
                                    if (o1.getStatus() < o2.getStatus()) {
                                        return 1;
                                    }
                                    if (o1.getStatus() > o2.getStatus()) {
                                        return -1;
                                    }
                                }
                                if (o1.getIssuper() < o2.getIssuper()) {
                                    return 1;
                                }
                                if (o1.getIssuper() > o2.getIssuper()) {
                                    return -1;
                                }
                                return 0;
                            }
                        });
                        userInfos.addAll(response);
                        mAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void responseDeviceShareResult(DeviceOperationResponse result) {
                super.responseDeviceShareResult(result);
                Message msg = Message.obtain();
                msg.what = MSG_SHOW_SHARE_OPTION_RESULT;
                msg.obj = result;
                mHandler.sendMessage(msg);

            }

            @Override
            public void responseCancelDeviceShare(DeviceOperationResponse result) {
                super.responseCancelDeviceShare(result);
                Message msg = Message.obtain();
                msg.what = MSG_SHOW_CANCEL_SHARE_RESULT;
                msg.obj = result;
                mHandler.sendMessage(msg);
            }
        };
        initMqtt();
        if (getIntent().getStringExtra("deviceuid") != null) {
            deviceuid = getIntent().getStringExtra("deviceuid");
        }
        userInfos = new ArrayList<>();
        userImage = new HashMap<>();
        mAdapter = new ShareDeviceListAdapter(this, userInfos, userImage);
    }

    private static final int MSG_SHOW_SHARE_OPTION_RESULT = 100;
    private static final int MSG_SHOW_CANCEL_SHARE_RESULT = 101;
    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            DeviceOperationResponse result = (DeviceOperationResponse) msg.obj;
            switch (msg.what) {
                case MSG_SHOW_SHARE_OPTION_RESULT:
                    switch (action) {
                        case "share":
                            if (result.getStatus().equalsIgnoreCase("ok")) {
                                Ftoast.create(ShareDeviceActivity.this).setText("分享设备成功").show();
                                mDeviceManager.readDeviceShareInfo(deviceuid);
                            } else {
                                Ftoast.create(ShareDeviceActivity.this).setText("分享设备失败").show();
                            }
                            break;
                        case "setmanager":
                            if (result.getStatus().equalsIgnoreCase("ok")) {
                                mDeviceManager.readDeviceShareInfo(deviceuid);
                                Ftoast.create(ShareDeviceActivity.this).setText("设置管理员成功").show();
                            } else {
                                Ftoast.create(ShareDeviceActivity.this).setText("设置管理员失败").show();
                            }
                            break;
                    }
                    break;
                case MSG_SHOW_CANCEL_SHARE_RESULT:
                    if (result.getStatus().equalsIgnoreCase("ok")) {
                        mDeviceManager.readDeviceShareInfo(deviceuid);
                        Ftoast.create(ShareDeviceActivity.this).setText("取消分享成功").show();
                    } else {
                        Ftoast.create(ShareDeviceActivity.this).setText("取消分享失败").show();
                    }
                    break;
            }
            return true;
        }
    };
    private Handler mHandler = new WeakRefHandler(mCallback);
    private HashMap<String, Bitmap> userImage;
    private String action;

    private void initMqtt() {
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
            public void deviceOpSuccess(String op, final String deviceKey) {
                super.deviceOpSuccess(op, deviceKey);
            }

            @Override
            public void onGetImageSuccess(SDKAction action, Bitmap bm) {
                userImage.clear();
                userImage.putAll(manager.getUserImage());
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void connectionLost(Throwable throwable) {
                super.connectionLost(throwable);
                isUserLogin = false;
            }

            @Override
            public void onFailure(SDKAction action, Throwable throwable) {
            }
        };
    }

    private void initViews() {
        listview_share_user = findViewById(R.id.listview_share_user);
        layout_title= findViewById(R.id.layout_title);
    }
}
