package com.fleettracking.app.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;

import com.fleettracking.app.LoginActivity;
import com.fleettracking.app.R;
import com.fleettracking.app.util.Prefs;

public class ProfilActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profil);

        ((TextView) findViewById(R.id.toolbar_title)).setText(R.string.profile_title);
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        configRow(R.id.row_personal, R.drawable.ic_person, R.string.profile_personal_info, false);
        configRow(R.id.row_settings, R.drawable.ic_settings, R.string.profile_settings, false);
        configRow(R.id.row_notifications, R.drawable.ic_bell, R.string.profile_notifications, true);
        configRow(R.id.row_help, R.drawable.ic_help, R.string.profile_help, false);
        View logout = configRow(R.id.row_logout, R.drawable.ic_logout, R.string.profile_logout, false);

        int danger = ContextCompat.getColor(this, R.color.danger);
        ((ImageView) logout.findViewById(R.id.menu_icon)).setColorFilter(danger);
        ((TextView) logout.findViewById(R.id.menu_label)).setTextColor(danger);

        Prefs prefs = new Prefs(this);
        SwitchCompat notifSwitch = findViewById(R.id.row_notifications)
                .findViewById(R.id.menu_switch);
        notifSwitch.setChecked(prefs.isNotificationsEnabled());
        notifSwitch.setOnCheckedChangeListener((b, checked) -> prefs.setNotificationsEnabled(checked));

        findViewById(R.id.row_personal).setOnClickListener(x -> startActivity(new Intent(this, EditProfileActivity.class)));
        findViewById(R.id.row_settings).setOnClickListener(x -> startActivity(new Intent(this, SettingsActivity.class)));
        findViewById(R.id.row_help).setOnClickListener(x -> startActivity(new Intent(this, HelpActivity.class)));
        logout.setOnClickListener(x -> doLogout());
    }

    private View configRow(int rowId, int iconRes, int labelRes, boolean isSwitch) {
        View row = findViewById(rowId);
        ((ImageView) row.findViewById(R.id.menu_icon)).setImageResource(iconRes);
        ((TextView) row.findViewById(R.id.menu_label)).setText(labelRes);
        if (isSwitch) {
            row.findViewById(R.id.menu_switch).setVisibility(View.VISIBLE);
            row.findViewById(R.id.menu_chevron).setVisibility(View.GONE);
        }
        return row;
    }

    private void toast(int res) {
        Toast.makeText(this, res, Toast.LENGTH_SHORT).show();
    }

    private void doLogout() {
        new Prefs(this).logout();
        Intent i = new Intent(this, LoginActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }
}
