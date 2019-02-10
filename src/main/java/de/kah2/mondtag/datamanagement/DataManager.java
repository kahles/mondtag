package de.kah2.mondtag.datamanagement;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import org.threeten.bp.LocalDate;
import org.threeten.bp.ZoneId;

import java.util.Locale;

import de.kah2.libZodiac.Calendar;
import de.kah2.libZodiac.DateRange;
import de.kah2.libZodiac.planetary.Position;
import de.kah2.mondtag.Mondtag;
import de.kah2.mondtag.R;
import de.kah2.mondtag.calendar.InterpreterMapping;

/**
 * This class manages all data needed by Mondtag.
 * It is intended to be accessed through application context.
 *
 * E.g. <code>((Mondtag) getContext).getDataManager();</code>
 *
 * @see Mondtag
 *
 * Created by kahles on 22.08.16.
 */
public class DataManager {

    private final static String TAG = DataManager.class.getSimpleName();

    private final static int DAYS_TO_CALCULATE_AHEAD = 7;

    public final static NamedGeoPosition DEFAULT_LOCATION_MUNICH =
            new NamedGeoPosition("Munich Germany",48.137,11.57521);

    private final static String DEFAULT_TZ = ZoneId.systemDefault().toString();

    private final Context context;

    private final DataFetcher fetcher;

    private final DataFetchingMessenger messenger;

    private boolean userShouldReviewConfig = false;

    private boolean importedExistingData = false;

    private InterpreterMapping selectedInterpreter;

    private Calendar calendar;

    /**
     * Should only be called by {@link Mondtag}!
     */
    public DataManager(Context context) {

        this.context = context;

        this.fetcher = new DataFetcher(context);
        this.messenger = new DataFetchingMessenger();

        this.calendar = this.createEmptyCalender();
    }

    private Calendar createEmptyCalender() {

        final LocalDate startDate = LocalDate.now();

        final Position position = this.getPosition();
        final ZoneId zoneId = this.getZoneId();
        final DateRange range = new DateRange( startDate,
                startDate.plusDays(DAYS_TO_CALCULATE_AHEAD) );

        final Calendar calendar = new Calendar( position, zoneId, range );

        calendar.addProgressListener(this.fetcher);
        calendar.addProgressListener(this.messenger);

        return calendar;
    }

    /**
     * Loads the configured observer position needed for rise- and set-calculation or sets the
     * default if <strong>an invalid position</strong> was saved.
     */
    private NamedGeoPosition getPosition() {

        final SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(this.context);

        final String prefKey = this.context.getString(R.string.pref_key_location);

        final String positionString = prefs.getString(prefKey, null);

        NamedGeoPosition position;

        try {

            position = NamedGeoPosition.from(positionString);

        } catch (Exception e) {

            Log.i( TAG, "getPosition: couldn't parse position, using default. "
                        + e.getMessage() );
            position = DEFAULT_LOCATION_MUNICH;

            SharedPreferences.Editor editor = prefs.edit();
            editor.putString( prefKey, position.toString() );
            editor.apply();

            this.userShouldReviewConfig = true;
        }

        return position;
    }

    /**
     * <p>Loads the configured timezone needed for rise- and set-calculation.</p>
     * <p>We don't need to set a default, because the user can't type any time zone.</p>
     */
    public ZoneId getZoneId() {

        final SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(context);

        final String prefKeyTz = context.getString(R.string.pref_key_timezone);

        String timezone = preferences.getString(prefKeyTz, null);

        if (timezone == null) {

            Log.d( TAG, "Setting default timezone: " + DEFAULT_TZ);

            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(prefKeyTz, DEFAULT_TZ);
            editor.apply();

            userShouldReviewConfig = true;

            timezone = DEFAULT_TZ;
        }

        return ZoneId.of(timezone);
    }

    public void extendExpectedRange() {

        final DateRange newRange = new DateRange(
                calendar.getRangeExpected().getStart(),
                calendar.getAllDays().getLast().getDate().plusDays(DAYS_TO_CALCULATE_AHEAD) );

        calendar.setRangeExpected(newRange);
    }

    /**
     * Starts calendar generation in a separate Thread to not block the UI.
     */
     void startCalendarGeneration() {

         final Runnable worker = this.createLibZodiacWorker();
         final Thread workerThread = new Thread(worker);

         workerThread.start();
    }

    /**
     * Creates a {@link Runnable} containing logic for importing and generating data.
     */
    private Runnable createLibZodiacWorker() {

        return () -> {

            // Moves the current Thread into the background
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);

            if ( ! this.importedExistingData ) {

                this.fetcher.importData(this.calendar);
                this.importedExistingData = true;
            }

            this.fetcher.startGeneratingMissingDays(this.calendar);
        };
    }

    /**
     * To use on configuration changes to trigger recalculation of data
     */
    public void resetCalendar() {

        Log.d(TAG, "resetCalendar");

        this.calendar = this.createEmptyCalender();

        DatabaseHelper dbHelper = new DatabaseHelper(this.context);
        dbHelper.resetDatabase(dbHelper.getWritableDatabase());
    }

    /** Tells if config was loaded from defaults */
    public boolean userShouldReviewConfig() {
        return this.userShouldReviewConfig;
    }

    /** To inform data manager, that configuration was initially reviewed by the user */
    public void setConfigReviewed() {
        this.userShouldReviewConfig = false;
    }

    /**
     * @return the calendar or null if none is present
     */
    public Calendar getCalendar() {
        return calendar;
    }

    /** @return the selected interpreter or null if none is selected */
    public InterpreterMapping getSelectedInterpreter() {
        return selectedInterpreter;
    }

    /**
     * @return the resource id of the selected interpreter's name or R.string.interpret_none if none
     * is selected.
     */
    public int getSelectedInterpreterNameId() {

        if (getSelectedInterpreter() == null) {
            return R.string.interpret_none;
        } else {
            return getSelectedInterpreter().getId();
        }
    }

    public void setSelectedInterpreter(InterpreterMapping interpreter) {
        this.selectedInterpreter = interpreter;
    }

    public DataFetchingMessenger getDataFetchingMessenger() {
        return messenger;
    }

    /** Returns system's default locale */
    public static Locale getLocale() {
        return Locale.getDefault();
    }
}
