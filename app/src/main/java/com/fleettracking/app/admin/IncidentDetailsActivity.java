package com.fleettracking.app.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.fleettracking.app.R;
import com.fleettracking.app.data.RepoCallback;
import com.fleettracking.app.data.Repository;
import com.fleettracking.app.model.Chauffeur;
import com.fleettracking.app.model.Incident;
import com.fleettracking.app.model.Vehicule;
import com.fleettracking.app.util.ImageUtils;
import com.fleettracking.app.util.UiUtils;

public class IncidentDetailsActivity extends AppCompatActivity {

    public static final String EXTRA_INCIDENT_ID = "extra_incident_id";

    private Repository repo;
    private LinearLayout photosContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incident_details);

        repo = new Repository(this);
        photosContainer = findViewById(R.id.photos_container);

        ((TextView) findViewById(R.id.toolbar_title)).setText(R.string.incident_details_title);
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        String id = getIntent().getStringExtra(EXTRA_INCIDENT_ID);
        if (id == null) { finish(); return; }

        repo.getIncident(id, new RepoCallback<Incident>() {
            @Override public void onResult(Incident in) { bind(in); }
            @Override public void onError(String message) {
                Toast.makeText(IncidentDetailsActivity.this, message, Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void bind(Incident in) {
        ((TextView) findViewById(R.id.text_type)).setText(in.type);
        ((TextView) findViewById(R.id.text_date)).setText(in.date);
        ((TextView) findViewById(R.id.text_description)).setText(in.description);

        TextView status = findViewById(R.id.text_status);
        status.setText(in.statut);
        status.setTextColor(UiUtils.statusColor(this, in.statut));

        // Chauffeur Card
        ((TextView) findViewById(R.id.text_chauffeur_name)).setText(in.chauffeurNom);
        if (in.chauffeurId != null && !in.chauffeurId.isEmpty()) {
            repo.getChauffeur(in.chauffeurId, new RepoCallback<Chauffeur>() {
                @Override public void onResult(Chauffeur c) {
                    ImageUtils.bind(findViewById(R.id.img_chauffeur), c.photo, R.drawable.ic_person);
                    findViewById(R.id.card_chauffeur).setOnClickListener(v -> {
                        Intent i = new Intent(IncidentDetailsActivity.this, ChauffeurDetailsActivity.class);
                        i.putExtra(ChauffeurDetailsActivity.EXTRA_CHAUFFEUR_ID, c.id);
                        startActivity(i);
                    });
                }
                @Override public void onError(String message) {}
            });
        }

        // Vehicle Card
        ((TextView) findViewById(R.id.text_vehicle_name)).setText(in.vehiculeNom);
        ((TextView) findViewById(R.id.text_vehicle_plate)).setText(in.immatriculation);
        if (in.vehiculeId != null && !in.vehiculeId.isEmpty()) {
            repo.getVehicule(in.vehiculeId, new RepoCallback<Vehicule>() {
                @Override public void onResult(Vehicule v) {
                    ImageUtils.bind(findViewById(R.id.img_vehicle), v.photo, R.drawable.ic_truck);
                    findViewById(R.id.card_vehicle).setOnClickListener(v_view -> {
                        Intent i = new Intent(IncidentDetailsActivity.this, VehiculeDetailsActivity.class);
                        i.putExtra(VehiculeDetailsActivity.EXTRA_VEHICLE_ID, v.id);
                        startActivity(i);
                    });
                }
                @Override public void onError(String message) {}
            });
        }

        // Photos
        photosContainer.removeAllViews();
        if (in.images != null && !in.images.isEmpty()) {
            for (String base64 : in.images) {
                ImageView img = new ImageView(this);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 600);
                lp.setMargins(0, 0, 0, 32);
                img.setLayoutParams(lp);
                img.setScaleType(ImageView.ScaleType.CENTER_CROP);
                img.setBackgroundResource(R.drawable.bg_card);
                img.setClipToOutline(true);
                img.setElevation(4);
                ImageUtils.bind(img, base64, R.drawable.ic_camera);
                
                // Clicking opens "gallery view" (just Toast for now or we could make a simple dialog)
                img.setOnClickListener(v -> {
                    // Simple full screen dialog or just a toast
                    Toast.makeText(this, "Agrandir l'image", Toast.LENGTH_SHORT).show();
                });
                
                photosContainer.addView(img);
            }
        } else {
            TextView tv = new TextView(this);
            tv.setText("Aucune photo disponible");
            tv.setPadding(32, 32, 32, 32);
            photosContainer.addView(tv);
        }
    }
}
