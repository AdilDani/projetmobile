package com.fleettracking.app.util;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.fleettracking.app.data.RepoCallback;
import com.fleettracking.app.data.Repository;
import com.fleettracking.app.model.Vehicule;

/**
 * Manages the active trip state and automatic position updates.
 * When a trip is active, it periodically updates the vehicle's coordinates
 * in the backend.
 */
public class TripManager {

    private static final long UPDATE_INTERVAL = 30000; // 30 seconds

    private static TripManager instance;
    private final Context context;
    private final Repository repo;
    private final Handler handler = new Handler(Looper.getMainLooper());
    
    private boolean isTripActive = false;
    private String activeVehicleId;
    
    private final Runnable updateTask = new Runnable() {
        @Override
        public void run() {
            if (isTripActive && activeVehicleId != null) {
                updatePosition();
                handler.postDelayed(this, UPDATE_INTERVAL);
            }
        }
    };

    private TripManager(Context context) {
        this.context = context.getApplicationContext();
        this.repo = new Repository(this.context);
    }

    public static synchronized TripManager getInstance(Context context) {
        if (instance == null) instance = new TripManager(context);
        return instance;
    }

    public boolean isTripActive() {
        return isTripActive;
    }

    public void startTrip(String vehicleId) {
        this.activeVehicleId = vehicleId;
        this.isTripActive = true;
        handler.post(updateTask);
    }

    public void stopTrip() {
        this.isTripActive = false;
        this.activeVehicleId = null;
        handler.removeCallbacks(updateTask);
    }

    private void updatePosition() {
        repo.getVehicule(activeVehicleId, new RepoCallback<Vehicule>() {
            @Override
            public void onResult(Vehicule v) {
                // Simulate a small movement
                v.lat += (Math.random() - 0.5) * 0.001;
                v.lng += (Math.random() - 0.5) * 0.001;
                v.vitesse = 40 + (int)(Math.random() * 20);
                v.statut = "En mission";
                
                repo.updateVehicule(v.id, v, new RepoCallback<Vehicule>() {
                    @Override public void onResult(Vehicule result) {}
                    @Override public void onError(String message) {}
                });
            }
            @Override public void onError(String message) {}
        });
    }
}
