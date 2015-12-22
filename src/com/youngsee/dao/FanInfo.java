package com.youngsee.dao;

import java.util.Date;
import java.util.HashMap;

import com.youngsee.network.HttpParamInterface;

public class FanInfo extends DMObject implements HttpParamInterface{
    private float indoorTemper1;
    private float indoorTemper2;
    private int   rotateSpeed1;
    private int   rotateSpeed2;
    private float heaterOnTemper;
    private float heaterOffTemper;
    private float fanOnTemper;
    private float fanOffTemper;
    private float fanLowSpeedTemper;
    private float fanHighSpeedTemper;
    private int   fanLowPower;
    private int   fanHighPower;
    private float alarmLowTemper;
    private float alarmHighTemper;
    
    public FanInfo(String cpuID, String mac){
        super(cpuID, mac);
    }
    
    public FanInfo(String cpuID, String mac, String name, String group, String model, Date time){
        super(cpuID, mac, name, group, model, time);
    }
    
    public void setIndoorTemper1(float value){
        indoorTemper1 = value;
    }
    
    public void setIndoorTemper2(float value){
        indoorTemper2 = value;
    }
    
    public void setRotateSpeed1(int value){
        rotateSpeed1 = value;
    }
    
    public void setRotateSpeed2(int value){
        rotateSpeed2 = value;
    }
    
    public void setHeaterOnTemper(float value){
        heaterOnTemper = value;
    }
    
    public void setHeaterOffTemper(float value){
        heaterOffTemper = value;
    }
    
    public void setFanOnTemper(float value){
        fanOnTemper = value;
    }
    
    public void setFanOffTemper(float value){
        fanOffTemper = value;
    }
    
    public void setFanLowSpeedTemper(float value){
        fanLowSpeedTemper = value;
    }
    
    public void setFanHighSpeedTemper(float value){
        fanHighSpeedTemper = value;
    }
    
    public void setFanLowPower(int value){
        fanLowPower = value;
    }
    
    public void setFanHighPower(int value){
        fanHighPower = value;
    }
    
    public void setAlarmHighTemper(float value){
        alarmHighTemper = value;
    }
    
    public void setAlarmLowTemper(float value){
        alarmLowTemper = value;
    }

    
    @Override
    public HashMap<String, String> getParameters(){
        HashMap<String, String> params = super.getParameters();
        
        params.put("indoor_tem_sensor1", String.valueOf(indoorTemper1));
        params.put("indoor_tem_sensor2", String.valueOf(indoorTemper2));
        params.put("rotate_speed1", String.valueOf(rotateSpeed1));
        params.put("rotate_speed2", String.valueOf(rotateSpeed2));
        params.put("heater_on_tem", String.valueOf(heaterOnTemper));
        params.put("heater_off_tem", String.valueOf(heaterOffTemper));
        params.put("fan_on_tem", String.valueOf(fanOnTemper));
        params.put("fan_off_tem", String.valueOf(fanOffTemper));
        params.put("fan_low_speed_tem", String.valueOf(fanLowSpeedTemper));
        params.put("fan_high_speed_tem", String.valueOf(fanHighSpeedTemper));
        params.put("fan_low_power", String.valueOf(fanLowPower));
        params.put("fan_high_power", String.valueOf(fanHighPower));
        params.put("low_alarm_tem", String.valueOf(alarmLowTemper));
        params.put("high_alarm_tem", String.valueOf(alarmHighTemper));
        
        return params;
    }
}
