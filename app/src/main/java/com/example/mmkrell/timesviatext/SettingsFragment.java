package com.example.mmkrell.timesviatext;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;

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
    }
}