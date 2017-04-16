package com.example.mmkrell.timesviatext;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.HashSet;

public class FavoritesFragment extends Fragment {

    private MyAdapter adapter;

    public FavoritesFragment() {
        // Required empty public constructor
    }

    public static FavoritesFragment newInstance() {
        return new FavoritesFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_favorites, container, false);

        RecyclerView recyclerView = (RecyclerView) v.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        HashSet<String> favoritesSet = new HashSet<>(sharedPreferences.getStringSet("favorites", new HashSet<String>()));

        String[] favoritesArray = sortStopCodesByNameAndDirection(favoritesSet);

        adapter = new MyAdapter((NavigationBarActivity) getActivity(), favoritesArray);
        recyclerView.setAdapter(adapter);

        return v;
    }

    public MyAdapter getAdapter() {
        return adapter;
    }

    String[] sortStopCodesByNameAndDirection(HashSet<String> favoritesSet) {
        SQLiteDatabase database = new CTAHelper(getContext()).getReadableDatabase();
        // Get a list of all stops, sorted by stop name and direction (stop_desc includes both)
        Cursor query = database.query("stops", new String[] {"stop_code"}, null, null, null, null, "stop_desc");
        query.moveToNext();

        String[] favoritesArray = new String[favoritesSet.size()];

        // Sort list of stop codes by corresponding stop names and directions
        int i = 0;
        while (i < favoritesArray.length) {
            if (favoritesSet.contains(query.getString(0))) {
                favoritesArray[i] = query.getString(0);
                i ++;
            }
            query.moveToNext();
        }
        query.close();

        return favoritesArray;
    }
}