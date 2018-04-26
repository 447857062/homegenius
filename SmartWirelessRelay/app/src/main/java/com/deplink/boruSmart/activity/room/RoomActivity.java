package com.deplink.boruSmart.activity.room;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.deplink.boruSmart.Protocol.json.Room;
import com.deplink.boruSmart.activity.device.DevicesActivity;
import com.deplink.boruSmart.activity.homepage.SmartHomeMainActivity;
import com.deplink.boruSmart.activity.personal.PersonalCenterActivity;
import com.deplink.boruSmart.activity.personal.login.LoginActivity;
import com.deplink.boruSmart.activity.room.adapter.ContentAdapter;
import com.deplink.boruSmart.application.AppManager;
import com.deplink.boruSmart.constant.AppConstant;
import com.deplink.boruSmart.manager.room.RoomListener;
import com.deplink.boruSmart.manager.room.RoomManager;
import com.deplink.boruSmart.util.NetUtil;
import com.deplink.boruSmart.util.Perfence;
import com.deplink.boruSmart.util.WeakRefHandler;
import com.deplink.boruSmart.view.combinationwidget.TitleLayout;
import com.deplink.boruSmart.view.toast.Ftoast;
import com.deplink.sdk.android.sdk.DeplinkSDK;
import com.deplink.sdk.android.sdk.EventCallback;
import com.deplink.sdk.android.sdk.SDKAction;
import com.deplink.sdk.android.sdk.manager.SDKManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import deplink.com.smartwirelessrelay.homegenius.EllESDK.R;

public class RoomActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "RoomActivity";
    private LinearLayout layout_home_page;
    private LinearLayout layout_devices;
    private LinearLayout layout_personal_center;
    private RoomManager mRoomManager;
    private List<Room> mRooms = new ArrayList<>();
    private ImageView imageview_devices;
    private ImageView imageview_home_page;
    private ImageView imageview_rooms;
    private ImageView imageview_personal_center;
    private TextView textview_home;
    private TextView textview_device;
    private TextView textview_room;
    private TextView textview_mine;
    private static final int MSG_UPDATE_ROOM = 100;
    private SDKManager manager;
    private EventCallback ec;
    private boolean isUserLogin;
    private TitleLayout layout_title;
    private RecyclerView mContentRv;
    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_UPDATE_ROOM:
                    mRooms.clear();
                    mRooms.addAll(mRoomManager.queryRooms());
                    Log.i(TAG, "mRooms.size()" + mRooms.size());
                    mContentAdapter.setData(mRooms);
                    break;
            }
            return true;
        }
    };
    private Handler mHandler = new WeakRefHandler(mCallback);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);
        initViews();
        initDatas();
        initEvents();
    }
    /**
     * 再按一次退出应用
     */
    private long exitTime = 0;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if ((System.currentTimeMillis() - exitTime) > 2000) {
                Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                AppManager.getAppManager().finishAllActivity();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    private void initMqttCallback() {
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
                isUserLogin = false;

            }
        };
    }
    private RoomListener mRoomListener;
    @Override
    protected void onPause() {
        super.onPause();
        manager.removeEventCallback(ec);
        mRoomManager.removeRoomListener(mRoomListener);
        if (isUserLogin) {
            if (isRoomOrdinalNumberChanged) {
                if (NetUtil.isNetAvailable(this)) {
                    mRoomManager.updateRoomsOrdinalNumber(mRooms);
                } else {
                    Ftoast.create(this).setText("网络连接不可用").show();
                }
            }
        }
    }

    private void initDatas() {
        layout_title.setEditImageClickListener(new TitleLayout.EditImageClickListener() {
            @Override
            public void onEditImagePressed() {
                if (isUserLogin) {
                    startActivity(new Intent(RoomActivity.this, AddRommActivity.class));
                } else {
                    startActivity(new Intent(RoomActivity.this, LoginActivity.class));
                }

            }
        });
        mRoomManager = RoomManager.getInstance();
        mRoomManager.initRoomManager(this);
        mRoomListener = new RoomListener() {
            @Override
            public void responseQueryResultHttps(List<Room> result) {
                super.responseQueryResultHttps(result);
                Message msg = Message.obtain();
                msg.what = MSG_UPDATE_ROOM;
                mHandler.sendMessage(msg);
            }
        };
        initMqttCallback();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initButtomBar();
        isUserLogin = Perfence.getBooleanPerfence(AppConstant.USER_LOGIN);
        manager.addEventCallback(ec);
        mRoomManager.addRoomListener(mRoomListener);
        imageview_personal_center.setImageResource(R.drawable.nocheckthemine);
        mRooms.clear();
        mRooms.addAll(mRoomManager.queryRooms());
        Log.i(TAG, "mRooms.size()" + mRooms.size());
        mContentAdapter.setData(mRooms);
        mRoomManager.updateRooms();
    }

    /**
     * 初始化底部的导航栏
     */
    private void initButtomBar() {
        textview_home.setTextColor(ContextCompat.getColor(this, R.color.line_clolor));
        textview_device.setTextColor(ContextCompat.getColor(this, R.color.line_clolor));
        textview_room.setTextColor(ContextCompat.getColor(this, R.color.room_type_text));
        textview_mine.setTextColor(ContextCompat.getColor(this, R.color.line_clolor));
        imageview_home_page.setImageResource(R.drawable.nocheckthehome);
        imageview_devices.setImageResource(R.drawable.nocheckthedevice);
        imageview_rooms.setImageResource(R.drawable.checktheroom);
    }
    private boolean isRoomOrdinalNumberChanged;
    private void initEvents() {
        AppManager.getAppManager().addActivity(this);
        layout_home_page.setOnClickListener(this);
        layout_devices.setOnClickListener(this);
        layout_personal_center.setOnClickListener(this);
    }

    private ContentAdapter mContentAdapter;

    private void initViews() {
        textview_home = (TextView) findViewById(R.id.textview_home);
        textview_device = (TextView) findViewById(R.id.textview_device);
        textview_room = (TextView) findViewById(R.id.textview_room);
        textview_mine = (TextView) findViewById(R.id.textview_mine);
        layout_home_page = (LinearLayout) findViewById(R.id.layout_home_page);
        layout_devices = (LinearLayout) findViewById(R.id.layout_devices);
        layout_personal_center = (LinearLayout) findViewById(R.id.layout_personal_center);
        layout_title = (TitleLayout) findViewById(R.id.layout_title);
        imageview_devices = (ImageView) findViewById(R.id.imageview_devices);
        imageview_home_page = (ImageView) findViewById(R.id.imageview_home_page);
        imageview_rooms = (ImageView) findViewById(R.id.imageview_rooms);
        imageview_personal_center = (ImageView) findViewById(R.id.imageview_personal_center);
        mContentRv = (RecyclerView) findViewById(R.id.rv_content);
        mContentRv.setLayoutManager(new LinearLayoutManager(this));
        mContentAdapter = new ContentAdapter(this,true);
        mContentAdapter.setItemListener(new ContentAdapter.onRecyclerItemClickerListener() {
            @Override
            public void onRecyclerItemClick(View view, Object data, int position) {
                mRoomManager.setCurrentSelectedRoom(mRoomManager.getmRooms().get(position));
                Intent intent = new Intent(RoomActivity.this, DeviceNumberActivity.class);
                startActivity(intent);
            }
        });
        mContentRv.setAdapter(mContentAdapter);
        //1.创建item helper
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        //2.绑定到recyclerview上面去
        itemTouchHelper.attachToRecyclerView(mContentRv);
    }

    //itemHelper的回调
    ItemTouchHelper.Callback callback = new ItemTouchHelper.Callback() {

        /**
         * 官方文档的说明如下：
         * o control which actions user can take on each view, you should override getMovementFlags(RecyclerView, ViewHolder)
         * and return appropriate set of direction flags. (LEFT, RIGHT, START, END, UP, DOWN).
         * 返回我们要监控的方向，上下左右，我们做的是上下拖动，要返回都是UP和DOWN
         * 关键坑爹的是下面方法返回值只有1个，也就是说只能监控一个方向。
         * 不过点入到源码里面有惊喜。源码标记方向如下：
         *  public static final int UP = 1     0001
         *  public static final int DOWN = 1 << 1; （位运算：值其实就是2）0010
         *  public static final int LEFT = 1 << 2   左 值是3
         *  public static final int RIGHT = 1 << 3  右 值是8
         */
        @Override
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            //也就是说返回值是组合式的
            //makeMovementFlags (int dragFlags, int swipeFlags)，看下面的解释说明
            int swipFlag=0;
            //如果也监控左右方向的话，swipFlag=ItemTouchHelper.LEFT|ItemTouchHelper.RIGHT;
            int dragflag = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
            //等价于：0001&0010;多点触控标记触屏手指的顺序和个数也是这样标记哦
            return  makeMovementFlags(dragflag,swipFlag);

            /**
             * 备注：由getMovementFlags可以联想到setMovementFlags，不过文档么有这个方法，但是：
             * 有 makeMovementFlags (int dragFlags, int swipeFlags)
             * Convenience method to create movement flags.便捷方法创建moveMentFlag
             * For instance, if you want to let your items be drag & dropped vertically and swiped left to be dismissed,
             * you can call this method with: makeMovementFlags(UP | DOWN, LEFT);
             * 这个recyclerview的文档写的简直完美，示例代码都弄好了！！！
             * 如果你想让item上下拖动和左边滑动删除，应该这样用： makeMovementFlags(UP | DOWN, LEFT)
             */

            //拓展一下：如果只想上下的话：makeMovementFlags（UP | DOWN, 0）,标记方向的最小值1
        }



        /**
         * 官方文档的说明如下
         * If user drags an item, ItemTouchHelper will call onMove(recyclerView, dragged, target). Upon receiving this callback,
         * you should move the item from the old position (dragged.getAdapterPosition()) to new position (target.getAdapterPosition())
         * in your adapter and also call notifyItemMoved(int, int).
         * 拖动某个item的回调，在return前要更新item位置，调用notifyItemMoved（draggedPosition，targetPosition）
         * viewHolde:正在拖动item
         * target：要拖到的目标
         * @return true 表示消费事件
         */
        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            //直接按照文档来操作啊，这文档写得太给力了,简直完美！
            mContentAdapter.notifyItemMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());
            //注意这里有个坑的，itemView 都移动了，对应的数据也要移动
            Collections.swap(mRooms, viewHolder.getAdapterPosition(), target.getAdapterPosition());
            isRoomOrdinalNumberChanged = true;
            return true;
        }
        /**
         * 谷歌官方文档说明如下：
         * 这个看了一下主要是做左右拖动的回调
         * When a View is swiped, ItemTouchHelper animates it until it goes out of bounds, then calls onSwiped(ViewHolder, int).
         * At this point, you should update your adapter (e.g. remove the item) and call related Adapter#notify event.
         * @param viewHolder
         * @param direction
         */
        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            //暂不处理
        }


        /**
         * 官方文档如下：返回true 当前tiem可以被拖动到目标位置后，直接”落“在target上，其他的上面的tiem跟着“落”，
         * 所以要重写这个方法，不然只是拖动的tiem在动，target tiem不动，静止的
         * Return true if the current ViewHolder can be dropped over the the target ViewHolder.
         * @param recyclerView
         * @param current
         * @param target
         * @return
         */
        @Override
        public boolean canDropOver(RecyclerView recyclerView, RecyclerView.ViewHolder current, RecyclerView.ViewHolder target) {
            return true;
        }


        /**
         * 官方文档说明如下：
         * Returns whether ItemTouchHelper should start a drag and drop operation if an item is long pressed.
         * 是否开启长按 拖动
         * @return
         */
        @Override
        public boolean isLongPressDragEnabled() {
            //return true后，可以实现长按拖动排序和拖动动画了
            return true;
        }

    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.layout_home_page:
                startActivity(new Intent(this, SmartHomeMainActivity.class));
                break;
            case R.id.layout_devices:
                startActivity(new Intent(this, DevicesActivity.class));
                break;
            case R.id.layout_personal_center:
                startActivity(new Intent(this, PersonalCenterActivity.class));
                break;
        }
    }
}
