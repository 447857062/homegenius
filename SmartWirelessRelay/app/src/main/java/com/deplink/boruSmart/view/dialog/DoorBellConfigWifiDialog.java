package com.deplink.boruSmart.view.dialog;

import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.deplink.boruSmart.util.Perfence;
import com.deplink.boruSmart.view.edittext.ClearEditText;
import com.deplink.boruSmart.view.toast.Ftoast;

import java.util.Timer;
import java.util.TimerTask;

import deplink.com.smartwirelessrelay.homegenius.EllESDK.R;


/**
 * Created by Administrator on 2017/7/25.
 * 带输入框的对话框，有些提示不一样，可以设置标题，输入框默认的字符
 */
public class DoorBellConfigWifiDialog {
    private static final String TAG = "InputAlertDialog";
    private Context context;
    private Dialog dialog;
    private RelativeLayout lLayout_bg;
    private Button btn_neg;
    private Button btn_pos;
    private Display display;
    private ClearEditText edittext_input;
    private ClearEditText edittext_input_password;
    public DoorBellConfigWifiDialog(Context context) {
        this.context = context;
        WindowManager windowManager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        display = windowManager.getDefaultDisplay();
    }

    public DoorBellConfigWifiDialog builder() {
        View view = LayoutInflater.from(context).inflate(
                R.layout.view_doorbell_input_alertdialog, null);
        lLayout_bg = view.findViewById(R.id.lLayout_bg);

        edittext_input = view.findViewById(R.id.edittext_input);
        edittext_input_password = view.findViewById(R.id.edittext_input_password);
        btn_neg = view.findViewById(R.id.btn_neg);
        btn_pos = view.findViewById(R.id.btn_pos);
        dialog = new Dialog(context, R.style.MakeSureDialog);
        dialog.setContentView(view);
        lLayout_bg.setLayoutParams(new FrameLayout.LayoutParams((int) (display
                .getWidth() * 0.85), (int) Perfence.dp2px(context,195.0f)));
        return this;
    }
    public DoorBellConfigWifiDialog setPositiveButton(final onSureBtnClickListener listener) {
        btn_pos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String wifiSsid = edittext_input.getText().toString().trim();
                String password = edittext_input_password.getText().toString().trim();
               if(!TextUtils.isEmpty(wifiSsid) && !TextUtils.isEmpty(password) && password.length()>=8){
                   listener.onSureBtnClicked(wifiSsid,password);
                   dialog.dismiss();
               }else{
                   Ftoast.create(context).setText("请输入正确的wif信息").show();
               }

            }
        });
        return this;
    }
    public interface onSureBtnClickListener {
        void onSureBtnClicked(String ssid,String password);
    }
    public DoorBellConfigWifiDialog setNegativeButton(final View.OnClickListener listener) {
        btn_neg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn_neg.setBackgroundResource(R.color. input_alert_dialog_stroke);
                listener.onClick(v);
                dialog.dismiss();
            }
        });
        return this;
    }
    private void showInputmothed() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
                           public void run() {
                               InputMethodManager inputManager =
                                       (InputMethodManager) edittext_input.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                               inputManager.showSoftInput(edittext_input, 0);
                           }
                       },
                500);
    }
    public void show() {
        dialog.show();
        showInputmothed();
    }
}
