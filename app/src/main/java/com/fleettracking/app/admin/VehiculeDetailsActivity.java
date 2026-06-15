package com.fleettracking.app.admin;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class VehiculeDetailsActivity extends AppCompatActivity {

    public static final String EXTRA_VEHICLE_ID = "extra_vehicle_id";
    public static final String EXTRA_NEW = "extra_new";

    private Repository repo;
    private Vehicule current;
    private boolean isNew;

    private ImageView imgVehicle;
    private EditText inputBrand, inputModel, inputPlate, inputYear, inputMileage, inputConsumption;
    private EditText inputVidangeRestantsKm, inputCtDate;
    private Spinner spinnerDriver;
    private final List<Chauffeur> drivers = new ArrayList<>();

    // Status state — derived, never a spinner
    private boolean isIndisponible = false;
    private String oldDriverId = null;

    // Sections
    private LinearLayout sectionAvailability;
    private LinearLayout sectionAssignDriver;
    private LinearLayout sectionCurrentDriver;
    private LinearLayout sectionEnTrajetBanner;
    private MaterialButton btnToggleDisponible;
    private MaterialButton btnToggleIndisponible;
    private MaterialButton btnRetirer;

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
        inputVidangeRestantsKm = findViewById(R.id.input_vidange_restants_km);
        inputCtDate = findViewById(R.id.input_ct_date);
        spinnerDriver = findViewById(R.id.spinner_driver);

        sectionAvailability = findViewById(R.id.section_availability);
        sectionAssignDriver = findViewById(R.id.section_assign_driver);
        sectionCurrentDriver = findViewById(R.id.section_current_driver);
        sectionEnTrajetBanner = findViewById(R.id.section_en_trajet_banner);
        btnToggleDisponible = findViewById(R.id.btn_toggle_disponible);
        btnToggleIndisponible = findViewById(R.id.btn_toggle_indisponible);
        btnRetirer = findViewById(R.id.btn_retirer);

        inputCtDate.setOnClickListener(v -> showDatePicker());

        android.text.TextWatcher nameWatcher = new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void afterTextChanged(android.text.Editable s) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {
                String brand = inputBrand.getText().toString().trim();
                String model = inputModel.getText().toString().trim();
                String name = (brand + " " + model).trim();
                ((TextView) findViewById(R.id.text_vehicle_name)).setText(
                        name.isEmpty() ? getString(R.string.add_vehicle_title) : name);
            }
        };
        inputBrand.addTextChangedListener(nameWatcher);
        inputModel.addTextChangedListener(nameWatcher);

        btnToggleDisponible.setOnClickListener(v -> setAvailability(false));
        btnToggleIndisponible.setOnClickListener(v -> setAvailability(true));

        btnRetirer.setOnClickListener(v -> {
            // Switch to Disponible state; actual save happens on Save button
            oldDriverId = current != null ? current.conducteurId : null;
            if (current != null) current.conducteurId = null;
            showDisponibleState();
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
        String txt = inputCtDate.getText().toString();
        if (txt.matches("\\d{4}-\\d{2}-\\d{2}")) {
            String[] p = txt.split("-");
            cal.set(Integer.parseInt(p[0]), Integer.parseInt(p[1]) - 1, Integer.parseInt(p[2]));
        }
        new DatePickerDialog(this, (view, year, month, day) ->
                inputCtDate.setText(String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, day)),
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void loadDrivers(Runnable then) {
        final String editingVehicleId = isNew ? null : getIntent().getStringExtra(EXTRA_VEHICLE_ID);
        repo.getVehicules(new RepoCallback<List<Vehicule>>() {
            @Override public void onResult(List<Vehicule> vehicules) {
                Set<String> takenIds = new HashSet<>();
                for (Vehicule v : vehicules) {
                    if (v.conducteurId != null && !v.conducteurId.isEmpty()
                            && !v.id.equals(editingVehicleId)) {
                        takenIds.add(v.conducteurId);
                    }
                }
                fetchChauffeurs(takenIds, then);
            }
            @Override public void onError(String m) { fetchChauffeurs(new HashSet<>(), then); }
        });
    }

    private void fetchChauffeurs(Set<String> takenIds, Runnable then) {
        repo.getChauffeurs(new RepoCallback<List<Chauffeur>>() {
            @Override public void onResult(List<Chauffeur> list) {
                drivers.clear();
                List<String> names = new ArrayList<>();
                names.add(getString(R.string.driver_none));
                for (Chauffeur c : list) {
                    if (!takenIds.contains(c.id)) {
                        drivers.add(c);
                        names.add(c.nom);
                    }
                }
                ArrayAdapter<String> ad = new ArrayAdapter<>(VehiculeDetailsActivity.this,
                        android.R.layout.simple_spinner_item, names);
                ad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerDriver.setAdapter(ad);
                then.run();
            }
            @Override public void onError(String message) { then.run(); }
        });
    }

    private void bind(Vehicule x) {
        ((TextView) findViewById(R.id.text_vehicle_name)).setText(x.getNomComplet());
        TextView statusBadge = findViewById(R.id.text_vehicle_status);
        statusBadge.setText(x.statut);
        statusBadge.setTextColor(UiUtils.statusColor(this, x.statut));

        ImageUtils.bind(imgVehicle, x.photo, R.drawable.ic_truck);

        inputBrand.setText(x.marque);
        inputModel.setText(x.modele);
        inputPlate.setText(x.immatriculation);
        inputYear.setText(String.valueOf(x.annee));
        inputMileage.setText(String.valueOf(x.kilometrage));
        inputConsumption.setText(String.valueOf(x.consommation));

        int restants = x.vidangeCibleKm > 0 ? Math.max(0, x.vidangeCibleKm - x.kilometrage) : 0;
        inputVidangeRestantsKm.setText(restants > 0 ? String.valueOf(restants) : "");
        inputCtDate.setText(x.controleTechniqueDate != null ? x.controleTechniqueDate : "");

        oldDriverId = x.conducteurId;

        String statut = x.statut != null ? x.statut : "";
        if (getString(R.string.status_en_trajet).equals(statut)) {
            showEnTrajetState(x);
        } else if (getString(R.string.status_assigned).equals(statut)) {
            showAssignedState(x);
        } else if (getString(R.string.status_unavailable).equals(statut)) {
            isIndisponible = true;
            showIndisponibleState();
        } else {
            isIndisponible = false;
            showDisponibleState();
        }
    }

    /** Locked — vehicle is actively on a trip. */
    private void showEnTrajetState(Vehicule x) {
        setFieldsEnabled(false);
        sectionEnTrajetBanner.setVisibility(View.VISIBLE);
        sectionAvailability.setVisibility(View.GONE);
        sectionAssignDriver.setVisibility(View.GONE);
        sectionCurrentDriver.setVisibility(View.VISIBLE);
        btnRetirer.setVisibility(View.GONE);
        loadCurrentDriver(x);
    }

    /** Has a driver — show driver card + Retirer button. */
    private void showAssignedState(Vehicule x) {
        setFieldsEnabled(true);
        sectionEnTrajetBanner.setVisibility(View.GONE);
        sectionAvailability.setVisibility(View.GONE);
        sectionAssignDriver.setVisibility(View.GONE);
        sectionCurrentDriver.setVisibility(View.VISIBLE);
        btnRetirer.setVisibility(View.VISIBLE);
        loadCurrentDriver(x);
    }

    /** No driver, available — show toggle (Disponible active) + assign spinner. */
    private void showDisponibleState() {
        isIndisponible = false;
        setFieldsEnabled(true);
        sectionEnTrajetBanner.setVisibility(View.GONE);
        sectionAvailability.setVisibility(View.VISIBLE);
        sectionAssignDriver.setVisibility(View.VISIBLE);
        sectionCurrentDriver.setVisibility(View.GONE);
        applyToggleStyle(false);
        // Reset spinner to "Aucun conducteur" so a fresh pick is required
        if (spinnerDriver.getAdapter() != null) spinnerDriver.setSelection(0);
    }

    /** No driver, unavailable — show toggle (Indisponible active), no assign section. */
    private void showIndisponibleState() {
        isIndisponible = true;
        setFieldsEnabled(true);
        sectionEnTrajetBanner.setVisibility(View.GONE);
        sectionAvailability.setVisibility(View.VISIBLE);
        sectionAssignDriver.setVisibility(View.GONE);
        sectionCurrentDriver.setVisibility(View.GONE);
        applyToggleStyle(true);
    }

    private void setAvailability(boolean indisponible) {
        isIndisponible = indisponible;
        applyToggleStyle(indisponible);
        sectionAssignDriver.setVisibility(indisponible ? View.GONE : View.VISIBLE);
    }

    private void applyToggleStyle(boolean indisponible) {
        if (indisponible) {
            btnToggleDisponible.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(
                            androidx.core.content.ContextCompat.getColor(this, R.color.card_bg)));
            btnToggleDisponible.setTextColor(
                    androidx.core.content.ContextCompat.getColor(this, R.color.text_secondary));
            btnToggleIndisponible.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(
                            androidx.core.content.ContextCompat.getColor(this, R.color.primary)));
            btnToggleIndisponible.setTextColor(
                    androidx.core.content.ContextCompat.getColor(this, R.color.white));
        } else {
            btnToggleDisponible.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(
                            androidx.core.content.ContextCompat.getColor(this, R.color.primary)));
            btnToggleDisponible.setTextColor(
                    androidx.core.content.ContextCompat.getColor(this, R.color.white));
            btnToggleIndisponible.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(
                            androidx.core.content.ContextCompat.getColor(this, R.color.card_bg)));
            btnToggleIndisponible.setTextColor(
                    androidx.core.content.ContextCompat.getColor(this, R.color.text_secondary));
        }
    }

    private void setFieldsEnabled(boolean enabled) {
        inputBrand.setEnabled(enabled);
        inputModel.setEnabled(enabled);
        inputPlate.setEnabled(enabled);
        inputYear.setEnabled(enabled);
        inputMileage.setEnabled(enabled);
        inputConsumption.setEnabled(enabled);
        inputVidangeRestantsKm.setEnabled(enabled);
        inputCtDate.setEnabled(enabled);
        if (!enabled) inputCtDate.setOnClickListener(null);
        else inputCtDate.setOnClickListener(v -> showDatePicker());
        findViewById(R.id.btn_save).setEnabled(enabled);
    }

    private void loadCurrentDriver(Vehicule x) {
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
        }
    }

    private void save() {
        if (current == null) return;
        if (getString(R.string.status_en_trajet).equals(current.statut)) {
            Toast.makeText(this, R.string.vehicle_en_trajet_locked, Toast.LENGTH_SHORT).show();
            return;
        }

        final String capturedOldDriverId = oldDriverId;

        current.marque = inputBrand.getText().toString();
        current.modele = inputModel.getText().toString();
        current.immatriculation = inputPlate.getText().toString();
        try {
            current.annee = Integer.parseInt(inputYear.getText().toString());
            current.kilometrage = Integer.parseInt(inputMileage.getText().toString());
            current.consommation = Double.parseDouble(inputConsumption.getText().toString());
        } catch (NumberFormatException ignored) {}

        try {
            String vStr = inputVidangeRestantsKm.getText().toString().trim();
            if (!vStr.isEmpty()) {
                current.vidangeCibleKm = current.kilometrage + Integer.parseInt(vStr);
            }
        } catch (NumberFormatException ignored) {}
        current.controleTechniqueDate = inputCtDate.getText().toString().trim();

        // Derive statut from UI state
        boolean driverPicked = sectionAssignDriver.getVisibility() == View.VISIBLE
                && spinnerDriver.getSelectedItemPosition() > 0
                && spinnerDriver.getSelectedItemPosition() <= drivers.size();

        if (driverPicked) {
            current.conducteurId = drivers.get(spinnerDriver.getSelectedItemPosition() - 1).id;
            current.statut = getString(R.string.status_assigned);
        } else {
            current.conducteurId = null;
            current.statut = isIndisponible
                    ? getString(R.string.status_unavailable)
                    : getString(R.string.status_available);
        }

        final String newDriverId = current.conducteurId;
        final String localPhoto = current.photo;
        RepoCallback<Vehicule> cb = new RepoCallback<Vehicule>() {
            @Override public void onResult(Vehicule saved) {
                if (saved != null) {
                    if (saved.photo == null) saved.photo = localPhoto;
                    current = saved;
                }
                handleDriverAssignment(capturedOldDriverId, newDriverId, current);
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

    private void handleDriverAssignment(String oldId, String newId, Vehicule veh) {
        boolean oldEmpty = oldId == null || oldId.isEmpty();
        boolean newEmpty = newId == null || newId.isEmpty();
        if (oldEmpty && newEmpty) { finishWithSuccess(); return; }
        if (!oldEmpty && oldId.equals(newId)) { finishWithSuccess(); return; }

        if (!oldEmpty) {
            repo.getChauffeur(oldId, new RepoCallback<Chauffeur>() {
                @Override public void onResult(Chauffeur c) {
                    c.vehiculeAffecte = "";
                    repo.updateChauffeur(c.id, c, new RepoCallback<Chauffeur>() {
                        @Override public void onResult(Chauffeur x) {
                            if (!newEmpty) assignNewDriver(newId, veh); else finishWithSuccess();
                        }
                        @Override public void onError(String m) {
                            if (!newEmpty) assignNewDriver(newId, veh); else finishWithSuccess();
                        }
                    });
                }
                @Override public void onError(String m) {
                    if (!newEmpty) assignNewDriver(newId, veh); else finishWithSuccess();
                }
            });
        } else {
            assignNewDriver(newId, veh);
        }
    }

    private void assignNewDriver(String driverId, Vehicule veh) {
        repo.getChauffeur(driverId, new RepoCallback<Chauffeur>() {
            @Override public void onResult(Chauffeur c) {
                c.vehiculeAffecte = veh.getNomComplet();
                repo.updateChauffeur(c.id, c, new RepoCallback<Chauffeur>() {
                    @Override public void onResult(Chauffeur x) { finishWithSuccess(); }
                    @Override public void onError(String m) { finishWithSuccess(); }
                });
            }
            @Override public void onError(String m) { finishWithSuccess(); }
        });
    }

    private void finishWithSuccess() {
        Toast.makeText(this, R.string.saved_toast, Toast.LENGTH_SHORT).show();
        finish();
    }
}
