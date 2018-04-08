package com.deplink.boruSmart.view.dialog.doorbeel;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.deplink.boruSmart.activity.device.doorbell.EditDoorbellActivity;
import com.deplink.boruSmart.util.Perfence;
import com.deplink.boruSmart.activity.device.doorbell.VistorHistoryActivity;

import deplink.com.smartwirelessrelay.homegenius.EllESDK.R;


/**
 * Created by Administrator on 2017/7/25.
 */
public class DoorbeelMenuDialog extends Dialog implements View.OnClickListener {
    private Context mContext;
    private View view_mode_menu;
    private RelativeLayout layout_edit;
    private RelativeLayout layout_record;

    public DoorbeelMenuDialog(Context context) {
        super(context, R.style.MakeSureDialog);
        mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowManager.LayoutParams p = new WindowManager.LayoutParams();
        DisplayMetrics dm = new DisplayMetrics();
        ((Activity) mContext).getWindowManager().getDefaultDisplay().getMetrics(dm);
        p.width = (int) Perfence.dp2px(mContext, 120);
        View view = LayoutInflater.from(mContext).inflate(R.layout.doorbeel_menu_dialog, null);
        setContentView(view, p);
        //初始化界面控件
        initView();
        //初始化界面控件的事件
        initEvent();
    }
    private void initView() {
        view_mode_menu = findViewById(R.id.view_mode_menu);
        layout_edit = (RelativeLayout) findViewById(R.id.layout_edit);
        layout_record = (RelativeLayout) findViewById(R.id.layout_record);
    }
    private void initEvent() {
        view_mode_menu.setOnClickListener(this);
        layout_edit.setOnClickListener(this);
        layout_record.setOnClickListener(this);
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.view_mode_menu:
                this.dismiss();
                break;
            case R.id.layout_edit:
                this.dismiss();
                mContext.startActivity(new Intent(mContext, EditDoorbellActivity.class));
                break;
            case R.id.layout_record:
                this.dismiss();
                Intent intent = new Intent(mContext, VistorHistoryActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
                break;
        }
    }
    @Override
    public void show() {
        Window dialogWindow = this.getWindow();
        dialogWindow.setGravity(Gravity.RIGHT | Gravity.TOP);
        super.show();
    }
}
