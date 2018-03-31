package com.deplink.boruSmart.util;

import android.content.Context;
import android.content.pm.PackageManager;

/**
 * Created by Administrator on 2017/9/18.
 */
public class APKVersionCodeUtils {
    /**
     * 获取版本号名称
     *
     * @param context 上下文
     * @return
     */
    public static String getVerName(Context context) {
        String verName = "";

        try {
            verName = context.getPackageManager().
                    getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return verName;
    }
}
