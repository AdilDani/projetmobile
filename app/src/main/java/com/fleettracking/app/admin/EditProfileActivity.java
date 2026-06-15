package com.fleettracking.app.admin;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.fleettracking.app.R;
import com.fleettracking.app.data.RepoCallback;
import com.fleettracking.app.data.Repository;
import com.fleettracking.app.model.AdminUser;
import com.fleettracking.app.model.Chauffeur;
import com.fleettracking.app.util.Prefs;

public class EditProfileActivity extends AppCompatActivity {

    private EditText inputPrenom, inputNom, inputLogin, inputPassword;
    private Repository repo;
    private Prefs prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_edit_profile);

        ((TextView) findViewById(R.id.toolbar_title)).setText(R.string.profile_personal_info);
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        repo = new Repository(this);
        prefs = new Prefs(this);

        inputPrenom   = findViewById(R.id.input_prenom);
        inputNom      = findViewById(R.id.input_nom);
        inputLogin    = findViewById(R.id.input_login);
        inputPassword = findViewById(R.id.input_password);

        String userId = prefs.getUserId();
        String role   = prefs.getRole();

        if ("admin".equals(role)) {
            loadAdmin(userId);
        } else {
            loadChauffeur(userId);
        }

        findViewById(R.id.btn_save).setOnClickListener(v -> {
            if ("admin".equals(role)) saveAdmin(userId);
            else saveChauffeur(userId);
        });
    }

    // ── Admin ──────────────────────────────────────────────────────────────

    private void loadAdmin(String id) {
        repo.getAdmin(id, new RepoCallback<AdminUser>() {
            @Override public void onResult(AdminUser a) {
                splitNom(a.nom);
                inputLogin.setText(a.login);
                inputPassword.setText(a.password);
            }
            @Override public void onError(String msg) {
                // Fall back to whatever Prefs has
                splitNom(prefs.getName());
                inputLogin.setText(prefs.getEmail());
            }
        });
    }

    private void saveAdmin(String id) {
        AdminUser a = new AdminUser();
        a.id       = id;
        a.nom      = joinNom();
        a.login    = inputLogin.getText().toString().trim();
        a.password = inputPassword.getText().toString();

        if (a.login.isEmpty()) {
            Toast.makeText(this, R.string.label_login, Toast.LENGTH_SHORT).show();
            return;
        }

        repo.updateAdmin(id, a, new RepoCallback<AdminUser>() {
            @Override public void onResult(AdminUser saved) {
                prefs.saveSession(prefs.getRole(), saved.login, saved.id, saved.nom);
                Toast.makeText(EditProfileActivity.this, R.string.saved_toast, Toast.LENGTH_SHORT).show();
                finish();
            }
            @Override public void onError(String msg) {
                Toast.makeText(EditProfileActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ── Chauffeur ──────────────────────────────────────────────────────────

    private void loadChauffeur(String id) {
        repo.getChauffeur(id, new RepoCallback<Chauffeur>() {
            @Override public void onResult(Chauffeur c) {
                splitNom(c.nom);
                inputLogin.setText(c.login);
                inputPassword.setText(c.password);
            }
            @Override public void onError(String msg) {
                splitNom(prefs.getName());
                inputLogin.setText(prefs.getEmail());
            }
        });
    }

    private void saveChauffeur(String id) {
        repo.getChauffeur(id, new RepoCallback<Chauffeur>() {
            @Override public void onResult(Chauffeur c) {
                c.nom      = joinNom();
                c.login    = inputLogin.getText().toString().trim();
                c.password = inputPassword.getText().toString();

                if (c.login.isEmpty()) {
                    Toast.makeText(EditProfileActivity.this, R.string.label_login, Toast.LENGTH_SHORT).show();
                    return;
                }

                repo.updateChauffeur(id, c, new RepoCallback<Chauffeur>() {
                    @Override public void onResult(Chauffeur saved) {
                        prefs.saveSession(prefs.getRole(), saved.login, saved.id, saved.nom);
                        Toast.makeText(EditProfileActivity.this, R.string.saved_toast, Toast.LENGTH_SHORT).show();
                        finish();
                    }
                    @Override public void onError(String msg) {
                        Toast.makeText(EditProfileActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });
            }
            @Override public void onError(String msg) {
                Toast.makeText(EditProfileActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    /** Split "Ahmed Benali" → prénom="Ahmed", nom="Benali". */
    private void splitNom(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) return;
        String[] parts = fullName.trim().split("\\s+", 2);
        inputPrenom.setText(parts[0]);
        inputNom.setText(parts.length > 1 ? parts[1] : "");
    }

    /** Recombine prénom + nom into a single full name string. */
    private String joinNom() {
        String p = inputPrenom.getText().toString().trim();
        String n = inputNom.getText().toString().trim();
        return (p + " " + n).trim();
    }
}
