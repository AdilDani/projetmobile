package com.fleettracking.app.admin;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fleettracking.app.R;
import com.fleettracking.app.adapters.EntretienAdapter;
import com.fleettracking.app.data.RepoCallback;
import com.fleettracking.app.data.Repository;
import com.fleettracking.app.model.Entretien;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class EntretiensActivity extends AppCompatActivity {

    private final List<Entretien> all = new ArrayList<>();
    private final List<Entretien> shown = new ArrayList<>();
    private EntretienAdapter adapter;
    private boolean showAVenir = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entretiens);

        ((TextView) findViewById(R.id.toolbar_title)).setText(R.string.maintenance_title);
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        adapter = new EntretienAdapter(shown);
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

        new Repository(this).getEntretiens(new RepoCallback<List<Entretien>>() {
            @Override public void onResult(List<Entretien> list) {
                all.clear();
                all.addAll(list);
                applyFilter();
            }
            @Override public void onError(String message) { /* empty list */ }
        });
    }

    private void applyFilter() {
        shown.clear();
        for (Entretien e : all) {
            if (e.aVenir == showAVenir) shown.add(e);
        }
        adapter.notifyDataSetChanged();
    }
}
