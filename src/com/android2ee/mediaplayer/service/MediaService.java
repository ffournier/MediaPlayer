package com.android2ee.mediaplayer.service;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;


import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaRecorder;
import android.media.audiofx.Visualizer;
import android.media.audiofx.Visualizer.OnDataCaptureListener;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

/**
 * 
 * @author florian
 *
 */
public class MediaService extends Service implements OnAudioFocusChangeListener {
	
	
	public static final int PLAY_PROGRESS = 0;
	public static final int PLAY_END = 1;
	public static final int PLAY_START = 2;
	public static final int PLAY_FFT = 3;
	public static final int PLAY_WAVEFROM = 4;
	
	
	public static final int ACTION_STOP = 1;
	
	
	public static final String KEY_PLAY_DURATION = "com.android2ee.mediaplayer.duration_media";
	public static final String KEY_PLAY_CURRENT = "com.android2ee.mediaplayer.current_media";
	public static final String KEY_PLAY_FFT = "com.android2ee.mediaplayer.fft_media";
	public static final String KEY_PLAY_WAVEFROM = "com.android2ee.mediaplayer.wavefrom_media";
	public static final String KEY_PLAY_RATE = "com.android2ee.mediaplayer.rate_media";
	public static final String KEY_ACTION_PLAY = "com.android2ee.mediaplayer.action_play";
	
	
	//declaration
	private MediaRecorder mRecorder;
	private MediaPlayer mPlayer;
	private StatePlayer isRecord;
	private StatePlayer isPlay;
	
	private Timer mTimer;
	
	private Handler handler;
	
	private AudioFocusHelper audioFocusHelper;
	
	private Visualizer mVisualizer;

	public enum StatePlayer {
		STATE_DEFAULT,
		STATE_PLAY,
		STATE_PAUSE,
		STATE_END
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
				isPlay = StatePlayer.STATE_END;
				mVisualizer.setEnabled(false);
				sendPlayEnding();
			}
		});
		isRecord = StatePlayer.STATE_DEFAULT;
		isPlay = StatePlayer.STATE_DEFAULT;
		handler = null;
		
		mTimer = new Timer();
		
		audioFocusHelper = new AudioFocusHelper(this);
		audioFocusHelper.requestFocus(this);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent != null && intent.getExtras() != null && intent.getExtras().containsKey(KEY_ACTION_PLAY)) {
			int action = intent.getIntExtra(KEY_ACTION_PLAY, -1);
			switch(action) {
			case ACTION_STOP :
				Log.i("MediaService", "ACTION STOP");
				if (isPlayerCreate() && isPlayerPlay()) {
					Log.i("MediaService", "ACTION STOP execute");
					prPlayer();
				}
				break;
			}
		}
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
				
				Log.i("MediaService", "audio id " +mPlayer.getAudioSessionId());
				if (mVisualizer != null) {
					mVisualizer.setEnabled(false);
					mVisualizer.release();
					mVisualizer = null;
				}
				mVisualizer = new Visualizer(mPlayer.getAudioSessionId());
				mVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
				Log.i("MediaService", "audio size " + Visualizer.getCaptureSizeRange()[1]);
				mVisualizer.setDataCaptureListener(new OnDataCaptureListener() {
					
					@Override
					public void onWaveFormDataCapture(Visualizer visualizer, byte[] waveform,
							int samplingRate) {
						sendPlayWaveFrom(waveform, samplingRate);
					}
					
					@Override
					public void onFftDataCapture(Visualizer visualizer, byte[] fft,
							int samplingRate) {
						sendPlayFFT(fft, samplingRate);
					}
				}, Visualizer.getMaxCaptureRate() / 2, true, true);
				
				mPlayer.prepareAsync();
				mPlayer.setOnPreparedListener(new OnPreparedListener() {
					
					@Override
					public void onPrepared(MediaPlayer mp) {
						mPlayer.start();
						isPlay = StatePlayer.STATE_PLAY;
						sendPlayStart();
						scheduleTimer();
						mVisualizer.setEnabled(true);
					}
				});
				
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
		Log.i("MediaService", "prPlayer");
		if(isPlay != StatePlayer.STATE_DEFAULT){
			if (isPlay == StatePlayer.STATE_PLAY) {
				Log.i("MediaService", "prPlayer Pause");
				mPlayer.pause();
				mVisualizer.setEnabled(false);
				isPlay = StatePlayer.STATE_PAUSE;
				purgeTimer();
			} else {
				Log.i("MediaService", "prPlayer Play");
				mPlayer.start();
				mVisualizer.setEnabled(true);
				if (isPlay == StatePlayer.STATE_END) {
					sendPlayStart();
				}
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
	private void sendPlayStart() {
		if (handler != null && isPlayerCreate()) {
			Message msg = handler.obtainMessage();
			msg.what = PLAY_START;
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
	private void sendPlayFFT(byte[] fft, int rate) {
		if (handler != null && isPlayerCreate()) {
			Message msg = handler.obtainMessage();
			msg.what = PLAY_FFT;
			Bundle bundle = new Bundle();
			bundle.putInt(KEY_PLAY_DURATION, mPlayer.getDuration() / 1000);
			bundle.putInt(KEY_PLAY_CURRENT, mPlayer.getCurrentPosition() / 1000);
			bundle.putByteArray(KEY_PLAY_FFT, fft);
			bundle.putInt(KEY_PLAY_RATE, rate);
			msg.setData(bundle);
			handler.sendMessage(msg);
		}
	}
	
	/**
	 * 
	 */
	private void sendPlayWaveFrom(byte[] waveFrom, int rate) {
		if (handler != null && isPlayerCreate()) {
			Message msg = handler.obtainMessage();
			msg.what = PLAY_WAVEFROM;
			Bundle bundle = new Bundle();
			bundle.putInt(KEY_PLAY_DURATION, mPlayer.getDuration() / 1000);
			bundle.putInt(KEY_PLAY_CURRENT, mPlayer.getCurrentPosition() / 1000);
			bundle.putByteArray(KEY_PLAY_WAVEFROM, waveFrom);
			bundle.putInt(KEY_PLAY_RATE, rate);
			msg.setData(bundle);
			handler.sendMessage(msg);
		}
	}
	
	/**
	 * 
	 */
	private void sendPlayEnding() {
		if (handler != null && isPlayerCreate()) {
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
			
			mTimer.scheduleAtFixedRate(timerTask, 0, 500);
			
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
		audioFocusHelper.abandonFocus(this);
		
		releaseRecorder();
		releasePlayer();
		
		cancelTimer();
		
		if (mVisualizer != null) {
			mVisualizer.release();
		}
		
		super.onDestroy();
		
	}

	@Override
	public void onAudioFocusChange(int focusChange) {
		switch(focusChange) {
			case AudioManager.AUDIOFOCUS_GAIN:
			case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT:
			case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE:
				if (isPlayerCreate() && isPlayerPlay()) {
					mPlayer.setVolume(1.0f, 1.0f);
				}
				break;
			
			case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK:
				if (isPlayerCreate() && isPlayerPlay()) {
					mPlayer.setVolume(0.8f, 0.8f);
				}
			break;
			case AudioManager.AUDIOFOCUS_LOSS:
				if (isPlayerCreate() && isPlayerPlay()) {
					mPlayer.setVolume(1.0f, 1.0f);
				}
				break;
			case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
				Log.i("MediaService", "AUDIOFOCUS_LOSS_TRANSIENT");
				if (isPlayerCreate() && isPlayerPlay()) {
					Log.i("MediaService", "AUDIOFOCUS_LOSS_TRANSIENT execute");
					
					prPlayer();
				}
				break;
			case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
				if (isPlayerCreate() && isPlayerPlay()) {
					mPlayer.setVolume(0.2f, 0.2f);
				}
				break;
		}
	}
	
}
