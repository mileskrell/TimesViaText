package com.example.mmkrell.timesviatext;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class RoutesFragment extends Fragment {

    // Used when clicking on RouteFragment icon
    static String currentAdapterName;

    private PositionSavingRecyclerView positionSavingRecyclerView;

    public RoutesFragment() {
        // Required empty public constructor
    }

    public static RoutesFragment newInstance() {
        return new RoutesFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_routes, container, false);

        positionSavingRecyclerView = (PositionSavingRecyclerView) v.findViewById(R.id.routes_position_saving_recycler_view);
        positionSavingRecyclerView.setHasFixedSize(true);
        positionSavingRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        positionSavingRecyclerView.setAdapter(new RoutesAdapter((NavigationBarActivity) getActivity(), positionSavingRecyclerView));
        currentAdapterName = "RoutesAdapter";

        return v;
    }

    PositionSavingRecyclerView getPositionSavingRecyclerView() {
        return positionSavingRecyclerView;
    }
}
