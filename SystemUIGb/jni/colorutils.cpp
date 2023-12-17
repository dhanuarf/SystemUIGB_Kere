#include <jni.h>
#include <string.h>
#include <math.h>
#include <stdio.h>
#include <android/log.h>
#include <android/bitmap.h>
 
#define LOG_TAG "libcolorutils"
#define LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)
 

extern "C"
{
JNIEXPORT jint JNICALL Java_kere_util_ColorUtils_getDominantColor
        (JNIEnv * je, jobject obj, jobject bitmapIn){
			
		AndroidBitmapInfo infoIn;
		void* pixelsIn;
		int ret;

		// Get image info
		if ((ret = AndroidBitmap_getInfo(je, bitmapIn, &infoIn)) < 0) {
			LOGE("get info failed ! error=%d", ret);
			return 0;
		}	  
		if ((ret = AndroidBitmap_lockPixels(je, bitmapIn, &pixelsIn)) < 0){
			LOGE("lock pixels failed ! error=%d", ret);
			return 0;
		}
		uint32_t w = infoIn.width;
		uint32_t h = infoIn.height;
		uint32_t pixelCount = w*h;
		uint32_t redBucket = 0 ;
		uint32_t greenBucket = 0 ;
		uint32_t blueBucket = 0 ;
		int x,y;
		for ( y =0; y < h; y++)
		{
			for ( x = 0; x < w; x++)
			{
				uint32_t color = * (uint32_t *) (((char *) pixelsIn) + x + y * w);
				//uint32_t color = pixels[x + y * w]; // x + y * width
				redBucket += ((color & 0x00FF0000) >> 16) ; // Color.red
				greenBucket += ((color & 0x0000FF00) >> 8) ; // Color.greed
				blueBucket += (color & 0x000000FF); // Color.blue
			}
		}
		
		AndroidBitmap_unlockPixels(je, bitmapIn);
		
		return (255 << 24) | (((char)redBucket / pixelCount) << 16) | (((char)greenBucket / pixelCount) << 8)| ((char)blueBucket / pixelCount);
			   // return (255 << 24) | (((char) red) << 16) | (((char) green) << 8) | ((char) blue);
		}
		
}
