package com.youngsee.dao;

import java.util.Date;
import java.util.HashMap;

import com.youngsee.network.HttpParamInterface;

public class AirConditionInfo extends DMObject implements HttpParamInterface{
    private float rt1Temper;
    private float refrigeratorOnTemper;
    private float refrigeratorOffTemper;
    private float heaterOnTemper;
    private float heaterOffTemper;
    private float alarmHighTemper;
    private float alarmLowTemper;
    
    private int   alarmHighPressure;
    private int   alarmLowPressure;
    
    public AirConditionInfo(String cpuID, String mac){
        super(cpuID, mac);
    }
    
    public AirConditionInfo(String cpuID, String mac, String name, String group, String model, Date time){
        super(cpuID, mac, name, group, model, time);
    }
    
    public void setRT1Temper(float value){
        rt1Temper = value;
    }
    
    public void setRefrigeratorOnTemper(float value){
        refrigeratorOnTemper = value;
    }
    
    public void setRefrigeratorOffTemper(float value){
        refrigeratorOffTemper = value;
    }
    
    public void setHeaterOnTemper(float value){
        heaterOnTemper = value;
    }
    
    public void setHeaterOffTemper(float value){
        heaterOffTemper = value;
    }
    
    public void setAlarmHighTemper(float value){
        alarmHighTemper = value;
    }
    
    public void setAlarmLowTemper(float value){
        alarmLowTemper = value;
    }
    
    public void setAlarmHighPressure(int value){
        alarmHighPressure = value;
    }
    
    public void setAlarmLowPressure(int value){
        alarmLowPressure = value;
    }
    

    
    @Override
    public HashMap<String, String> getParameters(){
        HashMap<String, String> params = super.getParameters();
        
        params.put("rt1_tem", String.valueOf(rt1Temper));
        params.put("refrigerator_on_tem", String.valueOf(refrigeratorOnTemper));
        params.put("refrigerator_off_tem", String.valueOf(refrigeratorOffTemper));
        params.put("heater_on_tem", String.valueOf(heaterOnTemper));
        params.put("heater_off_tem", String.valueOf(heaterOffTemper));
        params.put("high_alarm_temperature", String.valueOf(alarmHighTemper));
        params.put("low_alarm_temperature", String.valueOf(alarmLowTemper));
        params.put("high_alarm_pressure", String.valueOf(alarmHighPressure));
        params.put("low_alarm_pressure", String.valueOf(alarmLowPressure));

        return params;
    }
}
