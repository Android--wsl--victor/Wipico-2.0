package com.chinasvc.wipico.sync;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import com.chinasvc.wipico.player.PlayerConstant;
import com.chinasvc.wipico.util.WipicoConstant;

import android.util.Log;

/**
 * 客户端多媒体同步类
 * 
 * @since 1.0.0
 * */
public class SyncPlayClient {

	private String TAG = "SyncPlayClient";
	private boolean isDebug = false;

	private int currentMedia = 0;

	private ImageSyncListener mImageSyncListener;
	private AudioSyncListener mAudioSyncListener;
	private VideoSyncListener mVideoSyncListener;
	private DeviceSyncListener mDeviceSyncListener;

	private MediaSyncThread mediaSyncThread;

	/**
	 * 获取当前播放类型，0为没有播放，1为图片，2为音乐，3为视频
	 * */
	public int getMediaType() {
		return currentMedia;
	}

	/**
	 * 开启同步
	 * */
	public void startSync() {
		if (mediaSyncThread != null) {
			mediaSyncThread.stopSync();
			mediaSyncThread = null;
		}
		mediaSyncThread = new MediaSyncThread();
		mediaSyncThread.startSync();
	}

	/**
	 * 关闭同步
	 * */
	public void stopSync() {
		if (mediaSyncThread != null) {
			mediaSyncThread.stopSync();
			mediaSyncThread = null;
		}
	}

	/**
	 * 多媒体同步线程
	 * */
	private class MediaSyncThread extends Thread {

		private boolean isRuning = true;
		private DatagramSocket datagramSocket;

		@Override
		public void run() {
			// TODO Auto-generated method stub
			byte[] message = new byte[1024];
			try {
				if (datagramSocket == null) {
					datagramSocket = new DatagramSocket(null);
					datagramSocket.setReuseAddress(true);
					datagramSocket.bind(new InetSocketAddress(WipicoConstant.SYNC_UDP_PORT));
					datagramSocket.setSoTimeout(1000);
				}

				DatagramPacket datagramPacket = new DatagramPacket(message, message.length);
				while (isRuning) {
					try {
						if (!datagramSocket.isClosed()) {
							datagramSocket.receive(datagramPacket);
							update(datagramPacket.getData());
						}
					} catch (SocketTimeoutException e) {
						if (isDebug)
							Log.i(TAG, "同步读取超时");
						currentMedia = 0;
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if (!datagramSocket.isClosed()) {
					datagramSocket.close();
				}
			} catch (SocketException e) {
				e.printStackTrace();
			}
		}

		private void update(byte[] data) {
			try {
				DataInputStream is = null;
				is = new DataInputStream(new ByteArrayInputStream(data));
				int flag;
				flag = is.readInt();// 1为图片，2为音乐，3为视频, 100为设备同步
				if (flag == PlayerConstant.PLAY_IMAGE || flag == PlayerConstant.PLAY_MUSIC || flag == PlayerConstant.PLAY_VIDEO) {
					int progress, duration, status, isSilent, volume, volumeMax;
					progress = is.readInt();// 当前进度
					duration = is.readInt();// 总时长
					status = is.readInt();// 播放状态，1为正在播放，-1为播放结束
					isSilent = is.readInt();// 是否静音
					volume = is.readInt();// 当前音量
					volumeMax = is.readInt();// 音量总值
					currentMedia = flag;
					switch (currentMedia) {
					case PlayerConstant.PLAY_IMAGE:
						if (mImageSyncListener != null) {
							mImageSyncListener.syncPlay();
						}
						break;
					case PlayerConstant.PLAY_MUSIC:
						if (mAudioSyncListener != null) {
							mAudioSyncListener.syncPlay(progress, duration, status, int2boolean(isSilent), volume, volumeMax);
						}
						break;
					case PlayerConstant.PLAY_VIDEO:
						if (mVideoSyncListener != null) {
							mVideoSyncListener.syncPlay(progress, duration, status, int2boolean(isSilent), volume, volumeMax);
						}
						break;
					}
				} else if (flag == 100) {
					int appType = is.readInt();
					String wifi_ssid = is.readUTF();
					String version = is.readUTF();
					if (mDeviceSyncListener != null) {
						mDeviceSyncListener.deviceSync(appType, wifi_ssid, version);
					}
				} else if (flag == 200) {
					if (mDeviceSyncListener != null) {
						mDeviceSyncListener.gameSync();
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		private boolean int2boolean(int value) {
			if (value == 1) {
				return true;
			} else {
				return false;
			}
		}

		public void stopSync() {
			isRuning = false;
			try {
				join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (datagramSocket != null && !datagramSocket.isClosed()) {
				datagramSocket.close();
			}
		}

		public void startSync() {
			isRuning = true;
			start();
		}
	}

	/**
	 * 图片同步监听器
	 * */
	public interface ImageSyncListener {
		/**
		 * 图片同步播放
		 * */
		public void syncPlay();
	}

	/**
	 * 设备同步接口
	 * */
	public interface DeviceSyncListener {

		/**
		 * 设备同步
		 * 
		 * @param type
		 *                同步类型 <
		 *                {@link SyncConstants.START_APPLICATION_DEFAULT}
		 *                > <
		 *                {@link SyncConstants.START_APPLICATION_IMAGE}>
		 *                <
		 *                {@link SyncConstants.START_APPLICATION_AUDIO}>
		 *                <
		 *                {@link SyncConstants.START_APPLICATION_VIDEO}>
		 *                <
		 *                {@link SyncConstants.START_APPLICATION_OFFICE}
		 *                > <
		 *                {@link SyncConstants.START_APPLICATION_GAME}>
		 *                <
		 *                {@link SyncConstants.START_APPLICATION_OTHER}>
		 * @param ssid
		 *                SSID
		 * @param version
		 *                设备版本号
		 * */
		public void deviceSync(int type, String ssid, String version);

		/**
		 * 游戏列表同步
		 * */
		public void gameSync();
	}

	/**
	 * 音频同步监听器
	 * */
	public interface AudioSyncListener {
		/**
		 * 同步播放
		 * 
		 * @param progress
		 *                当前进度条
		 * @param duration
		 *                当前播放的总的长度
		 * @param status
		 *                播放状态
		 * @param status
		 *                是否静音
		 * @param volume
		 *                当前音量
		 * @param volumeMax
		 *                最大音量
		 * */
		public void syncPlay(int progress, int duration, int status, boolean isSilent, int volume, int volumeMax);
	}

	/**
	 * 视频同步监听器
	 * */
	public interface VideoSyncListener {
		/**
		 * 同步播放
		 * 
		 * @param progress
		 *                当前进度条
		 * @param duration
		 *                当前播放的总的长度
		 * @param status
		 *                播放状态
		 * @param status
		 *                是否静音
		 * @param volume
		 *                当前音量
		 * @param volumeMax
		 *                最大音量
		 * */
		public void syncPlay(int progress, int duration, int status, boolean isSilent, int volume, int volumeMax);
	}

	/**
	 * 设置图片同步监听器
	 * 
	 * @param imageAsyncListener
	 *                图片同步监听器
	 * */
	public void setOnImageAsyncListener(ImageSyncListener imageSyncListener) {
		this.mImageSyncListener = imageSyncListener;
	}

	public void setDeviceAsyncListener(DeviceSyncListener deviceSyncListener) {
		this.mDeviceSyncListener = deviceSyncListener;
	}

	/**
	 * 设置视频同步监听器
	 * 
	 * @param imageAsyncListener
	 *                视频同步监听器
	 * */
	public void setOnVideoAsyncListener(VideoSyncListener videoSyncListener) {
		this.mVideoSyncListener = videoSyncListener;
	}

	/**
	 * 设置音频同步监听器
	 * 
	 * @param imageAsyncListener
	 *                音频同步监听器
	 * */
	public void setOnAudioSyncListener(AudioSyncListener audioSyncListener) {
		this.mAudioSyncListener = audioSyncListener;
	}

}
