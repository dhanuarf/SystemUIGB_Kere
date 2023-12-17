package kere.settings.a;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import kere.settings.R;
import android.provider.*;
import kere.settings.*;
import android.net.*;
import kere.util.*;
import android.graphics.*;
import android.graphics.drawable.*;
public class Profile extends Activity
{

	ImageView imageView;
	ImageView done;
	EditText name;
	Uri uri;
	String imageUri;
	String profName;
	TextView owner;
	private int dominantcolor;
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.profile_info);

		ImageView photo = (ImageView) findViewById(R.id.photo_picker);
		owner = (TextView) findViewById(R.id.Owner);
		name = (EditText) findViewById(R.id.name_field);
		done = (ImageView) findViewById(R.id.name);
		imageView = (ImageView) findViewById(R.id.image);

		//checks shared preferences

		profName = Settings.System.getString(getContentResolver(), Setelan.PROFILE_NAME);
		if (profName == null)
		{
			owner.setText("Owner"); 					
		}
		else
		{
			owner.setText(profName);
			name.setText(profName); 
		}

		imageUri = Settings.System.getString(getContentResolver(), Setelan.PROFILE_PICTURE_PATH);
		if (imageUri == null)
		{
			imageView.setImageResource(R.drawable.ic_qs_default_user);	 	    	 
		}
		else
		{
			imageView.setImageURI(Uri.parse(imageUri));
		}
		
		updateColor();
		photo.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v)
				{
					Intent intent = new Intent();
					intent.setType("image/*");
					intent.setAction(Intent.ACTION_GET_CONTENT);
					startActivityForResult(Intent.createChooser(intent, "Select Picture"), 0);
		        }
			});

		done.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v)
				{
					Editable profileName = name.getText();
					owner.setText(profileName.toString());
					Settings.System.putString(getContentResolver(), Setelan.PROFILE_NAME, profileName.toString());

		        }
			});
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		switch (requestCode)
		{
			case 0:
				if (resultCode == RESULT_OK)
				{
					uri = Uri.parse(data.getDataString());
					imageView.setImageURI(uri);
					updateColor();
					Settings.System.putString(getContentResolver(), Setelan.PROFILE_PICTURE_PATH, uri.toString());
				}
				break;
	    }
	}
	private void updateColor(){
		if(imageView.getDrawable() == null)return;
		Bitmap b = ((BitmapDrawable)imageView.getDrawable()).getBitmap();
		dominantcolor = ColorUtils.getDominantColor(b);
		owner.setTextColor(dominantcolor);
		findViewById(R.id.big_owner_text_line).setBackgroundColor(dominantcolor);
		findViewById(R.id.profile_name_line).setBackgroundColor(dominantcolor);
		findViewById(R.id.profile_pic_text_line).setBackgroundColor(dominantcolor);
		((TextView)findViewById(R.id.profile_name_text)).setTextColor(dominantcolor);
		((TextView)findViewById(R.id.profile_pic_text)).setTextColor(dominantcolor);
		
		
	}
}

