package com.deplink.boruSmart.activity.device.smartlock.userid;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import com.deplink.boruSmart.constant.AppConstant;
import com.deplink.boruSmart.manager.device.smartlock.SmartLockManager;
import com.deplink.boruSmart.util.Perfence;
import com.deplink.boruSmart.manager.device.DeviceManager;
import com.deplink.boruSmart.view.combinationwidget.TitleLayout;
import com.deplink.sdk.android.sdk.json.homegenius.LockUserId;

import java.util.ArrayList;

import deplink.com.smartwirelessrelay.homegenius.EllESDK.R;

public class UpdateSmartLockUserIdActivity extends Activity {
    private static final String TAG="UserIdActivity";
    private ListView listview_update_ids;
    private UserIdAdapter mUserIdAdapter;
    private ArrayList<String> mIds;
    private SmartLockManager mSmartLockManager;
    private TitleLayout layout_title;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_smart_lock_user_id);
        initViews();
        initDatas();
        initEvents();
    }


    private String selfUserId;
    private void initEvents() {
        listview_update_ids.setAdapter(mUserIdAdapter);
    }
    private boolean isStartFromExperience;
    private String userName;
    private void initDatas() {
        layout_title.setReturnClickListener(new TitleLayout.ReturnImageClickListener() {
            @Override
            public void onBackPressed() {
                UpdateSmartLockUserIdActivity.this.onBackPressed();
            }
        });
        layout_title.setEditTextClickListener(new TitleLayout.EditTextClickListener() {
            @Override
            public void onEditTextPressed() {
                if(!Perfence.getPerfence(selfUserId).equalsIgnoreCase(selfUserId)){
                    LockUserId userIdBody = new LockUserId();
                    userIdBody.setUserid(selfUserId);
                    userIdBody.setUsername(Perfence.getPerfence(selfUserId));
                    mSmartLockManager.setLockUidNameHttp(mSmartLockManager.getCurrentSelectLock().getUid(), userIdBody);
                    UpdateSmartLockUserIdActivity.this.finish();
                }
            }
        });
        isStartFromExperience = DeviceManager.getInstance().isStartFromExperience();
        mSmartLockManager=SmartLockManager.getInstance();
        mSmartLockManager.InitSmartLockManager(this);
        mIds = new ArrayList<>();
        if (isStartFromExperience) {
            mIds.add("001");
            mIds.add("002");
            mIds.add("003");
        } else {
            userName=  Perfence.getPerfence(Perfence.PERFENCE_PHONE);
            mIds = getIntent().getStringArrayListExtra("recordlistid");
        }
        Log.i(TAG,"isStartFromExperience="+isStartFromExperience);
        Log.i(TAG,"mIds="+mIds.size());
        mUserIdAdapter = new UserIdAdapter(this, mIds);
    }

    @Override
    protected void onResume() {
        super.onResume();
        selfUserId=Perfence.getPerfence(AppConstant.PERFENCE_LOCK_SELF_USERID);
    }

    private void initViews() {
        layout_title= findViewById(R.id.layout_title);
        listview_update_ids = findViewById(R.id.listview_update_ids);
    }


}
