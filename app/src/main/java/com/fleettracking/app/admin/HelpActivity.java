package com.fleettracking.app.admin;

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
        ((TextView) findViewById(R.id.text_content)).setText("Besoin d'aide ?\n\nContactez le support technique :\nEmail: support@fleettracking.com\nTél: +212 5 22 00 00 00\n\nGuide d'utilisation :\nConsultez notre documentation en ligne.");
    }
}
