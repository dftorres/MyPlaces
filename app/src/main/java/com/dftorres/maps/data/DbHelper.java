package com.dftorres.maps.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.dftorres.maps.data.DatabaseContract.PlaceEntry;

public class DbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "myplaces.db";
    private static final int DATABASE_VERSION = 1;

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + PlaceEntry.TABLE_NAME + " (" +
            PlaceEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            PlaceEntry.COLUMN_PLACE_NAME + " TEXT NOT NULL, " +
            PlaceEntry.COLUMN_PLACE_LAT + " TEXT NOT NULL, " +
            PlaceEntry.COLUMN_PLACE_LNG + " TEXT NOT NULL, " +
            PlaceEntry.COLUMN_PLACE_IMAGE + " BLOB, " +
            PlaceEntry.COLUMN_PLACE_ADDRESS + " TEXT)";

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + PlaceEntry.TABLE_NAME);
        onCreate(db);
    }
}
