package com.example.mmkrell.timesviatext;

import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class StopFragment extends Fragment {

    private int stopCode;
    private String stopName;
    private String stopDesc;

    TextView stopFragmentTextViewName;
    TextView stopFragmentTextViewDirection;
    Button stopFragmentButton;

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
        stopFragmentTextViewName = (TextView) v.findViewById(R.id.stopFragmentTextViewName);
        stopFragmentTextViewDirection = (TextView) v.findViewById(R.id.stopFragmentTextViewDirection);
        stopFragmentButton = (Button) v.findViewById(R.id.stopFragmentButton);

        stopFragmentTextViewName.setText(stopName);

        if (stopDesc.equals("")) { // If stopDesc is empty, hide the TextView that displays the direction
            stopFragmentTextViewDirection.setVisibility(View.GONE);
        } else {
            int startPos = stopName.length() + 2; // Start at beginning of direction
            int endPos = stopDesc.indexOf(",", startPos); // End once the second comma is found
            String stopDirection = stopDesc.substring(startPos, endPos);
            stopFragmentTextViewDirection.setText(stopDirection);
        }

        stopFragmentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent getStopTimesIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:41411"));
                getStopTimesIntent.putExtra("sms_body", "CTABUS " + stopCode);
                if (getStopTimesIntent.resolveActivity(getActivity().getPackageManager()) != null)
                    startActivity(getStopTimesIntent);
                else
                    Toast.makeText(getActivity().getApplicationContext(), "No SMS app found", Toast.LENGTH_LONG).show();
            }
        });
        return v;
    }
}