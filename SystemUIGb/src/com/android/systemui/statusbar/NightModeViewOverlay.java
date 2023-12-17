package com.android.systemui.statusbar;

import android.widget.*;
import android.view.*;
import android.content.*;
import android.util.*;
import android.graphics.*;
public class NightModeViewOverlay extends View
{

	private int mIntensityValue = 0;
	private int mScrDimValue = 0;
	private int mRed, mGreen, mBlue;
	
	public NightModeViewOverlay(Context context){
		super(context);
		final int lowTemperatureColor = 0xFFFF0000;
		mRed = Color.red(lowTemperatureColor);
		mGreen = Color.green(lowTemperatureColor);
		mBlue = Color.blue(lowTemperatureColor);
	}
	
	private final int defScrDimValue = 60;
	@Override
	protected void onDraw(Canvas p1)
	{
		super.onDraw(p1);
		p1.drawARGB(mIntensityValue+25,mRed,0,0);
		p1.drawARGB(mScrDimValue + defScrDimValue,0,0,0);
	}
	
	public void updateIntensity(int intensityValue){
		mIntensityValue = intensityValue;
		
		invalidate();
	}
	public void updateScrDim(int scrDimValue){
		mScrDimValue = scrDimValue;
		
		invalidate();
	}
}
