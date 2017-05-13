package com.example.mmkrell.timesviatext;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

class RoutesAdapter extends RecyclerView.Adapter<RoutesAdapter.ViewHolder> {

    private final ArrayList<String> routeIds;
    private final SQLiteDatabase database;
    private final NavigationBarActivity navigationBarActivity;
    private final PositionSavingRecyclerView positionSavingRecyclerView;

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView textViewRouteId;
        TextView textViewRouteName;

        View itemView;

        ViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            textViewRouteName = (TextView) itemView.findViewById(R.id.route_text_view_route_name);
            textViewRouteId = (TextView) itemView.findViewById(R.id.route_text_view_route_id);
        }
    }

    RoutesAdapter(NavigationBarActivity navigationBarActivity,
                  PositionSavingRecyclerView positionSavingRecyclerView) {
        database = CTAHelper.getDatabaseInstance();
        routeIds = new ArrayList<>();
        Cursor query = database.rawQuery("SELECT route_id FROM routes ORDER BY route_sequence", null);
        while (query.moveToNext()) {
            routeIds.add(query.getString(0));
        }
        query.close();
        this.navigationBarActivity = navigationBarActivity;
        this.positionSavingRecyclerView = positionSavingRecyclerView;
    }

    @Override
    public RoutesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_route, parent, false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        Cursor query = database.rawQuery("SELECT route_long_name FROM routes " +
                "WHERE route_id = ?", new String[] {routeIds.get(position)});
        query.moveToNext();

        final String routeId = routeIds.get(position);
        String routeName = query.getString(0);
        holder.textViewRouteId.setText(routeId);
        holder.textViewRouteName.setText(routeName);

        query.close();

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Save position of RecyclerView
                positionSavingRecyclerView.onSaveInstanceState();

                // Switch to a DirectionsAdapter
                positionSavingRecyclerView.setAdapter(new DirectionsAdapter(routeId,
                        navigationBarActivity, positionSavingRecyclerView));

                // Update currentAdapterName
                RoutesFragment.currentAdapterName = "DirectionsAdapter";

                // Update the action bar's title
                navigationBarActivity.updateTitleAndUserLocation("DirectionsAdapter");
            }
        });
    }

    @Override
    public int getItemCount() {
        return routeIds.size();
    }
}