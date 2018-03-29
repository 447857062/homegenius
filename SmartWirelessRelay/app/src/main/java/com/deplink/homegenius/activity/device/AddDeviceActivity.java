package com.deplink.homegenius.activity.device;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import com.deplink.homegenius.Protocol.json.Room;
import com.deplink.homegenius.activity.device.adapter.AddDeviceGridViewAdapter;
import com.deplink.homegenius.activity.device.doorbell.EditDoorbellActivity;
import com.deplink.homegenius.activity.device.getway.GetwayDeviceActivity;
import com.deplink.homegenius.activity.device.light.LightEditActivity;
import com.deplink.homegenius.activity.device.remoteControl.realRemoteControl.RemoteControlActivity;
import com.deplink.homegenius.activity.device.router.RouterSettingActivity;
import com.deplink.homegenius.activity.device.smartSwitch.EditActivity;
import com.deplink.homegenius.activity.device.smartlock.EditSmartLockActivity;
import com.deplink.homegenius.activity.room.AddRommActivity;
import com.deplink.homegenius.constant.DeviceTypeConstant;
import com.deplink.homegenius.manager.device.DeviceManager;
import com.deplink.homegenius.manager.room.RoomManager;
import com.deplink.homegenius.view.combinationwidget.TitleLayout;

import java.util.List;

import deplink.com.smartwirelessrelay.homegenius.EllESDK.R;

public class AddDeviceActivity extends Activity {
    private static final String TAG = "RoomActivity";
    private GridView mDragGridView;
    private AddDeviceGridViewAdapter mRoomsAdapter;
    private RoomManager mRoomManager;
    private TextView textview_show_select_room;
    private String deviceType;

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
                AddDeviceActivity.this.onBackPressed();
            }
        });
        isStartFromExperience = DeviceManager.getInstance().isStartFromExperience();
        textview_show_select_room.setText("请选择设备所在的房间");
        layout_title.setEditTextVisiable(false);
        deviceType = getIntent().getStringExtra("DeviceType");
        //getintent data
        currentAddDevice = getIntent().getStringExtra("currentAddDevice");
    }

    private void initEvents() {
        mDragGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //最大值，最后一个，添加房间
                if (position == mRoomsAdapter.getCount() - 1) {
                    Intent intent = new Intent(AddDeviceActivity.this, AddRommActivity.class);
                    intent.putExtra("fromAddDevice", true);
                    startActivity(intent);
                } else {
                    Room currentSelectedRoom = RoomManager.getInstance().getmRooms().get(position);
                    String currentAddRomm = currentSelectedRoom.getRoomName();
                    RoomManager.getInstance().setCurrentSelectedRoom(currentSelectedRoom);
                    if (isStartFromExperience) {
                        if (DeviceManager.getInstance().isEditDevice()
                                && DeviceManager.getInstance().getCurrentEditDeviceType().equals(DeviceTypeConstant.TYPE.TYPE_ROUTER) ) {
                            Intent intentSeleteedRoom = new Intent(AddDeviceActivity.this, RouterSettingActivity.class);
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
                        if(DeviceManager.getInstance().isEditDevice()){
                            DeviceManager.getInstance().setEditDevice(false);
                            Intent intentSeleteedRoom;
                            switch (DeviceManager.getInstance().getCurrentEditDeviceType()){
                                case DeviceTypeConstant.TYPE.TYPE_LIGHT:
                                     intentSeleteedRoom = new Intent(AddDeviceActivity.this, LightEditActivity.class);
                                    intentSeleteedRoom.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    intentSeleteedRoom.putExtra("roomName", currentAddRomm);
                                    intentSeleteedRoom.putExtra("isupdateroom", true);
                                    startActivity(intentSeleteedRoom);
                                    break;
                                case DeviceTypeConstant.TYPE.TYPE_LOCK:
                                     intentSeleteedRoom = new Intent();
                                    intentSeleteedRoom.setClass(AddDeviceActivity.this, EditSmartLockActivity.class);
                                    intentSeleteedRoom.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    intentSeleteedRoom.putExtra("roomName", currentAddRomm);
                                    intentSeleteedRoom.putExtra("isupdateroom", true);
                                    startActivity(intentSeleteedRoom);
                                    break;
                                case DeviceTypeConstant.TYPE.TYPE_REMOTECONTROL:
                                    intentSeleteedRoom = new Intent(AddDeviceActivity.this, RemoteControlActivity.class);
                                    intentSeleteedRoom.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    intentSeleteedRoom.putExtra("roomName", currentAddRomm);
                                    intentSeleteedRoom.putExtra("isupdateroom", true);
                                    startActivity(intentSeleteedRoom);
                                    break;
                                case DeviceTypeConstant.TYPE.TYPE_SWITCH:
                                    intentSeleteedRoom = new Intent(AddDeviceActivity.this, EditActivity.class);
                                    intentSeleteedRoom.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    intentSeleteedRoom.putExtra("roomName", currentAddRomm);
                                    intentSeleteedRoom.putExtra("isupdateroom", true);
                                    startActivity(intentSeleteedRoom);
                                    break;
                                case DeviceTypeConstant.TYPE.TYPE_SMART_GETWAY:
                                    intentSeleteedRoom = new Intent(AddDeviceActivity.this, GetwayDeviceActivity.class);
                                    intentSeleteedRoom.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    intentSeleteedRoom.putExtra("roomName", currentAddRomm);
                                    intentSeleteedRoom.putExtra("isupdateroom", true);
                                    startActivity(intentSeleteedRoom);
                                    break;
                                case DeviceTypeConstant.TYPE.TYPE_MENLING:
                                    intentSeleteedRoom = new Intent(AddDeviceActivity.this, EditDoorbellActivity.class);
                                    intentSeleteedRoom.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    intentSeleteedRoom.putExtra("roomName", currentAddRomm);
                                    intentSeleteedRoom.putExtra("isupdateroom", true);
                                    startActivity(intentSeleteedRoom);
                                    break;
                                case DeviceTypeConstant.TYPE.TYPE_ROUTER:
                                    intentSeleteedRoom = new Intent(AddDeviceActivity.this, RouterSettingActivity.class);
                                    intentSeleteedRoom.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    intentSeleteedRoom.putExtra("roomName", currentAddRomm);
                                    intentSeleteedRoom.putExtra("isupdateroom", true);
                                    startActivity(intentSeleteedRoom);
                                    break;
                            }
                        }else{
                            Intent intent = new Intent(AddDeviceActivity.this, AddDeviceNameActivity.class);
                            intent.putExtra("DeviceType", deviceType);
                            intent.putExtra("currentAddDevice", currentAddDevice);
                            startActivity(intent);
                        }

                    }
                }
            }
        });
    }

    private void initViews() {
        mDragGridView = findViewById(R.id.dragGridView);
        textview_show_select_room = findViewById(R.id.textview_show_select_room);
        layout_title = findViewById(R.id.layout_title);
    }

    @Override
    protected void onResume() {
        super.onResume();
        List<Room> mRooms = mRoomManager.queryRooms();
        mRoomsAdapter = new AddDeviceGridViewAdapter(this, mRooms);
        //房间适配器
        mDragGridView.setAdapter(mRoomsAdapter);
    }
}
