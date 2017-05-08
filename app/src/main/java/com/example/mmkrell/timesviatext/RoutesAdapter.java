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

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView textViewRouteID;
        TextView textViewRouteName;

        View itemView;

        ViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            textViewRouteName = (TextView) itemView.findViewById(R.id.favorite_text_view_route_name);
            textViewRouteID = (TextView) itemView.findViewById(R.id.favorite_text_view_route_id);
        }
    }

    RoutesAdapter(RecyclerView recyclerView) {
        database = new CTAHelper(recyclerView.getContext()).getReadableDatabase();
        routes = new ArrayList<>();
        Cursor query = database.rawQuery("SELECT route_id FROM routes ORDER BY route_sequence", null);
        while (query.moveToNext()) {
            routes.add(query.getString(0));
        }
        query.close();
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

        holder.textViewRouteID.setText(routes.get(position));
        holder.textViewRouteName.setText(query.getString(0));

        query.close();
    }

    @Override
    public int getItemCount() {
        return routes.size();
    }
}