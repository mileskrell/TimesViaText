package com.example.mmkrell.timesviatext;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class StopFragment extends Fragment {

    public StopFragment() {

    }

    public static StopFragment newInstance(String stopName) {
        StopFragment stopFragment = new StopFragment();
        Bundle args = new Bundle();
        args.putString("stop_name", stopName);
        stopFragment.setArguments(args);
        return stopFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_stop, container, false);
    }
}