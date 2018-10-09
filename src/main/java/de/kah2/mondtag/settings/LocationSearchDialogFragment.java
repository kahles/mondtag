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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import de.kah2.mondtag.R;
import de.kah2.mondtag.datamanagement.StringConvertiblePosition;

import static de.kah2.mondtag.settings.LocationSearchResultListAdapter.NamedStringConvertiblePosition;
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

    // TODO save instance state of results or -adapter?
    private LocationSearchResultListAdapter resultsAdapter;

    private LocationConsumer consumer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            this.searchTerm =
                    savedInstanceState.getString(GeocodeIntentService.BUNDLE_KEY_SEARCH_TERM);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        final View dialog = inflater.inflate(
                R.layout.fragment_location_search_dialog, container, false);

        this.maximizeDialog();

        final Button searchButton = dialog.findViewById(R.id.location_search_button);
        searchButton.setOnClickListener(view -> LocationSearchDialogFragment.this.startSearch());

        final Button cancelButton = dialog.findViewById(R.id.location_search_cancel_button);
        cancelButton.setOnClickListener(view -> LocationSearchDialogFragment.this.onCancel());

        createSearchTermField(dialog);

        this.resultListView = createResultListView(dialog);

        this.progressBar = dialog.findViewById(R.id.location_search_progressbar);

        return dialog;
    }

    // FIXME dialog changes size - should be set to a fix and reasonable value ...
    private void maximizeDialog() {

        final Window window = super.getDialog().getWindow();

        final int size = ViewGroup.LayoutParams.MATCH_PARENT;

        if (window != null) {
            window.setLayout(size, size);
        }
    }

    private void createSearchTermField(View parent) {
        final EditText searchTermField = parent.findViewById(R.id.location_search_term_field);
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

        outState.putString(GeocodeIntentService.BUNDLE_KEY_SEARCH_TERM, this.searchTerm);

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

        final ResultReceiver resultReceiver = new AddressResultReceiver(null);

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
    public void onSearchResultSelected(StringConvertiblePosition position) {
        this.consumer.onSearchResultSelected(position);
        this.dismiss();
    }

    /** Class to receive results of {@link GeocodeIntentService} */
    private class AddressResultReceiver extends ResultReceiver {

        AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            if (resultData == null) {
                return;
            }

            if (resultCode == GeocodeIntentService.RESULT_SUCCESS) {

                final Parcelable[] parcelables =
                        resultData.getParcelableArray( GeocodeIntentService.BUNDLE_KEY_RESULT_DATA_KEY);

                final NamedStringConvertiblePosition[] results =
                        new NamedStringConvertiblePosition[parcelables.length];

                for (int i = 0; i < parcelables.length; i++) {

                    final Address address = (Address) parcelables[i];
                    results[i] = new NamedStringConvertiblePosition(address);
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
}
