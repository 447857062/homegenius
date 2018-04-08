package com.deplink.boruSmart.activity.device.getway.add;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.deplink.boruSmart.Protocol.json.device.getway.GatwayDevice;
import com.deplink.boruSmart.activity.device.DevicesActivity;
import com.deplink.boruSmart.manager.connect.local.udp.UdpManager;
import com.deplink.boruSmart.manager.connect.local.udp.interfaces.UdpManagerGetIPLintener;
import com.deplink.boruSmart.manager.device.getway.GetwayManager;
import com.deplink.boruSmart.util.WeakRefHandler;
import com.deplink.boruSmart.view.combinationwidget.TitleLayout;
import com.deplink.boruSmart.activity.device.getway.wifi.ScanWifiListActivity;

import deplink.com.smartwirelessrelay.homegenius.EllESDK.R;

/**
 * 获取所有的网关
 * 判断当前要添加的网关要不要配置wifi
 */
public class QueryGetwaysActivity extends Activity implements View.OnClickListener, UdpManagerGetIPLintener {
    private static final String TAG = "QueryGetwaysActivity";
    private static final int MSG_CHECK_GETWAY_OK = 100;
    private Button textview_cancel;
    private UdpManager mUdpmanager;
    private TitleLayout layout_title;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_query_getways);
        initViews();
        initDatas();
        initEvents();
    }

    private void initEvents() {
        textview_cancel.setOnClickListener(this);
    }

    private void initViews() {
        textview_cancel = (Button) findViewById(R.id.textview_cancel);
        layout_title= (TitleLayout) findViewById(R.id.layout_title);
    }

    private void initDatas() {
        layout_title.setReturnClickListener(new TitleLayout.ReturnImageClickListener() {
            @Override
            public void onBackPressed() {
                QueryGetwaysActivity.this.onBackPressed();
            }
        });
        String currentAddDevice = getIntent().getStringExtra("currentAddDevice");
        GetwayManager.getInstance().setCurrentAddDevice(currentAddDevice);
        mUdpmanager = UdpManager.getInstance();
        mUdpmanager.InitUdpConnect(this, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mUdpmanager.registerNetBroadcast(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUdpmanager.unRegisterNetBroadcast(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.textview_cancel:
                startActivity(new Intent(this, DevicesActivity.class));
                break;
        }
    }

    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_CHECK_GETWAY_OK:
                    GatwayDevice device = new GatwayDevice();
                    device.setIpAddress((String) msg.obj);
                    Toast.makeText(QueryGetwaysActivity.this, "检查到IP为:" + msg.obj + "的网关", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(QueryGetwaysActivity.this, ScanWifiListActivity.class);
                    startActivity(intent);
                    break;
            }
            return true;
        }
    };
    private Handler mHandler = new WeakRefHandler(mCallback);
    @Override
    public void onGetLocalConnectIp(String ipAddress, String uid) {
        Log.i(TAG, "检查网关，获取到IP地址=" + ipAddress);
        Message msg = Message.obtain();
        msg.what = MSG_CHECK_GETWAY_OK;
        msg.obj = ipAddress;
        mHandler.sendMessage(msg);
    }
}
