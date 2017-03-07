package com.example.mmkrell.timesviatext;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

public class NavigationBarActivity extends AppCompatActivity {

    // Only one MapFragment is used so that it doesn't have to be recreated every time the user moves between views
    MapFragment mapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_bar);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);

        navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation_favorites:
                        return true;
                    case R.id.navigation_map:
                        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                        if (mapFragment == null)
                            mapFragment = MapFragment.newInstance();
                        fragmentTransaction.replace(R.id.content, mapFragment);
                        fragmentTransaction.commit();
                        return true;
                    case R.id.navigation_routes:
                        return true;
                }
                return false;
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (mapFragment != null)
            mapFragment.deselectMarker();
        super.onBackPressed();
    }
}