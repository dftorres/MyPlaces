package com.dftorres.maps.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.dftorres.maps.data.DatabaseContract.PlaceEntry;

public class PlacesProvider extends ContentProvider {
    private static final String TAG = PlacesProvider.class.getSimpleName();
    private static final int PLACES = 1;
    private static final int PLACE_ID = 2;
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(DatabaseContract.CONTENT_AUTHORITY, DatabaseContract.PATH_PLACES, PLACES);
        sUriMatcher.addURI(DatabaseContract.CONTENT_AUTHORITY, DatabaseContract.PATH_PLACES + "/#", PLACE_ID);
    }

    private DbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = new DbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor cursor;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PLACES:
                cursor = db.query(PlaceEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case PLACE_ID:
                selection = PlaceEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = db.query(PlaceEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI: " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        if (match == PLACES) return insertPlace(uri, contentValues);
        else throw new IllegalArgumentException("Insertion is not supported for URI: " + uri);
    }

    @Override
    public int delete(Uri uri, String s, String[] strings) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        return 0;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case PLACES:
                return PlaceEntry.CONTENT_LIST_TYPE;
            case PLACE_ID:
                return PlaceEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    private Uri insertPlace(Uri uri, ContentValues values) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        String name = values.getAsString(PlaceEntry.COLUMN_PLACE_NAME);
        if (TextUtils.isEmpty(name)) {
            throw new IllegalArgumentException("Cannot resolve null name");
        }

        String lat = values.getAsString(PlaceEntry.COLUMN_PLACE_LAT);
        if (TextUtils.isEmpty(lat)) {
            throw new IllegalArgumentException("Cannot resolve null lattitude.");
        }

        String lng = values.getAsString(PlaceEntry.COLUMN_PLACE_LNG);
        if (TextUtils.isEmpty(lng)) {
            throw new IllegalArgumentException("Cannot resolve null longitude.");
        }

        String address = values.getAsString(PlaceEntry.COLUMN_PLACE_ADDRESS);
        if (TextUtils.isEmpty(address)) {
            throw new IllegalArgumentException("Cannot resolve null address.");
        }

        byte[] image = values.getAsByteArray(PlaceEntry.COLUMN_PLACE_IMAGE);
        if (image == null) {
            // TODO: handle
        }

        long id = db.insert(PlaceEntry.TABLE_NAME, null, values);
        if (id == -1) {
            Log.e(TAG, "insertPlace: Failed to insert place row for id: " + uri);
            return null;
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, id);
    }
}
