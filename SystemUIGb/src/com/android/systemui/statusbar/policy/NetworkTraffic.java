package com.android.systemui.statusbar.policy;

import java.text.DecimalFormat;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.PorterDuff;
import android.graphics.PorterDuff.Mode;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import kere.settings.Setelan;

import com.android.systemui.R;
import com.android.systemui.statusbar.*;
import android.util.*;

/*
*
* Seeing how an Integer object in java requires at least 16 Bytes, it seemed awfully wasteful
* to only use it for a single boolean. 32-bits is plenty of room for what we need it to do.
*
*/
public class NetworkTraffic extends TextView {
    public static final int MASK_UP = 0x00000001;        // Least valuable bit
    public static final int MASK_DOWN = 0x00000002;      // Second least valuable bit
//    public static final int MASK_UNIT = 0x00000004;      // Third least valuable bit
//    public static final int MASK_PERIOD = 0xFFFF0000;    // Most valuable 16 bits

    private static final int KILOBIT = 1000;
    private static final int KILOBYTE = 1024;

    private static DecimalFormat decimalFormat = new DecimalFormat("##0.#");
    static {
        decimalFormat.setMaximumIntegerDigits(3);
        decimalFormat.setMaximumFractionDigits(1);
    }
	// state -- 0 disable, 1 up, 2 down, 3 all
    private int mState = 3;
	
    private boolean mAttached;
    private long totalRxBytes;
    private long totalTxBytes;
    private long lastUpdateTime;
    private int txtSizeSingle;
    private int txtSizeMulti;
    private int KB = KILOBYTE;
    private int MB = KB * KB;
    private int GB = MB * KB;
    private boolean mAutoHide;
    private boolean mHideArrow;
    private int mAutoHideThreshold;
    private int mNetworkTrafficColor;
	private boolean mscreenon = true;
	
    private Handler mTrafficHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            long timeDelta = SystemClock.elapsedRealtime() - lastUpdateTime;

            if (timeDelta < getInterval(mState) * .95) {
                if (msg.what != 1) {
                    // we just updated the view, nothing further to do
                    return;
                }
                if (timeDelta < 1) {
                    // Can't div by 0 so make sure the value displayed is minimal
                    timeDelta = Long.MAX_VALUE;
                }
            }
            lastUpdateTime = SystemClock.elapsedRealtime();

            // Calculate the data rate from the change in total bytes and time
            long newTotalRxBytes = TrafficStats.getTotalRxBytes();
            long newTotalTxBytes = TrafficStats.getTotalTxBytes();
            long rxData = newTotalRxBytes - totalRxBytes;
            long txData = newTotalTxBytes - totalTxBytes;

            if (shouldHide(rxData, txData, timeDelta)) {
                setText("");
                setVisibility(View.GONE);
            } else if (!getConnectAvailable()) {
                clearHandlerCallbacks();
                setVisibility(View.GONE);
            } else {
                // If bit/s convert from Bytes to bits
                String symbol;
                if (KB == KILOBYTE) {
                    symbol = "B/s";
                } else {
                    symbol = "b/s";
                    rxData = rxData * 8;
                    txData = txData * 8;
                }

                // Get information for uplink ready so the line return can be added
                String output = "";
                if (isSet(mState, MASK_UP)) {
                    output = formatOutput(timeDelta, txData, symbol);
                }

                // Ensure text size is where it needs to be
                int textSize;
                if (isSet(mState, MASK_UP + MASK_DOWN)) {
                    output += "\n";
                    textSize = txtSizeMulti;
                } else {
                    textSize = txtSizeSingle;
                }

                // Add information for downlink if it's called for
                if (isSet(mState, MASK_DOWN)) {
                    output += formatOutput(timeDelta, rxData, symbol);
                }

                // Update view if there's anything new to show
                if (! output.contentEquals(getText())) {
                    setTextSize(TypedValue.COMPLEX_UNIT_PX, (float)textSize);
                    setText(output);
                }
                setVisibility(View.VISIBLE);
            }

            // Post delayed message to refresh in ~1000ms
            totalRxBytes = newTotalRxBytes;
            totalTxBytes = newTotalTxBytes;
            clearHandlerCallbacks();
            mTrafficHandler.postDelayed(mRunnable, getInterval(mState));
        }

        private String formatOutput(long timeDelta, long data, String symbol) {
            long speed = (long)(data / (timeDelta / 1000F));
            if (speed < KB) {
                return decimalFormat.format(speed) + symbol;
            } else if (speed < MB) {
                return decimalFormat.format(speed / (float)KB) + 'k' + symbol;
            } else if (speed < GB) {
                return decimalFormat.format(speed / (float)MB) + 'M' + symbol;
            }
            return decimalFormat.format(speed / (float)GB) + 'G' + symbol;
        }

        private boolean shouldHide(long rxData, long txData, long timeDelta) {
            long speedTxKB = (long)(txData / (timeDelta / 1000f)) / KILOBYTE;
            long speedRxKB = (long)(rxData / (timeDelta / 1000f)) / KILOBYTE;
            int mState = 2;
                return mAutoHide &&
                   (mState == MASK_DOWN && speedRxKB <= mAutoHideThreshold ||
                    mState == MASK_UP && speedTxKB <= mAutoHideThreshold ||
                    mState == MASK_UP + MASK_DOWN &&
                       speedRxKB <= mAutoHideThreshold &&
                       speedTxKB <= mAutoHideThreshold);
        }
    };

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            mTrafficHandler.sendEmptyMessage(0);
        }
    };

    class SettingsObserver extends ContentObserver {
        SettingsObserver(Handler handler) {
            super(handler);
        }

        void observe() {
            ContentResolver resolver = mContext.getContentResolver();
            Uri uri = Settings.System.getUriFor(Setelan.STATUS_BAR_TRAFFIC);
            resolver.registerContentObserver(uri,false,this);
            
        //            resolver.registerContentObserver(Settings.System
//                    .getUriFor(Settings.System.NETWORK_TRAFFIC_AUTOHIDE_THRESHOLD), false,
//                    this, UserHandle.USER_ALL);
                  }

        /*
         *  @hide
         */
        @Override
        public void onChange(boolean selfChange) {
            updateSettings();
        }
    }

    /*
     *  @hide
     */
    public NetworkTraffic(Context context) {
        this(context, null);
    }

    /*
     *  @hide
     */
    public NetworkTraffic(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /*
     *  @hide
     */
	 private Handler mHandler;
    public NetworkTraffic(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs);
        final Resources resources = getResources();
        txtSizeSingle = resources.getDimensionPixelSize(R.dimen.net_traffic_single_text_size);
        txtSizeMulti = resources.getDimensionPixelSize(R.dimen.net_traffic_multi_text_size);
        Handler h = new Handler();
        SettingsObserver settingsObserver = new SettingsObserver(h);
        settingsObserver.observe();
        updateSettings();
		
		mHandler = new Handler();
		ColorBarTint.addListener(new ColorBarTint.UpdateListener(){
				@Override
				public void onUpdateStatusBarIconColor(final int newColor){
					mHandler.post(new Runnable(){

							@Override
							public void run()
							{
								mNetworkTrafficColor = newColor;
								setTextColor(mNetworkTrafficColor);
								updateTrafficDrawable();
								// TODO: Implement this method
							}


						});
				}
			});
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!mAttached) {
            mAttached = true;
            IntentFilter filter = new IntentFilter();
            filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
			//filter.addAction(Intent.ACTION_SCREEN_OFF);
			//filter.addAction(Intent.ACTION_SCREEN_ON);
            mContext.registerReceiver(mIntentReceiver, filter, null, getHandler());
        }
        updateSettings();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mAttached) {
            mContext.unregisterReceiver(mIntentReceiver);
            mAttached = false;
        }
    }

    private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null && action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                updateSettings();
            }
			/*if (action != null && action.equals(Intent.ACTION_SCREEN_ON)) {
				mscreenon = true;
                updateVisibility();
            }
			if (action != null && action.equals(Intent.ACTION_SCREEN_ON)) {
				mscreenon = false;
                updateVisibility();
            }*/
        }
    };

    private boolean getConnectAvailable() {
        ConnectivityManager connManager =
                (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo network = (connManager != null) ? connManager.getActiveNetworkInfo() : null;
        return network != null && network.isConnected();
    }

    private void updateSettings() {
        ContentResolver resolver = mContext.getContentResolver();

        int defaultColor = 0xffffffff; //Settings.System.getInt(resolver,
               // Settings.System.NETWORK_TRAFFIC_COLOR, 0xFFFFFFFF);

        mAutoHide = false;

        mHideArrow = false; 
        mAutoHideThreshold = 10; //Settings.System.getIntForUser(resolver,
              //  Settings.System.NETWORK_TRAFFIC_AUTOHIDE_THRESHOLD, 10,
               // UserHandle.USER_CURRENT);
			   
	    //njejel aktifna
        mState = Settings.System.getInt(resolver, Setelan.STATUS_BAR_TRAFFIC, 0);
		if(mState>0)mState+=1;
        mNetworkTrafficColor = defaultColor; // Settings.System.getInt(resolver,
               // Settings.System.NETWORK_TRAFFIC_COLOR, -2);

            if (mNetworkTrafficColor == Integer.MIN_VALUE
                || mNetworkTrafficColor == -2) {
            mNetworkTrafficColor = defaultColor;
        }

            //setTextColor(mNetworkTrafficColor);
            updateTrafficDrawable();

//        if (isSet(mState, MASK_UNIT)) {
//            KB = KILOBYTE;
//        } else {
//            KB = KILOBIT;
//        }
        MB = KB * KB;
        GB = MB * KB;

        updateVisibility();
    }
	private void updateVisibility(){
		if ((isSet(mState, MASK_UP) || isSet(mState, MASK_DOWN))) {
            if (getConnectAvailable()) {
                if (mAttached) {
                    totalRxBytes = TrafficStats.getTotalRxBytes();
                    lastUpdateTime = SystemClock.elapsedRealtime();
                    mTrafficHandler.sendEmptyMessage(1);
                }
                setVisibility(View.VISIBLE);
                updateTrafficDrawable();
                return;
            }
        } else {
            clearHandlerCallbacks();
        }
        setVisibility(View.GONE);
	}
	
    private static boolean isSet(int intState, int intMask) {
        return (intState & intMask) == intMask;
    }

    private static int getInterval(int intState) {
        int intInterval = intState >>> 16;
        return (intInterval >= 250 && intInterval <= 32750) ? intInterval : 1000;
    }

    private void clearHandlerCallbacks() {
        mTrafficHandler.removeCallbacks(mRunnable);
        mTrafficHandler.removeMessages(0);
        mTrafficHandler.removeMessages(1);
    }

    private void updateTrafficDrawable() {
        int intTrafficDrawable;
        Drawable drw = null;
        if (!mHideArrow) {
            if (isSet(mState, MASK_UP + MASK_DOWN)) {
                intTrafficDrawable = R.drawable.stat_sys_network_traffic_updown;
            } else if (isSet(mState, MASK_UP)) {
                intTrafficDrawable = R.drawable.stat_sys_network_traffic_up;
            } else if (isSet(mState, MASK_DOWN)) {
                intTrafficDrawable = R.drawable.stat_sys_network_traffic_down;
            } else {
                intTrafficDrawable = 0;
            }
            if (intTrafficDrawable != 0) {
                drw = getContext().getResources().getDrawable(intTrafficDrawable);
                drw.setColorFilter(mNetworkTrafficColor, PorterDuff.Mode.SRC_ATOP);
            }
        } else {
            drw = null;
        }
        setCompoundDrawablesWithIntrinsicBounds(null, null, drw, null);
    }
}
