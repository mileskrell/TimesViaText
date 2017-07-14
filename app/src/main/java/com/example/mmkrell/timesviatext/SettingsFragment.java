package com.example.mmkrell.timesviatext;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;

import org.osmdroid.api.IGeoPoint;

public class SettingsFragment extends PreferenceFragment {

    public SettingsFragment() {
        // Required empty public constructor
    }

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);

        // Get a reference to the MapFragment instance
        final MapFragment mapFragment = (MapFragment) NavigationBarActivity
                .supportFragmentManager.findFragmentByTag("map_fragment");

        findPreference("pref_scale_tiles_to_dpi").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                // Save old map center
                IGeoPoint oldMapCenter = mapFragment.getMapView().getMapCenter();
                // Save whether follow me should be enabled
                boolean oldFollowMeState = mapFragment.followMeShouldBeEnabled;

                // Turn "scale tiles to DPI" on or off
                mapFragment.getMapView().setTilesScaledToDpi((boolean) newValue);

                // Unset bounding box to allow us to cross over it
                mapFragment.getMapView().setScrollableAreaLimitDouble(null);
                // Put the center back to where it was
                mapFragment.getMapView().getController().setCenter(oldMapCenter);
                // Set the bounding box again
                mapFragment.getMapView().setScrollableAreaLimitDouble(MapFragment.chicagoBoundingBox);
                // Put "follow me" status back to what it was
                mapFragment.setFollowMeState(oldFollowMeState);
                return true;
            }
        });

        findPreference("pref_download_new_tiles").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                mapFragment.getMapView().setUseDataConnection((boolean) newValue);
                return true;
            }
        });
    }
}
