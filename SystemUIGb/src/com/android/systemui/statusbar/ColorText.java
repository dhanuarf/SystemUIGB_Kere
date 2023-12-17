package com.android.systemui.statusbar;

import android.content.*;
import android.util.*;
import android.widget.*;
import android.provider.*;

public class ColorText extends TextView
{
	public ColorText(Context c, AttributeSet as){
		super(c,as);
		ContentResolver cr = c.getContentResolver();

		int kolor= Settings.System.getInt(cr,
                        "expanded_widget_color", 0xFFFFFFFF);
		setTextColor(kolor);
	}
}
