package com.android.systemui.statusbar;

import android.content.*;
import android.util.*;
import android.view.*;
import android.widget.*;
import com.android.systemui.observablescrollview.*;
import support.animator.view.*;
import android.graphics.*;
import support.animator.animation.*;
import android.view.animation.*;

import com.android.systemui.*;
import android.os.*;


/*
 *   View that control touch movement
 *   to create Material expanded
 *
 *   created by Dhanu Dwi Arfendi
 *
 *   some code are taken from stackoverflow , github and 
 *   Thanks to them who share their code!
 */

public class PanelContainerView extends FrameLayout
implements ObservableScrollViewCallbacks
{
	StatusBarService mService;

	private VelocityTracker mVelocityTracker = null;
	private boolean mStartAtZero;
	private int mScrollYOnDown;
	private boolean mToggleIsFullyOpen = false;
	private boolean mToggleIsOpen=false;
	private boolean mDownWhenToggleOpen = false;
	private long mZeroScrollerTime;

	private OnTranslationListener mTranslationListener ;




	public PanelContainerView(Context c, AttributeSet as)
	{
		super(c, as);
	}
	private boolean debug = true;
	private String tag = "PanelContainer";
	ObservableScrollView skrol;
	Button notif;

	TextView info;
	View togel;
	View movingView;
	ViewGroup konten, ganjel;
	boolean isTogelVisible;
	boolean isUpOrCancel;
	int togelHeight, togelInd;
	View ehem;
	ViewGroup.LayoutParams lp;
	WindowManager.LayoutParams winLp;
	private ValueAnimator mToggleAnimator;
	private FlingAnimationUtils mFlingAnimationUtils;
	int parentHeight;
	private int mMaxQsHeight ;

	@Override
	protected void onFinishInflate()
	{
		// TODO: Implement this method
		super.onFinishInflate();

		skrol = (ObservableScrollView)findViewById(R.id.scroll);
		togel = findViewById(R.id.qs_container);
		ehem = findViewById(R.id.ganjelView);
		mFlingAnimationUtils = new FlingAnimationUtils(getContext(), 0.2f);
		lp =  ehem.getLayoutParams(); //  new ViewGroup.MarginLayoutParams(togel.getWidth(),togel.getHeight());

		skrol.setScrollViewCallbacks(this);
		skrol.setVerticalFadingEdgeEnabled(false);

		// dikurangi 1 pixel ben ra ana blank space nang nduwur togel
		mMaxQsHeight = (getContext().getResources().getDimensionPixelSize(R.dimen.pw_height)-1);
	}

	int firstY;
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev)
	{
		int initialY = 0;
		switch (ev.getActionMasked())
		{
			case MotionEvent.ACTION_DOWN:
				initialY = (int) ev.getRawY();
				initVelocityTracker();
				trackMovement(ev);

				firstY = (int)ev.getY();

				mStopZeroScroller = true;
				removeCallbacks(mZeroScroller);

				toggleHeight = ehem.getHeight();
				mDownWhenToggleOpen = toggleHeight > 0;
				if (!mDownWhenToggleOpen)skrol.setBlockFlinging(false);

				mScrollYOnDown = skrol.getScrollY();
				mStartAtZero = mScrollYOnDown <= 1;
				
				break;
			case MotionEvent.ACTION_MOVE:
				trackMovement(ev);

				final int currY = (int)ev.getRawY();
				final int deltaY = initialY - currY;

				// geser nduwur
				if (deltaY > 0)
				{
					updateExpandedPos(deltaY);
				}
				break;
			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP:
				trackMovement(ev);
	//			flingCollapse((int)ev.getRawY());
				recycleTracker();
				
				int deltaUp = initialY-(int)ev.getRawY();
				//if(deltaUp > 170 && !toggleNotOpen)mService.performCollapse();
				break;
		}
		// TODO: Implement this method
		return super.onInterceptTouchEvent(ev);
	}

	private int deltaY = 0;
	private boolean alreadyUp = false;;
	@Override
	public void onTouchScroll(MotionEvent ev)
	{
		switch (ev.getActionMasked())
		{
			case MotionEvent.ACTION_DOWN:
				alreadyUp = false;
				break;

			case MotionEvent.ACTION_MOVE:
				final float currY = ev.getY();
				deltaY = (int)currY - firstY;
				mToggleIsOpen = ehem.getHeight() > 1;
				if (ehem.getHeight() <= 1)mToggleIsFullyOpen = false;
				if (ehem.getHeight() >= mMaxQsHeight && alreadyUp)mToggleIsFullyOpen = true;

				final int deltaTopY = deltaY;
				if (!skrol.canScroll())
				{
					if (!mToggleIsFullyOpen)
					{
						if (deltaTopY >= 1)
							onTopOverscrolled(deltaTopY);
							
						if (deltaTopY < -220)
							mService.animateCollapse();
					}

					else
					{
						if (deltaTopY <= -1)
							onTopOverscrolled(deltaTopY);
					}
				}
				else
				{
					if (!mToggleIsFullyOpen)
					{
						if (deltaTopY >= 1 /* && (scrollY <= 0) */ && (mStartAtZero)){
							skrol.scrollTo(0, 0);
							onTopOverscrolled(deltaTopY);
							}
					}

					else
					{
						if (deltaTopY <= -1 && mToggleIsOpen)
						{
							skrol.scrollTo(0, 0);
							onTopOverscrolled(deltaTopY);
						}
					}
				}
				break;

			case MotionEvent.ACTION_UP:
				final int yUp = deltaY;
				alreadyUp = true;
				if (!mToggleIsFullyOpen && (skrol.canScroll() ? mStartAtZero : true))
				{
					if (yUp >= 20)
					{
						animateQs(yUp, true);
					}
					else if (yUp > 0 && yUp < 20)
					{
						animateQs(yUp, false);
					}
					else if(yUp<0 && mToggleIsOpen){
						animateQs(false);
					}
				}
				else if (mToggleIsFullyOpen)
				{
					if (yUp < -10)
					{
						animateQs(yUp, false);
					}
				}
				if (yUp < -10)
				{
					if (mDownWhenToggleOpen)// && (ehem.getHeight() >0))
					{	skrol.smoothScrollTo(0, 0);
						mStopZeroScroller = false;
						skrol.setBlockFlinging(true);

						post(mZeroScroller);
						mZeroScrollerTime = SystemClock.uptimeMillis();
					}
				}
				break;
		}
		// TODO: Implement this method
	}

	@Override
	public void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging, ScrollState scrollState)
	{
		this.scrollY = scrollY;

	}

	int toggleHeight = 0;
	
	public void onTopOverscrolled(int scrollY)
	{
		setQsTranslation(scrollY + toggleHeight, false);
	}

	private AnimatorListenerAdapter animListener = new AnimatorListenerAdapter(){
		@Override
		public void onAnimationStart(Animator animation)
		{
			skrol.setBlockFlinging(true);
			//		Toast.makeText(getContext(), "start", 0).show();
		}
		@Override
		public void onAnimationEnd(Animator animation)
		{
			skrol.setBlockFlinging(false);
			skrol.smoothScrollTo(0, 0);
			//		Toast.makeText(getContext(), "end", 0).show();
		}
	};

	int lastH =0;
	int scrollY = 0;
	int topOverScrolledY;

	public void animateQs(boolean open)
	{
		int h = ehem.getHeight() ;
		animateQs(h, open, true);
	}
	private void animateQs(int initialHeight, boolean isExpanding){
		animateQs(initialHeight, isExpanding, false);
	}
	private void animateQs(int initialHeight, boolean isExpanding, boolean noTouch)
	{
		int startY = Math.min(initialHeight, mMaxQsHeight);
		int fromY = startY + (noTouch? 0 : toggleHeight);
		int toY = isExpanding ? mMaxQsHeight : 0;

		ValueAnimator animator = ValueAnimator.ofInt(fromY, toY);
        mFlingAnimationUtils.apply(animator, fromY, toY, getCurrentVelocity());
		animator.addListener(animListener);
		animator.addUpdateListener(mToggleAnimatorUpdater);
		//	Log.d("qsanimator", "dur: "+animator.getDuration()+" vel: "+ getCurrentVelocity() +" from: " +fromY);
		animator.start();

		mToggleIsFullyOpen = isExpanding;

	}
	private ValueAnimator.AnimatorUpdateListener mToggleAnimatorUpdater = new ValueAnimator.AnimatorUpdateListener(){

		@Override
		public void onAnimationUpdate(ValueAnimator p1)
		{
			setQsTranslation((Integer)p1.getAnimatedValue(), true);
		}

	};

	private void setQsTranslation(int height, boolean animating)
	{

		topOverScrolledY = height;

		int h = height;
//		if(!animating)
		h = Math.min(height, mMaxQsHeight);

		lp.height = h;

		ehem.setLayoutParams(lp);

		float f = (float) h / mMaxQsHeight; 
		if (mTranslationListener != null)
			mTranslationListener.onTranslation(f);

	}

	public void setTranslationListener(OnTranslationListener otl)
	{
		mTranslationListener = otl;
	}

	private void updateExpandedPos(int y)
	{
		if (mService != null && false)
		{
			float disph = mService.getDisplayHeight();
	//		mService.updateExpandedViewPos((int)(disph - (y * 1.2f)));
		}
	}
	private void flingCollapse(int y)
	{
		if (debug)Log.d(tag, "mService: " + mService);

		if (mService != null && false)
		{
			mVelocityTracker.computeCurrentVelocity(1000);

			float yVel = mVelocityTracker.getYVelocity();
			boolean negative = yVel < 0;

			float xVel = mVelocityTracker.getXVelocity();
			if (xVel < 0)
			{
				xVel = -xVel;
			}
			if (xVel > 150.0f)
			{
				xVel = 150.0f; // limit how much we care about the x axis
			}

			//	Log.d("Velocity", "velY:"+yVel+" velX:"+xVel);
			float vel = (float)Math.hypot(yVel, xVel);
			if (negative)
			{
				vel = -vel;
			}

//			mService.performFling(y, vel, false);
		}
	}
	private void trackMovement(MotionEvent event)
	{
        if (mVelocityTracker != null)
			mVelocityTracker.addMovement(event);
    }
	private void recycleTracker()
	{
		if (mVelocityTracker != null)
		{
			mVelocityTracker.recycle();
		}
	}
    private void initVelocityTracker()
	{
        if (mVelocityTracker != null)
		{
            mVelocityTracker.clear();
        }
		else mVelocityTracker = VelocityTracker.obtain();
    }

    private float getCurrentVelocity()
	{
        if (mVelocityTracker == null)
		{
            return 0;
        }
        mVelocityTracker.computeCurrentVelocity(1000);
        return mVelocityTracker.getYVelocity();
    }

	// zero checker, ketone ra penting
	// tapi demi perfection xD
	private boolean mStopZeroScroller ;
	int lastY;
	int REFRESHER = 100;
	Runnable mZeroScroller=new Runnable(){
		@Override
		public void run()
		{
			long time = SystemClock.uptimeMillis() - mZeroScrollerTime;
			if (!mStopZeroScroller && (time <= 1000))postDelayed(this, REFRESHER);
			else skrol.setBlockFlinging(false);
		}
	};


	public void setStatusBarService(StatusBarService sbs)
	{
		mService = sbs;
	}
	public interface OnTranslationListener
	{
		public void onTranslation(float fraction);
	}
}
