package com.deplink.boruSmart.activity.device.smartlock.lockhistory;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.deplink.boruSmart.Protocol.json.OpResult;
import com.deplink.boruSmart.Protocol.json.device.lock.LockHistorys;
import com.deplink.boruSmart.Protocol.json.device.lock.Record;
import com.deplink.boruSmart.Protocol.json.device.lock.UserIdInfo;
import com.deplink.boruSmart.activity.device.smartlock.userid.UpdateSmartLockUserIdActivity;
import com.deplink.boruSmart.activity.personal.login.LoginActivity;
import com.deplink.boruSmart.constant.AppConstant;
import com.deplink.boruSmart.constant.SmartLockConstant;
import com.deplink.boruSmart.manager.connect.local.tcp.LocalConnectmanager;
import com.deplink.boruSmart.manager.device.DeviceManager;
import com.deplink.boruSmart.manager.device.smartlock.SmartLockListener;
import com.deplink.boruSmart.manager.device.smartlock.SmartLockManager;
import com.deplink.boruSmart.util.DateUtil;
import com.deplink.boruSmart.util.Perfence;
import com.deplink.boruSmart.util.WeakRefHandler;
import com.deplink.boruSmart.view.combinationwidget.TitleLayout;
import com.deplink.boruSmart.view.dialog.AlertDialog;
import com.deplink.boruSmart.view.toast.Ftoast;
import com.deplink.sdk.android.sdk.DeplinkSDK;
import com.deplink.sdk.android.sdk.EventCallback;
import com.deplink.sdk.android.sdk.SDKAction;
import com.deplink.sdk.android.sdk.manager.SDKManager;
import com.google.gson.Gson;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import deplink.com.smartwirelessrelay.homegenius.EllESDK.R;

/**
 * 开锁记录界面
 */
public class LockHistoryActivity extends Activity implements SmartLockListener {
    private static final String TAG = "LockHistory";
    private ListView dev_list;
    private List<Record> mRecordList;
    private LockHistoryAdapter recordAdapter;
    private SmartLockManager mSmartLockManager;
    private boolean isStartFromExperience;
    private TextView textview_empty_record;
    private ImageView imageview_no_lockhostory;
    private boolean isLogin;
    private SDKManager manager;
    private EventCallback ec;
    private DeviceManager mDeviceManager;
    private ArrayList<String> mRecordListId;
    private TextView textview_get_record_ing;
    private TitleLayout layout_title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock_history);
        initViews();
        initData();
        initEvents();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSmartLockManager.removeSmartLockListener(this);
        //handler.removeMessage(0)会把所有可执行任务都移除掉。

        mHandler.removeMessages(0);
    }

    private void initEvents() {
        dev_list.setAdapter(recordAdapter);
    }

    private void initData() {
        layout_title.setReturnClickListener(new TitleLayout.ReturnImageClickListener() {
            @Override
            public void onBackPressed() {
                LockHistoryActivity.this.onBackPressed();
            }
        });
        layout_title.setEditTextClickListener(new TitleLayout.EditTextClickListener() {
            @Override
            public void onEditTextPressed() {
                if (isStartFromExperience) {
                    Intent intent = new Intent(LockHistoryActivity.this, UpdateSmartLockUserIdActivity.class);
                    startActivity(intent);
                } else {
                    if (mRecordListId != null) {
                        Intent intent = new Intent(LockHistoryActivity.this, UpdateSmartLockUserIdActivity.class);
                        intent.putStringArrayListExtra("recordlistid", mRecordListId);
                        startActivity(intent);
                    } else {
                        Ftoast.create(LockHistoryActivity.this).setText("未获取到开锁记录").show();
                    }
                }
            }
        });
        mDeviceManager = DeviceManager.getInstance();
        mDeviceManager.InitDeviceManager(this);
        isStartFromExperience = mDeviceManager.isStartFromExperience();
        mRecordList = new ArrayList<>();
        List<Record> records = DataSupport.findAll(Record.class);
        mRecordList.addAll(records);
        sortRecords(mRecordList);
        mRecordListId = new ArrayList<>();
        for (int i = 0; i < mRecordList.size(); i++) {
            if (!mRecordListId.contains(mRecordList.get(i).getUserID())) {
                mRecordListId.add(mRecordList.get(i).getUserID());
            }
        }
        recordAdapter = new LockHistoryAdapter(this, mRecordList);
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
            public void notifyHomeGeniusResponse(String result) {
                super.notifyHomeGeniusResponse(result);
                Gson gson = new Gson();
                OpResult type = gson.fromJson(result, OpResult.class);
                Log.i(TAG, "门锁管理器查询结果返回:" + result);
                if (type != null && type.getOP().equalsIgnoreCase("REPORT") &&
                        (type.getMethod().equalsIgnoreCase("SMART_LOCK") || type.getMethod().equalsIgnoreCase("SmartLock"))) {
                    Message msg = Message.obtain();
                    switch (type.getCommand()) {
                        case SmartLockConstant.CMD.QUERY:
                            int recondNum = type.getRecordNum();
                            recordNumTotal = recondNum;
                            msg.what = MSG_GET_HISRECORD;
                            if (LocalConnectmanager.getInstance().isLocalconnectAvailable()) {
                                msg.arg1 = 1;
                            } else {
                                msg.arg1 = 2;
                            }
                            msg.arg2 = recondNum;
                            mHandler.sendMessageDelayed(msg, 2000);
                            break;
                        case "HisRecord":
                            msg.obj = result;
                            msg.what = MSG_GET_HISTORYRECORD;
                            if (!isStartFromExperience) {
                                mHandler.sendMessage(msg);
                            }
                            break;
                    }
                }
            }

            @Override
            public void connectionLost(Throwable throwable) {
                super.connectionLost(throwable);
                Perfence.setPerfence(AppConstant.USER_LOGIN, false);
                isLogin = false;
                new AlertDialog(LockHistoryActivity.this).builder().setTitle("账号异地登录")
                        .setMsg("当前账号已在其它设备上登录,是否重新登录")
                        .setPositiveButton("确认", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                startActivity(new Intent(LockHistoryActivity.this, LoginActivity.class));
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
        dev_list = (ListView) findViewById(R.id.list_lock_histroy);
        textview_empty_record = (TextView) findViewById(R.id.textview_empty_record);
        imageview_no_lockhostory = (ImageView) findViewById(R.id.imageview_no_lockhostory);
        textview_get_record_ing = (TextView) findViewById(R.id.textview_get_record_ing);
        layout_title = (TitleLayout) findViewById(R.id.layout_title);
    }

    private String userId;

    @Override
    protected void onResume() {
        super.onResume();
        mRecordList.clear();
        userId = Perfence.getPerfence(AppConstant.PERFENCE_LOCK_SELF_USERID);
        mSmartLockManager = SmartLockManager.getInstance();
        mSmartLockManager.InitSmartLockManager(this);
        mSmartLockManager.addSmartLockListener(this);
        isLogin = Perfence.getBooleanPerfence(AppConstant.USER_LOGIN);
        isStartFromExperience = mDeviceManager.isStartFromExperience();
        manager.addEventCallback(ec);
        if (isStartFromExperience) {
            mRecordList.clear();
            Record temp = new Record();
            temp.setTime("2017-11-23 12:35:23");
            temp.setUserID("001");
            mRecordList.add(temp);
            temp = new Record();
            temp.setTime("2017-11-24 12:35:23");
            temp.setUserID("002");
            mRecordList.add(temp);
            temp = new Record();
            temp.setTime("2017-11-25 12:35:23");
            temp.setUserID("003");
            mRecordList.add(temp);
            temp = new Record();
            temp.setTime("2017-11-26 12:35:23");
            temp.setUserID("004");
            mRecordList.add(temp);
            textview_get_record_ing.setVisibility(View.GONE);
        } else {
            mSmartLockManager.queryLockStatu();
            mHandler.sendEmptyMessageDelayed(MSG_GET_HISRECORD, 2000);
        }
    }

    private static final int MSG_GET_HISTORYRECORD = 0x01;
    private static final int MSG_GET_HISRECORD = 0x03;
    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            String str = (String) msg.obj;
            switch (msg.what) {
                case MSG_GET_HISTORYRECORD:
                    Gson gson = new Gson();
                    LockHistorys aDeviceList = gson.fromJson(str, LockHistorys.class);
                    textview_get_record_ing.setVisibility(View.GONE);
                    int index = DataSupport.count(Record.class);
                    for (int i = 0; i < aDeviceList.getRecord().size(); i++) {
                        int insertIndex = index + (i + 1);
                        //如果小于总数就添加到数据库中,大于总数就更新数据库
                        if (index < recordNumTotal) {
                            Record tempRecord = new Record();
                            tempRecord.setIndex(insertIndex);
                            tempRecord.setTime(aDeviceList.getRecord().get(i).getTime());
                            tempRecord.setUserID(aDeviceList.getRecord().get(i).getUserID());
                            tempRecord.save();
                        } else {
                            String findindex = "" + insertIndex;
                            Record findIndexRecord = DataSupport.where("recordIndex = ?", findindex).findFirst(Record.class);
                            if (findIndexRecord != null) {
                                findIndexRecord.setTime(aDeviceList.getRecord().get(i).getTime());
                                findIndexRecord.setUserID(aDeviceList.getRecord().get(i).getUserID());
                                findIndexRecord.save();
                            }
                        }
                    }
                    if (mRecordList.size() < recordNumTotal) {
                        mRecordList.addAll(aDeviceList.getRecord());
                    } else {
                        //先移除再添加
                        sortRecords(mRecordList);
                        for (int i = 0; i < aDeviceList.getRecord().size(); i++) {
                            if (mRecordList.size() > i) {
                                mRecordList.remove(i);
                            }
                        }
                        for (int i = 0; i < aDeviceList.getRecord().size(); i++) {
                            mRecordList.add(aDeviceList.getRecord().get(i));
                        }
                    }
                    sortRecords(mRecordList);
                    mRecordListId = new ArrayList<>();
                    for (int i = 0; i < mRecordList.size(); i++) {
                        if (!mRecordListId.contains(mRecordList.get(i).getUserID())) {
                            mRecordListId.add(mRecordList.get(i).getUserID());
                        }
                    }
                    textview_empty_record.setVisibility(View.GONE);
                    imageview_no_lockhostory.setVisibility(View.GONE);
                    recordAdapter.notifyDataSetChanged();
                    msg = Message.obtain();
                    msg.what = MSG_GET_HISRECORD;
                    if (LocalConnectmanager.getInstance().isLocalconnectAvailable()) {
                        msg.arg1 = 1;
                    } else {
                        msg.arg1 = 2;
                    }
                    mHandler.sendMessageDelayed(msg, 2000);
                    break;
                case MSG_GET_HISRECORD:
                    if (mRecordList.size() == 0) {
                        imageview_no_lockhostory.setVisibility(View.VISIBLE);
                        textview_empty_record.setVisibility(View.VISIBLE);
                        textview_get_record_ing.setVisibility(View.GONE);
                    }
                    int localConnectAvailable = msg.arg1;
                    Log.i(TAG,"recordIndex="+recordIndex+"recordNumTotal="+recordNumTotal
                    +"localConnectAvailable="+localConnectAvailable
                    );
                    if (recordIndex != recordNumTotal) {
                        if (localConnectAvailable == 1) {
                            if (recordNumTotal <= 5) {
                                recordIndex += 5;
                                mSmartLockManager.queryLockHistory(true, 1, 5, userId);
                            } else {
                                recordIndex = recordNumTotal;
                                mSmartLockManager.queryLockHistory(true, 1, recordNumTotal, userId);
                            }
                        } else {
                            if (recordNumTotal <= 5) {
                                recordIndex += 5;
                                mSmartLockManager.queryLockHistory(false, 1, 5, userId);
                            } else {
                                recordIndex = recordNumTotal;
                                mSmartLockManager.queryLockHistory(false, 1, recordNumTotal, userId);
                            }
                        }
                    }
                    break;
            }
            return true;
        }
    };
    private Handler mHandler = new WeakRefHandler(mCallback);

    /**
     * 按照序号排序
     */
    public List<Record> sortRecords(List<Record> mRecords) {
        Collections.sort(mRecords, new Comparator<Record>() {
            @Override
            public int compare(Record o1, Record o2) {
                //compareTo就是比较两个值，如果前者大于后者，返回1，等于返回0，小于返回-1
                if (DateUtil.transStringTodata(o1.getTime()).getTime()
                        == DateUtil.transStringTodata(o2.getTime()).getTime()) {
                    return 0;
                }
                if (DateUtil.transStringTodata(o1.getTime()).getTime()
                        < DateUtil.transStringTodata(o2.getTime()).getTime()) {
                    return 1;
                }
                if (DateUtil.transStringTodata(o1.getTime()).getTime()
                        > DateUtil.transStringTodata(o2.getTime()).getTime()) {
                    return -1;
                }
                return 0;
            }
        });
        return mRecords;
    }

    @Override
    public void responseQueryResult(String result) {
        Message msg = Message.obtain();
        msg.obj = result;
        if (result.contains("HisRecord")) {
            msg.what = MSG_GET_HISTORYRECORD;
        }
        mHandler.sendMessage(msg);
    }

    @Override
    public void responseSetResult(String result) {

    }



    private int recordNumTotal;
    private int recordIndex;

    @Override
    public void responseLockStatu(int recondNum, int LockStatus) {
        recordNumTotal = recondNum;
        Message msg = Message.obtain();
        msg.what = MSG_GET_HISRECORD;
        if (LocalConnectmanager.getInstance().isLocalconnectAvailable()) {
            msg.arg1 = 1;
        } else {
            msg.arg1 = 2;
        }
        mHandler.sendMessageDelayed(msg, 2000);
    }

    @Override
    public void responseUserIdInfo(UserIdInfo userIdInfo) {

    }
}
