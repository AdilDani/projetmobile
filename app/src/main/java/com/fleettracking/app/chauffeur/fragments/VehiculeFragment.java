package com.fleettracking.app.chauffeur.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.fleettracking.app.R;
import com.fleettracking.app.data.RepoCallback;
import com.fleettracking.app.data.Repository;
import com.fleettracking.app.model.Vehicule;
import com.fleettracking.app.util.ImageUtils;
import com.fleettracking.app.util.Prefs;

public class VehiculeFragment extends Fragment {

    private LinearLayout container;
    private LayoutInflater inflater;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chauffeur_vehicule, container, false);
    }

    private ImageView imgHeader;

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        this.inflater = LayoutInflater.from(getContext());
        imgHeader = v.findViewById(R.id.img_vehicle_header);
        container = v.findViewById(R.id.details_container);

        Repository repo = new Repository(requireContext());
        String userId = new Prefs(requireContext()).getUserId();
        repo.getCurrentVehicule(userId, new RepoCallback<Vehicule>() {
            @Override public void onResult(Vehicule x) { if (isAdded()) bind(x); }
            @Override public void onError(String message) { /* nothing to show */ }
        });
    }

    private void bind(Vehicule x) {
        ImageUtils.bind(imgHeader, x.photo, R.drawable.ic_truck);
        container.removeAllViews();
        int textPrimary = ContextCompat.getColor(requireContext(), R.color.text_primary);
        int warning = ContextCompat.getColor(requireContext(), R.color.warning);
        int success = ContextCompat.getColor(requireContext(), R.color.success);

        addRow(R.drawable.ic_id_card, R.string.label_plate, x.immatriculation, textPrimary);
        addRow(R.drawable.ic_truck, R.string.label_brand, x.marque, textPrimary);
        addRow(R.drawable.ic_truck, R.string.label_model, x.modele, textPrimary);
        addRow(R.drawable.ic_calendar, R.string.label_year, String.valueOf(x.annee), textPrimary);
        addRow(R.drawable.ic_speed, R.string.label_mileage,
                String.format("%,d km", x.kilometrage), textPrimary);
        addFuelRow(x.carburantPct);
        String vidange = x.vidangeCibleKm > 0 ? getString(R.string.service_in_km, String.valueOf(x.vidangeCibleKm) + " km") : "--";
        addRow(R.drawable.ic_wrench, R.string.label_next_service, vidange, warning);
        String ct = (x.controleTechniqueDate != null && !x.controleTechniqueDate.isEmpty()) ? x.controleTechniqueDate : "--";
        addRow(R.drawable.ic_check_circle, R.string.label_technical_check, ct, success);
    }

    private View addRow(int iconRes, int labelRes, String value, int valueColor) {
        View row = inflater.inflate(R.layout.item_detail_row, container, false);
        ((ImageView) row.findViewById(R.id.row_icon)).setImageResource(iconRes);
        ((TextView) row.findViewById(R.id.row_label)).setText(labelRes);
        TextView val = row.findViewById(R.id.row_value);
        val.setText(value);
        val.setTextColor(valueColor);
        container.addView(row);
        addDivider();
        return row;
    }

    private void addFuelRow(int pct) {
        int textPrimary = ContextCompat.getColor(requireContext(), R.color.text_primary);
        View row = addRow(R.drawable.ic_fuel, R.string.label_fuel, pct + "%", textPrimary);

        ProgressBar bar = new ProgressBar(getContext(), null,
                android.R.attr.progressBarStyleHorizontal);
        bar.setMax(100);
        bar.setProgress(pct);
        bar.getProgressDrawable().setColorFilter(
                ContextCompat.getColor(requireContext(), R.color.primary),
                android.graphics.PorterDuff.Mode.SRC_IN);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        container.addView(bar, container.indexOfChild(row) + 1, lp);
    }

    private void addDivider() {
        View d = new View(getContext());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 1);
        d.setLayoutParams(lp);
        d.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.divider));
        container.addView(d);
    }
}
