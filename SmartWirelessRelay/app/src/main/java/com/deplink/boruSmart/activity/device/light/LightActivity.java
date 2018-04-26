package com.deplink.boruSmart.activity.device.light;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.deplink.boruSmart.Protocol.json.QueryOptions;
import com.deplink.boruSmart.Protocol.json.device.SmartDev;
import com.deplink.boruSmart.constant.AppConstant;
import com.deplink.boruSmart.manager.connect.local.tcp.LocalConnectmanager;
import com.deplink.boruSmart.manager.device.DeviceManager;
import com.deplink.boruSmart.manager.device.light.SmartLightListener;
import com.deplink.boruSmart.manager.device.light.SmartLightManager;
import com.deplink.boruSmart.util.NetUtil;
import com.deplink.boruSmart.util.Perfence;
import com.deplink.boruSmart.util.WeakRefHandler;
import com.deplink.boruSmart.view.combinationwidget.TitleLayout;
import com.deplink.boruSmart.view.toast.Ftoast;
import com.deplink.sdk.android.sdk.DeplinkSDK;
import com.deplink.sdk.android.sdk.EventCallback;
import com.deplink.sdk.android.sdk.SDKAction;
import com.deplink.sdk.android.sdk.manager.SDKManager;
import com.google.gson.Gson;

import deplink.com.smartwirelessrelay.homegenius.EllESDK.R;

public class LightActivity extends Activity implements View.OnClickListener, SmartLightListener {
    private static final String TAG = "LightActivity";
    /**
     * 中间的灯泡切换白色黄色灯泡背景
     */
    private ImageView button_switch_light;
    /**
     * 关闭状态:中间灯泡后面的一个空心圆形背景
     * 打开状态:实心白色背景
     */
    private ImageView iamgeview_switch;
    /**
     * 白色光晕的图片,白光亮度调节
     */
    private ImageView imageview_switch_bg;

    private SmartLightManager mSmartLightManager;
    private boolean switchStatus;
    private SeekBar progressBarLightYellow;
    private int lightColorProgress;
    private SeekBar progressBarLightWhite;
    private int lightBrightnessProgress;
    private ImageView imageview_lightyellow_reduce;
    private ImageView imageview_lightyellow_plus;
    private ImageView imageview_lightwhite_reduce;
    private ImageView imageview_lightwhite_plus;

    private TextView textview_switch_tips;
    private RelativeLayout layout_lightcolor_control;
    private RelativeLayout layout_brightness_control;
    private SDKManager manager;
    private EventCallback ec;
    private boolean isUserLogin;
    private boolean isStartFromExperience;
    private SmartDev currentSelectLight;
    private TitleLayout layout_title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_light);
        initViews();
        initDatas();
        initEvents();
    }

    private void initEvents() {
        button_switch_light.setOnClickListener(this);
        imageview_lightyellow_reduce.setOnClickListener(this);
        imageview_lightyellow_plus.setOnClickListener(this);
        imageview_lightwhite_reduce.setOnClickListener(this);
        imageview_lightwhite_plus.setOnClickListener(this);
        progressBarLightYellow.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                lightColorProgress = progress * 2;
                if (isStartFromExperience) {
                    Log.i(TAG, "lightColorProgress=" + lightColorProgress);
                    button_switch_light.setBackgroundResource(R.drawable.lightyellowlight);
                    float alpha = (float) (lightColorProgress / 200.0);
                    button_switch_light.setAlpha(alpha);
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (!isStartFromExperience) {
                    mSmartLightManager.setSmartLightParamas("regulation", lightColorProgress, lightBrightnessProgress);
                }
            }
        });
        progressBarLightWhite.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                lightBrightnessProgress = progress * 2;
                if (isStartFromExperience) {
                    float alpha = (float) (lightBrightnessProgress / 200.0);
                    imageview_switch_bg.setAlpha(alpha);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (!isStartFromExperience) {
                    mSmartLightManager.setSmartLightParamas("regulation", lightColorProgress, lightBrightnessProgress);
                }
            }
        });
    }

    private void initDatas() {
        layout_title.setBackImageResource(R.drawable.whitereturn);
        layout_title.setTitleTextWhiteColor();
        layout_title.setReturnClickListener(new TitleLayout.ReturnImageClickListener() {
            @Override
            public void onBackPressed() {
                LightActivity.this.onBackPressed();
            }
        });
        layout_title.setEditTextClickListener(new TitleLayout.EditTextClickListener() {
            @Override
            public void onEditTextPressed() {
                startActivity(new Intent(LightActivity.this, LightEditActivity.class));
            }
        });
        layout_title.setEditTextWhiteColor();
        layout_title.setLineDirverVisiable(false);
        mSmartLightManager = SmartLightManager.getInstance();
        mSmartLightManager.InitSmartLightManager(this);
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
            public void notifyHomeGeniusResponse(String result) {
                super.notifyHomeGeniusResponse(result);
                Gson gson = new Gson();
                QueryOptions resultObj = gson.fromJson(result, QueryOptions.class);
                Log.i(TAG, TAG + "notifyHomeGeniusResponse");
                if (resultObj.getOP().equalsIgnoreCase("REPORT") && resultObj.getMethod().equalsIgnoreCase("YWLIGHTCONTROL")) {
                    Message msg = Message.obtain();
                    msg.obj = resultObj;
                    msg.what = MSG_GET_LIGHT_RESULT;
                    if (!isStartFromExperience) {
                        mHandler.sendMessage(msg);
                    }
                }
            }

            @Override
            public void deviceOpSuccess(String op, String deviceKey) {
                super.deviceOpSuccess(op, deviceKey);
            }

            @Override
            public void connectionLost(Throwable throwable) {
                super.connectionLost(throwable);
                isUserLogin = false;

            }

            @Override
            public void onFailure(SDKAction action, Throwable throwable) {
            }
        };
    }

    private void initViews() {
        button_switch_light = findViewById(R.id.button_switch_light);
        progressBarLightYellow = findViewById(R.id.lightColorProgressBar);
        progressBarLightWhite = findViewById(R.id.progressBar_brightness);
        imageview_lightyellow_reduce = findViewById(R.id.imageview_lightyellow_reduce);
        imageview_lightyellow_plus = findViewById(R.id.imageview_lightyellow_plus);
        imageview_lightwhite_reduce = findViewById(R.id.imageview_lightwhite_reduce);
        imageview_lightwhite_plus = findViewById(R.id.imageview_lightwhite_plus);
        iamgeview_switch = findViewById(R.id.iamgeview_switch);
        textview_switch_tips = findViewById(R.id.textview_switch_tips);
        imageview_switch_bg = findViewById(R.id.imageview_switch_bg);
        layout_brightness_control = findViewById(R.id.layout_brightness_control);
        layout_lightcolor_control = findViewById(R.id.layout_lightcolor_control);
        layout_title = findViewById(R.id.layout_title);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSmartLightManager.releaswSmartManager();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isUserLogin = Perfence.getBooleanPerfence(AppConstant.USER_LOGIN);
        isStartFromExperience = DeviceManager.getInstance().isStartFromExperience();
        manager.addEventCallback(ec);
        if (progressBarLightWhite.getProgress() == 0) {
            imageview_switch_bg.setAlpha(0.0f);
        }
        if (isStartFromExperience) {
            iamgeview_switch.setBackgroundResource(R.drawable.offlight);
            imageview_switch_bg.setBackgroundResource(R.color.room_type_text);
            textview_switch_tips.setText("点击开启");
            button_switch_light.setBackgroundResource(R.drawable.lightwhitelight);
            layout_lightcolor_control.setVisibility(View.GONE);
            layout_brightness_control.setVisibility(View.GONE);

        } else {
            currentSelectLight = mSmartLightManager.getCurrentSelectLight();
            int usercount = currentSelectLight.getUserCount();
            usercount++;
            currentSelectLight.setUserCount(usercount);
            currentSelectLight.save();

            mSmartLightManager.queryLightStatus();
            mSmartLightManager.addSmartLightListener(this);
            //读取保存在数据库中的状态
            if (currentSelectLight != null) {
                if (currentSelectLight.getLightIsOpen() == 1) {
                    iamgeview_switch.setBackgroundResource(R.drawable.onlight);
                    imageview_switch_bg.setBackgroundResource(R.drawable.lightglowoutside);
                    textview_switch_tips.setText("点击关闭");
                    layout_lightcolor_control.setVisibility(View.VISIBLE);
                    layout_brightness_control.setVisibility(View.VISIBLE);
                    float alpha = (float) (currentSelectLight.getWhiteValue() / 200.0);
                    imageview_switch_bg.setAlpha(alpha);
                    progressBarLightWhite.setProgress(currentSelectLight.getWhiteValue() / 2);
                    button_switch_light.setBackgroundResource(R.drawable.lightyellowlight);
                    float alpha2 = (float) (currentSelectLight.getYellowValue() / 200.0);
                    button_switch_light.setAlpha(alpha2);
                    progressBarLightYellow.setProgress(currentSelectLight.getYellowValue() / 2);
                } else {
                    iamgeview_switch.setBackgroundResource(R.drawable.offlight);
                    imageview_switch_bg.setBackgroundResource(R.color.room_type_text);
                    textview_switch_tips.setText("点击开启");
                    layout_lightcolor_control.setVisibility(View.GONE);
                    layout_brightness_control.setVisibility(View.GONE);
                }
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        manager.removeEventCallback(ec);
        mSmartLightManager.removeSmartLightListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imageview_lightyellow_reduce:
                lightColorProgress -= 20;
                if (lightColorProgress < 0) {
                    lightColorProgress = 200;
                }
                if (isStartFromExperience) {
                    progressBarLightYellow.setProgress(lightColorProgress);
                } else {
                    mSmartLightManager.setSmartLightParamas("regulation", lightColorProgress, lightBrightnessProgress);
                }
                break;
            case R.id.imageview_lightyellow_plus:
                lightColorProgress += 20;
                if (lightColorProgress > 200) {
                    lightColorProgress = 0;
                }
                if (isStartFromExperience) {
                    progressBarLightYellow.setProgress(lightColorProgress);
                } else {
                    mSmartLightManager.setSmartLightParamas("regulation", lightColorProgress, lightBrightnessProgress);
                }
                break;
            case R.id.imageview_lightwhite_reduce:
                lightBrightnessProgress -= 20;
                if (lightBrightnessProgress < 0) {
                    lightBrightnessProgress = 200;
                }
                if (isStartFromExperience) {
                    progressBarLightWhite.setProgress(lightBrightnessProgress);
                } else {
                    mSmartLightManager.setSmartLightParamas("regulation", lightColorProgress, lightBrightnessProgress);
                }

                break;
            case R.id.imageview_lightwhite_plus:
                lightBrightnessProgress += 20;
                if (lightBrightnessProgress > 200) {
                    lightBrightnessProgress = 0;
                }
                if (isStartFromExperience) {
                    progressBarLightWhite.setProgress(lightBrightnessProgress);
                } else {
                    mSmartLightManager.setSmartLightParamas("regulation", lightColorProgress, lightBrightnessProgress);
                }

                break;
            case R.id.button_switch_light:
                Log.i(TAG,"switchStatus="+switchStatus+"isStartFromExperience="+isStartFromExperience);
                if (isStartFromExperience) {
                    if (switchStatus) {
                        switchStatus = false;
                        iamgeview_switch.setBackgroundResource(R.drawable.offlight);
                        imageview_switch_bg.setBackgroundResource(R.color.room_type_text);
                        textview_switch_tips.setText("点击开启");
                        layout_lightcolor_control.setVisibility(View.GONE);
                        layout_brightness_control.setVisibility(View.GONE);
                        button_switch_light.setAlpha(0.0f);
                    } else {
                        switchStatus = true;
                        iamgeview_switch.setBackgroundResource(R.drawable.onlight);
                        imageview_switch_bg.setBackgroundResource(R.drawable.lightglowoutside);
                        textview_switch_tips.setText("点击关闭");
                        layout_lightcolor_control.setVisibility(View.VISIBLE);
                        layout_brightness_control.setVisibility(View.VISIBLE);
                        button_switch_light.setAlpha(1.0f);
                    }
                } else {
                    if (NetUtil.isNetAvailable(this)) {
                        if (!isUserLogin && !LocalConnectmanager.getInstance().isLocalconnectAvailable()) {
                            Ftoast.create(LightActivity.this).setText("本地连接不可用,需要登录后才能操作").show();
                        } else {
                            if (switchStatus) {
                                switchStatus = false;
                                mSmartLightManager.setSmartLightSwitch("close");
                            } else {
                                switchStatus = true;
                                mSmartLightManager.setSmartLightSwitch("open");
                            }
                        }
                    } else {
                        Ftoast.create(LightActivity.this).setText("网络连接不正常").show();
                    }
                }
                break;

        }
    }

    private static final int MSG_GET_LIGHT_RESULT = 100;
    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_GET_LIGHT_RESULT:
                    QueryOptions resultObj = (QueryOptions) msg.obj;
                    Log.i(TAG, "resultObj=" + resultObj.toString());
                    if (resultObj.getMethod().equals("YWLIGHTCONTROL")) {
                        if (resultObj.getYellow() != 0) {
                            button_switch_light.setBackgroundResource(R.drawable.lightyellowlight);
                            float alpha = (float) (resultObj.getYellow() / 200.0);
                            Log.i(TAG, "alpha=" + alpha);
                            if (alpha > 1.0) {
                                alpha = 1.0f;
                            }
                            button_switch_light.setAlpha(alpha);
                            progressBarLightYellow.setProgress(resultObj.getYellow() / 2);
                        }
                        if (resultObj.getWhite() != 0) {
                            float alpha = (float) (resultObj.getWhite() / 200.0);
                            if (alpha > 1.0) {
                                alpha = 1.0f;
                            }
                            Log.i(TAG, "alpha=" + alpha);
                            imageview_switch_bg.setAlpha(alpha);
                            progressBarLightWhite.setProgress(resultObj.getWhite() / 2);
                        }
                        if (resultObj.getOpen() == 1) {
                            iamgeview_switch.setBackgroundResource(R.drawable.onlight);
                            imageview_switch_bg.setBackgroundResource(R.drawable.lightglowoutside);
                            textview_switch_tips.setText("点击关闭");
                            layout_lightcolor_control.setVisibility(View.VISIBLE);
                            layout_brightness_control.setVisibility(View.VISIBLE);
                            if (currentSelectLight != null) {
                                currentSelectLight.setLightIsOpen(1);
                                currentSelectLight.setStatus("在线");
                                currentSelectLight.saveFast();
                            }
                        } else if (resultObj.getOpen() == 2) {
                            button_switch_light.setAlpha(0.0f);
                            iamgeview_switch.setBackgroundResource(R.drawable.offlight);
                            imageview_switch_bg.setBackgroundResource(R.color.room_type_text);
                            textview_switch_tips.setText("点击开启");
                            layout_lightcolor_control.setVisibility(View.GONE);
                            layout_brightness_control.setVisibility(View.GONE);
                            if (currentSelectLight != null) {
                                currentSelectLight.setLightIsOpen(2);
                                currentSelectLight.setWhiteValue(resultObj.getWhite());
                                currentSelectLight.setYellowValue(resultObj.getYellow());
                                currentSelectLight.setStatus("在线");
                                currentSelectLight.saveFast();
                            }
                        }
                    }

                    break;
            }
            return true;
        }
    };
    private Handler mHandler = new WeakRefHandler(mCallback);

    @Override
    public void responseSetResult(String result) {
        Log.i(TAG, "responseSetResult=" + result);
        Gson gson = new Gson();
        QueryOptions resultObj = gson.fromJson(result, QueryOptions.class);
        Message msg = Message.obtain();
        msg.obj = resultObj;
        msg.what = MSG_GET_LIGHT_RESULT;
        if (!isStartFromExperience) {
            mHandler.sendMessage(msg);
        }
    }
}
