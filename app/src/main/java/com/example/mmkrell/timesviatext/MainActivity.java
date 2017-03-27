package com.example.mmkrell.timesviatext;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {

    Button buttonOpenMap;
    TextView textViewWelcomeMessage;

    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set the preferences accessible from SettingsFragment to their defaults
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean hasCompletedTutorial = sharedPreferences.getBoolean("has_completed_tutorial", false);
        // If the user has already been through the tutorial, go straight to the map
        if (hasCompletedTutorial)
            startActivity(new Intent(this, NavigationBarActivity.class));

        setContentView(R.layout.activity_main);
        buttonOpenMap = (Button) findViewById(R.id.buttonOpenMap);
        textViewWelcomeMessage = (TextView) findViewById(R.id.textViewWelcomeMessage);

        buttonOpenMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sharedPreferences.edit().putBoolean("has_completed_tutorial", true).apply();
                startActivity(new Intent(MainActivity.this, NavigationBarActivity.class));
            }
        });

        textViewWelcomeMessage.setMovementMethod(new ScrollingMovementMethod());

        if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) |
                (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // If any permission request is denied, just exit the app
        for (int x : grantResults)
            if (x == PackageManager.PERMISSION_DENIED)
                finish();
    }
}