package com.sec.android.app.screencapture;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.Log;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
//import android.view.WindowManager.La
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
//import android.widget.FrameLayout.LayoutParams;
import android.widget.LinearLayout;
import android.widget.Toast;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import android.view.*;
import android.os.*;
import android.graphics.*;
import android.util.*;

public class ScreenCaptureService extends Service
{
	private int mCurrentDsbColor = 0;

	private boolean DEBUG;
	private boolean captureResult;
	private StringBuffer filenameBuff;
	public boolean isLongKey;
	private AudioManager mAudioManager;
	private Handler mHandler;
	private ScreenCapture mScreenCapture;
	private SoundPool mSoundPool;
	private int[] mSoundPoolId;
	private Vibrator mVibrator;
	private VideoFrameLayout mVideoFrameLayout;
	private WindowManager mWindowManager;
	public boolean onCapture;

	
	public ScreenCaptureService()
	{
		super();
		DEBUG = false;
		mSoundPoolId = new int[2];
		onCapture = false;
		isLongKey = false;
	}
	
	Runnable runnable = new Runnable(){

		@Override
		public void run()
		{
			capture();
			// TODO: Implement this method
		}


	};
	private OnLoadCompleteListener mOlcl = new OnLoadCompleteListener(){

		@Override
		public void onLoadComplete(SoundPool p1, int p2, int p3)
		{
			mHandler.post(runnable);
			// TODO: Implement this method
		}

	};
	
	public class VideoFrameLayout extends FrameLayout
	{
		AlphaAnimation AlphaFadeIn;
		AlphaAnimation AlphaFadeOut;
		ScaleAnimation ScaleZoomIn;
		ScaleAnimation ScaleZoomOut;
		LinearLayout mAlphaLay;
		LinearLayout mBorderLay;
		private ScreenCaptureService mScreenCaptureService;

		AnimationListener animListener1 = new AnimationListener(){

			@Override
			public void onAnimationStart(Animation p1)
			{
				// TODO: Implement this method
			}

			@Override
			public void onAnimationEnd(Animation p1)
			{
				mBorderLay.startAnimation(ScaleZoomOut);
				mAlphaLay.startAnimation(AlphaFadeOut);
				// TODO: Implement this method
			}

			@Override
			public void onAnimationRepeat(Animation p1)
			{
				// TODO: Implement this method
			}


		};

		AnimationListener animListener2 = new AnimationListener(){

			@Override
			public void onAnimationStart(Animation p1)
			{
				// TODO: Implement this method
			}

			@Override
			public void onAnimationEnd(Animation p1)
			{
				mBorderLay.setVisibility(8);
				mAlphaLay.setVisibility(8);
				mScreenCaptureService.mWindowManager.removeView(mScreenCaptureService.mVideoFrameLayout);
				mScreenCaptureService.stopSelf();
			}

			@Override
			public void onAnimationRepeat(Animation p1)
			{
				// TODO: Implement this method
			}


		};


		public VideoFrameLayout(ScreenCaptureService r1_ScreenCaptureService, Context context)
		{
			super(context);
			mScreenCaptureService = r1_ScreenCaptureService;
			VideoLayoutStart(context);
		}

		private static final int dur3=250;
		private static final int dur4=400;
		private static final int dur2=200;
		private void VideoLayoutStart(Context context)
		{
			mBorderLay = new LinearLayout(context);
			mAlphaLay = new LinearLayout(context);
			mBorderLay.setLayoutParams(new LayoutParams(10, 10));
			mAlphaLay.setLayoutParams(new LayoutParams(10, 10));
			mBorderLay.setBackgroundDrawable(getResources().getDrawable(R.drawable.call_capture_border));
			mAlphaLay.setBackgroundColor(-1);
			mBorderLay.setVisibility(8);
			mAlphaLay.setVisibility(8);
			addView(mAlphaLay, 0);
			addView(mBorderLay, 1);
			ScaleZoomIn = new ScaleAnimation(1.5f, 1.0f, 1.5f, 1.0f, 1, 0.5f, 1, 0.5f);
			ScaleZoomIn.setDuration(dur3);
			ScaleZoomIn.setFillAfter(true);
			ScaleZoomOut = new ScaleAnimation(1.0f, 1.5f, 1.0f, 1.5f, 1, 0.5f, 1, 0.5f);
			ScaleZoomOut.setDuration(dur3);
			ScaleZoomOut.setFillAfter(true);
			AlphaFadeIn = new AlphaAnimation(0.0f, 0.2f);
			AlphaFadeIn.setDuration(dur3);
			AlphaFadeIn.setFillAfter(true);
			AlphaFadeOut = new AlphaAnimation(0.2f, 0.0f);
			AlphaFadeOut.setDuration(dur3);
			AlphaFadeOut.setFillAfter(true);
		}

		public void ClickEffect()
		{
			if (DEBUG)Log.d("Nawoong", "ClickEffect");
			mBorderLay.setVisibility(0);
			mAlphaLay.setVisibility(0);
			mBorderLay.startAnimation(ScaleZoomIn);
			mAlphaLay.startAnimation(AlphaFadeIn);
			ScaleZoomIn.setAnimationListener(animListener1);
			ScaleZoomOut.setAnimationListener(animListener2);
		}

		public void Setset(int width, int height)
		{
			if (DEBUG)Log.d("sf", "width-" + width + " height-" + height);
			LayoutParams borderlp = (LayoutParams) mBorderLay.getLayoutParams();
			borderlp.width = width;
			borderlp.height = height;
			borderlp.gravity = 17;
			mBorderLay.setLayoutParams(borderlp);
			LayoutParams alphalp = (LayoutParams) mAlphaLay.getLayoutParams();
			alphalp.width = width;
			alphalp.height = height;
			alphalp.gravity = 17;
			mAlphaLay.setLayoutParams(alphalp);
		}

		protected void onSizeChanged(int w, int h, int oldw, int oldh)
		{
			super.onSizeChanged(w, h, oldw, oldh);
			Setset(w, h);
		}

		public void showToast(int id)
		{
			Toast.makeText(getContext(), id, 0).show();
		}
	}

	private boolean saveAsFile()
	{
		onCapture = true;
		if (!"mounted".equals(Environment.getExternalStorageState()))
		{
			onCapture = false;
			return false;
		}
		else
		{
			String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/ScreenCapture/";
			filenameBuff = new StringBuffer();
			filenameBuff.append(filePath).append("SC").append(new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date())).append(".png");
			File tempFilePath = new File(filePath);
			if (!tempFilePath.isDirectory())
			{
				tempFilePath.mkdirs();
			}
			captureResult = mScreenCapture.capture(filenameBuff.toString(), 0);
			onCapture = false;
			return captureResult;
		}
	}

	public void capture()
	{
		String scservice = "ScreenCaptureService";
		if (mVideoFrameLayout != null)
		{
			mVideoFrameLayout.ClickEffect();
		}
		if (saveAsFile())
		{
			mVideoFrameLayout.showToast(R.string.screen_capture_success);
			String uriString = "file://" + filenameBuff.toString();
			sendBroadcast(new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE", Uri.parse(uriString)));
			if (DEBUG)
			{
				Log.d(scservice, "Media Scan for filepath: " + uriString);
			}
			int mRingerMode = mAudioManager.getRingerMode();
			if (DEBUG)
			{
				Log.d(scservice, "RingerMode = " + String.valueOf(mRingerMode));
			}
			switch (mRingerMode)
			{
				case 1:
					mVibrator.vibrate(500);
					break;
				case 2:
					playCameraSound(1, 0, ((float) mAudioManager.getStreamVolume(2)) / ((float) mAudioManager.getStreamMaxVolume(2)));
					break;
			}
		}
		else
		{
			mVideoFrameLayout.showToast(R.string.screen_capture_fail);
		}
	}

	public void initCameraSound()
	{
		if (DEBUG)
		{
			Log.d("ScreenCaptureService", "Initialize Camera Sound");
		}
		mSoundPool = new SoundPool(2, 1, 0);
		mSoundPoolId[0] = 0;
		mSoundPoolId[1] = mSoundPool.load(this, R.raw.shutter1, 0);
	}

	public IBinder onBind(Intent arg0)
	{
		return null;
	}
	Intent mIntentDsb;
	private static final int mStatusBarHeight = 19;
	Display mDisplay;
	@Override
	public void onCreate()
	{
		doLogClock("onCreate");

		mScreenCapture = new ScreenCapture();
		mIntentDsb = new Intent("statusbarmod.dsb");

		
		mHandler = new Handler();
		mAudioManager = (AudioManager) getSystemService("audio");
		mVibrator = (Vibrator) getSystemService("vibrator");
		mWindowManager = (WindowManager) getSystemService("window");
		mVideoFrameLayout = new VideoFrameLayout(this, this);

		android.view.WindowManager.LayoutParams params = new WindowManager.LayoutParams(-1, -1, 2006, 0, -3);
		params.screenOrientation = -1;
		params.gravity = Gravity.FILL;
		mWindowManager.addView(mVideoFrameLayout, params);
		mDisplay = ((WindowManager)getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		
	}
	private final static String DSBCOLOR ="dsbColor";
	// checker blink
	int blackChecker = 0;
	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		doLogClock("onStart");
		if (mScreenCapture != null)
		{
			
			
			if (intent.getIntExtra("dsb", 0) == 1)
			{
				//if (DEBUG)Log.d("dsb", "color: " + mScreenCapture.getTopScreenColor());
				final int xFromRight = intent.getIntExtra("xFromRight",1);
				final int rotation = mDisplay.getRotation();
				final int color = mScreenCapture.getTopScreenColor(mStatusBarHeight,rotation, xFromRight);
				if (mCurrentDsbColor == color)return 0;
				if (color != mCurrentDsbColor && color != 0 /*&& color != 0xff000000*/) // color is updated
				{
					mIntentDsb.putExtra(DSBCOLOR, color);
					sendBroadcast(mIntentDsb);
					//blackChecker = 0;
				}
				// cek warna ireng ben ora ngeblink
				/*else if(mCurrentDsbColor == color && color == 0xff000000)
					if(blackChecker<1){
						blackChecker ++;
					}
					else{
						mIntentDsb.putExtra(DSBCOLOR, color);
						sendBroadcast(mIntentDsb);
					}*/
				mCurrentDsbColor = color;

			}
			else
			{
				initCameraSound();
				mSoundPool.setOnLoadCompleteListener(mOlcl);
			}

			return 2;
		}
		return 0;
	}
	private void doLogClock(String metod)
	{
		if (DEBUG)Log.d("timeCalled", metod + SystemClock.uptimeMillis());
	}
	public void playCameraSound(int Sound, int loop, float adjustmentValue)
	{
		if (DEBUG)
		{
			Log.d("ScreenCaptureService", "adjustmentValue = " + String.valueOf(adjustmentValue));
		}
		mSoundPool.play(mSoundPoolId[Sound], adjustmentValue, adjustmentValue, 0, loop, 1.0f);
	}
}
