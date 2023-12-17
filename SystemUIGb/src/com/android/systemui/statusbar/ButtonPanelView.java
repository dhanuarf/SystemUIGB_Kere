package com.android.systemui.statusbar;

import android.widget.*;
import android.content.*;
import android.view.*;
import android.util.*;
import android.provider.*;
import android.database.*;
import android.os.*;
import kere.settings.*;
import com.android.internal.policy.impl.*;
import com.android.systemui.*;
import support.animator.animation.*;
import kere.util.*;
import android.graphics.*;
import support.animator.view.*;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import java.lang.reflect.*;

public class ButtonPanelView extends LinearLayout
{
	private static final String back = "back";
	private static final String home = "home";
	private static final String recent = "recent";
	private static final String menu = "menu";
	private static final String ss = "ss";
	private static final String volup = "volup";
	private static final String voldown = "voldown";
	private static final String powermenu = "powermenu";

	private static final String separator = "_-";
	private static final String defaultbuttons = back
	+ separator + home
	+ separator + recent
	+ separator + powermenu;
	

	private class SettingsObserver extends ContentObserver
	{
        SettingsObserver(Handler handler)
		{
            super(handler);
        }

        void observe()
		{
            ContentResolver resolver = getContext().getContentResolver();
            resolver.registerContentObserver(Settings.System.getUriFor(
												 Setelan.SIDE_PANEL_BUTTONS), false, this);
			resolver.registerContentObserver(Settings.System.getUriFor(
												 Setelan.SIDE_PANEL_ENABLE), false, this);
			resolver.registerContentObserver(Settings.System.getUriFor(
												 Setelan.SIDE_PANEL_ALPHA_VALUE), false, this);
			resolver.registerContentObserver(Settings.System.getUriFor(
												 Setelan.SIDE_PANEL_DELAY_DISSMISS_TIME), false, this);
			
			
		}


        @Override public void onChange(boolean selfChange)
		{
            updateSettings();

        }
    }
	private boolean enable = false;
	private long autohidetime = 4000;
	private long delaysstime = 300;
	private boolean hidepanelwhenss = true;
	private int alphavalue = 0xff;
	private float alphavaluefloat;

	private Context context;
	private Handler mHandler ;
	private LinearLayout.LayoutParams childparams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT , 22);
	private LinearLayout.LayoutParams layoutparams = new LinearLayout.LayoutParams(
		ViewGroup.LayoutParams.FILL_PARENT , ViewGroup.LayoutParams.FILL_PARENT);
	private LinearLayout layout;
	public ButtonPanelView(Context c)
	{
		this(c, null);
	}
	public ButtonPanelView(Context c, AttributeSet as)
	{
		super(c, as);

		context = c;
		layout = new LinearLayout(c);
		layout.setOrientation(VERTICAL);

		SettingsObserver so = new SettingsObserver(new Handler());
		so.observe();
		updateSettings();
		
		layout.setGravity(Gravity.CENTER);

		mHandler = new Handler();
		ColorBarTint.addListener(new ColorBarTint.UpdateListener(){
				@Override
				public void onUpdateStatusBarColor(final int p, final int n)
				{
					if (mIsShown && enable)mHandler.post(new Runnable(){

								@Override
								public void run()
								{
									final boolean transparent = p == 0x00000000;
									final ValueAnimator colorAnim = ObjectAnimator .ofInt(layout , "backgroundColor" , transparent ? 0xff000000: p, n);
									colorAnim.setDuration(180);
									colorAnim.setEvaluator(new ArgbEvaluator());
									colorAnim.start();

									for (int i = 0; i < layout.getChildCount();i++)
									{
										((ImageView)layout.getChildAt(i)).setColorFilter(ColorUtils.isBrightColor(n) ? 0xff222222: 0xffffffff);
									}
								}


							});
				}
			});
		addView(layout, layoutparams);
		setVisibility(GONE);
	}
	private void updateSettings()
	{
		final ContentResolver cr = context.getContentResolver();
		enable = Settings.System.getInt(cr, Setelan.SIDE_PANEL_ENABLE,0)==1;
		if (layout == null || !enable)return;
		
		autohidetime = Settings.System.getInt(cr, Setelan.SIDE_PANEL_DELAY_DISSMISS_TIME,5000);
		
		alphavalue = Settings.System.getInt(cr, Setelan.SIDE_PANEL_ALPHA_VALUE,0xff);
		alphavaluefloat =(float) alphavalue/0xff;
		
		layout.removeAllViews();
		String buttons = Settings.System.getString(getContext().getContentResolver(), Setelan.SIDE_PANEL_BUTTONS);
		if (buttons == null)buttons = defaultbuttons;
		String[] button = buttons.split(separator);
		for (int i = 0; i < button.length; i++)
		{
			final ButtonView v = new ButtonView(getContext());
			v.setName(button[i]);
			v.setImageResource(getResId(button[i]));
			v.setOnClickListener(mListener);
			childparams.topMargin = i > 0 ? 6: -12;
			v.setScaleType(ImageView.ScaleType.CENTER);
			layout.addView(v, childparams);
		}

	}
	private boolean mIsShown = false;
	public void show()
	{
		if (isShown())
		{
			//hide();
			return;
		}


		setVisibility(VISIBLE);
		ViewHelper.setAlpha(layout, 0);
		mIsShown = true;

		final int c = ColorBarTint.getCurrentBarColor();
		layout.setBackgroundColor(c);
		final ValueAnimator bganim = ValueAnimator .ofFloat(0, alphavaluefloat);
		bganim.setDuration(250);
		bganim.setInterpolator(new DecelerateInterpolator());
		bganim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(){

				@Override
				public void onAnimationUpdate(ValueAnimator p1)
				{
					ViewHelper.setAlpha(layout, (float)p1.getAnimatedValue());
					ViewHelper.setTranslationX(layout, (1f - p1.getAnimatedFraction()) * StatusBarService.BUTTON_PANEL_WIDTH);
				}


			});
		bganim.start();

		for (int i = 0; i < layout.getChildCount();i++)
		{
			((ImageView)layout.getChildAt(i)).setColorFilter(ColorUtils.isBrightColor(ColorBarTint.getCurrentBarColor()) ? 0xff222222: 0xffffffff);
			final ObjectAnimator animatorbuttons = ObjectAnimator.ofFloat(layout.getChildAt(i), "scaleX", 0, 1);
			animatorbuttons.setDuration(200);
			animatorbuttons.setStartDelay(180 + i * 100);
			animatorbuttons.start();
		}
		// autohide
		delayDismiss();
	}
	private void delayDismiss(){
		if(autohidetime == -123)return;
		mHandler.postDelayed(delayHide, autohidetime);
	}
	public void hide()
	{
		if (!mIsShown)return;
		mHandler.removeCallbacks(delayHide);

		final ValueAnimator bganim = ValueAnimator .ofFloat(0, alphavaluefloat);
		bganim.setDuration(250);
		bganim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(){

				@Override
				public void onAnimationUpdate(ValueAnimator p1)
				{
					ViewHelper.setTranslationX(layout, p1.getAnimatedFraction() * StatusBarService.BUTTON_PANEL_WIDTH);
					ViewHelper.setAlpha(layout, alphavaluefloat - (float)p1.getAnimatedValue());
				}


			});
		bganim.addListener(new Animator.AnimatorListener(){

				@Override
				public void onAnimationCancel(Animator p1)
				{
					// TODO: Implement this method
				}

				@Override
				public void onAnimationEnd(Animator p1)
				{
					setVisibility(GONE);
				}

				@Override
				public void onAnimationRepeat(Animator p1)
				{
					// TODO: Implement this method
				}

				@Override
				public void onAnimationStart(Animator p1)
				{
					// TODO: Implement this method
				}


			});
		bganim.start();
		mIsShown = false;
		
	}

	@Override
	protected void onAttachedToWindow()
	{
		// TODO: Implement this method
		super.onAttachedToWindow();
		//log("onAttach");
	}

	@Override
	protected void onDetachedFromWindow()
	{
		// TODO: Implement this method
		super.onDetachedFromWindow();
		//log("onDetach");
	}
	private void log(String s){
		Log.d(ButtonPanelView.class.getSimpleName(),s);
	}
	void d(Class s){
		s.getSimpleName();
	}
	public boolean getEnable(){
		return enable;
	}
	private OnClickListener mListener = new OnClickListener(){

		@Override
		public void onClick(View p1)
		{
			final ButtonView v = (ButtonView)p1;
			switch (v.getName())
			{
				case back:
					simulateKeypress(KeyEvent.KEYCODE_BACK);
					break;
				case home:
					simulateKeypress(KeyEvent.KEYCODE_HOME);
					break;
				case menu:
					simulateKeypress(KeyEvent.KEYCODE_MENU);
					break;
				case recent:
					simulateLongKeypress(KeyEvent.KEYCODE_HOME);
					break;
				case ss:
					screenshot();
					break;
				case volup:
					simulateKeypress(KeyEvent.KEYCODE_VOLUME_UP);
					break;
				case voldown:
					simulateKeypress(KeyEvent.KEYCODE_VOLUME_DOWN);
					break;
				case powermenu:
					simulateLongKeypress(KeyEvent.KEYCODE_POWER);
					break;

			}
			// TODO: Implement this method
		}


	};
	private int getResId(String buttonName)
	{
		switch (buttonName)
		{
			case back:
				return R.drawable.btnpnl_back;
			case home:
				return R.drawable.btnpnl_home;
			case menu:
				return R.drawable.btnpnl_menu;
			case recent:
				return R.drawable.btnpnl_recent;
			case ss:
				return R.drawable.btnpnl_ss;
			case volup:
				return R.drawable.btnpnl_volup;
			case voldown:
				return R.drawable.btnpnl_voldown;
			case powermenu:
				return R.drawable.btnpnl_powermenu;
			default:
			    return 0;
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent p1)
	{
		// TODO: Implement this method
		return super.onTouchEvent(p1);
	}

	private Runnable delayHide = new Runnable(){

		@Override
		public void run()
		{
			hide();
		}


	};
	private Runnable delaySs = new Runnable(){

		@Override
		public void run()
		{
			final Intent i = new Intent("com.sec.android.app.screencapture.capture");
			i.addCategory(Intent.CATEGORY_DEFAULT);
			getContext().startService(i);
		}


	};

	private void screenshot()
	{
		mHandler.postDelayed(delaySs, delaysstime);
		if (hidepanelwhenss)hide();
	}
	private void simulateKeypress(final int keyCode)
	{
        new Thread(new KeyEventInjector(keyCode , false)).start();
    }
	private void simulateLongKeypress(final int keyCode)
	{
        new Thread(new KeyEventInjector(keyCode, true)).start();
    }
    private class KeyEventInjector implements Runnable
	{
        private int keyCode;
		private boolean longpress;

		private static final String TAG = "logkeypress";
        KeyEventInjector(final int keyCode, final boolean longpress)
		{
            this.keyCode = keyCode;
			this.longpress = longpress;
        }

        public void run()
		{
            try
			{
                if (!(IWindowManager.Stub.asInterface(ServiceManager.getService("window")))
					.injectKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, keyCode), true))
				{
					Slog.w(TAG, "Key down event not injected");
					return;
				}
			}
			catch (RemoteException ex)
			{
				Slog.w(TAG, "Error injecting key event", ex);
			}
			postDelayed(new Runnable(){

					@Override
					public void run()
					{
						try
						{
							if (!(IWindowManager.Stub.asInterface(ServiceManager.getService("window")))
								.injectKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, keyCode), true))
							{
								Slog.w(TAG, "Key up event not injected");
							}
						}
						catch (RemoteException e)
						{}
						// TODO: Implement this method
					}


				}, longpress ? 1000: 20);
        }
    }
	private class ButtonView extends ImageView
	{
		private String mName;
		public ButtonView(Context c)
		{
			super(c);
		}
		public void setName(String name)
		{
			mName = name;
		}
		public String getName()
		{
			return mName;
		}

		@Override
		public boolean onTouchEvent(MotionEvent p1)
		{
			switch (p1.getAction())
			{
				case MotionEvent.ACTION_DOWN:
					setBackgroundColor(ColorUtils.isBrightColor(ColorBarTint.getCurrentBarColor()) ? 0x88222222: 0x88ffffff);
					// stop the autohide 
					mHandler.removeCallbacks(delayHide);
					break;
				case MotionEvent.ACTION_UP:
				case MotionEvent.ACTION_CANCEL:
					setBackgroundColor(0x00000000);
					// autohide after no activity
					delayDismiss();
					break;
			}
			return super.onTouchEvent(p1);
		}

	}
} 
