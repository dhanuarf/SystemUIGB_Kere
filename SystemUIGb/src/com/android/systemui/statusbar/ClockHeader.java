package com.android.systemui.statusbar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.graphics.Typeface;
import android.os.Handler;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.*;
import android.widget.TextView;
import android.text.format.DateFormat;
import com.android.systemui.R;
import java.lang.ref.WeakReference;
import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.*;
//import android.widget.*;

/**
 * Displays the time
 */
public class ClockHeader extends TextView {

    private final static String M12 = "hh:mm";
   // private final static String M24 = "kk:mm";

    private Calendar mCalendar;
    private String mFormat;
    private CharSequence mTimeChar, mDateChar, mAmPmChar;
	private String mAmString, mPmString;
	private boolean mMorning;
    private ContentObserver mFormatChangeObserver;
    private int mAttached = 0; // for debugging - tells us whether attach/detach is unbalanced

    /* called by system on minute ticks */
    private final Handler mHandler = new Handler();
    private BroadcastReceiver mIntentReceiver;

    private static class TimeChangedReceiver extends BroadcastReceiver {
        private WeakReference<ClockHeader> mClock;
        private Context mContext;
		
        public TimeChangedReceiver(ClockHeader clock) {
            mClock = new WeakReference<ClockHeader>(clock);
            mContext = clock.getContext();
			
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            // Post a runnable to avoid blocking the broadcast.
            final boolean timezoneChanged =
                    intent.getAction().equals(Intent.ACTION_TIMEZONE_CHANGED);
            final ClockHeader clock = mClock.get();
            if (clock != null) {
                clock.mHandler.post(new Runnable() {
                    public void run() {
                        if (timezoneChanged) {
                            clock.mCalendar = Calendar.getInstance();
                        }
                        clock.updateTime();
                    }
                });
            } else {
                try {
                    mContext.unregisterReceiver(this);
                } catch (RuntimeException e) {
                    // Shouldn't happen
                }
            }
        }
    };

    private static class FormatChangeObserver extends ContentObserver {
        private WeakReference<ClockHeader> mClock;
        private Context mContext;
        public FormatChangeObserver(ClockHeader clock) {
            super(new Handler());
            mClock = new WeakReference<ClockHeader>(clock);
            mContext = clock.getContext();
        }
        @Override
        public void onChange(boolean selfChange) {
            ClockHeader digitalClock = mClock.get();
            if (digitalClock != null) {
                digitalClock.setDateFormat();
                digitalClock.updateTime();
            } else {
                try {
                    mContext.getContentResolver().unregisterContentObserver(this);
                } catch (RuntimeException e) {
                    // Shouldn't happen
                }
            }
        }
    }

    public ClockHeader(Context context) {
        this(context, null);
    }

    public ClockHeader(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

   		String[] ampm = new DateFormatSymbols().getAmPmStrings();
            mAmString = ampm[0].toUpperCase() ;
            mPmString = ampm[1].toUpperCase();
        
        mCalendar = Calendar.getInstance();

        setDateFormat();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        mAttached++;

        /* monitor time ticks, time changed, timezone */
        if (mIntentReceiver == null) {
            mIntentReceiver = new TimeChangedReceiver(this);
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_TIME_TICK);
            filter.addAction(Intent.ACTION_TIME_CHANGED);
            filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
            getContext().registerReceiver(mIntentReceiver, filter);
        }

        /* monitor 12/24-hour display preference */
  /*      if (mFormatChangeObserver == null) {
            mFormatChangeObserver = new FormatChangeObserver(this);
            getContext().getContentResolver().registerContentObserver(
                    Settings.System.CONTENT_URI, true, mFormatChangeObserver);
        }
*/
        updateTime();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        mAttached--;

        if (mIntentReceiver != null) {
            getContext().unregisterReceiver(mIntentReceiver);
        }
       /* if (mFormatChangeObserver != null) {
            getContext().getContentResolver().unregisterContentObserver(
                    mFormatChangeObserver);
        }*/

        mFormatChangeObserver = null;
        mIntentReceiver = null;
    }
	void setShowAmPm(boolean show) {
		if(show)setIsMorning(mMorning);
		else mAmPmChar = null;
	}

	void setIsMorning(boolean isMorning) {
		mMorning = isMorning;
		mAmPmChar = (isMorning ? mAmString : mPmString);
	}
    void updateTime(Calendar c) {
        mCalendar = c;
        updateTime();
    }

    private void updateTime() {
		Date now = new Date();
        mDateChar = (DateFormat.format("EEEE, d MMMM",now));

        mCalendar.setTimeInMillis(System.currentTimeMillis());

        CharSequence newTime = DateFormat.format(mFormat, mCalendar);
        mTimeChar = (newTime);
        setIsMorning(mCalendar.get(Calendar.AM_PM) == 0);
		
		// set to textview
		setText(mTimeChar + " "+mAmPmChar) ; // +"  •  "+mDateChar);
    }

    private void setDateFormat() {
        mFormat = M12; //android.text.format.DateFormat.is24HourFormat(getContext())
       //     ? M24 : M12;
        setShowAmPm(mFormat.equals(M12));
    }
}
