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
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.deplink.boruSmart.activity.homepage.SmartHomeMainActivity;
import com.deplink.boruSmart.util.NetUtil;
import com.deplink.boruSmart.util.Perfence;
import com.deplink.boruSmart.util.StringValidatorUtil;
import com.deplink.boruSmart.util.WeakRefHandler;
import com.deplink.boruSmart.view.combinationwidget.TitleLayout;
import com.deplink.boruSmart.view.edittext.ClearEditText;
import com.deplink.boruSmart.view.toast.Ftoast;
import com.deplink.sdk.android.sdk.DeplinkSDK;
import com.deplink.sdk.android.sdk.EventCallback;
import com.deplink.sdk.android.sdk.SDKAction;
import com.deplink.sdk.android.sdk.manager.SDKManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import cn.smssdk.DefaultOnSendMessageHandler;
import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;
import deplink.com.smartwirelessrelay.homegenius.EllESDK.R;

public class RegistActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "RegistActivity";
    private View view_phonenumber_dirverline;
    private View view_password_dirverline;
    private View view_yanzhen_dirverline;
    private ClearEditText edittext_input_phone_number;
    private EditText edittext_verification_code;
    private EditText edittext_input_password;
    private FrameLayout layout_eye;
    private ImageView imageview_eye;
    private EventHandler eh;
    private ArrayList<HashMap<String, Object>> alhmCountries;
    private String simCountryCode = "86";
    private TextView button_SMS_verification_code;
    private int time = Perfence.VERIFYCODE_TIME;
    private SDKManager manager;
    private EventCallback ec;
    private Button button_regist;
    private TitleLayout layout_title;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_regist);
        initViews();
        initDatas();
        initEvents();
    }
    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            Ftoast.create(RegistActivity.this).setText((String) msg.obj).show();
            return true;
        }
    };
    private Handler mhandler = new WeakRefHandler(mCallback);
    String verifycode;
    String phoneNumber;
    String password;
    private boolean isGetCaptche;
    @Override
    protected void onResume() {
        super.onResume();
        button_SMS_verification_code.setBackgroundResource(R.drawable.get_vercode_button_disable_background);
        manager.addEventCallback(ec);
        edittext_input_password.setTransformationMethod(PasswordTransformationMethod.getInstance());
        if (StringValidatorUtil.isMobileNO(edittext_input_phone_number.getText().toString())
                && edittext_input_password.getText().toString().length() >= 6
                && edittext_verification_code.getText().toString().length()>=6
                ) {
            button_regist.setEnabled(true);
        } else {
            button_regist.setEnabled(false);
        }
        view_phonenumber_dirverline.setBackgroundResource(R.color.huise);
    }
    private String username;
    @Override
    protected void onPause() {
        super.onPause();
        manager.removeEventCallback(ec);
    }
    private void getPhoneCaptcha() {
        if (!NetUtil.isNetAvailable(this)) {
            Toast.makeText(RegistActivity.this, "无法访问网络", Toast.LENGTH_SHORT).show();
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
            SMSSDK.getVerificationCode(simCountryCode, username,"5853169",new DefaultOnSendMessageHandler());
            Toast.makeText(RegistActivity.this, "验证码已发送", Toast.LENGTH_SHORT).show();
            button_SMS_verification_code.setEnabled(false);
            time = Perfence.VERIFYCODE_TIME;
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {

                @Override
                public void run() {
                    button_SMS_verification_code.setText(String.format(getString(R.string.register_verifycode_time), time));
                    if (time > 0)
                        handler.postDelayed(this, Perfence.DELAY_VERIFYCODE);
                    else {
                        button_SMS_verification_code.setEnabled(true);
                        button_SMS_verification_code.setText(getResources().getString(R.string.get_sms_verification));
                    }
                    time--;
                }
            }, 0);
        }
    }
    private void initDatas() {
        layout_title.setReturnClickListener(new TitleLayout.ReturnImageClickListener() {
            @Override
            public void onBackPressed() {
                RegistActivity.this.onBackPressed();
            }
        });
        eh = new EventHandler() {
            @Override
            public void afterEvent(int event, int result, Object data) {
                Log.i(TAG, "event:" + event + ", result: " + result + ", data:" + data.toString());
                if (result == SMSSDK.RESULT_COMPLETE) {
                    switch (event) {
                        case SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE:
                            manager.register(phoneNumber, password, verifycode);
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
                        String error = object.optString("error");
                        int status = object.optInt("status");
                        if (status > 0 && !TextUtils.isEmpty(des)) {
                            // Perfence.showCallBack(des);
                            Message message = Message.obtain();
                            message.what = 1;
                            message.obj = des;
                            mhandler.sendMessage(message);
                        }
                        if(status>0 && !TextUtils.isEmpty(error)){
                            if(error.contains("verification code error")){
                                error="用户提交验证验证码错误";
                            }else if(error.contains("within 5 minutes")){
                                error="每个验证码可以在5分钟内验证3次";
                            }else if(error.contains("Illegal")){
                                error="非法检查要求";
                            }
                            Message message = Message.obtain();
                            message.what = 1;
                            message.obj = error;
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
                    case REGISTER:
                        Perfence.setPerfence(Perfence.USER_PASSWORD, password);
                        Perfence.setPerfence(Perfence.PERFENCE_PHONE, username);
                        Ftoast.create(RegistActivity.this).setText("注册成功").show();
                        Intent intent=new Intent(RegistActivity.this, SmartHomeMainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onBindSuccess(SDKAction action, String devicekey) {

            }



            @Override
            public void onFailure(SDKAction action, Throwable throwable) {
                switch (action) {
                    case REGISTER:
                        try {
                            Ftoast.create(RegistActivity.this).setText("注册失败:" + throwable.getMessage()).show();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                }
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
                    button_regist.setEnabled(true);
                }else{
                    button_regist.setEnabled(false);
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
                button_SMS_verification_code.setBackgroundResource(R.drawable.button_vercode_background);
            }else{
                button_SMS_verification_code.setBackgroundResource(R.drawable.get_vercode_button_disable_background);
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {
            Log.i(TAG,"afterTextChanged"+editable.toString());
        }
    };
    private void initEvents() {

        layout_eye.setOnClickListener(this);
        button_regist.setOnClickListener(this);
        button_SMS_verification_code.setOnClickListener(this);
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

    private void initViews() {
        layout_title= findViewById(R.id.layout_title);
        view_phonenumber_dirverline = findViewById(R.id.view_phonenumber_dirverline);
        view_password_dirverline = findViewById(R.id.view_password_dirverline);
        view_yanzhen_dirverline = findViewById(R.id.view_yanzhen_dirverline);
        edittext_input_phone_number = findViewById(R.id.edittext_input_phone_number);
        edittext_verification_code = findViewById(R.id.edittext_verification_code);
        edittext_input_password = findViewById(R.id.edittext_input_password);
        layout_eye = findViewById(R.id.layout_eye);
        imageview_eye = findViewById(R.id.imageview_eye);
        button_regist = findViewById(R.id.button_regist);
        button_SMS_verification_code = findViewById(R.id.buton_get_verification_code);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buton_get_verification_code:
                getPhoneCaptcha();
                break;
            case R.id.button_regist:
                phoneNumber = edittext_input_phone_number.getText().toString().trim();
                Log.i(TAG, "phoneNumber=" + phoneNumber);
                boolean isPhoneNumber = StringValidatorUtil.isMobileNO(phoneNumber);
                if (!isPhoneNumber) {
                    Toast.makeText(this, "请输入正确的手机号码", Toast.LENGTH_SHORT).show();
                    return;
                }
                password = edittext_input_password.getText().toString().trim();
                if (password.equals("")) {
                    Toast.makeText(this, "请输入密码", Toast.LENGTH_SHORT).show();
                }
                verifycode = edittext_verification_code.getText().toString().trim();
                if (verifycode.length() < 6) {
                    Ftoast.create(RegistActivity.this).setText("需要校验的验证码错误").show();
                    return;
                }
                //注册
                if (!isGetCaptche) {
                    Ftoast.create(RegistActivity.this).setText("需要校验的验证码错误").show();
                    return;
                } else {
                    SMSSDK.submitVerificationCode(simCountryCode, username, verifycode);
                }
                break;
            case R.id.layout_eye:
                changeInputCipher();
                break;
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
