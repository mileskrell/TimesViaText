package com.example.mmkrell.timesviatext;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.osmdroid.tileprovider.cachemanager.CacheManager;

public class DownloadMapTilesDialogFragment extends DialogFragment implements CacheManager.CacheManagerCallback {

    private ProgressBar progressBar;
    private TextView textViewPercentage;
    private TextView textViewTilesOutOfTotal;

    private int numberOfTiles;

    public DownloadMapTilesDialogFragment() {
        // Required empty public constructor
    }

    public static DownloadMapTilesDialogFragment newInstance() {
        return new DownloadMapTilesDialogFragment();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View v = getActivity().getLayoutInflater().inflate(R.layout.fragment_download_map_tiles_dialog, null);

        progressBar = (ProgressBar) v.findViewById(R.id.progress_bar_download_map_tiles);

        textViewPercentage = (TextView) v.findViewById(R.id.text_view_download_map_tiles_percentage);
        textViewTilesOutOfTotal = (TextView) v.findViewById(R.id.text_view_download_map_tiles_tiles_out_of_total);

        builder.setView(v)
                .setTitle(R.string.downloading_map_tiles)
                // If the dialog is created without a message, the message is never displayed
                .setMessage("")
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ((DownloadMapTilesActivity) getActivity()).getDownloadingTask().cancel(false);
                        dialog.dismiss();
                    }
                });

        return builder.create();
    }

    @Override
    public void onTaskComplete() {

    }

    @Override
    public void updateProgress(int progress, int currentZoomLevel, int zoomMin, int zoomMax) {
        progressBar.setProgress(progress);
        ((AlertDialog) getDialog()).setMessage("Handling zoom level: " + currentZoomLevel + " (from " + zoomMin + " to " + zoomMax + ")");
        int percentage = (int) Math.floor((double) progress / numberOfTiles * 100);
        textViewPercentage.setText(String.valueOf(percentage + "%"));
        textViewTilesOutOfTotal.setText(progress + " out of " + numberOfTiles + " tiles");
    }

    @Override
    public void downloadStarted() {

    }

    @Override
    public void setPossibleTilesInArea(int total) {
        numberOfTiles = total;
        progressBar.setMax(numberOfTiles);
    }

    @Override
    public void onTaskFailed(int errors) {

    }
}