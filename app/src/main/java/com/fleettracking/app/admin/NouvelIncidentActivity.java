package com.fleettracking.app.admin;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.fleettracking.app.R;
import com.fleettracking.app.data.RepoCallback;
import com.fleettracking.app.data.Repository;
import com.fleettracking.app.model.Incident;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NouvelIncidentActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nouvel_incident);

        ((TextView) findViewById(R.id.toolbar_title)).setText(R.string.new_incident_title);
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        Repository repo = new Repository(this);
        EditText vehicle = findViewById(R.id.input_vehicle);
        Spinner type = findViewById(R.id.spinner_type);
        EditText description = findViewById(R.id.input_description);

        findViewById(R.id.btn_save).setOnClickListener(v -> {
            String vehName = vehicle.getText().toString().trim();
            String typeStr = type.getSelectedItem() != null ? type.getSelectedItem().toString() : "";
            String desc = description.getText().toString().trim();
            String today = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());

            Incident incident = new Incident(null, vehName, "", typeStr, desc, today, "En cours");
            repo.createIncident(incident, new RepoCallback<Incident>() {
                @Override public void onResult(Incident saved) {
                    Toast.makeText(NouvelIncidentActivity.this, R.string.incident_sent_toast, Toast.LENGTH_SHORT).show();
                    finish();
                }
                @Override public void onError(String message) {
                    Toast.makeText(NouvelIncidentActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}
