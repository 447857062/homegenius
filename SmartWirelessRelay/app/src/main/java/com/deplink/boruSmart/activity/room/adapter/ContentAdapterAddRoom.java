package com.deplink.boruSmart.activity.room.adapter;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.deplink.boruSmart.constant.RoomConstant;

import java.util.ArrayList;
import java.util.List;

import deplink.com.smartwirelessrelay.homegenius.EllESDK.R;

/**
 * Created by ${kelijun} on 2018/4/2.
 */
public class ContentAdapterAddRoom extends RecyclerView.Adapter<ContentAdapterAddRoom.ContentHolder> {
    private List<String> mRooms = new ArrayList<>();
    private Context mContext;
    public ContentAdapterAddRoom(Context context) {
        this.mContext = context;
    }
    @Override
    public ContentAdapterAddRoom.ContentHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ContentHolder(LayoutInflater.from(mContext).inflate(R.layout.item_simple_list_2, parent, false));
    }

    @Override
    public void onBindViewHolder(ContentAdapterAddRoom.ContentHolder holder, int position) {
        holder.tv_title.setText("" + mRooms.get(position));
        switch (mRooms.get(position)) {
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
        if (position == getthisPosition()) {
            holder.iv_select_room.setVisibility(View.VISIBLE);
        } else {
            holder.iv_select_room.setVisibility(View.GONE);
        }
    }

    @Override
    public void onViewRecycled(ContentHolder holder) {
        super.onViewRecycled(holder);
        Log.i("onViewRecycled","onViewRecycled");
        holder.iv_select_room.setVisibility(View.GONE);
    }
    //先声明一个int成员变量
    private int thisPosition;
    //再定义一个int类型的返回值方法
    public int getthisPosition() {
        return thisPosition;
    }

    //其次定义一个方法用来绑定当前参数值的方法
    //此方法是在调用此适配器的地方调用的，此适配器内不会被调用到
    public void setThisPosition(int thisPosition) {
        this.thisPosition = thisPosition;
    }
    /**
     * 增加点击监听
     */
    public void setItemListener(onRecyclerItemClickerListener mListener) {
        this.mListener = mListener;
    }
    public void setData(List<String> dataList) {
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
                    v.findViewById(R.id.iv_select_room).setVisibility(View.VISIBLE);
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
        private ImageView iv_select_room;
        private CardView layout_room_background;

        public ContentHolder(View itemView) {
            super(itemView);
            tv_title = (TextView) itemView.findViewById(R.id.tv_title);
            layout_room_background = (CardView) itemView.findViewById(R.id.layout_room_background);
            iv_select_room = (ImageView) itemView.findViewById(R.id.iv_select_room);
        }
    }

}

