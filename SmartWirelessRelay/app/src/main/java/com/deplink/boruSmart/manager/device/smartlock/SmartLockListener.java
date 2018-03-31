package com.deplink.boruSmart.manager.device.smartlock;

import com.deplink.boruSmart.Protocol.json.device.lock.UserIdInfo;

/**
 * Created by Administrator on 2017/11/9.
 */
public interface SmartLockListener {
    /**
     *返回智能锁查询结果
     */
    void responseQueryResult(String result);
    /**
     *返回智能锁设置结果
     */
    void responseSetResult(String result);

    void responseLockStatu(int RecondNum,int LockStatus );
    void responseUserIdInfo(UserIdInfo userIdInfo);
}
