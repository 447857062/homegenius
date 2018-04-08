package com.deplink.boruSmart.view.dialog.devices;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import deplink.com.smartwirelessrelay.homegenius.EllESDK.R;
/*
*
 * Created by Administrator on 2017/11/11.
 */

public class DeviceRoomTypeDialogAdapter extends BaseAdapter {
    private Context mContext;
    private List<String> listTop;
    public DeviceRoomTypeDialogAdapter(Context mContext, List<String> mRooms) {
        this.mContext = mContext;
        listTop = mRooms;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(
                    R.layout.dialog_device_room_type_item, null);
            viewHolder.textview_room_type = (TextView) convertView
                    .findViewById(R.id.textview_room_type);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.textview_room_type.setText(listTop.get(position));
        if(position==0 ){
            convertView.setBackgroundResource(R.drawable.menudialog_halfrectangle_button_top);
        }else if( position==listTop.size()-1){
            convertView.setBackgroundResource(R.drawable.menudialog_halfrectangle_button_buttom);
        }
        else {
            convertView.setBackgroundResource(R.drawable.button_delete_background);
        }
        return convertView;
    }
    @Override
    public long getItemId(int position) {
        return position;
    }
    @Override
    public Object getItem(int position) {
        return position;
    }
    @Override
    public int getCount() {
        return listTop.size();
    }
    final static class ViewHolder {
        TextView textview_room_type;
    }
}
