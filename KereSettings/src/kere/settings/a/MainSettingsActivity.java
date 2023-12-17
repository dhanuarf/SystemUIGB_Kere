package kere.settings.a;

import android.app.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import android.preference.*;
import android.provider.*;

import kere.settings.*;
import net.margaritov.preference.colorpicker.*;
import android.graphics.*;
import android.provider.Settings.*;
import kere.settings.R;

public class MainSettingsActivity extends PreferenceActivity implements Preference.OnPreferenceChangeListener
                                                               // ColorPickerPreference.OnPreferenceChangeListener
{
	
	// expanded
	private ListPreference mLpExpStyle, mLpExpColorStyle;
	private ColorPickerPreference mCppExpCustomColor;
	private String EXP_STYLE_PREF="exp_style_pref";
	private String EXP_COLOR_SYLE_PREF="exp_color_style_pref";
	private String EXP_CUSTOM_COLOR_PREF="exp_custom_color_pref";
	
	// battery mode
	private ListPreference mLpBatteryMode;
	private static final String BATTER_MODE_PREF="battery_mode_pref";
	private static final CharSequence[] batterymodeentries = {"gone","icon only","text only","icon and text"};
	private static final int[] batteryentryvalues={0,1,2,3};
	// statusbar bg
	private ListPreference mLpStatusBarBgMode;
	private static final String STATUS_BAR_BG_PREF="status_bar_bg_pref";
	
	// tint
	private CharSequence[] tintupdatentries = {"1000ms (1s)","700ms","500ms","350ms","100ms"};
	private int[] tintupdatentryvalues = {1000,700,500,350,100};
	private CheckBoxPreference mCbpTintEnable;
	private ListPreference mLpTintUpdateRate ;
	private static final String TINT_UPDATE_RATE_PREF="tint_update_rate_pref";
	private static final String TINT_ENABLE_PREF="tint_enable_pref";
	
	// data traffic
	private ListPreference mLpTrafficMode;
	private static final String TRAFFIC_MODE_PREF="traffic_mode_pref";
	
	// weekday
	private ListPreference mLpWeekdayMode;
	private static final String WEEKDAY_MODE_PREF="weekday_mode_pref";
	
	// ampm
	private ListPreference mLpAmpmMode;
	private static final String AMPM_MODE_PREF="ampm_mode_pref";
	
	// music qp
	private ListPreference mLpQpMusicMode;
	private static final String QP_MUSIC_MODE_PREF="qp_music_mode_pref";
	
	// night mode 
	private CheckBoxPreference mCbpNightModeEnable;
	private static final String NIGHT_MODE_ENABLE_PREF = "night_mode_enable_pref";
	
	// double tap to sleep
	private CheckBoxPreference mCbpDoubleTapToSleepEnable;
	private static final String DOUBLE_TAP_SLEEP_PREF = "double_tap_sleep_pref";
	// peek new notif
	private CheckBoxPreference mCbpPeekNotifEnable;
	private static final String peek_notif_enable_pref = "peek_notif_enable_pref";

	
	// new notif headsup
	private CheckBoxPreference mCbpHeadsupNotifEnable;
	private static final String HEADSUP_NOTIF_ENABLE_PREF = "headsup_notif_enable_pref";
	
	// side panel
	private CheckBoxPreference mCbpSidePanelEnable;
	private static final String SIDE_PANEL_PREF = "side_panel_pref";
	private ListPreference mLpSidePanelAlpha;
	private static final String SIDE_PANEL_ALPHA_PREF = "side_panel_alpha_pref";
	private CharSequence[] sidepanelalphaentries = {"75%","50%","45%","25%","Opaque"};
	private int[] sidepanelalphavalues = {0x55,0x88,0xaa,0xdd,0xff};
	private CharSequence[] sidepaneltimeoutentries = {"2s","3s","4s","5s","6s", "7s","infinite"};
	private int[] sidepaneltimeoutvalues = {2000,3000,4000,5000,6000,7000,-123};
	private ListPreference mLpSidePanelTimeout ;
	private static final String SIDE_PANEL_TIMEOUT_PREF="side_panel_timeout_pref";
	
	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState);
 //       setContentView(R.layout.main);
		addPreferencesFromResource(R.xml.status_bar_settings);
		
		mLpTintUpdateRate=(ListPreference)findPreference(TINT_UPDATE_RATE_PREF);
		mLpTintUpdateRate.setEntries(tintupdatentries);
		mLpTintUpdateRate.setEntryValues(getCSValuesFromInt(tintupdatentryvalues));
		mLpTintUpdateRate.setValueIndex(getIndexPosition(tintupdatentryvalues, getIntValue(Setelan.TINT_UPDATE_RATE)));
		mLpTintUpdateRate.setOnPreferenceChangeListener(this);
		
		mLpExpStyle=(ListPreference)findPreference(EXP_STYLE_PREF);
		mLpExpColorStyle=(ListPreference)findPreference(EXP_COLOR_SYLE_PREF);
		mCppExpCustomColor=(ColorPickerPreference)findPreference(EXP_CUSTOM_COLOR_PREF);
		
		mLpExpStyle.setOnPreferenceChangeListener(this);
		mLpExpColorStyle.setOnPreferenceChangeListener(this);
		mLpExpStyle.setValueIndex(getIntValue(Setelan.EXPANDED_STYLE));
		mLpExpColorStyle.setValueIndex(getIntValue(Setelan.EXPANDED_COLOR_MODE));
		mCppExpCustomColor.setAlphaSliderEnabled(true);
		mCppExpCustomColor.setOnPreferenceChangeListener(mCppListener);
		mCppExpCustomColor.setHexValueEnabled(true);
		mCppExpCustomColor.setValue(getIntValue(Setelan.EXPANDED_CUSTOM_COLOR));
		mCppExpCustomColor.setEnabled(getIntValue(Setelan.EXPANDED_STYLE)==3);
		//batterymode
		mLpBatteryMode=(ListPreference)findPreference(BATTER_MODE_PREF);
		mLpBatteryMode.setEntries(batterymodeentries);
		mLpBatteryMode.setEntryValues(getCSValuesFromInt(batteryentryvalues));
		mLpBatteryMode.setOnPreferenceChangeListener(this);
		mLpBatteryMode.setValueIndex(Settings.System.getInt(this.getContentResolver(), Setelan.BATTERY_MODE, 1));
		
		// qpmusic
		mLpQpMusicMode = (ListPreference)findPreference(QP_MUSIC_MODE_PREF);
		mLpQpMusicMode.setValueIndex(getIntValue(Setelan.HEADS_UP_MUSIC_QP_MODE));
		mLpQpMusicMode.setOnPreferenceChangeListener(this);
		
		// sb cover
		mLpStatusBarBgMode=(ListPreference)findPreference(STATUS_BAR_BG_PREF);
		mLpStatusBarBgMode.setValueIndex(getIntValue(Setelan.STATUS_BAR_BG_STYLE));
		mLpStatusBarBgMode.setOnPreferenceChangeListener(this);
		
		mCbpTintEnable=(CheckBoxPreference)findPreference(TINT_ENABLE_PREF);
		mCbpTintEnable.setChecked(getIntValue(Setelan.TINT_ENABLE)==1);
		// traffic
		mLpTrafficMode=(ListPreference)findPreference(TRAFFIC_MODE_PREF);
		mLpTrafficMode.setOnPreferenceChangeListener(this);
		mLpTrafficMode.setValueIndex(getIntValue(Setelan.STATUS_BAR_TRAFFIC));
		
		mLpWeekdayMode=(ListPreference)findPreference(WEEKDAY_MODE_PREF);
		mLpWeekdayMode.setOnPreferenceChangeListener(this);
		mLpWeekdayMode.setValueIndex(getIntValue(Setelan.STATUS_BAR_WEEKDAY));
		
		mLpAmpmMode=(ListPreference)findPreference(AMPM_MODE_PREF);
		mLpAmpmMode.setOnPreferenceChangeListener(this);
		mLpAmpmMode.setValueIndex(getIntValue(Setelan.STATUS_BAR_CLOCK_AMPM));
		
		mCbpNightModeEnable = (CheckBoxPreference)findPreference(NIGHT_MODE_ENABLE_PREF);
		mCbpNightModeEnable.setChecked(getIntValue(Setelan.NIGHT_MODE_ENABLE) == 1);
		
		mCbpDoubleTapToSleepEnable = (CheckBoxPreference)findPreference(DOUBLE_TAP_SLEEP_PREF);
		mCbpDoubleTapToSleepEnable.setChecked(getIntValue(Setelan.DOUBLE_TAP_TO_SLEEP) == 1);
		
		mCbpPeekNotifEnable = (CheckBoxPreference)findPreference(peek_notif_enable_pref);
		mCbpPeekNotifEnable.setChecked(getIntValue(Setelan.PEEK_NEW_NOTIF)==1);
		
		mCbpHeadsupNotifEnable = (CheckBoxPreference)findPreference(HEADSUP_NOTIF_ENABLE_PREF);
		mCbpHeadsupNotifEnable.setChecked(getIntValue(Setelan.HEADS_UP_NOTIF_ENABLE) == 1);
		
		mCbpSidePanelEnable = (CheckBoxPreference)findPreference(SIDE_PANEL_PREF);
		mCbpSidePanelEnable.setChecked(getIntValue(Setelan.SIDE_PANEL_ENABLE)==1);
		if(mCbpSidePanelEnable.isChecked())mCbpSidePanelEnable.setSummary(R.string.sidepanel_enabled_summary);
		else mCbpSidePanelEnable.setSummary("Side panel is disabled");
		mLpSidePanelAlpha = (ListPreference)findPreference(SIDE_PANEL_ALPHA_PREF);
		mLpSidePanelAlpha.setEntries(sidepanelalphaentries);
		mLpSidePanelAlpha.setEntryValues(getCSValuesFromInt(sidepanelalphavalues));
		mLpSidePanelAlpha.setValueIndex(getIndexPosition(sidepanelalphavalues,getIntValue(Setelan.SIDE_PANEL_ALPHA_VALUE)));
		mLpSidePanelAlpha.setOnPreferenceChangeListener(this);
		mLpSidePanelTimeout = (ListPreference)findPreference(SIDE_PANEL_TIMEOUT_PREF);
		mLpSidePanelTimeout.setEntries(sidepaneltimeoutentries);
		mLpSidePanelTimeout.setEntryValues(getCSValuesFromInt(sidepaneltimeoutvalues));
		mLpSidePanelTimeout.setValueIndex(getIndexPosition(sidepaneltimeoutvalues, getIntValue(Setelan.SIDE_PANEL_DELAY_DISSMISS_TIME)));
		mLpSidePanelTimeout.setOnPreferenceChangeListener(this);
    }
	
	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue)
	{
		if(preference == mLpExpStyle) {
            putIntValue( Setelan.EXPANDED_STYLE, Integer.valueOf((String)newValue), mLpExpStyle);
			mCppExpCustomColor.setEnabled(getIntValue(Setelan.EXPANDED_STYLE)==3);
   		} else if(preference == mLpExpColorStyle) {
			int value = Integer.valueOf((String)newValue);
			putIntValue( Setelan.EXPANDED_COLOR_MODE, value,mLpExpColorStyle);
   		} else if(preference == mLpTrafficMode) {
            int value = Integer.valueOf((String)newValue);
            putIntValue( Setelan.STATUS_BAR_TRAFFIC, value,mLpTrafficMode);
        } else if(preference == mLpWeekdayMode) {
            int value = Integer.valueOf((String)newValue);
            putIntValue(Setelan.STATUS_BAR_WEEKDAY, value,mLpWeekdayMode);
        } else if(preference == mLpAmpmMode) {
			int value = Integer.valueOf((String)newValue);
            putIntValue( Setelan.STATUS_BAR_CLOCK_AMPM, value,mLpAmpmMode);
		} else if(preference == mLpStatusBarBgMode) {
			int value = Integer.valueOf((String)newValue);
            putIntValue( Setelan.STATUS_BAR_BG_STYLE, value,mLpStatusBarBgMode);
		}
		else if(preference == mLpQpMusicMode) {
			int value = Integer.valueOf((String)newValue);
			putIntValue( Setelan.HEADS_UP_MUSIC_QP_MODE, value,mLpQpMusicMode);
			}
		else if(preference == mLpTintUpdateRate) {
			int value = Integer.valueOf((String)newValue);
			putIntValue( Setelan.TINT_UPDATE_RATE, value);
			mLpTintUpdateRate.setValueIndex(getIndexPosition(tintupdatentryvalues, value));
		}
		else if(preference == mLpSidePanelAlpha) {
			int value = Integer.valueOf((String)newValue);
			putIntValue( Setelan.SIDE_PANEL_ALPHA_VALUE, value);
			mLpSidePanelAlpha.setValueIndex(getIndexPosition(sidepanelalphavalues, value));
		}
		else if(preference == mLpSidePanelTimeout) {
			int value = Integer.valueOf((String)newValue);
			putIntValue( Setelan.SIDE_PANEL_DELAY_DISSMISS_TIME, value);
			mLpSidePanelTimeout.setValueIndex(getIndexPosition(sidepaneltimeoutvalues,value));
		}
		else if(preference == mLpBatteryMode) {
			int value = Integer.valueOf((String)newValue);
			putIntValue( Setelan.BATTERY_MODE, value);
			mLpBatteryMode.setValueIndex(value);
		}
		return false;
	}
	ColorPickerPreference.OnPreferenceChangeListener mCppListener = new ColorPickerPreference.OnPreferenceChangeListener(){

		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue)
		{
		
		if(preference == mCppExpCustomColor) {
            Settings.System.putInt(getContentResolver(), Setelan.EXPANDED_CUSTOM_COLOR, pickWarna(newValue));
        }/* else if(preference == mCppPieColorBackground) {
            Settings.System.putInt(getContentResolver(), Setelan.PIE_COLOR_BACKGROUND, pickWarna(newValue));
        } else if(preference == mCppPieColorOutline) {
            Settings.System.putInt(getContentResolver(), Setelan.PIE_COLOR_OUTLINE, pickWarna(newValue));
		} else if(preference == mCppPieColorSelect) {
            Settings.System.putInt(getContentResolver(), Setelan.PIE_COLOR_SELECT, pickWarna(newValue));
        } else if(preference == mCppPieColorChevron) {
            Settings.System.putInt(getContentResolver(), Setelan.PIE_COLOR_CHEVRON, pickWarna(newValue));
		} else if(preference == mCppPieColorBattery) {
            Settings.System.putInt(getContentResolver(), Setelan.PIE_COLOR_BATTERY, pickWarna(newValue));
        } else if(preference == mCppPieColorClock) {
            Settings.System.putInt(getContentResolver(), Setelan.PIE_COLOR_CLOCK, pickWarna(newValue));
		}else if(preference == mCppPieThemeColor) {
			Settings.System.putInt(getContentResolver(), Setelan.PIE_COLOR_THEME, pickWarna(newValue));

			}*/
			// TODO: Implement this method
			return false;
		}
		
	};

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen p1, Preference preference)
	{
		/*if(preference == mCbpPieEnable) {
            putIntValue( Setelan.PIE_ENABLE, mCbpPieEnable.isChecked()? 1:0);
        } else if(preference == mCbpPieColorize) {
			mCppPieThemeColor.setEnabled(!mCbpPieColorize.isChecked());
            putIntValue( Setelan.PIE_COLOR_ENABLE, mCbpPieColorize.isChecked()?1:0);
			}*/
		if(preference == mCbpTintEnable){
			putIntValue(Setelan.TINT_ENABLE, mCbpTintEnable.isChecked()? 1:0);
		}
		
		else if(preference == mCbpNightModeEnable){
			putIntValue(Setelan.NIGHT_MODE_ENABLE, mCbpNightModeEnable.isChecked()? 1:0);
		}
		else if(preference == mCbpDoubleTapToSleepEnable){
			putIntValue(Setelan.DOUBLE_TAP_TO_SLEEP, mCbpDoubleTapToSleepEnable.isChecked()? 1:0);
		}
		else if(preference == mCbpHeadsupNotifEnable){
			putIntValue(Setelan.HEADS_UP_NOTIF_ENABLE, mCbpHeadsupNotifEnable.isChecked()? 1:0);
		}
		else if(preference == mCbpSidePanelEnable){
			putIntValue(Setelan.SIDE_PANEL_ENABLE, mCbpSidePanelEnable.isChecked()? 1:0);
			if(mCbpSidePanelEnable.isChecked())mCbpSidePanelEnable.setSummary(R.string.sidepanel_enabled_summary);
			else mCbpSidePanelEnable.setSummary("Side panel is disabled");
		}
		else if(preference == mCbpPeekNotifEnable){
			putIntValue(Setelan.PEEK_NEW_NOTIF, mCbpPeekNotifEnable.isChecked()? 1:0);
		}
			return super.onPreferenceTreeClick(p1, preference);
	}
	private int getIndexPosition(int[] oa, int value){
		int result = 0;
		for(int i = 0; i<oa.length; i++){
			if(oa[i] == value){
				result = i;
				break;
			}
		}
		return result;
	}
	
	private CharSequence[] getCSValuesFromInt(int[] ia){
		final CharSequence[] result = new CharSequence[ia.length];
		for(int i = 0; i<ia.length;i++){
			result[i] = Integer.toString(ia[i]);
		}
		return result;
	}
	
	private int pickWarna(Object o)
	{
		return Color.parseColor(ColorPickerPreference.convertToARGB(Integer.valueOf(String.valueOf(o))));
	}
	
	private int getIntValue(String s){
		return Settings.System.getInt(getContentResolver(), s,0);
	}
	private float getFloatValue(String s){
		return Settings.System.getFloat(getContentResolver(), s,0f);
	}
	
	private void putIntValue(String s, int i, ListPreference p){
		putIntValue(s,i);
		p.setValueIndex(i);
	}
	private void putIntValue(String s, int i){
		Settings.System.putInt(getContentResolver(), s, i);
	}
	private void putFloatValue(String s, float i){
		Settings.System.putFloat(getContentResolver(), s, i);
	}
	private void setAlphaHexEnable(ColorPickerPreference[] cpp, boolean enableAlpha, boolean hexEnable){
		
	}
}
