package com.deplink.boruSmart.activity.device.doorbell;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;

import com.deplink.boruSmart.manager.device.doorbeel.DoorBellListener;
import com.deplink.boruSmart.manager.device.doorbeel.DoorbeelManager;
import com.deplink.boruSmart.view.imageview.ZoomImageView;

import deplink.com.smartwirelessrelay.homegenius.EllESDK.R;

public class DoorbellLargeImage extends Activity implements View.OnClickListener{
    private ZoomImageView image_snap;
    private DoorbeelManager mDoorbeelManager;
    private DoorBellListener mDoorBellListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doorbell_large_image);
        initViews();
        initDatas();
        initEvents();
    }

    private void initEvents() {
    }
    private void initDatas() {
        mDoorbeelManager = DoorbeelManager.getInstance();
        mDoorbeelManager.InitDoorbeelManager(this);
        mDoorBellListener = new DoorBellListener() {
            public void responseVisitorImage(Bitmap bitmap, int count) {
                super.responseVisitorImage(bitmap, count);
                image_snap.setImageBitmap(bitmap);
            }
        };
        if(getIntent().getStringExtra("file")!=null){
            String filename=getIntent().getStringExtra("file");
            mDoorbeelManager.getDoorbellVistorImage(filename, 0);
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        mDoorbeelManager.addDeviceListener(mDoorBellListener);
    }
    @Override
    protected void onPause() {
        super.onPause();
        mDoorbeelManager.removeDeviceListener(mDoorBellListener);
    }
    private void initViews() {
        image_snap= findViewById(R.id.image_snap);
    }
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.layout_root:
                onBackPressed();
                break;
        }
    }
}
