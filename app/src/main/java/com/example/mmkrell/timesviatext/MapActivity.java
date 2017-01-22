package com.example.mmkrell.timesviatext;

import android.Manifest;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
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
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

public class MapActivity extends AppCompatActivity implements LocationListener {

    MapView mapView;

    Location currentLocation;
    LocationManager locationManager;
    MyLocationNewOverlay myLocationOverlay;
    ProgressDialog locationProgressDialog;

    TextView textViewZoomLevel;
    ImageButton buttonMyLocation;
    ImageButton buttonFollowMe;
    TextView textViewOpenStreetMapCredit;

    SQLiteDatabase database;
    String[] projection;
    String selection;

    boolean followMeShouldBeEnabled;

    FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        textViewZoomLevel = (TextView) findViewById(R.id.textViewZoomLevel);

        buttonMyLocation = (ImageButton) findViewById(R.id.buttonMyLocation);
        buttonFollowMe = (ImageButton) findViewById(R.id.buttonFollowMe);

        textViewOpenStreetMapCredit = (TextView) findViewById(R.id.textViewOpenStreetMapCredit);
        textViewOpenStreetMapCredit.setMovementMethod(LinkMovementMethod.getInstance()); // Makes the link clickable

        Configuration.getInstance().setUserAgentValue(BuildConfig.APPLICATION_ID);
        mapView = (MapView) findViewById(R.id.mapView);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        //mapView.setBuiltInZoomControls(true);
        mapView.setMaxZoomLevel(19);
        mapView.setMinZoomLevel(17);

        BoundingBox boundingBox = new BoundingBox(42.06470019, -87.52569948, 41.6441576, -87.884297);
        mapView.setScrollableAreaLimitDouble(boundingBox);

        mapView.getController().setZoom(18);
        GeoPoint startingPoint = new GeoPoint(41.945477, -87.690778);
        mapView.getController().setCenter(startingPoint);

        myLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(MapActivity.this), mapView);
        mapView.getOverlays().add(myLocationOverlay);

        locationProgressDialog = new ProgressDialog(MapActivity.this);
        locationProgressDialog.setMessage(getString(R.string.waiting_for_gps_signal));
        locationProgressDialog.show();

        myLocationOverlay.runOnFirstFix(new Runnable() {
            @Override
            public void run() {
                locationProgressDialog.dismiss();
            }
        });

        projection = new String[]{
                "stop_code",
                "stop_name",
                "stop_lat",
                "stop_lon"
        };
        selection = "(stop_lat < ?) AND (stop_lat > ?) AND (stop_lon < ?) AND (stop_lon > ?)";

        buttonMyLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentLocation != null) {
                    mapView.getController().animateTo(new GeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude()));
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
                if (! myLocationOverlay.isFollowLocationEnabled()) // Only call disableFollowMe() if the onScroll() was triggered by the user, which would have disabled follow me
                    disableFollowMe(); // This check prevents disableFollowMe() from being called immediately after the button is clicked
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

        followMeShouldBeEnabled = true;

        fragmentManager = getFragmentManager();

        final float[] startX = {0};
        final float[] startY = {0};

        mapView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startX[0] = event.getX();
                        startY[0] = event.getY();
                        break;
                    case MotionEvent.ACTION_UP:
                        float endX = event.getX();
                        float endY = event.getY();
                        if (isAClick(startX[0], startY[0], endX, endY)) {
                            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                            Fragment testFragment = fragmentManager.findFragmentByTag("stopFragment");
                            //Log.d("DEBUG", "MapView was clicked");
                            //Log.d("DEBUG", "Change in x: " + String.valueOf(Math.abs(startX[0] - endX)));
                            //Log.d("DEBUG", "Change in y: " + String.valueOf(Math.abs(startY[0] - endY)));
                            if (testFragment != null) {
                                Log.d("DEBUG", "The fragment was NOT null");
                                fragmentTransaction.remove(testFragment);
                                fragmentTransaction.commit();
                            } else {
                                Log.d("DEBUG", "The fragment was null");
                            }
                        }
                        break;

                }
                return false;
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        database = new GTFSHelper(MapActivity.this).getReadableDatabase();
    }

    @Override
    protected void onResume() {
        super.onResume();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        myLocationOverlay.enableMyLocation();
        if (followMeShouldBeEnabled)
            enableFollowMe();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            locationManager.removeUpdates(this);
        myLocationOverlay.disableFollowLocation();
        myLocationOverlay.disableMyLocation();

    }

    @Override
    protected void onStop() {
        super.onStop();
        database.close();
    }

    @Override
    public void onLocationChanged(Location location) {
        currentLocation = location;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    private void updateMarkers() {
        for (Overlay overlay : mapView.getOverlays()) {
            if (overlay instanceof ItemizedOverlayWithFocus) {
                mapView.getOverlays().remove(overlay); // Remove any existing ItemizedOverlayWithFocus, because we don't want its points anymore
                break;
            }
        }

        String[] selectionArgs = {String.valueOf(mapView.getBoundingBox().getLatNorth()), String.valueOf(mapView.getBoundingBox().getLatSouth()), String.valueOf(mapView.getBoundingBox().getLonEast()), String.valueOf(mapView.getBoundingBox().getLonWest())};

        Cursor query = database.query("stops", projection, selection, selectionArgs, null, null, null);

        ArrayList<OverlayItem> points = new ArrayList<OverlayItem>();

        while (query.moveToNext()) {
            points.add(new OverlayItem(query.getString(1), String.valueOf(query.getInt(0)), new GeoPoint(query.getDouble(2), query.getDouble(3))));
        }

        ItemizedOverlayWithFocus<OverlayItem> itemizedOverlayWithFocus = new ItemizedOverlayWithFocus<OverlayItem>(points, new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
            @Override
            public boolean onItemSingleTapUp(int index, OverlayItem item) {
                //Log.d("DEBUG", "Marker was clicked");
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.add(R.id.activity_map, StopFragment.newInstance(item.getTitle()), "stopFragment");
                fragmentTransaction.commit();
                return false;
            }

            @Override
            public boolean onItemLongPress(int index, OverlayItem item) {
                return false;
            }
        }, MapActivity.this);
        itemizedOverlayWithFocus.setFocusItemsOnTap(true);
        mapView.getOverlays().add(itemizedOverlayWithFocus);

        query.close();
    }

    private void enableFollowMe() {
        buttonFollowMe.setImageResource(R.drawable.ic_follow_me_on);
        myLocationOverlay.enableFollowLocation();
        followMeShouldBeEnabled = true;
    }

    private void disableFollowMe() {
        buttonFollowMe.setImageResource(R.drawable.ic_follow_me);
        myLocationOverlay.disableFollowLocation();
        followMeShouldBeEnabled = false;
    }

    private boolean isAClick(float startX, float startY, float endX, float endY) {
        return (Math.abs(startX - endX) < 10 && Math.abs(startY - endY) < 10);
    }
}