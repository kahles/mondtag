package de.kah2.mondtag.datamanagement;

import android.content.ContentValues;
import android.database.Cursor;
import android.provider.BaseColumns;

import java.time.Instant;
import java.time.LocalDate;

import de.kah2.zodiac.libZodiac4A.Day;
import de.kah2.zodiac.libZodiac4A.DayStorableDataSetPojo;
import de.kah2.zodiac.libZodiac4A.planetary.RiseSet;

/**
 * This class is used to map libZodiac-data to database-entries.
 * Created by kahles on 04.10.16.
 */

class DatabaseDayEntry extends DayStorableDataSetPojo implements BaseColumns {

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
        setDate(
                LocalDate.parse(
                    cursor.getString(
                            cursor.getColumnIndexOrThrow(COLUMN_NAME_DATE)
                    )
                )
        );
        setLunarLongitude( cursor.getDouble(
                cursor.getColumnIndexOrThrow(COLUMN_NAME_LUNAR_LONGITUDE)
        ));
        setLunarVisibility( cursor.getDouble(
                cursor.getColumnIndexOrThrow(COLUMN_NAME_LUNAR_VISIBILITY)
        ));
        setLunarRiseSet( this.getRiseSet(cursor, COLUMN_NAME_LUNAR_RISE, COLUMN_NAME_LUNAR_SET) );
        setSolarRiseSet( this.getRiseSet(cursor, COLUMN_NAME_SUN_RISE, COLUMN_NAME_SUN_SET) );
    }

    private RiseSet getRiseSet(Cursor cursor, String columnNameRise, String columnNameSet) {
        Instant rise = Instant.parse( cursor.getString(
                // FIXME Better way than orThrow?
                cursor.getColumnIndexOrThrow(columnNameRise)
        ));
        Instant set = Instant.parse( cursor.getString(
                cursor.getColumnIndexOrThrow(columnNameSet)
        ));
        return new RiseSet(rise, set);
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
