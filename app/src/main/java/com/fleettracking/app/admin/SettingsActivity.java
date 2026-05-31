package com.fleettracking.app.admin;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.fleettracking.app.R;

public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_text);
        ((TextView) findViewById(R.id.toolbar_title)).setText(R.string.profile_settings);
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        ((TextView) findViewById(R.id.text_content)).setText("Paramètres de l'application :\n\n- Langue : Français\n- Unités : Kilomètres (km)\n- Thème : Clair\n- Version : 1.0.0");
    }
}
