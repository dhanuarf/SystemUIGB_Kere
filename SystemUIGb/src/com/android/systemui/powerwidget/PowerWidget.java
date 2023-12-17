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
package com.android.systemui.powerwidget;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ScrollView;
import android.widget.LinearLayout;
import android.view.ViewGroup;

import com.android.systemui.R;

import support.v4.app.*;
import support.v4.view.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.view.*;
import android.widget.*;
import android.graphics.drawable.*;
import support.animator.animation.*;
import support.animator.view.*;
import android.graphics.*;
import kere.util.*;

public class PowerWidget extends LinearLayout
{
	boolean debug = false;
	String tag = "PowerWidget";
	
	private PowerWidgetContainer[] mButtonPage;
	private WrapContentHeightViewPager mButtonPager;
	private int mButtonPageNum;
	private TextView mPageIndicatorText;
	private Drawable mBgPageIndicator;
	private ObjectAnimator mPageIndicatorAnimator;
	
	private ScrollView mScrollView ;
    private static final String TAG = "PowerWidget";
    public static final String BUTTON_DELIMITER = "|";
    private static final String BUTTONS_DEFAULT = PowerButton.BUTTON_WIFI
	+ BUTTON_DELIMITER + PowerButton.BUTTON_BLUETOOTH
	+ BUTTON_DELIMITER + PowerButton.BUTTON_GPS
	+ BUTTON_DELIMITER + PowerButton.BUTTON_SOUND
	+ BUTTON_DELIMITER + PowerButton.BUTTON_MOBILEDATA
	+ BUTTON_DELIMITER + PowerButton.BUTTON_AUTOROTATE;

	
    protected static final ViewGroup.LayoutParams PARENT_LAYOUT_PARAMS =
	new FrameLayout.LayoutParams(
		ViewGroup.LayoutParams.MATCH_PARENT, // width = match_parent
		ViewGroup.LayoutParams.WRAP_CONTENT  // height = wrap_content
	);
	protected static final ViewGroup.LayoutParams INDICATOR_LAYOUT_PARAMS =
	new LinearLayout.LayoutParams(
		18 ,
		18
	);

	private ViewGroup.LayoutParams TILE_LAYOUT_PARAMS;	
    private static final int LAYOUT_SCROLL_BUTTON_THRESHOLD = 6;
    // this is a list of all possible buttons and their corresponding classes
    protected static final HashMap<String, Class<? extends PowerButton>> sPossibleButtons =
	new HashMap<String, Class<? extends PowerButton>>();
    static {
        sPossibleButtons.put(PowerButton.BUTTON_WIFI, WifiButton.class);
        sPossibleButtons.put(PowerButton.BUTTON_GPS, GPSButton.class);
        sPossibleButtons.put(PowerButton.BUTTON_BLUETOOTH, BluetoothButton.class);
        sPossibleButtons.put(PowerButton.BUTTON_BRIGHTNESS, BrightnessButton.class);
        sPossibleButtons.put(PowerButton.BUTTON_SOUND, SoundButton.class);
        sPossibleButtons.put(PowerButton.BUTTON_SYNC, SyncButton.class);
        sPossibleButtons.put(PowerButton.BUTTON_WIFIAP, WifiApButton.class);
        sPossibleButtons.put(PowerButton.BUTTON_SCREENTIMEOUT, ScreenTimeoutButton.class);
        sPossibleButtons.put(PowerButton.BUTTON_MOBILEDATA, MobileDataButton.class);
        sPossibleButtons.put(PowerButton.BUTTON_LOCKSCREEN, LockScreenButton.class);
        sPossibleButtons.put(PowerButton.BUTTON_AUTOROTATE, AutoRotateButton.class);
        sPossibleButtons.put(PowerButton.BUTTON_AIRPLANE, AirplaneButton.class);
        sPossibleButtons.put(PowerButton.BUTTON_FLASHLIGHT, FlashlightButton.class);
        sPossibleButtons.put(PowerButton.BUTTON_SLEEP, SleepButton.class);
		sPossibleButtons.put(PowerButton.BUTTON_NIGHTMODE, NightModeButton.class);
        sPossibleButtons.put(PowerButton.BUTTON_DSB, DSBButton.class);
//		sPossibleButtons.put(PowerButton.BUTTON_SETTINGS, SettingsButton.class);
		

    }
    // this is a list of our currently loaded buttons
    protected final HashMap<String, PowerButton> mButtons = new HashMap<String, PowerButton>();
    protected final ArrayList<String> mButtonNames = new ArrayList<String>();
    private View.OnClickListener mAllButtonClickListener;
    private View.OnLongClickListener mAllButtonLongClickListener;
    protected Context mContext;
    protected Handler mHandler;
    protected LayoutInflater mInflater;
    private WidgetBroadcastReceiver mBroadcastReceiver = null;
    private WidgetSettingsObserver mObserver = null;
	private ButtonSettingsObserver mButtonObserver = null;
    private LinearLayout mButtonLayout;
    public PowerWidget(Context context, AttributeSet attrs)
	{
        super(context, attrs);
        mContext = context;
        mHandler = new Handler();
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		// some not customizable stuff
		mPageIndicatorText = new TextView(getContext());
		mPageIndicatorText.setTextSize(15);
		mPageIndicatorText.setGravity(Gravity.CENTER);
		mBgPageIndicator = getContext().getResources().getDrawable(R.drawable.bg_page_indicator);
		mPageIndicatorAnimator = ObjectAnimator.ofFloat(mPageIndicatorText, "alpha", 1f, 0f);
		mPageIndicatorAnimator.setDuration(200);
		mPageIndicatorAnimator.setStartDelay(1000);
		
		// setup everything
        setupWidget();
		updateAllButtons();
//        updateVisibility();
		
    }
    public void destroyWidget()
	{
        if(debug) Log.i(TAG, "Clearing any old widget stuffs");
        // remove all views from the layout
        removeAllViews();
        // unregister our content receiver
        if (mBroadcastReceiver != null)
		{
            mContext.unregisterReceiver(mBroadcastReceiver);
            mBroadcastReceiver = null;
        }
        // unobserve our content
        if (mObserver != null && mButtonObserver != null)
		{
            mObserver.unobserve();
			mButtonObserver.unobserve();
            mObserver = null;
			mButtonObserver = null;
        }
        // clear the button instances
        unloadAllButtons();
    }
    public void setupWidget()
	{
        destroyWidget();
        if(debug)Log.i(TAG, "Setting up widget");
        String buttons = Settings.System.getString(mContext.getContentResolver(), Nusettings.WIDGET_BUTTONS);
        if (buttons == null)
		{
            Log.i(TAG, "Default buttons being loaded");
            buttons = BUTTONS_DEFAULT;
            // Add the WiMAX button if it's supported

        }
        if(debug)Log.i(TAG, "Button list: " + buttons);
        for (String button : buttons.split("\\|"))
		{
            if (loadButton(button))
			{
                mButtonNames.add(button);
            }
			else
			{
                Log.e(TAG, "Error setting up button: " + button);
            }
        }
        recreateButtonLayout();
        // set up a broadcast receiver for our intents, based off of what our power buttons have been loaded
        setupBroadcastReceiver();
        IntentFilter filter = getMergedBroadcastIntentFilter();
        // we add this so we can update views and such if the settings for our widget change
        //filter.addAction(Settings.SETTINGS_CHANGED);
        // we need to detect orientation changes and update the static button width value appropriately
        //filter.addAction(Intent.ACTION_CONFIGURATION_CHANGED);
        // register the receiver
        mContext.registerReceiver(mBroadcastReceiver, filter);
        // register our observer
        mObserver = new WidgetSettingsObserver(mHandler);
		mButtonObserver = new ButtonSettingsObserver(mHandler);
        mObserver.observe();
		mButtonObserver.observe();
    }
    private boolean loadButton(String key)
	{
        // first make sure we have a valid button
        if (!sPossibleButtons.containsKey(key))
		{
            return false;
        }
        if (mButtons.containsKey(key))
		{
            return true;
        }
        try
		{
            // we need to instantiate a new button and add it
            PowerButton pb = sPossibleButtons.get(key).newInstance();
			//		pb.anuin(getContext());
            pb.setExternalClickListener(mAllButtonClickListener);
            pb.setExternalLongClickListener(mAllButtonLongClickListener);
            // save it
            mButtons.put(key, pb);
        }
		catch (Exception e)
		{
            Log.e(TAG, "Error loading button: " + key, e);
            return false;
        }
        return true;
    }

    private void unloadAllButtons()
	{
        // cycle through setting the buttons to null
        for (PowerButton pb : mButtons.values())
		{
            pb.setupButton(null);
        }
        // clear our list
        mButtons.clear();
        mButtonNames.clear();
    }
    protected void recreateButtonLayout()
	{
        removeAllViews();
		
		int h = getContext().getResources().getDimensionPixelSize(R.dimen.pw_tile_height);
		TILE_LAYOUT_PARAMS = new ViewGroup.LayoutParams(
			ViewGroup.LayoutParams.WRAP_CONTENT, 
			h);
		
		// create layout to hold the buttons with quicksettings style

		int buttonNum = mButtons.size();
		int pageNum = (int)Math.ceil((double)buttonNum/9);
		if(debug)Log.d(tag, "pageNum: "+ pageNum + " buttonNum: "+ buttonNum);
		mButtonPageNum = pageNum;
		
		mButtonPage = new PowerWidgetContainer[mButtonPageNum];
		for (int i=0; i < mButtonPage.length; i++)
		{
			mButtonPage[i] = new PowerWidgetContainer(getContext());
		}		
		
		int index = 1;
		int pageIndex = 0;
        for (String button : mButtonNames)
		{
            PowerButton pb = mButtons.get(button);
            if (pb != null)
			{
                PowerButtonView buttonView = (PowerButtonView)mInflater.inflate(R.layout.qs_item, null, false);
                pb.setupButton(buttonView);
				
				if(index > 9){
					index = 1;
					pageIndex ++;
				}
				
				mButtonPage[pageIndex].addView(buttonView, TILE_LAYOUT_PARAMS);
				
				index++;

            }
        }
		mButtonPager = new WrapContentHeightViewPager(getContext());
		mButtonPager.setAdapter(new ButtonPageAdapter());
		mButtonPager.setOnPageChangeListener(mPagerListener);
		
		
		addView(mButtonPager, PARENT_LAYOUT_PARAMS);
		addView(mPageIndicatorText, INDICATOR_LAYOUT_PARAMS);
		updateAllButtons();
	    }
    public void updateAllButtons()
	{
        // cycle through our buttons and update them
        for (PowerButton pb : mButtons.values())
		{
            pb.update(mContext);
        }
    }
	public void setTintColor(int color)
	{
        // color them
        for (PowerButton pb : mButtons.values())
		{
            pb.setTintColor(color);
			
        }
		if ( mPageIndicatorText != null && mBgPageIndicator != null){
			mBgPageIndicator.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
			mPageIndicatorText.setTextColor(ColorUtils.isBrightColor(color)? 0xff222222 : 0xffffffff);
			mPageIndicatorText.setBackgroundDrawable(mBgPageIndicator);
		}
		
    }
	// fade out page indicator
	private void showPageIndicator()
	{
		if(mPageIndicatorText !=null){
			mPageIndicatorAnimator.cancel();
			ViewHelper.setAlpha(mPageIndicatorText, 1f);
			mPageIndicatorAnimator.start();
		}
        

    }
    private IntentFilter getMergedBroadcastIntentFilter()
	{
        IntentFilter filter = new IntentFilter();
        for (PowerButton button : mButtons.values())
		{
            IntentFilter tmp = button.getBroadcastIntentFilter();
            // cycle through these actions, and see if we need them
            int num = tmp.countActions();
            for (int i = 0; i < num; i++)
			{
                String action = tmp.getAction(i);
                if (!filter.hasAction(action))
				{
                    filter.addAction(action);
                }
            }
        }
        // return our merged filter
        return filter;
    }
    private List<Uri> getAllObservedUris()
	{
        List<Uri> uris = new ArrayList<Uri>();
        for (PowerButton button : mButtons.values())
		{
            List<Uri> tmp = button.getObservedUris();
            for (Uri uri : tmp)
			{
                if (!uris.contains(uri))
				{
                    uris.add(uri);
                }
            }
        }
        return uris;

    }
	
    public void setGlobalButtonOnClickListener(View.OnClickListener listener)
	{
        mAllButtonClickListener = listener;
        for (PowerButton pb : mButtons.values())
		{
            pb.setExternalClickListener(listener);
        }
    }
    public void setGlobalButtonOnLongClickListener(View.OnLongClickListener listener)
	{
        mAllButtonLongClickListener = listener;
        for (PowerButton pb : mButtons.values())
		{
            pb.setExternalLongClickListener(listener);
        }
    }
    private void setupBroadcastReceiver()
	{
        if (mBroadcastReceiver == null)
		{
            mBroadcastReceiver = new WidgetBroadcastReceiver();
        }
    }
	public void setScrollView(ScrollView sv){
		mScrollView = sv;
	}
	
	private ViewPager.OnPageChangeListener mPagerListener= new ViewPager.OnPageChangeListener(){

		@Override
		public void onPageScrollStateChanged(int p1)
		{
			// TODO: Implement this method
		}

		@Override
		public void onPageScrolled(int p1, float p2, int p3)
		{
			// TODO: Implement this method
		}

		@Override
		public void onPageSelected(int p1)
		{
			mPageIndicatorText.setText(Integer.toString(p1+1));
			//mPageIndicatorText.setTextColor(0xffff0000);
			showPageIndicator();
			
			//Log.d("pageselected", "pagetext: "+ mPageIndicatorText.getText());
			
			// TODO: Implement this method
		}
		
		
	};
    // our own broadcast receiver :D
    private class WidgetBroadcastReceiver extends BroadcastReceiver
	{
        public void onReceive(Context context, Intent intent)
		{
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_CONFIGURATION_CHANGED))
			{
                recreateButtonLayout();
				
				
            }
			else
			{
                // handle the intent through our power buttons
                for (PowerButton button : mButtons.values())
				{
                    // call "onReceive" on those that matter
                    if (button.getBroadcastIntentFilter().hasAction(action))
					{
                        button.onReceive(context, intent);
                    }
                }
            }
            // update our widget
            updateAllButtons();
        }
    };
	
	
    // our own settings observer :D
    private class WidgetSettingsObserver extends ContentObserver
	{
        public WidgetSettingsObserver(Handler handler)
		{
            super(handler);
        }
        public void observe()
		{
            ContentResolver resolver = mContext.getContentResolver();
		
            resolver.registerContentObserver(
			Settings.System.getUriFor(Nusettings.WIDGET_BUTTONS), false, this);

        }
        public void unobserve()
		{
            ContentResolver resolver = mContext.getContentResolver();
            resolver.unregisterContentObserver(this);
        }

		@Override
		public void onChange(boolean p1)
		{
			// TODO: Implement this method
			super.onChange(p1);
			
			// update everything
			setupWidget();
		}
    }
	  private class ButtonSettingsObserver extends ContentObserver
	{
        public ButtonSettingsObserver(Handler handler)
		{
            super(handler);
        }
        public void observe()
		{
            ContentResolver resolver = mContext.getContentResolver();
			//   watch for changes in buttons
		
            for (Uri uri : getAllObservedUris())
			{
                resolver.registerContentObserver(uri, false, this);
            }
        }
        public void unobserve()
		{
            ContentResolver resolver = mContext.getContentResolver();
            resolver.unregisterContentObserver(this);
        }

		@Override
		public void onChange(boolean p1)
		{
			// TODO: Implement this method
			super.onChange(p1);
			
			ContentResolver resolver = mContext.getContentResolver();

			for (PowerButton button : mButtons.values())
			{
				button.onChangeUri(resolver, null);
            }
			updateAllButtons();
						
		}
	}
     
	private class ButtonPageAdapter extends PagerAdapter
	{
		@Override
		public boolean isViewFromObject(View p1, Object p2)
		{
			// TODO: Implement this method
			return p1 == p2;
		}

		@Override
		public Object instantiateItem(ViewGroup p1, int p2)
		{
			((ViewPager)p1).addView(mButtonPage[p2], PARENT_LAYOUT_PARAMS);

			// TODO: Implement this method
			return mButtonPage[p2];
		}

		@Override
		public int getCount()
		{
			// TODO: Implement this method
			return mButtonPageNum;
		}

		@Override
		public void destroyItem(ViewGroup p1, int p2, Object p3)
		{
			// TODO: Implement this method
			super.destroyItem(p1, p2, p3);

			((ViewPager)p1).removeView((View)p3);

		}
		


	}
	
	private class WrapContentHeightViewPager extends ViewPager
	{

		public WrapContentHeightViewPager(Context context)
		{
			super(context);
		}

		public WrapContentHeightViewPager(Context context, AttributeSet attrs)
		{
			super(context, attrs);
		}

		@Override
		protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
		{
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);

			int height = 0;

			for (int i = 0; i < getChildCount(); i++)
			{
				View child = getChildAt(i);

				child.measure(widthMeasureSpec, View.MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));

				int h = child.getMeasuredHeight();

				if (h > height) height = h;
			}

			heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);

			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		}

		
		public boolean onTouch(View v, MotionEvent event)
		{
			int dragthreshold = 30;

			int downX = 0;

			int downY = 0;
			if(mScrollView != null){
				
			switch (event.getAction())
			{
				case MotionEvent.ACTION_DOWN:
					downX = (int) event.getRawX();

					downY = (int) event.getRawY();

					break;

				case MotionEvent.ACTION_MOVE:
					int distanceX = Math.abs((int) event.getRawX() - downX);

					int distanceY = Math.abs((int) event.getRawY() - downY);

					if (distanceY > distanceX && distanceY > dragthreshold)
					{
						getParent().requestDisallowInterceptTouchEvent(false);

						mScrollView.getParent().requestDisallowInterceptTouchEvent(true);
					}
					else if (distanceX > distanceY && distanceX > dragthreshold)
					{
						getParent().requestDisallowInterceptTouchEvent(true);

						mScrollView.getParent().requestDisallowInterceptTouchEvent(false);
					}

					break;
				case MotionEvent.ACTION_UP:
					mScrollView.getParent().requestDisallowInterceptTouchEvent(false);

					getParent().requestDisallowInterceptTouchEvent(false);

					break;
			}
			}
			return false;
		}
	}
}
