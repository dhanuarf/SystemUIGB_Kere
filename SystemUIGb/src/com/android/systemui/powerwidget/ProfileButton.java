package com.android.systemui.powerwidget;

import com.android.systemui.R;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import java.util.ArrayList;
import java.util.List;
import kere.settings.*;
public class ProfileButton extends PowerButton {

	private static final List<Uri> OBSERVED_URIS = new ArrayList<Uri>();
    static {
        OBSERVED_URIS.add(Settings.System.getUriFor(Setelan.PROFILE_PICTURE_PATH));
    }
    public ProfileButton() {
		mType = BUTTON_PROFILE;
	}

	@Override
    protected void updateState(Context context) {
        boolean enabled = true;//Settings.System.getInt(context.getContentResolver(), Settings.System.TORCH_STATE, 0) == 1;
        if(getPicPath(context) == null) {
          //  mIcon = R.drawable.ic_qs_default_user;
            mState = STATE_ENABLED;
        } else {
            mIcon = R.drawable.stat_flashlight_off;
            mState = STATE_DISABLED;
        }
    }
    @Override
    protected void toggleState(Context context) {
        Intent intent = new Intent("com.android.systemui.powerwidget.FlashlightActivity");
		//      intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);

	}
    @Override
    protected boolean handleLongClick(Context context) {
        // it may be better to make an Intent action for the Torch
        // we may want to look at that option later
        Intent intent = new Intent("com.android.systemui.powerwidget.FlashlightActivity");
		//    intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
		return true;
    }
	
	@Override
    protected List<Uri> getObservedUris() {
        return OBSERVED_URIS;
    }
	
	private String getPicPath(Context c){
		return Settings.System.getString(c.getContentResolver(), Setelan.PROFILE_PICTURE_PATH);
	}/*
	String uri;
    String imageUri;
	public ProfilePicture(final Context context, AttributeSet attrs) {
		super(context, attrs); 
		setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View p1)
				{
					getContext().startActivity((new Intent()).setClassName("krupuk.mod.krupukable","krupuk.mod.krupukable.Profile").setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
					try{ 
						Object service  = context.getSystemService("statusbar");
						Class<?> statusbarManager = Class.forName("android.app.StatusBarManager");
						Method collapse = statusbarManager.getMethod("collapse");
						collapse.invoke(service);
					}
					catch(Exception ex){           

					}

					// TODO: Implement this method
				}

			});
//	profilePicture = (ImageView) findViewById(R.id.profile);

		SharedPreferences sharedPreferences = context.getSharedPreferences("EvoPrefsFile",Context.MODE_PRIVATE);    
		imageUri = sharedPreferences.getString("profilePic","null");
		if (imageUri == "null") {
			setImageResource(setDrw("ic_qs_default_user"));	 	    	 
		} else {
			setImageURI(Uri.parse(imageUri));
		}
		BroadcastReceiver mMediaReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context c, Intent i) {

				SharedPreferences sharedPreferences = context.getSharedPreferences("EvoPrefsFile",Context.MODE_PRIVATE);    
				imageUri = sharedPreferences.getString("profilePic","null");
				if (imageUri == "null") {
					setImageResource(setDrw("ic_qs_default_user"));	 	    	 
				} else {
					setImageURI(Uri.parse(imageUri));
				}

			}

		};
		BroadcastReceiver mReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context c, Intent i) {
				uri = i.getStringExtra("URI");
				setImageURI(Uri.parse(uri));
				SharedPreferences sharedPreferences = context.getSharedPreferences("EvoPrefsFile",Context.MODE_PRIVATE);
				SharedPreferences.Editor editor = sharedPreferences.edit(); //opens the editor
				editor.putString("profilePic", uri); //true or false
				editor.commit();	 
			}

		};      

		context.registerReceiver(mReceiver, new IntentFilter("com.b16h22.statusbar.CHANGE_PROFILE_PICTURE"));  
		IntentFilter intentFilter =  new IntentFilter(Intent.ACTION_MEDIA_SCANNER_FINISHED);
		intentFilter.addDataScheme("file");
		context.registerReceiver(mMediaReceiver, intentFilter);
	}

	int setDrw(String st){
		return	getResources().getIdentifier(st, "drawable", getContext().getPackageName());
	}*/
	}
