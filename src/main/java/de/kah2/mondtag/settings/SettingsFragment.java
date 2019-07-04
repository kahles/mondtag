package de.kah2.mondtag.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.util.Log;
import android.view.MenuItem;

import de.kah2.mondtag.Mondtag;
import de.kah2.mondtag.MondtagActivity;
import de.kah2.mondtag.R;
import de.kah2.mondtag.settings.location.LocationPreference;
import de.kah2.mondtag.settings.location.LocationPrefDialogFragment;

/**
 * This fragment is used to configure the app.
 * It is displayed on start if the app is started the first time or if the user enters configuration
 * via options menu.
 */
public class SettingsFragment extends PreferenceFragmentCompat
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    public final static String TAG = SettingsFragment.class.getSimpleName();

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {

        Log.d(TAG, "onCreatePreferences");

        addPreferencesFromResource(R.xml.preferences);

        this.initTimezonesList();

        this.setupActionBar();

        this.onSharedPreferenceChanged(null, getString(R.string.pref_key_location));
        this.onSharedPreferenceChanged(null, getString(R.string.pref_key_timezone));

        // Needed for #onOptionsItemSelected to work
        this.setHasOptionsMenu(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // The action bar's back/up-button
        if (item.getItemId() == android.R.id.home) {

            final MondtagActivity mondtagActivity = (MondtagActivity) getActivity();

            if (mondtagActivity != null) {

                mondtagActivity.onBackPressed();
                return true;
                
            } else {

                Log.e(TAG, "onOptionsItemSelected: mondtagActivity is null");
            }
        }

        return super.onOptionsItemSelected(item);
    }


    /**
     * Initializes the {@link ListPreference} responsible for choosing timezones with the available
     * timezones.
     */
    private void initTimezonesList() {
        final ListPreference tzList = (ListPreference) findPreference(
                getString(R.string.pref_key_timezone) );

        tzList.setEntries(TimeZonesArrayGenerator.getZoneDescriptions());
        tzList.setEntryValues(TimeZonesArrayGenerator.getZoneIds());
    }

    private void setupActionBar() {

        final MondtagActivity mondtagActivity = (MondtagActivity) getActivity();

        if (mondtagActivity == null) {
            Log.e(TAG, "setupActionBar: mondtagActivity is null");
        } else {
            mondtagActivity.getSupportActionBar().setSubtitle(R.string.action_settings);
            mondtagActivity.setUpButtonVisible(true);
        }
    }

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {

        if (preference instanceof LocationPreference) {

            final DialogFragment dialogFragment =
                    LocationPrefDialogFragment.newInstance(preference.getKey());

            dialogFragment.setTargetFragment(this, 0);

            this.showDialogFragment(dialogFragment, LocationPrefDialogFragment.TAG);

        } else {

            super.onDisplayPreferenceDialog(preference);
        }
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

            // location changed
            final LocationPreference locPref = (LocationPreference) pref;
            String summary = locPref.getPosition().toFormattedString();
            locPref.setSummary( summary );
            Log.d(TAG, "onSharedPreferenceChanged: setting " + key + " to " + summary);

        } else if (pref instanceof ListPreference) {

            // timezone changed
            final ListPreference listPref = (ListPreference) pref;
            listPref.setSummary( listPref.getEntry() );
            Log.d(TAG, "onSharedPreferenceChanged: setting " + key + " to " + listPref.getEntry());
        }

        // if any value is changed, it is delivered through sharedPreferences
        // if nothing changed, it's null
        if (sharedPreferences != null) {

            Log.d(TAG, "onSharedPreferenceChanged: resetting calendar");

            final MondtagActivity activity = (MondtagActivity) this.getActivity();

            if (activity == null) {
                Log.e(TAG,
                    "onSharedPreferenceChanged: can't reset calendar - activity is null");
            } else {

                ((Mondtag) activity.getApplicationContext()).getDataManager().resetCalendar();
            }
        }
    }

    /**
     * Informs the user why settings are automatically opened on first start.
     */
    public void showHelpDialog() {

        this.showDialogFragment(
                new SettingsHelpDialogFragment(),
                SettingsHelpDialogFragment.class.getSimpleName() );
    }

    private void showDialogFragment(DialogFragment fragment, String tag) {

        final FragmentManager manager = this.getFragmentManager();

        if (manager == null) {
            Log.e(TAG, "showDialogFragment: getFragmentManager returned null");
        } else {
            fragment.show(manager, tag);
        }
    }
}
