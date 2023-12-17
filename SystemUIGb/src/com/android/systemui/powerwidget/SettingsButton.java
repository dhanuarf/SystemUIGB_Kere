package com.android.systemui.powerwidget;

import com.android.systemui.R;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import java.util.ArrayList;
import java.util.List;
public class SettingsButton extends PowerButton {

    public SettingsButton() {
		mType = BUTTON_SETTINGS;
	}

	@Override
    protected void updateState(Context context) {
            mIcon = R.drawable.ic_qs_settings;
            mState = STATE_ENABLED;
    }
    @Override
    protected void toggleState(Context context) {
        Intent intent = new Intent("com.android.settings.Settings");
		//      intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);

	}
    @Override
    protected boolean handleLongClick(Context context) {
        // it may be better to make an Intent action for the Torch
        // we may want to look at that option later
        Intent intent = new Intent("com.android.settings.Settings");
		//    intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
		return true;
    }
	}
