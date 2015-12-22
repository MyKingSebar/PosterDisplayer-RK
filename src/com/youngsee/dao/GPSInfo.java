package com.youngsee.dao;

import java.util.Date;
import java.util.HashMap;

import com.youngsee.network.HttpParamInterface;

public class GPSInfo extends DMObject implements HttpParamInterface{
    private boolean north;    // 是否为北半球 false:不是, true:是
    private boolean east;     // 是否为东半球 false:不是, true:是
                               
    private String  latitude; // 纬度
    private String  longitude; // 经度
    private float   altitude; // 高度， 米
                               
    public GPSInfo(String cpuID, String mac){
        super(cpuID, mac);
    }
    
    public GPSInfo(String cpuID, String mac, String name, String group, String model, Date time){
        super(cpuID, mac, name, group, model, time);
    }
    
    public void setNorth(boolean value){
        north = value;
    }
    
    public void setEast(boolean value){
        east = value;
    }
    
    public void setLatitude(String value){
        latitude = value;
    }
    
    public void setLongitude(String value){
        longitude = value;
    }
    
    public void setAltitude(float value){
        altitude = value;
    }
    
    @Override
    public HashMap<String, String> getParameters(){
        HashMap<String, String> params = super.getParameters();
        
        params.put("north", getBooleanString(north));
        params.put("east", getBooleanString(east));
        params.put("latitude", latitude);
        params.put("longitude", longitude);
        params.put("altitude", String.valueOf(altitude));
        
        return params;
    }
}
