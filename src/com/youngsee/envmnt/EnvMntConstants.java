package com.youngsee.envmnt;

public class EnvMntConstants {

	public final static int CFGSPC_ADDR_0 = 0x0000;
	public final static int CFGSPC_ADDR_1 = 0x0001;
	public final static int CFGSPC_ADDR_2 = 0x0002;
	public final static int CFGSPC_ADDR_3 = 0x0003;
	public final static int CFGSPC_ADDR_4 = 0x0004;
	public final static int CFGSPC_ADDR_5 = 0x0005;
	public final static int CFGSPC_ADDR_6 = 0x0006;
	public final static int CFGSPC_ADDR_7 = 0x0007;
	public final static int CFGSPC_ADDR_8 = 0x0008;
	public final static int CFGSPC_ADDR_9 = 0x0009;
	public final static int CFGSPC_ADDR_A = 0x000A;
	public final static int CFGSPC_ADDR_B = 0x000B;
	public final static int CFGSPC_ADDR_C = 0x000C;
	public final static int CFGSPC_ADDR_D = 0x000D;

	public final static int CMD_CFGSPC_WRITE = 0x57;
	public final static int CMD_CFGSPC_READ = 0x52;

	public final static int READADDR_SENSORSPC = 0x9000;
	public final static int READADDR_AIRCONSTATS = 0x8000;
	public final static int READADDR_DRTFANSTATS = 0x8100;
	public final static int READADDR_LCDDRVBOARD = 0x8200;
	public final static int READADDR_LGTRADSENSOR = 0x8300;
	public final static int READADDR_PM25SENSOR = 0x8400;
	public final static int READADDR_GPSRECVDATA = 0x8800;

	public final static int READTYPE_SENSORSPC = 0x07;
	public final static int READTYPE_AIRCONSTATS = 0x00;
	public final static int READTYPE_DRTFANSTATS = 0x01;
	public final static int READTYPE_LCDDRVBOARD = 0x02;
	public final static int READTYPE_LGTRADSENSOR = 0x03;
	public final static int READTYPE_PM25SENSOR = 0x04;
	public final static int READTYPE_RESERVED = 0x05;
	public final static int READTYPE_GPSRECVDATA = 0x06;
	public final static int READTYPE_NONE = 0xFF;

	public final static int CMDRESP_CFGSPC_WRITE = 0x59;

	public final static int READ_HEAD_LEN = 5;

	public final static int READLEN_SENSORSPC = 22;
	public final static int READLEN_AIRCONSTATS = 135;
	public final static int READLEN_DRTFANSTATS = 75;
	public final static int READLEN_LCDDRVBOARD = 17;
	public final static int READLEN_LGTRADSENSOR = 15;
	public final static int READLEN_PM25SENSOR = 12;
	public final static int READLEN_GPSRECVDATA = 1040;

	public final static int MAX_READ_LEN = READLEN_GPSRECVDATA;

	public final static int CHANNEL_NUM = 8;
	public final static int UART_NUM = 7;
	public final static int SENSOR_NUM = 2;

	public final static int UARTNUM_AIRCONSTATS = 0;
	public final static int UARTNUM_DRTFANSTATS = 1;
	public final static int UARTNUM_LCDDRVBOARD = 2;
	public final static int UARTNUM_LGTRADSENSOR = 3;
	public final static int UARTNUM_PM25SENSOR = 4;
	public final static int UARTNUM_RESERVED = 5;
	public final static int UARTNUM_GPSRECVDATA = 6;

	public final static int REQUESTCODE_MONITORDEVICE = 0;
	public final static int REQUESTCODE_AIRCONALARM = 1;
	public final static int REQUESTCODE_AIRCONINFO = 2;
	public final static int REQUESTCODE_DRTFANALARM = 3;
	public final static int REQUESTCODE_DRTFANINFO = 4;
	public final static int REQUESTCODE_DISPLAYALARM = 5;
	public final static int REQUESTCODE_SWITCHINFO = 6;
	public final static int REQUESTCODE_GPSINFO = 7;
	public final static int REQUESTCODE_OTHERINFO = 8;

}
