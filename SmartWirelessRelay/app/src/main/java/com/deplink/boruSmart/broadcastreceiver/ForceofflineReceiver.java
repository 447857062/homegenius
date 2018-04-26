package com.deplink.boruSmart.broadcastreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.deplink.boruSmart.Protocol.json.Room;
import com.deplink.boruSmart.Protocol.json.device.SmartDev;
import com.deplink.boruSmart.Protocol.json.device.getway.GatwayDevice;
import com.deplink.boruSmart.Protocol.json.device.lock.Record;
import com.deplink.boruSmart.Protocol.json.device.router.Router;
import com.deplink.boruSmart.activity.personal.login.LoginActivity;
import com.deplink.boruSmart.constant.AppConstant;
import com.deplink.boruSmart.util.Perfence;
import com.deplink.boruSmart.view.dialog.AlertDialog;

import org.litepal.crud.DataSupport;

/**
 * Created by ${kelijun} on 2018/4/26.
 */

public class ForceofflineReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, Intent intent) {
        String action=intent.getAction();
        if(action.equals(AppConstant.FORCE_OFFLINE_ACIION)){
            Perfence.setPerfence(AppConstant.USER_LOGIN, false);
            DataSupport.deleteAll(SmartDev.class);
            DataSupport.deleteAll(GatwayDevice.class);
            DataSupport.deleteAll(Room.class);
            DataSupport.deleteAll(Record.class);
            DataSupport.deleteAll(Router.class);
            new AlertDialog(context).builder().setTitle("账号异地登录")
                    .setMsg("当前账号已在其它设备上登录,是否重新登录")
                    .setPositiveButton("确认", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent=new Intent(context, LoginActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(intent);
                        }
                    }).setNegativeButton("取消", new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            }).show();
        }

    }
}
