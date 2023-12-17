package kere.util;
import android.graphics.*;
import android.util.*;

public class BitmapUtils
{
	public static native void bitmapBlur(Bitmap input, Bitmap output, int __radius);
	public static Bitmap resize(Bitmap image, int maxWidth, int maxHeight)
	{
		if (maxHeight > 0 && maxWidth > 0)
		{
			int width = image.getWidth();
			int height = image.getHeight();
			float ratioBitmap = ( float ) width / ( float ) height;
			float ratioMax = ( float ) maxWidth / ( float ) maxHeight;
			int finalWidth = maxWidth;
			int finalHeight = maxHeight;
			if (ratioMax > 1)
			{
				finalWidth = ( int) (( float )maxHeight * ratioBitmap);
			}
			else
			{
				finalHeight = ( int) (( float )maxWidth / ratioBitmap);
			}
			image = Bitmap .createScaledBitmap(image, finalWidth, finalHeight, true);
			return image;
		}
		else
		{
			return image;
		}
	}
	public static Bitmap centerCrop(Bitmap bitmap, int widthOutput, int heightOutput)
	{
		if(widthOutput == 0 || heightOutput == 0){
			 Log.e("BitmapUtils", "centerCrop, output must be > 0 , returning without cropped -- widthOutput:"
			         +widthOutput+" heightOutput:"+heightOutput);
			 return bitmap;
		}
		float widthInput = bitmap.getWidth(); 
		float heightInput = bitmap.getHeight();

		float scale;
		float dx = 0, dy = 0;

		if (widthInput * heightOutput > widthOutput * heightInput) {
			scale = (float) heightOutput / (float) heightInput; 
			dx = (widthOutput - widthInput * scale) * 0.5f;
		} else {
			scale = (float) widthOutput / (float) widthInput;
			dy = (heightOutput - heightInput * scale) * 0.5f;
		}
		
		Bitmap background = Bitmap.createBitmap((int)widthOutput, (int)heightOutput, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(background);
		
//		Log.d("centerCrop","startMatrix, wIn:"+widthInput+" hIn:"+heightInput+" wOut:"
//		       +widthOutput+" hOut:"+heightOutput+" scale:"+scale+" dX:"+dx+" dy:"+dy);
		Matrix transformation = new Matrix();
		transformation.setScale(scale, scale);
		transformation.postTranslate((int) (dx + 0.5f), (int) (dy + 0.5f));
		//Log.d("centerCrop","doneMatrix, startPaint");
		Paint paint = new Paint();
		paint.setFilterBitmap(true);
		//Log.d("centerCrop","donePaint, startDraw");
		canvas.drawBitmap(bitmap, transformation, paint);
		//Log.d("centerCrop","done, returning...");
		return background;
		}
		/*float scale = widthOutput / originalWidth;

		float xTranslation = 0.0f;
		 float yTranslation = (height - originalHeight * scale) / 2.0f;


		 transformation.postTranslate(xTranslation, yTranslation);
		 transformation.preScale(scale, scale);
		 
    (ScaleType.CENTER_CROP == mScaleType) {
                mDrawMatrix = mMatrix;

                float scale;
                float dx = 0, dy = 0;

                if (dwidth * vheight > vwidth * dheight) {
                    scale = (float) vheight / (float) dheight; 
                    dx = (vwidth - dwidth * scale) * 0.5f;
                } else {
                    scale = (float) vwidth / (float) dwidth;
                    dy = (vheight - dheight * scale) * 0.5f;
                }

                mDrawMatrix.setScale(scale, scale);
                mDrawMatrix.postTranslate((int) (dx + 0.5f), (int) (dy + 0.5f));

		//float ratioOutput = (float) widthOutput/heightOutput;
		/*final int widthInput = bitmap.getWidth();
		final int heightInput = bitmap.getHeight();
		
		Bitmap b = bitmap.copy(Bitmap.Config.ARGB_8888,true);
		if(widthInput < widthOutput || heightInput < heightOutput){
			
			//#if(widthInput < wid b = Bitmap.createScaledBitmap(bitmap,widthOutput,heightOutput, false);
			Log.e("BitmapUtils-centerCrop", "input kurang kat output - widthInput:"+widthInput+" widthOutput:"+widthOutput+" heightInput:"
			       +heightInput+" heightOutput:"+heightOutput+" -- returning null...");
			return null;
		}
		float widthDivider;
		float heightDivider;
		int finalWidthOutput = widthOutput;
		int finalHeightOutput = heightOutput;

		boolean wideOutput = widthOutput > heightOutput;
		// output amba tapi ambane tetep lewih kat input
		if (wideOutput && widthOutput > widthInput)
		{
			widthDivider = (float)widthOutput / widthInput;
			// nek widthout lewih lebar widthinput go patokan
			finalWidthOutput = widthInput; // (int)((float)widthOutput / widthDivider);
			finalHeightOutput = (int)((float)heightOutput / widthDivider);

		}
		//output duwur tapi ambane tetep lewih duwur kat input
		else if (!wideOutput && heightOutput > heightInput)
		{
			heightDivider = (float) heightOutput / widthInput;
			// nek heightout lewih duwur heightinput go patokan
			finalHeightOutput = heightInput;
			finalWidthOutput = (int)((float)heightOutput / heightDivider);
		}
		// lebar >= tinggi
		boolean wideInput = widthInput > heightInput;
		Log.d("bitmaputil--centercrop", "inW:"+widthInput+" outW:"+ widthOutput+" finalW:"+finalWidthOutput+
		      " inH:"+heightInput+" outH:"+heightOutput+" finalH:"+finalHeightOutput);

		if (wideOutput)
		{
			int y =Math.abs(heightInput - heightOutput) / 2;
			final Bitmap outW = Bitmap.createBitmap(b, 0, y, finalWidthOutput, finalHeightOutput);

			return outW;
		}
		else
		{
			int x =Math.abs(widthInput - widthOutput) / 2;
			final Bitmap outH = Bitmap.createBitmap(b, x, 0, finalWidthOutput, finalHeightOutput);

			return outH;

		}

	}
	 public static Bitmap centerCrop(Bitmap srcBmp)
	 {
	 // lebar >= tinggi
	 if (srcBmp.getWidth() >= srcBmp.getHeight())
	 {
	 Bitmap dstBmpW;
	 dstBmpW = Bitmap.createBitmap(
	 srcBmp, 
	 srcBmp.getWidth() / 2 - srcBmp.getHeight() / 2, // xStart
	 0, // yStart
	 srcBmp.getHeight(), //lebar
	 srcBmp.getHeight()  //tinggi
	 );
	 return dstBmpW;
	 }
	 else
	 // tinggi >= lebar
	 {
	 Bitmap dstBmpH;
	 dstBmpH = Bitmap.createBitmap(
	 srcBmp,
	 0,
	 srcBmp.getHeight() / 2 - srcBmp.getWidth() / 4,
	 srcBmp.getWidth(),
	 srcBmp.getWidth()/2
	 );
	 return dstBmpH;
	 }
	 }*/
    static {
        System.loadLibrary("bitmaputils");		
    }
}
