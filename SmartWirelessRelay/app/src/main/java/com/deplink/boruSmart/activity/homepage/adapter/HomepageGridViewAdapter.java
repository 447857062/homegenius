package com.deplink.boruSmart.activity.homepage.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
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

public class HomepageGridViewAdapter extends BaseAdapter {
    private Context mContext;
    private List<Device> mDeviceList = null;

    public HomepageGridViewAdapter(Context mContext, List<Device> deviceList) {
        this.mContext = mContext;
        this.mDeviceList = deviceList;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(
                    R.layout.homepage_device_item, null);
            viewHolder.imageview_device_type = convertView
                    .findViewById(R.id.imageview_device_type);
            viewHolder.textview_device_name = convertView
                    .findViewById(R.id.textview_device_name);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        //智能网关
        if (mDeviceList.get(position).getType().equalsIgnoreCase(DeviceTypeConstant.TYPE.TYPE_SMART_GETWAY)) {
            viewHolder.imageview_device_type.setImageResource(R.drawable.gatewayicon);
            String deviceName = mDeviceList.get(position).getName();
            viewHolder.textview_device_name.setText(deviceName);
        } else {
            String deviceType =mDeviceList.get(position).getType();
            String deviceName = mDeviceList.get(position).getName();
            if ("SMART_LOCK".equals(deviceType)) {
                deviceType = DeviceTypeConstant.TYPE.TYPE_LOCK;
            }
            if ("IRMOTE_V2".equals(deviceType)) {
                deviceType = DeviceTypeConstant.TYPE.TYPE_REMOTECONTROL;
            }
            viewHolder.textview_device_name.setText(deviceName);
            getDeviceTypeImage(viewHolder, deviceType, position);
        }
        return convertView;
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
    public int getViewTypeCount() {
        return 2;
    }


    @Override
    public int getCount() {
      return mDeviceList.size();
    }

    private void getDeviceTypeImage(ViewHolder viewHolder, String deviceType, int position) {
        switch (deviceType) {
            case DeviceTypeConstant.TYPE.TYPE_ROUTER:
                viewHolder.imageview_device_type.setImageResource(R.drawable.routericon);
                break;
            case DeviceTypeConstant.TYPE.TYPE_LOCK:

                viewHolder.imageview_device_type.setImageResource(R.drawable.doorlockicon);
                break;
            case DeviceTypeConstant.TYPE.TYPE_MENLING:
                viewHolder.imageview_device_type.setImageResource(R.drawable.doorbellicon);
                break;
            case DeviceTypeConstant.TYPE.TYPE_SWITCH:
                String deviceSubType;
                deviceSubType =((SmartDev) mDeviceList.get(position)).getSubType();
                if (deviceSubType == null) {
                    return;
                }
                switch (deviceSubType) {
                    case DeviceTypeConstant.TYPE_SWITCH_SUBTYPE.SUB_TYPE_SWITCH_ONEWAY:
                        viewHolder.imageview_device_type.setImageResource(R.drawable.switchalltheway);
                        break;
                    case DeviceTypeConstant.TYPE_SWITCH_SUBTYPE.SUB_TYPE_SWITCH_TWOWAY:
                        viewHolder.imageview_device_type.setImageResource(R.drawable.roadswitch);
                        break;
                    case DeviceTypeConstant.TYPE_SWITCH_SUBTYPE.SUB_TYPE_SWITCH_THREEWAY:
                        viewHolder.imageview_device_type.setImageResource(R.drawable.threewayswitch);
                        break;
                    case DeviceTypeConstant.TYPE_SWITCH_SUBTYPE.SUB_TYPE_SWITCH_FOURWAY:
                        viewHolder.imageview_device_type.setImageResource(R.drawable.fourwayswitch);
                        break;
                }
                break;
            case DeviceTypeConstant.TYPE.TYPE_REMOTECONTROL:
                viewHolder.imageview_device_type.setImageResource(R.drawable.infraredremotecontrolicon);
                break;
            case DeviceTypeConstant.TYPE.TYPE_TV_REMOTECONTROL:
            case "智能电视":
                viewHolder.imageview_device_type.setImageResource(R.drawable.tvicon);
                break;
            case DeviceTypeConstant.TYPE.TYPE_AIR_REMOTECONTROL:
            case "智能空调":
                viewHolder.imageview_device_type.setImageResource(R.drawable.airconditioningicon);
                break;
            case DeviceTypeConstant.TYPE.TYPE_TVBOX_REMOTECONTROL:
            case "智能机顶盒遥控":
                viewHolder.imageview_device_type.setImageResource(R.drawable.settopboxesicon);
                break;
            case DeviceTypeConstant.TYPE.TYPE_LIGHT:
                viewHolder.imageview_device_type.setImageResource(R.drawable.equipmentlight);
                break;
        }
    }

    final static class ViewHolder {
        TextView textview_device_name;
        ImageView imageview_device_type;
    }
}
