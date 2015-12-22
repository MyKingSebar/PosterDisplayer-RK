package com.youngsee.dao;

import java.util.Date;
import java.util.HashMap;

import com.youngsee.network.HttpParamInterface;

public class FanAlarm extends DMObject implements HttpParamInterface{
    private boolean indoorTemper1;
    private boolean indoorTemper2;
    private boolean fan1;
    private boolean fan2;
    private boolean highTemper;
    private boolean lowTemper;

    private boolean heaterState;
    private boolean fanPowerState;
    private boolean innerFanState;
    private boolean outerFanState;
    
    public FanAlarm(String cpuID, String mac){
        super(cpuID, mac);
    }
    
    public FanAlarm(String cpuID, String mac, String name, String group, String model, Date time){
        super(cpuID, mac, name, group, model, time);
    }
    
    public void setIndoorTemper1(boolean value){
        indoorTemper1 = value;
    }
    
    public void setIndoorTemper2(boolean value){
        indoorTemper2 = value;
    }
    
    public void setFan1(boolean value){
        fan1 = value;
    }
    
    public void setFan2(boolean value){
        fan2 = value;
    }
    
    public void setHighTemper(boolean value){
        highTemper = value;
    }
    
    public void setLowTemper(boolean value){
        lowTemper = value;
    }
    
    public void setHeaterState(boolean value){
        heaterState = value;
    }
    
    public void setFanPowerState(boolean value){
        fanPowerState = value;
    }
    
    public void setInnerFanState(boolean value){
        innerFanState = value;
    }
    
    public void setOuterFanState(boolean value){
        outerFanState = value;
    }
    
    @Override
    public HashMap<String, String> getParameters(){
        HashMap<String, String> params = super.getParameters();
        
        params.put("indoor_temp_sensor1", getBooleanString(indoorTemper1));
        params.put("indoor_temp_sensor2", getBooleanString(indoorTemper2));
        params.put("fan1", getBooleanString(fan1));
        params.put("fan2", getBooleanString(fan2));
        params.put("high_temperature", getBooleanString(highTemper));
        params.put("low_temperature", getBooleanString(lowTemper));

        params.put("electric_heater_state", getBooleanString(heaterState));
        params.put("fan_power_state", getBooleanString(fanPowerState));
        params.put("inner_fan_state", getBooleanString(innerFanState));
        params.put("outer_fan_state", getBooleanString(outerFanState));
        
        return params;
    }
}
