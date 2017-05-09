package com.example.mmkrell.timesviatext;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

public class NavigationBarActivity extends AppCompatActivity {

    static String userLocation;

    // Only one of each fragment is used so that they don't have to be recreated every time the user moves between views
    private MapFragment mapFragment;
    private RoutesFragment routesFragment;
    private FavoritesFragment favoritesFragment;

    private BottomNavigationView bottomNavigationView;

    private BottomNavigationView.OnNavigationItemSelectedListener onNavigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            switch (item.getItemId()) {
                case R.id.navigation_map:
                    userLocation = "MapFragment";
                    getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                    getSupportActionBar().setTitle(R.string.title_map);
                    fragmentTransaction.show(mapFragment);

                    fragmentTransaction.hide(routesFragment);
                    fragmentTransaction.hide(favoritesFragment);
                    fragmentTransaction.commit();
                    return true;
                case R.id.navigation_routes:
                    userLocation = RoutesFragment.currentAdapterName;

                    // If viewing DirectionsAdapter or StopsAdapter, display the back button
                    switch (userLocation) {
                        case "RoutesAdapter":
                            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                            getSupportActionBar().setTitle(R.string.title_routes);
                            break;
                        case "DirectionsAdapter":
                            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                            getSupportActionBar().setTitle(RoutesAdapter.selectedRouteTitle);
                            break;
                    }

                    fragmentTransaction.show(routesFragment);

                    fragmentTransaction.hide(mapFragment);
                    fragmentTransaction.hide(favoritesFragment);
                    fragmentTransaction.commit();
                    return true;
                case R.id.navigation_favorites:
                    userLocation = "FavoritesFragment";
                    getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                    getSupportActionBar().setTitle(R.string.title_favorites);
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

        getSupportFragmentManager().beginTransaction()
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
                routesFragment.getRecyclerView().setAdapter(
                        new RoutesAdapter(this, routesFragment.getRecyclerView()));
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                getSupportActionBar().setTitle(R.string.title_routes);
                RoutesFragment.currentAdapterName = "RoutesAdapter";
                NavigationBarActivity.userLocation = RoutesFragment.currentAdapterName;
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
}