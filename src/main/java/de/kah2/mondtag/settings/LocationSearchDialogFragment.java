package de.kah2.mondtag.settings;

import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import de.kah2.mondtag.R;
import de.kah2.mondtag.datamanagement.StringConvertiblePosition;

/**
 * <p>Dialog for geocoding. Allows entering a location and search its coordinates.</p>
 * <p>Called by {@link LocationPreference}, uses {@link LocationFetchingTask for search}.</p>
 */
public class LocationSearchDialogFragment extends DialogFragment {

    private LocationReceiver receiver;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        final View dialog = inflater.inflate(R.layout.fragment_location_search_dialog, container, false);

        final Button searchButton = dialog.findViewById(R.id.location_search_button);
        searchButton.setOnClickListener(view -> LocationSearchDialogFragment.this.startSearch());

        final Button cancelButton = dialog.findViewById(R.id.location_search_cancel_button);
        cancelButton.setOnClickListener(view -> LocationSearchDialogFragment.this.onCancel());

        return dialog;
    }

    void setLocationReceiver(LocationReceiver receiver) {
        this.receiver = receiver;
    }

    private void startSearch() {

        // TODO start a search
    }

    // TODO add callback for searching task

    private void onCancel() {
        this.dismiss();
    }

    private void onSearchedItemSelected() {

        // TODO implement item selection

        this.receiver.onSearchResultSelected(new StringConvertiblePosition(1,1));
    }

    interface LocationReceiver {
        void onSearchResultSelected(StringConvertiblePosition position);
    }
}
