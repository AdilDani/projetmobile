package com.fleettracking.app.chauffeur.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.fleettracking.app.R;
import com.fleettracking.app.data.RepoCallback;
import com.fleettracking.app.data.Repository;
import com.fleettracking.app.model.Vehicule;
import com.fleettracking.app.util.Prefs;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PositionFragment extends Fragment implements OnMapReadyCallback {

    private static final long POLL_INTERVAL = 30_000;

    private MapView mapView;
    private GoogleMap googleMap;
    private Repository repo;
    private String userId;
    private LatLng lastPosition;
    private Marker currentMarker;
    private TextView textLastUpdate;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable pollRunnable = new Runnable() {
        @Override public void run() {
            refreshPosition();
            handler.postDelayed(this, POLL_INTERVAL);
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chauffeur_position, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        repo = new Repository(requireContext());
        userId = new Prefs(requireContext()).getUserId();
        textLastUpdate = v.findViewById(R.id.text_last_update);

        v.findViewById(R.id.btn_locate).setOnClickListener(x -> {
            if (googleMap != null && lastPosition != null) {
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lastPosition, 15f));
            }
        });

        mapView = v.findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        map.getUiSettings().setZoomControlsEnabled(true);
        refreshPosition();
    }

    private void refreshPosition() {
        if (repo == null || userId == null) return;
        repo.getCurrentVehicule(userId, new RepoCallback<Vehicule>() {
            @Override public void onResult(Vehicule vehicule) {
                if (googleMap == null || !isAdded()) return;
                LatLng pos = new LatLng(vehicule.lat, vehicule.lng);
                lastPosition = pos;
                if (currentMarker == null) {
                    currentMarker = googleMap.addMarker(
                            new MarkerOptions().position(pos).title(vehicule.getNomComplet()));
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 13f));
                } else {
                    currentMarker.setPosition(pos);
                }
                String now = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
                textLastUpdate.setText(getString(R.string.last_update, now));
            }
            @Override public void onError(String message) { /* keep last marker */ }
        });
    }

    @Override public void onResume() {
        super.onResume();
        if (mapView != null) mapView.onResume();
        handler.post(pollRunnable);
    }

    @Override public void onPause() {
        handler.removeCallbacks(pollRunnable);
        if (mapView != null) mapView.onPause();
        super.onPause();
    }

    @Override public void onStart() { super.onStart(); if (mapView != null) mapView.onStart(); }
    @Override public void onStop() { super.onStop(); if (mapView != null) mapView.onStop(); }
    @Override public void onLowMemory() { super.onLowMemory(); if (mapView != null) mapView.onLowMemory(); }
    @Override public void onDestroyView() {
        handler.removeCallbacks(pollRunnable);
        googleMap = null;
        if (mapView != null) mapView.onDestroy();
        super.onDestroyView();
    }
    @Override public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mapView != null) mapView.onSaveInstanceState(outState);
    }
}
