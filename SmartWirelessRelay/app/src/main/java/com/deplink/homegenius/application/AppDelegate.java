package com.deplink.homegenius.application;

import android.content.Context;
import android.support.multidex.MultiDex;
import android.util.Log;

import com.deplink.homegenius.util.Perfence;
import com.mob.MobSDK;

import org.litepal.LitePalApplication;


/**
 * Created by ${kelijun} on 2017/2/6.
 */
public class AppDelegate extends LitePalApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        MobSDK.init(this);
        Perfence.setContext(this);
    }
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        Log.i("onTerminate","onTerminate");
        android.os.Process.killProcess(android.os.Process.myPid());
    }

}
