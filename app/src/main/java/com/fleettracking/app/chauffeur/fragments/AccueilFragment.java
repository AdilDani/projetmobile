package com.fleettracking.app.chauffeur.fragments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.fleettracking.app.R;
import com.fleettracking.app.chauffeur.ChauffeurMainActivity;
import com.fleettracking.app.chauffeur.DeclarerIncidentActivity;
import com.fleettracking.app.chauffeur.HistoriqueActivity;
import com.fleettracking.app.data.RepoCallback;
import com.fleettracking.app.data.Repository;
import com.fleettracking.app.model.Chauffeur;
import com.fleettracking.app.model.Vehicule;
import com.fleettracking.app.util.ImageUtils;
import com.fleettracking.app.util.Prefs;
import com.fleettracking.app.util.TripManager;
import com.google.android.material.button.MaterialButton;

public class AccueilFragment extends Fragment {

    private Vehicule currentVehicule;
    private MaterialButton btnTrip;

    private final ActivityResultLauncher<String> locationPermLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            granted -> {
                if (granted) startTripNow();
                else Toast.makeText(getContext(),
                        R.string.location_permission_required, Toast.LENGTH_SHORT).show();
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chauffeur_accueil, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        Repository repo = new Repository(requireContext());
        String userId = new Prefs(requireContext()).getUserId();
        TripManager tripManager = TripManager.getInstance(requireContext());

        repo.getChauffeur(userId, new RepoCallback<Chauffeur>() {
            @Override public void onResult(Chauffeur driver) {
                if (!isAdded()) return;
                String name = driver.nom.split(" ")[0];
                ((TextView) v.findViewById(R.id.text_greeting))
                        .setText(getString(R.string.greeting_hello, name));
            }
            @Override public void onError(String message) { /* keep default */ }
        });

        repo.getCurrentVehicule(userId, new RepoCallback<Vehicule>() {
            @Override public void onResult(Vehicule vehicule) {
                if (!isAdded()) return;
                currentVehicule = vehicule;
                ((TextView) v.findViewById(R.id.text_vehicle_name)).setText(vehicule.getNomComplet());
                ((TextView) v.findViewById(R.id.text_vehicle_plate)).setText(vehicule.immatriculation);
                ((TextView) v.findViewById(R.id.text_vehicle_km))
                        .setText(String.format("%,d km", vehicule.kilometrage));
                
                ImageUtils.bind(v.findViewById(R.id.img_vehicle_thumb), vehicule.photo, R.drawable.ic_truck);
                updateTripButton();
            }
            @Override public void onError(String message) {
                v.findViewById(R.id.btn_trip_control).setVisibility(View.GONE);
            }
        });

        btnTrip = v.findViewById(R.id.btn_trip_control);
        btnTrip.setOnClickListener(x -> {
            if (tripManager.isTripActive()) {
                tripManager.stopTrip();
                Toast.makeText(getContext(), R.string.trip_stopped, Toast.LENGTH_SHORT).show();
                updateTripButton();
            } else if (currentVehicule != null) {
                // A trip streams real GPS, so we need location permission first.
                if (ContextCompat.checkSelfPermission(requireContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    startTripNow();
                } else {
                    locationPermLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
                }
            }
        });

        ChauffeurMainActivity host = (ChauffeurMainActivity) requireActivity();
        v.findViewById(R.id.shortcut_position).setOnClickListener(x ->
                host.selectTab(ChauffeurMainActivity.TAB_POSITION));
        v.findViewById(R.id.shortcut_incident).setOnClickListener(x ->
                startActivity(new Intent(getContext(), DeclarerIncidentActivity.class)));
        v.findViewById(R.id.shortcut_maintenance).setOnClickListener(x ->
                host.selectTab(ChauffeurMainActivity.TAB_VEHICULE));
        v.findViewById(R.id.shortcut_consumption).setOnClickListener(x ->
                startActivity(new Intent(getContext(), HistoriqueActivity.class)));
    }

    private void startTripNow() {
        if (currentVehicule == null) return;
        String userId = new Prefs(requireContext()).getUserId();
        TripManager.getInstance(requireContext()).startTrip(currentVehicule.id, currentVehicule.getNomComplet(), userId);
        Toast.makeText(getContext(), R.string.trip_started, Toast.LENGTH_SHORT).show();
        updateTripButton();
    }

    private void updateTripButton() {
        if (btnTrip == null) return;
        boolean active = TripManager.getInstance(requireContext()).isTripActive();
        if (active) {
            btnTrip.setText(R.string.stop_trip);
            btnTrip.setIconResource(R.drawable.ic_stop);
            btnTrip.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.danger));
        } else {
            btnTrip.setText(R.string.start_trip);
            btnTrip.setIconResource(R.drawable.ic_play);
            btnTrip.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.primary));
        }
    }
}
