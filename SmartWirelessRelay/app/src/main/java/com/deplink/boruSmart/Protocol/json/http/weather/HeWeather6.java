package com.deplink.boruSmart.Protocol.json.http.weather;

import java.util.List;

/**
 * Created by ${kelijun} on 2018/1/16.
 */
public class HeWeather6 {
    private List<WeatherInfo>HeWeather6;
    public List<WeatherInfo> getInfoList() {
        return HeWeather6;
    }

    public void setInfoList(List<WeatherInfo> infoList) {
        this.HeWeather6 = infoList;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder=new StringBuilder();
        for(int i=0;i<HeWeather6.size();i++){
            stringBuilder.append(HeWeather6.get(i).toString());
        }
        return stringBuilder.toString();
    }
}
