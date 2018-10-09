package de.kah2.mondtag.settings;

import android.location.Address;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import de.kah2.mondtag.R;
import de.kah2.mondtag.datamanagement.StringConvertiblePosition;

/**
 * <p>This class allows displaying search results of type {@link Address} within a
 * {@link RecyclerView}.</p>
 * It's slight overkill, because normally only one result is returned - but theoretically
 * {@link android.location.Geocoder} could deliver more than one result ...
 */
public class LocationSearchResultListAdapter
        extends RecyclerView.Adapter<LocationSearchResultListAdapter.SearchResult> {

    private NamedStringConvertiblePosition[] results;

    private LocationConsumer locationConsumer;

    @NonNull
    @Override
    public SearchResult onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflatedView = LayoutInflater.from(parent.getContext())
                .inflate( R.layout.location_search_result , parent, false);

        return new SearchResult(inflatedView);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchResult holder, int position) {
        final NamedStringConvertiblePosition result = results[position];
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
     * Viewholder pattern implementation, that binds geocoordinates inclusive a describing name to
     * a list element.
     */
    class SearchResult extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final View itemView;

        private NamedStringConvertiblePosition address;

        SearchResult(View itemView) {
            super(itemView);
            this.itemView = itemView;
        }

        void bindElement(NamedStringConvertiblePosition address) {
            this.address = address;

            final TextView nameView = itemView.findViewById(R.id.location_search_result_name);
            nameView.setText( address.getName() );

            final TextView latitudeView = itemView.findViewById(R.id.location_search_result_latitude);
            latitudeView.setText( address.getFormattedLatitude() );

            final TextView longitudeView = itemView.findViewById(R.id.location_search_result_longitude);
            longitudeView.setText( address.getFormattedLongitude() );

            this.itemView.setOnClickListener(this);
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

    void setResults(NamedStringConvertiblePosition[] results) {
        this.results = results;
    }

    /**
     * The purpose of this class is to easily manage {@link StringConvertiblePosition}s with a
     * describing name.
     */
    static class NamedStringConvertiblePosition extends StringConvertiblePosition {

        private final String name;

        NamedStringConvertiblePosition(Address address) {
            super(address.getLatitude(), address.getLongitude());
            this.name = address.getAddressLine(0);
        }

        public String getName() {
            return name;
        }
    }

    /** Simple interface to deliver a search result to another class */
    interface LocationConsumer {
        void onSearchResultSelected(StringConvertiblePosition position);
    }
}
