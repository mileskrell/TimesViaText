package com.example.mmkrell.timesviatext;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

class FavoritesAdapter extends RecyclerView.Adapter<FavoritesAdapter.ViewHolder> {

    private String[] favorites;
    private final NavigationBarActivity navigationBarActivity;
    private final MapFragment mapFragment;
    private final SQLiteDatabase database;

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView textViewStopName;
        TextView textViewStopDirection;

        View itemView;

        ViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            textViewStopName = (TextView) itemView.findViewById(R.id.favorite_text_view_stop_name);
            textViewStopDirection = (TextView) itemView.findViewById(R.id.favorite_text_view_stop_direction);
        }
    }

    FavoritesAdapter(NavigationBarActivity navigationBarActivity, String[] favorites) {
        this.favorites = favorites;
        this.navigationBarActivity = navigationBarActivity;
        this.mapFragment = (MapFragment) navigationBarActivity.getSupportFragmentManager().findFragmentByTag("map_fragment");
        database = new CTAHelper(navigationBarActivity).getReadableDatabase();
    }

    @Override
    public FavoritesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_favorite, parent, false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final FavoritesAdapter.ViewHolder holder, int position) {
        Cursor query = database.rawQuery("SELECT stop_name, stop_dir FROM stops WHERE stop_id = ?",
                new String[] {favorites[position]});
        query.moveToNext();

        holder.textViewStopName.setText(query.getString(0));

        String stopDirection = query.getString(1);
        if (stopDirection.isEmpty())
            holder.textViewStopDirection.setVisibility(View.GONE);
        else
            holder.textViewStopDirection.setText(stopDirection);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mapFragment.disableFollowMe();

                navigationBarActivity.getBottomNavigationView().setSelectedItemId(R.id.navigation_map);

                mapFragment.deselectMarkerAndRemoveStopFragment(false);
                mapFragment.selectMarkerAndAddStopFragment(favorites[holder.getAdapterPosition()]);

                mapFragment.animateToMarker(favorites[holder.getAdapterPosition()]);
            }
        });

        query.close();
    }

    @Override
    public int getItemCount() {
        return favorites.length;
    }

    void swap(String[] newFavorites) {
        favorites = newFavorites;
        notifyDataSetChanged();
    }
}