package com.youngsee.envmnt;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import com.youngsee.common.Logger;
import com.youngsee.common.SerialPort;
import com.youngsee.common.SysParamManager;
import com.youngsee.dao.AirConditionAlarm;
import com.youngsee.dao.AirConditionInfo;
import com.youngsee.dao.DisplayAlarm;
import com.youngsee.dao.FanAlarm;
import com.youngsee.dao.FanInfo;
import com.youngsee.dao.GPSInfo;
import com.youngsee.dao.MonitorDevice;
import com.youngsee.dao.NWResult;
import com.youngsee.dao.OtherInfo;
import com.youngsee.dao.SwitchingInfo;
import com.youngsee.network.DeviceMonitorRequest;
import com.youngsee.network.HttpRequest;
import com.youngsee.posterdisplayer.PosterApplication;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

public class EnvMntManager {

	private final int EVENT_BASE = 0x9000;
	private final int EVENT_SEND_SERIAL_DATA = EVENT_BASE + 1;

	private final long DEFAULT_MONITORTHREAD_PERIOD = 60 * 1000;
	private final long DEFAULT_CMD_TIMEOUT = 2 * 1000;

	private final long MAX_READ_COUNT = 3;

	private Logger mLogger = new Logger();

	private final String DEVFILE_SERIALPORT = "/dev/ttyS2";
	private final int BAUTRATE = 9600;

	private ReadThread mReadThread = null;
	private MonitorThread mMonitorThread = null;

	private SerialPort mSerialPort = null;
	private OutputStream mOutputStream = null;
	private InputStream mInputStream = null;

	private int mRecvSize = 0;
	private byte[] mRecvBuf = new byte[EnvMntConstants.MAX_READ_LEN];

	private int mPacketLen = 0;
	private byte[] mPacketBuf = new byte[EnvMntConstants.MAX_READ_LEN];

	private HandlerThread mHandlerThread = null;
	private MyHandler mHandler = null;

	private Object mReadLock = new Object();
	private int mCurrentReadType = EnvMntConstants.READTYPE_NONE;

	private String mCpuId = null;
	private String mMac = null;

	private DeviceMonitorRequest mNetWorkRequest = null;

	private EnvMntManager() {
		mMac = PosterApplication.getEthFormatMac().replace(":", "-").toUpperCase();
		mCpuId = PosterApplication.getCpuId().toUpperCase();
		mNetWorkRequest = new DeviceMonitorRequest(mNetWorkHandler);

		initHandler();

		if (initSerialPort() && configMonitorBoard()) {
			initThreads();
			updateMonitorDevice();
		}
	}

	private static class EnvMntHolder {
        static final EnvMntManager INSTANCE = new EnvMntManager();
    }

	public static EnvMntManager getInstance() {
		return EnvMntHolder.INSTANCE;
	}

	private void initHandler() {
		mHandlerThread = new HandlerThread("epmgr_hthd");
		mHandlerThread.start();
		mHandler = new MyHandler(mHandlerThread.getLooper());
	}

	private boolean initSerialPort(){
		try {
			mSerialPort = new SerialPort(new File(DEVFILE_SERIALPORT), BAUTRATE, 0);
			mOutputStream = mSerialPort.getOutputStream();
			mInputStream = mSerialPort.getInputStream();
		} catch (SecurityException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	private boolean configMonitorBoard() {
		mLogger.i("Config Monitor Board ...");

		setHostOnline();

		return true;
	}

	private void initThreads() {
		mReadThread = new ReadThread();
		mReadThread.start();

    	mMonitorThread = new MonitorThread();
    	mMonitorThread.start();
	}

	public void destroy() {
		if (mMonitorThread != null) {
			mMonitorThread.cancel();
			mMonitorThread = null;
		}
		if (mReadThread != null) {
			mReadThread.cancel();
			mReadThread = null;
		}

		if (mHandler != null) {
			mHandler.removeMessages(EVENT_SEND_SERIAL_DATA);
			mHandler = null;
		}
		if (mHandlerThread != null) {
			mHandlerThread.getLooper().quit();
			mHandlerThread = null;
		}

		if (mSerialPort != null) {
			mSerialPort.close();
			mSerialPort = null;
		}
	}

	private void setHostOnline() {
		sendWriteCmd(EnvMntConstants.CFGSPC_ADDR_0, 0x72);
	}

	private void sendWriteCmd(int addr, int data) {
		byte[] databuf = new byte[4];
		databuf[0] = (byte) (EnvMntConstants.CMD_CFGSPC_WRITE & 0xFF);
		databuf[1] = (byte) ((addr >> 8) & 0xFF);
		databuf[2] = (byte) (addr & 0xFF);
		databuf[3] = (byte) data;

		sendSerialData(databuf);
	}

	public void updateMonitorDevice() {
		MonitorDevice device = new MonitorDevice(mCpuId, mMac);
		device.setName(SysParamManager.getInstance().getTerm());
		device.setGroup(SysParamManager.getInstance().getTermGrp());

		mNetWorkRequest.addMonitorDevice(device, EnvMntConstants.REQUESTCODE_MONITORDEVICE);
	}

	private void sendReadCmd(int type) {
		int addr, len;

		switch (type) {
		case EnvMntConstants.READTYPE_SENSORSPC:
			addr = EnvMntConstants.READADDR_SENSORSPC;
			len = EnvMntConstants.READLEN_SENSORSPC;
			break;
		case EnvMntConstants.READTYPE_AIRCONSTATS:
			addr = EnvMntConstants.READADDR_AIRCONSTATS;
			len = EnvMntConstants.READLEN_AIRCONSTATS;
			break;
		case EnvMntConstants.READTYPE_DRTFANSTATS:
			addr = EnvMntConstants.READADDR_DRTFANSTATS;
			len = EnvMntConstants.READLEN_DRTFANSTATS;
			break;
		case EnvMntConstants.READTYPE_LCDDRVBOARD:
			addr = EnvMntConstants.READADDR_LCDDRVBOARD;
			len = EnvMntConstants.READLEN_LCDDRVBOARD;
			break;
		case EnvMntConstants.READTYPE_LGTRADSENSOR:
			addr = EnvMntConstants.READADDR_LGTRADSENSOR;
			len = EnvMntConstants.READLEN_LGTRADSENSOR;
			break;
		case EnvMntConstants.READTYPE_PM25SENSOR:
			addr = EnvMntConstants.READADDR_PM25SENSOR;
			len = EnvMntConstants.READLEN_PM25SENSOR;
			break;
		case EnvMntConstants.READTYPE_GPSRECVDATA:
			addr = EnvMntConstants.READADDR_GPSRECVDATA;
			len = EnvMntConstants.READLEN_GPSRECVDATA / 16;
			break;
		default:
			mLogger.i("Unknown read type, type = " + type + ".");
			return;
		}

		mCurrentReadType = type;

		byte[] databuf = new byte[4];
		databuf[0] = (byte) (EnvMntConstants.CMD_CFGSPC_READ & 0xFF);
		databuf[1] = (byte) ((addr >> 8) & 0xFF);
		databuf[2] = (byte) (addr & 0xFF);
		databuf[3] = (byte) len;

		sendSerialData(databuf);
	}

	private void readWait() {
		try {
			synchronized (mReadLock) {
				mReadLock.wait(DEFAULT_CMD_TIMEOUT);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void readNotify() {
		synchronized (mReadLock) {
			mReadLock.notify();
		}
	}

	private boolean isDataValid(int type) {
		switch (type) {
		case EnvMntConstants.READTYPE_SENSORSPC:
			return EnvMntParamManager.getInstance().isSensorSpcValid();
		case EnvMntConstants.READTYPE_AIRCONSTATS:
			return EnvMntParamManager.getInstance().isAirConStatsValid();
		case EnvMntConstants.READTYPE_DRTFANSTATS:
			return EnvMntParamManager.getInstance().isDrtFanStatsValid();
		case EnvMntConstants.READTYPE_LCDDRVBOARD:
			return EnvMntParamManager.getInstance().isLcdDrvBoardValid();
		case EnvMntConstants.READTYPE_LGTRADSENSOR:
			return EnvMntParamManager.getInstance().isLgtRadSensorValid();
		case EnvMntConstants.READTYPE_PM25SENSOR:
			return EnvMntParamManager.getInstance().isPm25SensorValid();
		case EnvMntConstants.READTYPE_GPSRECVDATA:
			return EnvMntParamManager.getInstance().isGpsRecvDataValid();
		default:
			mLogger.i("Invalid read type, type = " + type + ".");
			return false;
		}
	}

	private void readMonitorData(int type) {
		int count = 0;

		do {
			sendReadCmd(type);

			readWait();
		} while (!isDataValid(type) && (++count < MAX_READ_COUNT));
	}

	private boolean isAirConNormal(SensorSpcInfo info) {
		return info.uart_state[EnvMntConstants.UARTNUM_AIRCONSTATS] == 0;
	}

	private boolean isDrtFanNormal(SensorSpcInfo info) {
		return info.uart_state[EnvMntConstants.UARTNUM_DRTFANSTATS] == 0;
	}

	private boolean isLcdDrvBoardNormal(SensorSpcInfo info) {
		return info.uart_state[EnvMntConstants.UARTNUM_LCDDRVBOARD] == 0;
	}

	private boolean isLgtRadSensorNormal(SensorSpcInfo info) {
		return info.uart_state[EnvMntConstants.UARTNUM_LGTRADSENSOR] == 0;
	}

	private boolean isPm25SensorNormal(SensorSpcInfo info) {
		return info.uart_state[EnvMntConstants.UARTNUM_PM25SENSOR] == 0;
	}

	private boolean isGpsRecvDataNormal(SensorSpcInfo info) {
		return info.uart_state[EnvMntConstants.UARTNUM_GPSRECVDATA] == 0;
	}

	private void sendMonitorCmds() {
		EnvMntParamManager.getInstance().resetAllValid();

		// Sensor Space
		readMonitorData(EnvMntConstants.READTYPE_SENSORSPC);

		if (!EnvMntParamManager.getInstance().isSensorSpcValid()) {
			mLogger.i("Failed to read sensor space info.");
			mCurrentReadType = EnvMntConstants.READTYPE_NONE;
			return;
		}

		SensorSpcInfo info = EnvMntParamManager.getInstance().getSensorSpcInfo();

		if (isAirConNormal(info)) {
			// Air Conditioner Status
			readMonitorData(EnvMntConstants.READTYPE_AIRCONSTATS);
		}

		if (isDrtFanNormal(info)) {
			// Draft Fan Status
			readMonitorData(EnvMntConstants.READTYPE_DRTFANSTATS);
		}

		if (isLcdDrvBoardNormal(info)) {
			// LCD Driver Board
			readMonitorData(EnvMntConstants.READTYPE_LCDDRVBOARD);
		}

		if (isLgtRadSensorNormal(info)) {
			// Light Radiation Sensor
			readMonitorData(EnvMntConstants.READTYPE_LGTRADSENSOR);
		}

		if (isPm25SensorNormal(info)) {
			// PM2.5 Sensor
			readMonitorData(EnvMntConstants.READTYPE_PM25SENSOR);
		}

		if (isGpsRecvDataNormal(info)) {
			// GPS Received Data
			readMonitorData(EnvMntConstants.READTYPE_GPSRECVDATA);
		}

		mCurrentReadType = EnvMntConstants.READTYPE_NONE;

		submitMonitorData();
	}

	private boolean getBooleanValue(int value) {
		return value != 0 ? true : false;
	}

	private Handler mNetWorkHandler = new Handler(){
		public void handleMessage(Message msg) {
	    	if(msg.arg1 != HttpRequest.NETWORK_OK){
	    		mLogger.i("Network error.");
	            return;
	        }

	        NWResult result = (NWResult)msg.obj;
	        switch(msg.what){
	        case EnvMntConstants.REQUESTCODE_MONITORDEVICE:
	            if(result.getErrorCode() < 0){
	                
	            }

	            break;
	        case EnvMntConstants.REQUESTCODE_AIRCONALARM:
	            if(result.getErrorCode() < 0){
	                
	            }

	            break;
	        case EnvMntConstants.REQUESTCODE_AIRCONINFO:
	            if(result.getErrorCode() < 0){
	                
	            }

	            break;
	        case EnvMntConstants.REQUESTCODE_DRTFANALARM:
	            if(result.getErrorCode() < 0){
	                
	            }

	            break;
	        case EnvMntConstants.REQUESTCODE_DRTFANINFO:
	            if(result.getErrorCode() < 0){
	                
	            }

	            break;
	        case EnvMntConstants.REQUESTCODE_DISPLAYALARM:
	            if(result.getErrorCode() < 0){
	                
	            }

	            break;
	        case EnvMntConstants.REQUESTCODE_SWITCHINFO:
	            if(result.getErrorCode() < 0){
	                
	            }

	            break;
	        case EnvMntConstants.REQUESTCODE_GPSINFO:
	            if(result.getErrorCode() < 0){
	                
	            }

	            break;
	        case EnvMntConstants.REQUESTCODE_OTHERINFO:
	            if(result.getErrorCode() < 0){
	                
	            }

	            break;
	        }
	    }
	};

	private void submitAirConStats() {
		if (!EnvMntParamManager.getInstance().isAirConStatsValid()) {
			mLogger.i("Air conditioner status info is invalid.");
			return;
		}

		AirConStatsInfo statsinfo = EnvMntParamManager.getInstance().getAirConStatsInfo();

		AirConditionAlarm alarm = new AirConditionAlarm(mCpuId, mMac);
		alarm.setFailureRT1(getBooleanValue(statsinfo.rt1_sensor_error));
		alarm.setHighPressure(getBooleanValue(statsinfo.compressor_high_pressure_alarm));
		alarm.setLowPressure(getBooleanValue(statsinfo.compressor_low_pressure_alarm));
		alarm.setHighTemperature(getBooleanValue(statsinfo.high_temp_alarm));
		alarm.setLowTemperature(getBooleanValue(statsinfo.low_temp_alarm));
		alarm.setHighVoltage(getBooleanValue(statsinfo.power_input_overvoltage_protect));
		alarm.setLowVoltage(getBooleanValue(statsinfo.power_input_undervoltage_protect));
		alarm.setCompressorState(getBooleanValue(statsinfo.compressor_status));
		alarm.setElectricHeaterState(getBooleanValue(statsinfo.eheating_status));
		alarm.setInnerFanState(getBooleanValue(statsinfo.inside_draftfan_status));
		alarm.setOuterFanState(getBooleanValue(statsinfo.outside_draftfan_status));

		mNetWorkRequest.submitAirConditionAlarm(alarm, EnvMntConstants.REQUESTCODE_AIRCONALARM);

		AirConditionInfo info = new AirConditionInfo(mCpuId, mMac);
		info.setRT1Temper(statsinfo.rt1_temp);
		info.setRefrigeratorOnTemper(statsinfo.refrigeration_start_temp);
		info.setRefrigeratorOffTemper(statsinfo.refrigeration_stop_temp);
		info.setHeaterOnTemper(statsinfo.heating_start_temp);
		info.setHeaterOffTemper(statsinfo.heating_stop_temp);
		info.setAlarmHighTemper(statsinfo.high_temp_alarm_setting);
		info.setAlarmLowTemper(statsinfo.low_temp_alarm_setting);
		info.setAlarmLowPressure(statsinfo.underpressure_alarm_setting);
		info.setAlarmHighPressure(statsinfo.overpressure_alarm_setting);

		mNetWorkRequest.submitAirConditionInfo(info, EnvMntConstants.REQUESTCODE_AIRCONINFO);
	}

	private void submitDrtFanStats() {
		if (!EnvMntParamManager.getInstance().isDrtFanStatsValid()) {
			mLogger.i("Draft fan status info is invalid.");
			return;
		}

		DrtFanStatsInfo statsinfo = EnvMntParamManager.getInstance().getDrtFanStatsInfo();

		FanAlarm alarm = new FanAlarm(mCpuId, mMac);
		alarm.setIndoorTemper1(getBooleanValue(statsinfo.indoor_temp_sensor1_error));
		alarm.setIndoorTemper2(getBooleanValue(statsinfo.indoor_temp_sensor2_error));
		alarm.setFan1(getBooleanValue(statsinfo.draftfan1_error));
		alarm.setFan2(getBooleanValue(statsinfo.draftfan2_error));
		alarm.setHighTemper(getBooleanValue(statsinfo.high_temp_alarm));
		alarm.setLowTemper(getBooleanValue(statsinfo.low_temp_alarm));
		alarm.setHeaterState(getBooleanValue(statsinfo.eheating_status));
		alarm.setFanPowerState(getBooleanValue(statsinfo.draftfan_power_status));

		mNetWorkRequest.submitFanAlarm(alarm, EnvMntConstants.REQUESTCODE_DRTFANINFO);

		FanInfo info = new FanInfo(mCpuId, mMac);
		info.setIndoorTemper1(statsinfo.indoor_temp_sensor1_temp);
		info.setIndoorTemper2(statsinfo.indoor_temp_sensor2_temp);
		info.setRotateSpeed1(statsinfo.draftfan1_speed);
		info.setRotateSpeed2(statsinfo.draftfan2_speed);
		info.setHeaterOnTemper(statsinfo.eheating_start_temp);
		info.setHeaterOffTemper(statsinfo.eheating_stop_temp);
		info.setFanOnTemper(statsinfo.draftfan_power_start_temp);
		info.setFanOffTemper(statsinfo.draftfan_power_stop_temp);
		info.setFanLowSpeedTemper(statsinfo.draftfan_lowspeed_temp);
		info.setFanHighSpeedTemper(statsinfo.draftfan_highspeed_temp);
		info.setFanLowPower(statsinfo.draftfan_low_speed_running_efficiency);
		info.setFanHighPower(statsinfo.draftfan_high_speed_running_efficiency);
		info.setAlarmLowTemper(statsinfo.alarm_lower_limit_temp);
		info.setAlarmHighTemper(statsinfo.alarm_upper_limit_temp);

		mNetWorkRequest.submitFanInfo(info, EnvMntConstants.REQUESTCODE_DRTFANINFO);
	}

	private int getSwitchValue(int enable, int status) {
		if (enable != 0) {
			return -1;
		}

		return status;
	}

	private void submitOtherStats() {
		if (EnvMntParamManager.getInstance().isLcdDrvBoardValid()) {
			// Display Alarm
			LcdDrvBoardInfo lcddrvboardinfo = EnvMntParamManager.getInstance().getLcdDrvBoardInfo();
	
			DisplayAlarm displayalarm = new DisplayAlarm(mCpuId, mMac);
			displayalarm.setAwaiting(getBooleanValue(lcddrvboardinfo.standby_status));
			displayalarm.setSignalSource(lcddrvboardinfo.current_signal_source);
			displayalarm.setVolume(lcddrvboardinfo.sound_volume);
			displayalarm.setBrightness(lcddrvboardinfo.image_brightness);
			displayalarm.setFanPower(lcddrvboardinfo.fan_switch_status);
			displayalarm.setTemperature(lcddrvboardinfo.current_temp);
			displayalarm.setContrast(lcddrvboardinfo.image_contrast);
			displayalarm.setMute(getBooleanValue(lcddrvboardinfo.mute_status));
			displayalarm.setResolution(lcddrvboardinfo.image_resolution);
	
			mNetWorkRequest.submitDisplayAlarm(displayalarm, EnvMntConstants.REQUESTCODE_DISPLAYALARM);
		} else {
			mLogger.i("Lcd driver board info is invalid.");
		}

		// Switch Info
		SensorSpcInfo sensorspcinfo = EnvMntParamManager.getInstance().getSensorSpcInfo();

		SwitchingInfo switchinfo = new SwitchingInfo(mCpuId, mMac);
		switchinfo.setWaterInvasion(getSwitchValue(sensorspcinfo.sw_enable[0], sensorspcinfo.sw_state[0]));
		switchinfo.setDoorGuard(getSwitchValue(sensorspcinfo.sw_enable[1], sensorspcinfo.sw_state[1]));
		switchinfo.setVibration(getSwitchValue(sensorspcinfo.sw_enable[2], sensorspcinfo.sw_state[2]));
		switchinfo.setPressureSwitch(getSwitchValue(sensorspcinfo.sw_enable[3], sensorspcinfo.sw_state[3]));
		switchinfo.setBrightness(getSwitchValue(sensorspcinfo.lv_enable[0], sensorspcinfo.lv_state[0]));

		mNetWorkRequest.submitSwitchingInfo(switchinfo, EnvMntConstants.REQUESTCODE_SWITCHINFO);

		if (EnvMntParamManager.getInstance().isGpsRecvDataValid()) {
			// GPS Info
			GpsRecvDataInfo recvdatainfo = EnvMntParamManager.getInstance().getGpsRecvDataInfo();
	
			GPSInfo gpsinfo = new GPSInfo(mCpuId, mMac);
			gpsinfo.setLatitude(recvdatainfo.latitude);
			gpsinfo.setNorth((recvdatainfo.latitude_indication != "S") ? true : false);
			gpsinfo.setLongitude(recvdatainfo.longitude);
			gpsinfo.setEast((recvdatainfo.latitude_indication != "W") ? true : false);
			gpsinfo.setAltitude(recvdatainfo.altitude);
	
			mNetWorkRequest.submitGPSInfo(gpsinfo, EnvMntConstants.REQUESTCODE_GPSINFO);
		} else {
			mLogger.i("GPS received data info is invalid.");
		}

		boolean islgtradsensorvalid = EnvMntParamManager.getInstance().isLgtRadSensorValid();
		boolean ispm25sensorvalid = EnvMntParamManager.getInstance().isPm25SensorValid();
		boolean issensor1valid = sensorspcinfo.sensor_state[0] == 0;
		boolean issensor2valid = sensorspcinfo.sensor_state[1] == 0;
		if (!islgtradsensorvalid && !ispm25sensorvalid && !issensor1valid && !issensor2valid) {
			mLogger.i("Light radiation sensor info, pm 2.5 sensor info, sensor1 and sensor2 all are invalid.");
			return;
		}

		// Other Info
		OtherInfo otherinfo = new OtherInfo(mCpuId, mMac);
		if (islgtradsensorvalid) {
			LgtRadSensorInfo lgtradsensorinfo = EnvMntParamManager.getInstance().getLgtRadSensorInfo();
			otherinfo.setRadiation(lgtradsensorinfo.radiation);
		}
		if (ispm25sensorvalid) {
			Pm25SensorInfo pm25sensorinfo = EnvMntParamManager.getInstance().getPm25SensorInfo();
			otherinfo.setPM25(pm25sensorinfo.pm25);
		}
		if (issensor1valid) {
			otherinfo.setTemperature1(sensorspcinfo.temp0);
			otherinfo.setHumidity1(sensorspcinfo.humidity0);
		}
		if (issensor2valid) {
			otherinfo.setTemperature2(sensorspcinfo.temp1);
			otherinfo.setHumidity2(sensorspcinfo.humidity1);
		}

		mNetWorkRequest.submitOtherInfo(otherinfo, EnvMntConstants.REQUESTCODE_OTHERINFO);
	}

	private void submitMonitorData() {
		// Air Conditioner
		submitAirConStats();
		// Draft Fan
		submitDrtFanStats();
		// Other
		submitOtherStats();
	}

	private final class MonitorThread extends Thread {
		private boolean mIsCanceled = false;

		public void cancel() {
        	mLogger.i("Cancel the monitor thread.");
        	mIsCanceled = true;
            interrupt();
        }

		@Override
		public void run() {
			mLogger.i("A new monitor thread is started. Thread id is " + getId() + ".");

			while (!mIsCanceled) {
                try {
                	sendMonitorCmds();

                	Thread.sleep(DEFAULT_MONITORTHREAD_PERIOD);
                } catch (InterruptedException e) {
                	e.printStackTrace();
                }
            }

            mLogger.i("Monitor thread is safely terminated, id is: " + currentThread().getId());
		}
	}

	private void handleSensorSpc(byte[] data) {
		int count = 0;

		SensorSpcInfo info = new SensorSpcInfo();

		for (int i = 0; i < EnvMntConstants.CHANNEL_NUM; i++) {
			info.sw_enable[i] = (data[count] >> i) & 1;
		}

		count++;

		for (int i = 0; i < EnvMntConstants.CHANNEL_NUM; i++) {
			info.sw_state[i] = (data[count] >> i) & 1;
		}

		count++;

		for (int i = 0; i < EnvMntConstants.CHANNEL_NUM; i++) {
			info.lv_enable[i] = (data[count] >> i) & 1;
		}

		count++;

		for (int i = 0; i < EnvMntConstants.CHANNEL_NUM; i++) {
			info.lv_state[i] = (data[count] >> i) & 1;
		}

		count++;

		info.humidity0 = getFloatValue(data[count], data[count+1] & 0xFF);

		count += 2;

		info.temp0 = ((((data[count] >> 7) & 1) != 1) ? 1 : -1)
				* (getIntValue(data[count] & 0x7F, data[count+1] & 0xFF) / 10f);

		count += 2;

		info.dht0_sum = data[count++] & 0xFF;

		info.humidity1 = getFloatValue(data[count], data[count+1] & 0xFF);

		count += 2;

		info.temp1 = ((((data[count] >> 7) & 1) != 1) ? 1 : -1)
				* (getIntValue(data[count] & 0x7F, data[count+1] & 0xFF) / 10f);

		count += 2;

		info.dht1_sum = data[count++] & 0xFF;

		info.gps_state = (data[count] >> 6) & 1;

		for (int i = 0; i < EnvMntConstants.UART_NUM; i++) {
			info.uart_state[i] = (data[count] >> i) & 1;
		}

		count++;

		for (int i = 0; i < EnvMntConstants.SENSOR_NUM; i++) {
			info.sensor_state[i] = (data[count] >> i) & 1;
		}

		count++;

		info.version = data[count] & 0xFF;

		EnvMntParamManager.getInstance().setSensorSpcInfo(info);
		EnvMntParamManager.getInstance().setSensorSpcValid(true);
	}

	private float getFloatValue(int high_byte, int low_byte) {
		return ((high_byte << 8) | low_byte) / 10f;
	}

	private int getIntValue(int high_byte, int low_byte) {
		return (high_byte << 8) | low_byte;
	}

	private void handleAirConStats(byte[] data) {
		int count = 0;

		AirConStatsInfo info = new AirConStatsInfo();

		info.command1 = ((data[count++] & 0xFF) << 16) | ((data[count++] & 0xFF) << 8)
				| (data[count++] & 0xFF);

		info.rt1_sensor_error = data[count] & 1;
		info.rt2_sensor_error = (data[count] >> 1) & 1;
		info.humidity_sensor_error = (data[count] >> 4) & 1;
		info.eeprom_setting_error = (data[count] >> 6) & 1;
		info.slave_onoff = (data[count++] >> 7) & 1;

		info.high_temp_alarm = data[count] & 1;
		info.low_temp_alarm = (data[count] >> 1) & 1;
		info.smoke_alarm = (data[count] >> 3) & 1;
		info.compressor_high_pressure_alarm = (data[count] >> 4) & 1;
		info.compressor_low_pressure_alarm = (data[count] >> 5) & 1;
		info.dooropen_alarm = (data[count] >> 6) & 1;
		info.vibration_alarm = (data[count++] >> 7) & 1;

		info.compressor_status = data[count] & 1;
		info.eheating_status = (data[count] >> 1) & 1;
		info.inside_draftfan_status = (data[count] >> 2) & 1;
		info.outside_draftfan_status = (data[count] >> 3) & 1;
		info.emergency_draftfan_status = (data[count] >> 4) & 1;
		info.inside_draftfan2_status = (data[count] >> 5) & 1;
		info.outside_draftfan2_status = (data[count] >> 6) & 1;
		info.power_input_undervoltage_protect = (data[count++] >> 7) & 1;

		info.power_input_overvoltage_protect = data[count] & 1;
		info.ultra_temp_alarm = (data[count++] >> 1) & 1;

		info.crc1 = getIntValue(data[count] & 0xFF, data[count+1] & 0xFF);

		count += 2;

		info.command2 = ((data[count++] & 0xFF) << 16) | ((data[count++] & 0xFF) << 8)
				| (data[count++] & 0xFF);

		info.rt1_temp = getFloatValue(data[count], data[count+1] & 0xFF);

		count += 2;

		info.rt2_temp = getFloatValue(data[count], data[count+1] & 0xFF);

		count += 6; // 4 bytes for reserved.

		info.humidity = getFloatValue(data[count], data[count+1] & 0xFF);

		count += 16; // 14 bytes for reserved.

		info.inner_draftfan1_speed = getIntValue(data[count] & 0xFF, data[count+1] & 0xFF);

		count += 2;

		info.inner_draftfan2_speed = getIntValue(data[count] & 0xFF, data[count+1] & 0xFF);

		count += 2;

		info.outer_draftfan1_speed = getIntValue(data[count] & 0xFF, data[count+1] & 0xFF);

		count += 2;

		info.outer_draftfan2_speed = getIntValue(data[count] & 0xFF, data[count+1] & 0xFF);

		count += 2;

		info.refrigeration_start_temp = getFloatValue(data[count], data[count+1] & 0xFF);

		count += 2;

		info.refrigeration_stop_temp = getFloatValue(data[count], data[count+1] & 0xFF);

		count += 2;

		info.heating_start_temp = getFloatValue(data[count], data[count+1] & 0xFF);

		count += 2;

		info.heating_stop_temp = getFloatValue(data[count], data[count+1] & 0xFF);

		count += 2;

		info.high_temp_alarm_setting = getFloatValue(data[count], data[count+1] & 0xFF);

		count += 2;

		info.low_temp_alarm_setting = getFloatValue(data[count], data[count+1] & 0xFF);

		count += 2;

		info.ultra_temp_alarm_setting = getFloatValue(data[count], data[count+1] & 0xFF);

		count += 2;

		info.airconditioner_return_diff = getFloatValue(data[count], data[count+1] & 0xFF);

		count += 2;

		info.inner_recycle_stop_temp = getFloatValue(data[count], data[count+1] & 0xFF);

		count += 2;

		info.inner_recycle_running_temp = getFloatValue(data[count], data[count+1] & 0xFF);

		count += 2;

		info.inner_recycle_acceleration_temp = getFloatValue(data[count], data[count+1] & 0xFF);

		count += 2;

		info.inner_recycle_highspeed_temp = getFloatValue(data[count], data[count+1] & 0xFF);

		count += 2;

		info.inner_recycle_fullspeed_temp = getFloatValue(data[count], data[count+1] & 0xFF);

		count += 2;

		info.outer_recycle_stop_temp = getFloatValue(data[count], data[count+1] & 0xFF);

		count += 2;

		info.outer_recycle_running_temp = getFloatValue(data[count], data[count+1] & 0xFF);

		count += 2;

		info.outer_recycle_acceleration_temp = getFloatValue(data[count], data[count+1] & 0xFF);

		count += 2;

		info.outer_recycle_highspeed_temp = getFloatValue(data[count], data[count+1] & 0xFF);

		count += 2;

		info.outer_recycle_fullspeed_temp = getFloatValue(data[count], data[count+1] & 0xFF);

		count += 2;

		info.rt1_temp_sensor_correct_value = getFloatValue(data[count], data[count+1] & 0xFF);

		count += 2;

		info.rt2_temp_sensor_correct_value = getFloatValue(data[count], data[count+1] & 0xFF);

		count += 2;

		info.inner_recycle_draftfan1_setting = getIntValue(data[count] & 0xFF, data[count+1] & 0xFF);

		count += 2;

		info.inner_recycle_draftfan2_setting = getIntValue(data[count] & 0xFF, data[count+1] & 0xFF);

		count += 2;

		info.outer_recycle_draftfan1_setting = getIntValue(data[count] & 0xFF, data[count+1] & 0xFF);

		count += 2;

		info.outer_recycle_draftfan2_setting = getIntValue(data[count] & 0xFF, data[count+1] & 0xFF);

		count += 2;

		info.compressor_setting = getIntValue(data[count] & 0xFF, data[count+1] & 0xFF);

		count += 2;

		info.eheating_setting = getIntValue(data[count] & 0xFF, data[count+1] & 0xFF);

		count += 2;

		info.relay_a_setting = getIntValue(data[count] & 0xFF, data[count+1] & 0xFF);

		count += 2;

		info.relay_b_setting = getIntValue(data[count] & 0xFF, data[count+1] & 0xFF);

		count += 2;

		info.relay_c_setting = getIntValue(data[count] & 0xFF, data[count+1] & 0xFF);

		count += 2;

		info.p1_protect_input_setting = getIntValue(data[count] & 0xFF, data[count+1] & 0xFF);

		count += 2;

		info.p2_protect_input_setting = getIntValue(data[count] & 0xFF, data[count+1] & 0xFF);

		count += 2;

		info.rt1_sensor_enable_setting = getIntValue(data[count] & 0xFF, data[count+1] & 0xFF);

		count += 2;

		info.rt2_sensor_enable_setting = getIntValue(data[count] & 0xFF, data[count+1] & 0xFF);

		count += 2;

		info.humidity_sensor_enable_setting = getIntValue(data[count] & 0xFF, data[count+1] & 0xFF);

		count += 2;

		info.underpressure_alarm_setting = getIntValue(data[count] & 0xFF, data[count+1] & 0xFF);

		count += 2;

		info.overpressure_alarm_setting = getIntValue(data[count] & 0xFF, data[count+1] & 0xFF);

		count += 2;

		info.inside_draftfan_feedback_pulse = getIntValue(data[count] & 0xFF, data[count+1] & 0xFF);

		count += 2;

		info.outside_draftfan_feedback_pulse = getIntValue(data[count] & 0xFF, data[count+1] & 0xFF);

		count += 2;

		info.mute_mode_setting = getIntValue(data[count] & 0xFF, data[count+1] & 0xFF);

		count += 2;

		info.system_onoff = getIntValue(data[count] & 0xFF, data[count+1] & 0xFF);

		count += 2;

		info.buzzer_mode = getIntValue(data[count] & 0xFF, data[count+1] & 0xFF);

		count += 2;

		info.lcd_contrast_setting = getIntValue(data[count] & 0xFF, data[count+1] & 0xFF);

		count += 2;

		info.crc2 = getIntValue(data[count] & 0xFF, data[count+1] & 0xFF);

		EnvMntParamManager.getInstance().setAirConStatsInfo(info);
		EnvMntParamManager.getInstance().setAirConStatsValid(true);
	}

	private void handleDrtFanStats(byte[] data) {
		int count = 0;

		DrtFanStatsInfo info = new DrtFanStatsInfo();

		info.command1 = ((data[count++] & 0xFF) << 16) | ((data[count++] & 0xFF) << 8)
				| (data[count++] & 0xFF);

		info.indoor_temp_sensor1_error = data[count] & 1;
		info.indoor_temp_sensor2_error = (data[count] >> 1) & 1;
		info.eeprom_storage_error = (data[count] >> 5) & 1;
		info.high_temp_alarm = (data[count] >> 6) & 1;
		info.low_temp_alarm = (data[count++] >> 7) & 1;

		info.draftfan1_error = data[count] & 1;
		info.draftfan2_error = (data[count] >> 1) & 1;
		info.smoke_sensor_alarm = (data[count] >> 3) & 1;
		info.poweronoff_status = (data[count++] >> 7) & 1;

		info.eheating_status = data[count] & 1;
		info.draftfan_power_status = (data[count] >> 1) & 1;
		info.lowpressure_alarm = (data[count++] >> 7) & 1;

		info.highpressure_alarm = data[count++] & 1;

		info.crc1 = getIntValue(data[count] & 0xFF, data[count+1] & 0xFF);

		count += 2;

		info.command2 = ((data[count++] & 0xFF) << 16) | ((data[count++] & 0xFF) << 8)
				| (data[count++] & 0xFF);

		info.indoor_temp_sensor1_temp = getFloatValue(data[count], data[count+1] & 0xFF);

		count += 2;

		info.indoor_temp_sensor2_temp = getFloatValue(data[count], data[count+1] & 0xFF);

		count += 10; // 8 bytes for reserved.

		info.draftfan1_speed = getIntValue(data[count] & 0xFF, data[count+1] & 0xFF);

		count += 2;

		info.draftfan2_speed = getIntValue(data[count] & 0xFF, data[count+1] & 0xFF);

		count += 6; // 4 bytes for reserved.

		info.eheating_start_temp = getFloatValue(data[count], data[count+1] & 0xFF);

		count += 2;

		info.eheating_stop_temp = getFloatValue(data[count], data[count+1] & 0xFF);

		count += 2;

		info.draftfan_power_start_temp = getFloatValue(data[count], data[count+1] & 0xFF);

		count += 2;

		info.draftfan_power_stop_temp = getFloatValue(data[count], data[count+1] & 0xFF);

		count += 2;

		info.draftfan_lowspeed_temp = getFloatValue(data[count], data[count+1] & 0xFF);

		count += 2;

		info.draftfan_highspeed_temp = getFloatValue(data[count], data[count+1] & 0xFF);

		count += 2;

		info.draftfan_low_speed_running_efficiency = getIntValue(data[count] & 0xFF, data[count+1] & 0xFF);

		count += 2;

		info.draftfan_high_speed_running_efficiency = getIntValue(data[count] & 0xFF, data[count+1] & 0xFF);

		count += 2;

		info.draftfan1_feedback_number = getIntValue(data[count] & 0xFF, data[count+1] & 0xFF);

		count += 2;

		info.draftfan2_feedback_number = getIntValue(data[count] & 0xFF, data[count+1] & 0xFF);

		count += 2;

		info.smoke_sensor_alarm_setting = getIntValue(data[count] & 0xFF, data[count+1] & 0xFF);

		count += 2;

		info.alarm_lower_limit_voltage = getIntValue(data[count] & 0xFF, data[count+1] & 0xFF);

		count += 2;

		info.alarm_upper_limit_voltage = getIntValue(data[count] & 0xFF, data[count+1] & 0xFF);

		count += 2;

		info.alarm_lower_limit_temp = getFloatValue(data[count], data[count+1] & 0xFF);

		count += 2;

		info.alarm_upper_limit_temp = getFloatValue(data[count], data[count+1] & 0xFF);

		count += 2;

		info.sensor1_correct_value = getFloatValue(data[count], data[count+1] & 0xFF);

		count += 2;

		info.sensor2_correct_value = getFloatValue(data[count], data[count+1] & 0xFF);

		count += 2;

		info.system_onoff = getIntValue(data[count] & 0xFF, data[count+1] & 0xFF);

		count += 2;

		info.crc2 = getIntValue(data[count] & 0xFF, data[count+1] & 0xFF);

		EnvMntParamManager.getInstance().setDrtFanStatsInfo(info);
		EnvMntParamManager.getInstance().setDrtFanStatsValid(true);
	}

	private void handleLcdDrvBoard(byte[] data) {
		int count = 0;

		LcdDrvBoardInfo info = new LcdDrvBoardInfo();

		info.start_flag = data[count++] & 0xFF;
		if (info.start_flag != 0xFF) {
			mLogger.i("The start flag of LCD driver board is invalid, flag = " + info.start_flag + ".");
			return;
		}
		info.standby_status = data[count++] & 0xFF;
		info.current_signal_source = data[count++] & 0xFF;
		info.sound_volume = data[count++] & 0xFF;
		info.image_brightness = data[count++] & 0xFF;
		info.fan_switch_status = data[count++] & 0xFF;
		info.current_temp = data[count++] & 0xFF;
		info.image_contrast = data[count++] & 0xFF;
		info.mute_status = data[count++] & 0xFF;
		info.image_resolution = data[count++] & 0xFF;
		info.end_flag1 = data[count++] & 0xFF;
		info.end_flag2 = data[count++] & 0xFF;

		EnvMntParamManager.getInstance().setLcdDrvBoardInfo(info);
		EnvMntParamManager.getInstance().setLcdDrvBoardValid(true);
	}

	private void handleLgtRadSensor(byte[] data) {
		int count = 0;

		LgtRadSensorInfo info = new LgtRadSensorInfo();

		info.address = data[count++] & 0xFF;
		if (info.address != 0x01) {
			mLogger.i("The address domain of light radiation sensor is invalid, addr = "
					+ info.address + ".");
			return;
		}

		info.function = data[count++] & 0xFF;
		if (info.function != 0x85) {
			mLogger.i("The function domain of light radiation sensor is invalid, func = "
					+ info.function + ".");
			return;
		}

		info.datalength = getIntValue(data[count+1] & 0xFF, data[count] & 0xFF);
		if (info.datalength != 0x0004) {
			mLogger.i("The data domain length of light radiation sensor is invalid, len = "
					+ info.datalength + ".");
			return;
		}

		count += 4; // 2 bytes for reserved.

		info.radiation = getIntValue(data[count+1] & 0xFF, data[count] & 0xFF);

		count += 2;

		info.checksum = getIntValue(data[count+1] & 0xFF, data[count] & 0xFF);

		EnvMntParamManager.getInstance().setLgtRadSensorInfo(info);
		EnvMntParamManager.getInstance().setLgtRadSensorValid(true);
	}

	private void handlePm25Sensor(byte[] data) {
		Pm25SensorInfo info = new Pm25SensorInfo();

		info.start_flag = data[0] & 0xFF;
		if (info.start_flag != 0xAA) {
			mLogger.i("The start flag of PM2.5 sensor is invalid, flag = " + info.start_flag + ".");
			return;
		}

		info.end_flag = data[6] & 0xFF;
		if (info.end_flag != 0xFF) {
			mLogger.i("The stop flag of PM2.5 sensor is invalid, flag = " + info.end_flag + ".");
			return;
		}

		int calc_checksum = (data[1] & 0xFF) + (data[2] & 0xFF)
				+ (data[3] & 0xFF) + (data[4] & 0xFF);

		info.checksum = data[5] & 0xFF;
		if (calc_checksum != info.checksum) {
			mLogger.i("The checksum of PM2.5 sensor is incorrect, calculated checksum = "
					+ calc_checksum + " checksum = " + info.checksum + ".");
			return;
		}

		info.pm25 = (((data[4] & 0xFF) << 24) + ((data[3] & 0xFF) << 16)
				+ ((data[2] & 0xFF) << 8) + (data[1] & 0xFF)) / 100f;

		EnvMntParamManager.getInstance().setPm25SensorInfo(info);
		EnvMntParamManager.getInstance().setPm25SensorValid(true);
	}

	private void handleGpsRecvData(byte[] data) {
		String datastr = new String(data);

		int gpgga_start_pos = datastr.indexOf("$GPGGA");
		if (gpgga_start_pos != -1) {
			int gpgga_end_pos = datastr.indexOf("\r\n", gpgga_start_pos);
			if (gpgga_end_pos != -1) {
				String gpgga_str = datastr.substring(gpgga_start_pos, gpgga_end_pos);
				String[] gpgga_array = gpgga_str.split(",");
				if (gpgga_array == null) {
					mLogger.i("GPGGA array is null.");
					return;
				} else if (gpgga_array.length != 15) {
					mLogger.i("The length of GPGGA is invalid, len = " + gpgga_array.length + ".");
					return;
				} else if (gpgga_array[1].equals("") || gpgga_array[2].equals("") || gpgga_array[3].equals("")
						|| gpgga_array[4].equals("") || gpgga_array[5].equals("") || gpgga_array[6].equals("")
						|| gpgga_array[7].equals("") || gpgga_array[8].equals("") || gpgga_array[9].equals("")
						|| gpgga_array[10].equals("") || gpgga_array[11].equals("") || gpgga_array[12].equals("")
						|| gpgga_array[13].equals("") || gpgga_array[14].equals("")) {
					mLogger.i("Invalid GPS data: " + gpgga_str);
					return;
				}

				GpsRecvDataInfo info = new GpsRecvDataInfo();

				info.latitude = gpgga_array[2];
				info.latitude_indication = gpgga_array[3];
				info.longitude = gpgga_array[4];
				info.longitude_indication = gpgga_array[5];
				info.location_indication = Integer.parseInt(gpgga_array[6]);
				info.satellite_number = Integer.parseInt(gpgga_array[7]);
				info.altitude = Float.parseFloat(gpgga_array[9]);

				EnvMntParamManager.getInstance().setGpsRecvDataInfo(info);
				EnvMntParamManager.getInstance().setGpsRecvDataValid(true);
			} else {
				mLogger.i("Can't find the end flag of the GPGGA data.");
			}
		} else {
			mLogger.i("Can't find \"$GPGGA\" in the gps data.");
		}
	}

	private int getExpectedLength(int type) {
		switch (type) {
		case EnvMntConstants.READTYPE_SENSORSPC:
			return EnvMntConstants.READLEN_SENSORSPC;
		case EnvMntConstants.READTYPE_AIRCONSTATS:
			return EnvMntConstants.READLEN_AIRCONSTATS;
		case EnvMntConstants.READTYPE_DRTFANSTATS:
			return EnvMntConstants.READLEN_DRTFANSTATS;
		case EnvMntConstants.READTYPE_LCDDRVBOARD:
			return EnvMntConstants.READLEN_LCDDRVBOARD;
		case EnvMntConstants.READTYPE_LGTRADSENSOR:
			return EnvMntConstants.READLEN_LGTRADSENSOR;
		case EnvMntConstants.READTYPE_PM25SENSOR:
			return EnvMntConstants.READLEN_PM25SENSOR;
		case EnvMntConstants.READTYPE_GPSRECVDATA:
			return EnvMntConstants.READLEN_GPSRECVDATA;
		default:
			mLogger.i("Unknown read type, type = " + type + ".");
			return -1;
		}
	}

	private void onDataReceived(byte[] buffer, int size) {
		if (mPacketLen == 0) {
			if ((size == 1) && (buffer[0] == EnvMntConstants.CMDRESP_CFGSPC_WRITE)) {
				mLogger.e("Receive the writing response ("
						+ EnvMntConstants.CMDRESP_CFGSPC_WRITE + ").");
				return;
			} else if ((buffer[0] != 'B') || (buffer[1] != 'J')
					|| (buffer[2] != 'Y') || (buffer[3] != 'S')) {
				mLogger.e("Received data is invalid, data[0] = " + buffer[0]
						+ " data[1] = " + buffer[1] + " data[2] = " + buffer[2]
						+ " data[3] = " + buffer[3] + ".");
				return;
			}

			if (buffer[4] != mCurrentReadType) {
				mLogger.e("The type of received data is incorrect, ptype = " + buffer[4]
						+ " rtype = " + mCurrentReadType + ".");
				//return; // Just for a hardware error.
			}

			int len = size - EnvMntConstants.READ_HEAD_LEN;
			System.arraycopy(buffer, EnvMntConstants.READ_HEAD_LEN, mPacketBuf, 0, len);
			mPacketLen = len;
		} else {
			int len = mPacketLen + size;
			if (len > (EnvMntConstants.MAX_READ_LEN - EnvMntConstants.READ_HEAD_LEN)) {
				mLogger.e("The current total length of received data is invalid, len = " + len + ".");
				// Reset the packet buffer.
				Arrays.fill(mPacketBuf, (byte) 0);
				mPacketLen = 0;
				return;
			}

			System.arraycopy(buffer, 0, mPacketBuf, mPacketLen, size);
			mPacketLen += size;
		}

		if (mPacketLen != (getExpectedLength(mCurrentReadType) - EnvMntConstants.READ_HEAD_LEN)) {
			return;
		}

		switch (mCurrentReadType) {
		case EnvMntConstants.READTYPE_SENSORSPC:
			handleSensorSpc(mPacketBuf);
			break;
		case EnvMntConstants.READTYPE_AIRCONSTATS:
			handleAirConStats(mPacketBuf);
			break;
		case EnvMntConstants.READTYPE_DRTFANSTATS:
			handleDrtFanStats(mPacketBuf);
			break;
		case EnvMntConstants.READTYPE_LCDDRVBOARD:
			handleLcdDrvBoard(mPacketBuf);
			break;
		case EnvMntConstants.READTYPE_LGTRADSENSOR:
			handleLgtRadSensor(mPacketBuf);
			break;
		case EnvMntConstants.READTYPE_PM25SENSOR:
			handlePm25Sensor(mPacketBuf);
			break;
		case EnvMntConstants.READTYPE_GPSRECVDATA:
			handleGpsRecvData(mPacketBuf);
			break;
		}

		// Reset the packet buffer.
		Arrays.fill(mPacketBuf, (byte) 0);
		mPacketLen = 0;

		readNotify();
	}

	private class ReadThread extends Thread {
		private boolean mIsCanceled = false;

		public void cancel() {
        	mLogger.i("Cancel the read thread.");
        	mIsCanceled = true;
        }

		@Override
		public void run() {
			mLogger.i("A new read thread is started. Thread id is " + getId() + ".");

			while (!mIsCanceled) {
				try {
					if (mInputStream == null) {
						mLogger.i("Input stream is null.");
						return;
					}

					mRecvSize = mInputStream.read(mRecvBuf);
					if ((mRecvSize <= 0) || (mRecvSize > EnvMntConstants.MAX_READ_LEN)) {
						mLogger.i("Received size is invalid, size = " + mRecvSize + ".");
						continue;
					}

					onDataReceived(mRecvBuf, mRecvSize);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			mLogger.i("Read thread is safely terminated, id is: " + currentThread().getId());
		}
	}

	private void sendSerialData(byte[] data) {
		Message msg = mHandler.obtainMessage();
		msg.what = EVENT_SEND_SERIAL_DATA;
		msg.obj = data;
		msg.sendToTarget();
	}

	private void doSendSerialData(byte[] data) {
		if (data == null) {
			mLogger.i("Data is null.");
			return;
		} else if (data.length <= 0) {
			mLogger.i("Data length is invalid, len = " + data.length + ".");
			return;
		}

		if (mOutputStream == null) {
			mLogger.i("Output stream is null.");
			return;
		}

		try {
			mOutputStream.write(data);
		} catch (IOException e) {
			e.printStackTrace();
		}

		StringBuilder sb = new StringBuilder();
		sb.append("Send data: ");
		for (int i = 0; i < data.length; i++) {
			sb.append(data[i] + " ");
		}
		mLogger.i(sb.toString());
	}

	private class MyHandler extends Handler {
		public MyHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
            case EVENT_SEND_SERIAL_DATA:
            	doSendSerialData((byte[])msg.obj);

            	break;
            default:
            	mLogger.i("Unknown message, msg.what = " + msg.what);

                break;
            }
            super.handleMessage(msg);
		}
	}

}
