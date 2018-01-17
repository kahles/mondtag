package de.kah2.mondtag.datamanagement;

/**
 * Defines the database schema.
 *
 * Created by kahles on 30.09.16.
 */

class DatabaseContract {

    private final static String TEXT_TYPE = "TEXT";
    private final static String INT_TYPE = "INTEGER";
    private final static String FLOAT_TYPE = "REAL";
    private final static String COMMA_SEP = ",";

    final static String SQL_CREATE_TABLES = "CREATE TABLE " + DatabaseDayEntry.TABLE_NAME + " (" +
            DatabaseDayEntry._ID + " " + INT_TYPE + " PRIMARY KEY" + COMMA_SEP +
            DatabaseDayEntry.COLUMN_NAME_DATE + " " + TEXT_TYPE + COMMA_SEP +
            DatabaseDayEntry.COLUMN_NAME_SUN_RISE + " " + TEXT_TYPE + COMMA_SEP +
            DatabaseDayEntry.COLUMN_NAME_SUN_SET + " " + TEXT_TYPE + COMMA_SEP +
            DatabaseDayEntry.COLUMN_NAME_LUNAR_RISE + " " + TEXT_TYPE + COMMA_SEP +
            DatabaseDayEntry.COLUMN_NAME_LUNAR_SET + " " + TEXT_TYPE + COMMA_SEP +
            DatabaseDayEntry.COLUMN_NAME_LUNAR_VISIBILITY + " " + FLOAT_TYPE + COMMA_SEP +
            DatabaseDayEntry.COLUMN_NAME_LUNAR_LONGITUDE + " " + FLOAT_TYPE +
            ");";

    static final String SQL_DELETE_TABLES = "DROP TABLE IF EXISTS " + DatabaseDayEntry.TABLE_NAME;

    private DatabaseContract() {}


}
