    package com.deplink.boruSmart.activity.device.doorbell;

    import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.deplink.boruSmart.constant.AppConstant;
import com.deplink.boruSmart.manager.device.DeviceManager;
import com.deplink.boruSmart.manager.device.doorbeel.DoorBellListener;
import com.deplink.boruSmart.manager.device.doorbeel.DoorbeelManager;
import com.deplink.boruSmart.util.Perfence;
import com.deplink.boruSmart.util.WeakRefHandler;
import com.deplink.boruSmart.view.combinationwidget.TitleLayout;
import com.deplink.boruSmart.view.dialog.AlertDialog;
import com.deplink.boruSmart.view.listview.swipemenulistview.SwipeMenu;
import com.deplink.boruSmart.view.listview.swipemenulistview.SwipeMenuCreator;
import com.deplink.boruSmart.view.listview.swipemenulistview.SwipeMenuItem;
import com.deplink.boruSmart.view.listview.swipemenulistview.SwipeMenuListView;
import com.deplink.boruSmart.view.toast.Ftoast;
import com.deplink.sdk.android.sdk.DeplinkSDK;
import com.deplink.sdk.android.sdk.EventCallback;
import com.deplink.sdk.android.sdk.SDKAction;
import com.deplink.sdk.android.sdk.json.homegenius.DoorBellItem;
import com.deplink.sdk.android.sdk.manager.SDKManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import deplink.com.smartwirelessrelay.homegenius.EllESDK.R;

    public class VistorHistoryActivity extends Activity {
        private DoorbeelManager mDoorbeelManager;
        private SwipeMenuListView listview_vistor_list;
        private boolean isStartFromExperience;
        private List<DoorBellItem> visitorList;
        private HashMap<Integer, Bitmap> visitorListImage = new HashMap<>();
        private DoorBellListener mDoorBellListener;
        private VisitorListAdapter mAdapter;
        private RelativeLayout layout_no_visitor;
        private SDKManager manager;
        private EventCallback ec;
        private boolean isUserLogin;
        private TextView textview_visitor_loading;
        private TitleLayout layout_title;
        private ScrollView scroll_wrap;
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_vistor_history);
            initViews();
            initDatas();
            initEvents();
        }
        @Override
        protected void onResume() {
            super.onResume();
            isUserLogin = Perfence.getBooleanPerfence(AppConstant.USER_LOGIN);
            isStartFromExperience = DeviceManager.getInstance().isStartFromExperience();
            mDoorbeelManager.addDeviceListener(mDoorBellListener);
            manager.addEventCallback(ec);
            if (!isStartFromExperience) {
                if (isUserLogin) {
                    if(visitorListImage.size()==0){
                        mDoorbeelManager.getDoorbellHistory();
                    }
                } else {
                    Ftoast.create(VistorHistoryActivity.this).setText("未登录").show();
                }
            } else {
                layout_no_visitor.setVisibility(View.VISIBLE);
                textview_visitor_loading.setVisibility(View.GONE);
            }
        }

        @Override
        protected void onPause() {
            super.onPause();
            mDoorbeelManager.removeDeviceListener(mDoorBellListener);
            manager.removeEventCallback(ec);
        }
        private void initEvents() {
            if (!isStartFromExperience) {
                listview_vistor_list.setAdapter(mAdapter);
                listview_vistor_list.setOnTouchListener(new View.OnTouchListener() {

                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        if (event.getAction()==MotionEvent.ACTION_UP) {
                            scroll_wrap.requestDisallowInterceptTouchEvent(false);
                        }else {
                            scroll_wrap.requestDisallowInterceptTouchEvent(true);
                        }
                        return false;
                    }
                });
                SwipeMenuCreator creator = new SwipeMenuCreator() {

                    @Override
                    public void create(SwipeMenu menu) {
                        SwipeMenuItem deleteItem = new SwipeMenuItem(
                                getApplicationContext());
                        deleteItem.setBackground(new ColorDrawable(ContextCompat.getColor(
                                VistorHistoryActivity.this,R.color.delete_button)));
                        deleteItem.setWidth((int) Perfence.dp2px(VistorHistoryActivity.this, 70));
                        deleteItem.setTitle("删除");
                        deleteItem.setTitleSize(14);
                        // set item title font color
                        deleteItem.setTitleColor(Color.WHITE);
                        menu.addMenuItem(deleteItem);
                    }
                };
                // set creator
                listview_vistor_list.setMenuCreator(creator);
                listview_vistor_list.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(final int position, SwipeMenu menu, int index) {
                                new AlertDialog(VistorHistoryActivity.this).builder().setTitle("删除记录")
                                        .setMsg("删除访问记录?")
                                        .setPositiveButton("确认", new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                if (!isStartFromExperience) {
                                                    mDoorbeelManager.deleteDoorbellVistorImage(visitorList.get(position).getFile());
                                                    visitorList.remove(position);
                                                    mAdapter.notifyDataSetChanged();
                                                }
                                            }
                                        }).setNegativeButton("取消", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                    }
                                }).show();
                        return false;
                    }
                });
                listview_vistor_list.setOnItemClickListener(deviceItemClickListener);
            }
        }

        private Handler.Callback mCallback = new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG_TOAST_DELETE_RECORD_RESULT:
                       /* if ((boolean) msg.obj) {
                            Ftoast.create(VistorHistoryActivity.this).setText("删除访客记录成功").show();
                        } else {
                            Ftoast.create(VistorHistoryActivity.this).setText("删除访客记录失败").show();
                        }*/
                        break;
                    case MSG_GET_ONE_PHOTO:
                        int count=msg.arg1;
                        Bitmap bitmap= (Bitmap) msg.obj;
                        if (count <= visitorList.size() ) {
                            if(visitorListImage.get(count)==null){
                                visitorListImage.put(count,toRoundCorner(bitmap,30));
                            }
                        }
                        if(count% 5==0){
                            mAdapter.notifyDataSetChanged();
                        }
                        break;
                    case MSG_GET_PHOTO_LIST:
                        List<DoorBellItem> list= (List<DoorBellItem>) msg.obj;
                        if (list != null) {
                            Collections.sort(list, new Comparator<DoorBellItem>() {
                                @Override
                                public int compare(DoorBellItem o1, DoorBellItem o2) {
                                    //compareTo就是比较两个值，如果前者大于后者，返回1，等于返回0，小于返回-1
                                    if (o1.getTimestamp() == o2.getTimestamp()) {
                                        return 0;
                                    }
                                    if (o1.getTimestamp() > o2.getTimestamp()) {
                                        return -1;
                                    }
                                    if (o1.getTimestamp() < o2.getTimestamp()) {
                                        return 1;
                                    }
                                    return 0;
                                }
                            });
                            visitorList.clear();
                            visitorList.addAll(list);
                            visitorListImage.clear();
                            for (int i = 0; i < visitorList.size(); i++) {
                                mDoorbeelManager.getDoorbellVistorImage(list.get(i).getFile(), i);
                            }
                            if (visitorList.size() > 0) {
                                layout_no_visitor.setVisibility(View.GONE);
                                textview_visitor_loading.setVisibility(View.GONE);
                            } else {
                                layout_no_visitor.setVisibility(View.VISIBLE);
                                textview_visitor_loading.setVisibility(View.GONE);
                            }
                            mAdapter.notifyDataSetChanged();
                        }
                        break;
                }
                return true;
            }
        };
        public static final int MSG_TOAST_DELETE_RECORD_RESULT = 100;
        public static final int MSG_GET_ONE_PHOTO = 101;
        public static final int MSG_GET_PHOTO_LIST = 102;
        private Handler mHandler = new WeakRefHandler(mCallback);
        private AdapterView.OnItemClickListener deviceItemClickListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                Intent intent = new Intent(VistorHistoryActivity.this, DoorbellLargeImage.class);
                intent.putExtra("file", visitorList.get(position).getFile());
                startActivity(intent);
            }
        };
        private void initDatas() {
            layout_title.setReturnClickListener(new TitleLayout.ReturnImageClickListener() {
                @Override
                public void onBackPressed() {
                    VistorHistoryActivity.this.finish();
                }
            });
            mDoorbeelManager = DoorbeelManager.getInstance();
            mDoorbeelManager.InitDoorbeelManager(this);
            visitorList = new ArrayList<>();
            mDoorBellListener = new DoorBellListener() {
                @Override
                public void responseVisitorListResult(List<DoorBellItem> list) {
                    super.responseVisitorListResult(list);
                    Message msg=Message.obtain();
                    msg.what=MSG_GET_PHOTO_LIST;
                    msg.obj=list;
                    mHandler.sendMessage(msg);

                }

                @Override
                public void responseVisitorImage(Bitmap bitmap, int count) {
                    super.responseVisitorImage(bitmap, count);
                    Message msg=Message.obtain();
                    msg.what=MSG_GET_ONE_PHOTO;
                    msg.obj=bitmap;
                    msg.arg1=count;
                    mHandler.sendMessage(msg);

                }

                @Override
                public void responseDeleteRecordHistory(boolean success) {
                    super.responseDeleteRecordHistory(success);
                  /*  Message msg = Message.obtain();
                    msg.what = MSG_TOAST_DELETE_RECORD_RESULT;
                    msg.obj = success;
                    mHandler.sendMessage(msg);*/
                }
            };
            mAdapter = new VisitorListAdapter(this, visitorList, visitorListImage);
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
                public void deviceOpSuccess(String op, final String deviceKey) {
                    super.deviceOpSuccess(op, deviceKey);

                }

                @Override
                public void connectionLost(Throwable throwable) {
                    super.connectionLost(throwable);
                    mAdapter.notifyDataSetChanged();
                    isUserLogin = false;
                }

                @Override
                public void onFailure(SDKAction action, Throwable throwable) {
                }
            };
        }
       /* @Override
        public void finish() {
            super.finish();
            //注释掉activity本身的过渡动画
            overridePendingTransition(R.anim.in_right_offset, R.anim.out_left_offset);
        }*/
        /**
         * 获取圆角位图的方法
         *
         * @param bitmap 需要转化成圆角的位图
         * @param pixels 圆角的度数，数值越大，圆角越大
         * @return 处理后的圆角位图
         */
        public Bitmap toRoundCorner(Bitmap bitmap, int pixels) {
            Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                    bitmap.getHeight(), Bitmap.Config.ARGB_4444);
            Canvas canvas = new Canvas(output);
            int color = 0xff424242;
            Paint paint = new Paint();
            Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
            RectF rectF = new RectF(rect);
            paint.setAntiAlias(true);
            canvas.drawARGB(0, 0, 0, 0);
            paint.setColor(color);
            canvas.drawRoundRect(rectF, (float) pixels, (float) pixels, paint);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            canvas.drawBitmap(bitmap, rect, rect, paint);
            return output;
        }
        private void initViews() {
            listview_vistor_list = findViewById(R.id.listview_vistor_list);
            layout_no_visitor = findViewById(R.id.layout_no_visitor);
            textview_visitor_loading = findViewById(R.id.textview_visitor_loading);
            layout_title = findViewById(R.id.layout_title);
            scroll_wrap = findViewById(R.id.scroll_wrap);
        }
    }
