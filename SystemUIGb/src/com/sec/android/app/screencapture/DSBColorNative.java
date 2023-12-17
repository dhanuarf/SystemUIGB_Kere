package com.sec.android.app.screencapture;
import android.graphics.*;

public class DSBColorNative
{
	public static native int[]getColors(Bitmap ss, int rotation, int statusbarHeight, int navigationbarHeight, int xFromRightSide);
	
	static{
		System.loadLibrary("dsbcolor");
	}
}

