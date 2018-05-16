package com.deplink.boruSmart.activity.device.doorbell.add;

import android.app.Activity;
import android.os.Bundle;

import com.deplink.boruSmart.view.combinationwidget.TitleLayout;

import deplink.com.smartwirelessrelay.homegenius.EllESDK.R;

public class PairGuideTwoActivity extends Activity {
    private TitleLayout layout_title;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pair_guide_two);
        initViews();
        initEvents();
        initDatas();
    }
    private void initDatas() {

    }
    @Override
    public void finish() {
        super.finish();
        //注释掉activity本身的过渡动画
        overridePendingTransition(R.anim.in_left, R.anim.out_right);
    }
    private void initEvents() {
        layout_title.setReturnClickListener(new TitleLayout.ReturnImageClickListener() {
            @Override
            public void onBackPressed() {
                PairGuideTwoActivity.this.onBackPressed();
            }
        });
    }

    private void initViews() {
        layout_title = (TitleLayout) findViewById(R.id.layout_title);
    }
}
