package com.deplink.boruSmart.activity.homepage;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.deplink.boruSmart.Protocol.json.Room;
import com.deplink.boruSmart.Protocol.json.device.ExperienceCenterDevice;
import com.deplink.boruSmart.Protocol.json.device.SmartDev;
import com.deplink.boruSmart.Protocol.json.device.getway.GatwayDevice;
import com.deplink.boruSmart.Protocol.json.device.router.Router;
import com.deplink.boruSmart.Protocol.json.http.weather.HeWeather6;
import com.deplink.boruSmart.Protocol.packet.ellisdk.BasicPacket;
import com.deplink.boruSmart.Protocol.packet.ellisdk.EllESDK;
import com.deplink.boruSmart.Protocol.packet.ellisdk.EllE_Listener;
import com.deplink.boruSmart.activity.device.AddDeviceQRcodeActivity;
import com.deplink.boruSmart.activity.device.DevicesActivity;
import com.deplink.boruSmart.activity.device.doorbell.DoorbeelMainActivity;
import com.deplink.boruSmart.activity.device.getway.GetwayDeviceActivity;
import com.deplink.boruSmart.activity.device.light.LightActivity;
import com.deplink.boruSmart.activity.device.remoteControl.airContorl.AirRemoteControlMianActivity;
import com.deplink.boruSmart.activity.device.remoteControl.realRemoteControl.RemoteControlActivity;
import com.deplink.boruSmart.activity.device.remoteControl.topBox.TvBoxMainActivity;
import com.deplink.boruSmart.activity.device.remoteControl.tv.TvMainActivity;
import com.deplink.boruSmart.activity.device.router.RouterMainActivity;
import com.deplink.boruSmart.activity.device.smartSwitch.SwitchFourActivity;
import com.deplink.boruSmart.activity.device.smartSwitch.SwitchOneActivity;
import com.deplink.boruSmart.activity.device.smartSwitch.SwitchThreeActivity;
import com.deplink.boruSmart.activity.device.smartSwitch.SwitchTwoActivity;
import com.deplink.boruSmart.activity.device.smartlock.SmartLockActivity;
import com.deplink.boruSmart.activity.homepage.adapter.ExperienceCenterListAdapter;
import com.deplink.boruSmart.activity.homepage.adapter.HomepageGridViewAdapter;
import com.deplink.boruSmart.activity.homepage.adapter.HomepageRoomShowTypeChangedViewAdapter;
import com.deplink.boruSmart.activity.personal.PersonalCenterActivity;
import com.deplink.boruSmart.activity.personal.experienceCenter.ExperienceDevicesActivity;
import com.deplink.boruSmart.activity.personal.login.LoginActivity;
import com.deplink.boruSmart.activity.room.RoomActivity;
import com.deplink.boruSmart.application.AppManager;
import com.deplink.boruSmart.constant.AppConstant;
import com.deplink.boruSmart.constant.DeviceTypeConstant;
import com.deplink.boruSmart.manager.connect.local.tcp.LocalConnectService;
import com.deplink.boruSmart.manager.device.DeviceListener;
import com.deplink.boruSmart.manager.device.DeviceManager;
import com.deplink.boruSmart.manager.device.doorbeel.DoorbeelManager;
import com.deplink.boruSmart.manager.device.getway.GetwayManager;
import com.deplink.boruSmart.manager.device.light.SmartLightManager;
import com.deplink.boruSmart.manager.device.remoteControl.RemoteControlListener;
import com.deplink.boruSmart.manager.device.remoteControl.RemoteControlManager;
import com.deplink.boruSmart.manager.device.router.RouterManager;
import com.deplink.boruSmart.manager.device.smartlock.SmartLockManager;
import com.deplink.boruSmart.manager.device.smartswitch.SmartSwitchManager;
import com.deplink.boruSmart.manager.room.RoomListener;
import com.deplink.boruSmart.manager.room.RoomManager;
import com.deplink.boruSmart.util.DataExchange;
import com.deplink.boruSmart.util.ListViewUtil;
import com.deplink.boruSmart.util.Perfence;
import com.deplink.boruSmart.util.WeakRefHandler;
import com.deplink.boruSmart.view.dialog.AlertDialog;
import com.deplink.boruSmart.view.scrollview.MyScrollView;
import com.deplink.boruSmart.view.scrollview.NonScrollableListView;
import com.deplink.sdk.android.sdk.DeplinkSDK;
import com.deplink.sdk.android.sdk.EventCallback;
import com.deplink.sdk.android.sdk.SDKAction;
import com.deplink.sdk.android.sdk.bean.User;
import com.deplink.sdk.android.sdk.homegenius.DeviceOperationResponse;
import com.deplink.sdk.android.sdk.homegenius.Deviceprops;
import com.deplink.sdk.android.sdk.manager.SDKManager;
import com.deplink.sdk.android.sdk.rest.RestfulToolsWeather;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import deplink.com.smartwirelessrelay.homegenius.EllESDK.R;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 智能家居主页
 */
public class SmartHomeMainActivity extends Activity implements View.OnClickListener, EllE_Listener {
    private static final String TAG = "SmartHomeMainActivity";
    private LinearLayout layout_devices;
    private LinearLayout layout_rooms;
    private LinearLayout layout_personal_center;
    private ImageView imageview_devices;
    private ImageView imageview_home_page;
    private ImageView imageview_rooms;
    private ImageView imageview_personal_center;
    private GridView deviceGridView;
    private ListView listview_experience_center;
    private ExperienceCenterListAdapter mExperienceCenterListAdapter;
    private List<ExperienceCenterDevice> mExperienceCenterDeviceList;
    private RelativeLayout layout_experience_center_top;
    private FrameLayout textview_change_show_type;
    private TextView textview_home;
    private TextView textview_device;
    private TextView textview_room;
    private TextView textview_mine;
    private TextView textview_pm25;
    private HomepageRoomShowTypeChangedViewAdapter mRoomSelectTypeChangedAdapter;
    private SDKManager manager;
    private EventCallback ec;
    private TextView textview_city;
    public LocationClient mLocationClient = null;
    private MyLocationListener myListener = new MyLocationListener();
    private TextView textview_tempature;
    private RoomManager mRoomManager;
    private HorizontalScrollView layout_roomselect_normal;
    private NonScrollableListView layout_roomselect_changed_ype;
    private boolean isLogin;
    private String province;
    private String city;
    private DeviceListener mDeviceListener;
    private RemoteControlListener mRemoteControlListener;
    private DeviceManager mDeviceManager;
    private RemoteControlManager mRemoteControlManager;
    private MyScrollView scroll_inner_wrap;
    private String locationStr;
    private String tempature;
    private String pm25;
    private RelativeLayout layout_weather;
    private List<Room> mRoomList = new ArrayList<>();
    /**
     * 上面半部分列表的数据
     */
    private List<GatwayDevice> datasTop;
    /**
     * 下面半部分列表的数据
     */
    private List<SmartDev> datasBottom;
    private HomepageGridViewAdapter mDeviceAdapter;
    private DoorbeelManager mDoorbeelManager;
    private SmartLockManager mSmartLockManager;
    private RelativeLayout empty_recently_device;
    private ImageView add_equiment;
    private RoomListener mRoomListener;
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i(TAG, "onServiceConnected");
        }
    };
    private static final int MSG_GET_ROOM = 100;
    private static final int MSG_QUERY_WEATHER_PM25 = 101;
    private static final int MSG_SHOW_PM25_TEXT = 102;
    private static final int MSG_SHOW_WEATHER_TEXT = 103;
    private static final int MSG_INIT_LOCATIONSERVICE = 104;
    private static final int MSG_GET_DEVS_HTTPS = 105;
    private static final int MSG_GET_VIRTUAL_DEVS_HTTPS = 106;
    private static final int MSG_CHECK_DOORBELL_ONLINE_STATU = 0x03;
    private EllESDK ellESDK;
    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_GET_ROOM:
                    mDeviceManager.queryDeviceListHttp();
                    break;
                case MSG_QUERY_WEATHER_PM25:
                    if (city.substring(city.length() - 1, city.length()).equals("市")) {
                        city = city.substring(0, city.length() - 1);
                    }
                    if (province.substring(province.length() - 1, province.length()).equals("省")) {
                        province = province.substring(0, province.length() - 1);
                    }
                    textview_city.setText(city);
                    initWaetherData();
                    sendRequestWithHttpClient(city);
                    break;
                case MSG_SHOW_PM25_TEXT:
                    textview_pm25.setText("" + msg.obj);
                    break;
                case MSG_SHOW_WEATHER_TEXT:
                    String temp = (String) msg.obj;
                    if (temp != null) {
                        temp = temp.split("℃")[0];
                        textview_tempature.setText(temp);
                    }
                    break;
                case MSG_INIT_LOCATIONSERVICE:
                    new Thread() {
                        @Override
                        public void run() {
                            super.run();
                            mLocationClient = new LocationClient(getApplicationContext());
                            //声明LocationClient类
                            mLocationClient.registerLocationListener(myListener);
                            //注册监听函数
                            LocationClientOption option = new LocationClientOption();
                            option.setIsNeedAddress(true);
                            //可选，是否需要地址信息，默认为不需要，即参数为false
                            //如果开发者需要获得当前点的地址信息，此处必须为true
                            mLocationClient.setLocOption(option);
                            //mLocationClient为第二步初始化过的LocationClient对象
                            //需将配置好的LocationClientOption对象，通过setLocOption方法传递给LocationClient对象使用
                            //更多LocationClientOption的配置，请参照类参考中LocationClientOption类的详细说明
                            mLocationClient.start();
                        }
                    }.start();
                    break;
                case MSG_GET_DEVS_HTTPS:
                    mRemoteControlManager.queryVirtualDeviceList();
                    break;
                case MSG_GET_VIRTUAL_DEVS_HTTPS:
                    mRoomList.clear();
                    mRoomList.addAll(mRoomManager.queryRooms());
                    setRoomNormalLayout();
                    Log.i(TAG, "mRoomList.size=" + mRoomList.size());
                    initRecentlyDeviceData();
                    mDeviceAdapter.notifyDataSetChanged();
                    ListViewUtil.setListViewHeight(layout_roomselect_changed_ype);
                    mRoomSelectTypeChangedAdapter.notifyDataSetChanged();

                    break;
                case MSG_CHECK_DOORBELL_ONLINE_STATU:
                    if(currentSmartDoorBell!=null){
                        if(!receverDoorbellMsg){
                            currentSmartDoorBell.setStatus("离线");
                            currentSmartDoorBell.save();
                        }
                    }
                    //停止搜索门铃设备
                    ellESDK.stopSearchDevs();
                    break;
            }
            return true;
        }
    };
    private Handler mHandler = new WeakRefHandler(mCallback);

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    public class MyLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            //此处的BDLocation为定位结果信息类，通过它的各种get方法可获取定位相关的全部结果
            //以下只列举部分获取地址相关的结果信息
            //更多结果信息获取说明，请参照类参考中BDLocation类中的说明
            // String addr = location.getAddrStr();    //获取详细地址信息
            // String country = location.getCountry();    //获取国家
            province = location.getProvince();    //获取省份
            city = location.getCity();    //获取城市
            if (city != null && province != null) {
                if (!(city).equalsIgnoreCase(locationStr)) {
                    Perfence.setPerfence(AppConstant.LOCATION_RECEIVED, city);
                }
                Message msg = Message.obtain();
                msg.what = MSG_QUERY_WEATHER_PM25;
                mHandler.sendMessage(msg);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smart_home_main);
        initViews();
        initDatas();
        initEvents();
    }
    private Timer refreshTimer = null;
    private TimerTask refreshTask = null;
    private static final int TIME_DIFFERENCE_BETWEEN_MESSAGE_INTERVALS = 10000;
    private SmartDev currentSmartDoorBell;
    private String seachedDoorbellmac;
    private boolean isRunSeachDoorbell=false;
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
                            for (int i = 0; i < datasTop.size(); i++) {
                                if ((mDeviceManager.getmDevicesStatus().get(datasTop.get(i).getUid()) != null)) {
                                    datasTop.get(i).setStatus(mDeviceManager.getmDevicesStatus().get(datasTop.get(i).getUid()));
                                    datasTop.get(i).saveFast();
                                }
                            }
                            for(int i=0;i<datasBottom.size();i++){
                                if ((mDeviceManager.getmDevicesStatus().get(datasBottom.get(i).getMac()) != null)) {
                                    datasBottom.get(i).setStatus(mDeviceManager.getmDevicesStatus().get(datasBottom.get(i).getMac()));
                                    datasBottom.get(i).saveFast();
                                }
                                if(datasBottom.get(i).getType().equalsIgnoreCase(DeviceTypeConstant.TYPE.TYPE_MENLING)){
                                    currentSmartDoorBell=datasBottom.get(i);
                                }
                            }
                            if(currentSmartDoorBell!=null){
                                //查询门邻设备状态
                                if(!isRunSeachDoorbell){
                                    ellESDK.startSearchDevs();
                                    isRunSeachDoorbell=true;
                                    mHandler.sendEmptyMessageDelayed(MSG_CHECK_DOORBELL_ONLINE_STATU,5000);
                                }
                            }
                            virtualDeviceUpdate();
                            initRecentlyDeviceData();
                            mDeviceAdapter.notifyDataSetChanged();
                            ListViewUtil.setListViewHeight(layout_roomselect_changed_ype);
                            mRoomSelectTypeChangedAdapter.notifyDataSetChanged();
                        }
                    });
                }
            };
        }
        if (refreshTimer != null) {
            //10秒钟发一次查询的命令
            try {
                refreshTimer.schedule(refreshTask, 0, TIME_DIFFERENCE_BETWEEN_MESSAGE_INTERVALS);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    /**
     * 更新虚拟设备的状态
     */
    private void virtualDeviceUpdate() {
        List<SmartDev> airRcs = DataSupport.where("Type=?", DeviceTypeConstant.TYPE.TYPE_AIR_REMOTECONTROL).find(SmartDev.class);
        for (int i = 0; i < airRcs.size(); i++) {
            SmartDev realRc = DataSupport.where("Uid=?", airRcs.get(i).getRemotecontrolUid()).findFirst(SmartDev.class, true);
            if (realRc != null) {
                airRcs.get(i).setRooms(realRc.getRooms());
                airRcs.get(i).setStatus(realRc.getStatus());
                airRcs.get(i).saveFast();
            }
        }
        List<SmartDev> tvRcs = DataSupport.where("Type=?", DeviceTypeConstant.TYPE.TYPE_TV_REMOTECONTROL).find(SmartDev.class);
        for (int i = 0; i < tvRcs.size(); i++) {
            SmartDev realRc = DataSupport.where("Uid=?", tvRcs.get(i).getRemotecontrolUid()).findFirst(SmartDev.class, true);
            if (realRc != null) {
                tvRcs.get(i).setStatus(realRc.getStatus());
                tvRcs.get(i).setRooms(realRc.getRooms());
                tvRcs.get(i).saveFast();
            }
        }
        List<SmartDev> tvboxRcs = DataSupport.where("Type=?", DeviceTypeConstant.TYPE.TYPE_TVBOX_REMOTECONTROL).find(SmartDev.class);
        for (int i = 0; i < tvboxRcs.size(); i++) {
            SmartDev realRc = DataSupport.where("Uid=?", tvboxRcs.get(i).getRemotecontrolUid()).findFirst(SmartDev.class, true);
            if (realRc != null) {
                tvboxRcs.get(i).setStatus(realRc.getStatus());
                tvboxRcs.get(i).setRooms(realRc.getRooms());
                tvboxRcs.get(i).saveFast();
            }
        }
    }
    /**
     * 获取pm2.5
     *
     * @param city
     */
    private void sendRequestWithHttpClient(final String city) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                RestfulToolsWeather.getSingleton().getWeatherPm25(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                        if (response.code() == 200) {
                            JsonObject jsonObjectGson = response.body();
                            Gson gson = new Gson();
                            Log.i(TAG, "weatherObject=" + jsonObjectGson.toString());
                            HeWeather6 weatherObject = gson.fromJson(jsonObjectGson.toString(), HeWeather6.class);

                            try {
                                //{"HeWeather6":[{"status":"no more requests"}]}
                                if (!weatherObject.getInfoList().get(0).getStatus().equalsIgnoreCase("no more requests")) {
                                    if (!weatherObject.getInfoList().get(0).getAir_now_city().getPm25().equalsIgnoreCase(pm25)) {
                                        Perfence.setPerfence(AppConstant.PM25_VALUE, weatherObject.getInfoList().get(0).getAir_now_city().getPm25());
                                        Message message = new Message();
                                        message.what = MSG_SHOW_PM25_TEXT;
                                        message.obj = weatherObject.getInfoList().get(0).getAir_now_city().getPm25();
                                        mHandler.sendMessage(message);
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t) {

                    }
                }, city);

            }
        }).start();
    }

    public void initWaetherData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                RestfulToolsWeather.getSingleton().getWeatherInfo(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                        if (response.code() == 200) {
                            JsonObject jsonObjectGson = response.body();
                            Gson gson = new Gson();
                            HeWeather6 weatherObject = gson.fromJson(jsonObjectGson.toString(), HeWeather6.class);
                            Log.i(TAG, "获取天气数据=" + weatherObject.toString());
                            if (!weatherObject.getInfoList().get(0).getStatus().equalsIgnoreCase("no more requests")) {
                                if (!weatherObject.getInfoList().get(0).getNow().getTmp().equalsIgnoreCase(tempature)) {
                                    Perfence.setPerfence(AppConstant.TEMPATURE_VALUE, weatherObject.getInfoList().get(0).getNow().getTmp());
                                    Message message = new Message();
                                    message.what = MSG_SHOW_WEATHER_TEXT;
                                    message.obj = weatherObject.getInfoList().get(0).getNow().getTmp();
                                    mHandler.sendMessage(message);
                                }
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t) {

                    }
                }, city);

            }
        }).start();
    }
    private void initListener() {
        ellESDK = EllESDK.getInstance();
        ellESDK.InitEllESDK(this, this);
        mDeviceListener = new DeviceListener() {
            @Override
            public void responseQueryHttpResult(List<Deviceprops> devices) {
                super.responseQueryHttpResult(devices);
                //保存设备列表
                List<SmartDev> dbSmartDev = mDeviceManager.findAllSmartDevice();
                for (int i = 0; i < devices.size(); i++) {
                    boolean addToDb = true;
                    if (devices.get(i).getDevice_type().equalsIgnoreCase("LKSGW")
                            ) {
                        addToDb = false;
                    } else {
                        for (int j = 0; j < dbSmartDev.size(); j++) {
                            if (dbSmartDev.get(j).getUid().equals(devices.get(i).getUid())) {
                                addToDb = false;
                            }
                        }
                    }
                    if (addToDb) {
                        Log.i(TAG, "http查询到智能设备,保存下来:");
                        saveSmartDeviceToSqlite(devices, i);
                    }
                }
                List<GatwayDevice> dbGetwayDev = GetwayManager.getInstance().getAllGetwayDevice();
                for (int i = 0; i < devices.size(); i++) {
                    boolean addToDb = true;
                    if (devices.get(i).getDevice_type().equalsIgnoreCase("LKSGW")) {
                        for (int j = 0; j < dbGetwayDev.size(); j++) {
                            if (dbGetwayDev.get(j).getUid().equals(devices.get(i).getUid())) {
                                addToDb = false;
                            }
                        }
                    } else {
                        addToDb = false;
                    }
                    if (addToDb) {
                        saveGetwayDeviceToSqlite(devices, i);
                    }
                }
                mHandler.sendEmptyMessage(MSG_GET_DEVS_HTTPS);
            }
        };
        mRemoteControlListener = new RemoteControlListener() {
            @Override
            public void responseQueryVirtualDevices(List<DeviceOperationResponse> result) {
                super.responseQueryVirtualDevices(result);
                //保存虚拟设备
                for (int i = 0; i < result.size(); i++) {
                    saveVirtualDeviceToSqlite(result, i);
                }
                mHandler.sendEmptyMessage(MSG_GET_VIRTUAL_DEVS_HTTPS);
            }
        };
        mRoomListener = new RoomListener() {
            @Override
            public void responseQueryResultHttps(List<Room> result) {
                super.responseQueryResultHttps(result);
                Log.i(TAG, "主页获取到房间列表=" + result);
                Message msg = Message.obtain();
                msg.what = MSG_GET_ROOM;
                mHandler.sendMessage(msg);
            }
        };
    }
    /**
     * 保存网关设备到本地数据库
     *
     * @param devices
     * @param i
     */
    private void saveGetwayDeviceToSqlite(List<Deviceprops> devices, int i) {
        GatwayDevice dev = new GatwayDevice();
        String deviceType = devices.get(i).getDevice_type();
        dev.setType(deviceType);
        String deviceName = devices.get(i).getDevice_name();
        if (deviceType.equalsIgnoreCase("LKSWG") || deviceType.equalsIgnoreCase("LKSGW")) {
            if (deviceName == null || deviceName.equals("")) {
                dev.setName("中继器");
            } else {
                deviceName = deviceName.replace("/路由器", "");
                dev.setName(deviceName);
            }
            deviceType = DeviceTypeConstant.TYPE.TYPE_SMART_GETWAY;
            dev.setType(deviceType);
        }
        dev.setUid(devices.get(i).getUid());
        dev.setOrg(devices.get(i).getOrg_code());
        dev.setVer(devices.get(i).getVersion());
        dev.setTopic("device/" + devices.get(i).getUid() + "/sub");
        List<Room> rooms = new ArrayList<>();
        Room room = DataSupport.where("Uid=?", devices.get(i).getRoom_uid()).findFirst(Room.class);
        if (room != null) {
            Log.i(TAG, "添加中继器房间是:" + room.toString());
            rooms.add(room);
        } else {
            rooms.addAll(DataSupport.findAll(Room.class));
        }
        dev.setRoomList(rooms);
        boolean success = dev.save();
        Log.i(TAG, "保存设备:" + success + "deviceName=" + deviceName);
    }

    private void saveSmartDeviceToSqlite(List<Deviceprops> devices, int i) {
        Log.i(TAG, "saveSmartDeviceToSqlite");
        SmartDev dev = new SmartDev();
        String deviceType = devices.get(i).getDevice_type();
        dev.setType(deviceType);
        String deviceName = devices.get(i).getDevice_name();
        if (deviceType.equalsIgnoreCase("SMART_LOCK")) {
            deviceType = DeviceTypeConstant.TYPE.TYPE_LOCK;
            dev.setType(deviceType);
            dev.setName(deviceName);
        } else if (deviceType.equalsIgnoreCase("IRMOTE_V2")) {
            deviceType = DeviceTypeConstant.TYPE.TYPE_REMOTECONTROL;
            dev.setType(deviceType);
            dev.setName(deviceName);
        } else if (deviceType.equalsIgnoreCase("SmartWallSwitch1")) {
            deviceType = DeviceTypeConstant.TYPE.TYPE_SWITCH;
            dev.setType(deviceType);
            dev.setName(deviceName);
            dev.setSubType(DeviceTypeConstant.TYPE_SWITCH_SUBTYPE.SUB_TYPE_SWITCH_ONEWAY);
        } else if (deviceType.equalsIgnoreCase("SmartWallSwitch2")) {
            deviceType = DeviceTypeConstant.TYPE.TYPE_SWITCH;
            dev.setType(deviceType);
            dev.setName(deviceName);
            dev.setSubType(DeviceTypeConstant.TYPE_SWITCH_SUBTYPE.SUB_TYPE_SWITCH_TWOWAY);
        } else if (deviceType.equalsIgnoreCase("SmartWallSwitch3")) {
            deviceType = DeviceTypeConstant.TYPE.TYPE_SWITCH;
            dev.setType(deviceType);
            dev.setName(deviceName);
            dev.setSubType(DeviceTypeConstant.TYPE_SWITCH_SUBTYPE.SUB_TYPE_SWITCH_THREEWAY);
        } else if (deviceType.equalsIgnoreCase("SmartWallSwitch4")) {
            deviceType = DeviceTypeConstant.TYPE.TYPE_SWITCH;
            dev.setType(deviceType);
            dev.setName(deviceName);
            dev.setSubType(DeviceTypeConstant.TYPE_SWITCH_SUBTYPE.SUB_TYPE_SWITCH_FOURWAY);
        } else if (deviceType.equalsIgnoreCase("YWLIGHTCONTROL")) {
            deviceType = DeviceTypeConstant.TYPE.TYPE_LIGHT;
            dev.setType(deviceType);
            dev.setName(deviceName);
        } else if (deviceType.equalsIgnoreCase("SMART_BELL")) {
            deviceType = DeviceTypeConstant.TYPE.TYPE_MENLING;
            dev.setType(deviceType);
            dev.setStatus("在线");
            dev.setName(deviceName);
        } else if (deviceType.equalsIgnoreCase("LKRT")) {
            deviceType = DeviceTypeConstant.TYPE.TYPE_ROUTER;
            dev.setType(deviceType);
            Router router = new Router();
            Log.i(TAG, "获取绑定的设备" + manager.getDeviceList().size());
            if (deviceName == null || deviceName.equals("")) {
                dev.setName("路由器");
            } else {
                dev.setName(deviceName);
            }
            router.setSign_seed(devices.get(i).getSign_seed());
            router.setSignature(devices.get(i).getSignature());
            router.setChannels(devices.get(i).getChannels().getSecondary().getSub());
            router.setReceveChannels(devices.get(i).getChannels().getSecondary().getPub());
            router.setSmartDev(dev);
            router.save();
            dev.setRouter(router);
        }
        GatwayDevice addGatwayDevice = null;
        String gw_uid = devices.get(i).getGw_uid();
        if (gw_uid != null) {
            dev.setGetwayDeviceUid(gw_uid);
            addGatwayDevice = DataSupport.where("uid=?", gw_uid).findFirst(GatwayDevice.class);
        }
        Log.i(TAG, "gw_uid=" + gw_uid + "addGatwayDevice" + (addGatwayDevice != null));
        if (addGatwayDevice != null) {
            dev.setGetwayDevice(addGatwayDevice);
        }

        dev.setUid(devices.get(i).getUid());
        dev.setOrg(devices.get(i).getOrg_code());
        dev.setVer(devices.get(i).getVersion());
        dev.setMac(devices.get(i).getMac().toLowerCase());
        List<Room> rooms = new ArrayList<>();
        Room room = DataSupport.where("Uid=?", devices.get(i).getRoom_uid()).findFirst(Room.class);
        if (room != null) {
            Log.i(TAG, "saveSmartDeviceToSqlite:" + (room.toString()));
            rooms.add(room);
            dev.setRooms(rooms);
        } else {
            dev.setRooms(mRoomManager.queryRooms());
        }
        dev.save();
    }

    private void saveVirtualDeviceToSqlite(List<DeviceOperationResponse> devices, int i) {
        SmartDev dev = DataSupport.where("Uid=?", devices.get(i).getUid()).findFirst(SmartDev.class);
        if (dev == null) {
            dev = new SmartDev();
        }
        String deviceType = devices.get(i).getDevice_type();
        switch (deviceType) {
            case "IREMOTE_V2_AC":
                deviceType = DeviceTypeConstant.TYPE.TYPE_AIR_REMOTECONTROL;
                break;
            case "IREMOTE_V2_TV":
                deviceType = DeviceTypeConstant.TYPE.TYPE_TV_REMOTECONTROL;
                break;
            case "IREMOTE_V2_STB":
                deviceType = DeviceTypeConstant.TYPE.TYPE_TVBOX_REMOTECONTROL;
                break;
        }
        dev.setType(deviceType);
        String deviceName = devices.get(i).getDevice_name();
        dev.setName(deviceName);
        dev.setUid(devices.get(i).getUid());
        SmartDev realRc = DataSupport.where("Uid=?", devices.get(i).getIrmote_uid()).findFirst(SmartDev.class, true);
        if (realRc != null && realRc.getRooms() != null) {
            dev.setRooms(realRc.getRooms());
        }
        dev.setRemotecontrolUid(devices.get(i).getIrmote_uid());
        dev.setMac(devices.get(i).getIrmote_mac());
        String key_codes = devices.get(i).getKey_codes();
        if (key_codes != null) {
            dev.setKey_codes(key_codes);
        }
        dev.save();
    }
    /**
     * 按照序号排序
     */
    public List<GatwayDevice> sortGatwayDevices(List<GatwayDevice> mGatwayDevices) {
        Collections.sort(mGatwayDevices, new Comparator<GatwayDevice>() {
            @Override
            public int compare(GatwayDevice o1, GatwayDevice o2) {
                //compareTo就是比较两个值，如果前者大于后者，返回1，等于返回0，小于返回-1
                if (o1.getUseCount() == o2.getUseCount()) {
                    return 0;
                }
                if (o1.getUseCount() > o2.getUseCount()) {
                    return -1;
                }
                if (o1.getUseCount() < o2.getUseCount()) {
                    return 1;
                }
                return 0;
            }
        });
        return mGatwayDevices;
    }

    private String currentRecentDeviceShowStyle;

    /**
     * 按照序号排序
     */
    public List<SmartDev> sortSmartDevices(List<SmartDev> mGatwayDevices) {
        Collections.sort(mGatwayDevices, new Comparator<SmartDev>() {
            @Override
            public int compare(SmartDev o1, SmartDev o2) {
                //compareTo就是比较两个值，如果前者大于后者，返回1，等于返回0，小于返回-1
                if (o1.getUserCount() == o2.getUserCount()) {
                    return 0;
                }
                if (o1.getUserCount() > o2.getUserCount()) {
                    return -1;
                }
                if (o1.getUserCount() < o2.getUserCount()) {
                    return 1;
                }
                return 0;
            }
        });
        return mGatwayDevices;
    }
    private boolean receverDoorbellMsg=false;
    @Override
    protected void onResume() {
        super.onResume();
        receverDoorbellMsg=false;
        isLogin = Perfence.getBooleanPerfence(AppConstant.USER_LOGIN);

        manager.addEventCallback(ec);
        mRoomList.clear();
        mRoomList.addAll(mRoomManager.queryRooms());
        mDeviceManager.addDeviceListener(mDeviceListener);
        mDeviceManager.startQueryStatu();
        mRemoteControlManager.addRemoteControlListener(mRemoteControlListener);
        mRoomManager.addRoomListener(mRoomListener);
        setWeatherBackground();
        setButtomBarIcon();
        initRecentlyDeviceData();
        layout_roomselect_changed_ype.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.i(TAG, "OnItemClickListener position=" + position);
                mDeviceManager.setStartFromExperience(false);
                if (datasTop.size() < (position + 1)) {
                    //智能设备
                    String deviceType = datasBottom.get(position - datasTop.size()).getType();
                    String deviceSubType = datasBottom.get(position - datasTop.size()).getSubType();
                    Log.i(TAG, "智能设备类型=" + deviceType);
                    mDeviceManager.setCurrentSelectSmartDevice(datasBottom.get(position - datasTop.size()));
                    switch (deviceType) {
                        case DeviceTypeConstant.TYPE.TYPE_LOCK:
                            //设置当前选中的门锁设备
                            mSmartLockManager.setCurrentSelectLock(datasBottom.get(position - datasTop.size()));
                            startActivity(new Intent(SmartHomeMainActivity.this, SmartLockActivity.class));
                            break;
                        case "IRMOTE_V2":
                        case DeviceTypeConstant.TYPE.TYPE_REMOTECONTROL:
                            RemoteControlManager.getInstance().setmSelectRemoteControlDevice(datasBottom.get(position - datasTop.size()));
                            startActivity(new Intent(SmartHomeMainActivity.this, RemoteControlActivity.class));
                            break;
                        case DeviceTypeConstant.TYPE.TYPE_AIR_REMOTECONTROL:
                            RemoteControlManager.getInstance().setmSelectRemoteControlDevice(datasBottom.get(position - datasTop.size()));
                            startActivity(new Intent(SmartHomeMainActivity.this, AirRemoteControlMianActivity.class));
                            break;
                        case DeviceTypeConstant.TYPE.TYPE_ROUTER:
                            RouterManager.getInstance().setCurrentSelectedRouter(datasBottom.get(position - datasTop.size()));
                            startActivity(new Intent(SmartHomeMainActivity.this, RouterMainActivity.class));
                            break;
                        case DeviceTypeConstant.TYPE.TYPE_TV_REMOTECONTROL:
                            RemoteControlManager.getInstance().setmSelectRemoteControlDevice(datasBottom.get(position - datasTop.size()));
                            startActivity(new Intent(SmartHomeMainActivity.this, TvMainActivity.class));
                            break;
                        case DeviceTypeConstant.TYPE.TYPE_TVBOX_REMOTECONTROL:
                            RemoteControlManager.getInstance().setmSelectRemoteControlDevice(datasBottom.get(position - datasTop.size()));
                            startActivity(new Intent(SmartHomeMainActivity.this, TvBoxMainActivity.class));
                            break;
                        case DeviceTypeConstant.TYPE.TYPE_SWITCH:
                            SmartSwitchManager.getInstance().setCurrentSelectSmartDevice(datasBottom.get(position - datasTop.size()));
                            switch (deviceSubType) {
                                case DeviceTypeConstant.TYPE_SWITCH_SUBTYPE.SUB_TYPE_SWITCH_ONEWAY:
                                    startActivity(new Intent(SmartHomeMainActivity.this, SwitchOneActivity.class));
                                    break;
                                case DeviceTypeConstant.TYPE_SWITCH_SUBTYPE.SUB_TYPE_SWITCH_TWOWAY:
                                    startActivity(new Intent(SmartHomeMainActivity.this, SwitchTwoActivity.class));
                                    break;
                                case DeviceTypeConstant.TYPE_SWITCH_SUBTYPE.SUB_TYPE_SWITCH_THREEWAY:
                                    startActivity(new Intent(SmartHomeMainActivity.this, SwitchThreeActivity.class));
                                    break;
                                case DeviceTypeConstant.TYPE_SWITCH_SUBTYPE.SUB_TYPE_SWITCH_FOURWAY:
                                    startActivity(new Intent(SmartHomeMainActivity.this, SwitchFourActivity.class));
                                    break;
                            }
                            break;
                        case DeviceTypeConstant.TYPE.TYPE_MENLING:
                            mDoorbeelManager.setCurrentSelectedDoorbeel(datasBottom.get(position - datasTop.size()));
                            startActivity(new Intent(SmartHomeMainActivity.this, DoorbeelMainActivity.class));
                            break;
                        case DeviceTypeConstant.TYPE.TYPE_LIGHT:
                            SmartLightManager.getInstance().setCurrentSelectLight(datasBottom.get(position - datasTop.size()));
                            startActivity(new Intent(SmartHomeMainActivity.this, LightActivity.class));
                            break;
                    }
                } else {
                    //网关设备
                    GetwayManager.getInstance().setCurrentSelectGetwayDevice(datasTop.get(position));
                    startActivity(new Intent(SmartHomeMainActivity.this, GetwayDeviceActivity.class));
                }
            }
        });
        layout_roomselect_changed_ype.setAdapter(mRoomSelectTypeChangedAdapter);
        currentRecentDeviceShowStyle = Perfence.getPerfence(Perfence.HOMEPAGE_DEVICE_SHOW_STYLE);
        mDeviceAdapter.setTopList(datasTop);
        mDeviceAdapter.setBottomList(datasBottom);
        mDeviceAdapter.notifyDataSetChanged();
        scroll_inner_wrap.smoothScrollTo(0, 0);
        mRoomSelectTypeChangedAdapter.setTopList(datasTop);
        mRoomSelectTypeChangedAdapter.setBottomList(datasBottom);
        mRoomSelectTypeChangedAdapter.notifyDataSetChanged();
        if (currentRecentDeviceShowStyle.equals(Perfence.HOMEPAGE_DEVICE_SHOW_STYLE_NORMAL)) {
            layout_roomselect_normal.setVisibility(View.VISIBLE);
            layout_roomselect_changed_ype.setVisibility(View.GONE);
            scroll_inner_wrap.smoothScrollTo(0, 0);
        } else if (currentRecentDeviceShowStyle.equals(Perfence.HOMEPAGE_DEVICE_SHOW_STYLE_CHANGE)) {
            layout_roomselect_normal.setVisibility(View.GONE);
            layout_roomselect_changed_ype.setVisibility(View.VISIBLE);
        } else {
            layout_roomselect_normal.setVisibility(View.GONE);
            layout_roomselect_changed_ype.setVisibility(View.VISIBLE);
        }
        initDefaultTempaturePm25();
        if (isLogin) {
            mRoomManager.updateRooms();
        }
        startTimer();
    }

    private void initRecentlyDeviceData() {
        List<GatwayDevice> mGatwayDevices = new ArrayList<>();
        List<SmartDev> mSmartDevs = new ArrayList<>();
        mGatwayDevices.addAll(GetwayManager.getInstance().getAllGetwayDevice());
        mSmartDevs.addAll(DataSupport.findAll(SmartDev.class, true));
        datasTop.clear();
        datasBottom.clear();
        for (int i = 0; i < mGatwayDevices.size(); i++) {
            datasTop.add(mGatwayDevices.get(i));
        }
        for (int i = 0; i < mSmartDevs.size(); i++) {
            datasBottom.add(mSmartDevs.get(i));
        }
        sortGatwayDevices(datasTop);
        sortSmartDevices(datasBottom);
        while (datasTop.size() + datasBottom.size() > 5) {
            if (datasTop.size() > 0) {
                if (datasTop.get(datasTop.size() - 1).getUseCount() > datasBottom.get(datasBottom.size() - 1).getUserCount()) {
                    datasBottom.remove(datasBottom.size() - 1);
                } else {
                    datasTop.remove(datasTop.size() - 1);
                }
            } else {
                if (datasBottom.size() > 5) {
                    datasBottom.remove(datasBottom.size() - 1);
                }
            }
        }
        currentRecentDeviceShowStyle = Perfence.getPerfence(Perfence.HOMEPAGE_DEVICE_SHOW_STYLE);
        if (datasTop.size() + datasBottom.size() == 0) {
            textview_change_show_type.setVisibility(View.GONE);
            empty_recently_device.setVisibility(View.VISIBLE);
        } else {
            textview_change_show_type.setVisibility(View.VISIBLE);
            empty_recently_device.setVisibility(View.GONE);
        }
        setRoomNormalLayout();
    }

    private void initDefaultTempaturePm25() {
        locationStr = Perfence.getPerfence(AppConstant.LOCATION_RECEIVED);
        tempature = Perfence.getPerfence(AppConstant.TEMPATURE_VALUE);
        pm25 = Perfence.getPerfence(AppConstant.PM25_VALUE);
        if (locationStr != null && !locationStr.equals("")) {
            textview_city.setText(locationStr);
            city=locationStr;
            initWaetherData();
            sendRequestWithHttpClient(city);
        }
        if (tempature != null) {
            textview_tempature.setText(tempature);
        }
        if (pm25 != null) {
            textview_pm25.setText(pm25);
        }

    }

    private void setButtomBarIcon() {
        textview_home.setTextColor(ContextCompat.getColor(this, R.color.room_type_text));
        textview_device.setTextColor(ContextCompat.getColor(this, R.color.line_clolor));
        textview_room.setTextColor(ContextCompat.getColor(this, R.color.line_clolor));
        textview_mine.setTextColor(ContextCompat.getColor(this, R.color.line_clolor));
        imageview_home_page.setImageResource(R.drawable.checkthehome);
        imageview_devices.setImageResource(R.drawable.nocheckthedevice);
        imageview_rooms.setImageResource(R.drawable.nochecktheroom);
        imageview_personal_center.setImageResource(R.drawable.nocheckthemine);
    }

    private void setWeatherBackground() {
        Calendar cal = Calendar.getInstance();
        int m = cal.get(Calendar.MONTH);
        switch (m) {
            case 2:
            case 3:
            case 4:
                //春
                layout_weather.setBackgroundResource(R.drawable.springrbackground);
                break;
            case 5:
            case 6:
            case 7:
                layout_weather.setBackgroundResource(R.drawable.summerbackground);
                //夏
                break;
            case 8:
            case 9:
            case 10:
                layout_weather.setBackgroundResource(R.drawable.fallbackground);
                //秋
                break;
            case 11:
            case 0:
            case 1:
                //冬
                layout_weather.setBackgroundResource(R.drawable.weatherbackground);
                break;
            default:
                break;
        }
    }

    private void setRoomNormalLayout() {
        int size = datasTop.size() + datasBottom.size();
        int length = 92;
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        float density = dm.density;
        int gridviewWidth = (int) (size * (length + 1) * density);
        int itemWidth = (int) (length * density);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                gridviewWidth, LinearLayout.LayoutParams.MATCH_PARENT);
        params.gravity = Gravity.CENTER_VERTICAL | Gravity.START;
        deviceGridView.setLayoutParams(params); // 设置GirdView布局参数,横向布局的关键
        deviceGridView.setColumnWidth(itemWidth); // 设置列表项宽
        deviceGridView.setStretchMode(GridView.NO_STRETCH);
        deviceGridView.setNumColumns(size); // 设置列数量=列表集合数
        deviceGridView.setAdapter(mDeviceAdapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        manager.removeEventCallback(ec);
        manager.onDestroy();
    }

    private void initDatas() {
        Intent bindIntent = new Intent(SmartHomeMainActivity.this, LocalConnectService.class);
        startService(bindIntent);
        try {
            bindService(bindIntent, connection, BIND_AUTO_CREATE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        initManager();
        datasTop = new ArrayList<>();
        datasBottom = new ArrayList<>();
        mDeviceAdapter = new HomepageGridViewAdapter(this, datasTop, datasBottom);
        mRoomSelectTypeChangedAdapter = new HomepageRoomShowTypeChangedViewAdapter(this, datasTop, datasBottom);
        mExperienceCenterDeviceList = new ArrayList<>();
        ExperienceCenterDevice oneDevice = new ExperienceCenterDevice();
        oneDevice.setDeviceName("智能门锁");
        oneDevice.setOnline(true);
        mExperienceCenterDeviceList.add(oneDevice);
        oneDevice = new ExperienceCenterDevice();
        oneDevice.setDeviceName("智能网关");
        oneDevice.setOnline(true);
        mExperienceCenterDeviceList.add(oneDevice);
        mExperienceCenterListAdapter = new ExperienceCenterListAdapter(this, mExperienceCenterDeviceList, true);
        listview_experience_center.setOnItemClickListener(mExperienceCenterListClickListener);
        DeplinkSDK.initSDK(getApplicationContext(), Perfence.SDK_APP_KEY);
        manager = DeplinkSDK.getSDKManager();
        ec = new EventCallback() {
            @Override
            public void onSuccess(SDKAction action) {
                switch (action) {
                    case LOGIN:
                        manager.connectMQTT(SmartHomeMainActivity.this);
                        Log.i(TAG, "login mqtt success uuid=" + manager.getUserInfo().getUuid());
                        Perfence.setPerfence(AppConstant.PERFENCE_BIND_APP_UUID, manager.getUserInfo().getUuid());
                        User user = manager.getUserInfo();
                        Perfence.setPerfence(Perfence.USER_PASSWORD, user.getPassword());
                        Perfence.setPerfence(Perfence.PERFENCE_PHONE, user.getName());
                        Perfence.setPerfence(AppConstant.USER_LOGIN, true);
                        mRoomManager.updateRooms();
                        break;
                }
            }

            @Override
            public void onBindSuccess(SDKAction action, String devicekey) {

            }

            @Override
            public void onFailure(SDKAction action, Throwable throwable) {

            }

            @Override
            public void connectionLost(Throwable throwable) {
                super.connectionLost(throwable);
                Perfence.setPerfence(AppConstant.USER_LOGIN, false);
                isLogin = false;
                new AlertDialog(SmartHomeMainActivity.this).builder().setTitle("账号异地登录")
                        .setMsg("当前账号已在其它设备上登录,是否重新登录")
                        .setPositiveButton("确认", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                startActivity(new Intent(SmartHomeMainActivity.this, LoginActivity.class));
                            }
                        }).setNegativeButton("取消", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                }).show();
            }
        };
        String phoneNumber = Perfence.getPerfence(Perfence.PERFENCE_PHONE);
        String password = Perfence.getPerfence(Perfence.USER_PASSWORD);
        Log.i(TAG, "phoneNumber=" + phoneNumber + "password=" + password);
        if (!password.equals("")) {
            Perfence.setPerfence(AppConstant.USER_LOGIN, false);
            manager.login(phoneNumber, password);
        }
        initListener();
    }

    private void initManager() {
        mRoomManager = RoomManager.getInstance();
        mRoomManager.initRoomManager(this);
        mDeviceManager = DeviceManager.getInstance();
        mDeviceManager.InitDeviceManager(this);
        mRemoteControlManager = RemoteControlManager.getInstance();
        mRemoteControlManager.InitRemoteControlManager(this);
        mSmartLockManager = SmartLockManager.getInstance();
        mSmartLockManager.InitSmartLockManager(this);
        mDoorbeelManager = DoorbeelManager.getInstance();
    }

    private AdapterView.OnItemClickListener mExperienceCenterListClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            DeviceManager.getInstance().setStartFromExperience(true);
            DeviceManager.getInstance().setStartFromHomePage(true);
            switch (mExperienceCenterDeviceList.get(position).getDeviceName()) {
                case "智能门锁":
                    Intent intent = new Intent(SmartHomeMainActivity.this, SmartLockActivity.class);
                    startActivity(intent);
                    break;
                case "智能网关":
                    Intent intentGetwayDevice = new Intent(SmartHomeMainActivity.this, GetwayDeviceActivity.class);
                    startActivity(intentGetwayDevice);
                    break;
            }
        }
    };

    private void initEvents() {
        AppManager.getAppManager().addActivity(this);
        layout_experience_center_top.setOnClickListener(this);
        layout_devices.setOnClickListener(this);
        layout_rooms.setOnClickListener(this);
        layout_personal_center.setOnClickListener(this);
        textview_change_show_type.setOnClickListener(this);
        add_equiment.setOnClickListener(this);
        deviceGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mDeviceManager.setStartFromExperience(false);
                if (datasTop.size() < (position + 1)) {
                    //智能设备
                    String deviceType = datasBottom.get(position - datasTop.size()).getType();
                    String deviceSubType = datasBottom.get(position - datasTop.size()).getSubType();
                    Log.i(TAG, "智能设备类型=" + deviceType);
                    mDeviceManager.setCurrentSelectSmartDevice(datasBottom.get(position - datasTop.size()));
                    switch (deviceType) {
                        case DeviceTypeConstant.TYPE.TYPE_LOCK:
                            //设置当前选中的门锁设备
                            mSmartLockManager.setCurrentSelectLock(datasBottom.get(position - datasTop.size()));
                            startActivity(new Intent(SmartHomeMainActivity.this, SmartLockActivity.class));
                            break;
                        case "IRMOTE_V2":
                        case DeviceTypeConstant.TYPE.TYPE_REMOTECONTROL:
                            RemoteControlManager.getInstance().setmSelectRemoteControlDevice(datasBottom.get(position - datasTop.size()));
                            startActivity(new Intent(SmartHomeMainActivity.this, RemoteControlActivity.class));
                            break;
                        case DeviceTypeConstant.TYPE.TYPE_AIR_REMOTECONTROL:
                            RemoteControlManager.getInstance().setmSelectRemoteControlDevice(datasBottom.get(position - datasTop.size()));
                            startActivity(new Intent(SmartHomeMainActivity.this, AirRemoteControlMianActivity.class));
                            break;
                        case DeviceTypeConstant.TYPE.TYPE_ROUTER:
                            RouterManager.getInstance().setCurrentSelectedRouter(datasBottom.get(position - datasTop.size()));
                            startActivity(new Intent(SmartHomeMainActivity.this, RouterMainActivity.class));
                            break;
                        case DeviceTypeConstant.TYPE.TYPE_TV_REMOTECONTROL:
                            RemoteControlManager.getInstance().setmSelectRemoteControlDevice(datasBottom.get(position - datasTop.size()));
                            startActivity(new Intent(SmartHomeMainActivity.this, TvMainActivity.class));
                            break;
                        case DeviceTypeConstant.TYPE.TYPE_TVBOX_REMOTECONTROL:
                            RemoteControlManager.getInstance().setmSelectRemoteControlDevice(datasBottom.get(position - datasTop.size()));
                            startActivity(new Intent(SmartHomeMainActivity.this, TvBoxMainActivity.class));
                            break;
                        case DeviceTypeConstant.TYPE.TYPE_SWITCH:
                            SmartSwitchManager.getInstance().setCurrentSelectSmartDevice(datasBottom.get(position - datasTop.size()));
                            switch (deviceSubType) {
                                case DeviceTypeConstant.TYPE_SWITCH_SUBTYPE.SUB_TYPE_SWITCH_ONEWAY:
                                    startActivity(new Intent(SmartHomeMainActivity.this, SwitchOneActivity.class));
                                    break;
                                case DeviceTypeConstant.TYPE_SWITCH_SUBTYPE.SUB_TYPE_SWITCH_TWOWAY:
                                    startActivity(new Intent(SmartHomeMainActivity.this, SwitchTwoActivity.class));
                                    break;
                                case DeviceTypeConstant.TYPE_SWITCH_SUBTYPE.SUB_TYPE_SWITCH_THREEWAY:
                                    startActivity(new Intent(SmartHomeMainActivity.this, SwitchThreeActivity.class));
                                    break;
                                case DeviceTypeConstant.TYPE_SWITCH_SUBTYPE.SUB_TYPE_SWITCH_FOURWAY:
                                    startActivity(new Intent(SmartHomeMainActivity.this, SwitchFourActivity.class));
                                    break;
                            }
                            break;
                        case DeviceTypeConstant.TYPE.TYPE_MENLING:
                            mDoorbeelManager.setCurrentSelectedDoorbeel(datasBottom.get(position - datasTop.size()));
                            startActivity(new Intent(SmartHomeMainActivity.this, DoorbeelMainActivity.class));
                            break;
                        case DeviceTypeConstant.TYPE.TYPE_LIGHT:
                            SmartLightManager.getInstance().setCurrentSelectLight(datasBottom.get(position - datasTop.size()));
                            startActivity(new Intent(SmartHomeMainActivity.this, LightActivity.class));
                            break;
                    }
                } else {
                    //网关设备
                    GetwayManager.getInstance().setCurrentSelectGetwayDevice(datasTop.get(position));
                    startActivity(new Intent(SmartHomeMainActivity.this, GetwayDeviceActivity.class));
                }
            }
        });
        listview_experience_center.setAdapter(mExperienceCenterListAdapter);
        Message msg = Message.obtain();
        msg.what = MSG_INIT_LOCATIONSERVICE;
        mHandler.sendMessage(msg);
    }

    private void initViews() {
        scroll_inner_wrap = (MyScrollView) findViewById(R.id.scroll_inner_wrap);
        layout_devices = (LinearLayout) findViewById(R.id.layout_devices);
        layout_rooms = (LinearLayout) findViewById(R.id.layout_rooms);
        layout_personal_center = (LinearLayout) findViewById(R.id.layout_personal_center);
        deviceGridView = (GridView) findViewById(R.id.grid);
        listview_experience_center = (ListView) findViewById(R.id.listview_experience_center);
        imageview_devices = (ImageView) findViewById(R.id.imageview_devices);
        imageview_home_page = (ImageView) findViewById(R.id.imageview_home_page);
        imageview_rooms = (ImageView) findViewById(R.id.imageview_rooms);
        imageview_personal_center = (ImageView) findViewById(R.id.imageview_personal_center);
        layout_experience_center_top = (RelativeLayout) findViewById(R.id.layout_experience_center_top);
        textview_change_show_type = (FrameLayout) findViewById(R.id.textview_change_show_type);
        textview_home = (TextView) findViewById(R.id.textview_home);
        textview_device = (TextView) findViewById(R.id.textview_device);
        textview_room = (TextView) findViewById(R.id.textview_room);
        textview_mine = (TextView) findViewById(R.id.textview_mine);
        textview_city = (TextView) findViewById(R.id.textview_city);
        textview_tempature = (TextView) findViewById(R.id.textview_tempature);
        textview_pm25 = (TextView) findViewById(R.id.textview_pm25);
        layout_roomselect_normal = (HorizontalScrollView) findViewById(R.id.layout_roomselect_normal);
        layout_roomselect_changed_ype = (NonScrollableListView) findViewById(R.id.layout_roomselect_changed_ype);
        layout_weather = (RelativeLayout) findViewById(R.id.layout_weather);
        empty_recently_device = (RelativeLayout) findViewById(R.id.empty_recently_device);
        add_equiment = (ImageView) findViewById(R.id.add_equiment);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mRoomManager.removeRoomListener(mRoomListener);
        mDeviceManager.removeDeviceListener(mDeviceListener);
        mDeviceManager.stopQueryStatu();
        mRemoteControlManager.removeRemoteControlListener(mRemoteControlListener);
        manager.removeEventCallback(ec);
        stopTimer();
    }
    private void stopTimer() {
        if (refreshTask != null) {
            refreshTask.cancel();
            refreshTask = null;
        }
        if (refreshTimer != null) {
            refreshTimer.cancel();
            refreshTimer = null;
        }
    }
    /**
     * 再按一次退出应用
     */
    private long exitTime = 0;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if ((System.currentTimeMillis() - exitTime) > 2000) {
                Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                AppManager.getAppManager().finishAllActivity();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.layout_experience_center_top:
                DeviceManager.getInstance().setExperCenterStartFromHomePage(true);
                startActivity(new Intent(this, ExperienceDevicesActivity.class));
                break;
            case R.id.layout_devices:
                startActivity(new Intent(this, DevicesActivity.class));
                break;
            case R.id.layout_rooms:
                startActivity(new Intent(this, RoomActivity.class));
                break;
            case R.id.layout_personal_center:
                startActivity(new Intent(this, PersonalCenterActivity.class));
                break;
            case R.id.add_equiment:
                startActivity(new Intent(this, AddDeviceQRcodeActivity.class));
                break;
            case R.id.textview_change_show_type:
                if (layout_roomselect_normal.getVisibility() == View.VISIBLE) {
                    Perfence.setPerfence(Perfence.HOMEPAGE_DEVICE_SHOW_STYLE, Perfence.HOMEPAGE_DEVICE_SHOW_STYLE_CHANGE);
                    layout_roomselect_normal.setVisibility(View.GONE);
                    layout_roomselect_changed_ype.setVisibility(View.VISIBLE);
                    scroll_inner_wrap.smoothScrollTo(0, 0);
                } else {
                    Perfence.setPerfence(Perfence.HOMEPAGE_DEVICE_SHOW_STYLE, Perfence.HOMEPAGE_DEVICE_SHOW_STYLE_NORMAL);
                    layout_roomselect_normal.setVisibility(View.VISIBLE);
                    layout_roomselect_changed_ype.setVisibility(View.GONE);
                    layout_roomselect_normal.smoothScrollTo(0, 0);
                    mDeviceAdapter.setTopList(datasTop);
                    mDeviceAdapter.setBottomList(datasBottom);
                    mDeviceAdapter.notifyDataSetChanged();
                }
                break;
        }
    }
    private void updateDoorBellStatus() {
        if (seachedDoorbellmac != null) {
            seachedDoorbellmac = seachedDoorbellmac.replaceAll("0x", "").trim();
            seachedDoorbellmac = seachedDoorbellmac.replaceAll(" ", "-");
            if (currentSmartDoorBell != null) {
                if (seachedDoorbellmac.equalsIgnoreCase(currentSmartDoorBell.getMac())) {
                    currentSmartDoorBell.setStatus("在线");
                    currentSmartDoorBell.saveFast();
                    ellESDK.stopSearchDevs();
                }
            }
        }
    }
    @Override
    public void onRecvEllEPacket(BasicPacket packet) {
        receverDoorbellMsg=true;
        Log.i(TAG, "onRecvEllEPacket" + packet.toString() + packet.mac);
        seachedDoorbellmac = DataExchange.byteArrayToHexString(DataExchange.longToEightByte(packet.mac));
        updateDoorBellStatus();
        Log.i(TAG, "onRecvEllEPacket" + seachedDoorbellmac);

    }
    @Override
    public void searchDevCBS(long mac, byte type, byte ver) {
        receverDoorbellMsg=true;
        Log.e(TAG, "mac:" + mac + "type:" + type + "ver:" + ver);
        seachedDoorbellmac = DataExchange.byteArrayToHexString(DataExchange.longToEightByte(mac));
        updateDoorBellStatus();
    }
}
