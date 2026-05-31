package com.fleettracking.app.admin.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.fleettracking.app.R;
import com.fleettracking.app.admin.ProfilActivity;
import com.fleettracking.app.data.RepoCallback;
import com.fleettracking.app.data.Repository;
import com.fleettracking.app.model.Vehicule;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

public class CarteFragment extends Fragment implements OnMapReadyCallback {

    private MapView mapView;
    private GoogleMap googleMap;
    private Repository repo;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_carte, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        repo = new Repository(requireContext());

        ((TextView) v.findViewById(R.id.toolbar_title)).setText(R.string.nav_carte);
        v.findViewById(R.id.btn_profile).setOnClickListener(x ->
                startActivity(new Intent(getContext(), ProfilActivity.class)));

        mapView = v.findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        LatLng center = new LatLng(33.5731, -7.5898);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(center, 12f));
        repo.getVehicules(new RepoCallback<List<Vehicule>>() {
            @Override public void onResult(List<Vehicule> list) {
                if (googleMap == null) return;
                for (Vehicule veh : list) {
                    googleMap.addMarker(new MarkerOptions()
                            .position(new LatLng(veh.lat, veh.lng))
                            .title(veh.getNomComplet())
                            .snippet(veh.immatriculation));
                }
            }
            @Override public void onError(String message) { /* no markers */ }
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
