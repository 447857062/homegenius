package com.deplink.homegenius.activity.device.getway.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.deplink.homegenius.Protocol.json.device.lock.SSIDList;

import java.util.List;

import deplink.com.smartwirelessrelay.homegenius.EllESDK.R;

public class WifiListAdapter extends BaseAdapter {
    private Context mContext;
    private List<SSIDList> mData;

    private int quality = 0;

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


    public WifiListAdapter(Context context, List<SSIDList> list) {
        this.mContext = context;
        this.mData = list;
    }


    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        Log.i("notifyDataSetChanged", "" + mData.size());
    }
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder vh;
        if (convertView == null) {
            vh = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.listitem_wireless_relay, null);
            vh.tv = convertView.findViewById(R.id.list_text_show_name);
            vh.image = convertView.findViewById(R.id.iamge_item);
            vh.encryption_type = convertView.findViewById(R.id.encryption_type);
            vh.iamge_item_jiami = convertView.findViewById(R.id.iamge_item_jiami);
            convertView.setTag(vh);
        } else {
            vh = (ViewHolder) convertView.getTag();
        }
        String encryption = mData.get(position).getEncryption();
        if(mData.get(position).getQuality().contains("/")){
            quality=Integer.parseInt(mData.get(position).getQuality().substring(0,mData.get(position).getQuality().indexOf("/")));
        }
        //wifi信号图片
        if (!encryption.equalsIgnoreCase("none")) {
            vh.iamge_item_jiami.setImageResource(R.drawable.wifipassword);
        }
        if (quality < 33) {
            vh.image.setImageLevel(0);
        } else if (quality >= 33 && quality < 66) {
            vh.image.setImageLevel(1);
        } else if (quality >= 66) {
            vh.image.setImageLevel(2);
        }
        vh.tv.setText(mData.get(position).getSSID());
        vh.encryption_type.setText(mData.get(position).getEncryption());
        return convertView;
    }
    private static class ViewHolder {
        TextView tv;
        TextView encryption_type;
        ImageView image;
        ImageView iamge_item_jiami;
    }
}
