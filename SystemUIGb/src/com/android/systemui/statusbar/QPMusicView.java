package com.android.systemui.statusbar;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.net.*;
import android.os.*;
import android.provider.*;
import android.util.*;
import android.view.*;
import android.view.animation.*;
import android.widget.*;
import com.android.systemui.*;
import java.io.*;
import kere.settings.*;
import kere.util.*;
import kere.widget.*;
import support.animator.animation.*;
import support.animator.view.*;;

public class QPMusicView extends LinearLayout
{
	
	StatusBarService mService;

	private static final String PLAYPAUSE = "com.android.music.musicservicecommand.togglepause";
	private static final String PREVIOUS = "com.android.music.musicservicecommand.previous";
	private static final String NEXT = "com.android.music.musicservicecommand.next";

	private static final String META = "com.android.music.metachanged";
	private static final String PLAYSTATE= "com.android.music.playstatechanged";
	
	private int mCurrentHeightMode = 0;
	private boolean mIsPlaying = false;
	private LinearLayout bigLayout, smallLayout;
	//big
	private RoundImageView albArt;
	private ImageView mundur, plepaus, maju;
	private TextView judul, artis;
	private int mBigHeight;
	
	//small
	private RoundImageView albArtSmall;
	private TextView judulSmall, artisSmall;
	private int mSmallHeight;
	
	private LinearLayout mparent;
	private ActivityManager mActivityManager;
	public QPMusicView(Context c)
	{
		super(c, null);
	}
	public QPMusicView(Context c ,AttributeSet as)
	{
		super(c, as);
	}
	@Override
	protected void onFinishInflate()
	{
		// TODO: Implement this method
		super.onFinishInflate();
		mActivityManager = (ActivityManager)getContext().getSystemService(Context.ACTIVITY_SERVICE);
		mparent = (LinearLayout)findViewById(R.id.qp_parent);
		bigLayout = (LinearLayout)findViewById(R.id.qp_bigLayout);
		smallLayout = (LinearLayout)findViewById(R.id.qp_smallLayout);
		
		bigLayout.setVisibility(GONE);
		smallLayout.setVisibility(GONE);
		
		albArt = (RoundImageView)findViewById(R.id.albumart);
		mundur = (ImageView)findViewById(R.id.mundur);
		plepaus = (ImageView)findViewById(R.id.plepaus);
		maju = (ImageView)findViewById(R.id.maju);
		judul = (TextView)findViewById(R.id.judul);
		artis = (TextView)findViewById(R.id.artis);

		albArtSmall = (RoundImageView)findViewById(R.id.albumart_small);
		judulSmall = (TextView)findViewById(R.id.judul_small);
		artisSmall = (TextView)findViewById(R.id.artis_small);
		
		mundur.setImageResource(R.drawable.ic_qp_prev);
		plepaus.setImageResource(R.drawable.ic_qp_pause);
		maju.setImageResource(R.drawable.ic_qp_next);
		
		judulSmall.setEnabled(true);
		
		albArt.setCornerRadius(2);
		albArtSmall.setCornerRadius(1);
		
		mundur.setOnClickListener(ocl);
		plepaus.setOnClickListener(ocl);
		maju.setOnClickListener(ocl);
		albArt.setOnClickListener(ocl);
		albArtSmall.setOnClickListener(ocl);
		
		mBigHeight = getContext().getResources().getDimensionPixelSize(R.dimen.qp_music_big_height);
		mSmallHeight = getContext().getResources().getDimensionPixelSize(R.dimen.qp_music_small_height);
		
	}
	BroadcastReceiver br= new BroadcastReceiver(){

		@Override
		public void onReceive(Context p1, Intent p2)
		{
			if (META.equals(p2.getAction()))
			{
				artis.setText(p2.getStringExtra("artist"));
				judul.setText(p2.getStringExtra("track"));
				
				artisSmall.setText(p2.getStringExtra("artist"));
				judulSmall.setText(p2.getStringExtra("track"));

				final Bitmap defAlb=((BitmapDrawable)getResources().getDrawable(R.drawable.def_albart)).getBitmap();
				final Bitmap alb=getAlbumArt(p2.getLongExtra("albumId", 0));
				Bitmap color = null;
				if (alb != null){
					albArt.setImageBitmap(alb);
					albArtSmall.setImageBitmap(alb);
					color = alb;
					}
				else{
					albArt.setImageBitmap(defAlb);
					albArtSmall.setImageBitmap(defAlb);
					color = defAlb;
					}
				// Bitmap ba= ((BitmapDrawable)albArt.getDrawable()).getBitmap();
				if (color != null)
				{

					int warnaDominan=ColorUtils.getDominantColor(color);
					boolean b= ColorUtils.isBrightColor(warnaDominan);
					artis.setTextColor(b ? 0xff222222 : 0xffffffff);
					judul.setTextColor(b ? 0xff444444 : 0xffdddddd);
					
					mundur.setColorFilter(b? 0xff666666 : 0xffeeeeee);
					plepaus.setColorFilter(b? 0xff666666 : 0xffeeeeee);
					maju.setColorFilter(b? 0xff666666 : 0xffeeeeee);
					
					artisSmall.setTextColor(b ? 0xff222222 : 0xffffffff);
					judulSmall.setTextColor(b ? 0xff444444 : 0xffdddddd);
	
					final int h = mCurrentHeightMode;
					final Bitmap d2 = BitmapUtils.centerCrop(color, bigLayout.getWidth(), h);

					final Bitmap blurred = d2.copy(Bitmap.Config.ARGB_8888,true);
					BitmapUtils.bitmapBlur(d2, blurred, 6);
					final BitmapDrawable bg= new BitmapDrawable(blurred);
					bg.setColorFilter(warnaDominan & 0xaaffffff, PorterDuff.Mode.SRC_ATOP);
					mparent.setBackgroundDrawable(bg);

					//	Settings.System.putInt(p1.getContentResolver(), Setelan.COLOR_ALBUM_ART, warnaDominan);

				}
				//show
				if(mIsPlaying)delayDismiss();

			}
			else if (PLAYSTATE.equals(p2.getAction()))
			{
				mIsPlaying = p2.getBooleanExtra("playing", false);
				if (mIsPlaying)
				{	
					plepaus.setImageResource(android.R.drawable.ic_media_pause);
					// setVisibility(View.VISIBLE) ;
					//	removeCallbacks(r);
				}
				else
				{
					plepaus.setImageResource(android.R.drawable.ic_media_play);
					// GONE didelay 1/2 menit
					//	postDelayed(r, 30000);
				}
			}


			// TODO: Implement this method
		}

	};
	View.OnClickListener ocl= new View.OnClickListener(){

		@Override
		public void onClick(View p1)
		{
			
			if (albArt.isPressed() || albArtSmall.isPressed())
			{
				Intent iten= new Intent();
				iten.setClassName("com.android.music", "com.android.music.MediaPlaybackActivity");
				iten.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

				p1.getContext().startActivity(iten);
				dismiss();

			}
			if (mundur.isPressed())
			{
				Intent i1= new Intent(PREVIOUS);
				getContext().sendBroadcast(i1);
				
				delayDismiss();
			}

			if (plepaus.isPressed())
			{
				Intent i2= new Intent(PLAYPAUSE);
				getContext().sendBroadcast(i2);
				
				delayDismiss();
			}

			if (maju.isPressed())
			{
				Intent i3= new Intent(NEXT);
				getContext().sendBroadcast(i3);
				
				delayDismiss();
			}

			// TODO: Implement this method
		}


	};
	private boolean allow(){
		final String app = mActivityManager.getRunningTasks(1).get(0).topActivity.getPackageName();
		return !("com.android.music".equals(app));
	}
	public boolean isPlaying(){
		return mIsPlaying;
	}
	public void updateLayout(){
		// 0=disable, 1=small, 2=big
		int mode = Settings.System.getInt(getContext().getContentResolver(), Setelan.HEADS_UP_MUSIC_QP_MODE,0);
		if (mode == 0){
			bigLayout.setVisibility(GONE);
			smallLayout.setVisibility(GONE);
		}
		else if(mode == 1){
			mCurrentHeightMode = mSmallHeight;
			bigLayout.setVisibility(GONE);
			smallLayout.setVisibility(VISIBLE);
		}
		else if(mode == 2){
			mCurrentHeightMode = mBigHeight;
			smallLayout.setVisibility(GONE);
			bigLayout.setVisibility(VISIBLE);
		}
	}
	private boolean mIsShown = false;
	public void show()
	{
		if (isShown())return;

		setVisibility(VISIBLE);
		ViewHelper.setAlpha(mparent, 0);
		ViewHelper.setTranslationY(mparent, -getHeight());
		mIsShown = true;

		final ValueAnimator bganim = ValueAnimator .ofFloat(0, 1f);
		bganim.setDuration(250);
		bganim.setInterpolator(new DecelerateInterpolator());
		bganim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(){

				@Override
				public void onAnimationUpdate(ValueAnimator p1)
				{
					ViewHelper.setAlpha(mparent, (float)p1.getAnimatedValue());
					ViewHelper.setTranslationY(mparent, (1f-p1.getAnimatedFraction()) * -getHeight());
				}


			});
		bganim.start();
	}

	public void hide()
	{
		if (!mIsShown)return;

		final ValueAnimator bganim = ValueAnimator .ofFloat(0, 1f);
		bganim.setDuration(250);
		bganim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(){

				@Override
				public void onAnimationUpdate(ValueAnimator p1)
				{
					ViewHelper.setTranslationY(mparent, p1.getAnimatedFraction() * -getHeight());
					//ViewHelper.setAlpha(mparent, 1f - (float)p1.getAnimatedValue());
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
	private void delayDismiss()
	{
		// delay hide quickpanel
		if(allow())mService.showQPMusicView();

	}
	private void dismiss(){
		mService.hideQPMusicViewNoDelay();
	}
	
	public int getCurrentHeightMode(){
		return mCurrentHeightMode;
	}
	
	@Override
	protected void onAttachedToWindow()
	{
		// TODO: Implement this method
		super.onAttachedToWindow();

		IntentFilter ifil=new IntentFilter();
		ifil.addAction(META);
		ifil.addAction(PLAYSTATE);
		getContext().registerReceiver(br, ifil);
	}

	@Override
	protected void onDetachedFromWindow()
	{
		// TODO: Implement this method
		super.onDetachedFromWindow();

		getContext().unregisterReceiver(br);
	}
	
	private long maxDoubleTapTime = 300;
	private long doubleTapTime = 0;
	@Override
	public boolean onTouchEvent(MotionEvent p1)
	{
		if (p1.getAction() == MotionEvent.ACTION_DOWN)
		{
			
			long doubleTap = SystemClock.uptimeMillis() - doubleTapTime;
			if(doubleTap<=maxDoubleTapTime){
				dismiss();
				doubleTapTime = 0;
			}
			else{
				doubleTapTime = 0;
				delayDismiss();
			}
			
			doubleTapTime = SystemClock.uptimeMillis();

			
		}
		// TODO: Implement this method
		return super.onTouchEvent(p1);
	}


	public Bitmap getAlbumArt(long album_id)
	{
        Bitmap bm = null;
        try
        {
            final Uri u = Uri.parse("content://media/external/audio/albumart");
            Uri uri = ContentUris.withAppendedId(u, album_id);

            ParcelFileDescriptor pfd =getContext().getContentResolver().openFileDescriptor(uri, "r");
            if (pfd != null)
            {
                FileDescriptor fd = pfd.getFileDescriptor();
                bm = BitmapFactory.decodeFileDescriptor(fd);
            }
		}
		catch (Exception e)
		{
		}
		return bm;
	}

}
