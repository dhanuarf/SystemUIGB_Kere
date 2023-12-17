package com.android.systemui.statusbar;

import android.widget.*;
import android.view.*;
import android.os.*;
import android.content.*;
import android.util.*;

public class ColorTextViewTint extends TextView 
{
	private Handler mHandler ;
	boolean DEBUG_COLOR_CHANGE = true;
	public ColorTextViewTint(Context c, AttributeSet as){
		super(c,as);
		setTextColor(0xffffffff);
		mHandler = new Handler();
		ColorBarTint.addListener(new ColorBarTint.UpdateListener(){
			@Override
			public void onUpdateStatusBarIconColor(final int newColor){
				mHandler.post(new Runnable(){

						@Override
						public void run()
						{
							setTextColor(newColor);
							// TODO: Implement this method
						}
						
					
				});
			}
		});
	}

	@Override
	protected void onAttachedToWindow()
	{
		// TODO: Implement this method
		super.onAttachedToWindow();
	}

	@Override
	protected void onDetachedFromWindow()
	{
		// TODO: Implement this method
		super.onDetachedFromWindow();
		
	}

	@Override
	protected void onVisibilityChanged(View p1, int p2)
	{
		// TODO: Implement this method
		super.onVisibilityChanged(p1, p2);
	}
	
}
