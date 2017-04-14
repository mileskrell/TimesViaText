package com.example.mmkrell.timesviatext;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashSet;

public class StopFragment extends Fragment {

    private int stopCode;
    private String stopName;
    private String stopDesc;

    private boolean checked;
    private SharedPreferences sharedPreferences;
    private HashSet<String> favorites;

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
        final ImageView buttonFavorite = (ImageView) v.findViewById(R.id.stop_fragment_button_favorite);
        Button buttonWriteText = (Button) v.findViewById(R.id.stop_fragment_button_write_text);

        final Drawable uncheckedFavorite =  ContextCompat.getDrawable(getContext(), R.drawable.ic_favorite_border_red_24dp);
        final Drawable checkedFavorite = ContextCompat.getDrawable(getContext(), R.drawable.ic_favorite_red_24dp);

        textViewName.setText(stopName);

        if (stopDesc.isEmpty()) { // If stopDesc is empty, hide the TextView that displays the direction
            textViewDirection.setVisibility(View.GONE);
        } else {
            int startPos = stopName.length() + 2; // Start at beginning of direction
            int endPos = stopDesc.indexOf(",", startPos); // End once the second comma is found
            String stopDirection = stopDesc.substring(startPos, endPos);
            textViewDirection.setText(stopDirection);
        }

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        favorites = new HashSet<>(sharedPreferences.getStringSet("favorites", new HashSet<String>()));

        // If this stop is a favorite, make the heart filled to represent that
        if (favorites.contains(String.valueOf(stopCode))) {
            checked = true;
            buttonFavorite.setImageDrawable(checkedFavorite);
        }

        buttonFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Flip the "checked" boolean
                checked = ! checked;
                SharedPreferences.Editor editor = sharedPreferences.edit();

                if (checked) {
                    // Set image to filled heart and add stop to favorites
                    buttonFavorite.setImageDrawable(checkedFavorite);
                    favorites.add(String.valueOf(stopCode));
                } else {
                    // Set image to empty heart and remove stop from favorites
                    buttonFavorite.setImageDrawable(uncheckedFavorite);
                    favorites.remove(String.valueOf(stopCode));
                }

                editor.putStringSet("favorites", favorites);
                editor.apply();
            }
        });

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