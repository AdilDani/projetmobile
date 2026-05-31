package com.fleettracking.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.fleettracking.app.R;
import com.fleettracking.app.model.Entretien;

import java.util.List;

public class EntretienAdapter extends RecyclerView.Adapter<EntretienAdapter.VH> {

    private final List<Entretien> items;

    public EntretienAdapter(List<Entretien> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_entretien, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Entretien e = items.get(position);
        h.type.setText(e.type);
        h.vehicle.setText(e.vehiculeNom + "  ·  " + e.immatriculation);
        h.date.setText(e.date);
        h.echeance.setText(e.echeance);
        int color = e.aVenir ? R.color.warning : R.color.text_tertiary;
        h.echeance.setTextColor(ContextCompat.getColor(h.echeance.getContext(), color));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView type, vehicle, date, echeance;
        VH(@NonNull View v) {
            super(v);
            type = v.findViewById(R.id.entretien_type);
            vehicle = v.findViewById(R.id.entretien_vehicle);
            date = v.findViewById(R.id.entretien_date);
            echeance = v.findViewById(R.id.entretien_echeance);
        }
    }
}
