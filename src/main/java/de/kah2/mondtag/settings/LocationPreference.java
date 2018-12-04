package de.kah2.mondtag.settings;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.ResultReceiver;
import android.preference.DialogPreference;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.util.AttributeSet;
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
 * This is a subclass of {@link DialogPreference} which is used to set-up observer position needed
 * for rise- and set-calculation.
 *
 * Created by kahles on 16.11.16.
 */
public class LocationPreference extends DialogPreference
        implements LocationSearchResultListAdapter.LocationConsumer {

    private final static String TAG = LocationPreference.class.getSimpleName();

    private EditText locationNameField;
    private EditText latitudeField;
    private EditText longitudeField;
    private ProgressBar progressBar;
    private TextView selectHintText;
    private RecyclerView resultListView;
    private LocationSearchResultListAdapter resultsAdapter;

    private int editTextDefaultColor;

    private NamedGeoPosition position;

    public LocationPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        setDialogLayoutResource(R.layout.location_preference);

        setPositiveButtonText(android.R.string.ok);
        setNegativeButtonText(android.R.string.cancel);

        setDialogIcon(null);
    }

    @Override
    protected void onBindDialogView(View view) {

        super.onBindDialogView(view);

        Log.d(TAG, "onBindDialogView: initializing view");

        this.initPositionTextFields(view);

        final Button locationSearchButton = view.findViewById(R.id.location_search_button);
        locationSearchButton.setOnClickListener(v -> LocationPreference.this.startSearch());

        final Button locationOpenButton = view.findViewById(R.id.location_open_button);
        locationOpenButton.setOnClickListener(v -> LocationPreference.this.showMap());

        this.progressBar = view.findViewById(R.id.location_search_progressbar);
        this.selectHintText = view.findViewById(R.id.location_search_hint_select_result);
        this.resultListView = createResultListView(view);
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
                LocationPreference.this.position.setName(s.toString());
            }
        });
    }

    private void addLatLngListener(EditText source, NamedGeoPosition.ValueType target) {
        source.addTextChangedListener( new AbstractSimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {

                boolean isValid = true;

                // Only write to this.position if value is valid
                try {

                    LocationPreference.this.position.set(target,
                            Double.valueOf( s.toString() ) );
                    Log.d(TAG, "afterTextChanged: " + target + "=" + s.toString());

                } catch (IllegalArgumentException e) {

                    Log.d(TAG, "afterTextChanged: invalid " + target + ": "
                            + s.toString() );
                    isValid = false;
                }

                // mark field to show invalid data was entered
                LocationPreference.this.setUserInputValid( isValid, source );
            }
        });
    }

    private void setUserInputValid(boolean isValid, EditText inputField) {

        if (isValid) {
            inputField.setTextColor(this.editTextDefaultColor);
        } else {
            inputField.setTextColor(ContextCompat.getColor(super.getContext(), R.color.invalid_text));
        }

        // Can be null, since listeners are triggert during view creation
        final AlertDialog dialog = (AlertDialog) this.getDialog();
        if (dialog != null) {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(isValid);
        }
    }

    private void startSearch() {

        this.progressBar.setVisibility(View.VISIBLE);

        if (!Geocoder.isPresent()) {
            Toast.makeText( super.getContext(),
                    R.string.location_search_no_geocoder, Toast.LENGTH_LONG).show();
            return;
        }

        final ResultReceiver resultReceiver = new AddressResultReceiver();

        final Intent intent = new Intent( super.getContext(), GeocodeIntentService.class );

        intent.putExtra(GeocodeIntentService.BUNDLE_KEY_RECEIVER, resultReceiver);
        intent.putExtra(GeocodeIntentService.BUNDLE_KEY_SEARCH_TERM, this.position.getName());

        this.getActivity().startService(intent);
    }

    private RecyclerView createResultListView(View parent) {

        final RecyclerView view = parent.findViewById(R.id.locations_search_result_list);

        final LinearLayoutManager linearLayoutManager =
                new LinearLayoutManager(super.getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        view.setLayoutManager(linearLayoutManager);

        this.resultsAdapter = new LocationSearchResultListAdapter();
        this.resultsAdapter.setLocationConsumer(this);
        view.setAdapter(this.resultsAdapter);

        return view;
    }

    private void setSearchResults(NamedGeoPosition[] results) {

        this.resultsAdapter.setResults(results);
        this.updateResultsVisibility();
    }

    private void updateResultsVisibility() {

        if (this.resultsAdapter.getItemCount() > 0) {
            Log.d(TAG, "updateResultsVisibility: VISIBLE");
            LocationPreference.this.selectHintText.setVisibility(View.VISIBLE);
            LocationPreference.this.resultListView.setVisibility(View.VISIBLE);
        } else {
            // leave default "GONE"
            Log.d(TAG, "updateResultsVisibility: GONE");
        }
    }

    @Override
    public void setSelectedSearchResult(NamedGeoPosition position) {

        this.position = position;
        this.updatePositionFields();
    }

    /**
     * called by {@link #initPositionTextFields(View)} and
     * @link #setSelectedSearchResult(NamedGeoPosition)}
     */
    private void updatePositionFields() {

        Log.d(TAG, "updatePositionFields called" );

        this.locationNameField.setText( this.position.getName() );
        this.latitudeField.setText( this.position.getFormattedLatitude() );
        this.longitudeField.setText( this.position.getFormattedLongitude() );
    }

    /**
     * Creates an {@link Intent} to show a position on a map and starts an activity for it.
     */
    private void showMap() {
        Log.d(TAG, "showing map");
        final Uri geoUri = this.getPosition().toGeoUri();
        Intent mapCall = new Intent(Intent.ACTION_VIEW, geoUri);
        super.getContext().startActivity(mapCall);
    }

    /**
     * Saves the configured position if dialog was closed by clicking ok-button.
     */
    @Override
    protected void onDialogClosed(boolean okPressed) {
        super.onDialogClosed(okPressed);

        if (okPressed) {
            final String positionString = this.position.toString();

            Log.d(TAG, "onDialogClosed: saving position \"" + positionString + "\"");
            persistString(positionString);
        }
    }

    /**
     * Initializes this preference.
     */
    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {

        if (restorePersistedValue) {

            // (From Android Docs) "You cannot use the defaultValue as the default value in the
            // getPersisted*() method, because its value is always null when restorePersistedValue
            // is true."

            final String positionString = super.getPersistedString( null );

            Log.d(TAG, "onSetInitialValue: setting position to " + positionString);

            // This shouldn't throw an exception because we already set a valid default at
            // DataManager#getPosition
            this.position = NamedGeoPosition.from(positionString);
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getString(index);
    }

    /** Getter for the actually configured position. */
    NamedGeoPosition getPosition() {
        return position;
    }

    /** Class to receive and process results of {@link GeocodeIntentService} */
    private class AddressResultReceiver extends ResultReceiver {

        AddressResultReceiver() {
            super(null);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            if (resultData == null) {
                return;
            }

            if (resultCode == GeocodeIntentService.RESULT_SUCCESS) {

                final NamedGeoPosition[] results =
                        NamedGeoPosition.convertParcelableAddressesToPositions(
                            resultData.getParcelableArray(GeocodeIntentService.BUNDLE_KEY_RESULTS) );

                getActivity().runOnUiThread( () ->
                        LocationPreference.this.setSearchResults(results) );

            } else {

                final int errorMessageId =
                        resultData.getInt(GeocodeIntentService.BUNDLE_KEY_ERROR_MESSAGE);

                getActivity().runOnUiThread( () ->
                        Toast.makeText(
                                LocationPreference.super.getContext(),
                                errorMessageId,
                                Toast.LENGTH_SHORT).show() );
            }

            getActivity().runOnUiThread(() ->
                    LocationPreference.this.progressBar.setVisibility(View.GONE));
        }
    }

    /*
     * Below some "magic" to save the state of this DialogPreference - see the android tutorial for
     * information ;)
     */

    @Override
    protected Parcelable onSaveInstanceState() {
        final SavedState state = new SavedState(super.onSaveInstanceState());

        // We only need to save the preference value if it's not persistent

        if (!this.isPersistent()) {
            Log.d(TAG, "onSaveInstanceState: preference is not persistent - saving position");
            state.position = this.position;
        } else {
            Log.d(TAG, "onSaveInstanceState: preference is persistent - NOT saving position");
        }

        // this isn't part of the persisted preference - we save it anyway
        // the adapter could be null, we have to check it 🙄
        if (this.resultsAdapter != null) {
            state.searchResults = this.resultsAdapter.getResults();
        }

        return state;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {

        if ( !(state instanceof SavedState) ) {
            Log.d(TAG, "onRestoreInstanceState: No state saved - can't be restored");
            super.onRestoreInstanceState(state);
            return;
        }

        final SavedState savedState = (SavedState) state;

        if (savedState.position != null) {

            this.position = savedState.position;
            Log.d(TAG, "onRestoreInstanceState: location := " + savedState.position);
        }

        // initialize text fields - calls also this.onBindDialogView() but sets stored fields
        // afterwards again :-/
        super.onRestoreInstanceState(savedState.getSuperState());

        // finally we set the search results if available - must happen after
        // super#onRestoreInstanceState since the resultsAdapter is null before
        if (savedState.searchResults != null) {

            this.resultsAdapter.setResults(savedState.searchResults);
            Log.d(TAG, "onRestoreInstanceState: restored "
                    + savedState.searchResults.length + " search results");
            this.updateResultsVisibility();

        } else {
            Log.d(TAG, "onRestoreInstanceState: no saved search results");
        }
    }

    private static class SavedState extends BaseSavedState {

        NamedGeoPosition position;
        NamedGeoPosition[] searchResults;

        SavedState(Parcelable superState) {
            super(superState);
        }

        SavedState(Parcel source) {
            super(source);

            final String serializedPosition = source.readString();

            try {

                this.position = NamedGeoPosition.from(serializedPosition);
                Log.d(TAG, "SavedState: read position: " + this.position);

            } catch (IllegalArgumentException e) {

                Log.d(TAG, "SavedState: position couldn't be restored: "
                        + serializedPosition);
                this.position = null;
            }

            try {
                this.searchResults = NamedGeoPosition.convertStringsToPositions(
                        source.createStringArray());
                Log.d(TAG, "SavedState: restored " + this.searchResults.length + " results.");
            } catch (IllegalArgumentException e) {
                Log.d(TAG, "SavedState: search results couldn't be read");
            }
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {

            Log.d(TAG, "writeToParcel: " + this.position);

            if (this.position != null) {
                dest.writeString( this.position.toString() );
            }

            if (this.searchResults != null) {
                dest.writeStringArray(
                        NamedGeoPosition.convertPositionsToStrings(this.searchResults) );
            }

            super.writeToParcel(dest, flags);
        }

        // Standard creator object using an instance of this class
        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {

                    public SavedState createFromParcel(Parcel in) {
                        return new SavedState(in);
                    }

                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };
    }

    private Activity getActivity() {
        return (Activity) super.getContext();
    }
}
