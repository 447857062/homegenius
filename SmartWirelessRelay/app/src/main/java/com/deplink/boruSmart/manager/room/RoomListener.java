package com.deplink.boruSmart.manager.room;

import com.deplink.boruSmart.Protocol.json.Room;

import java.util.List;

/**
 * Created by Administrator on 2017/11/9.
 */
public  abstract class RoomListener {
    /**
     *返回查询结果
     */
    public void responseQueryResultHttps(List<Room> result){

    };
    public void responseAddRoomResult(String result){

    };
    public void responseDeleteRoomResult(){

    };
    public void responseUpdateRoomNameResult(){

    };
}
