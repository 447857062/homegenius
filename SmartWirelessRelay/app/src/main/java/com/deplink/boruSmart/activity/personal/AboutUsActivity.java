package com.deplink.boruSmart.activity.personal;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

import com.deplink.boruSmart.view.combinationwidget.TitleLayout;

import deplink.com.smartwirelessrelay.homegenius.EllESDK.R;

public class AboutUsActivity extends Activity {
    private WebView webView;
    private TitleLayout layout_title;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);
        layout_title=findViewById(R.id.layout_title);
        webView = findViewById(R.id.main_webview);
        webView.loadUrl("http://www.deplink.net/app/deplinkinfo.html");
        layout_title.setReturnClickListener(new TitleLayout.ReturnImageClickListener() {
            @Override
            public void onBackPressed() {
                AboutUsActivity.this.onBackPressed();
            }
        });
    }
}
