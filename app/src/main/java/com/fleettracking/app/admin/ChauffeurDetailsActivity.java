package com.fleettracking.app.admin;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.fleettracking.app.R;
import com.fleettracking.app.data.RepoCallback;
import com.fleettracking.app.data.Repository;
import com.fleettracking.app.model.Chauffeur;
import com.fleettracking.app.model.Vehicule;
import com.fleettracking.app.util.UiUtils;

import java.util.ArrayList;
import java.util.List;

public class ChauffeurDetailsActivity extends AppCompatActivity {

    public static final String EXTRA_CHAUFFEUR_ID = "extra_chauffeur_id";

    private Repository repo;
    private Chauffeur current;
    private String chauffeurId;

    private Spinner spinnerVehicle;
    private List<Vehicule> availableVehicles = new ArrayList<>();
    private String oldVehicleId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chauffeur_details);

        repo = new Repository(this);
        chauffeurId = getIntent().getStringExtra(EXTRA_CHAUFFEUR_ID);
        if (chauffeurId == null) chauffeurId = "c1";

        ((TextView) findViewById(R.id.toolbar_title)).setText(R.string.chauffeur_details_title);
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        spinnerVehicle = findViewById(R.id.spinner_vehicle);

        ImageView btnDelete = findViewById(R.id.btn_delete);
        btnDelete.setVisibility(View.VISIBLE);
        btnDelete.setOnClickListener(v -> showDeleteConfirmation());

        refresh();
    }

    private void refresh() {
        repo.getChauffeur(chauffeurId, new RepoCallback<Chauffeur>() {
            @Override public void onResult(Chauffeur c) {
                current = c;
                loadVehiclesAndBind(c);
            }
            @Override public void onError(String message) {
                Toast.makeText(ChauffeurDetailsActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadVehiclesAndBind(Chauffeur c) {
        repo.getVehicules(new RepoCallback<List<Vehicule>>() {
            @Override
            public void onResult(List<Vehicule> list) {
                availableVehicles.clear();
                List<String> displayNames = new ArrayList<>();
                displayNames.add("Aucun véhicule");
                
                int selectedIndex = 0;
                oldVehicleId = null;

                for (Vehicule v : list) {
                    // On garde les véhicules dispos OU celui déjà assigné à ce chauffeur
                    if ("Disponible".equals(v.statut) || c.id.equals(v.conducteurId)) {
                        availableVehicles.add(v);
                        displayNames.add(v.getNomComplet() + " (" + v.immatriculation + ")");
                        
                        if (c.id.equals(v.conducteurId)) {
                            selectedIndex = availableVehicles.size();
                            oldVehicleId = v.id;
                        }
                    }
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(ChauffeurDetailsActivity.this,
                        android.R.layout.simple_spinner_item, displayNames);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerVehicle.setAdapter(adapter);
                spinnerVehicle.setSelection(selectedIndex);

                bind(c);
            }

            @Override public void onError(String m) { bind(c); }
        });
    }

    private void showDeleteConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Supprimer le chauffeur")
                .setMessage("Êtes-vous sûr de vouloir supprimer ce chauffeur ?")
                .setPositiveButton("Supprimer", (dialog, which) -> deleteChauffeur())
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void deleteChauffeur() {
        repo.deleteChauffeur(chauffeurId, new RepoCallback<Void>() {
            @Override
            public void onResult(Void result) {
                Toast.makeText(ChauffeurDetailsActivity.this, "Chauffeur supprimé", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            }
            @Override
            public void onError(String message) {
                Toast.makeText(ChauffeurDetailsActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void bind(Chauffeur c) {
        ((TextView) findViewById(R.id.text_chauffeur_name)).setText(c.nom);
        TextView statusView = findViewById(R.id.text_chauffeur_status);
        statusView.setText(c.statut);
        statusView.setTextColor(UiUtils.statusColor(this, c.statut));

        EditText name = findViewById(R.id.input_name);
        EditText phone = findViewById(R.id.input_phone);
        EditText email = findViewById(R.id.input_email);
        EditText license = findViewById(R.id.input_license);
        EditText login = findViewById(R.id.input_login);
        EditText password = findViewById(R.id.input_password);

        name.setText(c.nom);
        phone.setText(c.telephone);
        email.setText(c.email);
        license.setText(c.permis);
        login.setText(c.login);
        password.setText(c.password);

        findViewById(R.id.btn_save).setOnClickListener(v -> {
            c.nom = name.getText().toString();
            c.telephone = phone.getText().toString();
            c.email = email.getText().toString();
            c.permis = license.getText().toString();
            c.login = login.getText().toString();
            c.password = password.getText().toString();

            int selection = spinnerVehicle.getSelectedItemPosition();
            Vehicule selectedVeh = (selection > 0) ? availableVehicles.get(selection - 1) : null;
            String newVehicleId = (selectedVeh != null) ? selectedVeh.id : null;

            c.vehiculeAffecte = (selectedVeh != null) ? selectedVeh.getNomComplet() : "";

            // Mise à jour du chauffeur
            repo.updateChauffeur(c.id, c, new RepoCallback<Chauffeur>() {
                @Override public void onResult(Chauffeur saved) {
                    handleVehicleStatusChange(oldVehicleId, newVehicleId);
                }
                @Override public void onError(String message) {
                    Toast.makeText(ChauffeurDetailsActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void handleVehicleStatusChange(String oldId, String newId) {
        if (oldId != null && oldId.equals(newId)) {
            // Pas de changement de véhicule
            finishWithSuccess();
            return;
        }

        // 1. Libérer l'ancien véhicule s'il y en avait un
        if (oldId != null) {
            repo.getVehicule(oldId, new RepoCallback<Vehicule>() {
                @Override public void onResult(Vehicule v) {
                    v.statut = "Disponible";
                    v.conducteurId = null;
                    repo.updateVehicule(v.id, v, new RepoCallback<Vehicule>() {
                        @Override public void onResult(Vehicule x) { 
                            if (newId == null) finishWithSuccess();
                            else assignNewVehicle(newId);
                        }
                        @Override public void onError(String m) {}
                    });
                }
                @Override public void onError(String m) {}
            });
        } else if (newId != null) {
            assignNewVehicle(newId);
        } else {
            finishWithSuccess();
        }
    }

    private void assignNewVehicle(String newId) {
        repo.getVehicule(newId, new RepoCallback<Vehicule>() {
            @Override public void onResult(Vehicule v) {
                v.statut = "En mission";
                v.conducteurId = chauffeurId;
                repo.updateVehicule(v.id, v, new RepoCallback<Vehicule>() {
                    @Override public void onResult(Vehicule x) { finishWithSuccess(); }
                    @Override public void onError(String m) {}
                });
            }
            @Override public void onError(String m) {}
        });
    }

    private void finishWithSuccess() {
        Toast.makeText(this, "Chauffeur et véhicule mis à jour", Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
        finish();
    }
}
