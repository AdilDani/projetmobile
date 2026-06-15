package com.fleettracking.app.admin;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.fleettracking.app.R;
import com.fleettracking.app.data.RepoCallback;
import com.fleettracking.app.data.Repository;
import com.fleettracking.app.model.Entretien;
import com.fleettracking.app.model.Vehicule;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CreateEntretienActivity extends AppCompatActivity {

    private Repository repo;
    private final List<Vehicule> vehicules = new ArrayList<>();
    private Spinner spinnerVehicule;
    private EditText inputType, inputTargetDate, inputTargetKm;
    private RadioGroup radioBasis;
    private boolean kmBased = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_entretien);

        repo = new Repository(this);

        ((TextView) findViewById(R.id.toolbar_title)).setText(R.string.add_entretien_title);
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        spinnerVehicule = findViewById(R.id.spinner_vehicule);
        inputType = findViewById(R.id.input_type);
        inputTargetDate = findViewById(R.id.input_target_date);
        inputTargetKm = findViewById(R.id.input_target_km);
        radioBasis = findViewById(R.id.radio_basis);
        TextView labelTarget = findViewById(R.id.label_target);

        inputTargetDate.setOnClickListener(v -> showDatePicker());

        radioBasis.setOnCheckedChangeListener((group, checkedId) -> {
            kmBased = checkedId == R.id.radio_km;
            if (kmBased) {
                inputTargetDate.setVisibility(View.GONE);
                inputTargetKm.setVisibility(View.VISIBLE);
                labelTarget.setText(R.string.label_target_km);
            } else {
                inputTargetDate.setVisibility(View.VISIBLE);
                inputTargetKm.setVisibility(View.GONE);
                labelTarget.setText(R.string.label_target_date);
            }
        });

        repo.getVehicules(new RepoCallback<List<Vehicule>>() {
            @Override public void onResult(List<Vehicule> list) {
                vehicules.clear();
                vehicules.addAll(list);
                List<String> names = new ArrayList<>();
                for (Vehicule v : list) names.add(v.getNomComplet() + " — " + v.immatriculation);
                ArrayAdapter<String> ad = new ArrayAdapter<>(CreateEntretienActivity.this,
                        android.R.layout.simple_spinner_item, names);
                ad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerVehicule.setAdapter(ad);
            }
            @Override public void onError(String message) {
                Toast.makeText(CreateEntretienActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.btn_save).setOnClickListener(v -> save());
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, day) -> {
            String date = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, day);
            inputTargetDate.setText(date);
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void save() {
        int pos = spinnerVehicule.getSelectedItemPosition();
        if (vehicules.isEmpty() || pos < 0 || pos >= vehicules.size()) {
            Toast.makeText(this, R.string.field_vehicle, Toast.LENGTH_SHORT).show();
            return;
        }
        String type = inputType.getText().toString().trim();
        if (type.isEmpty()) {
            Toast.makeText(this, R.string.label_entretien_type, Toast.LENGTH_SHORT).show();
            return;
        }

        Vehicule v = vehicules.get(pos);
        Entretien e = new Entretien();
        e.vehiculeId = v.id;
        e.type = type;
        e.estKmBase = kmBased;

        if (kmBased) {
            try {
                e.cibleKm = Integer.parseInt(inputTargetKm.getText().toString().trim());
            } catch (NumberFormatException ex) {
                Toast.makeText(this, R.string.label_target_km, Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            e.cibleDate = inputTargetDate.getText().toString().trim();
            if (e.cibleDate.isEmpty()) {
                Toast.makeText(this, R.string.label_target_date, Toast.LENGTH_SHORT).show();
                return;
            }
        }

        repo.createEntretien(e, new RepoCallback<Entretien>() {
            @Override public void onResult(Entretien created) {
                setResult(RESULT_OK);
                finish();
            }
            @Override public void onError(String message) {
                Toast.makeText(CreateEntretienActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
