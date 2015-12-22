package com.youngsee.envmnt;

public class SensorSpcInfo {

	int[] sw_enable = new int[EnvMntConstants.CHANNEL_NUM];
	int[] sw_state = new int[EnvMntConstants.CHANNEL_NUM];
	int[] lv_enable = new int[EnvMntConstants.CHANNEL_NUM];
	int[] lv_state = new int[EnvMntConstants.CHANNEL_NUM];
	float humidity0;
	float temp0;
	int dht0_sum;
	float humidity1;
	float temp1;
	int dht1_sum;
	int gps_state;
	int[] uart_state = new int[EnvMntConstants.UART_NUM];
	int[] sensor_state = new int[EnvMntConstants.SENSOR_NUM];
	int version;

	public SensorSpcInfo() {
		
	}

	public SensorSpcInfo(SensorSpcInfo info) {
		for (int i = 0; i < EnvMntConstants.CHANNEL_NUM; i++) {
			sw_enable[i] = info.sw_enable[i];
			sw_state[i] = info.sw_state[i];
			lv_enable[i] = info.lv_enable[i];
			lv_state[i] = info.lv_state[i];
		}
		humidity0 = info.humidity0;
		temp0 = info.temp0;
		dht0_sum = info.dht0_sum;
		humidity1 = info.humidity1;
		temp1 = info.temp1;
		dht1_sum = info.dht1_sum;
		gps_state = info.gps_state;
		for (int i = 0; i < EnvMntConstants.UART_NUM; i++) {
			uart_state[i] = info.uart_state[i];
		}
		for (int i = 0; i < EnvMntConstants.SENSOR_NUM; i++) {
			sensor_state[i] = info.sensor_state[i];
		}
		version = info.version;
	}

}
