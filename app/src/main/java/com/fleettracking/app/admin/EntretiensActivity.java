package com.fleettracking.app.admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fleettracking.app.R;
import com.fleettracking.app.adapters.EntretienAdapter;
import com.fleettracking.app.data.RepoCallback;
import com.fleettracking.app.data.Repository;
import com.fleettracking.app.model.Entretien;
import com.fleettracking.app.model.Vehicule;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EntretiensActivity extends AppCompatActivity {

    public static final String EXTRA_VEHICULE_ID = "extra_vehicule_id";

    private Repository repo;
    private final List<Entretien> all = new ArrayList<>();
    private final List<Entretien> shown = new ArrayList<>();
    private final Map<String, Vehicule> vehiculeMap = new HashMap<>();
    private EntretienAdapter adapter;
    private boolean showAVenir = true;
    private String filterVehiculeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entretiens);

        repo = new Repository(this);
        filterVehiculeId = getIntent().getStringExtra(EXTRA_VEHICULE_ID);

        ((TextView) findViewById(R.id.toolbar_title)).setText(R.string.maintenance_title);
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        adapter = new EntretienAdapter(shown, vehiculeMap, this::confirmMarkDone);
        RecyclerView rv = findViewById(R.id.recycler_entretiens);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        TabLayout tabs = findViewById(R.id.tabs);
        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab) {
                showAVenir = tab.getPosition() == 0;
                applyFilter();
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        findViewById(R.id.btn_action).setOnClickListener(v -> {
            Intent i = new Intent(this, CreateEntretienActivity.class);
            startActivityForResult(i, 1);
        });

        load();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) load();
    }

    private void load() {
        repo.getVehicules(new RepoCallback<List<Vehicule>>() {
            @Override public void onResult(List<Vehicule> list) {
                vehiculeMap.clear();
                for (Vehicule v : list) vehiculeMap.put(v.id, v);
                repo.getEntretiens(new RepoCallback<List<Entretien>>() {
                    @Override public void onResult(List<Entretien> list) {
                        all.clear();
                        all.addAll(list);
                        applyFilter();
                    }
                    @Override public void onError(String message) { applyFilter(); }
                });
            }
            @Override public void onError(String message) {
                repo.getEntretiens(new RepoCallback<List<Entretien>>() {
                    @Override public void onResult(List<Entretien> list) {
                        all.clear();
                        all.addAll(list);
                        applyFilter();
                    }
                    @Override public void onError(String msg) { applyFilter(); }
                });
            }
        });
    }

    private void applyFilter() {
        shown.clear();
        String targetStatut = showAVenir ? "aVenir" : "effectue";
        for (Entretien e : all) {
            if (!targetStatut.equals(e.statut)) continue;
            if (filterVehiculeId != null && !filterVehiculeId.equals(e.vehiculeId)) continue;
            shown.add(e);
        }
        adapter.notifyDataSetChanged();
    }

    private void confirmMarkDone(Entretien e) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.mark_done_confirm_title)
                .setMessage(getString(R.string.mark_done_confirm) + "\n\n" + e.type + " — " + e.vehiculeNom)
                .setPositiveButton(R.string.confirm, (d, w) -> {
                    repo.markEntretienDone(e.id, new RepoCallback<Entretien>() {
                        @Override public void onResult(Entretien updated) { load(); }
                        @Override public void onError(String message) {
                            Toast.makeText(EntretiensActivity.this, message, Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}
