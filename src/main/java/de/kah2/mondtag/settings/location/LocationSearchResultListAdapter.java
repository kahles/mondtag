package de.kah2.mondtag.settings.location;

import android.location.Address;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import de.kah2.mondtag.R;
import de.kah2.mondtag.datamanagement.NamedGeoPosition;

/**
 * <p>This class allows displaying search results of type {@link Address} within a
 * {@link RecyclerView}.</p>
 * It's slight overkill, because normally only one result is returned - but theoretically
 * {@link android.location.Geocoder} could deliver more than one result ...
 */
public class LocationSearchResultListAdapter
        extends RecyclerView.Adapter<LocationSearchResultListAdapter.SearchResult> {

    private NamedGeoPosition[] results;

    private LocationConsumer locationConsumer;

    @NonNull
    @Override
    public SearchResult onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View inflatedView = LayoutInflater.from(parent.getContext())
                .inflate( R.layout.location_search_result , parent, false);

        return new SearchResult(inflatedView);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchResult holder, int position) {
        final NamedGeoPosition result = results[position];
        holder.bindElement(result);
    }

    @Override
    public int getItemCount() {

        if ( results == null ) {
            return 0;
        } else {
            return results.length;
        }
    }

    /**
     * View holder pattern implementation, that binds geocoordinates inclusive a describing name to
     * a list element.
     */
    class SearchResult extends RecyclerView.ViewHolder implements View.OnClickListener {

        private NamedGeoPosition address;

        SearchResult(View itemView) {
            super(itemView);
        }

        void bindElement(NamedGeoPosition address) {
            this.address = address;

            final TextView nameView = itemView.findViewById(R.id.location_search_result_name);
            nameView.setText( address.getName() );

            final TextView latitudeView = itemView.findViewById(R.id.location_search_result_latitude);
            latitudeView.setText( address.getFormattedLatitude() );

            final TextView longitudeView = itemView.findViewById(R.id.location_search_result_longitude);
            longitudeView.setText( address.getFormattedLongitude() );

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Log.d( SearchResult.class.getSimpleName(), "onClick: " + this.address.getName() );

            final LocationConsumer consumer = LocationSearchResultListAdapter.this.locationConsumer;

            if (consumer != null) {
                consumer.onSearchResultSelected(this.address);
            }
        }
    }

    void setLocationConsumer(LocationConsumer locationConsumer) {

        this.locationConsumer = locationConsumer;
    }

    void setResults(NamedGeoPosition[] results) {

        this.results = results;
        this.notifyDataSetChanged();
    }

    /** Simple interface to deliver a search result to another class */
    interface LocationConsumer {
        void onSearchResultSelected(NamedGeoPosition position);
    }

    /**
     * Needed for {@link de.kah2.mondtag.settings.location.LocationPrefDialogFragment} to save
     * instance state.
     */
    String[] getResultsAsStringArray() {

        if (this.results == null || this.results.length == 0) {
            return new String[0];
        } else {
            return NamedGeoPosition.convertPositionsToStrings(this.results);
        }
    }

    /**
     * Needed for {@link de.kah2.mondtag.settings.location.LocationPrefDialogFragment} to restore
     * instance state.
     */
    void setResults(String[] resultStrings) {
        this.results = NamedGeoPosition.convertStringsToPositions(resultStrings);
    }
}
