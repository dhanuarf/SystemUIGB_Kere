package com.android.systemui.statusbar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.provider.Settings;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.graphics.*;
import kere.widget.*;
import com.android.systemui.R;
import android.view.*;

public class WifiCluster extends FrameLayout
{
	private int IC_IN=R.drawable.ic_qs_in, IC_INOUT=R.drawable.ic_qs_inout, IC_OUT=R.drawable.ic_qs_out;
	private Handler mWifiHandler;
	private ImageView mWifiSignalIcon;
	private boolean mIsWifiConnected = false;
	private boolean isWifiInUse; 
	private long lastTx,lastRx;
	//View mWifiCluster;
	IntentFilter mFilter;
	ImageView mWifiInOutIcon;
	private Handler mHandler;
	public WifiCluster(Context c, AttributeSet as){
		super(c,as);
		mWifiHandler = new Handler();
		//wifi dan kawan2
		mWifiSignalIcon = new ImageView(c,as); mWifiInOutIcon = new ImageView(c);
		final int iconSize = mContext.getResources().getDimensionPixelSize(R.dimen.status_bar_icon_size);
		ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(LayoutParams.WRAP_CONTENT,iconSize);
		addView(mWifiSignalIcon, lp); addView(mWifiInOutIcon);
		mWifiInOutIcon.setImageDrawable(null); mWifiInOutIcon.setColorFilter(Color.BLACK);
		setVisibility(View.GONE);
		mWifiSignalIcon.setImageResource(R.drawable.stat_sys_wifi_signal);
		
		mFilter = new IntentFilter();
		mFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		mFilter.addAction(ConnectivityManager.INET_CONDITION_ACTION);
		mFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
		mFilter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
		mFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
		mFilter.addAction(WifiManager.RSSI_CHANGED_ACTION);

		c.registerReceiver(mReceiver, mFilter);
		
		mHandler = new Handler();
		ColorBarTint.addListener(new ColorBarTint.UpdateListener(){
			@Override
			public void onUpdateStatusBarIconColor(final int newColor){
				mHandler.post(new Runnable(){

						@Override
						public void run()
						{
							mWifiSignalIcon.setColorFilter(newColor, PorterDuff.Mode.MULTIPLY);
							mWifiInOutIcon.setColorFilter((newColor ==0x95000000)? Color.WHITE : Color.BLACK);
							// TODO: Implement this method
						}
						
					
				});
			}
		});
	}
	private int mLastWifiSignal = -1;
	private void updateConnectivity(Intent intent){
		NetworkInfo info = (NetworkInfo)(intent.getParcelableExtra(
			ConnectivityManager.EXTRA_NETWORK_INFO));
		switch(info.getType()){
			//case ConnectivityManager.TYPE_MOBILE:
			//	mWifiCluster.setVisibility(View.GONE);
			//	break;
			case ConnectivityManager.TYPE_WIFI:
				if (info.isConnected()) {
					mIsWifiConnected = true;
					setVisibility(View.VISIBLE);
					mWifiHandler.post(WifiInOut);
					int iconId;
					if (mLastWifiSignal == -1) {
						iconId = 1;
					} else {
						iconId = mLastWifiSignal;
					}
					mWifiSignalIcon.setImageLevel(iconId);
					
				}else{
					mLastWifiSignal =  -1;
					mIsWifiConnected = false;
					setVisibility(View.GONE);
					mWifiHandler.removeCallbacks(WifiInOut);
				}
				break;
		}
	}
//	private final void updateWifi(Intent intent) {
//        final String action = intent.getAction();
//        if (action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
//
//            final boolean enabled = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,
//													   WifiManager.WIFI_STATE_UNKNOWN) == WifiManager.WIFI_STATE_ENABLED;
//
//            if (!enabled) {
//                // If disabled, hide the icon. (We show icon when connected.)
//                mService.setIconVisibility("wifi", false);
//            }
//
//        } else if (action.equals(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION)) {
//            final boolean enabled = intent.getBooleanExtra(WifiManager.EXTRA_SUPPLICANT_CONNECTED,
//                                                           false);
//            if (!enabled) {
//                mService.setIconVisibility("wifi", false);
//            }
//        } else if (action.equals(WifiManager.RSSI_CHANGED_ACTION)) {
//            int iconId;
//            final int newRssi = intent.getIntExtra(WifiManager.EXTRA_NEW_RSSI, -200);
//            int newSignalLevel = WifiManager.calculateSignalLevel(newRssi,
//                                                                  sWifiSignalImages[0].length);
//            if (newSignalLevel != mLastWifiSignalLevel) {
//                mLastWifiSignalLevel = newSignalLevel;
//                if (mIsWifiConnected) {
//                    iconId = sWifiSignalImages[mInetCondition][newSignalLevel];
//                } else {
//                    iconId = sWifiTemporarilyNotConnectedImage;
//                }
//                mService.setIcon("wifi", iconId, 0);
//            }
//        }
//    }
	private final static boolean DEBUG = false;
	private static final String LOG = "wifiCluster";
	private void updateWifiSignalIcon(Intent intent){
		final String action = intent.getAction();
		if (action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {

			final boolean enabled = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,
					WifiManager.WIFI_STATE_UNKNOWN) == WifiManager.WIFI_STATE_ENABLED;

			if (!enabled) {
				// If disabled, hide the icon. (We show icon when connected.)
				setVisibility(View.GONE);
			}

		} else if (action.equals(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION)) {
			final boolean enabled = intent.getBooleanExtra(WifiManager.EXTRA_SUPPLICANT_CONNECTED,
					false);
			if (!enabled) {
				setVisibility(View.GONE);
			}
		} else if (action.equals(WifiManager.RSSI_CHANGED_ACTION)) {
			int iconId;
			final int newRssi = intent.getIntExtra(WifiManager.EXTRA_NEW_RSSI, -200);
			int newSignalLevel = WifiManager.calculateSignalLevel(newRssi,5);
			if (newSignalLevel != mLastWifiSignal) {
                mLastWifiSignal = newSignalLevel;
                if (mIsWifiConnected) {
                    iconId = newSignalLevel;
                } else {
                    iconId = 0;
                }
				mWifiSignalIcon.setImageLevel(iconId);
			}
			if(DEBUG)Log.d(LOG, "levelIcon:"+iconId);
			//mWifiSignalIcon.setImageResource(R.drawable.stat_sys_wifi_signal);
		}
	}
	private BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			String action = arg1.getAction();
			if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION) ||
					action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION) ||
					action.equals(WifiManager.RSSI_CHANGED_ACTION)) {
				updateWifiSignalIcon(arg1);
			}
			else if(action.equals(ConnectivityManager.CONNECTIVITY_ACTION) 
				     || action.equals(ConnectivityManager.INET_CONDITION_ACTION)){
				updateConnectivity(arg1);
			}
			
		}
	};
	private Runnable WifiInOut = new Runnable() {

	    public void run() {
	        long cRx = TrafficStats.getTotalRxBytes() - TrafficStats.getMobileRxBytes();
	        long cTx = TrafficStats.getTotalTxBytes() - TrafficStats.getMobileTxBytes();
	        if (cTx - lastTx != 0 || cRx - lastRx != 0){
	        	if (!isWifiInUse){
	                isWifiInUse = true;
	                mWifiInOutIcon.setImageResource(IC_INOUT);
	                if(cTx - lastTx !=0 && cRx - lastRx == 0){
	                	mWifiInOutIcon.setImageResource(IC_IN);
	                }else if(cTx - lastTx ==0 && cRx - lastRx != 0){
	                	mWifiInOutIcon.setImageResource(IC_OUT);
	                }
	        	}
	        }else if (isWifiInUse){
	        	isWifiInUse = false;
	            mWifiInOutIcon.setImageDrawable(null);
	        }
	        lastRx = cRx;
	        lastTx = cTx;
	        mWifiHandler.postDelayed(this, 1000);
	    }
	}; 


}
