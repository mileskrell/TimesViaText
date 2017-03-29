package com.example.mmkrell.timesviatext;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class DownloadMapTilesActivity extends AppCompatActivity {

    SharedPreferences sharedPreferences;
    boolean hasDownloadedTiles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_map_tiles);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        hasDownloadedTiles = sharedPreferences.getBoolean("has_downloaded_tiles", false);

        if (hasDownloadedTiles)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Button buttonDownloadMapTiles = (Button) findViewById(R.id.button_download_map_tiles);
        buttonDownloadMapTiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sharedPreferences.edit().putBoolean("has_downloaded_tiles", true).apply();
                finish();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        // If the user hasn't yet downloaded the map tiles, don't return
        // to NavigationBarActivity when the back button is pressed
        // TODO: find a way to do this without making the back button completely nonfunctional
        if (hasDownloadedTiles)
            super.onBackPressed();
    }
}