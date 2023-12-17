package com.android.systemui.statusbar;

import android.content.*;
import android.graphics.drawable.*;
import android.graphics.drawable.shapes.*;
import android.util.*;
import android.view.*;
import android.widget.*;
import android.provider.*;
import android.database.*;
import android.os.*;

import kere.settings.*;

public class StatusBarCover extends FrameLayout
{
	StatusBarService mService;
	private int currentmode;
	private Handler mHandler= new Handler();
	private SettingsObserver sObserver;
	private class SettingsObserver extends ContentObserver
	{
        SettingsObserver(Handler handler)
		{
            super(handler);
        }

        void observe()
		{
            ContentResolver resolver = mContext.getContentResolver();
            resolver.registerContentObserver(Settings.System.getUriFor(
												 Setelan.STATUS_BAR_BG_STYLE), false, this);  		}

        @Override public void onChange(boolean selfChange)
		{
            updateCover();
        }
    }
	GradientDrawable mGradientDrawable;

	public StatusBarCover(Context c, AttributeSet as){
		super(c,as);
		sObserver=new SettingsObserver(mHandler);
		mGradientDrawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
								  new int[] { 
									  0xbb000000, 0x99000000, 0x77000000 , 0x55000000,
									  0x00000000/*, 0x81000000, 0x7c000000, 0x77000000, 
									  0x72000000, 0x6c000000, 0x68000000, 0x2f000000, 
									  0x27000000, 0x20000000, 0x1a000000, 0x1f000000,
									  0x0a000000, 0x00000000*/ });
		mGradientDrawable.setShape(GradientDrawable.RECTANGLE);
		mGradientDrawable.setGradientCenter(0.5f,0.95f);
		
	}

	@Override
	protected void onAttachedToWindow()
	{
		// TODO: Implement this method
		super.onAttachedToWindow();
		sObserver.observe();
		updateCover();
	}
	public int getCurrentMode(){
		return currentmode;
	}
	private void updateCover(){
		int value=Settings.System.getInt(getContext().getContentResolver(),Setelan.STATUS_BAR_BG_STYLE, 0);
		currentmode = value;
		if(value==0)
			setBackgroundColor(0x00000000);

		else if(value==1)
			setBackgroundDrawable(mGradientDrawable);

		else if(value==2)
			setBackgroundColor(0x77000000);		
			
		mService.setIsUpdateDsbIcon();
	}
}
