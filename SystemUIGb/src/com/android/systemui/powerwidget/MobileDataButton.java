package com.android.systemui.powerwidget;

import com.android.systemui.R;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.provider.Settings;
import com.android.internal.telephony.Phone;
public class MobileDataButton extends PowerButton {
    public static final String ACTION_MODIFY_NETWORK_MODE = "com.android.internal.telephony.MODIFY_NETWORK_MODE";
    public static final String ACTION_MOBILE_DATA_CHANGED = "com.android.internal.telephony.MOBILE_DATA_CHANGED";
    public static final String EXTRA_NETWORK_MODE = "networkMode";
    public MobileDataButton() { mType = BUTTON_MOBILEDATA; }
    @Override
    protected void updateState(Context context) {
        if (getDataState(context)) {
            mIcon = R.drawable.stat_data_on;
            mState = STATE_ENABLED;
        } else {
            mIcon = R.drawable.stat_data_off;
            mState = STATE_DISABLED;
        }
    }
    @Override
    protected void toggleState(Context context) {
		updateState(context);
        boolean mobiledataEnabled = getDataState(context);
        boolean toggleNetworkMode = false;// Settings.System.getInt(context.getContentResolver(),
 //               Settings.System.EXPANDED_MOBILEDATANETWORK_MODE, 0) == 1;
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (mobiledataEnabled) {
            if (toggleNetworkMode) {
                // mobile data being disabled - switch network mode to 2g only
                Intent intent = new Intent(ACTION_MODIFY_NETWORK_MODE);
                intent.putExtra(EXTRA_NETWORK_MODE, Phone.NT_MODE_GSM_ONLY);
                context.sendBroadcast(intent);
            }
            // disable mobile data
            cm.setMobileDataEnabled(false);
        } else {
            if (toggleNetworkMode) {
                // mobile data being enabled - switch network mode to 2g/3g
                Intent intent = new Intent(ACTION_MODIFY_NETWORK_MODE);
                intent.putExtra(EXTRA_NETWORK_MODE, Phone.NT_MODE_WCDMA_PREF);
                context.sendBroadcast(intent);
            }
            // enable mobile data
            cm.setMobileDataEnabled(true);
        }
    }
	
    @Override
    protected boolean handleLongClick(Context context) {
        // it may be better to make an Intent action for this or find the appropriate one
        // we may want to look at that option later
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setClassName("com.android.phone", "com.android.phone.Settings");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
        return true;
    }
    @Override
    protected IntentFilter getBroadcastIntentFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        return filter;
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        updateState(context);
    }	
    private boolean getDataState(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
            .getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getMobileDataEnabled();
    }
}
