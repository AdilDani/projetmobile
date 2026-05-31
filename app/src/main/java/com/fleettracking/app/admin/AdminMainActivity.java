package com.fleettracking.app.admin;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.fleettracking.app.R;
import com.fleettracking.app.admin.fragments.AdminAccueilFragment;
import com.fleettracking.app.admin.fragments.AdminHistoriqueFragment;
import com.fleettracking.app.admin.fragments.CarteFragment;
import com.fleettracking.app.admin.fragments.ChauffeursFragment;
import com.fleettracking.app.admin.fragments.VehiculesFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AdminMainActivity extends AppCompatActivity {

    public static final String EXTRA_OPEN_CARTE = "extra_open_carte";

    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main);

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
                bottomNav.setSelectedItemId(R.id.nav_carte);
            } else {
                bottomNav.setSelectedItemId(R.id.nav_accueil);
            }
        }
    }

    public void goToVehicules() { bottomNav.setSelectedItemId(R.id.nav_vehicules); }
    public void goToChauffeurs() { bottomNav.setSelectedItemId(R.id.nav_chauffeurs); }
    public void goToCarte() { bottomNav.setSelectedItemId(R.id.nav_carte); }

    private void show(@NonNull Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}
