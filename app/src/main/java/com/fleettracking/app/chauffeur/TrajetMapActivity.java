package com.fleettracking.app.chauffeur;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.fleettracking.app.R;
import com.fleettracking.app.model.Trajet;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

public class TrajetMapActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static final String EXTRA_TRAJET = "extra_trajet_json";

    private MapView mapView;
    private Trajet trajet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trajet_map);

        String json = getIntent().getStringExtra(EXTRA_TRAJET);
        trajet = new Gson().fromJson(json, Trajet.class);

        ((TextView) findViewById(R.id.toolbar_title)).setText(
                trajet.date != null ? trajet.date : getString(R.string.tab_trips));
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        TextView stats = findViewById(R.id.text_stats);
        stats.setText(trajet.distanceKm + " km  ·  " + trajet.duree
                + "  ·  " + trajet.vitesseMoyenne + " km/h moy.");

        mapView = findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        map.getUiSettings().setZoomControlsEnabled(true);

        List<LatLng> points = parseWaypoints();

        LatLng depart  = new LatLng(trajet.departLat,  trajet.departLng);
        LatLng arrivee = new LatLng(trajet.arriveeLat, trajet.arriveeLng);

        map.addMarker(new MarkerOptions()
                .position(depart)
                .title(trajet.depart != null ? trajet.depart : "Départ")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        map.addMarker(new MarkerOptions()
                .position(arrivee)
                .title(trajet.arrivee != null ? trajet.arrivee : "Arrivée")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

        if (points.size() >= 2) {
            map.addPolyline(new PolylineOptions()
                    .addAll(points)
                    .width(8f)
                    .color(0xFF1565C0));
        } else {
            // Fall back to a straight line between start and end
            map.addPolyline(new PolylineOptions()
                    .add(depart, arrivee)
                    .width(8f)
                    .color(0xFF1565C0));
        }

        LatLngBounds.Builder bounds = new LatLngBounds.Builder();
        for (LatLng p : points.isEmpty() ? List.of(depart, arrivee) : points) bounds.include(p);
        map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 100));
    }

    private List<LatLng> parseWaypoints() {
        if (trajet.waypoints == null || trajet.waypoints.isEmpty()) return new ArrayList<>();
        try {
            List<List<Double>> raw = new Gson().fromJson(trajet.waypoints,
                    new TypeToken<List<List<Double>>>(){}.getType());
            List<LatLng> result = new ArrayList<>();
            for (List<Double> pair : raw) {
                if (pair.size() >= 2) result.add(new LatLng(pair.get(0), pair.get(1)));
            }
            return result;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    @Override public void onResume()  { super.onResume();  if (mapView != null) mapView.onResume(); }
    @Override public void onStart()   { super.onStart();   if (mapView != null) mapView.onStart(); }
    @Override public void onStop()    { super.onStop();    if (mapView != null) mapView.onStop(); }
    @Override public void onPause()   { if (mapView != null) mapView.onPause(); super.onPause(); }
    @Override public void onLowMemory() { super.onLowMemory(); if (mapView != null) mapView.onLowMemory(); }
    @Override public void onDestroy() { if (mapView != null) mapView.onDestroy(); super.onDestroy(); }
    @Override public void onSaveInstanceState(@NonNull Bundle out) {
        super.onSaveInstanceState(out);
        if (mapView != null) mapView.onSaveInstanceState(out);
    }
}
