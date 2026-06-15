package com.fleettracking.app.util;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.fleettracking.app.R;
import com.fleettracking.app.data.ApiClient;
import com.fleettracking.app.data.ApiService;
import com.fleettracking.app.model.Entretien;
import com.fleettracking.app.model.Vehicule;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class EntretienNotificationWorker extends Worker {

    public EntretienNotificationWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context ctx = getApplicationContext();
        Prefs prefs = new Prefs(ctx);
        if (!prefs.isLoggedIn()) return Result.success();

        String role = prefs.getRole();
        String userId = prefs.getUserId();

        try {
            ApiService api = ApiClient.get();

            List<Vehicule> vehicules = api.getVehicules().execute().body();
            List<Entretien> entretiens = api.getEntretiens().execute().body();
            if (vehicules == null || entretiens == null) return Result.success();

            Map<String, Vehicule> vehiculeMap = new HashMap<>();
            String myVehiculeId = null;
            for (Vehicule v : vehicules) {
                vehiculeMap.put(v.id, v);
                if (Prefs.ROLE_CHAUFFEUR.equals(role) && userId.equals(v.conducteurId)) {
                    myVehiculeId = v.id;
                }
            }

            NotificationHelper.ensureMaintenanceChannel(ctx);

            for (Entretien e : entretiens) {
                if (!"aVenir".equals(e.statut)) continue;
                if (Prefs.ROLE_CHAUFFEUR.equals(role) && !e.vehiculeId.equals(myVehiculeId)) continue;

                Vehicule v = vehiculeMap.get(e.vehiculeId);
                if (v == null) continue;

                String vehicleLabel = v.getNomComplet();
                String typeLabel = e.type != null ? e.type : "Entretien";

                if (e.estKmBase) {
                    int remaining = e.cibleKm - v.kilometrage;
                    if (remaining <= 0) {
                        String text = ctx.getString(R.string.notif_maintenance_overdue, typeLabel, vehicleLabel);
                        NotificationHelper.notifyMaintenance(ctx, text, e.vehiculeId);
                    } else if (remaining <= 500) {
                        String text = ctx.getString(R.string.notif_maintenance_soon, typeLabel, vehicleLabel);
                        NotificationHelper.notifyMaintenance(ctx, text, e.vehiculeId);
                    }
                } else {
                    int days = daysUntil(e.cibleDate);
                    if (days <= 0) {
                        String text = ctx.getString(R.string.notif_maintenance_overdue, typeLabel, vehicleLabel);
                        NotificationHelper.notifyMaintenance(ctx, text, e.vehiculeId);
                    } else if (days <= 7) {
                        String text = ctx.getString(R.string.notif_maintenance_soon, typeLabel, vehicleLabel);
                        NotificationHelper.notifyMaintenance(ctx, text, e.vehiculeId);
                    }
                }
            }
        } catch (Exception ignored) {}

        return Result.success();
    }

    private int daysUntil(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return Integer.MAX_VALUE;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Calendar target = Calendar.getInstance();
            target.setTime(sdf.parse(dateStr));
            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0); today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0); today.set(Calendar.MILLISECOND, 0);
            target.set(Calendar.HOUR_OF_DAY, 0); target.set(Calendar.MINUTE, 0);
            target.set(Calendar.SECOND, 0); target.set(Calendar.MILLISECOND, 0);
            long diff = target.getTimeInMillis() - today.getTimeInMillis();
            return (int) (diff / (1000 * 60 * 60 * 24));
        } catch (Exception ex) {
            return Integer.MAX_VALUE;
        }
    }
}
