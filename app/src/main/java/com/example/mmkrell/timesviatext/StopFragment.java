package com.example.mmkrell.timesviatext;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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

    public StopFragment() {
        // Required empty public constructor
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
        TextView textViewName = (TextView) v.findViewById(R.id.stop_fragment_text_view_name);
        TextView textViewDirection = (TextView) v.findViewById(R.id.stop_fragment_text_view_direction);
        Button buttonWriteText = (Button) v.findViewById(R.id.stop_fragment_button_write_text);

        textViewName.setText(stopName);

        if (stopDesc.isEmpty()) { // If stopDesc is empty, hide the TextView that displays the direction
            textViewDirection.setVisibility(View.GONE);
        } else {
            int startPos = stopName.length() + 2; // Start at beginning of direction
            int endPos = stopDesc.indexOf(",", startPos); // End once the second comma is found
            String stopDirection = stopDesc.substring(startPos, endPos);
            textViewDirection.setText(stopDirection);
        }

        buttonWriteText.setOnClickListener(new View.OnClickListener() {
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