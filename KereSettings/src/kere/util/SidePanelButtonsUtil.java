/*
 * Copyright (C) 2011 The CyanogenMod Project
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

package kere.util;

import kere.settings.R;

import android.content.Context;
import android.provider.Settings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import kere.settings.*;
import android.util.*;

/**
 * THIS CLASS'S DATA MUST BE KEPT UP-TO-DATE WITH THE DATA IN
 * com.android.systemui.statusbar.powerwidget.PowerWidget AND
 * com.android.systemui.statusbar.powerwidget.PowerButton IN THE
 * SystemUI PACKAGE.
 */
public class SidePanelButtonsUtil {
    private static final String back = "back";
	private static final String home = "home";
	private static final String recent = "recent";
	private static final String menu = "menu";
	private static final String ss = "ss";
	private static final String volup = "volup";
	private static final String voldown = "voldown";
	private static final String powermenu = "powermenu";
	
	
    public static final HashMap<String, ButtonInfo> BUTTONS = new HashMap<String, ButtonInfo>();
    static {
        BUTTONS.put(back, new SidePanelButtonsUtil.ButtonInfo(
                back, R.string.btnpnl_back, getStringIcon(back)));
        BUTTONS.put(home, new SidePanelButtonsUtil.ButtonInfo(
                home, R.string.btnpnl_home, getStringIcon(home)));
        BUTTONS.put(recent, new SidePanelButtonsUtil.ButtonInfo(
                recent, R.string.btnpnl_recent, getStringIcon(recent)));
        BUTTONS.put(menu, new SidePanelButtonsUtil.ButtonInfo(
                menu, R.string.btnpnl_menu, getStringIcon(menu)));
        BUTTONS.put(ss, new SidePanelButtonsUtil.ButtonInfo(
                ss, R.string.btnpnl_ss, getStringIcon(ss)));
        BUTTONS.put(volup, new SidePanelButtonsUtil.ButtonInfo(
                volup, R.string.btnpnl_volup, getStringIcon(volup)));
        BUTTONS.put(voldown, new SidePanelButtonsUtil.ButtonInfo(
                voldown, R.string.btnpnl_voldown, getStringIcon(voldown)));
		BUTTONS.put(powermenu, new SidePanelButtonsUtil.ButtonInfo(
						powermenu, R.string.btnpnl_powermenu, getStringIcon(powermenu)));
		
		}
	private static String getStringIcon(String buttonName)
	{
		switch (buttonName)
		{
			case back:
				return "com.android.systemui:drawable/btnpnl_back";
			case home:
				return "com.android.systemui:drawable/btnpnl_home";
			case menu:
				return "com.android.systemui:drawable/btnpnl_menu";
			case recent:
				return "com.android.systemui:drawable/btnpnl_recent";
			case ss:
				return "com.android.systemui:drawable/btnpnl_ss";
			case volup:
				return "com.android.systemui:drawable/btnpnl_volup";
			case voldown:
				return "com.android.systemui:drawable/btnpnl_voldown";
			case powermenu:
				return "com.android.systemui:drawable/btnpnl_powermenu";
			default:
			    return null;
		}
	}
    private static final String separator = "_-";
    private static final String BUTTONS_DEFAULT = back
                             + separator + home
                             + separator + recent
                             + separator + powermenu;

    public static String getCurrentButtons(Context context) {
        String buttons = Settings.System.getString(context.getContentResolver(), Setelan.SIDE_PANEL_BUTTONS);
        if (buttons == null) {
            buttons = BUTTONS_DEFAULT;

        }
        return buttons;
    }

    public static void saveCurrentButtons(Context context, String buttons) {
        Settings.System.putString(context.getContentResolver(),
                Setelan.SIDE_PANEL_BUTTONS, buttons);
    }

    public static String mergeInNewButtonString(String oldString, String newString) {
        ArrayList<String> oldList = getButtonListFromString(oldString);
        ArrayList<String> newList = getButtonListFromString(newString);
        ArrayList<String> mergedList = new ArrayList<String>();

        // add any items from oldlist that are in new list
        for(String button : oldList) {
            if(newList.contains(button)) {
                mergedList.add(button);
            }
        }

        // append anything in newlist that isn't already in the merged list to the end of the list
        for(String button : newList) {
            if(!mergedList.contains(button)) {
                mergedList.add(button);
            }
        }

        // return merged list
        return getButtonStringFromList(mergedList);
    }

    public static ArrayList<String> getButtonListFromString(String buttons) {
        return new ArrayList<String>(Arrays.asList(buttons.split("\\_-")));
    }

    public static String getButtonStringFromList(ArrayList<String> buttons) {
        if(buttons == null || buttons.size() <= 0) {
            return "";
        } else {
            String s = buttons.get(0);
            for(int i = 1; i < buttons.size(); i++) {
                s += separator + buttons.get(i);
            }
            return s;
        }
    }

    public static class ButtonInfo {
        private String mId;
        private int mTitleResId;
        private String mIcon;

        public ButtonInfo(String id, int titleResId, String icon) {
            mId = id;
            mTitleResId = titleResId;
            mIcon = icon;
        }

        public String getId() { return mId; }
        public int getTitleResId() { return mTitleResId; }
        public String getIcon() { return mIcon; }
    }
}
