package de.kah2.mondtag.datamanagement;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

import org.threeten.bp.Clock;
import org.threeten.bp.Duration;
import org.threeten.bp.Instant;

import java.util.LinkedList;
import java.util.List;

import de.kah2.libZodiac.Calendar;
import de.kah2.libZodiac.Day;
import de.kah2.libZodiac.DayStorableDataSet;
import de.kah2.libZodiac.ProgressListener;
import de.kah2.mondtag.Mondtag;

/**
 * This class contains logic to import and calculate needed data.
 */
class DataFetcher implements ProgressListener{

    private final static String LOG_TAG = DataFetcher.class.getSimpleName();

    private final Context context;

    private final DatabaseHelper databaseHelper;

    private Instant startTime = null;

    public DataFetcher(Context context) {
        this.context = context;
        this.databaseHelper = new DatabaseHelper(context);
    }

    /**
     * The main task ...
     * <ol>
     *     <li>Tries to import data</li>
     *     <li>Removes old data</li>
     *     <li>Starts calculation of the rest of needed data</li>
     * </ol>
     */
    public void startLoadingAndGeneratingRest() {

        this.startTime = Clock.systemUTC().instant();

        final List<DayStorableDataSet> loadedData = this.loadData();
        final Calendar calendar = ((Mondtag) this.context).getDataManager().getCalendar();

        calendar.importDays(loadedData);
        Log.d(LOG_TAG, "Imported " + loadedData.size() + " days");

        final List<Day> daysDeleted = calendar.removeOverhead(false);
        deleteFromDb(daysDeleted);

        calendar.startGeneration();
    }

    /**
     * Deletes a {@link List} of {@link Day}s from database.
     */
    private void deleteFromDb(List<Day> days) {
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        int deleted = 0;
        if (days == null) {
            deleted = db.delete(DatabaseDayEntry.TABLE_NAME, null, null);
        } else if (days.size() > 0) {
            final String selection = DatabaseDayEntry.COLUMN_NAME_DATE + " IN (?)";
            final String[] dates = {joinDates(days)};
            deleted = db.delete(DatabaseDayEntry.TABLE_NAME, selection, dates);
        }
        Log.d(LOG_TAG, "Deleted " + deleted + " days");
        db.close();
    }

    /**
     * Transforms a {@link List} of {@link Day}s into a comma-separated list in order to insert them
     * into a database command.
     */
    private String joinDates(List<Day> days) {
        LinkedList<String> dates = new LinkedList<>();
        for (Day day: days) {
            dates.add(day.getDate().toString());
        }
        return TextUtils.join(", ", dates);
    }

    /**
     * Loads all available data from database.
     */
    private List<DayStorableDataSet> loadData() {

        final SQLiteDatabase db = this.databaseHelper.getReadableDatabase();
        Cursor cursor = db.query(
                DatabaseDayEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                DatabaseDayEntry.COLUMN_NAME_DATE + " ASC"
        );

        final List<DayStorableDataSet> result = new LinkedList<>();

        cursor.moveToFirst();

        while ( !cursor.isAfterLast() ) {
            DatabaseDayEntry entry = new DatabaseDayEntry(cursor);
            long id = cursor.getInt(cursor.getColumnIndex(DatabaseDayEntry._ID));
            Log.d(LOG_TAG, "Read entry with ID " + id + ": " + entry.getDate());
            result.add(entry);
            cursor.moveToNext();
        }

        cursor.close();
        db.close();

        return result;
    }

    @Override
    public void onStateChanged(State state) {

        if (state == State.FINISHED) {

            SQLiteDatabase db = this.databaseHelper.getWritableDatabase();

            final LinkedList<Day> generatedDays =
                    ((Mondtag) this.context).getDataManager().getCalendar().getNewlyGenerated();

            for (Day day : generatedDays) {
                ContentValues values = new DatabaseDayEntry(day).toContentValues();
                long id = db.insert(DatabaseDayEntry.TABLE_NAME, null, values);
                Log.d(LOG_TAG, "Wrote day " + day.getDate() + " --> ID: " + id);
            }
            db.close();

            Instant endTime = Clock.systemUTC().instant();

            Log.d( LOG_TAG, "Calculation finished in: "
                    + Duration.between(this.startTime, endTime) );
        }
    }

    @Override
    public void onCalculationProgress(float percent) {
        Log.d(LOG_TAG, "Calculation progress is: " + percent);
    }
}
