package com.deplink.homegenius.activity.room;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.deplink.homegenius.constant.AppConstant;
import com.deplink.homegenius.manager.room.RoomListener;
import com.deplink.homegenius.manager.room.RoomManager;
import com.deplink.homegenius.util.NetUtil;
import com.deplink.homegenius.util.Perfence;
import com.deplink.homegenius.view.combinationwidget.TitleLayout;
import com.deplink.homegenius.view.dialog.AlertDialog;
import com.deplink.homegenius.view.toast.ToastSingleShow;

import deplink.com.smartwirelessrelay.homegenius.EllESDK.R;

public class ManageRoomActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "ManageRoomActivity";
    private Button button_delete_room;
    private TextView textview_room_name;
    private String mRoomName;
    private RelativeLayout layout_room_name;
    private RoomManager mRoomManager;
    private TitleLayout layout_title;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_room);
        initViews();
        initDatas();
        initEvents();
    }

    private void initDatas() {
        layout_title.setReturnClickListener(new TitleLayout.ReturnImageClickListener() {
            @Override
            public void onBackPressed() {
                Intent intent = new Intent(ManageRoomActivity.this, DeviceNumberActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
        mRoomManager = RoomManager.getInstance();
        mRoomManager.initRoomManager(this);
        mRoomListener=new RoomListener() {
            @Override
            public void responseDeleteRoomResult() {
                super.responseDeleteRoomResult();
                int result = mRoomManager.deleteRoom(mRoomName);
                Log.i(TAG, "删除房间，影响的行数=" + result);
                if (result > 0) {
                    startActivity(new Intent(ManageRoomActivity.this, RoomActivity.class));
                    Toast.makeText(ManageRoomActivity.this, "删除房间成功", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ManageRoomActivity.this, "删除房间失败", Toast.LENGTH_SHORT).show();
                }
            }
        };
    }

    private void initEvents() {
        button_delete_room.setOnClickListener(this);
        layout_room_name.setOnClickListener(this);

    }

    private void initViews() {
        layout_title= findViewById(R.id.layout_title);
        button_delete_room = findViewById(R.id.button_delete_room);
        textview_room_name = findViewById(R.id.textview_room_name);
        layout_room_name = findViewById(R.id.layout_room_name);
    }
    private RoomListener mRoomListener;
    @Override
    protected void onPause() {
        super.onPause();
        mRoomManager.removeRoomListener(mRoomListener);
    }
    private boolean isUserLogin;
    @Override
    protected void onResume() {
        super.onResume();
        isUserLogin = Perfence.getBooleanPerfence(AppConstant.USER_LOGIN);
        mRoomName = mRoomManager.getCurrentSelectedRoom().getRoomName();
        Log.i(TAG, "当前编辑的房间名称= " + mRoomName);
        textview_room_name.setText(mRoomName);
        mRoomManager.addRoomListener(mRoomListener);
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {

            case R.id.button_delete_room:
                if (!NetUtil.isNetAvailable(this)) {
                    ToastSingleShow.showText(this, "无可用网络连接,请检查网络");
                    return;
                }
                if (! isUserLogin) {
                    ToastSingleShow.showText(this, "用户未登录");
                    return;
                }
                if (mRoomName != null) {
                    new AlertDialog(ManageRoomActivity.this).builder().setTitle("删除房间")
                            .setMsg("确定删除房间(" + mRoomName + ")?")
                            .setPositiveButton("确认", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    String uid = mRoomManager.findRoom(mRoomName, true).getUid();
                                    Log.i(TAG, "删除房间,UID=" + uid);
                                    if (uid == null) {
                                        return;
                                    }
                                    mRoomManager.deleteRoomHttp(uid);
                                }
                            }).setNegativeButton("取消", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                        }
                    }).show();
                }
                break;
            case R.id.layout_room_name:
                intent = new Intent(this, ModifyRoomNameActivity.class);
                startActivity(intent);
                break;
        }
    }

}
