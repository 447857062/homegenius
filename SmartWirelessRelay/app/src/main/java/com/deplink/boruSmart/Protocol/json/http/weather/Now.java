package com.deplink.boruSmart.Protocol.json.http.weather;

/**
 * Created by ${kelijun} on 2018/1/16.
 */
public class Now {
    private String tmp;

    public String getTmp() {
        return tmp;
    }

    public void setTmp(String tmp) {
        this.tmp = tmp;
    }

    @Override
    public String toString() {
        return "Now{" +
                "tmp='" + tmp + '\'' +
                '}';
    }
}
