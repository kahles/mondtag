package de.kah2.mondtag.settings;

import android.app.DialogFragment;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.Log;

import de.kah2.mondtag.Mondtag;
import de.kah2.mondtag.R;

/**
 * This {@link PreferenceFragment} is used to configure the app.
 * It is displayed on start if the app is started the first time or if the user enters configuration
 * via options menu.
 */
public class SettingsFragment extends PreferenceFragment
        implements OnSharedPreferenceChangeListener {

    public final static String TAG = SettingsFragment.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);

        this.initTimezonesList();

        this.onSharedPreferenceChanged(null, getString(R.string.pref_key_location));
        this.onSharedPreferenceChanged(null, getString(R.string.pref_key_timezone));
    }

    @Override
    public void onPause() {
        super.onPause();

        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();

        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        final Preference pref = findPreference(key);

        if (pref instanceof LocationPreference) {

            final LocationPreference locPref = (LocationPreference) pref;
            String summary = locPref.getLocation().toString();
            locPref.setSummary( summary );
            Log.d(TAG, "onSharedPreferenceChanged: setting " + key + " to " + summary);

        } else if (pref instanceof ListPreference) {

            final ListPreference listPref = (ListPreference) pref;
            listPref.setSummary( listPref.getEntry() );
            Log.d(TAG, "onSharedPreferenceChanged: setting " + key + " to " + listPref.getEntry());
        }

        // If not called in onCreate ...
        if (sharedPreferences != null) {
            ((Mondtag) getActivity().getApplicationContext()).getDataManager().resetCalendar();
        }
    }

    /**
     * Informs the user why settings are automatically opened on first start.
     */
    public void showHelpDialog() {
        DialogFragment helpDialog = new SettingsHelpDialogFragment();
        helpDialog.show(getFragmentManager(), SettingsHelpDialogFragment.class.getSimpleName());
    }

    /**
    /**
     * Initialized the {@link ListPreference} responsible for choosing timezones with available
     * timezones.
     */
    private void initTimezonesList() {
        final ListPreference tzList = (ListPreference) findPreference(
                getString(R.string.pref_key_timezone) );
        
        tzList.setEntries(TimeZonesArrayGenerator.getZoneDescriptions());
        tzList.setEntryValues(TimeZonesArrayGenerator.getZoneIds());
    }
}
