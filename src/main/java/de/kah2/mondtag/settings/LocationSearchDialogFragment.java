package de.kah2.mondtag.settings;

import android.app.DialogFragment;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.ResultReceiver;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import de.kah2.mondtag.R;
import de.kah2.mondtag.datamanagement.NamedGeoPosition;

import static de.kah2.mondtag.settings.LocationSearchResultListAdapter.LocationConsumer;

/**
 * <p>Dialog for geocoding. Allows entering a location and search its coordinates.</p>
 * <p>Called by {@link LocationPreference}, uses {@link GeocodeIntentService for search}.</p>
 */
public class LocationSearchDialogFragment extends DialogFragment
        implements LocationConsumer {

    private static final String TAG = LocationSearchDialogFragment.class.getSimpleName();

    private String searchTerm = "";

    private ProgressBar progressBar;

    private RecyclerView resultListView;

    private LocationSearchResultListAdapter resultsAdapter;

    private LocationConsumer consumer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            this.searchTerm =
                    savedInstanceState.getString(GeocodeIntentService.BUNDLE_KEY_SEARCH_TERM);

                final String[] strings = savedInstanceState.getStringArray(
                        GeocodeIntentService.BUNDLE_KEY_RESULTS );

                if (strings != null) {
                    this.resultsAdapter.setResults( convertStringsToPositions(strings) );
                }
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        final View dialog = inflater.inflate(
                R.layout.fragment_location_search_dialog, container, false);

        final Button searchButton = dialog.findViewById(R.id.location_search_button);
        searchButton.setOnClickListener(view -> LocationSearchDialogFragment.this.startSearch());

        final Button cancelButton = dialog.findViewById(R.id.location_search_cancel_button);
        cancelButton.setOnClickListener(view -> LocationSearchDialogFragment.this.onCancel());

        createSearchTermField(dialog);

        this.resultListView = createResultListView(dialog);

        this.progressBar = dialog.findViewById(R.id.location_search_progressbar);

        return dialog;
    }

    private void createSearchTermField(View parent) {
        final EditText searchTermField = parent.findViewById(R.id.location_search_term_field);
        searchTermField.setText(this.searchTerm);
        searchTermField.addTextChangedListener(new AbstractSimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {

                LocationSearchDialogFragment.this.searchTerm = s.toString();

                Log.d(TAG, "afterTextChanged: searchTerm := "
                        + LocationSearchDialogFragment.this.searchTerm);
            }
        });
    }

    private RecyclerView createResultListView(View parent) {
        final RecyclerView view = parent.findViewById(R.id.locations_search_result_list);

        final LinearLayoutManager linearLayoutManager =
                new LinearLayoutManager(getActivity().getApplicationContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        view.setLayoutManager(linearLayoutManager);

        this.resultsAdapter = new LocationSearchResultListAdapter();
        this.resultsAdapter.setLocationConsumer(this);
        view.setAdapter(this.resultsAdapter);

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        outState.putString( GeocodeIntentService.BUNDLE_KEY_SEARCH_TERM, this.searchTerm );

        outState.putStringArray( GeocodeIntentService.BUNDLE_KEY_RESULTS,
                convertPositionsToStrings( this.resultsAdapter.getResults() ) );

        super.onSaveInstanceState(outState);
    }

    private void startSearch() {

        this.progressBar.setVisibility(View.VISIBLE);

        if (!Geocoder.isPresent()) {
            Toast.makeText(getActivity().getApplicationContext(),
                    R.string.location_search_no_geocoder,
                    Toast.LENGTH_LONG).show();
            return;
        }

        final ResultReceiver resultReceiver = new AddressResultReceiver();

        final Intent intent = new Intent( getActivity().getApplicationContext(),
                GeocodeIntentService.class );

        intent.putExtra(GeocodeIntentService.BUNDLE_KEY_RECEIVER, resultReceiver);
        intent.putExtra(GeocodeIntentService.BUNDLE_KEY_SEARCH_TERM, this.searchTerm);

        getActivity().startService(intent);
    }

    private void onCancel() {
        this.dismiss();
    }

    @Override
    public void setSearchResult(NamedGeoPosition position) {
        this.consumer.setSearchResult(position);
        this.dismiss();
    }

    /** Class to receive results of {@link GeocodeIntentService} */
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

                final Parcelable[] parcelables =
                        resultData.getParcelableArray( GeocodeIntentService.BUNDLE_KEY_RESULTS);

                final NamedGeoPosition[] results =
                        new NamedGeoPosition[parcelables.length];

                for (int i = 0; i < results.length; i++) {

                    final Address address = (Address) parcelables[i];

                    if (address != null) {
                        results[i] = new NamedGeoPosition(address);
                    }
                }

                LocationSearchDialogFragment.this.resultsAdapter.setResults(results);
                getActivity().runOnUiThread( () ->
                    LocationSearchDialogFragment.this.resultListView.setVisibility(View.VISIBLE) );
            } else {

                final int errorMessageId =
                        resultData.getInt(GeocodeIntentService.BUNDLE_KEY_ERROR_MESSAGE);

                getActivity().runOnUiThread( () ->
                Toast.makeText(
                        getActivity().getApplicationContext(),
                        errorMessageId,
                        Toast.LENGTH_SHORT).show() );
            }

            getActivity().runOnUiThread(() ->
                    LocationSearchDialogFragment.this.progressBar.setVisibility(View.GONE));
        }
    }

    void setLocationConsumer(LocationConsumer consumer) {
        this.consumer = consumer;
    }

    /**
     * Bloat code for serialization
     */
    private static NamedGeoPosition[] convertStringsToPositions(String[] strings) {

        if (strings == null)
            return null;

        final NamedGeoPosition[] positions = new NamedGeoPosition[strings.length];

        for (int i = 0; i < strings.length; i++) {
            positions[i] = NamedGeoPosition.from(strings[i]);
        }

        return positions;
    }

    /**
     * Bloat code for serialization
     */
    private static String[] convertPositionsToStrings(NamedGeoPosition[] positions) {

        if (positions == null)
            return null;

        final String[] strings = new String[positions.length];

        for (int i = 0; i < positions.length; i++) {
            strings[i] = positions[i].toString();
        }

        return strings;
    }
}
