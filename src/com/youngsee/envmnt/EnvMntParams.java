package com.youngsee.envmnt;

public class EnvMntParams {

	public boolean issensorspcvalid;
	public boolean isairconstatsvalid;
	public boolean isdrtfanstatsvalid;
	public boolean islcddrvboardvalid;
	public boolean islgtradsensorvalid;
	public boolean ispm25sensorvalid;
	public boolean isgpsrecvdatavalid;

	public SensorSpcInfo sensorspcinfo = new SensorSpcInfo();
	public AirConStatsInfo airconstatsinfo = new AirConStatsInfo();
	public DrtFanStatsInfo drtfanstatsinfo = new DrtFanStatsInfo();
	public LcdDrvBoardInfo lcddrvboardinfo = new LcdDrvBoardInfo();
	public LgtRadSensorInfo lgtradsensorinfo = new LgtRadSensorInfo();
	public Pm25SensorInfo pm25sensorinfo = new Pm25SensorInfo();
	public GpsRecvDataInfo gpsrecvdatainfo = new GpsRecvDataInfo();

}
