package com.fleettracking.app.admin;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
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

public class VehiculeDetailsActivity extends AppCompatActivity {

    public static final String EXTRA_VEHICLE_ID = "extra_vehicle_id";

    private Repository repo;
    private Vehicule current;
    
    private ImageView imgVehicle;
    private EditText inputBrand, inputModel, inputPlate, inputYear, inputMileage, inputConsumption, inputVidange, inputControle;

    private final ActivityResultLauncher<String> picker = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    String base64 = ImageUtils.encodeFromUri(this, uri);
                    if (base64 != null) {
                        current.photo = base64;
                        ImageUtils.bind(imgVehicle, base64, R.drawable.ic_truck);
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicule_details);

        repo = new Repository(this);

        ((TextView) findViewById(R.id.toolbar_title)).setText(R.string.vehicle_details_title);
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        
        imgVehicle = findViewById(R.id.img_vehicle);
        inputBrand = findViewById(R.id.input_brand);
        inputModel = findViewById(R.id.input_model);
        inputPlate = findViewById(R.id.input_plate);
        inputYear = findViewById(R.id.input_year);
        inputMileage = findViewById(R.id.input_mileage);
        inputConsumption = findViewById(R.id.input_consumption);
        inputVidange = findViewById(R.id.input_vidange);
        inputControle = findViewById(R.id.input_controle);

        String id = getIntent().getStringExtra(EXTRA_VEHICLE_ID);
        repo.getVehicule(id == null ? "v1" : id, new RepoCallback<Vehicule>() {
            @Override public void onResult(Vehicule x) { current = x; bind(x); }
            @Override public void onError(String message) {
                Toast.makeText(VehiculeDetailsActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.btn_change_photo).setOnClickListener(v -> picker.launch("image/*"));

        findViewById(R.id.btn_save).setOnClickListener(v -> save());

        findViewById(R.id.btn_see_map).setOnClickListener(v -> {
            Intent i = new Intent(this, AdminMainActivity.class);
            i.putExtra(AdminMainActivity.EXTRA_OPEN_CARTE, true);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        });
    }

    private void bind(Vehicule x) {
        ((TextView) findViewById(R.id.text_vehicle_name)).setText(x.getNomComplet());
        TextView status = findViewById(R.id.text_vehicle_status);
        status.setText(x.statut);
        status.setTextColor(UiUtils.statusColor(this, x.statut));

        ImageUtils.bind(imgVehicle, x.photo, R.drawable.ic_truck);

        inputBrand.setText(x.marque);
        inputModel.setText(x.modele);
        inputPlate.setText(x.immatriculation);
        inputYear.setText(String.valueOf(x.annee));
        inputMileage.setText(String.valueOf(x.kilometrage));
        inputConsumption.setText(String.valueOf(x.consommation));
        inputVidange.setText(x.prochaineVidange);
        inputControle.setText(x.controleTechnique);

        TextView dn = findViewById(R.id.driver_name);
        TextView dp = findViewById(R.id.driver_phone);
        ImageView di = findViewById(R.id.img_driver);

        if (x.conducteurId != null && !x.conducteurId.isEmpty()) {
            repo.getChauffeur(x.conducteurId, new RepoCallback<Chauffeur>() {
                @Override public void onResult(Chauffeur driver) {
                    dn.setText(driver.nom);
                    dp.setText(driver.telephone);
                    ImageUtils.bind(di, driver.photo, R.drawable.ic_person);
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
            di.setImageResource(R.drawable.ic_person);
        }
    }

    private void save() {
        if (current == null) return;

        current.marque = inputBrand.getText().toString();
        current.modele = inputModel.getText().toString();
        current.immatriculation = inputPlate.getText().toString();
        try {
            current.annee = Integer.parseInt(inputYear.getText().toString());
            current.kilometrage = Integer.parseInt(inputMileage.getText().toString());
            current.consommation = Double.parseDouble(inputConsumption.getText().toString());
        } catch (NumberFormatException ignored) {}
        current.prochaineVidange = inputVidange.getText().toString();
        current.controleTechnique = inputControle.getText().toString();

        repo.updateVehicule(current.id, current, new RepoCallback<Vehicule>() {
            @Override public void onResult(Vehicule saved) {
                ((TextView) findViewById(R.id.text_vehicle_name)).setText(saved.getNomComplet());
                Toast.makeText(VehiculeDetailsActivity.this, R.string.saved_toast, Toast.LENGTH_SHORT).show();
            }
            @Override public void onError(String message) {
                Toast.makeText(VehiculeDetailsActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
