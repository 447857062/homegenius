package com.deplink.boruSmart.activity.device.doorbell.add.wifilistadapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import deplink.com.smartwirelessrelay.homegenius.EllESDK.R;

/**
 * Created by benond on 2017/3/7.
 */

public class MacListAdapter extends BaseAdapter {
    private static final String TAG = "AdapterForWirelessRelay";
    private Context mContext;
    private List<String> mData;

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {

        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }


    public MacListAdapter(Context context, List<String> list) {
        this.mContext = context;
        this.mData = list;
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        MacListAdapter.ViewHolder vh;
        if (convertView == null) {
            vh = new MacListAdapter.ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.listitem_mac_list, null);
            vh.tv = convertView.findViewById(R.id.list_text_show_name);
            convertView.setTag(vh);
        } else {
            vh = (MacListAdapter.ViewHolder) convertView.getTag();
        }
        vh.tv.setText("MAC:"+mData.get(position));
        return convertView;
    }


    private static class ViewHolder {
        TextView tv;
    }
}
