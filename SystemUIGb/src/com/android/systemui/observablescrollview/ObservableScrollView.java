/*
 * Copyright 2014 Soichiro Kashima
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.systemui.observablescrollview;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.util.*;

/**
 * ScrollView that its scroll position can be observed.
 */
public class ObservableScrollView extends ScrollView implements Scrollable
{

    // Fields that should be saved onSaveInstanceState
    private int mPrevScrollY;
    private int mScrollY;
	private int mOverScrollY = 0;
	private boolean mBlockFlinging = false;

    // Fields that don't need to be saved onSaveInstanceState
    private ObservableScrollViewCallbacks mCallbacks;
    private ScrollState mScrollState;
    private boolean mFirstScroll;
    private boolean mDragging;
    private boolean mIntercepted;
    private MotionEvent mPrevMoveEvent;
    private ViewGroup mTouchInterceptionViewGroup;
	private boolean firstOver;
	private boolean topOverscrolled;
	private boolean mToggleIsOpen;
	private int firstTouchedY;
	
	/**
     * ID of the active pointer. This is used to retain consistency during
     * drags/flings if multiple pointers are used.
     */
    private int mActivePointerId = -1;

    public ObservableScrollView(Context context)
	{
        super(context);
    }

    public ObservableScrollView(Context context, AttributeSet attrs)
	{
        super(context, attrs);
    }

    public ObservableScrollView(Context context, AttributeSet attrs, int defStyle)
	{
        super(context, attrs, defStyle);
    }
	
    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt)
	{
        super.onScrollChanged(l, t, oldl, oldt);
        if (mCallbacks != null)
		{
			mPrevScrollY = oldt;
            mScrollY = t;

			topOverscrolled =  t <= 0;

			if (!topOverscrolled)firstOver = false;

            mCallbacks.onScrollChanged(t, mFirstScroll, mDragging, mScrollState);
            if (mFirstScroll)
			{
                mFirstScroll = false;
            }

            if (mPrevScrollY < t)
			{
                mScrollState = ScrollState.UP;
            }
			else if (t + 1 < mPrevScrollY)
			{
                mScrollState = ScrollState.DOWN;
                //} else {
                // Keep previous state while dragging.
                // Never makes it STOP even if scrollY not changed.
                // Before Android 4.4, onTouchEvent calls onScrollChanged directly for ACTION_MOVE,
                // which makes mScrollState always STOP when onUpOrCancelMotionEvent is called.
                // STOP state is now meaningless for ScrollView.
            }

        }
    }
	int scrollYOnDown =0;
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev)
	{
        if (mCallbacks != null)
		{
            switch (ev.getActionMasked())
			{
                case MotionEvent.ACTION_DOWN:
                    // Whether or not motion events are consumed by children,
                    // flag initializations which are related to ACTION_DOWN events should be executed.
                    // Because if the ACTION_DOWN is consumed by children and only ACTION_MOVEs are
                    // passed to parent (this view), the flags will be invalid.
                    // Also, applications might implement initialization codes to onDownMotionEvent,
                    // so call it here.
                    mFirstScroll = mDragging = true;

					scrollYOnDown = getScrollY();
					mActivePointerId = ev.getPointerId(0);
                    break;
					case MotionEvent.ACTION_UP:
					case MotionEvent.ACTION_CANCEL:
					
						mActivePointerId = -1;
						
						break;
            }
        }
        return super.onInterceptTouchEvent(ev);
    }
	int deltaTopY=0;
	int lastTopY=0;
	int initialHeight=0;

    @Override
    public boolean onTouchEvent(MotionEvent ev)
	{
		
        if (mCallbacks != null)
		{
			mCallbacks.onTouchScroll(ev);
            switch (ev.getActionMasked())
			{
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    mIntercepted = false;
                    mDragging = false;
					mActivePointerId = -1;
                    break;
                case MotionEvent.ACTION_MOVE:
					if (mCallbacks != null)
					{
//						deltaTopY = currY - firstTouchedY;
//
//						/* mbok butuh kie
//						 *if(!firstOver){
//						 firstOver=true;
//						 //ev.setAction(MotionEvent.ACTION_DOWN);
//						 }*/
//						if (topOverscrolled)
//						{
//							if (!mToggleIsOpen)
//							{
//								mCallbacks.onTopOverscrolled(deltaTopY);
//							}
//						}
					}
					//	mCallbacks.onMoveMotionEvent(ev);
                    if (mPrevMoveEvent == null)
					{
                        mPrevMoveEvent = ev;
                    }
                    float diffY = ev.getY() - mPrevMoveEvent.getY();
                    mPrevMoveEvent = MotionEvent.obtainNoHistory(ev);

                    if (getCurrentScrollY() - diffY <= 0)
					{
                        // Can't scroll anymore.

                        if (mIntercepted)
						{
                            // Already dispatched ACTION_DOWN event to parents, so stop here.
                            return false;
                        }

                        // Apps can set the interception target other than the direct parent.
                        final ViewGroup parent;
                        if (mTouchInterceptionViewGroup == null)
						{
                            parent = (ViewGroup) getParent();
                        }
						else
						{
                            parent = mTouchInterceptionViewGroup;
                        }

                        // Get offset to parents. If the parent is not the direct parent,
                        // we should aggregate offsets from all of the parents.
                        float offsetX = 0;
                        float offsetY = 0;
                        for (View v = this; v != null && v != parent; v = (View) v.getParent())
						{
                            offsetX += v.getLeft() - v.getScrollX();
                            offsetY += v.getTop() - v.getScrollY();
                        }
                        final MotionEvent event = MotionEvent.obtainNoHistory(ev);
                        event.offsetLocation(offsetX, offsetY);

                        if (parent.onInterceptTouchEvent(event))
						{
                            mIntercepted = true;

                            // If the parent wants to intercept ACTION_MOVE events,
                            // we pass ACTION_DOWN event to the parent
                            // as if these touch events just have began now.
                            event.setAction(MotionEvent.ACTION_DOWN);

                            // Return this onTouchEvent() first and set ACTION_DOWN event for parent
                            // to the queue, to keep events sequence.
                            post(new Runnable() {
									@Override
									public void run()
									{
										parent.dispatchTouchEvent(event);
									}
								});
                            return false;
                        }
                        // Even when this can't be scrolled anymore,
                        // simply returning false here may cause subView's click,
                        // so delegate it to super.
                        return super.onTouchEvent(ev);
                    }
                    break;
				case MotionEvent.ACTION_DOWN:
					mActivePointerId = ev.getPointerId(0);
					
					break;
					
            }
        }
        return super.onTouchEvent(ev);
    }
	public boolean getBlockFlinging(){
		return mBlockFlinging;
	}
	public void setBlockFlinging(boolean blockFling){
		mBlockFlinging=blockFling;
	}

	@Override
	public void fling(int p1)
	{
		if(!mBlockFlinging)
			super.fling(p1);
		// TODO: Implement this method
	}
	private void startTopOverscrolling(int i)
	{

	}
	public boolean canScroll()
	{
        View child = getChildAt(0);
        if (child != null)
		{
            int childHeight = child.getHeight();
            return getHeight() < childHeight + mPaddingTop + mPaddingBottom;
        }
        return false;
    }
    @Override
    public void setScrollViewCallbacks(ObservableScrollViewCallbacks listener)
	{
        mCallbacks = listener;
    }

    @Override
    public void setTouchInterceptionViewGroup(ViewGroup viewGroup)
	{
        mTouchInterceptionViewGroup = viewGroup;
    }

    @Override
    public void scrollVerticallyTo(int y)
	{
        scrollTo(0, y);
    }

    @Override
    public int getCurrentScrollY()
	{
        return mScrollY;
    }

	@Override
	protected void onOverScrolled(int p1, int p2, boolean p3, boolean p4)
	{
		// TODO: Implement this method
		super.onOverScrolled(p1, p2, p3, p4);
		//Log.d("onOverScrolled", p1+" "+p2);
		mOverScrollY = p2;
		//topOverscrolled = p2<0;

	}
}
