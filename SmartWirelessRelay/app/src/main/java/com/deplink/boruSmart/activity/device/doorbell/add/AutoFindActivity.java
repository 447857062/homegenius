package com.deplink.boruSmart.activity.device.doorbell.add;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.deplink.boruSmart.Protocol.packet.ellisdk.BasicPacket;
import com.deplink.boruSmart.Protocol.packet.ellisdk.EllESDK;
import com.deplink.boruSmart.Protocol.packet.ellisdk.EllE_Listener;
import com.deplink.boruSmart.activity.device.AddDeviceNameActivity;
import com.deplink.boruSmart.activity.device.doorbell.add.wifilistadapter.MacListAdapter;
import com.deplink.boruSmart.constant.DeviceTypeConstant;
import com.deplink.boruSmart.manager.device.doorbeel.DoorbeelManager;
import com.deplink.boruSmart.util.DataExchange;
import com.deplink.boruSmart.util.WeakRefHandler;
import com.deplink.boruSmart.util.qrcode.qrcodecapture.CaptureActivity;
import com.deplink.boruSmart.view.combinationwidget.TitleLayout;
import com.deplink.boruSmart.view.toast.Ftoast;

import java.util.ArrayList;
import java.util.List;

import deplink.com.smartwirelessrelay.homegenius.EllESDK.R;

public class AutoFindActivity extends AppCompatActivity implements View.OnClickListener, EllE_Listener {
    private static  final String TAG="AutoFindActivity";
    private TitleLayout layout_title;
    private ImageView imageview_gif;
    private Button button_scan_qrcode;
    private Button button_re_search;
    private ListView listview_find_doorbell;
    private List<String> macList;
    private EllESDK ellESDK;
    private MacListAdapter adapter;
    private FrameLayout frame_gif_inner;
    private TextView textview_tips;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto_find);
        initViews();
        initEvents();
        initDatas();
    }
    private void initDatas() {
        layout_title.setReturnClickListener(new TitleLayout.ReturnImageClickListener() {
            @Override
            public void onBackPressed() {
                AutoFindActivity.this.onBackPressed();
            }
        });
        macList = new ArrayList<>();
        adapter = new MacListAdapter(this, macList);
    }

    private static  final int button_re_search_state_searching=1;
    private static  final int button_re_search_state_stoped=2;
    private int current_button_re_search_state;
    private void initEvents() {
        showAnim();
        button_scan_qrcode.setOnClickListener(this);
        button_re_search.setOnClickListener(this);
        listview_find_doorbell.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String mac = macList.get(position);
                Intent intent = new Intent(AutoFindActivity.this, AddDeviceNameActivity.class);
                intent.putExtra("currentAddDevice", mac);
                intent.putExtra("DeviceType", DeviceTypeConstant.TYPE.TYPE_MENLING);
                DoorbeelManager.getInstance().setMac(mac);
                startActivity(intent);
            }
        });
        ellESDK = EllESDK.getInstance();
        ellESDK.InitEllESDK(this, this);
        listview_find_doorbell.setAdapter(adapter);
    }

    private void showAnim() {
        RotateAnimation animation;
        int magnify = 10000;
        int toDegrees = 360;
        int duration = 1000;
        toDegrees *= magnify;
        duration *= magnify;
        animation = new RotateAnimation(0, toDegrees,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setDuration(duration);
        LinearInterpolator lir = new LinearInterpolator();
        animation.setInterpolator(lir);
        animation.setRepeatCount(Animation.INFINITE);
        animation.setRepeatMode(Animation.RESTART);
        imageview_gif.startAnimation(animation);

    }

    private void initViews() {
        layout_title = (TitleLayout) findViewById(R.id.layout_title);
        imageview_gif = (ImageView) findViewById(R.id.imageview_gif);
        button_scan_qrcode = (Button) findViewById(R.id.button_scan_qrcode);
        button_re_search = (Button) findViewById(R.id.button_re_search);
        listview_find_doorbell = (ListView) findViewById(R.id.listview_find_doorbell);
        frame_gif_inner = (FrameLayout) findViewById(R.id.frame_gif_inner);
        textview_tips = (TextView) findViewById(R.id.textview_tips);
    }

    public final static int REQUEST_CODE_DEVICE_QRCODE = 1;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_scan_qrcode:
                Intent intentQrcodeSn = new Intent();
                intentQrcodeSn.setClass(AutoFindActivity.this, CaptureActivity.class);
                intentQrcodeSn.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intentQrcodeSn.putExtra("requestType", REQUEST_CODE_DEVICE_QRCODE);
                startActivityForResult(intentQrcodeSn, REQUEST_CODE_DEVICE_QRCODE);
                break;
            case R.id.button_re_search:
                //显示搜索的动画
                switch (current_button_re_search_state){
                    case button_re_search_state_searching:
                        current_button_re_search_state=button_re_search_state_stoped;
                        //继续搜索的步骤
                        frame_gif_inner.setVisibility(View.GONE);
                        ellESDK.stopSearchDevs();
                        button_re_search.setBackgroundResource(R.drawable.login_button_enable_background);
                        button_re_search.setText("继续搜索");
                        break;
                    case button_re_search_state_stoped:
                        frame_gif_inner.setVisibility(View.VISIBLE);
                        textview_tips.setText("确保手机和门铃连接到同一个WiFi网络");
                        current_button_re_search_state=button_re_search_state_searching;
                        showAnim();
                        //停止搜索
                        button_re_search.setText("暂停搜索");
                        button_re_search.setBackgroundResource(R.drawable.button_read_background);
                        ellESDK.startSearchDevs();
                        break;
                        default:
                            break;
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Intent intent = new Intent(AutoFindActivity.this, AddDeviceNameActivity.class);
        if (resultCode == RESULT_OK) {
            String qrCodeResult = data.getStringExtra("deviceSN");
            switch (requestCode) {
                case REQUEST_CODE_DEVICE_QRCODE:
                    //{"org":"ismart","tp":"MINI_DOORBELL","ad":"00-FF-02-00-1F-25-21-00","ver":"1"}
                    if (qrCodeResult.length() >= 16) {//网关,路由器
                        intent.putExtra("currentAddDevice", qrCodeResult);
                        intent.putExtra("DeviceType", DeviceTypeConstant.TYPE.TYPE_MENLING);
                        intent.putExtra("showTips", false);
                        DoorbeelManager.getInstance().setMac(qrCodeResult);
                        startActivity(intent);
                    } else {
                        Ftoast.create(this).setText("不支持的设备").setDuration(Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        ellESDK.startSearchDevs();
        macList.clear();
        current_button_re_search_state=button_re_search_state_searching;
        textview_tips.setText("确保手机和门铃连接到同一个WiFi网络");
    }
    private static final int MSG_GET_DEVS = 0x01;
    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_GET_DEVS:
                    textview_tips.setText("请选择要添加的门铃设备");
                    listview_find_doorbell.setVisibility(View.VISIBLE);
                    adapter = new MacListAdapter(AutoFindActivity.this, macList);
                    listview_find_doorbell.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                    break;
            }
            return true;
        }
    };
    private Handler mHandler = new WeakRefHandler(mCallback);
    @Override
    public void onRecvEllEPacket(BasicPacket packet) {
        String mac = DataExchange.byteArrayToHexString(DataExchange.longToEightByte(packet.mac));
        if (!macList.contains(mac)) {
            macList.add(mac);
            Log.i(TAG,"macList="+macList.size());
            Message msg=Message.obtain();
            msg.obj=mac;
            msg.what=MSG_GET_DEVS;
            mHandler.sendMessage(msg);
        }
    }

    @Override
    public void searchDevCBS(long mac, byte type, byte ver) {
        String findmac = DataExchange.byteArrayToHexString(DataExchange.longToEightByte(mac));
        if (!macList.contains(findmac)) {
            Log.i(TAG,"macList="+macList.size());
            macList.add(findmac);
            Message msg=Message.obtain();
            msg.obj=findmac;
            msg.what=MSG_GET_DEVS;
            mHandler.sendMessage(msg);
        }

    }
}
