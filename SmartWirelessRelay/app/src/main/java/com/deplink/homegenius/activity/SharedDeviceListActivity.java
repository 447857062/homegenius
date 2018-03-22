package com.deplink.homegenius.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.deplink.homegenius.Protocol.json.device.SmartDev;
import com.deplink.homegenius.Protocol.json.device.getway.GatwayDevice;
import com.deplink.homegenius.activity.device.ShareDeviceActivity;
import com.deplink.homegenius.activity.device.adapter.SharedDeviceListAdapter;
import com.deplink.homegenius.constant.DeviceTypeConstant;
import com.deplink.homegenius.manager.device.DeviceManager;
import com.deplink.homegenius.manager.device.doorbeel.DoorbeelManager;
import com.deplink.homegenius.manager.device.getway.GetwayManager;
import com.deplink.homegenius.manager.device.light.SmartLightManager;
import com.deplink.homegenius.manager.device.router.RouterManager;
import com.deplink.homegenius.manager.device.smartlock.SmartLockManager;
import com.deplink.homegenius.manager.device.smartswitch.SmartSwitchManager;
import com.deplink.homegenius.view.combinationwidget.TitleLayout;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

import deplink.com.smartwirelessrelay.homegenius.EllESDK.R;

public class SharedDeviceListActivity extends Activity {
    private static final String TAG = "SharedDeviceList";
    private TitleLayout layout_title;
    private List<SmartDev> smartDevices;
    private List<GatwayDevice> getwayDevices;
    /**
     * 上面半部分列表的数据
     */
    private List<GatwayDevice> datasTop;
    /**
     * 下面半部分列表的数据
     */
    private List<SmartDev> datasBottom;
    private SharedDeviceListAdapter adapter;
    private ListView shareddevices_list;
    private boolean isStartFromExperience;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shared_device);
        initViews();
        initDatas();
        initEvents();
    }

    private void initEvents() {
        layout_title.setReturnClickListener(new TitleLayout.ReturnImageClickListener() {
            @Override
            public void onBackPressed() {
                SharedDeviceListActivity.this.onBackPressed();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        isStartFromExperience = DeviceManager.getInstance().isStartFromExperience();
    }

    private void initDatas() {
        datasTop = new ArrayList<>();
        datasBottom = new ArrayList<>();
        smartDevices = DataSupport.findAll(SmartDev.class);
        getwayDevices = DataSupport.findAll(GatwayDevice.class);
        for (int i = 0; i < getwayDevices.size(); i++) {
            if (getwayDevices.get(i).isShared()) {
                datasTop.add(getwayDevices.get(i));
            }
        }
        for (int i = 0; i < smartDevices.size(); i++) {
            if (smartDevices.get(i).isShared()) {
                datasBottom.add(smartDevices.get(i));
            }
        }
        View viewEmpty= LayoutInflater.from(this).inflate(R.layout.sharedevice_empty_view,null);
        adapter = new SharedDeviceListAdapter(this, datasTop, datasBottom);
        ((ViewGroup)shareddevices_list.getParent()).addView(viewEmpty);
        shareddevices_list.setEmptyView(viewEmpty);
        shareddevices_list.setAdapter(adapter);
        shareddevices_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Intent inentShareDevice = new Intent(SharedDeviceListActivity.this, ShareDeviceActivity.class);
                String deviceUid;
                if (datasTop.size() < (position + 1)) {
                    switch (datasBottom.get(position - datasTop.size()).getType()) {
                        case DeviceTypeConstant.TYPE.TYPE_MENLING:
                            DoorbeelManager.getInstance().setCurrentSelectedDoorbeel(datasBottom.get(position - datasTop.size()));
                            break;
                        case DeviceTypeConstant.TYPE.TYPE_SWITCH:
                            SmartSwitchManager.getInstance().setCurrentSelectSmartDevice(datasBottom.get(position - datasTop.size()));
                            break;
                        case DeviceTypeConstant.TYPE.TYPE_LIGHT:
                            SmartLightManager.getInstance().setCurrentSelectLight(datasBottom.get(position - datasTop.size()));
                            break;
                        case DeviceTypeConstant.TYPE.TYPE_LOCK:
                            SmartLockManager.getInstance().setCurrentSelectLock(datasBottom.get(position - datasTop.size()));
                            break;
                        case DeviceTypeConstant.TYPE.TYPE_REMOTECONTROL:
                            DoorbeelManager.getInstance().setCurrentSelectedDoorbeel(datasBottom.get(position - datasTop.size()));
                            break;
                        case DeviceTypeConstant.TYPE.TYPE_ROUTER:
                            RouterManager.getInstance().setCurrentSelectedRouter(datasBottom.get(position - datasTop.size()));
                            break;
                    }
                    inentShareDevice.putExtra("devicetype", datasBottom.get(position - datasTop.size()).getType());
                    deviceUid = datasBottom.get(position - datasTop.size()).getUid();
                } else {
                    GetwayManager.getInstance().setCurrentSelectGetwayDevice(datasTop.get(position));
                    inentShareDevice.putExtra("devicetype", DeviceTypeConstant.TYPE.TYPE_SMART_GETWAY);
                    deviceUid = datasTop.get(position).getUid();
                }
                if (isStartFromExperience) {
                    startActivity(inentShareDevice);
                } else {
                    inentShareDevice.putExtra("deviceuid", deviceUid);
                    startActivity(inentShareDevice);

                }
            }
        });
    }

    private void initViews() {
        layout_title = findViewById(R.id.layout_title);
        shareddevices_list = findViewById(R.id.shareddevices_list);
    }
}
