package com.youngsee.envmnt;

public class LcdDrvBoardInfo {

	int start_flag;
	int standby_status;
	int current_signal_source;
	int sound_volume;
	int image_brightness;
	int fan_switch_status;
	int current_temp;
	int image_contrast;
	int mute_status;
	int image_resolution;
	int end_flag1;
	int end_flag2;

	public LcdDrvBoardInfo() {
		
	}

	public LcdDrvBoardInfo(LcdDrvBoardInfo info) {
		start_flag = info.start_flag;
		standby_status = info.standby_status;
		current_signal_source = info.current_signal_source;
		sound_volume = info.sound_volume;
		image_brightness = info.image_brightness;
		fan_switch_status = info.fan_switch_status;
		current_temp = info.current_temp;
		image_contrast = info.image_contrast;
		mute_status = info.mute_status;
		image_resolution = info.image_resolution;
		end_flag1 = info.end_flag1;
		end_flag2 = info.end_flag2;
	}

}
