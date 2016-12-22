package com.example.mmkrell.timesviatext;

import android.content.res.AssetManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    ArrayList<Stop> stops; // TODO: 12/20/16  This uses a huge amount of RAM. Learn how to use databases (or maybe just use arrays or something)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        supportMapFragment.getMapAsync(this);

        stops = new ArrayList<Stop>();

        AssetManager assetManager = getApplicationContext().getAssets();

        Scanner outerScanner = null;
        try {
            outerScanner = new Scanner(assetManager.open("messages.txt"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        outerScanner.nextLine(); // Skip the first line; it's just the legend
        while (outerScanner.hasNextLine()) {
            Scanner innerScanner = new Scanner(outerScanner.nextLine()).useDelimiter(",");
            int currentStopId = innerScanner.nextInt();
            int currentStopCode = innerScanner.nextInt();
            String currentStopName = innerScanner.next();

            String currentStopDesc;
            String currentToken = innerScanner.next();
            if (currentToken.equals("\"\"")) { // If stop_desc is "empty" (it has 2 sets of quotes), just move on
                currentStopDesc = "";
            } else {
                currentStopDesc = currentToken + "," + innerScanner.next() + "," + innerScanner.next(); // Otherwise, use it and the following 2 tokens for the description
            }

            double currentStopLat = innerScanner.nextDouble();
            double currentStopLon = innerScanner.nextDouble();
            int currentLocationType = innerScanner.nextInt();

            int currentParentStation;
            if (innerScanner.hasNextInt()) { // If parent_station isn't empty, get its value
                currentParentStation = innerScanner.nextInt();
            } else {
                currentParentStation = -1; // Otherwise, use -1 and move on
                innerScanner.next();
            }

            int currentWheelchairBoarding = innerScanner.nextInt();
            Log.d("Debug", "Adding new stop: " + String.valueOf(currentStopId));
            stops.add(new Stop(currentStopId, currentStopCode, currentStopName, currentStopDesc, currentStopLat, currentStopLon, currentLocationType, currentParentStation, currentWheelchairBoarding));
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        for (Stop stop : stops) {
            LatLng currentStop = new LatLng(stop.getStopLat(), stop.getStopLon());
            googleMap.addMarker(new MarkerOptions().position(currentStop).title(String.valueOf(stop.getStopCode())));
        }
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(stops.get(0).getStopLat(), stops.get(0).getStopLon())));
    }
}
