package com.example.mmkrell.timesviatext;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class RoutesFragment extends Fragment {

    static String currentAdapterName;

    private RecyclerView recyclerView;

    public RoutesFragment() {
        // Required empty public constructor
    }

    public static RoutesFragment newInstance() {
        return new RoutesFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_routes, container, false);

        recyclerView = (RecyclerView) v.findViewById(R.id.routes_recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        recyclerView.setAdapter(new RoutesAdapter((NavigationBarActivity) getActivity(), recyclerView));
        currentAdapterName = "RoutesAdapter";

        return v;
    }

    RecyclerView getRecyclerView() {
        return recyclerView;
    }
}