package com.fleettracking.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fleettracking.app.R;
import com.fleettracking.app.model.Incident;
import com.fleettracking.app.util.UiUtils;

import java.util.List;

public class IncidentAdapter extends RecyclerView.Adapter<IncidentAdapter.VH> {

    public interface OnIncidentClickListener {
        void onIncidentClick(Incident incident);
    }

    private final List<Incident> items;
    private final OnIncidentClickListener listener;

    public IncidentAdapter(List<Incident> items, OnIncidentClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_incident, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Incident in = items.get(position);
        h.type.setText(in.type);
        h.vehicle.setText(in.vehiculeNom + "  ·  " + in.immatriculation);
        h.description.setText(in.description);
        h.date.setText(in.date);
        UiUtils.applyStatusChip(h.status, in.statut);
        
        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onIncidentClick(in);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView type, vehicle, description, date, status;
        VH(@NonNull View v) {
            super(v);
            type = v.findViewById(R.id.incident_type);
            vehicle = v.findViewById(R.id.incident_vehicle);
            description = v.findViewById(R.id.incident_description);
            date = v.findViewById(R.id.incident_date);
            status = v.findViewById(R.id.incident_status);
        }
    }
}
