package com.fleettracking.app.chauffeur.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.fleettracking.app.R;
import com.fleettracking.app.chauffeur.ChauffeurMainActivity;
import com.fleettracking.app.chauffeur.DeclarerIncidentActivity;
import com.fleettracking.app.chauffeur.HistoriqueActivity;
import com.fleettracking.app.data.RepoCallback;
import com.fleettracking.app.data.Repository;
import com.fleettracking.app.model.Chauffeur;
import com.fleettracking.app.model.Vehicule;
import com.fleettracking.app.util.Prefs;

public class AccueilFragment extends Fragment {

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

        repo.getChauffeur(userId, new RepoCallback<Chauffeur>() {
            @Override public void onResult(Chauffeur driver) {
                if (!isAdded()) return;
                String firstName = driver.nom.split(" ")[0];
                ((TextView) v.findViewById(R.id.text_greeting))
                        .setText(getString(R.string.greeting_hello, firstName));
            }
            @Override public void onError(String message) { /* keep default */ }
        });

        repo.getCurrentVehicule(userId, new RepoCallback<Vehicule>() {
            @Override public void onResult(Vehicule vehicule) {
                if (!isAdded()) return;
                ((TextView) v.findViewById(R.id.text_vehicle_name)).setText(vehicule.getNomComplet());
                ((TextView) v.findViewById(R.id.text_vehicle_plate)).setText(vehicule.immatriculation);
                ((TextView) v.findViewById(R.id.text_vehicle_km))
                        .setText(String.format("%,d km", vehicule.kilometrage));
            }
            @Override public void onError(String message) { /* keep default */ }
        });

        v.findViewById(R.id.btn_start_trip).setOnClickListener(x ->
                Toast.makeText(getContext(), R.string.start_trip, Toast.LENGTH_SHORT).show());

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
}
