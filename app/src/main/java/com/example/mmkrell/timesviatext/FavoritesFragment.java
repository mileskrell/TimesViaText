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

import java.util.ArrayList;
import java.util.HashSet;

public class FavoritesFragment extends Fragment {

    private FavoritesAdapter adapter;
    private SharedPreferences sharedPreferences;

    public FavoritesFragment() {
        // Required empty public constructor
    }

    public static FavoritesFragment newInstance() {
        return new FavoritesFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_favorites, container, false);

        RecyclerView recyclerView = (RecyclerView) v.findViewById(R.id.favorites_recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        HashSet<String> favoritesSet = new HashSet<>(sharedPreferences.getStringSet("favorites", new HashSet<String>()));

        ArrayList<Integer> favoritesArrayList = sortStopIdsByNameAndDirection(favoritesSet);

        adapter = new FavoritesAdapter((NavigationBarActivity) getActivity(), favoritesArrayList);
        recyclerView.setAdapter(adapter);

        return v;
    }

    FavoritesAdapter getAdapter() {
        return adapter;
    }

    ArrayList<Integer> sortStopIdsByNameAndDirection(HashSet<String> favoritesSet) {
        SQLiteDatabase database = CTAHelper.getDatabaseInstance();
        // Get a list of all stops, sorted by stop name and direction
        Cursor query = database.rawQuery("SELECT stop_id FROM stops ORDER BY stop_name || stop_dir",
                null);
        query.moveToNext();

        ArrayList<String> favoritesArrayList = new ArrayList<>(favoritesSet.size());

        // Sort list of stop codes by corresponding stop names and directions
        int i = 0;
        while (i < favoritesSet.size()) {
            if (favoritesSet.contains(query.getString(0))) {
                favoritesArrayList.add(query.getString(0));
                i ++;
            }
            if (! query.moveToNext()) {
                // At this point, we've looked at every stop and added every one that's a favorite.
                // However, we still haven't added as many as we should have added.
                // This means that one or more of our favorites were never found in the database;
                // maybe the stops used to exist but don't anymore.

                // We'll replace the stored favorites with our list of "verified" favorites
                // so this doesn't happen again.
                sharedPreferences.edit()
                        .putStringSet("favorites", new HashSet<>(favoritesArrayList))
                        .apply();
                break;
            }
        }
        query.close();

        // Android wants to store HashSets of Strings, but we really just want to store ints.
        // Within this method, we deal with the favorites like they're Strings,
        // but we turn them into ints before passing them anywhere else.

        ArrayList<Integer> convertedFavoritesArrayList = new ArrayList<>(favoritesArrayList.size());
        for (i = 0; i < favoritesArrayList.size(); i ++) {
            convertedFavoritesArrayList.add(Integer.parseInt(favoritesArrayList.get(i)));
        }

        return convertedFavoritesArrayList;
    }
}