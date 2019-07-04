package de.kah2.mondtag.settings.location;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.DialogPreference;
import android.support.v7.preference.PreferenceDialogFragmentCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import de.kah2.mondtag.R;
import de.kah2.mondtag.datamanagement.NamedGeoPosition;

/**
 * This is the fragment of {@link LocationPreference}. See this
 * <a href="https://medium.com/@JakobUlbrich/building-a-settings-screen-for-android-part-3-ae9793fd31ec">Article</a>
 * and <a href="https://stackoverflow.com/a/32812614/6747171">Stackoverflow</a>
 * for further details.
 */
public class LocationPrefDialogFragment extends PreferenceDialogFragmentCompat
        implements GeocodeServiceObserverFragment.CallbackClass,
        LocationSearchResultListAdapter.LocationConsumer {

    public static final String TAG = LocationPrefDialogFragment.class.getSimpleName();
    public static final String BUNDLE_KEY_RESULTS = TAG + ".results";

    /** this is only used to check if valid values are set */
    private NamedGeoPosition position;

    private EditText locationNameField;
    private EditText latitudeField;
    private EditText longitudeField;

    private ProgressBar progressBar;
    private Button locationSearchButton;

    private TextView selectHintText;
    private RecyclerView resultListView;
    private LocationSearchResultListAdapter resultsAdapter;

    private int editTextDefaultColor;

    public static LocationPrefDialogFragment newInstance(String key) {

        final LocationPrefDialogFragment
                fragment = new LocationPrefDialogFragment();
        final Bundle bundle = new Bundle(1);
        bundle.putString(ARG_KEY, key);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        DialogPreference preference = getPreference();

        if (preference instanceof LocationPreference) {

            // This gets overridden when restoring instance states of EditText fields
            this.position = ((LocationPreference) preference).getPosition();

            Log.d(TAG, "onBindDialogView: setting position to " + this.position);

            this.initPositionTextFields(view);

            final Button locationOpenButton = view.findViewById(R.id.location_open_button);
            locationOpenButton.setOnClickListener(v ->
                    LocationPrefDialogFragment.this.showMap());

            this.initSearchElements(view);

            this.selectHintText = view.findViewById(R.id.location_search_hint_select_result);
            this.resultListView = createResultListView(view);

            // if results were set when restoring instance state through onCreateDialog, show them
            this.updateResultsVisibility();
        }
    }

    private void initPositionTextFields(View view) {

        // get fields
        this.locationNameField = view.findViewById(R.id.location_name);
        this.latitudeField = view.findViewById(R.id.location_latitude);
        this.longitudeField = view.findViewById(R.id.location_longitude);

        // needed for #setUserInputValid called by listeners
        this.editTextDefaultColor = this.latitudeField.getCurrentTextColor();

        // add listeners
        this.addLocationNameListener();
        this.addLatLngListener(this.latitudeField, NamedGeoPosition.ValueType.LATITUDE);
        this.addLatLngListener(this.longitudeField, NamedGeoPosition.ValueType.LONGITUDE);

        // set values
        this.updatePositionFields();
    }

    private void addLocationNameListener() {

        this.locationNameField.addTextChangedListener(new AbstractSimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {

                Log.d(TAG, "afterTextChanged: name = " + s.toString());
                LocationPrefDialogFragment.this.position.setName(s.toString());
            }
        });
    }

    private void addLatLngListener(EditText source, NamedGeoPosition.ValueType target) {
        source.addTextChangedListener(new AbstractSimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {

                boolean isValid = true;

                // Only write to this.position if value is valid
                try {

                    LocationPrefDialogFragment.this.position.set(target,
                            Double.valueOf(s.toString()));
                    Log.d(TAG, "afterTextChanged: " + target + "=" + s.toString());

                } catch (IllegalArgumentException e) {

                    Log.d(TAG, "afterTextChanged: invalid " + target + ": "
                            + s.toString());
                    isValid = false;
                }

                // mark field to show invalid data was entered
                LocationPrefDialogFragment.this.setUserInputValid(isValid, source);
            }
        });
    }

    private void setUserInputValid(boolean isValid, EditText inputField) {

        final Context context = super.getContext();

        if (isValid) {
            inputField.setTextColor(this.editTextDefaultColor);
        } else if (context != null) {
            inputField.setTextColor(ContextCompat.getColor(context, R.color.invalid_text));
        }

        // Can be null, since listeners are triggered during view creation
        final AlertDialog dialog = (AlertDialog) this.getDialog();
        if (dialog != null) {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(isValid);
        }
    }

    private void initSearchElements(View view) {

        this.locationSearchButton = view.findViewById(R.id.location_search_button);
        locationSearchButton.setOnClickListener(v ->
                LocationPrefDialogFragment.this.startGeocodeService());

        this.progressBar = view.findViewById(R.id.location_search_progressbar);

        final GeocodeServiceObserverFragment fragment =
                this.getGeocodeServiceObserverFragment(false);

        if (fragment == null) {

            Log.d(TAG, "onBindDialogView: no fragment found");
            this.setSearchActive(false);

        } else {

            Log.d(TAG, "onBindDialogView: fragment found");
            this.setSearchActive(fragment.isSearching());
            fragment.setCallbackClass(this);
        }
    }

    /**
     * Tries to find an {@link GeocodeServiceObserverFragment}.
     *
     * @param createIfNotFound if true and no fragment is found, a new instance will be created
     */
    private GeocodeServiceObserverFragment getGeocodeServiceObserverFragment(
            boolean createIfNotFound) {

        final FragmentManager manager =
                ((AppCompatActivity) getContext()).getSupportFragmentManager();

        GeocodeServiceObserverFragment fragment = (GeocodeServiceObserverFragment)
                manager.findFragmentByTag(GeocodeServiceObserverFragment.TAG);

        if (fragment == null && createIfNotFound) {

            Log.d(TAG, "getGeocodeServiceObserverFragment: creating fragment ...");
            fragment = new GeocodeServiceObserverFragment();
            manager.beginTransaction().add(fragment, GeocodeServiceObserverFragment.TAG).commit();
        }

        return fragment;
    }

    private void startGeocodeService() {

        if (!Geocoder.isPresent()) {
            Toast.makeText(super.getContext(),
                    R.string.location_search_no_geocoder, Toast.LENGTH_LONG).show();
            return;
        }

        Log.d(TAG, "startResultService");

        this.setSearchActive(true);

        final GeocodeServiceObserverFragment fragment =
                this.getGeocodeServiceObserverFragment(true);

        fragment.setCallbackClass(this);
        fragment.startService(getContext(), this.position.getName());
    }

    @Override
    public void onGeocodeServiceFinished(NamedGeoPosition[] result, int messageId) {

        if (messageId == GeocodeServiceObserverFragment.CallbackClass.NO_ERROR_MESSAGE) {

            Log.d(TAG, "onGeocodeServiceFinished: got " + result.length + " results");

            this.resultsAdapter.setResults(result);
            this.updateResultsVisibility();

        } else {

            Log.d(TAG,
                    "onGeocodeServiceFinished: an error occured and a message gets displayed");

            Toast.makeText(
                    LocationPrefDialogFragment.super.getContext(),
                    messageId,
                    Toast.LENGTH_SHORT).show();
        }

        this.setSearchActive(false);
    }

    /**
     * When true is passed, the {@link ProgressBar} is shown and the search button gets disabled.
     */
    private void setSearchActive(boolean isSearching) {

        if (isSearching) {
            Log.d(TAG, "setSearchActive: showing ProgressBar and disabling button");
            this.progressBar.setVisibility(View.VISIBLE);
            this.locationSearchButton.setEnabled(false);
        } else {
            Log.d(TAG, "setSearchActive: hiding ProgressBar and enabling button");
            this.progressBar.setVisibility(View.GONE);
            this.locationSearchButton.setEnabled(true);
        }
    }

    private RecyclerView createResultListView(View parent) {

        Log.d(TAG, "createResultListView");

        final RecyclerView view = parent.findViewById(R.id.locations_search_result_list);

        final LinearLayoutManager linearLayoutManager =
                new LinearLayoutManager(super.getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        view.setLayoutManager(linearLayoutManager);

        createResultListAdapter();

        view.setAdapter(this.resultsAdapter);

        return view;
    }

    private void createResultListAdapter() {

        // check if already created by onCreateDialog to restore instance state
        if (this.resultsAdapter == null) {
            this.resultsAdapter = new LocationSearchResultListAdapter();
            this.resultsAdapter.setLocationConsumer(this);
        }
    }

    private void updateResultsVisibility() {

        if (this.resultsAdapter.getItemCount() > 0) {
            Log.d(TAG, "updateResultsVisibility: VISIBLE");
            LocationPrefDialogFragment.this.selectHintText.setVisibility(View.VISIBLE);
            LocationPrefDialogFragment.this.resultListView.setVisibility(View.VISIBLE);
        } else {
            // leave default "GONE"
            Log.d(TAG, "updateResultsVisibility: GONE");
        }
    }

    @Override
    public void onSearchResultSelected(NamedGeoPosition position) {

        this.position = position;
        this.updatePositionFields();
    }

    /**
     * called by {@link #initPositionTextFields(View)} and
     * {@link #onSearchResultSelected(NamedGeoPosition)}
     */
    private void updatePositionFields() {

        Log.d(TAG, "updatePositionFields called");

        this.locationNameField.setText(this.position.getName());
        this.latitudeField.setText(this.position.getFormattedLatitude());
        this.longitudeField.setText(this.position.getFormattedLongitude());
    }

    /**
     * Creates an {@link Intent} to show a position on a map and starts an activity for it.
     */
    private void showMap() {
        Log.d(TAG, "showing map");
        final Uri geoUri = this.position.toGeoUri();
        Intent mapCall = new Intent(Intent.ACTION_VIEW, geoUri);
        super.getContext().startActivity(mapCall);
    }


    @Override
    public void onDialogClosed(boolean okPressed) {

        if (okPressed) {

            DialogPreference preference = getPreference();
            if (preference instanceof LocationPreference) {
                LocationPreference locationPreference = (LocationPreference) preference;

                // This allows the client to ignore the user value.
                if (locationPreference.callChangeListener(this.position)) {
                    //Save the value
                    locationPreference.setPosition(this.position);
                }
            }
        }
    }

    // below the code for saving state / search result list

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        if (savedInstanceState != null) {

            String[] results = savedInstanceState.getStringArray( BUNDLE_KEY_RESULTS );
            Log.d(TAG, "onCreateDialog: restoring " + results.length + " search results");

            this.createResultListAdapter();
            this.resultsAdapter.setResults(results);
        }

        return super.onCreateDialog(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        String[] results = this.resultsAdapter.getResultsAsStringArray();

        Log.d(TAG, "onSaveInstanceState: saving " + results.length + " search results");

        outState.putStringArray( BUNDLE_KEY_RESULTS, results );
    }
}
