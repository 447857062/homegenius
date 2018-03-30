package com.deplink.homegenius.activity.personal;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.deplink.homegenius.Protocol.json.device.SmartDev;
import com.deplink.homegenius.Protocol.json.device.getway.GatwayDevice;
import com.deplink.homegenius.activity.device.getway.GetwayDeviceActivity;
import com.deplink.homegenius.activity.device.getway.adapter.HomeNetWorkAdapter;
import com.deplink.homegenius.activity.device.router.RouterMainActivity;
import com.deplink.homegenius.manager.device.DeviceManager;
import com.deplink.homegenius.manager.device.getway.GetwayManager;
import com.deplink.homegenius.manager.device.router.RouterManager;
import com.deplink.homegenius.view.combinationwidget.TitleLayout;
import com.deplink.homegenius.view.scrollview.NonScrollableListView;

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
    }

    private void initEvents() {
        listviewNetworkDevices.setAdapter(mAdapter);
        listviewNetworkDevices.setOnItemClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mRouterDevice = RouterManager.getInstance().getAllRouterDevice();
        mGatwayDevices=getwayManager.getAllGetwayDevice();
        mAdapter.setTopList(mGatwayDevices);
        mAdapter.setBottomList(mRouterDevice);
        mAdapter.notifyDataSetChanged();
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
