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

package com.android.systemui.statusbar.phone;

public class BarBackgroundUpdaterNative {
    public static native int[] getColors(int rotation, int statusBarHeight,
            int navigationBarHeight, int xFromRightSide);

    public static native void setScreenSize(int rotation, int width, int height);

    static {
        System.loadLibrary("bar-updater");		
    }
}
