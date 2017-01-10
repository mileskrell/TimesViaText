package com.example.mmkrell.timesviatext;

import android.Manifest;
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
import android.view.DragEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

public class MapActivity extends AppCompatActivity implements LocationListener {

    MapView mapView;

    Location currentLocation;
    LocationManager locationManager;
    CompassOverlay compassOverlay;
    MyLocationNewOverlay myLocationOverlay;

    ImageButton buttonMyLocation;
    ImageButton buttonFollowMe;

    TextView textViewOpenStreetMapCredit;

    boolean firstTime = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        buttonMyLocation = (ImageButton) findViewById(R.id.buttonMyLocation);
        buttonFollowMe = (ImageButton) findViewById(R.id.buttonFollowMe);
        buttonFollowMe.setTag("disabled");

        textViewOpenStreetMapCredit = (TextView) findViewById(R.id.textViewOpenStreetMapCredit);
        textViewOpenStreetMapCredit.setMovementMethod(LinkMovementMethod.getInstance()); // Makes the link clickable

        Configuration.getInstance().setUserAgentValue(BuildConfig.APPLICATION_ID);
        mapView = (MapView) findViewById(R.id.mapView);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        //mapView.setBuiltInZoomControls(true);
        mapView.setMaxZoomLevel(20);
        mapView.setMinZoomLevel(11);

        BoundingBox boundingBox = new BoundingBox(42.06470019, -87.52569948, 41.6441576, -87.884297);
        mapView.setScrollableAreaLimitDouble(boundingBox);

        final IMapController iMapController = mapView.getController();
        iMapController.setZoom(18);
        GeoPoint startingPoint = new GeoPoint(41.945477, -87.690778);
        iMapController.setCenter(startingPoint);

        compassOverlay = new CompassOverlay(MapActivity.this, new InternalCompassOrientationProvider(getApplicationContext()), mapView);
        mapView.getOverlays().add(compassOverlay);

        myLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(MapActivity.this), mapView);
        myLocationOverlay.enableMyLocation();
        mapView.getOverlays().add(myLocationOverlay);
        if (currentLocation != null)
            mapView.getController().setCenter(new GeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude()));

        buttonMyLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //System.out.println("Clicked myLocation button");
                //System.out.println("followMe state is " + String.valueOf(myLocationOverlay.isFollowLocationEnabled()));
                //System.out.println("North Latitude: " + mapView.getBoundingBox().getLatNorth());
                //System.out.println("East Longitude: " + mapView.getBoundingBox().getLonEast());
                //System.out.println("South Latitude: " + mapView.getBoundingBox().getLatSouth());
                //System.out.println("West Longitude: " + mapView.getBoundingBox().getLonWest());
                if (currentLocation != null) {
                //    iMapController.animateTo(new GeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude()));
                }
            }
        });

        buttonFollowMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (myLocationOverlay.isFollowLocationEnabled()) {
                    myLocationOverlay.disableFollowLocation();
                    buttonFollowMe.setImageResource(R.drawable.ic_follow_me);
                } else {
                    myLocationOverlay.enableFollowLocation();
                    buttonFollowMe.setImageResource(R.drawable.ic_follow_me_on);
                }
            }
        });

        mapView.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {
                return false;
            }
        });

        SQLiteDatabase database = new GTFSHelper(MapActivity.this).getReadableDatabase();

        String[] projection = {
                "stop_code",
                "stop_name",
                "stop_lat",
                "stop_lon"
        };

        //String selection = "(stop_lat BETWEEN ? AND ?) AND (stop_lon BETWEEN ? AND ?)";

        //System.out.println("NORTH BORDER IS " + mapView.getBoundingBox().getLatNorth());
        //System.out.println("EAST BORDER IS " + mapView.getBoundingBox().getLonEast());
        //System.out.println("SOUTH BORDER IS " + mapView.getBoundingBox().getLatSouth());
        //System.out.println("WEST BORDER IS " + mapView.getBoundingBox().getLonWest());
        //String[] selectionArgs = {String.valueOf(mapView.getBoundingBox().getLatNorth()), String.valueOf(mapView.getBoundingBox().getLatSouth()), String.valueOf(mapView.getBoundingBox().getLonEast()), String.valueOf(mapView.getBoundingBox().getLonWest())};

        Cursor query = database.query("stops", projection, null, null, null, null, null);

        ArrayList<OverlayItem> points = new ArrayList<OverlayItem>();

        while (query.moveToNext()) {
            //System.out.println("stop_name: " + query.getString(1));
            //System.out.println("stop_code: " + query.getInt(0));
            //System.out.println("stop_lat: " + query.getDouble(2) + " is less than " + north);
            //System.out.println("stop_lon: " + query.getDouble(3));
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

    @Override
    protected void onResume() {
        super.onResume();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        myLocationOverlay.enableMyLocation();
        if (firstTime)
            myLocationOverlay.enableFollowLocation();
        firstTime = false;
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
}