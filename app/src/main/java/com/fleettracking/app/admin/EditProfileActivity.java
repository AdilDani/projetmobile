package com.fleettracking.app.admin;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.fleettracking.app.R;

public class EditProfileActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_edit_profile);

        ((TextView) findViewById(R.id.toolbar_title)).setText(R.string.profile_personal_info);
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        EditText name = findViewById(R.id.input_name);
        EditText email = findViewById(R.id.input_email);
        
        name.setText(R.string.admin_name);
        email.setText("admin@fleettracking.com");

        findViewById(R.id.btn_save).setOnClickListener(v -> {
            Toast.makeText(this, R.string.saved_toast, Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
