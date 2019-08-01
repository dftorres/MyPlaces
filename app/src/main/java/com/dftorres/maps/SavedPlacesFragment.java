package com.dftorres.maps;


import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.appcompat.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dftorres.maps.data.DatabaseContract.PlaceEntry;
import com.dftorres.maps.data.DbHelper;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class SavedPlacesFragment extends Fragment {

    public SavedPlacesFragment() { } // Constructor vacío

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_saved_places, container, false);

        ArrayList<SavedPlace> savedPlaces = new ArrayList<>();
        RecyclerView recyclerView = rootView.findViewById(R.id.recycler_view);
        Toolbar toolbar = rootView.findViewById(R.id.toolbar);
        DbHelper mDbHelper = new DbHelper(getContext());
        final DrawerLayout mDrawer = getActivity().findViewById(R.id.drawer_layout);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            toolbar.setNavigationIcon(R.drawable.ic_nav_icon);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mDrawer.openDrawer(GravityCompat.START);
                }
            });
        }
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        String[] columns = {
                PlaceEntry.COLUMN_PLACE_NAME,
                PlaceEntry.COLUMN_PLACE_LAT,
                PlaceEntry.COLUMN_PLACE_LNG,
                PlaceEntry.COLUMN_PLACE_ADDRESS,
                PlaceEntry.COLUMN_PLACE_IMAGE};
        String orderBy = PlaceEntry._ID + " DESC";

        Cursor cursor = db.query(
                PlaceEntry.TABLE_NAME,
                columns,
                null,
                null,
                null,
                null,
                orderBy);

        if (cursor.getCount() > 0) {
            int placeNameColumnIndex = cursor.getColumnIndex(PlaceEntry.COLUMN_PLACE_NAME);
            int latColumnIndex = cursor.getColumnIndex(PlaceEntry.COLUMN_PLACE_LAT);
            int lngColumnIndex = cursor.getColumnIndex(PlaceEntry.COLUMN_PLACE_LNG);
            int addressColumnIndex = cursor.getColumnIndex(PlaceEntry.COLUMN_PLACE_ADDRESS);
            int imageColumnIndex = cursor.getColumnIndex(PlaceEntry.COLUMN_PLACE_IMAGE);

            while (cursor.moveToNext()) {
                String placeName = cursor.getString(placeNameColumnIndex);
                String placeLat = cursor.getString(latColumnIndex);
                String placeLng = cursor.getString(lngColumnIndex);
                String placeAddress = cursor.getString(addressColumnIndex);
                byte[] placeImage = cursor.getBlob(imageColumnIndex);
                savedPlaces.add(new SavedPlace(placeName, placeLat, placeLng, placeAddress, placeImage));
            }
        }

        cursor.close();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(rootView.getContext());
        recyclerView.setLayoutManager(linearLayoutManager);

        SavedPlaceAdapter adapter = new SavedPlaceAdapter(rootView.getContext(), savedPlaces);
        recyclerView.setAdapter(adapter);
        cursor.close();
        return rootView;
    }
}