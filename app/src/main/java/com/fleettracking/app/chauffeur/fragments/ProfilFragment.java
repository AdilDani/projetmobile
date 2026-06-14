package com.fleettracking.app.chauffeur.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.fleettracking.app.LoginActivity;
import com.fleettracking.app.R;
import com.fleettracking.app.admin.EditProfileActivity;
import com.fleettracking.app.admin.HelpActivity;
import com.fleettracking.app.admin.SettingsActivity;
import com.fleettracking.app.util.Prefs;

public class ProfilFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chauffeur_profil, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        ((TextView) v.findViewById(R.id.text_name)).setText(new Prefs(requireContext()).getName());

        configRow(v, R.id.row_personal, R.drawable.ic_person, R.string.profile_personal_info, false);
        configRow(v, R.id.row_settings, R.drawable.ic_settings, R.string.profile_settings, false);
        configRow(v, R.id.row_notifications, R.drawable.ic_bell, R.string.profile_notifications, true);
        configRow(v, R.id.row_help, R.drawable.ic_help, R.string.profile_help, false);
        View logout = configRow(v, R.id.row_logout, R.drawable.ic_logout, R.string.profile_logout, false);

        int danger = ContextCompat.getColor(requireContext(), R.color.danger);
        ((ImageView) logout.findViewById(R.id.menu_icon)).setColorFilter(danger);
        ((TextView) logout.findViewById(R.id.menu_label)).setTextColor(danger);

        Prefs prefs = new Prefs(requireContext());
        SwitchCompat notifSwitch = v.findViewById(R.id.row_notifications)
                .findViewById(R.id.menu_switch);
        notifSwitch.setChecked(prefs.isNotificationsEnabled());
        notifSwitch.setOnCheckedChangeListener((b, checked) -> prefs.setNotificationsEnabled(checked));

        v.findViewById(R.id.row_personal).setOnClickListener(x ->
                startActivity(new Intent(getContext(), EditProfileActivity.class)));
        v.findViewById(R.id.row_settings).setOnClickListener(x ->
                startActivity(new Intent(getContext(), SettingsActivity.class)));
        v.findViewById(R.id.row_help).setOnClickListener(x ->
                startActivity(new Intent(getContext(), HelpActivity.class)));
        logout.setOnClickListener(x -> doLogout());
    }

    private View configRow(View root, int rowId, int iconRes, int labelRes, boolean isSwitch) {
        View row = root.findViewById(rowId);
        ((ImageView) row.findViewById(R.id.menu_icon)).setImageResource(iconRes);
        ((TextView) row.findViewById(R.id.menu_label)).setText(labelRes);
        if (isSwitch) {
            row.findViewById(R.id.menu_switch).setVisibility(View.VISIBLE);
            row.findViewById(R.id.menu_chevron).setVisibility(View.GONE);
        }
        return row;
    }

    private void doLogout() {
        new Prefs(requireContext()).logout();
        Intent i = new Intent(getContext(), LoginActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        requireActivity().finish();
    }
}
