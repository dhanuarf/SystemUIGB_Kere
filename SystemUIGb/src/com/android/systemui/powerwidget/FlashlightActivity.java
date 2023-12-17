package com.android.systemui.powerwidget;

import android.app.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import android.widget.FrameLayout.*;
import android.graphics.drawable.*;

public class FlashlightActivity extends Activity 
{

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		FrameLayout ll=new FrameLayout(this);
		ll.setBackgroundColor(0xffffffff);
		ll.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		
		WindowManager.LayoutParams wparams = getWindow().getAttributes();
		getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		//getWindow().setBackgroundDrawable(new ColorDrawable(0xffffffff));
		wparams.screenBrightness = 1F;
		wparams.flags = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_FULLSCREEN;
		getWindow().setAttributes(wparams);
		
		setContentView(ll);
		
			
		// TODO: Implement this method
		
		}
	
}
