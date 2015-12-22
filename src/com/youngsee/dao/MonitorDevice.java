package com.youngsee.dao;

import java.util.Date;
import java.util.HashMap;

import com.youngsee.network.HttpParamInterface;

public class MonitorDevice extends DMObject implements HttpParamInterface{
    private String   location;   // 设备安装位置
                                
    public MonitorDevice(String cpuID, String mac){
        super(cpuID, mac);
    }
    
    public MonitorDevice(String cpuID, String mac, String name, String group, String model, Date time){
        super(cpuID, mac, name, group, model, time);
    }
    
    public void setLocation(String value){
        location = value;
    }
    
    @Override
    public HashMap<String, String> getParameters(){
        HashMap<String, String> params = super.getParameters();
        params.put("location", location);
    
        return params;
    }
}
