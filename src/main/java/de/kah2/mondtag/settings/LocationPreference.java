package de.kah2.mondtag.settings;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.DialogPreference;
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

    private EditText latField;
    private EditText longField;
    private TextView locationInfoTextView;

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

        this.latField = view.findViewById(R.id.location_latitude);
        this.longField = view.findViewById(R.id.location_longitude);

        if (this.location != null) {
            this.latField.setText( this.location.getFormattedLatitude() );
            this.longField.setText( this.location.getFormattedLongitude() );
        }

        this.locationInfoTextView = view.findViewById(R.id.location_info);

        final Button locationSearchButton = view.findViewById(R.id.location_search_button);
        locationSearchButton.setOnClickListener(v -> LocationPreference.this.openSearchDialog());

        final Button locationOpenButton = view.findViewById(R.id.location_open_button);
        locationOpenButton.setOnClickListener(v -> LocationPreference.this.showMap());
    }

    /**
     * Opens a dialog for geocoding-search
     */
    private void openSearchDialog() {
        Log.d(TAG, "opening location search dialog");
        /* TODO implement */
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
            this.location = savedState.position;
            Log.d(TAG, "onRestoreInstanceState: position := " + this.location);
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

        SavedState(Parcelable superState) {
            super(superState);
        }

        SavedState(Parcel source) {
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
                // FIXME save values
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
