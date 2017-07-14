package com.example.mmkrell.timesviatext;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

public class NavigationBarActivity extends AppCompatActivity {

    // Used in onBackPressed()
    static String userLocation;

    // Used in SettingsFragment
    static FragmentManager supportFragmentManager;

    // Only one of each fragment is used so that they don't have to be recreated every time the user moves between views
    private MapFragment mapFragment;
    private RoutesFragment routesFragment;
    private FavoritesFragment favoritesFragment;

    private BottomNavigationView bottomNavigationView;

    private BottomNavigationView.OnNavigationItemSelectedListener onNavigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            FragmentTransaction fragmentTransaction = supportFragmentManager.beginTransaction();
            switch (item.getItemId()) {
                case R.id.navigation_map:
                    updateTitleAndUserLocation("MapFragment");
                    fragmentTransaction.show(mapFragment);

                    fragmentTransaction.hide(routesFragment);
                    fragmentTransaction.hide(favoritesFragment);
                    fragmentTransaction.commit();
                    return true;
                case R.id.navigation_routes:
                    updateTitleAndUserLocation(RoutesFragment.currentAdapterName);
                    fragmentTransaction.show(routesFragment);

                    fragmentTransaction.hide(mapFragment);
                    fragmentTransaction.hide(favoritesFragment);
                    fragmentTransaction.commit();
                    return true;
                case R.id.navigation_favorites:
                    updateTitleAndUserLocation("FavoritesFragment");
                    fragmentTransaction.show(favoritesFragment);

                    fragmentTransaction.hide(mapFragment);
                    fragmentTransaction.hide(routesFragment);
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
        bottomNavigationView = (BottomNavigationView) findViewById(R.id.navigation);

        // Initialize the database
        CTAHelper.setDatabaseInstance(new CTAHelper(getApplicationContext()).getReadableDatabase());

        favoritesFragment = FavoritesFragment.newInstance();
        mapFragment = MapFragment.newInstance();
        routesFragment = RoutesFragment.newInstance();

        supportFragmentManager = getSupportFragmentManager();

        supportFragmentManager.beginTransaction()
                .add(R.id.content, mapFragment, "map_fragment")
                .add(R.id.content, routesFragment, "routes_fragment")
                .add(R.id.content, favoritesFragment, "favorites_fragment")
                .commit();

        bottomNavigationView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener);

        // Select the map fragment at startup
        bottomNavigationView.setSelectedItemId(R.id.navigation_map);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
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

    // Handle when the back button is pressed depending on where the user is
    @Override
    public void onBackPressed() {

        switch (userLocation) {
            case "RoutesAdapter":
                finish();
                return;
            case "DirectionsAdapter":
                // Return to a RoutesAdapter
                routesFragment.getPositionSavingRecyclerView().setAdapter(
                        new RoutesAdapter(this, routesFragment.getPositionSavingRecyclerView()));

                // Restore scroll position of Routes list
                routesFragment.getPositionSavingRecyclerView()
                        .onRestoreInstanceState(PositionSavingRecyclerView.routesAdapterState);

                RoutesFragment.currentAdapterName = "RoutesAdapter";
                updateTitleAndUserLocation("RoutesAdapter");
                return;
            case "StopsAdapter":
                // Return to a DirectionsAdapter
                routesFragment.getPositionSavingRecyclerView().setAdapter(
                        new DirectionsAdapter(DirectionsAdapter.routeId, this,
                                routesFragment.getPositionSavingRecyclerView()));
                RoutesFragment.currentAdapterName = "DirectionsAdapter";
                updateTitleAndUserLocation("DirectionsAdapter");
                return;
            case "FavoritesFragment":
                finish();
                return;
        }

        // If we've gotten this far, the user must be viewing MapFragment

        // Try to remove StopFragment
        if (! mapFragment.deselectMarkerAndRemoveStopFragment(true))
            // If nothing was removed, call onBackPressed()
            super.onBackPressed();
    }

    BottomNavigationView getBottomNavigationView() {
        return bottomNavigationView;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Close the database since the activity is being destroyed
        CTAHelper.getDatabaseInstance().close();
    }

    void updateTitleAndUserLocation(String newLocation) {
        userLocation = newLocation;
        switch (newLocation) {
            case "MapFragment":
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                getSupportActionBar().setTitle(R.string.title_map);
                return;
            case "RoutesAdapter":
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                getSupportActionBar().setTitle(R.string.title_routes);
                return;
            case "DirectionsAdapter":
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle(DirectionsAdapter
                        .getTitle(getApplicationContext()));
                return;
            case "StopsAdapter":
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle(StopsAdapter
                        .getTitle(getApplicationContext()));
                return;
            case "FavoritesFragment":
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                getSupportActionBar().setTitle(R.string.title_favorites);
        }
    }
}
