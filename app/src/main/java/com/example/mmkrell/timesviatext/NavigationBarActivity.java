package com.example.mmkrell.timesviatext;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

public class NavigationBarActivity extends AppCompatActivity {

    // Only one of each fragment is used so that they don't have to be recreated every time the user moves between views
    private MapFragment mapFragment;
    private RoutesFragment routesFragment;
    private FavoritesFragment favoritesFragment;

    private SharedPreferences sharedPreferences;

    private BottomNavigationView.OnNavigationItemSelectedListener onNavigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            switch (item.getItemId()) {
                case R.id.navigation_map:
                    fragmentTransaction.show(mapFragment);
                    mapFragment.setUserVisibleHint(true);

                    fragmentTransaction.hide(routesFragment);
                    routesFragment.setUserVisibleHint(false);
                    fragmentTransaction.hide(favoritesFragment);
                    favoritesFragment.setUserVisibleHint(false);
                    fragmentTransaction.commit();
                    return true;
                case R.id.navigation_routes:
                    fragmentTransaction.show(routesFragment);
                    routesFragment.setUserVisibleHint(true);

                    fragmentTransaction.hide(mapFragment);
                    mapFragment.setUserVisibleHint(false);
                    fragmentTransaction.hide(favoritesFragment);
                    favoritesFragment.setUserVisibleHint(false);
                    fragmentTransaction.commit();
                    return true;
                case R.id.navigation_favorites:
                    fragmentTransaction.show(favoritesFragment);
                    favoritesFragment.setUserVisibleHint(true);

                    fragmentTransaction.hide(mapFragment);
                    mapFragment.setUserVisibleHint(false);
                    fragmentTransaction.hide(routesFragment);
                    routesFragment.setUserVisibleHint(false);
                    fragmentTransaction.commit();
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_bar);
        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.navigation);

        favoritesFragment = FavoritesFragment.newInstance();
        mapFragment = MapFragment.newInstance();
        routesFragment = RoutesFragment.newInstance();

        getSupportFragmentManager().beginTransaction()
                .add(R.id.content, mapFragment, "map_fragment")
                .add(R.id.content, routesFragment, "routes_fragment")
                .add(R.id.content, favoritesFragment, "favorites_fragment")
                .commit();

        bottomNavigationView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener);

        // Select the map fragment at startup
        bottomNavigationView.setSelectedItemId(R.id.navigation_map);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        // Have the user download map tiles, if they haven't before
        if (! sharedPreferences.getBoolean("has_downloaded_tiles", false)) {
            startActivity(new Intent(this, DownloadMapTilesActivity.class));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.options_menu_download_tiles:
                startActivity(new Intent(this, DownloadMapTilesActivity.class));
                return true;
            case R.id.options_menu_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.options_menu_about:
                startActivity(new Intent(this, AboutActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // Without this override, onBackPressed() would remove
    // any StopFragment even if the user wasn't viewing it.
    // This means that, if the user wasn't viewing MapFragment but a StopFragment existed,
    // finish() wouldn't be called until the back button was pressed twice.
    @Override
    public void onBackPressed() {
        // If MapFragment is visible, try to remove StopFragment
        if (mapFragment.getUserVisibleHint()) {
            if (! mapFragment.deselectMarkerAndRemoveStopFragment(true))
                // If nothing was removed, call onBackPressed()
                super.onBackPressed();
            return;
        }

        // MapFragment isn't visible, so just call finish()
        finish();
    }
}