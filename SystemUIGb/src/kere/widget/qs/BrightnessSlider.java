package kere.widget.qs;

import android.content.*;
import android.util.*;
import android.widget.*;
import android.provider.*;
import android.provider.Settings.*;
import android.os.*;
import android.graphics.drawable.*;
import android.graphics.*;
import android.database.*;

public class BrightnessSlider extends RelativeLayout implements SeekBar.OnSeekBarChangeListener
{
	int kecerahan;
	Drawable thumb;
	ContentResolver cr;

	private static final int MINIMUM_BACKLIGHT = android.os.Power.BRIGHTNESS_DIM;
    private static final int MAXIMUM_BACKLIGHT = android.os.Power.BRIGHTNESS_ON;

	private SeekBar slider;
	private ImageView ic;
	private TextView percentage;
	
	private class SettingsObserver extends ContentObserver{
		public SettingsObserver(Handler h){
			super(h);
		}
		void observe(){
			ContentResolver resolver = mContext.getContentResolver();
            resolver.registerContentObserver(Settings.System.getUriFor(
												 Settings.System.SCREEN_BRIGHTNESS), false, this);
          }

        @Override public void onChange(boolean selfChange) {
            updateSettings();
        }

	}
	private SettingsObserver sObserver;
	public BrightnessSlider(Context c, AttributeSet as)
	{
		super(c, as);
		sObserver= new SettingsObserver(new Handler());
	}

	@Override
	protected void onFinishInflate()
	{
		// TODO: Implement this method
		super.onFinishInflate();

		slider = (SeekBar)getChildAt(0);
		ic = (ImageView)getChildAt(1);
		percentage = (TextView)getChildAt(2);

		slider.setMax(MAXIMUM_BACKLIGHT);
		thumb = getResources().getDrawable(com.android.internal.R.drawable.seek_thumb);
		slider.setThumb(thumb);
		cr = getContext().getContentResolver();
		kecerahan = Settings.System.getInt(cr, Settings.System.SCREEN_BRIGHTNESS, MINIMUM_BACKLIGHT);
		slider.setProgress(kecerahan);
		slider.setOnSeekBarChangeListener(this);
		
		sObserver.observe();
	}
	@Override
	public void onProgressChanged(SeekBar p1, int p2, boolean p3)
	{
		kecerahan = p2;
		
		float perc = (kecerahan /(float)MAXIMUM_BACKLIGHT)*100;
		//float f = perc / 88 * 100;
		percentage.setText((int)perc +"%");
		
		setBrightness(kecerahan);

		// TODO: Implement this method
	}

	@Override
	public void onStartTrackingTouch(SeekBar p1)
	{
		// TODO: Implement this method
	}

	@Override
	public void onStopTrackingTouch(SeekBar p1)
	{
		System.putInt(cr, System.SCREEN_BRIGHTNESS, kecerahan);

		// TODO: Implement this method
	}

	private void setBrightness(int brightness)
	{
        try
		{
            IPowerManager power = IPowerManager.Stub.asInterface(
				ServiceManager.getService("power"));
            if (power != null)
			{
                power.setBacklightBrightness(brightness);
            }
        }
		catch (RemoteException doe)
		{

        }        
    }

	public void updateColor(int kl)
	{
		ic.setColorFilter(kl);
		thumb.setColorFilter(kl & 0xddffffff, PorterDuff.Mode.SRC_ATOP);
		slider.getProgressDrawable().setColorFilter(kl, PorterDuff.Mode.SRC_ATOP);
		percentage.setTextColor(kl);
	}
	private void updateSettings(){
		kecerahan = Settings.System.getInt(cr, Settings.System.SCREEN_BRIGHTNESS, MINIMUM_BACKLIGHT);
		slider.setProgress(kecerahan);
	}
}
