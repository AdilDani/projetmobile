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
import com.fleettracking.app.admin.AdminMainActivity;
import com.fleettracking.app.admin.EntretiensActivity;
import com.fleettracking.app.admin.IncidentsActivity;
import com.fleettracking.app.admin.ProfilActivity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.fleettracking.app.data.RepoCallback;
import com.fleettracking.app.data.Repository;
import com.fleettracking.app.data.Stats;
import com.fleettracking.app.model.Vehicule;
import com.fleettracking.app.util.FleetConfig;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

public class AdminAccueilFragment extends Fragment implements OnMapReadyCallback {

    private MapView mapView;
    private GoogleMap googleMap;
    private Repository repo;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_accueil, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        repo = new Repository(requireContext());

        ((TextView) v.findViewById(R.id.toolbar_title)).setText(R.string.nav_accueil);
        v.findViewById(R.id.btn_profile).setOnClickListener(x ->
                startActivity(new Intent(getContext(), ProfilActivity.class)));

        loadStats(v);

        AdminMainActivity host = (AdminMainActivity) requireActivity();
        v.findViewById(R.id.stat_vehicles).setOnClickListener(x -> host.goToVehicules());
        v.findViewById(R.id.stat_drivers).setOnClickListener(x -> host.goToChauffeurs());
        v.findViewById(R.id.stat_incidents).setOnClickListener(x ->
                startActivity(new Intent(getContext(), IncidentsActivity.class)));
        v.findViewById(R.id.stat_maintenance).setOnClickListener(x ->
                startActivity(new Intent(getContext(), EntretiensActivity.class)));
        v.findViewById(R.id.btn_see_all_map).setOnClickListener(x -> host.goToCarte());

        mapView = v.findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }

    private void loadStats(@NonNull View v) {
        repo.getStats(new RepoCallback<Stats>() {
            @Override public void onResult(Stats s) {
                if (!isAdded()) return;
                ((TextView) v.findViewById(R.id.count_vehicles)).setText(String.valueOf(s.vehicules));
                ((TextView) v.findViewById(R.id.count_drivers)).setText(String.valueOf(s.chauffeurs));
                ((TextView) v.findViewById(R.id.count_incidents)).setText(String.valueOf(s.incidents));
                ((TextView) v.findViewById(R.id.count_maintenance)).setText(String.valueOf(s.entretiens));
            }
            @Override public void onError(String message) { /* keep placeholders */ }
        });
    }

    private static final LatLng DEPOT = new LatLng(FleetConfig.DEPOT_LAT, FleetConfig.DEPOT_LNG);

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(DEPOT, 12f));

        // Permanent depot pin
        map.addMarker(new MarkerOptions()
                .position(DEPOT)
                .title(getString(R.string.depot_name))
                .icon(pinBitmap(0xFF00695C, R.drawable.ic_warehouse))
                .zIndex(0f));

        repo.getVehicules(new RepoCallback<List<Vehicule>>() {
            @Override public void onResult(List<Vehicule> list) {
                if (googleMap == null) return;
                for (Vehicule veh : list) {
                    boolean hasPos = veh.lat != 0.0 || veh.lng != 0.0;
                    LatLng pos = hasPos ? new LatLng(veh.lat, veh.lng) : DEPOT;
                    googleMap.addMarker(new MarkerOptions()
                            .position(pos)
                            .title(veh.getNomComplet())
                            .snippet(veh.immatriculation)
                            .icon(pinBitmap(vehicleColor(veh.statut), R.drawable.ic_truck))
                            .zIndex(1f));
                }
            }
            @Override public void onError(String message) { /* no markers */ }
        });
    }

    private int vehicleColor(String statut) {
        if ("En mission".equals(statut))       return 0xFFE65100;
        if ("Indisponible".equals(statut))     return 0xFF757575;
        if ("Maintenance".equals(statut))      return 0xFF6A1B9A;
        return 0xFF1565C0;
    }

    private BitmapDescriptor pinBitmap(int bgColor, int iconRes) {
        float d = getResources().getDisplayMetrics().density;
        int diam = (int)(44 * d), tail = (int)(14 * d);
        Bitmap bmp = Bitmap.createBitmap(diam, diam + tail, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);
        float cx = diam / 2f, cy = diam / 2f, r = cy - 2 * d;
        Paint body = new Paint(Paint.ANTI_ALIAS_FLAG); body.setColor(bgColor);
        canvas.drawCircle(cx, cy, r, body);
        Paint ring = new Paint(Paint.ANTI_ALIAS_FLAG);
        ring.setColor(Color.WHITE); ring.setStyle(Paint.Style.STROKE); ring.setStrokeWidth(2 * d);
        canvas.drawCircle(cx, cy, r - d, ring);
        Path tp = new Path(); float tw = 6 * d;
        tp.moveTo(cx - tw, diam - 4 * d); tp.lineTo(cx + tw, diam - 4 * d);
        tp.lineTo(cx, diam + tail - d); tp.close();
        canvas.drawPath(tp, body);
        Drawable icon = ContextCompat.getDrawable(requireContext(), iconRes);
        if (icon != null) {
            int pad = (int)(11 * d);
            icon.setBounds(pad, pad, diam - pad, diam - pad);
            DrawableCompat.setTint(DrawableCompat.wrap(icon.mutate()), Color.WHITE);
            icon.draw(canvas);
        }
        return BitmapDescriptorFactory.fromBitmap(bmp);
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
