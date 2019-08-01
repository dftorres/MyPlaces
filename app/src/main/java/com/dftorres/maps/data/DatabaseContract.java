package com.dftorres.maps.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public final class DatabaseContract {

    // Content Authority para el ContentProvider
    public static final String CONTENT_AUTHORITY = "com.dftorres.maps.provider";
    // Base content URI
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    // content://com.dftorres.maps/places
    public static final String PATH_PLACES = "places";

    // Constructor vacío
    public DatabaseContract() {
    }

    public static abstract class PlaceEntry implements BaseColumns {
        // El URI para acceder al contenido de la tabla places en el ContentProvider
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_PLACES);

        // El MIME type del CONTENT_URI para una lista de lugares
        public static final String CONTENT_LIST_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + PATH_PLACES;
        // El MIME type del CONTENT_URI para un solo lugar
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + PATH_PLACES;

        public static final String _ID = BaseColumns._ID;               // Tipo: INTEGER
        public static final String COLUMN_PLACE_NAME = "name";          // Tipo: TEXT
        public static final String COLUMN_PLACE_LAT = "lat";            // Tipo: TEXT
        public static final String COLUMN_PLACE_LNG = "lng";            // Tipo: TEXT
        public static final String COLUMN_PLACE_ADDRESS = "address";    // Tipo: TEXT
        public static final String COLUMN_PLACE_IMAGE = "image";        // Tipo: BLOB
        public static final String TABLE_NAME = "places";               // Nombre de la tabla
    }
}
