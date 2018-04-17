package com.deplink.boruSmart.activity.room.adapter;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.deplink.boruSmart.Protocol.json.Room;
import com.deplink.boruSmart.constant.RoomConstant;

import java.util.ArrayList;
import java.util.List;

import deplink.com.smartwirelessrelay.homegenius.EllESDK.R;

/**
 * Created by ${kelijun} on 2018/4/2.
 */
public class ContentAdapter  extends RecyclerView.Adapter<ContentAdapter.ContentHolder>{
    private List<Room> mRooms = new ArrayList<>();
    private Context mContext;
    private boolean showDeviceNumber;
    public ContentAdapter(Context context,boolean showDevice) {
        this.mContext=context;
        this.showDeviceNumber=showDevice;
    }

    @Override
    public ContentAdapter.ContentHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ContentHolder(LayoutInflater.from(mContext).inflate(R.layout.item_simple_list_1, parent, false));
    }
    @Override
    public void onBindViewHolder(ContentAdapter.ContentHolder holder, int position) {
        holder.tv_title.setText("" + mRooms.get(position).getRoomName());
        int deviceNumberTotal = mRooms.get(position).getmDevices().size() + mRooms.get(position).getmGetwayDevices().size();
        int onlineDeviceNumber=0;
        for(int i=0;i< mRooms.get(position).getmDevices().size();i++){
            if( mRooms.get(position).getmDevices().get(i).getStatus()!=null
                    &&( mRooms.get(position).getmDevices().get(i).getStatus().equalsIgnoreCase("on")
                    ||mRooms.get(position).getmDevices().get(i).getStatus().equalsIgnoreCase("在线"))
                    ){
                onlineDeviceNumber++;
            }
        }
        for(int i=0;i< mRooms.get(position).getmGetwayDevices().size();i++){
            if(mRooms.get(position).getmGetwayDevices().get(i).getStatus()!=null &&
                    (mRooms.get(position).getmGetwayDevices().get(i).getStatus().equalsIgnoreCase("on")
                    ||mRooms.get(position).getmGetwayDevices().get(i).getStatus().equalsIgnoreCase("在线"))
                    ){
                onlineDeviceNumber++;
            }
        }
        if(showDeviceNumber){
            holder.tv_content.setText(""+onlineDeviceNumber+"/"+ deviceNumberTotal +" 在线");
        }else{
            holder.tv_content.setText("");
        }

        switch ( mRooms.get(position).getRoomType()){
            case RoomConstant.ROOMTYPE.TYPE_BED:
                holder.layout_room_background.setBackgroundResource(R.drawable.roombedroom);
                break;
            case RoomConstant.ROOMTYPE.TYPE_DINING:
                holder.layout_room_background.setBackgroundResource(R.drawable.roomdiningroom);
                break;
            case RoomConstant.ROOMTYPE.TYPE_KITCHEN:
                holder.layout_room_background.setBackgroundResource(R.drawable.roomkitchen);
                break;
            case RoomConstant.ROOMTYPE.TYPE_LIVING:
                holder.layout_room_background.setBackgroundResource(R.drawable.roomlivingroom);
                break;
            case RoomConstant.ROOMTYPE.TYPE_STORAGE:
                holder.layout_room_background.setBackgroundResource(R.drawable.roomstorageroom);
                break;
            case RoomConstant.ROOMTYPE.TYPE_STUDY:
                holder.layout_room_background.setBackgroundResource(R.drawable.roomstudy);
                break;
            case RoomConstant.ROOMTYPE.TYPE_TOILET:
                holder.layout_room_background.setBackgroundResource(R.drawable.roomtoilet);
                break;
        }
        holder.layout_room_background.setOnClickListener(getOnClickListener(position));
    }
    /**
     * 增加点击监听
     */
    public void setItemListener(onRecyclerItemClickerListener mListener) {
        this.mListener = mListener;
    }
    public void setData(List<Room> dataList) {
        if (null != dataList) {
            this.mRooms.clear();
            this.mRooms.addAll(dataList);
            notifyDataSetChanged();
        }
    }
    private onRecyclerItemClickerListener mListener;
    private View.OnClickListener getOnClickListener(final int position) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener && null != v) {
                    mListener.onRecyclerItemClick(v, mRooms.get(position), position);
                }
            }
        };
    }
    @Override
    public int getItemCount() {
        return mRooms.size();
    }
    /**
     * 点击监听回调接口
     */
    public interface onRecyclerItemClickerListener {
        void onRecyclerItemClick(View view, Object data, int position);
    }
    class ContentHolder extends RecyclerView.ViewHolder {
        private TextView tv_title;
        private TextView tv_content;
        private CardView layout_room_background;
        public ContentHolder(View itemView) {
            super(itemView);
            tv_title = (TextView) itemView.findViewById(R.id.tv_title);
            tv_content = (TextView) itemView.findViewById(R.id.tv_content);
            layout_room_background = (CardView) itemView.findViewById(R.id.layout_room_background);
        }
    }

}

