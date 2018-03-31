package com.deplink.boruSmart.activity.room;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.deplink.boruSmart.Protocol.json.Room;
import com.deplink.boruSmart.constant.AppConstant;
import com.deplink.boruSmart.manager.room.RoomListener;
import com.deplink.boruSmart.manager.room.RoomManager;
import com.deplink.boruSmart.util.NetUtil;
import com.deplink.boruSmart.util.Perfence;
import com.deplink.boruSmart.view.combinationwidget.TitleLayout;
import com.deplink.boruSmart.view.dialog.AlertDialog;
import com.deplink.boruSmart.view.edittext.ClearEditText;
import com.deplink.boruSmart.view.toast.ToastSingleShow;

import org.litepal.crud.DataSupport;

import deplink.com.smartwirelessrelay.homegenius.EllESDK.R;

public class ManageRoomActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "ManageRoomActivity";
    private Button button_delete_room;
    private String mRoomName;
    private RoomManager mRoomManager;
    private TitleLayout layout_title;
    private ClearEditText edittext_input_devie_name;
    private String roomName;
    private String orgRoomName;
    private boolean isLogin;
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
        layout_title.setEditTextClickListener(new TitleLayout.EditTextClickListener() {
            @Override
            public void onEditTextPressed() {
                roomName = edittext_input_devie_name.getText().toString();
                if (isLogin) {
                    if (!roomName.equalsIgnoreCase("")  ) {
                        if(!roomName.equals(orgRoomName)){
                            //查询看看有没有重名的
                            Room room = DataSupport.where("roomName = ?", roomName).findFirst(Room.class);
                            if(room!=null){
                                ToastSingleShow.showText(ManageRoomActivity.this,"已存在同名的房间,修改房间名称失败");
                                return;
                            }else{
                                String roomUid = mRoomManager.getCurrentSelectedRoom().getUid();
                                int roomOrdinalNumber=mRoomManager.getCurrentSelectedRoom().getRoomOrdinalNumber();
                                Log.i(TAG, "roomUid=" + roomUid);
                                mRoomManager.updateRoomNameHttp(roomUid, roomName,roomOrdinalNumber);
                            }
                        }else{
                            ManageRoomActivity.this.finish();
                        }
                    } else {
                        Toast.makeText(ManageRoomActivity.this, "请输入房间名称", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    ToastSingleShow.showText(ManageRoomActivity.this, "未登陆，请先登陆");
                }

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
            @Override
            public void responseUpdateRoomNameResult() {
                super.responseUpdateRoomNameResult();
                int result = RoomManager.getInstance().updateRoomName(orgRoomName, roomName);
                if (result != 1) {
                    Toast.makeText(ManageRoomActivity.this, "修改房间名称失败", Toast.LENGTH_SHORT).show();
                } else {
                    mRoomManager.getCurrentSelectedRoom().setRoomName(roomName);
                    Intent intent = new Intent(ManageRoomActivity.this, DeviceNumberActivity.class);
                    startActivity(intent);
                }
            }
        };
    }

    private void initEvents() {
        button_delete_room.setOnClickListener(this);
    }

    private void initViews() {
        layout_title= findViewById(R.id.layout_title);
        button_delete_room = findViewById(R.id.button_delete_room);
        edittext_input_devie_name = findViewById(R.id.edittext_input_devie_name);
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
        isLogin = Perfence.getBooleanPerfence(AppConstant.USER_LOGIN);
        orgRoomName = RoomManager.getInstance().getCurrentSelectedRoom().getRoomName();
        edittext_input_devie_name.setText(orgRoomName);
        edittext_input_devie_name.setSelection(orgRoomName.length());
        Log.i(TAG, "当前编辑的房间名称= " + mRoomName);
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
        }
    }

}
