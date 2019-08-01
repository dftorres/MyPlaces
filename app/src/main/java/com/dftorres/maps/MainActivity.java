package com.dftorres.maps;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import com.dftorres.maps.data.DatabaseContract.PlaceEntry;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PointOfInterest;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.FetchPhotoResponse;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.navigation.NavigationView;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.mancj.materialsearchbar.adapter.SuggestionsAdapter;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements
        OnMapReadyCallback,
        LoaderManager.LoaderCallbacks<Cursor>,
        NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final float DEFAULT_ZOOM = 15;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private final LatLng mDefaultLocation = new LatLng(-33.8523341, 151.2106085);
    MapsFragment mMapsFragment;
    private GoogleMap mMap;
    private boolean mLocationPermissionGranted;
    private Location mLastKnownLocation;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private PlacesClient placesClient;
    private MaterialSearchBar materialSearchBar;
    private List<AutocompletePrediction> predictionList;
    private DrawerLayout mDrawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDrawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_home);

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        Places.initialize(getApplicationContext(), getString(R.string.google_maps_key));
        placesClient = Places.createClient(this);
        materialSearchBar = findViewById(R.id.searchBar);
        final AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();
        if (isServicesUpdated()) {
            mMapsFragment = MapsFragment.newInstance();
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.map_container, mMapsFragment)
                    .commit();
            mMapsFragment.getMapAsync(this);
            getSupportLoaderManager().initLoader(0, null, this);
        }


        materialSearchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {

            }

            @Override
            public void onSearchConfirmed(CharSequence text) {
                startSearch(text.toString(), true, null, true);
            }

            @Override
            public void onButtonClicked(int buttonCode) {
                if (buttonCode == MaterialSearchBar.BUTTON_BACK) {
                    materialSearchBar.disableSearch();
                    materialSearchBar.clearSuggestions();
                } else if (buttonCode == MaterialSearchBar.BUTTON_NAVIGATION) {
                    mDrawer.openDrawer(GravityCompat.START);
                }
            }
        });
        materialSearchBar.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                        .setTypeFilter(TypeFilter.ESTABLISHMENT)
                        .setCountry("ec")
                        .setSessionToken(token)
                        .setQuery(s.toString())
                        .build();

                placesClient.findAutocompletePredictions(request).addOnCompleteListener(new OnCompleteListener<FindAutocompletePredictionsResponse>() {
                    @Override
                    public void onComplete(@NonNull Task<FindAutocompletePredictionsResponse> task) {
                        if (task.isSuccessful()) {
                            FindAutocompletePredictionsResponse response = task.getResult();
                            if (response != null) {
                                predictionList = response.getAutocompletePredictions();
                                List<String> suggestionsList = new ArrayList<>();

                                for (int i = 0; i < predictionList.size(); i++) {
                                    AutocompletePrediction prediction = predictionList.get(i);
                                    suggestionsList.add(prediction.getPrimaryText(null).toString());
                                }

                                materialSearchBar.updateLastSuggestions(suggestionsList);
                                materialSearchBar.showSuggestionsList();
                            }
                        } else {
                            Log.i(TAG, "onComplete: Prediction fetching task unsuccessful");
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        ApiException exception = (ApiException) e;
                        Log.e(TAG, "onFailure: Place not found" + exception.getMessage());
                    }
                });
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        materialSearchBar.setSuggestionsClickListener(new SuggestionsAdapter.OnItemViewClickListener() {
            @Override
            public void OnItemClickListener(int position, View v) {
                if (position >= predictionList.size()) {
                    return;
                }
                AutocompletePrediction selectedPrediction = predictionList.get(position);
                String suggestion = materialSearchBar.getLastSuggestions().get(position).toString();
                materialSearchBar.setText(suggestion);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        materialSearchBar.clearSuggestions();
                    }
                }, 1000);
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (imm != null)
                    imm.hideSoftInputFromWindow(materialSearchBar.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
                final String placeId = selectedPrediction.getPlaceId();
                List<Place.Field> placeFields = Collections.singletonList(Place.Field.LAT_LNG);
                FetchPlaceRequest request = FetchPlaceRequest.builder(placeId, placeFields).build();

                placesClient.fetchPlace(request).addOnSuccessListener(new OnSuccessListener<FetchPlaceResponse>() {
                    @Override
                    public void onSuccess(FetchPlaceResponse fetchPlaceResponse) {
                        Place place = fetchPlaceResponse.getPlace();
                        Log.i(TAG, "Place found: " + place.getName());
                        LatLng latLngOfPlace = place.getLatLng();
                        if (latLngOfPlace != null) {
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngOfPlace, DEFAULT_ZOOM));
                        }
                        materialSearchBar.clearSuggestions();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (e instanceof ApiException) {
                            ApiException apiException = (ApiException) e;
                            apiException.printStackTrace();
                            int statusCode = apiException.getStatusCode();
                            Log.i(TAG, "place not found: " + e.getMessage());
                            Log.i(TAG, "status code: " + statusCode);
                        }
                    }
                });
            }

            @Override
            public void OnItemDeleteListener(int position, View v) {

            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        getLocationPermission();
        updateLocationUI();
        getDeviceLocation();
        setPoiClickListener(mMap);
        setMapLongClick(mMap);
    }

    private void setPoiClickListener(final GoogleMap map) {
        map.setOnPoiClickListener(new GoogleMap.OnPoiClickListener() {
            @Override
            public void onPoiClick(final PointOfInterest poi) {
                final Marker poiMarker = map.addMarker(new MarkerOptions().position(poi.latLng).title(poi.name));
                poiMarker.showInfoWindow();
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(poiMarker.getPosition(), 16), 800, null);

                final ContentValues values = new ContentValues();
                values.put(PlaceEntry.COLUMN_PLACE_NAME, poi.name.replace("\n", " "));
                values.put(PlaceEntry.COLUMN_PLACE_LAT, poi.latLng.latitude);
                values.put(PlaceEntry.COLUMN_PLACE_LNG, poi.latLng.longitude);

                final String placeId = poi.placeId;
                List<Place.Field> placeFields = Arrays.asList(Place.Field.ADDRESS, Place.Field.PHOTO_METADATAS);
                FetchPlaceRequest request = FetchPlaceRequest.builder(placeId, placeFields).build();
                placesClient.fetchPlace(request).addOnSuccessListener(new OnSuccessListener<FetchPlaceResponse>() {
                    @Override
                    public void onSuccess(FetchPlaceResponse fetchPlaceResponse) {
                        Place place = fetchPlaceResponse.getPlace();
                        String[] address = place.getAddress().split(",");
                        values.put(PlaceEntry.COLUMN_PLACE_ADDRESS, address[0]);

                        placesClient
                                .fetchPhoto(FetchPhotoRequest.builder(place.getPhotoMetadatas().get(0)).build())
                                .addOnSuccessListener(new OnSuccessListener<FetchPhotoResponse>() {
                                    @Override
                                    public void onSuccess(FetchPhotoResponse fetchPhotoResponse) {
                                        Bitmap bitmap = fetchPhotoResponse.getBitmap();
                                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                                        bitmap.compress(Bitmap.CompressFormat.JPEG, 25, bos);
                                        byte[] img = bos.toByteArray();
                                        values.put(PlaceEntry.COLUMN_PLACE_IMAGE, img);
                                        if (values.get(PlaceEntry.COLUMN_PLACE_IMAGE) == null) {
                                            values.put(PlaceEntry.COLUMN_PLACE_IMAGE, "");
                                            Toast.makeText(MainActivity.this, "No available image", Toast.LENGTH_SHORT).show();
                                        }

                                        LocationInsertTask task = new LocationInsertTask();
                                        task.execute(values);
                                        Toast.makeText(MainActivity.this, "Place saved correctly", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                });

            }
        });
    }

    private void setMapLongClick(final GoogleMap map) {
        map.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                String snippet = String.format(Locale.getDefault(), "Lat: %1$.5f, Long: %2$.5f", latLng.latitude, latLng.longitude);

                map.addMarker(new MarkerOptions()
                        .position(latLng)
                        .snippet(snippet));
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16), 800, null);
            }
        });
    }

    private void updateLocationUI() {
        if (mMap == null) return;
        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
                getDeviceLocation();
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mLastKnownLocation = null;
            }
        } catch (SecurityException e) {
            Log.e(TAG, "updateLocationUI: %s", e);
        }
    }

    /**
     * Get the best and most recent location of the device, which may be null in rare
     * cases when a location is not available.
     */
    private void getDeviceLocation() {
        try {
            if (mLocationPermissionGranted) {
                Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            mLastKnownLocation = task.getResult();
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    /**
     * Prompts the user for permission to use the device's current location.
     * The result of the permission request is handled by onRequestPermissionsResult method.
     */
    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};
            ActivityCompat.requestPermissions(this, permissions, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    /**
     * Handles the result of the request for location permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionGranted = true;
            } else {
                Toast.makeText(this, "You must enable the location permission", Toast.LENGTH_SHORT).show();
            }
        }
        updateLocationUI();
    }

    /**
     * Validates if the user's Google Play Services is up to date.
     *
     * @return true if it's updated.
     */
    private boolean isServicesUpdated() {
        Log.d(TAG, "isServicesUpdated: Checking Google Play Services availability.");
        int availability = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainActivity.this);

        if (availability == ConnectionResult.SUCCESS) {
            // Google Play Services is up-to-date and user can make map requests
            Log.d(TAG, "isServicesUpdated: Google Play Services is working.");
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(availability)) {
            // An error occurred but it can be fixed
            Log.d(TAG, "isServicesUpdated: An error occurred but it can be fixed.");
            // Google provides a solution to fix that problem
            GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this, availability, 9001).show();
        } else {
            Toast.makeText(this, "You need to update the Google Play Services APK", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        Uri uri = PlaceEntry.CONTENT_URI;
        String[] projection = {
                PlaceEntry._ID,
                PlaceEntry.COLUMN_PLACE_NAME,
                PlaceEntry.COLUMN_PLACE_LAT,
                PlaceEntry.COLUMN_PLACE_LNG
        };
        return new CursorLoader(this, uri, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        int count = cursor.getCount();
        double lat;
        double lng;

        cursor.moveToFirst();
        for (int i = 0; i < count; i++) {
            lat = cursor.getDouble(cursor.getColumnIndex(PlaceEntry.COLUMN_PLACE_LAT));
            lng = cursor.getDouble(cursor.getColumnIndex(PlaceEntry.COLUMN_PLACE_LNG));

            LatLng location = new LatLng(lat, lng);
            mMap.addMarker(new MarkerOptions().position(location));
            cursor.moveToNext();
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int id = menuItem.getItemId();
        switch (id) {
            case R.id.nav_home:
                mMapsFragment = MapsFragment.newInstance();
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.map_container, mMapsFragment)
                        .commit();
                mMapsFragment.getMapAsync(this);
                getSupportLoaderManager().initLoader(0, null, MainActivity.this);
                materialSearchBar.setVisibility(View.VISIBLE);
                mDrawer.closeDrawer(GravityCompat.START);
                break;
            case R.id.nav_my_places:
                getSupportFragmentManager()
                        .beginTransaction()
                        .add(R.id.map_container, new SavedPlacesFragment())
                        .commit();
                materialSearchBar.setVisibility(View.GONE);
                mDrawer.closeDrawer(GravityCompat.START);
                break;
            case R.id.nav_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                mDrawer.closeDrawer(GravityCompat.START);
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private class LocationInsertTask extends AsyncTask<ContentValues, Void, Void> {
        @Override
        protected Void doInBackground(ContentValues... contentValues) {
            // Setting up values to insert the clicked location into SQLite database
            getContentResolver().insert(PlaceEntry.CONTENT_URI, contentValues[0]);
            return null;
        }
    }
}