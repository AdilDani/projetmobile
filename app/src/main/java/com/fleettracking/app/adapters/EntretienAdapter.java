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
import com.fleettracking.app.model.Vehicule;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class EntretienAdapter extends RecyclerView.Adapter<EntretienAdapter.VH> {

    public interface OnMarkDoneListener {
        void onMarkDone(Entretien e);
    }

    private final List<Entretien> items;
    private final Map<String, Vehicule> vehiculeMap;
    private final OnMarkDoneListener listener;

    public EntretienAdapter(List<Entretien> items, Map<String, Vehicule> vehiculeMap, OnMarkDoneListener listener) {
        this.items = items;
        this.vehiculeMap = vehiculeMap;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_entretien, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Entretien e = items.get(position);
        h.type.setText(e.type != null ? e.type : "—");
        h.vehicle.setText((e.vehiculeNom != null ? e.vehiculeNom : "") + "  ·  " + (e.immatriculation != null ? e.immatriculation : ""));

        boolean isHistorique = "effectue".equals(e.statut);

        if (isHistorique) {
            String done = e.dateEffectuee != null ? e.dateEffectuee : "—";
            h.sub.setText(h.sub.getContext().getString(R.string.done_on, done));
            if (e.retardJours > 0) {
                h.badge.setText(h.badge.getContext().getString(R.string.overdue_by_days, e.retardJours));
                h.badge.setBackgroundResource(R.drawable.bg_badge_red);
            } else {
                h.badge.setText(R.string.on_time);
                h.badge.setBackgroundResource(R.drawable.bg_badge_green);
            }
            h.btnMarkDone.setVisibility(View.GONE);
        } else {
            // à venir
            int urgency = 0; // 0=green, 1=orange, 2=red
            String badgeText;
            boolean overdue = false;

            if (e.estKmBase) {
                Vehicule v = vehiculeMap.get(e.vehiculeId);
                int currentKm = v != null ? v.kilometrage : 0;
                int remaining = e.cibleKm - currentKm;
                if (remaining <= 0) {
                    badgeText = h.badge.getContext().getString(R.string.overdue_by_km, -remaining);
                    urgency = 2;
                    overdue = true;
                } else if (remaining <= 500) {
                    badgeText = h.badge.getContext().getString(R.string.due_in_km, remaining);
                    urgency = 1;
                } else {
                    badgeText = h.badge.getContext().getString(R.string.due_in_km, remaining);
                    urgency = 0;
                }
                h.sub.setText("Cible : " + e.cibleKm + " km");
            } else {
                int daysRemaining = daysUntil(e.cibleDate);
                if (daysRemaining < 0) {
                    badgeText = h.badge.getContext().getString(R.string.overdue_by_days, -daysRemaining);
                    urgency = 2;
                    overdue = true;
                } else if (daysRemaining == 0) {
                    badgeText = h.badge.getContext().getString(R.string.due_today);
                    urgency = 2;
                    overdue = true;
                } else if (daysRemaining <= 7) {
                    badgeText = h.badge.getContext().getString(R.string.due_in_days, daysRemaining);
                    urgency = 1;
                } else {
                    badgeText = h.badge.getContext().getString(R.string.due_in_days, daysRemaining);
                    urgency = 0;
                }
                h.sub.setText("Cible : " + (e.cibleDate != null ? e.cibleDate : "—"));
            }

            h.badge.setText(badgeText);
            if (urgency == 2) h.badge.setBackgroundResource(R.drawable.bg_badge_red);
            else if (urgency == 1) h.badge.setBackgroundResource(R.drawable.bg_badge_orange);
            else h.badge.setBackgroundResource(R.drawable.bg_badge_green);

            if (overdue) {
                h.btnMarkDone.setVisibility(View.VISIBLE);
                h.btnMarkDone.setOnClickListener(v -> listener.onMarkDone(e));
            } else {
                h.btnMarkDone.setVisibility(View.GONE);
            }
        }
    }

    private int daysUntil(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return Integer.MAX_VALUE;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Calendar target = Calendar.getInstance();
            target.setTime(sdf.parse(dateStr));
            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);
            target.set(Calendar.HOUR_OF_DAY, 0);
            target.set(Calendar.MINUTE, 0);
            target.set(Calendar.SECOND, 0);
            target.set(Calendar.MILLISECOND, 0);
            long diff = target.getTimeInMillis() - today.getTimeInMillis();
            return (int) (diff / (1000 * 60 * 60 * 24));
        } catch (Exception ex) {
            return Integer.MAX_VALUE;
        }
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView type, vehicle, sub, badge;
        com.google.android.material.button.MaterialButton btnMarkDone;
        VH(@NonNull View v) {
            super(v);
            type = v.findViewById(R.id.entretien_type);
            vehicle = v.findViewById(R.id.entretien_vehicle);
            sub = v.findViewById(R.id.entretien_sub);
            badge = v.findViewById(R.id.entretien_badge);
            btnMarkDone = v.findViewById(R.id.btn_mark_done);
        }
    }
}
