package com.android.systemui.powerwidget;

import com.android.systemui.R;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import java.util.ArrayList;
import java.util.List;
import java.lang.reflect.*;
public class FlashlightButton extends PowerButton {

    public FlashlightButton() {
		mType = BUTTON_FLASHLIGHT;
		}
 
	@Override
    protected void updateState(Context context) {
        boolean enabled = true;//Settings.System.getInt(context.getContentResolver(), Settings.System.TORCH_STATE, 0) == 1;
        if(enabled) {
            mIcon = R.drawable.stat_flashlight_on;
            mState = STATE_ENABLED;
        } else {
            mIcon = R.drawable.stat_flashlight_off;
            mState = STATE_DISABLED;
        }
    }
    @Override
    protected void toggleState(Context context) {
        Intent intent = new Intent();
		intent.setClassName("com.android.systemui", "com.android.systemui.powerwidget.FlashlightActivity"); 
		//      intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
		try
		{ 
			Object service  = context.getSystemService("statusbar");
			Class<?> statusbarManager = Class.forName("android.app.StatusBarManager");
			Method collapse = statusbarManager.getMethod("collapse");
			collapse.invoke(service);
		}
		catch (Exception ex)
		{           

		}
		
		}
    @Override
    protected boolean handleLongClick(Context context) {
        // it may be better to make an Intent action for the Torch
        // we may want to look at that option later
        Intent intent = new Intent();//("com.android.systemui.powerwidget.FlashlightActivity");
		intent.setClassName("com.android.systemui", "com.android.systemui.powerwidget.FlashlightActivity");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
		return true;
    }
}
