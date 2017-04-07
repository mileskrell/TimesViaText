package com.example.mmkrell.timesviatext;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import org.osmdroid.tileprovider.cachemanager.CacheManager;

public class DownloadMapTilesActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    private boolean hasDownloadedTiles;

    private CacheManager cacheManager;
    private CacheManager.CacheManagerCallback cacheManagerCallback;

    private CacheManager.DownloadingTask downloadingTask;
    private DownloadMapTilesDialogFragment downloadMapTilesDialogFragment;

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
                downloadMapTilesDialogFragment = DownloadMapTilesDialogFragment.newInstance();
                downloadMapTilesDialogFragment.setCancelable(false);
                // The DialogFragment is added manually instead of using DialogFragment.show()
                // because show() calls commit(), but we need to call commitNow()
                getSupportFragmentManager().beginTransaction().add(downloadMapTilesDialogFragment,
                        "download_map_tiles_dialog_fragment").commitNow();

                cacheManager = new CacheManager(MapFragment.mapView);
                downloadingTask = cacheManager.downloadAreaAsyncNoUI(DownloadMapTilesActivity.this,
                        MapFragment.chicagoBoundingBox, 15, 18, cacheManagerCallback);
            }
        });

        cacheManagerCallback = new CacheManager.CacheManagerCallback() {
            @Override
            public void onTaskComplete() {
                downloadMapTilesDialogFragment.onTaskComplete();
                sharedPreferences.edit().putBoolean("has_downloaded_tiles", true).apply();
                downloadMapTilesDialogFragment.dismiss();
                finish();
            }

            @Override
            public void updateProgress(int progress, int currentZoomLevel, int zoomMin, int zoomMax) {
                downloadMapTilesDialogFragment.updateProgress(progress, currentZoomLevel, zoomMin, zoomMax);
            }

            @Override
            public void downloadStarted() {
                downloadMapTilesDialogFragment.downloadStarted();
            }

            @Override
            public void setPossibleTilesInArea(int total) {
                downloadMapTilesDialogFragment.setPossibleTilesInArea(total);
            }

            @Override
            public void onTaskFailed(int errors) {
                downloadMapTilesDialogFragment.onTaskFailed(errors);
            }
        };
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

    CacheManager.DownloadingTask getDownloadingTask() {
        return downloadingTask;
    }
}