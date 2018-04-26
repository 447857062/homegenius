package com.deplink.boruSmart.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.deplink.boruSmart.Protocol.json.device.SmartDev;
import com.deplink.boruSmart.Protocol.json.device.getway.GatwayDevice;
import com.deplink.boruSmart.Protocol.json.device.share.UserShareInfo;
import com.deplink.boruSmart.activity.device.ShareDeviceActivity;
import com.deplink.boruSmart.activity.device.adapter.SharedDeviceListAdapter;
import com.deplink.boruSmart.constant.DeviceTypeConstant;
import com.deplink.boruSmart.manager.device.DeviceListener;
import com.deplink.boruSmart.manager.device.DeviceManager;
import com.deplink.boruSmart.manager.device.doorbeel.DoorbeelManager;
import com.deplink.boruSmart.manager.device.getway.GetwayManager;
import com.deplink.boruSmart.manager.device.light.SmartLightManager;
import com.deplink.boruSmart.manager.device.router.RouterManager;
import com.deplink.boruSmart.manager.device.smartlock.SmartLockManager;
import com.deplink.boruSmart.manager.device.smartswitch.SmartSwitchManager;
import com.deplink.boruSmart.util.JsonArrayParseUtil;
import com.deplink.boruSmart.util.WeakRefHandler;
import com.deplink.boruSmart.view.combinationwidget.TitleLayout;
import com.deplink.boruSmart.view.scrollview.NonScrollableListView;

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
    private NonScrollableListView shareddevices_list;
    private boolean isStartFromExperience;
    private DeviceManager mDeviceManager;
    private DeviceListener mDeviceListener;
    private RelativeLayout layout_empty_share_device;
    private ImageView refresh_image;

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

    Animation animationFadeIn;

    @Override
    protected void onResume() {
        super.onResume();
        querySmartDeviceShareInfo = false;
        mDeviceManager.addDeviceListener(mDeviceListener);
        if (!(getwayDevices.size() == 0 && smartDevices.size()==0)) {
            refresh_image.setVisibility(View.VISIBLE);
            animationFadeIn = AnimationUtils.loadAnimation(this, R.anim.rotate_find_device);
            refresh_image.setAnimation(animationFadeIn);
        }
     /*   myScrollView.setmOnScrollViewPull(new MyScrollView.OnScrollViewPull() {
            @Override
            public void onScrollViewPull(int offset) {
                if(offset<0){
                    animationFadeIn.cancel();
                    refresh_image.setVisibility(View.GONE);
                    refresh_image.setBackground(null);
                }
            }
        });*/
    }

    private static final int MSG_UPDATE_SHARE_DEVS = 100;
    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_UPDATE_SHARE_DEVS:
                    adapter.setTopList(datasTop);
                    adapter.setBottomList(datasBottom);
                    if (datasTop.size() + datasBottom.size() == 0) {
                        layout_empty_share_device.setVisibility(View.VISIBLE);
                    } else {
                        layout_empty_share_device.setVisibility(View.GONE);
                    }
                    adapter.notifyDataSetChanged();
                    animationFadeIn.cancel();
                    refresh_image.setVisibility(View.GONE);
                    break;
            }
            return true;
        }
    };
    private Handler mHandler = new WeakRefHandler(mCallback);


    @Override
    protected void onPause() {
        super.onPause();
        mDeviceManager.removeDeviceListener(mDeviceListener);
    }

    private int indexCurrentGatway = 0;
    private int indexCurrentSmartDevcie = 0;

    private void initDatas() {
        mDeviceManager = DeviceManager.getInstance();
        mDeviceManager.InitDeviceManager(this);
        datasTop = new ArrayList<>();
        datasBottom = new ArrayList<>();
        smartDevices = DataSupport.findAll(SmartDev.class);
        getwayDevices = DataSupport.findAll(GatwayDevice.class);
        if (getwayDevices.size() > 0) {
            mDeviceManager.readDeviceShareInfo(getwayDevices.get(0).getUid());
        } else {
            if (smartDevices.size() > 0) {
                mDeviceManager.readDeviceShareInfo(smartDevices.get(0).getUid());
            } else {
                layout_empty_share_device.setVisibility(View.VISIBLE);
                refresh_image.setVisibility(View.GONE);
            }
        }
        adapter = new SharedDeviceListAdapter(this, datasTop, datasBottom);
        shareddevices_list.setAdapter(adapter);
        shareddevices_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                String deviceTypedata;
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

                    deviceTypedata=""+datasBottom.get(position - datasTop.size()).getType();
                    deviceUid = datasBottom.get(position - datasTop.size()).getUid();
                } else {
                    GetwayManager.getInstance().setCurrentSelectGetwayDevice(datasTop.get(position));
                    deviceTypedata=DeviceTypeConstant.TYPE.TYPE_SMART_GETWAY;
                    deviceUid = datasTop.get(position).getUid();
                }
                if (isStartFromExperience) {
                    ShareDeviceActivity.actionStart(SharedDeviceListActivity.this,deviceTypedata,deviceUid);
                } else {
                    ShareDeviceActivity.actionStart(SharedDeviceListActivity.this,deviceTypedata,deviceUid);
                }
            }
        });
        mDeviceListener = new DeviceListener() {
            @Override
            public void responseGetDeviceShareInfo(String result) {
                super.responseGetDeviceShareInfo(result);
                if (result.contains("errcode")) {

                } else {
                    List<UserShareInfo> response = JsonArrayParseUtil.jsonToArrayList(result, UserShareInfo.class);
                    if (response != null) {
                        if (!querySmartDeviceShareInfo) {
                            if (getwayDevices.size() != 0) {
                                if (response.size() >= 2) {
                                    datasTop.add(getwayDevices.get(indexCurrentGatway));
                                }
                                indexCurrentGatway++;
                                if (getwayDevices.size() > indexCurrentGatway) {
                                    mDeviceManager.readDeviceShareInfo(getwayDevices.get(indexCurrentGatway).getUid());
                                } else {
                                    querySmartDeviceShareInfo = true;
                                    if(smartDevices.size()>(indexCurrentGatway)){
                                        mDeviceManager.readDeviceShareInfo(smartDevices.get(indexCurrentSmartDevcie).getUid());
                                    }
                                }

                            } else {
                                if (response.size() >= 2) {
                                    datasBottom.add(smartDevices.get(indexCurrentSmartDevcie));
                                }
                                indexCurrentSmartDevcie++;
                                if (smartDevices.size() > indexCurrentSmartDevcie) {
                                    mDeviceManager.readDeviceShareInfo(smartDevices.get(indexCurrentSmartDevcie).getUid());
                                }
                            }
                        } else {
                            if (response.size() >= 2) {
                                datasBottom.add(smartDevices.get(indexCurrentSmartDevcie));
                            }
                            indexCurrentSmartDevcie++;
                            if (smartDevices.size() > indexCurrentSmartDevcie) {
                                mDeviceManager.readDeviceShareInfo(smartDevices.get(indexCurrentSmartDevcie).getUid());
                            }
                        }
                    }
                    mHandler.sendEmptyMessageDelayed(MSG_UPDATE_SHARE_DEVS, 1500);
                }
            }


        };
    }

    private boolean querySmartDeviceShareInfo = false;

    private void initViews() {
        layout_title = findViewById(R.id.layout_title);
        shareddevices_list = findViewById(R.id.shareddevices_list);
        layout_empty_share_device = findViewById(R.id.layout_empty_share_device);
        refresh_image = findViewById(R.id.refresh_image);
    }
}
