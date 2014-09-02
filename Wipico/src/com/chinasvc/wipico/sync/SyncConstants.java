package com.chinasvc.wipico.sync;

/**
 * 同步配置类
 * */
public class SyncConstants {

	public static final String BROADCAST_WIPICO_START_APPLICATION = "com.chinasvc.wipico.BROADCAST_WIPICO_START_APPLICATION";

	/**
	 * 设备状态同步
	 * */
	public static final String BROADCAST_WIPICO_SYNC_DEVICE = "com.chinasvc.wipico.BROADCAST_WIPICO_SYNC_DEVICE";

	/**
	 * 游戏同步
	 * */
	public static final String BROADCAST_WIPICO_SYNC_GAME = "com.chinasvc.wipico.BROADCAST_WIPICO_SYNC_GAME";

	public static final String START_APPLICATION_FLAG = "start_application_flag";
	public static final String WIFI_SSID = "wifi_ssid";

	/** 默认 */
	public static final int START_APPLICATION_DEFAULT = 0;

	/** 图片 */
	public static final int START_APPLICATION_IMAGE = 1;

	/** 音乐 */
	public static final int START_APPLICATION_AUDIO = 2;

	/** 视频 */
	public static final int START_APPLICATION_VIDEO = 3;

	/** Office */
	public static final int START_APPLICATION_OFFICE = 4;

	/** Game */
	public static final int START_APPLICATION_GAME = 5;

	/** Other */
	public static final int START_APPLICATION_OTHER = 6;

}
