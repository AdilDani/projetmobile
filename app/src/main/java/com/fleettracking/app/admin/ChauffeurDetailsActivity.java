package com.fleettracking.app.admin;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.fleettracking.app.R;
import com.fleettracking.app.data.RepoCallback;
import com.fleettracking.app.data.Repository;
import com.fleettracking.app.model.Chauffeur;
import com.fleettracking.app.model.Vehicule;
import com.fleettracking.app.util.ImageUtils;
import com.fleettracking.app.util.UiUtils;

import java.util.ArrayList;
import java.util.List;

public class ChauffeurDetailsActivity extends AppCompatActivity {

    public static final String EXTRA_CHAUFFEUR_ID = "extra_chauffeur_id";
    public static final String EXTRA_NEW = "extra_new";

    private Repository repo;
    private Chauffeur current;
    private boolean isNew;
    private ImageView imgChauffeur;

    private Spinner spinnerVehicle;
    private final List<Vehicule> availableVehicles = new ArrayList<>();
    private String oldVehicleId = null;

    private final ActivityResultLauncher<String> picker = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null && current != null) {
                    String base64 = ImageUtils.encodeFromUri(this, uri);
                    if (base64 != null) {
                        current.photo = base64;
                        ImageUtils.bind(imgChauffeur, base64, R.drawable.ic_person);
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chauffeur_details);

        repo = new Repository(this);
        isNew = getIntent().getBooleanExtra(EXTRA_NEW, false);

        ((TextView) findViewById(R.id.toolbar_title)).setText(
                isNew ? R.string.add_chauffeur_title : R.string.chauffeur_details_title);
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        imgChauffeur = findViewById(R.id.img_chauffeur);
        spinnerVehicle = findViewById(R.id.spinner_vehicle);
        findViewById(R.id.btn_change_photo).setOnClickListener(v -> picker.launch("image/*"));

        if (isNew) {
            current = new Chauffeur();
            current.statut = "Disponible";
            current.nom = "";
            current.telephone = "";
            current.email = "";
            current.permis = "";
            current.login = "";
            current.password = "";
            loadVehiclesAndBind(current);
        } else {
            String id = getIntent().getStringExtra(EXTRA_CHAUFFEUR_ID);
            repo.getChauffeur(id == null ? "c1" : id, new RepoCallback<Chauffeur>() {
                @Override public void onResult(Chauffeur c) { current = c; loadVehiclesAndBind(c); }
                @Override public void onError(String message) {
                    Toast.makeText(ChauffeurDetailsActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void loadVehiclesAndBind(Chauffeur c) {
        repo.getVehicules(new RepoCallback<List<Vehicule>>() {
            @Override
            public void onResult(List<Vehicule> list) {
                availableVehicles.clear();
                List<String> displayNames = new ArrayList<>();
                displayNames.add(getString(R.string.no_vehicle_assigned));

                int selectedIndex = 0;
                oldVehicleId = null;

                for (Vehicule v : list) {
                    boolean isAssigned = c.id != null && c.id.equals(v.conducteurId);
                    availableVehicles.add(v);
                    displayNames.add(v.getNomComplet() + " (" + v.immatriculation + ")");
                    if (isAssigned) {
                        selectedIndex = availableVehicles.size();
                        oldVehicleId = v.id;
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

    private void bind(Chauffeur c) {
        ((TextView) findViewById(R.id.text_chauffeur_name)).setText(c.nom);
        TextView status = findViewById(R.id.text_chauffeur_status);
        status.setText(c.statut);
        status.setTextColor(UiUtils.statusColor(this, c.statut));

        ImageUtils.bind(imgChauffeur, c.photo, R.drawable.ic_person);

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

            int sel = spinnerVehicle.getSelectedItemPosition();
            Vehicule selectedVeh = (sel > 0) ? availableVehicles.get(sel - 1) : null;
            if (selectedVeh != null && "En trajet".equals(selectedVeh.statut)) {
                Toast.makeText(ChauffeurDetailsActivity.this,
                        "Ce véhicule est en trajet et ne peut pas être réassigné", Toast.LENGTH_SHORT).show();
                return;
            }
            String newVehicleId = (selectedVeh != null) ? selectedVeh.id : null;
            c.vehiculeAffecte = (selectedVeh != null) ? selectedVeh.getNomComplet() : "";

            RepoCallback<Chauffeur> cb = new RepoCallback<Chauffeur>() {
                @Override public void onResult(Chauffeur saved) {
                    current = saved;
                    ((TextView) findViewById(R.id.text_chauffeur_name)).setText(saved.nom);
                    handleVehicleStatusChange(oldVehicleId, newVehicleId);
                }
                @Override public void onError(String message) {
                    Toast.makeText(ChauffeurDetailsActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            };
            if (isNew) {
                repo.createChauffeur(c, cb);
            } else {
                repo.updateChauffeur(c.id, c, cb);
            }
        });
    }

    private void handleVehicleStatusChange(String oldId, String newId) {
        if (oldId != null && oldId.equals(newId)) {
            finishWithSuccess();
            return;
        }

        // Release the previously assigned vehicle
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
                        @Override public void onError(String m) { finishWithSuccess(); }
                    });
                }
                @Override public void onError(String m) { finishWithSuccess(); }
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
                v.statut = "Assigné";
                v.conducteurId = current.id;
                repo.updateVehicule(v.id, v, new RepoCallback<Vehicule>() {
                    @Override public void onResult(Vehicule x) { finishWithSuccess(); }
                    @Override public void onError(String m) { finishWithSuccess(); }
                });
            }
            @Override public void onError(String m) { finishWithSuccess(); }
        });
    }

    private void finishWithSuccess() {
        Toast.makeText(this, R.string.saved_toast, Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
        finish();
    }
}
