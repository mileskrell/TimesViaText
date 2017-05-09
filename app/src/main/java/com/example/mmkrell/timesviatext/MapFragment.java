package com.example.mmkrell.timesviatext;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;

import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

public class MapFragment extends Fragment implements LocationListener {

    private static MapView mapView;
    private static final BoundingBox chicagoBoundingBox = new BoundingBox(42.07, -87.52, 41.64, -87.89);

    private Location currentLocation;
    private LocationManager locationManager;
    private MyLocationNewOverlay myLocationOverlay;

    private View viewWaitingForGpsSignal;
    private View viewGpsDisabled;

    private TextView textViewZoomLevel;
    private ImageButton buttonMyLocation;
    private ImageButton buttonFollowMe;
    private TextView textViewOpenStreetMapCredit;

    private SQLiteDatabase database;

    private boolean followMeShouldBeEnabled = true;

    private ItemizedIconOverlay<OverlayItem> itemizedIconOverlay;
    private String selectedMarker;

    private float startX;
    private float startY;

    public MapFragment() {
        // Required empty public constructor
    }

    public static MapFragment newInstance() {
        return new MapFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_map, container, false);

        textViewZoomLevel = (TextView) v.findViewById(R.id.text_view_zoom_level);
        viewWaitingForGpsSignal = v.findViewById(R.id.view_waiting_for_gps_signal);
        viewGpsDisabled = v.findViewById(R.id.view_gps_disabled);

        buttonMyLocation = (ImageButton) v.findViewById(R.id.button_my_location);
        buttonFollowMe = (ImageButton) v.findViewById(R.id.button_follow_me);

        textViewOpenStreetMapCredit = (TextView) v.findViewById(R.id.text_view_openstreetmap_credit);
        // Makes the link clickable
        textViewOpenStreetMapCredit.setMovementMethod(LinkMovementMethod.getInstance());

        database = CTAHelper.getDatabaseInstance();

        Configuration.getInstance().setUserAgentValue(BuildConfig.APPLICATION_ID);
        mapView = (MapView) v.findViewById(R.id.map_view);

        if (PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("pref_offline_mode", false))
            mapView.setUseDataConnection(false);

        mapView.setTileSource(TileSourceFactory.MAPNIK);

        if (PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("pref_tiles_scaled_to_dpi", true))
            mapView.setTilesScaledToDpi(true);

        mapView.setScrollableAreaLimitDouble(chicagoBoundingBox);
        mapView.setMinZoomLevel(15);
        mapView.setMaxZoomLevel(18);
        mapView.getController().setZoom(18);

        mapView.setMultiTouchControls(true);

        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        Location lastKnownLocation = null;
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        // Set the map center to the last known location, if available
        if (lastKnownLocation != null)
            mapView.getController().setCenter(new GeoPoint(lastKnownLocation));
        else
            mapView.getController().setCenter(new GeoPoint(41.945477, -87.690778));

        myLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(getContext()), mapView);
        mapView.getOverlays().add(myLocationOverlay);

        buttonMyLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentLocation != null) {
                    mapView.getController().animateTo(new GeoPoint(currentLocation));
                }
            }
        });

        buttonFollowMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (myLocationOverlay.isFollowLocationEnabled()) {
                    disableFollowMe();
                } else {
                    enableFollowMe();
                }
            }
        });

        mapView.setMapListener(new MapListener() {
            @Override
            public boolean onScroll(ScrollEvent event) {
                // Only call disableFollowMe() if the onScroll() was triggered by the user, which would have disabled follow me
                // This check prevents disableFollowMe() from being called immediately after the button is clicked
                if (! myLocationOverlay.isFollowLocationEnabled())
                    disableFollowMe();
                updateMarkers();
                return false;
            }

            @Override
            public boolean onZoom(ZoomEvent event) {
                textViewZoomLevel.setText("Zoom level: " + event.getZoomLevel());
                updateMarkers();
                return false;
            }
        });

        // Used in place of an OnClickListener
        mapView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = event.getX();
                        startY = event.getY();
                        break;
                    case MotionEvent.ACTION_UP:
                        float endX = event.getX();
                        float endY = event.getY();
                        if (Math.abs(startX - endX) < 10 && Math.abs(startY - endY) < 10) {
                            // Deselect marker and remove StopFragment when MapView is clicked
                            deselectMarkerAndRemoveStopFragment(true);
                        }
                        break;
                }
                return false;
            }
        });

        itemizedIconOverlay = new ItemizedIconOverlay<>(new ArrayList<OverlayItem>(), new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
            @Override
            public boolean onItemSingleTapUp(int index, OverlayItem item) {
                // If multiple markers are clicked, this block is run multiple times after the OnTouchListener
                // That's why this line is needed both here and in the OnTouchListener
                getActivity().getSupportFragmentManager().popBackStackImmediate();

                selectMarkerAndAddStopFragment(item.getTitle());

                return false;
            }

            @Override
            public boolean onItemLongPress(int index, OverlayItem item) {
                return false;
            }
        }, getContext());
        mapView.getOverlays().add(itemizedIconOverlay);

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);

        // This check is needed because if this stuff is enabled while GPS is disabled, it won't appear
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            myLocationOverlay.enableMyLocation();
            if (followMeShouldBeEnabled)
                enableFollowMe();
            if (shouldShowWaitingForGpsSignalView())
                viewWaitingForGpsSignal.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            locationManager.removeUpdates(this);
        myLocationOverlay.disableFollowLocation();
        myLocationOverlay.disableMyLocation();

        // Remove the GPS messages
        // Their removal is visible, but it's better than removing them in onResume()
        viewWaitingForGpsSignal.setVisibility(View.INVISIBLE);
        viewGpsDisabled.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onLocationChanged(Location location) {
        currentLocation = location;
        viewWaitingForGpsSignal.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {
        // Now we can enable this stuff
        myLocationOverlay.enableMyLocation();
        if (followMeShouldBeEnabled)
            enableFollowMe();

        viewGpsDisabled.setVisibility(View.INVISIBLE);
        if (shouldShowWaitingForGpsSignalView())
            viewWaitingForGpsSignal.setVisibility(View.VISIBLE);

    }

    @Override
    public void onProviderDisabled(String provider) {
        // Hide "waiting for GPS signal" view
        viewWaitingForGpsSignal.setVisibility(View.INVISIBLE);
        // If GPS is disabled, prompt the user to enable it
        viewGpsDisabled.setVisibility(View.VISIBLE);
        // If GPS is disabled when requestLocationUpdates() is called in onResume(), onProviderDisabled() will be called
        // That means that viewGpsDisabled.setVisibility(View.VISIBLE) doesn't need to also be called in onResume()
    }

    private void updateMarkers() {
        itemizedIconOverlay.removeAllItems();

        double north = mapView.getBoundingBox().getLatNorth() + mapView.getBoundingBox().getLatitudeSpan() / 10;
        double south = mapView.getBoundingBox().getLatSouth() - mapView.getBoundingBox().getLatitudeSpan() / 10;
        double east = mapView.getBoundingBox().getLonEast() + mapView.getBoundingBox().getLongitudeSpan() / 10;
        double west = mapView.getBoundingBox().getLonWest() - mapView.getBoundingBox().getLongitudeSpan() / 10;

        String[] selectionArgs = {String.valueOf(north), String.valueOf(south), String.valueOf(east), String.valueOf(west)};

        Cursor query = database.rawQuery("SELECT stop_id, stop_lat, stop_lon FROM stops " +
                "WHERE stop_lat < ? AND stop_lat > ? AND stop_lon < ? AND stop_lon > ?",
                selectionArgs);

        while (query.moveToNext()) {
            OverlayItem marker = new OverlayItem(query.getString(0), null, new GeoPoint(query.getDouble(1), query.getDouble(2)));
            // If the stop code matches the selected stop code, give this marker a different drawable to reflect that
            if (marker.getTitle().equals(selectedMarker))
                marker.setMarker(ContextCompat.getDrawable(getContext(), R.drawable.marker_selected));
            itemizedIconOverlay.addItem(marker);
        }

        query.close();
    }

    private boolean shouldShowWaitingForGpsSignalView() {
        // If currentLocation is null, we should obviously show the view
        if (currentLocation == null)
            return true;

        // If it's been more than a minute since the last GPS fix, we should show the view
        long nanosecondsSinceLastFix = SystemClock.elapsedRealtimeNanos() - currentLocation.getElapsedRealtimeNanos();
        int millisecondsSinceLastFix = (int) (nanosecondsSinceLastFix / 1000000);
        return millisecondsSinceLastFix > 60000;
    }

    private void enableFollowMe() {
        buttonFollowMe.setImageResource(R.drawable.ic_follow_me_on);
        myLocationOverlay.enableFollowLocation();
        followMeShouldBeEnabled = true;
    }

    void disableFollowMe() {
        buttonFollowMe.setImageResource(R.drawable.ic_follow_me);
        myLocationOverlay.disableFollowLocation();
        followMeShouldBeEnabled = false;
    }

    void selectMarkerAndAddStopFragment(String stopCode) {
        Cursor query = database.rawQuery("SELECT stop_name, stop_dir FROM stops WHERE stop_id = ?",
                new String[] {stopCode});
        query.moveToNext();

        // Set selectedMarker to the stop code of the marker that's been tapped
        selectedMarker = stopCode;
        // Update markers to make this marker's drawable update
        updateMarkers();

        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
        // Set custom animations for both normal and "pop" (e.g. popBackStack()) fragment additions and removals
        fragmentTransaction.setCustomAnimations(R.anim.slide_in_up, R.anim.slide_out_down, R.anim.slide_in_up, R.anim.slide_out_down);
        fragmentTransaction.add(R.id.activity_map, StopFragment.newInstance(Integer.parseInt(stopCode), query.getString(0), query.getString(1)));
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();

        query.close();
    }

    // Returns true if StopFragment was removed
    boolean deselectMarkerAndRemoveStopFragment(boolean animate) {
        if (! animate)
            StopFragment.enableAnimations = false;

        // Remove StopFragment
        boolean removed = getActivity().getSupportFragmentManager().popBackStackImmediate();

        StopFragment.enableAnimations = true;

        if (! removed)
            return false;

        // Now that the StopFragment has been removed, set selectedMarker to 0
        selectedMarker = "0";
        // Update markers to reset the icon for the previously-selected marker
        updateMarkers();

        return true;
    }

    void animateToMarker(String stopCode) {
        Cursor query = database.rawQuery("SELECT stop_lat, stop_lon FROM stops WHERE stop_id = ?",
                new String[] {stopCode});
        query.moveToNext();

        mapView.getController().animateTo(new GeoPoint(Double.valueOf(query.getString(0)), Double.valueOf(query.getString(1))));

        query.close();
    }

    static MapView getMapView() {
        return mapView;
    }
}