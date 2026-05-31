package com.fleettracking.app.admin;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.fleettracking.app.R;
import com.fleettracking.app.data.RepoCallback;
import com.fleettracking.app.data.Repository;
import com.fleettracking.app.model.Chauffeur;
import com.fleettracking.app.util.UiUtils;

public class ChauffeurDetailsActivity extends AppCompatActivity {

    public static final String EXTRA_CHAUFFEUR_ID = "extra_chauffeur_id";

    private Repository repo;
    private Chauffeur current;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chauffeur_details);

        repo = new Repository(this);

        ((TextView) findViewById(R.id.toolbar_title)).setText(R.string.chauffeur_details_title);
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        String id = getIntent().getStringExtra(EXTRA_CHAUFFEUR_ID);
        repo.getChauffeur(id == null ? "c1" : id, new RepoCallback<Chauffeur>() {
            @Override public void onResult(Chauffeur c) { current = c; bind(c); }
            @Override public void onError(String message) {
                Toast.makeText(ChauffeurDetailsActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void bind(Chauffeur c) {
        ((TextView) findViewById(R.id.text_chauffeur_name)).setText(c.nom);
        TextView status = findViewById(R.id.text_chauffeur_status);
        status.setText(c.statut);
        status.setTextColor(UiUtils.statusColor(this, c.statut));

        EditText name = findViewById(R.id.input_name);
        EditText phone = findViewById(R.id.input_phone);
        EditText email = findViewById(R.id.input_email);
        EditText vehicle = findViewById(R.id.input_vehicle);
        EditText license = findViewById(R.id.input_license);
        EditText login = findViewById(R.id.input_login);
        EditText password = findViewById(R.id.input_password);

        name.setText(c.nom);
        phone.setText(c.telephone);
        email.setText(c.email);
        vehicle.setText(c.vehiculeAffecte);
        license.setText(c.permis);
        login.setText(c.login);
        password.setText(c.password);

        findViewById(R.id.btn_save).setOnClickListener(v -> {
            c.nom = name.getText().toString();
            c.telephone = phone.getText().toString();
            c.email = email.getText().toString();
            c.vehiculeAffecte = vehicle.getText().toString();
            c.permis = license.getText().toString();
            c.login = login.getText().toString();
            c.password = password.getText().toString();
            repo.updateChauffeur(c.id, c, new RepoCallback<Chauffeur>() {
                @Override public void onResult(Chauffeur saved) {
                    ((TextView) findViewById(R.id.text_chauffeur_name)).setText(saved.nom);
                    Toast.makeText(ChauffeurDetailsActivity.this, R.string.saved_toast, Toast.LENGTH_SHORT).show();
                }
                @Override public void onError(String message) {
                    Toast.makeText(ChauffeurDetailsActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}
