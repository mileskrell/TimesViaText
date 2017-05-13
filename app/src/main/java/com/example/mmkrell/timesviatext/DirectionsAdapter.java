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

class DirectionsAdapter extends RecyclerView.Adapter<DirectionsAdapter.ViewHolder> {

    private final ArrayList<String> directions;
    private final SQLiteDatabase database;

    // This is used in getTitle() and when returning to a DirectionsAdapter
    static String routeId;

    private final NavigationBarActivity navigationBarActivity;
    private final RecyclerView recyclerView;

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView textViewDirection;

        View itemView;

        ViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            textViewDirection = (TextView) itemView.findViewById(R.id.direction_text_view_direction);
        }
    }

    DirectionsAdapter(String routeId, NavigationBarActivity navigationBarActivity, RecyclerView recyclerView) {
        DirectionsAdapter.routeId = routeId;
        this.navigationBarActivity = navigationBarActivity;
        this.recyclerView = recyclerView;

        database = CTAHelper.getDatabaseInstance();
        directions = new ArrayList<>();
        Cursor query = database.rawQuery("SELECT DISTINCT direction " +
                        "FROM route_" + routeId + "_stops ORDER BY direction", null);
        while (query.moveToNext()) {
            directions.add(query.getString(0));
        }
        query.close();
    }

    @Override
    public DirectionsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_direction, parent, false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final String direction = directions.get(position);
        holder.textViewDirection.setText(direction);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recyclerView.setAdapter(new StopsAdapter(routeId, direction, navigationBarActivity));

                // Update our position
                RoutesFragment.currentAdapterName = "StopsAdapter";

                // Update the action bar's title
                navigationBarActivity.updateTitleAndUserLocation("StopsAdapter");
            }
        });
    }

    @Override
    public int getItemCount() {
        return directions.size();
    }

    static String getTitle(Context context) {
        return context.getString(R.string.route) + " " + routeId;
    }
}