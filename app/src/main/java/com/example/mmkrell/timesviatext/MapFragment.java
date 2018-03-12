package com.example.mmkrell.timesviatext;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
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

import com.example.mmkrell.timesviatext.databinding.FragmentMapBinding;

import java.io.File;
import java.util.ArrayList;

import org.osmdroid.config.Configuration;
import org.osmdroid.config.IConfigurationProvider;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.tileprovider.modules.SqlTileWriter;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

public class MapFragment extends Fragment implements LocationListener {

    private FragmentMapBinding binding;

    // Accessed from SettingsFragment
    static final BoundingBox chicagoBoundingBox = new BoundingBox(42.07, -87.52, 41.64, -87.89);

    private LocationManager locationManager;
    private MyLocationNewOverlay myLocationOverlay;
    private Thread fixIsAging;

    private boolean paused;

    private SQLiteDatabase database;

    // Accessed from SettingsFragment
    boolean followMeShouldBeEnabled = true;

    private ItemizedIconOverlay<OverlayItem> itemizedIconOverlay;
    private int selectedMarker;

    private float startX;
    private float startY;

    private Bitmap personBitmap;
    private Bitmap directionArrowBitmap;
    private Bitmap grayPersonBitmap;
    private Bitmap grayDirectionArrowBitmap;

    public MapFragment() {
        // Required empty public constructor
    }

    public static MapFragment newInstance() {
        return new MapFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        IConfigurationProvider config = Configuration.getInstance();

        config.setUserAgentValue(BuildConfig.APPLICATION_ID);

        long freeSpace = config.getOsmdroidTileCache().getFreeSpace();
        long cacheSize = new File(config.getOsmdroidTileCache().getAbsolutePath() + File.separator + SqlTileWriter.DATABASE_FILENAME).length();
        config.setTileFileSystemCacheMaxBytes((long)((freeSpace + cacheSize) * 0.95));
        config.setTileFileSystemCacheTrimBytes((long)((freeSpace + cacheSize) * 0.90));

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_map, container, false);
        View v = binding.getRoot();

        personBitmap = BitmapFactory.decodeResource(getResources(),
                org.osmdroid.library.R.drawable.person);
        directionArrowBitmap = BitmapFactory.decodeResource(getResources(),
                org.osmdroid.library.R.drawable.direction_arrow);
        grayPersonBitmap = BitmapFactory.decodeResource(getResources(),
                R.drawable.gray_person_icon);
        grayDirectionArrowBitmap = BitmapFactory.decodeResource(getResources(),
                R.drawable.gray_direction_arrow);

        // Makes the link clickable
        binding.textViewOpenstreetmapCredit.setMovementMethod(LinkMovementMethod.getInstance());

        database = CTAHelper.getDatabaseInstance();

        if (! PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("pref_download_new_tiles", true)) {
            binding.mapView.setUseDataConnection(false);
        }

        binding.mapView.setTileSource(TileSourceFactory.MAPNIK);

        if (PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("pref_scale_tiles_to_dpi", true)) {
            binding.mapView.setTilesScaledToDpi(true);
        }

        binding.mapView.setScrollableAreaLimitDouble(chicagoBoundingBox);
        binding.mapView.setMinZoomLevel(15.0);
        binding.mapView.setMaxZoomLevel(18.0);
        binding.mapView.getController().setZoom(18);

        binding.mapView.setMultiTouchControls(true);
        binding.mapView.setBuiltInZoomControls(false);

        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        Location lastKnownLocation = null;
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }
        // Set the map center to the last known location, if available
        if (lastKnownLocation != null) {
            binding.mapView.getController().setCenter(new GeoPoint(lastKnownLocation));
        } else {
            binding.mapView.getController().setCenter(new GeoPoint(41.945477, -87.690778));
        }

        myLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(getContext()), binding.mapView);
        binding.mapView.getOverlays().add(myLocationOverlay);

        binding.buttonFollowMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (myLocationOverlay.isFollowLocationEnabled()) {
                    setFollowMeState(false);
                } else {
                    setFollowMeState(true);
                }
            }
        });

        binding.mapView.setMapListener(new MapListener() {
            @Override
            public boolean onScroll(ScrollEvent event) {
                // Only disable follow me if the onScroll() was triggered by the user, which would have disabled follow me
                // This check prevents follow me from being disabled immediately after the button is clicked

                // Also, make sure that MapFragment is visible
                // This keeps follow me from being disabled when resuming
                // if the location had changed while it was paused
                if (! myLocationOverlay.isFollowLocationEnabled() && ! paused) {
                    setFollowMeState(false);
                }
                updateMarkers();
                return false;
            }

            @Override
            public boolean onZoom(ZoomEvent event) {
                binding.textViewZoomLevel.setText("Zoom level: " + event.getZoomLevel());
                updateMarkers();
                return false;
            }
        });

        // Used in place of an OnClickListener
        binding.mapView.setOnTouchListener(new View.OnTouchListener() {
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

                selectMarkerAndAddStopFragment(Integer.parseInt(item.getTitle()));

                return false;
            }

            @Override
            public boolean onItemLongPress(int index, OverlayItem item) {
                return false;
            }
        }, getContext());
        binding.mapView.getOverlays().add(itemizedIconOverlay);

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        }

        // This check is needed because if this stuff is enabled while GPS is disabled, it won't appear
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            myLocationOverlay.enableMyLocation();
            if (followMeShouldBeEnabled) {
                setFollowMeState(true);
            }

            // If the thread is null, no Location has been received yet.
            // If this thread isn't running, then it's been over ten seconds since the last fix.
            if (fixIsAging == null || ! fixIsAging.isAlive()) {
                binding.viewWaitingForGpsSignal.setVisibility(View.VISIBLE);
                myLocationOverlay.setPersonIcon(grayPersonBitmap);
            }
        }
        paused = false;
    }

    @Override
    public void onPause() {
        super.onPause();
        paused = true;
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.removeUpdates(this);
        }
        myLocationOverlay.disableFollowLocation();
        myLocationOverlay.disableMyLocation();

        // Remove the GPS messages
        // Their removal is visible, but it's better than removing them in onResume()
        binding.viewWaitingForGpsSignal.setVisibility(View.INVISIBLE);
        binding.viewGpsDisabled.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onLocationChanged(Location location) {
        binding.viewWaitingForGpsSignal.setVisibility(View.INVISIBLE);
        myLocationOverlay.setDirectionArrow(personBitmap, directionArrowBitmap);

        if (fixIsAging != null) {
            fixIsAging.interrupt();
        }

        if (chicagoBoundingBox.contains(new GeoPoint(location))) {
            binding.viewOutsideOfChicago.setVisibility(View.INVISIBLE);
        } else {
            binding.viewOutsideOfChicago.setVisibility(View.VISIBLE);
        }

        fixIsAging = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Sleep for 10 seconds
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    // If this is interrupted, it means we've received a new Location,
                    // so we return to end the thread
                    return;
                }
                // Make person icon gray and show "waiting for GPS signal" view
                myLocationOverlay.setDirectionArrow(grayPersonBitmap, grayDirectionArrowBitmap);
                binding.viewWaitingForGpsSignal.post(new Runnable() {
                    @Override
                    public void run() {
                        // Since it's been ten seconds since the last fix, we don't know if
                        // the person is still outside of Chicago (if they had been before)
                        binding.viewOutsideOfChicago.setVisibility(View.INVISIBLE);
                        binding.viewWaitingForGpsSignal.setVisibility(View.VISIBLE);
                    }
                });
            }
        });

        fixIsAging.start();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {
        // Now we can enable this stuff
        myLocationOverlay.enableMyLocation();
        if (followMeShouldBeEnabled) {
            setFollowMeState(true);
        }

        binding.viewGpsDisabled.setVisibility(View.INVISIBLE);
        binding.viewWaitingForGpsSignal.setVisibility(View.VISIBLE);
    }

    @Override
    public void onProviderDisabled(String provider) {
        // Kill the thread so "waiting for GPS signal" view doesn't pop up while GPS is disabled
        if (fixIsAging != null) {
            fixIsAging.interrupt();
        }

        // The displayed location will probably be wrong really soon, so make the icon gray
        myLocationOverlay.setDirectionArrow(grayPersonBitmap, grayDirectionArrowBitmap);

        // Hide "waiting for GPS signal" view
        binding.viewWaitingForGpsSignal.setVisibility(View.INVISIBLE);

        // Hide "outside of Chicago" view
        binding.viewOutsideOfChicago.setVisibility(View.INVISIBLE);

        // Show "Gps disabled" view
        binding.viewGpsDisabled.setVisibility(View.VISIBLE);
        // If GPS is disabled when requestLocationUpdates() is called in onResume(), onProviderDisabled() will be called
        // That means that viewGpsDisabled.setVisibility(View.VISIBLE) doesn't need to also be called in onResume()
    }

    private void updateMarkers() {
        itemizedIconOverlay.removeAllItems();

        double north = binding.mapView.getBoundingBox().getLatNorth() + binding.mapView.getBoundingBox().getLatitudeSpan() / 10;
        double south = binding.mapView.getBoundingBox().getLatSouth() - binding.mapView.getBoundingBox().getLatitudeSpan() / 10;
        double east = binding.mapView.getBoundingBox().getLonEast() + binding.mapView.getBoundingBox().getLongitudeSpan() / 10;
        double west = binding.mapView.getBoundingBox().getLonWest() - binding.mapView.getBoundingBox().getLongitudeSpan() / 10;

        String[] selectionArgs = {String.valueOf(north), String.valueOf(south), String.valueOf(east), String.valueOf(west)};

        Cursor query = database.rawQuery("SELECT stop_id, stop_lat, stop_lon FROM stops " +
                "WHERE stop_lat < ? AND stop_lat > ? AND stop_lon < ? AND stop_lon > ?",
                selectionArgs);

        while (query.moveToNext()) {
            OverlayItem marker = new OverlayItem(query.getString(0), null, new GeoPoint(query.getDouble(1), query.getDouble(2)));
            // If the stop code matches the selected stop code, give this marker a different drawable to reflect that
            if (marker.getTitle().equals(String.valueOf(selectedMarker))) {
                marker.setMarker(ContextCompat.getDrawable(getContext(), R.drawable.marker_selected));
            }
            itemizedIconOverlay.addItem(marker);
        }

        query.close();
    }

    void setFollowMeState(boolean enabled) {
        if (enabled) {
            binding.buttonFollowMe.setImageResource(org.osmdroid.library.R.drawable.osm_ic_follow_me_on);
            myLocationOverlay.enableFollowLocation();
            followMeShouldBeEnabled = true;
        } else {
            binding.buttonFollowMe.setImageResource(org.osmdroid.library.R.drawable.osm_ic_follow_me);
            myLocationOverlay.disableFollowLocation();
            followMeShouldBeEnabled = false;
        }
    }

    void selectMarkerAndAddStopFragment(int stopId) {
        Cursor query = database.rawQuery("SELECT stop_name, stop_dir FROM stops " +
                "WHERE stop_id = ?", new String[] {String.valueOf(stopId)});
        query.moveToNext();

        // Set selectedMarker to the stop code of the marker that's been tapped
        selectedMarker = stopId;
        // Update markers to make this marker's drawable update
        updateMarkers();

        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
        // Set custom animations for both normal and "pop" (e.g. popBackStack()) fragment additions and removals
        fragmentTransaction.setCustomAnimations(R.anim.slide_in_up, R.anim.slide_out_down, R.anim.slide_in_up, R.anim.slide_out_down);
        fragmentTransaction.add(R.id.activity_map, StopFragment.newInstance(stopId, query.getString(0), query.getString(1)));
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();

        query.close();
    }

    // Returns true if StopFragment was removed
    boolean deselectMarkerAndRemoveStopFragment(boolean animate) {
        if (! animate) {
            StopFragment.enableAnimations = false;
        }

        // Remove StopFragment
        boolean removed = getActivity().getSupportFragmentManager().popBackStackImmediate();

        StopFragment.enableAnimations = true;

        if (! removed) {
            return false;
        }

        // Now that the StopFragment has been removed, set selectedMarker to 0
        selectedMarker = 0;
        // Update markers to reset the icon for the previously-selected marker
        updateMarkers();

        return true;
    }

    void animateToMarker(int stopId) {
        Cursor query = database.rawQuery("SELECT stop_lat, stop_lon FROM stops " +
                "WHERE stop_id = ?", new String[] {String.valueOf(stopId)});
        query.moveToNext();

        binding.mapView.getController().animateTo(new GeoPoint(query.getDouble(0), query.getDouble(1)));

        query.close();
    }

    // Used in SettingsFragment
    MapView getMapView() {
        return binding.mapView;
    }
}
