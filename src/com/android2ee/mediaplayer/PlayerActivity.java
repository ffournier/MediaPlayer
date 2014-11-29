package com.android2ee.mediaplayer;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class PlayerActivity extends ABoundActivity {
	
	public static final String KEY_AUDIO_PLAY = "com.android2ee.mediaplayer.audio_player";
	
	public static final String KEY_AUDIO_PLAYING = "com.android2ee.mediaplayer.audio_playing";
	public static final String KEY_AUDIO_DURATION = "com.android2ee.mediaplayer.audio_duration";
	public static final String KEY_AUDIO_CURRENT = "com.android2ee.mediaplayer.audio_current";
	
	private POJOAudio audio = null;
	private ImageButton play;
	private ImageButton reset;
	private SeekBar seekbar;
	private TextView tvDuration;
	private TextView tvTimer;
	private GraphicalView mChartFFT;
	
	private boolean isPlayingBefore = false;
	
    private XYMultipleSeriesDataset mDatasetFFT = new XYMultipleSeriesDataset();
    private XYMultipleSeriesRenderer mRendererFFT = new XYMultipleSeriesRenderer();
    private XYSeries mCurrentSeriesFFT;
    private XYSeriesRenderer mCurrentRendererFFT;
    
    private class MyHandler extends Handler {
		
		@Override
	    public void handleMessage(Message message) {
			int progress;
			int duration;
			switch (message.what) {
			case MediaService.PLAY_PROGRESS:
	        	progress = message.getData().getInt(MediaService.KEY_PLAY_CURRENT);
	        	duration = message.getData().getInt(MediaService.KEY_PLAY_DURATION);
	        	updateResource(true, duration, progress);
			    break;
			    
	        case MediaService.PLAY_END:
	        	progress = message.getData().getInt(MediaService.KEY_PLAY_CURRENT);
        		duration = message.getData().getInt(MediaService.KEY_PLAY_DURATION);
        		updateResource(false, duration, duration);
	        	mService.restartPlayer();
	        	break;
	       case MediaService.PLAY_FFT:
	        	byte[] fft = message.getData().getByteArray(MediaService.KEY_PLAY_FFT);
	        	int rateFFT = message.getData().getInt(MediaService.KEY_PLAY_RATE);
        		addDataFFT(fft, rateFFT);
	            break;    
	        }
			
		}
	}
	
	private Handler myHandler = new MyHandler();
	
	@Override
	protected void serviceConnected(MediaService service) {
		//TODO start play ?
		mService.setHandler(myHandler);
		if (!mService.isPlayerCreate()) {
			mService.startPlayer(audio.getPath());
		} else if (isPlayingBefore) {
			Log.i("PlayerActivity", "playing before ");
			mService.prPlayer();
		} else {
			Log.i("PlayerActivity", "not playing before ");
		}
	}
	
	/**
	 * 
	 */
	private void disconnect() {
		if (mBound) {
			
			isPlayingBefore = mService.isPlayerPlay();
			if (isFinishing()) {
				mService.stopPlayer();
			} else {
				if (mService.isPlayerPlay()) {
					mService.prPlayer();
				}
			}
			mService.setHandler(null);
		} else {
			isPlayingBefore = false;
		}
	}
	
	@Override
	protected void serviceDisconnected(MediaService service) {
		disconnect();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_player);
		
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			audio = bundle.getParcelable(KEY_AUDIO_PLAY);
		} else {
			// TODO Error
		}
		
		play = (ImageButton) findViewById(R.id.player_play);
		play.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO 
				if (mBound) {
					if (!mService.isPlayerCreate()) {
						mService.startPlayer(audio.getPath());
					} else {
						mService.prPlayer();
					}
					updateResource(mService.isPlayerPlay(), mService.getPlayerDuration(), mService.getPlayerProgress());
				}
			}
		});
		
		reset = (ImageButton) findViewById(R.id.player_reset);
		reset.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO 
				if (mBound) {
					if (mService.isPlayerCreate()) {
						mService.restartPlayer();
					}
					updateResource(mService.isPlayerPlay(), mService.getPlayerDuration(), mService.getPlayerProgress());
				}
			}
		});
		
		seekbar = (SeekBar) findViewById(R.id.player_seekbar);
		seekbar.setIndeterminate(true);
		seekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				if (mBound) {
					if (!seekBar.isIndeterminate()) {
						int progress = seekBar.getProgress();
						if (mBound) {
							mService.seekPlayerTo(progress);
						}
					}
				}
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
			}
		});
		
		tvDuration = (TextView) findViewById(R.id.player_duration);
		tvTimer = (TextView) findViewById(R.id.player_timer);
	}
	
	private void updateResource(boolean isPlaying, int duration, int progress) {
		play.setImageResource(isPlaying ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play);
		
		// seek bar ?
		
		if (duration > 0) {
			seekbar.setIndeterminate(false);
			seekbar.setMax(duration);
			seekbar.setProgress(progress);
			
			// Text
			SimpleDateFormat format = new SimpleDateFormat("HH-mm-ss", Locale.getDefault());
			format.setTimeZone(TimeZone.getTimeZone("UTC"));
			tvDuration.setText(format.format(new Date(duration * 1000)));
			tvTimer.setText(format.format(new Date(progress * 1000)));
		} else {
			seekbar.setIndeterminate(true);
			seekbar.setMax(0);
			seekbar.setProgress(0);
			tvDuration.setText("");
			tvTimer.setText("");
		}
	}
	
	
	
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		
		outState.putInt(KEY_AUDIO_DURATION, seekbar.getMax());
		outState.putInt(KEY_AUDIO_CURRENT, seekbar.getProgress());
		outState.putBoolean(KEY_AUDIO_PLAYING, !seekbar.isIndeterminate());
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		int duration = savedInstanceState.getInt(KEY_AUDIO_DURATION);
		int progress = savedInstanceState.getInt(KEY_AUDIO_CURRENT);
		isPlayingBefore = savedInstanceState.getBoolean(KEY_AUDIO_PLAYING);
		Log.i("PlayerActivity", "onRestoreInstanceState");
		if (isPlayingBefore && mService != null && !mService.isPlayerPlay()) {
			mService.prPlayer();
		} 
		updateResource(isPlayingBefore, duration, progress);
	}

	@Override
	protected void onStart() {
		super.onStart();
		
		LinearLayout layoutFFT = (LinearLayout) findViewById(R.id.player_chart1);
        if (mChartFFT == null) {
            initChartFFT();
            mChartFFT = ChartFactory.getCubeLineChartView(this, mDatasetFFT, mRendererFFT, 0.3f);
            mChartFFT.setBackgroundResource(android.R.color.transparent);
            layoutFFT.addView(mChartFFT);
        } else {
        	mChartFFT.repaint();
        }
	}
	
	/**
	 * 
	 */
	private void initChartFFT() {
        mCurrentSeriesFFT = new XYSeries("FFT Data");
        mDatasetFFT.addSeries(mCurrentSeriesFFT);
        mCurrentRendererFFT = new XYSeriesRenderer();
        mCurrentRendererFFT.setColor(Color.BLUE);
        mRendererFFT.addSeriesRenderer(mCurrentRendererFFT);
        mRendererFFT.setApplyBackgroundColor(true);
        mRendererFFT.setBackgroundColor(Color.argb(0x00, 0xff, 0x00, 0x00));
        mRendererFFT.setMarginsColor(Color.argb(0x00, 0xff, 0x00, 0x00));
        mRendererFFT.setYAxisMax(128);
        mRendererFFT.setYAxisMin(-128);
    }
	
	/**
	 * 
	 */
    private void addDataFFT(byte[] fft, int rate) {
    	mCurrentSeriesFFT.clear();
    	for (int i = 0 ; i < fft.length; i+=2) {
    		mCurrentSeriesFFT.add( (i/2), fft[i]);
    	}
        mChartFFT.repaint();
    }
	
	
	@Override
	protected void onStop() {
		disconnect();
		super.onStop();
		
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		
	}

}
