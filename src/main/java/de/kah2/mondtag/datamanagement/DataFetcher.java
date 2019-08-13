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
import java.util.stream.StreamSupport;

import de.kah2.zodiac.libZodiac4A.Calendar;
import de.kah2.zodiac.libZodiac4A.Day;
import de.kah2.zodiac.libZodiac4A.DayStorableDataSet;
import de.kah2.zodiac.libZodiac4A.ProgressListener;
import de.kah2.mondtag.Mondtag;

/**
 * This class contains logic to import and calculate needed data.
 */
class DataFetcher implements ProgressListener{

    private final static String TAG = DataFetcher.class.getSimpleName();

    private final Context context;

    private final DatabaseHelper databaseHelper;

    private Instant startTime = null;

    DataFetcher(Context context) {
        this.context = context;
        this.databaseHelper = new DatabaseHelper(context);
    }

    /**
     * Loads existing data and removes days outside expected range
     */
    void importData(Calendar calendar) {
        final List<DayStorableDataSet> loadedData = this.loadData();

        calendar.importDays(loadedData);
        Log.d(TAG, "Imported " + loadedData.size() + " days");

        final List<Day> daysDeleted = calendar.removeOverhead(false);
        this.deleteFromDb(daysDeleted);
    }

    /**
     * Starts calculation of days missing withing expected range
     */
    void startGeneratingMissingDays(Calendar calendar) {

        this.startTime = Clock.systemUTC().instant();

        calendar.startGeneration();
    }

    /**
     * Deletes a {@link List} of {@link Day}s from database.
     */
    private void deleteFromDb(List<Day> days) {

        if ( days == null || days.size() == 0 ) {

            Log.d(TAG, "deleteFromDb: No unwanted past days to delete");
            
        } else {

            final SQLiteDatabase db = databaseHelper.getWritableDatabase();

            final String dates = this.joinDates(days);
            Log.d(TAG, "Deleting " + days.size() + " unused days from database: " + dates );

            final String selection = DatabaseDayEntry.COLUMN_NAME_DATE + " IN (" + dates + ")";

            // Using param whereArgs doesn't work
            final int deleted = db.delete(DatabaseDayEntry.TABLE_NAME, selection, null);

            Log.d(TAG, "Deleted " + deleted + " days");
            db.close();
        }
    }

    /**
     * Transforms a {@link List} of {@link Day}s into a comma-separated list in order to insert them
     * into a database command.
     */
    private String joinDates(List<Day> days) {
        LinkedList<String> dates = new LinkedList<>();
        for (Day day: days) {
            dates.add("'" + day.getDate().toString() + "'");
        }
        return TextUtils.join(",", dates);
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
            Log.d(TAG, "Read entry with ID " + id + ": " + entry.getDate());
            result.add(entry);
            cursor.moveToNext();
        }

        cursor.close();
        db.close();

        return result;
    }

    @Override
    public void onStateChanged(State state) {

        Log.d(TAG, "onStateChanged: " + state);

        if (state == State.FINISHED) {

            SQLiteDatabase db = this.databaseHelper.getWritableDatabase();

            final LinkedList<Day> generatedDays =
                    ((Mondtag) this.context).getDataManager().getCalendar().getNewlyGenerated();

            for (Day day : generatedDays) {
                ContentValues values = new DatabaseDayEntry(day).toContentValues();
                long id = db.insert(DatabaseDayEntry.TABLE_NAME, null, values);
                Log.d(TAG, "Wrote day " + day.getDate() + " --> ID: " + id);
            }
            db.close();

            Instant endTime = Clock.systemUTC().instant();

            Log.i( TAG, "Calculation finished in: "
                    + Duration.between(this.startTime, endTime) );
        }
    }

    @Override
    public void onCalculationProgress(float percent) {
        Log.d(TAG, "Calculation progress is: " + percent);
    }
}
