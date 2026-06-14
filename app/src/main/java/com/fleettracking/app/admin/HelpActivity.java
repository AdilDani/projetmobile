package com.fleettracking.app.admin;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.fleettracking.app.R;

public class HelpActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_text);
        ((TextView) findViewById(R.id.toolbar_title)).setText(R.string.profile_help);
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        ((TextView) findViewById(R.id.text_content)).setText(
                "Besoin d'aide ?\n\nPour toute question ou problème technique, contactez l'administrateur :");

        findViewById(R.id.btn_contact).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SENDTO,
                    Uri.parse("mailto:adil.dani@uit.ac.ma"));
            intent.putExtra(Intent.EXTRA_SUBJECT, "FleetTracking – Demande d'aide");
            startActivity(Intent.createChooser(intent, "Envoyer un email"));
        });
    }
}
