package com.android.systemui.statusbar;

import android.content.*;
import android.database.*;
import android.os.*;
import android.provider.*;
import android.util.*;
import android.view.*;
import android.widget.*;
import com.android.systemui.*;
import com.android.systemui.observablescrollview.*;
import com.android.systemui.powerwidget.*;
import kere.util.*;
import kere.settings.*;
import kere.widget.*;
import support.animator.view.*;
import com.b16h22.statusbar.*;
import kere.widget.qs.*;
import support.animator.animation.*;
import android.graphics.*;
import java.io.*;
import android.net.*;
import android.graphics.drawable.*;
import com.android.systemui.statusbar.phone.BarBackgroundUpdaterNative;
import com.sec.android.app.screencapture.ScreenCapture;

/*   View that control all the moving view
 *   to create Material expanded
 *
 *   created by Dhanu Dwi Arfendi
 *
 *   some code are taken from stackoverflow , github and 
 *   Thanks to them!
 */

public class ExpandedPanelView extends LinearLayout
implements PanelContainerView.OnTranslationListener
{

	private int mBaseColor = 0;
	private final static int DEF_COLOR =0xff2f4449;

	ContentResolver cr ;

	private ColorAlbumArt mColorAlbumArt;
	private ColorAlbumArt.ColorAlbumArtUpdater mAlbartUpdater = new ColorAlbumArt.ColorAlbumArtUpdater(){

		@Override
		public void onAlbumartUpdate()
		{
			updateViews();

		}
	};

	private int mCurrentAlbumArtColor, mCurrentColorBg, mCurrentColorWidget;
	private int mBSliderHeight;
	private boolean mBSliderOpen;

	StatusBarService mService ;
	// views header
	ImageView mSettingsBtn, mMoreButton;
	TextView mClockHeader, mDate , mCarrierLabel;
	ProfilePicture mProfilePic;
	View mHeaderView;
	BrightnessSlider mBSlider;
	View mGanjelBSlider;
	ViewGroup.LayoutParams mLpGanjel;
	// views exp
	View mPWParent;
	View mPWContainer;
	QuickPowerWidget mQPWidget;
	PowerWidget mPWidget;

	View[] mQButton = null ;
	private static final float MOVEMENT_CLOCK_Y=14;
	private static final float MOVEMENT_CLOCK_X=5;

	private static final float MOVEMENT_DATE_X=4;
	private static final float MOVEMENT_SETTINGS_BTN_X=18;
	private static final float MOVEMENT_PROFILE_PIC_X=30;
	private static final float MOVEMENT_MORE_BTN_Y=7;

	// expanded style ==> 0=default, 1=dinamiktint, 2=albart, 3 custom
	private int mExpandedStyle;
	private static final int EXP_STYLE_DEFAULT=0;
	private static final int EXP_STYLE_ALBUMART=1;
	private static final int EXP_STYLE_TINT=2;
	private static final int EXP_STYLE_CUSTOM=3;


	private PanelContainerView mPanelContainerView;
	private ObservableScrollView mScrollView;
	private SettingsObserver mObserver;
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
												 Setelan.EXPANDED_STYLE), false, this);

			resolver.registerContentObserver(Settings.System.getUriFor(
												 Setelan.EXPANDED_CUSTOM_COLOR), false, this);

			resolver.registerContentObserver(Settings.System.getUriFor(
												 Setelan.EXPANDED_COLOR_MODE), false, this);


		}


        @Override public void onChange(boolean selfChange)
		{
            updateSettings();
        }
    }

	public ExpandedPanelView(Context c)
	{
		this(c, null);
	}
	private Context mContext;
	public ExpandedPanelView(Context c, AttributeSet as)
	{
		super(c, as);
		mContext = c;
		mObserver = new SettingsObserver(new Handler());
		mColorAlbumArt = new ColorAlbumArt(c);
		mColorAlbumArt.setUpdater(mAlbartUpdater);
		cr = getContext().getContentResolver();

	}
	private void setInitialViewPos()
	{
		translate(0f);
		setTranslationY(mDate, MOVEMENT_CLOCK_Y - 2);
	}
	@Override
	public void onTranslation(float fraction)
	{
		translate(fraction);
		mQPWidget.setQsState(fraction > 0.00f);

	}

	// boolean ben ora ngeloop
	private boolean openedRightSide = false;
	// opening, f = 0-1
	// closing, f = 1-0
	private void translate(float f)
	{
		setTranslationY(mClockHeader, MOVEMENT_CLOCK_Y * f);
		setTranslationX(mClockHeader, MOVEMENT_CLOCK_X * f);
		setScaleXY(mClockHeader, 1f + (f * 0.1f));

		setTranslationY(mMoreButton, MOVEMENT_MORE_BTN_Y - MOVEMENT_MORE_BTN_Y * f);

		setAlpha(mCarrierLabel, 1f - f);

		setTranslationX(mDate, MOVEMENT_DATE_X * f);

		setAlpha(mDate, f);

		setTranslationX(mSettingsBtn, MOVEMENT_SETTINGS_BTN_X - MOVEMENT_SETTINGS_BTN_X * f);
		setAlpha(mSettingsBtn, f);
		ViewHelper.setRotation(mSettingsBtn, 90 * (1f - f));

		setTranslationX(mProfilePic, MOVEMENT_PROFILE_PIC_X - MOVEMENT_PROFILE_PIC_X * f);
		setAlpha(mProfilePic, f);
		setScaleXY(mProfilePic, 1 + (f * 0.4f));

		// special purposes dissapear when qs closes
		if (f <= 0f)
		{
			mProfilePic.setVisibility(View.GONE);
			mSettingsBtn.setVisibility(View.GONE);
			mSettingsBtn.setClickable(false);
			mProfilePic.setClickable(false);
			for (int i =0;i < mQPWidget.getButtonCount();i++)
			{
				if (mQButton[i] != null)
					mQButton[i].setVisibility(View.VISIBLE);
			}
			mQPWidget.requestDisallowClick(false);
			openedRightSide = false;
		}
		else
		{
			if (!openedRightSide)
			{
				mProfilePic.setVisibility(View.VISIBLE);
				mSettingsBtn.setVisibility(View.VISIBLE);
				mProfilePic.setClickable(true);
				mSettingsBtn.setClickable(true);
				mQPWidget.requestDisallowClick(true);
				for (int i =0;i < mQPWidget.getButtonCount();i++)
				{
					if (mQButton[i] != null)mQButton[i].setVisibility(View.GONE);

				}
				openedRightSide = true;
			}
		}
	}

	private void setTranslationY(View v, float f)
	{
		ViewHelper.setTranslationY(v, f);

	}
	private void setTranslationX(View v, float f)
	{
		ViewHelper.setTranslationX(v, f);
	}
	private void setAlpha(View v, float f)
	{
		ViewHelper.setAlpha(v, f);
	}

	private void setScaleXY(View v, float f)
	{
		ViewHelper.setScaleX(v, f);
		ViewHelper.setScaleY(v, f);
	}

	private float getReversedAlpha(float alpha)
	{
		float reversedAlpha= 1f - alpha;
		// kurang kat 0*/
		return reversedAlpha;
	}

	private void setReverseAlpha(View view, float alpha)
	{
		ViewHelper.setAlpha(view, getReversedAlpha(alpha));
	}
	@Override
	protected void onFinishInflate()
	{
		// TODO: Implement this method
		super.onFinishInflate();

		mPWidget = (PowerWidget) findViewById(R.id.pw_view);
		mClockHeader = (TextView)findViewById(R.id.clockHeader);
		mDate = (TextView) findViewById(R.id.hdr_tgl);
		mSettingsBtn = (ImageView)findViewById(R.id.sleting);
		mMoreButton = (ImageView)findViewById(R.id.qs_more_button);
		mProfilePic = (ProfilePicture) findViewById(R.id.profile_pic);
		mCarrierLabel = (TextView)findViewById(R.id.carrier_label);
		mHeaderView = (RelativeLayout)findViewById(R.id.exp_header);
		mPanelContainerView = (PanelContainerView)findViewById(R.id.toggleContainer);
		mPWContainer = findViewById(R.id.qs_container);
		mPWParent = findViewById(R.id.togel_view_parent);
		mPWidget = (PowerWidget)findViewById(R.id.pw_view);
		mQPWidget = (QuickPowerWidget)findViewById(R.id.qpw_view);
		mBSlider = (BrightnessSlider)findViewById(R.id.brightness_slider);
		mGanjelBSlider = findViewById(R.id.ganjelViewBSlider);
		mLpGanjel = mGanjelBSlider.getLayoutParams();

		// initial state is gone
		mBSlider.setVisibility(View.GONE);

		mBSliderHeight = (getContext().getResources().getDimensionPixelSize(R.dimen.bslider_height) - 1);

		mPanelContainerView.setTranslationListener(this);

		mPWidget.setGlobalButtonOnLongClickListener(onQsLongClickListener);

		mQPWidget.setExpandedPanel(this);
		mQPWidget.setGlobalButtonOnLongClickListener(onQsLongClickListener);

		mMoreButton.setOnClickListener(onMoreButtonClickListener);
		mMoreButton.setOnLongClickListener(onMoreButtonLongClickListener);

		updateQPW();

		mObserver.observe();

		setInitialViewPos();

		postDelayed(r, 2500);

	}
	int mStatusBarHeight;
	int i = 0;
	Runnable r = new Runnable(){

		@Override
		public void run()
		{
			updateSettings();
		}


	};

	private void updateSettings()
	{
		// 0= normal , 1=dark

		mExpandedStyle = Settings.System.getInt(cr, Setelan.EXPANDED_STYLE, 0);
		boolean dsbEnable = Settings.System.getInt(cr, Setelan.TINT_ENABLE, 0) == 1;
		if (mExpandedStyle == EXP_STYLE_TINT)
		{
			/*if (dsbEnable)
			 ColorBarTint.set(getContext());
			 */
			//else mExpandedStyle = EXP_STYLE_DEFAULT;
		}
		else
		{
			// if in albumart color style
			if (mExpandedStyle == EXP_STYLE_ALBUMART)
			{
				mColorAlbumArt.set();
			}
			else
			{
				mColorAlbumArt.unset();
			}

			// unset dsb because it's not used
			//ColorBarTint.unset();

		}
		updateViews();
	}
	// we can call it from expanded visible or somewhere else
	public void updateTintDsb()
	{
		if (mExpandedStyle == EXP_STYLE_TINT)
			updateViews();
	}
	// color mode
	private int modeWidgetBg = 0;
	private int modeBaseBg = 1;
	private int modeWidgetHBg = 2;
	private int modeBaseHBg = 3;

	private void updateViews()
	{
		Bitmap albcolor = null;
		// define baseColor for each mode
		if (mExpandedStyle == EXP_STYLE_DEFAULT)mBaseColor = DEF_COLOR;
		else if (mExpandedStyle == EXP_STYLE_CUSTOM)mBaseColor = Settings.System.getInt(cr, Setelan.EXPANDED_CUSTOM_COLOR, 0xffffffff);
		else if (mExpandedStyle == EXP_STYLE_TINT)mBaseColor = ColorBarTint.getCurrentBarColor();
		else if (mExpandedStyle == EXP_STYLE_ALBUMART)
		{
			final Bitmap defAlb=((BitmapDrawable)getResources().getDrawable(R.drawable.def_albart)).getBitmap();
			final Bitmap alb= mColorAlbumArt.getCurentAlbBitmap();

			//Drawable d;//=getResources().getDrawable(R.drawable.bg_musik);
			if (alb != null)
			{

				albcolor = alb;
			}
			else
			{
				albcolor = defAlb;
			}
			mBaseColor = kere.util.ColorUtils.getDominantColor(albcolor);


		}

	    int baseColor = mBaseColor;
		//default for albumart & tint
		if (baseColor == 0)baseColor = 0xff111111;

		boolean isAlbart = mExpandedStyle == EXP_STYLE_ALBUMART;

		boolean isBright= ColorUtils.isBrightColor(baseColor, 100);

		int monoColor= isBright ? 0xff111111 : 0xffffffff;
		boolean isNormalHeader = true ; // expHeaderStyle==0;
		int widgetColor = isNormalHeader ? monoColor : 0xffffffff;

		int colorMode = Settings.System.getInt(cr, Setelan.EXPANDED_COLOR_MODE, modeWidgetBg); 
		int colorAlbArt = 0;
		//--
		//header
		if (colorMode == modeBaseBg || colorMode == modeBaseHBg)
		{
			if (mExpandedStyle != EXP_STYLE_ALBUMART) mHeaderView.setBackgroundColor(baseColor);
			colorAlbArt = baseColor;
			mClockHeader.setTextColor(widgetColor);
			mCarrierLabel.setTextColor(widgetColor);
			mMoreButton.setColorFilter(widgetColor);
			mDate.setTextColor(widgetColor);
			mSettingsBtn.setColorFilter(widgetColor);
			mQPWidget.setTintColor(widgetColor);
			mCurrentColorBg = baseColor;
			mCurrentColorWidget = widgetColor;

		}
		else
		{
			if (mExpandedStyle != EXP_STYLE_ALBUMART) mHeaderView.setBackgroundColor(widgetColor);
			colorAlbArt = widgetColor;
			mClockHeader.setTextColor(baseColor);
			mCarrierLabel.setTextColor(baseColor);
			mMoreButton.setColorFilter(baseColor);
			mDate.setTextColor(baseColor);
			mSettingsBtn.setColorFilter(baseColor);
			mQPWidget.setTintColor(baseColor);
			mCurrentColorBg = widgetColor;
			mCurrentColorWidget = baseColor;
		}

		//processing albart
		if (mExpandedStyle == EXP_STYLE_ALBUMART)
		{
			if (getWidth() != 0)
			{
				albcolor = kere.util.BitmapUtils.centerCrop(albcolor, getWidth(), mHeaderView.getHeight());
				final Bitmap blurred =  albcolor.copy(Bitmap.Config.ARGB_8888, true);
				kere.util.BitmapUtils.bitmapBlur(albcolor, blurred, 4);
				final Drawable d = new BitmapDrawable(blurred);
				final boolean b = ColorUtils.isBrightColor(colorAlbArt);
				d.setColorFilter(colorAlbArt & (b ? 0xaaffffff : 0x66ffffff), PorterDuff.Mode.SRC_ATOP);
				mHeaderView.setBackgroundDrawable(d);
			}
			else 
				mHeaderView.setBackgroundColor(colorAlbArt);
			if (mService != null) mService.setTintCloseView();

		}
		//-----------
		//togel
		if (colorMode == modeBaseBg || colorMode == modeWidgetHBg)
		{
			mPWParent.setBackgroundColor(baseColor);
			mPWidget.setTintColor(widgetColor);
			mBSlider.updateColor(widgetColor);
		}

		else
		{
			mPWParent.setBackgroundColor(widgetColor);
			mPWidget.setTintColor(baseColor);
			mBSlider.updateColor(baseColor);

		}

	}

	private View.OnLongClickListener onQsLongClickListener = new View.OnLongClickListener(){

		@Override
		public boolean onLongClick(View p1)
		{
			mService.delayedAnimateCollapse(true);

			return true;
		}
	};
	private View.OnLongClickListener onMoreButtonLongClickListener = new View.OnLongClickListener(){

		@Override
		public boolean onLongClick(View p1)
		{
			if (mQPWidget.getQsState())
				mService.delayedAnimateCollapse(true);
			return false;
		}
	};
	private View.OnClickListener onMoreButtonClickListener = new View.OnClickListener(){

		@Override
		public void onClick(View p1)
		{
			if (mBSliderOpen)
			{
				//close
				translateBSlider(false);
			}
			else 
			//open
				translateBSlider(true);
			// TODO: Implement this method
		}
	};

	public void openQs()
	{
		mPanelContainerView.animateQs(true);
		mQPWidget.setQsState(true);
	}
	public void closeQs()
	{
		mPanelContainerView.animateQs(false);
		mQPWidget.setQsState(false);
	}
	private void translateBSlider(boolean open)
	{
		int fromY=0;
		int toY=0;
		if (open)
		{
			fromY = 0;
			toY = mBSliderHeight;
			mBSliderOpen = true;
		}
		else
		{
			fromY = mBSliderHeight;
			toY = 0;
			mBSliderOpen = false;
		}
		ValueAnimator va = ValueAnimator.ofInt(fromY, toY);
		va.setDuration(400);
		va.addUpdateListener(vaUpdateListener);
		va.start();
	}
	private ValueAnimator.AnimatorUpdateListener vaUpdateListener = new ValueAnimator.AnimatorUpdateListener(){

		@Override
		public void onAnimationUpdate(ValueAnimator p1)
		{
			int h=(Integer)p1.getAnimatedValue();
			mLpGanjel.height = h;
			mGanjelBSlider.setLayoutParams(mLpGanjel);
			ViewHelper.setRotation(mMoreButton, 180 * ((float) h / mBSliderHeight));
			int max = mBSliderHeight;
			if (h >= 2)
			{
				mBSlider.setVisibility(View.VISIBLE);
			}
			else mBSlider.setVisibility(View.GONE);
		}


	};


	public void updateQPW()
	{
		mQButton = mQPWidget.getButtonView();
	}

	public int getCurrentColorTintBg()
	{
		return mCurrentColorBg;
	}
	public int getCurrentColorTintWidget()
	{
		return mCurrentColorWidget;
	}
	public void setStatusBarService(StatusBarService sbs)
	{
		mService = sbs;

		mPanelContainerView.setStatusBarService(mService);
	}
}
