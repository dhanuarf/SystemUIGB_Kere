package kere.util;

import android.graphics.*;
import android.graphics.drawable.*;
import android.util.*;
import android.widget.*;
import java.util.*;

public class ColorUtils
{
	private final static String LOG_TAG ="kereColorUtils";
	
//	static{
//		System.loadLibrary("colorutils1");
//	}
//	// get the average color in the bitmap
//	public static native int getAverageColor(Bitmap bitmap);
	public static int getDominantColor(Bitmap bitmap){
		if (bitmap == null)
		{
			Log.e(LOG_TAG, "@getDominantColor, bitmap null, returning 0");
		}

		final int w = bitmap.getWidth();
		final int h = bitmap.getHeight();
		final int def=20;
		int finalw = def, finalh = def;
		if(w>h)finalw *= (w/h);
		else if(h>w)finalh *= (h/w);

		final Bitmap b = Bitmap.createScaledBitmap(bitmap, finalw,finalh,true);
		int width = b.getWidth();
		int height = b.getHeight();
		int size = width * height;
		int pixels[] = new int[size];
		Bitmap bitmap2 = b.copy(Bitmap.Config.ARGB_4444, false);
		bitmap2.getPixels(pixels, 0, width, 0, 0, width, height);
		HashMap<Integer, Integer> colorMap = new HashMap<Integer, Integer>();
		int color = 0;
		Integer count = 0;
		for (int i = 0; i < pixels.length; i++)
		{
			color = pixels[i];
			count = colorMap.get(color);
			if (count == null)
				count = 0;
			colorMap.put(color, ++count);
		}
		int dominantColor = 0;
		int dominantColor2 = 0;
		int dominantColor3 = 0;
		int max = 0;
		int max2 = 0;
		int max3 = 0;
		for (Map.Entry<Integer, Integer> entry : colorMap.entrySet())
		{

			if (entry.getValue() > max)
			{
				final int col= entry.getKey();
				dominantColor = col;
				max = entry.getValue();
			}
			if (entry.getValue() > max2)
			{
				final int col= entry.getKey();
				float[] hsv = new float[3];
				Color.colorToHSV(col,hsv);
				if(hsv[2]> 0.2f){
					max2 = entry.getValue();
					dominantColor2 = col;
				}
			}
			/*if (entry.getValue() > max3)
			{
				final int col= entry.getKey();
				float[] hsv = new float[3];
				Color.colorToHSV(col,hsv);

				if(hsv[2]> 0.5f){
					max3 = entry.getValue();
					dominantColor3 = col;
				}

			}*/
		}
		// jiot warna paling cerah
		final int result = dominantColor2==0? dominantColor : dominantColor2;
		return result;
	}
	public static int getSimpleDominantColor(Bitmap bitmap)
	{
		final Bitmap bitmap1 = Bitmap.createScaledBitmap(bitmap, 1, 1, true);
		final int color = bitmap1.getPixel(0, 0);
		
		return color;
	}
	/*public static int getDominantColor( Bitmap bitmap) {
		if ( null == bitmap) return Color .TRANSPARENT;
		int redBucket = 0 ;
		int greenBucket = 0 ;
		int blueBucket = 0 ;
		int alphaBucket = 0 ;
		boolean hasAlpha = bitmap.hasAlpha();
		int pixelCount = bitmap.getWidth() * bitmap.getHeight();
		int [] pixels = new int[pixelCount];
		bitmap.getPixels(pixels, 0 , bitmap.getWidth(), 0 , 0 , bitmap.getWid
		for ( int y = 0 , h = bitmap.getHeight(); y < h; y++)
		{
			for ( int x = 0 , w = bitmap.getWidth(); x < w; x++)
			{
				int color = pixels[x + y * w]; // x + y * width
				redBucket += (color >> 16 ) & 0xFF ; // Color.red
				greenBucket += (color >> 8 ) & 0xFF ; // Color.greed
				blueBucket += (color & 0xFF ); // Color.blue
				if (hasAlpha) alphaBucket += (color >>> 24 ); // Color.alph
			}
		}
		return Color .argb(
			(hasAlpha) ? (alphaBucket / pixelCount) : 255 ,
			redBucket / pixelCount,
			greenBucket / pixelCount,
			blueBucket / pixelCount);
	}*/
    /**
     * Gets the average color in a drawable
     * This is calculated by sampling a subset of pixels
     * All calculations are done in HSV color space
     *
     * Written by David Webb (http://makingmoneywithandroid.com/)
     *
     * @param image The drawable to sample
     * @return An integer representing the average Color of the sampled pixels
     */
    public static int getAverageColor(Drawable image)
	{
        //Setup initial variables
        int hSamples = 40;            //Number of pixels to sample on horizontal axis
        int vSamples = 40;            //Number of pixels to sample on vertical axis
        int sampleSize = hSamples * vSamples; //Total number of pixels to sample
        float[] sampleTotals = {0, 0, 0};   //Holds temporary sum of HSV values

        //If white pixels are included in sample, the average color will
        //  often have an unexpected shade. For this reason, we set a
        //  minimum saturation for pixels to be included in the sample set.
        //  Saturation < 0.1 is very close to white (see http://mkweb.bcgsc.ca/color_summarizer/?faq)
        float minimumSaturation = 0.1f;     //Saturation range is 0...1

        //By the same token, we should ignore transparent pixels
        //  (pixels with low alpha value)
        int minimumAlpha = 200;         //Alpha range is 0...255

        //Get bitmap
        Bitmap b = ((BitmapDrawable)image).getBitmap();
        int width = b.getWidth();
        int height = b.getHeight();

        //Loop through pixels horizontally
        float[] hsv = new float[3];
        int sample;
        for (int i=0; i < width; i += (width / hSamples))
		{
            //Loop through pixels vertically
            for (int j=0; j < height; j += (height / vSamples))
			{
                //Get pixel & convert to HSV format
                sample = b.getPixel(i, j);
                Color.colorToHSV(sample, hsv);

                //Check pixel matches criteria to be included in sample
                if ((Color.alpha(sample) > minimumAlpha) && (hsv[1] >= minimumSaturation))
				{
                    //Add sample values to total
                    sampleTotals[0] += hsv[0];  //H
                    sampleTotals[1] += hsv[1];  //S
                    sampleTotals[2] += hsv[2];  //V
                }
				else
				{
					//    Log.v(TAG, "Pixel rejected: Alpha " + Color.alpha(sample) + ", H: " + hsv[0] + ", S:" + hsv[1] + ", V:" + hsv[1]);
                }
            }
        }

        //Divide total by number of samples to get average HSV values
        float[] average = new float[3];
        average[0] = sampleTotals[0] / sampleSize;
        average[1] = sampleTotals[1] / sampleSize;
        average[2] = sampleTotals[2] / sampleSize;

        //Return average tuplet as RGB color
        return Color.HSVToColor(average);
    }
	public static boolean isBrightColor(int color)
	{
		return isBrightColor(color, 190);
	}
	public static boolean isBrightColor(int color, int minValue)
	{
		if (android.R.color.transparent == color)
			return true;

		boolean rtnValue = false;
		int[] rgb = { Color.red(color), Color.green(color), Color.blue(color) };
		int brightness = (int)Math.sqrt(rgb[0] * rgb[0] * .241 + rgb[1]
										* rgb[1] * .691
										+ rgb[2] * rgb[2] * .068);
		// color is light
		if (brightness >= minValue)
		{
			rtnValue = true;
		}

		return rtnValue;
	}

	public static int brighter(int c)
	{
		int r = Color.red(c);
		int b = Color.blue(c);
		int g = Color.green(c);
		if (r == 0 && b == 0 && g == 0)
		{
			return Color.DKGRAY;
		}
		// brighting red
		if (r < 3 && r != 0)
		{ 
			r = 3;
		}
		else
		{
			r = ( int ) (r / .7); r = (r > 255) ? 255 : r; 
		}
		// brighting blue
		if (b < 3 && b != 0)
		{ 
			b = 3; 
		}
		else
		{
			b = ( int ) (b / .7); b = (b > 255) ? 255 : b; 
		}
		// brighting green
		if (g < 3 && g != 0)
		{
			g = 3;
		}
		else
		{
			g = ( int ) (g / .7); g = (g > 255) ? 255 : g; 
		}

		return Color.rgb(r, g, b); 
	}
	public static int darker(int c)
	{
		int r = Color.red(c);
		int b = Color.blue(c);
		int g = Color.green(c);

		return Color.rgb(( int)(r * .7), ( int)(g * .7), ( int)(b * .7));
	}

	// ngambil nang Palette.Swatch class
	/*public static int getContrastTextColor(int color)
	{
		int textColorResult = 0;
		float MIN_CONTRAST_BODY_TEXT = 4.5f;
// First check white, as most colors will be dark
		final int lightBodyAlpha = ColorUtils . calculateMinimumAlpha(
			Color . WHITE , color, MIN_CONTRAST_BODY_TEXT);
		if (lightBodyAlpha != - 1)
		{
// If we found valid light values, use them and return
			return ColorUtils . setAlphaComponent(Color . WHITE , lightBodyAlpha);
		}
		final int darkBodyAlpha = ColorUtils . calculateMinimumAlpha(
			Color . BLACK , color, MIN_CONTRAST_BODY_TEXT);
		if (darkBodyAlpha != - 1)
		{
// If we found valid dark values, use them and return
			return ColorUtils . setAlphaComponent(Color . BLACK , darkBodyAlpha);
		}
// If we reach here then we can not find title and body values which use the same
// lightness, we need to use mismatched values
		return lightBodyAlpha != - 1
			? ColorUtils . setAlphaComponent(Color . WHITE , lightBodyAlpha)
			: ColorUtils . setAlphaComponent(Color . BLACK , darkBodyAlpha);
	}*/
}

		
	
