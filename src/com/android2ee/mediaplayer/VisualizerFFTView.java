package com.android2ee.mediaplayer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

public class VisualizerFFTView extends View {
	
	  private byte[] mBytes;
	  private float[] mPoints;
	  private Rect mRect = new Rect();

	  private Paint mForePaint = new Paint();
	  
	  private int mDivisions;

	  /**
	   * Renders the FFT data as a series of lines, in histogram form
	   */
	  public VisualizerFFTView(Context context) {
		  super(context);
		  init();
	  }
	    
	    

	  public VisualizerFFTView(Context context, AttributeSet attrs,
				int defStyleAttr, int defStyleRes) {
		  super(context, attrs, defStyleAttr, defStyleRes);
		  init();
	  }



	  public VisualizerFFTView(Context context, AttributeSet attrs, int defStyle) {
		  super(context, attrs, defStyle);
		  init();
	  }



	  public VisualizerFFTView(Context context, AttributeSet attrs) {
		  super(context, attrs);
		  init();
	  }



	  private void init() {
		  mBytes = null;

		  mForePaint.setAntiAlias(true);
	      mForePaint.setColor(Color.rgb(255, 128, 0));
		  mDivisions = 16;
	  }
	  
	  public void updateVisualizer(byte[] bytes) {
		  mBytes = bytes;
	      invalidate();
	  }
	  
	  public void startVisualizer() {
		  mBytes = null;
		  mPoints = null;
		  invalidate();
	  }

	  public void endVisualizer() {
		  invalidate();
	  }
	  
	  @Override
	  public void onDraw(Canvas canvas)
	  {
		  
		  if (mBytes == null) {
			  return;
		  }
		  
		  if (mPoints == null || mPoints.length < (mBytes.length * 4 / mDivisions)) {
	          mPoints = new float[(mBytes.length * 4 / mDivisions)];
	      } 
		  
		  mRect.set(0, 0, getWidth(), getHeight());
		  
		  int width_stroke = (getWidth() / mDivisions) - 5;
		  mForePaint.setStrokeWidth(width_stroke); // 5 margin 
		  
		  for (int i = 0; i < mBytes.length / mDivisions; i++) {
			  mPoints[i * 4] = i * 4 * mDivisions + width_stroke/ 2;
			  mPoints[i * 4 + 2] = i * 4 * mDivisions + width_stroke / 2;
			  byte rfk = mBytes[mDivisions * i];
			  byte ifk = mBytes[mDivisions * i + 1];
			  float magnitude = (rfk * rfk + ifk * ifk);
			  int dbValue = (int) (10 * Math.log10(magnitude));
			
			  mPoints[i * 4 + 1] = mRect.height();
			  // max 102.3502
			  mPoints[i * 4 + 3] = (float) (mRect.height() - (((dbValue * 2) * mRect.height()) / 102.3502)); 
		  }
		  canvas.drawLines(mPoints, mForePaint);
	  }
}
