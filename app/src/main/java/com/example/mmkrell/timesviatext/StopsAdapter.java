package com.example.mmkrell.timesviatext;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

class StopsAdapter extends RecyclerView.Adapter<StopsAdapter.ViewHolder> {

    private final ArrayList<Integer> stopIds;
    private final SQLiteDatabase database;
    private final NavigationBarActivity navigationBarActivity;

    private static String direction;

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView textViewStopName;

        View itemView;

        ViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            textViewStopName = (TextView) itemView.findViewById(R.id.stop_text_view_stop_name);
        }
    }

    StopsAdapter(String routeId, String direction, NavigationBarActivity navigationBarActivity) {
        StopsAdapter.direction = direction;
        this.navigationBarActivity = navigationBarActivity;

        database = CTAHelper.getDatabaseInstance();
        stopIds = new ArrayList<>();
        Cursor query = database.rawQuery("SELECT stop_id " +
                "FROM stops NATURAL JOIN route_" + routeId + "_stops " +
                "WHERE direction = '" + direction + "' ORDER BY stop_name", null);
        while (query.moveToNext()) {
            stopIds.add(query.getInt(0));
        }
        query.close();
    }

    @Override
    public StopsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_stop, parent, false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final int stopId = stopIds.get(position);

        Cursor query = database.rawQuery("SELECT stop_name FROM stops " +
                "WHERE stop_id = " + stopId, null);
        query.moveToNext();
        holder.textViewStopName.setText(query.getString(0));

        query.close();

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MapFragment mapFragment = (MapFragment) navigationBarActivity
                        .getSupportFragmentManager().findFragmentByTag("map_fragment");
                mapFragment.setFollowMeState(false);

                navigationBarActivity.getBottomNavigationView().setSelectedItemId(R.id.navigation_map);

                mapFragment.deselectMarkerAndRemoveStopFragment(false);
                mapFragment.selectMarkerAndAddStopFragment(stopId);

                mapFragment.animateToMarker(stopId);
            }
        });
    }

    @Override
    public int getItemCount() {
        return stopIds.size();
    }

    static String getTitle(Context context) {
        return DirectionsAdapter.getTitle(context) + " (" + direction + ")";
    }
}