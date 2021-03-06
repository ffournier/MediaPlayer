package com.android2ee.mediaplayer.service;

import android.content.Context;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;

public class AudioFocusHelper {
	
	private AudioManager mAudioManager;
	private Context mContext;
	 
	public AudioFocusHelper(Context ctx) {
		mContext = ctx;
        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
    }

    public boolean requestFocus(OnAudioFocusChangeListener listener) {
        return AudioManager.AUDIOFOCUS_REQUEST_GRANTED ==
            mAudioManager.requestAudioFocus(listener, AudioManager.STREAM_MUSIC,
            AudioManager.AUDIOFOCUS_GAIN);
    }

    public boolean abandonFocus(OnAudioFocusChangeListener listener) {
        return AudioManager.AUDIOFOCUS_REQUEST_GRANTED ==
            mAudioManager.abandonAudioFocus(listener);
    }
    
}
