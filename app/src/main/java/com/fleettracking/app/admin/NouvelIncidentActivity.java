package com.fleettracking.app.admin;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Deprecated: incidents are now declared by chauffeurs (see
 * {@link com.fleettracking.app.chauffeur.DeclarerIncidentActivity}), not
 * created by the admin. This class is kept only as a harmless stub because
 * the source file cannot be removed from the workspace; it is no longer
 * launched anywhere and immediately finishes if started.
 */
public class NouvelIncidentActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        finish();
    }
}
