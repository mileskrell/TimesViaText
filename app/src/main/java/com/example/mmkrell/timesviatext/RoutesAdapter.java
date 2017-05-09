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

    private final ArrayList<String> routes;
    private final SQLiteDatabase database;
    private final NavigationBarActivity navigationBarActivity;
    private final RecyclerView recyclerView;

    // Used as the title of NavigationBarActivity while this adapter is visible
    static String selectedRouteTitle;

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView textViewRouteID;
        TextView textViewRouteName;

        View itemView;

        ViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            textViewRouteName = (TextView) itemView.findViewById(R.id.route_text_view_route_name);
            textViewRouteID = (TextView) itemView.findViewById(R.id.route_text_view_route_id);
        }
    }

    RoutesAdapter(NavigationBarActivity navigationBarActivity, RecyclerView recyclerView) {
        database = CTAHelper.getDatabaseInstance();
        routes = new ArrayList<>();
        Cursor query = database.rawQuery("SELECT route_id FROM routes ORDER BY route_sequence", null);
        while (query.moveToNext()) {
            routes.add(query.getString(0));
        }
        query.close();
        this.navigationBarActivity = navigationBarActivity;
        this.recyclerView = recyclerView;
    }

    @Override
    public RoutesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_route, parent, false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        Cursor query = database.rawQuery("SELECT route_long_name FROM routes WHERE route_id = ?",
                new String[] {routes.get(position)});
        query.moveToNext();

        final String routeID = routes.get(position);
        String routeName = query.getString(0);
        holder.textViewRouteID.setText(routeID);
        holder.textViewRouteName.setText(routeName);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recyclerView.setAdapter(new DirectionsAdapter(routeID));

                // Update our position, as stored in the global variables
                // TODO: Do we really need both of these variables?
                RoutesFragment.currentAdapterName = "DirectionsAdapter";
                NavigationBarActivity.userLocation = RoutesFragment.currentAdapterName;

                // Modify the action bar to reflect where we are
                navigationBarActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                selectedRouteTitle = navigationBarActivity
                        .getString(R.string.title_directions_route) + " " + routeID;
                navigationBarActivity.getSupportActionBar().setTitle(selectedRouteTitle);

            }
        });

        query.close();
    }

    @Override
    public int getItemCount() {
        return routes.size();
    }
}