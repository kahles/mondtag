package de.kah2.mondtag;

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
import de.kah2.mondtag.calendar.ResourceMapper;
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
        GENERATING,

        /** show day details through {@link DayDetailFragment} */
        DAY_DETAILS
    }

    private final static String BUNDLE_KEY_STATE =
            MondtagActivity.class.getName() + ".state";
    private State state = State.UNDEFINED;

    private final static String BUNDLE_KEY_FIRST_START =
            MondtagActivity.class.getName() + ".isFirstStart";
    private boolean isFirstStart = false;

    /** To store the action bar's subtitle at {@link #onSaveInstanceState(Bundle)} */
    private static final String BUNDLE_KEY_SUBTITLE = MondtagActivity.class.getName() + ".subtitle";

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

        super.onCreate(savedInstanceState);

        super.setContentView(R.layout.activity_mondtag);

        final Toolbar toolbar = findViewById(R.id.toolbar);
        super.setSupportActionBar(toolbar);

        // if app wasn't already started ...
        if (savedInstanceState == null) {

            // Mondtag#onCreate creates an empty calendar, when config is missing or invalid.
            // In this case following method call will return true.
            this.isFirstStart = this.getDataManager().userShouldReviewConfig();
            
            if (this.state == State.UNDEFINED) {

                if (this.isFirstStart) {

                    // automatically start configuration if config is missing/invalid
                    this.activateConfiguration();

                } else if ( !getDataManager().getCalendar().isComplete() ) {

                    // if days are missing in expected date range we automatically start generation
                    this.activateDataGeneration();

                } else {

                    // everything ok => show calendar
                    this.activateCalendarView();
                }
            }
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

        if (this.isUiUpdatePostponed) {

            Log.d(TAG, "onResume: UI update was postponed - doing it now");
            this.updateContent();
            this.isUiUpdatePostponed = false;

        }
    }

    /** called by onCreate and {@link CalendarFragment#onOptionsItemSelected(MenuItem)} */
    public void activateConfiguration() {

        Log.d(TAG, "activateConfiguration");
        this.state = State.CONFIGURING;
        this.updateContent();
    }

    /** Called when button to calculate more days is clicked */
    public void extendFuture() {

        this.getDataManager().extendExpectedRange();
        this.activateDataGeneration();
    }

    /** called by onCreate and onBackPressed */
    private void activateDataGeneration() {

        Log.d(TAG, "activateDataGeneration");
        this.state = State.GENERATING;
        this.updateContent();
    }

    /** called by onCreate, onBackPressed and onDataReady */
    private void activateCalendarView() {

        Log.d(TAG, "activateCalendarView");
        this.state = State.DISPLAYING;
        this.updateContent();
    }

    /**
     * Shows detailed view for a selected {@link Day}.
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

            final ActionBar actionBar = this.getSupportActionBar();

            Log.d(TAG, "updateContent: state is " + state);

            switch (state) {

                case CONFIGURING:
                    initConfiguration(transaction, actionBar);
                    break;

                case GENERATING: initDataGeneration(transaction, actionBar);
                    break;

                case DISPLAYING: initCalendarView(transaction);
                    break;

                case DAY_DETAILS: initDayDetailView(transaction, actionBar);
                    break;
            }

            transaction.commit();

        } else {

            Log.d(TAG, "updateContent: UI not visible - skipping update");
            this.isUiUpdatePostponed = true;
        }
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

        final ActionBar bar = getSupportActionBar();
        bar.setDisplayShowHomeEnabled(visible);
        bar.setDisplayHomeAsUpEnabled(visible);
    }

    private void initConfiguration(FragmentTransaction transaction, ActionBar actionBar) {

        actionBar.setSubtitle(R.string.action_settings);

        SettingsFragment fragment = new SettingsFragment();
        transaction.replace(R.id.content_frame,
                fragment, SettingsFragment.TAG);

        if (this.isFirstStart) {
            fragment.showHelpDialog();
            this.isFirstStart = false;
        }
    }

    private void initDataGeneration(FragmentTransaction transaction, ActionBar actionBar) {

        actionBar.setSubtitle(R.string.data_fetching_toolbar_subtitle);

        transaction.replace(R.id.content_frame,
                new DataFetchingFragment(), DataFetchingFragment.TAG);
    }

    private void initCalendarView(FragmentTransaction transaction) {

        // Handles subtitle for itself

        transaction.replace(R.id.content_frame,
                new CalendarFragment(), CalendarFragment.TAG);
    }

    private void initDayDetailView(FragmentTransaction transaction, ActionBar actionBar) {

        actionBar.setSubtitle(
                ResourceMapper.formatLongDate( this.selectedDay.getDate() ) );

        final DayDetailFragment dayDetailFragment = new DayDetailFragment();
        dayDetailFragment.setDay(this.selectedDay);

        transaction.replace(R.id.content_frame,
                dayDetailFragment, DayDetailFragment.TAG);
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
                this.activateDataGeneration();
            } else {
                this.activateCalendarView();
            }

        } else if (state == State.DAY_DETAILS) {

            // here we could also use the backstack but this would lead to more complexity at
            // #updateContent
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
        outState.putSerializable(BUNDLE_KEY_STATE, this.state);
        Log.d(TAG, "onSaveInstanceState: state = " + this.state);
        outState.putBoolean(BUNDLE_KEY_FIRST_START, this.isFirstStart);
        Log.d(TAG, "onSaveInstanceState: isFirstStart = " + this.isFirstStart);

        final ActionBar bar = this.getSupportActionBar();
        String subtitle = "";
        if ( bar.getSubtitle() != null ) {
            subtitle = bar.getSubtitle().toString();
        }
        outState.putString( BUNDLE_KEY_SUBTITLE, subtitle );
        Log.d(TAG, "onSaveInstanceState: subtitle = " + subtitle);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        this.state = (State) savedInstanceState.getSerializable(BUNDLE_KEY_STATE);
        Log.d(TAG, "onRestoreInstanceState: state := " + this.state);
        this.isFirstStart = savedInstanceState.getBoolean(BUNDLE_KEY_FIRST_START);
        Log.d(TAG, "onRestoreInstanceState: isFirstStart := " + this.isFirstStart);

        final ActionBar bar = this.getSupportActionBar();
        final String subtitle = savedInstanceState.getString(BUNDLE_KEY_SUBTITLE);
        bar.setSubtitle( subtitle );
        Log.d(TAG, "onRestoreInstanceState: subtitle := " + subtitle);
    }

    private DataManager getDataManager() {
        return ((Mondtag) getApplicationContext()).getDataManager();
    }
}