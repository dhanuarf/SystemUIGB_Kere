package com.android.systemui.powerwidget;

import android.content.*;
import android.net.*;
import android.provider.*;
import java.util.*;
import com.android.systemui.*;
import kere.settings.*;
public class DSBButton extends PowerButton
{
	private static final List<Uri> OBSERVED_URIS = new ArrayList<Uri>();
    static {
        OBSERVED_URIS.add(Settings.System.getUriFor(Setelan.TINT_ENABLE));
    }

    public DSBButton() { mType = BUTTON_DSB; }

    @Override
    protected void updateState(Context context) {
        if (getDsbState(context) == 1) {
            mIcon = R.drawable.stat_dsb_on;
            mState = STATE_ENABLED;
        } else {
            mIcon = R.drawable.stat_dsb_off;
            mState = STATE_DISABLED;
        }
    }

    @Override
    protected void toggleState(Context context) {
        if (getDsbState(context) == 0) {
            Settings.System.putInt(
				context.getContentResolver(),
				Setelan.TINT_ENABLE, 1);
        } else {
            Settings.System.putInt(
				context.getContentResolver(),
				Setelan.TINT_ENABLE, 0);
        }
    }


    @Override
    protected boolean handleLongClick(Context context) {
     /*   Intent intent = new Intent("android.settings.DISPLAY_SETTINGS");
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);*/
        return true;
    }

    @Override
    protected List<Uri> getObservedUris() {
        return OBSERVED_URIS;
    }

    private int getDsbState(Context context) {
        return Settings.System.getInt(
			context.getContentResolver(),
			Setelan.TINT_ENABLE , 0);
    }
}
