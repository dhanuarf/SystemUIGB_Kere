package com.android.systemui.statusbar;
import com.android.systemui.R;
import android.widget.*;
import android.view.*;
import android.content.*;
import android.util.*;
import android.os.*;
public class BatteryView extends ImageView{
	public BatteryView(Context context) {
        this(context, null);
    }
	private Handler mHandler;
    public BatteryView(Context context, AttributeSet attrs) {
        super(context, attrs);

  		mHandler = new Handler();
		ColorBarTint.addListener(new ColorBarTint.UpdateListener(){
			@Override
			public void onUpdateStatusBarIconColor(int color){
				setColorFilter(color);
				if(color==0xffffffff)setColorFilter(0x00000000);
			}
		});
    }
	private BroadcastReceiver batteryreceiver = new BroadcastReceiver(){

		@Override
		public void onReceive(Context p1, Intent p2)
		{
			final int level = p2.getIntExtra("level",0);
			final boolean plugged = p2.getIntExtra("plugged",0) ==1;
			setImageLevel(level);
			setImageResource(plugged? R.drawable.stat_sys_battery_charge : R.drawable.stat_sys_battery);
		}


	};
	private boolean set;
	public void set(){
		if(set)return;
		//IntentFilter ifil= new IntentFilter();
		//ifil.addAction(Intent.ACTION_BATTERY_CHANGED);
		getContext().registerReceiver(batteryreceiver,new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
		set = true;
	}
	public void unset(){
		if(!set)return;
		getContext().unregisterReceiver(batteryreceiver);
		set=false;
	}
}

