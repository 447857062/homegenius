package com.deplink.boruSmart.activity.device.remoteControl.topBox;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.deplink.boruSmart.Protocol.json.http.QueryBandResponse;
import com.deplink.boruSmart.activity.device.remoteControl.RemoteControlQuickLearnActivity;
import com.deplink.boruSmart.view.listview.sortlistview.CharacterParser;
import com.deplink.boruSmart.view.listview.sortlistview.SideBar;
import com.deplink.boruSmart.view.listview.sortlistview.SortAdapter;
import com.deplink.boruSmart.view.listview.sortlistview.SortModel;
import com.deplink.boruSmart.manager.connect.remote.https.RestfulTools;
import com.deplink.boruSmart.view.combinationwidget.TitleLayout;
import com.deplink.boruSmart.view.edittext.ClearEditText;
import com.deplink.boruSmart.view.listview.sortlistview.PinyinComparator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import deplink.com.smartwirelessrelay.homegenius.EllESDK.R;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddTopBoxActivity extends Activity implements AdapterView.OnItemClickListener{
    private ListView listview_band;
    private SideBar sideBar;
    private TextView dialog;
    private SortAdapter adapter;
    private ClearEditText edittext_band_name;
    private CharacterParser characterParser;
    private List<SortModel> SourceDateList = new ArrayList<>();
    private PinyinComparator pinyinComparator;
    private TitleLayout layout_title;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_top_box);
        initViews();
        initDatas();
        initEvents();
    }

    @Override
    protected void onResume() {
        super.onResume();
        RestfulTools.getSingleton(this).queryBand("STB", "cn", new Callback<QueryBandResponse>() {
            @Override
            public void onResponse(Call<QueryBandResponse> call, Response<QueryBandResponse> response) {
                if(response.body().getValue().size()>0){
                        SourceDateList.clear();
                        SourceDateList.addAll(filledData(response.body().getValue()));
                        Collections.sort(SourceDateList, pinyinComparator);
                        adapter.notifyDataSetChanged();
                }
            }
            @Override
            public void onFailure(Call<QueryBandResponse> call, Throwable t) {

            }
        });
    }

    private void initEvents() {
        listview_band.setOnItemClickListener(this);
        sideBar.setOnTouchingLetterChangedListener(new SideBar.OnTouchingLetterChangedListener() {

            @Override
            public void onTouchingLetterChanged(String s) {

                int position = adapter.getPositionForSection(s.charAt(0));
                if (position != -1) {
                    listview_band.setSelection(position);
                }

            }
        });
        listview_band.setAdapter(adapter);
        edittext_band_name.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterData(s.toString());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void initDatas() {
        sideBar.setTextView(dialog);
        characterParser = CharacterParser.getInstance();
        pinyinComparator = new PinyinComparator();
        adapter = new SortAdapter(this, SourceDateList);
        layout_title.setReturnClickListener(new TitleLayout.ReturnImageClickListener() {
            @Override
            public void onBackPressed() {
                AddTopBoxActivity.this.onBackPressed();
            }
        });
    }

    private void initViews() {
        edittext_band_name = (ClearEditText) findViewById(R.id.edittext_band_name);
        listview_band= (ListView) findViewById(R.id.listview_band);
        sideBar = (SideBar) findViewById(R.id.sidrbar);
        dialog = (TextView) findViewById(R.id.dialog);
        layout_title= (TitleLayout) findViewById(R.id.layout_title);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent=new Intent(AddTopBoxActivity.this,RemoteControlQuickLearnActivity.class);
        intent.putExtra("bandname",SourceDateList.get(position).getName());
        intent.putExtra("type","智能机顶盒遥控");
        startActivity(intent);
    }
    private List<SortModel> filledData(List<String> date) {
        List<SortModel> mSortList = new ArrayList<>();

        for (int i = 0; i < date.size(); i++) {
            SortModel sortModel = new SortModel();
            sortModel.setName(date.get(i));
            String pinyin = characterParser.getSelling(date.get(i));
            String sortString = pinyin.substring(0, 1).toUpperCase();
            if (sortString.matches("[A-Z]")) {
                sortModel.setSortLetters(sortString.toUpperCase());
            } else {
                sortModel.setSortLetters("#");
            }

            mSortList.add(sortModel);
        }
        return mSortList;

    }


    private void filterData(String filterStr) {
        List<SortModel> filterDateList = new ArrayList<>();

        if (TextUtils.isEmpty(filterStr)) {
            filterDateList = SourceDateList;
        } else {
            filterDateList.clear();
            for (SortModel sortModel : SourceDateList) {
                String name = sortModel.getName();
                if (name.contains(filterStr) || characterParser.getSelling(name).startsWith(filterStr)) {
                    filterDateList.add(sortModel);
                }
            }
        }
        Collections.sort(filterDateList, pinyinComparator);
        adapter.updateListView(filterDateList);
    }
}
