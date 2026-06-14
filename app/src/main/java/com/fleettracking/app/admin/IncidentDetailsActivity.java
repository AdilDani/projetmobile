package com.fleettracking.app.admin;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
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

        // Resolve button — hidden if already resolved
        View btnResolve = findViewById(R.id.btn_resolve);
        if ("Résolu".equals(in.statut)) {
            btnResolve.setVisibility(View.GONE);
        } else {
            btnResolve.setVisibility(View.VISIBLE);
            btnResolve.setOnClickListener(v -> {
                btnResolve.setEnabled(false);
                in.statut = "Résolu";
                repo.updateIncident(in.id, in, new RepoCallback<Incident>() {
                    @Override public void onResult(Incident updated) {
                        status.setText(updated.statut);
                        status.setTextColor(UiUtils.statusColor(IncidentDetailsActivity.this, updated.statut));
                        btnResolve.setVisibility(View.GONE);
                    }
                    @Override public void onError(String message) {
                        in.statut = "En cours";
                        btnResolve.setEnabled(true);
                        Toast.makeText(IncidentDetailsActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                });
            });
        }

        // Chauffeur Card
        TextView tvChauffeurName = findViewById(R.id.text_chauffeur_name);
        tvChauffeurName.setText(in.chauffeurNom);
        if (in.chauffeurId != null && !in.chauffeurId.isEmpty()) {
            final String cid = in.chauffeurId;
            // Wire navigation immediately — just like vehicle card
            findViewById(R.id.card_chauffeur).setOnClickListener(v -> {
                Intent i = new Intent(IncidentDetailsActivity.this, ChauffeurDetailsActivity.class);
                i.putExtra(ChauffeurDetailsActivity.EXTRA_CHAUFFEUR_ID, cid);
                startActivity(i);
            });
            // Load photo, name fallback, and call/SMS buttons asynchronously
            repo.getChauffeur(in.chauffeurId, new RepoCallback<Chauffeur>() {
                @Override public void onResult(Chauffeur c) {
                    if (in.chauffeurNom == null || in.chauffeurNom.isEmpty()) {
                        tvChauffeurName.setText(c.nom);
                    }
                    ImageUtils.bind(findViewById(R.id.img_chauffeur), c.photo, R.drawable.ic_person);
                    if (c.telephone != null && !c.telephone.isEmpty()) {
                        findViewById(R.id.btn_call_chauffeur).setOnClickListener(v ->
                                startActivity(new Intent(Intent.ACTION_DIAL,
                                        Uri.parse("tel:" + c.telephone))));
                        findViewById(R.id.btn_sms_chauffeur).setOnClickListener(v ->
                                startActivity(new Intent(Intent.ACTION_SENDTO,
                                        Uri.parse("smsto:" + c.telephone))));
                    }
                }
                @Override public void onError(String message) {}
            });
        }

        // Vehicle Card
        ((TextView) findViewById(R.id.text_vehicle_name)).setText(in.vehiculeNom);
        ((TextView) findViewById(R.id.text_vehicle_plate)).setText(in.immatriculation);
        if (in.vehiculeId != null && !in.vehiculeId.isEmpty()) {
            // Wire navigation immediately — vehiculeId is all we need
            final String vid = in.vehiculeId;
            findViewById(R.id.card_vehicle).setOnClickListener(v_view -> {
                Intent i = new Intent(IncidentDetailsActivity.this, VehiculeDetailsActivity.class);
                i.putExtra(VehiculeDetailsActivity.EXTRA_VEHICLE_ID, vid);
                startActivity(i);
            });
            // Load photo asynchronously
            repo.getVehicule(in.vehiculeId, new RepoCallback<Vehicule>() {
                @Override public void onResult(Vehicule v) {
                    ImageUtils.bind(findViewById(R.id.img_vehicle), v.photo, R.drawable.ic_truck);
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

                final String full = base64;
                img.setOnClickListener(v -> showFullImage(full));

                photosContainer.addView(img);
            }
        } else {
            TextView tv = new TextView(this);
            tv.setText(R.string.no_photo_available);
            int pad = getResources().getDimensionPixelSize(R.dimen.space_l);
            tv.setPadding(pad, pad, pad, pad);
            photosContainer.addView(tv);
        }
    }

    private void showFullImage(String base64) {
        Bitmap bmp = ImageUtils.decode(base64);
        if (bmp == null) return;

        ImageView full = new ImageView(this);
        full.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        full.setScaleType(ImageView.ScaleType.FIT_CENTER);
        full.setImageBitmap(bmp);

        Dialog dialog = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.setContentView(full);
        full.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }
}
