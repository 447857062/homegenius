package com.deplink.homegenius.view.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import deplink.com.smartwirelessrelay.homegenius.EllESDK.R;


/**
 * Created by Administrator on 2017/7/25.
 * 带输入框的对话框，有些提示不一样，可以设置标题，输入框默认的字符
 */
public class InputAlertDialog {
    private static final String TAG = "InputAlertDialog";
    private Context context;
    private Dialog dialog;
    private LinearLayout lLayout_bg;
    private TextView txt_title;
    private TextView textview_msg;
    private Button btn_neg;
    private Button btn_pos;
    private Display display;
    private TextView textview_edittext_header;
    private EditText edittext_input;
    private RelativeLayout layout_input;

    public InputAlertDialog(Context context) {
        this.context = context;
        WindowManager windowManager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        display = windowManager.getDefaultDisplay();
    }

    public InputAlertDialog builder() {
        View view = LayoutInflater.from(context).inflate(
                R.layout.view_input_alertdialog, null);
        lLayout_bg = view.findViewById(R.id.lLayout_bg);
        txt_title = view.findViewById(R.id.txt_title);
        textview_edittext_header = view.findViewById(R.id.textview_edittext_header);
        edittext_input = view.findViewById(R.id.edittext_input);
        textview_msg = view.findViewById(R.id.textview_msg);
        layout_input = view.findViewById(R.id.layout_input);
        textview_msg.setVisibility(View.GONE);
        txt_title.setVisibility(View.GONE);
        btn_neg = view.findViewById(R.id.btn_neg);
        btn_neg.setVisibility(View.GONE);
        btn_pos = view.findViewById(R.id.btn_pos);
        btn_pos.setVisibility(View.GONE);
        dialog = new Dialog(context, R.style.MakeSureDialog);
        dialog.setContentView(view);
        lLayout_bg.setLayoutParams(new FrameLayout.LayoutParams((int) (display
                .getWidth() * 0.85), LinearLayout.LayoutParams.WRAP_CONTENT));
        return this;
    }

    public InputAlertDialog setTitle(String title) {
        if ("".equals(title)) {
            txt_title.setText("标题");
        } else {
            txt_title.setText(title);
        }
        return this;
    }

    public InputAlertDialog setMsg(String msg) {
        textview_msg.setVisibility(View.VISIBLE);
        layout_input.setVisibility(View.GONE);
        textview_msg.setText(msg);
        return this;
    }

    public InputAlertDialog setEditTextHeader(String title) {

        textview_edittext_header.setText(title);

        return this;
    }

    public InputAlertDialog setEditTextHint(String title) {
        edittext_input.setHint(title);
        return this;
    }


    public InputAlertDialog setCancelable(boolean cancel) {
        dialog.setCancelable(cancel);
        return this;
    }

    public InputAlertDialog setPositiveButton(String text,
                                              final onSureBtnClickListener listener) {
        if ("".equals(text)) {
            btn_pos.setText("确定");
        } else {
            btn_pos.setText(text);
        }
        btn_pos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String password = edittext_input.getText().toString().trim();
                listener.onSureBtnClicked(password);
                dialog.dismiss();
            }
        });
        return this;
    }
    public interface onSureBtnClickListener {
        void onSureBtnClicked(String password);
    }

    public InputAlertDialog setNegativeButton(String text,
                                              final View.OnClickListener listener) {
        if ("".equals(text)) {
            btn_neg.setText("取消");
        } else {
            btn_neg.setText(text);
        }
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

    private void setLayout() {
        txt_title.setVisibility(View.VISIBLE);
        btn_pos.setVisibility(View.VISIBLE);
        btn_neg.setVisibility(View.VISIBLE);
    }

    public void show() {
        setLayout();
        dialog.show();
    }
}
