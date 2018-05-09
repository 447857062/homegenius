package com.deplink.boruSmart.activity.device.doorbell.add;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.deplink.boruSmart.activity.device.AddDeviceNameActivity;
import com.deplink.boruSmart.constant.DeviceTypeConstant;
import com.deplink.boruSmart.manager.device.doorbeel.DoorbeelManager;
import com.deplink.boruSmart.util.qrcode.qrcodecapture.CaptureActivity;
import com.deplink.boruSmart.view.combinationwidget.TitleLayout;
import com.deplink.boruSmart.view.toast.Ftoast;

import deplink.com.smartwirelessrelay.homegenius.EllESDK.R;

public class DoorBellConfigWifiTypeSelect extends Activity implements View.OnClickListener{
    private TitleLayout layout_title;
    private ImageView imageview_doorbell_configwifi;
    private ImageView imageview_doorbell_add_configedwifi;
    private ImageView imageview_instrution;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_door_bell_config_wifi_type_select);
        initViews();
        initEvents();
        initDatas();
    }

    private void initDatas() {

    }

    private void initEvents() {
        layout_title.setReturnClickListener(new TitleLayout.ReturnImageClickListener() {
            @Override
            public void onBackPressed() {
                DoorBellConfigWifiTypeSelect.this.onBackPressed();
            }
        });
        imageview_doorbell_configwifi.setOnClickListener(this);
        imageview_doorbell_add_configedwifi.setOnClickListener(this);
        imageview_instrution.setOnClickListener(this);
    }

    private void initViews() {
        layout_title = findViewById(R.id.layout_title);
        imageview_doorbell_configwifi = findViewById(R.id.imageview_doorbell_configwifi);
        imageview_doorbell_add_configedwifi = findViewById(R.id.imageview_doorbell_add_configedwifi);
        imageview_instrution = findViewById(R.id.imageview_instrution);
    }
    public final static int REQUEST_CODE_DEVICE_QRCODE = 1;
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.imageview_doorbell_configwifi:
                startActivity(new Intent(this,ApModeActivity.class));
                break;
            case R.id.imageview_doorbell_add_configedwifi:
                Intent intentQrcodeSn = new Intent();
                intentQrcodeSn.setClass(DoorBellConfigWifiTypeSelect.this, CaptureActivity.class);
                intentQrcodeSn.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intentQrcodeSn.putExtra("requestType", REQUEST_CODE_DEVICE_QRCODE);
                startActivityForResult(intentQrcodeSn, REQUEST_CODE_DEVICE_QRCODE);
                break;
            case R.id.imageview_instrution:
                startActivity(new Intent(this,PairingGuideOneActivity.class));
                break;
           default:
                break;

        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Intent intent = new Intent(DoorBellConfigWifiTypeSelect.this, AddDeviceNameActivity.class);
        if (resultCode == RESULT_OK) {
            String qrCodeResult = data.getStringExtra("deviceSN");
            switch (requestCode) {
                case REQUEST_CODE_DEVICE_QRCODE:
                    //
                    if (qrCodeResult.length() >= 16  ) {//网关,路由器
                        intent.putExtra("currentAddDevice", qrCodeResult);
                        intent.putExtra("DeviceType", DeviceTypeConstant.TYPE.TYPE_MENLING);
                        DoorbeelManager.getInstance().setMac(qrCodeResult);
                        startActivity(intent);
                    } else {
                        Ftoast.create(this).setText("不支持的设备").setDuration(Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    }
}
