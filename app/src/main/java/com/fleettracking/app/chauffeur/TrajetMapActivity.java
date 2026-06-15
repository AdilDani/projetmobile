package com.fleettracking.app.chauffeur;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.fleettracking.app.R;
import com.fleettracking.app.model.Trajet;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
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
                .icon(createPinBitmap(0xFF2E7D32, R.drawable.ic_flag)));   // deep green + flag
        map.addMarker(new MarkerOptions()
                .position(arrivee)
                .title(trajet.arrivee != null ? trajet.arrivee : "Arrivée")
                .icon(createPinBitmap(0xFFC62828, R.drawable.ic_stop)));   // deep red + stop

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

    /** Draws a teardrop-shaped map pin with a white icon inside. */
    private BitmapDescriptor createPinBitmap(int bgColor, int iconRes) {
        float density = getResources().getDisplayMetrics().density;
        int diameter = (int) (48 * density);
        int tail     = (int) (16 * density);
        int width    = diameter;
        int height   = diameter + tail;

        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);
        float cx = width / 2f;
        float cy = diameter / 2f;
        float r  = cy - 2 * density;

        // drop shadow
        Paint shadow = new Paint(Paint.ANTI_ALIAS_FLAG);
        shadow.setColor(0x40000000);
        canvas.drawCircle(cx + density, cy + density, r, shadow);

        // circle body
        Paint body = new Paint(Paint.ANTI_ALIAS_FLAG);
        body.setColor(bgColor);
        canvas.drawCircle(cx, cy, r, body);

        // white ring border
        Paint ring = new Paint(Paint.ANTI_ALIAS_FLAG);
        ring.setColor(Color.WHITE);
        ring.setStyle(Paint.Style.STROKE);
        ring.setStrokeWidth(2.5f * density);
        canvas.drawCircle(cx, cy, r - density, ring);

        // triangular tail
        float tw = 7 * density;
        float tipY = height - density;
        float baseY = diameter - 5 * density;
        Path tailPath = new Path();
        tailPath.moveTo(cx - tw, baseY);
        tailPath.lineTo(cx + tw, baseY);
        tailPath.lineTo(cx, tipY);
        tailPath.close();
        canvas.drawPath(tailPath, body);

        // icon centred in the circle
        Drawable icon = ContextCompat.getDrawable(this, iconRes);
        if (icon != null) {
            int pad = (int) (12 * density);
            icon.setBounds(pad, pad, width - pad, diameter - pad);
            Drawable wrappedIcon = DrawableCompat.wrap(icon.mutate());
            DrawableCompat.setTint(wrappedIcon, Color.WHITE);
            wrappedIcon.draw(canvas);
        }

        return BitmapDescriptorFactory.fromBitmap(bmp);
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
