package com.deplink.boruSmart.activity.personal;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.deplink.boruSmart.Protocol.json.device.SmartDev;
import com.deplink.boruSmart.Protocol.json.device.getway.GatwayDevice;
import com.deplink.boruSmart.activity.device.getway.GetwayDeviceActivity;
import com.deplink.boruSmart.activity.device.getway.adapter.HomeNetWorkAdapter;
import com.deplink.boruSmart.activity.device.router.RouterMainActivity;
import com.deplink.boruSmart.activity.personal.login.LoginActivity;
import com.deplink.boruSmart.constant.AppConstant;
import com.deplink.boruSmart.manager.device.DeviceManager;
import com.deplink.boruSmart.manager.device.getway.GetwayManager;
import com.deplink.boruSmart.manager.device.router.RouterManager;
import com.deplink.boruSmart.util.Perfence;
import com.deplink.boruSmart.view.combinationwidget.TitleLayout;
import com.deplink.boruSmart.view.scrollview.NonScrollableListView;
import com.deplink.sdk.android.sdk.DeplinkSDK;
import com.deplink.sdk.android.sdk.EventCallback;
import com.deplink.sdk.android.sdk.SDKAction;
import com.deplink.sdk.android.sdk.manager.SDKManager;

import java.util.ArrayList;
import java.util.List;

import deplink.com.smartwirelessrelay.homegenius.EllESDK.R;

/**
 * 家庭网络
 */
public class HomeNetWorkActivity extends Activity implements  AdapterView.OnItemClickListener {
    private static final String TAG = "HomeNetworkActivity";
    private NonScrollableListView listviewNetworkDevices;
    private List<GatwayDevice> mGatwayDevices;
    private HomeNetWorkAdapter mAdapter;
    private GetwayManager getwayManager;
    private List<SmartDev> mRouterDevice;
    private TitleLayout layout_title;
    private boolean isUserLogin;
    private SDKManager manager;
    private EventCallback ec;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_getway_check_list);
        initViews();
        initDatas();
        initEvents();
    }
    private void initDatas() {
        getwayManager = GetwayManager.getInstance();
        mRouterDevice = new ArrayList<>();
        mRouterDevice = RouterManager.getInstance().getAllRouterDevice();
        mGatwayDevices = new ArrayList<>();
        mGatwayDevices=getwayManager.getAllGetwayDevice();
        View viewEmpty= LayoutInflater.from(this).inflate(R.layout.homenetwork_empty_view,null);
        ((ViewGroup)listviewNetworkDevices.getParent()).addView(viewEmpty);
        listviewNetworkDevices.setEmptyView(viewEmpty);
        mAdapter = new HomeNetWorkAdapter(this, mGatwayDevices, mRouterDevice);
        listviewNetworkDevices.setAdapter(mAdapter);
        layout_title.setReturnClickListener(new TitleLayout.ReturnImageClickListener() {
            @Override
            public void onBackPressed() {
                HomeNetWorkActivity.this.onBackPressed();
            }
        });
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
            public void onGetImageSuccess(SDKAction action, final Bitmap bm) {


            }


            @Override
            public void onFailure(SDKAction action, Throwable throwable) {

            }
            @Override
            public void connectionLost(Throwable throwable) {
                super.connectionLost(throwable);

                isUserLogin=false;

            }
        };
    }

    private void initEvents() {
        listviewNetworkDevices.setAdapter(mAdapter);
        listviewNetworkDevices.setOnItemClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        manager.addEventCallback(ec);
        isUserLogin = Perfence.getBooleanPerfence(AppConstant.USER_LOGIN);
        mRouterDevice = RouterManager.getInstance().getAllRouterDevice();
        mGatwayDevices=getwayManager.getAllGetwayDevice();
        mAdapter.setTopList(mGatwayDevices);
        mAdapter.setBottomList(mRouterDevice);
        mAdapter.notifyDataSetChanged();
        if(!isUserLogin){
            Intent intent=new Intent(HomeNetWorkActivity.this, LoginActivity.class);
            intent.putExtra("startfrom","userinfoactivity");
            startActivity(intent);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        manager.removeEventCallback(ec);
    }
    private void initViews() {
        listviewNetworkDevices = findViewById(R.id.listview_getway_devices);
        layout_title= findViewById(R.id.layout_title);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        DeviceManager.getInstance().setStartFromExperience(false);
        if (mGatwayDevices.size() < (position + 1)) {
            RouterManager.getInstance().setCurrentSelectedRouter(mRouterDevice.get(position - mGatwayDevices.size()));
            startActivity(new Intent(HomeNetWorkActivity.this, RouterMainActivity.class));
        }else{
            GetwayManager.getInstance().setCurrentSelectGetwayDevice(mGatwayDevices.get(position));
            startActivity(new Intent(HomeNetWorkActivity.this, GetwayDeviceActivity.class));
        }
    }
}
