package com.deplink.boruSmart.activity.device.router;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.deplink.boruSmart.Protocol.json.device.SmartDev;
import com.deplink.boruSmart.activity.device.router.adapter.BlackListAdapter;
import com.deplink.boruSmart.activity.device.router.adapter.ConnectedDeviceListAdapter;
import com.deplink.boruSmart.activity.personal.login.LoginActivity;
import com.deplink.boruSmart.constant.AppConstant;
import com.deplink.boruSmart.manager.connect.remote.HomeGenius;
import com.deplink.boruSmart.manager.device.DeviceManager;
import com.deplink.boruSmart.manager.device.router.RouterManager;
import com.deplink.boruSmart.util.NetUtil;
import com.deplink.boruSmart.util.Perfence;
import com.deplink.boruSmart.util.WeakRefHandler;
import com.deplink.boruSmart.view.combinationwidget.TitleLayout;
import com.deplink.boruSmart.view.dialog.AlertDialog;
import com.deplink.boruSmart.view.dialog.InputAlertDialog;
import com.deplink.boruSmart.view.listview.swipemenulistview.SwipeMenu;
import com.deplink.boruSmart.view.listview.swipemenulistview.SwipeMenuCreator;
import com.deplink.boruSmart.view.listview.swipemenulistview.SwipeMenuItem;
import com.deplink.boruSmart.view.listview.swipemenulistview.SwipeMenuListView;
import com.deplink.boruSmart.view.toast.Ftoast;
import com.deplink.sdk.android.sdk.DeplinkSDK;
import com.deplink.sdk.android.sdk.EventCallback;
import com.deplink.sdk.android.sdk.SDKAction;
import com.deplink.sdk.android.sdk.json.BLACKLIST;
import com.deplink.sdk.android.sdk.json.DeviceControl;
import com.deplink.sdk.android.sdk.json.DevicesOnline;
import com.deplink.sdk.android.sdk.json.DevicesOnlineRoot;
import com.deplink.sdk.android.sdk.json.PERFORMANCE;
import com.deplink.sdk.android.sdk.json.WHITELIST;
import com.deplink.sdk.android.sdk.manager.SDKManager;
import com.deplink.sdk.android.sdk.mqtt.MQTTController;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import deplink.com.smartwirelessrelay.homegenius.EllESDK.R;

public class RouterMainActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "RouterMainActivity";
    private static final int TIME_DIFFERENCE_BETWEEN_MESSAGE_INTERVALS = 3000;
    private int refreshCount = 0;
    /**
     * 设备上线下线监控，2次发送没有回数据就认为设备下线,所以是大于1
     */
    private static final int TIME_OUT_WATCHDOG_MAXCOUNT = 1;
    /**
     * 已连接设备
     */
    private SwipeMenuListView listview_device_list;
    private List<DevicesOnline> mConnectedDevices;
    private ConnectedDeviceListAdapter mAdapter;
    private SwipeMenuListView listview_black_list;
    private List<BLACKLIST> mBlackListDatas;
    private BlackListAdapter mBlackListAdapter;
    private RelativeLayout layout_blak_list;
    private RelativeLayout layout_connected_devices;
    private TextView textview_show_query_device_result;
    private SDKManager manager;
    private EventCallback ec;
    private boolean isUserLogin;
    private boolean isSetBlackList = false;
    private boolean isRemoveBlackList = false;
    private Timer refreshTimer = null;
    private TimerTask refreshTask = null;
    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {

            return true;
        }
    };
    private Handler mHandler = new WeakRefHandler(mCallback);
    private TextView textview_show_blacklist_device_result;
    private RelativeLayout layout_no_blacklist;
    private RelativeLayout layout_no_connected_device;
    private FrameLayout frame_blacklist_content;
    private FrameLayout frame_devicelist_content_content;
    private TextView textview_cpu_use;
    private TextView textview_memory_use;
    private TextView textview_upload_speed;
    private TextView textview_download_speend;
    private RouterManager mRouterManager;
    private TextView textview_connected_devices;
    private TextView textview_blak_list;
    /**
     * 检查使用本地接口的本地路由器连接情况
     */
    private boolean isConnectLocalRouter = false;
    private ImageView iamgeview_no_connected_device;
    private ImageView iamgeview_no_blacklist;
    private View line_dirver_connectdevice;
    private View line_dirver_blacklist;
    private TitleLayout layout_title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_router_main);
        initViews();
        initDatas();
        initEvents();
    }

    private AdapterView.OnItemClickListener deviceItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
            String devicename = mConnectedDevices.get(position).getDeviceName().trim();
            new AlertDialog(RouterMainActivity.this).builder().setTitle("加入黑名单")
                    .setMsg("确定将设备\"" + devicename + "\"拉入黑名单")
                    .setPositiveButton("确认", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            isSetBlackList = true;
                            String mac = mConnectedDevices.get(position).getMAC();
                            String name = mConnectedDevices.get(position).getDeviceName();
                            DeviceControl control = new DeviceControl();
                            com.deplink.sdk.android.sdk.json.BLACKLIST blacklist = new com.deplink.sdk.android.sdk.json.BLACKLIST();
                            blacklist.setDeviceMac(mac);
                            blacklist.setDeviceName(name);
                            control.setBLACKLIST(blacklist);
                            mHomeGenius.setDeviceControl(control, channels);
                        }
                    }).setNegativeButton("取消", new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            }).show();
        }
    };
    private String channels;
    private HomeGenius mHomeGenius;

    @Override
    protected void onResume() {
        super.onResume();
        mHomeGenius = new HomeGenius();
        isStartFromExperience = DeviceManager.getInstance().isStartFromExperience();
        isUserLogin = Perfence.getBooleanPerfence(AppConstant.USER_LOGIN);
        frame_blacklist_content.setVisibility(View.GONE);
        if (isStartFromExperience) {
            showQueryingDialog();
        } else {
            int usercount = mRouterManager.getCurrentSelectedRouter().getUserCount();
            usercount++;
            mRouterManager.getCurrentSelectedRouter().setUserCount(usercount);
            mRouterManager.getCurrentSelectedRouter().save();
            manager.addEventCallback(ec);
            String receiverChannels = null;
            try {
                channels = mRouterManager.getCurrentSelectedRouter().getRouter().getChannels();
                receiverChannels = mRouterManager.getCurrentSelectedRouter().getRouter().getReceveChannels();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (!isUserLogin) {
                textview_cpu_use.setText("--");
                textview_memory_use.setText("--");
                textview_upload_speed.setText("--");
                textview_download_speend.setText("--");
                layout_no_connected_device.setVisibility(View.VISIBLE);
                iamgeview_no_connected_device.setBackgroundResource(R.drawable.router);
                iamgeview_no_blacklist.setBackgroundResource(R.drawable.router);
            } else {
                iamgeview_no_connected_device.setBackgroundResource(R.drawable.connectthedevice);
                iamgeview_no_blacklist.setBackgroundResource(R.drawable.blacklist);
            }
            startTimer();
            if (channels != null) {
                Log.i(TAG, "通道:" + channels);
                MQTTController.getSingleton().subscribe(receiverChannels, manager.getmDeviceManager());
                try {
                    mHomeGenius.queryDevices(channels);
                } catch (JsonSyntaxException e) {
                    e.printStackTrace();
                }
            }
        }
        frame_blacklist_content.setVisibility(View.GONE);
        frame_devicelist_content_content.setVisibility(View.VISIBLE);
        textview_connected_devices.setTextColor(ContextCompat.getColor(this, R.color.title_blue_bg));
        textview_blak_list.setTextColor(ContextCompat.getColor(this, R.color.huise));
        line_dirver_connectdevice.setVisibility(View.VISIBLE);
    }


    @Override
    protected void onPause() {
        super.onPause();
        stopTimer();
        manager.removeEventCallback(ec);
    }

    private void stopTimer() {
        if (refreshTask != null) {
            refreshTask.cancel();
            refreshTask = null;
        }
        if (refreshTimer != null) {
            refreshTimer.cancel();//到其他界面就不要发请求数据了
            refreshTimer = null;
        }
    }

    private void startTimer() {
        if (refreshTimer == null) {
            refreshTimer = new Timer();
        }
        if (refreshTask == null) {
            refreshTask = new TimerTask() {
                @Override
                public void run() {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            queryRouterInfo();
                        }
                    });
                }
            };
        }
        if (refreshTimer != null) {
            //3秒钟发一次查询的命令
            refreshTimer.schedule(refreshTask, 0, TIME_DIFFERENCE_BETWEEN_MESSAGE_INTERVALS);
        }
    }

    private void queryRouterInfo() {
        if (refreshCount > TIME_OUT_WATCHDOG_MAXCOUNT) {
            Log.i(TAG, "设备离线了");
            mRouterManager.getCurrentSelectedRouter().setStatus("离线");
            ContentValues values = new ContentValues();
            values.put("Status", "离线");
            final int affectColumn = DataSupport.updateAll(SmartDev.class, values, "Uid=?", mRouterManager.getCurrentSelectedRouter().getUid());
            Log.i(TAG, "更新设备在线状态 :离线 affectColumn=" + affectColumn);
            textview_cpu_use.setText("--");
            textview_memory_use.setText("--");
            textview_upload_speed.setText("--");
            textview_download_speend.setText("--");
            mHomeGenius.getReport(channels);
            queryDevices();
        } else {
            refreshCount++;
            mHomeGenius.getReport(channels);//进界面更新
            queryDevices();
        }
    }

    /**
     * 查询已挂载到当前路由器的设备
     */
    private void queryDevices() {
        if (NetUtil.isNetAvailable(RouterMainActivity.this)) {
            if (isUserLogin) {
                boolean deviceOnline = true;
                if (deviceOnline) {
                    mHomeGenius.queryDevices(channels);
                } else {
                    textview_show_query_device_result.setVisibility(View.VISIBLE);
                    iamgeview_no_connected_device.setImageResource(R.drawable.routeroffline);
                    textview_show_query_device_result.setText("路由器设备处于离线状态");
                }
            } else {
                textview_show_query_device_result.setVisibility(View.VISIBLE);
                textview_show_query_device_result.setText("尚未登录，无法读取设备信息");
                mConnectedDevices.clear();
                mAdapter.notifyDataSetChanged();
            }
        } else {
            textview_show_query_device_result.setVisibility(View.VISIBLE);
            textview_show_query_device_result.setText("没有网络连接，无法读取设备信息");
            mConnectedDevices.clear();
            mAdapter.notifyDataSetChanged();
        }
    }

    private void showQueryingDialog() {
        if (isStartFromExperience) {
            mHandler.post(hideLoadingCallback);
        } else {
            mHandler.postDelayed(hideLoadingCallback, 1500);
        }

    }

    private Runnable hideLoadingCallback = new Runnable() {
        @Override
        public void run() {
            if (isStartFromExperience) {
                if (frame_devicelist_content_content.getVisibility() == View.VISIBLE) {
                    layout_no_connected_device.setVisibility(View.VISIBLE);
                    textview_show_query_device_result.setVisibility(View.VISIBLE);
                    iamgeview_no_connected_device.setImageResource(R.drawable.connectthedevice);
                    textview_show_query_device_result.setText("暂时还没有设备连接");

                }
                if (frame_blacklist_content.getVisibility() == View.VISIBLE) {
                    layout_no_blacklist.setVisibility(View.VISIBLE);
                    textview_show_blacklist_device_result.setVisibility(View.VISIBLE);
                    textview_show_blacklist_device_result.setText("暂时还没有黑名单");
                }
            }

        }
    };

    private boolean isStartFromExperience;

    private void initDatas() {
        layout_title.setReturnClickListener(new TitleLayout.ReturnImageClickListener() {
            @Override
            public void onBackPressed() {
                RouterMainActivity.this.onBackPressed();
            }
        });
        layout_title.setEditImageClickListener(new TitleLayout.EditImageClickListener() {
            @Override
            public void onEditImagePressed() {
                if (isStartFromExperience) {
                    startActivity(new Intent(RouterMainActivity.this, RouterSettingActivity.class));
                } else {
                    startActivity(new Intent(RouterMainActivity.this, RouterSettingActivity.class));
                   /* if (mRouterManager.getCurrentSelectedRouter().getStatus().equals("在线")) {
                        startActivity(new Intent(this, RouterSettingActivity.class));
                    } else {
                        //本地配置先连路由器
                        checkRouter();
                    }*/
                }
            }
        });
        mConnectedDevices = new ArrayList<>();
        mBlackListDatas = new ArrayList<>();
        mAdapter = new ConnectedDeviceListAdapter(this, mConnectedDevices);
        mBlackListAdapter = new BlackListAdapter(this, mBlackListDatas);
        mRouterManager = RouterManager.getInstance();
        DeplinkSDK.initSDK(getApplicationContext(), Perfence.SDK_APP_KEY);
        manager = DeplinkSDK.getSDKManager();
        isStartFromExperience = DeviceManager.getInstance().isStartFromExperience();
        if (!isStartFromExperience) {
            ec = new EventCallback() {
                @Override
                public void onSuccess(SDKAction action) {
                }

                @Override
                public void onBindSuccess(SDKAction action, String devicekey) {

                }

                @Override
                public void deviceOpSuccess(String op, final String deviceKey) {
                    super.deviceOpSuccess(op, deviceKey);
                }

                @Override
                public void connectionLost(Throwable throwable) {
                    super.connectionLost(throwable);
                    mConnectedDevices.clear();
                    mAdapter.notifyDataSetChanged();
                    isUserLogin = false;
                    Perfence.setPerfence(AppConstant.USER_LOGIN, false);
                    new AlertDialog(RouterMainActivity.this).builder().setTitle("账号异地登录")
                            .setMsg("当前账号已在其它设备上登录,是否重新登录")
                            .setPositiveButton("确认", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    startActivity(new Intent(RouterMainActivity.this, LoginActivity.class));
                                }
                            }).setNegativeButton("取消", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                        }
                    }).show();
                }

                @Override
                public void onFailure(SDKAction action, Throwable throwable) {
                }

                @Override
                public void notifyHomeGeniusResponse(String result) {
                    super.notifyHomeGeniusResponse(result);
                    parseDeviceReport(result);
                }
            };
        }

    }

    private PERFORMANCE performance;
    private long currentReceivedDataTime = 0;
    /**
     * 上一次设备上传的流量
     */
    private long lastUploadData = 0;
    /**
     * 上一次设备下载的流量
     */
    private long lastDownloadData = 0;
    /**
     * 设备上行速率(bps)
     */
    private float uprate;
    /**
     * 设备下行速率(bps)
     */
    private float downrate;

    private void updateDevice(PERFORMANCE report) {
        //流量下载
        String downloadBytes = report.getDevice().getDataTraffic().getRX();
        if (downloadBytes.length() > 6) {//去掉数据开始的字符：bytes:
            downloadBytes = downloadBytes.substring(6);
        } else {
            downloadBytes = "0";
        }
        long downloadBytesMath = Long.parseLong(downloadBytes);
        Log.i(TAG, downloadBytes + "downloadBytesMath=" + downloadBytesMath);
        //流量上传
        String uploadBytes = report.getDevice().getDataTraffic().getTX();
        if (uploadBytes.length() > 6) {
            uploadBytes = report.getDevice().getDataTraffic().getTX().substring(6);
        } else {
            uploadBytes = "0";
        }
        long uploadBytesMath = Long.parseLong(uploadBytes);
        Log.i(TAG, uploadBytes + "uploadBytesMath=" + uploadBytesMath);
        /**
         *两次获取到的数据（PERFORMANCE）的时间差
         */
        long timeDif;
        if (currentReceivedDataTime == 0) {
            //说明是第一次收到反馈数据,不处理
            currentReceivedDataTime = System.currentTimeMillis();
        } else {
            timeDif = System.currentTimeMillis() - currentReceivedDataTime;
            currentReceivedDataTime = System.currentTimeMillis();
            Log.i(TAG, "下载总数=" + (float) (downloadBytesMath - lastDownloadData) / 1024 + "KB");
            Log.i(TAG, "上传总数=" + (float) (downloadBytesMath - lastDownloadData) / 1024 + "KB");
            downrate = (float) (Math.abs(downloadBytesMath - lastDownloadData)) / 1024 / ((float) timeDif / 1000);
            uprate = (float) (Math.abs(uploadBytesMath - lastUploadData)) / 1024 / ((float) timeDif / 1000);

        }
        lastDownloadData = downloadBytesMath;
        lastUploadData = uploadBytesMath;
    }

    private void parseDeviceReport(String xmlStr) {
        String op;
        String method;
        Gson gson = new Gson();
        PERFORMANCE content = gson.fromJson(xmlStr, PERFORMANCE.class);
        op = content.getOP();
        method = content.getMethod();
        Log.i(TAG, "op=" + op + "method=" + method + "result=" + content.getResult() + "xmlStr=" + xmlStr);

        if (op == null) {
            if (content.getResult().equalsIgnoreCase("OK")) {
                Log.i(TAG, " mSDKCoordinator.notifyDeviceOpSuccess");
                if (frame_devicelist_content_content.getVisibility() == View.VISIBLE) {
                    if (isSetBlackList) {
                        Ftoast.create(RouterMainActivity.this).setText( "加入黑名单成功").show();
                    }
                }
                if (frame_blacklist_content.getVisibility() == View.VISIBLE) {
                    if (isRemoveBlackList) {
                        Ftoast.create(RouterMainActivity.this).setText( "恢复上网设置成功").show();
                    }
                }
            }
        } else if (op.equalsIgnoreCase("REPORT")) {
            if (method.equalsIgnoreCase("PERFORMANCE")) {
                performance = gson.fromJson(xmlStr, PERFORMANCE.class);
                Log.i(TAG, "performance=" + performance.toString());
                updateDevice(performance);
                SmartDev routerDevices = DataSupport.where("Uid = ?", mRouterManager.getCurrentSelectedRouter().getUid()).findFirst(SmartDev.class, true);
                if (routerDevices != null) {
                    routerDevices.setStatus("在线");
                    routerDevices.saveFast();
                }
                updatePerformance();
            }
        } else if (op.equalsIgnoreCase("DEVICES")) {
            if (method.equalsIgnoreCase("REPORT")) {
                SmartDev routerDevices = DataSupport.where("Uid = ?", mRouterManager.getCurrentSelectedRouter().getUid()).findFirst(SmartDev.class, true);
                Log.i(TAG, "routerDevices!=null" + (routerDevices != null));
                if (routerDevices != null) {
                    routerDevices.setStatus("在线");
                    routerDevices.saveFast();
                }

                DevicesOnlineRoot mDevicesOnlineRoot = gson.fromJson(xmlStr, DevicesOnlineRoot.class);
                mConnectedDevices.clear();
                mConnectedDevices.addAll(mDevicesOnlineRoot.getDevicesOnline());
                try {
                    if (frame_devicelist_content_content.getVisibility() == View.VISIBLE) {
                        Log.i(TAG, "设备界面：获取已连接设备列表:" + mConnectedDevices.size());
                        if (mConnectedDevices.size() == 0) {
                            layout_no_connected_device.setVisibility(View.VISIBLE);
                            textview_show_query_device_result.setVisibility(View.VISIBLE);
                            textview_show_query_device_result.setText("没有设备连接当前的路由器");
                        } else {
                            layout_no_connected_device.setVisibility(View.GONE);
                            textview_show_query_device_result.setVisibility(View.GONE);
                        }
                        mAdapter.notifyDataSetChanged();
                    }
                    if (frame_blacklist_content.getVisibility() == View.VISIBLE) {
                        if (mDevicesOnlineRoot.getBLACKLIST().size() == 0) {
                            layout_no_blacklist.setVisibility(View.VISIBLE);
                            textview_show_blacklist_device_result.setVisibility(View.VISIBLE);
                            textview_show_blacklist_device_result.setText("黑名单中没有添加设备");
                        } else {
                            layout_no_blacklist.setVisibility(View.GONE);
                            textview_show_blacklist_device_result.setVisibility(View.GONE);
                        }
                        mBlackListDatas.clear();
                        mBlackListDatas.addAll(mDevicesOnlineRoot.getBLACKLIST());
                        mBlackListAdapter.notifyDataSetChanged();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void updatePerformance() {
        Log.i(TAG, "updatePerformance");
        refreshCount = 0;
        //这里不用设置设备上线的字段，在routerdevice类里面设置了
        String mem = performance.getDevice().getMEM();
        textview_memory_use.setText(mem);
        String cpu = performance.getDevice().getCPU();
        textview_cpu_use.setText(cpu);
        textview_upload_speed.setText("" + String.format(getResources().getString(R.string.rate_format), uprate));
        textview_download_speend.setText("" + String.format(getResources().getString(R.string.rate_format), downrate));
    }

    private void initEvents() {
        layout_connected_devices.setOnClickListener(this);
        layout_blak_list.setOnClickListener(this);
        if (!isStartFromExperience) {
            listview_device_list.setAdapter(mAdapter);
        }

        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {
                // create "delete" item
                SwipeMenuItem deleteItem = new SwipeMenuItem(
                        getApplicationContext());
                // set item background
                deleteItem.setBackground(new ColorDrawable(getResources().getColor(R.color.delete_button)));
                deleteItem.setWidth((int) Perfence.dp2px(RouterMainActivity.this, 70));
                //  deleteItem.setBackground(R.layout.listview_deleteitem_layout);
                // set item width
                deleteItem.setTitle("拉黑");
                deleteItem.setTitleSize(14);
                // set item title font color
                deleteItem.setTitleColor(Color.WHITE);
                menu.addMenuItem(deleteItem);
            }
        };
        // set creator
        listview_device_list.setMenuCreator(creator);
        listview_device_list.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(final int position, SwipeMenu menu, int index) {
                switch (index) {
                    case 0:
                        String devicename = mConnectedDevices.get(position).getDeviceName().trim();
                        new AlertDialog(RouterMainActivity.this).builder().setTitle("加入黑名单")
                                .setMsg("确定将设备\"" + devicename + "\"拉入黑名单")
                                .setPositiveButton("确认", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        isSetBlackList = true;
                                        String mac = mConnectedDevices.get(position).getMAC();
                                        String name = mConnectedDevices.get(position).getDeviceName();
                                        DeviceControl control = new DeviceControl();
                                        com.deplink.sdk.android.sdk.json.BLACKLIST blacklist = new com.deplink.sdk.android.sdk.json.BLACKLIST();
                                        blacklist.setDeviceMac(mac);
                                        blacklist.setDeviceName(name);
                                        control.setBLACKLIST(blacklist);
                                        mHomeGenius.setDeviceControl(control, channels);
                                    }
                                }).setNegativeButton("取消", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                            }
                        }).show();
                        break;
                }
                return false;
            }
        });
        //下拉刷星
        listview_device_list.setOnItemClickListener(deviceItemClickListener);
        listview_black_list.setAdapter(mBlackListAdapter);
        SwipeMenuCreator creatorBlackList = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {
                // create "open" item
                SwipeMenuItem openItem = new SwipeMenuItem(
                        getApplicationContext());
                // set item background
                openItem.setBackground(new ColorDrawable(getResources().getColor(R.color.delete_button)));
                // set item width
                openItem.setWidth((int) Perfence.dp2px(RouterMainActivity.this, 70));
                // set item title
                openItem.setTitle("恢复上网");
                // set item title fontsize
                openItem.setTitleSize(14);
                // set item title font color
                openItem.setTitleColor(Color.WHITE);
                // add to menu
                menu.addMenuItem(openItem);

            }
        };
        // set creator
        listview_black_list.setMenuCreator(creatorBlackList);
        listview_black_list.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(final int position, SwipeMenu menu, int index) {
                //   String item = mDatas.get(position);
                switch (index) {
                    case 0:
                        // open
                        new AlertDialog(RouterMainActivity.this).builder().setTitle("移除黑名单")
                                .setMsg("确定将该设备从黑名单移除")
                                .setPositiveButton("确认", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        isRemoveBlackList = true;
                                        String mac = mBlackListDatas.get(position).getMAC();
                                        DeviceControl control = new DeviceControl();
                                        WHITELIST whitelist = new WHITELIST();
                                        whitelist.setDeviceMac(mac);
                                        control.setWHITELIST(whitelist);
                                        mHomeGenius.setDeviceControl(control, channels);
                                    }
                                }).setNegativeButton("取消", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                            }
                        }).show();
                        break;

                }
                return false;
            }
        });

        listview_black_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                new AlertDialog(RouterMainActivity.this).builder().setTitle("移除黑名单")
                        .setMsg("确定将该设备从黑名单移除")
                        .setPositiveButton("确认", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                isRemoveBlackList = true;
                                String mac = mBlackListDatas.get(position).getMAC();
                                DeviceControl control = new DeviceControl();
                                WHITELIST whitelist = new WHITELIST();
                                whitelist.setDeviceMac(mac);
                                control.setWHITELIST(whitelist);
                                mHomeGenius.setDeviceControl(control, channels);
                            }
                        }).setNegativeButton("取消", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                }).show();
            }
        });
    }

    private void initViews() {
        layout_title = findViewById(R.id.layout_title);
        textview_connected_devices = findViewById(R.id.textview_connected_devices);
        textview_blak_list = findViewById(R.id.textview_blak_list);
        listview_device_list = findViewById(R.id.listview_device_list);
        layout_connected_devices = findViewById(R.id.layout_connected_devices);
        layout_blak_list = findViewById(R.id.layout_blak_list);
        listview_black_list = findViewById(R.id.listview_black_list);
        textview_show_query_device_result = findViewById(R.id.textview_show_query_device_result);
        textview_show_blacklist_device_result = findViewById(R.id.textview_show_blacklist_device_result);
        frame_devicelist_content_content = findViewById(R.id.frame_devicelist_content_content);
        frame_blacklist_content = findViewById(R.id.frame_blacklist_content);
        textview_cpu_use = findViewById(R.id.textview_cpu_use);
        textview_memory_use = findViewById(R.id.textview_memory_use);
        textview_upload_speed = findViewById(R.id.textview_upload_speed);
        textview_download_speend = findViewById(R.id.textview_download_speend);
        layout_no_blacklist = findViewById(R.id.layout_no_blacklist);
        layout_no_connected_device = findViewById(R.id.layout_no_connected_device);
        iamgeview_no_connected_device = findViewById(R.id.iamgeview_no_connected_device);
        iamgeview_no_blacklist = findViewById(R.id.iamgeview_no_blacklist);
        line_dirver_connectdevice = findViewById(R.id.line_dirver_connectdevice);
        line_dirver_blacklist =  findViewById(R.id.line_dirver_blacklist);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.layout_connected_devices:
                frame_blacklist_content.setVisibility(View.GONE);
                frame_devicelist_content_content.setVisibility(View.VISIBLE);
                textview_connected_devices.setTextColor(getResources().getColor(R.color.title_blue_bg));
                textview_blak_list.setTextColor(getResources().getColor(R.color.huise));
                line_dirver_connectdevice.setVisibility(View.VISIBLE);
                line_dirver_blacklist.setVisibility(View.GONE);
                showQueryingDialog();
                break;
            case R.id.layout_blak_list:
                frame_devicelist_content_content.setVisibility(View.GONE);
                frame_blacklist_content.setVisibility(View.VISIBLE);
                textview_connected_devices.setTextColor(getResources().getColor(R.color.huise));
                textview_blak_list.setTextColor(getResources().getColor(R.color.title_blue_bg));
                line_dirver_connectdevice.setVisibility(View.GONE);
                line_dirver_blacklist.setVisibility(View.VISIBLE);
                showQueryingDialog();
                break;
        }
    }

    /**
     * 显示检查本地路由器是否连接上的加载中的dialog,超时就取消显示
     */
    private void showCheckRouterLoadingDialog() {
        mHandler.postDelayed(connectStatus, 3000);
    }


    private Runnable connectStatus = new Runnable() {
        @Override
        public void run() {
            Log.i(TAG, "connectStatus isConnectLocalRouter=" + isConnectLocalRouter);
            if (!isConnectLocalRouter) {
                new InputAlertDialog(RouterMainActivity.this).builder()
                        .setEditTextHint("请输入wifi密码")
                        .setPositiveButton("确认", new InputAlertDialog.onSureBtnClickListener() {
                            @Override
                            public void onSureBtnClicked(String result) {
                                startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                            }
                        }).setNegativeButton("取消", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                }).show();
            }
        }
    };

    /**
     * 检查是否连接本地路由器，（成功连接本地路由器后）选择上网方式
     */
/*    private void checkRouter() {
        InputAlertDialog dialog = new InputAlertDialog(this, InputAlertDialog.DIALOG_TYPE_BINDED_CHANGE_ROUTER_NAME);
        dialog.setSureBtnClickListener(new InputAlertDialog.onSureBtnClickListener() {
            @Override
            public void onSureBtnClicked(String password) {
                if (!password.equals("")) {
                    showCheckRouterLoadingDialog();
                    isConnectLocalRouter = false;
                    RestfulToolsRouter.getSingleton(getApplicationContext()).checkRouter(password, new Callback<CheckResponse>() {
                        @Override
                        public void onResponse(Call<CheckResponse> call, Response<CheckResponse> response) {
                            Log.i(TAG, "checkRouter " + response.body().toString());
                            CheckResponse result = response.body();
                            String token = response.headers().get("Set-Cookie");
                            int preferenceMode;
                            preferenceMode = MODE_PRIVATE;
                            SharedPreferences sp = getSharedPreferences("user", preferenceMode);
                            SharedPreferences.Editor editor = sp.edit();
                            editor.putString("token", token);
                            editor.apply();
                            if (!result.getLink().equalsIgnoreCase("")) {
                                RestfulToolsRouter.getSingleton(getApplicationContext()).setLink(result.getLink());
                                isConnectLocalRouter = true;
                                mHandler.removeCallbacks(connectStatus);
                                connetWifiDialog.dismiss();
                                startActivity(new Intent(RouterMainActivity.this, RouterSettingActivity.class));
                            }
                        }

                        @Override
                        public void onFailure(Call<CheckResponse> call, Throwable t) {
                            t.printStackTrace();
                        }
                    });
                }

            }
        });
        dialog.show();
        dialog.setTitleText("输入管理员密码");
        dialog.setEditText("admin");
    }*/
}