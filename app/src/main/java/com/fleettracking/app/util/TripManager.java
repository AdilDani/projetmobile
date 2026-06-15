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
    private String activeVehiculeNom;
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

    public void startTrip(String vehicleId, String vehiculeNom, String chauffeurId,
                          Runnable onStarted, Runnable onNotAssigned) {
        repo.getVehicule(vehicleId, new RepoCallback<com.fleettracking.app.model.Vehicule>() {
            @Override public void onResult(com.fleettracking.app.model.Vehicule v) {
                if (!chauffeurId.equals(v.conducteurId)) {
                    if (onNotAssigned != null) onNotAssigned.run();
                } else {
                    doStartTrip(vehicleId, vehiculeNom, chauffeurId);
                    if (onStarted != null) onStarted.run();
                }
            }
            @Override public void onError(String m) {
                // Offline — proceed optimistically
                doStartTrip(vehicleId, vehiculeNom, chauffeurId);
                if (onStarted != null) onStarted.run();
            }
        });
    }

    @SuppressLint("MissingPermission")
    private void doStartTrip(String vehicleId, String vehiculeNom, String chauffeurId) {
        this.activeVehicleId = vehicleId;
        this.activeVehiculeNom = vehiculeNom;
        this.activeChauffeurId = chauffeurId;
        this.isTripActive = true;
        this.waypoints.clear();
        this.firstLocation = null;
        this.startTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
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
        t.vehiculeNom = vehiculeNom;
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

        String endTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
        String duree = computeDuree(startTime, endTime);

        Trajet t = new Trajet();
        t.chauffeurId = activeChauffeurId;
        t.vehiculeId = activeVehicleId;
        t.vehiculeNom = activeVehiculeNom;
        t.date = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
        t.heureDepart = startTime;
        t.heureArrivee = endTime;
        t.duree = duree;
        t.enCours = false;
        t.distanceKm = Math.round(totalKm * 10.0) / 10.0;
        t.vitesseMoyenne = computeAvgSpeed(totalKm, startTime, endTime);
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

        // Reset vehicle status back to Assigné now that the trip is done
        final String finishedVehicleId = activeVehicleId;
        repo.getVehicule(finishedVehicleId, new RepoCallback<com.fleettracking.app.model.Vehicule>() {
            @Override public void onResult(com.fleettracking.app.model.Vehicule v) {
                v.statut = "Assigné";
                repo.updateVehicule(v.id, v, new RepoCallback<com.fleettracking.app.model.Vehicule>() {
                    @Override public void onResult(com.fleettracking.app.model.Vehicule x) {}
                    @Override public void onError(String m) {}
                });
            }
            @Override public void onError(String m) {}
        });

        activeVehicleId = null;
        activeVehiculeNom = null;
        activeChauffeurId = null;
        activeTrajetId = null;
    }

    private String computeDuree(String start, String end) {
        try {
            int startSec = toSeconds(start);
            int endSec   = toSeconds(end);
            int diff = endSec - startSec;
            if (diff < 0) diff += 24 * 3600;
            int h = diff / 3600;
            int m = (diff % 3600) / 60;
            int s = diff % 60;
            if (h > 0) return h + "h " + String.format(Locale.getDefault(), "%02d", m) + "min";
            if (m > 0) return m + "min " + String.format(Locale.getDefault(), "%02d", s) + "s";
            return s + "s";
        } catch (Exception ex) {
            return "--";
        }
    }

    private int computeAvgSpeed(double distanceKm, String start, String end) {
        try {
            int startSec = toSeconds(start);
            int endSec   = toSeconds(end);
            int diff = endSec - startSec;
            if (diff < 0) diff += 24 * 3600;
            if (diff <= 0) return 0;
            return (int) Math.round(distanceKm / (diff / 3600.0));
        } catch (Exception ex) {
            return 0;
        }
    }

    private static int toSeconds(String time) {
        String[] parts = time.split(":");
        int h = Integer.parseInt(parts[0]);
        int m = Integer.parseInt(parts[1]);
        int s = parts.length > 2 ? Integer.parseInt(parts[2]) : 0;
        return h * 3600 + m * 60 + s;
    }

    /** Push the vehicle's current coordinates to the backend. */
    private void pushPosition(Location loc) {
        final String vehId = activeVehicleId;
        repo.getVehicule(vehId, new RepoCallback<Vehicule>() {
            @Override public void onResult(Vehicule v) {
                v.lat = loc.getLatitude();
                v.lng = loc.getLongitude();
                v.vitesse = loc.hasSpeed() ? Math.round(loc.getSpeed() * 3.6f) : 0;
                v.statut = "En trajet";
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
            try {
                pair.put(pt[0]);
                pair.put(pt[1]);
            } catch (org.json.JSONException ignored) {}
            arr.put(pair);
        }
        return arr.toString();
    }
}
