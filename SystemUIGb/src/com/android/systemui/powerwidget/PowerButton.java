package com.android.systemui.powerwidget;

import com.android.systemui.R;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.PorterDuff.Mode;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.provider.Settings;
import android.view.View;
import android.provider.Settings;
import java.util.ArrayList;
import java.util.List;
import android.view.View.*;
import android.content.*;
import android.graphics.*;
import android.widget.*;
public abstract class PowerButton
{
    public static final String TAG = "PowerButton";
    public static final int STATE_ENABLED = 1;
    public static final int STATE_DISABLED = 2;
    public static final int STATE_TURNING_ON = 3;
    public static final int STATE_TURNING_OFF = 4;
    public static final int STATE_INTERMEDIATE = 5;
    public static final int STATE_UNKNOWN = 6;
    public static final String BUTTON_WIFI = "toggleWifi";
    public static final String BUTTON_GPS = "toggleGPS";
    public static final String BUTTON_BLUETOOTH = "toggleBluetooth";
    public static final String BUTTON_BRIGHTNESS = "toggleBrightness";
    public static final String BUTTON_SOUND = "toggleSound";
    public static final String BUTTON_SYNC = "toggleSync";
    public static final String BUTTON_WIFIAP = "toggleWifiAp";
    public static final String BUTTON_SCREENTIMEOUT = "toggleScreenTimeout";
    public static final String BUTTON_MOBILEDATA = "toggleMobileData";
    public static final String BUTTON_LOCKSCREEN = "toggleLockScreen";
    public static final String BUTTON_NETWORKMODE = "toggleNetworkMode";
    public static final String BUTTON_AUTOROTATE = "toggleAutoRotate";
    public static final String BUTTON_AIRPLANE = "toggleAirplane";
    public static final String BUTTON_FLASHLIGHT = "toggleFlashlight";
    public static final String BUTTON_SLEEP = "toggleSleepMode";
	public static final String BUTTON_NIGHTMODE = "toggleNightMode";
    public static final String BUTTON_DSB = "toggleTint";
	public static final String BUTTON_SETTINGS = "toggleSettings";
    public static final String BUTTON_PROFILE = "toggleProfilePic";
	

	public static final String BUTTON_MORE = "toggleMore";	
    public static final String BUTTON_UNKNOWN = "unknown";
    private static final Mode MASK_MODE = Mode.SCREEN;
    protected int mIcon;
    protected int mState;
    protected View mView;
    protected String mType = BUTTON_UNKNOWN;
    private ImageView mIconView;
	// private ImageView mIndicatorView;
	private TextView mText;
    private View.OnClickListener mExternalClickListener;
    private View.OnLongClickListener mExternalLongClickListener;

	private boolean mDisableClick;

    // we use this to ensure we update our views on the UI thread
    private Handler mViewUpdateHandler = new Handler() {
        @Override
        public void handleMessage(Message msg)
		{
            if (mIconView != null)
			{
                mIconView.setImageResource(mIcon);
            }
			
            if (mView != null)
			{
                Context context;
				context = mIconView.getContext();
//				context= mIndicatorView.getContext();
				ContentResolver cr = context.getContentResolver();

				int colorMaskBase = 0xFFFFFFFF;
				int colorMask;

				switch (mState)
				{
					case STATE_ENABLED:
						colorMask = (colorMaskBase & 0x00FFFFFF) | 0xA0000000;
						break;
					case STATE_DISABLED:
						colorMask = (colorMaskBase & 0x00FFFFFF) | 0x55000000;
						break;
					default:
						colorMask = (colorMaskBase & 0x00FFFFFF) | 0x77000000;
						break;
				}
//				mIconView.setColorFilter(colorMask);
//				mIndicatorView.setColorFilter(colorMask);

//				boolean visible = Settings.System.getInt(cr,
//                        Settings.System.EXPANDED_HIDE_INDICATOR, 0) != 1;
//                 mIndicatorView.setVisibility(visible ? View.VISIBLE : View.GONE);
//                mIndicatorView.setImageDrawable(context.getResources().getDrawable(
//                            R.drawable.stat_bgon_custom));

            }
        }
    };
    protected abstract void updateState(Context context);
    protected abstract void toggleState(Context context);
    protected abstract boolean handleLongClick(Context context);
    protected void update(Context context)
	{
        updateState(context);
        updateView();
    }
    protected void onReceive(Context context, Intent intent)
	{
        // do nothing as a standard, override this if the button needs to respond
        // to broadcast events from the StatusBarService broadcast receiver
    }
    protected void onChangeUri(ContentResolver resolver, Uri uri)
	{
        // do nothing as a standard, override this if the button needs to respond
        // to a changed setting
    }
    protected IntentFilter getBroadcastIntentFilter()
	{
		IntentFilter itf= new IntentFilter();

        return itf;
    }
    protected List<Uri> getObservedUris()
	{
        return new ArrayList<Uri>();
    }
    protected void setupButton(View view)
	{
        mView = view;
        if (mView != null)
		{
            mView.setTag(mType);
            mView.setOnClickListener(mClickListener);
            mView.setOnLongClickListener(mLongClickListener);
            mIconView = (ImageView) mView.findViewById(R.id.ic_qs_state);
			mText = (TextView) mView.findViewById(R.id.qs_name);
			mText.setText(getButtonTitleId(mType));
//            mIndicatorView = (ImageView) mView.findViewById(R.id.power_widget_button_indic);

        }
		else
		{
            mIconView = null;
			mText = null;
//            mIndicatorView = null;
        }
    }
	public void setTextVisible(boolean visible)
	{
		mText.setVisibility(visible ? View.VISIBLE : View.GONE);
	}
    protected void updateView()
	{
        mViewUpdateHandler.sendEmptyMessage(0);
    }
	protected int getButtonTitleId(String key)
	{
		PowerWidgetUtil.ButtonInfo b = PowerWidgetUtil.BUTTONS.get(key);
		return b.getTitleResId();
	}
    private View.OnClickListener mClickListener = new View.OnClickListener() {
        public void onClick(View v)
		{
			if (!mDisableClick)
			{
				toggleState(v.getContext());
				update(v.getContext());
				if (mExternalClickListener != null)
				{
					mExternalClickListener.onClick(v);
				}
			}
        }
    };
    private View.OnLongClickListener mLongClickListener = new View.OnLongClickListener() {
        public boolean onLongClick(View v)
		{
			if (!mDisableClick)
			{
				boolean result = handleLongClick(v.getContext());

				if (result && mExternalLongClickListener != null)
				{
					mExternalLongClickListener.onLongClick(v);
				}
				return result;
			}
            return false;
		}
    };
	public void setTintColor(int color){
		if(mIconView != null){
			mIconView.setColorFilter(color);
		}
		if(mText != null){
			mText.setTextColor(color);
		}
		if(mView != null){
			((PowerButtonView)mView).setColor(color);
		}
	}
    void setExternalClickListener(View.OnClickListener listener)
	{
        mExternalClickListener = listener;
    }
    void setExternalLongClickListener(View.OnLongClickListener listener)
	{
        mExternalLongClickListener = listener;
    }

	public void disableClick(boolean disable)
	{
		mDisableClick = disable;
	}
}
