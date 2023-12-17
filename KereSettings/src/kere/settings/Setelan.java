package kere.settings;

import android.provider.Settings;
import android.provider.Settings.System;
import android.content.*;
import android.*;
public class Setelan
{
	public static final String INTENT_SETELAN_CHANGED="setelan.changed";
	public static final String 
	COLOR_ALBUM_ART="albumart_context_color",
	COLOR_DYNAMIC="dynamic_color",
		
	STATUS_BAR_BG_STYLE="status_bar_bg_style",
	TINT_ENABLE="tint_enable",
	TINT_UPDATE_RATE="tint_update_rate",
	STATUS_BAR_CLOCK_AMPM="status_bar_clock_ampm",
	STATUS_BAR_WEEKDAY="status_bar_weekday_enable",
	STATUS_BAR_WEEKDAY_STYLE="status_bar_weekday_style",
	STATUS_BAR_TRAFFIC="status_bar_traffic",
	
	EXPANDED_STYLE="expanded_style",
	EXPANDED_CUSTOM_COLOR="expanded_custom_color",
	EXPANDED_COLOR_MODE="expanded_color_mode",
	EXPANDED_HEADER_STYLE="exp_header_style",
	
	BATTERY_LOW_STYLE="low_battery_style",
	BATTERY_LOW_LEVEL_ALERT="low_battery_level_alert",
	
	NEW_NOTIF_ALERT_ENABLE="new_notif_alert_enable",
	HEADS_UP_NOTIF_ENABLE="heads_up_notif_enable" ,
	HEADS_UP_MUSIC_QP_MODE="heads_up_music_qp_mode",
	
	NIGHT_MODE_ENABLE="night_mode_enable",
	NIGHT_MODE_INTENSITY_VALUE="night_mode_intensity_value",
	NIGHT_MODE_SCRDIM_VALUE="night_mode_scrdim_value",
	
	PROFILE_PICTURE_PATH = "profile_picture_path",
	PROFILE_NAME = "profile_name",
	
	SIDE_PANEL_ENABLE="side_panel_enable",
	SIDE_PANEL_MODE="side_panel_mode",
	SIDE_PANEL_ALPHA_VALUE="side_panel_alpha_value",
	SIDE_PANEL_DELAY_DISSMISS_TIME="side_panel_dissmis_time",
	
	DOUBLE_TAP_TO_SLEEP = "double_tap_to_sleep",		 
	PEEK_NEW_NOTIF = "peek_new_notif",
	BATTERY_MODE = "battery_mode",
	BATTERY_PERCENTAGE_ENABLE="battery_percentage_enable"
	;

	public static final String 
	QS_SINGLE_CLICK_ENABLE="qs_single_click_enable";

	public static final int
	STYLE_EXPANDED_DEFAULT=0,
	STYLE_EXPANDED_DYNAMIC=1,
	STYLE_EXPANDED_ALBUMART_CONTEXT=2,
	STYLE_EXPANDED_CUSTOM_COLOR=3,
	STYLE_BATTERY_LOW_NOTIF=0,
	STYLE_BATTERY_LOW_DIALOG=1,
	STYLE_BATTERY_LOW_TOAST=2,
	STYLE_BATTERY_LOW_TOAST_NOTIF=3;


	public static String WIDGET_BUTTONS = "pw_buttons";

	public static String SIDE_PANEL_BUTTONS = "side_panel_buttons";

	// tell device setting has been changed
	public static void sendChanged(Context c)
	{
		Intent i =new Intent(INTENT_SETELAN_CHANGED);
		c.sendBroadcast(i);
	}
}
