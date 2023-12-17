package com.android.systemui.powerwidget;

import kere.settings.*;
import android.content.*;
import android.net.*;
import android.provider.*;
import java.util.*;
import com.android.systemui.*;

public class NightModeButton extends PowerButton
{
	private static final List<Uri> OBSERVED_URIS = new ArrayList<Uri>();
    static {
        OBSERVED_URIS.add(Settings.System.getUriFor(Setelan.NIGHT_MODE_ENABLE));
    }

    public NightModeButton() { mType = BUTTON_NIGHTMODE; }

    @Override
    protected void updateState(Context context) {
        if (getNightModeState(context) == 1) {
            mIcon = R.drawable.stat_night_mode_on;
            mState = STATE_ENABLED;
        } else {
            mIcon = R.drawable.stat_night_mode_off;
            mState = STATE_DISABLED;
        }
    }

    @Override
    protected void toggleState(Context context) {
        if (getNightModeState(context) == 0) {
            Settings.System.putInt(
				context.getContentResolver(),
				Setelan.NIGHT_MODE_ENABLE, 1);
				// set brightness to lowest
			Settings.System.putInt(context.getContentResolver(), 
			                       Settings.System.SCREEN_BRIGHTNESS, android.os.Power.BRIGHTNESS_DIM);
				openNightModeDialog(context, true);
        } else {
            Settings.System.putInt(
				context.getContentResolver(),
				Setelan.NIGHT_MODE_ENABLE, 0);
        }
    }
	private void openNightModeDialog(Context context, boolean expCollapse){
		Intent intent = new Intent("openNightModeDialog");
		if(expCollapse)intent.putExtra("expCollapse",1);
        context.sendBroadcast(intent);
	}

    @Override
    protected boolean handleLongClick(Context context) {
           openNightModeDialog(context, false);
		return true;
    }

    @Override
    protected List<Uri> getObservedUris() {
        return OBSERVED_URIS;
    }

    private int getNightModeState(Context context) {
        return Settings.System.getInt(
			context.getContentResolver(),
			Setelan.NIGHT_MODE_ENABLE , 0);
    }
}
