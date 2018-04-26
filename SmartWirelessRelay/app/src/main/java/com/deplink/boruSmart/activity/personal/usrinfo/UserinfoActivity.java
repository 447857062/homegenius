package com.deplink.boruSmart.activity.personal.usrinfo;

import android.app.Activity;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bigkoo.pickerview.TimePickerView;
import com.deplink.boruSmart.Protocol.json.Room;
import com.deplink.boruSmart.Protocol.json.device.SmartDev;
import com.deplink.boruSmart.Protocol.json.device.getway.GatwayDevice;
import com.deplink.boruSmart.Protocol.json.device.lock.Record;
import com.deplink.boruSmart.Protocol.json.device.router.Router;
import com.deplink.boruSmart.activity.personal.login.LoginActivity;
import com.deplink.boruSmart.constant.AppConstant;
import com.deplink.boruSmart.util.Perfence;
import com.deplink.boruSmart.util.WeakRefHandler;
import com.deplink.boruSmart.util.bitmap.BitmapHandler;
import com.deplink.boruSmart.view.combinationwidget.TitleLayout;
import com.deplink.boruSmart.view.dialog.ActionSheetDialog;
import com.deplink.boruSmart.view.dialog.AlertDialog;
import com.deplink.boruSmart.view.imageview.CircleImageView;
import com.deplink.boruSmart.view.toast.Ftoast;
import com.deplink.sdk.android.sdk.DeplinkSDK;
import com.deplink.sdk.android.sdk.EventCallback;
import com.deplink.sdk.android.sdk.SDKAction;
import com.deplink.sdk.android.sdk.homegenius.UserInfoAlertBody;
import com.deplink.sdk.android.sdk.manager.SDKManager;
import com.google.gson.Gson;
import com.zxy.tiny.Tiny;
import com.zxy.tiny.callback.FileCallback;
import com.zxy.tiny.core.FileKit;

import org.litepal.crud.DataSupport;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import deplink.com.smartwirelessrelay.homegenius.EllESDK.R;

public class UserinfoActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "UserinfoActivity";
    private RelativeLayout layout_user_header_image;
    private RelativeLayout layout_update_user_nickname;
    private RelativeLayout layout_update_sex;
    private RelativeLayout layout_birthday;
    private CircleImageView user_head_portrait;
    private TextView textview_show_birthday;
    private TextView textview_show_sex;
    private TextView textview_show_nicknamke;
    private SDKManager manager;
    private EventCallback ec;
    private TextView button_logout;
    private TitleLayout layout_title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userinfo);
        initViews();
        initDatas();
        initEvents();
    }

    private void initDatas() {
        layout_title.setReturnClickListener(new TitleLayout.ReturnImageClickListener() {
            @Override
            public void onBackPressed() {
                UserinfoActivity.this.onBackPressed();
            }
        });
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
            public void onGetImageSuccess(SDKAction action, final Bitmap bm) {
                //保存到本地
                try {
                    Log.i(TAG, "onGetImageSuccess");
                    user_head_portrait.setImageBitmap(bm);
                    saveToSDCard(bm);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onGetUserInfouccess(String info) {
                super.onGetUserInfouccess(info);
                Gson gson = new Gson();
                if (!info.equalsIgnoreCase("[]")) {
                    UserInfoAlertBody responseInfo = gson.fromJson(info, UserInfoAlertBody.class);
                    textview_show_nicknamke.setText(responseInfo.getNickname());
                    textview_show_sex.setText(responseInfo.getGender());
                    textview_show_birthday.setText(responseInfo.getBirthday());
                }
            }

            @Override
            public void onFailure(SDKAction action, Throwable throwable) {

            }

            @Override
            public void connectionLost(Throwable throwable) {
                super.connectionLost(throwable);
                isUserLogin = false;
                button_logout.setText("登录");
            }
        };
    }

    private void saveToSDCard(Bitmap bitmap) {
        String path = this.getFilesDir().getAbsolutePath();
        path = path + File.separator + "userIcon" + "userIcon.png";
        File dest = new File(path);
        try {
            FileOutputStream out = new FileOutputStream(dest);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isUserLogin;
    private boolean hasGetUserImage;
    private String userName;

    @Override
    protected void onResume() {
        super.onResume();
        manager.addEventCallback(ec);
        isUserLogin = Perfence.getBooleanPerfence(AppConstant.USER_LOGIN);
        if (isUserLogin) {
            hasGetUserImage = Perfence.getBooleanPerfence(AppConstant.USER.USER_GETIMAGE_FROM_SERVICE);
            if (!isOnActivityResult) {
                if (!hasGetUserImage) {
                    Perfence.setPerfence(AppConstant.USER.USER_GETIMAGE_FROM_SERVICE, true);
                    manager.getImage(Perfence.getPerfence(Perfence.PERFENCE_PHONE));
                } else {
                    setLocalImage(user_head_portrait);
                }
                userName = Perfence.getPerfence(Perfence.PERFENCE_PHONE);
                manager.getUserInfo(userName);
            }
            button_logout.setText("退出登录");
        } else {
            button_logout.setText("登录");
            user_head_portrait.setImageDrawable(getResources().getDrawable(R.drawable.defaultavatar));
        }
    }

    private boolean isOnActivityResult;

    @Override
    protected void onPause() {
        super.onPause();
        manager.removeEventCallback(ec);
        isOnActivityResult = false;
    }

    private void setLocalImage(CircleImageView user_head_portrait) {
        boolean isSdCardExist = Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);// 判断sdcard是否存在
        if (isSdCardExist) {
            String path = this.getFilesDir().getAbsolutePath();
            path = path + File.separator + "userIcon" + "userIcon.png";
            File file = new File(path);
            if (file.exists()) {
                Bitmap bm = BitmapFactory.decodeFile(file.getPath());
                // 将图片显示到ImageView中
                user_head_portrait.setImageBitmap(bm);
            } else {
                user_head_portrait.setImageDrawable(getResources().getDrawable(R.drawable.defaultavatar));
            }
        } else {
            Ftoast.create(this).setText("sd卡不存在！").show();
        }
    }

    private void initViews() {
        layout_title = (TitleLayout) findViewById(R.id.layout_title);
        textview_show_nicknamke = (TextView) findViewById(R.id.textview_show_nicknamke);
        layout_user_header_image = (RelativeLayout) findViewById(R.id.layout_user_header_image);
        layout_update_user_nickname = (RelativeLayout) findViewById(R.id.layout_update_user_nickname);
        layout_update_sex = (RelativeLayout) findViewById(R.id.layout_update_sex);
        layout_birthday = (RelativeLayout) findViewById(R.id.layout_birthday);
        user_head_portrait = (CircleImageView) findViewById(R.id.user_head_portrait);
        textview_show_birthday = (TextView) findViewById(R.id.textview_show_birthday);
        textview_show_sex = (TextView) findViewById(R.id.textview_show_sex);
        button_logout = (TextView) findViewById(R.id.button_logout);

    }

    private void initEvents() {
        layout_user_header_image.setOnClickListener(this);
        layout_update_user_nickname.setOnClickListener(this);
        layout_update_sex.setOnClickListener(this);
        layout_birthday.setOnClickListener(this);
        button_logout.setOnClickListener(this);
    }

    /**
     * 拍照选择图片
     */
    private void chooseFromCamera() {
        //构建隐式Intent
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //调用系统相机
        startActivityForResult(intent, CAMERA_CODE);
    }

    private void showImagePopup() {
        final Intent galleryIntent = new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.setAction(Intent.ACTION_PICK);
        final Intent chooserIntent = Intent.createChooser(galleryIntent, "Choose image");
        startActivityForResult(chooserIntent, 100);
    }

    /**
     * 拍照选取图片
     */
    private static final int CAMERA_CODE = 1;
    private static final int CROP_CODE = 3;
    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {

            return true;
        }
    };
    private Handler mHandler = new WeakRefHandler(mCallback);

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_logout:

                new AlertDialog(UserinfoActivity.this).builder().setTitle("退出登录")
                        .setMsg("确定退出登录?")
                        .setPositiveButton("确认", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                manager.logout();
                                Perfence.setPerfence(AppConstant.USER_LOGIN, false);
                                Perfence.setPerfence(Perfence.USER_PASSWORD, "");//重置登录密码
                                DataSupport.deleteAll(SmartDev.class);
                                DataSupport.deleteAll(GatwayDevice.class);
                                DataSupport.deleteAll(Room.class);
                                DataSupport.deleteAll(Record.class);
                                DataSupport.deleteAll(Router.class);
                                Intent intent = new Intent(UserinfoActivity.this, LoginActivity.class);
                                intent.putExtra("startfrom", "userinfoactivity");
                                startActivity(intent);

                            }
                        }).setNegativeButton("取消", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                }).show();

                break;
            case R.id.layout_update_user_nickname:
                if (isUserLogin) {
                    Intent intent = new Intent(this, UpdateNicknameActivity.class);
                    intent.putExtra("nickname", textview_show_nicknamke.getText().toString());
                    startActivity(intent);
                } else {
                    Ftoast.create(this).setText("未登录,登录后操作").show();
                }

                break;
            case R.id.layout_update_sex:
                if (isUserLogin) {
                    new ActionSheetDialog(UserinfoActivity.this)
                            .builder()
                            .setCancelable(true)
                            .setCanceledOnTouchOutside(true)
                            .addSheetItem("男", ActionSheetDialog.SheetItemColor.GRAY,
                                    new ActionSheetDialog.OnSheetItemClickListener() {
                                        @Override
                                        public void onClick(int which) {
                                            textview_show_sex.setText("男");
                                            UserInfoAlertBody body = new UserInfoAlertBody();
                                            body.setGender("男");
                                            manager.alertUserInfo(userName, body);
                                        }
                                    })
                            .addSheetItem("女", ActionSheetDialog.SheetItemColor.GRAY,
                                    new ActionSheetDialog.OnSheetItemClickListener() {
                                        @Override
                                        public void onClick(int which) {
                                            textview_show_sex.setText("女");
                                            UserInfoAlertBody body = new UserInfoAlertBody();
                                            body.setGender("女");
                                            manager.alertUserInfo(userName, body);
                                        }
                                    })
                            .show();
                } else {
                    Ftoast.create(this).setText("未登录,登录后操作").show();
                }
                break;
            case R.id.layout_birthday:
                if (isUserLogin) {
                    final Calendar calender = Calendar.getInstance();
                    TimePickerView pvTime = new TimePickerView.Builder(UserinfoActivity.this, new TimePickerView.OnTimeSelectListener() {
                        @Override
                        public void onTimeSelect(Date date2, View v) {//选中事件回调
                            String time = getTime(date2);
                            textview_show_birthday.setText(time);
                            UserInfoAlertBody body = new UserInfoAlertBody();
                            body.setBirthday(time);
                            manager.alertUserInfo(userName, body);

                        }
                    })
                            .setType(TimePickerView.Type.YEAR_MONTH_DAY)//默认全部显示
                            .setCancelText("取消")//取消按钮文字
                            .setSubmitText("确定")//确认按钮文字
                            .setContentSize(16)//滚轮文字大小
                            .setOutSideCancelable(true)//点击屏幕，点在控件外部范围时，是否取消显示
                            .isCyclic(true)//是否循环滚动
                            .setTextColorCenter(Color.BLACK)//设置选中项的颜色
                            .setSubmitColor(Color.BLACK)//确定按钮文字颜色
                            .setCancelColor(Color.BLACK)//取消按钮文字颜色
                            .setBgColor(0xFFF1F2F3)//滚轮背景颜色 Night mode
                            .setRange(calender.get(Calendar.YEAR) - 100, calender.get(Calendar.YEAR))//默认是1900-2100年
//                        .setDate(selectedDate)// 如果不设置的话，默认是系统时间*/
//                        .setRangDate(startDate,endDate)//起始终止年月日设定
//                        .setLabel("年","月","日","时","分","秒")
                            .isCenterLabel(false) //是否只显示中间选中项的label文字，false则每项item全部都带有label。
                            .isDialog(false)//是否显示为对话框样式
                            .build();
                    pvTime.setDate(Calendar.getInstance());//注：根据需求来决定是否使用该方法（一般是精确到秒的情况），此项可以在弹出选择器的时候重新设置当前时间，避免在初始化之后由于时间已经设定，导致选中时间与当前时间不匹配的问题。
                    pvTime.show();
                } else {
                    Ftoast.create(this).setText("未登录,登录后操作").show();
                }
                break;
            case R.id.layout_user_header_image:
                new ActionSheetDialog(UserinfoActivity.this)
                        .builder()
                        .setCancelable(true)
                        .setCanceledOnTouchOutside(true)
                        .addSheetItem("从相册中选取", ActionSheetDialog.SheetItemColor.GRAY,
                                new ActionSheetDialog.OnSheetItemClickListener() {
                                    @Override
                                    public void onClick(int which) {
                                        mHandler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                showImagePopup();
                                            }
                                        });

                                    }
                                })
                        .addSheetItem("拍照", ActionSheetDialog.SheetItemColor.GRAY,
                                new ActionSheetDialog.OnSheetItemClickListener() {
                                    @Override
                                    public void onClick(int which) {
                                        //拍照选择
                                        mHandler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                chooseFromCamera();
                                            }
                                        });
                                    }
                                })
                        .show();
                break;
        }
    }
    public String getTime(Date date) {//可根据需要自行截取数据显示
        SimpleDateFormat format = new SimpleDateFormat("yyyy年MM月dd日");
        return format.format(date);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Perfence.setPerfence(AppConstant.USER.USER_GETIMAGE_FROM_SERVICE, false);
        isOnActivityResult = true;
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case CROP_CODE:
                    Log.i(TAG, "照片裁剪" + (data == null));
                    if (data == null) {
                        return;
                    } else {
                        Bundle extras = data.getExtras();
                        if (extras != null) {
                            //获取到裁剪后的图像
                            Bitmap bm = extras.getParcelable("data");
                            File file = new File(path);
                            if (file.exists()) {
                                bm = BitmapFactory.decodeFile(file.getPath());
                                // 将图片显示到ImageView中
                                user_head_portrait.setImageBitmap(bm);
                            } else {
                                user_head_portrait.setImageDrawable(getResources().getDrawable(R.mipmap.ic_launcher));
                            }
                            user_head_portrait.setImageBitmap(bm);
                        }
                        manager.uploadImage(path);
                    }
                    break;
                case CAMERA_CODE:
                    //用户点击了取消
                    if (data == null) {
                        return;
                    } else {
                        Bundle extras = data.getExtras();
                        if (extras != null) {
                            //获得拍的照片
                            Bitmap bm = extras.getParcelable("data");
                            //将Bitmap转化为uri
                            //user_head_portrait.setImageBitmap(bm);
                            Uri uri = saveBitmap(bm, "temp");
                            //启动图像裁剪
                            startImageZoom(uri, bm);
                        }
                    }
                    break;
                case 100:
                    if (data == null) {
                        Toast.makeText(getApplicationContext(), "Unable to pick image", Toast.LENGTH_LONG).show();
                        return;
                    }
                    Uri imageUri = data.getData();
                    // user_head_portrait.setImageURI(imageUri);
                    Bitmap photoBmp = null;
                    if (imageUri != null) {
                        try {
                            photoBmp = BitmapHandler.getBitmapFormUri(UserinfoActivity.this, imageUri);
                            user_head_portrait.setImageBitmap(photoBmp);
                        } catch (IOException ignored) {

                        }
                    }
                    final String imagePath = getRealPathFromURI(imageUri);
                    saveToSDCard(photoBmp);
                    //  String imagePathCompress = imagePath + "compressPic.jpg";
                    //使用tiny框架压缩图片
                    Tiny.getInstance().init(UserinfoActivity.this.getApplication());
                    Tiny.FileCompressOptions options = new Tiny.FileCompressOptions();
                    options.outfile = FileKit.getDefaultFileCompressDirectory() + "/tiny-useriamge.jpg";
                    Log.i(TAG, "options.outfile=" + options.outfile);
                    Tiny.getInstance().source(imagePath).asFile().withOptions(options).compress(new FileCallback() {
                        @Override
                        public void callback(boolean isSuccess, String outfile, Throwable t) {
                            //return the compressed file path
                            Log.i(TAG, "imagePath=" + imagePath + "   outfile=" + outfile);
                            manager.uploadImage(outfile);
                        }
                    });
                    break;

            }
        }

    }

    private String getRealPathFromURI(Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        CursorLoader loader = new CursorLoader(getApplicationContext(), contentUri, proj, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String result = cursor.getString(column_index);
        cursor.close();
        return result;
    }

    /**
     * 通过Uri传递图像信息以供裁剪
     *
     * @param uri
     */
    private void startImageZoom(Uri uri, Bitmap bm) {
        //构建隐式Intent来启动裁剪程序
        Intent intent = new Intent("com.android.camera.action.CROP");
        //设置数据uri和类型为图片类型
        intent.setDataAndType(uri, "image/*");
        //显示View为可裁剪的
        intent.putExtra("crop", true);
        //裁剪的宽高的比例为1:1
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("scale", false);
        //输出图片的宽高均为150
        intent.putExtra("outputX", 150);
        intent.putExtra("outputY", 150);
        //裁剪之后的数据是通过Intent返回
        intent.putExtra("return-data", true);
        intent.putExtra("uri", uri);
        startActivityForResult(intent, CROP_CODE);
    }

    private String path = "";

    /**
     * 将Bitmap写入SD卡中的一个文件中,并返回写入文件的Uri
     *
     * @param bm
     * @param dirPath
     * @return
     */
    private Uri saveBitmap(Bitmap bm, String dirPath) {
        Log.i(TAG, "保存照片:" + dirPath);
        //新建文件夹用于存放裁剪后的图片
        File tmpDir = new File(Environment.getExternalStorageDirectory() + "/" + dirPath);
        if (!tmpDir.exists()) {
            tmpDir.mkdir();
        }
        //新建文件存储裁剪后的图片
        File img = new File(tmpDir.getAbsolutePath() + "/avator.png");
        path = img.getPath();
        try {
            //打开文件输出流
            FileOutputStream fos = new FileOutputStream(img);
            //将bitmap压缩后写入输出流(参数依次为图片格式、图片质量和输出流)
            bm.compress(Bitmap.CompressFormat.PNG, 85, fos);
            //刷新输出流
            fos.flush();
            //关闭输出流
            fos.close();
            //返回File类型的Uri
            return Uri.fromFile(img);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }
}
