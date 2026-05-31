package com.fleettracking.app.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.fleettracking.app.R;
import com.fleettracking.app.data.RepoCallback;
import com.fleettracking.app.data.Repository;
import com.fleettracking.app.model.Chauffeur;
import com.fleettracking.app.model.Vehicule;
import com.fleettracking.app.util.UiUtils;

public class VehiculeDetailsActivity extends AppCompatActivity {

    public static final String EXTRA_VEHICLE_ID = "extra_vehicle_id";

    private LinearLayout container;
    private LayoutInflater inflater;
    private Repository repo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicule_details);

        repo = new Repository(this);

        ((TextView) findViewById(R.id.toolbar_title)).setText(R.string.vehicle_details_title);
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        ImageView action = findViewById(R.id.btn_action);
        action.setVisibility(View.VISIBLE);
        action.setImageResource(R.drawable.ic_edit);

        inflater = LayoutInflater.from(this);
        container = findViewById(R.id.details_container);

        String id = getIntent().getStringExtra(EXTRA_VEHICLE_ID);
        repo.getVehicule(id == null ? "v1" : id, new RepoCallback<Vehicule>() {
            @Override public void onResult(Vehicule x) { bind(x); }
            @Override public void onError(String message) {
                Toast.makeText(VehiculeDetailsActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void bind(Vehicule x) {
        ((TextView) findViewById(R.id.text_vehicle_name)).setText(x.getNomComplet());

        int textPrimary = ContextCompat.getColor(this, R.color.text_primary);
        container.removeAllViews();
        addRow(R.string.label_plate, x.immatriculation, textPrimary);
        addRow(R.string.label_brand, x.marque, textPrimary);
        addRow(R.string.label_model, x.modele, textPrimary);
        addRow(R.string.label_year, String.valueOf(x.annee), textPrimary);
        addRow(R.string.label_mileage, String.format("%,d km", x.kilometrage), textPrimary);
        addRow(R.string.label_status, x.statut, UiUtils.statusColor(this, x.statut));

        TextView dn = findViewById(R.id.driver_name);
        TextView dp = findViewById(R.id.driver_phone);
        if (x.conducteurId != null && !x.conducteurId.isEmpty()) {
            repo.getChauffeur(x.conducteurId, new RepoCallback<Chauffeur>() {
                @Override public void onResult(Chauffeur driver) {
                    dn.setText(driver.nom);
                    dp.setText(driver.telephone);
                    findViewById(R.id.driver_card).setOnClickListener(v -> {
                        Intent i = new Intent(VehiculeDetailsActivity.this, ChauffeurDetailsActivity.class);
                        i.putExtra(ChauffeurDetailsActivity.EXTRA_CHAUFFEUR_ID, driver.id);
                        startActivity(i);
                    });
                }
                @Override public void onError(String message) {
                    dn.setText(R.string.assigned_driver);
                    dp.setText("--");
                }
            });
        } else {
            dn.setText(R.string.assigned_driver);
            dp.setText("--");
        }

        findViewById(R.id.btn_see_map).setOnClickListener(v -> {
            Intent i = new Intent(this, AdminMainActivity.class);
            i.putExtra(AdminMainActivity.EXTRA_OPEN_CARTE, true);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        });
    }

    private void addRow(int labelRes, String value, int valueColor) {
        View row = inflater.inflate(R.layout.item_detail_row, container, false);
        row.findViewById(R.id.row_icon).setVisibility(View.GONE);
        ((TextView) row.findViewById(R.id.row_label)).setText(labelRes);
        TextView val = row.findViewById(R.id.row_value);
        val.setText(value);
        val.setTextColor(valueColor);
        container.addView(row);

        View d = new View(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 1);
        d.setLayoutParams(lp);
        d.setBackgroundColor(ContextCompat.getColor(this, R.color.divider));
        container.addView(d);
    }
}
