package com.example.mmkrell.timesviatext;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.StringTokenizer;

import org.osmdroid.api.IGeoPoint;
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
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;
import org.osmdroid.views.overlay.simplefastpoint.LabelledGeoPoint;
import org.osmdroid.views.overlay.simplefastpoint.SimpleFastPointOverlay;
import org.osmdroid.views.overlay.simplefastpoint.SimpleFastPointOverlayOptions;
import org.osmdroid.views.overlay.simplefastpoint.SimplePointTheme;

public class MapActivity extends AppCompatActivity implements LocationListener {

    //int[] stopId;
    String[] stopCode;
    String[] stopName;
    //String[] stopDesc;
    String[] stopLat;
    String[] stopLon;
    //int[] locationType;
    //int[] parentStation;
    //int[] wheelchairBoarding;

    MapView mapView;

    Location currentLocation;
    LocationManager locationManager;
    CompassOverlay compassOverlay;
    MyLocationNewOverlay myLocationOverlay;

    ImageButton buttonGoToMyLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        buttonGoToMyLocation = (ImageButton) findViewById(R.id.buttonGoToMyLocation);

        Configuration.getInstance().setUserAgentValue(BuildConfig.APPLICATION_ID);
        mapView = (MapView) findViewById(R.id.mapView);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        //mapView.setBuiltInZoomControls(true);
        mapView.setMaxZoomLevel(20);
        mapView.setMinZoomLevel(11);

        BoundingBox boundingBox = new BoundingBox(42.06470019, -87.52569948, 41.6441576, -87.884297);
        mapView.setScrollableAreaLimitDouble(boundingBox);

        IMapController iMapController = mapView.getController();
        iMapController.setZoom(18);
        GeoPoint startingPoint = new GeoPoint(41.945477, -87.690778);
        iMapController.setCenter(startingPoint);

        compassOverlay = new CompassOverlay(MapActivity.this, new InternalCompassOrientationProvider(getApplicationContext()), mapView);
        mapView.getOverlays().add(compassOverlay);

        myLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(MapActivity.this), mapView);
        mapView.getOverlays().add(myLocationOverlay);

        buttonGoToMyLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentLocation != null)
                    mapView.getController().animateTo(new GeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude()));
            }
        });

        Log.d("Progress", "Finished initial map stuff");

        Scanner numberOfStopsScanner;
        //Scanner stopIdScanner = null;
        BufferedReader stopCodeBufferedReader = null;
        BufferedReader stopNameBufferedReader = null;
        //Scanner stopDescScanner = null;
        BufferedReader stopLatBufferedReader = null;
        BufferedReader stopLonBufferedReader = null;
        //Scanner locationTypeScanner = null;
        //Scanner parentStationScanner = null;
        //Scanner wheelchairBoardingScanner = null;
        int numberOfStops = 0;

        AssetManager assetManager = getApplicationContext().getAssets();

        try {
            numberOfStopsScanner = new Scanner(assetManager.open("stop_code_list.txt")).useDelimiter(", ");
            numberOfStops = new StringTokenizer(numberOfStopsScanner.nextLine()).countTokens();

            //stopIdScanner = new Scanner(assetManager.open("stop_id_list.txt")).useDelimiter(", ");
            stopCodeBufferedReader = new BufferedReader(new InputStreamReader(assetManager.open("stop_code_list.txt")));
            stopNameBufferedReader = new BufferedReader(new InputStreamReader(assetManager.open("stop_name_list.txt")));
            //stopDescScanner = new Scanner(assetManager.open("stop_desc_list.txt")).useDelimiter(", ");
            stopLatBufferedReader = new BufferedReader(new InputStreamReader(assetManager.open("stop_lat_list.txt")));
            stopLonBufferedReader = new BufferedReader(new InputStreamReader(assetManager.open("stop_lon_list.txt")));
            //locationTypeScanner = new Scanner(assetManager.open("location_type_list.txt")).useDelimiter(", ");
            //parentStationScanner = new Scanner(assetManager.open("parent_station_list.txt")).useDelimiter(", ");
            //wheelchairBoardingScanner = new Scanner(assetManager.open("wheelchair_boarding_list.txt")).useDelimiter(", ");
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.d("Progress", "Just created scanners and buffered readers from files");
        try {
            //stopId = new int[numberOfStops];
            stopCode = stopCodeBufferedReader.readLine().split(", ");
            stopName = stopNameBufferedReader.readLine().split(", ");
            //stopDesc = new String[numberOfStops];
            stopLat = stopLatBufferedReader.readLine().split(", ");
            stopLon = stopLonBufferedReader.readLine().split(", ");
            //locationType = new int[numberOfStops];
            //parentStation = new int[numberOfStops];
            //wheelchairBoarding = new int[numberOfStops];
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.d("Progress", "Just finished adding everything to arrays");

        ArrayList<OverlayItem> points = new ArrayList<OverlayItem>();
        for (int i = 0; i < numberOfStops; i++) {
            points.add(new OverlayItem(stopName[i], String.valueOf(stopCode[i]), new GeoPoint(Double.valueOf(stopLat[i]), Double.valueOf(stopLon[i]))));
        }

        Log.d("Progress", "Just finished adding everything to \"points\" ArrayList");

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
        Log.d("Progress", "Just created itemizedOverlayWithFocus");
        itemizedOverlayWithFocus.setFocusItemsOnTap(true);
        mapView.getOverlays().add(itemizedOverlayWithFocus);
    }

    @Override
    protected void onResume() {
        super.onResume();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this);
        myLocationOverlay.enableMyLocation();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            locationManager.removeUpdates(this);
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