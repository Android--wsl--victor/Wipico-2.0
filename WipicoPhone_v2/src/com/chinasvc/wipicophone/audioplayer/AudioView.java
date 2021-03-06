package com.chinasvc.wipicophone.audioplayer;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.MediaController;
import android.widget.MediaController.MediaPlayerControl;

import java.io.IOException;

public class AudioView implements MediaPlayerControl {

	private String TAG = "MusicView";
	private Context mContext;

	private MediaPlayer mMediaPlayer = null;

	private Uri mUri;
	private int mDuration;
	// All the stuff we need for playing and showing a video

	private boolean mIsPrepared;
	private MediaController mMediaController;
	private int mCurrentBufferPercentage;

	private OnErrorListener mOnErrorListener;
	private OnCompletionListener mOnCompletionListener;
	private OnPreparedListener mOnPreparedListener;

	private boolean mStartWhenPrepared;
	private int mSeekWhenPrepared;

	public AudioView(Context context) {
		mContext = context;
	}

	public AudioView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
		mContext = context;
	}

	public AudioView(Context context, AttributeSet attrs, int defStyle) {
		mContext = context;
	}

	public void setAudioPath(String path) {
		setAudioURI(Uri.parse(path));
	}

	public void setAudioURI(Uri uri) {
		mUri = uri;
		mStartWhenPrepared = false;
		mSeekWhenPrepared = 0;
		openAudio();
	}

	public void stopPlayback() {
		if (mMediaPlayer != null) {
			if (mMediaPlayer.isPlaying()) {
				mMediaPlayer.stop();
			}
			mMediaPlayer.release();
			mMediaPlayer = null;
		}
	}

	private void openAudio() {
		if (mUri == null) {
			return;
		}

		if (mMediaPlayer != null) {
			mMediaPlayer.stop();
			mMediaPlayer.release();
			mMediaPlayer = null;
		}

		try {
			mMediaPlayer = new MediaPlayer();
			mMediaPlayer.setOnPreparedListener(mPreparedListener);
			mIsPrepared = false;
			mDuration = -1;// reset duration to -1 in openVideo
			mMediaPlayer.setOnCompletionListener(mCompletionListener);
			mMediaPlayer.setOnErrorListener(mErrorListener);
			mMediaPlayer.setOnBufferingUpdateListener(mBufferingUpdateListener);
			mCurrentBufferPercentage = 0;
			mMediaPlayer.setDataSource(mContext, mUri);
			mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mMediaPlayer.setScreenOnWhilePlaying(true);
			mMediaPlayer.prepareAsync();
		} catch (IOException ex) {
			Log.w(TAG, "Unable to open content: " + mUri, ex);
			return;
		} catch (IllegalArgumentException ex) {
			Log.w(TAG, "Unable to open content: " + mUri, ex);
			return;
		}
	}

	MediaPlayer.OnPreparedListener mPreparedListener = new MediaPlayer.OnPreparedListener() {
		public void onPrepared(MediaPlayer mp) {
			// briefly show the mediacontroller
			mIsPrepared = true;
			if (mOnPreparedListener != null) {
				mOnPreparedListener.onPrepared(mMediaPlayer);
			}
			if (mMediaController != null) {
				mMediaController.setEnabled(true);
			}
			// We don't know the video size yet, but should
			// start anyway.
			// The video size might be reported to us later.
			if (mSeekWhenPrepared != 0) {
				mMediaPlayer.seekTo(mSeekWhenPrepared);
				mSeekWhenPrepared = 0;
			}
			if (mStartWhenPrepared) {
				mMediaPlayer.start();
				mStartWhenPrepared = false;
			}
		}
	};

	private MediaPlayer.OnCompletionListener mCompletionListener = new MediaPlayer.OnCompletionListener() {
		public void onCompletion(MediaPlayer mp) {
			if (mMediaController != null) {
				mMediaController.hide();
			}
			if (mOnCompletionListener != null) {
				mOnCompletionListener.onCompletion(mMediaPlayer);
			}
		}
	};

	private MediaPlayer.OnErrorListener mErrorListener = new MediaPlayer.OnErrorListener() {
		public boolean onError(MediaPlayer mp, int framework_err, int impl_err) {
			Log.d(TAG, "Error: " + framework_err + "," + impl_err);
			if (mMediaController != null) {
				mMediaController.hide();
			}

			/*
			 * If an error handler has been supplied, use it and
			 * finish.
			 */
			if (mOnErrorListener != null) {
				if (mOnErrorListener.onError(mMediaPlayer, framework_err, impl_err)) {
					return true;
				}
			}
			return true;
		}
	};

	private MediaPlayer.OnBufferingUpdateListener mBufferingUpdateListener = new MediaPlayer.OnBufferingUpdateListener() {
		public void onBufferingUpdate(MediaPlayer mp, int percent) {
			mCurrentBufferPercentage = percent;
		}
	};

	/**
	 * Register a callback to be invoked when the media file is loaded and
	 * ready to go.
	 * 
	 * @param l
	 *                The callback that will be run
	 */
	public void setOnPreparedListener(MediaPlayer.OnPreparedListener l) {
		mOnPreparedListener = l;
	}

	/**
	 * Register a callback to be invoked when the end of a media file has
	 * been reached during playback.
	 * 
	 * @param l
	 *                The callback that will be run
	 */
	public void setOnCompletionListener(OnCompletionListener l) {
		mOnCompletionListener = l;
	}

	/**
	 * Register a callback to be invoked when an error occurs during
	 * playback or setup. If no listener is specified, or if the listener
	 * returned false, VideoView will inform the user of any errors.
	 * 
	 * @param l
	 *                The callback that will be run
	 */
	public void setOnErrorListener(OnErrorListener l) {
		mOnErrorListener = l;
	}

	public void start() {
		if (mMediaPlayer != null && mIsPrepared) {
			mMediaPlayer.start();
			mStartWhenPrepared = false;
		} else {
			mStartWhenPrepared = true;
		}
	}

	public void pause() {
		if (mMediaPlayer != null && mIsPrepared) {
			if (mMediaPlayer.isPlaying()) {
				mMediaPlayer.pause();
			}
		}
		mStartWhenPrepared = false;
	}

	public int getDuration() {
		if (mMediaPlayer != null && mIsPrepared) {
			if (mDuration > 0) {
				return mDuration;
			}
			mDuration = mMediaPlayer.getDuration();
			return mDuration;
		}
		mDuration = -1;
		return mDuration;
	}

	public int getCurrentPosition() {
		if (mMediaPlayer != null && mIsPrepared) {
			return mMediaPlayer.getCurrentPosition();
		}
		return 0;
	}

	public void seekTo(int msec) {
		if (mMediaPlayer != null && mIsPrepared) {
			mMediaPlayer.seekTo(msec);
		} else {
			mSeekWhenPrepared = msec;
		}
	}

	public boolean isPlaying() {
		if (mMediaPlayer != null && mIsPrepared) {
			return mMediaPlayer.isPlaying();
		}
		return false;
	}

	public int getBufferPercentage() {
		if (mMediaPlayer != null) {
			return mCurrentBufferPercentage;
		}
		return 0;
	}

	@Override
	public boolean canPause() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean canSeekBackward() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean canSeekForward() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getAudioSessionId() {
		// TODO Auto-generated method stub
		return 0;
	}
}
