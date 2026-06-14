package com.fleettracking.app;

import android.content.Intent;
import android.os.Bundle;
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

/**
 * Shared entry point. Credentials are validated against the Spring backend
 * (POST /api/auth/login); the role returned by the server decides whether the
 * user lands in the Admin dashboard or the Chauffeur space.
 */
public class LoginActivity extends AppCompatActivity {

    private MaterialButtonToggleGroup toggleRole;
    private EditText inputLogin;
    private EditText inputPassword;
    private MaterialButton btnLogin;
    private Repository repo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        repo = new Repository(this);

        toggleRole = findViewById(R.id.toggle_role);
        toggleRole.check(R.id.btn_role_admin);

        inputLogin = findViewById(R.id.input_email);
        inputPassword = findViewById(R.id.input_password);

        btnLogin = findViewById(R.id.btn_login);
        btnLogin.setOnClickListener(v -> doLogin());

        findViewById(R.id.text_signup).setOnClickListener(v ->
                Toast.makeText(this, R.string.login_signup, Toast.LENGTH_SHORT).show());
        findViewById(R.id.text_forgot).setOnClickListener(v ->
                Toast.makeText(this, R.string.login_forgot, Toast.LENGTH_SHORT).show());
    }

    private void doLogin() {
        String login = inputLogin.getText().toString().trim();
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

                // The credentials are valid, but they must match the selected role.
                if (isChauffeur != wantsChauffeur) {
                    Toast.makeText(LoginActivity.this,
                            R.string.login_role_mismatch, Toast.LENGTH_SHORT).show();
                    return;
                }

                new Prefs(LoginActivity.this)
                        .saveSession(res.role, login, res.userId, res.name);

                Intent intent = isChauffeur
                        ? new Intent(LoginActivity.this, ChauffeurMainActivity.class)
                        : new Intent(LoginActivity.this, AdminMainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }

            @Override public void onError(String message) {
                btnLogin.setEnabled(true);
                Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
