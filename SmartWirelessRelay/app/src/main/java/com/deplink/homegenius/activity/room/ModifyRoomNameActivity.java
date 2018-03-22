package com.deplink.homegenius.activity.room;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.deplink.homegenius.Protocol.json.Room;
import com.deplink.homegenius.activity.personal.login.LoginActivity;
import com.deplink.homegenius.constant.AppConstant;
import com.deplink.homegenius.manager.room.RoomListener;
import com.deplink.homegenius.manager.room.RoomManager;
import com.deplink.homegenius.util.Perfence;
import com.deplink.homegenius.view.combinationwidget.TitleLayout;
import com.deplink.homegenius.view.dialog.AlertDialog;
import com.deplink.homegenius.view.edittext.ClearEditText;
import com.deplink.homegenius.view.toast.ToastSingleShow;
import com.deplink.sdk.android.sdk.DeplinkSDK;
import com.deplink.sdk.android.sdk.EventCallback;
import com.deplink.sdk.android.sdk.SDKAction;
import com.deplink.sdk.android.sdk.manager.SDKManager;

import org.litepal.crud.DataSupport;

import deplink.com.smartwirelessrelay.homegenius.EllESDK.R;

public class ModifyRoomNameActivity extends Activity  {
    private static final String TAG = "ModifyRoomNameActivity";
    private ClearEditText clearEditText;
    private RoomManager mRoomManager;
    private String orgRoomName;
    private boolean isLogin;
    private SDKManager manager;
    private EventCallback ec;
    private TitleLayout layout_title;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_room_name);
        initViews();
        initDatas();
        initEvents();
    }
    @Override
    protected void onResume() {
        super.onResume();
        isLogin = Perfence.getBooleanPerfence(AppConstant.USER_LOGIN);
        orgRoomName = RoomManager.getInstance().getCurrentSelectedRoom().getRoomName();
        clearEditText.setText(orgRoomName);
        clearEditText.setSelection(orgRoomName.length());
        manager.addEventCallback(ec);
        mRoomManager.addRoomListener(mRoomListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        manager.removeEventCallback(ec);
        mRoomManager.removeRoomListener(mRoomListener);
    }
    private RoomListener mRoomListener;
    private void initDatas() {

        layout_title.setReturnClickListener(new TitleLayout.ReturnImageClickListener() {
            @Override
            public void onBackPressed() {
                ModifyRoomNameActivity.this.onBackPressed();
            }
        });
        layout_title.setEditTextClickListener(new TitleLayout.EditTextClickListener() {
            @Override
            public void onEditTextPressed() {
                roomName = clearEditText.getText().toString();
                if (isLogin) {
                    if (!roomName.equalsIgnoreCase("")  ) {
                        if(!roomName.equals(orgRoomName)){
                            //查询看看有没有重名的
                            Room room = DataSupport.where("roomName = ?", roomName).findFirst(Room.class);
                            if(room!=null){
                                ToastSingleShow.showText(ModifyRoomNameActivity.this,"已存在同名的房间,修改房间名称失败");
                                return;
                            }else{
                                String roomUid = mRoomManager.getCurrentSelectedRoom().getUid();
                                int roomOrdinalNumber=mRoomManager.getCurrentSelectedRoom().getRoomOrdinalNumber();
                                Log.i(TAG, "roomUid=" + roomUid);
                                mRoomManager.updateRoomNameHttp(roomUid, roomName,roomOrdinalNumber);
                            }
                        }else{
                            ModifyRoomNameActivity.this.finish();
                        }
                    } else {
                        Toast.makeText(ModifyRoomNameActivity.this, "请输入房间名称", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    ToastSingleShow.showText(ModifyRoomNameActivity.this, "未登陆，请先登陆");
                }
            }
        });
        mRoomManager = RoomManager.getInstance();
        mRoomManager.initRoomManager(this);
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
            public void deviceOpSuccess(String op, String deviceKey) {
                super.deviceOpSuccess(op, deviceKey);
            }

            @Override
            public void onFailure(SDKAction action, Throwable throwable) {

            }

            @Override
            public void connectionLost(Throwable throwable) {
                super.connectionLost(throwable);
                isLogin = false;
                Perfence.setPerfence(AppConstant.USER_LOGIN, false);
                new AlertDialog(ModifyRoomNameActivity.this).builder().setTitle("账号异地登录")
                        .setMsg("当前账号已在其它设备上登录,是否重新登录")
                        .setPositiveButton("确认", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                startActivity(new Intent(ModifyRoomNameActivity.this, LoginActivity.class));
                            }
                        }).setNegativeButton("取消", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                }).show();
            }
        };
        mRoomListener=new RoomListener() {
            @Override
            public void responseUpdateRoomNameResult() {
                super.responseUpdateRoomNameResult();
                int result = RoomManager.getInstance().updateRoomName(orgRoomName, roomName);
                if (result != 1) {
                    Toast.makeText(ModifyRoomNameActivity.this, "修改房间名称失败", Toast.LENGTH_SHORT).show();
                } else {
                    mRoomManager.getCurrentSelectedRoom().setRoomName(roomName);
                    Intent intent = new Intent(ModifyRoomNameActivity.this, ManageRoomActivity.class);
                    startActivity(intent);
                }
            }
        };
    }

    private void initEvents() {
    }

    private void initViews() {
        clearEditText = findViewById(R.id.clear);
        layout_title= findViewById(R.id.layout_title);
    }

    private String roomName;


}
