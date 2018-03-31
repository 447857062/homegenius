package com.deplink.boruSmart.activity.device.smartlock.alarmhistory;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.deplink.boruSmart.Protocol.json.device.lock.alertreport.Info;
import com.deplink.boruSmart.manager.device.DeviceManager;
import com.deplink.boruSmart.manager.device.smartlock.SmartLockManager;
import com.deplink.boruSmart.view.combinationwidget.TitleLayout;

import java.util.ArrayList;
import java.util.List;

import deplink.com.smartwirelessrelay.homegenius.EllESDK.R;

public class AlarmHistoryActivity extends Activity {
    private ListView list_alart_histroy;
    private AlarmHistoryAdapter mAlarmHistoryAdapter;
    private List<Info> mLockHistory;
    private SmartLockManager mSmartLockManager;
    private boolean isStartFromExperience;
    private RelativeLayout layout_no_alarm_recoed;
    private TitleLayout layout_title;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_history);
        initViews();
        initDatas();
        initEvents();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isStartFromExperience) {
            mLockHistory.clear();
            Info temp = new Info();
            temp.setUserid("003");
            temp.setTime("2017-11-19 12:35:23");
            mLockHistory.add(temp);
            temp = new Info();
            temp.setUserid("004");
            temp.setTime("2017-11-20 12:35:23");
            mLockHistory.add(temp);
            temp = new Info();
            temp.setUserid("004");
            temp.setTime("2017-11-21 12:35:23");
            mLockHistory.add(temp);
            temp = new Info();
            temp.setUserid("005");
            temp.setTime("2017-11-23 12:35:23");
            mLockHistory.add(temp);
            mAlarmHistoryAdapter.notifyDataSetChanged();
        } else {
            mSmartLockManager.InitSmartLockManager(this);
            mLockHistory.clear();
            if (mSmartLockManager.getAlarmRecord() != null) {
                if(mSmartLockManager.getAlarmRecord()!=null && mSmartLockManager.getAlarmRecord().size()>0){
                    mLockHistory.addAll(mSmartLockManager.getAlarmRecord());
                    layout_no_alarm_recoed.setVisibility(View.GONE);
                }else{
                    layout_no_alarm_recoed.setVisibility(View.VISIBLE);
                }

            }
            mAlarmHistoryAdapter.notifyDataSetChanged();
        }


    }

    private void initEvents() {
        list_alart_histroy.setAdapter(mAlarmHistoryAdapter);
    }

    private void initViews() {
        list_alart_histroy = findViewById(R.id.list_alart_histroy);
        layout_no_alarm_recoed = findViewById(R.id.layout_no_alarm_recoed);
        layout_title= findViewById(R.id.layout_title);
    }

    private void initDatas() {
        isStartFromExperience =  DeviceManager.getInstance().isStartFromExperience();
        mLockHistory = new ArrayList<>();
        mAlarmHistoryAdapter = new AlarmHistoryAdapter(this, mLockHistory);
        if (!isStartFromExperience) {
            mSmartLockManager = SmartLockManager.getInstance();
        }
        layout_title.setReturnClickListener(new TitleLayout.ReturnImageClickListener() {
            @Override
            public void onBackPressed() {
                AlarmHistoryActivity.this.onBackPressed();
            }
        });
    }

}
