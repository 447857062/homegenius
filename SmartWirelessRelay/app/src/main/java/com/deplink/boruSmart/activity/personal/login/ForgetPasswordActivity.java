package com.deplink.boruSmart.activity.personal.login;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.deplink.boruSmart.activity.homepage.SmartHomeMainActivity;
import com.deplink.boruSmart.constant.AppConstant;
import com.deplink.boruSmart.util.NetUtil;
import com.deplink.boruSmart.util.Perfence;
import com.deplink.boruSmart.util.StringValidatorUtil;
import com.deplink.boruSmart.util.WeakRefHandler;
import com.deplink.boruSmart.view.combinationwidget.TitleLayout;
import com.deplink.boruSmart.view.dialog.AlertDialog;
import com.deplink.boruSmart.view.edittext.ClearEditText;
import com.deplink.boruSmart.view.toast.Ftoast;
import com.deplink.sdk.android.sdk.DeplinkSDK;
import com.deplink.sdk.android.sdk.EventCallback;
import com.deplink.sdk.android.sdk.SDKAction;
import com.deplink.sdk.android.sdk.bean.User;
import com.deplink.sdk.android.sdk.manager.SDKManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import cn.smssdk.DefaultOnSendMessageHandler;
import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;
import deplink.com.smartwirelessrelay.homegenius.EllESDK.R;

public class ForgetPasswordActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "ForgetPasswordActivity";
    private View view_phonenumber_dirverline;
    private View view_password_dirverline;
    private View view_yanzhen_dirverline;
    private ClearEditText edittext_input_phone_number;
    private EditText edittext_verification_code;
    private EditText edittext_input_password;
    private ImageView imageview_eye;
    private SDKManager manager;
    private EventCallback ec;
    private String newPassword;
    private Button button_login;
    private int time = Perfence.VERIFYCODE_TIME;
    private String username;
    private ArrayList<HashMap<String, Object>> alhmCountries;
    private String simCountryCode = "86";
    private TextView buton_get_verification_code;
    private EventHandler eh;
    private TitleLayout layout_title;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);
        initViews();
        initDatas();
        initEvents();
    }
    @Override
    protected void onResume() {
        super.onResume();
        buton_get_verification_code.setBackgroundResource(R.drawable.get_vercode_button_disable_background);
        manager.addEventCallback(ec);
        if (StringValidatorUtil.isMobileNO(edittext_input_phone_number.getText().toString())
                && edittext_input_password.getText().toString().length() >= 6
                && edittext_verification_code.getText().toString().length()>=6
                ) {
            button_login.setEnabled(true);
        } else {
            button_login.setEnabled(false);
        }
        view_phonenumber_dirverline.setBackgroundResource(R.color.huise);
    }

    @Override
    protected void onPause() {
        super.onPause();
        manager.removeEventCallback(ec);
    }

    private void initDatas() {
        layout_title.setReturnClickListener(new TitleLayout.ReturnImageClickListener() {
            @Override
            public void onBackPressed() {
                ForgetPasswordActivity.this.onBackPressed();
            }
        });
        eh = new EventHandler() {
            @Override
            public void afterEvent(int event, int result, Object data) {
                Log.i(TAG, "event:" + event + ", result: " + result + ", data:" + data.toString());
                if (result == SMSSDK.RESULT_COMPLETE) {
                    switch (event) {
                        case SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE:
                            manager.resetPassword(username, newPassword,verifycode);
                            break;
                        case SMSSDK.EVENT_GET_VERIFICATION_CODE:
                            break;
                        case SMSSDK.EVENT_GET_SUPPORTED_COUNTRIES:
                            Perfence.alhmCountries = alhmCountries = (ArrayList<HashMap<String, Object>>) data;
                            break;
                        default:
                            break;
                    }
                } else {
                    String msg = ((Throwable) data).getMessage();
                    Log.i(TAG, msg);
                    try {
                        JSONObject object = new JSONObject(msg);
                        String des = object.optString("detail");
                        int status = object.optInt("status");
                        if (status > 0 && !TextUtils.isEmpty(des)) {
                            Message message = Message.obtain();
                            message.what = 1;
                            message.obj = des;
                            mhandler.sendMessage(message);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

        };
        SMSSDK.registerEventHandler(eh);
        if (Perfence.alhmCountries == null || Perfence.alhmCountries.size() == 0)
            SMSSDK.getSupportedCountries();
        else {
            alhmCountries = Perfence.alhmCountries;
        }
        DeplinkSDK.initSDK(getApplicationContext(), Perfence.SDK_APP_KEY);
        manager = DeplinkSDK.getSDKManager();
        ec = new EventCallback() {

            @Override
            public void onSuccess(SDKAction action) {
                switch (action) {
                    case RESET_PASSWORD:

                        Perfence.setPerfence(Perfence.USER_PASSWORD, newPassword);
                        manager.login(Perfence.getPerfence(Perfence.PERFENCE_PHONE), newPassword);
                        Ftoast.create(ForgetPasswordActivity.this).setText("重置密码成功").show();
                        break;
                    case LOGIN:
                        Perfence.setPerfence(AppConstant.PERFENCE_BIND_APP_UUID, manager.getUserInfo().getUuid());
                        User user = manager.getUserInfo();
                        Perfence.setPerfence(Perfence.USER_PASSWORD, user.getPassword());
                        Perfence.setPerfence(Perfence.PERFENCE_PHONE, user.getName());
                        Perfence.setPerfence(AppConstant.USER.USER_GETIMAGE_FROM_SERVICE,false);
                        Perfence.setPerfence(AppConstant.USER_LOGIN, true);
                        startActivity(new Intent(ForgetPasswordActivity.this, SmartHomeMainActivity.class));
                        break;
                }
            }

            @Override
            public void onBindSuccess(SDKAction action, String devicekey) {

            }



            @Override
            public void onFailure(SDKAction action, Throwable throwable) {
                switch (action) {
                    case ALERTPASSWORD:
                        Ftoast.create(ForgetPasswordActivity.this).setText("更改密码失败:" + throwable.getMessage()).show();
                        break;
                }
            }

            @Override
            public void connectionLost(Throwable throwable) {
                super.connectionLost(throwable);
                Perfence.setPerfence(AppConstant.USER_LOGIN, false);
                new AlertDialog(ForgetPasswordActivity.this).builder().setTitle("账号异地登录")
                        .setMsg("当前账号已在其它设备上登录,是否重新登录")
                        .setPositiveButton("确认", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                startActivity(new Intent(ForgetPasswordActivity.this, LoginActivity.class));
                            }
                        }).setNegativeButton("取消", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                }).show();
            }
        };
        edittext_input_phone_number.addTextChangedListener(mPhoneNumberInputWatcher);
        edittext_input_password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(editable.toString().length()>=6){
                    button_login.setEnabled(true);
                }else{
                    button_login.setEnabled(false);
                }
            }
        });
    }
    private TextWatcher mPhoneNumberInputWatcher=new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            Log.i(TAG,"beforeTextChanged"+charSequence);
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            Log.i(TAG,"onTextChanged"+charSequence);
            if(StringValidatorUtil.isMobileNO(charSequence.toString())){
                buton_get_verification_code.setBackgroundResource(R.drawable.button_vercode_background);
            }else{
                buton_get_verification_code.setBackgroundResource(R.drawable.get_vercode_button_disable_background);
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {
            Log.i(TAG,"afterTextChanged"+editable.toString());

        }
    };
    private void initEvents() {
        imageview_eye.setOnClickListener(this);
        button_login.setOnClickListener(this);
        buton_get_verification_code.setOnClickListener(this);
        edittext_input_phone_number.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        view_phonenumber_dirverline.setBackgroundResource(R.color.huise);
                        view_password_dirverline.setBackgroundResource(R.color.line_dirver_color);
                        view_yanzhen_dirverline.setBackgroundResource(R.color.line_dirver_color);
                        break;
                }
                return false;
            }
        });
        edittext_verification_code.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        view_phonenumber_dirverline.setBackgroundResource(R.color.line_dirver_color);
                        view_password_dirverline.setBackgroundResource(R.color.line_dirver_color);
                        view_yanzhen_dirverline.setBackgroundResource(R.color.huise);
                        break;
                }
                return false;
            }
        });
        edittext_input_password.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        view_phonenumber_dirverline.setBackgroundResource(R.color.line_dirver_color);
                        view_password_dirverline.setBackgroundResource(R.color.huise);
                        view_yanzhen_dirverline.setBackgroundResource(R.color.line_dirver_color);
                        break;
                }
                return false;
            }
        });
    }
    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            Ftoast.create(ForgetPasswordActivity.this).setText((String) msg.obj).show();
            return true;
        }
    };
    private Handler mhandler = new WeakRefHandler(mCallback);
    private void initViews() {
        buton_get_verification_code = findViewById(R.id.buton_get_verification_code);
        view_phonenumber_dirverline = findViewById(R.id.view_phonenumber_dirverline);
        view_password_dirverline = findViewById(R.id.view_password_dirverline);
        view_yanzhen_dirverline = findViewById(R.id.view_yanzhen_dirverline);
        edittext_input_phone_number = findViewById(R.id.edittext_input_phone_number);
        edittext_verification_code = findViewById(R.id.edittext_verification_code);
        edittext_input_password = findViewById(R.id.edittext_input_password);
        imageview_eye = findViewById(R.id.imageview_eye);
        button_login = findViewById(R.id.button_login);
        layout_title= findViewById(R.id.layout_title);
    }

    private boolean isGetCaptche;
    String verifycode;
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imageview_eye:
                changeInputCipher();
                break;
            case R.id.buton_get_verification_code:
                getPhoneCaptcha();
                break;
            case R.id.button_login:
                String newPassword = edittext_input_password.getText().toString().trim();
                if (newPassword.length() < 6) {
                    Ftoast.create(ForgetPasswordActivity.this).setText("密码位数不对").show();
                    return;
                }
                this.newPassword = newPassword;
                verifycode = edittext_verification_code.getText().toString().trim();
                if (verifycode.length() < 6) {
                    Ftoast.create(ForgetPasswordActivity.this).setText("密码位数不对").show();
                    return;
                }
                if (!isGetCaptche) {
                    Ftoast.create(ForgetPasswordActivity.this).setText("需要校验的验证码错误").show();
                    return;
                } else {
                    SMSSDK.submitVerificationCode(simCountryCode, username, verifycode);
                }
                break;

        }
    }

    private void getPhoneCaptcha() {
        if (!NetUtil.isNetAvailable(this)) {
            Toast.makeText(ForgetPasswordActivity.this, "无法访问网络", Toast.LENGTH_SHORT).show();
            return;
        }
        username = edittext_input_phone_number.getText().toString();
        if (username.equals("")) {
            return;
        }
        boolean support = false;
        for (int i = 0; i < alhmCountries.size(); i++) {
            HashMap<String, Object> hashMap = alhmCountries.get(i);
            if (simCountryCode.equalsIgnoreCase(hashMap.get("zone").toString()) && username.matches(hashMap.get("rule").toString()))
                support = true;
        }
        if (support) {
            isGetCaptche = true;
            SMSSDK.getVerificationCode(simCountryCode, username,"14967877",new DefaultOnSendMessageHandler());
            Toast.makeText(ForgetPasswordActivity.this, "验证码已发送", Toast.LENGTH_SHORT).show();
            buton_get_verification_code.setEnabled(false);
            time = Perfence.VERIFYCODE_TIME;
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    buton_get_verification_code.setText(String.format(getString(R.string.register_verifycode_time), time));
                    if (time > 0)
                        handler.postDelayed(this, Perfence.DELAY_VERIFYCODE);
                    else {
                        buton_get_verification_code.setEnabled(true);
                        buton_get_verification_code.setText(getResources().getString(R.string.get_sms_verification));
                    }
                    time--;
                }
            }, 0);
        }
    }

    /**
     * 设置密文明文之间切换
     */
    private void changeInputCipher() {
        if (edittext_input_password.getTransformationMethod() instanceof PasswordTransformationMethod) {
            imageview_eye.setImageResource(R.drawable.displayicon);
            edittext_input_password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());

        } else if (edittext_input_password.getTransformationMethod() instanceof HideReturnsTransformationMethod) {

            imageview_eye.setImageResource(R.drawable.hideicon);
            edittext_input_password.setTransformationMethod(PasswordTransformationMethod.getInstance());

        }
        int length = edittext_input_password.getText().toString().trim().length();
        if (length != 0) {
            edittext_input_password.setSelection(length);
        }
    }
}
