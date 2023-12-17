/*
 * Copyright (C) 2010 The ndroid Open Sopdrce Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this ullile except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
## DateView dipindah meng expanded, dadi setDateVisibility ora perlu
## togel diganti lollitogel @krupuk
*/
package com.android.systemui.statusbar;

import android.app.Service;
import com.android.internal.statusbar.IStatusBar;
import com.android.internal.statusbar.IStatusBarService;
import com.android.internal.statusbar.StatusBarIcon;
import com.android.internal.statusbar.StatusBarIconList;
import com.android.internal.statusbar.StatusBarNotification;

//import com.android.systemui.statusbar.powerwidget.PowerWidget;

import com.android.systemui.*;
import android.app.ActivityManagerNative;
import android.app.Dialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.app.StatusBarManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.Binder;
import android.os.Handler;
import android.os.Message;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Slog;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManagerImpl;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RemoteViews;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.FrameLayout;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import com.android.systemui.statusbar.policy.*;
import com.android.systemui.dialog.*;

import com.android.systemui.R;
import android.view.animation.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.content.*;
import kere.util.*;
import kere.widget.*;
import android.widget.*;
import kere.settings.*;
//import com.sec.android.app.screencapture.*;
import android.os.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import android.database.*;
import android.widget.RelativeLayout.*;
import android.view.ViewTreeObserver.*;

public class StatusBarService extends Service implements CommandQueue.Callbacks {
    static final String TAG = "StatusBarService";
    static final boolean SPEW_ICONS = false;
    static final boolean SPEW = false;

    public static final String ACTION_STATUSBAR_START
            = "com.android.internal.policy.statusbar.START";

    static final int EXPANDED_LEAVE_ALONE = -10000;
    static final int EXPANDED_FULL_OPEN = -10001;

    private static final int MSG_ANIMATE = 1000;
    private static final int MSG_ANIMATE_REVEAL = 1001;
	
	private static final int MSG_SHOW_INTRUDER = 1002;
    private static final int MSG_HIDE_INTRUDER = 1003;
	
	private static final int MSG_SHOW_QP_MUSIC = 1004;
    private static final int MSG_HIDE_QP_MUSIC = 1005;
	
	private static final int MSG_MAKE_EXP_VISIBLE = 1006;
	
	private static final int MSG_HIDE_PEEK_STATUS_BAR = 1007;

    StatusBarPolicy mIconPolicy;
	
	Clock jam;

    CommandQueue mCommandQueue;
    IStatusBarService mBarService;

	StatusBarCover mSbCover;
    int mIconSize;
    Display mDisplay;
    StatusBarView mStatusBarView;
	
    int mPixelFormat;
    H mHandler = new H();
	Handler ha= new Handler();
    Object mQueueLock = new Object();
	
	// status bar items
	private BatteryTextView mBatteryTextView;
	private BatteryView mBatteryView;
	private SignalCluster mSignalClusterView;
//	ScreenCapture sc;
    // icons
    LinearLayout mIcons;
    IconMerger mNotificationIcons;
    LinearLayout mStatusIcons;

    // expanded notifications
	ExpandedPanelView mExpPanelView;
    Dialog mExpandedDialog;
    ExpandedView mExpandedView;
    WindowManager.LayoutParams mExpandedParams;
    ScrollView mScrollView;
    ViewGroup mNotificationLinearLayout;
    View mExpandedContents;
//	CarrierLabel mCarrierLabel;
    // top bar
    TextView mNoNotificationsTitle;
    ImageView mClearButton;
    // drag bar
    CloseDragHandle mCloseView;
    // ongoing
    NotificationData mOngoing = new NotificationData();
    TextView mOngoingTitle;
    LinearLayout mOngoingItems;
    // latest
    NotificationData mLatest = new NotificationData();
    TextView mLatestTitle;
	LatestItemView latestEvent;
    LinearLayout mLatestItems;
	ItemTouchDispatcher mTouchDispatcher;
	ViewGroup content;
	//miniCon
	NotificationData mMiniConData = new NotificationData();;
	LinearLayout mMiniCon;
    // position
    int[] mPositionTmp = new int[2];
    boolean mExpanded;
    boolean mExpandedVisible;
	
	ImageView sleting;

	//musik togel;
	TextView penyanyi, judul;
	
	// night mode
	private static final String INTENT_NIGHTMODE_DIALOG = "openNightModeDialog";
	NightModeViewOverlay mNightModeView;
	NightModeDialog mNightModeDialog;
	
	// buttonPanel
	private ButtonPanelView mButtonPanelView;
	public static final int BUTTON_PANEL_WIDTH = 30;
	
	// bool nggo setting contextable
	boolean albumartContext=true;
    // the tracker view
    TrackingView mTrackingView;
    WindowManager.LayoutParams mTrackingParams;
    int mTrackingPosition; // the position of the top of the tracking view.
    private boolean mPanelSlightlyVisible;

    // ticker
    private Ticker mTicker;
    private View mTickerView;
    private boolean mTicking;

    // Tracking finger for opening/closing.
    int mEdgeBorder; // corresponds to R.dimen.status_bar_edge_ignore
    boolean mTracking;
    VelocityTracker mVelocityTracker;
	ImageView closDrag;

	private BgPanelView mBgPanelView;
	private static final int BG_PANEL_ALPHA = 0xdd;
	
    private static final int ANIM_FRAME_DURATION = (1000/60);

	
    boolean mAnimating;
    long mCurAnimationTime;
    float mDisplayHeight;
    float mAnimY;
    float mAnimVel;
    float mAnimAccel;
    long mAnimLastTime;
    boolean mAnimatingReveal = false;
    int mViewDelta;
    int[] mAbsPos = new int[2];

	// qp musik headsup
	private QPMusicView mQpMusicView;
	private static final long QP_MUSIC_DELAY= 5000;
	
	//intruder
	private IntruderView mIntruderAlertView;
	private static final int INTRUDER_ALERT_DECAY_MS = 3000;
	private static final int IntruderTime=INTRUDER_ALERT_DECAY_MS;
	
	// alert new notif
	boolean mAlertNewNotif = true;
	
    // for disabling the status bar
    int mDisabled = 0;
	
	ColorDrawable mBgColor;
	private SettingsObserver sObserver;
	
	// dsb stuff
	private FullScreenDetector mFullScreenDetectorView;
	private static final String DSB_INTENT = "statusbarmod.dsb";
	private Intent mIntentDsb;
	private static final boolean DEBUG_DSB = false;
	private static final String LOG_DSB = "sbServiceDSB";
	private boolean mScreenOff;
	private boolean mFullscreen;
	private boolean mEnableDsb = true;
	private int mScreenWidth;
	private int mScreenHeight;
	private int mDsbUpdateTime;
	private static final int DSB_UPDATE_TIME = 1200;
	
	// statusbar peeker
	private View mSBPeekerView;
	private View mSBPeekingView;
	private boolean mOnSBPeeked = false;
	
	
	private class SettingsObserver extends ContentObserver {
        SettingsObserver(Handler handler) {
            super(handler);
        }

        void observe() {
            ContentResolver resolver = getContentResolver();
            resolver.registerContentObserver(Settings.System.getUriFor(
                    Setelan.HEADS_UP_MUSIC_QP_MODE), false, this);
			resolver.registerContentObserver(Settings.System.getUriFor(
			        Setelan.TINT_ENABLE), false, this);
			resolver.registerContentObserver(Settings.System.getUriFor(
					Setelan.TINT_UPDATE_RATE), false, this);
			resolver.registerContentObserver(Settings.System.getUriFor(
			        Setelan.NIGHT_MODE_ENABLE), false, this);
			resolver.registerContentObserver(Settings.System.getUriFor(
			        Setelan.BATTERY_MODE),false,this);
					
          }
		  
		  
        @Override public void onChange(boolean selfChange) {
			updateQpMusicView();
			updateNightMode();
			updateDsbSetting();
			updateBatteryVisibility();
			
        }
    }
    private class ExpandedDialog extends Dialog {
        ExpandedDialog(Context context) {
            super(context, com.android.internal.R.style.Theme_Light_NoTitleBar);
        }

        @Override
        public boolean dispatchKeyEvent(KeyEvent event) {
            boolean down = event.getAction() == KeyEvent.ACTION_DOWN;
            switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_BACK:
                if (!down) {
                    delayedAnimateCollapse();
                }
				return true;
			case KeyEvent.KEYCODE_HOME:
				animateCollapse();
				return true;
            }
            return super.dispatchKeyEvent(event);
        }
    }


    @Override
    public void onCreate() {
	//	sc=new ScreenCapture();
        // First set up our views and stuff.
        mDisplay = ((WindowManager)getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		
		mScreenWidth = mDisplay.getWidth();
		mScreenHeight = mDisplay.getHeight();
		
		//Log.d("screensize", "width : "+mScreenWidth + " height: "+mScreenHeight);
		
        makeStatusBarView(this);
		
		// observe settings
		sObserver = new SettingsObserver(new Handler());
		sObserver.observe();
		
        // Connect in to the status bar manager service
        StatusBarIconList iconList = new StatusBarIconList();
        ArrayList<IBinder> notificationKeys = new ArrayList<IBinder>();
        ArrayList<StatusBarNotification> notifications = new ArrayList<StatusBarNotification>();
        mCommandQueue = new CommandQueue(this, iconList);
        mBarService = IStatusBarService.Stub.asInterface(
                ServiceManager.getService(Context.STATUS_BAR_SERVICE));
        try {
            mBarService.registerStatusBar(mCommandQueue, iconList, notificationKeys, notifications);
        } catch (RemoteException ex) {
            // If the system process isn't there we're doomed anyway.
        }

        // Set up the initial icon state
        int N = iconList.size();
        int viewIndex = 0;
        for (int i=0; i<N; i++) {
            StatusBarIcon icon = iconList.getIcon(i);
            if (icon != null) {
                addIcon(iconList.getSlot(i), i, viewIndex, icon);
                viewIndex++;
            }
        }

        // Set up the initial notification state
        N = notificationKeys.size();
        if (N == notifications.size()) {
            for (int i=0; i<N; i++) {
                addNotification(notificationKeys.get(i), notifications.get(i));
            }
        } else {
            Log.wtf(TAG, "Notification list length mismatch: keys=" + N
                    + " notifications=" + notifications.size());
        }

        // Put up the view
		addStatusBarView();
		addIntruderView();
		updateQpMusicView();
		addButtonPanelView();
		addFullscreenCheckerView();
		updateDsbSetting();
		addSBPeekerView();
		addSBPeekingView();
		updateBatteryVisibility();
        // Lastly, call to the icon policy to install/update all the icons.
        mIconPolicy = new StatusBarPolicy(this);
    }
	

    @Override
    public void onDestroy() {
        // we're never destroyed
    }

    /**
     * Nobody binds to us.
     */
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
	ContentResolver mContentResolver ;
	Drawable bgLatest;
	boolean onCapture;
    // ================================================================================
    // Constructing the view
    // ================================================================================
    private void makeStatusBarView(Context context) {
		mContentResolver = context.getContentResolver();
        Resources res = context.getResources();
		
		//swipe2remove
		mTouchDispatcher = new ItemTouchDispatcher(this);

        mIconSize = res.getDimensionPixelSize(R.dimen.status_bar_icon_size);

        ExpandedView expanded = (ExpandedView)View.inflate(context,
                R.layout.status_bar_expanded, null);
        expanded.mService = this;
        expanded.mTouchDispatcher = mTouchDispatcher;

        StatusBarView sb = (StatusBarView)View.inflate(context, R.layout.status_bar, null);
        sb.mService = this;
        // figure out which pixel-format to use for the status bar.
        mPixelFormat = PixelFormat.TRANSLUCENT;
        Drawable bg = sb.getBackground();
        if (bg != null) {
            mPixelFormat = bg.getOpacity();
        }
		
		mIntruderAlertView = (IntruderView) View.inflate(this,R.layout.intruder_content, null);
        mIntruderAlertView.mService = this;
        mIntruderAlertView.mTouchDispatcher = mTouchDispatcher;
        mIntruderAlertView.setVisibility(View.GONE);
		
		mQpMusicView = (QPMusicView)View.inflate(this, R.layout.qp_music_dialog, null);
		mQpMusicView.mService = this;
		
        mStatusBarView = sb;
        mStatusIcons = (LinearLayout)sb.findViewById(R.id.statusIcons);
        mNotificationIcons = (IconMerger)sb.findViewById(R.id.notificationIcons);
        mIcons = (LinearLayout)sb.findViewById(R.id.icons);
        mTickerView = sb.findViewById(R.id.ticker);
        mExpandedDialog = new ExpandedDialog(context);
        mExpandedView = expanded;
		
		// status bar items
		mBatteryTextView = (BatteryTextView)sb.findViewById(R.id.sbBatteryText);
		mBatteryView = (BatteryView)sb.findViewById(R.id.sbBattery);
		mSignalClusterView =(SignalCluster)sb.findViewById(R.id.klaster_view);
		mSbCover = (StatusBarCover)sb.findViewById(R.id.sbCover);
		mSbCover.mService = this;
		
		mExpPanelView = (ExpandedPanelView)expanded.findViewById(R.id.expPanelView);
		mExpPanelView.setStatusBarService(this);
		
        mExpandedContents = expanded.findViewById(R.id.notificationLinearLayout);
		
        mOngoingTitle = (TextView)expanded.findViewById(R.id.ongoingTitle);
        mOngoingItems = (LinearLayout)expanded.findViewById(R.id.ongoingItems);
        mLatestTitle = (TextView)expanded.findViewById(R.id.latestTitle);
        mLatestItems = (LinearLayout)expanded.findViewById(R.id.latestItems);
        mNoNotificationsTitle = (TextView)expanded.findViewById(R.id.noNotificationsTitle);
		
		jam=(Clock)sb.findViewById(R.id.jam);
//		mCarrierLabel=(CarrierLabel)expanded.findViewById(R.id.carrier_label);
        mClearButton = (ImageView)expanded.findViewById(R.id.clear_all_button);
        mClearButton.setOnClickListener(mClearButtonListener);
        mScrollView = (ScrollView)expanded.findViewById(R.id.scroll);
        mNotificationLinearLayout =(ViewGroup) expanded.findViewById(R.id.notificationLinearLayout);
		
		penyanyi=(TextView)expanded.findViewById(R.id.artis);
		judul= (TextView)expanded.findViewById(R.id.judul);
		
		sleting=(ImageView)expanded.findViewById(R.id.sleting);
		
		sleting.setOnClickListener(shortCuts);
        mExpandedView.setVisibility(View.GONE);
        mOngoingTitle.setVisibility(View.GONE);
        mLatestTitle.setVisibility(View.GONE);
		
		// night mode
		mNightModeView = new NightModeViewOverlay(this);
		mNightModeDialog = new NightModeDialog(this);
		mNightModeDialog.setOnUpdateListener(mNightModeUpdater);
		updateNightMode();
		
		// sb peeker
		mSBPeekingView = new View(this);
		mSBPeekerView = new View(this);
		mSBPeekerView.setOnTouchListener(mSBPeekerListener);
		// button panel
		mButtonPanelView = new ButtonPanelView(this);
		
		
        mTicker = new MyTicker(context, sb);

        TickerView tickerView = (TickerView)sb.findViewById(R.id.tickerText);
        tickerView.mTicker = mTicker;

		mBgPanelView = new BgPanelView(this);
		mBgPanelView.mService = this;
		mBgPanelView.setVisibility(View.GONE);
        mTrackingView = (TrackingView)View.inflate(context, R.layout.status_bar_tracking, null);
        mTrackingView.mService = this;
        mCloseView = (CloseDragHandle)mTrackingView.findViewById(R.id.close);
		
        mCloseView.mService = this;
		closDrag=(ImageView)mTrackingView.findViewById(R.id.closDrag);
		
        mEdgeBorder = res.getDimensionPixelSize(R.dimen.status_bar_edge_ignore);
		
        // set the inital view visibility
        setAreThereNotifications();

        // receive broadcasts
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_CONFIGURATION_CHANGED);
        filter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
		filter.addAction(Intent.ACTION_SCREEN_ON);
		// nightmodedialogopener
		filter.addAction(INTENT_NIGHTMODE_DIALOG);
		
        context.registerReceiver(mBroadcastReceiver, filter);
		
		mMiniCon = new LinearLayout(context);
		mMiniCon.setOrientation(1);
		mNotificationLinearLayout.addView(mMiniCon, 0);
		
		ColorBarTintUpdater.init(this);
				
    }
//	-------------------
	// OnClick Shortcut --------------
//	-------------------l

	View.OnClickListener shortCuts= new View.OnClickListener(){
		@Override
		public void onClick(View v){
			delayedAnimateCollapse();
			if(sleting.isPressed()){
				Intent setting= new Intent();
				setting.setClassName("com.android.settings", "com.android.settings.Settings");
				setting.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				v.getContext().startActivity(setting);
			}
		}
	};
	
	// night mode stuff
	
	public void showNightModeDialog(){
		mNightModeDialog.show();
	}
	private NightModeDialog.OnUpdateListener mNightModeUpdater = new NightModeDialog.OnUpdateListener(){

		@Override
		public void onIntensityUpdate(int intensityValue)
		{
			mNightModeView.updateIntensity(intensityValue);
		}

		@Override
		public void onScrDimUpdate(int scrDimValue)
		{
			mNightModeView.updateScrDim(scrDimValue);
		}
		
	};
	// sb peeker
	/*private void showSBPeeker(boolean show){
		if(show){
			WindowManager.LayoutParams lp =(WindowManager.LayoutParams) mSBPeekerView.getLayoutParams();
			lp.height=SB_PEEKER_HEIGHT;
			WindowManagerImpl.getDefault().updateViewLayout(mSBPeekerView, lp);
			
		}
		else {
			WindowManager.LayoutParams lp =(WindowManager.LayoutParams) mSBPeekerView.getLayoutParams();
			lp.height=0;
			WindowManagerImpl.getDefault().updateViewLayout(mSBPeekerView, lp);
		}
		//Log.d("sbpeeker@showSBPeeker", "show:"+show);
	}*/
	private final int SB_PEEKER_HEIGHT=5;
	private void showStatusBarPeeker(boolean show){
		if(show){
			WindowManager.LayoutParams lp =(WindowManager.LayoutParams) mSBPeekerView.getLayoutParams();
			lp.height=SB_PEEKER_HEIGHT;
			lp.flags = WindowManager.LayoutParams.FLAG_TOUCHABLE_WHEN_WAKING
				| WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
				| WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
				| WindowManager.LayoutParams.FLAG_SPLIT_TOUCH;
			WindowManagerImpl.getDefault().updateViewLayout(mSBPeekerView, lp);
			
			}
		else{
			WindowManager.LayoutParams lp =(WindowManager.LayoutParams) mSBPeekerView.getLayoutParams();
			lp.height=0;
			lp.flags = //WindowManager.LayoutParams.FLAG_TOUCHABLE_WHEN_WAKING
				WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
				//| WindowManager.LayoutParams.FLAG_SPLIT_TOUCH;
			WindowManagerImpl.getDefault().updateViewLayout(mSBPeekerView, lp);
		}
		//Log.d("sbpeeker@peekSB", "peek:"+peek);
	}
	private void peekStatusBar(boolean nohide){
		WindowManager.LayoutParams lp =(WindowManager.LayoutParams) mSBPeekingView.getLayoutParams();
		lp.height=0;
		lp.flags = WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN
				| WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
		WindowManagerImpl.getDefault().updateViewLayout(mSBPeekingView, lp);
		mOnSBPeeked = true;
		showStatusBarPeeker(false);
			
		if(!nohide)stopPeekingStatusbar(true);
					//Log.d("sbpeeker@peekSB", "peek:"+peek);
	}
	
	private void stopPeekingStatusbar(boolean delay){
		if(delay){
			mHandler.removeMessages(MSG_HIDE_PEEK_STATUS_BAR);
			mHandler.sendMessageAtTime(mHandler.obtainMessage(MSG_HIDE_PEEK_STATUS_BAR), SystemClock.uptimeMillis()+3000);
			}
			else{
				if(mExpandedVisible)return;
				WindowManager.LayoutParams lp =(WindowManager.LayoutParams) mSBPeekingView.getLayoutParams();
				lp.height=0;
				lp.flags =// WindowManager.LayoutParams.FLAG_TOUCHABLE_WHEN_WAKING
					//| WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
					WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
				//| WindowManager.LayoutParams.FLAG_SPLIT_TOUCH;
				WindowManagerImpl.getDefault().updateViewLayout(mSBPeekingView, lp);
				mOnSBPeeked = false;
				mHandler.postDelayed(new Runnable(){

					@Override
					public void run()
					{
						showStatusBarPeeker(getFullscreen());
						// TODO: Implement this method
					}
					
					
				},1000);
				
			}
	}
	private int peekerlasty;
	private boolean alreadyup;
	private View.OnTouchListener mSBPeekerListener= new View.OnTouchListener(){

		@Override
		public boolean onTouch(View p1, MotionEvent p2)
		{
			switch(p2.getAction()){
				case MotionEvent.ACTION_DOWN:
					peekerlasty = (int)p2.getY();
					break;
				case MotionEvent.ACTION_MOVE:
					final int y = (int)p2.getY() - peekerlasty;
					if(y>30 && !mOnSBPeeked && alreadyup){
						peekStatusBar(false);
						alreadyup = false;
					}
					break;
				case MotionEvent.ACTION_UP:
					alreadyup = true;
					break;
			}
			return false;
		}
		
		
	};
	
	// button panel stuff
	public boolean getButtonPanelEnable(){
		return mButtonPanelView.getEnable();
	}
	public void showButtonPanel(boolean show){
		if(show){
			mButtonPanelView.show();
			ColorBarTintUpdater.setIsButtonPanelShown(true);
			}
			else {
				mButtonPanelView.hide();
				ColorBarTintUpdater.setIsButtonPanelShown(false);
			}
	}
	
	// dsb stuff
	private void updateDsbSetting(){
		mEnableDsb = (Settings.System.getInt(mContentResolver, Setelan.TINT_ENABLE, 0)) == 1;
		mDsbUpdateTime = (Settings.System.getInt(mContentResolver, Setelan.TINT_UPDATE_RATE, DSB_UPDATE_TIME));
		ColorBarTintUpdater.setUpdateTime(mDsbUpdateTime);
		if(!mEnableDsb){
			// dsb mati, send default color
			mIntentDsb = new Intent(DSB_INTENT);
			//mIntentDsb.addCategory(Intent.CATEGORY_DEFAULT);
			mIntentDsb.putExtra("dsbColor",0xff000000);
			sendBroadcast(mIntentDsb);
		}
		updateDsb();
	}
	private void updateBatteryVisibility(){
		// mode: 0=gone 1=icon only 2=text only 3=ikon text
		final int mode = Settings.System.getInt(this.getContentResolver(), Setelan.BATTERY_MODE,1);
		LinearLayout.LayoutParams lpsignal =(LinearLayout.LayoutParams)mSignalClusterView.getLayoutParams();
		lpsignal.rightMargin =4;
		switch(mode){
			case 0:
				mBatteryView.setVisibility(View.GONE);
				mBatteryTextView.setVisibility(View.GONE);
				mBatteryView.unset();
				mBatteryTextView.unset();
				
				mSignalClusterView.setLayoutParams(lpsignal);
				
				break;
			case 1:
				mBatteryView.setVisibility(View.VISIBLE);
				mBatteryTextView.setVisibility(View.GONE);
				mBatteryView.set();
				mBatteryTextView.unset();
				
				LinearLayout.LayoutParams lpsignal0 =(LinearLayout.LayoutParams)mSignalClusterView.getLayoutParams();
				lpsignal0.rightMargin =1;
				mSignalClusterView.setLayoutParams(lpsignal0);
				break;
			case 2:
				mBatteryView.setVisibility(View.GONE);
				mBatteryTextView.setVisibility(View.VISIBLE);
				mBatteryView.unset();
				
				LinearLayout.LayoutParams lp =(LinearLayout.LayoutParams)mBatteryTextView.getLayoutParams();
				lp.rightMargin =3;
				mBatteryTextView.setLayoutParams(lp);
				mBatteryTextView.set();
				
				mSignalClusterView.setLayoutParams(lpsignal);
				break;
			case 3:
				mBatteryView.setVisibility(View.VISIBLE);
				mBatteryTextView.setVisibility(View.VISIBLE);
				mBatteryView.set();
				
				LinearLayout.LayoutParams lp0 =(LinearLayout.LayoutParams)mBatteryTextView.getLayoutParams();
				lp0.rightMargin = 0;
				mBatteryTextView.setLayoutParams(lp0);
				mBatteryTextView.set();
				
				mSignalClusterView.setLayoutParams(lpsignal);
				break;
		}

	}
	Runnable delaydsbresume = new Runnable(){

		@Override
		public void run()
		{
			ColorBarTintUpdater.resume();
		}
	};
	
	private void updateDsb(){
		if(getDsbAllowUpdate()){
			mHandler.postDelayed(delaydsbresume, 500);
			
		}
		else{
			ColorBarTintUpdater.pause();
		}
		if(DEBUG_DSB)Log.d(LOG_DSB, "enable:"+mEnableDsb+" fullScr:"+getFullscreen()
						   +" scrOff:"+mScreenOff);
	}
	public void setIsUpdateDsbIcon(){
		ColorBarTint.setIsUpdateIcon(mSbCover.getCurrentMode() ==0);
	}
	private boolean getDsbAllowUpdate(){
		return mEnableDsb && !getFullscreen() && !mScreenOff  && !mExpandedVisible;
		
	}
	private boolean getFullscreen(){
		return mScreenHeight == mFullScreenDetectorView.getHeight();
	}
	
	private boolean getScreenOffPM(){
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		return pm.isScreenOn();
	}
	public void setTintCloseView(){
		int colorbg = mExpPanelView.getCurrentColorTintBg();
		int colorLine = mExpPanelView.getCurrentColorTintWidget();
		mTrackingView.setBackgroundColor(colorbg&0x44ffffff);
		mCloseView.setBackgroundColor(colorbg);
		closDrag.setBackgroundColor(colorLine);
	}
	
	/* ---------
	 * Views
	 * ---------
	 */
    protected void addStatusBarView() {
        Resources res = getResources();
        final int height= res.getDimensionPixelSize(R.dimen.status_bar_height);

        final StatusBarView view = mStatusBarView;
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                height,
                WindowManager.LayoutParams.TYPE_STATUS_BAR,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_TOUCHABLE_WHEN_WAKING,
                PixelFormat.TRANSLUCENT);
        lp.gravity = Gravity.TOP | Gravity.FILL_HORIZONTAL;
        lp.setTitle("StatusBar");
        lp.windowAnimations = com.android.internal.R.style.Animation_StatusBar;

        WindowManagerImpl.getDefault().addView(view, lp);
    }
	// night mode
	boolean mNightModeViewAttached=false;
	private void addNightModeView(){
		WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
		ViewGroup.LayoutParams.MATCH_PARENT, 
                ViewGroup.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                    0
                    | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                    | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                    | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
		//			| WindowManager.LayoutParams.FLAG_DIM_BEHIND,
                PixelFormat.TRANSLUCENT);
      
        lp.setTitle("NightModeMask");
        WindowManagerImpl.getDefault().addView(mNightModeView, lp);
		mNightModeViewAttached = true;

	}
	private void addSBPeekerView(){
		WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
			ViewGroup.LayoutParams.FILL_PARENT,SB_PEEKER_HEIGHT,
			WindowManager.LayoutParams.TYPE_NAVIGATION_BAR_PANEL,
			0
			| WindowManager.LayoutParams.FLAG_TOUCHABLE_WHEN_WAKING
			//| WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
			| WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
			| WindowManager.LayoutParams.FLAG_SPLIT_TOUCH,
			PixelFormat.TRANSLUCENT);

		lp.gravity = Gravity.TOP | Gravity.FILL_HORIZONTAL;
        lp.setTitle("SBPeekerView");
        WindowManagerImpl.getDefault().addView(mSBPeekerView, lp);
	}
	private void addSBPeekingView(){
		WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
			ViewGroup.LayoutParams.FILL_PARENT,0,
			WindowManager.LayoutParams.TYPE_NAVIGATION_BAR_PANEL,
			0
			//| WindowManager.LayoutParams.FLAG_TOUCHABLE_WHEN_WAKING
			//| WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
			| WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
			//| WindowManager.LayoutParams.FLAG_SPLIT_TOUCH,
			PixelFormat.TRANSLUCENT);

		lp.gravity = Gravity.TOP ;
        lp.setTitle("SBPeekingView");
        WindowManagerImpl.getDefault().addView(mSBPeekingView, lp);
	}
	// button panel
	
	private void addButtonPanelView(){
		WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
			BUTTON_PANEL_WIDTH, 
			ViewGroup.LayoutParams.FILL_PARENT,
			WindowManager.LayoutParams.TYPE_NAVIGATION_BAR_PANEL,
			0
			| WindowManager.LayoutParams.FLAG_TOUCHABLE_WHEN_WAKING
			| WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
			| WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
			| WindowManager.LayoutParams.FLAG_SPLIT_TOUCH,
			PixelFormat.TRANSLUCENT);

		lp.gravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;
        lp.setTitle("ButtonPanelView");
        WindowManagerImpl.getDefault().addView(mButtonPanelView, lp);
	}
	
	private void addIntruderView() {
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
		ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_NAVIGATION_BAR_PANEL,
                    0
                   // | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                    | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                    | WindowManager.LayoutParams.FLAG_TOUCHABLE_WHEN_WAKING
                    | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_SPLIT_TOUCH,
                PixelFormat.TRANSLUCENT);
        lp.gravity = Gravity.TOP | Gravity.FILL_HORIZONTAL;
        lp.setTitle("IntruderAlert"); 
        //lp.windowAnimations = R.style.Animations_PopDownMenu_Center;

        WindowManagerImpl.getDefault().addView(mIntruderAlertView, lp);
    }
	
	private void addQPMusicView() {

   		// 0=disable, 1=small, 2=big
		int mode = Settings.System.getInt(getContentResolver(), Setelan.HEADS_UP_MUSIC_QP_MODE,0);
		boolean enable = mode !=0;

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
		ViewGroup.LayoutParams.MATCH_PARENT,
               mQpMusicView.getCurrentHeightMode() ,
                WindowManager.LayoutParams.TYPE_VOLUME_OVERLAY,
                    0
                  //  | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                 //   | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                    | WindowManager.LayoutParams.FLAG_TOUCHABLE_WHEN_WAKING
                    | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_SPLIT_TOUCH,
                PixelFormat.TRANSLUCENT);
        lp.gravity = Gravity.TOP | Gravity.FILL_HORIZONTAL;
        lp.setTitle("QuickPanelMusic"); 
		
		// attach the view
        WindowManagerImpl.getDefault().addView(mQpMusicView, lp);
		qpAttached = true;
		
		
		
    }
	
	private void addFullscreenCheckerView(){
		
		mFullScreenDetectorView = new FullScreenDetector(this,null);
		WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
		lp.type = WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;
		lp.gravity = Gravity.RIGHT | Gravity.TOP;
		lp.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
		lp.width = 1;
		lp.height = LayoutParams.MATCH_PARENT;
		lp.format = PixelFormat.TRANSPARENT;
		
		
		WindowManagerImpl.getDefault().addView(mFullScreenDetectorView, lp);
		mFullScreenDetectorView.setListener(oasvcl);
		
	}
	
	FullScreenDetector.OnStatusbarVisibilityChangeListener oasvcl = new FullScreenDetector.OnStatusbarVisibilityChangeListener(){

		@Override
		public void OnStatusbarVisibilityChange()
		{
			showStatusBarPeeker(getFullscreen());
			updateDsb();
			// TODO: Implement this method
		}
		
		
	};
	private void updateNightMode(){
		final boolean nightModeEnable = Settings.System.getInt(getContentResolver(), Setelan.NIGHT_MODE_ENABLE, 0) == 1;
		if(mNightModeViewAttached && nightModeEnable){
				//mNightModeView.setBackgroundColor(0x44000000);
			}
		else if(mNightModeViewAttached && (!nightModeEnable)){
				WindowManagerImpl.getDefault().removeView(mNightModeView);
				mNightModeViewAttached = false;
			}
		else if((!mNightModeViewAttached) && nightModeEnable){
			addNightModeView();
		}
	//	Log.d("NightMode", "Attached: "+ mNightModeViewAttached);
	}
	boolean qpAttached = false;
	int currentQpMusicMode = 0;
	public void updateQpMusicView() {		
		// 0=disable, 1=small, 2=big
		int mode = Settings.System.getInt(getContentResolver(), Setelan.HEADS_UP_MUSIC_QP_MODE,0);
		boolean enable = mode !=0;
		boolean modeChanged = currentQpMusicMode != mode;
		
        if (mQpMusicView != null && qpAttached && (!enable || modeChanged) ) {
			//mQpMusicView.dismiss();
            WindowManagerImpl.getDefault().removeView(mQpMusicView);
			qpAttached = false;
        }
		// if mode has been changed, re-add the view and update
		if (mQpMusicView != null && !qpAttached && enable && modeChanged) {
			  //mQpMusicView.show();
			  mQpMusicView.updateLayout();
			  addQPMusicView();
			  if(!mQpMusicView.isPlaying())mQpMusicView.setVisibility(View.GONE);
			  
			}
		
		currentQpMusicMode = mode;
     }
	 /* ---------
	  * Status Bar Icon
	  * ---------
	  */
    public void addIcon(String slot, int index, int viewIndex, StatusBarIcon icon) {
        if (SPEW_ICONS) {
            Slog.d(TAG, "addIcon slot=" + slot + " index=" + index + " viewIndex=" + viewIndex
                    + " icon=" + icon);
        }
        StatusBarIconView view = new StatusBarIconView(this, slot);
        view.set(icon);
		mStatusIcons.addView(view, new LinearLayout.LayoutParams(mIconSize, mIconSize));
		
//        mStatusIcons.addView(view, viewIndex, new LinearLayout.LayoutParams(mIconSize, mIconSize));
    }

    public void updateIcon(String slot, int index, int viewIndex,
            StatusBarIcon old, StatusBarIcon icon) {
        if (SPEW_ICONS) {
            Slog.d(TAG, "updateIcon slot=" + slot + " index=" + index + " viewIndex=" + viewIndex
                    + " old=" + old + " icon=" + icon);
        }
        StatusBarIconView view = (StatusBarIconView)mStatusIcons.getChildAt(viewIndex);
        view.set(icon);
		
    }

    public void removeIcon(String slot, int index, int viewIndex) {
        if (SPEW_ICONS) {
            Slog.d(TAG, "removeIcon slot=" + slot + " index=" + index + " viewIndex=" + viewIndex);
        }
        mStatusIcons.removeViewAt(viewIndex);
    }
    public void addNotification(IBinder key, StatusBarNotification notification) {
        boolean shouldTick = true;
        if (notification.notification.fullScreenIntent != null) {
            shouldTick = false;
            //Slog.d(TAG, "Notification has fullScreenIntent; sending fullScreenIntent");
            try {
                notification.notification.fullScreenIntent.send();
            } catch (PendingIntent.CanceledException e) {
            }
        } 

        StatusBarIconView iconView = addNotificationViews(key, notification);
		StatusBarIconView iconViewIntruder = mIntruderAlertView.addNotificationViews(key, notification);
			  
          if ((iconView == null) || (iconViewIntruder == null)) return;

     /*   if (shouldTicker && mShowNotif) {
            if (!shouldTick) {
                tick(notification);
            } else {
                IntroducerView();
            }
        }*/
		
		// anu
		final boolean headsupEnable= Settings.System.getInt(getContentResolver(), Setelan.HEADS_UP_NOTIF_ENABLE, 0) == 1;
		
        if (shouldTick) {
			if(headsupEnable){
				showIntroducerView(notification);
			}
			tick(notification);
        }
        
        // Recalculate the position of the sliding windows and the titles.
        setAreThereNotifications();
		mIntruderAlertView.setAreThereNotifications();
		
        updateExpandedViewPos(EXPANDED_LEAVE_ALONE);
    }

	public void updateNotification(IBinder key, StatusBarNotification notification) {
		mIntruderAlertView.updateNotification(key, notification);
		NotificationData oldList = null;
		int oldIndex = mOngoing.findEntry(key);
		if (oldIndex >= 0) {
			oldList = mOngoing;
		} else {
			oldIndex = mLatest.findEntry(key);
			if (oldIndex >= 0) {
				oldList = mLatest;
			} else {
				oldIndex = mMiniConData.findEntry(key);
				if (oldIndex < 0) {
					Slog.w("StatusBarService", "updateNotification for unknown key: " + key);
				} else {
					oldList = mMiniConData;
				}
			}
		}
		final NotificationData.Entry oldEntry = oldList.getEntryAt(oldIndex);
		final StatusBarNotification oldNotification = oldEntry.notification;
		RemoteViews oldContentView = oldNotification.notification.contentView;
		RemoteViews contentView = notification.notification.contentView;
		if (notification.notification.when != oldNotification.notification.when || notification.isOngoing() != oldNotification.isOngoing() || oldEntry.expanded == null || contentView == null || oldContentView == null || contentView.getPackage() == null || oldContentView.getPackage() == null || !oldContentView.getPackage().equals(contentView.getPackage()) || oldContentView.getLayoutId() != contentView.getLayoutId()) {
			removeNotificationViews(key);
			addNotificationViews(key, notification);
		} else {
			PendingIntent contentIntent;
			StatusBarIcon ic;
			oldEntry.notification = notification;
			try {
				if (!notification.isMiniCon()) {
					contentView.reapply(this, oldEntry.content);
				} else {
					contentView.reapply(this, oldEntry.expanded);
					Slog.i("StatusBarService", "UPDATE:MiniCon-" + notification.notification.twQuickPanelEvent);
				}
				contentIntent = notification.notification.contentIntent;
				if (contentIntent != null) {
					oldEntry.content.setOnClickListener(new Launcher(contentIntent, notification.pkg, notification.tag, notification.id));
				}
				ic = new StatusBarIcon(notification.pkg, notification.notification.icon, notification.notification.iconLevel, notification.notification.number);
				if (!oldEntry.icon.set(ic)) {
					handleNotificationError(key, notification, "Couldn't update icon: " + ic);
				}
			} catch (RuntimeException e) {
				Slog.w("StatusBarService", "Couldn't reapply views for package " + contentView.getPackage(), e);
				removeNotificationViews(key);
				addNotificationViews(key, notification);
			}
		}
		if (notification.notification.tickerText == null || TextUtils.equals(notification.notification.tickerText, oldEntry.notification.notification.tickerText)) {
			setAreThereNotifications();
			updateExpandedViewPos(EXPANDED_LEAVE_ALONE);
		} else {
			boolean headsupEnable= Settings.System.getInt(getContentResolver(), Setelan.HEADS_UP_NOTIF_ENABLE, 0) == 1;
			if(headsupEnable)showIntroducerView(notification);
			tick(notification);
			setAreThereNotifications();
			updateExpandedViewPos(EXPANDED_LEAVE_ALONE);
		}
	}
    public void removeNotification(IBinder key) {
        if (SPEW) Slog.d(TAG, "removeNotification key=" + key);
        StatusBarNotification old = removeNotificationViews(key);
        StatusBarNotification oldIntruder = mIntruderAlertView.removeNotificationViews(key);
		if (old != null || oldIntruder != null) {
            // Cancel the ticker if it's still running
            mTicker.removeEntry(old);

            // Recalculate the position of the sliding windows and the titles.
            setAreThereNotifications();
            mIntruderAlertView.setAreThereNotifications();
            updateExpandedViewPos(EXPANDED_LEAVE_ALONE);
        }
    }
	

    private int chooseIconIndex(boolean isMiniCon, boolean isOngoing, int viewIndex) {
        final int latestSize = mLatest.size();
			
		if (isMiniCon) {
			return (latestSize + mOngoing.size()) + (mMiniConData.size() - viewIndex);
			}
        else if (isOngoing) {
            return latestSize + (mOngoing.size() - viewIndex);
        } else {
            return latestSize - viewIndex;
        }
    }
    View[] makeNotificationView(final StatusBarNotification notification, ViewGroup parent) {
        Notification n = notification.notification;
        RemoteViews remoteViews =  n.contentView;
        if (remoteViews == null) {
            return null;
        }

        // create the row view
        LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LatestItemContainer row = (LatestItemContainer) inflater.inflate(R.layout.status_bar_latest_event, parent, false);
        if ((n.flags & Notification.FLAG_ONGOING_EVENT) == 0 && (n.flags & Notification.FLAG_NO_CLEAR) == 0) {
            row.setOnSwipeCallback(mTouchDispatcher, new Runnable() {
                public void run() {
                    try {
                        mBarService.onNotificationClear(notification.pkg, notification.tag, notification.id);

                    } catch (RemoteException e) {
                        // Skip it, don't crash.
                    }
                }
            });
        }

        // bind the click event to the content area
        content = (ViewGroup)row.findViewById(R.id.content);
     //   content.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
       // content.setOnFocusChangeListener(mFocusChangeListener);
        PendingIntent contentIntent = n.contentIntent;
        if (contentIntent != null) {
            content.setOnClickListener(new Launcher(contentIntent, notification.pkg,
                        notification.tag, notification.id));
        }

        View expanded = null;
        Exception exception = null;
        try {
            expanded = remoteViews.apply(this, content);
        }
        catch (RuntimeException e) {
            exception = e;
        }
        if (expanded == null) {
            String ident = notification. pkg + "/0x" + Integer.toHexString(notification.id);
            Slog.e(TAG, "couldn't inflate view for notification " + ident, exception);
            return null;
        } else {
            content.addView(expanded);
            row.setDrawingCacheEnabled(true);
        }

        return new View[] { row, content, expanded };
    }
		
	StatusBarIconView addNotificationViews(IBinder r14_IBinder, StatusBarNotification statusBarNotification) {
		NotificationData list;
		ViewGroup parent;
		boolean onGoing = statusBarNotification.isOngoing();
		boolean miniCon = statusBarNotification.isMiniCon();
		if (miniCon) {
			list = mMiniConData;
			parent = mMiniCon;
		} else if (onGoing) {
			list = mOngoing;
			parent = mOngoingItems;
		} else {
			list = mLatest;
			parent = mLatestItems;
		}
		View[] views = makeNotificationView(statusBarNotification, parent);
		if (views == null) {
			handleNotificationError(r14_IBinder, statusBarNotification, "Couldn't expand RemoteViews for: " + statusBarNotification);
			return null;
		} else {
			View row = views[0]; //r3
			View content = views[1]; //r4
			View expanded = views[2]; //r5
			StatusBarIconView r6_StatusBarIconView = new StatusBarIconView(this, statusBarNotification.pkg + "/0x" + Integer.toHexString(statusBarNotification.id));
			StatusBarIcon r1_StatusBarIcon = new StatusBarIcon(statusBarNotification.pkg, statusBarNotification.notification.icon, statusBarNotification.notification.iconLevel, statusBarNotification.notification.number);
			if (!r6_StatusBarIconView.set(r1_StatusBarIcon)) {
				handleNotificationError(r14_IBinder, statusBarNotification, "Coulding create icon: " + r1_StatusBarIcon);
				return null;
			} else {
				int r0i = list.add(r14_IBinder, statusBarNotification, row, content, expanded, r6_StatusBarIconView);
				if (miniCon) {
					((ViewGroup) content).removeView(expanded);
					parent.addView(expanded, r0i);
				//	Slog.i("StatusBarService", "ADD:MiniCon-" + statusBarNotification.notification.twQuickPanelEvent);
				} else {
					parent.addView(row, r0i);
				}
				mNotificationIcons.addView(r6_StatusBarIconView, chooseIconIndex(miniCon, onGoing, r0i));
							
				return r6_StatusBarIconView;
			}
		}
		
	}
	
	public void showQPMusicView(){
		if(mExpandedVisible)return;
		mHandler.sendEmptyMessage(MSG_SHOW_QP_MUSIC);
        mHandler.removeMessages(MSG_HIDE_QP_MUSIC);

		mHandler.sendEmptyMessageDelayed(MSG_HIDE_QP_MUSIC, QP_MUSIC_DELAY);

	}
	public void hideQPMusicViewNoDelay(){
		mHandler.removeMessages(MSG_HIDE_QP_MUSIC);
		
		setQPMusicViewVisibility(false);
	}
	private void setQPMusicViewVisibility(boolean b){
		if(b){
			mQpMusicView.show();
		}else{
			mQpMusicView.hide();
		}
	}
	private boolean intruderIsShown;
	public void showIntroducerView(StatusBarNotification notification) {
		// white list [TODO in KereSettings]
		if( !("com.android.mms".equals(notification.pkg)) ){
			return;
			}
			
		// special actions
		if( ("com.android.mms".equals(notification.pkg)) ){
			if(notification.notification.tickerText !=null){
				if(notification.notification.tickerText.toString().contains("Delivery"))return;
			}
		}
        mIntruderAlertView.updateLayout();
        mHandler.sendEmptyMessage(MSG_SHOW_INTRUDER);
        mHandler.removeMessages(MSG_HIDE_INTRUDER);
		intruderIsShown = true;
     
    }

    public void hideIntroducerView() {
        mIntruderAlertView.updateLayout();

        mHandler.sendEmptyMessage(MSG_HIDE_INTRUDER);
		intruderIsShown = false;
		if(mOnSBPeeked)stopPeekingStatusbar(false);
    }
	public void setIntruderAlertVisibility(boolean b){
		if(b){
			mIntruderAlertView.show();
		} else {
			mIntruderAlertView.hide();
		}
	}
	
	StatusBarNotification removeNotificationViews(IBinder r5_IBinder) {
		String r3_String = "StatusBarService";
		//mPreviousFocusValue = mExpandedView.getDescendantFocusability();
		//mFocusBlocked = true;
		//mExpandedView.setDescendantFocusability(393216);
		NotificationData.Entry r0_Entry = mOngoing.remove(r5_IBinder);
		if (r0_Entry == null) {
			r0_Entry = mLatest.remove(r5_IBinder);
			if (r0_Entry == null) {
				r0_Entry = mMiniConData.remove(r5_IBinder);
				if (r0_Entry == null) {
					Slog.w(r3_String, "removeNotification for unknown key: " + r5_IBinder);
					return null;
				}
			}
		}
		if (!r0_Entry.notification.isMiniCon()) {
			((ViewGroup) r0_Entry.row.getParent()).removeView(r0_Entry.row);
		} else {
			((ViewGroup) r0_Entry.expanded.getParent()).removeView(r0_Entry.expanded);
		//	Slog.i(r3_String, "REMOVE:MiniCon-" + r0_Entry.notification.notification.twQuickPanelEvent);
		}
		((ViewGroup) r0_Entry.icon.getParent()).removeView(r0_Entry.icon);
		return r0_Entry.notification;
	}
    private void setAreThereNotifications() {
        boolean ongoing = mOngoing.hasVisibleItems();
        boolean latest = mLatest.hasVisibleItems();
		
        // (no ongoing notifications are clearable)
        if (mLatest.hasClearableItems()) {
            mClearButton.setVisibility(View.VISIBLE);
        } else {
            mClearButton.setVisibility(View.GONE);
        }

        mOngoingTitle.setVisibility(ongoing ? View.VISIBLE : View.GONE);
        mLatestTitle.setVisibility(latest ? View.VISIBLE : View.GONE);

        if (ongoing || latest) {
            mNoNotificationsTitle.setVisibility(View.GONE);
        } else {
            mNoNotificationsTitle.setVisibility(View.VISIBLE);
        }
    }


    /**
     * State is one or more of the DISABLE constants from StatusBarManager.
     */
    public void disable(int state) {
        final int old = mDisabled;
        final int diff = state ^ old;
        mDisabled = state;

        if ((diff & StatusBarManager.DISABLE_EXPAND) != 0) {
            if ((state & StatusBarManager.DISABLE_EXPAND) != 0) {
                if (SPEW) Slog.d(TAG, "DISABLE_EXPAND: yes");
                animateCollapse();
            }
        }
        if ((diff & StatusBarManager.DISABLE_NOTIFICATION_ICONS) != 0) {
            if ((state & StatusBarManager.DISABLE_NOTIFICATION_ICONS) != 0) {
                if (SPEW) Slog.d(TAG, "DISABLE_NOTIFICATION_ICONS: yes");
                if (mTicking) {
                    mTicker.halt();
                } else {
                    setNotificationIconVisibility(false, com.android.internal.R.anim.fade_out);
                }
            } else {
                if (SPEW) Slog.d(TAG, "DISABLE_NOTIFICATION_ICONS: no");
                if (!mExpandedVisible) {
                    setNotificationIconVisibility(true, com.android.internal.R.anim.fade_in);
                }
            }
        } else if ((diff & StatusBarManager.DISABLE_NOTIFICATION_TICKER) != 0) {
            if (mTicking && (state & StatusBarManager.DISABLE_NOTIFICATION_TICKER) != 0) {
                if (SPEW) Slog.d(TAG, "DISABLE_NOTIFICATION_TICKER: yes");
                mTicker.halt();
            }
        }
    }

    /**
     * All changes to the status bar and notifications funnel through here and are batched.
     */
    private class H extends Handler {
        public void handleMessage(Message m) {
            switch (m.what) {
                case MSG_ANIMATE:
                    doAnimation();
                    break;
                case MSG_ANIMATE_REVEAL:
                    doRevealAnimation();
                    break;
				case MSG_SHOW_INTRUDER:
                    setIntruderAlertVisibility(true);
                    break;
                case MSG_HIDE_INTRUDER:
                    setIntruderAlertVisibility(false);
                    break;
				case MSG_SHOW_QP_MUSIC:
                    setQPMusicViewVisibility(true);
                    break;
                case MSG_HIDE_QP_MUSIC:
                    setQPMusicViewVisibility(false);
                    break;
				case MSG_MAKE_EXP_VISIBLE:
                    if(!mAlreadyUp)makeExpandedVisible();
                    break;
				case MSG_HIDE_PEEK_STATUS_BAR:
					stopPeekingStatusbar(false);
					break;
            }
        }
    }

    View.OnFocusChangeListener mFocusChangeListener = new View.OnFocusChangeListener() {
        public void onFocusChange(View v, boolean hasFocus) {
            // Because 'v' is a ViewGroup, all its children will be (un)selected
            // too, which allows marqueeing to work.
            v.setSelected(hasFocus);
        }
    };

    private void makeExpandedVisible() {
        if (SPEW) Slog.d(TAG, "Make expanded visible: expanded visible=" + mExpandedVisible);
        if (mExpandedVisible) {
            return;
        }
        mExpandedVisible = true;
        visibilityChanged(true);
	
		// my stuff
		updateDsb();
		mExpPanelView.updateTintDsb();
		setTintCloseView();
		hideQPMusicViewNoDelay();
		showButtonPanel(false);
		mTicker.setAllowTicking(false);
		hideIntroducerView();
		
        updateExpandedViewPos(EXPANDED_LEAVE_ALONE);
        mExpandedParams.flags &= ~WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        mExpandedParams.flags |= WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM;
        mExpandedDialog.getWindow().setAttributes(mExpandedParams);
        mExpandedView.requestFocus(View.FOCUS_FORWARD);
		mBgPanelView.setVisibility(View.VISIBLE);
        mTrackingView.setVisibility(View.VISIBLE);
		
        mExpandedView.setVisibility(View.VISIBLE);

    /*    if (!mTicking) {
            setDateViewVisibility(true, com.android.internal.R.anim.fade_in);
        }*/
		
    }

    public void animateExpand() {
     //   if (SPEW) Slog.d(TAG, "Animate expand: expanded=" + mExpanded);
        if ((mDisabled & StatusBarManager.DISABLE_EXPAND) != 0) {
            return ;
        }
        if (mExpanded) {
            return;
        }
		
        prepareTracking(0, true);
        performFling(0, 2000.0f, true);
    }

    public void animateCollapse() {
      /*  if (SPEW) {
            Slog.d(TAG, "animateCollapse(): mExpanded=" + mExpanded
                    + " mExpandedVisible=" + mExpandedVisible
                    + " mExpanded=" + mExpanded
                    + " mAnimating=" + mAnimating
                    + " mAnimY=" + mAnimY
                    + " mAnimVel=" + mAnimVel); 
        }
*/

        if (!mExpandedVisible) {
            return;
        }

        int y;
        if (mAnimating) {
            y = (int)mAnimY;
        } else {
            y = mDisplay.getHeight()-1;
        }
		
		// animate togel sit sedurunge collapse 
		mExpPanelView.closeQs();
        // Let the fling think that we're open so it goes in the right direction
        // and doesn't try to re-open the windowshade.
        mExpanded = true;
        prepareTracking(y, false);
        performFling(y, -800.0f, true);
		
    }
	private void delayedAnimateCollapse(){
		delayedAnimateCollapse(true);
	}
	
	public float getDisplayHeight(){
		return mDisplayHeight = mDisplay.getHeight();

	}
	public void delayedAnimateCollapse(boolean animate){
		if(animate)mExpPanelView.closeQs();
		
		Runnable mRun=new Runnable(){
			@Override
			public void run(){
				animateCollapse();
			}
		};
		mHandler.postDelayed(mRun, 300);
	}
    void performExpand() {
        if (SPEW) Slog.d(TAG, "performExpand: mExpanded=" + mExpanded);
        if ((mDisabled & StatusBarManager.DISABLE_EXPAND) != 0) {
            return ;
        }
        if (mExpanded) {
            return;
        }

        mExpanded = true;
        makeExpandedVisible();
        updateExpandedViewPos(EXPANDED_FULL_OPEN);

        if (false) postStartTracing();
    }

    void performCollapse() {
        if (SPEW) Slog.d(TAG, "performCollapse: mExpanded=" + mExpanded
                + " mExpandedVisible=" + mExpandedVisible
                + " mTicking=" + mTicking);

        if (!mExpandedVisible) {
            return;
        }
        mExpandedVisible = false;
        visibilityChanged(false);
		
		// my stuff
		updateDsb();
		mTicker.setAllowTicking(true);
		if(mOnSBPeeked)stopPeekingStatusbar(false);
		
        mExpandedParams.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        mExpandedParams.flags &= ~WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM;
        mExpandedDialog.getWindow().setAttributes(mExpandedParams);
		mBgPanelView.setVisibility(View.GONE);
        mTrackingView.setVisibility(View.GONE);
        mExpandedView.setVisibility(View.GONE);

        if ((mDisabled & StatusBarManager.DISABLE_NOTIFICATION_ICONS) == 0) {
            setNotificationIconVisibility(true, com.android.internal.R.anim.fade_in);
        }
   /*     if (mDateView.getVisibility() == View.VISIBLE) {
            setDateViewVisibility(false, com.android.internal.R.anim.fade_out);
        }
*/
        if (!mExpanded) {
            return;
        }
        mExpanded = false;
    }

    void doAnimation() {
        if (mAnimating) {
         //   if (SPEW) Slog.d(TAG, "doAnimation");
          //  if (SPEW) Slog.d(TAG, "doAnimation before mAnimY=" + mAnimY);
            incrementAnim();
         //   if (SPEW) Slog.d(TAG, "doAnimation after  mAnimY=" + mAnimY);
            if (mAnimY >= mDisplay.getHeight()-1) {
             //   if (SPEW) Slog.d(TAG, "Animation completed to expanded state.");
                mAnimating = false;
                updateExpandedViewPos(EXPANDED_FULL_OPEN);
                performExpand();
            }
            else if (mAnimY < mStatusBarView.getHeight()) {
             //   if (SPEW) Slog.d(TAG, "Animation completed to collapsed state.");
                mAnimating = false;
                updateExpandedViewPos(0);
                performCollapse();
            }
            else {
                updateExpandedViewPos((int)mAnimY);
                mCurAnimationTime += ANIM_FRAME_DURATION;
                mHandler.sendMessageAtTime(mHandler.obtainMessage(MSG_ANIMATE), mCurAnimationTime);
            }
        }
    }

    void stopTracking() {
        mTracking = false;
        mVelocityTracker.recycle();
        mVelocityTracker = null;
    }

    void incrementAnim() {
        long now = SystemClock.uptimeMillis();
        float t = ((float)(now - mAnimLastTime)) / 1000;            // ms -> s
        final float y = mAnimY;
        final float v = mAnimVel;                                   // px/s
        final float a = mAnimAccel;                                 // px/s/s
        mAnimY = y + (v*t) + (0.2f*a*t*t);                          // px
        mAnimVel = v + (a*t);                                       // px/s
        mAnimLastTime = now;                                        // ms
		/*if(false){Log.d("revealAnimIncrement", "animY:"+(int) mAnimY);
        Log.d(TAG+"revealINCREMENT", "y=" + y + " v=" + v + " a=" + a + " t=" + t + " mAnimY=" + mAnimY
                + " mAnimAccel=" + mAnimAccel);

			}*/
    }

    void doRevealAnimation() {
	
        final int h = mCloseView.getHeight() + mStatusBarView.getHeight();
		//Log.d("revealAnim", "animY:"+(int) mAnimY+" h:"+h);
        if (mAnimatingReveal && mAnimating && mAnimY < h) {
            incrementAnim();
            if (mAnimY >= h) {
                mAnimY = h;
                updateExpandedViewPos((int)mAnimY);
            } else {
                updateExpandedViewPos((int)mAnimY);
                mCurAnimationTime += ANIM_FRAME_DURATION;
                mHandler.sendMessageAtTime(mHandler.obtainMessage(MSG_ANIMATE_REVEAL),
                        mCurAnimationTime);
            }
        }
    }
	void prepareTracking(int y, boolean opening) {
		prepareTracking(y,opening,false);
		}
    void prepareTracking(int y, boolean opening, boolean delay) {
        mTracking = true;
        mVelocityTracker = VelocityTracker.obtain();
        if (opening) {
            mAnimAccel = 100.0f;
            mAnimVel = 10;
            mAnimY = mStatusBarView.getHeight();
            updateExpandedViewPos((int)mAnimY);
            mAnimating = true;
            mAnimatingReveal = true;
            mHandler.removeMessages(MSG_ANIMATE);
            mHandler.removeMessages(MSG_ANIMATE_REVEAL);
            long now = SystemClock.uptimeMillis();
            mAnimLastTime = now;
            mCurAnimationTime = now + ANIM_FRAME_DURATION;
            mAnimating = true;
            
					
			mHandler.removeMessages(MSG_MAKE_EXP_VISIBLE);
            if(delay){
				mHandler.sendEmptyMessageDelayed(MSG_MAKE_EXP_VISIBLE,300);
				mHandler.sendMessageAtTime(mHandler.obtainMessage(MSG_ANIMATE_REVEAL),
										   mCurAnimationTime + 340);
				
			}
			else {
				mHandler.sendMessageAtTime(mHandler.obtainMessage(MSG_ANIMATE_REVEAL),
										   mCurAnimationTime);
				makeExpandedVisible(); 
			}
        } else {
            // it's open, close it?
            if (mAnimating) {
                mAnimating = false;
                mHandler.removeMessages(MSG_ANIMATE);
            }
            updateExpandedViewPos(y + mViewDelta);
        }
    }

    void performFling(int y, float vel, boolean always) {
        mAnimatingReveal = false;
        mDisplayHeight = mDisplay.getHeight();

        mAnimY = y;
        mAnimVel = vel;

        //Slog.d(TAG, "starting with mAnimY=" + mAnimY + " mAnimVel=" + mAnimVel);

        if (mExpanded) {
            if (!always && (
                    vel > 200.0f
                    || (y > (mDisplayHeight-25) && vel > -200.0f))) {
                // We are expanded, but they didn't move sufficiently to cause
                // us to retract.  Animate back to the expanded position.
                mAnimAccel = 2000.0f;
                if (vel < 0) {
                    mAnimVel = 0;
                }
            }
            else {
                // We are expanded and are now going to animate away.
                mAnimAccel = -2000.0f;
                if (vel > 0) {
                    mAnimVel = 0;
                }
            }
        } else {
            if (always || (
                    vel > 200.0f
                    || (y > (mDisplayHeight/2) && vel > -200.0f))) {
                // We are collapsed, and they moved enough to allow us to
                // expand.  Animate in the notifications.
                mAnimAccel = 2000.0f;
                if (vel < 0) {
                    mAnimVel = 0;
                }
            }
            else {
                // We are collapsed, but they didn't move sufficiently to cause
                // us to retract.  Animate back to the collapsed position.
                mAnimAccel = -2000.0f;
                if (vel > 0) {
                    mAnimVel = 0;
                }
            }
        }
     //   Log.d(TAG+"PERFLING", "mAnimY=" + mAnimY + " mAnimVel=" + mAnimVel
         //       + " mAnimAccel=" + mAnimAccel);

        long now = SystemClock.uptimeMillis();
        mAnimLastTime = now;
        mCurAnimationTime = now + ANIM_FRAME_DURATION;
        mAnimating = true;
        mHandler.removeMessages(MSG_ANIMATE);
        mHandler.removeMessages(MSG_ANIMATE_REVEAL);
        mHandler.sendMessageAtTime(mHandler.obtainMessage(MSG_ANIMATE), mCurAnimationTime);
        stopTracking();
    }
	/*
	private long doubleTouchElapsedTime;
	private int doubleTouchFirstX;
	private boolean mDoubleTapToSleepEnable = true;
	private boolean mButtonPanelEnable = true;
	*/
	private boolean mAlreadyUp ;

	private int lasty;
    boolean interceptTouchEvent(MotionEvent event) {
        if (SPEW) {
          //  Slog.d(TAG, "Touch: rawY=" + event.getRawY() + " event=" + event + " mDisabled="
           //     + mDisabled);
        }

        if ((mDisabled & StatusBarManager.DISABLE_EXPAND) != 0) {
            return false;
        }
	
        final int statusBarSize = mStatusBarView.getHeight();
		
        final int hitSize = statusBarSize*2;
		
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
			mAlreadyUp = false;
            final int y = (int)event.getRawY();
			lasty = y;
            if (!mExpanded) {
                mViewDelta = statusBarSize - y;				
            } else {
                mTrackingView.getLocationOnScreen(mAbsPos);
                mViewDelta = mAbsPos[1] + mTrackingView.getHeight() - y;
            }
			
            if ((!mExpanded && y < hitSize) ||
                    (mExpanded && y > (mDisplay.getHeight()-hitSize))) {

                // We drop events at the edge of the screen to make the windowshade come
                // down by accident less, especially when pushing open a device with a keyboard
                // that rotates (like g1 and droid)
                final int x = (int)event.getRawX();
                final int edgeBorder = mEdgeBorder;
                if (x >= edgeBorder && x < mDisplay.getWidth() - edgeBorder) {
                    prepareTracking(y, !mExpanded, true);// opening if we're not already fully visible
                    mVelocityTracker.addMovement(event);
                }
            }
        } else if (mTracking) {
            mVelocityTracker.addMovement(event);
            final int minY = statusBarSize + mCloseView.getHeight();
            if (event.getAction() == MotionEvent.ACTION_MOVE) {
				final int y = (int)event.getRawY();
				if ((y - lasty)>3 && !mExpandedVisible && mAnimatingReveal){
					mHandler.removeMessages(MSG_ANIMATE_REVEAL);
					mHandler.removeMessages(MSG_MAKE_EXP_VISIBLE);
					
					mHandler.sendMessage(mHandler.obtainMessage(MSG_MAKE_EXP_VISIBLE));
					mHandler.sendMessageAtTime(mHandler.obtainMessage(MSG_ANIMATE_REVEAL), SystemClock.uptimeMillis() +40);
					
					}
                if (mAnimatingReveal && y < minY) {
                    // nothing
                }else  {
					
                    mAnimatingReveal = false;
                    updateExpandedViewPos(y + mViewDelta);
                }		

            } else if (event.getAction() == MotionEvent.ACTION_UP) {
				mAlreadyUp = true;
                mVelocityTracker.computeCurrentVelocity(1000);

                final float yVel = mVelocityTracker.getYVelocity();
                final boolean negative = yVel < 0;

                float xVel = mVelocityTracker.getXVelocity();
                if (xVel < 0) {
                    xVel = -xVel;
                }
                if (xVel > 150.0f) {
                    xVel = 150.0f; // limit how much we care about the x axis
                }
                float vel = (float)Math.hypot(yVel, xVel);
                if (negative) {
                    vel = -vel;
                }

                performFling((int)event.getRawY(), vel, false);
            }

        }
        return false;
    }
	public Launcher makeLauncher(PendingIntent intent, String pkg, String tag, int id) {
        return new Launcher(intent, pkg, tag, id);
    }
    public class Launcher implements View.OnClickListener {
        private PendingIntent mIntent;
        private String mPkg;
        private String mTag;
        private int mId;

        Launcher(PendingIntent intent, String pkg, String tag, int id) {
            mIntent = intent;
            mPkg = pkg;
            mTag = tag;
            mId = id;
        }

        public void onClick(View v) {
            try {
                // The intent we are sending is for the application, which
                // won't have permission to immediately start an activity after
                // the user switches to home.  We know it is safe to do at this
                // point, so make sure new activity switches are now allowed.
                ActivityManagerNative.getDefault().resumeAppSwitches();
            } catch (RemoteException e) {
            }

            if (mIntent != null) {
                int[] pos = new int[2];
                v.getLocationOnScreen(pos);
                Intent overlay = new Intent();
                overlay.setSourceBounds(
                        new Rect(pos[0], pos[1], pos[0]+v.getWidth(), pos[1]+v.getHeight()));
                try {
                    mIntent.send(StatusBarService.this, 0, overlay);
                } catch (PendingIntent.CanceledException e) {
                    // the stack trace isn't very helpful here.  Just log the exception message.
                    Slog.w(TAG, "Sending contentIntent failed: " + e);
                }
            }

            try {
                mBarService.onNotificationClick(mPkg, mTag, mId);
            } catch (RemoteException ex) {
                // system process is dead if we're here.
            }

            // close the shade if it was open
            animateCollapse();
        }
    }

    private void tick(StatusBarNotification n) {
        // Show the ticker if one is requested. Also don't do this
        // until status bar window is attached to the window manager,
        // because...  well, what's the point otherwise?  And trying to
        // run a ticker without being attached will crash!
        if (n.notification.tickerText != null && mStatusBarView.getWindowToken() != null) {
            if (0 == (mDisabled & (StatusBarManager.DISABLE_NOTIFICATION_ICONS
                            | StatusBarManager.DISABLE_NOTIFICATION_TICKER))) {
                mTicker.addEntry(n);
				
				//peek
				boolean peekNewNotif = Settings.System.getInt(getContentResolver(), Setelan.PEEK_NEW_NOTIF, 0) == 1;
				if(getFullscreen() && peekNewNotif){
					peekStatusBar(true);
				}
			}
        }
		
    }

    /**
     * Cancel this notification and tell the StatusBarManagerService / NotificationManagerService
     * about the failure.
     *
     * WARNING: this will call back into us.  Don't hold any locks.
     */
    void handleNotificationError(IBinder key, StatusBarNotification n, String message) {
        removeNotification(key);
        try {
            mBarService.onNotificationError(n.pkg, n.tag, n.id, n.uid, n.initialPid, message);
        } catch (RemoteException ex) {
            // The end is nigh.
        }
    }

    private class MyTicker extends Ticker {
        MyTicker(Context context, StatusBarView sb) {
            super(context, sb);
        }

        @Override
        void tickerStarting() {
            if (SPEW) Slog.d(TAG, "tickerStarting");
            mTicking = true;
            mIcons.setVisibility(View.GONE);
            mTickerView.setVisibility(View.VISIBLE);
            mTickerView.startAnimation(loadAnim(com.android.internal.R.anim.push_up_in, null));
            mIcons.startAnimation(loadAnim(com.android.internal.R.anim.push_up_out, null));
            if (mExpandedVisible) {
             //   setDateViewVisibility(false, com.android.internal.R.anim.push_up_out);
            }
        }

        @Override
        void tickerDone() {
            if (SPEW) Slog.d(TAG, "tickerDone");
            mTicking = false;
            mIcons.setVisibility(View.VISIBLE);
            mTickerView.setVisibility(View.GONE);
            mIcons.startAnimation(loadAnim(com.android.internal.R.anim.push_down_in, null));
            mTickerView.startAnimation(loadAnim(com.android.internal.R.anim.push_down_out, null));
            if (mExpandedVisible) {
         //       setDateViewVisibility(true, com.android.internal.R.anim.push_down_in);
            }
			if(mOnSBPeeked && !intruderIsShown)stopPeekingStatusbar(false);

        }

        void tickerHalting() {
            if (SPEW) Slog.d(TAG, "tickerHalting");
            mTicking = false;
            mIcons.setVisibility(View.VISIBLE);
            mTickerView.setVisibility(View.GONE);
            mIcons.startAnimation(loadAnim(com.android.internal.R.anim.fade_in, null));
            mTickerView.startAnimation(loadAnim(com.android.internal.R.anim.fade_out, null));
            if (mExpandedVisible) {
    //            setDateViewVisibility(true, com.android.internal.R.anim.fade_in);
            }
        }
    }

    private Animation loadAnim(int id, Animation.AnimationListener listener) {
        Animation anim = AnimationUtils.loadAnimation(StatusBarService.this, id);
        if (listener != null) {
            anim.setAnimationListener(listener);
        }
        return anim;
    }

    public String viewInfo(View v) {
        return "(" + v.getLeft() + "," + v.getTop() + ")(" + v.getRight() + "," + v.getBottom()
                + " " + v.getWidth() + "x" + v.getHeight() + ")";
    }

    protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (checkCallingOrSelfPermission(android.Manifest.permission.DUMP)
                != PackageManager.PERMISSION_GRANTED) {
            pw.println("Permission Denial: can't dump StatusBar from from pid="
                    + Binder.getCallingPid()
                    + ", uid=" + Binder.getCallingUid());
            return;
        }

        synchronized (mQueueLock) {
            pw.println("Current Status Bar state:");
            pw.println("  mExpanded=" + mExpanded
                    + ", mExpandedVisible=" + mExpandedVisible);
            pw.println("  mTicking=" + mTicking);
            pw.println("  mTracking=" + mTracking);
            pw.println("  mAnimating=" + mAnimating
                    + ", mAnimY=" + mAnimY + ", mAnimVel=" + mAnimVel
                    + ", mAnimAccel=" + mAnimAccel);
            pw.println("  mCurAnimationTime=" + mCurAnimationTime
                    + " mAnimLastTime=" + mAnimLastTime);
            pw.println("  mDisplayHeight=" + mDisplayHeight
                    + " mAnimatingReveal=" + mAnimatingReveal
                    + " mViewDelta=" + mViewDelta);
            pw.println("  mDisplayHeight=" + mDisplayHeight);
            pw.println("  mExpandedParams: " + mExpandedParams);
            pw.println("  mExpandedView: " + viewInfo(mExpandedView));
            pw.println("  mExpandedDialog: " + mExpandedDialog);
            pw.println("  mTrackingParams: " + mTrackingParams);
            pw.println("  mTrackingView: " + viewInfo(mTrackingView));
            pw.println("  mOngoingTitle: " + viewInfo(mOngoingTitle));
            pw.println("  mOngoingItems: " + viewInfo(mOngoingItems));
            pw.println("  mLatestTitle: " + viewInfo(mLatestTitle));
            pw.println("  mLatestItems: " + viewInfo(mLatestItems));
            pw.println("  mNoNotificationsTitle: " + viewInfo(mNoNotificationsTitle));
            pw.println("  mCloseView: " + viewInfo(mCloseView));
            pw.println("  mTickerView: " + viewInfo(mTickerView));
            pw.println("  mScrollView: " + viewInfo(mScrollView)
                    + " scroll " + mScrollView.getScrollX() + "," + mScrollView.getScrollY());
            pw.println("mNotificationLinearLayout: " + viewInfo(mNotificationLinearLayout));
        }

        if (true) {
            // must happen on ui thread
            mHandler.post(new Runnable() {
                    public void run() {
                        Slog.d(TAG, "mStatusIcons:");
                        mStatusIcons.debug();
                    }
                });
        }

    }
	void onBarViewAttached() {
		WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
			ViewGroup.LayoutParams.MATCH_PARENT, 
			ViewGroup.LayoutParams.MATCH_PARENT,
			WindowManager.LayoutParams.TYPE_STATUS_BAR_FULL_PANEL,
			0
			| WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
			| WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
			| WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
			//			| WindowManager.LayoutParams.FLAG_DIM_BEHIND,
			PixelFormat.TRANSLUCENT);

        lp.setTitle("BgPanelView");
        WindowManagerImpl.getDefault().addView(mBgPanelView, lp);
		
	}
	void onBgPanelViewAttached(){
		WindowManager.LayoutParams lp;
        int pixelFormat;
        Drawable bg;

        /// ---------- Tracking View --------------
        pixelFormat = PixelFormat.RGBX_8888;
        bg = mTrackingView.getBackground();
        if (bg != null) {
            pixelFormat = bg.getOpacity();
        }

        lp = new WindowManager.LayoutParams(
			ViewGroup.LayoutParams.MATCH_PARENT,
			ViewGroup.LayoutParams.MATCH_PARENT,
			// ## full expanded TYPE_STATUS_BAR_PANEL 
			//                           diganti ↓
			WindowManager.LayoutParams.TYPE_STATUS_BAR_FULL_PANEL,
			WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
			| WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
			| WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM,
			//| WindowManager.LayoutParams.FLAG_DIM_BEHIND,
			pixelFormat);
//        lp.token = mStatusBarView.getWindowToken();
        lp.gravity = Gravity.TOP | Gravity.FILL_HORIZONTAL;
        lp.setTitle("TrackingView");

        lp.y = mTrackingPosition;
        mTrackingParams = lp;

        WindowManagerImpl.getDefault().addView(mTrackingView, lp);
		
	}
    
    void onTrackingViewAttached() {
        WindowManager.LayoutParams lp;
        int pixelFormat;
        Drawable bg;

        /// ---------- Expanded View --------------
        pixelFormat = PixelFormat.TRANSLUCENT;

        final int disph = mDisplay.getHeight();
        lp = mExpandedDialog.getWindow().getAttributes();
        lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
        lp.height = getExpandedHeight();
        lp.x = 0;
        mTrackingPosition = lp.y = -disph; // sufficiently large negative
		// ## full expanded
        lp.type =WindowManager.LayoutParams.TYPE_STATUS_BAR_FULL_PANEL;// WindowManager.LayoutParams.TYPE_STATUS_BAR_PANEL;
        lp.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
				
                | WindowManager.LayoutParams.FLAG_DITHER
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        lp.format = pixelFormat;
        lp.gravity = Gravity.TOP | Gravity.FILL_HORIZONTAL;
        lp.setTitle("StatusBarExpanded");
        mExpandedDialog.getWindow().setAttributes(lp);
        mExpandedDialog.getWindow().setFormat(pixelFormat);
        mExpandedParams = lp;

        mExpandedDialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        mExpandedDialog.setContentView(mExpandedView,
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                           ViewGroup.LayoutParams.MATCH_PARENT));
										   
		mExpandedDialog.getWindow().setBackgroundDrawable(null);
        mExpandedDialog.show();
        FrameLayout hack = (FrameLayout)mExpandedView.getParent();
    }

/*    void setDateViewVisibility(boolean visible, int anim) {
        mDateView.setUpdates(visible);
        mDateView.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
        mDateView.startAnimation(loadAnim(anim, null));
    }*/

    void setNotificationIconVisibility(boolean visible, int anim) {
        int old = mNotificationIcons.getVisibility();
        int v = visible ? View.VISIBLE : View.INVISIBLE;
        if (old != v) {
            mNotificationIcons.setVisibility(v);
            mNotificationIcons.startAnimation(loadAnim(anim, null));
        }
    }
	
	boolean log = true;
    public void updateExpandedViewPos(int expandedPosition) {
        if (SPEW) {
           /* Slog.d(TAG, "updateExpandedViewPos before expandedPosition=" + expandedPosition
                    + " mTrackingParams.y=" 
                    + ((mTrackingParams == null) ? "???" : mTrackingParams.y)
                    + " mTrackingPosition=" + mTrackingPosition);*/
        }
		// ## full expanded
        final int h =0; // mStatusBarView.getHeight();
        final int disph = mDisplay.getHeight();

        // If the expanded view is not visible, make sure they're still off screen.
        // Maybe the view was resized.
        if (!mExpandedVisible) {
            if (mTrackingView != null) {
                mTrackingPosition = -disph;
                if (mTrackingParams != null) {
                    mTrackingParams.y = mTrackingPosition;
                    WindowManagerImpl.getDefault().updateViewLayout(mTrackingView, mTrackingParams);
                }
            }
            if (mExpandedParams != null) {
                mExpandedParams.y = -disph;
                mExpandedDialog.getWindow().setAttributes(mExpandedParams);
            }
            return;
        }

        // tracking view...
        int pos;
        if (expandedPosition == EXPANDED_FULL_OPEN) {
            pos = h;
        }
        else if (expandedPosition == EXPANDED_LEAVE_ALONE) {
            pos = mTrackingPosition;
        }
        else {
            if (expandedPosition <= disph) {
                pos = expandedPosition;
            } else {
                pos = disph;
            }
            pos -= disph-h;
        }
        mTrackingPosition = mTrackingParams.y = pos;
		mTrackingParams.height = disph-h;
		WindowManagerImpl.getDefault().updateViewLayout(mTrackingView, mTrackingParams);
		
		final int maxalpha = disph/2;
		//Log.d("bgtransition","bgalpha:"+bgalpha+" disph+pos:"+(disph+pos)+" alphafraction:"+(float)BG_PANEL_ALPHA/(disph*3/4)+" disph3/4:"+disph*3/4);
		if(pos<maxalpha){
			final int bgalpha = (int) (Math.min(maxalpha,(disph+pos))*( (float) BG_PANEL_ALPHA/(maxalpha)) ) ;
			mBgPanelView.updatebgalpha(bgalpha);
		}
		
        

        if (mExpandedParams != null) {
            mCloseView.getLocationInWindow(mPositionTmp);
            final int closePos = mPositionTmp[1];

            mExpandedContents.getLocationInWindow(mPositionTmp);
			
            final int contentsBottom = mPositionTmp[1] + mExpandedContents.getHeight();

            if (expandedPosition != EXPANDED_LEAVE_ALONE) {
                mExpandedParams.y = pos + mTrackingView.getHeight()
                        - (mTrackingParams.height-closePos) - contentsBottom;
                int max = h;
                if (mExpandedParams.y > max) {
                    mExpandedParams.y = max;
                }
                int min = mTrackingPosition;
                if (mExpandedParams.y < min) {
                    mExpandedParams.y = min;
                }

                final boolean visible = (mTrackingPosition + mTrackingView.getHeight()) > h;
                if (!visible) {
                    // if the contents aren't visible, move the expanded view way off screen
                    // because the window itself extends below the content view.
                    mExpandedParams.y = -disph;
                }
				
				mExpandedDialog.getWindow().setAttributes(mExpandedParams);

             //   if (SPEW) Slog.d(TAG, "updateExpandedViewPos visibilityChanged(" + visible + ")");
                visibilityChanged(visible);
            }
        }

     /*   if (SPEW) {
            Slog.d(TAG, "updateExpandedViewPos after  expandedPosition=" + expandedPosition
                    + " mTrackingParams.y=" + mTrackingParams.y
                    + " mTrackingPosition=" + mTrackingPosition
                    + " mExpandedParams.y=" + mExpandedParams.y
                    + " mExpandedParams.height=" + mExpandedParams.height); 
        }*/
    }
// ## full expanded
    int getExpandedHeight() {
		
        return mDisplay.getHeight() /*- mStatusBarView.getHeight() */- mCloseView.getHeight();
    }

    void updateExpandedHeight() {
        if (mExpandedView != null) {
            mExpandedParams.height = getExpandedHeight();
            mExpandedDialog.getWindow().setAttributes(mExpandedParams);
        }
    }

    /**
     * The LEDs are turned o)ff when the notification panel is shown, even just a little bit.
     * This was added last-minute and is inconsistent with the way the rest of the notifications
     * are handled, because the notification isn't really cancelled.  The lights are just
     * turned off.  If any other notifications happen, the lights will turn back on.  Steve says
     * this is what he wants. (see bug 1131461)
     */
    void visibilityChanged(boolean visible) {
        if (mPanelSlightlyVisible != visible) {
            mPanelSlightlyVisible = visible;
            try {
                mBarService.onPanelRevealed();
            } catch (RemoteException ex) {
                // Won't fail unless the world has ended.
            }
        }
    }

    void performDisableActions(int net) {
        int old = mDisabled;
        int diff = net ^ old;
        mDisabled = net;

        // act accordingly
        if ((diff & StatusBarManager.DISABLE_EXPAND) != 0) {
            if ((net & StatusBarManager.DISABLE_EXPAND) != 0) {
                Slog.d(TAG, "DISABLE_EXPAND: yes");
                animateCollapse();
            }
        }
        if ((diff & StatusBarManager.DISABLE_NOTIFICATION_ICONS) != 0) {
            if ((net & StatusBarManager.DISABLE_NOTIFICATION_ICONS) != 0) {
                Slog.d(TAG, "DISABLE_NOTIFICATION_ICONS: yes");
                if (mTicking) {
                    mNotificationIcons.setVisibility(View.INVISIBLE);
                    mTicker.halt();
                } else {
                    setNotificationIconVisibility(false, com.android.internal.R.anim.fade_out);
                }
            } else {
                Slog.d(TAG, "DISABLE_NOTIFICATION_ICONS: no");
                if (!mExpandedVisible) {
                    setNotificationIconVisibility(true, com.android.internal.R.anim.fade_in);
                }
            }
        } else if ((diff & StatusBarManager.DISABLE_NOTIFICATION_TICKER) != 0) {
            Slog.d(TAG, "DISABLE_NOTIFICATION_TICKER: "
                + (((net & StatusBarManager.DISABLE_NOTIFICATION_TICKER) != 0)
                    ? "yes" : "no"));
            if (mTicking && (net & StatusBarManager.DISABLE_NOTIFICATION_TICKER) != 0) {
                mTicker.halt();
            }
        }
    }

    private View.OnClickListener mClearButtonListener = new View.OnClickListener() {
        public void onClick(View v) {
            clearAllNotifications();
            animateCollapse();
        }
    };
	public void clearAllNotifications(){
		try {
                mBarService.onClearAllNotifications();
            } catch (RemoteException ex) {
                // system process is dead if we're here.
            }
	}
	public IStatusBarService getServiceBar(){
		return mBarService;
	}
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_CLOSE_SYSTEM_DIALOGS.equals(action)) {
                animateCollapse();
				}
            else if (Intent.ACTION_CONFIGURATION_CHANGED.equals(action)) {
                updateResources();
            }
			else if(Intent.ACTION_SCREEN_OFF.equals(action)){
				mScreenOff = true;
				animateCollapse();
				updateDsb();
				showButtonPanel(false);
			}
			else if(Intent.ACTION_SCREEN_ON.equals(action)){
				
				mScreenOff = false;
				updateDsb();
				}
			else if(INTENT_NIGHTMODE_DIALOG.equals(action)){
				final boolean collapse = intent.getIntExtra("expCollapse",0) == 1;
				// show night mode dialog
				if(collapse)animateCollapse();
				showNightModeDialog();
			}
		}
    };

    /**
     * Reload some of our resources when the configuration changes.
     *
     * We don't reload everything when the configuration changes -- we probably
     * should, but getting that smooth is tough.  Someday we'll fix that.  In the
     * meantime, just update the things that we know change.
     */
    void updateResources() {
        Resources res = getResources();

    //    mClearButton.setText(getText(R.string.status_bar_clear_all_button));
        mOngoingTitle.setText(getText(R.string.status_bar_ongoing_events_title));
        mLatestTitle.setText(getText(R.string.status_bar_latest_events_title));
        mNoNotificationsTitle.setText(getText(R.string.status_bar_no_notifications_title));

        mEdgeBorder = res.getDimensionPixelSize(R.dimen.status_bar_edge_ignore);

        if (false) Slog.v(TAG, "updateResources");
    }

    //
    // tracing
    //

    void postStartTracing() {
        mHandler.postDelayed(mStartTracing, 3000);
    }

    void vibrate() {
        android.os.Vibrator vib = (android.os.Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
        vib.vibrate(250);
    }

    Runnable mStartTracing = new Runnable() {
        public void run() {
            vibrate();
            SystemClock.sleep(250);
            Slog.d(TAG, "startTracing");
            android.os.Debug.startMethodTracing("/data/statusbar-traces/trace");
            mHandler.postDelayed(mStopTracing, 10000);
        }
    };

    Runnable mStopTracing = new Runnable() {
        public void run() {
            android.os.Debug.stopMethodTracing();
            Slog.d(TAG, "stopTracing");
            vibrate();
        }
    };
	
}

