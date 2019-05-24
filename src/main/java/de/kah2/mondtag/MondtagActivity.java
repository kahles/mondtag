package de.kah2.mondtag;

// TODO use supportFragmentManager
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import de.kah2.libZodiac.Calendar;
import de.kah2.libZodiac.Day;
import de.kah2.mondtag.calendar.CalendarFragment;
import de.kah2.mondtag.calendar.DayDetailFragment;
import de.kah2.mondtag.datamanagement.DataFetchingFragment;
import de.kah2.mondtag.datamanagement.DataManager;
import de.kah2.mondtag.settings.SettingsFragment;

/**
 * This is THE activity of Mondtag.
 * I switched to only replacing fragments instead of using multiple activities, because state
 * handling is much easier this way - e.g. when the app gets interrupted during start.
 */
public class MondtagActivity extends AppCompatActivity {

    private final static String TAG = MondtagActivity.class.getSimpleName();

    private enum State {

        /** when the app is started */
        UNDEFINED,

        /** {@link CalendarFragment} is active */
        DISPLAYING,

        /** {@link SettingsFragment} is active */
        CONFIGURING,

        /** {@link DataFetchingFragment} is active */
        FETCHING_DATA,

        /** show day details through {@link DayDetailFragment} */
        DAY_DETAILS
    }

    private final static String BUNDLE_KEY_STATE =
            MondtagActivity.class.getName() + ".state";
    private State state = State.UNDEFINED;

    /**
     * Indicates if the UI is in foreground and Fragment transactions are possible.
     * true between {@link #onResume()} and {@link #onPause()}.
     * Needed to prevent running {@link #updateContent()} if calculation finishes while app is in
     * background.
     */
    private boolean isVisible = false;

    /**
     * Indicates if the UI couldn't be updated, because the view wasn't in foreground. If true, the
     * update is done by {@link #onResume()}.
     * @see #isVisible
     */
    private boolean isUiUpdatePostponed = false;

    private Day selectedDay = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.d(TAG, "onCreate");

        super.setContentView(R.layout.activity_mondtag);

        // toolbar must be set before #onCreate
        final Toolbar toolbar = findViewById(R.id.toolbar);
        super.setSupportActionBar(toolbar);

        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            this.state = (State) savedInstanceState.getSerializable(BUNDLE_KEY_STATE);
            Log.d(TAG, "onCreate: restoring state := " + this.state);
        }
    }

    @Override
    protected void onPause() {

        super.onPause();
        Log.d(TAG, "onPause");

        // Prevents running #updateContent when app was sent to background. This could be the case
        // if app is suspended during calculation and calculation finishes is background.
        this.isVisible = false;
    }

    @Override
    protected void onResume() {

        super.onResume();
        Log.d(TAG, "onResume");

        this.isVisible = true;

        this.initActivity();
    }

    private void initActivity() {

        // Mondtag#onCreate constructs a new DataManager which tries to load the config.
        // If config isn't valid the following call will return true.
        if ( this.getDataManager().userShouldReviewConfig() ) {

            this.activateConfiguration();

        } else if ( ! this.getDataManager().getCalendar().isComplete() ) {

            // days are missing in expected date range => we start fetching data
            this.activateDataFetching();

        } else {

            // ok, we have config and data

            if (state == State.UNDEFINED) {

                // app is freshly started and not resumed
                this.activateCalendarView();

            } else if (this.isUiUpdatePostponed) {

                    // app was resumed and we have a pending update
                    Log.d(TAG, "onResume: UI update was postponed - doing it now");
                    this.updateContent();
                    this.isUiUpdatePostponed = false;
            }

            // if this point is reached, we seem to have fully resumed the app and nothing is to do
        }
    }

    /**
     * called by {@link #initActivity()} and
     * {@link CalendarFragment#onOptionsItemSelected(MenuItem)}
     */
    public void activateConfiguration() {

        Log.d(TAG, "activateConfiguration");
        this.state = State.CONFIGURING;
        this.updateContent();
    }

    /**
     * Called when button to calculate more days is clicked
     * @see de.kah2.mondtag.calendar.DayRecyclerViewAdapter
     */
    public void extendFuture() {
        Log.d(TAG, "extendFuture");
        this.getDataManager().extendExpectedRange();
        this.activateDataFetching();
    }

    /**
     * called by {@link #initActivity()} and {@link #onBackPressed()}
     */
    private void activateDataFetching() {

        Log.d(TAG, "activateDataFetching");
        this.state = State.FETCHING_DATA;

        final FragmentManager manager = this.getFragmentManager();

        this.updateContent();
    }

    /**
     * Prepares displaying of {@link CalendarFragment} through {@link #updateContent()} OR
     * pops one from backStack, if available
     * Called by {@link #initActivity()}, {@link #onBackPressed()} and {@link #onDataReady()}.
     * @see #showDayDetailView(FragmentTransaction) where the existing CalendarFragment gets pushed
     * to backStack
     */
    private void activateCalendarView() {

        final FragmentManager manager = this.getFragmentManager();

        Log.d(TAG, "activateCalendarView");
        this.state = State.DISPLAYING;

        if ( manager.getBackStackEntryCount() > 0 ) {
            manager.popBackStackImmediate();
        } else {
            this.updateContent();
       }
    }

    /**
     * Shows detailed view for a selected {@link Day} when a day is clicked
     * @see CalendarFragment
     */
    public void activateDayDetailView(Day day) {

        Log.d(TAG, "activateDayDetailView");
        this.selectedDay = day;
        this.state = State.DAY_DETAILS;
        this.updateContent();
    }

    /** used to replace the fragments */
    private void updateContent() {

        final FragmentTransaction transaction = getFragmentManager().beginTransaction();

        if (this.isVisible) {

            Log.d(TAG, "updateContent: state is " + state);

            switch (state) {

                case CONFIGURING:
                    showConfiguration(transaction);
                    break;

                case FETCHING_DATA:
                    transaction.replace(R.id.content_frame,
                        new DataFetchingFragment(), DataFetchingFragment.TAG);
                    break;

                case DISPLAYING:
                    transaction.replace(R.id.content_frame,
                        new CalendarFragment(), CalendarFragment.TAG);
                    break;

                case DAY_DETAILS: showDayDetailView(transaction);
                    break;
            }

            transaction.commit();

        } else {

            Log.d(TAG, "updateContent: UI not visible - skipping update");
            this.isUiUpdatePostponed = true;
        }
    }

    private void showConfiguration(FragmentTransaction transaction) {

        SettingsFragment fragment = new SettingsFragment();
        transaction.replace(R.id.content_frame,
                fragment, SettingsFragment.TAG);

        if ( this.getDataManager().userShouldReviewConfig() ) {
            // We do this here to not show the help dialog again if e.g. screen is rotated
            fragment.showHelpDialog();
        }
    }

    /**
     * Since {@link DayDetailFragment} is called from {@link CalendarFragment} and we ant to return
     * to the same position in Calendar afterwards, we push the fragment to backStack here ...
     * @see #activateCalendarView() where it gets restored
     */
    private void showDayDetailView(FragmentTransaction transaction) {

        final DayDetailFragment dayDetailFragment = new DayDetailFragment();
        dayDetailFragment.setDay(this.selectedDay);

        transaction.replace(R.id.content_frame, dayDetailFragment, DayDetailFragment.TAG)
            .addToBackStack(null);
    }

    public void showInfo() {

        Log.d(TAG, "Showing info ...");
        InfoDialogFragment infoDialog = new InfoDialogFragment();
        infoDialog.show( getFragmentManager() );
    }

    /**
     * Shortcut for {@link AppCompatActivity#getSupportActionBar()}.
     * <p>Since this is our only Activity and we set an ActionBar at {@link #onCreate(Bundle)}, this
     * should never be null.</p>
     */
    @NonNull
    @Override
    public ActionBar getSupportActionBar() {

        final ActionBar actionBar = super.getSupportActionBar();
        assert actionBar != null;
        return actionBar;
    }

    /**
     * To hide or show the back-/up-button - should be called by every fragment to ensure proper
     * display of the actionbar.
     */
    public void setUpButtonVisible(boolean visible) {

        final ActionBar bar = this.getSupportActionBar();

        bar.setDisplayShowHomeEnabled(visible);
        bar.setDisplayHomeAsUpEnabled(visible);
    }

    /** Callback for {@link DataFetchingFragment} */
    public void onDataReady() {
        this.activateCalendarView();
    }

    /**
     * When the user presses "back" the app is closed, except if {@link SettingsFragment} or
     * {@link DayDetailFragment} are active. In this case the app returns to calendar view if data
     * is available OR to data fetching, if data needs to be generated.
     */
    @Override
    public void onBackPressed() {

        Log.d(TAG, "onBackPressed: state was " + this.state);

        if (this.state == State.CONFIGURING) {

            // User comes back from configuration
            this.getDataManager().setConfigReviewed();

            final Calendar calendar = this.getDataManager().getCalendar();

            if (calendar == null || !calendar.isComplete()) {
                this.activateDataFetching();
            } else {
                this.activateCalendarView();
            }

        } else if (state == State.DAY_DETAILS) {

            this.selectedDay = null;
            this.activateCalendarView();

        } else {

            Log.d(TAG, "onBackPressed: calling super.onBackPressed()");
            super.onBackPressed();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save the state to be recovered at #onCreate
        outState.putSerializable(BUNDLE_KEY_STATE, this.state);
        Log.d(TAG, "onSaveInstanceState: state = " + this.state);
    }

    private DataManager getDataManager() {
        return ((Mondtag) getApplicationContext()).getDataManager();
    }
}