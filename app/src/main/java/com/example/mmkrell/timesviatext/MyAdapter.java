package com.example.mmkrell.timesviatext;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

    private String[] favorites;
    private SQLiteDatabase database;

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView textViewName;
        TextView textViewDirection;

        ViewHolder(View itemView) {
            super(itemView);
            textViewName = (TextView) itemView.findViewById(R.id.favorite_text_view_name);
            textViewDirection = (TextView) itemView.findViewById(R.id.favorite_text_view_direction);
        }
    }

    MyAdapter(Context context, String[] favorites) {
        this.favorites = favorites;
        database = new CTAHelper(context).getReadableDatabase();
    }

    @Override
    public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_favorite, parent, false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyAdapter.ViewHolder holder, int position) {
        Cursor query = database.query("stops", new String[] {"stop_name", "stop_desc"},
                "stop_code = ?", new String[] {favorites[position]}, null, null, null);
        query.moveToNext();

        holder.textViewName.setText(query.getString(0));
        holder.textViewDirection.setText(getDirection(query.getString(0), query.getString(1)));

        query.close();
    }

    @Override
    public int getItemCount() {
        return favorites.length;
    }

    // Modified from code in StopFragment
    private String getDirection(String stopName, String stopDesc) {
        if (stopDesc.isEmpty()) {
            return null;
        } else {
            int startPos = stopName.length() + 2;
            int endPos = stopDesc.indexOf(",", startPos);
            return stopDesc.substring(startPos, endPos);
        }
    }

    void swap(String[] newFavorites) {
        favorites = newFavorites;
        notifyDataSetChanged();
    }
}