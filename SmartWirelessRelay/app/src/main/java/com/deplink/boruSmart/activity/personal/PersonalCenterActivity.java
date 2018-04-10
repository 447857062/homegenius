package com.deplink.boruSmart.activity.personal;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.deplink.boruSmart.activity.SharedDeviceListActivity;
import com.deplink.boruSmart.activity.device.DevicesActivity;
import com.deplink.boruSmart.activity.homepage.SmartHomeMainActivity;
import com.deplink.boruSmart.activity.personal.experienceCenter.ExperienceDevicesActivity;
import com.deplink.boruSmart.activity.personal.login.LoginActivity;
import com.deplink.boruSmart.activity.personal.softupdate.UpdateImmediateActivity;
import com.deplink.boruSmart.activity.personal.usrinfo.UserinfoActivity;
import com.deplink.boruSmart.activity.room.RoomActivity;
import com.deplink.boruSmart.application.AppManager;
import com.deplink.boruSmart.constant.AppConstant;
import com.deplink.boruSmart.manager.device.DeviceManager;
import com.deplink.boruSmart.util.APKVersionCodeUtils;
import com.deplink.boruSmart.util.Perfence;
import com.deplink.boruSmart.util.WeakRefHandler;
import com.deplink.boruSmart.view.combinationwidget.TitleLayout;
import com.deplink.boruSmart.view.dialog.AlertDialog;
import com.deplink.boruSmart.view.imageview.CircleImageView;
import com.deplink.boruSmart.view.toast.Ftoast;
import com.deplink.sdk.android.sdk.DeplinkSDK;
import com.deplink.sdk.android.sdk.EventCallback;
import com.deplink.sdk.android.sdk.SDKAction;
import com.deplink.sdk.android.sdk.homegenius.UserInfoAlertBody;
import com.deplink.sdk.android.sdk.manager.SDKManager;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileOutputStream;

import deplink.com.smartwirelessrelay.homegenius.EllESDK.R;

public class PersonalCenterActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "PersonalCenterActivity";
    private RelativeLayout layout_getway_check;
    private RelativeLayout layout_experience_center;
    private LinearLayout layout_home_page;
    private LinearLayout layout_devices;
    private LinearLayout layout_rooms;
    private LinearLayout layout_personal_center;
    private CircleImageView user_head_portrait;
    private RelativeLayout layout_user_info;
    private ImageView imageview_devices;
    private ImageView imageview_home_page;
    private ImageView imageview_rooms;
    private ImageView imageview_personal_center;
    private TextView textview_home;
    private TextView textview_device;
    private TextView textview_room;
    private TextView textview_mine;
    private SDKManager manager;
    private EventCallback ec;
    private boolean isUserLogin;
    private TextView user_nickname;
    private TextView textview_update_now;
    private boolean isAppUpdate = false;
    private boolean isClickUpdate;
    private RelativeLayout layout_update_soft;
    private TextView textview_current_version;
    private RelativeLayout layout_device_share;
    private RelativeLayout layout_about;
    private TitleLayout layout_title;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_center);
        initViews();
        initDatas();
        initEvents();

    }
    @Override
    protected void onResume() {
        super.onResume();
        manager.addEventCallback(ec);
        textview_home.setTextColor(ContextCompat.getColor(this,R.color.line_clolor));
        textview_device.setTextColor(ContextCompat.getColor(this,R.color.line_clolor));
        textview_room.setTextColor(ContextCompat.getColor(this,R.color.line_clolor));
        textview_mine.setTextColor(ContextCompat.getColor(this,R.color.room_type_text));
        imageview_home_page.setImageResource(R.drawable.nocheckthehome);
        imageview_devices.setImageResource(R.drawable.nocheckthedevice);
        imageview_rooms.setImageResource(R.drawable.nochecktheroom);
        imageview_personal_center.setImageResource(R.drawable.checkthemine);
        isUserLogin = Perfence.getBooleanPerfence(AppConstant.USER_LOGIN);
        if (isUserLogin) {
            boolean hasGetUserImage = Perfence.getBooleanPerfence(AppConstant.USER.USER_GETIMAGE_FROM_SERVICE);
            if (!hasGetUserImage) {
                Perfence.setPerfence(AppConstant.USER.USER_GETIMAGE_FROM_SERVICE, true);
                manager.getImage(Perfence.getPerfence(Perfence.PERFENCE_PHONE));
            } else {
                setLocalImage(user_head_portrait);
            }
            String userName=Perfence.getPerfence(Perfence.PERFENCE_PHONE);
            manager.getUserInfo(userName);

        }else{
            user_head_portrait.setImageDrawable(ContextCompat.getDrawable(PersonalCenterActivity.this,R.drawable.defaultavatar));
            user_nickname.setText("请登录");
        }
        manager.queryAppUpdateInfo(Perfence.SDK_APP_KEY, APKVersionCodeUtils.getVerName(this));
        if( manager.getAppUpdateInfo()!=null){
            String version = manager.getAppUpdateInfo().getVersion();
            int oldVersion=Integer.valueOf(APKVersionCodeUtils.getVerName(PersonalCenterActivity.this).replace(".",""));
            int newVersion=Integer.valueOf(version.replace(".",""));
            isAppUpdate = !APKVersionCodeUtils.getVerName(PersonalCenterActivity.this).equals(version) && (newVersion > oldVersion);
        }else{
            isAppUpdate = false;
            textview_update_now.setText("已是最新版本");
        }
        textview_current_version.setText("当前版本:"+APKVersionCodeUtils.getVerName(this));
    }
    private void setLocalImage(CircleImageView user_head_portrait) {
        boolean isSdCardExist = Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);// 判断sdcard是否存在
        if (isSdCardExist) {
            String path = this.getFilesDir().getAbsolutePath();
            path = path + File.separator + "userIcon" + "userIcon.png";
            File file = new File(path);
            if (file.exists()) {
                Bitmap bm = BitmapFactory.decodeFile(file.getPath());
                // 将图片显示到ImageView中
                user_head_portrait.setImageBitmap(bm);
            } else {
                user_head_portrait.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.defaultavatar));
            }
        } else {
            Ftoast.create(PersonalCenterActivity.this).setText("sd卡不存在").show();
        }
    }
    private void initDatas() {
        layout_title.setReturnClickListener(new TitleLayout.ReturnImageClickListener() {
            @Override
            public void onBackPressed() {
                PersonalCenterActivity.this.onBackPressed();
            }
        });
        DeplinkSDK.initSDK(getApplicationContext(), Perfence.SDK_APP_KEY);
        manager = DeplinkSDK.getSDKManager();
        ec = new EventCallback() {

            @Override
            public void onSuccess(SDKAction action) {
                switch (action){
                    case LOGOUT:
                        Perfence.setPerfence(AppConstant.USER_LOGIN,false);
                        startActivity(new Intent(PersonalCenterActivity.this, LoginActivity.class));
                        break;
                    case APPUPDATE:
                            String version = manager.getAppUpdateInfo().getVersion();
                            int oldVersion=Integer.valueOf(APKVersionCodeUtils.getVerName(PersonalCenterActivity.this).replace(".",""));
                            int newVersion=Integer.valueOf(version.replace(".",""));
                            if (!APKVersionCodeUtils.getVerName(PersonalCenterActivity.this).equals(version) && (newVersion>oldVersion)) {
                                isAppUpdate = true;
                                textview_update_now.setText("立即升级");
                            } else {
                                textview_update_now.setText("已是最新版本");
                                isAppUpdate = false;
                            }
                            if(isClickUpdate){
                                isClickUpdate=false;
                                if(isAppUpdate){
                                    startActivity(new Intent(PersonalCenterActivity.this, UpdateImmediateActivity.class));
                                }else{
                                    Ftoast.create(PersonalCenterActivity.this).setText("已是最新版本").show();
                                }
                            }
                        break;
                }
            }
            @Override
            public void onBindSuccess(SDKAction action, String devicekey) {
            }

            @Override
            public void onGetImageSuccess(SDKAction action, final Bitmap bm) {
                //保存到本地
                try {
                    Log.i(TAG,"onGetImageSuccess");
                    user_head_portrait.setImageBitmap(bm);
                    saveToSDCard(bm);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onGetUserInfouccess(String info) {
                super.onGetUserInfouccess(info);
                Gson gson = new Gson();
                if(!info.equalsIgnoreCase("[]")){
                    UserInfoAlertBody responseInfo = gson.fromJson(info, UserInfoAlertBody.class);
                    user_nickname.setText(responseInfo.getNickname());
                }
            }

            @Override
            public void onFailure(SDKAction action, Throwable throwable) {

            }
            @Override
            public void connectionLost(Throwable throwable) {
                super.connectionLost(throwable);
                mHandler.sendEmptyMessage(MSG_SHOW_CONNECT_LOST);

            }
        };
    }
    private static final int MSG_SHOW_CONNECT_LOST=100;
    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what){
                case MSG_SHOW_CONNECT_LOST:
                    Perfence.setPerfence(AppConstant.USER_LOGIN, false);
                    isUserLogin=false;
                    user_head_portrait.setImageDrawable(ContextCompat.getDrawable(PersonalCenterActivity.this,R.drawable.defaultavatar));
                    new AlertDialog(PersonalCenterActivity.this).builder().setTitle("账号异地登录")
                            .setMsg("当前账号已在其它设备上登录,是否重新登录")
                            .setPositiveButton("确认", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    startActivity(new Intent(PersonalCenterActivity.this, LoginActivity.class));
                                }
                            }).setNegativeButton("取消", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                        }
                    }).show();
                    break;
            }
            return true;
        }
    };
    private Handler mHandler = new WeakRefHandler(mCallback);
    private void saveToSDCard(Bitmap bitmap) {
        String path = this.getFilesDir().getAbsolutePath();
        path = path + File.separator + "userIcon" + "userIcon.png";
        File dest = new File(path);
        try {
            FileOutputStream out = new FileOutputStream(dest);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
            // PNG is a lossless format, the compression factor (100) is ignored
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void initEvents() {
        AppManager.getAppManager().addActivity(this);
        layout_getway_check.setOnClickListener(this);
        layout_experience_center.setOnClickListener(this);
        layout_home_page.setOnClickListener(this);
        layout_devices.setOnClickListener(this);
        layout_rooms.setOnClickListener(this);
        layout_personal_center.setOnClickListener(this);
        layout_user_info.setOnClickListener(this);
        layout_update_soft.setOnClickListener(this);
        layout_device_share.setOnClickListener(this);
        layout_about.setOnClickListener(this);
    }

    private void initViews() {
        layout_title= (TitleLayout) findViewById(R.id.layout_title);
        textview_home = (TextView) findViewById(R.id.textview_home);
        layout_device_share = (RelativeLayout) findViewById(R.id.layout_device_share);
        layout_about = (RelativeLayout) findViewById(R.id.layout_about);
        textview_device = (TextView) findViewById(R.id.textview_device);
        textview_room = (TextView) findViewById(R.id.textview_room);
        textview_mine = (TextView) findViewById(R.id.textview_mine);
        imageview_devices = (ImageView) findViewById(R.id.imageview_devices);
        imageview_home_page = (ImageView) findViewById(R.id.imageview_home_page);
        imageview_rooms = (ImageView) findViewById(R.id.imageview_rooms);
        imageview_personal_center = (ImageView) findViewById(R.id.imageview_personal_center);
        layout_getway_check = (RelativeLayout) findViewById(R.id.layout_getway_check);
        layout_experience_center = (RelativeLayout) findViewById(R.id.layout_experience_center);
        layout_home_page = (LinearLayout) findViewById(R.id.layout_home_page);
        layout_devices = (LinearLayout) findViewById(R.id.layout_devices);
        layout_rooms = (LinearLayout) findViewById(R.id.layout_rooms);
        layout_personal_center = (LinearLayout) findViewById(R.id.layout_personal_center);
        user_head_portrait = (CircleImageView) findViewById(R.id.user_head_portrait);
        layout_user_info = (RelativeLayout) findViewById(R.id.layout_user_info);
        user_nickname = (TextView) findViewById(R.id.user_nickname);
        textview_update_now = (TextView) findViewById(R.id.textview_update_now);
        layout_update_soft = (RelativeLayout) findViewById(R.id.layout_update_soft);
        textview_current_version = (TextView) findViewById(R.id.textview_current_version);
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
    protected void onPause() {
        super.onPause();
        manager.removeEventCallback(ec);
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.layout_getway_check:
                startActivity(new Intent(PersonalCenterActivity.this, HomeNetWorkActivity.class));
                break;
            case R.id.layout_experience_center:
                DeviceManager.getInstance().setExperCenterStartFromHomePage(false);
                startActivity(new Intent(this, ExperienceDevicesActivity.class));
                break;
            case R.id.layout_home_page:
                startActivity(new Intent(this, SmartHomeMainActivity.class));
                break;
            case R.id.layout_devices:
                startActivity(new Intent(this, DevicesActivity.class));
                break;
            case R.id.layout_rooms:
                startActivity(new Intent(this, RoomActivity.class));
                break;
            case R.id.layout_user_info:
                if(isUserLogin){
                    startActivity(new Intent(this, UserinfoActivity.class));
                }else{
                    startActivity(new Intent(this, LoginActivity.class));
                }

                break;
            case R.id.layout_update_soft:
                manager.queryAppUpdateInfo(Perfence.SDK_APP_KEY, APKVersionCodeUtils.getVerName(this));
                isClickUpdate=true;
                break;
            case R.id.layout_device_share:
                if(isUserLogin){
                    startActivity(new Intent(this, SharedDeviceListActivity.class));
                }else{
                    startActivity(new Intent(PersonalCenterActivity.this, LoginActivity.class));
                }
                break;
            case R.id.layout_about:

                break;

        }
    }
}
