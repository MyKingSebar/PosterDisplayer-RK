package com.youngsee.dao;

import java.util.Date;
import java.util.HashMap;

import com.youngsee.network.HttpParamInterface;

public class AirConditionAlarm extends DMObject implements HttpParamInterface{
    private boolean failureRT1;
    private boolean highPressure;
    private boolean lowPressure;
    private boolean highTemperature;
    private boolean lowTemperature;
    private boolean highVoltage;
    private boolean lowVoltage;
    private boolean compressorState;
    private boolean electricHeaterState;
    private boolean innerFanState;
    private boolean outerFanState;
    
    public AirConditionAlarm(String cpuID, String mac){
        super(cpuID, mac);
    }

    public AirConditionAlarm(String cpuID, String mac, String name, String group, String model, Date time){
        super(cpuID, mac, name, group, model, time);
    }
    
    public void setFailureRT1(boolean value){
        failureRT1 = value;
    }
    
    public void setHighPressure(boolean value){
        highPressure = value;
    }
    
    public void setLowPressure(boolean value){
        lowPressure = value;
    }
    
    public void setHighTemperature(boolean value){
        highTemperature = value;
    }
    
    public void setLowTemperature(boolean value){
        lowTemperature = value;
    }
    
    public void setHighVoltage(boolean value){
        highVoltage = value;
    }
    
    public void setLowVoltage(boolean value){
        lowVoltage = value;
    }
    
    public void setCompressorState(boolean value){
        compressorState = value;
    }
    
    public void setElectricHeaterState(boolean value){
        electricHeaterState = value;
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
        
        params.put("sensor_rt1_failure", getBooleanString(failureRT1));
        params.put("compressor_high_pressure", getBooleanString(highPressure));
        params.put("compressor_low_pressure", getBooleanString(lowPressure));
        params.put("high_temperature", getBooleanString(highTemperature));
        params.put("low_temperature", getBooleanString(lowTemperature));
        params.put("high_voltage", getBooleanString(highVoltage));
        params.put("low_voltage", getBooleanString(lowVoltage));
        params.put("compressor_state", getBooleanString(compressorState));
        params.put("electric_heater_state", getBooleanString(electricHeaterState));
        params.put("inner_fan_state", getBooleanString(innerFanState));
        params.put("outer_fan_state", getBooleanString(outerFanState));
        
        return params;
    }
}
