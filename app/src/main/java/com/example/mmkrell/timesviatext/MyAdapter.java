package com.example.mmkrell.timesviatext;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

    private String[] favorites;

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView textView;

        ViewHolder(TextView textView) {
            super(textView);
            this.textView = textView;
        }
    }

    MyAdapter(String[] favorites) {
        this.favorites = favorites;
    }

    @Override
    public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        TextView textView = (TextView) LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_1, parent, false);

        return new ViewHolder(textView);
    }

    @Override
    public void onBindViewHolder(MyAdapter.ViewHolder holder, int position) {
        holder.textView.setText(favorites[position]);
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