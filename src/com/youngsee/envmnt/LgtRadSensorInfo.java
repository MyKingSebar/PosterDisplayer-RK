package com.youngsee.envmnt;

public class LgtRadSensorInfo {

	int address;
	int function;
	int datalength;
	int radiation;
	int checksum;

	public LgtRadSensorInfo() {
		
	}

	public LgtRadSensorInfo(LgtRadSensorInfo info) {
		address = info.address;
		function = info.function;
		datalength = info.datalength;
		radiation = info.radiation;
		checksum = info.checksum;
	}

}
