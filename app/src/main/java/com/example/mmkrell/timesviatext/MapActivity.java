package com.example.mmkrell.timesviatext;

import android.content.res.AssetManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.io.IOException;
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
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;
import org.osmdroid.views.overlay.simplefastpoint.LabelledGeoPoint;
import org.osmdroid.views.overlay.simplefastpoint.SimpleFastPointOverlay;
import org.osmdroid.views.overlay.simplefastpoint.SimpleFastPointOverlayOptions;
import org.osmdroid.views.overlay.simplefastpoint.SimplePointTheme;

public class MapActivity extends AppCompatActivity {

    //int[] stopId;
    int[] stopCode;
    String[] stopName;
    //String[] stopDesc;
    double[] stopLat;
    double[] stopLon;
    //int[] locationType;
    //int[] parentStation;
    //int[] wheelchairBoarding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        Configuration.getInstance().setUserAgentValue(BuildConfig.APPLICATION_ID);
        MapView mapView = (MapView) findViewById(R.id.mapView);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.setMaxZoomLevel(20);
        mapView.setMinZoomLevel(12);

        BoundingBox boundingBox = new BoundingBox(42.06470019, -87.52569948, 41.6441576, -87.884297);
        mapView.setScrollableAreaLimitDouble(boundingBox);

        IMapController iMapController = mapView.getController();
        iMapController.setZoom(18);
        GeoPoint startingPoint = new GeoPoint(41.945477, -87.690778);
        iMapController.setCenter(startingPoint);

        CompassOverlay compassOverlay = new CompassOverlay(MapActivity.this, new InternalCompassOrientationProvider(getApplicationContext()), mapView);
        compassOverlay.enableCompass();
        mapView.getOverlays().add(compassOverlay);

        //RotationGestureOverlay rotationGestureOverlay = new RotationGestureOverlay(mapView);
        //rotationGestureOverlay.setEnabled(true);
        //mapView.getOverlays().add(rotationGestureOverlay);

        MyLocationNewOverlay myLocationNewOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(MapActivity.this), mapView);
        myLocationNewOverlay.enableMyLocation();
        mapView.getOverlays().add(myLocationNewOverlay);

        Scanner numberOfStopsScanner;
        //Scanner stopIdScanner = null;
        Scanner stopCodeScanner = null;
        Scanner stopNameScanner = null;
        //Scanner stopDescScanner = null;
        Scanner stopLatScanner = null;
        Scanner stopLonScanner = null;
        //Scanner locationTypeScanner = null;
        //Scanner parentStationScanner = null;
        //Scanner wheelchairBoardingScanner = null;
        int numberOfStops = 0;

        AssetManager assetManager = getApplicationContext().getAssets();

        try {
            numberOfStopsScanner = new Scanner(assetManager.open("stop_code_list.txt")).useDelimiter(", ");
            numberOfStops = new StringTokenizer(numberOfStopsScanner.nextLine()).countTokens();

            //stopIdScanner = new Scanner(assetManager.open("stop_id_list.txt")).useDelimiter(", ");
            stopCodeScanner = new Scanner(assetManager.open("stop_code_list.txt")).useDelimiter(", ");
            stopNameScanner = new Scanner(assetManager.open("stop_name_list.txt")).useDelimiter(", ");
            //stopDescScanner = new Scanner(assetManager.open("stop_desc_list.txt")).useDelimiter(", ");
            stopLatScanner = new Scanner(assetManager.open("stop_lat_list.txt")).useDelimiter(", ");
            stopLonScanner = new Scanner(assetManager.open("stop_lon_list.txt")).useDelimiter(", ");
            //locationTypeScanner = new Scanner(assetManager.open("location_type_list.txt")).useDelimiter(", ");
            //parentStationScanner = new Scanner(assetManager.open("parent_station_list.txt")).useDelimiter(", ");
            //wheelchairBoardingScanner = new Scanner(assetManager.open("wheelchair_boarding_list.txt")).useDelimiter(", ");
        } catch (IOException e) {
            e.printStackTrace();
        }

        //stopId = new int[numberOfStops];
        stopCode = new int[numberOfStops];
        stopName = new String[numberOfStops];
        //stopDesc = new String[numberOfStops];
        stopLat = new double[numberOfStops];
        stopLon = new double[numberOfStops];
        //locationType = new int[numberOfStops];
        //parentStation = new int[numberOfStops];
        //wheelchairBoarding = new int[numberOfStops];

        int i = 0;
        while (i < numberOfStops) {
            //stopId[i] = stopIdScanner.nextInt();
            stopCode[i] = stopCodeScanner.nextInt();
            stopName[i] = stopNameScanner.next();
            //stopDesc[i] = stopDescScanner.next() + ", " + stopDescScanner.next() + ", " + stopDescScanner.next(); // The stop description has three parts
            stopLat[i] = stopLatScanner.nextDouble();
            stopLon[i] = stopLonScanner.nextDouble();
            //locationType[i] = locationTypeScanner.nextInt();
            //parentStation[i] = parentStationScanner.nextInt();
            //wheelchairBoarding[i] = wheelchairBoardingScanner.nextInt();
            i ++;
        }

        ArrayList<IGeoPoint> points = new ArrayList<IGeoPoint>();
        for (i = 0; i < numberOfStops; i ++) {
            points.add(new LabelledGeoPoint(stopLat[i], stopLon[i], stopName[i]));
        }

        SimpleFastPointOverlay simpleFastPointOverlay = new SimpleFastPointOverlay(new SimplePointTheme(points, true), new SimpleFastPointOverlayOptions().setSymbol(SimpleFastPointOverlayOptions.Shape.SQUARE));
        simpleFastPointOverlay.setOnClickListener(new SimpleFastPointOverlay.OnClickListener() {
            @Override
            public void onClick(SimpleFastPointOverlay.PointAdapter points, Integer point) {

            }
        });
        simpleFastPointOverlay.setEnabled(true);
        mapView.getOverlays().add(simpleFastPointOverlay);
    }
}
