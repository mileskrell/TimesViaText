package com.example.mmkrell.timesviatext;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class IntroActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {

    private Button buttonOpenMap;
    private TextView textViewWelcomeMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final boolean permissionsAlreadyGranted =
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;

        // If the user has already been through the tutorial, go straight to the map
        if (permissionsAlreadyGranted) {
            startActivity(new Intent(this, NavigationBarActivity.class));
        }

        setContentView(R.layout.activity_main);
        buttonOpenMap = (Button) findViewById(R.id.button_open_map);
        textViewWelcomeMessage = (TextView) findViewById(R.id.text_view_welcome_message);

        buttonOpenMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (! permissionsAlreadyGranted) {
                    ActivityCompat.requestPermissions(IntroActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
                }
            }
        });

        textViewWelcomeMessage.setMovementMethod(new ScrollingMovementMethod());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // If any permission request is denied, just exit the app
        boolean permissionDenied = false;

        for (int x : grantResults) {
            if (x == PackageManager.PERMISSION_DENIED) {
                permissionDenied = true;
            }
        }

        if (permissionDenied) {
            finish();
        } else {
            startActivity(new Intent(this, NavigationBarActivity.class));
        }
    }
}
