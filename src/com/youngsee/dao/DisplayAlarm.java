package com.youngsee.dao;

import java.util.Date;
import java.util.HashMap;

import com.youngsee.network.HttpParamInterface;

public class DisplayAlarm extends DMObject implements HttpParamInterface{
    private boolean awaiting;    // 待机状态 false:开机, true:待机
    private int     signalSource; // 信号源： 1 = av1, 2 = av2, 3 = av3, 5 = pc, 6
                                  // = ypbpr, 8 = dvi, 9 = hdm'
    private int     volume;      // 音量大小
    private int     brightness;  // 亮度
    private int     fanPower;    // 风扇开关状态 0 = 关，1 = 智能 ， 2 = 常开
    private float   temperature; // 机器当前温度
                                  
    private int     contrast;    // 对比度
    private boolean mute;        // 是否静音 false = off ， true = mute
    private int     resolution;  // 屏幕分辨率 , 例如： "1920*1024"
                                  
    public DisplayAlarm(String cpuID, String mac){
        super(cpuID, mac);
    }
    
    public DisplayAlarm(String cpuID, String mac, String name, String group, String model, Date time){
        super(cpuID, mac, name, group, model, time);
    }
    
    public void setAwaiting(boolean value){
        awaiting = value;
    }
    
    public void setSignalSource(int value){
        signalSource = value;
    }
    
    public void setVolume(int value){
        volume = value;
    }
    
    public void setBrightness(int value){
        brightness = value;
    }
    
    public void setFanPower(int value){
        fanPower = value;
    }
    
    public void setTemperature(float value){
        temperature = value;
    }
    
    public void setContrast(int value){
        contrast = value;
    }
    
    public void setMute(boolean value){
        mute = value;
    }
    
    public void setResolution(int value){
        resolution = value;
    }
    
    @Override
    public HashMap<String, String> getParameters(){
        HashMap<String, String> params = super.getParameters();
        
        params.put("awaiting", getBooleanString(awaiting));
        params.put("signal_source", String.valueOf(signalSource));
        params.put("volume", String.valueOf(volume));
        params.put("brightness", String.valueOf(brightness));
        params.put("fan_power", String.valueOf(fanPower));
        params.put("temperature", String.valueOf(temperature));
        params.put("contrast", String.valueOf(contrast));
        params.put("mute", getBooleanString(mute));
        params.put("resolution", String.valueOf(resolution));
        
        return params;
    }
}
