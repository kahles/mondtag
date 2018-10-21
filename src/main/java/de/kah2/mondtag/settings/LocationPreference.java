package de.kah2.mondtag.settings;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import de.kah2.mondtag.R;
import de.kah2.mondtag.datamanagement.DataManager;
import de.kah2.mondtag.datamanagement.NamedGeoPosition;
import de.kah2.mondtag.helpers.AbstractSimpleTextWatcher;

import static de.kah2.mondtag.settings.LocationSearchResultListAdapter.LocationConsumer;

/**
 * This is a subclass of {@link DialogPreference} which is used to set-up observer position needed
 * for rise- and set-calculation.
 *
 * Created by kahles on 16.11.16.
 */
public class LocationPreference extends DialogPreference
        implements LocationConsumer {

    private final static String TAG = LocationPreference.class.getSimpleName();

    private TextView locationNameTextView;
    private EditText latitudeField;
    private EditText longitudeField;

    private int editTextDefaultColor;

    private NamedGeoPosition position;

    private boolean listenersEnabled = true;

    public LocationPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        setDialogLayoutResource(R.layout.location_picker_layout);
        setPositiveButtonText(android.R.string.ok);
        setNegativeButtonText(android.R.string.cancel);

        setDialogIcon(null);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        this.initPositionTextFields(view);

        final Button locationSearchButton = view.findViewById(R.id.location_search_button);
        locationSearchButton.setOnClickListener(v -> LocationPreference.this.openSearchDialog());

        final Button locationOpenButton = view.findViewById(R.id.location_open_button);
        locationOpenButton.setOnClickListener(v -> LocationPreference.this.showMap());

        this.editTextDefaultColor = this.latitudeField.getCurrentTextColor();
    }

    private void initPositionTextFields(View view) {

        // get fields
        this.locationNameTextView = view.findViewById(R.id.location_name);
        this.latitudeField = view.findViewById(R.id.location_latitude);
        this.longitudeField = view.findViewById(R.id.location_longitude);

        // set values
        this.setSearchResult( this.getPosition() );

        // add listeners
        this.locationNameTextView.addTextChangedListener(new AbstractSimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (listenersEnabled) {
                    Log.d(TAG, "afterTextChanged: name = " + s.toString());
                    LocationPreference.this.position.setName(s.toString());
                }
            }
        });
        this.addLatLngListener(this.latitudeField, NamedGeoPosition.ValueType.LATITUDE);
        this.addLatLngListener(this.longitudeField, NamedGeoPosition.ValueType.LONGITUDE);
    }

    private void addLatLngListener(EditText source, NamedGeoPosition.ValueType target) {
        source.addTextChangedListener( new AbstractSimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {

                if (listenersEnabled) {
                    final double value = Double.parseDouble( s.toString() );

                    boolean isValid = true;

                    try {
                        LocationPreference.this.position.set(target,
                                Double.valueOf( s.toString() ) );
                        Log.d(TAG, "afterTextChanged: " + target + "=" + s.toString());

                    } catch (IllegalArgumentException e) {

                        Log.d(TAG, "afterTextChanged: invalid " + target + ": " + value);
                        isValid = false;
                    }

                    // mark field to show invalid data was entered
                    LocationPreference.this.setUserInputValid( isValid, source );
                }
            }
        });
    }

    private void setUserInputValid(boolean isValid, EditText inputField) {

        if (isValid) {
            inputField.setTextColor(this.editTextDefaultColor);
        } else {
            inputField.setTextColor(ContextCompat.getColor(getContext(), R.color.invalid_text));
        }

        ((AlertDialog) this.getDialog()).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(isValid);
    }

    /**
     * Opens a dialog for geocoding-search
     */
    private void openSearchDialog() {
        Log.d(TAG, "opening position search dialog");

        final LocationSearchDialogFragment searchDialog = new LocationSearchDialogFragment();

        searchDialog.setLocationConsumer(this);

        searchDialog.show(
                ((Activity) getContext()).getFragmentManager(),
                LocationSearchDialogFragment.class.getSimpleName());
    }

    /** Callback for {@link LocationSearchDialogFragment} */
    @Override
    public void setSearchResult(NamedGeoPosition position) {

        // do not trigger listeners
        this.listenersEnabled = false;

        this.position = position;
        this.locationNameTextView.setText( this.position.getName() );
        this.latitudeField.setText( this.position.getFormattedLatitude() );
        this.longitudeField.setText( this.position.getFormattedLongitude() );

        this.listenersEnabled = true;
    }

    /**
     * Creates an {@link Intent} to show a position on a map and starts an activity for it.
     */
    private void showMap() {
        Log.d(TAG, "showing map");
        final Uri geoUri = this.getPosition().toGeoUri();
        Intent mapCall = new Intent(Intent.ACTION_VIEW, geoUri);
        getContext().startActivity(mapCall);
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
     * Initializes this preference. Since we already set a default in
     * {@link DataManager#getPosition()}, we don't need to do it here 😎
     */
    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        if (restorePersistedValue) {

            /*
             * (From Android Docs) "You cannot use the defaultValue as the default value in the
             * getPersisted*() method, because its value is always null when restorePersistedValue
             * is true."
             */
            final String positionString = super.getPersistedString( null );
            try {

                this.position = NamedGeoPosition.from(positionString);

            } catch (Exception e) {
                Log.w(TAG, "onSetInitialValue: persisted position couldn't be loaded: \"" +
                        positionString + "\" - setting default", e);
                this.position = DataManager.DEFAULT_LOCATION_MUNICH;
            }

        } else {

            Log.d(TAG, "onSetInitialValue: no persisted position exists - setting default");

            final String positionString = (String) defaultValue;

            this.position = NamedGeoPosition.from(positionString);

            persistString(positionString);
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

        return state;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {

        if ( !(state instanceof SavedState) ) {
            Log.d(TAG, "onRestoreInstanceState: Didn't save the state, so call superclass");
            super.onRestoreInstanceState(state);
            return;
        }

        final SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());

        if (savedState.position != null) {
            this.position = savedState.position;
            Log.d(TAG, "onRestoreInstanceState: location := " + savedState.position);
        }
        Log.d(TAG, "onRestoreInstanceState: locationInfoText := " + savedState.infoText);
    }

    private static class SavedState extends BaseSavedState {

        NamedGeoPosition position;
        String infoText;

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

                Log.d(TAG, "SavedState: position couldn't be restored: " + serializedPosition);
                this.position = null;
            }
            this.infoText = source.readString();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {

            Log.d(TAG, "writeToParcel: " + this.position);

            if (this.position != null) {
                dest.writeString( this.position.toString() );
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
}
