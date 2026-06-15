package com.fleettracking.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fleettracking.app.R;
import com.fleettracking.app.model.Vehicule;
import com.fleettracking.app.util.ImageUtils;
import com.fleettracking.app.util.UiUtils;

import java.util.List;

public class VehiculeAdapter extends RecyclerView.Adapter<VehiculeAdapter.VH> {

    public interface OnVehiculeClick {
        void onClick(Vehicule v);
    }

    private final List<Vehicule> items;
    private final OnVehiculeClick listener;

    public VehiculeAdapter(List<Vehicule> items, OnVehiculeClick listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_vehicule, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Vehicule v = items.get(position);
        h.name.setText(v.getNomComplet());
        h.plate.setText(v.immatriculation);
        UiUtils.applyStatusChip(h.status, v.statut);
        ImageUtils.bind(h.image, v.photo, R.drawable.ic_truck);
        h.itemView.setOnClickListener(x -> {
            if (listener != null) listener.onClick(v);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView image;
        TextView name, plate, status;
        VH(@NonNull View v) {
            super(v);
            image = v.findViewById(R.id.vehicle_image);
            name = v.findViewById(R.id.vehicle_name);
            plate = v.findViewById(R.id.vehicle_plate);
            status = v.findViewById(R.id.vehicle_status);
        }
    }
}
