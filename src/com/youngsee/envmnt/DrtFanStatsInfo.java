package com.youngsee.envmnt;

public class DrtFanStatsInfo {

	int command1;
	int indoor_temp_sensor1_error;
	int indoor_temp_sensor2_error;
	int eeprom_storage_error;
	int high_temp_alarm;
	int low_temp_alarm;
	int draftfan1_error;
	int draftfan2_error;
	int smoke_sensor_alarm;
	int poweronoff_status;
	int eheating_status;
	int draftfan_power_status;
	int lowpressure_alarm;
	int highpressure_alarm;
	int crc1;
	int command2;
	float indoor_temp_sensor1_temp;
	float indoor_temp_sensor2_temp;
	int draftfan1_speed;
	int draftfan2_speed;
	float eheating_start_temp;
	float eheating_stop_temp;
	float draftfan_power_start_temp;
	float draftfan_power_stop_temp;
	float draftfan_lowspeed_temp;
	float draftfan_highspeed_temp;
	int draftfan_low_speed_running_efficiency;
	int draftfan_high_speed_running_efficiency;
	int draftfan1_feedback_number;
	int draftfan2_feedback_number;
	int smoke_sensor_alarm_setting;
	int alarm_lower_limit_voltage;
	int alarm_upper_limit_voltage;
	float alarm_lower_limit_temp;
	float alarm_upper_limit_temp;
	float sensor1_correct_value;
	float sensor2_correct_value;
	int system_onoff;
	int crc2;

	public DrtFanStatsInfo() {
		
	}

	public DrtFanStatsInfo(DrtFanStatsInfo info) {
		command1 = info.command1;
		indoor_temp_sensor1_error = info.indoor_temp_sensor1_error;
		indoor_temp_sensor2_error = info.indoor_temp_sensor2_error;
		eeprom_storage_error = info.eeprom_storage_error;
		high_temp_alarm = info.high_temp_alarm;
		low_temp_alarm = info.low_temp_alarm;
		draftfan1_error = info.draftfan1_error;
		draftfan2_error = info.draftfan2_error;
		smoke_sensor_alarm = info.smoke_sensor_alarm;
		poweronoff_status = info.poweronoff_status;
		eheating_status = info.eheating_status;
		draftfan_power_status = info.draftfan_power_status;
		lowpressure_alarm = info.lowpressure_alarm;
		highpressure_alarm = info.highpressure_alarm;
		crc1 = info.crc1;
		command2 = info.command2;
		indoor_temp_sensor1_temp = info.indoor_temp_sensor1_temp;
		indoor_temp_sensor2_temp = info.indoor_temp_sensor2_temp;
		draftfan1_speed = info.draftfan1_speed;
		draftfan2_speed = info.draftfan2_speed;
		eheating_start_temp = info.eheating_start_temp;
		eheating_stop_temp = info.eheating_stop_temp;
		draftfan_power_start_temp = info.draftfan_power_start_temp;
		draftfan_power_stop_temp = info.draftfan_power_stop_temp;
		draftfan_lowspeed_temp = info.draftfan_lowspeed_temp;
		draftfan_highspeed_temp = info.draftfan_highspeed_temp;
		draftfan_low_speed_running_efficiency = info.draftfan_low_speed_running_efficiency;
		draftfan_high_speed_running_efficiency = info.draftfan_high_speed_running_efficiency;
		draftfan1_feedback_number = info.draftfan1_feedback_number;
		draftfan2_feedback_number = info.draftfan2_feedback_number;
		smoke_sensor_alarm_setting = info.smoke_sensor_alarm_setting;
		alarm_lower_limit_voltage = info.alarm_lower_limit_voltage;
		alarm_upper_limit_voltage = info.alarm_upper_limit_voltage;
		alarm_lower_limit_temp = info.alarm_lower_limit_temp;
		alarm_upper_limit_temp = info.alarm_upper_limit_temp;
		sensor1_correct_value = info.sensor1_correct_value;
		sensor2_correct_value = info.sensor2_correct_value;
		system_onoff = info.system_onoff;
		crc2 = info.crc2;
	}

}
