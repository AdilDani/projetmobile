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
import java.util.Locale;

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
        h.vehicle.setText(t.vehiculeNom != null ? t.vehiculeNom : "—");
        h.date.setText(t.date != null ? t.date : "");
        if (t.heureDepart != null && t.heureArrivee != null) {
            h.times.setText(t.heureDepart + " → " + t.heureArrivee);
        } else if (t.heureDepart != null) {
            h.times.setText(t.heureDepart);
        } else {
            h.times.setText("");
        }
        h.distance.setText(String.format(Locale.getDefault(), "%.1f km", t.distanceKm));
        h.duration.setText(t.duree != null ? t.duree : "—");
        h.itemView.setOnClickListener(v -> { if (listener != null) listener.onClick(t); });
    }

    @Override public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView vehicle, date, times, distance, duration;
        VH(@NonNull View v) {
            super(v);
            vehicle  = v.findViewById(R.id.trip_vehicle);
            date     = v.findViewById(R.id.trip_date);
            times    = v.findViewById(R.id.trip_times);
            distance = v.findViewById(R.id.trip_distance);
            duration = v.findViewById(R.id.trip_duration);
        }
    }
}
