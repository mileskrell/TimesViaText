package com.example.mmkrell.timesviatext;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.Scanner;
import java.util.StringTokenizer;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

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
        SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        supportMapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            finish();

        googleMap.setMyLocationEnabled(true);

        googleMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(41.945477, -87.690778)));
        googleMap.moveCamera(CameraUpdateFactory.zoomTo(20));

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
            i++;
        }

        for (i = 0; i < stopCode.length; i ++) {
            LatLng currentStop = new LatLng(stopLat[i], stopLon[i]);
            googleMap.addMarker(new MarkerOptions().position(currentStop).title(stopName[i]).snippet(String.valueOf(stopCode[i])));
        }
    }
}
