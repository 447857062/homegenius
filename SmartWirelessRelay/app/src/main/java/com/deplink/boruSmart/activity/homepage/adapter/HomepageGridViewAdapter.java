package com.deplink.boruSmart.activity.homepage.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.deplink.boruSmart.Protocol.json.device.SmartDev;
import com.deplink.boruSmart.Protocol.json.device.getway.GatwayDevice;
import com.deplink.boruSmart.constant.DeviceTypeConstant;

import java.util.List;

import deplink.com.smartwirelessrelay.homegenius.EllESDK.R;
/*
*
 * Created by Administrator on 2017/11/11.
 */

public class HomepageGridViewAdapter extends BaseAdapter {
    private Context mContext;
    private List<GatwayDevice> listTop = null;
    private List<SmartDev> listBottom = null;
    private final int TOP_ITEM = 0;
    private int TopCount = 0;

    public HomepageGridViewAdapter(Context mContext, List<GatwayDevice> list, List<SmartDev> datasOther) {
        this.mContext = mContext;
        listTop = list;
        this.listBottom = datasOther;
        TopCount = listTop.size();
    }

    public void setTopList(List<GatwayDevice> list) {
        this.listTop = list;
        TopCount = listTop.size();
    }

    public void setBottomList(List<SmartDev> list) {
        this.listBottom = list;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(
                    R.layout.homepage_device_item, null);
            viewHolder.imageview_device_type = (ImageView) convertView
                    .findViewById(R.id.imageview_device_type);
            viewHolder.textview_device_name = (TextView) convertView
                    .findViewById(R.id.textview_device_name);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        //智能网关
        if (position < TopCount) {
            viewHolder.imageview_device_type.setImageResource(R.drawable.gatewayicon);
            String deviceName = listTop.get(position).getName();
            viewHolder.textview_device_name.setText(deviceName);
        } else {
            String deviceType = listBottom.get(position - TopCount).getType();
            String deviceName = listBottom.get(position - TopCount).getName();
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
        if (position >= 0 && position < TopCount) {
            return listTop.get(position);
        }
        if (position > TopCount) {
            return listBottom.get(position - TopCount);
        }
        if (position <= 1) {
            return null;
        }
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        int BOTTOM_ITEM = 1;
        if (position < TopCount)
            return TOP_ITEM;
        else
            return BOTTOM_ITEM;
    }

    @Override
    public int getCount() {
        int count = 0;
        if (listTop != null && listBottom != null) {
            count = TopCount + listBottom.size();
        }
        if (listTop != null && listBottom == null) {
            count = TopCount;
        }
        if (listBottom != null && listTop == null) {
            count = listBottom.size();
        }
        return count;
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
                deviceSubType = listBottom.get(position - TopCount).getSubType();
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
