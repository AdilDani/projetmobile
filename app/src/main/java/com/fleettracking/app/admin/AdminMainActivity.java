package com.fleettracking.app.admin;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.fleettracking.app.util.EntretienNotificationWorker;

import java.util.concurrent.TimeUnit;

import com.fleettracking.app.R;
import com.fleettracking.app.admin.fragments.AdminAccueilFragment;
import com.fleettracking.app.admin.fragments.AdminHistoriqueFragment;
import com.fleettracking.app.admin.fragments.CarteFragment;
import com.fleettracking.app.admin.fragments.ChauffeursFragment;
import com.fleettracking.app.admin.fragments.VehiculesFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AdminMainActivity extends AppCompatActivity {

    public static final String EXTRA_OPEN_CARTE = "extra_open_carte";
    public static final String EXTRA_FOCUS_LAT  = "extra_focus_lat";
    public static final String EXTRA_FOCUS_LNG  = "extra_focus_lng";

    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main);

        scheduleMaintenanceCheck();
        requestNotificationPermission();

        bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_accueil) {
                show(new AdminAccueilFragment());
            } else if (id == R.id.nav_vehicules) {
                show(new VehiculesFragment());
            } else if (id == R.id.nav_carte) {
                show(new CarteFragment());
            } else if (id == R.id.nav_chauffeurs) {
                show(new ChauffeursFragment());
            } else if (id == R.id.nav_historiques) {
                show(new AdminHistoriqueFragment());
            }
            return true;
        });

        if (savedInstanceState == null) {
            if (getIntent().getBooleanExtra(EXTRA_OPEN_CARTE, false)) {
                double lat = getIntent().getDoubleExtra(EXTRA_FOCUS_LAT, 0);
                double lng = getIntent().getDoubleExtra(EXTRA_FOCUS_LNG, 0);
                CarteFragment carte = new CarteFragment();
                if (lat != 0 || lng != 0) {
                    Bundle args = new Bundle();
                    args.putDouble(EXTRA_FOCUS_LAT, lat);
                    args.putDouble(EXTRA_FOCUS_LNG, lng);
                    carte.setArguments(args);
                }
                // Show the fragment directly so we control which instance is shown,
                // then sync the nav bar without triggering the item-selected listener.
                show(carte);
                bottomNav.setOnItemSelectedListener(null);
                bottomNav.setSelectedItemId(R.id.nav_carte);
                // Restore listener after the item is selected
                bottomNav.post(() -> bottomNav.setOnItemSelectedListener(item -> {
                    int id = item.getItemId();
                    if (id == R.id.nav_accueil)     show(new AdminAccueilFragment());
                    else if (id == R.id.nav_vehicules)  show(new VehiculesFragment());
                    else if (id == R.id.nav_carte)      show(new CarteFragment());
                    else if (id == R.id.nav_chauffeurs) show(new ChauffeursFragment());
                    else if (id == R.id.nav_historiques) show(new AdminHistoriqueFragment());
                    return true;
                }));
            } else {
                bottomNav.setSelectedItemId(R.id.nav_accueil);
            }
        }
    }

    public void goToVehicules() { bottomNav.setSelectedItemId(R.id.nav_vehicules); }
    public void goToChauffeurs() { bottomNav.setSelectedItemId(R.id.nav_chauffeurs); }
    public void goToCarte() { bottomNav.setSelectedItemId(R.id.nav_carte); }

    private void scheduleMaintenanceCheck() {
        // Fire immediately on launch; the worker self-reschedules every 3 minutes
        WorkManager.getInstance(this).enqueue(
                new OneTimeWorkRequest.Builder(EntretienNotificationWorker.class).build());
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 100);
            }
        }
    }

    private void show(@NonNull Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}
