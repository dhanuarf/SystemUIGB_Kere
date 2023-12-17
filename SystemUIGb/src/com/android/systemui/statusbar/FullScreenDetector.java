package com.android.systemui.statusbar;

import android.view.*;
import android.content.*;
import android.util.*;
import android.widget.*;

public class FullScreenDetector extends View
{
	private OnStatusbarVisibilityChangeListener osvcl;
	public FullScreenDetector (Context c, AttributeSet as){
		super(c,as);
		
	}

	@Override
	protected void onLayout(boolean p1, int left, int top, int right, int bottom)
	{
		// TODO: Implement this method
		super.onLayout(p1, left, top, right, bottom);
		
		if(osvcl != null )osvcl.OnStatusbarVisibilityChange();
	}
	public void setListener(OnStatusbarVisibilityChangeListener listener){
		osvcl = listener;
	}
	public interface OnStatusbarVisibilityChangeListener{
		public void OnStatusbarVisibilityChange();
	}
}
