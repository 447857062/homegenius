package com.deplink.boruSmart.activity.room;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.deplink.boruSmart.Protocol.json.Room;
import com.deplink.boruSmart.activity.device.AddDeviceActivity;
import com.deplink.boruSmart.activity.personal.login.LoginActivity;
import com.deplink.boruSmart.activity.room.adapter.ContentAdapterAddRoom;
import com.deplink.boruSmart.constant.AppConstant;
import com.deplink.boruSmart.constant.RoomConstant;
import com.deplink.boruSmart.manager.room.RoomListener;
import com.deplink.boruSmart.manager.room.RoomManager;
import com.deplink.boruSmart.util.NetUtil;
import com.deplink.boruSmart.util.Perfence;
import com.deplink.boruSmart.util.WeakRefHandler;
import com.deplink.boruSmart.view.dialog.AlertDialog;
import com.deplink.boruSmart.view.edittext.ClearEditText;
import com.deplink.boruSmart.view.toast.ToastSingleShow;
import com.deplink.sdk.android.sdk.DeplinkSDK;
import com.deplink.sdk.android.sdk.EventCallback;
import com.deplink.sdk.android.sdk.SDKAction;
import com.deplink.sdk.android.sdk.manager.SDKManager;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

import deplink.com.smartwirelessrelay.homegenius.EllESDK.R;

public class AddRommActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "AddRommActivity";
    private TextView textview_add_room_complement;
    private ImageView image_back;
    private ClearEditText edittext_room_name;
    private RoomManager roomManager;
    private List<String> listTop = new ArrayList<>();
    private SDKManager manager;
    private EventCallback ec;
    private RecyclerView mContentRv;
    private boolean isUserLogin;
    private ContentAdapterAddRoom mContentAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_romm);
        initViews();
        initDatas();
        initEvents();
    }

    private boolean fromAddDevice;
    private RoomListener mRoomListener;

    private void initDatas() {
        roomManager = RoomManager.getInstance();
        roomManager.initRoomManager(this);
        listTop.add(RoomConstant.ROOMTYPE.TYPE_LIVING);
        listTop.add(RoomConstant.ROOMTYPE.TYPE_BED);
        listTop.add(RoomConstant.ROOMTYPE.TYPE_KITCHEN);
        listTop.add(RoomConstant.ROOMTYPE.TYPE_STUDY);
        listTop.add(RoomConstant.ROOMTYPE.TYPE_STORAGE);
        listTop.add(RoomConstant.ROOMTYPE.TYPE_TOILET);
        listTop.add(RoomConstant.ROOMTYPE.TYPE_DINING);
        roomType = RoomConstant.ROOMTYPE.TYPE_LIVING;
        mContentAdapter.setData(listTop);
        edittext_room_name.setText(roomType);
        edittext_room_name.setSelection(roomType.length());
        fromAddDevice = getIntent().getBooleanExtra("fromAddDevice", false);
        mRoomListener = new RoomListener() {
            @Override
            public void responseAddRoomResult(String result) {
                super.responseAddRoomResult(result);
                boolean addDbResult = roomManager.addRoom(roomType, roomName, result);
                if (addDbResult) {
                    mHandler.sendEmptyMessage(MSG_ADD_ROOM_SUCCESS);
                    finish();
                }
            }
        };
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
            public void onFailure(SDKAction action, Throwable throwable) {


            }
            @Override
            public void connectionLost(Throwable throwable) {
                super.connectionLost(throwable);
                mHandler.sendEmptyMessage(MSG_SHOW_CONNECT_LOST);

            }
        };
    }

    private void initEvents() {
        textview_add_room_complement.setOnClickListener(this);
        image_back.setOnClickListener(this);
    }

    private void initViews() {
        textview_add_room_complement = (TextView) findViewById(R.id.textview_add_room_complement);
        image_back = (ImageView) findViewById(R.id.image_back);
        edittext_room_name = (ClearEditText) findViewById(R.id.edittext_room_name);
        mContentRv = (RecyclerView) findViewById(R.id.rv_content);
        mContentRv.setLayoutManager(new LinearLayoutManager(this));
        mContentAdapter = new ContentAdapterAddRoom(this);
        mContentAdapter.setItemListener(new ContentAdapterAddRoom.onRecyclerItemClickerListener() {
            @Override
            public void onRecyclerItemClick(View view, Object data, int position) {
                roomType = listTop.get(position);
                edittext_room_name.setText(roomType);
                //拿适配器调用适配器内部自定义好的setThisPosition方法（参数写点击事件的参数的position）
                mContentAdapter.setThisPosition(position);
                //嫑忘记刷新适配器
                mContentAdapter.notifyDataSetChanged();
                edittext_room_name.setSelection(roomType.length());
            }
        });
        mContentRv.setAdapter(mContentAdapter);
    }

    private String roomType;
    private static final int MSG_ADD_ROOM_FAILED = 100;
    private static final int MSG_ADD_ROOM_SUCCESS = 101;
    private static final int MSG_SHOW_CONNECT_LOST=102;
    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_ADD_ROOM_FAILED:
                    Toast.makeText(AddRommActivity.this, "添加房间失败，已存在同名房间", Toast.LENGTH_LONG).show();
                    break;
                case MSG_ADD_ROOM_SUCCESS:
                    Toast.makeText(AddRommActivity.this, "添加房间成功", Toast.LENGTH_LONG).show();
                    if (fromAddDevice) {
                        RoomManager.getInstance().setCurrentSelectedRoom(null);
                        Intent intent = new Intent(AddRommActivity.this, AddDeviceActivity.class);
                        startActivity(intent);
                    }
                    break;
                case MSG_SHOW_CONNECT_LOST:
                    Perfence.setPerfence(AppConstant.USER_LOGIN, false);
                    isUserLogin=false;
                    new AlertDialog(AddRommActivity.this).builder().setTitle("账号异地登录")
                            .setMsg("当前账号已在其它设备上登录,是否重新登录")
                            .setPositiveButton("确认", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    startActivity(new Intent(AddRommActivity.this, LoginActivity.class));
                                }
                            }).setNegativeButton("取消", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                        }
                    }).show();
                    break;
            }
            return true;
        }
    };
    private Handler mHandler = new WeakRefHandler(mCallback);
    @Override
    protected void onResume() {
        super.onResume();
        manager.addEventCallback(ec);
        isUserLogin = Perfence.getBooleanPerfence(AppConstant.USER_LOGIN);
        roomManager.addRoomListener(mRoomListener);
    }
    private String roomName;
    @Override
    protected void onPause() {
        super.onPause();
        manager.removeEventCallback(ec);
        roomManager.removeRoomListener(mRoomListener);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //完成
            case R.id.textview_add_room_complement:
                roomName = edittext_room_name.getText().toString();
                if (!NetUtil.isNetAvailable(this)) {
                    ToastSingleShow.showText(this, "无可用网络连接,请检查网络");
                    return;
                }
                if (!isUserLogin) {
                    ToastSingleShow.showText(this, "用户未登录");
                    return;
                }
                if (roomName.equals("")) {
                    Toast.makeText(AddRommActivity.this, "请输入房间名称", Toast.LENGTH_SHORT).show();
                    return;
                }
                int sort_num = DataSupport.findAll(Room.class, true).size();
                Room room = DataSupport.where("roomName = ?", roomName).findFirst(Room.class);
                if(room!=null){
                    mHandler.sendEmptyMessage(MSG_ADD_ROOM_FAILED);
                    return;
                }else{
                    roomManager.addRoomHttp(roomName, roomType, sort_num - 1);
                }
                break;
            case R.id.image_back:
                onBackPressed();
                break;
            default:
                break;
        }
    }

}
