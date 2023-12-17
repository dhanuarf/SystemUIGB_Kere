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
 
import com.android.internal.statusbar.StatusBarIcon;
import com.android.internal.statusbar.StatusBarNotification;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RemoteViews;
import android.widget.ScrollView;
import android.widget.TextView;

import android.graphics.drawable.GradientDrawable;
import com.android.systemui.R;
import java.util.*;
import support.animator.animation.*;
import support.animator.view.*;
import android.view.animation.*;
import android.content.*;

public class IntruderView extends LinearLayout {
    StatusBarService mService;
    ItemTouchDispatcher mTouchDispatcher;
	
	private static final boolean DEBUG = false;
	private static final String LOG_HEADSUP = "headsup";
	
    private ScrollView mIntruderScrollView;
    private TextView mIntruderLatestTitle;
    private LinearLayout mIntruderLatestItems;
    private LinearLayout mIntruderNotificationLinearLayout;
    private NotificationData mNotificationData = new NotificationData();
    private Context mContext;
    private Button mClearButton;
	private Button mDismissButton;
	private TextView mTickerTextView;
	private LinearLayout mparent;
	// background
	private GradientDrawable mGradientDrawable;
	
    public IntruderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
		mparent = (LinearLayout)findViewById(R.id.intruder_parent);
        mIntruderScrollView = (ScrollView) findViewById(R.id.intruderscroll);
        mIntruderNotificationLinearLayout = (LinearLayout) findViewById(R.id.intrudernotificationLinearLayout);
        mIntruderLatestItems = (LinearLayout) findViewById(R.id.intruderlatestItems);
        mClearButton = (Button) findViewById(R.id.intruder_clear_all_button);
		mDismissButton=(Button)findViewById(R.id.intruder_alert_dismiss);
		mTickerTextView = (TextView)findViewById(R.id.intruder_alert_tickertext);
		
		mTickerTextView.setMaxLines(2);
		mTickerTextView.setVisibility(GONE);
        mClearButton.setOnClickListener(clicker);
		mDismissButton.setOnClickListener(clicker);
		
		// set bg
		mGradientDrawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
								  new int[] { 
									  0x99000000,0x99000000,0x99000000,0x88000000,0x88000000, 0x77000000,0x77000000,0x66000000,0x66000000,0x55000000,
									  0x33000000,0x11000000,0x00000000});
		mGradientDrawable.setShape(GradientDrawable.RECTANGLE);
		mparent.setBackgroundDrawable(mGradientDrawable);
		setVisibility(GONE);
    }

    private View.OnClickListener clicker = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
			if(v.getId()==mClearButton.getId()){
				mService.clearAllNotifications();
				mService.animateCollapse();
			}
			else if(v.getId()==mDismissButton.getId()){
				mService.hideIntroducerView();
			}
        }
    };

    public void updateLayout() {
        mIntruderNotificationLinearLayout.removeAllViews();
        mIntruderNotificationLinearLayout.addView(mIntruderLatestItems);
    }

    /** We want to shrink down to 0, and ignore the background. */
    @Override
    public int getSuggestedMinimumHeight() {
        return 0;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (mTouchDispatcher.needsInterceptTouch(event)) {
            return true;
        }
        return super.onInterceptTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean handled = mTouchDispatcher.handleTouchEvent(event);
        
        if (super.onTouchEvent(event)) {
            handled = true;
        }
	
        return handled;
    }

    public void setAreThereNotifications() {
        boolean hasItems = mNotificationData.hasVisibleItems();
		boolean hasClearableItems= mNotificationData.hasClearableItems();
        if (hasClearableItems) {
            mClearButton.setVisibility(View.VISIBLE);
        } else {
            mClearButton.setVisibility(View.GONE);
        }
		if(!hasItems)mService.hideIntroducerView();
    }

    public void updateNotification(IBinder key, StatusBarNotification notification) {
		if(DEBUG)Log.d(LOG_HEADSUP, "package: "+notification.pkg);
		
		if(!("com.android.mms".equals(notification.pkg)))
			return;
			
		if( ("com.android.mms".equals(notification.pkg)) ){
			if(notification.notification.tickerText !=null){
				if(notification.notification.tickerText.toString().contains("Delivery"))return;
			}
		}
        NotificationData oldList;
        int oldIndex = mNotificationData.findEntry(key);
        if (oldIndex < 0) {
            return;
        } else {
            oldList = mNotificationData;
        }
        final NotificationData.Entry oldEntry = oldList.getEntryAt(oldIndex);
        final StatusBarNotification oldNotification = oldEntry.notification;
        final RemoteViews oldContentView = oldNotification.notification.contentView;
        final RemoteViews contentView = notification.notification.contentView;

        // Can we just reapply the RemoteViews in place?  If when didn't change, the order
        // didn't change.
        if (notification.notification.when == oldNotification.notification.when
                && notification.isOngoing() == oldNotification.isOngoing()
                && oldEntry.expanded != null
                && contentView != null && oldContentView != null
                && contentView.getPackage() != null
                && oldContentView.getPackage() != null
                && oldContentView.getPackage().equals(contentView.getPackage())
                && oldContentView.getLayoutId() == contentView.getLayoutId()) {
            oldEntry.notification = notification;
            try {
                // Reapply the RemoteViews
                contentView.reapply(mContext, oldEntry.content);
                // update the contentIntent
                final PendingIntent contentIntent = notification.notification.contentIntent;
                if (contentIntent != null) {
                    oldEntry.content.setOnClickListener(mService.makeLauncher(contentIntent, notification.pkg, notification.tag, notification.id));
                }
                // Update the icon.
                final StatusBarIcon ic = new StatusBarIcon(notification.pkg,
                        notification.notification.icon, notification.notification.iconLevel,
                        notification.notification.number);
                if (!oldEntry.icon.set(ic)) {
                    return;
                }
            }
            catch (RuntimeException e) {
                removeNotificationViews(key);
                addNotificationViews(key, notification);
            }
        } else {
            removeNotificationViews(key);
            addNotificationViews(key, notification);
        }
        setAreThereNotifications();
    }
	private boolean mIsShown = false;
	public void show()
	{
		if (isShown())return;

		setVisibility(VISIBLE);
		//ViewHelper.setAlpha(mparent, 0);
		ViewHelper.setTranslationY(mparent,-getHeight());
		mIsShown = true;
		
		final ValueAnimator bganim = ValueAnimator .ofFloat(0, 1f);
		bganim.setDuration(250);
		bganim.setInterpolator(new DecelerateInterpolator());
		bganim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(){

				@Override
				public void onAnimationUpdate(ValueAnimator p1)
				{
					//ViewHelper.setAlpha(mparent, (float)p1.getAnimatedValue());
					ViewHelper.setTranslationY(mparent, (1f-p1.getAnimatedFraction()) * -getHeight());
				}


			});
		bganim.start();
	}
	
	public void hide()
	{
		if (!mIsShown)return;

		final ValueAnimator bganim = ValueAnimator .ofFloat(0, 1f);
		bganim.setDuration(250);
		bganim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(){

				@Override
				public void onAnimationUpdate(ValueAnimator p1)
				{
					ViewHelper.setTranslationY(mparent, p1.getAnimatedFraction() * -getHeight());
					ViewHelper.setAlpha(mparent, 1f - (float)p1.getAnimatedValue());
				}


			});
		bganim.addListener(new Animator.AnimatorListener(){

				@Override
				public void onAnimationCancel(Animator p1)
				{
					// TODO: Implement this method
				}

				@Override
				public void onAnimationEnd(Animator p1)
				{
					setVisibility(GONE);
				}

				@Override
				public void onAnimationRepeat(Animator p1)
				{
					// TODO: Implement this method
				}

				@Override
				public void onAnimationStart(Animator p1)
				{
					// TODO: Implement this method
				}


			});
		bganim.start();
		mIsShown = false;

	}
    private View[] makeNotificationView(final IBinder key, final StatusBarNotification notification, ViewGroup parent) {
        Notification n = notification.notification;
        RemoteViews remoteViews = n.contentView;
        if (remoteViews == null) {
            return null;
        }

        // create the row view
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LatestItemContainer row = (LatestItemContainer) inflater.inflate(R.layout.status_bar_latest_event, parent, false);
        if ((n.flags & Notification.FLAG_ONGOING_EVENT) == 0 && (n.flags & Notification.FLAG_NO_CLEAR) == 0) {
            row.setOnSwipeCallback(mTouchDispatcher, new Runnable() {
                @Override
                public void run() {
                    try {
                        mService.getServiceBar().onNotificationClear(notification.pkg, notification.tag, notification.id);
                       // mService.setClearLauncherNotif(notification.pkg);
                        NotificationData list = mNotificationData;
                        int index = mNotificationData.findEntry(key);
                        if (index < 0) {
                            list = mNotificationData;
                            index = mNotificationData.findEntry(key);
                        }
                        if (index >= 0) {
                            list.getEntryAt(index).cancelled = true;
                        }
                    } catch (RemoteException e) {
                        // Skip it, don't crash.
                    }
                }
            });
        }

        // bind the click event to the content area
        ViewGroup content = (ViewGroup) row.findViewById(R.id.content);
        content.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        content.setOnFocusChangeListener(mFocusChangeListener);
        PendingIntent contentIntent = n.contentIntent;
        if (contentIntent != null) {
            content.setOnClickListener(mService.makeLauncher(contentIntent, notification.pkg,
                        notification.tag, notification.id));
        }

        View expanded = null;
        Exception exception = null;
        try {
            expanded = remoteViews.apply(mContext, content);
        }
        catch (RuntimeException e) {
            exception = e;
        }
        if (expanded == null) {
            String ident = notification.pkg + "/0x" + Integer.toHexString(notification.id);
            return null;
        } else {
         //   mService.resetTextViewColors(expanded);
            content.addView(expanded);
            row.setDrawingCacheEnabled(true);
        }

        return new View[] { row, content, expanded };
    }
	
    public StatusBarIconView addNotificationViews(IBinder key, StatusBarNotification notification) {
		if(!("com.android.mms".equals(notification.pkg)))
			return null;
			
		if( ("com.android.mms".equals(notification.pkg)) ){
			if(notification.notification.tickerText !=null){
				if(notification.notification.tickerText.toString().contains("Delivery"))return null;
			}
		}
        ViewGroup parent;
	
		//getContext().startActivity(pi);
		//log("intentTargetPackage:"+notification.notification.contentIntent.get);
        final boolean isOngoing = notification.isOngoing();
        if (!isOngoing) {
            parent = mIntruderLatestItems;
        } else {
            return null;
        }
        // Construct the expanded view.
        final View[] views = makeNotificationView(key, notification, parent);
        if (views == null) {
            return null;
        }
        final View row = views[0];
        final View content = views[1];
        final View expanded = views[2];
        // Construct the icon.
        final StatusBarIconView iconView = new StatusBarIconView(mContext,
                notification.pkg + "/0x" + Integer.toHexString(notification.id));
        final StatusBarIcon ic = new StatusBarIcon(notification.pkg, notification.notification.icon,
                    notification.notification.iconLevel, notification.notification.number);
        if (!iconView.set(ic)) {
            return null; 
        }
		if(iconView!=null)iconView.setColorFilter(0xffffffff);
        // Add the expanded view.
        final int viewIndex = mNotificationData.add(key, notification, row, content, expanded, iconView);
		
        parent.addView(row, viewIndex);
        return iconView;
    }
	public void log(String text){
		
		Log.d(IntruderView.class.getSimpleName(), text);
	}
    public StatusBarNotification removeNotificationViews(IBinder key) {
        NotificationData.Entry entry = mNotificationData.remove(key);
        if (entry == null) {
            entry = mNotificationData.remove(key);
            if (entry == null) {
                return null; 
            }
        }
        // Remove the expanded view.
        ((ViewGroup)entry.row.getParent()).removeView(entry.row);
        // Remove the icon.
        if ((entry.icon != null) && (((ViewGroup)entry.icon.getParent()) != null)) {
           ((ViewGroup)entry.icon.getParent()).removeView(entry.icon);
        }

        if (entry.cancelled) {
            if (!mNotificationData.hasClearableItems()) {
                mService.setIntruderAlertVisibility(false);
            }
        }
        return entry.notification;
    }

    private View.OnFocusChangeListener mFocusChangeListener = new View.OnFocusChangeListener() {
        public void onFocusChange(View v, boolean hasFocus) {
            // Because 'v' is a ViewGroup, all its children will be (un)selected
            // too, which allows marqueeing to work.
            v.setSelected(hasFocus);
        }
    };
}
