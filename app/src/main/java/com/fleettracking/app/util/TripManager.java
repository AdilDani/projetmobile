package com.fleettracking.app.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.os.Looper;

import com.fleettracking.app.data.RepoCallback;
import com.fleettracking.app.data.Repository;
import com.fleettracking.app.model.Trajet;
import com.fleettracking.app.model.Vehicule;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import org.json.JSONArray;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Manages the active trip state and the vehicle's live position.
 *
 * Two GPS callbacks run during a trip:
 *  - Every 2 seconds: accumulate waypoints for the route polyline.
 *  - Every 30 seconds: push the latest position to the backend so the
 *    admin map updates in near-real-time.
 *
 * On stopTrip() the full waypoint list is serialised to JSON and saved
 * as a completed Trajet record in the backend.
 */
public class TripManager {

    private static final long POSITION_INTERVAL = 30_000;
    private static final long WAYPOINT_INTERVAL  =  2_000;

    private static TripManager instance;
    private final Context context;
    private final Repository repo;
    private final FusedLocationProviderClient fused;

    private boolean isTripActive = false;
    private String activeVehicleId;
    private String activeChauffeurId;
    private String activeTrajetId;

    // Accumulated waypoints as [lat, lng] pairs
    private final List<double[]> waypoints = new ArrayList<>();
    private Location firstLocation;
    private String startTime;

    // Slow callback: push vehicle position to backend every 30 s
    private final LocationCallback positionCallback = new LocationCallback() {
        @Override public void onLocationResult(LocationResult result) {
            Location loc = result.getLastLocation();
            if (loc != null && isTripActive && activeVehicleId != null) {
                pushPosition(loc);
            }
        }
    };

    // Fast callback: record a waypoint every 2 s
    private final LocationCallback waypointCallback = new LocationCallback() {
        @Override public void onLocationResult(LocationResult result) {
            Location loc = result.getLastLocation();
            if (loc == null || !isTripActive) return;
            if (firstLocation == null) firstLocation = loc;
            waypoints.add(new double[]{loc.getLatitude(), loc.getLongitude()});
        }
    };

    private TripManager(Context context) {
        this.context = context.getApplicationContext();
        this.repo = new Repository(this.context);
        this.fused = LocationServices.getFusedLocationProviderClient(this.context);
    }

    public static synchronized TripManager getInstance(Context context) {
        if (instance == null) instance = new TripManager(context);
        return instance;
    }

    public boolean isTripActive() { return isTripActive; }

    @SuppressLint("MissingPermission")
    public void startTrip(String vehicleId, String chauffeurId) {
        this.activeVehicleId = vehicleId;
        this.activeChauffeurId = chauffeurId;
        this.isTripActive = true;
        this.waypoints.clear();
        this.firstLocation = null;
        this.startTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
        this.activeTrajetId = null;

        LocationRequest slowReq = new LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY, POSITION_INTERVAL).build();
        fused.requestLocationUpdates(slowReq, positionCallback, Looper.getMainLooper());

        LocationRequest fastReq = new LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY, WAYPOINT_INTERVAL).build();
        fused.requestLocationUpdates(fastReq, waypointCallback, Looper.getMainLooper());

        // Open a trajet record in the backend immediately
        Trajet t = new Trajet();
        t.chauffeurId = chauffeurId;
        t.vehiculeId = vehicleId;
        t.heureDepart = startTime;
        t.date = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
        t.enCours = true;
        repo.createTrajet(t, new RepoCallback<Trajet>() {
            @Override public void onResult(Trajet created) { activeTrajetId = created.id; }
            @Override public void onError(String message) {}
        });
    }

    public void stopTrip() {
        isTripActive = false;
        fused.removeLocationUpdates(positionCallback);
        fused.removeLocationUpdates(waypointCallback);

        if (activeTrajetId == null || waypoints.isEmpty()) {
            activeVehicleId = null;
            activeChauffeurId = null;
            return;
        }

        double[] last = waypoints.get(waypoints.size() - 1);
        double totalKm = computeDistanceKm();

        Trajet t = new Trajet();
        t.chauffeurId = activeChauffeurId;
        t.vehiculeId = activeVehicleId;
        t.date = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
        t.heureDepart = startTime;
        t.heureArrivee = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
        t.enCours = false;
        t.distanceKm = (int) Math.round(totalKm);
        if (firstLocation != null) {
            t.departLat = firstLocation.getLatitude();
            t.departLng = firstLocation.getLongitude();
        }
        t.arriveeLat = last[0];
        t.arriveeLng = last[1];
        t.waypoints = serializeWaypoints();

        final String trajetId = activeTrajetId;
        repo.updateTrajet(trajetId, t, new RepoCallback<Trajet>() {
            @Override public void onResult(Trajet result) {}
            @Override public void onError(String message) {}
        });

        activeVehicleId = null;
        activeChauffeurId = null;
        activeTrajetId = null;
    }

    /** Push the vehicle's current coordinates to the backend. */
    private void pushPosition(Location loc) {
        final String vehId = activeVehicleId;
        repo.getVehicule(vehId, new RepoCallback<Vehicule>() {
            @Override public void onResult(Vehicule v) {
                v.lat = loc.getLatitude();
                v.lng = loc.getLongitude();
                v.vitesse = loc.hasSpeed() ? Math.round(loc.getSpeed() * 3.6f) : 0;
                v.statut = "En mission";
                repo.updateVehicule(v.id, v, new RepoCallback<Vehicule>() {
                    @Override public void onResult(Vehicule x) {}
                    @Override public void onError(String m) {}
                });
            }
            @Override public void onError(String m) {}
        });
    }

    /** Sum of haversine distances between consecutive waypoints, in km. */
    private double computeDistanceKm() {
        double total = 0;
        for (int i = 1; i < waypoints.size(); i++) {
            total += haversine(waypoints.get(i - 1), waypoints.get(i));
        }
        return total;
    }

    private double haversine(double[] a, double[] b) {
        final double R = 6371.0;
        double dLat = Math.toRadians(b[0] - a[0]);
        double dLng = Math.toRadians(b[1] - a[1]);
        double sinLat = Math.sin(dLat / 2);
        double sinLng = Math.sin(dLng / 2);
        double h = sinLat * sinLat
                + Math.cos(Math.toRadians(a[0])) * Math.cos(Math.toRadians(b[0])) * sinLng * sinLng;
        return 2 * R * Math.asin(Math.sqrt(h));
    }

    private String serializeWaypoints() {
        JSONArray arr = new JSONArray();
        for (double[] pt : waypoints) {
            JSONArray pair = new JSONArray();
            pair.put(pt[0]);
            pair.put(pt[1]);
            arr.put(pair);
        }
        return arr.toString();
    }
}
