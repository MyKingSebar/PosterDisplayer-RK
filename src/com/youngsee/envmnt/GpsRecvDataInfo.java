package com.youngsee.envmnt;

public class GpsRecvDataInfo {

	String latitude;
	String latitude_indication;
	String longitude;
	String longitude_indication;
	int location_indication;
	int satellite_number;
	float altitude;

	public GpsRecvDataInfo() {
		
	}

	public GpsRecvDataInfo(GpsRecvDataInfo info) {
		latitude = info.latitude;
		latitude_indication = info.latitude_indication;
		longitude = info.longitude;
		longitude_indication = info.longitude_indication;
		location_indication = info.location_indication;
		satellite_number = info.satellite_number;
		altitude = info.altitude;
	}

}
