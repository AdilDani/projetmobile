package com.fleettracking.app.chauffeur.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.android.gms.maps.model.MarkerOptions;

public class PositionFragment extends Fragment implements OnMapReadyCallback {

    private MapView mapView;
    private GoogleMap googleMap;
    private Repository repo;

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

        ((TextView) v.findViewById(R.id.text_last_update))
                .setText(getString(R.string.last_update, "10:24:30"));

        mapView = v.findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        map.getUiSettings().setZoomControlsEnabled(true);
        String userId = new Prefs(requireContext()).getUserId();
        repo.getCurrentVehicule(userId, new RepoCallback<Vehicule>() {
            @Override public void onResult(Vehicule vehicule) {
                if (googleMap == null) return;
                LatLng pos = new LatLng(vehicule.lat, vehicule.lng);
                googleMap.addMarker(new MarkerOptions().position(pos).title(vehicule.getNomComplet()));
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 13f));
            }
            @Override public void onError(String message) { /* no marker */ }
        });
    }

    @Override public void onResume() { super.onResume(); if (mapView != null) mapView.onResume(); }
    @Override public void onStart() { super.onStart(); if (mapView != null) mapView.onStart(); }
    @Override public void onStop() { super.onStop(); if (mapView != null) mapView.onStop(); }
    @Override public void onPause() { if (mapView != null) mapView.onPause(); super.onPause(); }
    @Override public void onLowMemory() { super.onLowMemory(); if (mapView != null) mapView.onLowMemory(); }
    @Override public void onDestroyView() { googleMap = null; if (mapView != null) mapView.onDestroy(); super.onDestroyView(); }
    @Override public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mapView != null) mapView.onSaveInstanceState(outState);
    }
}
