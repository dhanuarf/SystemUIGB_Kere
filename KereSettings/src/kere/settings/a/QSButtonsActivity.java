package kere.settings.a;

import com.android.internal.telephony.Phone;

import android.content.ContentResolver;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.util.Log;

import kere.settings.R;

import kere.util.PowerWidgetUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class QSButtonsActivity extends PreferenceActivity implements OnPreferenceChangeListener {

    private static final String TAG = "QSButtonsActivity";

    private static final String BUTTONS_CATEGORY = "pref_buttons";
    private static final String SELECT_BUTTON_KEY_PREFIX = "pref_button_";

    private static final String EXP_BRIGHTNESS_MODE = "pref_brightness_mode";
    private static final String EXP_NETWORK_MODE = "pref_network_mode";
    private static final String EXP_SCREENTIMEOUT_MODE = "pref_screentimeout_mode";
    private static final String EXP_RING_MODE = "pref_ring_mode";
    private static final String EXP_FLASH_MODE = "pref_flash_mode";
    private static final String EXP_MOBILEDATANETWORK_MODE = "pref_mobiledatanetwork_mode";

    private HashMap<CheckBoxPreference, String> mCheckBoxPrefs = new HashMap<CheckBoxPreference, String>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
	//	setContentView(R.layout.main);
        addPreferencesFromResource(R.xml.quicksettings);

        PreferenceScreen prefSet = getPreferenceScreen();

        PreferenceCategory prefButtons = (PreferenceCategory) prefSet.findPreference(BUTTONS_CATEGORY);

        // empty our preference category and set it to order as added
        prefButtons.removeAll();
        prefButtons.setOrderingAsAdded(false);

        // emtpy our checkbox map
        mCheckBoxPrefs.clear();

        // get our list of buttons
        ArrayList<String> buttonList = PowerWidgetUtil.getButtonListFromString(PowerWidgetUtil.getCurrentButtons(this));
        // fill that checkbox map!
        for(PowerWidgetUtil.ButtonInfo button : PowerWidgetUtil.BUTTONS.values()) {
            // create a checkbox
            CheckBoxPreference cb = new CheckBoxPreference(this);

            // set a dynamic key based on button id
            cb.setKey(SELECT_BUTTON_KEY_PREFIX + button.getId());

            // set vanity info
            cb.setTitle(button.getTitleResId());

            // set our checked state
            cb.setChecked(buttonList.contains(button.getId()));

            // add to our prefs set
            mCheckBoxPrefs.put(cb, button.getId());

            // specific checks for availability on some platforms
            if (PowerWidgetUtil.BUTTON_FLASHLIGHT.equals(button.getId()) &&
				true ){ //!getResources().getBoolean(R.bool.has_led_flash)) { // disable flashlight if it's not supported
                cb.setEnabled(true);
//                mFlashMode.setEnabled(false);
            } else if (PowerWidgetUtil.BUTTON_NETWORKMODE.equals(button.getId())) {
                // some phones run on networks not supported by this button, so disable it
                boolean knownState = false;

                try {
                    int networkState = Settings.Secure.getInt(getContentResolver(),
															  Settings.Secure.PREFERRED_NETWORK_MODE);

                    switch (networkState) {
							// list of supported network modes
                        case Phone.NT_MODE_WCDMA_PREF:
                        case Phone.NT_MODE_WCDMA_ONLY:
                        case Phone.NT_MODE_GSM_UMTS:
                        case Phone.NT_MODE_GSM_ONLY:
                            knownState = true;
                            break;
                    }
                } catch(Settings.SettingNotFoundException e) {
                    Log.e(TAG, "Unable to retrieve PREFERRED_NETWORK_MODE", e);
                }

                if (!knownState) {
                    cb.setEnabled(false);
                  //  mNetworkMode.setEnabled(false);
                }
            } 

            // add to the category
            prefButtons.addPreference(cb);
        }												 
    }

    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        // we only modify the button list if it was one of our checks that was clicked
        boolean buttonWasModified = false;
        ArrayList<String> buttonList = new ArrayList<String>();
        for(Map.Entry<CheckBoxPreference, String> entry : mCheckBoxPrefs.entrySet()) {
            if(entry.getKey().isChecked()) {
                buttonList.add(entry.getValue());
            }

            if(preference == entry.getKey()) {
                buttonWasModified = true;
            }
        }

        if(buttonWasModified) {
            // now we do some wizardry and reset the button list
            PowerWidgetUtil.saveCurrentButtons(this, PowerWidgetUtil.mergeInNewButtonString(
												   PowerWidgetUtil.getCurrentButtons(this), PowerWidgetUtil.getButtonStringFromList(buttonList)));
            return true;
        }

        return false;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {

        return true;
    }
}
