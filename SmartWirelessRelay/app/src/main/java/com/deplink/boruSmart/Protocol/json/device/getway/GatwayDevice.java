package com.deplink.boruSmart.Protocol.json.device.getway;

import com.deplink.boruSmart.Protocol.json.device.Device;
import com.deplink.boruSmart.Protocol.json.Room;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/10/30.
 */
public class GatwayDevice extends Device{

    private String topic;
   /* private String sign_seed;
    private String signature;*/
    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }


    private List<Room>roomList=new ArrayList<>();

    public List<Room> getRoomList() {
        return roomList;
    }

    public void setRoomList(List<Room> roomList) {
        this.roomList = roomList;
    }

}
