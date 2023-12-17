package com.sec.android.app.screencapture;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.Matrix;
import android.util.Log;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import android.content.*;
import android.graphics.*;
import com.android.systemui.statusbar.phone.*;

public class ScreenCapture {
	private static boolean DEBUG;
	private static String TAG;
	private int mBytePerPixel;
	private int mFrameBufferSize;
	private int mHeight;
	private int mWidth;
	
	static {
		DEBUG = false;
		TAG = "ScreenCapture";
		System.loadLibrary("screencapture");
	}

	public ScreenCapture() {
		super();
		mBytePerPixel = getBytePerPixel();
		mWidth = getLineLength() / mBytePerPixel;
		mHeight = getHeight();
		mFrameBufferSize = (mWidth * mHeight) * mBytePerPixel;
		BarBackgroundUpdaterNative.setScreenSize(0,240,320);
		
	}

	public static Bitmap bitmapRotate(Bitmap b, int mRotation) {
		int r0i = 0;
		switch(mRotation) {
		case 0:
			return rotate(b, r0i);
		case 1:
			return rotate(b, 90);
		case 2:
			return rotate(b, 180);
		case 3:
			return rotate(b, 270);
		}
		return rotate(b, r0i);
	}

	private native int getBytePerPixel();

	private native boolean getFrameBuffer(byte[] r1_byte_A);

	private native int getHeight();

	private native int getLineLength();

	public static Bitmap rotate(Bitmap b, int degrees) {
		if (degrees != 0) {
			if (b != null) {
				Bitmap b2;
				Matrix m = new Matrix();
				m.setRotate((float) degrees, ((float) b.getWidth()) / 2.0f, ((float) b.getHeight()) / 2.0f);
				int r1i = 0;
				int r2i = 0;
				try {
					b2 = Bitmap.createBitmap(b, r1i, r2i, b.getWidth(), b.getHeight(), m, true);
					if (b != b2) {
						b.recycle();
						return b2;
					} else {
						return b;
					}
				} catch (OutOfMemoryError e) {
					if (DEBUG) {
						Log.d(TAG, "Out of Memmory");
						return b;
					} else {
						return b;
					}
				}
			} else {
				return b;
			}
		} else {
			return b;
		}
	}

	public boolean capture(String dst, int mRotation) {
		return capture(dst, Config.ARGB_8888, CompressFormat.PNG, mRotation);
	}
	public int getTopScreenColor(final int statusbarHeight, final int rotation, final int xFromRight){
		//final int colors[]=DSBColorNative.getColors(bitmap, rotation,statusbarHeight,0,1);
		final int color = (BarBackgroundUpdaterNative.getColors(rotation,statusbarHeight,0,xFromRight))[0];//colors[0];// bitmap.getPixel(2 , mStatusBarHeight);
			
		return color;
	}
	public boolean capture(String dst, Config bitmapConfig, CompressFormat bitmapCompressFormat, int mRotation) {
		if (DEBUG) {
			Log.d(TAG, "FrameBufferSize = " + mFrameBufferSize + " Width = " + mWidth + " Height = " + mHeight + " BytePerPixel =" + mBytePerPixel);
		}
		byte[] frameBufferbyteArray = new byte[mFrameBufferSize];
		if (!getFrameBufferData(frameBufferbyteArray)) {
			if (DEBUG) {
				Log.e(TAG, "Error on getting FrameBuffer");
			}
			return false;
		} else {
			Bitmap bitmap = Bitmap.createBitmap(mWidth, mHeight, bitmapConfig);
			bitmap.copyPixelsFromBuffer(ByteBuffer.wrap(frameBufferbyteArray));
			try {
				OutputStream out = new FileOutputStream(dst);
				if (bitmap.hasAlpha()) {
					bitmap.setHasAlpha(false);
				}
				bitmapRotate(bitmap, mRotation).compress(bitmapCompressFormat, 100, out);
				out.close();
				return true;
			} catch (Exception e) {
				if (DEBUG) {
					Log.d(TAG, "Fail to save as image.");
				}
				return false;
			}
		}
	}

	public boolean getFrameBufferData(byte[] bytes) {
		return getFrameBuffer(bytes);
	}
}
