package de.kah2.mondtag.settings;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import java.io.IOException;
import java.util.List;

import de.kah2.mondtag.datamanagement.DataManager;

/**
 * TODO docs
 * <a href="https://developer.android.com/training/location/display-address">Android docs</a>
 */
public class GeocodeIntentService extends IntentService {

    public static final String TAG = GeocodeIntentService.class.getSimpleName();

    public static final int RESULT_SUCCESS = 0;
    public static final int RESULT_FAILURE = 1;

    public static final String RECEIVER = TAG + ".RECEIVER";
    public static final String SEARCH_TERM = TAG + ".SEARCH_TERM";
    public static final String RESULT_DATA_KEY = TAG + ".RESULT_DATA_KEY";

    public static final int MAX_RESULTS = 20;

    protected ResultReceiver receiver;

    /**
     * TODO docs
     */
    public GeocodeIntentService() {
        super( TAG );
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        if (intent == null) {
            return;
        }

        this.receiver = intent.getParcelableExtra(RECEIVER);

        // Get the location passed to this service through an extra.
        final String searchTerm = intent.getStringExtra(SEARCH_TERM);

        List<Address> results = null;

        try {

            final Geocoder geocoder = new Geocoder(this, DataManager.getLocale() );
            results = geocoder.getFromLocationName(searchTerm, MAX_RESULTS);

        } catch (IOException e) {
            Log.e(TAG, "Error while geocoding:", e);
        }

        if (results == null || results.size()  == 0) {

            Log.e(TAG, "No addresses found");
            deliverResultToReceiver(RESULT_FAILURE, null);

        } else {

            final Address[] resultArray =results.toArray( new Address[0] );
            deliverResultToReceiver(RESULT_SUCCESS,
                        resultArray );
        }
    }

    private void deliverResultToReceiver(int resultCode, Address[] results) {
        Bundle bundle = new Bundle();

        if (resultCode == RESULT_SUCCESS) {
            Log.d(TAG, "deliverResultToReceiver: delivering " + results.length + " results.");
            bundle.putParcelableArray(RESULT_DATA_KEY, results);
        }

        this.receiver.send(resultCode, bundle);
    }

}
