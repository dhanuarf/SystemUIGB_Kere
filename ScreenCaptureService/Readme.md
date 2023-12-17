ScreenCaptureService.apk needs to be moded for DSB implementation because all the DSB picking color processing happens here. Somehow samsung GB devices framebuffer can only be read by this app.

So basically, this app takes a screenshoot, calculate the top pixels, and then send it to SystemUI. It's so stupid and badly optimized.

The pixel calculation happens in cpp, for faster calculation