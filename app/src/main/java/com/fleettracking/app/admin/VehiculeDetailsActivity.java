package com.fleettracking.app.admin;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
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
import com.fleettracking.app.util.FleetConfig;
import com.fleettracking.app.util.ImageUtils;
import com.fleettracking.app.util.UiUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class VehiculeDetailsActivity extends AppCompatActivity {

    public static final String EXTRA_VEHICLE_ID = "extra_vehicle_id";
    public static final String EXTRA_NEW = "extra_new";

    private Repository repo;
    private Vehicule current;
    private boolean isNew;

    private ImageView imgVehicle;
    private EditText inputBrand, inputModel, inputPlate, inputYear, inputMileage, inputConsumption;
    private EditText inputVidangeCibleKm, inputCtDate;
    private Spinner spinnerStatus, spinnerDriver;
    private final List<Chauffeur> drivers = new ArrayList<>();

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

        isNew = getIntent().getBooleanExtra(EXTRA_NEW, false);

        ((TextView) findViewById(R.id.toolbar_title))
                .setText(isNew ? R.string.add_vehicle_title : R.string.vehicle_details_title);
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        imgVehicle = findViewById(R.id.img_vehicle);
        inputBrand = findViewById(R.id.input_brand);
        inputModel = findViewById(R.id.input_model);
        inputPlate = findViewById(R.id.input_plate);
        inputYear = findViewById(R.id.input_year);
        inputMileage = findViewById(R.id.input_mileage);
        inputConsumption = findViewById(R.id.input_consumption);
        inputVidangeCibleKm = findViewById(R.id.input_vidange_cible_km);
        inputCtDate = findViewById(R.id.input_ct_date);
        spinnerStatus = findViewById(R.id.spinner_status);
        spinnerDriver = findViewById(R.id.spinner_driver);

        inputCtDate.setOnClickListener(v -> showDatePicker());

        android.text.TextWatcher nameWatcher = new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void afterTextChanged(android.text.Editable s) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {
                String brand = inputBrand.getText().toString().trim();
                String model = inputModel.getText().toString().trim();
                String name = (brand + " " + model).trim();
                ((TextView) findViewById(R.id.text_vehicle_name)).setText(name.isEmpty() ? getString(R.string.add_vehicle_title) : name);
            }
        };
        inputBrand.addTextChangedListener(nameWatcher);
        inputModel.addTextChangedListener(nameWatcher);

        spinnerStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                boolean enMission = getString(R.string.status_on_mission)
                        .equals(spinnerStatus.getSelectedItem().toString());
                spinnerDriver.setEnabled(enMission);
                if (!enMission) spinnerDriver.setSelection(0);
            }
            @Override public void onNothingSelected(AdapterView<?> p) {}
        });

        loadDrivers(() -> {
            if (isNew) {
                current = new Vehicule();
                current.statut = getString(R.string.status_available);
                current.marque = "";
                current.modele = "";
                current.immatriculation = "";
                current.lat = FleetConfig.DEPOT_LAT;
                current.lng = FleetConfig.DEPOT_LNG;
                bind(current);
            } else {
                String id = getIntent().getStringExtra(EXTRA_VEHICLE_ID);
                repo.getVehicule(id == null ? "v1" : id, new RepoCallback<Vehicule>() {
                    @Override public void onResult(Vehicule x) { current = x; bind(x); }
                    @Override public void onError(String message) {
                        Toast.makeText(VehiculeDetailsActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        findViewById(R.id.btn_change_photo).setOnClickListener(v -> picker.launch("image/*"));
        findViewById(R.id.btn_save).setOnClickListener(v -> save());

        findViewById(R.id.btn_see_map).setOnClickListener(v -> {
            Intent i = new Intent(this, AdminMainActivity.class);
            i.putExtra(AdminMainActivity.EXTRA_OPEN_CARTE, true);
            if (current != null) {
                i.putExtra(AdminMainActivity.EXTRA_FOCUS_LAT, current.lat);
                i.putExtra(AdminMainActivity.EXTRA_FOCUS_LNG, current.lng);
            }
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        });
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        String current_text = inputCtDate.getText().toString();
        if (current_text.matches("\\d{4}-\\d{2}-\\d{2}")) {
            String[] parts = current_text.split("-");
            cal.set(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]) - 1, Integer.parseInt(parts[2]));
        }
        new DatePickerDialog(this, (view, year, month, day) -> {
            String date = String.format(java.util.Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, day);
            inputCtDate.setText(date);
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void loadDrivers(Runnable then) {
        repo.getChauffeurs(new RepoCallback<List<Chauffeur>>() {
            @Override public void onResult(List<Chauffeur> list) {
                drivers.clear();
                drivers.addAll(list);
                List<String> names = new ArrayList<>();
                names.add(getString(R.string.driver_none));
                for (Chauffeur c : list) names.add(c.nom);
                ArrayAdapter<String> ad = new ArrayAdapter<>(VehiculeDetailsActivity.this,
                        android.R.layout.simple_spinner_item, names);
                ad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerDriver.setAdapter(ad);
                then.run();
            }
            @Override public void onError(String message) { then.run(); }
        });
    }

    private void selectStatus(String statut) {
        String[] values = {
                getString(R.string.status_available),
                getString(R.string.status_unavailable),
                getString(R.string.status_on_mission)
        };
        for (int i = 0; i < values.length; i++) {
            if (values[i].equals(statut)) { spinnerStatus.setSelection(i); return; }
        }
        spinnerStatus.setSelection(0);
    }

    private void selectDriver(String conducteurId) {
        if (conducteurId != null) {
            for (int i = 0; i < drivers.size(); i++) {
                if (conducteurId.equals(drivers.get(i).id)) { spinnerDriver.setSelection(i + 1); return; }
            }
        }
        spinnerDriver.setSelection(0);
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
        inputVidangeCibleKm.setText(x.vidangeCibleKm > 0 ? String.valueOf(x.vidangeCibleKm) : "");
        inputCtDate.setText(x.controleTechniqueDate != null ? x.controleTechniqueDate : "");

        selectStatus(x.statut);
        selectDriver(x.conducteurId);

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
        try {
            String vStr = inputVidangeCibleKm.getText().toString().trim();
            current.vidangeCibleKm = vStr.isEmpty() ? 0 : Integer.parseInt(vStr);
        } catch (NumberFormatException ignored) {}
        current.controleTechniqueDate = inputCtDate.getText().toString().trim();

        String statut = spinnerStatus.getSelectedItem().toString();
        boolean enMission = getString(R.string.status_on_mission).equals(statut);
        if (enMission) {
            int pos = spinnerDriver.getSelectedItemPosition();
            if (pos <= 0 || pos > drivers.size()) {
                Toast.makeText(this, R.string.select_driver_required, Toast.LENGTH_SHORT).show();
                return;
            }
            current.conducteurId = drivers.get(pos - 1).id;
        } else {
            current.conducteurId = null;
        }
        current.statut = statut;

        final String localPhoto = current.photo;
        RepoCallback<Vehicule> cb = new RepoCallback<Vehicule>() {
            @Override public void onResult(Vehicule saved) {
                if (saved != null) {
                    if (saved.photo == null) saved.photo = localPhoto;
                    current = saved;
                    bind(saved);
                }
                Toast.makeText(VehiculeDetailsActivity.this, R.string.saved_toast, Toast.LENGTH_SHORT).show();
                if (isNew) finish();
            }
            @Override public void onError(String message) {
                Toast.makeText(VehiculeDetailsActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        };

        if (isNew || current.id == null || current.id.isEmpty()) {
            repo.createVehicule(current, cb);
        } else {
            repo.updateVehicule(current.id, current, cb);
        }
    }
}
