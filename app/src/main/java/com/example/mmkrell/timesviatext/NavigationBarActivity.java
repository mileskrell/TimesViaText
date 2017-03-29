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
import android.widget.Toast;

public class NavigationBarActivity extends AppCompatActivity {

    // Only one of each fragment is used so that they don't have to be recreated every time the user moves between views
    FavoritesFragment favoritesFragment;
    MapFragment mapFragment;
    RoutesFragment routesFragment;

    SharedPreferences sharedPreferences;

    private BottomNavigationView.OnNavigationItemSelectedListener onNavigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            switch (item.getItemId()) {
                case R.id.navigation_favorites:
                    fragmentTransaction.show(favoritesFragment);
                    favoritesFragment.setUserVisibleHint(true);

                    fragmentTransaction.hide(mapFragment);
                    mapFragment.setUserVisibleHint(false);
                    fragmentTransaction.hide(routesFragment);
                    routesFragment.setUserVisibleHint(false);
                    fragmentTransaction.commit();
                    return true;
                case R.id.navigation_map:
                    fragmentTransaction.show(mapFragment);
                    mapFragment.setUserVisibleHint(true);

                    fragmentTransaction.hide(favoritesFragment);
                    favoritesFragment.setUserVisibleHint(false);
                    fragmentTransaction.hide(routesFragment);
                    routesFragment.setUserVisibleHint(false);
                    fragmentTransaction.commit();
                    return true;
                case R.id.navigation_routes:
                    fragmentTransaction.show(routesFragment);
                    routesFragment.setUserVisibleHint(true);

                    fragmentTransaction.hide(favoritesFragment);
                    favoritesFragment.setUserVisibleHint(false);
                    fragmentTransaction.hide(mapFragment);
                    mapFragment.setUserVisibleHint(false);
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
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);

        favoritesFragment = FavoritesFragment.newInstance();
        mapFragment = MapFragment.newInstance();
        routesFragment = RoutesFragment.newInstance();

        getSupportFragmentManager().beginTransaction()
                .add(R.id.content, favoritesFragment, "favorites_fragment")
                .add(R.id.content, mapFragment, "map_fragment")
                .add(R.id.content, routesFragment, "routes_fragment")
                .commit();

        navigation.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener);

        // Select the favorites fragment at startup
        onNavigationItemSelectedListener.onNavigationItemSelected(navigation.getMenu().getItem(0));

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
                Toast.makeText(this, "You selected About", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        mapFragment.deselectMarker();
        super.onBackPressed();
    }
}