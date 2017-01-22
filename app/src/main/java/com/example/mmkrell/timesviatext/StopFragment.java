package com.example.mmkrell.timesviatext;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class StopFragment extends Fragment {

    private int stopCode;
    private String stopName;

    TextView stopFragmentTextView;

    public StopFragment() {

    }

    public static StopFragment newInstance(int stopCode, String stopName) {
        StopFragment stopFragment = new StopFragment();
        Bundle args = new Bundle();
        args.putInt("stopCode", stopCode);
        args.putString("stopName", stopName);
        stopFragment.setArguments(args);
        return stopFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            stopCode = getArguments().getInt("stopCode");
            stopName = getArguments().getString("stopName");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_stop, container, false);
        stopFragmentTextView = (TextView) v.findViewById(R.id.stopFragmentTextView);
        stopFragmentTextView.setText(stopName + "\n" + stopCode);
        return v;
    }
}