package com.deplink.boruSmart.activity.device.doorbell.add.wifilistadapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import deplink.com.smartwirelessrelay.homegenius.EllESDK.R;

/**
 * Created by benond on 2017/3/7.
 */

public class WIFIListAdapter extends BaseAdapter {
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


    public WIFIListAdapter(Context context, List<String> list) {
        this.mContext = context;
        this.mData = list;
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        WIFIListAdapter.ViewHolder vh;
        if (convertView == null) {
            vh = new WIFIListAdapter.ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.listitem_wireless_relay, null);
            vh.tv = convertView.findViewById(R.id.list_text_show_name);
            vh.image = convertView.findViewById(R.id.iamge_item);
            vh.encryption_type = convertView.findViewById(R.id.encryption_type);
            vh.iamge_item_jiami = convertView.findViewById(R.id.iamge_item_jiami);
            convertView.setTag(vh);
        } else {
            vh = (WIFIListAdapter.ViewHolder) convertView.getTag();
        }
        //wifi信号图片

        vh.iamge_item_jiami.setImageResource(R.drawable.wifipassword);
        vh.image.setImageLevel(2);
        vh.tv.setText(mData.get(position));
        vh.encryption_type.setVisibility(View.GONE);
        return convertView;
    }


    private static class ViewHolder {
        TextView tv;
        TextView encryption_type;
        ImageView image;
        ImageView iamge_item_jiami;
    }
}
