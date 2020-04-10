package de.kah2.mondtag.datamanagement;

import android.content.ContentValues;
import android.database.Cursor;
import android.provider.BaseColumns;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;

import de.kah2.zodiac.libZodiac4A.Day;
import de.kah2.zodiac.libZodiac4A.DayStorableDataSet;
import de.kah2.zodiac.libZodiac4A.planetary.ZonedRiseSet;

/**
 * This class is used to map libZodiac-data to database-entries.
 * Created by kahles on 04.10.16.
 */

class DatabaseDayEntry extends DayStorableDataSet implements BaseColumns {

    public static final String TABLE_NAME = "DAY";

    public static final String COLUMN_NAME_DATE = "DATE";
    public static final String COLUMN_NAME_SUN_RISE = "SUN_RISE";
    public static final String COLUMN_NAME_SUN_SET = "SUN_SET";
    public static final String COLUMN_NAME_LUNAR_RISE = "LUNAR_RISE";
    public static final String COLUMN_NAME_LUNAR_SET = "LUNAR_SET";
    public static final String COLUMN_NAME_LUNAR_VISIBILITY = "LUNAR_VISIBILITY";
    public static final String COLUMN_NAME_LUNAR_LONGITUDE = "LUNAR_LONGITUDE";

    DatabaseDayEntry(Day day) {
        super(day);
    }

    DatabaseDayEntry(Cursor cursor) {
        this.setDate(
                LocalDate.parse(
                    cursor.getString(
                            cursor.getColumnIndexOrThrow(COLUMN_NAME_DATE)
                    )
                )
        );
        this.setLunarLongitude( cursor.getDouble(
                cursor.getColumnIndexOrThrow(COLUMN_NAME_LUNAR_LONGITUDE)
        ));
        this.setLunarVisibility( cursor.getDouble(
                cursor.getColumnIndexOrThrow(COLUMN_NAME_LUNAR_VISIBILITY)
        ));
        this.setLunarRiseSet( this.getRiseSet(cursor, COLUMN_NAME_LUNAR_RISE, COLUMN_NAME_LUNAR_SET) );
        this.setSolarRiseSet( this.getRiseSet(cursor, COLUMN_NAME_SUN_RISE, COLUMN_NAME_SUN_SET) );
    }

    private ZonedRiseSet getRiseSet(Cursor cursor, String columnNameRise, String columnNameSet) {
        LocalDateTime rise = LocalDateTime.parse( cursor.getString(
                // FIXME Better way than orThrow?
                cursor.getColumnIndexOrThrow(columnNameRise)
        ));
        LocalDateTime set = LocalDateTime.parse( cursor.getString(
                cursor.getColumnIndexOrThrow(columnNameSet)
        ));
        return new ZonedRiseSet(rise, set);
    }

    ContentValues toContentValues() {
        ContentValues result = new ContentValues();

        result.put(COLUMN_NAME_DATE, this.getDate().toString());
        result.put(COLUMN_NAME_SUN_RISE, this.getSolarRiseSet().getRise().toString());
        result.put(COLUMN_NAME_SUN_SET, this.getSolarRiseSet().getSet().toString());
        result.put(COLUMN_NAME_LUNAR_RISE, this.getLunarRiseSet().getRise().toString());
        result.put(COLUMN_NAME_LUNAR_SET, this.getLunarRiseSet().getSet().toString());
        result.put(COLUMN_NAME_LUNAR_VISIBILITY, this.getLunarVisibility());
        result.put(COLUMN_NAME_LUNAR_LONGITUDE, this.getLunarLongitude());

        return result;
    }
}
