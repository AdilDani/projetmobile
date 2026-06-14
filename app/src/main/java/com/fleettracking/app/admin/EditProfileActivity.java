package com.fleettracking.app.admin;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.fleettracking.app.R;
import com.fleettracking.app.util.Prefs;

public class EditProfileActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_edit_profile);

        ((TextView) findViewById(R.id.toolbar_title)).setText(R.string.profile_personal_info);
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        Prefs prefs = new Prefs(this);
        EditText name = findViewById(R.id.input_name);
        EditText email = findViewById(R.id.input_email);

        String savedName = prefs.getName();
        name.setText(savedName == null || savedName.isEmpty() ? getString(R.string.admin_name) : savedName);
        email.setText(prefs.getEmail());

        findViewById(R.id.btn_save).setOnClickListener(v -> {
            prefs.saveSession(prefs.getRole(), email.getText().toString(),
                    prefs.getUserId(), name.getText().toString());
            Toast.makeText(this, R.string.saved_toast, Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
