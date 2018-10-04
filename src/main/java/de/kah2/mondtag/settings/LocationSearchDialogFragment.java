package de.kah2.mondtag.settings;

import android.app.DialogFragment;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.os.ResultReceiver;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.Arrays;

import de.kah2.mondtag.R;
import de.kah2.mondtag.datamanagement.StringConvertiblePosition;

/**
 * <p>Dialog for geocoding. Allows entering a location and search its coordinates.</p>
 * <p>Called by {@link LocationPreference}, uses {@link GeocodeIntentService for search}.</p>
 */
public class LocationSearchDialogFragment extends DialogFragment {

    private static final String TAG = LocationSearchDialogFragment.class.getSimpleName();

    public static final String BUNDLE_KEY_SEARCH_TERM = TAG + ".SEARCH_TERM";

    private String searchTerm = "";

    private ProgressBar progressBar;

    private LocationConsumer consumer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            this.searchTerm = savedInstanceState.getString(BUNDLE_KEY_SEARCH_TERM);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        final View dialog = inflater.inflate(R.layout.fragment_location_search_dialog, container, false);

        final Button searchButton = dialog.findViewById(R.id.location_search_button);
        searchButton.setOnClickListener(view -> LocationSearchDialogFragment.this.startSearch());

        final Button cancelButton = dialog.findViewById(R.id.location_search_cancel_button);
        cancelButton.setOnClickListener(view -> LocationSearchDialogFragment.this.onCancel());

        final EditText searchTermField = dialog.findViewById(R.id.location_search_term_field);
        searchTermField.setText(this.searchTerm);
        searchTermField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {

                LocationSearchDialogFragment.this.searchTerm = s.toString();

                Log.d(TAG, "afterTextChanged: searchTerm := "
                        + LocationSearchDialogFragment.this.searchTerm);
            }
        });

        this.progressBar = dialog.findViewById(R.id.location_search_progressbar);

        return dialog;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        outState.putString(BUNDLE_KEY_SEARCH_TERM, this.searchTerm);

        super.onSaveInstanceState(outState);
    }

    private void startSearch() {

        this.progressBar.setVisibility(View.VISIBLE);

        if (!Geocoder.isPresent()) {
            Toast.makeText(getActivity().getApplicationContext(),
                    R.string.location_search_no_geocoder_available,
                    Toast.LENGTH_LONG).show();
            return;
        }

        ResultReceiver resultReceiver = new AddressResultReceiver(null);

        Intent intent = new Intent( getActivity().getApplicationContext(),
                GeocodeIntentService.class );

        intent.putExtra(GeocodeIntentService.RECEIVER, resultReceiver);
        intent.putExtra(GeocodeIntentService.SEARCH_TERM, this.searchTerm);

        getActivity().startService(intent);
    }

    private void onCancel() {
        this.dismiss();
    }

    private void onSearchedItemSelected() {

        // TODO implement item selection

        this.consumer.onSearchResultSelected(new StringConvertiblePosition(1,1));
    }

    private class AddressResultReceiver extends ResultReceiver {

        AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            if (resultData == null) {
                return;
            }

            // Show a toast message if an address was found.
            if (resultCode == GeocodeIntentService.RESULT_SUCCESS) {

                Parcelable[] parcelables =
                        resultData.getParcelableArray( GeocodeIntentService.RESULT_DATA_KEY );

                Address[] results = Arrays.copyOf(parcelables, parcelables.length, Address[].class);

                LocationSearchDialogFragment.this.progressBar.setVisibility(View.GONE);

                for (int resultNumber = 0; resultNumber < results.length; resultNumber++) {

                    final Address address = results[resultNumber];

                    // TODO process results
                    /*address.getAddressLine(0);
                    address.getLatitude();
                    address.getLongitude();*/

                    Log.i(TAG, "onReceiveResult: Result #" + resultNumber + ":" );

                    for (int lineNumber = 0; lineNumber <= address.getMaxAddressLineIndex(); lineNumber++) {
                        Log.i( TAG, "onReceiveResult:\t\t" + address.getAddressLine(lineNumber) );
                    }
                }
            }
        }
    }

    void setLocationConsumer(LocationConsumer consumer) {
        this.consumer = consumer;
    }

    interface LocationConsumer {
        void onSearchResultSelected(StringConvertiblePosition position);
    }
}
