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
        InputStream inputStream = null;
        try {
            inputStream = assetManager.open("message.txt");

        } catch (IOException e) {
            e.printStackTrace();
        }

        Scanner outerScanner = new Scanner(inputStream);
        outerScanner.nextLine();            // Skip the first line; it's just the legend
        while (outerScanner.hasNextLine()) {
            Scanner innerScanner = new Scanner(outerScanner.nextLine()).useDelimiter(",");
            while (innerScanner.hasNext()) {
                //stops.add(new Stop(innerScanner.nextInt(), innerScanner.nextInt(), innerScanner.next(), innerScanner.next(), innerScanner.nextDouble(), innerScanner.nextDouble(), innerScanner.nextInt(), innerScanner.nextInt(), innerScanner.nextInt()));
                stops.add(new Stop());
            }
        }
        //textView.setText(stops.get(0).getStopName() + " " + stops.get(0).getStopCode());
        //textView2.setText(stops.get(1).getStopName() + " " + stops.get(1).getStopCode());
    }
}
