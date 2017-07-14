package com.example.mmkrell.timesviatext;

import android.content.Context;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

// Used within RoutesFragment so that RoutesAdapter scroll position can be restored
// when navigating from DirectionsAdapter back to RoutesAdapter

public class PositionSavingRecyclerView extends RecyclerView {

    static Parcelable routesAdapterState;

    public PositionSavingRecyclerView(Context context) {
        super(context);
    }

    public PositionSavingRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PositionSavingRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable state = super.onSaveInstanceState();
        if (getAdapter() instanceof RoutesAdapter)
            routesAdapterState = state;
        return state;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        super.onRestoreInstanceState(state);
    }
}
