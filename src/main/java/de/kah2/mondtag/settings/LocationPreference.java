package de.kah2.mondtag.settings;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.location.Location;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import de.kah2.mondtag.R;
import de.kah2.mondtag.datamanagement.StringConvertiblePosition;

/**
 * This is a subclass of {@link DialogPreference} which is used to set-up observer position needed
 * for rise- and set-calculation.
 *
 * Created by kahles on 16.11.16.
 */
public class LocationPreference extends DialogPreference {

    private final static String TAG = LocationPreference.class.getSimpleName();

    private EditText locationField;
    private TextView locationInfoTextView;

    private TextWatcher locationWatcher;

    private Button locationOpenButton;
    private Button locationPickButton;

    private StringConvertiblePosition location;

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

        this.locationField = view.findViewById(R.id.location);

        if (this.location != null) {
            this.locationField.setText(this.location.toString());
        }

        this.locationField.addTextChangedListener(getLocationTextWatcher());

        this.locationInfoTextView = view.findViewById(R.id.location_info);

        this.locationPickButton = view.findViewById(R.id.location_pick_button);
        locationPickButton.setOnClickListener(v -> requestLocation());

        this.locationOpenButton = view.findViewById(R.id.location_open_button);
        locationOpenButton.setOnClickListener(v -> LocationPreference.this.showMap());
    }

    /**
     * Creates a {@link TextWatcher} if not already present that tries to read
     * {@link StringConvertiblePosition}s on text changes.
     */
    private TextWatcher getLocationTextWatcher() {
        if (this.locationWatcher == null)
            this.locationWatcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { /*nothing*/ }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    try {
                        LocationPreference.this.location =
                                StringConvertiblePosition.from( charSequence.toString() );
                        LocationPreference.this.locationInfoTextView.setText("");
                        setButtonsEnabled(true);
                    } catch (Exception e) {
                        LocationPreference.this.locationInfoTextView.setText(
                                getContext().getString(R.string.location_not_valid));
                        setButtonsEnabled(false);
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) { /*nothing*/ }
            };
        return this.locationWatcher;
    }

    /**
     * Saves the configured location if dialog was closed by clicking ok-button.
     */
    @Override
    protected void onDialogClosed(boolean okPressed) {
        super.onDialogClosed(okPressed);

        if (okPressed) {
            final String positionString = this.location.toString();

            Log.d(TAG, "onDialogClosed: saving location \"" + positionString + "\"");
            persistString(positionString);
        }

        this.locationField.removeTextChangedListener(getLocationTextWatcher());
    }

    /**
     * Tries to load the persisted position data.
     */
    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        if (restorePersistedValue) {
            final String positionString = this.getPersistedString(null);
            try {

                this.location = StringConvertiblePosition.from(positionString);

                // If no exception occurred, we're done.
                return;

            } catch (Exception e) {
                Log.e(TAG, "onSetInitialValue: \"" + positionString + "\"", e);
                this.locationInfoTextView.setText(
                        getContext().getString(R.string.location_info_default_loaded) );
            }
        }

        // no persisted position exists or it' couldn't be read:

        final String positionString = (String) defaultValue;

        this.location = StringConvertiblePosition.from(positionString);

        persistString(positionString);
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getString(index);
    }

    /**
     * "Orders" the location at {@link SettingsFragment}.
     */
    private void requestLocation() {
        Log.d(TAG, "requestLocation ...");

        SettingsFragment fragment = (SettingsFragment) ((Activity) getContext())
                .getFragmentManager().findFragmentByTag(SettingsFragment.TAG);
        fragment.requestLocation(this);
    }

    /**
     * Callback to set the requested location.
     */
    public void onLocationDelivered(Location location) {
        if (location == null) {

            Log.d(TAG, "onLocationDelivered: null returned as location");
            this.locationInfoTextView.setText(
                    getContext().getString(R.string.location_not_available));
        } else {
            this.locationInfoTextView.setText(getContext().getString(R.string.location_last_known));
            this.locationField.setText( location.getLatitude() +
                            StringConvertiblePosition.VALUE_SEPARATOR + location.getLongitude() );
            this.locationField.setEnabled(true);
        }
    }

    /**
     * Creates an {@link Intent} to show a location on a map and starts an activity for it.
     */
    private void showMap() {
        Log.d(TAG, "showing map");
        final Uri geoUri = this.getLocation().toGeoUri();
        Intent mapCall = new Intent(Intent.ACTION_VIEW, geoUri);
        getContext().startActivity(mapCall);
    }

    /**
     * Is used to disable the ok-button if an invalid location was entered.
     */
    private void setButtonsEnabled(boolean enabled) {
        ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(enabled);
        this.locationOpenButton.setEnabled(enabled);
    }

    /** Getter for the actually configured location. */
    public StringConvertiblePosition getLocation() {
        return location;
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
            Log.d(TAG, "onSaveInstanceState: preference is not persistent - saving location");
            state.position = this.location;
        } else {
            Log.d(TAG, "onSaveInstanceState: preference is persistent - NOT saving location");
        }

        if (this.locationInfoTextView != null) {
            state.infoText = this.locationInfoTextView.getText().toString();
        }

        return state;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {

        if ( state == null || !(state instanceof SavedState) ) {
            Log.d(TAG, "onRestoreInstanceState: Didn't save the state, so call superclass");
            super.onRestoreInstanceState(state);
            return;
        }

        final SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());

        if (savedState.position != null) {
            final String locationString = savedState.position.toString();
            Log.d(TAG, "onRestoreInstanceState: position := " + locationString);
            this.locationField.setText(locationString);
        }
        Log.d(TAG, "onRestoreInstanceState: locationInfoText := " + savedState.infoText);

        if (this.locationInfoTextView != null) {
            this.locationInfoTextView.setText(savedState.infoText);
        }
    }

    private static class SavedState extends BaseSavedState {

        private static final int INDEX_POSITION = 0;
        private static final int INDEX_TEXT = 1;

        StringConvertiblePosition position;
        String infoText;

        public SavedState(Parcelable superState) {
            super(superState);
        }

        public SavedState(Parcel source) {
            super(source);
            final String[] values = new String[2];
            source.readStringArray(values);

            try {
                // We transform the String to a StringConvertiblePosition to have validation
                this.position = StringConvertiblePosition.from( values[INDEX_POSITION] );
                Log.d(TAG, "SavedState: read position: " + this.position);
            } catch (Exception e) {
                Log.d(TAG, "SavedState: position couldn't be restored: " + position);
                this.position = null;
            }
            this.infoText = values[INDEX_TEXT];
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {

            Log.d(TAG, "writeToParcel: " + this.position);

            final String[] values = new String[2];
            if (this.position == null) {
                values[INDEX_POSITION] = "";
            } else {
                values[INDEX_POSITION] = this.position.toString();
            }
            values[INDEX_TEXT] = this.infoText;

            dest.writeStringArray(values);

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
