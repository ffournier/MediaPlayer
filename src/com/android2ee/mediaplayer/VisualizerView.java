package com.android2ee.mediaplayer;

import java.util.ArrayList;
import java.util.Date;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.audiofx.Visualizer;
import android.util.AttributeSet;
import android.view.View;



/**
 * A simple class that draws waveform data received from a
 * {@link Visualizer.OnDataCaptureListener#onWaveFormDataCapture }
 */
class VisualizerView extends View {
	
    private ArrayList<Byte> mBytesArray;
    private byte[] mBytes;
    private float[] mPoints;
    private Rect mRect = new Rect();
    
    private Paint mForePaint = new Paint();

    public VisualizerView(Context context) {
        super(context);
        init();
    }
    
    

    public VisualizerView(Context context, AttributeSet attrs,
			int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init();
	}



	public VisualizerView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}



	public VisualizerView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}



	private void init() {
        mBytesArray = new ArrayList<Byte>();
        mBytes = null;
        mForePaint.setStrokeWidth(1f);
        mForePaint.setAntiAlias(true);
        mForePaint.setColor(Color.rgb(0, 128, 255));
    }

    public void updateVisualizer(byte[] bytes) {
    	mBytes = bytes;
    	for (byte value : bytes) {
    		mBytesArray.add(value);
    	}
    	invalidate();
    }
    
    public void endVisualizer() {
    	mBytes = null;
    	Byte[] temp = mBytesArray.toArray(new Byte[mBytesArray.size()]);
       	mBytes = new byte[temp.length];
       	for (int i = 0; i < temp.length ; i++) {
       		mBytes[i] = temp[i];
       	}
       	mBytesArray.clear();
       	invalidate();
    }
    
    public void startVisualizer() {
    	mBytes = null;
       	mBytesArray.clear();
       	mPoints = null;
       	invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mBytes == null || mBytes.length == 0) {
        	return;
        }
        
      

        if (mPoints == null || mPoints.length < mBytes.length * 4) {
            mPoints = new float[mBytes.length * 4];
        } 

        mRect.set(0, 0, getWidth(), getHeight());
        
        for (int i = 0; i < mBytes.length - 1; i++) {
            
        	mPoints[i * 4] = mRect.width() * i / (mBytes.length - 1);
            mPoints[i * 4 + 1] = mRect.height() / 2
                    + ((byte) (mBytes[i] + 128)) * (mRect.height() / 2) / 128;
            mPoints[i * 4 + 2] = mRect.width() * (i + 1) / (mBytes.length - 1);
            mPoints[i * 4 + 3] = mRect.height() / 2
                    + ((byte) (mBytes[i + 1] + 128)) * (mRect.height() / 2) / 128;
            
        }
        canvas.drawLines(mPoints, mForePaint);
    }
}