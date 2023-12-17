package com.android.systemui.statusbar;

import android.content.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.net.*;
import android.os.*;
import com.android.systemui.*;
import java.io.*;
import kere.util.*;
import android.util.*;

public class ColorAlbumArt 
{
	private ColorAlbumArtUpdater mUpdater;
	
	private boolean isBright;
	private int mCurrentColor;
	private long mCurrentAlbumId;
	private Bitmap mAlbBitmap;
	private BroadcastReceiver br= new BroadcastReceiver(){
		
		@Override
		public void onReceive(Context p1, Intent p2)
		{
			mCurrentAlbumId = p2.getLongExtra("albumId", 0);
			final Bitmap alb=getAlbumArt(mCurrentAlbumId);
			mAlbBitmap = alb;
			if(mUpdater!=null)mUpdater.onAlbumartUpdate();
		// TODO: Implement this method
		}
	};
	private final String META = "com.android.music.metachanged";
	
	private Context mContext;
	private boolean mRegistered = false;
	
	public ColorAlbumArt(Context c){
		mContext = c;
	}
	public void set(){
		if(mContext!=null && !mRegistered){
			mContext.registerReceiver(br, new IntentFilter(META));
			mRegistered=true;
		//	Log.d("ColorAlbumArt", "onSet()");
			}
			
	//	Log.d("ColorAlbumArt", "context:"+mContext+" registered:"+mRegistered);
	}
	public void unset(){
		
		if(mContext!=null && mRegistered){
			mContext.unregisterReceiver(br);
			mRegistered=false;
			}
	}
	
	public Bitmap getAlbumArt(long album_id)
	{
        Bitmap bm = null;
        try
        {
            final Uri u = Uri.parse("content://media/external/audio/albumart");
            Uri uri = ContentUris.withAppendedId(u, album_id);

            ParcelFileDescriptor pfd =mContext.getContentResolver().openFileDescriptor(uri, "r");
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
	
	/*public int getCurrentColor(){
		return mCurrentColor;
	}*/
	public Bitmap getCurentAlbBitmap(){
		return mAlbBitmap;
	}/*
	public boolean getIsBright(){
		return isBright;
	}*/
	public void setUpdater(ColorAlbumArtUpdater cau){
		mUpdater = cau;
	}
	public interface ColorAlbumArtUpdater{
		public void onAlbumartUpdate();
	}
}
