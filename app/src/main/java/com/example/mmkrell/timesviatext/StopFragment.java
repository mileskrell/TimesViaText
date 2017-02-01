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
    private String stopDesc;

    TextView stopFragmentTextView;

    public StopFragment() {

    }

    public static StopFragment newInstance(int stopCode, String stopName, String stopDesc) {
        StopFragment stopFragment = new StopFragment();
        Bundle args = new Bundle();
        args.putInt("stopCode", stopCode);
        args.putString("stopName", stopName);
        args.putString("stopDesc", stopDesc);
        stopFragment.setArguments(args);
        return stopFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            stopCode = getArguments().getInt("stopCode");
            stopName = getArguments().getString("stopName");
            stopDesc = getArguments().getString("stopDesc");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_stop, container, false);
        stopFragmentTextView = (TextView) v.findViewById(R.id.stopFragmentTextView);
        System.out.println("Starting at " + stopName.length());

        if (stopDesc.equals("")) { // If stopDesc is empty, don't try to get the direction
            stopFragmentTextView.setText(stopName + "\n" + stopCode);
        } else {
            int startPos = stopName.length() + 2; // Start at beginning of direction
            int endPos = stopDesc.indexOf(",", startPos); // End once the second comma is found
            String stopDirection = stopDesc.substring(startPos, endPos);
            stopFragmentTextView.setText(stopName + " (" + stopDirection + ")\n" + stopCode);
        }
        return v;
    }
}