package com.fleettracking.app.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fleettracking.app.R;
import com.fleettracking.app.adapters.IncidentAdapter;
import com.fleettracking.app.data.RepoCallback;
import com.fleettracking.app.data.Repository;
import com.fleettracking.app.model.Incident;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class IncidentsActivity extends AppCompatActivity {

    private final List<Incident> all = new ArrayList<>();
    private final List<Incident> shown = new ArrayList<>();
    private IncidentAdapter adapter;
    private int currentTab = 0;
    private Repository repo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incidents);

        repo = new Repository(this);

        ((TextView) findViewById(R.id.toolbar_title)).setText(R.string.incidents_title);
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        ImageView action = findViewById(R.id.btn_action);
        action.setVisibility(View.VISIBLE);
        action.setImageResource(R.drawable.ic_plus);
        action.setOnClickListener(v ->
                startActivity(new Intent(this, NouvelIncidentActivity.class)));

        adapter = new IncidentAdapter(shown);
        RecyclerView rv = findViewById(R.id.recycler_incidents);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        TabLayout tabs = findViewById(R.id.tabs);
        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab) {
                currentTab = tab.getPosition();
                applyFilter();
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh on resume so a newly declared incident shows up.
        repo.getIncidents(new RepoCallback<List<Incident>>() {
            @Override public void onResult(List<Incident> list) {
                all.clear();
                all.addAll(list);
                applyFilter();
            }
            @Override public void onError(String message) { /* keep current */ }
        });
    }

    private void applyFilter() {
        shown.clear();
        for (Incident in : all) {
            boolean keep = currentTab == 0
                    || (currentTab == 1 && "En cours".equals(in.statut))
                    || (currentTab == 2 && "Résolu".equals(in.statut));
            if (keep) shown.add(in);
        }
        adapter.notifyDataSetChanged();
    }
}
