package com.android.systemui.statusbar;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.util.*;
import kere.util.*;
import android.util.*;
import android.provider.*;
import kere.settings.*;
import android.database.*;

/*
 * DSB (ParanoidAndroid's feature)-like mod 
 * to tint your staturbar follow the top of 
 * your screen
 *
 * ---------- ### ----------
 *
 * ported to Touchwiz GB by Dhanu Dwi Arfendi.
 * it uses Touchwiz's screencapture lib,
 * so it is for touchwiz rom only 
 */
public class ColorBarTint
{
	private static final String mDsbIntent = "statusbarmod.dsb";

	private static DSBBroadcastReceiver mDsbReceiver = new DSBBroadcastReceiver();
	private static boolean mReceiverRegistered = false;
	private static int mCurrentBarColor = 0xff000000, mCurrentIconColor =0xffffffff;
	private static Context mContext = null;

	private static boolean mUpdateIcon = true;
	private static final boolean DEBUG_COLOR_CHANGE = false;

	private static String LOG_TAG = "ColorBarTint";

	public static void setIsUpdateIcon(boolean b){
		mUpdateIcon = b;
	}
	public synchronized static void set(Context c)
	{
		mContext = c;
		if (!mReceiverRegistered && mContext != null)
		{
			mContext.registerReceiver(mDsbReceiver, new IntentFilter(mDsbIntent));
			mReceiverRegistered = true;
		}
	}
	public synchronized static void unset()
	{
		if (mReceiverRegistered && mContext != null)
		{
			mContext.unregisterReceiver(mDsbReceiver);
			mReceiverRegistered = false;
		}
	} 
	private static final ArrayList<UpdateListener> mListeners = new ArrayList<UpdateListener>();
	public synchronized static void addListener(final UpdateListener... listeners)
	{
        for (final UpdateListener listener : listeners)
		{
            if (listener == null)
			{
                continue;
            }

			boolean shouldAdd = true;

            for (final UpdateListener existingListener : mListeners)
			{
                if (existingListener == listener)
				{
                    shouldAdd = false;
                }
            }

            if (shouldAdd)
			{
                mListeners.add(listener);
            }
			updateStatusBarColor(mCurrentBarColor);
			updateStatusBarIconColor(mCurrentIconColor);
        }
	}
	public synchronized static int getCurrentBarColor()
	{
		return mCurrentBarColor;
	}
	public synchronized static void updateStatusBarColor(final int newColor)
	{

		if (DEBUG_COLOR_CHANGE && mCurrentBarColor != newColor)
		{
            Log.d(LOG_TAG, "statusBarOverrideColor=" + (newColor == 0 ? "none" :
				  "0x" + Integer.toHexString(newColor)));
        }

        for (final UpdateListener listener : mListeners)
		{
			if (listener == null)continue;

            listener.onUpdateStatusBarColor(mCurrentBarColor, newColor);

        }
		mCurrentBarColor = newColor;
    }
	private static boolean alreadyupdatewhite = false;
	public synchronized static void updateStatusBarIconColor(final int newColor)
	{
		if(!mUpdateIcon){
			if(!alreadyupdatewhite){
				for (final UpdateListener listener : mListeners)
				{
					if (listener == null)continue;

					listener.onUpdateStatusBarIconColor(0xffffffff);
				}
				alreadyupdatewhite = true;
			}
			return;
		}
		
		if(mCurrentIconColor == newColor)return;
		alreadyupdatewhite = false;
		if (DEBUG_COLOR_CHANGE && mCurrentIconColor != newColor)
		{
            Log.d(LOG_TAG, "statusBarIconOverrideColor=" + (newColor == 0 ? "none" :
				  "0x" + Integer.toHexString(newColor)));
        }

        for (final UpdateListener listener : mListeners)
		{
			if (listener == null)continue;

            listener.onUpdateStatusBarIconColor(newColor);
        }
		mCurrentIconColor = newColor;
    }

	// own receiver
	private static class DSBBroadcastReceiver extends BroadcastReceiver
	{

		@Override
		public void onReceive(Context p1, Intent p2)
		{ synchronized (ColorBarTint.class)
			{
				final int color = p2.getIntExtra("dsbColor", 0xff000000);
				final boolean isBright= ColorUtils.isBrightColor(color);

				updateStatusBarColor(color);
				updateStatusBarIconColor(isBright ? 0x95000000 : 0xffffffff);
				// TODO: Implement this method
			}
		}
	};
	public static class UpdateListener
	{
		//   private final WeakReference<Object> mRef;

        public UpdateListener()
		{
			//   mRef = new WeakReference<Object>(ref);
        }

		/* public final boolean shouldGc() {
		 return mRef.get() == null;
		 }
		 */
        public void onUpdateStatusBarColor(final int previousColor, final int color)
		{
        }

        public void onUpdateStatusBarIconColor(final int iconColor)
		{
        }
	}
}
