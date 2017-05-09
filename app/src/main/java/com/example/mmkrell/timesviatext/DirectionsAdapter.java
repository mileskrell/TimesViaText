package com.example.mmkrell.timesviatext;

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
    private final String routeId;

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView textViewDirection;

        View itemView;

        ViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            textViewDirection = (TextView) itemView.findViewById(R.id.direction_text_view_direction);
        }
    }

    DirectionsAdapter(String routeId) {
        database = CTAHelper.getDatabaseInstance();
        directions = new ArrayList<>();
        Cursor query = database.rawQuery("SELECT DISTINCT direction_id " +
                        "FROM route_" + routeId + "_stop_list ORDER BY direction_id", null);
        while (query.moveToNext()) {
            directions.add(query.getString(0));
        }
        query.close();
        this.routeId = routeId;
    }

    @Override
    public DirectionsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_direction, parent, false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        Cursor query = database.rawQuery("SELECT route_direction_" + position + " FROM routes " +
                        "WHERE route_id = '" + routeId + "'", null);
        query.moveToNext();

        holder.textViewDirection.setText(query.getString(0));

        query.close();
    }

    @Override
    public int getItemCount() {
        return directions.size();
    }
}