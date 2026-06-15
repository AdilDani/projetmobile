package com.fleettracking.app.chauffeur;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.fleettracking.app.util.EntretienNotificationWorker;

import java.util.concurrent.TimeUnit;

import com.fleettracking.app.R;
import com.fleettracking.app.chauffeur.fragments.AccueilFragment;
import com.fleettracking.app.chauffeur.fragments.PositionFragment;
import com.fleettracking.app.chauffeur.fragments.ProfilFragment;
import com.fleettracking.app.chauffeur.fragments.VehiculeFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ChauffeurMainActivity extends AppCompatActivity {

    public static final String EXTRA_TAB = "extra_tab";
    public static final int TAB_ACCUEIL = 0;
    public static final int TAB_POSITION = 1;
    public static final int TAB_VEHICULE = 2;
    public static final int TAB_PROFIL = 3;

    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chauffeur_main);

        scheduleMaintenanceCheck();
        requestNotificationPermission();

        bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_accueil) {
                show(new AccueilFragment());
            } else if (id == R.id.nav_position) {
                show(new PositionFragment());
            } else if (id == R.id.nav_vehicule) {
                show(new VehiculeFragment());
            } else if (id == R.id.nav_profil) {
                show(new ProfilFragment());
            }
            return true;
        });

        if (savedInstanceState == null) {
            int tab = getIntent().getIntExtra(EXTRA_TAB, TAB_ACCUEIL);
            selectTab(tab);
        }
    }

    public void selectTab(int tab) {
        switch (tab) {
            case TAB_POSITION: bottomNav.setSelectedItemId(R.id.nav_position); break;
            case TAB_VEHICULE: bottomNav.setSelectedItemId(R.id.nav_vehicule); break;
            case TAB_PROFIL: bottomNav.setSelectedItemId(R.id.nav_profil); break;
            default: bottomNav.setSelectedItemId(R.id.nav_accueil);
        }
    }

    private void scheduleMaintenanceCheck() {
        PeriodicWorkRequest work = new PeriodicWorkRequest.Builder(
                EntretienNotificationWorker.class, 24, TimeUnit.HOURS)
                .build();
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "maintenance_check", ExistingPeriodicWorkPolicy.KEEP, work);
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
