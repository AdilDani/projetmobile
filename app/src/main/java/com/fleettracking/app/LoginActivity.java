package com.fleettracking.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.fleettracking.app.admin.AdminMainActivity;
import com.fleettracking.app.chauffeur.ChauffeurMainActivity;
import com.fleettracking.app.data.LoginResponse;
import com.fleettracking.app.data.RepoCallback;
import com.fleettracking.app.data.Repository;
import com.fleettracking.app.util.Prefs;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;

public class LoginActivity extends AppCompatActivity {

    private MaterialButtonToggleGroup toggleRole;
    private EditText inputLogin;
    private EditText inputPassword;
    private CheckBox checkRemember;
    private MaterialButton btnLogin;
    private Repository repo;
    private Prefs prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        repo = new Repository(this);
        prefs = new Prefs(this);

        toggleRole = findViewById(R.id.toggle_role);
        toggleRole.check(R.id.btn_role_admin);

        inputLogin    = findViewById(R.id.input_email);
        inputPassword = findViewById(R.id.input_password);
        checkRemember = findViewById(R.id.checkbox_remember);
        btnLogin      = findViewById(R.id.btn_login);

        btnLogin.setOnClickListener(v -> doLogin(false));

        // If saved credentials exist, attempt silent auto-login
        String savedLogin    = prefs.getSavedLogin();
        String savedPassword = prefs.getSavedPassword();
        String savedRole     = prefs.getSavedRole();
        if (savedLogin != null && savedPassword != null) {
            checkRemember.setChecked(true);
            // Pre-select the saved role in the toggle
            if (Prefs.ROLE_CHAUFFEUR.equals(savedRole)) {
                toggleRole.check(R.id.btn_role_chauffeur);
            }
            autoLogin(savedLogin, savedPassword, savedRole);
        }
    }

    private void autoLogin(String login, String password, String expectedRole) {
        repo.login(login, password, new RepoCallback<LoginResponse>() {
            @Override public void onResult(LoginResponse res) {
                // Only proceed if role matches what was saved
                if (!res.role.equals(expectedRole)) return;
                prefs.saveSession(res.role, login, res.userId, res.name);
                navigateTo(res.role);
            }
            @Override public void onError(String message) {
                // Silent fail — credentials may be stale, just show the form
            }
        });
    }

    private void doLogin(boolean silent) {
        String login    = inputLogin.getText().toString().trim();
        String password = inputPassword.getText().toString();
        if (login.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, R.string.login_missing_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        boolean wantsChauffeur = toggleRole.getCheckedButtonId() == R.id.btn_role_chauffeur;

        btnLogin.setEnabled(false);
        repo.login(login, password, new RepoCallback<LoginResponse>() {
            @Override public void onResult(LoginResponse res) {
                btnLogin.setEnabled(true);
                boolean isChauffeur = Prefs.ROLE_CHAUFFEUR.equals(res.role);

                if (isChauffeur != wantsChauffeur) {
                    Toast.makeText(LoginActivity.this,
                            R.string.login_role_mismatch, Toast.LENGTH_SHORT).show();
                    return;
                }

                prefs.saveSession(res.role, login, res.userId, res.name);

                if (checkRemember.isChecked()) {
                    prefs.saveCredentials(login, password, res.role);
                } else {
                    prefs.clearCredentials();
                }

                navigateTo(res.role);
            }

            @Override public void onError(String message) {
                btnLogin.setEnabled(true);
                Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateTo(String role) {
        boolean isChauffeur = Prefs.ROLE_CHAUFFEUR.equals(role);
        Intent intent = isChauffeur
                ? new Intent(this, ChauffeurMainActivity.class)
                : new Intent(this, AdminMainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
