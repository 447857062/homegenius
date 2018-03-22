package com.deplink.homegenius.activity.personal.login;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.deplink.homegenius.activity.homepage.SmartHomeMainActivity;

import deplink.com.smartwirelessrelay.homegenius.EllESDK.R;

public class LogoActivity extends Activity{
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.logo);
		Handler handler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				switch (msg.what) {
				case 0:
					Intent intent = new Intent(LogoActivity.this,SmartHomeMainActivity.class);
					startActivity(intent);
					finish();
					break;
				}
			}
		};
		Message msg = new Message();
		msg.what = 0;
		handler.sendMessageDelayed(msg, 2000);
	}
}
