package de.kah2.mondtag.datamanagement;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import de.kah2.zodiac.libZodiac4A.ProgressListener;
import de.kah2.mondtag.Mondtag;
import de.kah2.mondtag.MondtagActivity;
import de.kah2.mondtag.R;
import de.kah2.mondtag.calendar.ResourceMapper;

/**
 * This {@link Fragment} is used to display data fetching/generating progress.
 */
public class DataFetchingFragment extends Fragment
        implements ProgressListener{

    public final static String TAG = DataFetchingFragment.class.getSimpleName();

    private static final String BUNDLE_KEY_ACTION =
            DataFetchingFragment.class.getName() + ".action";
    private TextView actionTextView;

    private static final String BUNDLE_KEY_PROGRESS =
            DataFetchingFragment.class.getName() + ".progress";
    private ProgressBar progressBar;

    private MondtagActivity mondtagActivity;

    @Override
    public void onAttach(Context context) {

        Log.d(TAG, "onAttach");

        super.onAttach(context);

        this.mondtagActivity = (MondtagActivity) context;
    }

    /** Called when restored e.g. after rotation */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View view =
                inflater.inflate(R.layout.fragment_data_fetching_progress, container, false);

        this.actionTextView =
                view.findViewById(R.id.data_fetching_action_text_field);
        this.progressBar =
                view.findViewById(R.id.data_fetching_progress_bar);

        if (savedInstanceState != null) {
            Log.d(TAG, "onCreateView: restoring progress information");
            this.actionTextView.setText( savedInstanceState.getString(BUNDLE_KEY_ACTION) );
            this.progressBar.setProgress( savedInstanceState.getInt(BUNDLE_KEY_PROGRESS) );
        } else {
            Log.d(TAG, "onCreateView: no progress information to restore");
            this.actionTextView.setText( getString(R.string.status_generating) );
        }

        this.mondtagActivity.getSupportActionBar().setSubtitle(R.string.data_fetching_toolbar_subtitle);
        this.mondtagActivity.setUpButtonVisible(false);

        final DataManager dataManager =
                ((Mondtag) getActivity().getApplicationContext()).getDataManager();

        dataManager.getDataFetchingMessenger().setDisplayer(this);

        dataManager.startCalendarGenerationIfNotAlreadyWorking();

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString( BUNDLE_KEY_ACTION, this.actionTextView.getText().toString() );
        outState.putInt( BUNDLE_KEY_PROGRESS, this.progressBar.getProgress() );
    }

    /** Updates {@link #actionTextView} with appropriate status text */
    @Override
    public void onStateChanged(State state) {
        if (mondtagActivity != null) {

            if (state == null) {
                
                this.actionTextView.setText("");

            } else if (state == State.FINISHED) {

                // We're ready and display the calendar
                this.mondtagActivity.onDataReady();

            } else if (state != State.IMPORT_FINISHED) {

            /*
             * We update state information - except for IMPORT_FINISHED, because this state
             * would be displayed too short to be read.
             */
                this.actionTextView.setText(
                        mondtagActivity.getString(
                                ResourceMapper.getResourceIds(state)[ResourceMapper.INDEX_STRING]));
            }
        }
    }

    /** Updates the {@link #progressBar} */
    public void onCalculationProgress(float percent) {

        int absPercent = (int)(percent * 100);
        this.progressBar.setProgress(absPercent);
    }
}