package com.example.mmkrell.timesviatext;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class RoutesFragment extends Fragment {

    // Used when clicking on RouteFragment icon
    static String currentAdapterName;

    private RecyclerView routesRecyclerView;
    private Parcelable routesRecyclerViewState;

    public RoutesFragment() {
        // Required empty public constructor
    }

    public static RoutesFragment newInstance() {
        return new RoutesFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_routes, container, false);

        routesRecyclerView = (RecyclerView) v.findViewById(R.id.routes_recycler_view);
        routesRecyclerView.setHasFixedSize(true);
        routesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        routesRecyclerView.setAdapter(new RoutesAdapter((NavigationBarActivity) getActivity(), this));
        currentAdapterName = "RoutesAdapter";

        return v;
    }

    RecyclerView getRoutesRecyclerView() {
        return routesRecyclerView;
    }

    void saveRoutesRecyclerViewState() {
        routesRecyclerViewState = routesRecyclerView.getLayoutManager().onSaveInstanceState();
    }

    void restoreRoutesRecyclerViewState() {
        routesRecyclerView.getLayoutManager().onRestoreInstanceState(routesRecyclerViewState);
    }
}
