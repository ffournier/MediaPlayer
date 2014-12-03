package com.android2ee.mediaplayer.view;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.audiofx.Visualizer;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;



/**
 * A simple class that draws waveform data received from a
 * {@link Visualizer.OnDataCaptureListener#onWaveFormDataCapture }
 */
public class VisualizerAmpView extends View {
	
    private ArrayList<Integer> mIntArray;
    private float[] mPoints;
    private Rect mRect = new Rect();
    
    private Paint mForePaint = new Paint();

    public VisualizerAmpView(Context context) {
        super(context);
        init();
    }
    
    

    public VisualizerAmpView(Context context, AttributeSet attrs,
			int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init();
	}



	public VisualizerAmpView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}



	public VisualizerAmpView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}



	private void init() {
		mIntArray = new ArrayList<Integer>();
        mForePaint.setStrokeWidth(1f);
        mForePaint.setAntiAlias(true);
        mForePaint.setColor(Color.rgb(0, 128, 0));
    }

    public void updateVisualizer(ArrayList<Integer> values) {
    	for (Integer value : values) {
    		mIntArray.add(value);
    	}
    	invalidate();
    }
    
    public void updateVisualizer(Integer value) {
    	mIntArray.add(value);
    	invalidate();
    }
    
    public void endVisualizer() {
    }
    
    public void startVisualizer() {
    	mIntArray.clear();
       	mPoints = null;
       	invalidate();
    }
    
    @Override
	  protected void onRestoreInstanceState(Parcelable state) {
		  if (!(state instanceof SavedState)) {
			  super.onRestoreInstanceState(state);
		  } else {
			  SavedState ss = (SavedState)state;
			  super.onRestoreInstanceState(ss.getSuperState());
			  this.mPoints = ss.mPoints;
			  this.mIntArray = ss.mIntArray;
			  invalidate();
		  }
	  }

	  @Override
	  protected Parcelable onSaveInstanceState() {
		Parcelable superState = super.onSaveInstanceState();
	    SavedState ss = new SavedState(superState);
	    //end
	    ss.mPoints = this.mPoints;
	    ss.mIntArray = this.mIntArray;

	    return ss;
	  }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mIntArray == null || mIntArray.size() == 0) {
        	return;
        }
        
      

        if (mPoints == null || mPoints.length < mIntArray.size() * 4) {
            mPoints = new float[mIntArray.size() * 4];
        } 

        mRect.set(0, 0, getWidth(), getHeight());
        
        for (int i = 0; i < mIntArray.size() - 1; i++) {
            
        	mPoints[i * 4] = mRect.width() * i / (mIntArray.size() - 1);
            mPoints[i * 4 + 1] = mRect.height() - 
                     ((mIntArray.get(i) * mRect.height() ) / 65536);
            mPoints[i * 4 + 2] = mRect.width() * (i + 1) / (mIntArray.size() - 1);
            mPoints[i * 4 + 3] = mRect.height() - 
                    ((mIntArray.get(i+1) * mRect.height() ) / 65536);
            
        }
        canvas.drawLines(mPoints, mForePaint);
    }
    
    private static class SavedState extends BaseSavedState {
		  
	    float[] mPoints;
	    ArrayList<Integer> mIntArray;
	    
	    SavedState(Parcelable superState) {
	    	super(superState);
	    }

	    @SuppressWarnings("unchecked")
		private SavedState(Parcel in) {
	    	super(in);
	    	this.mPoints = in.createFloatArray();
	    	this.mIntArray = (ArrayList<Integer>) in.readSerializable();
	    }

	    @Override
	    public void writeToParcel(Parcel out, int flags) {
	    	super.writeToParcel(out, flags);
	    	out.writeFloatArray(mPoints);
	    	out.writeSerializable(mIntArray);
	   }

	    //required field that makes Parcelables from a Parcel
	    public static final Parcelable.Creator<SavedState> CREATOR =
	        new Parcelable.Creator<SavedState>() {
	          public SavedState createFromParcel(Parcel in) {
	            return new SavedState(in);
	          }
	          public SavedState[] newArray(int size) {
	            return new SavedState[size];
	          }
	    };
  }
}