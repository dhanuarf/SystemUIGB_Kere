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
import kere.util.*;

public class SidePanelButtonsActivity extends PreferenceActivity implements OnPreferenceChangeListener {

    private static final String TAG = "SidePanelButtonsActivity";

    private static final String BUTTONS_CATEGORY = "pref_buttons";
    private static final String SELECT_BUTTON_KEY_PREFIX = "pref_button_";
    private HashMap<CheckBoxPreference, String> mCheckBoxPrefs = new HashMap<CheckBoxPreference, String>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
	//	setContentView(R.layout.main);
        addPreferencesFromResource(R.xml.sidepanelbuttons);

        PreferenceScreen prefSet = getPreferenceScreen();

        PreferenceCategory prefButtons = (PreferenceCategory) prefSet.findPreference(BUTTONS_CATEGORY);

        // empty our preference category and set it to order as added
        prefButtons.removeAll();
        prefButtons.setOrderingAsAdded(false);

        // emtpy our checkbox map
        mCheckBoxPrefs.clear();

        // get our list of buttons
        ArrayList<String> buttonList = SidePanelButtonsUtil.getButtonListFromString(SidePanelButtonsUtil.getCurrentButtons(this));
        // fill that checkbox map!
        for(SidePanelButtonsUtil.ButtonInfo button : SidePanelButtonsUtil.BUTTONS.values()) {
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
            SidePanelButtonsUtil.saveCurrentButtons(this, SidePanelButtonsUtil.mergeInNewButtonString(
														SidePanelButtonsUtil.getCurrentButtons(this), SidePanelButtonsUtil.getButtonStringFromList(buttonList)));
            return true;
        }

        return false;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {

        return true;
    }
}
