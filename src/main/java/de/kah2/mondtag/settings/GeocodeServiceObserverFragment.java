package de.kah2.mondtag.settings;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import de.kah2.mondtag.datamanagement.NamedGeoPosition;

/**
 * <p>This is pure madness ... 😣</p>
 *
 * <p>... and a {@link Fragment}, that doesn't get destroyed on configuration changes (e.g. screen
 * rotation). This is needed to be available, when {@link GeocodeIntentService} finishes, to receive
 * results and to deliver them when bound to a {@link LocationPreference}-instance.</p>
 * <p>See
 * <a href="https://www.androiddesignpatterns.com/2013/04/retaining-objects-across-config-changes.html">this post</a>
 * and <a href="https://stanmots.blogspot.com/2016/10/androids-bad-company-intentservice.html">this post</a>
 * for further information.
 * </p>
 */
public class GeocodeServiceObserverFragment extends Fragment {

    public static final String TAG = GeocodeServiceObserverFragment.class.getSimpleName();

    private GeocodeResultReceiver geocodeResultReceiver;
    private CallbackClass callbackClass;

    private boolean isSearching = false;
    private boolean isResultDeliverancePostponed = false;

    private NamedGeoPosition[] result;
    private int messageId;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        this.setRetainInstance(true);
    }

    /**
     * Starts the search.
     */
    void startService(Context context, String searchTerm) {

        Log.d(TAG, "startService called");

        this.isSearching = true;
        this.geocodeResultReceiver = new GeocodeResultReceiver();
        final Intent intent = new Intent( context, GeocodeIntentService.class );
        intent.putExtra(GeocodeIntentService.BUNDLE_KEY_RECEIVER, geocodeResultReceiver);
        intent.putExtra(GeocodeIntentService.BUNDLE_KEY_SEARCH_TERM, searchTerm);

        context.startService(intent);
    }

    @Override
    public void onDetach() {
        super.onDetach();

        Log.d(TAG, "onDetach: resetting receiver and callbackClass");

        this.geocodeResultReceiver = null;
        this.callbackClass = null;
    }

    /**
     * Sets the class to be notified, when search is finished. If a search result couldn't be
     * delivered, because no {@link CallbackClass} or context was available, this method also
     * triggers delivering the last result.
     */
    void setCallbackClass(CallbackClass callbackClass) {

        Log.d(TAG, "setCallbackClass --> " + callbackClass.getClass().getSimpleName());

        this.callbackClass = callbackClass;

        if (isResultDeliverancePostponed) {

            Log.d(TAG, "setCallbackClass: delivering result was postponed - delivering now");
            this.isResultDeliverancePostponed = false;
            GeocodeServiceObserverFragment.this.callbackClass.onGeocodeServiceFinished(
                    result, messageId );
        }
    }

    /** Class to receive and process results of {@link GeocodeIntentService} */
    private class GeocodeResultReceiver extends ResultReceiver {

        GeocodeResultReceiver() {
            super(null);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            if (resultCode == GeocodeIntentService.RESULT_SUCCESS) {

                Log.d(TAG, "onReceiveResult: SUCCESS");

                final NamedGeoPosition[] result =
                        NamedGeoPosition.convertParcelableAddressesToPositions(
                                resultData.getParcelableArray(
                                        GeocodeIntentService.BUNDLE_KEY_RESULTS ) );

                tryDeliverResult(result, CallbackClass.NO_ERROR_MESSAGE);

            } else {

                Log.d(TAG, "onReceiveResult: FAILURE");

                final int errorMessageId =
                        resultData.getInt(GeocodeIntentService.BUNDLE_KEY_ERROR_MESSAGE);

                tryDeliverResult(null, errorMessageId );
            }
        }

        private void tryDeliverResult(NamedGeoPosition[] result, int messageId) {

            final Activity activity = GeocodeServiceObserverFragment.this.getActivity();

            if (activity == null || GeocodeServiceObserverFragment.this.callbackClass == null) {

                Log.d(TAG, "tryDeliverResult: can't deliver - postponing result");
                GeocodeServiceObserverFragment.this.result = result;
                GeocodeServiceObserverFragment.this.messageId = messageId;
                GeocodeServiceObserverFragment.this.isResultDeliverancePostponed = true;

            } else {

                Log.d(TAG, "tryDeliverResult: delivering result");
                activity.runOnUiThread( () ->
                    GeocodeServiceObserverFragment.this.callbackClass.onGeocodeServiceFinished(
                        result, messageId ) );
            }

            GeocodeServiceObserverFragment.this.isSearching = false;
        }
    }

    /** Interface to deliver a result list or an error message */
    interface CallbackClass {

        int NO_ERROR_MESSAGE = -1;

        /**
         * @param result a list of results or null if no results are available
         * @param messageId an Android String resource ID of an error message or
         * {@link #NO_ERROR_MESSAGE}, if no error occured and results are available
         */
        void onGeocodeServiceFinished(NamedGeoPosition[] result, int messageId);
    }

    public boolean isSearching() {
        return isSearching;
    }
}