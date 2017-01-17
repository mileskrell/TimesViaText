package com.example.mmkrell.timesviatext;

import android.Manifest;
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

        locationProgressDialog = new ProgressDialog(MapActivity.this);
        locationProgressDialog.setMessage(getString(R.string.waiting_for_gps_signal));
        locationProgressDialog.show();
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
        locationProgressDialog.dismiss();
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
}