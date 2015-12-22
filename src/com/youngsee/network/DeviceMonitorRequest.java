package com.youngsee.network;

import java.util.HashMap;

import android.os.Handler;
import android.os.Message;

import com.youngsee.dao.AirConditionAlarm;
import com.youngsee.dao.AirConditionInfo;
import com.youngsee.dao.DMObject;
import com.youngsee.dao.DisplayAlarm;
import com.youngsee.dao.FanAlarm;
import com.youngsee.dao.FanInfo;
import com.youngsee.dao.GPSInfo;
import com.youngsee.dao.MonitorDevice;
import com.youngsee.dao.OtherInfo;
import com.youngsee.dao.SwitchingInfo;

/**
 * 空调相关网络接口
 * @author Administrator
 *
 */
public class DeviceMonitorRequest extends HttpRequest{
    private final static String URL_AIRCONDITION = "/AirCondition";
    private final static String URL_DISPLAY = "/Display";
    private final static String URL_FAN = "/Fan";
    private final static String URL_GPS = "/Gps";
    private final static String URL_SWITCHING = "/Switching";
    private final static String URL_OTHER = "/Other";
    private final static String URL_DEVICE = "/MonitorDevice";
    
    /**
     * 主机地址，通过覆盖父类的此函数修改服务器地址
     */
    protected String getHost(){
        return "http://120.25.105.239:8001";
    }
    
    /**
     * 构造函数
     * @param handler 获得服务器结果后通过此handler发送消息通知调用端，Message消息内容如下：
     *              msg.what = code          //此code为请求时传入的标识码
     *              msg.arg1 = NETWORK_OK    //arg1参数为http请求返回的http层的错误码
     *              msg.obj = NWResult       //NWResult对象或其子类的对象
     */
    public DeviceMonitorRequest(Handler handler){
        super(handler);
    }

    /**
     * 提交空调告警信息
     * @param alarm
     * @param code
     * @return
     */
    public boolean submitAirConditionAlarm(AirConditionAlarm alarm, int code){
        return submitAlarm(URL_AIRCONDITION, alarm, code);
    }
    
    /**
     * 提交空调状态信息 
     * @param info
     * @param code
     * @return
     */
    public boolean submitAirConditionInfo(AirConditionInfo info, int code){
        return submitInfo(URL_AIRCONDITION, info, code);
    }
    
    /**
     * 提交风机告警信息
     * @param alarm
     * @param code
     * @return
     */
    public boolean submitFanAlarm(FanAlarm alarm, int code){
        return submitAlarm(URL_FAN, alarm, code);
    }
    
    /**
     * 提交风机状态信息
     * @param info
     * @param code
     * @return
     */
    public boolean submitFanInfo(FanInfo info, int code){
        return submitInfo(URL_FAN, info, code);
    }
    
    /**
     * 提交显示板告警信息
     * @param alarm
     * @param code
     * @return
     */
    public boolean submitDisplayAlarm(DisplayAlarm alarm, int code){
        return submitAlarm(URL_DISPLAY, alarm, code);
    }
    
    /**
     * 提交GPS信息
     * @param info
     * @param code
     * @return
     */
    public boolean submitGPSInfo(GPSInfo info, int code){
        return submitInfo(URL_GPS, info, code);
    }
    
    /**
     * 提交继电器相关信息
     * @param info
     * @param code
     * @return
     */
    public boolean submitSwitchingInfo(SwitchingInfo info, int code){
        return submitInfo(URL_SWITCHING, info, code);
    }
    
    /**
     * 提交其他信息，温度，湿度等
     * @param info
     * @param code
     * @return
     */
    public boolean submitOtherInfo(OtherInfo info, int code){
        return submitInfo(URL_OTHER, info, code);
    }
    
    /**
     * 增加设备信息
     * @param info
     * @param code
     * @return
     */
    public boolean addMonitorDevice(MonitorDevice info, int code){
        if(info == null){
            return false;
        }
        
        HashMap<String, String> params = info.getParameters();
        params.put("command", "addDevice");
        return post(URL_DEVICE, params, code);
    }
    
    /**
     * 提交告警信息
     * @param url
     * @param alarm
     * @param code
     * @return
     */
    protected boolean submitAlarm(String url, DMObject alarm, int code){
        if(alarm == null){
            return false;
        }
        
        HashMap<String, String> params = alarm.getParameters();
        params.put("command", "submitAlarm");
        return post(url, params, code);
    }
    
    /**
     * 提交状态信息
     * 
     * @param url
     * @param info
     * @param code
     * @return
     */
    protected boolean submitInfo(String url, DMObject info, int code){
        if(info == null){
            return false;
        }
        
        HashMap<String, String> params = info.getParameters();
        params.put("command", "submitInfo");
        return post(url, params, code);
    }
}
