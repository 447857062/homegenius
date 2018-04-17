package com.deplink.boruSmart.activity.personal.login;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

import com.deplink.boruSmart.activity.personal.AboutUsActivity;
import com.deplink.boruSmart.view.combinationwidget.TitleLayout;

import deplink.com.smartwirelessrelay.homegenius.EllESDK.R;

public class BoraeulaActivity extends Activity {
    private WebView webView;
    private TitleLayout layout_title;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_boraeula);
        layout_title=findViewById(R.id.layout_title);
        webView = findViewById(R.id.main_webview);
        webView.loadUrl("http://www.deplink.net/app/boraeula.html");
        layout_title.setReturnClickListener(new TitleLayout.ReturnImageClickListener() {
            @Override
            public void onBackPressed() {
                BoraeulaActivity.this.onBackPressed();
            }
        });
    }
}
