package com.fleettracking.app.admin.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.fleettracking.app.R;
import com.fleettracking.app.admin.ProfilActivity;
import com.fleettracking.app.admin.VehiculeDetailsActivity;
import com.fleettracking.app.data.RepoCallback;
import com.fleettracking.app.data.Repository;
import com.fleettracking.app.model.Vehicule;
import com.fleettracking.app.util.ImageUtils;
import com.fleettracking.app.util.UiUtils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

public class CarteFragment extends Fragment implements OnMapReadyCallback {

    private static final long POLL_INTERVAL = 30_000;

    private MapView mapView;
    private GoogleMap googleMap;
    private Repository repo;
    private View card;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable pollRunnable = new Runnable() {
        @Override public void run() {
            refreshMarkers();
            handler.postDelayed(this, POLL_INTERVAL);
        }
    };

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

        card = v.findViewById(R.id.card_vehicle);

        mapView = v.findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        LatLng center = new LatLng(33.5731, -7.5898);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(center, 12f));

        map.setOnMarkerClickListener(marker -> {
            Object tag = marker.getTag();
            if (tag instanceof Vehicule) {
                showCard((Vehicule) tag);
                googleMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
                return true;
            }
            return false;
        });
        map.setOnMapClickListener(p -> card.setVisibility(View.GONE));

        refreshMarkers();
    }

    private void refreshMarkers() {
        if (repo == null) return;
        repo.getVehicules(new RepoCallback<List<Vehicule>>() {
            @Override public void onResult(List<Vehicule> list) {
                if (googleMap == null || !isAdded()) return;
                googleMap.clear();
                for (Vehicule veh : list) {
                    Marker m = googleMap.addMarker(new MarkerOptions()
                            .position(new LatLng(veh.lat, veh.lng))
                            .title(veh.getNomComplet())
                            .snippet(veh.immatriculation));
                    if (m != null) m.setTag(veh);
                }
            }
            @Override public void onError(String message) { /* keep existing markers */ }
        });
    }

    /** Populate and reveal the bottom card; a second tap opens the full detail. */
    private void showCard(Vehicule v) {
        ImageUtils.bind(card.findViewById(R.id.card_image), v.photo, R.drawable.ic_truck);
        ((TextView) card.findViewById(R.id.card_name)).setText(v.getNomComplet());
        ((TextView) card.findViewById(R.id.card_plate)).setText(v.immatriculation);
        UiUtils.applyStatusChip(card.findViewById(R.id.card_status), v.statut);
        card.setOnClickListener(x -> {
            Intent i = new Intent(getContext(), VehiculeDetailsActivity.class);
            i.putExtra(VehiculeDetailsActivity.EXTRA_VEHICLE_ID, v.id);
            startActivity(i);
        });
        card.setVisibility(View.VISIBLE);
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
