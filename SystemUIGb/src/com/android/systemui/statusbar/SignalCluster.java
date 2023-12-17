package com.android.systemui.statusbar;

import com.android.systemui.R;

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
import android.view.*;
import com.android.internal.telephony.*;

public class SignalCluster extends FrameLayout{
	View mSignalCluster;
	private ImageView mInOutIcon;
	
	
	private IntentFilter mFilter;
	private Context mContext;
	private ServiceState mService;
	
	private static final int IC_IN=R.drawable.ic_qs_in, IC_INOUT=R.drawable.ic_qs_inout, IC_OUT=R.drawable.ic_qs_out;
	
	private int mDataState, mDataActivity, mSignalStrength;
	
	private AnimatedImageView mDataIcon, mSignalIcon;
	
	private final String TAG = "SignalCluster";
	public SignalCluster(Context context, AttributeSet arg1) {
		super(context, arg1);

		mContext = context;

		//sinyal dan kawan2
}
	@Override
	protected void onFinishInflate()
	{
		// TODO: Implement this method
		super.onFinishInflate();
		
		LayoutInflater li = LayoutInflater.from(mContext);
		mSignalCluster = li.inflate(R.layout.sinyal_cluster, this, false); 
		int iconSize = mContext.getResources().getDimensionPixelSize(R.dimen.status_bar_icon_size);
		ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(LayoutParams.WRAP_CONTENT,iconSize);
		
		mDataIcon = (AnimatedImageView)mSignalCluster.findViewById(R.id.tipe); 
		mSignalIcon = (AnimatedImageView)mSignalCluster.findViewById(R.id.sinyal);
		mInOutIcon = (ImageView)mSignalCluster.findViewById(R.id.inout); mInOutIcon.setColorFilter(Color.BLACK);
		addView(mSignalCluster,lp);

		mFilter = new IntentFilter();
		mFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		//mFilter.addAction(TelephonyIntents.ACTION_SIM_STATE_CHANGED);
		
		mContext.registerReceiver(mReceiver, mFilter);

		((TelephonyManager)mContext.getSystemService(Context.TELEPHONY_SERVICE))
			.listen(mPhoneStateListener, PhoneStateListener.LISTEN_DATA_CONNECTION_STATE
					| PhoneStateListener.LISTEN_DATA_ACTIVITY
					| PhoneStateListener.LISTEN_SERVICE_STATE
					| PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
	}

	private void updateConnectivity(Intent intent){
		NetworkInfo info = (NetworkInfo)(intent.getParcelableExtra(
				ConnectivityManager.EXTRA_NETWORK_INFO));
		switch(info.getType()){
		case ConnectivityManager.TYPE_MOBILE:
			updateDataNetType(info.getSubtype());
			updateInOutIcon();
			updateSignalIcon();
			break;
		default:
			mDataIcon.setImageDrawable(null);
			mInOutIcon.setImageDrawable(null);
			break;
		}
	}
	private void updateDataNetType(int type){
		if (hasService() && mDataState == TelephonyManager.DATA_CONNECTED) {
			switch (type) {
			case TelephonyManager.NETWORK_TYPE_EDGE:
					mDataIcon.setImageResource(R.drawable.ic_e);
				break;
			case  TelephonyManager.NETWORK_TYPE_UMTS:
				mDataIcon.setImageResource(R.drawable.ic_3g);
				break;
			case TelephonyManager.NETWORK_TYPE_HSDPA:
			case TelephonyManager.NETWORK_TYPE_HSUPA:
			case TelephonyManager.NETWORK_TYPE_HSPA:
					mDataIcon.setImageResource(R.drawable.ic_h);
				break;
			case TelephonyManager.NETWORK_TYPE_GPRS:
					mDataIcon.setImageResource(R.drawable.ic_g);
				break;
			default:
				mDataIcon.setImageDrawable(null);
				break;
			}
			return;
		}
		this.mDataIcon.setImageDrawable(null);
	}
	private void updateInOutIcon(){
		if (hasService() && mDataState == TelephonyManager.DATA_CONNECTED) {
			switch (mDataActivity) {
			case TelephonyManager.DATA_ACTIVITY_IN:
				//makeLog("INOUT: IN");
				this.mInOutIcon.setImageResource(IC_IN);
				break;
			case TelephonyManager.DATA_ACTIVITY_OUT:
				//makeLog("INOUT: OUT");
				this.mInOutIcon.setImageResource(IC_OUT);
				break;
			case TelephonyManager.DATA_ACTIVITY_INOUT:
				//makeLog("INOUT: INOUT");
				this.mInOutIcon.setImageResource(IC_INOUT);
				break;
			default:
				//makeLog("INOUT: NULL");
				this.mInOutIcon.setImageDrawable(null);
				break;
			}
			return;
		}
		mInOutIcon.setImageDrawable(null);
	}
	private void updateSignalIcon(){
		int level;

		if (mService == null || (!hasService()) ) {
			if (Settings.System.getInt(mContext.getContentResolver(),
					Settings.System.AIRPLANE_MODE_ON, 0) == 1) {
				mSignalIcon.setImageResource(R.drawable.stat_sys_signal_flightmode);
			} else {
				mSignalIcon.setImageResource(R.drawable.stat_sys_signal_null);
			}
			return; 
		}

		if (mSignalStrength <= 0)
			level = 0;
		else if (mSignalStrength >= 1 && mSignalStrength <= 4)
			level = 1;
		else if (mSignalStrength >= 5 && mSignalStrength <= 7)
			level = 2;
		else if (mSignalStrength >= 8 && mSignalStrength <= 11)
			level = 3;
		else if (mSignalStrength >= 12)
			level = 4;
		else
			level = 0;
		//makeLog("LEVEL: " + String.valueOf(level));
		this.mSignalIcon.setImageLevel(level);
		this.mSignalIcon.setImageResource(R.drawable.stat_sys_signal);
	}

		private boolean hasService() {
		if (mService != null) {
			switch (mService.getState()) {
			case ServiceState.STATE_OUT_OF_SERVICE:
			case ServiceState.STATE_POWER_OFF:
				return false;
			default:
				return true;
			}
		} else {
			return false;
		}
	}
	private BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			String action = arg1.getAction().toString();
			if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION) ||
					action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION) ||
					action.equals(WifiManager.RSSI_CHANGED_ACTION)) {
			}else if(action.equals(ConnectivityManager.CONNECTIVITY_ACTION)){
				updateConnectivity(arg1);
			//}else if(action.equals(TelephonyIntents.ACTION_SIM_STATE_CHANGED)){
			//updateConnectivity(arg1);
			}
		}
	};
	
	
	private PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
		@Override
		public void onServiceStateChanged(ServiceState state) {
			mService = state;
			updateSignalIcon();
			updateInOutIcon();
		}

		@Override
		public void onSignalStrengthsChanged(SignalStrength paramSignalStrength)
		{
			mSignalStrength = paramSignalStrength.getGsmSignalStrength();
			updateSignalIcon();
		}
		@Override
		public void onDataConnectionStateChanged(int state, int networkType) {
			mDataState = state;
			updateDataNetType(networkType);
			updateInOutIcon();
		}

		@Override
		public void onDataActivity(int direction) {
			mDataActivity = direction;
			updateInOutIcon();
		}
	};
}
