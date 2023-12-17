package com.android.systemui.powerwidget;

import android.widget.*;
import android.view.*;
import android.util.*;
import android.content.*;
import android.graphics.*;
import android.os.*;
import android.graphics.drawable.*;

public class PowerButtonView extends LinearLayout
{
	private static final int STATE_IN = 0;
	private static final int STATE_PRESS = 1;
	private static final int STATE_RELEASE = 2;
	private static final int STATE_OUT = 3;

	private int mState;

	
	private float mCx, mCy;
	private float mRadius;
	private long mStartTime;
	private static final long mDuration = 350;
	private float mAlphaPercent;

	private boolean isRunning;
	private static final int FPS = 1000 / 60;
	private int COLOR;
	private Paint mCircle ;
	
	private View mView;
	private Drawable mRipple;
	
	private class Ripple extends Drawable
	{
		public Ripple()
		{
			if(COLOR==0)COLOR = 0xaaffffff;
			mCircle = new Paint();
			mCircle.setAntiAlias(true);
			mCircle.setColor(COLOR);
			mCircle.setStyle(Paint.Style.FILL);
			
		}
		@Override
		public void draw(Canvas p1)
		{
			mCircle.setAlpha((int)Math.round(255 * mAlphaPercent));
			p1.drawCircle(mCx,mCy,mRadius,mCircle);
			// TODO: Implement this method
		}

		@Override
		public int getOpacity()
		{
			// TODO: Implement this method
			return 0;
		}

		@Override
		public void setAlpha(int p1)
		{
			// TODO: Implement this method
		}

		@Override
		public void setColorFilter(ColorFilter p1)
		{
			// TODO: Implement this method
		}
		
	}
	private float mColumnSpan;
	
	public PowerButtonView(Context c, AttributeSet as){
		super(c,as);
		mColumnSpan = 1f;
		mView=this;
		
	}
		@Override
	protected void onLayout(boolean p1, int p2, int p3, int p4, int p5)
	{
		// TODO: Implement this method
		super.onLayout(p1, p2, p3, p4, p5);
		mCx = mView.getWidth()/2f;
		mCy = mView.getHeight()/2f;

		// the circle should fit to center 
		mRadius = mView.getWidth() < mView.getHeight() ? mCx : mCy ;		
		
		mRipple = new Ripple();
		setBackgroundDrawable(mRipple);
	}
	public void setColumnSpan(float i){
		mColumnSpan = i;
	}
	public float getColumnSpan(){
		return mColumnSpan;
	}
	public void setContent(int layoutId, LayoutInflater inflater) {
        inflater.inflate(layoutId, this);
    }
		private void setState(int s)
	{
		mState = s;

		switch (mState)
		{
			case STATE_PRESS:
			case STATE_OUT:
				stop();
				break;
		}
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent p1)
	{
		onTouch(p1);
		// TODO: Implement this method
		return super.dispatchTouchEvent(p1);
	}

	
	private boolean onTouch(MotionEvent me)
	{
		switch (me.getAction())
		{
			case MotionEvent.ACTION_DOWN:
				setState(STATE_IN);
				mStartTime = SystemClock.uptimeMillis();
				start();
				
				break;
				/*			case MotionEvent.ACTION_MOVE:
				 break;
				 */
			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP:
				setState(STATE_RELEASE);
				mStartTime = SystemClock.uptimeMillis();
				start();
				break;
		}
		return true;
	}
	private void animate()
	{
		if (mState == STATE_IN)
		{
			float backgroundProgressIn = Math.min(1f, (float)(SystemClock.uptimeMillis() - mStartTime) / mDuration);

			mAlphaPercent = backgroundProgressIn * Color.alpha(COLOR) / 255f;
			if (backgroundProgressIn == 1f)
			{
				setState(STATE_PRESS);
			}
		}
		if (mState == STATE_RELEASE)
		{
			float backgroundProgressOut = Math.min(1f, (float)(SystemClock.uptimeMillis() - mStartTime) / mDuration);

			mAlphaPercent = (1f - backgroundProgressOut) * Color.alpha(COLOR) / 255f;

			if (backgroundProgressOut == 1f)
				setState(STATE_OUT);
		}
		
		// update
		if(isRunning)postDelayed(mScheduler, FPS);
		mRipple.invalidateSelf();
	}
	private Runnable mScheduler =new Runnable(){

		@Override
		public void run()
		{
			animate();
			// TODO: Implement this method
		}
	};
	void start(){
		isRunning=true;
		post(mScheduler);
	}
	void stop(){
		isRunning=false;
		removeCallbacks(mScheduler);
	}
	
	public void setColor(int c){
		COLOR = c&0xaaffffff;
		if(mCircle!=null)mCircle.setColor(COLOR);
		setBackgroundDrawable(mRipple);
	}
}
