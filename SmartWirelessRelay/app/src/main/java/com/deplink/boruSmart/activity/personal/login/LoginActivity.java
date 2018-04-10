package com.deplink.boruSmart.activity.personal.login;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.deplink.boruSmart.Protocol.json.Room;
import com.deplink.boruSmart.Protocol.json.device.SmartDev;
import com.deplink.boruSmart.Protocol.json.device.getway.GatwayDevice;
import com.deplink.boruSmart.Protocol.json.device.lock.Record;
import com.deplink.boruSmart.Protocol.json.device.router.Router;
import com.deplink.boruSmart.activity.homepage.SmartHomeMainActivity;
import com.deplink.boruSmart.activity.personal.PersonalCenterActivity;
import com.deplink.boruSmart.constant.AppConstant;
import com.deplink.boruSmart.util.NetUtil;
import com.deplink.boruSmart.util.Perfence;
import com.deplink.boruSmart.util.StringValidatorUtil;
import com.deplink.boruSmart.view.combinationwidget.TitleLayout;
import com.deplink.boruSmart.view.toast.Ftoast;
import com.deplink.sdk.android.sdk.DeplinkSDK;
import com.deplink.sdk.android.sdk.EventCallback;
import com.deplink.sdk.android.sdk.SDKAction;
import com.deplink.sdk.android.sdk.bean.User;
import com.deplink.sdk.android.sdk.manager.SDKManager;
import com.tencent.android.tpush.XGIOperateCallback;
import com.tencent.android.tpush.XGPushManager;

import org.litepal.crud.DataSupport;

import java.util.Timer;
import java.util.TimerTask;

import deplink.com.smartwirelessrelay.homegenius.EllESDK.R;

public class LoginActivity extends Activity implements View.OnClickListener, View.OnFocusChangeListener {
    private static final String TAG = "LoginActivity";
    private TextView textview_forget_password;
    private TextView textview_regist_now;
    private Button button_login;
    private EditText edittext_input_phone_number;
    private EditText edittext_input_password;
    private SDKManager manager;
    private EventCallback ec;
    private View view_phonenumber_dirverline;
    private View view_password_dirverline;
    private ImageView imageview_eye;
    private TitleLayout layout_title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initViews();
        initDatas();
        initEvents();
    }

    private void showInputmothed() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
                           public void run() {
                               InputMethodManager inputManager =
                                       (InputMethodManager) edittext_input_phone_number.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                               inputManager.showSoftInput(edittext_input_phone_number, 0);
                           }
                       },
                500);

    }

    @Override
    protected void onResume() {
        super.onResume();
        manager.addEventCallback(ec);
        showInputmothed();
        if (StringValidatorUtil.isMobileNO(edittext_input_phone_number.getText().toString())
                && edittext_input_password.getText().toString().length() >= 6) {
            button_login.setEnabled(true);
        } else {
            button_login.setEnabled(false);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        manager.removeEventCallback(ec);
    }

    String isFromUserinfoActivity;

    private void initDatas() {
        isFromUserinfoActivity = getIntent().getStringExtra("startfrom");
        layout_title.setReturnClickListener(new TitleLayout.ReturnImageClickListener() {
            @Override
            public void onBackPressed() {
                if (isFromUserinfoActivity != null && isFromUserinfoActivity.equals("userinfoactivity")) {
                    startActivity(new Intent(LoginActivity.this, PersonalCenterActivity.class));
                } else {
                    LoginActivity.this.onBackPressed();
                }

            }
        });
        DeplinkSDK.initSDK(getApplicationContext(), Perfence.SDK_APP_KEY);
        manager = DeplinkSDK.getSDKManager();
        ec = new EventCallback() {
            @Override
            public void onSuccess(SDKAction action) {
                switch (action) {
                    case LOGIN:
                        Perfence.setPerfence(AppConstant.PERFENCE_BIND_APP_UUID, manager.getUserInfo().getUuid());
                        manager.connectMQTT(getApplicationContext());
                        User user = manager.getUserInfo();
                        //清除本地数据库
                        String lastLoginUser = Perfence.getPerfence(Perfence.PERFENCE_PHONE);
                        Log.i(TAG, "lastLoginUser=" + lastLoginUser);
                        if (!lastLoginUser.equalsIgnoreCase(user.getName())) {
                            DataSupport.deleteAll(SmartDev.class);
                            DataSupport.deleteAll(GatwayDevice.class);
                            DataSupport.deleteAll(Room.class);
                            DataSupport.deleteAll(Record.class);
                            DataSupport.deleteAll(Router.class);
                        }
                        Perfence.setPerfence(AppConstant.USER.USER_GETIMAGE_FROM_SERVICE, false);
                        Perfence.setPerfence(Perfence.USER_PASSWORD, user.getPassword());
                        Perfence.setPerfence(Perfence.PERFENCE_PHONE, user.getName());
                        Perfence.setPerfence(AppConstant.USER_LOGIN, true);
                        startActivity(new Intent(LoginActivity.this, SmartHomeMainActivity.class));
                        LoginActivity.this.finish();
                        String uuid = manager.getUserInfo().getUuid();
                        Log.i(TAG, "点击登录 onSuccess login uuid=" + uuid);
                        if (!uuid.equalsIgnoreCase("")) {
                            Log.i("TPush", "注册uuid：" + uuid);
                            XGPushManager.registerPush(getApplicationContext(), uuid, new XGIOperateCallback() {
                                @Override
                                public void onSuccess(Object data, int flag) {
                                    Log.i("TPush", "注册成功，设备token为：" + data);
                                }

                                @Override
                                public void onFail(Object data, int errCode, String msg) {
                                    Log.i("TPush", "注册失败，错误码：" + errCode + ",错误信息：" + msg);
                                }
                            });
                            XGPushManager.enableService(LoginActivity.this, true);
                        }
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
                    case LOGIN:
                        Ftoast.create(LoginActivity.this).setText("" + throwable.getMessage()).show();
                        Perfence.setPerfence(AppConstant.USER_LOGIN, false);
                        break;
                }
            }

        };
        edittext_input_password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().length() >= 6) {
                    button_login.setEnabled(true);
                } else {
                    button_login.setEnabled(false);
                }
            }
        });
    }


    private void initEvents() {
        textview_forget_password.setOnClickListener(this);
        textview_regist_now.setOnClickListener(this);
        button_login.setOnClickListener(this);
        edittext_input_phone_number.setOnFocusChangeListener(this);
        edittext_input_password.setOnFocusChangeListener(this);
        imageview_eye.setOnClickListener(this);
    }

    private void initViews() {
        textview_forget_password = (TextView) findViewById(R.id.textview_forget_password);
        textview_regist_now = (TextView) findViewById(R.id.textview_regist_now);
        button_login = (Button) findViewById(R.id.button_login);
        edittext_input_password = (EditText) findViewById(R.id.edittext_input_password);
        edittext_input_phone_number = (EditText) findViewById(R.id.edittext_input_phone_number);
        view_phonenumber_dirverline = findViewById(R.id.view_phonenumber_dirverline);
        view_password_dirverline = findViewById(R.id.view_password_dirverline);
        imageview_eye = (ImageView) findViewById(R.id.imageview_eye);
        layout_title = (TitleLayout) findViewById(R.id.layout_title);
        layout_title.setBackImageResource(R.drawable.notloggedinicon);
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imageview_eye:
                changeInputCipher();
                break;
            case R.id.button_login:
                final String phoneNumber = edittext_input_phone_number.getText().toString().trim();
                boolean isValidatorPhone = StringValidatorUtil.isMobileNO(phoneNumber);
                if (!isValidatorPhone) {
                    Toast.makeText(this, "请输入正确的手机号码", Toast.LENGTH_SHORT).show();
                    return;
                }
                String password = edittext_input_password.getText().toString().trim();
                if (password.length() < 6) {
                    Toast.makeText(this, "密码位数6-20", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (NetUtil.isNetAvailable(this)) {
                    manager.login(phoneNumber, password);
                } else {
                    Ftoast.create(LoginActivity.this).setText("网络连接不可用").show();
                }

                break;
            case R.id.textview_forget_password:
                startActivity(new Intent(LoginActivity.this, ForgetPasswordActivity.class));
                break;
            case R.id.textview_regist_now:
                startActivity(new Intent(LoginActivity.this, RegistActivity.class));
                break;
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        switch (v.getId()) {
            case R.id.edittext_input_phone_number:
                if (hasFocus) {
                    // 此处为得到焦点时的处理内容
                    view_phonenumber_dirverline.setBackgroundResource(R.color.huise);
                } else {
                    // 此处为失去焦点时的处理内容
                    view_phonenumber_dirverline.setBackgroundResource(R.color.line_dirver_color);
                }
                break;
            case R.id.edittext_input_password:
                if (hasFocus) {
                    // 此处为得到焦点时的处理内容
                    view_password_dirverline.setBackgroundResource(R.color.huise);
                } else {
                    // 此处为失去焦点时的处理内容
                    view_password_dirverline.setBackgroundResource(R.color.line_dirver_color);
                }
                break;
        }
    }
}
