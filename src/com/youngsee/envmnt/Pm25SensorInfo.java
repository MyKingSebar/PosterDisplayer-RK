package com.youngsee.envmnt;

public class Pm25SensorInfo {

	int start_flag;
	float pm25;
	int checksum;
	int end_flag;

	public Pm25SensorInfo() {
		
	}

	public Pm25SensorInfo(Pm25SensorInfo info) {
		start_flag = info.start_flag;
		pm25 = info.pm25;
		checksum = info.checksum;
		end_flag = info.end_flag;
	}

}
