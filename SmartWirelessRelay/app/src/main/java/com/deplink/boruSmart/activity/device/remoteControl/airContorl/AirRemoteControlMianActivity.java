package com.deplink.boruSmart.activity.device.remoteControl.airContorl;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.deplink.boruSmart.Protocol.json.device.remotecontrol.AirconditionInitKeyValue;
import com.deplink.boruSmart.Protocol.json.device.remotecontrol.AirconditionKeyCode;
import com.deplink.boruSmart.Protocol.json.device.remotecontrol.AirconditionKeyLearnStatu;
import com.deplink.boruSmart.activity.device.remoteControl.LearnByHandActivity;
import com.deplink.boruSmart.constant.AirKeyNameConstant;
import com.deplink.boruSmart.constant.DeviceTypeConstant;
import com.deplink.boruSmart.manager.device.DeviceManager;
import com.deplink.boruSmart.manager.device.remoteControl.RemoteControlManager;
import com.deplink.boruSmart.util.DataExchange;
import com.deplink.boruSmart.view.combinationwidget.TitleLayout;
import com.deplink.boruSmart.view.dialog.ActionSheetDialog;
import com.deplink.boruSmart.view.dialog.KeynotlearnDialog;
import com.deplink.boruSmart.view.dialog.remotecontrol.RemoteControlMenuDialog;

import org.litepal.crud.DataSupport;

import deplink.com.smartwirelessrelay.homegenius.EllESDK.R;

public class AirRemoteControlMianActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "ARCMianActivity";
    private TextView textview_model;
    private TextView textview_wind_speed;
    private TextView textview_wind_direction;
    private ImageView imageview_model;
    private ImageView imageview_wind_speed;
    private ImageView imageview_wind_direction;
    private ImageView imageview_power;
    private ImageView imageview_temperature_reduce;
    private ImageView imageview_temperature_plus;
    private RemoteControlMenuDialog menu_dialog;
    private RelativeLayout layout_top_content;
    /**
     * 空调各个按键的学习状态
     */
    private boolean key_tempature_reduce;
    private boolean key_tempature_plus;
    private boolean key_power;
    private boolean key_mode_hot;
    private boolean key_mode_cold;
    private boolean key_mode_dehumit;
    private boolean key_mode_wind;
    private boolean key_mode_auto;
    private boolean key_windspeed_hight;
    private boolean key_windspeed_middle;
    private boolean key_windspeed_low;
    private boolean key_windspeed_auto;
    private boolean key_winddirection_up;
    private boolean key_winddirection_middle;
    private boolean key_winddirection_down;
    private boolean key_winddirection_auto;
    /**
     * 未学习按键的提示
     */
    private KeynotlearnDialog mKeynotlearnDialog;
    private RemoteControlManager mRemoteControlManager;
    private int group;
    private String code;
    private TextView textview_tips;
    private TextView textview_temperature;
    private boolean isStartFromExperience;
    private int currentMode;
    private int wind;
    private int directionHand;
    private int directionAuto;
    private int tempature;
    private int power;
    private TitleLayout layout_title;
    private TextView textview_tempature_unit;
    private TextView textview_power;
    private ImageView gatwaygif;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_air_remote_control);
        initViews();
        initDatas();
        initEvents();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isStartFromExperience = DeviceManager.getInstance().isStartFromExperience();
        initKeylearnStatus();
        initKeyCodeData();
        layout_title.setEditTextVisiable(false);
        if(!isStartFromExperience){
            int usercount=mRemoteControlManager.getmSelectRemoteControlDevice().getUserCount();
            usercount++;
            mRemoteControlManager.getmSelectRemoteControlDevice().setUserCount(usercount);
            mRemoteControlManager.getmSelectRemoteControlDevice().save();
        }
        initImageViewKeyBackground();
    }

    /**
     * 初始化按键的背景，学习过和未学习的按键背景不一样，点击效果也不一样
     */
    private void initImageViewKeyBackground() {
        if(isStartFromExperience){
            currentMode=0x05;
            wind=0x04;
            directionAuto=0x01;
        }else{
            currentMode = mAirconditionInitKeyValue.getMode();
            wind = mAirconditionInitKeyValue.getWind();
            directionAuto = mAirconditionInitKeyValue.getDirectionAuto();
        }
        directionHand = mAirconditionInitKeyValue.getDirectionHand();
        tempature = mAirconditionInitKeyValue.getTempature();
        power = mAirconditionInitKeyValue.getKeyPower();
        final Animation animationFadeIn= AnimationUtils.loadAnimation(this, R.anim.fade_in);
        final Animation animationFadeOut= AnimationUtils.loadAnimation(this, R.anim.fade_out);
        final Animation animationFadeHold= AnimationUtils.loadAnimation(this, R.anim.fade_hold);
        if (key_power) {
            gatwaygif.setVisibility(View.VISIBLE);
            imageview_power.setBackgroundResource(R.drawable.button_power_learn_normal);
            animationFadeIn.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    gatwaygif.startAnimation(animationFadeOut);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            animationFadeOut.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    gatwaygif.startAnimation(animationFadeHold);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            animationFadeHold.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    gatwaygif.startAnimation(animationFadeIn);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            gatwaygif.startAnimation(animationFadeIn);
        } else {
            imageview_power.setBackgroundResource(R.drawable.button_power_notlearn);
            gatwaygif.setVisibility(View.GONE);
        }

        //空调开关时设置其他按键的可不可以点击
        if (power == 1) {
            switchAirconditionOnOff(true);
        } else {
            switchAirconditionOnOff(false);
        }
        textview_temperature.setText("" + tempature);
        switch (currentMode) {
            case 0x01:
                if (key_mode_cold) {
                    imageview_model.setBackgroundResource(R.drawable.button_aircondition_mode_cold_learned);
                } else {
                    imageview_model.setBackgroundResource(R.drawable.button_aircondition_mode_cold_notlearn);
                }
                textview_model.setText("制冷模式");
                break;
            case 0x02:
                if (key_mode_dehumit) {
                    imageview_model.setBackgroundResource(R.drawable.button_aircondition_mode_dehumid_learned);
                } else {
                    imageview_model.setBackgroundResource(R.drawable.button_aircondition_mode_dehumid_notlearn);
                }
                textview_model.setText("除湿模式");
                break;
            case 0x03:
                if (key_mode_wind) {
                    imageview_model.setBackgroundResource(R.drawable.button_aircondition_mode_wind_learned);
                } else {
                    imageview_model.setBackgroundResource(R.drawable.button_aircondition_mode_wind_notlearn);
                }
                textview_model.setText("送风模式");
                break;
            case 0x04:
                if (key_mode_hot) {
                    imageview_model.setBackgroundResource(R.drawable.button_aircondition_mode_hot_learned);
                } else {
                    imageview_model.setBackgroundResource(R.drawable.button_aircondition_mode_hot_notlearn);
                }
                textview_model.setText("制热模式");
                break;
            case 0x05:
                if (key_mode_auto) {
                    imageview_model.setBackgroundResource(R.drawable.button_aircondition_mode_auto_learned);
                } else {
                    imageview_model.setBackgroundResource(R.drawable.button_aircondition_mode_auto_notlearn);
                }
                textview_model.setText("自动模式");
                break;
        }
        Log.i(TAG, "wind=" + wind);
        switch (wind) {
            case 1:
                if (key_windspeed_low) {
                    imageview_wind_speed.setBackgroundResource(R.drawable.button_aircondition_windspeed_low_learned);
                } else {
                    imageview_wind_speed.setBackgroundResource(R.drawable.button_aircondition_windspeed_low_notlearn);
                }
                textview_wind_speed.setText("低风");
                break;
            case 2:
                if (key_windspeed_middle) {
                    imageview_wind_speed.setBackgroundResource(R.drawable.button_aircondition_windspeed_middle_learned);
                } else {
                    imageview_wind_speed.setBackgroundResource(R.drawable.button_aircondition_windspeed_middle_notlearn);
                }
                textview_wind_speed.setText("中风");
                break;
            case 3:
                if (key_windspeed_hight) {
                    imageview_wind_speed.setBackgroundResource(R.drawable.button_aircondition_windspeed_hight_learned);
                } else {
                    imageview_wind_speed.setBackgroundResource(R.drawable.button_aircondition_windspeed_hight_notlearn);
                }
                textview_wind_speed.setText("高风");
                break;
            case 4:
                if (key_windspeed_auto) {
                    imageview_wind_speed.setBackgroundResource(R.drawable.button_aircondition_windspeed_auto_learned);
                } else {
                    imageview_wind_speed.setBackgroundResource(R.drawable.button_aircondition_windspeed_auto_notlearn);
                }
                textview_wind_speed.setText("自动风速");
                break;
        }
        Log.i(TAG, "directionAuto=" + directionAuto + "directionHand" + directionHand);
        switch (directionAuto) {
            case 0:
                switch (directionHand) {
                    case 1:
                        if (key_winddirection_up) {
                            imageview_wind_direction.setBackgroundResource(R.drawable.button_aircondition_winddirection_up_learned);
                        } else {
                            imageview_wind_direction.setBackgroundResource(R.drawable.button_aircondition_winddirection_up_notlearn);
                        }
                        textview_wind_direction.setText("风向向上");
                        break;
                    case 2:
                        if (key_winddirection_middle) {
                            imageview_wind_direction.setBackgroundResource(R.drawable.button_aircondition_winddirection_middle_learned);
                        } else {
                            imageview_wind_direction.setBackgroundResource(R.drawable.button_aircondition_winddirection_middle_notlearn);
                        }
                        textview_wind_direction.setText("风向居中");
                        break;
                    case 3:
                        if (key_winddirection_down) {
                            imageview_wind_direction.setBackgroundResource(R.drawable.button_aircondition_winddirection_down_learned);
                        } else {
                            imageview_wind_direction.setBackgroundResource(R.drawable.button_aircondition_winddirection_down_notlearn);
                        }
                        textview_wind_direction.setText("风向向下");
                        break;
                }
                break;
            case 1:
                if (key_winddirection_auto) {
                    imageview_wind_direction.setBackgroundResource(R.drawable.button_aircondition_winddirection_auto_learned);
                } else {
                    imageview_wind_direction.setBackgroundResource(R.drawable.button_aircondition_winddirection_auto_notlearn);
                }
                textview_wind_direction.setText("自动风向");
                break;
        }
        if (key_tempature_reduce) {
            imageview_temperature_reduce.setBackgroundResource(R.drawable.button_temp_reduce_learned);
        } else {
            imageview_temperature_reduce.setBackgroundResource(R.drawable.button_temp_reduce_notlearn);
        }
        if (key_tempature_plus) {
            imageview_temperature_plus.setBackgroundResource(R.drawable.button_temp_plus_learned);
        } else {
            imageview_temperature_plus.setBackgroundResource(R.drawable.button_temp_plus_notlearn);
        }

    }

    private String data_key_power;
    private String data_key_mode_hot;
    private String data_key_mode_cold;
    private String data_key_mode_dehumit;
    private String data_key_mode_wind;
    private String data_key_mode_auto;
    private String data_key_windspeed_hight;
    private String data_key_windspeed_middle;
    private String data_key_windspeed_low;
    private String data_key_windspeed_auto;
    private String data_key_winddirection_up;
    private String data_key_winddirection_middle;
    private String data_key_winddirection_down;
    private String data_key_winddirection_auto;


    private String data_key_tempature_hot_16;
    private String data_key_tempature_hot_17;
    private String data_key_tempature_hot_18;
    private String data_key_tempature_hot_19;
    private String data_key_tempature_hot_20;
    private String data_key_tempature_hot_21;
    private String data_key_tempature_hot_22;
    private String data_key_tempature_hot_23;
    private String data_key_tempature_hot_24;
    private String data_key_tempature_hot_25;
    private String data_key_tempature_hot_26;
    private String data_key_tempature_hot_27;
    private String data_key_tempature_hot_28;
    private String data_key_tempature_hot_29;
    private String data_key_tempature_hot_30;
    private String data_key_tempature_cold_16;
    private String data_key_tempature_cold_17;
    private String data_key_tempature_cold_18;
    private String data_key_tempature_cold_19;
    private String data_key_tempature_cold_20;
    private String data_key_tempature_cold_21;
    private String data_key_tempature_cold_22;
    private String data_key_tempature_cold_23;
    private String data_key_tempature_cold_24;
    private String data_key_tempature_cold_25;
    private String data_key_tempature_cold_26;
    private String data_key_tempature_cold_27;
    private String data_key_tempature_cold_28;
    private String data_key_tempature_cold_29;
    private String data_key_tempature_cold_30;

    private void initKeyCodeData() {
        if (!isStartFromExperience) {
            String currentDeviceUid = mRemoteControlManager.getmSelectRemoteControlDevice().getUid();
            AirconditionKeyCode mAirconditionKeyCode =
                    DataSupport.where("mAirconditionUid = ?", currentDeviceUid).findFirst(AirconditionKeyCode.class);
            if (mAirconditionKeyCode != null) {
                mRemoteControlManager.alertVirtualDevice(currentDeviceUid, null, mAirconditionKeyCode.getKeycode(), null);
                Log.i(TAG, "mAirconditionKeyCode=" + mAirconditionKeyCode.toString());
                group = mAirconditionKeyCode.getGroupData();
                code = mAirconditionKeyCode.getKeycode();
                //手动学习的按键
                data_key_power = mAirconditionKeyCode.getKey_power();
                data_key_mode_hot = mAirconditionKeyCode.getKey_mode_hot();
                data_key_mode_cold = mAirconditionKeyCode.getKey_mode_cold();
                data_key_mode_dehumit = mAirconditionKeyCode.getKey_mode_dehumit();
                data_key_mode_wind = mAirconditionKeyCode.getKey_mode_wind();
                data_key_mode_auto = mAirconditionKeyCode.getKey_mode_auto();
                data_key_windspeed_hight = mAirconditionKeyCode.getKey_windspeed_hight();
                data_key_windspeed_middle = mAirconditionKeyCode.getKey_windspeed_middle();
                data_key_windspeed_low = mAirconditionKeyCode.getKey_windspeed_low();
                data_key_windspeed_auto = mAirconditionKeyCode.getKey_windspeed_auto();
                data_key_winddirection_up = mAirconditionKeyCode.getKey_winddirection_up();
                data_key_winddirection_middle = mAirconditionKeyCode.getKey_winddirection_middle();
                data_key_winddirection_down = mAirconditionKeyCode.getKey_winddirection_down();
                data_key_winddirection_auto = mAirconditionKeyCode.getKey_winddirection_auto();

                data_key_tempature_hot_16 = mAirconditionKeyCode.getKey_tempature_hot_16();
                data_key_tempature_hot_17 = mAirconditionKeyCode.getKey_tempature_hot_17();
                data_key_tempature_hot_18 = mAirconditionKeyCode.getKey_tempature_hot_18();
                data_key_tempature_hot_19 = mAirconditionKeyCode.getKey_tempature_hot_19();
                data_key_tempature_hot_20 = mAirconditionKeyCode.getKey_tempature_hot_20();
                data_key_tempature_hot_21 = mAirconditionKeyCode.getKey_tempature_hot_21();
                data_key_tempature_hot_22 = mAirconditionKeyCode.getKey_tempature_hot_22();
                data_key_tempature_hot_23 = mAirconditionKeyCode.getKey_tempature_hot_23();
                data_key_tempature_hot_24 = mAirconditionKeyCode.getKey_tempature_hot_24();
                data_key_tempature_hot_25 = mAirconditionKeyCode.getKey_tempature_hot_25();
                data_key_tempature_hot_26 = mAirconditionKeyCode.getKey_tempature_hot_26();
                data_key_tempature_hot_27 = mAirconditionKeyCode.getKey_tempature_hot_27();
                data_key_tempature_hot_28 = mAirconditionKeyCode.getKey_tempature_hot_28();
                data_key_tempature_hot_29 = mAirconditionKeyCode.getKey_tempature_hot_29();
                data_key_tempature_hot_30 = mAirconditionKeyCode.getKey_tempature_hot_30();
                data_key_tempature_cold_16 = mAirconditionKeyCode.getKey_tempature_cold_16();
                data_key_tempature_cold_17 = mAirconditionKeyCode.getKey_tempature_cold_17();
                data_key_tempature_cold_18 = mAirconditionKeyCode.getKey_tempature_cold_18();
                data_key_tempature_cold_19 = mAirconditionKeyCode.getKey_tempature_cold_19();
                data_key_tempature_cold_20 = mAirconditionKeyCode.getKey_tempature_cold_20();
                data_key_tempature_cold_21 = mAirconditionKeyCode.getKey_tempature_cold_21();
                data_key_tempature_cold_22 = mAirconditionKeyCode.getKey_tempature_cold_22();
                data_key_tempature_cold_23 = mAirconditionKeyCode.getKey_tempature_cold_23();
                data_key_tempature_cold_24 = mAirconditionKeyCode.getKey_tempature_cold_24();
                data_key_tempature_cold_25 = mAirconditionKeyCode.getKey_tempature_cold_25();
                data_key_tempature_cold_26 = mAirconditionKeyCode.getKey_tempature_cold_26();
                data_key_tempature_cold_27 = mAirconditionKeyCode.getKey_tempature_cold_27();
                data_key_tempature_cold_28 = mAirconditionKeyCode.getKey_tempature_cold_28();
                data_key_tempature_cold_29 = mAirconditionKeyCode.getKey_tempature_cold_29();
                data_key_tempature_cold_30 = mAirconditionKeyCode.getKey_tempature_cold_30();
            }
        }
    }

    private AirconditionInitKeyValue mAirconditionInitKeyValue;

    private void initDatas() {
        mAirconditionInitKeyValue = DataSupport.findFirst(AirconditionInitKeyValue.class);
        if (mAirconditionInitKeyValue == null) {
            mAirconditionInitKeyValue = new AirconditionInitKeyValue();
            //设置默认值
            mAirconditionInitKeyValue.setTempature(0x19);
            mAirconditionInitKeyValue.setWind(0x01);
            mAirconditionInitKeyValue.setDirectionHand(0x02);
            mAirconditionInitKeyValue.setDirectionAuto(0x01);
            mAirconditionInitKeyValue.setMode(0x01);
            mAirconditionInitKeyValue.save();
        }
        Log.i(TAG, "mAirconditionInitKeyValue!=null" + (mAirconditionInitKeyValue != null));
        func[0] = (byte) mAirconditionInitKeyValue.getTempature();
        func[1] = (byte) mAirconditionInitKeyValue.getDirectionHand();
        func[2] = (byte) mAirconditionInitKeyValue.getDirectionAuto();
        func[3] = (byte) mAirconditionInitKeyValue.getKeyPower();
        // func[4] =  mAirconditionInitKeyValue.get;
        func[5] = (byte) mAirconditionInitKeyValue.getWind();
        func[6] = (byte) mAirconditionInitKeyValue.getMode();
        mRemoteControlManager = RemoteControlManager.getInstance();
        mRemoteControlManager.InitRemoteControlManager(this);
        mKeynotlearnDialog = new KeynotlearnDialog(this);

        menu_dialog = new RemoteControlMenuDialog(this, RemoteControlMenuDialog.TYPE_AIRCONDITION);
        menu_dialog.setmLearnHandClickListener(new RemoteControlMenuDialog.onLearnHandClickListener() {
            @Override
            public void onLearnHandBtnClicked() {
                mRemoteControlManager.setCurrentLearnByHandTypeName(DeviceTypeConstant.TYPE.TYPE_AIR_REMOTECONTROL);
                isLearnByHand = true;
                layout_title.setEditTextVisiable(true);
                layout_title.setEditImageVisiable(false);
                textview_tips.setVisibility(View.VISIBLE);
            }
        });
        layout_title.setReturnClickListener(new TitleLayout.ReturnImageClickListener() {
            @Override
            public void onBackPressed() {
                AirRemoteControlMianActivity.this.onBackPressed();
            }
        });
        layout_title.setEditTextClickListener(new TitleLayout.EditTextClickListener() {
            @Override
            public void onEditTextPressed() {
                textview_tips.setVisibility(View.GONE);
                layout_title.setEditTextVisiable(false);
                layout_title.setEditImageVisiable(true);
                isLearnByHand = false;
            }
        });
        layout_title.setEditImageClickListener(new TitleLayout.EditImageClickListener() {
            @Override
            public void onEditImagePressed() {
                menu_dialog.show();
            }
        });

    }

    private boolean isLearnByHand;

    private void initKeylearnStatus() {
        if (isStartFromExperience) {
            key_tempature_reduce = true;
            key_tempature_plus = true;
            key_power = true;
            key_mode_hot = true;
            key_mode_cold = true;
            key_mode_dehumit = true;
            key_mode_wind = true;
            key_mode_auto = true;
            key_windspeed_hight = true;
            key_windspeed_middle = true;
            key_windspeed_low = true;
            key_windspeed_auto = true;
            key_winddirection_up = true;
            key_winddirection_middle = true;
            key_winddirection_down = true;
            key_winddirection_auto = true;
        } else {
            String currentDeviceUid = mRemoteControlManager.getmSelectRemoteControlDevice().getUid();
            AirconditionKeyLearnStatu mAirconditionKeyLearnStatu = DataSupport.where("mAirconditionUid = ?", currentDeviceUid).findFirst(AirconditionKeyLearnStatu.class);
            if (mAirconditionKeyLearnStatu != null) {
                key_tempature_reduce = mAirconditionKeyLearnStatu.isKey_tempature_reduce();
                key_tempature_plus = mAirconditionKeyLearnStatu.isKey_tempature_plus();
                key_power = mAirconditionKeyLearnStatu.isKey_power();
                key_mode_hot = mAirconditionKeyLearnStatu.isKey_mode_hot();
                key_mode_cold = mAirconditionKeyLearnStatu.isKey_mode_cold();
                key_mode_dehumit = mAirconditionKeyLearnStatu.isKey_mode_dehumit();
                key_mode_wind = mAirconditionKeyLearnStatu.isKey_mode_wind();
                key_mode_auto = mAirconditionKeyLearnStatu.isKey_mode_auto();
                key_windspeed_hight = mAirconditionKeyLearnStatu.isKey_windspeed_hight();
                key_windspeed_middle = mAirconditionKeyLearnStatu.isKey_windspeed_middle();
                key_windspeed_low = mAirconditionKeyLearnStatu.isKey_windspeed_low();
                key_windspeed_auto = mAirconditionKeyLearnStatu.isKey_windspeed_auto();
                key_winddirection_up = mAirconditionKeyLearnStatu.isKey_winddirection_up();
                key_winddirection_middle = mAirconditionKeyLearnStatu.isKey_winddirection_middle();
                key_winddirection_down = mAirconditionKeyLearnStatu.isKey_winddirection_down();
                key_winddirection_auto = mAirconditionKeyLearnStatu.isKey_winddirection_auto();
            } else {
                key_tempature_reduce = false;
                key_tempature_plus = false;
                key_power = false;
                key_mode_hot = false;
                key_mode_cold = false;
                key_mode_dehumit = false;
                key_mode_wind = false;
                key_mode_auto = false;
                key_windspeed_hight = false;
                key_windspeed_middle = false;
                key_windspeed_low = false;
                key_windspeed_auto = false;
                key_winddirection_up = false;
                key_winddirection_middle = false;
                key_winddirection_down = false;
                key_winddirection_auto = false;
            }
        }
    }

    private void initEvents() {
        imageview_model.setOnClickListener(this);
        imageview_wind_speed.setOnClickListener(this);
        imageview_wind_direction.setOnClickListener(this);
        imageview_power.setOnClickListener(this);
        imageview_temperature_reduce.setOnClickListener(this);
        imageview_temperature_plus.setOnClickListener(this);

    }

    private void initViews() {
        textview_model = findViewById(R.id.textview_model);
        textview_temperature = findViewById(R.id.textview_temperature);
        textview_tips = findViewById(R.id.textview_tips);
        textview_wind_direction = findViewById(R.id.textview_wind_center);
        textview_wind_speed = findViewById(R.id.textview_wind_speed);
        imageview_model = findViewById(R.id.imageview_auto_model);
        imageview_wind_speed = findViewById(R.id.imageview_auto_wind_speed);
        imageview_wind_direction = findViewById(R.id.imageview_wind_center);
        imageview_power = findViewById(R.id.imageview_power);
        imageview_temperature_reduce = findViewById(R.id.imageview_temperature_reduce);
        imageview_temperature_plus = findViewById(R.id.imageview_temperature_plus);
        layout_top_content = findViewById(R.id.layout_top_content);
        layout_title = findViewById(R.id.layout_title);
        textview_tempature_unit = findViewById(R.id.textview_tempature_unit);
        textview_power = findViewById(R.id.textview_power);
        gatwaygif = findViewById(R.id.gatwaygif);
    }

    private byte[] data;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imageview_temperature_reduce:
                if (isLearnByHand) {
                    switch (currentMode) {
                        case 2://制冷
                            switch (tempature) {
                                case 16:
                                    mRemoteControlManager.setCurrentLearnByHandKeyName
                                            (AirKeyNameConstant.KEYNAME.KEYNAME_TEMPTURE_COLD_16);
                                    break;
                                case 17:
                                    mRemoteControlManager.setCurrentLearnByHandKeyName
                                            (AirKeyNameConstant.KEYNAME.KEYNAME_TEMPTURE_COLD_17);
                                    break;
                                case 18:
                                    mRemoteControlManager.setCurrentLearnByHandKeyName
                                            (AirKeyNameConstant.KEYNAME.KEYNAME_TEMPTURE_COLD_18);
                                    break;
                                case 19:
                                    mRemoteControlManager.setCurrentLearnByHandKeyName
                                            (AirKeyNameConstant.KEYNAME.KEYNAME_TEMPTURE_COLD_19);
                                    break;
                                case 20:
                                    mRemoteControlManager.setCurrentLearnByHandKeyName
                                            (AirKeyNameConstant.KEYNAME.KEYNAME_TEMPTURE_COLD_20);
                                    break;
                                case 21:
                                    mRemoteControlManager.setCurrentLearnByHandKeyName
                                            (AirKeyNameConstant.KEYNAME.KEYNAME_TEMPTURE_COLD_21);
                                    break;
                                case 22:
                                    mRemoteControlManager.setCurrentLearnByHandKeyName
                                            (AirKeyNameConstant.KEYNAME.KEYNAME_TEMPTURE_COLD_22);
                                    break;
                                case 23:
                                    mRemoteControlManager.setCurrentLearnByHandKeyName
                                            (AirKeyNameConstant.KEYNAME.KEYNAME_TEMPTURE_COLD_23);
                                    break;
                                case 24:
                                    mRemoteControlManager.setCurrentLearnByHandKeyName
                                            (AirKeyNameConstant.KEYNAME.KEYNAME_TEMPTURE_COLD_24);
                                    break;
                                case 25:
                                    mRemoteControlManager.setCurrentLearnByHandKeyName
                                            (AirKeyNameConstant.KEYNAME.KEYNAME_TEMPTURE_COLD_25);
                                    break;
                                case 26:
                                    mRemoteControlManager.setCurrentLearnByHandKeyName
                                            (AirKeyNameConstant.KEYNAME.KEYNAME_TEMPTURE_COLD_26);
                                    break;
                                case 27:
                                    mRemoteControlManager.setCurrentLearnByHandKeyName
                                            (AirKeyNameConstant.KEYNAME.KEYNAME_TEMPTURE_COLD_27);
                                    break;
                                case 28:
                                    mRemoteControlManager.setCurrentLearnByHandKeyName
                                            (AirKeyNameConstant.KEYNAME.KEYNAME_TEMPTURE_COLD_28);
                                    break;
                                case 29:
                                    mRemoteControlManager.setCurrentLearnByHandKeyName
                                            (AirKeyNameConstant.KEYNAME.KEYNAME_TEMPTURE_COLD_29);
                                    break;
                                case 30:
                                    mRemoteControlManager.setCurrentLearnByHandKeyName
                                            (AirKeyNameConstant.KEYNAME.KEYNAME_TEMPTURE_COLD_30);
                                    break;
                            }
                            break;
                        case 5://制热
                            switch (tempature) {
                                case 16:
                                    mRemoteControlManager.setCurrentLearnByHandKeyName
                                            (AirKeyNameConstant.KEYNAME.KEYNAME_TEMPTURE_HOT_16);
                                    break;
                                case 17:
                                    mRemoteControlManager.setCurrentLearnByHandKeyName
                                            (AirKeyNameConstant.KEYNAME.KEYNAME_TEMPTURE_HOT_17);
                                    break;
                                case 18:
                                    mRemoteControlManager.setCurrentLearnByHandKeyName
                                            (AirKeyNameConstant.KEYNAME.KEYNAME_TEMPTURE_HOT_18);
                                    break;
                                case 19:
                                    mRemoteControlManager.setCurrentLearnByHandKeyName
                                            (AirKeyNameConstant.KEYNAME.KEYNAME_TEMPTURE_HOT_19);
                                    break;
                                case 20:
                                    mRemoteControlManager.setCurrentLearnByHandKeyName
                                            (AirKeyNameConstant.KEYNAME.KEYNAME_TEMPTURE_HOT_20);
                                    break;
                                case 21:
                                    mRemoteControlManager.setCurrentLearnByHandKeyName
                                            (AirKeyNameConstant.KEYNAME.KEYNAME_TEMPTURE_HOT_21);
                                    break;
                                case 22:
                                    mRemoteControlManager.setCurrentLearnByHandKeyName
                                            (AirKeyNameConstant.KEYNAME.KEYNAME_TEMPTURE_HOT_22);
                                    break;
                                case 23:
                                    mRemoteControlManager.setCurrentLearnByHandKeyName
                                            (AirKeyNameConstant.KEYNAME.KEYNAME_TEMPTURE_HOT_23);
                                    break;
                                case 24:
                                    mRemoteControlManager.setCurrentLearnByHandKeyName
                                            (AirKeyNameConstant.KEYNAME.KEYNAME_TEMPTURE_HOT_24);
                                    break;
                                case 25:
                                    mRemoteControlManager.setCurrentLearnByHandKeyName
                                            (AirKeyNameConstant.KEYNAME.KEYNAME_TEMPTURE_HOT_25);
                                    break;
                                case 26:
                                    mRemoteControlManager.setCurrentLearnByHandKeyName
                                            (AirKeyNameConstant.KEYNAME.KEYNAME_TEMPTURE_HOT_26);
                                    break;
                                case 27:
                                    mRemoteControlManager.setCurrentLearnByHandKeyName
                                            (AirKeyNameConstant.KEYNAME.KEYNAME_TEMPTURE_HOT_27);
                                    break;
                                case 28:
                                    mRemoteControlManager.setCurrentLearnByHandKeyName
                                            (AirKeyNameConstant.KEYNAME.KEYNAME_TEMPTURE_HOT_28);
                                    break;
                                case 29:
                                    mRemoteControlManager.setCurrentLearnByHandKeyName
                                            (AirKeyNameConstant.KEYNAME.KEYNAME_TEMPTURE_HOT_29);
                                    break;
                                case 30:
                                    mRemoteControlManager.setCurrentLearnByHandKeyName
                                            (AirKeyNameConstant.KEYNAME.KEYNAME_TEMPTURE_HOT_30);
                                    break;
                            }
                            break;
                    }

                    startActivity(new Intent(this, LearnByHandActivity.class));
                } else {
                    if (key_tempature_reduce) {
                        if (!isStartFromExperience) {
                            if (code == null) {
                                return;
                            }
                            Log.i(TAG, "mAirconditionInitKeyValue!=null" + (mAirconditionInitKeyValue != null));
                            if (tempature > 16) {
                                func[0] = (byte) (tempature--);
                                func[5] = (byte) (0x07);
                                textview_temperature.setText("" + tempature);
                                mAirconditionInitKeyValue.setTempature(tempature);
                                mAirconditionInitKeyValue.save();
                            }
                            data = packData();
                            switch (tempature) {
                                case 16:
                                    switch (currentMode) {
                                        case 2:
                                            if (data_key_tempature_cold_16 != null) {
                                                mRemoteControlManager.sendData(data_key_tempature_cold_16);
                                            } else {

                                                mRemoteControlManager.sendData(DataExchange.dbBytesToString(data));
                                            }
                                            break;
                                        case 5:
                                        default:
                                            if (data_key_tempature_hot_16 != null) {
                                                mRemoteControlManager.sendData(data_key_tempature_hot_16);
                                            } else {

                                                mRemoteControlManager.sendData(DataExchange.dbBytesToString(data));
                                            }
                                            break;
                                    }
                                    break;
                                case 17:
                                    switch (currentMode) {
                                        case 2:
                                            if (data_key_tempature_cold_17 != null) {
                                                mRemoteControlManager.sendData(data_key_tempature_cold_17);
                                            } else {

                                                mRemoteControlManager.sendData(DataExchange.dbBytesToString(data));
                                            }
                                            break;
                                        case 5:
                                        default:
                                            if (data_key_tempature_hot_17 != null) {
                                                mRemoteControlManager.sendData(data_key_tempature_hot_17);
                                            } else {

                                                mRemoteControlManager.sendData(DataExchange.dbBytesToString(data));
                                            }
                                            break;
                                    }
                                    break;
                                case 18:
                                    switch (currentMode) {
                                        case 2:
                                            if (data_key_tempature_cold_18 != null) {
                                                mRemoteControlManager.sendData(data_key_tempature_cold_18);
                                            } else {

                                                mRemoteControlManager.sendData(DataExchange.dbBytesToString(data));
                                            }
                                            break;
                                        case 5:
                                        default:
                                            if (data_key_tempature_hot_18 != null) {
                                                mRemoteControlManager.sendData(data_key_tempature_hot_18);
                                            } else {

                                                mRemoteControlManager.sendData(DataExchange.dbBytesToString(data));
                                            }
                                            break;
                                    }
                                    break;
                                case 19:
                                    switch (currentMode) {
                                        case 2:
                                            if (data_key_tempature_cold_19 != null) {
                                                mRemoteControlManager.sendData(data_key_tempature_cold_19);
                                            } else {

                                                mRemoteControlManager.sendData(DataExchange.dbBytesToString(data));
                                            }
                                            break;
                                        case 5:
                                        default:
                                            if (data_key_tempature_hot_19 != null) {
                                                mRemoteControlManager.sendData(data_key_tempature_hot_19);
                                            } else {

                                                mRemoteControlManager.sendData(DataExchange.dbBytesToString(data));
                                            }
                                            break;
                                    }
                                    break;
                                case 20:
                                    switch (currentMode) {
                                        case 2:
                                            if (data_key_tempature_cold_20 != null) {
                                                mRemoteControlManager.sendData(data_key_tempature_cold_20);
                                            } else {

                                                mRemoteControlManager.sendData(DataExchange.dbBytesToString(data));
                                            }
                                            break;
                                        case 5:
                                        default:
                                            if (data_key_tempature_hot_20 != null) {
                                                mRemoteControlManager.sendData(data_key_tempature_hot_20);
                                            } else {

                                                mRemoteControlManager.sendData(DataExchange.dbBytesToString(data));
                                            }
                                            break;
                                    }
                                    break;
                                case 21:
                                    switch (currentMode) {
                                        case 2:
                                            if (data_key_tempature_cold_21 != null) {
                                                mRemoteControlManager.sendData(data_key_tempature_cold_21);
                                            } else {

                                                mRemoteControlManager.sendData(DataExchange.dbBytesToString(data));
                                            }
                                            break;
                                        case 5:
                                        default:
                                            if (data_key_tempature_hot_21 != null) {
                                                mRemoteControlManager.sendData(data_key_tempature_hot_21);
                                            } else {

                                                mRemoteControlManager.sendData(DataExchange.dbBytesToString(data));
                                            }
                                            break;
                                    }
                                    break;
                                case 22:
                                    switch (currentMode) {
                                        case 2:
                                            if (data_key_tempature_cold_22 != null) {
                                                mRemoteControlManager.sendData(data_key_tempature_cold_22);
                                            } else {

                                                mRemoteControlManager.sendData(DataExchange.dbBytesToString(data));
                                            }
                                            break;
                                        case 5:
                                        default:
                                            if (data_key_tempature_hot_22 != null) {
                                                mRemoteControlManager.sendData(data_key_tempature_hot_22);
                                            } else {

                                                mRemoteControlManager.sendData(DataExchange.dbBytesToString(data));
                                            }
                                            break;
                                    }
                                    break;
                                case 23:
                                    switch (currentMode) {
                                        case 2:
                                            if (data_key_tempature_cold_23 != null) {
                                                mRemoteControlManager.sendData(data_key_tempature_cold_23);
                                            } else {

                                                mRemoteControlManager.sendData(DataExchange.dbBytesToString(data));
                                            }
                                            break;
                                        case 5:
                                        default:
                                            if (data_key_tempature_hot_23 != null) {
                                                mRemoteControlManager.sendData(data_key_tempature_hot_23);
                                            } else {

                                                mRemoteControlManager.sendData(DataExchange.dbBytesToString(data));
                                            }
                                            break;
                                    }
                                    break;
                                case 24:
                                    switch (currentMode) {
                                        case 2:
                                            if (data_key_tempature_cold_24 != null) {
                                                mRemoteControlManager.sendData(data_key_tempature_cold_24);
                                            } else {

                                                mRemoteControlManager.sendData(DataExchange.dbBytesToString(data));
                                            }
                                            break;
                                        case 5:
                                        default:
                                            if (data_key_tempature_hot_24 != null) {
                                                mRemoteControlManager.sendData(data_key_tempature_hot_24);
                                            } else {

                                                mRemoteControlManager.sendData(DataExchange.dbBytesToString(data));
                                            }
                                            break;
                                    }
                                    break;
                                case 25:
                                    switch (currentMode) {
                                        case 2:
                                            if (data_key_tempature_cold_25 != null) {
                                                mRemoteControlManager.sendData(data_key_tempature_cold_25);
                                            } else {

                                                mRemoteControlManager.sendData(DataExchange.dbBytesToString(data));
                                            }
                                            break;
                                        case 5:
                                        default:
                                            if (data_key_tempature_hot_25 != null) {
                                                mRemoteControlManager.sendData(data_key_tempature_hot_25);
                                            } else {

                                                mRemoteControlManager.sendData(DataExchange.dbBytesToString(data));
                                            }
                                            break;
                                    }
                                    break;
                                case 26:
                                    switch (currentMode) {
                                        case 2:
                                            if (data_key_tempature_cold_26 != null) {
                                                mRemoteControlManager.sendData(data_key_tempature_cold_26);
                                            } else {

                                                mRemoteControlManager.sendData(DataExchange.dbBytesToString(data));
                                            }
                                            break;
                                        case 5:
                                        default:
                                            if (data_key_tempature_hot_26 != null) {
                                                mRemoteControlManager.sendData(data_key_tempature_hot_26);
                                            } else {

                                                mRemoteControlManager.sendData(DataExchange.dbBytesToString(data));
                                            }
                                            break;
                                    }
                                    break;
                                case 27:
                                    switch (currentMode) {
                                        case 2:
                                            if (data_key_tempature_cold_27 != null) {
                                                mRemoteControlManager.sendData(data_key_tempature_cold_27);
                                            } else {

                                                mRemoteControlManager.sendData(DataExchange.dbBytesToString(data));
                                            }
                                            break;
                                        case 5:
                                        default:
                                            if (data_key_tempature_hot_27 != null) {
                                                mRemoteControlManager.sendData(data_key_tempature_hot_27);
                                            } else {

                                                mRemoteControlManager.sendData(DataExchange.dbBytesToString(data));
                                            }
                                            break;
                                    }
                                    break;
                                case 28:
                                    switch (currentMode) {
                                        case 2:
                                            if (data_key_tempature_cold_28 != null) {
                                                mRemoteControlManager.sendData(data_key_tempature_cold_28);
                                            } else {

                                                mRemoteControlManager.sendData(DataExchange.dbBytesToString(data));
                                            }
                                            break;
                                        case 5:
                                        default:
                                            if (data_key_tempature_hot_28 != null) {
                                                mRemoteControlManager.sendData(data_key_tempature_hot_28);
                                            } else {

                                                mRemoteControlManager.sendData(DataExchange.dbBytesToString(data));
                                            }
                                            break;
                                    }
                                    break;
                                case 29:
                                    switch (currentMode) {
                                        case 2:
                                            if (data_key_tempature_cold_29 != null) {
                                                mRemoteControlManager.sendData(data_key_tempature_cold_29);
                                            } else {

                                                mRemoteControlManager.sendData(DataExchange.dbBytesToString(data));
                                            }
                                            break;
                                        case 5:
                                        default:
                                            if (data_key_tempature_hot_29 != null) {
                                                mRemoteControlManager.sendData(data_key_tempature_hot_29);
                                            } else {

                                                mRemoteControlManager.sendData(DataExchange.dbBytesToString(data));
                                            }
                                            break;
                                    }
                                    break;
                                case 30:
                                    switch (currentMode) {
                                        case 2:
                                            if (data_key_tempature_cold_30 != null) {
                                                mRemoteControlManager.sendData(data_key_tempature_cold_30);
                                            } else {

                                                mRemoteControlManager.sendData(DataExchange.dbBytesToString(data));
                                            }
                                            break;
                                        case 5:
                                        default:
                                            if (data_key_tempature_hot_30 != null) {
                                                mRemoteControlManager.sendData(data_key_tempature_hot_30);
                                            } else {
                                                mRemoteControlManager.sendData(DataExchange.dbBytesToString(data));
                                            }
                                            break;
                                    }
                                    break;
                            }
                        }else{
                            if (tempature > 16) {
                                tempature--;
                                textview_temperature.setText("" + tempature);
                            }
                        }
                    } else {
                        mKeynotlearnDialog.show();
                    }
                }
                break;
            case R.id.imageview_temperature_plus:
                if (isLearnByHand) {
                    //mRemoteControlManager.setCurrentLearnByHandKeyName(AirKeyNameConstant.KEYNAME.KEYNAME_TEMPTURE_ADD);
                    startActivity(new Intent(this, LearnByHandActivity.class));
                } else {
                    if (key_tempature_plus) {
                        if (!isStartFromExperience) {
                            if (code == null) {
                                return;
                            }
                            Log.i(TAG, "mAirconditionInitKeyValue!=null" + (mAirconditionInitKeyValue != null) + "tempature" + tempature);
                            if (tempature < 30) {
                                func[0] = (byte) (tempature++);
                                func[5] = (byte) (0x06);
                                textview_temperature.setText("" + tempature);
                                mAirconditionInitKeyValue.setTempature(tempature);
                                mAirconditionInitKeyValue.save();
                            }
                            data = packData();
                            mRemoteControlManager.sendData(DataExchange.dbBytesToString(data));

                        }else{
                            if (tempature < 30) {
                                tempature++;
                            }
                            textview_temperature.setText("" + tempature);
                        }
                    } else {
                        mKeynotlearnDialog.show();
                    }
                }
                break;

            case R.id.imageview_auto_model:
                if (isLearnByHand) {
                    startActivity(new Intent(this, LearnByHandActivity.class));
                } else {
                    if (key_mode_hot || key_mode_cold || key_mode_dehumit || key_mode_wind || key_mode_auto) {
                        new ActionSheetDialog(AirRemoteControlMianActivity.this)
                                .builder()
                                .setCancelable(true)
                                .setCanceledOnTouchOutside(true)
                                .addSheetItem("制热模式", ActionSheetDialog.SheetItemColor.GRAY,
                                        new ActionSheetDialog.OnSheetItemClickListener() {
                                            @Override
                                            public void onClick(int which) {
                                                textview_model.setText("制热模式");
                                                if (isLearnByHand) {
                                                    mRemoteControlManager.setCurrentLearnByHandKeyName(AirKeyNameConstant.KEYNAME.KEYNAME_MODE_HOT);
                                                    startActivity(new Intent(AirRemoteControlMianActivity.this, LearnByHandActivity.class));
                                                } else {
                                                    if (key_mode_hot) {
                                                        imageview_model.setBackgroundResource(R.drawable.button_aircondition_mode_hot_learned);
                                                        if (!isStartFromExperience) {
                                                            if (code == null) {
                                                                return;
                                                            }
                                                            func[5] = (byte) 0x02;
                                                            func[6] = (byte) 0x05;
                                                            currentMode = 0x05;
                                                            mAirconditionInitKeyValue.setMode(currentMode);
                                                            mAirconditionInitKeyValue.save();
                                                            data = packData();
                                                            if (data_key_mode_hot != null) {
                                                                mRemoteControlManager.sendData(data_key_mode_hot);
                                                            } else {
                                                                mRemoteControlManager.sendData(DataExchange.dbBytesToString(data));
                                                            }
                                                        }
                                                    } else {
                                                        imageview_model.setBackgroundResource(R.drawable.button_aircondition_mode_hot_notlearn);
                                                        mKeynotlearnDialog.show();
                                                    }
                                                }
                                            }
                                        })
                                .addSheetItem("制冷模式", ActionSheetDialog.SheetItemColor.GRAY,
                                        new ActionSheetDialog.OnSheetItemClickListener() {
                                            @Override
                                            public void onClick(int which) {
                                                textview_model.setText("制冷模式");
                                                if (isLearnByHand) {
                                                    mRemoteControlManager.setCurrentLearnByHandKeyName(AirKeyNameConstant.KEYNAME.KEYNAME_MODE_COLD);
                                                    startActivity(new Intent(AirRemoteControlMianActivity.this, LearnByHandActivity.class));
                                                } else {
                                                    if (key_mode_cold) {
                                                        imageview_model.setBackgroundResource(R.drawable.button_aircondition_mode_cold_learned);
                                                        if (!isStartFromExperience) {
                                                            if (code == null) {
                                                                return;
                                                            }
                                                            func[5] = (byte) 0x02;
                                                            func[6] = (byte) 0x02;
                                                            currentMode = 0x02;
                                                            mAirconditionInitKeyValue.setMode(currentMode);
                                                            mAirconditionInitKeyValue.save();
                                                            data = packData();
                                                            if (data_key_mode_cold != null) {
                                                                mRemoteControlManager.sendData(data_key_mode_cold);
                                                            } else {
                                                                mRemoteControlManager.sendData(DataExchange.dbBytesToString(data));
                                                            }
                                                        }

                                                    } else {
                                                        imageview_model.setBackgroundResource(R.drawable.button_aircondition_mode_cold_notlearn);
                                                        mKeynotlearnDialog.show();
                                                    }
                                                }
                                            }
                                        })
                                .addSheetItem("除湿模式", ActionSheetDialog.SheetItemColor.GRAY,
                                        new ActionSheetDialog.OnSheetItemClickListener() {
                                            @Override
                                            public void onClick(int which) {
                                                textview_model.setText("除湿模式");
                                                if (isLearnByHand) {
                                                    mRemoteControlManager.setCurrentLearnByHandKeyName(AirKeyNameConstant.KEYNAME.KEYNAME_MODE_DEHUMIT);
                                                    startActivity(new Intent(AirRemoteControlMianActivity.this, LearnByHandActivity.class));
                                                } else {
                                                    if (key_mode_dehumit) {
                                                        imageview_model.setBackgroundResource(R.drawable.button_aircondition_mode_dehumid_learned);
                                                        if (!isStartFromExperience) {

                                                            if (code == null) {
                                                                return;
                                                            }
                                                            func[5] = (byte) 0x02;
                                                            func[6] = (byte) 0x03;
                                                            currentMode = 0x03;
                                                            mAirconditionInitKeyValue.setMode(currentMode);
                                                            mAirconditionInitKeyValue.save();

                                                            data = packData();
                                                            if (data_key_mode_dehumit != null) {
                                                                mRemoteControlManager.sendData(data_key_mode_dehumit);
                                                            } else {
                                                                mRemoteControlManager.sendData(DataExchange.dbBytesToString(data));
                                                            }
                                                        }
                                                    } else {
                                                        imageview_model.setBackgroundResource(R.drawable.button_aircondition_mode_dehumid_notlearn);
                                                        mKeynotlearnDialog.show();
                                                    }
                                                }
                                            }
                                        })
                                .addSheetItem("送风模式", ActionSheetDialog.SheetItemColor.GRAY,
                                        new ActionSheetDialog.OnSheetItemClickListener() {
                                            @Override
                                            public void onClick(int which) {
                                                textview_model.setText("送风模式");
                                                if (isLearnByHand) {
                                                    mRemoteControlManager.setCurrentLearnByHandKeyName(AirKeyNameConstant.KEYNAME.KEYNAME_MODE_WIND);
                                                    startActivity(new Intent(AirRemoteControlMianActivity.this, LearnByHandActivity.class));
                                                } else {
                                                    if (key_mode_wind) {
                                                        imageview_model.setBackgroundResource(R.drawable.button_aircondition_mode_wind_learned);
                                                        if (!isStartFromExperience) {

                                                            if (code == null) {
                                                                return;
                                                            }
                                                            func[5] = (byte) 0x02;
                                                            func[6] = (byte) 0x04;
                                                            currentMode = 0x04;
                                                            mAirconditionInitKeyValue.setMode(currentMode);
                                                            mAirconditionInitKeyValue.save();

                                                            data = packData();
                                                            if (data_key_mode_wind != null) {
                                                                mRemoteControlManager.sendData(data_key_mode_wind);
                                                            } else {
                                                                mRemoteControlManager.sendData(DataExchange.dbBytesToString(data));
                                                            }
                                                        }
                                                    } else {
                                                        imageview_model.setBackgroundResource(R.drawable.button_aircondition_mode_wind_notlearn);
                                                        mKeynotlearnDialog.show();
                                                    }
                                                }
                                            }
                                        })
                                .addSheetItem("自动模式", ActionSheetDialog.SheetItemColor.GRAY,
                                        new ActionSheetDialog.OnSheetItemClickListener() {
                                            @Override
                                            public void onClick(int which) {
                                                textview_model.setText("自动模式");
                                                if (isLearnByHand) {
                                                    mRemoteControlManager.setCurrentLearnByHandKeyName(AirKeyNameConstant.KEYNAME.KEYNAME_MODE_AUTO);
                                                    startActivity(new Intent(AirRemoteControlMianActivity.this, LearnByHandActivity.class));
                                                } else {
                                                    if (key_mode_auto) {
                                                        imageview_model.setBackgroundResource(R.drawable.button_aircondition_mode_auto_learned);
                                                        if (!isStartFromExperience) {

                                                            if (code == null) {
                                                                return;
                                                            }
                                                            func[5] = (byte) 0x02;
                                                            func[6] = (byte) 0x01;
                                                            currentMode = 0x01;
                                                            mAirconditionInitKeyValue.setMode(currentMode);
                                                            mAirconditionInitKeyValue.save();

                                                            data = packData();
                                                            if (data_key_mode_auto != null) {
                                                                mRemoteControlManager.sendData(data_key_mode_auto);
                                                            } else {
                                                                mRemoteControlManager.sendData(DataExchange.dbBytesToString(data));
                                                            }
                                                        }
                                                    } else {
                                                        imageview_model.setBackgroundResource(R.drawable.button_aircondition_mode_auto_notlearn);
                                                        mKeynotlearnDialog.show();
                                                    }
                                                }
                                            }
                                        })
                                .show();
                    } else {
                        mKeynotlearnDialog.show();
                    }
                }
                break;
            case R.id.imageview_auto_wind_speed:
                if (isLearnByHand) {
                    startActivity(new Intent(this, LearnByHandActivity.class));
                } else {
                    if (key_windspeed_hight || key_windspeed_middle || key_windspeed_low || key_windspeed_auto) {
                        new ActionSheetDialog(AirRemoteControlMianActivity.this)
                                .builder()
                                .setCancelable(true)
                                .setCanceledOnTouchOutside(true)
                                .addSheetItem("高风", ActionSheetDialog.SheetItemColor.GRAY,
                                        new ActionSheetDialog.OnSheetItemClickListener() {
                                            @Override
                                            public void onClick(int which) {
                                                textview_wind_speed.setText("高风");
                                                if (isLearnByHand) {
                                                    mRemoteControlManager.setCurrentLearnByHandKeyName(AirKeyNameConstant.KEYNAME.KEYNAME_WIND_HIGH);
                                                    startActivity(new Intent(AirRemoteControlMianActivity.this, LearnByHandActivity.class));
                                                } else {
                                                    if (key_windspeed_hight) {
                                                        imageview_wind_speed.setBackgroundResource(R.drawable.button_aircondition_windspeed_hight_learned);
                                                        if (!isStartFromExperience) {

                                                            if (code == null) {
                                                                return;
                                                            }
                                                            func[5] = (byte) 0x02;
                                                            func[1] = (byte) 0x04;
                                                            wind = 0x04;
                                                            mAirconditionInitKeyValue.setWind(wind);
                                                            mAirconditionInitKeyValue.save();

                                                            data = packData();
                                                            if (data_key_windspeed_hight != null) {
                                                                mRemoteControlManager.sendData(data_key_windspeed_hight);
                                                            } else {
                                                                mRemoteControlManager.sendData(DataExchange.dbBytesToString(data));
                                                            }
                                                        }
                                                    } else {
                                                        imageview_wind_speed.setBackgroundResource(R.drawable.button_aircondition_windspeed_hight_notlearn);
                                                        mKeynotlearnDialog.show();
                                                    }
                                                }
                                            }
                                        })
                                .addSheetItem("中风", ActionSheetDialog.SheetItemColor.GRAY,
                                        new ActionSheetDialog.OnSheetItemClickListener() {
                                            @Override
                                            public void onClick(int which) {
                                                textview_wind_speed.setText("中风");
                                                if (isLearnByHand) {
                                                    mRemoteControlManager.setCurrentLearnByHandKeyName(AirKeyNameConstant.KEYNAME.KEYNAME_WIND_MIDDLE);
                                                    startActivity(new Intent(AirRemoteControlMianActivity.this, LearnByHandActivity.class));
                                                } else {
                                                    if (key_windspeed_middle) {
                                                        imageview_wind_speed.setBackgroundResource(R.drawable.button_aircondition_windspeed_middle_learned);
                                                        if (!isStartFromExperience) {

                                                            if (code == null) {
                                                                return;
                                                            }
                                                            func[5] = (byte) 0x02;
                                                            func[1] = (byte) 0x03;
                                                            wind = 0x03;
                                                            mAirconditionInitKeyValue.setWind(wind);
                                                            mAirconditionInitKeyValue.save();

                                                            data = packData();
                                                            if (data_key_windspeed_middle != null) {
                                                                mRemoteControlManager.sendData(data_key_windspeed_middle);
                                                            } else {
                                                                mRemoteControlManager.sendData(DataExchange.dbBytesToString(data));
                                                            }
                                                        }

                                                    } else {
                                                        imageview_wind_speed.setBackgroundResource(R.drawable.button_aircondition_windspeed_middle_notlearn);
                                                        mKeynotlearnDialog.show();
                                                    }
                                                }
                                            }
                                        })
                                .addSheetItem("低风", ActionSheetDialog.SheetItemColor.GRAY,
                                        new ActionSheetDialog.OnSheetItemClickListener() {
                                            @Override
                                            public void onClick(int which) {
                                                textview_wind_speed.setText("低风");
                                                if (isLearnByHand) {
                                                    mRemoteControlManager.setCurrentLearnByHandKeyName(AirKeyNameConstant.KEYNAME.KEYNAME_WIND_LOW);
                                                    startActivity(new Intent(AirRemoteControlMianActivity.this, LearnByHandActivity.class));
                                                } else {
                                                    if (key_windspeed_low) {
                                                        imageview_wind_speed.setBackgroundResource(R.drawable.button_aircondition_windspeed_low_learned);
                                                        if (!isStartFromExperience) {

                                                            if (code == null) {
                                                                return;
                                                            }
                                                            func[5] = (byte) 0x02;
                                                            func[1] = (byte) 0x02;
                                                            wind = 0x02;
                                                            mAirconditionInitKeyValue.setWind(wind);
                                                            mAirconditionInitKeyValue.save();

                                                            data = packData();
                                                            if (data_key_windspeed_low != null) {
                                                                mRemoteControlManager.sendData(data_key_windspeed_low);
                                                            } else {
                                                                mRemoteControlManager.sendData(DataExchange.dbBytesToString(data));
                                                            }
                                                        }

                                                    } else {
                                                        imageview_wind_speed.setBackgroundResource(R.drawable.button_aircondition_windspeed_low_notlearn);
                                                        mKeynotlearnDialog.show();
                                                    }
                                                }
                                            }
                                        })
                                .addSheetItem("自动风速", ActionSheetDialog.SheetItemColor.GRAY,
                                        new ActionSheetDialog.OnSheetItemClickListener() {
                                            @Override
                                            public void onClick(int which) {
                                                textview_wind_speed.setText("自动风速");
                                                if (isLearnByHand) {
                                                    mRemoteControlManager.setCurrentLearnByHandKeyName(AirKeyNameConstant.KEYNAME.KEYNAME_WIND_AUTO);
                                                    startActivity(new Intent(AirRemoteControlMianActivity.this, LearnByHandActivity.class));
                                                } else {
                                                    if (key_windspeed_auto) {
                                                        imageview_wind_speed.setBackgroundResource(R.drawable.button_aircondition_windspeed_auto_learned);
                                                        if (!isStartFromExperience) {

                                                            if (code == null) {
                                                                return;
                                                            }
                                                            func[5] = (byte) 0x02;
                                                            func[1] = (byte) 0x01;
                                                            mAirconditionInitKeyValue.setWind(wind);
                                                            mAirconditionInitKeyValue.save();

                                                            data = packData();
                                                            if (data_key_windspeed_auto != null) {
                                                                mRemoteControlManager.sendData(data_key_windspeed_auto);
                                                            } else {
                                                                mRemoteControlManager.sendData(DataExchange.dbBytesToString(data));
                                                            }
                                                        }

                                                    } else {
                                                        imageview_wind_speed.setBackgroundResource(R.drawable.button_aircondition_windspeed_auto_notlearn);
                                                        mKeynotlearnDialog.show();
                                                    }
                                                }
                                            }
                                        })

                                .show();
                    } else {
                        mKeynotlearnDialog.show();
                    }
                }
                break;
            case R.id.imageview_wind_center:
                if (isLearnByHand) {
                    startActivity(new Intent(this, LearnByHandActivity.class));
                } else {
                    if (key_winddirection_up || key_winddirection_middle || key_winddirection_down || key_winddirection_auto) {
                        new ActionSheetDialog(AirRemoteControlMianActivity.this)
                                .builder()
                                .setCancelable(true)
                                .setCanceledOnTouchOutside(true)
                                .addSheetItem("风向向上", ActionSheetDialog.SheetItemColor.GRAY,
                                        new ActionSheetDialog.OnSheetItemClickListener() {
                                            @Override
                                            public void onClick(int which) {
                                                textview_wind_direction.setText("风向向上");
                                                if (isLearnByHand) {
                                                    mRemoteControlManager.setCurrentLearnByHandKeyName(AirKeyNameConstant.KEYNAME.KEYNAME_WIND_DIRECTION_UP);
                                                    startActivity(new Intent(AirRemoteControlMianActivity.this, LearnByHandActivity.class));
                                                } else {
                                                    imageview_wind_direction.setBackgroundResource(R.drawable.button_aircondition_winddirection_up_learned);
                                                    if (key_winddirection_up) {
                                                        if (!isStartFromExperience) {

                                                            if (code == null) {
                                                                return;
                                                            }
                                                            func[5] = (byte) 0x03;
                                                            func[2] = (byte) 0x01;
                                                            directionHand = 0x01;
                                                            mAirconditionInitKeyValue.setDirectionHand(directionHand);
                                                            mAirconditionInitKeyValue.save();

                                                            data = packData();
                                                            if (data_key_winddirection_up != null) {
                                                                mRemoteControlManager.sendData(data_key_winddirection_up);
                                                            } else {
                                                                mRemoteControlManager.sendData(DataExchange.dbBytesToString(data));
                                                            }
                                                        }

                                                    } else {
                                                        imageview_wind_direction.setBackgroundResource(R.drawable.button_aircondition_winddirection_up_notlearn);
                                                        mKeynotlearnDialog.show();
                                                    }
                                                }
                                            }
                                        })
                                .addSheetItem("风向居中", ActionSheetDialog.SheetItemColor.GRAY,
                                        new ActionSheetDialog.OnSheetItemClickListener() {
                                            @Override
                                            public void onClick(int which) {
                                                textview_wind_direction.setText("风向居中");
                                                if (isLearnByHand) {
                                                    mRemoteControlManager.setCurrentLearnByHandKeyName(AirKeyNameConstant.KEYNAME.KEYNAME_WIND_DIRECTION_MIDDLE);
                                                    startActivity(new Intent(AirRemoteControlMianActivity.this, LearnByHandActivity.class));
                                                } else {
                                                    if (key_winddirection_middle) {
                                                        imageview_wind_direction.setBackgroundResource(R.drawable.button_aircondition_winddirection_middle_learned);
                                                        if (!isStartFromExperience) {
                                                            if (code == null) {
                                                                return;
                                                            }
                                                            func[5] = (byte) 0x03;
                                                            func[2] = (byte) 0x02;
                                                            directionHand = 0x02;
                                                            mAirconditionInitKeyValue.setDirectionHand(directionHand);
                                                            mAirconditionInitKeyValue.save();

                                                            data = packData();
                                                            if (data_key_winddirection_middle != null) {
                                                                mRemoteControlManager.sendData(data_key_winddirection_middle);
                                                            } else {
                                                                mRemoteControlManager.sendData(DataExchange.dbBytesToString(data));
                                                            }
                                                        }
                                                    } else {
                                                        imageview_wind_direction.setBackgroundResource(R.drawable.button_aircondition_winddirection_middle_notlearn);
                                                        mKeynotlearnDialog.show();
                                                    }
                                                }
                                            }
                                        })
                                .addSheetItem("风向向下", ActionSheetDialog.SheetItemColor.GRAY,
                                        new ActionSheetDialog.OnSheetItemClickListener() {
                                            @Override
                                            public void onClick(int which) {
                                                textview_wind_direction.setText("风向向下");
                                                if (isLearnByHand) {
                                                    mRemoteControlManager.setCurrentLearnByHandKeyName(AirKeyNameConstant.KEYNAME.KEYNAME_WIND_DIRECTION_DOWN);
                                                    startActivity(new Intent(AirRemoteControlMianActivity.this, LearnByHandActivity.class));
                                                } else {
                                                    if (key_winddirection_down) {
                                                        imageview_wind_direction.setBackgroundResource(R.drawable.button_aircondition_winddirection_down_learned);
                                                        if (!isStartFromExperience) {
                                                            if (code == null) {
                                                                return;
                                                            }
                                                            func[5] = (byte) 0x03;
                                                            func[2] = (byte) 0x03;
                                                            directionHand = 0x03;
                                                            mAirconditionInitKeyValue.setDirectionHand(directionHand);
                                                            mAirconditionInitKeyValue.save();
                                                            data = packData();
                                                            if (data_key_winddirection_down != null) {
                                                                mRemoteControlManager.sendData(data_key_winddirection_down);
                                                            } else {
                                                                mRemoteControlManager.sendData(DataExchange.dbBytesToString(data));
                                                            }
                                                        }
                                                    } else {
                                                        imageview_wind_direction.setBackgroundResource(R.drawable.button_aircondition_winddirection_down_notlearn);
                                                        mKeynotlearnDialog.show();
                                                    }
                                                }
                                            }
                                        })
                                .addSheetItem("自动风向", ActionSheetDialog.SheetItemColor.GRAY,
                                        new ActionSheetDialog.OnSheetItemClickListener() {
                                            @Override
                                            public void onClick(int which) {
                                                textview_wind_direction.setText("自动风向");
                                                if (isLearnByHand) {
                                                    mRemoteControlManager.setCurrentLearnByHandKeyName(AirKeyNameConstant.KEYNAME.KEYNAME_WIND_DIRECTION_AUTO);
                                                    startActivity(new Intent(AirRemoteControlMianActivity.this, LearnByHandActivity.class));
                                                } else {
                                                    if (key_winddirection_auto) {
                                                        imageview_wind_direction.setBackgroundResource(R.drawable.button_aircondition_winddirection_auto_learned);
                                                        if (!isStartFromExperience) {
                                                            if (code == null) {
                                                                return;
                                                            }
                                                            func[5] = (byte) 0x04;
                                                            func[3] = (byte) 0x01;
                                                            directionAuto = 0x01;
                                                            mAirconditionInitKeyValue.setDirectionAuto(directionAuto);
                                                            mAirconditionInitKeyValue.save();
                                                            data = packData();
                                                            if (data_key_winddirection_auto != null) {
                                                                mRemoteControlManager.sendData(data_key_winddirection_auto);
                                                            } else {
                                                                mRemoteControlManager.sendData(DataExchange.dbBytesToString(data));
                                                            }
                                                        }
                                                    } else {
                                                        imageview_wind_direction.setBackgroundResource(R.drawable.button_aircondition_winddirection_auto_notlearn);
                                                        mKeynotlearnDialog.show();
                                                    }
                                                }
                                            }
                                        })
                                .show();
                    } else {
                        mKeynotlearnDialog.show();
                    }
                }
                break;
            case R.id.imageview_power:
                if (isLearnByHand) {
                    mRemoteControlManager.setCurrentLearnByHandKeyName(AirKeyNameConstant.KEYNAME.KEYNAME_POWER_OPEN);
                    startActivity(new Intent(this, LearnByHandActivity.class));
                } else {
                    if (key_power) {
                        if (isStartFromExperience) {
                            if (power == 0x00) {
                                power = (byte) 0x01;
                                switchAirconditionOnOff(true);
                            } else if (power == 0x01) {
                                power = (byte) 0x0;
                                switchAirconditionOnOff(false);
                            }
                        } else {
                            if (code == null) {
                                return;
                            }
                            Log.i(TAG, "mAirconditionInitKeyValue!=null" + (mAirconditionInitKeyValue != null));
                            func[4] = (byte) 0x01;
                            func[5] = (byte) (0x01);
                            if (power == 0x00) {
                                power = (byte) 0x01;
                                mAirconditionInitKeyValue.setKeyPower(0x01);
                                mAirconditionInitKeyValue.save();
                                switchAirconditionOnOff(true);
                            } else if (power == 0x01) {
                                power = (byte) 0x0;
                                mAirconditionInitKeyValue.setKeyPower(0x00);
                                mAirconditionInitKeyValue.save();
                                switchAirconditionOnOff(false);
                            }
                            data = packData();
                            if (data_key_power != null) {
                                mRemoteControlManager.sendData(data_key_power);
                            } else {
                                mRemoteControlManager.sendData(DataExchange.dbBytesToString(data));
                            }
                        }
                    } else {
                        mKeynotlearnDialog.show();
                    }
                }
                break;
        }
    }

    private void switchAirconditionOnOff(boolean on) {
        if (on) {
            imageview_power.setBackgroundResource(R.drawable.button_power_learn_on_normal);
            layout_top_content.setBackgroundResource(R.drawable.airconditioningon);
            textview_model.setTextColor(getResources().getColor(R.color.room_type_text));
            textview_wind_speed.setTextColor(getResources().getColor(R.color.room_type_text));
            textview_wind_direction.setTextColor(getResources().getColor(R.color.room_type_text));
            textview_temperature.setTextColor(0xFF60a3f6);
            textview_tempature_unit.setTextColor(0xFF60a3f6);
            textview_power.setTextColor(getResources().getColor(R.color.room_type_text));
            final Animation animationFadeIn= AnimationUtils.loadAnimation(this, R.anim.fade_in);
            final Animation animationFadeOut= AnimationUtils.loadAnimation(this, R.anim.fade_out);
            final Animation animationFadeHold= AnimationUtils.loadAnimation(this, R.anim.fade_hold);
            animationFadeIn.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    gatwaygif.startAnimation(animationFadeOut);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            animationFadeOut.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    gatwaygif.startAnimation(animationFadeHold);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            animationFadeHold.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    gatwaygif.startAnimation(animationFadeIn);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            gatwaygif.startAnimation(animationFadeIn);
            gatwaygif.setVisibility(View.VISIBLE);
        } else {
            imageview_power.setBackgroundResource(R.drawable.button_power_learn_normal);
            layout_top_content.setBackgroundResource(R.drawable.airconditioningoff);
            textview_model.setTextColor(getResources().getColor(R.color.line_clolor));
            textview_wind_speed.setTextColor(getResources().getColor(R.color.line_clolor));
            textview_wind_direction.setTextColor(getResources().getColor(R.color.line_clolor));
            textview_power.setTextColor(getResources().getColor(R.color.title_blue_bg));
            textview_temperature.setTextColor(0xFF999999);
            textview_tempature_unit.setTextColor(0xFF999999);
            gatwaygif.setAnimation(null);
            gatwaygif.setVisibility(View.GONE);
        }
        imageview_model.setEnabled(on);
        imageview_wind_speed.setEnabled(on);
        imageview_wind_direction.setEnabled(on);
        imageview_temperature_reduce.setEnabled(on);
        imageview_temperature_plus.setEnabled(on);
    }

    byte[] func = new byte[7];

    /**
     * 包装空调数据
     *
     * @return
     */
    public byte[] packData() {
        int len = 0;
        byte[] codeByte = DataExchange.dbString_ToBytes(code);
        data = new byte[13 + codeByte.length];
        data[len++] = (byte) 0x30;
        data[len++] = (byte) 0x01;
        byte[] groupByte = DataExchange.intToTwoByte(group);
        System.arraycopy(groupByte, 0, data, len, 2);
        len += 2;
        System.arraycopy(func, 0, data, len, 7);
        len += 7;
        data[len++] = (byte) (codeByte[0] + 1);
        System.arraycopy(codeByte, 1, data, len, codeByte.length - 1);
        len += codeByte.length - 1;
        data[len++] = (byte) 0xff;
        byte crc = 0;
        for (int i = 0; i < len - 1; i++) {
            crc += data[i];
        }
        data[len] = (byte) (crc & 0xff);//最后一个检验位
        Log.i(TAG, "打包空调控制数据dbBytesToString=" + DataExchange.dbBytesToString(data));
        return data;
    }
}
