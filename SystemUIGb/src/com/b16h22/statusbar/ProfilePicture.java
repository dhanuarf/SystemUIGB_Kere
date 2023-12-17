package com.b16h22.statusbar;

//import com.mycompany.myapp2.R;
import android.app.*;
import android.content.*;
import android.net.*;
import android.util.*;
import android.view.*;
import support.animator.view.*;
import java.lang.reflect.*;
import java.util.*;
import kere.widget.*;
import android.provider.*;
import kere.settings.*;

import com.android.systemui.R;
import android.database.*;
import android.os.*;

public class ProfilePicture extends CircleImageView
{

	//   ImageView profilePicture;

    String uri;
    String imageUri;

	private class SettingsObserver extends ContentObserver{
		SettingsObserver(Handler h){
			super(h);
		}
		void observe(){
			ContentResolver resolver = getContext().getContentResolver();
//            resolver.registerContentObserver(Settings.System.getUriFor(
//												 Setelan.PROFILE_NAME), false, this);
			resolver.registerContentObserver(Settings.System.getUriFor(
												 Setelan.PROFILE_PICTURE_PATH), false, this);
			
		}

		@Override
		public void onChange(boolean p1)
		{
			super.onChange(p1);
			updateSettings();
		}
		
	}
	private String mProfileName;
	private ContentResolver mcr;
	public ProfilePicture(final Context context, AttributeSet attrs)
	{
		super(context, attrs); 
		mcr=context.getContentResolver();
		setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View p1)
				{
					getContext().startActivity((new Intent()).setClassName("kere.settings", "kere.settings.a.Profile").setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
					try
					{ 
						Object service  = context.getSystemService("statusbar");
						Class<?> statusbarManager = Class.forName("android.app.StatusBarManager");
						Method collapse = statusbarManager.getMethod("collapse");
						collapse.invoke(service);
					}
					catch (Exception ex)
					{           

					}

					// TODO: Implement this method
				}

			});
		new SettingsObserver(new Handler()).observe();
		updateSettings();
		context.registerReceiver(mMediaReceiver, new IntentFilter(Intent.ACTION_MEDIA_SCANNER_FINISHED));
	}
	private void updateSettings(){
		imageUri = Settings.System.getString(mcr, Setelan.PROFILE_PICTURE_PATH);
		if (imageUri == null)
		{
			setImageResource(R.drawable.ic_qs_default_user);	 	    	 
		}
		else
		{
			setImageURI(Uri.parse(imageUri));
		}
		mProfileName = Settings.System.getString(mcr,Setelan.PROFILE_NAME);
		
	}
	public String getProfileName()
	{
		return mProfileName;
	}
	
	private BroadcastReceiver mMediaReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context c, Intent i) {
			updateSettings();
		}

	};
}
