package com.fleettracking.app.admin.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fleettracking.app.R;
import com.fleettracking.app.admin.AddVehiculeActivity;
import com.fleettracking.app.admin.EntretiensActivity;
import com.fleettracking.app.admin.IncidentsActivity;
import com.fleettracking.app.admin.ProfilActivity;
import com.fleettracking.app.admin.VehiculeDetailsActivity;
import com.fleettracking.app.adapters.VehiculeAdapter;
import com.fleettracking.app.data.RepoCallback;
import com.fleettracking.app.data.Repository;
import com.fleettracking.app.model.Vehicule;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class VehiculesFragment extends Fragment {

    private final List<Vehicule> all = new ArrayList<>();
    private final List<Vehicule> filtered = new ArrayList<>();
    private VehiculeAdapter adapter;
    private EditText searchInput;
    private Repository repo;

    private final ActivityResultLauncher<Intent> addVehiculeLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    loadVehicles(); // Refresh the list
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_vehicules, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        repo = new Repository(requireContext());

        ((TextView) v.findViewById(R.id.toolbar_title)).setText(R.string.nav_vehicules);
        View action = v.findViewById(R.id.btn_action);
        action.setVisibility(View.VISIBLE);
        action.setOnClickListener(x -> {
            Intent intent = new Intent(getContext(), AddVehiculeActivity.class);
            addVehiculeLauncher.launch(intent);
        });

        v.findViewById(R.id.btn_profile).setOnClickListener(x ->
                startActivity(new Intent(getContext(), ProfilActivity.class)));

        v.findViewById(R.id.btn_incidents).setOnClickListener(x ->
                startActivity(new Intent(getContext(), IncidentsActivity.class)));
        v.findViewById(R.id.btn_entretiens).setOnClickListener(x ->
                startActivity(new Intent(getContext(), EntretiensActivity.class)));

        adapter = new VehiculeAdapter(filtered, this::openDetails);
        RecyclerView rv = v.findViewById(R.id.recycler_vehicles);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(adapter);

        searchInput = v.findViewById(R.id.input_search);
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) { filter(s.toString()); }
            @Override public void afterTextChanged(Editable s) {}
        });

        loadVehicles();
    }

    private void loadVehicles() {
        repo.getVehicules(new RepoCallback<List<Vehicule>>() {
            @Override public void onResult(List<Vehicule> list) {
                all.clear();
                all.addAll(list);
                filter(searchInput.getText().toString());
            }
            @Override public void onError(String message) { /* empty list */ }
        });
    }

    private void filter(String query) {
        String q = query.toLowerCase(Locale.getDefault()).trim();
        filtered.clear();
        for (Vehicule veh : all) {
            if (veh.getNomComplet().toLowerCase(Locale.getDefault()).contains(q)
                    || veh.immatriculation.toLowerCase(Locale.getDefault()).contains(q)) {
                filtered.add(veh);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void openDetails(Vehicule veh) {
        Intent i = new Intent(getContext(), VehiculeDetailsActivity.class);
        i.putExtra(VehiculeDetailsActivity.EXTRA_VEHICLE_ID, veh.id);
        startActivity(i);
    }
}
