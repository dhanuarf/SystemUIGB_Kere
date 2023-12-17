package com.android.systemui.statusbar;
import com.android.systemui.R;
import android.widget.*;
import android.view.*;
import android.content.*;
import android.util.*;
import android.os.*;
public class BatteryTextView extends ColorTextViewTint{
	public BatteryTextView(Context context) {
        this(context, null);
    }
	private Handler mHandler;
    public BatteryTextView(Context context, AttributeSet attrs) {
        super(context, attrs);

  		mHandler = new Handler();
    }
	private BroadcastReceiver batteryreceiver = new BroadcastReceiver(){

		@Override
		public void onReceive(Context p1, Intent p2)
		{
			final int level = p2.getIntExtra("level",0);
			setText(Integer.toString(level)+"%");
		}
		
		
	};
	private boolean set;
	public void set(){
		if(set)return;
		getContext().registerReceiver(batteryreceiver,new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
		set = true;
	}
	public void unset(){
		if(!set)return;
		getContext().unregisterReceiver(batteryreceiver);
		set= false;
	}
}

