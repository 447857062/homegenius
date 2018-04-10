package com.deplink.boruSmart.view.toast;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.deplink.boruSmart.util.Perfence;

import deplink.com.smartwirelessrelay.homegenius.EllESDK.R;

/**
 * Created by ${kelijun} on 2018/4/9.
 */
public class Ftoast {
    private Context mContext; //上下文
    private TextView txtContent; //文本内容
    private Toast toast; //原始Toast

    //注意，这里是private，意思就是不让你自己new。
    private Ftoast(Context context) {
        LayoutInflater inflate = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        //加载自定义的XML布局
        View root = inflate.inflate(R.layout.toast, null);
        txtContent = (TextView) root.findViewById(R.id.txtToast);
        toast = new Toast(context);
        toast.setView(root); //这是setView。就是你的自定义View
        //这是，放着顶部，然后水平放满屏幕
        float distance= Perfence.dp2px(context,200);
        toast.setGravity(Gravity.BOTTOM | Gravity.FILL_HORIZONTAL, 0, (int) distance);
    }

    //这是个简易工厂模式
    public static Ftoast create(Context context) {
        Ftoast ft = new Ftoast(context);
        ft.toast.setDuration(Toast.LENGTH_SHORT);
        return ft;
    }
    // 文字 （然而这并没有什么卵用）
    public Ftoast setText(String text) {
        this.txtContent.setText(text);
        return this;
    }
    // Duration。这是Toast.LENGTH_SHORT 或者 Toast.LENGTH_LONG
    // 别瞎写什么 1000 、2000。真是醉了。
    public Ftoast setDuration(int duration) {
        this.toast.setDuration(duration);
        return this;
    }
    // 注意，必须要调用show。不然写Toast干嘛。
    public void show() {
        this.toast.show();
    }
}

