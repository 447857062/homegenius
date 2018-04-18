package com.deplink.boruSmart.activity.homepage.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.deplink.boruSmart.Protocol.json.device.Device;
import com.deplink.boruSmart.Protocol.json.device.SmartDev;
import com.deplink.boruSmart.constant.DeviceTypeConstant;

import java.util.List;

import deplink.com.smartwirelessrelay.homegenius.EllESDK.R;
/*
*
 * Created by Administrator on 2017/11/11.
 */

public class HomepageRoomShowTypeChangedViewAdapter extends BaseAdapter {
    private Context mContext;
    private List<Device> mDeviceList = null;
    public HomepageRoomShowTypeChangedViewAdapter(Context mContext, List<Device> deviceList) {
        this.mContext = mContext;
        this.mDeviceList = deviceList;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(
                    R.layout.homepage_roomchanged_item, null);
            viewHolder.textview_room_item = convertView
                    .findViewById(R.id.textview_room_item);
            viewHolder.textview_device_status = convertView
                    .findViewById(R.id.textview_device_status);
            viewHolder.imageview_room_type = convertView
                    .findViewById(R.id.imageview_room_type);
            viewHolder.view_line = convertView
                    .findViewById(R.id.view_line);
            viewHolder.layout_root = convertView
                    .findViewById(R.id.layout_root);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        //智能网关
            if (mDeviceList.get(position).getType().equalsIgnoreCase(DeviceTypeConstant.TYPE.TYPE_SMART_GETWAY)) {
                viewHolder.imageview_room_type.setImageResource(R.drawable.gatewayicon);
                String statu = mDeviceList.get(position).getStatus();
                if (statu != null) {
                    switch (statu) {
                        case "on":
                            statu = "在线";
                            break;
                        case "off":
                            statu = "离线";
                            break;
                    }
                } else {
                    statu = "离线";
                }
                viewHolder.textview_device_status.setText(statu);
                if (statu.equalsIgnoreCase("在线")) {
                    viewHolder.textview_device_status.setTextColor(0xFF60a3f6);
                } else {
                    viewHolder.textview_device_status.setTextColor(0xFF999999);
                }
                String deviceName =mDeviceList.get(position).getName();
                viewHolder.textview_room_item.setText(deviceName);

            } else {
                String deviceType = mDeviceList.get(position).getType();
                String deviceName = mDeviceList.get(position).getName();
                String deviceStatu =mDeviceList.get(position).getStatus();
                if (deviceStatu != null) {
                    switch (deviceStatu) {
                        case "on":
                            deviceStatu = "在线";
                            break;
                        case "off":
                            deviceStatu = "离线";
                            break;
                    }
                } else {
                    deviceStatu = "离线";
                }
                if ("SMART_LOCK".equals(deviceType)) {
                    deviceType = DeviceTypeConstant.TYPE.TYPE_LOCK;
                }
                if ("IRMOTE_V2".equals(deviceType)) {
                    deviceType = DeviceTypeConstant.TYPE.TYPE_REMOTECONTROL;
                }
                viewHolder.textview_room_item.setText(deviceName);
                if (deviceStatu.equalsIgnoreCase("在线")) {
                    viewHolder.textview_device_status.setTextColor(0xFF60a3f6);
                } else {
                    viewHolder.textview_device_status.setTextColor(0xFF999999);
                }
                viewHolder.textview_device_status.setText(deviceStatu);
                getDeviceTypeImage(viewHolder, deviceType, position);

        }
        return convertView;
    }
    private void getDeviceTypeImage(ViewHolder viewHolder, String deviceType, int position) {
        switch (deviceType) {
            case DeviceTypeConstant.TYPE.TYPE_ROUTER:
                viewHolder.imageview_room_type.setImageResource(R.drawable.routericon);
                break;
            case DeviceTypeConstant.TYPE.TYPE_LOCK:

                viewHolder.imageview_room_type.setImageResource(R.drawable.doorlockicon);
                break;
            case DeviceTypeConstant.TYPE.TYPE_MENLING:
                viewHolder.imageview_room_type.setImageResource(R.drawable.doorbellicon);
                break;
            case DeviceTypeConstant.TYPE.TYPE_SWITCH:
                String deviceSubType;
                deviceSubType = ((SmartDev)mDeviceList.get(position)).getSubType();
                if (deviceSubType == null) {
                    return;
                }
                switch (deviceSubType) {
                    case DeviceTypeConstant.TYPE_SWITCH_SUBTYPE.SUB_TYPE_SWITCH_ONEWAY:
                        viewHolder.imageview_room_type.setImageResource(R.drawable.switchalltheway);
                        break;
                    case DeviceTypeConstant.TYPE_SWITCH_SUBTYPE.SUB_TYPE_SWITCH_TWOWAY:
                        viewHolder.imageview_room_type.setImageResource(R.drawable.roadswitch);
                        break;
                    case DeviceTypeConstant.TYPE_SWITCH_SUBTYPE.SUB_TYPE_SWITCH_THREEWAY:
                        viewHolder.imageview_room_type.setImageResource(R.drawable.threewayswitch);
                        break;
                    case DeviceTypeConstant.TYPE_SWITCH_SUBTYPE.SUB_TYPE_SWITCH_FOURWAY:
                        viewHolder.imageview_room_type.setImageResource(R.drawable.fourwayswitch);
                        break;
                }
                break;
            case DeviceTypeConstant.TYPE.TYPE_REMOTECONTROL:
                viewHolder.imageview_room_type.setImageResource(R.drawable.infraredremotecontrolicon);
                break;
            case DeviceTypeConstant.TYPE.TYPE_TV_REMOTECONTROL:
            case "智能电视":
                viewHolder.imageview_room_type.setImageResource(R.drawable.tvicon);
                break;
            case DeviceTypeConstant.TYPE.TYPE_AIR_REMOTECONTROL:
            case "智能空调":
                viewHolder.imageview_room_type.setImageResource(R.drawable.airconditioningicon);
                break;
            case DeviceTypeConstant.TYPE.TYPE_TVBOX_REMOTECONTROL:
            case "智能机顶盒遥控":
                viewHolder.imageview_room_type.setImageResource(R.drawable.settopboxesicon);
                break;
            case DeviceTypeConstant.TYPE.TYPE_LIGHT:
                viewHolder.imageview_room_type.setImageResource(R.drawable.equipmentlight);
                break;
        }
    }



    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public Object getItem(int position) {

        return mDeviceList.get(position);
    }
    @Override
    public int getCount() {

        return mDeviceList.size();
    }

    final static class ViewHolder {
        RelativeLayout layout_root;
        TextView textview_room_item;
        TextView textview_device_status;
        ImageView imageview_room_type;
        View view_line;
    }
}
