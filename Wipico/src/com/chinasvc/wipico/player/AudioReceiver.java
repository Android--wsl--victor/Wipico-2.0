package com.chinasvc.wipico.player;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

import com.chinasvc.wipico.util.WipicoConstant;

/**
 * 音频接收处理类
 * 
 * @since 1.0.0
 * */
public class AudioReceiver {

	private Context mContext;

	private SyncThread syncThread;

	private MediaState mMediaState;

	private AudioReceiveListener mAudioReceiveListener;

	/**
	 * 构造方法
	 * 
	 * @param context
	 * */
	public AudioReceiver(Context context) {
		mContext = context;

		// 开启广播接收
		IntentFilter filter = new IntentFilter();
		filter.addAction(com.chinasvc.wipico.type.Audio.BROADCAST_ACTION_AUDIO);
		mContext.registerReceiver(mReceiver, filter);
	}

	/**
	 * 开启音频接收控制
	 * */
	public void startReceive() {
		startSync();// 启动同步
		onNewIntent(((Activity) mContext).getIntent());
	}

	private void onNewIntent(Intent intent) {
		Bundle bundle = intent.getExtras();
		if (bundle != null) {
			try {
				String path = bundle.getString(WipicoConstant.BUNDLE_MEDIA_URL_KEY);
				String newPath = "";
				newPath = URLDecoder.decode(path, "utf-8");
				if (mAudioReceiveListener != null) {
					mAudioReceiveListener.openAudio(newPath);
				}
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 设置音频状态信息
	 * 
	 * @param mediaState
	 *                音频当前状态信息对象
	 * */
	public void setMediaState(MediaState mediaState) {
		mMediaState = mediaState;
	}

	/**
	 * 开启音频同步
	 * */
	private void startSync() {
		if (syncThread != null) {
			syncThread.stopSync();
		}
		syncThread = new SyncThread();
		syncThread.startSync();
	}

	/**
	 * 关闭音频同步
	 * */
	private void stopSync() {
		if (syncThread != null) {
			syncThread.stopSync();
		}
	}

	/**
	 * 播放同步线程
	 * */
	private class SyncThread extends Thread {

		boolean needSync = true;

		/**
		 * 启动同步
		 * */
		public void startSync() {
			needSync = true;
			start();
		}

		/**
		 * 停止同步
		 * */
		public void stopSync() {
			needSync = false;
			try {
				join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void run() {
			while (needSync) {
				if (mMediaState != null) {
					Intent intent = new Intent();
					intent.setAction(PlayerConstant.BROADCAST_UPDATE_MEDIA);
					Bundle bundle = new Bundle();
					bundle.putInt(PlayerConstant.KEY_PLAY_FLAG, PlayerConstant.PLAY_MUSIC);
					bundle.putInt(PlayerConstant.KEY_PLAY_STATE, mMediaState.getPlayState());
					bundle.putInt(PlayerConstant.KEY_PLAY_DURATION, mMediaState.getDuration());
					bundle.putInt(PlayerConstant.KEY_PLAY_PROGRESS, mMediaState.getPosition());
					bundle.putInt(PlayerConstant.KEY_PLAY_VOLUME, mMediaState.getVolume());
					bundle.putInt(PlayerConstant.KEY_PLAY_VOLUME_MAX, mMediaState.getVolumeMax());
					bundle.putBoolean(PlayerConstant.KEY_PLAY_SILENT, mMediaState.isSilent());
					intent.putExtras(bundle);
					mContext.sendBroadcast(intent);
					if (mMediaState.getPlayState() == PlayerConstant.PLAYER_FINISH) {
						mMediaState.setPlayState(PlayerConstant.PLAYER_PAUSE);
					}
				}
				try {
					sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}


	private BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle bundle = new Bundle();
			bundle = intent.getExtras();
			int itemCmd = bundle.getInt(WipicoConstant.BUNDLE_SERVER_ITEM_KEY);
			switch (itemCmd) {
			case com.chinasvc.wipico.type.Audio.SERVER_CMD_MUSIC_ITME_SEEKBAR:
				int progress = bundle.getInt(WipicoConstant.BUNDLE_MEDIA_SEEKBAR_PROGRESS_KEY);
				if (mAudioReceiveListener != null) {
					mAudioReceiveListener.setAudioProgress(progress);
				}
				break;
			case com.chinasvc.wipico.type.Audio.SERVER_CMD_MUSIC_ITME_OPEN:
				String path = bundle.getString(WipicoConstant.BUNDLE_MEDIA_URL_KEY);
				String newPath = "";
				try {
					newPath = URLDecoder.decode(path, "utf-8");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				if (mAudioReceiveListener != null) {
					mAudioReceiveListener.openAudio(newPath);
				}
				break;
			case com.chinasvc.wipico.type.Audio.SERVER_CMD_MUSIC_ITME_STOP:
				if (mAudioReceiveListener != null) {
					mAudioReceiveListener.stop();
				}
				break;
			case com.chinasvc.wipico.type.Audio.SERVER_CMD_MUSIC_ITME_PAUSE:
				if (mAudioReceiveListener != null) {
					mAudioReceiveListener.pause();
				}
				break;
			case com.chinasvc.wipico.type.Audio.SERVER_CMD_MUSIC_ITME_PLAY:
				if (mAudioReceiveListener != null) {
					mAudioReceiveListener.play();
				}
				break;
			case com.chinasvc.wipico.type.Audio.SERVER_CMD_MUSIC_ITME_MUTE:
				if (mAudioReceiveListener != null) {
					mAudioReceiveListener.mute();
				}
				break;
			case com.chinasvc.wipico.type.Audio.SERVER_CMD_MUSIC_ITME_VOICE:
				if (mAudioReceiveListener != null) {
					mAudioReceiveListener.disMute();
				}
				break;
			case com.chinasvc.wipico.type.Audio.SERVER_CMD_MUSIC_ITME_VOICE_ADD:
				if (mAudioReceiveListener != null) {
					mAudioReceiveListener.addVolume();
				}
				break;
			case com.chinasvc.wipico.type.Audio.SERVER_CMD_MUSIC_ITME_VOICE_DESC:
				if (mAudioReceiveListener != null) {
					mAudioReceiveListener.decreaseVolume();
				}
				break;
			}

		}
	};

	/**
	 * 关闭音频接收控制
	 * */
	public void closeReceive() {
		mContext.unregisterReceiver(mReceiver);
		stopSync();
	}

	/**
	 * 设置控制接收监听器
	 * 
	 * @param receiveListener
	 *                控制接收监听器
	 * */
	public void setAudioReceiveListener(AudioReceiveListener audioReceiveListener) {
		this.mAudioReceiveListener = audioReceiveListener;
	}

}
