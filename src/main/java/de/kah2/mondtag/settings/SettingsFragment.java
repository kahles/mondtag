package de.kah2.mondtag.settings;


import android.Manifest;
import android.app.DialogFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.util.List;

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

    private final static int PERMISSION_LOCATION = 1;

    private LocationPreference locationRequester;

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
     * This method exists to enable the {@link LocationPreference} to "order" location data.
     * @param requester the {@link LocationPreference} which requested location data.
     */
    void requestLocation(LocationPreference requester) {
        this.locationRequester = requester;
        this.getLocationWithPermission();
    }

    /**
     * Handles requesting the needed permissions to get the location.
     */
    private void getLocationWithPermission() {
        if (ContextCompat.checkSelfPermission(
                    getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {

                DialogFragment dialog = new LocationPermissionHelpDialog();
                dialog.show( getFragmentManager(), LocationPermissionHelpDialog.class.getSimpleName() );

            } else {
                this.requestPermission();
            }
        } else {
            this.deliverLocation();
        }
    }

    /**
     * Requests the location permission from the system.
     */
    void requestPermission() {
        ActivityCompat.requestPermissions(getActivity(),
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_LOCATION);
    }

    /**
     * Callback used by the system to notify the app, when the requested permissions are granted.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if ( requestCode == PERMISSION_LOCATION
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED ) {
            deliverLocation();
        } else {
            locationRequester.onLocationDelivered(null);
        }
    }

    /**
     * Gets the location and delivers it to the {@link LocationPreference} that requested the
     * location. The reference to the {@link LocationPreference} is set to null afterwards.
     */
    private void deliverLocation() {

        if (locationRequester == null) {

            Log.d(TAG, "deliverLocation: locationRequester is null - could not deliver location");

        } else {

            final Location location = getLastKnownLocation();

            locationRequester.onLocationDelivered( location );

            locationRequester = null;
        }
    }

    /**
     * Gets the last known location.
     * Throws a {@link SecurityException} if permission wasn't granted. => Use
     * {@link #getLocationWithPermission()} instead, if unsure about permission state.
     */
    @SuppressWarnings("MissingPermission")
    private Location getLastKnownLocation() {
        LocationManager locationManager = (LocationManager) getActivity().getApplicationContext()
                        .getSystemService(Context.LOCATION_SERVICE);

        if (locationManager == null) {
            Log.e(TAG, "getLastKnownLocation: could not obtain LocationManager" );
            return null;
        }

        // TODO can't get location
        List<String> providers = locationManager.getProviders(true);

        Location bestLocation = null;
        for (String provider : providers) {
            Log.d(TAG, "getLastKnownLocation: requesting " + provider);
            Location location = locationManager.getLastKnownLocation(provider);
            if (location == null) {
                continue;
            }
            if (bestLocation == null || location.getAccuracy() < bestLocation.getAccuracy()) {
                bestLocation = location;
            }
        }
        return bestLocation;
    }

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
