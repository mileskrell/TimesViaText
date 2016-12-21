package com.example.mmkrell.timesviatext;

import android.content.res.AssetManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {

    TextView textView;
    TextView textView2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.textView);
        textView2 = (TextView) findViewById(R.id.textView2);

        ArrayList<Stop> stops = new ArrayList<Stop>();

        AssetManager assetManager = getApplicationContext().getAssets();

        Scanner outerScanner = null;
        try {
            outerScanner = new Scanner(assetManager.open("message.txt"));
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
            stops.add(new Stop(currentStopId, currentStopCode, currentStopName, currentStopDesc, currentStopLat, currentStopLon, currentLocationType, currentParentStation, currentWheelchairBoarding));
        }
        textView.setText(stops.get(0).getStopName() + " " + stops.get(0).getStopLat());
        textView2.setText(stops.get(1).getStopName() + " " + stops.get(1).getStopLat());
    }
}
