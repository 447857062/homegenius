package com.deplink.boruSmart.activity.device;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.deplink.boruSmart.Protocol.json.Room;
import com.deplink.boruSmart.activity.device.doorbell.EditDoorbellActivity;
import com.deplink.boruSmart.activity.device.getway.GetwayDeviceActivity;
import com.deplink.boruSmart.activity.device.light.LightEditActivity;
import com.deplink.boruSmart.activity.device.remoteControl.realRemoteControl.RemoteControlActivity;
import com.deplink.boruSmart.activity.device.router.RouterSettingActivity;
import com.deplink.boruSmart.activity.device.smartSwitch.EditActivity;
import com.deplink.boruSmart.activity.device.smartlock.EditSmartLockActivity;
import com.deplink.boruSmart.activity.room.AddRommActivity;
import com.deplink.boruSmart.activity.room.adapter.ContentAdapter;
import com.deplink.boruSmart.constant.DeviceTypeConstant;
import com.deplink.boruSmart.manager.device.DeviceManager;
import com.deplink.boruSmart.manager.room.RoomManager;
import com.deplink.boruSmart.view.combinationwidget.TitleLayout;

import java.util.List;

import deplink.com.smartwirelessrelay.homegenius.EllESDK.R;

public class SelectRommActivity extends AppCompatActivity {
    private static final String TAG = "RoomActivity";
    private RoomManager mRoomManager;
    private TextView textview_show_select_room;
    private String deviceType;
    private RecyclerView mContentRv;
    private ContentAdapter mContentAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_device);
        initViews();
        initDatas();
        initEvents();
    }

    private boolean isStartFromExperience;
    private TitleLayout layout_title;
    private String currentAddDevice;

    private void initDatas() {
        mRoomManager = RoomManager.getInstance();
        mRoomManager.initRoomManager(this);
        layout_title.setReturnClickListener(new TitleLayout.ReturnImageClickListener() {
            @Override
            public void onBackPressed() {
                SelectRommActivity.this.onBackPressed();
            }
        });
        layout_title.setEditImageClickListener(new TitleLayout.EditImageClickListener() {
            @Override
            public void onEditImagePressed() {
                Intent intent = new Intent(SelectRommActivity.this, AddRommActivity.class);
                intent.putExtra("fromAddDevice", true);
                startActivity(intent);
            }
        });
        layout_title.setBackImageResource(R.drawable.returnicon);
        isStartFromExperience = DeviceManager.getInstance().isStartFromExperience();
        textview_show_select_room.setText("请选择设备所在的房间");
        layout_title.setEditTextVisiable(false);
        deviceType = getIntent().getStringExtra("DeviceType");
        //getintent data
        currentAddDevice = getIntent().getStringExtra("currentAddDevice");
    }

    private void initEvents() {

    }

    private void initViews() {
        textview_show_select_room = (TextView) findViewById(R.id.textview_show_select_room);
        layout_title = (TitleLayout) findViewById(R.id.layout_title);
        mContentRv = (RecyclerView) findViewById(R.id.rv_content);
        mContentRv.setLayoutManager(new LinearLayoutManager(this));
        mContentAdapter = new ContentAdapter(this,false);
        mContentAdapter.setItemListener(new ContentAdapter.onRecyclerItemClickerListener() {
            @Override
            public void onRecyclerItemClick(View view, Object data, int position) {
                Room currentSelectedRoom = RoomManager.getInstance().getmRooms().get(position);
                String currentAddRomm = currentSelectedRoom.getRoomName();
                RoomManager.getInstance().setCurrentSelectedRoom(currentSelectedRoom);
                if (isStartFromExperience) {
                    if (DeviceManager.getInstance().isEditDevice()
                            && DeviceManager.getInstance().getCurrentEditDeviceType().equals(DeviceTypeConstant.TYPE.TYPE_ROUTER)) {
                        Intent intentSeleteedRoom = new Intent(SelectRommActivity.this, RouterSettingActivity.class);
                        intentSeleteedRoom.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intentSeleteedRoom.putExtra("roomName", currentAddRomm);
                        intentSeleteedRoom.putExtra("isupdateroom", true);
                        startActivity(intentSeleteedRoom);
                    } else {
                        Intent mIntent = new Intent();
                        mIntent.putExtra("roomName", currentAddRomm);
                        // 设置结果，并进行传送
                        setResult(RESULT_OK, mIntent);
                        finish();
                    }
                } else {
                    if (DeviceManager.getInstance().isEditDevice()) {
                        DeviceManager.getInstance().setEditDevice(false);
                        Intent intentSeleteedRoom;
                        switch (DeviceManager.getInstance().getCurrentEditDeviceType()) {
                            case DeviceTypeConstant.TYPE.TYPE_LIGHT:
                                intentSeleteedRoom = new Intent(SelectRommActivity.this, LightEditActivity.class);
                                intentSeleteedRoom.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                intentSeleteedRoom.putExtra("roomName", currentAddRomm);
                                intentSeleteedRoom.putExtra("isupdateroom", true);
                                startActivity(intentSeleteedRoom);
                                break;
                            case DeviceTypeConstant.TYPE.TYPE_LOCK:
                                intentSeleteedRoom = new Intent();
                                intentSeleteedRoom.setClass(SelectRommActivity.this, EditSmartLockActivity.class);
                                intentSeleteedRoom.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                intentSeleteedRoom.putExtra("roomName", currentAddRomm);
                                intentSeleteedRoom.putExtra("isupdateroom", true);
                                startActivity(intentSeleteedRoom);
                                break;
                            case DeviceTypeConstant.TYPE.TYPE_REMOTECONTROL:
                                intentSeleteedRoom = new Intent(SelectRommActivity.this, RemoteControlActivity.class);
                                intentSeleteedRoom.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                intentSeleteedRoom.putExtra("roomName", currentAddRomm);
                                intentSeleteedRoom.putExtra("isupdateroom", true);
                                startActivity(intentSeleteedRoom);
                                break;
                            case DeviceTypeConstant.TYPE.TYPE_SWITCH:
                                intentSeleteedRoom = new Intent(SelectRommActivity.this, EditActivity.class);
                                intentSeleteedRoom.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                intentSeleteedRoom.putExtra("roomName", currentAddRomm);
                                intentSeleteedRoom.putExtra("isupdateroom", true);
                                startActivity(intentSeleteedRoom);
                                break;
                            case DeviceTypeConstant.TYPE.TYPE_SMART_GETWAY:
                                intentSeleteedRoom = new Intent(SelectRommActivity.this, GetwayDeviceActivity.class);
                                intentSeleteedRoom.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                intentSeleteedRoom.putExtra("roomName", currentAddRomm);
                                intentSeleteedRoom.putExtra("isupdateroom", true);
                                startActivity(intentSeleteedRoom);
                                break;
                            case DeviceTypeConstant.TYPE.TYPE_MENLING:
                                intentSeleteedRoom = new Intent(SelectRommActivity.this, EditDoorbellActivity.class);
                                intentSeleteedRoom.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                intentSeleteedRoom.putExtra("roomName", currentAddRomm);
                                intentSeleteedRoom.putExtra("isupdateroom", true);
                                startActivity(intentSeleteedRoom);
                                break;
                            case DeviceTypeConstant.TYPE.TYPE_ROUTER:
                                intentSeleteedRoom = new Intent(SelectRommActivity.this, RouterSettingActivity.class);
                                intentSeleteedRoom.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                intentSeleteedRoom.putExtra("roomName", currentAddRomm);
                                intentSeleteedRoom.putExtra("isupdateroom", true);
                                startActivity(intentSeleteedRoom);
                                break;
                        }
                    } else {
                        Intent intent = new Intent(SelectRommActivity.this, AddDeviceNameActivity.class);
                        intent.putExtra("DeviceType", deviceType);
                        intent.putExtra("currentAddDevice", currentAddDevice);
                        startActivity(intent);
                    }

                }
            }

        });
        mContentRv.setAdapter(mContentAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        List<Room> mRooms = mRoomManager.queryRooms();
        //房间适配器
        mContentAdapter.setData(mRooms);
        mContentAdapter.notifyDataSetChanged();
    }
}
