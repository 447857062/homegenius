package com.deplink.homegenius.activity.device.smartlock;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.deplink.homegenius.Protocol.json.OpResult;
import com.deplink.homegenius.Protocol.json.device.lock.UserIdInfo;
import com.deplink.homegenius.Protocol.json.device.lock.UserIdPairs;
import com.deplink.homegenius.activity.device.DevicesActivity;
import com.deplink.homegenius.activity.device.smartlock.alarmhistory.AlarmHistoryActivity;
import com.deplink.homegenius.activity.device.smartlock.lockhistory.LockHistoryActivity;
import com.deplink.homegenius.activity.homepage.SmartHomeMainActivity;
import com.deplink.homegenius.activity.personal.experienceCenter.ExperienceDevicesActivity;
import com.deplink.homegenius.activity.personal.login.LoginActivity;
import com.deplink.homegenius.constant.AppConstant;
import com.deplink.homegenius.constant.SmartLockConstant;
import com.deplink.homegenius.manager.connect.local.tcp.LocalConnectmanager;
import com.deplink.homegenius.manager.device.DeviceManager;
import com.deplink.homegenius.manager.device.smartlock.SmartLockListener;
import com.deplink.homegenius.manager.device.smartlock.SmartLockManager;
import com.deplink.homegenius.util.Perfence;
import com.deplink.homegenius.util.WeakRefHandler;
import com.deplink.homegenius.view.combinationwidget.TitleLayout;
import com.deplink.homegenius.view.dialog.ActionSheetDialog;
import com.deplink.homegenius.view.dialog.AlertDialog;
import com.deplink.homegenius.view.dialog.smartlock.AuthoriseDialog;
import com.deplink.homegenius.view.toast.ToastSingleShow;
import com.deplink.sdk.android.sdk.DeplinkSDK;
import com.deplink.sdk.android.sdk.EventCallback;
import com.deplink.sdk.android.sdk.SDKAction;
import com.deplink.sdk.android.sdk.manager.SDKManager;
import com.google.gson.Gson;

import org.litepal.crud.DataSupport;

import java.util.List;

import deplink.com.smartwirelessrelay.homegenius.EllESDK.R;

public class SmartLockActivity extends Activity implements View.OnClickListener, SmartLockListener, AuthoriseDialog.GetDialogAuthtTypeTimeListener {
    private static final String TAG = "SmartLockActivity";
    private RelativeLayout layout_password_not_save;
    private RelativeLayout layout_auth;
    private RelativeLayout layout_clear_record;
    private RelativeLayout layout_lock_record;

    private ImageView imageview_unlock;
    private SmartLockManager mSmartLockManager;
    private AuthoriseDialog mAuthoriseDialog;
    /**
     * 测试管理密码，使用密码开门，如果返回成功，就是正确的管理密码
     */
    private boolean saveManagetPassword;
    private String savedManagePassword;
    private boolean isStartFromExperience;
    private boolean saveManagetPasswordExperience;
    private boolean isLogin;
    private SDKManager manager;
    private EventCallback ec;
    private DeviceManager mDeviceManager;
    private long currentTime;
    private TitleLayout layout_title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smart_lock);
        initViews();
        initDatas();
        initEvents();
    }

    private void initDatas() {
        layout_title.setReturnClickListener(new TitleLayout.ReturnImageClickListener() {
            @Override
            public void onBackPressed() {
                Intent intentBack;
                if (isStartFromExperience) {
                    if (mDeviceManager.isStartFromHomePage()) {
                        intentBack = new Intent(SmartLockActivity.this, SmartHomeMainActivity.class);
                    } else {
                        intentBack = new Intent(SmartLockActivity.this, ExperienceDevicesActivity.class);
                    }
                    intentBack.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intentBack);
                } else {
                    intentBack = new Intent(SmartLockActivity.this, DevicesActivity.class);
                    intentBack.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intentBack);
                }
            }
        });
        layout_title.setEditTextClickListener(new TitleLayout.EditTextClickListener() {
            @Override
            public void onEditTextPressed() {
                Intent intent = new Intent(SmartLockActivity.this, EditSmartLockActivity.class);
                startActivity(intent);
            }
        });
        mSmartLockManager = SmartLockManager.getInstance();
        mDeviceManager = DeviceManager.getInstance();
        mSmartLockManager.InitSmartLockManager(this);
        saveManagetPasswordExperience = true;
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
            public void notifyHomeGeniusResponse(String setResult) {
                super.notifyHomeGeniusResponse(setResult);
                Gson gson = new Gson();
                OpResult type = gson.fromJson(setResult, OpResult.class);
                Log.i(TAG, "智能门锁接收远程门锁设置结果返回:" + setResult);
                if (type != null && type.getOP().equalsIgnoreCase("REPORT") && type.getMethod().equalsIgnoreCase("SmartLock")) {
                    switch (type.getCommand()) {
                        case SmartLockConstant.CMD.OPEN:
                            switch (type.getResult()) {
                                case SmartLockConstant.OPENLOCK.TIMEOUT:
                                    setResult = "开锁超时";
                                    break;
                                case SmartLockConstant.OPENLOCK.SUCCESS:
                                    setResult = "开锁成功";
                                    break;
                                case SmartLockConstant.OPENLOCK.PASSWORDERROR:
                                    setResult = "密码错误";
                                    break;
                                case SmartLockConstant.OPENLOCK.FAIL:
                                    setResult = "开锁失败";
                                    break;
                            }
                            break;
                        case SmartLockConstant.CMD.ONCE:
                        case SmartLockConstant.CMD.PERMANENT:
                        case SmartLockConstant.CMD.TIMELIMIT:
                            switch (type.getResult()) {
                                case SmartLockConstant.AUTH.TIMEOUT:
                                    setResult = "录入超时";
                                    break;
                                case SmartLockConstant.AUTH.SUCCESS:
                                    setResult = "录入成功";
                                    break;
                                case SmartLockConstant.AUTH.PASSWORDERROR:
                                    setResult = "密码错误";
                                    break;
                                case SmartLockConstant.AUTH.FAIL:
                                    setResult = "录入失败";
                                    break;
                                case SmartLockConstant.AUTH.FORBADE:
                                    setResult = "禁止操作";
                                    break;
                            }
                            break;
                        case SmartLockConstant.CMD.QUERY:
                            Log.i(TAG, "门锁状态type=" + type.getResult());
                            Message msg = Message.obtain();
                            msg.arg1 = type.getResult();
                            msg.what = MSG_UPDATE_DEVICE_STATU;
                            mHandler.sendMessage(msg);


                            break;
                    }
                    Message msg = Message.obtain();
                    msg.what = MSG_SHOW_TOAST;
                    if (setResult.length() == 4) {
                        msg.obj = setResult;
                        mHandler.sendMessage(msg);
                    }
                }
            }

            @Override
            public void connectionLost(Throwable throwable) {
                super.connectionLost(throwable);
                Perfence.setPerfence(AppConstant.USER_LOGIN, false);
                isLogin = false;
                new AlertDialog(SmartLockActivity.this).builder().setTitle("账号异地登录")
                        .setMsg("当前账号已在其它设备上登录,是否重新登录")
                        .setPositiveButton("确认", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                startActivity(new Intent(SmartLockActivity.this, LoginActivity.class));
                            }
                        }).setNegativeButton("取消", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                }).show();
            }
        };
    }

    private String selfUserId;
    private String statu;

    @Override
    protected void onResume() {
        super.onResume();
        isLogin = Perfence.getBooleanPerfence(AppConstant.USER_LOGIN);
        isStartFromExperience = mDeviceManager.isStartFromExperience();
        mSmartLockManager.addSmartLockListener(this);
        manager.addEventCallback(ec);
        selfUserId = Perfence.getPerfence(AppConstant.PERFENCE_LOCK_SELF_USERID);
        if (!isStartFromExperience) {
            mSmartLockManager.queryLockStatu();
            mSmartLockManager.queryLockUidHttp(mSmartLockManager.getCurrentSelectLock().getUid());
            statu = mSmartLockManager.getCurrentSelectLock().getStatus();
            Log.i(TAG, "当前设备uid=" + mSmartLockManager.getCurrentSelectLock().getUid() + "状态=" + statu);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        manager.removeEventCallback(ec);
        mSmartLockManager.removeSmartLockListener(this);
    }

    private void initEvents() {
        layout_lock_record.setOnClickListener(this);
        layout_password_not_save.setOnClickListener(this);
        layout_auth.setOnClickListener(this);
        layout_clear_record.setOnClickListener(this);
        imageview_unlock.setOnClickListener(this);
    }

    private void initViews() {
        layout_title = findViewById(R.id.layout_title);
        layout_lock_record = findViewById(R.id.layout_lock_record);
        layout_password_not_save = findViewById(R.id.layout_password_not_save);
        layout_auth = findViewById(R.id.layout_auth);
        layout_clear_record = findViewById(R.id.layout_clear_record);
        imageview_unlock = findViewById(R.id.imageview_unlock);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.layout_lock_record:
                new ActionSheetDialog(SmartLockActivity.this)
                        .builder()
                        .setCancelable(true)
                        .setCanceledOnTouchOutside(true)
                        .addSheetItem("开锁记录", ActionSheetDialog.SheetItemColor.GRAY,
                                new ActionSheetDialog.OnSheetItemClickListener() {
                                    @Override
                                    public void onClick(int which) {
                                        Intent intentLockHistory = new Intent(SmartLockActivity.this, LockHistoryActivity.class);
                                        intentLockHistory.putExtra("isStartFromExperience", isStartFromExperience);
                                        startActivity(intentLockHistory);
                                    }
                                })
                        .addSheetItem("报警记录", ActionSheetDialog.SheetItemColor.GRAY,
                                new ActionSheetDialog.OnSheetItemClickListener() {
                                    @Override
                                    public void onClick(int which) {
                                        Intent intentAlarmHistory = new Intent(SmartLockActivity.this, AlarmHistoryActivity.class);
                                        intentAlarmHistory.putExtra("isStartFromExperience", isStartFromExperience);
                                        startActivity(intentAlarmHistory);
                                    }
                                })
                        .show();
                break;
            case R.id.layout_password_not_save:
                new AlertDialog(SmartLockActivity.this).builder().setTitle("温馨提示")
                        .setMsg("确定密码不保存?")
                        .setPositiveButton("确认", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (isStartFromExperience) {
                                    saveManagetPasswordExperience = false;
                                    mHandler.sendEmptyMessage(MSG_SHOW_NOTSAVE_PASSWORD_DIALOG);
                                } else {
                                    saveManagetPassword = false;
                                    savedManagePassword = "";
                                    mSmartLockManager.getCurrentSelectLock().setLockPassword("");
                                    boolean saveResult = mSmartLockManager.getCurrentSelectLock().save();
                                    Log.i(TAG, "密码不保存=" + saveResult);
                                }
                            }
                        }).setNegativeButton("取消", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                }).show();
                break;
            case R.id.layout_auth:
                if (statu == null) {
                    statu = "off";
                }
                if (isStartFromExperience) {
                    mAuthoriseDialog = new AuthoriseDialog(this);
                    mAuthoriseDialog.setGetDialogAuthtTypeTimeListener(this);
                    mAuthoriseDialog.show();
                } else {
                    if (statu.equalsIgnoreCase("off")) {
                        ToastSingleShow.showText(this, "设备已离线");
                        return;
                    } else {
                        saveManagetPassword = (mSmartLockManager.getCurrentSelectLock().isRemerberPassword());
                        savedManagePassword = mSmartLockManager.getCurrentSelectLock().getLockPassword();
                        if (saveManagetPassword && !savedManagePassword.equals("")) {
                            mAuthoriseDialog = new AuthoriseDialog(this);
                            mAuthoriseDialog.setGetDialogAuthtTypeTimeListener(this);
                            mAuthoriseDialog.show();
                        } else {
                            ToastSingleShow.showText(this, "没有记住开锁密码,请在开锁后记住开锁密码,才能授权");
                        }
                    }
                }

                break;

            case R.id.imageview_unlock:
                if ((System.currentTimeMillis() - currentTime) / 1000 > 10) {
                    currentTime = System.currentTimeMillis();
                    if (isStartFromExperience) {
                        if (saveManagetPasswordExperience) {
                            Toast.makeText(SmartLockActivity.this, "开门成功", Toast.LENGTH_SHORT).show();
                        } else {
                            Intent intentSetLockPwd = new Intent(SmartLockActivity.this, SetLockPwdActivity.class);
                            startActivity(intentSetLockPwd);
                        }
                    } else {
                        if (statu == null) {
                            statu = "off";
                        }
                        if (statu.equalsIgnoreCase("off")) {
                            ToastSingleShow.showText(this, "设备已离线");
                            return;
                        } else {
                            saveManagetPassword = (mSmartLockManager.getCurrentSelectLock().isRemerberPassword());
                            savedManagePassword = mSmartLockManager.getCurrentSelectLock().getLockPassword();
                            Log.i(TAG, "saveManagetPassword=" + saveManagetPassword + "savedManagePassword=" + savedManagePassword);
                            if (LocalConnectmanager.getInstance().isLocalconnectAvailable()) {
                                if (saveManagetPassword && !savedManagePassword.equals("")) {
                                    mSmartLockManager.setSmartLockParmars(SmartLockConstant.OPEN_LOCK, selfUserId, savedManagePassword, null, null);
                                } else {
                                    Intent intentSetLockPwd = new Intent(SmartLockActivity.this, SetLockPwdActivity.class);
                                    startActivity(intentSetLockPwd);
                                }
                            } else {
                                if (isLogin) {
                                    if (saveManagetPassword && !savedManagePassword.equals("")) {
                                        mSmartLockManager.setSmartLockParmars(SmartLockConstant.OPEN_LOCK, selfUserId, savedManagePassword, null, null);
                                    } else {
                                        Intent intentSetLockPwd = new Intent(SmartLockActivity.this, SetLockPwdActivity.class);
                                        startActivity(intentSetLockPwd);
                                    }
                                } else {
                                    ToastSingleShow.showText(SmartLockActivity.this, "未登录");
                                }
                            }
                        }

                    }
                }
                break;
            case R.id.layout_clear_record:
                new AlertDialog(SmartLockActivity.this).builder().setTitle("清除记录")
                        .setMsg("确定清除所有报警记录和开锁记录?")
                        .setPositiveButton("确认", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (!isStartFromExperience) {
                                    mSmartLockManager.clearAlarmRecord();
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

    @Override
    public void responseQueryResult(String result) {
    }

    @Override
    public void responseSetResult(String result) {
        Log.i(TAG, "设置结果=" + result);
        Message msg = Message.obtain();
        msg.what = MSG_SHOW_TOAST;
        msg.obj = result;
        mHandler.sendMessage(msg);
    }

    @Override
    public void responseBind(String result) {

    }

    @Override
    public void responseLockStatu(int RecondNum, int LockStatus) {
        Log.i(TAG, "返回门锁状态结果=" + LockStatus);
        //状态不一致需要更新
        //LockStatus 锁的打开状态
        Message msg = Message.obtain();
        msg.arg1 = LockStatus;
        msg.what = MSG_UPDATE_DEVICE_STATU;
        mHandler.sendMessage(msg);
    }

    @Override
    public void responseUserIdInfo(UserIdInfo userIdInfo) {
        List<UserIdPairs> mUserIdPairs = userIdInfo.getAlluser();
        for (int i = 0; i < mUserIdPairs.size(); i++) {
            UserIdPairs tempUserIdPair = DataSupport.where("userid = ?", mUserIdPairs.get(i).getUserid()).findFirst(UserIdPairs.class);
            if (tempUserIdPair != null) {
                tempUserIdPair.setUsername(userIdInfo.getAlluser().get(i).getUsername());
                tempUserIdPair.saveFast();
            } else {
                UserIdPairs addUserIdPair = new UserIdPairs();
                addUserIdPair.setUserid(userIdInfo.getAlluser().get(i).getUserid());
                addUserIdPair.setUsername(userIdInfo.getAlluser().get(i).getUsername());
                addUserIdPair.saveFast();
            }
        }
        Perfence.setPerfence(AppConstant.PERFENCE_LOCK_SELF_USERID, userIdInfo.getSelfid());
    }

    private static final int MSG_SHOW_TOAST = 1;
    private static final int MSG_SHOW_NOTSAVE_PASSWORD_DIALOG = 2;
    private static final int MSG_UPDATE_DEVICE_STATU = 3;
    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_SHOW_TOAST:
                    Toast.makeText(SmartLockActivity.this, msg.obj.toString(), Toast.LENGTH_SHORT).show();
                    break;
                case MSG_SHOW_NOTSAVE_PASSWORD_DIALOG:
                    Toast.makeText(SmartLockActivity.this, "密码不保存", Toast.LENGTH_SHORT).show();
                    break;
                case MSG_UPDATE_DEVICE_STATU:
                    Log.i(TAG, "MSG_UPDATE_DEVICE_STATU");
                    if (msg.arg1 != -1) {
                        mSmartLockManager.getCurrentSelectLock().setStatus("在线");
                        mSmartLockManager.getCurrentSelectLock().saveFast();
                    } else {
                        mSmartLockManager.getCurrentSelectLock().setStatus("离线");
                        mSmartLockManager.getCurrentSelectLock().saveFast();
                    }
                    break;
            }
            return true;
        }
    };
    private Handler mHandler = new WeakRefHandler(mCallback);

    @Override
    public void onGetDialogAuthtTypeTime(String authType, String password, String limitTime) {
        Log.i(TAG, "authType=" + authType);
        if (isStartFromExperience) {
            switch (authType) {
                case SmartLockConstant.AUTH_TYPE_ONCE:
                    Toast.makeText(SmartLockActivity.this, "单次授权成功", Toast.LENGTH_SHORT).show();
                    break;
                case SmartLockConstant.AUTH_TYPE_PERPETUAL:
                    Toast.makeText(SmartLockActivity.this, "永久授权成功", Toast.LENGTH_SHORT).show();
                    break;
                case SmartLockConstant.AUTH_TYPE_TIME_LIMIT:
                    Toast.makeText(SmartLockActivity.this, "限时授权成功", Toast.LENGTH_SHORT).show();
                    break;
            }
        } else {
            if (statu.equalsIgnoreCase("off")) {
                ToastSingleShow.showText(SmartLockActivity.this, "设备已离线");
            } else {
                switch (authType) {
                    case SmartLockConstant.AUTH_TYPE_ONCE:
                        mSmartLockManager.setSmartLockParmars(SmartLockConstant.AUTH_TYPE_ONCE, selfUserId, mSmartLockManager.getCurrentSelectLock().getLockPassword(), password, null);
                        break;
                    case SmartLockConstant.AUTH_TYPE_PERPETUAL:
                        mSmartLockManager.setSmartLockParmars(SmartLockConstant.AUTH_TYPE_PERPETUAL, selfUserId, mSmartLockManager.getCurrentSelectLock().getLockPassword(), password, null);
                        break;
                    case SmartLockConstant.AUTH_TYPE_TIME_LIMIT:
                        long hour = Long.valueOf(limitTime);
                        limitTime = String.valueOf(hour * 60 * 1000);
                        mSmartLockManager.setSmartLockParmars(SmartLockConstant.AUTH_TYPE_PERPETUAL, selfUserId, mSmartLockManager.getCurrentSelectLock().getLockPassword(), password, limitTime);
                        break;
                }
            }

        }

    }
}
