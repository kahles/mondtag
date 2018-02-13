package de.kah2.mondtag;

import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import de.kah2.libZodiac.interpretation.Interpreter;
import de.kah2.libZodiac.interpretation.Translatable;
import de.kah2.mondtag.calendar.CalendarFragment;
import de.kah2.mondtag.calendar.InfoDialogFragment;
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

    private final static int STATE_UNDEFINED = -1;
    private final static int STATE_DISPLAYING = 0;
    private final static int STATE_CONFIGURING = 1;
    private final static int STATE_GENERATING = 2;

    private final static String BUNDLE_KEY_STATE =
            MondtagActivity.class.getName() + ".state";
    private int state = STATE_UNDEFINED;

    private final static String BUNDLE_KEY_FIRST_START =
            MondtagActivity.class.getName() + ".isFirstStart";
    private boolean isFirstStart = false;
    
    /**
     * Indicates if the UI is in foreground and Fragment transactions are possible.
     * true between {@link #onResume()} and {@link #onPause()}.
     */
    private boolean isVisible = false;

    /**
     * Indicates if the UI couldn't be updated, because the view wasn't in foreground. If true, the
     * update is done by {@link #onResume()}.
     * @see #isVisible
     */
    private boolean isUiUpdatePostponed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_mondtag);

        final Toolbar toolbar = findViewById(R.id.toolbar);
        // TODO add button for interpretation
        super.setSupportActionBar(toolbar);

        // if app wasn't already started ...
        if (savedInstanceState == null) {

            this.isFirstStart = this.getDataManager().userShouldReviewConfig();
            
            if (this.state == STATE_UNDEFINED) {
                if (this.isFirstStart) {
                    this.activateConfiguration();
                } else if ( !getDataManager().getCalendar().isComplete() ) {
                    this.activateDataGeneration();
                } else {
                    this.activateCalendarView();
                }
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: isVisible := false");
        this.isVisible = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: isVisible := true");
        this.isVisible = true;
        if (this.isUiUpdatePostponed) {
            Log.d(TAG, "onResume: UI update was postponed - doing it now");
            this.updateContent();
            this.isUiUpdatePostponed = false;
        }
    }

    // called by onCreate and onOptionsItemSelected
    private void activateConfiguration() {
        Log.d(TAG, "activateConfiguration");
        this.state = STATE_CONFIGURING;
        this.updateContent();
    }

    public void extendFuture() {
        this.getDataManager().extendExpectedRange();

        this.activateDataGeneration();
    }

    // called by onCreate and onBackPressed
    private void activateDataGeneration() {
        Log.d(TAG, "activateDataGeneration");
        this.state = STATE_GENERATING;
        this.updateContent();
    }

    // called by onCreate, onBackPressed and onDataReady
    private void activateCalendarView() {
        Log.d(TAG, "activateCalendarView");
        this.state = STATE_DISPLAYING;
        this.updateContent();
    }

    private void updateContent() {

        if (!this.isVisible) {
            Log.d(TAG, "updateContent: UI not visible - skipping update");
            this.isUiUpdatePostponed = true;
            return;
        } else {
            Log.d(TAG, "updateContent: updating UI");
        }

        FragmentTransaction transaction = getFragmentManager().beginTransaction();

        // TODO ... may produce NPE ... :-/

        switch (state) {
            case STATE_CONFIGURING:
                getSupportActionBar().setSubtitle(R.string.action_settings);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
                SettingsFragment fragment = new SettingsFragment();
                transaction.replace( R.id.content_frame,
                        fragment, SettingsFragment.TAG );
                if (this.isFirstStart) {
                    fragment.showHelpDialog();
                    this.isFirstStart = false;
                }
                break;
            case STATE_GENERATING:
                getSupportActionBar().setSubtitle(R.string.data_fetching_toolbar_subtitle);
                getSupportActionBar().setDisplayShowHomeEnabled(false);
                transaction.replace( R.id.content_frame,
                        new DataFetchingFragment(), DataFetchingFragment.TAG );
                break;
            case STATE_DISPLAYING:
                initInterpreterName();
                getSupportActionBar().setDisplayShowHomeEnabled(false);
                transaction.replace( R.id.content_frame,
                        new CalendarFragment(), CalendarFragment.TAG );
                break;
        }

        transaction.commit();

        this.invalidateOptionsMenu();
    }

    private void initInterpreterName() {
        final Class<? extends Interpreter> interpreterClass =
                getDataManager().getCalendar().getInterpreterClass();

        if ( interpreterClass == null) {
            getSupportActionBar().setSubtitle("");
        } else {
            getSupportActionBar().setSubtitle( ResourceMapper.getResourceIds(
                    Translatable.getKey( interpreterClass.getName() ) )
                    [ResourceMapper.INDEX_STRING] );
        }
    }

    public void onDataReady() {
        this.activateCalendarView();
    }

    /**
     * Called when the user clicks on a {@link MenuItem}.
     * Can open the info dialog or the settings.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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
            default:
                Log.e(TAG, "Unknown Action: " + item.getTitle());
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * When the user presses "back" the app is closed, except if configuration is active. In this
     * case the app returns to calendar view if data is available OR to data fetching, if data needs
     * to be generated.
     */
    @Override
    public void onBackPressed() {
        if (this.state != STATE_CONFIGURING) {
            super.onBackPressed();
        }

        this.getDataManager().setConfigReviewed();

        if ( !this.getDataManager().getCalendar().isComplete() ) {
            this.activateDataGeneration();
        } else {
            this.activateCalendarView();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(BUNDLE_KEY_STATE, this.state);
        Log.d(TAG, "onSaveInstanceState: state = " + this.state);
        outState.putBoolean(BUNDLE_KEY_FIRST_START, this.isFirstStart);
        Log.d(TAG, "onSaveInstanceState: isFirstStart = " + this.isFirstStart);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        this.state = savedInstanceState.getInt(BUNDLE_KEY_STATE);
        Log.d(TAG, "onRestoreInstanceState: state := " + this.state);
        this.isFirstStart = savedInstanceState.getBoolean(BUNDLE_KEY_FIRST_START);
        Log.d(TAG, "onRestoreInstanceState: isFirstStart := " + this.isFirstStart);
    }

    private DataManager getDataManager() {
        return ((Mondtag) getApplicationContext()).getDataManager();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "onCreateOptionsMenu: creating menu");
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (this.state == STATE_DISPLAYING) {
            Log.d(TAG, "onPrepareOptionsMenu: showing menu");
            return super.onPrepareOptionsMenu(menu);
        } else {
            Log.d(TAG, "onPrepareOptionsMenu: hiding menu");
            return false;
        }
    }
}
