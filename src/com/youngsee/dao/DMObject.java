package com.youngsee.dao;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import com.youngsee.network.HttpParamInterface;

public abstract class DMObject implements HttpParamInterface{
    private String mMac;
    private String mCpuID;
    private String mName;
    private String mGroup;
    private String mModel;
    private Date   mTime;
    
    public DMObject(String cpuID, String mac){
        mCpuID = cpuID;
        mMac = mac;
    }
    
    public DMObject(String cpuID, String mac, String name, String group, String model, Date time){
        mMac = mac;
        mCpuID = cpuID;
        mName = name;
        mGroup = group;
        mModel = model;
        mTime = time;
    }
    
    public void setMac(String mac){
        mMac = mac;
    }
    
    public String getMac(){
        return mMac;
    }
    
    public void setCpuID(String cpuID){
        mCpuID = cpuID;
    }
    
    public String getCpuID(){
        return mCpuID;
    }
    
    
    public void setName(String name){
        mName = name;
    }
    
    public String getName(){
        return mName;
    }
    
    
    public void setGroup(String group){
        mGroup = group;
    }
    
    public String getGroup(){
        return mGroup;
    }
    
    
    public void setModel(String model){
        mModel = model;
    }
    
    public String getModel(){
        return mModel;
    }
    
    public void setTime(Date time){
        mTime = time;
    }
    
    public Date getTime(){
        return mTime;
    }
    
    public String getTimeString(){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        
        if(mTime == null){
            return format.format(new Date());
        }
        
        return format.format(mTime);
    }
    
    /**
     * convert the Boolean to Integer.
     * @param value
     * @return
     */
    public String getBooleanString(boolean value){
        return value ? "1" : "0";
    }
    
    public HashMap<String, String> getParameters(){
        HashMap<String, String> params = new HashMap<String, String>();
        
        params.put("cpu_id", mCpuID);
        params.put("mac", mMac == null ? "" : mMac);
        params.put("name", mName == null ? "" : mName);
        params.put("device_group", mGroup == null ? "" : mGroup);
        params.put("model", mModel == null ? "" : mModel);
        params.put("time", getTimeString());

        return params;
    }
}
