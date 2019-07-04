package de.kah2.mondtag.settings.location;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;

import de.kah2.mondtag.R;
import de.kah2.mondtag.datamanagement.DataManager;
import de.kah2.mondtag.datamanagement.NamedGeoPosition;

/**
 * This is the actual preference class for choosing the observer location.
 * @see LocationPrefDialogFragment for the actual fragment
 */
public class LocationPreference extends DialogPreference {

    private static final String TAG = LocationPreference.class.getSimpleName();

    private NamedGeoPosition position;

    public LocationPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public LocationPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public LocationPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LocationPreference(Context context) {
        super(context);
    }


    public NamedGeoPosition getPosition() {
        return position;
    }

    public void setPosition(NamedGeoPosition position) {

        this.position = position;
        persistString( position.toString() );
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {

        return NamedGeoPosition.from( a.getString(index) );
    }

    @Override
    protected void onSetInitialValue(Object defaultValue) {

        try {

            final String positionString = super.getPersistedString((String) defaultValue);

            setPosition( NamedGeoPosition.from(positionString) );
            Log.d(TAG, "onSetInitialValue: loaded location " + positionString);

        } catch (Exception e) {

            setPosition( DataManager.DEFAULT_LOCATION_MUNICH );
            Log.e(TAG, "onSetInitialValue: loading location failed - using default", e);
        }
    }

    @Override
    public int getDialogLayoutResource() {
        return R.layout.location_preference;
    }
}
