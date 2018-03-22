package com.deplink.homegenius.activity.room.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.deplink.homegenius.constant.RoomConstant;

import java.util.ArrayList;
import java.util.List;

import deplink.com.smartwirelessrelay.homegenius.EllESDK.R;
/*
*
 * Created by Administrator on 2017/11/11.
 */

public class GridViewRommTypeAdapter extends BaseAdapter {
    private Context mContext;
    private List<String> listTop = new ArrayList<>();

    public GridViewRommTypeAdapter(Context mContext) {
        this.mContext = mContext;
        listTop.add(RoomConstant.ROOMTYPE.TYPE_LIVING);
        listTop.add(RoomConstant.ROOMTYPE.TYPE_BED);
        listTop.add(RoomConstant.ROOMTYPE.TYPE_KITCHEN);
        listTop.add(RoomConstant.ROOMTYPE.TYPE_STUDY);
        listTop.add(RoomConstant.ROOMTYPE.TYPE_STORAGE);
        listTop.add(RoomConstant.ROOMTYPE.TYPE_TOILET);
        listTop.add(RoomConstant.ROOMTYPE.TYPE_DINING);
    }

    private int selectedPosition = 0;// 选中的位置

    public void setSelectedPosition(int position) {
        selectedPosition = position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(
                    R.layout.roomtype_item, null);
            viewHolder.imageview_room_type = convertView
                    .findViewById(R.id.imageview_room_type);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        if (selectedPosition == position) {
            setSelectRoomTypeImageResource(position, viewHolder);
        } else {
            setRoomTypeImageResource(position, viewHolder);
        }
        return convertView;
    }
    /**
     * 设置房间类型对应的图片
     * @param position
     * @param viewHolder
     */
    private void setRoomTypeImageResource(int position, ViewHolder viewHolder) {
        if(listTop.get(position)==null){
            return;
        }
        switch (listTop.get(position)){
            case RoomConstant.ROOMTYPE.TYPE_LIVING:
                viewHolder.imageview_room_type.setImageResource(R.drawable.noaddroomlivingroom);
                break;
            case RoomConstant.ROOMTYPE.TYPE_BED:
                viewHolder.imageview_room_type.setImageResource(R.drawable.noaddroombedroom);
                break;
            case RoomConstant.ROOMTYPE.TYPE_DINING:
                viewHolder.imageview_room_type.setImageResource(R.drawable.noaddroomdiningroom);
                break;
            case RoomConstant.ROOMTYPE.TYPE_KITCHEN:
                viewHolder.imageview_room_type.setImageResource(R.drawable.noaddroomkitchen);
                break;
            case RoomConstant.ROOMTYPE.TYPE_STORAGE:
                viewHolder.imageview_room_type.setImageResource(R.drawable.noaddroomstorageroom);
                break;
            case RoomConstant.ROOMTYPE.TYPE_STUDY:
                viewHolder.imageview_room_type.setImageResource(R.drawable.noaddroomstudy);
                break;
            case RoomConstant.ROOMTYPE.TYPE_TOILET:
                viewHolder.imageview_room_type.setImageResource(R.drawable.noaddroomtoilet);
                break;
        }
    }
    /**
     * 设置房间类型对应的图片
     * @param position
     * @param viewHolder
     */
    private void setSelectRoomTypeImageResource(int position, ViewHolder viewHolder) {
        if(listTop.get(position)==null){
            return;
        }
        switch (listTop.get(position)){
            case RoomConstant.ROOMTYPE.TYPE_LIVING:
                viewHolder.imageview_room_type.setImageResource(R.drawable.addroomlivingroom);
                break;
            case RoomConstant.ROOMTYPE.TYPE_BED:
                viewHolder.imageview_room_type.setImageResource(R.drawable.addroombedroom);
                break;
            case RoomConstant.ROOMTYPE.TYPE_DINING:
                viewHolder.imageview_room_type.setImageResource(R.drawable.addroomdiningroom);
                break;
            case RoomConstant.ROOMTYPE.TYPE_KITCHEN:
                viewHolder.imageview_room_type.setImageResource(R.drawable.addroomkitchen);
                break;
            case RoomConstant.ROOMTYPE.TYPE_STORAGE:
                viewHolder.imageview_room_type.setImageResource(R.drawable.addroomstorageroom);
                break;
            case RoomConstant.ROOMTYPE.TYPE_STUDY:
                viewHolder.imageview_room_type.setImageResource(R.drawable.addroomstudy);
                break;
            case RoomConstant.ROOMTYPE.TYPE_TOILET:
                viewHolder.imageview_room_type.setImageResource(R.drawable.addroomtoilet);
                break;
        }
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
        ImageView imageview_room_type;
    }
}
