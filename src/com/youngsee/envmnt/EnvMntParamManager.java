package com.youngsee.envmnt;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.youngsee.common.Logger;

public class EnvMntParamManager {

	private Logger mLogger = new Logger();

	ReadWriteLock mReadWriteLock = new ReentrantReadWriteLock();
	private EnvMntParams mEnvMntParams = new EnvMntParams();

	private EnvMntParamManager() {

	}

	private static class EnvMntParamHolder {
        static final EnvMntParamManager INSTANCE = new EnvMntParamManager();
    }

	public static EnvMntParamManager getInstance() {
		return EnvMntParamHolder.INSTANCE;
	}

	public boolean isSensorSpcValid() {
		boolean isvalid;

		mReadWriteLock.readLock().lock();

		isvalid = mEnvMntParams.issensorspcvalid;

		mReadWriteLock.readLock().unlock();

		return isvalid;
	}

	public boolean isAirConStatsValid() {
		boolean isvalid;

		mReadWriteLock.readLock().lock();

		isvalid = mEnvMntParams.isairconstatsvalid;

		mReadWriteLock.readLock().unlock();

		return isvalid;
	}

	public boolean isDrtFanStatsValid() {
		boolean isvalid;

		mReadWriteLock.readLock().lock();

		isvalid = mEnvMntParams.isdrtfanstatsvalid;

		mReadWriteLock.readLock().unlock();

		return isvalid;
	}

	public boolean isLcdDrvBoardValid() {
		boolean isvalid;

		mReadWriteLock.readLock().lock();

		isvalid = mEnvMntParams.islcddrvboardvalid;

		mReadWriteLock.readLock().unlock();

		return isvalid;
	}

	public boolean isLgtRadSensorValid() {
		boolean isvalid;

		mReadWriteLock.readLock().lock();

		isvalid = mEnvMntParams.islgtradsensorvalid;

		mReadWriteLock.readLock().unlock();

		return isvalid;
	}

	public boolean isPm25SensorValid() {
		boolean isvalid;

		mReadWriteLock.readLock().lock();

		isvalid = mEnvMntParams.ispm25sensorvalid;

		mReadWriteLock.readLock().unlock();

		return isvalid;
	}

	public boolean isGpsRecvDataValid() {
		boolean isvalid;

		mReadWriteLock.readLock().lock();

		isvalid = mEnvMntParams.isgpsrecvdatavalid;

		mReadWriteLock.readLock().unlock();

		return isvalid;
	}

	public SensorSpcInfo getSensorSpcInfo() {
		SensorSpcInfo info;

		mReadWriteLock.readLock().lock();

		info = new SensorSpcInfo(mEnvMntParams.sensorspcinfo);

		mReadWriteLock.readLock().unlock();

		return info;
	}

	public AirConStatsInfo getAirConStatsInfo() {
		AirConStatsInfo info;

		mReadWriteLock.readLock().lock();

		info = new AirConStatsInfo(mEnvMntParams.airconstatsinfo);

		mReadWriteLock.readLock().unlock();

		return info;
	}

	public DrtFanStatsInfo getDrtFanStatsInfo() {
		DrtFanStatsInfo info;

		mReadWriteLock.readLock().lock();

		info = new DrtFanStatsInfo(mEnvMntParams.drtfanstatsinfo);

		mReadWriteLock.readLock().unlock();

		return info;
	}

	public LcdDrvBoardInfo getLcdDrvBoardInfo() {
		LcdDrvBoardInfo info;

		mReadWriteLock.readLock().lock();

		info = new LcdDrvBoardInfo(mEnvMntParams.lcddrvboardinfo);

		mReadWriteLock.readLock().unlock();

		return info;
	}

	public LgtRadSensorInfo getLgtRadSensorInfo() {
		LgtRadSensorInfo info;

		mReadWriteLock.readLock().lock();

		info = new LgtRadSensorInfo(mEnvMntParams.lgtradsensorinfo);

		mReadWriteLock.readLock().unlock();

		return info;
	}

	public Pm25SensorInfo getPm25SensorInfo() {
		Pm25SensorInfo info;

		mReadWriteLock.readLock().lock();

		info = new Pm25SensorInfo(mEnvMntParams.pm25sensorinfo);

		mReadWriteLock.readLock().unlock();

		return info;
	}

	public GpsRecvDataInfo getGpsRecvDataInfo() {
		GpsRecvDataInfo info;

		mReadWriteLock.readLock().lock();

		info = new GpsRecvDataInfo(mEnvMntParams.gpsrecvdatainfo);

		mReadWriteLock.readLock().unlock();

		return info;
	}

	public void setSensorSpcValid(boolean isvalid) {
		mReadWriteLock.writeLock().lock();

		mEnvMntParams.issensorspcvalid = isvalid;

		mReadWriteLock.writeLock().unlock();
	}

	public void setAirConStatsValid(boolean isvalid) {
		mReadWriteLock.writeLock().lock();

		mEnvMntParams.isairconstatsvalid = isvalid;

		mReadWriteLock.writeLock().unlock();
	}

	public void setDrtFanStatsValid(boolean isvalid) {
		mReadWriteLock.writeLock().lock();

		mEnvMntParams.isdrtfanstatsvalid = isvalid;

		mReadWriteLock.writeLock().unlock();
	}

	public void setLcdDrvBoardValid(boolean isvalid) {
		mReadWriteLock.writeLock().lock();

		mEnvMntParams.islcddrvboardvalid = isvalid;

		mReadWriteLock.writeLock().unlock();
	}

	public void setLgtRadSensorValid(boolean isvalid) {
		mReadWriteLock.writeLock().lock();

		mEnvMntParams.islgtradsensorvalid = isvalid;

		mReadWriteLock.writeLock().unlock();
	}

	public void setPm25SensorValid(boolean isvalid) {
		mReadWriteLock.writeLock().lock();

		mEnvMntParams.ispm25sensorvalid = isvalid;

		mReadWriteLock.writeLock().unlock();
	}

	public void setGpsRecvDataValid(boolean isvalid) {
		mReadWriteLock.writeLock().lock();

		mEnvMntParams.isgpsrecvdatavalid = isvalid;

		mReadWriteLock.writeLock().unlock();
	}

	public void resetAllValid() {
		mReadWriteLock.writeLock().lock();

		mEnvMntParams.issensorspcvalid = false;
		mEnvMntParams.isairconstatsvalid = false;
		mEnvMntParams.isdrtfanstatsvalid = false;
		mEnvMntParams.islcddrvboardvalid = false;
		mEnvMntParams.islgtradsensorvalid = false;
		mEnvMntParams.ispm25sensorvalid = false;
		mEnvMntParams.isgpsrecvdatavalid = false;

		mReadWriteLock.writeLock().unlock();
	}

	public void setSensorSpcInfo(SensorSpcInfo info) {
		if (info == null) {
			mLogger.i("Sensor space info is null.");
			return;
		}

		mReadWriteLock.writeLock().lock();

		for (int i = 0; i < EnvMntConstants.CHANNEL_NUM; i++) {
			mEnvMntParams.sensorspcinfo.sw_enable[i] = info.sw_enable[i];
			mEnvMntParams.sensorspcinfo.sw_state[i] = info.sw_state[i];
			mEnvMntParams.sensorspcinfo.lv_enable[i] = info.lv_enable[i];
			mEnvMntParams.sensorspcinfo.lv_state[i] = info.lv_state[i];
		}
		mEnvMntParams.sensorspcinfo.humidity0 = info.humidity0;
		mEnvMntParams.sensorspcinfo.temp0 = info.temp0;
		mEnvMntParams.sensorspcinfo.humidity1 = info.humidity1;
		mEnvMntParams.sensorspcinfo.temp1 = info.temp1;
		mEnvMntParams.sensorspcinfo.gps_state = info.gps_state;
		for (int i = 0; i < EnvMntConstants.UART_NUM; i++) {
			mEnvMntParams.sensorspcinfo.uart_state[i] = info.uart_state[i];
		}
		for (int i = 0; i < EnvMntConstants.SENSOR_NUM; i++) {
			mEnvMntParams.sensorspcinfo.sensor_state[i] = info.sensor_state[i];
		}

		mReadWriteLock.writeLock().unlock();
	}

	public void setAirConStatsInfo(AirConStatsInfo info) {
		if (info == null) {
			mLogger.i("Air conditioner status info is null.");
			return;
		}

		mReadWriteLock.writeLock().lock();

		mEnvMntParams.airconstatsinfo.command1 = info.command1;
		mEnvMntParams.airconstatsinfo.rt1_sensor_error = info.rt1_sensor_error;
		mEnvMntParams.airconstatsinfo.rt2_sensor_error = info.rt2_sensor_error;
		mEnvMntParams.airconstatsinfo.humidity_sensor_error = info.humidity_sensor_error;
		mEnvMntParams.airconstatsinfo.eeprom_setting_error = info.eeprom_setting_error;
		mEnvMntParams.airconstatsinfo.slave_onoff = info.slave_onoff;
		mEnvMntParams.airconstatsinfo.high_temp_alarm = info.high_temp_alarm;
		mEnvMntParams.airconstatsinfo.low_temp_alarm = info.low_temp_alarm;
		mEnvMntParams.airconstatsinfo.smoke_alarm = info.smoke_alarm;
		mEnvMntParams.airconstatsinfo.compressor_high_pressure_alarm = info.compressor_high_pressure_alarm;
		mEnvMntParams.airconstatsinfo.compressor_low_pressure_alarm = info.compressor_low_pressure_alarm;
		mEnvMntParams.airconstatsinfo.dooropen_alarm = info.dooropen_alarm;
		mEnvMntParams.airconstatsinfo.vibration_alarm = info.vibration_alarm;
		mEnvMntParams.airconstatsinfo.compressor_status = info.compressor_status;
		mEnvMntParams.airconstatsinfo.eheating_status = info.eheating_status;
		mEnvMntParams.airconstatsinfo.inside_draftfan_status = info.inside_draftfan_status;
		mEnvMntParams.airconstatsinfo.outside_draftfan_status = info.outside_draftfan_status;
		mEnvMntParams.airconstatsinfo.emergency_draftfan_status = info.emergency_draftfan_status;
		mEnvMntParams.airconstatsinfo.inside_draftfan2_status = info.inside_draftfan2_status;
		mEnvMntParams.airconstatsinfo.outside_draftfan2_status = info.outside_draftfan2_status;
		mEnvMntParams.airconstatsinfo.power_input_undervoltage_protect = info.power_input_undervoltage_protect;
		mEnvMntParams.airconstatsinfo.power_input_overvoltage_protect = info.power_input_overvoltage_protect;
		mEnvMntParams.airconstatsinfo.ultra_temp_alarm = info.ultra_temp_alarm;
		mEnvMntParams.airconstatsinfo.crc1 = info.crc1;
		mEnvMntParams.airconstatsinfo.command2 = info.command2;
		mEnvMntParams.airconstatsinfo.rt1_temp = info.rt1_temp;
		mEnvMntParams.airconstatsinfo.rt2_temp = info.rt2_temp;
		mEnvMntParams.airconstatsinfo.humidity = info.humidity;
		mEnvMntParams.airconstatsinfo.inner_draftfan1_speed = info.inner_draftfan1_speed;
		mEnvMntParams.airconstatsinfo.inner_draftfan2_speed = info.inner_draftfan2_speed;
		mEnvMntParams.airconstatsinfo.outer_draftfan1_speed = info.outer_draftfan1_speed;
		mEnvMntParams.airconstatsinfo.outer_draftfan2_speed = info.outer_draftfan2_speed;
		mEnvMntParams.airconstatsinfo.refrigeration_start_temp = info.refrigeration_start_temp;
		mEnvMntParams.airconstatsinfo.refrigeration_stop_temp = info.refrigeration_stop_temp;
		mEnvMntParams.airconstatsinfo.heating_start_temp = info.heating_start_temp;
		mEnvMntParams.airconstatsinfo.heating_stop_temp = info.heating_stop_temp;
		mEnvMntParams.airconstatsinfo.high_temp_alarm_setting = info.high_temp_alarm_setting;
		mEnvMntParams.airconstatsinfo.low_temp_alarm_setting = info.low_temp_alarm_setting;
		mEnvMntParams.airconstatsinfo.ultra_temp_alarm_setting = info.ultra_temp_alarm_setting;
		mEnvMntParams.airconstatsinfo.airconditioner_return_diff = info.airconditioner_return_diff;
		mEnvMntParams.airconstatsinfo.inner_recycle_stop_temp = info.inner_recycle_stop_temp;
		mEnvMntParams.airconstatsinfo.inner_recycle_running_temp = info.inner_recycle_running_temp;
		mEnvMntParams.airconstatsinfo.inner_recycle_acceleration_temp = info.inner_recycle_acceleration_temp;
		mEnvMntParams.airconstatsinfo.inner_recycle_highspeed_temp = info.inner_recycle_highspeed_temp;
		mEnvMntParams.airconstatsinfo.inner_recycle_fullspeed_temp = info.inner_recycle_fullspeed_temp;
		mEnvMntParams.airconstatsinfo.outer_recycle_stop_temp = info.outer_recycle_stop_temp;
		mEnvMntParams.airconstatsinfo.outer_recycle_running_temp = info.outer_recycle_running_temp;
		mEnvMntParams.airconstatsinfo.outer_recycle_acceleration_temp = info.outer_recycle_acceleration_temp;
		mEnvMntParams.airconstatsinfo.outer_recycle_highspeed_temp = info.outer_recycle_highspeed_temp;
		mEnvMntParams.airconstatsinfo.outer_recycle_fullspeed_temp = info.outer_recycle_fullspeed_temp;
		mEnvMntParams.airconstatsinfo.rt1_temp_sensor_correct_value = info.rt1_temp_sensor_correct_value;
		mEnvMntParams.airconstatsinfo.rt2_temp_sensor_correct_value = info.rt2_temp_sensor_correct_value;
		mEnvMntParams.airconstatsinfo.inner_recycle_draftfan1_setting = info.inner_recycle_draftfan1_setting;
		mEnvMntParams.airconstatsinfo.inner_recycle_draftfan2_setting = info.inner_recycle_draftfan2_setting;
		mEnvMntParams.airconstatsinfo.outer_recycle_draftfan1_setting = info.outer_recycle_draftfan1_setting;
		mEnvMntParams.airconstatsinfo.outer_recycle_draftfan2_setting = info.outer_recycle_draftfan2_setting;
		mEnvMntParams.airconstatsinfo.compressor_setting = info.compressor_setting;
		mEnvMntParams.airconstatsinfo.eheating_setting = info.eheating_setting;
		mEnvMntParams.airconstatsinfo.relay_a_setting = info.relay_a_setting;
		mEnvMntParams.airconstatsinfo.relay_b_setting = info.relay_b_setting;
		mEnvMntParams.airconstatsinfo.relay_c_setting = info.relay_c_setting;
		mEnvMntParams.airconstatsinfo.p1_protect_input_setting = info.p1_protect_input_setting;
		mEnvMntParams.airconstatsinfo.p2_protect_input_setting = info.p2_protect_input_setting;
		mEnvMntParams.airconstatsinfo.rt1_sensor_enable_setting = info.rt1_sensor_enable_setting;
		mEnvMntParams.airconstatsinfo.rt2_sensor_enable_setting = info.rt2_sensor_enable_setting;
		mEnvMntParams.airconstatsinfo.humidity_sensor_enable_setting = info.humidity_sensor_enable_setting;
		mEnvMntParams.airconstatsinfo.underpressure_alarm_setting = info.underpressure_alarm_setting;
		mEnvMntParams.airconstatsinfo.overpressure_alarm_setting = info.overpressure_alarm_setting;
		mEnvMntParams.airconstatsinfo.inside_draftfan_feedback_pulse = info.inside_draftfan_feedback_pulse;
		mEnvMntParams.airconstatsinfo.outside_draftfan_feedback_pulse = info.outside_draftfan_feedback_pulse;
		mEnvMntParams.airconstatsinfo.mute_mode_setting = info.mute_mode_setting;
		mEnvMntParams.airconstatsinfo.system_onoff = info.system_onoff;
		mEnvMntParams.airconstatsinfo.buzzer_mode = info.buzzer_mode;
		mEnvMntParams.airconstatsinfo.lcd_contrast_setting = info.lcd_contrast_setting;
		mEnvMntParams.airconstatsinfo.crc2 = info.crc2;

		mReadWriteLock.writeLock().unlock();
	}

	public void setDrtFanStatsInfo(DrtFanStatsInfo info) {
		if (info == null) {
			mLogger.i("Draft fan status info is null.");
			return;
		}

		mReadWriteLock.writeLock().lock();

		mEnvMntParams.drtfanstatsinfo.command1 = info.command1;
		mEnvMntParams.drtfanstatsinfo.indoor_temp_sensor1_error = info.indoor_temp_sensor1_error;
		mEnvMntParams.drtfanstatsinfo.indoor_temp_sensor2_error = info.indoor_temp_sensor2_error;
		mEnvMntParams.drtfanstatsinfo.eeprom_storage_error = info.eeprom_storage_error;
		mEnvMntParams.drtfanstatsinfo.high_temp_alarm = info.high_temp_alarm;
		mEnvMntParams.drtfanstatsinfo.low_temp_alarm = info.low_temp_alarm;
		mEnvMntParams.drtfanstatsinfo.draftfan1_error = info.draftfan1_error;
		mEnvMntParams.drtfanstatsinfo.draftfan2_error = info.draftfan2_error;
		mEnvMntParams.drtfanstatsinfo.smoke_sensor_alarm = info.smoke_sensor_alarm;
		mEnvMntParams.drtfanstatsinfo.poweronoff_status = info.poweronoff_status;
		mEnvMntParams.drtfanstatsinfo.eheating_status = info.eheating_status;
		mEnvMntParams.drtfanstatsinfo.draftfan_power_status = info.draftfan_power_status;
		mEnvMntParams.drtfanstatsinfo.lowpressure_alarm = info.lowpressure_alarm;
		mEnvMntParams.drtfanstatsinfo.highpressure_alarm = info.highpressure_alarm;
		mEnvMntParams.drtfanstatsinfo.crc1 = info.crc1;
		mEnvMntParams.drtfanstatsinfo.command2 = info.command2;
		mEnvMntParams.drtfanstatsinfo.indoor_temp_sensor1_temp = info.indoor_temp_sensor1_temp;
		mEnvMntParams.drtfanstatsinfo.indoor_temp_sensor2_temp = info.indoor_temp_sensor2_temp;
		mEnvMntParams.drtfanstatsinfo.draftfan1_speed = info.draftfan1_speed;
		mEnvMntParams.drtfanstatsinfo.draftfan2_speed = info.draftfan2_speed;
		mEnvMntParams.drtfanstatsinfo.eheating_start_temp = info.eheating_start_temp;
		mEnvMntParams.drtfanstatsinfo.eheating_stop_temp = info.eheating_stop_temp;
		mEnvMntParams.drtfanstatsinfo.draftfan_power_start_temp = info.draftfan_power_start_temp;
		mEnvMntParams.drtfanstatsinfo.draftfan_power_stop_temp = info.draftfan_power_stop_temp;
		mEnvMntParams.drtfanstatsinfo.draftfan_lowspeed_temp = info.draftfan_lowspeed_temp;
		mEnvMntParams.drtfanstatsinfo.draftfan_highspeed_temp = info.draftfan_highspeed_temp;
		mEnvMntParams.drtfanstatsinfo.draftfan_low_speed_running_efficiency = info.draftfan_low_speed_running_efficiency;
		mEnvMntParams.drtfanstatsinfo.draftfan_high_speed_running_efficiency = info.draftfan_high_speed_running_efficiency;
		mEnvMntParams.drtfanstatsinfo.draftfan1_feedback_number = info.draftfan1_feedback_number;
		mEnvMntParams.drtfanstatsinfo.draftfan2_feedback_number = info.draftfan2_feedback_number;
		mEnvMntParams.drtfanstatsinfo.smoke_sensor_alarm_setting = info.smoke_sensor_alarm_setting;
		mEnvMntParams.drtfanstatsinfo.alarm_lower_limit_voltage = info.alarm_lower_limit_voltage;
		mEnvMntParams.drtfanstatsinfo.alarm_upper_limit_voltage = info.alarm_upper_limit_voltage;
		mEnvMntParams.drtfanstatsinfo.alarm_lower_limit_temp = info.alarm_lower_limit_temp;
		mEnvMntParams.drtfanstatsinfo.alarm_upper_limit_temp = info.alarm_upper_limit_temp;
		mEnvMntParams.drtfanstatsinfo.sensor1_correct_value = info.sensor1_correct_value;
		mEnvMntParams.drtfanstatsinfo.sensor2_correct_value = info.sensor2_correct_value;
		mEnvMntParams.drtfanstatsinfo.system_onoff = info.system_onoff;
		mEnvMntParams.drtfanstatsinfo.crc2 = info.crc2;

		mReadWriteLock.writeLock().unlock();
	}

	public void setLcdDrvBoardInfo(LcdDrvBoardInfo info) {
		if (info == null) {
			mLogger.i("Lcd driver board info is null.");
			return;
		}

		mReadWriteLock.writeLock().lock();

		mEnvMntParams.lcddrvboardinfo.start_flag = info.start_flag;
		mEnvMntParams.lcddrvboardinfo.standby_status = info.standby_status;
		mEnvMntParams.lcddrvboardinfo.current_signal_source = info.current_signal_source;
		mEnvMntParams.lcddrvboardinfo.sound_volume = info.sound_volume;
		mEnvMntParams.lcddrvboardinfo.image_brightness = info.image_brightness;
		mEnvMntParams.lcddrvboardinfo.fan_switch_status = info.fan_switch_status;
		mEnvMntParams.lcddrvboardinfo.current_temp = info.current_temp;
		mEnvMntParams.lcddrvboardinfo.image_contrast = info.image_contrast;
		mEnvMntParams.lcddrvboardinfo.mute_status = info.mute_status;
		mEnvMntParams.lcddrvboardinfo.image_resolution = info.image_resolution;
		mEnvMntParams.lcddrvboardinfo.end_flag1 = info.end_flag1;
		mEnvMntParams.lcddrvboardinfo.end_flag2 = info.end_flag2;

		mReadWriteLock.writeLock().unlock();
	}

	public void setLgtRadSensorInfo(LgtRadSensorInfo info) {
		if (info == null) {
			mLogger.i("Light radiation sensor info is null.");
			return;
		}

		mReadWriteLock.writeLock().lock();

		mEnvMntParams.lgtradsensorinfo.address = info.address;
		mEnvMntParams.lgtradsensorinfo.function = info.function;
		mEnvMntParams.lgtradsensorinfo.datalength = info.datalength;
		mEnvMntParams.lgtradsensorinfo.radiation = info.radiation;
		mEnvMntParams.lgtradsensorinfo.checksum = info.checksum;

		mReadWriteLock.writeLock().unlock();
	}

	public void setPm25SensorInfo(Pm25SensorInfo info) {
		if (info == null) {
			mLogger.i("PM2.5 sensor info is null.");
			return;
		}

		mReadWriteLock.writeLock().lock();

		mEnvMntParams.pm25sensorinfo.start_flag = info.start_flag;
		mEnvMntParams.pm25sensorinfo.pm25 = info.pm25;
		mEnvMntParams.pm25sensorinfo.checksum = info.checksum;
		mEnvMntParams.pm25sensorinfo.end_flag = info.end_flag;

		mReadWriteLock.writeLock().unlock();
	}

	public void setGpsRecvDataInfo(GpsRecvDataInfo info) {
		if (info == null) {
			mLogger.i("Gps received data info is null.");
			return;
		}

		mReadWriteLock.writeLock().lock();

		mEnvMntParams.gpsrecvdatainfo.latitude = info.latitude;
		mEnvMntParams.gpsrecvdatainfo.latitude_indication = info.latitude_indication;
		mEnvMntParams.gpsrecvdatainfo.longitude = info.longitude;
		mEnvMntParams.gpsrecvdatainfo.longitude_indication = info.longitude_indication;
		mEnvMntParams.gpsrecvdatainfo.location_indication = info.location_indication;
		mEnvMntParams.gpsrecvdatainfo.satellite_number = info.satellite_number;
		mEnvMntParams.gpsrecvdatainfo.altitude = info.altitude;

		mReadWriteLock.writeLock().unlock();
	}

}
