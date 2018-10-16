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

import de.kah2.mondtag.R;
import de.kah2.mondtag.datamanagement.DataManager;

/**
 * This is an intent service to allow geocoing in background. See
 * <a href="https://developer.android.com/training/location/display-address">Android docs</a> for
 * details.
 */
public class GeocodeIntentService extends IntentService {

    public static final String TAG = GeocodeIntentService.class.getSimpleName();

    static final int RESULT_SUCCESS = 0;
    static final int RESULT_FAILURE = 1;

    static final String BUNDLE_KEY_RECEIVER = TAG + ".RECEIVER";
    static final String BUNDLE_KEY_SEARCH_TERM = TAG + ".SEARCH_TERM";
    static final String BUNDLE_KEY_RESULTS = TAG + ".RESULTS";
    static final String BUNDLE_KEY_ERROR_MESSAGE = TAG + ".ERROR";

    public static final int MAX_RESULTS = 20;

    private ResultReceiver receiver;

    public GeocodeIntentService() {
        super( TAG );
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        if (intent == null) {
            return;
        }

        this.receiver = intent.getParcelableExtra(BUNDLE_KEY_RECEIVER);

        // Get the location passed to this service through an extra.
        final String searchTerm = intent.getStringExtra(BUNDLE_KEY_SEARCH_TERM);

        List<Address> results = null;

        try {

            final Geocoder geocoder = new Geocoder(this, DataManager.getLocale() );

            results = geocoder.getFromLocationName(searchTerm, MAX_RESULTS);

        } catch (IOException e) {
            Log.e(TAG, "Error while geocoding:", e);
        }

        if (results == null) {

            Log.e(TAG, "onHandleIntent: geocoder returned null value");

            this.deliverResultToReceiver( RESULT_FAILURE,
                    null, R.string.location_search_connection_error);

        } else if (results.size() == 0) {

            Log.e(TAG, "No addresses found");
            deliverResultToReceiver( RESULT_FAILURE,
                    null, R.string.location_search_no_results );

        } else {

            final Address[] resultArray =results.toArray( new Address[0] );
            deliverResultToReceiver( RESULT_SUCCESS, resultArray, 0);
        }
    }

    private void deliverResultToReceiver(int resultCode, Address[] addresses, int messageId) {

        final Bundle bundle = new Bundle();

        if (resultCode == RESULT_SUCCESS) {

            Log.d(TAG, "deliverResultToReceiver: delivering " + addresses.length + " results.");
            bundle.putParcelableArray(BUNDLE_KEY_RESULTS, addresses);

        } else {

            Log.d(TAG, "deliverResultToReceiver: delivering error message");
            bundle.putInt(BUNDLE_KEY_ERROR_MESSAGE, messageId);
        }

        this.receiver.send(resultCode, bundle);
    }
}
