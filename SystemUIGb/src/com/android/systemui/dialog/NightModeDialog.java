/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package com.android.systemui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.*;
import android.view.WindowManager;
import android.widget.*;

import com.android.systemui.R;
import android.content.*;
import android.provider.*;
import kere.settings.*;

/** A dialog that provides controls for adjusting the screen brightness. */
public class NightModeDialog extends Dialog{

    private static final String TAG = "BrightnessDialog";
    private static final boolean DEBUG = false;

    protected Handler mHandler = new Handler();

	private View mDisableBtn;
	private int mIntensityValue, mScrDimValue;
    private SeekBar mSeekBarIntensity, mSeekBarScrDim;
    private OnUpdateListener mOnUpdateListener = null;
    private ImageView mSetupButton;
    

    public NightModeDialog(Context ctx) {
        super(ctx);
		}


    /**
     * Create the brightness dialog and any resources that are used for the
     * entire lifetime of the dialog.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        window.setType(WindowManager.LayoutParams.TYPE_VOLUME_OVERLAY);
        window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
		
        window.requestFeature(Window.FEATURE_NO_TITLE);
		window.setBackgroundDrawableResource(R.drawable.bg_night_mode_dialog);
		//setTitle("Night mode");
        setContentView(R.layout.qs_night_mode_dialog);
        setCanceledOnTouchOutside(true);
		
		WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
		lp.copyFrom(window.getAttributes());
		lp.width= WindowManager.LayoutParams.MATCH_PARENT;
		lp.height =WindowManager.LayoutParams.WRAP_CONTENT;
		window.setAttributes(lp);
		
		mDisableBtn = findViewById(R.id.disableBtn);
        mSeekBarIntensity = (SeekBar) findViewById(R.id.seekbar_intensity);
		mSeekBarScrDim = (SeekBar) findViewById(R.id.seekbar_scr_dim);
		mSeekBarIntensity.setMax(150);
		mSeekBarScrDim.setMax(150);
		mSeekBarIntensity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){

				@Override
				public void onProgressChanged(SeekBar p1, int p2, boolean p3)
				{
					updateIntensity(p2);
					mIntensityValue = p2;
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
					// TODO: Implement this method
				}
				
			
		});
		mSeekBarScrDim.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){

				@Override
				public void onProgressChanged(SeekBar p1, int p2, boolean p3)
				{
					updateScrDim(p2);
					mScrDimValue = p2;
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
					// TODO: Implement this method
				}


			});
		mDisableBtn.setOnClickListener(new View.OnClickListener(){

				@Override
				public void onClick(View p1)
				{
					final ContentResolver cr = getContext().getContentResolver();
					Settings.System.putInt(cr, Setelan.NIGHT_MODE_ENABLE, 0);
					
				}			
			});
//        mSetupButtonDivider = findViewById(R.id.brightness_setup_button_divider);
//        mSetupButton = (ImageView) findViewById(R.id.brightness_setup_button);
//        mSetupButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent();
//                intent.setClassName("com.android.settings",
//                        "com.android.settings.cyanogenmod.AutoBrightnessSetup");
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
//                        | Intent.FLAG_ACTIVITY_SINGLE_TOP
//                        | Intent.FLAG_ACTIVITY_CLEAR_TOP
//                        | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
//                getContext().startActivity(intent);
//                dismissBrightnessDialog(0);
//            }
//        });
    }
	private void updateIntensity(int value){
		if(mOnUpdateListener != null){
			mOnUpdateListener.onIntensityUpdate(value);
		}
	}
	private void updateScrDim(int value){
		if(mOnUpdateListener != null){
			mOnUpdateListener.onScrDimUpdate(value);
		}
	}

	public void setOnUpdateListener(OnUpdateListener osl){
		mOnUpdateListener = osl;
	}
	
    @Override
    protected void onStart() {
        super.onStart();
		
		// get current settings
		final ContentResolver cr = getContext().getContentResolver();
		final int intensity = Settings.System.getInt(cr, Setelan.NIGHT_MODE_INTENSITY_VALUE, 0);
		final int scrDim = Settings.System.getInt(cr, Setelan.NIGHT_MODE_SCRDIM_VALUE, 0);
		mSeekBarIntensity.setProgress(intensity);
		mSeekBarScrDim.setProgress(scrDim);
		updateIntensity(intensity);
		updateScrDim(scrDim);

    }

    @Override
    protected void onStop() {
        super.onStop();
		// save settings
		final ContentResolver cr = getContext().getContentResolver();
		Settings.System.putInt(cr, Setelan.NIGHT_MODE_INTENSITY_VALUE, mIntensityValue);
		Settings.System.putInt(cr, Setelan.NIGHT_MODE_SCRDIM_VALUE, mScrDimValue);
		
    }
    
	
	public interface OnUpdateListener{
		public void onIntensityUpdate(int intensityValue);
		public void onScrDimUpdate(int scrDimValue);
	}

}
