/*
 * Copyright (C) 2014 ParanoidAndroid Project
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

#include <com_android_systemui_statusbar_phone_BarBackgroundUpdaterNative.h>

#define LOG_TAG "BarBackgroundUpdaterNative"
#define DEBUG_FLOOD false
#define DEBUG false
#define OPTIONAL_DEBUG false

#define SHOT_SCALE 1
#define ROTATION_0 0
#define ROTATION_90 1
#define ROTATION_180 2
#define ROTATION_270 3



#include <sys/ioctl.h>
#include <stdio.h>
#include <unistd.h>
#include <stdlib.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <linux/fb.h>

#include <surfaceflinger/ISurfaceComposer.h>
#include <surfaceflinger/SurfaceComposerClient.h>
#include <utils/Log.h>

using namespace android;

int screenRotation;

void const * shotBase;

uint32_t shotWidth;
uint32_t shotHeight;
uint32_t shotStride;
PixelFormat shotFormat;

uint32_t requestedShotWidth = 0;
uint32_t requestedShotHeight = 0;

uint32_t sampleColors(int n, uint32_t sources[])
{
    float red = 0;
    float green = 0;
    float blue = 0;

    int i;
    for (i = 0; i < n; i++)
    {
        uint32_t color = sources[i];
        red += ((color & 0x00FF0000) >> 16) / n;
        green += ((color & 0x0000FF00) >> 8) / n;
        blue += (color & 0x000000FF) / n;
    }

    return (255 << 24) | (((char) red) << 16) | (((char) green) << 8) | ((char) blue);
}

uint32_t getPixel(int32_t dx, int32_t dy)
{
    dx = (uint32_t) (dx * SHOT_SCALE);
    dy = (uint32_t) (dy * SHOT_SCALE);

    uint32_t x = 0;
    uint32_t y = 0;

    switch (screenRotation)
    {
    case ROTATION_90:
        // turned counter-clockwise;  invert some of the things
        x = (dy >= 0) ? (shotWidth - 1 - dy) : -dy;
        y = (dx >= 0) ? dx : (shotHeight - 1 + dx);
        break;
    case ROTATION_180:
        // turned upside down; invert all the things
        x = (dx >= 0) ? (shotWidth - 1 - dx) : -dx;
        y = (dy >= 0) ? (shotHeight - 1 - dy) : -dy;
        break;
    case ROTATION_270:
        // turned clockwise; invert some of the things
        x = (dy >= 0) ? dy : (shotWidth - 1 + dy);
        y = (dx >= 0) ? (shotHeight - 1 - dx) : -dx;
        break;
    case ROTATION_0:
    default: // Just smile and wave, boys. Smile and wave.
        // natural orientation; don't invert anything
        x = (dx >= 0) ? dx : (shotWidth - 1 + dx);
        y = (dy >= 0) ? dy : (shotHeight - 1 + dy);
        break;
    }

    if (x >= shotWidth)
    {
        x = shotWidth - 1;
    }

    if (y >= shotHeight)
    {
        y = shotHeight - 1;
    }

    if (shotFormat == PIXEL_FORMAT_RGBA_8888)
    {
        // this is stored as BGRA behind the scenes, it seems
        // wonders of interacting with the lower level

        uint32_t color = * (uint32_t *) (((char *) shotBase) + y * shotStride *4 + x * 4);

        char blue = (color & 0x00FF0000) >> 16;
        char green = (color & 0x0000FF00) >> 8;
        char red = color & 0x000000FF;

        return (255 << 24) | (red << 16) | (green << 8) | blue;
    }
	else if (shotFormat == PIXEL_FORMAT_RGBX_8888)
    {
      
        uint32_t color = * (uint32_t *) (((char *) shotBase) + y * shotStride*4 + x * 4);

        char red = (color & 0x00FF0000) >> 16;
        char green = (color & 0x0000FF00) >> 8;
        char blue = color & 0x000000FF;
		
        return (255 << 24) | (red << 16) | (green << 8) | blue;
    }
    else if (shotFormat == PIXEL_FORMAT_RGB_565)
    {
        uint16_t color = * (uint16_t *) (((char *) shotBase) + y * shotStride * 2 + x * 2);

        uint32_t red = ((color & 0xF800) >> 11) * 255 / 31;
        uint32_t green = ((color & 0x07E0) >> 5) * 255 / 63;
        uint32_t blue = (color & 0x001F) * 255 / 31;
		
        return (255 << 24) | (red << 16) | (green << 8) | blue;
    }

    return 0;
}
static status_t vinfoToPixelFormat(const fb_var_screeninfo& vinfo,
        uint32_t* bytespp, uint32_t* f)
{

    switch (vinfo.bits_per_pixel) {
        case 16:
            *f = PIXEL_FORMAT_RGB_565;
            *bytespp = 2;
            break;
        case 24:
            *f = PIXEL_FORMAT_RGB_888;
            *bytespp = 3;
            break;
        case 32:
            // TODO: do better decoding of vinfo here
            *f = PIXEL_FORMAT_RGBX_8888;
            *bytespp = 4;
            break;
        default:
            return BAD_VALUE;
    }
    return NO_ERROR;
}

JNIEXPORT void JNICALL Java_com_android_systemui_statusbar_phone_BarBackgroundUpdaterNative_setScreenSize
        (JNIEnv * je, jclass jc, jint rotation, jint width, jint height)
{
  /*
  screenRotation = rotation;

    bool isNatural = rotation != ROTATION_90 && rotation != ROTATION_270;
    requestedShotWidth = (isNatural ? width : height) * SHOT_SCALE;
    requestedShotHeight = (isNatural ? height : width) * SHOT_SCALE;
	if(DEBUG)__android_log_print(ANDROID_LOG_DEBUG, "barUpdater", "setScreenSize()||shotWidth=%d shotHeight=%d",requestedShotWidth,requestedShotHeight);
	*/
}

JNIEXPORT jintArray JNICALL Java_com_android_systemui_statusbar_phone_BarBackgroundUpdaterNative_getColors
        (JNIEnv * je, jclass jc, jint rotation, jint statusBarHeight, jint navigationBarHeight, jint xFromRightSide)
{
	if(DEBUG)__android_log_print(ANDROID_LOG_DEBUG, "barUpdater::getColors()--","start");
	
	jint response[2] = { 0, 0 };
	
    screenRotation = rotation;

	void const* mapbase = MAP_FAILED;
    ssize_t mapsize = -1;

    void const* base = 0;
    uint32_t w, h, f;
	size_t size = 0;
	//debug thingy ---**
	uint32_t wi,he,xvir,yvir,bps;

	
	const char* fbpath = "/dev/graphics/fb0";
	int fb = open(fbpath, O_RDONLY);
	if (fb >= 0) {
		struct fb_var_screeninfo vinfo;
		if (ioctl(fb, FBIOGET_VSCREENINFO, &vinfo) == 0) {
			uint32_t bytespp;
			if (vinfoToPixelFormat(vinfo, &bytespp, &f) == NO_ERROR) {
				size_t offset = (vinfo.xoffset + vinfo.yoffset*vinfo.xres) * bytespp;
				w = vinfo.xres;
				h = vinfo.yres;
				//debug ---**
				wi = vinfo.width;
				he = vinfo.height;
				xvir = vinfo.xres_virtual;
				yvir = vinfo.yres_virtual;
				bps = vinfo.bits_per_pixel;
				// ---**
				if(OPTIONAL_DEBUG)__android_log_print(ANDROID_LOG_DEBUG, "barUpdater::getColors()--framebuffer ",
						"wi:%d he:%d xvir:%d yvir:%d bps:%d", wi, he,xvir,yvir,bps);
				if(OPTIONAL_DEBUG)__android_log_print(ANDROID_LOG_DEBUG, "barUpdater::getColors()--framebuffer ","xOff:%d yoff:%d", vinfo.xoffset, vinfo.yoffset);
				size = w*h*bytespp;
				mapsize = offset + size;
				mapbase = mmap(0, mapsize, PROT_READ, MAP_PRIVATE, fb, 0);
				if (mapbase != MAP_FAILED) {
					base = (void const *)((char const *)mapbase + offset);
					if(DEBUG)__android_log_print(ANDROID_LOG_DEBUG, "barUpdater::getColors()--framebuffer ","SUCCESS, continuing....");
				}
				else {
					if(DEBUG)__android_log_print(ANDROID_LOG_DEBUG, "barUpdater::getColors()--framebuffer ","ERROR, continuing....");
				}
			}
		}
	}
	else if(DEBUG)__android_log_print(ANDROID_LOG_DEBUG, "barUpdater::getColors()--framebuffer ","FAILED OPENING FB, continuing....");
	close(fb);

    shotBase = base;
    if (shotBase == NULL || shotBase == 0)
    {
        jintArray arr = je->NewIntArray(2);
        je->SetIntArrayRegion(arr, 0, 2, response);
        return arr;
    }

    shotWidth = w;
    shotHeight = h;
    shotStride = w;
    shotFormat = f;
	
	if(DEBUG)__android_log_print(ANDROID_LOG_DEBUG, "barUpdater::getColors()","--geting info -- w:%d h:%d stride:%d", shotWidth, shotHeight, shotStride);
	
	bool isNatural = screenRotation != ROTATION_90 && rotation != ROTATION_270;
		
    int fsbh = 2 + statusBarHeight;
    uint32_t colorTopLeft = getPixel(1, fsbh);
    uint32_t colorTopLeftPadding = getPixel(1 + (isNatural? 10 : 20), fsbh);
    uint32_t colorTopRight = getPixel(-1 - xFromRightSide, fsbh);
    uint32_t colorTopRightPadding = getPixel(-1 - xFromRightSide - (isNatural? 10 :20), fsbh);

    if (colorTopLeft == colorTopRight)
    {
        // status bar appears to be completely uniform
        response[0] = colorTopLeft;
    }
    else if (colorTopRightPadding == colorTopRight)
    {
        // the right side of the status bar appears to be uniform
        response[0] = colorTopRight;
    }
    else if (colorTopLeftPadding == colorTopLeft)
    {
        // the left side of the status bar appears to be uniform
        response[0] = colorTopLeft;
    }
    else
    {
        // status bar does not appear to be uniform at all
        uint32_t colorsTop[4] = { colorTopLeft, colorTopLeftPadding, colorTopRight, colorTopRightPadding };
        response[0] = sampleColors(4, colorsTop);
    }
	if(DEBUG)__android_log_print(ANDROID_LOG_DEBUG, "barUpdater::getColors()--","init response[0]");
    response[1] = getPixel(1, 1) == getPixel(1, 5) ? 1 : 0;

    jintArray arr = je->NewIntArray(2);
    je->SetIntArrayRegion(arr, 0, 2, response);
	if(DEBUG)__android_log_print(ANDROID_LOG_DEBUG, "barUpdater::getColors()--","returning results...");
    return arr;
}

