package com.youngsee.dao;

import java.util.Date;
import java.util.HashMap;

import com.youngsee.network.HttpParamInterface;

public class SwitchingInfo extends DMObject implements HttpParamInterface{
    private int waterInvasion; // 水侵传感器   -1：禁止，0：闭合，1：断开
    private int doorGuard;     // 门禁传感器   -1：禁止，0：闭合，1：断开
    private int vibration;     // 振动传感器   -1：禁止，0：闭合，1：断开
    private int pressureSwitch; // 压差传感器开关   -1：禁止，0：闭合，1：断开
    private int brightness; // 屏亮度开关   -1：禁止，0：闭合，1：断开
                                    
    public SwitchingInfo(String cpuID, String mac){
        super(cpuID, mac);
    }
    
    public SwitchingInfo(String cpuID, String mac, String name, String group, String model, Date time){
        super(cpuID, mac, name, group, model, time);
    }
    
    public void setWaterInvasion(int value){
        waterInvasion = value;
    }
    
    public void setDoorGuard(int value){
        doorGuard = value;
    }
    
    public void setVibration(int value){
        vibration = value;
    }
    
    public void setPressureSwitch(int value){
        pressureSwitch = value;
    }
    
    public void setBrightness(int value){
        brightness = value;
    }
    
    @Override
    public HashMap<String, String> getParameters(){
        HashMap<String, String> params = super.getParameters();
        
        params.put("water_invasion", String.valueOf(waterInvasion));
        params.put("door_guard", String.valueOf(doorGuard));
        params.put("vibration", String.valueOf(vibration));
        params.put("pressure_switch", String.valueOf(pressureSwitch));
        params.put("brightness", String.valueOf(brightness));
        
        return params;
    }
}
