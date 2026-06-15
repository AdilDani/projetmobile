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
import com.fleettracking.app.util.FleetConfig;
import com.fleettracking.app.util.ImageUtils;
import com.fleettracking.app.util.UiUtils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import java.util.List;

public class CarteFragment extends Fragment implements OnMapReadyCallback {

    private static final long POLL_INTERVAL = 3_000;

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

        // Default center: Casablanca. If launched from a vehicle detail, focus on that vehicle.
        Bundle args = getArguments();
        LatLng center;
        float zoom;
        if (args != null && (args.getDouble("extra_focus_lat") != 0 || args.getDouble("extra_focus_lng") != 0)) {
            center = new LatLng(args.getDouble("extra_focus_lat"), args.getDouble("extra_focus_lng"));
            zoom = 16f;
        } else {
            center = new LatLng(33.5731, -7.5898);
            zoom = 12f;
        }
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(center, zoom));

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

    private static final LatLng DEPOT = new LatLng(FleetConfig.DEPOT_LAT, FleetConfig.DEPOT_LNG);

    private void refreshMarkers() {
        if (repo == null) return;
        repo.getVehicules(new RepoCallback<List<Vehicule>>() {
            @Override public void onResult(List<Vehicule> list) {
                if (googleMap == null || !isAdded()) return;
                googleMap.clear();

                // Permanent depot / warehouse pin
                Marker depot = googleMap.addMarker(new MarkerOptions()
                        .position(DEPOT)
                        .title(getString(R.string.depot_name))
                        .icon(depotIcon())
                        .zIndex(0f));

                for (Vehicule veh : list) {
                    // Fall back to depot when position has never been set
                    boolean hasPosition = veh.lat != 0.0 || veh.lng != 0.0;
                    LatLng pos = hasPosition ? new LatLng(veh.lat, veh.lng) : DEPOT;

                    BitmapDescriptor icon = vehicleIcon(veh.statut);
                    Marker m = googleMap.addMarker(new MarkerOptions()
                            .position(pos)
                            .title(veh.getNomComplet())
                            .snippet(veh.immatriculation + (hasPosition ? "" : " — dépôt"))
                            .icon(icon)
                            .zIndex(1f));
                    if (m != null) m.setTag(veh);
                }
            }
            @Override public void onError(String message) { /* keep existing markers */ }
        });
    }

    /** Orange pin for "En mission", grey for unavailable, blue for available. */
    private BitmapDescriptor vehicleIcon(String statut) {
        int color;
        if ("En mission".equals(statut))       color = 0xFFE65100;  // deep orange
        else if ("Indisponible".equals(statut)) color = 0xFF757575;  // grey
        else if ("Maintenance".equals(statut))  color = 0xFF6A1B9A;  // purple
        else                                    color = 0xFF1565C0;  // blue = available
        return pinBitmap(color, R.drawable.ic_truck);
    }

    /** Dark teal diamond-shaped depot marker with warehouse icon. */
    private BitmapDescriptor depotIcon() {
        return pinBitmap(0xFF00695C, R.drawable.ic_warehouse);
    }

    private BitmapDescriptor pinBitmap(int bgColor, int iconRes) {
        float density = requireContext().getResources().getDisplayMetrics().density;
        int diameter = (int)(44 * density);
        int tail     = (int)(14 * density);
        Bitmap bmp = Bitmap.createBitmap(diameter, diameter + tail, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);
        float cx = diameter / 2f, cy = diameter / 2f, r = cy - 2 * density;

        Paint body = new Paint(Paint.ANTI_ALIAS_FLAG);
        body.setColor(bgColor);
        canvas.drawCircle(cx, cy, r, body);

        Paint ring = new Paint(Paint.ANTI_ALIAS_FLAG);
        ring.setColor(Color.WHITE); ring.setStyle(Paint.Style.STROKE);
        ring.setStrokeWidth(2 * density);
        canvas.drawCircle(cx, cy, r - density, ring);

        Path tailPath = new Path();
        float tw = 6 * density;
        tailPath.moveTo(cx - tw, diameter - 4 * density);
        tailPath.lineTo(cx + tw, diameter - 4 * density);
        tailPath.lineTo(cx, diameter + tail - density);
        tailPath.close();
        canvas.drawPath(tailPath, body);

        Drawable icon = ContextCompat.getDrawable(requireContext(), iconRes);
        if (icon != null) {
            int pad = (int)(11 * density);
            icon.setBounds(pad, pad, diameter - pad, diameter - pad);
            DrawableCompat.setTint(DrawableCompat.wrap(icon.mutate()), Color.WHITE);
            icon.draw(canvas);
        }
        return BitmapDescriptorFactory.fromBitmap(bmp);
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
