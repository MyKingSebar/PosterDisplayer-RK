package com.youngsee.dao;

import java.util.Date;
import java.util.HashMap;

import com.youngsee.network.HttpParamInterface;

public class OtherInfo extends DMObject implements HttpParamInterface{
    private int   radiation;   // 光辐射传感器值 精度：瓦
    private float pm25;        // PM2.5值 单位 ug/m3
    private float temperature1; // 温度传感器1温度
    private float temperature2; // 温度传感器2温度
    private float humidity1;   // 湿度传感器1湿度 单位RH
    private float humidity2;   // 湿度传感器2湿度 单位RH
                                
    public OtherInfo(String cpuID, String mac){
        super(cpuID, mac);
    }
    
    public OtherInfo(String cpuID, String mac, String name, String group, String model, Date time){
        super(cpuID, mac, name, group, model, time);
    }
    
    public void setRadiation(int value){
        radiation = value;
    }
    
    public void setPM25(float value){
        pm25 = value;
    }
    
    public void setTemperature1(float value){
        temperature1 = value;
    }
    
    public void setTemperature2(float value){
        temperature2 = value;
    }
    
    public void setHumidity1(float value){
        humidity1 = value;
    }
    
    public void setHumidity2(float value){
        humidity2 = value;
    }
    
    @Override
    public HashMap<String, String> getParameters(){
        HashMap<String, String> params = super.getParameters();
        
        params.put("radiation", String.valueOf(radiation));
        params.put("pm25", String.valueOf(pm25));
        params.put("temperature1", String.valueOf(temperature1));
        params.put("temperature2", String.valueOf(temperature2));
        params.put("humidity1", String.valueOf(humidity1));
        params.put("humidity2", String.valueOf(humidity2));
        
        return params;
    }
}
