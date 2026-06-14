package com.fleettracking.app.admin.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fleettracking.app.R;
import com.fleettracking.app.admin.ProfilActivity;
import com.fleettracking.app.adapters.TrajetAdapter;
import com.fleettracking.app.data.RepoCallback;
import com.fleettracking.app.chauffeur.TrajetMapActivity;
import com.fleettracking.app.data.Repository;
import com.fleettracking.app.model.Trajet;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class AdminHistoriqueFragment extends Fragment {

    private final List<Trajet> trajets = new ArrayList<>();
    private TrajetAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_historique, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        ((TextView) v.findViewById(R.id.toolbar_title)).setText(R.string.nav_historiques);
        v.findViewById(R.id.btn_profile).setOnClickListener(x ->
                startActivity(new Intent(getContext(), ProfilActivity.class)));

        adapter = new TrajetAdapter(trajets, t -> {
            Intent i = new Intent(getContext(), TrajetMapActivity.class);
            i.putExtra(TrajetMapActivity.EXTRA_TRAJET, new Gson().toJson(t));
            startActivity(i);
        });
        RecyclerView rv = v.findViewById(R.id.recycler_trajets);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(adapter);

        new Repository(requireContext()).getTrajets(new RepoCallback<List<Trajet>>() {
            @Override public void onResult(List<Trajet> list) {
                trajets.clear();
                trajets.addAll(list);
                adapter.notifyDataSetChanged();
            }
            @Override public void onError(String message) { /* empty list */ }
        });
    }
}
