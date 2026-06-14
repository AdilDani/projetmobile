package com.fleettracking.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fleettracking.app.R;
import com.fleettracking.app.model.Trajet;

import java.util.List;

public class TrajetAdapter extends RecyclerView.Adapter<TrajetAdapter.VH> {

    public interface OnTrajetClick { void onClick(Trajet t); }

    private final List<Trajet> items;
    private final OnTrajetClick listener;

    public TrajetAdapter(List<Trajet> items, OnTrajetClick listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_trajet, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Trajet t = items.get(position);
        h.date.setText(t.date);
        h.vehicle.setText(t.vehiculeNom);
        h.distance.setText(t.distanceKm + " km");
        h.consumption.setText(String.format("%.1f L/100km", t.consommation));
        h.itemView.setOnClickListener(v -> { if (listener != null) listener.onClick(t); });
    }

    @Override public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView date, vehicle, distance, consumption;
        VH(@NonNull View v) {
            super(v);
            date = v.findViewById(R.id.trip_date);
            vehicle = v.findViewById(R.id.trip_vehicle);
            distance = v.findViewById(R.id.trip_distance);
            consumption = v.findViewById(R.id.trip_consumption);
        }
    }
}
