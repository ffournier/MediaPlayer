package com.android2ee.mediaplayer;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaRecorder;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

/**
 * 
 * @author florian
 *
 */
public class MediaService extends Service {
	
	
	public static final int PLAY_PROGRESS = 0;
	public static final int PLAY_END = 1;
	
	public static final String KEY_PLAY_DURATION = "com.android2ee.mediaplayer.duration_media";
	public static final String KEY_PLAY_CURRENT = "com.android2ee.mediaplayer.current_media";
	
	
	//declaration
	private MediaRecorder mRecorder;
	private MediaPlayer mPlayer;
	private StatePlayer isRecord;
	private StatePlayer isPlay;
	
	private Timer mTimer;
	
	private Handler handler;
	
	public enum StatePlayer {
		STATE_DEFAULT,
		STATE_PLAY,
		STATE_PAUSE
	}
	
	private Object objectRecord = null;
	
	private LocalBinder mBinder = new LocalBinder();
	
	/**
	 * Binder
	 * @author florian
	 *
	 */
	public class LocalBinder extends Binder {

		public MediaService getService() {
			return MediaService.this;
		}
		
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		
		mRecorder = new MediaRecorder();
		mPlayer = new MediaPlayer();
		mPlayer.setOnCompletionListener(new OnCompletionListener() {
			
			@Override
			public void onCompletion(MediaPlayer mp) {
				isPlay = StatePlayer.STATE_PAUSE;
				sendPlayEnding();
			}
		});
		isRecord = StatePlayer.STATE_DEFAULT;
		isPlay = StatePlayer.STATE_DEFAULT;
		handler = null;
		
		mTimer = new Timer();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return super.onStartCommand(intent, flags, startId);
	}
	
	public void setHandler(Handler handler) {
		this.handler = handler;
		
	}
	
	public boolean isRecorderCreate() {
		return isRecord != StatePlayer.STATE_DEFAULT;
	}
	
	public boolean isRecorderPlay() {
		return isRecord == StatePlayer.STATE_PLAY;
	}
	
	
	/**
	 * 
	 * @return
	 */
	public boolean startRecorder(String path) {
		if (isRecord !=  StatePlayer.STATE_DEFAULT) {
			stopRecorder();
		}
		
		if (mRecorder != null) {
			// TODO
			mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
			mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
			mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
			mRecorder.setOutputFile(path);
			try {
				mRecorder.prepare();
				mRecorder.start();
				isRecord = StatePlayer.STATE_PLAY;
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return isRecord !=  StatePlayer.STATE_DEFAULT;
	}
	
	
	/**
	 * 
	 * @return
	 */
	public boolean stopRecorder() {
		if (mRecorder != null) {
			if (isRecord != StatePlayer.STATE_DEFAULT) {
				mRecorder.stop();
				mRecorder.reset();
				isRecord = StatePlayer.STATE_DEFAULT;
			}
		}
		return isRecord == StatePlayer.STATE_DEFAULT;
	}
	
	/**
	 * 
	 */
	public void releaseRecorder() {
		if (mRecorder != null) {
			stopRecorder();
			mRecorder.release();
			isRecord = StatePlayer.STATE_DEFAULT; 
		}
	}
	
	/**
	 * 
	 * @param object
	 */
	public void setAudioRecord(Object object) {
		objectRecord = object;
	}
	
	/**
	 * 
	 * @return
	 */
	public Object getAudioRecord() {
		return objectRecord;
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isPlayerCreate() {
		return isPlay != StatePlayer.STATE_DEFAULT;
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isPlayerPlay() {
		return isPlay == StatePlayer.STATE_PLAY ;
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean startPlayer(String path) {
		if (isPlay != StatePlayer.STATE_DEFAULT) {
			stopPlayer();
		}
		if (mPlayer != null) {
			// TODO 
			try {
				mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
				mPlayer.setDataSource(path);
				mPlayer.prepare();
				mPlayer.start();
				isPlay = StatePlayer.STATE_PLAY;
				scheduleTimer();
			} catch (IllegalArgumentException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (SecurityException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return isPlay != StatePlayer.STATE_DEFAULT;
	}
	
	
	/**
	 * 
	 * @return
	 */
	public void prPlayer(){
		if(isPlay != StatePlayer.STATE_DEFAULT){
			if (isPlay == StatePlayer.STATE_PLAY) {
				mPlayer.pause();
				isPlay = StatePlayer.STATE_PAUSE;
				purgeTimer();
			} else {
				mPlayer.start();
				isPlay = StatePlayer.STATE_PLAY;
				scheduleTimer();
			}
		}
	}
	
	/**
	 * 
	 */
	public void restartPlayer() {
		if(isPlay != StatePlayer.STATE_DEFAULT){
			mPlayer.seekTo(0);
		}
	}
	
	/**
	 * 
	 */
	public int getPlayerProgress() {
		int result = 0;
		if(isPlay != StatePlayer.STATE_DEFAULT){
			result = mPlayer.getCurrentPosition() / 1000;
		}
		return result;
	}
	
	/**
	 * 
	 */
	public int getPlayerDuration() {
		int result = 0;
		if(isPlay != StatePlayer.STATE_DEFAULT){
			result = mPlayer.getDuration() / 1000;
		}
		return result;
	}
	
	/**
	 * 
	 */
	public void seekPlayerTo(int progress) {
		if(isPlay != StatePlayer.STATE_DEFAULT){
			mPlayer.seekTo(progress * 1000);
		}
	}
	
	/**
	 * 
	 */
	private void sendPlayProgress() {
		if (handler != null && isPlayerPlay()) {
			Message msg = handler.obtainMessage();
			msg.what = PLAY_PROGRESS;
			Bundle bundle = new Bundle();
			bundle.putInt(KEY_PLAY_DURATION, mPlayer.getDuration() / 1000);
			bundle.putInt(KEY_PLAY_CURRENT, mPlayer.getCurrentPosition() / 1000);
			msg.setData(bundle);
			handler.sendMessage(msg);
		}
	}
	
	/**
	 * 
	 */
	private void sendPlayEnding() {
		if (handler != null) {
			Message msg = handler.obtainMessage();
			msg.what = PLAY_END;
			Bundle bundle = new Bundle();
			bundle.putInt(KEY_PLAY_DURATION, mPlayer.getDuration() / 1000);
			bundle.putInt(KEY_PLAY_CURRENT, mPlayer.getCurrentPosition() / 1000);
			msg.setData(bundle);
			handler.sendMessage(msg);
		}
	}
	
	/**
	 * 
	 */
	private void scheduleTimer() {
		if (mTimer != null) {
			TimerTask timerTask = new TimerTask() {
				
				@Override
				public void run() {
					sendPlayProgress();
				}
			};
			
			mTimer.scheduleAtFixedRate(timerTask, 0, 1000);
			
		}
	}
	
	/**
	 * 
	 */
	private void purgeTimer() {
		if (mTimer != null) {
			mTimer.purge();
		}
	}
	
	/**
	 * 
	 */
	private void cancelTimer() {
		if (mTimer != null) {
			mTimer.cancel();
		}
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean stopPlayer() {
		if (mPlayer != null) {
			if (isPlay != StatePlayer.STATE_DEFAULT) {
				mPlayer.stop();
				mPlayer.reset();
				purgeTimer();
				
				isPlay = StatePlayer.STATE_DEFAULT;
			}
		}
		return isPlay == StatePlayer.STATE_DEFAULT;
	}
	
	/**
	 * 
	 */
	public void releasePlayer() {
		if (mPlayer != null) {
			stopPlayer();
			mPlayer.release();
			isPlay = StatePlayer.STATE_DEFAULT;
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		
		releaseRecorder();
		releasePlayer();
		
		cancelTimer();
		
		
	}
	
}
