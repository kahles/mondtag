package de.kah2.mondtag;

import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import de.kah2.libZodiac.Calendar;
import de.kah2.libZodiac.Day;
import de.kah2.mondtag.calendar.CalendarFragment;
import de.kah2.mondtag.calendar.DayDetailFragment;
import de.kah2.mondtag.calendar.InfoDialogFragment;
import de.kah2.mondtag.calendar.InterpretationMenuManager;
import de.kah2.mondtag.calendar.InterpreterMapping;
import de.kah2.mondtag.calendar.ResourceMapper;
import de.kah2.mondtag.datamanagement.DataFetchingFragment;
import de.kah2.mondtag.datamanagement.DataManager;
import de.kah2.mondtag.settings.SettingsFragment;

/**
 * This is THE activity of Mondtag.
 * I switched to only replacing fragments instead of using multiple activities, because state
 * handling is much easier this way - e.g. when the app gets interrupted during start.
 */
public class MondtagActivity extends AppCompatActivity
        implements InterpretationMenuManager.InterpretationChangeListener{

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

    private final InterpretationMenuManager interpretationMenuManager =
            new InterpretationMenuManager();

    private final static String BUNDLE_KEY_INTERPRETER_ID =
            MondtagActivity.class.getName() + ".interpretationNameResId";
    private int interpretationNameResId = R.string.interpret_none;

    private Day selectedDay = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate");

        setContentView(R.layout.activity_mondtag);

        final Toolbar toolbar = findViewById(R.id.toolbar);
        super.setSupportActionBar(toolbar);

        // if app wasn't already started ...
        if (savedInstanceState == null) {

            // Mondtag#onCreate creates an empty calendar, when config is missing or invalid
            // following will return true
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

        this.interpretationMenuManager.resetInterpretationChangeListener();
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

    /** called by onCreate and onOptionsItemSelected */
    private void activateConfiguration() {

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

        final ActionBar actionBar = getSupportActionBar();

        if (!this.isVisible || actionBar == null) {

            Log.d(TAG, "updateContent: UI not visible - skipping update");
            this.isUiUpdatePostponed = true;

        } else {

            Log.d(TAG, "updateContent: state is " + state);

            switch (state) {

                case CONFIGURING: initConfiguration(transaction, actionBar);
                    break;

                case GENERATING: initDataGeneration(transaction, actionBar);
                    break;

                case DISPLAYING: initCalendarView(transaction, actionBar);
                    break;

                case DAY_DETAILS: initDayDetailView(transaction, actionBar);
                    break;
            }

            transaction.commit();

            // This is needed or our menu isn't removed if settings are shown
            this.invalidateOptionsMenu();
        }
    }

    private void initConfiguration(FragmentTransaction transaction, ActionBar actionBar) {

        actionBar.setSubtitle(R.string.action_settings);
        actionBar.setDisplayShowHomeEnabled(true);

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
        actionBar.setDisplayShowHomeEnabled(false);

        transaction.replace(R.id.content_frame,
                new DataFetchingFragment(), DataFetchingFragment.TAG);
    }

    private void initCalendarView(FragmentTransaction transaction, ActionBar actionBar) {

        actionBar.setSubtitle(this.interpretationNameResId);
        actionBar.setDisplayShowHomeEnabled(false);

        transaction.replace(R.id.content_frame,
                new CalendarFragment(), CalendarFragment.TAG);
    }

    private void initDayDetailView(FragmentTransaction transaction, ActionBar actionBar) {

        actionBar.setSubtitle(
                ResourceMapper.formatLongDate( this.selectedDay.getDate() ) );
        actionBar.setDisplayShowHomeEnabled(true);

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
     * Called when the user clicks on a {@link MenuItem}.
     * Can open the info dialog or the settings.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        Log.d(TAG, "onOptionsItemSelected: " + item.getTitle() + " selected.");

        switch (item.getItemId()) {
            case R.id.action_info:
                Log.d(TAG, "Showing info ...");
                DialogFragment infoDialog = new InfoDialogFragment();
                infoDialog.show(getFragmentManager(), InfoDialogFragment.class.getSimpleName());
                return true;
            case R.id.action_settings:
                Log.d(TAG, "Showing settings ...");
                this.activateConfiguration();
                return true;
            case R.id.action_interpretation:
                // Nothing to do
                return true;
            default:
                if ( this.interpretationMenuManager.onMenuItemClick(item) ) {
                    // click got handled by InterpretationMenuManager
                    return true;
                } else {
                    Log.e(TAG, "Unknown Action: " + item.getTitle());
                    return super.onOptionsItemSelected(item);
                }
        }
    }

    /**
     * When the user presses "back" the app is closed, except if configuration is active. In this
     * case the app returns to calendar view if data is available OR to data fetching, if data needs
     * to be generated.
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

            // here we could also use backstack but this would lead to more complexity for showing
            // e.g. the menu
            this.selectedDay = null;
            this.activateCalendarView();
                
        } else {

            super.onBackPressed();
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        if (this.state == State.DISPLAYING) {
            Log.d(TAG, "onPrepareOptionsMenu: showing main menu");

            // clear - otherwise we get duplicate menu entries
            menu.clear();

            getMenuInflater().inflate(R.menu.menu, menu);

            final Menu interpretationsMenu = menu.getItem(0).getSubMenu();
            this.interpretationMenuManager.addInterpreters( interpretationsMenu );
            this.interpretationMenuManager.setInterpretationChangeListener(this);

            return true;

        } else {
            Log.d(TAG, "onPrepareOptionsMenu: hiding menu - state is " + this.state);
            return false;
        }
    }

    @Override
    public void onInterpreterChanged(InterpreterMapping mapping) {

        if (mapping == null) {

            Log.d(TAG, "onInterpreterChanged: mapping is null - removing interpreter");
            this.getDataManager().setSelectedInterpreter(null);
            this.interpretationNameResId = R.string.interpret_none;

        } else {

            Log.d(TAG, "onInterpreterChanged: setting interpreter: "
                    + mapping.getInterpreterName());
            this.getDataManager().setSelectedInterpreter( mapping );
            this.interpretationNameResId = mapping.getId();
        }

        this.updateContent();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(BUNDLE_KEY_STATE, this.state);
        Log.d(TAG, "onSaveInstanceState: state = " + this.state);
        outState.putBoolean(BUNDLE_KEY_FIRST_START, this.isFirstStart);
        Log.d(TAG, "onSaveInstanceState: isFirstStart = " + this.isFirstStart);
        outState.putInt(BUNDLE_KEY_INTERPRETER_ID, this.interpretationNameResId);
        Log.d(TAG, "onSaveInstanceState: interpretationNameResId = "
                + this.interpretationNameResId);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        this.state = (State) savedInstanceState.getSerializable(BUNDLE_KEY_STATE);
        Log.d(TAG, "onRestoreInstanceState: state := " + this.state);
        this.isFirstStart = savedInstanceState.getBoolean(BUNDLE_KEY_FIRST_START);
        Log.d(TAG, "onRestoreInstanceState: isFirstStart := " + this.isFirstStart);
        this.interpretationNameResId = savedInstanceState.getInt(BUNDLE_KEY_INTERPRETER_ID);
        Log.d(TAG, "onRestoreInstanceState: interpretationNameResId := "
                + this.interpretationNameResId);
    }

    private DataManager getDataManager() {
        return ((Mondtag) getApplicationContext()).getDataManager();
    }
}
