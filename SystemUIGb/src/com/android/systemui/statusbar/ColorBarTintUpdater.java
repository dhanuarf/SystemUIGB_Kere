package com.android.systemui.statusbar;

import android.os.*;
import android.util.*;
import android.content.*;

public class ColorBarTintUpdater
{
	
	/*
	* Updater class (broadcast sender)
	*
	*/
	
	private static Intent mIntentDsb;
	
	public synchronized static void init(Context c){
		mContext = c;
		mIntentDsb = new Intent("com.sec.android.app.screencapture.capture");
		mIntentDsb.addCategory(Intent.CATEGORY_DEFAULT);
		mIntentDsb.putExtra("dsb",1);
	}
	public static void setUpdateTime(int i){
		sMinDelay = i;
	}
	public static void setIsButtonPanelShown(boolean isshown){
		sIsButtonPanelShown = isshown;
	}
//	public void disableDsb(){
//		Intent i = new Intent("statusbarmod.dsb");
//		i.putExtra("dsbColor", 666); //custom number (rung kanggo)
//		mService.sendBroadcast(i);
//	}
	private static boolean sPaused =true;
	private static boolean sIsButtonPanelShown = false;
	private static boolean sUpdateIcon = false;
	private static int sMinDelay = 800;
	private static Context mContext = null;
	private static final String LOG_TAG ="ColorUpdater";
	private static final boolean DEBUG_DELAY = false;
	private static final boolean DEBUG = false;
	
	private final static Thread sThread = new Thread(new Runnable() {

			@Override
			public void run() {
				while (true) {
					final long now = System.currentTimeMillis();
					if(DEBUG)Log.d(LOG_TAG, "Thread-run()nduwurPaused, paused:"+ sPaused);
					if (sPaused) {
						// we have been told to do nothing; wait for notify to continue
						synchronized (ColorBarTintUpdater.class) {
							try {
								ColorBarTintUpdater.class.wait();
							} catch (InterruptedException e) {
								return;
							}
						}

						continue;
					}
					final Context context = mContext;
					if(DEBUG)Log.d(LOG_TAG, "Thread-run()ngisorPaused, paused:"+ sPaused);
                    if (context == null) {
                        // we haven't been initiated yet; retry in a bit

                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            return;
                        }

                        continue;
                    }
					if(sIsButtonPanelShown)mIntentDsb.putExtra("xFromRight",StatusBarService.BUTTON_PANEL_WIDTH+2);
					else mIntentDsb.putExtra("xFromRight",1);
					context.startService(mIntentDsb);
					
					final long delta = System.currentTimeMillis() - now;
					final long delay = Math.max(sMinDelay, delta * 2);

					if (DEBUG_DELAY) {
						Log.d(LOG_TAG, "delta=" + Long.toString(delta) + "ms " +
							  "delay=" + Long.toString(delay) + "ms");
					}

					try {
						Thread.sleep(delay);
					} catch (InterruptedException e) {
						return;
					}
				}
			}

		});

    static {
        sThread.setPriority(4);
		sThread.start();
    }

    
    private ColorBarTintUpdater() {
    }

    private synchronized static void setPauseState(final boolean isPaused) {
        sPaused = isPaused;
		if(DEBUG)Log.d(LOG_TAG, "isPause: "+ sPaused);
        if (!isPaused) {
            // the thread should be notified to resume
            ColorBarTintUpdater.class.notify();
        }
    }

    public static void pause() {
		if(!sPaused)
			setPauseState(true);
    }
	
    public static void resume() {
		if(sPaused)
			setPauseState(false);
    }
	
}
