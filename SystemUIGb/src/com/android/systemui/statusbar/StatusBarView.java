/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.systemui.statusbar;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.*;

import kere.widget.*;
import kere.util.*;
import com.android.systemui.R;
import com.android.systemui.statusbar.*;
import android.os.*;
import support.animator.animation.*;
import java.util.*;
import android.util.*;
import android.content.*;
import support.animator.view.ViewHelper;
import android.provider.*;
import kere.settings.*;
public class StatusBarView extends FrameLayout 
{
    private static final String TAG = "StatusBarView";

    static final int DIM_ANIM_TIME = 400;
    
	
    StatusBarService mService;
    boolean mTracking;
    int mStartX, mStartY;
    ViewGroup mNotificationIcons;
    ViewGroup mStatusIcons;
    View mDate;
	
	View mParentIcons, mClock, mBattery, mRightIcons,mLeftIcons, mTraffic;
	
    FixedSizeDrawable mBackground;
	
	private Handler mHandler;
    public StatusBarView(Context context, AttributeSet attrs) {
        super(context, attrs);
		mHandler = new Handler();
		ColorBarTint.set(context);
		ColorBarTint.addListener(new ColorBarTint.UpdateListener(){
				@Override
				public void onUpdateStatusBarColor(final int prevColor, final int newColor){
					mHandler.post(new Runnable(){

							@Override
							public void run()
							{
								boolean transparent = prevColor == 0x00000000;
								ValueAnimator colorAnim = ObjectAnimator .ofInt(StatusBarView.this ,"backgroundColor" , transparent ? 0xff000000:prevColor , newColor);
								colorAnim.setDuration(180);
								colorAnim.setEvaluator( new ArgbEvaluator());
								colorAnim.start();
								
								// TODO: Implement this method
							}


						});
				}
			});
    }
	
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mNotificationIcons = (ViewGroup)findViewById(R.id.notificationIcons);
        mStatusIcons = (ViewGroup)findViewById(R.id.statusIcons);		
		mDate= new View(getContext());
		mParentIcons = findViewById(R.id.icons);
		//ViewHelper.setAlpha(mParentIcons, 0.9f);
		
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mService.onBarViewAttached();

    }
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mService.updateExpandedViewPos(StatusBarService.EXPANDED_LEAVE_ALONE);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        // put the date date view quantized to the icons
    /*    int oldDateRight = mDate.getRight();
        int newDateRight;

        newDateRight = getDateSize(mNotificationIcons, oldDateRight,
                getViewOffset(mNotificationIcons));
        if (newDateRight < 0) {
            int offset = getViewOffset(mStatusIcons);
            if (oldDateRight < offset) {
                newDateRight = oldDateRight;
            } else {
                newDateRight = getDateSize(mStatusIcons, oldDateRight, offset);
                if (newDateRight < 0) {
                    newDateRight = r;
                }
            }
        }
        int max = r - getPaddingRight();
        if (newDateRight > max) {
            newDateRight = max;
        }

        mDate.layout(mDate.getLeft(), mDate.getTop(), newDateRight, mDate.getBottom());
		*/
      }

    /**
     * Gets the left position of v in this view.  Throws if v is not
     * a child of this.
     */
 
/*   private int getViewOffset(View v) {
        int offset = 0;
        while (v != this) {
            offset += v.getLeft();
            ViewParent p = v.getParent();
            if (v instanceof View) {
                v = (View)p;
            } else {
                throw new RuntimeException(v + " is not a child of " + this);
            }
        }
        return offset;
    }

    private int getDateSize(ViewGroup g, int w, int offset) {
        final int N = g.getChildCount();
        for (int i=0; i<N; i++) {
            View v = g.getChildAt(i);
            int l = v.getLeft() + offset;
            int r = v.getRight() + offset;
            if (w >= l && w <= r) {
                return r;
            }
        }
        return -1;
    }
*/
    /**
     * Ensure that, if there is no target under us to receive the touch,
     * that we process it ourself.  This makes sure that onInterceptTouchEvent()
     * is always called for the entire gesture.
     */
	 
	private long doubleTouchElapsedTime;
	private int doubleTouchFirstX;
	//private boolean doubletaptosleep = true;
	//private boolean sidepanelenable = true;
	private boolean mTouchToHideButtonPanelEnable = true;
    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        if (event.getAction() != MotionEvent.ACTION_DOWN) {
			mService.interceptTouchEvent(event);
        } 
		else if (event.getAction() == MotionEvent.ACTION_DOWN) {
			// double tap actions ------ ##
			final boolean doubletaptosleep = Settings.System.getInt(getContext().getContentResolver(), Setelan.DOUBLE_TAP_TO_SLEEP,0)==1;
			final boolean sidepanelenable = mService.getButtonPanelEnable();
			if(doubletaptosleep || sidepanelenable){
				if(sidepanelenable && mTouchToHideButtonPanelEnable)mService.showButtonPanel(false);
				
				final long now=SystemClock.uptimeMillis();
				final long doubleTouchTime = now-doubleTouchElapsedTime;
				final boolean doubleTouch = doubleTouchTime <300;
				if(doubleTouch){
					final int rightside = getWidth()*3/4;
					final boolean right =  doubleTouchFirstX > rightside && event.getX() > rightside;
					if(right&&sidepanelenable){
						// show button panel
						mService.showButtonPanel(true);
					}
					else if((!right)&&doubletaptosleep){
						// sleep
						final PowerManager pm = (PowerManager)getContext().getSystemService(Context.POWER_SERVICE);
						pm.goToSleep(SystemClock.uptimeMillis() + 1);
					}
				}
				doubleTouchElapsedTime = SystemClock.uptimeMillis();
				doubleTouchFirstX = (int)event.getX();
			}
			//------------ ##
        }
        return true;
    }
	
    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return mService.interceptTouchEvent(event)
                ? true : super.onInterceptTouchEvent(event);
    }
}

