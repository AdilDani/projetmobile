package com.fleettracking.app.chauffeur;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fleettracking.app.R;
import com.fleettracking.app.adapters.TrajetAdapter;
import com.fleettracking.app.data.RepoCallback;
import com.fleettracking.app.data.Repository;
import com.fleettracking.app.model.Trajet;

import java.util.ArrayList;
import java.util.List;

public class HistoriqueActivity extends AppCompatActivity {

    private final List<Trajet> trajets = new ArrayList<>();
    private TrajetAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historique_trajets);

        ((TextView) findViewById(R.id.toolbar_title)).setText(R.string.history_trips_title);
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        adapter = new TrajetAdapter(trajets);
        RecyclerView rv = findViewById(R.id.recycler_trips);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        new Repository(this).getTrajets(new RepoCallback<List<Trajet>>() {
            @Override public void onResult(List<Trajet> list) {
                trajets.clear();
                trajets.addAll(list);
                adapter.notifyDataSetChanged();
                if (!list.isEmpty()) bindSummary(list.get(0));
            }
            @Override public void onError(String message) { /* empty */ }
        });
    }

    private void bindSummary(Trajet today) {
        ((TextView) findViewById(R.id.stat_distance)).setText(today.distanceKm + " km");
        ((TextView) findViewById(R.id.stat_duration)).setText(today.duree);
        ((TextView) findViewById(R.id.stat_avg_speed)).setText(today.vitesseMoyenne + " km/h");

        ((TextView) findViewById(R.id.text_depart)).setText(today.depart);
        ((TextView) findViewById(R.id.text_depart_time)).setText(today.heureDepart);
        ((TextView) findViewById(R.id.text_arrival)).setText(today.arrivee);
        ((TextView) findViewById(R.id.text_arrival_time)).setText(today.heureArrivee);

        ((TextView) findViewById(R.id.text_total_distance)).setText(today.distanceKm + " km");
        ((TextView) findViewById(R.id.text_total_time)).setText(today.duree);
        ((TextView) findViewById(R.id.text_consumption))
                .setText(String.format("%.1f L", today.consommation));
    }
}
